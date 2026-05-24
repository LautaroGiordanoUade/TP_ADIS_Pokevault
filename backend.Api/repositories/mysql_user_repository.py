import hashlib
from datetime import datetime, timedelta, timezone
from decimal import Decimal
from secrets import token_urlsafe

from db.database import connection, init_db
from models.order import Order, OrderItem
from models.pokemon import Pokemon
from models.user import User
from repositories.mysql_pokemon_repository import _parse_datetime, _parse_json_list


class InsufficientStockError(ValueError):
    pass


def _float(value) -> float:
    if isinstance(value, Decimal):
        return float(value)
    return float(value or 0)


def _token_hash(token: str) -> str:
    return hashlib.sha256(token.encode("utf-8")).hexdigest()


def _user_from_row(row: dict) -> User:
    return User(
        id=row["id"],
        google_sub=row["google_sub"],
        email=row["email"],
        name=row["name"],
        avatar_url=row.get("avatar_url"),
        created_at=_parse_datetime(row.get("created_at")),
        updated_at=_parse_datetime(row.get("updated_at")),
    )


def _pokemon_from_prefixed_row(row: dict) -> Pokemon | None:
    if row.get("card_id") is None:
        return None
    return Pokemon(
        id=row["card_id"],
        external_id=row["card_external_id"],
        name=row["card_name"],
        image=row["card_image"],
        rarity=row.get("card_rarity"),
        price=_float(row.get("card_price")),
        description=row.get("card_description"),
        type=_parse_json_list(row.get("card_type")),
        set_name=row.get("card_set_name"),
        number=row.get("card_number"),
        artist=row.get("card_artist"),
        source=row.get("card_source") or "local",
        raw_json=row.get("card_raw_json"),
        created_at=_parse_datetime(row.get("card_created_at")),
        updated_at=_parse_datetime(row.get("card_updated_at")),
    )


class MySQLUserRepository:
    def __init__(self) -> None:
        self._initialized = False

    def _ensure_initialized(self) -> None:
        if self._initialized:
            return
        init_db()
        self._initialized = True

    async def upsert_google_user(
        self,
        google_sub: str,
        email: str,
        name: str,
        avatar_url: str | None,
    ) -> User:
        self._ensure_initialized()
        with connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(
                    """
                    INSERT INTO users (google_sub, email, name, avatar_url)
                    VALUES (%s, %s, %s, %s)
                    ON DUPLICATE KEY UPDATE
                        email = VALUES(email),
                        name = VALUES(name),
                        avatar_url = VALUES(avatar_url),
                        updated_at = CURRENT_TIMESTAMP
                    """,
                    (google_sub, email, name, avatar_url),
                )
                cursor.execute("SELECT * FROM users WHERE google_sub = %s", (google_sub,))
                row = cursor.fetchone()
        return _user_from_row(row)

    async def create_session(self, user_id: int, ttl_hours: int) -> str:
        self._ensure_initialized()
        token = token_urlsafe(32)
        expires_at = datetime.now(timezone.utc) + timedelta(hours=ttl_hours)
        with connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(
                    """
                    INSERT INTO user_sessions (user_id, token_hash, expires_at)
                    VALUES (%s, %s, %s)
                    """,
                    (user_id, _token_hash(token), expires_at),
                )
        return token

    async def get_user_by_session(self, token: str) -> User | None:
        self._ensure_initialized()
        with connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(
                    """
                    SELECT users.*
                    FROM user_sessions
                    INNER JOIN users ON users.id = user_sessions.user_id
                    WHERE user_sessions.token_hash = %s
                      AND user_sessions.revoked_at IS NULL
                      AND user_sessions.expires_at > CURRENT_TIMESTAMP
                    """,
                    (_token_hash(token),),
                )
                row = cursor.fetchone()
        return _user_from_row(row) if row else None

    async def revoke_session(self, token: str) -> None:
        self._ensure_initialized()
        with connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(
                    """
                    UPDATE user_sessions
                    SET revoked_at = CURRENT_TIMESTAMP
                    WHERE token_hash = %s AND revoked_at IS NULL
                    """,
                    (_token_hash(token),),
                )

    async def list_orders(self, user_id: int) -> list[Order]:
        self._ensure_initialized()
        with connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(
                    """
                    SELECT
                        orders.*,
                        order_statuses.code AS status
                    FROM orders
                    INNER JOIN order_statuses ON order_statuses.id = orders.status_id
                    WHERE orders.user_id = %s
                    ORDER BY orders.created_at DESC
                    """,
                    (user_id,),
                )
                order_rows = cursor.fetchall()

                orders: list[Order] = []
                for order_row in order_rows:
                    cursor.execute(
                        """
                        SELECT
                            order_items.*,
                            pokemon_cards.id AS card_id,
                            pokemon_cards.external_id AS card_external_id,
                            pokemon_cards.name AS card_name,
                            pokemon_cards.image AS card_image,
                            pokemon_cards.rarity AS card_rarity,
                            pokemon_cards.price AS card_price,
                            pokemon_cards.description AS card_description,
                            pokemon_cards.type AS card_type,
                            pokemon_cards.set_name AS card_set_name,
                            pokemon_cards.number AS card_number,
                            pokemon_cards.artist AS card_artist,
                            pokemon_cards.source AS card_source,
                            pokemon_cards.raw_json AS card_raw_json,
                            pokemon_cards.created_at AS card_created_at,
                            pokemon_cards.updated_at AS card_updated_at
                        FROM order_items
                        LEFT JOIN pokemon_cards ON pokemon_cards.id = order_items.pokemon_id
                        WHERE order_items.order_id = %s
                        ORDER BY order_items.id
                        """,
                        (order_row["id"],),
                    )
                    item_rows = cursor.fetchall()
                    orders.append(self._order_from_rows(order_row, item_rows))
        return orders

    async def create_order(
        self,
        user_id: int,
        items: list[tuple[int, int]],
        delivery_address: str,
        payment_method: str,
    ) -> Order:
        self._ensure_initialized()
        with connection() as conn:
            conn.autocommit(False)
            try:
                with conn.cursor() as cursor:
                    grouped_items: dict[int, int] = {}
                    for pokemon_id, quantity in items:
                        grouped_items[pokemon_id] = grouped_items.get(pokemon_id, 0) + quantity

                    pokemon_ids = list(grouped_items.keys())
                    if not pokemon_ids:
                        raise ValueError("Order must include at least one pokemon card")

                    placeholders = ",".join(["%s"] * len(pokemon_ids))
                    cursor.execute(
                        f"""
                        SELECT
                            pokemon_cards.id,
                            pokemon_cards.name,
                            pokemon_cards.price,
                            COALESCE(inventory.quantity, 0) AS quantity
                        FROM pokemon_cards
                        LEFT JOIN inventory ON inventory.pokemon_id = pokemon_cards.id
                        WHERE pokemon_cards.id IN ({placeholders})
                        FOR UPDATE
                        """,
                        pokemon_ids,
                    )
                    stock_rows = {row["id"]: row for row in cursor.fetchall()}
                    missing_ids = [pokemon_id for pokemon_id in pokemon_ids if pokemon_id not in stock_rows]
                    if missing_ids:
                        raise ValueError(f"Pokemon card not found: {missing_ids[0]}")
                    for pokemon_id, quantity in grouped_items.items():
                        available = int(stock_rows[pokemon_id]["quantity"])
                        if available < quantity:
                            card_name = stock_rows[pokemon_id]["name"]
                            raise InsufficientStockError(
                                f"No hay stock suficiente de {card_name}. Disponible: {available}."
                            )

                    prices = {pokemon_id: _float(row["price"]) for pokemon_id, row in stock_rows.items()}
                    subtotal = sum(prices[pokemon_id] * quantity for pokemon_id, quantity in grouped_items.items())
                    shipping = 0.0
                    total = subtotal + shipping
                    cursor.execute(
                        """
                        INSERT INTO orders (
                            user_id, delivery_address, payment_method,
                            status_id, subtotal, shipping, total
                        )
                        VALUES (%s, %s, %s, %s, %s, %s, %s)
                        """,
                        (
                            user_id,
                            delivery_address,
                            payment_method,
                            1,
                            subtotal,
                            shipping,
                            total,
                        ),
                    )
                    order_id = cursor.lastrowid
                    for pokemon_id, quantity in grouped_items.items():
                        cursor.execute(
                            """
                            INSERT INTO order_items (
                                order_id, pokemon_id, quantity, unit_price
                            )
                            VALUES (%s, %s, %s, %s)
                            """,
                            (order_id, pokemon_id, quantity, prices[pokemon_id]),
                        )
                        cursor.execute(
                            """
                            UPDATE inventory
                            SET quantity = quantity - %s
                            WHERE pokemon_id = %s AND quantity >= %s
                            """,
                            (quantity, pokemon_id, quantity),
                        )
                        if cursor.rowcount != 1:
                            raise InsufficientStockError(
                                f"No hay stock suficiente de {stock_rows[pokemon_id]['name']}."
                            )
                conn.commit()
            except Exception:
                conn.rollback()
                raise
            finally:
                conn.autocommit(True)
        orders = await self.list_orders(user_id)
        return next(order for order in orders if order.id == order_id)

    def _order_from_rows(self, order_row: dict, item_rows: list[dict]) -> Order:
        order = Order(
            id=order_row["id"],
            user_id=order_row["user_id"],
            delivery_address=order_row["delivery_address"],
            payment_method=order_row["payment_method"],
            status_id=order_row["status_id"],
            status=order_row["status"],
            subtotal=_float(order_row["subtotal"]),
            shipping=_float(order_row["shipping"]),
            total=_float(order_row["total"]),
            created_at=_parse_datetime(order_row.get("created_at")),
            updated_at=_parse_datetime(order_row.get("updated_at")),
        )
        order.items = [
            OrderItem(
                id=row["id"],
                order_id=row["order_id"],
                pokemon_id=row["pokemon_id"],
                quantity=int(row["quantity"]),
                unit_price=_float(row["unit_price"]),
                card=_pokemon_from_prefixed_row(row),
            )
            for row in item_rows
        ]
        return order


user_repository = MySQLUserRepository()
