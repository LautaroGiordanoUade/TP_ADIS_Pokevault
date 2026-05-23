from abc import ABC, abstractmethod

from models.pokemon import Pokemon, VaultItem


class PokemonRepository(ABC):
    @abstractmethod
    async def find_all(
        self,
        page: int,
        page_size: int,
        name: str | None = None,
    ) -> tuple[list[Pokemon], int]:
        raise NotImplementedError

    @abstractmethod
    async def find_by_id(self, pokemon_id: int) -> Pokemon | None:
        raise NotImplementedError

    @abstractmethod
    async def upsert_many(self, pokemon: list[Pokemon]) -> int:
        raise NotImplementedError

    @abstractmethod
    async def get_vault(self, user_id: int) -> list[VaultItem]:
        raise NotImplementedError

    @abstractmethod
    async def add_to_vault(self, user_id: int, pokemon_id: int) -> None:
        raise NotImplementedError

    @abstractmethod
    async def remove_from_vault(self, user_id: int, pokemon_id: int) -> None:
        raise NotImplementedError
