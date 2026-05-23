from dataclasses import dataclass, field
from datetime import datetime, timezone


@dataclass(slots=True)
class User:
    id: int
    google_sub: str
    email: str
    name: str
    avatar_url: str | None = None
    created_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    updated_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
