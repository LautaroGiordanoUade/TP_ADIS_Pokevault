from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

from schemas.pokemon import PokemonRead


class AddVaultItemRequest(BaseModel):
    pokemon_id: str = Field(alias="pokemonId")

    model_config = ConfigDict(populate_by_name=True)


class VaultItemRead(BaseModel):
    id: str
    user_id: str = Field(alias="userId")
    pokemon_id: str = Field(alias="pokemonId")
    added_at: datetime = Field(alias="addedAt")
    card: PokemonRead | None = None

    model_config = ConfigDict(from_attributes=True, populate_by_name=True)
