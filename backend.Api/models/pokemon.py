from dataclasses import dataclass, field
from datetime import datetime, timezone


@dataclass(slots=True)
class Pokemon:
    id: int | None
    external_id: str
    name: str
    image: str
    price: float
    rarity: str | None = None
    description: str | None = None
    type: list[str] | None = None
    set_name: str | None = None
    number: str | None = None
    artist: str | None = None
    source: str = "local"
    raw_json: str | None = None
    created_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    updated_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))


@dataclass(slots=True)
class VaultItem:
    id: int
    user_id: int
    pokemon_id: int
    added_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    card: Pokemon | None = None
