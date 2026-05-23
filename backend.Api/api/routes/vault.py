from fastapi import APIRouter, Depends

from api.dependencies import current_user
from models.user import User
from repositories.mysql_pokemon_repository import pokemon_repository
from schemas.vault import AddVaultItemRequest, VaultItemRead
from services.pokemon_service import PokemonService

router = APIRouter(prefix="/vault", tags=["vault"])
service = PokemonService(repository=pokemon_repository)


@router.get("/me", response_model=list[VaultItemRead])
async def get_my_vault(user: User = Depends(current_user)) -> list[VaultItemRead]:
    return await service.get_vault(user.id)


@router.post("/me/items")
async def add_my_vault_item(
    payload: AddVaultItemRequest,
    user: User = Depends(current_user),
) -> dict[str, bool]:
    await service.add_to_vault(user_id=user.id, pokemon_id=payload.pokemon_id)
    return {"success": True}


@router.delete("/me/items/{pokemon_id}")
async def remove_my_vault_item(
    pokemon_id: int,
    user: User = Depends(current_user),
) -> dict[str, bool]:
    await service.remove_from_vault(user_id=user.id, pokemon_id=pokemon_id)
    return {"success": True}
