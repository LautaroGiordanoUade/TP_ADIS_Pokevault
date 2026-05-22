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
                    id VARCHAR(128) PRIMARY KEY,
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
                    INDEX idx_pokemon_cards_name (name)
                )
                """
            )
            cursor.execute(
                """
                CREATE TABLE IF NOT EXISTS vault_items (
                    id VARCHAR(255) PRIMARY KEY,
                    user_id VARCHAR(128) NOT NULL,
                    pokemon_id VARCHAR(128) NOT NULL,
                    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_vault_items_user_id (user_id),
                    CONSTRAINT fk_vault_items_pokemon
                        FOREIGN KEY (pokemon_id)
                        REFERENCES pokemon_cards(id)
                        ON DELETE CASCADE
                )
                """
            )
