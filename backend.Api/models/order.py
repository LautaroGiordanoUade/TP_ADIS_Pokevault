from dataclasses import dataclass, field
from datetime import datetime, timezone

from models.pokemon import Pokemon


@dataclass(slots=True)
class OrderItem:
    id: int
    order_id: int
    pokemon_id: int
    quantity: int
    unit_price: float
    card: Pokemon | None = None


@dataclass(slots=True)
class Order:
    id: int
    user_id: int
    delivery_address: str
    payment_method: str
    status: str
    subtotal: float
    shipping: float
    total: float
    created_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    updated_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    items: list[OrderItem] = field(default_factory=list)
