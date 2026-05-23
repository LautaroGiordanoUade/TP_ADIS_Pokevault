from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class PokemonBase(BaseModel):
    external_id: str = Field(alias="externalId")
    name: str
    image: str
    rarity: str | None = None
    price: float = Field(ge=0)
    description: str | None = None
    type: list[str] | None = None
    set_name: str | None = Field(default=None, alias="setName")
    number: str | None = None
    artist: str | None = None
    source: str = "local"

    model_config = ConfigDict(populate_by_name=True)


class PokemonCreate(PokemonBase):
    pass


class PokemonRead(PokemonBase):
    id: int
    created_at: datetime = Field(alias="createdAt")
    updated_at: datetime = Field(alias="updatedAt")

    model_config = ConfigDict(from_attributes=True, populate_by_name=True)


class SyncPokemonResponse(BaseModel):
    message: str
    count: int


class PokemonPage(BaseModel):
    items: list[PokemonRead]
    total: int
    page: int
    page_size: int = Field(alias="pageSize")
    total_pages: int = Field(alias="totalPages")

    model_config = ConfigDict(populate_by_name=True)
