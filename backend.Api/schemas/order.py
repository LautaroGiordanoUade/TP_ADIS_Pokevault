from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

from schemas.pokemon import PokemonRead


class CreateOrderItemRequest(BaseModel):
    pokemon_id: int = Field(alias="pokemonId")
    quantity: int = Field(ge=1, le=99)

    model_config = ConfigDict(populate_by_name=True)


class CreateOrderRequest(BaseModel):
    items: list[CreateOrderItemRequest] = Field(min_length=1)
    delivery_address: str = Field(alias="deliveryAddress", min_length=1)
    payment_method: str = Field(default="Google Pay", alias="paymentMethod")

    model_config = ConfigDict(populate_by_name=True)


class OrderItemRead(BaseModel):
    id: int
    pokemon_id: int = Field(alias="pokemonId")
    quantity: int
    unit_price: float = Field(alias="unitPrice")
    card: PokemonRead | None = None

    model_config = ConfigDict(from_attributes=True, populate_by_name=True)


class OrderRead(BaseModel):
    id: int
    user_id: int = Field(alias="userId")
    delivery_address: str = Field(alias="deliveryAddress")
    payment_method: str = Field(alias="paymentMethod")
    status: str
    subtotal: float
    shipping: float
    total: float
    created_at: datetime = Field(alias="createdAt")
    updated_at: datetime = Field(alias="updatedAt")
    items: list[OrderItemRead]

    model_config = ConfigDict(from_attributes=True, populate_by_name=True)
