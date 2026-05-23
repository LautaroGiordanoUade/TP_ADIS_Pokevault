import json
from datetime import datetime, timezone
from decimal import Decimal

from db.database import connection, init_db
from models.pokemon import Pokemon, VaultItem
from repositories.base import PokemonRepository

DEFAULT_INVENTORY_QUANTITY = 10


def _parse_datetime(value) -> datetime:
    if isinstance(value, datetime):
        return value.replace(tzinfo=timezone.utc) if value.tzinfo is None else value
    if not value:
        return datetime.now(timezone.utc)
    normalized = str(value).replace("Z", "+00:00")
    try:
        return datetime.fromisoformat(normalized)
    except ValueError:
        return datetime.now(timezone.utc)


def _parse_json_list(value) -> list[str] | None:
    if value is None:
        return None
    if isinstance(value, list):
        return value
    try:
        loaded = json.loads(value)
    except (TypeError, json.JSONDecodeError):
        return None
    return loaded if isinstance(loaded, list) else None


def _pokemon_from_row(row: dict) -> Pokemon:
    price = row.get("price") or 0
    if isinstance(price, Decimal):
        price = float(price)

    return Pokemon(
        id=row.get("id"),
        external_id=row["external_id"],
        name=row["name"],
        image=row["image"],
        rarity=row.get("rarity"),
        price=float(price),
        description=row.get("description"),
        type=_parse_json_list(row.get("type")),
        set_name=row.get("set_name"),
        number=row.get("number"),
        artist=row.get("artist"),
        source=row.get("source") or "local",
        raw_json=row.get("raw_json"),
        created_at=_parse_datetime(row.get("created_at")),
        updated_at=_parse_datetime(row.get("updated_at")),
    )


class MySQLPokemonRepository(PokemonRepository):
    def __init__(self) -> None:
        self._initialized = False

    def _ensure_initialized(self) -> None:
        if self._initialized:
            return
        init_db()
        self._initialized = True

    async def find_all(
        self,
        page: int,
        page_size: int,
        name: str | None = None,
    ) -> tuple[list[Pokemon], int]:
        self._ensure_initialized()
        offset = (page - 1) * page_size
        params: list[object] = []
        where = ""

        if name:
            where = "WHERE LOWER(name) LIKE LOWER(%s)"
            params.append(f"%{name}%")

        with connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(
                    f"SELECT COUNT(*) AS total FROM pokemon_cards {where}",
                    params,
                )
                total = int(cursor.fetchone()["total"])
                cursor.execute(
                    f"""
                    SELECT *
                    FROM pokemon_cards
                    {where}
                    ORDER BY name, id
                    LIMIT %s OFFSET %s
                    """,
                    [*params, page_size, offset],
                )
                rows = cursor.fetchall()

        return [_pokemon_from_row(row) for row in rows], total

    async def find_by_id(self, pokemon_id: int) -> Pokemon | None:
        self._ensure_initialized()
        with connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(
                    "SELECT * FROM pokemon_cards WHERE id = %s",
                    (pokemon_id,),
                )
                row = cursor.fetchone()
        return _pokemon_from_row(row) if row else None

    async def upsert_many(self, pokemon: list[Pokemon]) -> int:
        self._ensure_initialized()
        with connection() as conn:
            with conn.cursor() as cursor:
                for card in pokemon:
                    cursor.execute(
                        """
                        INSERT INTO pokemon_cards (
                            external_id, name, image, rarity, price, description,
                            type, set_name, number, artist, source, raw_json
                        )
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                        ON DUPLICATE KEY UPDATE
                            name = VALUES(name),
                            image = VALUES(image),
                            rarity = VALUES(rarity),
                            price = VALUES(price),
                            description = VALUES(description),
                            type = VALUES(type),
                            set_name = VALUES(set_name),
                            number = VALUES(number),
                            artist = VALUES(artist),
                            source = VALUES(source),
                            raw_json = VALUES(raw_json),
                            updated_at = CURRENT_TIMESTAMP
                        """,
                        (
                            card.external_id,
                            card.name,
                            card.image,
                            card.rarity,
                            card.price,
                            card.description,
                            json.dumps(card.type) if card.type is not None else None,
                            card.set_name,
                            card.number,
                            card.artist,
                            card.source,
                            card.raw_json,
                        ),
                    )
                    cursor.execute(
                        "SELECT id FROM pokemon_cards WHERE external_id = %s",
                        (card.external_id,),
                    )
                    pokemon_id = cursor.fetchone()["id"]
                    cursor.execute(
                        """
                        INSERT INTO inventory (pokemon_id, quantity)
                        VALUES (%s, %s)
                        ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)
                        """,
                        (pokemon_id, DEFAULT_INVENTORY_QUANTITY),
                    )
        return len(pokemon)

    async def get_vault(self, user_id: int) -> list[VaultItem]:
        self._ensure_initialized()
        with connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(
                    """
                    SELECT
                        vault_items.id AS vault_id,
                        vault_items.user_id,
                        vault_items.pokemon_id,
                        vault_items.added_at,
                        pokemon_cards.*
                    FROM vault_items
                    LEFT JOIN pokemon_cards ON vault_items.pokemon_id = pokemon_cards.id
                    WHERE vault_items.user_id = %s
                    ORDER BY vault_items.added_at DESC
                    """,
                    (user_id,),
                )
                rows = cursor.fetchall()

        items: list[VaultItem] = []
        for row in rows:
            card = _pokemon_from_row(row) if row.get("id") is not None else None
            items.append(
                VaultItem(
                    id=row["vault_id"],
                    user_id=row["user_id"],
                    pokemon_id=row["pokemon_id"],
                    added_at=_parse_datetime(row["added_at"]),
                    card=card,
                )
            )
        return items

    async def add_to_vault(self, user_id: int, pokemon_id: int) -> None:
        self._ensure_initialized()
        with connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(
                    """
                    INSERT IGNORE INTO vault_items (user_id, pokemon_id)
                    VALUES (%s, %s)
                    """,
                    (user_id, pokemon_id),
                )

    async def remove_from_vault(self, user_id: int, pokemon_id: int) -> None:
        self._ensure_initialized()
        with connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(
                    "DELETE FROM vault_items WHERE user_id = %s AND pokemon_id = %s",
                    (user_id, pokemon_id),
                )


pokemon_repository = MySQLPokemonRepository()
