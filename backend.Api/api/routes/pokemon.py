from fastapi import APIRouter, HTTPException, Query, status

from repositories.mysql_pokemon_repository import pokemon_repository
from schemas.pokemon import PokemonCreate, PokemonPage, PokemonRead, SyncPokemonResponse
from schemas.vault import AddVaultItemRequest, VaultItemRead
from services.pokemon_service import PokemonService

router = APIRouter(prefix="/pokemon", tags=["pokemon"])
service = PokemonService(repository=pokemon_repository)


@router.get("", response_model=PokemonPage)
async def list_pokemon(
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=25, ge=1, le=100, alias="pageSize"),
    name: str | None = Query(default=None, min_length=1),
) -> PokemonPage:
    return await service.get_all_pokemon(
        page=page,
        page_size=page_size,
        name=name,
    )


@router.get("/search", response_model=PokemonPage)
async def search_pokemon(
    name: str = Query(min_length=1),
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=25, ge=1, le=100, alias="pageSize"),
) -> PokemonPage:
    return await service.get_all_pokemon(
        page=page,
        page_size=page_size,
        name=name,
    )


@router.get("/{pokemon_id}", response_model=PokemonRead)
async def get_pokemon(pokemon_id: int) -> PokemonRead:
    pokemon = await service.get_pokemon_by_id(pokemon_id)
    if pokemon is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Pokemon not found",
        )
    return pokemon


@router.post("/sync", response_model=SyncPokemonResponse)
async def sync_pokemon(cards: list[PokemonCreate]) -> SyncPokemonResponse:
    count = await service.sync_pokemon(cards)
    return SyncPokemonResponse(message="Sync successful", count=count)


@router.get("/vault/{user_id}", response_model=list[VaultItemRead])
async def get_vault(user_id: int) -> list[VaultItemRead]:
    return await service.get_vault(user_id)


@router.post("/vault/{user_id}/add")
async def add_to_vault(user_id: int, payload: AddVaultItemRequest) -> dict[str, bool]:
    await service.add_to_vault(user_id=user_id, pokemon_id=payload.pokemon_id)
    return {"success": True}


@router.delete("/vault/{user_id}/remove/{pokemon_id}")
async def remove_from_vault(user_id: int, pokemon_id: int) -> dict[str, bool]:
    await service.remove_from_vault(user_id=user_id, pokemon_id=pokemon_id)
    return {"success": True}
