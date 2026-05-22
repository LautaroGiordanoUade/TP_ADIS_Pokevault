from models.pokemon import Pokemon
from repositories.base import PokemonRepository
from schemas.pokemon import PokemonCreate, PokemonPage, PokemonRead
from schemas.vault import VaultItemRead


class PokemonService:
    def __init__(self, repository: PokemonRepository) -> None:
        self.repository = repository

    async def get_all_pokemon(
        self,
        page: int,
        page_size: int,
        name: str | None = None,
    ) -> PokemonPage:
        pokemon, total = await self.repository.find_all(
            page=page,
            page_size=page_size,
            name=name,
        )
        total_pages = (total + page_size - 1) // page_size if total else 0
        return PokemonPage(
            items=[PokemonRead.model_validate(card) for card in pokemon],
            total=total,
            page=page,
            pageSize=page_size,
            totalPages=total_pages,
        )

    async def get_pokemon_by_id(self, pokemon_id: str) -> PokemonRead | None:
        pokemon = await self.repository.find_by_id(pokemon_id)
        return PokemonRead.model_validate(pokemon) if pokemon else None

    async def sync_pokemon(self, cards: list[PokemonCreate]) -> int:
        domain_cards = [
            Pokemon(
                id=card.id,
                name=card.name,
                image=card.image,
                rarity=card.rarity,
                price=card.price,
                description=card.description,
                type=card.type,
                set_name=card.set_name,
                number=card.number,
                artist=card.artist,
                source=card.source,
            )
            for card in cards
        ]
        return await self.repository.upsert_many(domain_cards)

    async def get_vault(self, user_id: str) -> list[VaultItemRead]:
        vault = await self.repository.get_vault(user_id)
        return [VaultItemRead.model_validate(item) for item in vault]

    async def add_to_vault(self, user_id: str, pokemon_id: str) -> None:
        await self.repository.add_to_vault(user_id=user_id, pokemon_id=pokemon_id)

    async def remove_from_vault(self, user_id: str, pokemon_id: str) -> None:
        await self.repository.remove_from_vault(user_id=user_id, pokemon_id=pokemon_id)
