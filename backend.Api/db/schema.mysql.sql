CREATE DATABASE IF NOT EXISTS pokevault
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE pokevault;

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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_pokemon_cards_name (name)
);

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
);
