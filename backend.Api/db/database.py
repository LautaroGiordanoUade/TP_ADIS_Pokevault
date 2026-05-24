from collections.abc import Iterator
from contextlib import contextmanager

import pymysql
from pymysql.connections import Connection
from pymysql.cursors import DictCursor

from core.config import settings


def _server_connection() -> Connection:
    return pymysql.connect(
        host=settings.mysql_host,
        port=settings.mysql_port,
        user=settings.mysql_user,
        password=settings.mysql_password_value,
        charset=settings.mysql_charset,
        cursorclass=DictCursor,
        autocommit=True,
    )


def get_connection() -> Connection:
    return pymysql.connect(
        host=settings.mysql_host,
        port=settings.mysql_port,
        user=settings.mysql_user,
        password=settings.mysql_password_value,
        database=settings.mysql_database,
        charset=settings.mysql_charset,
        cursorclass=DictCursor,
        autocommit=True,
    )


@contextmanager
def connection() -> Iterator[Connection]:
    conn = get_connection()
    try:
        yield conn
    finally:
        conn.close()


def recreate_db() -> None:
    with _server_connection() as conn:
        with conn.cursor() as cursor:
            cursor.execute(f"DROP DATABASE IF EXISTS `{settings.mysql_database}`")
    init_db()


def init_db() -> None:
    with _server_connection() as conn:
        with conn.cursor() as cursor:
            cursor.execute(
                f"""
                CREATE DATABASE IF NOT EXISTS `{settings.mysql_database}`
                CHARACTER SET {settings.mysql_charset}
                COLLATE {settings.mysql_charset}_unicode_ci
                """
            )

    with connection() as conn:
        with conn.cursor() as cursor:
            cursor.execute(
                """
                CREATE TABLE IF NOT EXISTS pokemon_cards (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    external_id VARCHAR(128) NOT NULL UNIQUE,
                    name VARCHAR(255) NOT NULL,
                    image TEXT NOT NULL,
                    rarity VARCHAR(128),
                    price DECIMAL(10, 2) NOT NULL DEFAULT 0,
                    description TEXT,
                    type JSON,
                    set_name VARCHAR(255),
                    number VARCHAR(64),
                    artist VARCHAR(255),
                    source VARCHAR(64) NOT NULL DEFAULT 'local',
                    raw_json JSON,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_pokemon_cards_name (name),
                    INDEX idx_pokemon_cards_external_id (external_id)
                )
                """
            )
            cursor.execute(
                """
                CREATE TABLE IF NOT EXISTS inventory (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    pokemon_id INT NOT NULL UNIQUE,
                    quantity INT NOT NULL DEFAULT 0,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,
                    CONSTRAINT fk_inventory_pokemon
                        FOREIGN KEY (pokemon_id)
                        REFERENCES pokemon_cards(id)
                        ON DELETE CASCADE
                )
                """
            )
            cursor.execute(
                """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    google_sub VARCHAR(255) NOT NULL UNIQUE,
                    email VARCHAR(255) NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    avatar_url TEXT,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_users_email (email)
                )
                """
            )
            cursor.execute(
                """
                CREATE TABLE IF NOT EXISTS user_sessions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    token_hash CHAR(64) NOT NULL UNIQUE,
                    expires_at TIMESTAMP NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    revoked_at TIMESTAMP NULL,
                    INDEX idx_user_sessions_user_id (user_id),
                    INDEX idx_user_sessions_token_hash (token_hash),
                    CONSTRAINT fk_user_sessions_user
                        FOREIGN KEY (user_id)
                        REFERENCES users(id)
                        ON DELETE CASCADE
                )
                """
            )
            cursor.execute(
                """
                CREATE TABLE IF NOT EXISTS vault_items (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    pokemon_id INT NOT NULL,
                    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_vault_items_user_id (user_id),
                    UNIQUE KEY uq_vault_items_user_pokemon (user_id, pokemon_id),
                    CONSTRAINT fk_vault_items_user
                        FOREIGN KEY (user_id)
                        REFERENCES users(id)
                        ON DELETE CASCADE,
                    CONSTRAINT fk_vault_items_pokemon
                        FOREIGN KEY (pokemon_id)
                        REFERENCES pokemon_cards(id)
                        ON DELETE CASCADE
                )
                """
            )
            cursor.execute(
                """
                CREATE TABLE IF NOT EXISTS order_statuses (
                    id INT PRIMARY KEY,
                    code VARCHAR(32) NOT NULL UNIQUE,
                    name VARCHAR(64) NOT NULL
                )
                """
            )
            cursor.execute(
                """
                INSERT INTO order_statuses (id, code, name)
                VALUES
                    (1, 'ready_for_pickup', 'Ready for pickup'),
                    (2, 'delivered', 'Delivered')
                ON DUPLICATE KEY UPDATE
                    code = VALUES(code),
                    name = VALUES(name)
                """
            )
            cursor.execute(
                """
                CREATE TABLE IF NOT EXISTS orders (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    delivery_address TEXT NOT NULL,
                    payment_method VARCHAR(64) NOT NULL DEFAULT 'Google Pay',
                    status_id INT NOT NULL DEFAULT 1,
                    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0,
                    shipping DECIMAL(10, 2) NOT NULL DEFAULT 0,
                    total DECIMAL(10, 2) NOT NULL DEFAULT 0,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_orders_user_id (user_id),
                    CONSTRAINT fk_orders_user
                        FOREIGN KEY (user_id)
                        REFERENCES users(id)
                        ON DELETE CASCADE,
                    CONSTRAINT fk_orders_status
                        FOREIGN KEY (status_id)
                        REFERENCES order_statuses(id)
                )
                """
            )
            _migrate_order_statuses(cursor)
            cursor.execute(
                """
                CREATE TABLE IF NOT EXISTS order_items (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    order_id INT NOT NULL,
                    pokemon_id INT NOT NULL,
                    quantity INT NOT NULL DEFAULT 1,
                    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
                    CONSTRAINT fk_order_items_order
                        FOREIGN KEY (order_id)
                        REFERENCES orders(id)
                        ON DELETE CASCADE,
                    CONSTRAINT fk_order_items_pokemon
                        FOREIGN KEY (pokemon_id)
                        REFERENCES pokemon_cards(id)
                        ON DELETE CASCADE
                )
                """
            )


def _column_exists(cursor: DictCursor, table_name: str, column_name: str) -> bool:
    cursor.execute(
        """
        SELECT COUNT(*) AS total
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = %s
          AND TABLE_NAME = %s
          AND COLUMN_NAME = %s
        """,
        (settings.mysql_database, table_name, column_name),
    )
    return int(cursor.fetchone()["total"]) > 0


def _constraint_exists(cursor: DictCursor, table_name: str, constraint_name: str) -> bool:
    cursor.execute(
        """
        SELECT COUNT(*) AS total
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE TABLE_SCHEMA = %s
          AND TABLE_NAME = %s
          AND CONSTRAINT_NAME = %s
        """,
        (settings.mysql_database, table_name, constraint_name),
    )
    return int(cursor.fetchone()["total"]) > 0


def _migrate_order_statuses(cursor: DictCursor) -> None:
    if not _column_exists(cursor, "orders", "status_id"):
        cursor.execute("ALTER TABLE orders ADD COLUMN status_id INT NOT NULL DEFAULT 1 AFTER payment_method")

    if _column_exists(cursor, "orders", "status"):
        cursor.execute(
            """
            UPDATE orders
            LEFT JOIN order_statuses ON order_statuses.code = orders.status
            SET orders.status_id = CASE
                WHEN orders.status IN ('delivered', 'deliver') THEN 2
                ELSE COALESCE(order_statuses.id, 1)
            END
            WHERE orders.status_id IS NULL OR orders.status_id = 1
            """
        )
        cursor.execute("ALTER TABLE orders DROP COLUMN status")

    if not _constraint_exists(cursor, "orders", "fk_orders_status"):
        cursor.execute(
            """
            ALTER TABLE orders
            ADD CONSTRAINT fk_orders_status
            FOREIGN KEY (status_id)
            REFERENCES order_statuses(id)
            """
        )
