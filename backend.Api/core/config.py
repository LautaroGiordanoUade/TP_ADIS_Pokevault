from pathlib import Path

from cryptography.fernet import Fernet
from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "PokeVault API"
    api_v1_prefix: str = "/api"
    backend_cors_origins: str = Field(
        default="http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173"
    )
    environment: str = "development"
    mysql_host: str = "127.0.0.1"
    mysql_port: int = 3306
    mysql_user: str = "root"
    mysql_password: str | None = None
    mysql_password_file: str = "db/mysql_password.enc"
    mysql_password_key: str | None = None
    mysql_password_key_file: str = "db/mysql_password.key"
    mysql_database: str = "pokevault"
    mysql_charset: str = "utf8mb4"
    mysql_ssl_mode: str = "DISABLED"
    tcggo_api_base_url: str = "https://cardmarket-api-tcg.p.rapidapi.com"
    tcggo_api_key: str | None = None
    tcggo_game: str = "pokemon"
    pokemon_tcg_api_base_url: str = "https://api.pokemontcg.io/v2"
    pokemon_tcg_api_key: str | None = None
    google_web_client_id: str | None = None
    session_token_ttl_hours: int = 720

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    @property
    def cors_origins(self) -> list[str]:
        return [
            origin.strip()
            for origin in self.backend_cors_origins.split(",")
            if origin.strip()
        ]

    @property
    def mysql_password_value(self) -> str:
        if self.mysql_password:
            return self.mysql_password

        encrypted_path = Path(self.mysql_password_file)
        if not encrypted_path.exists():
            return ""

        key = self.mysql_password_key
        if not key:
            key_path = Path(self.mysql_password_key_file)
            if key_path.exists():
                key = key_path.read_text(encoding="utf-8").strip()

        if not key:
            raise ValueError(
                "Missing MySQL password key. Set MYSQL_PASSWORD_KEY or "
                "MYSQL_PASSWORD_KEY_FILE."
            )

        encrypted_password = encrypted_path.read_bytes()
        return Fernet(key.encode("utf-8")).decrypt(encrypted_password).decode("utf-8")


settings = Settings()
