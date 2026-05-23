import json
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT_DIR))

from core.config import settings  # noqa: E402
from db.database import connection, recreate_db  # noqa: E402
from models.pokemon import Pokemon  # noqa: E402
from repositories.mysql_pokemon_repository import MySQLPokemonRepository  # noqa: E402


POKE_CARDS_FILE = Path(__file__).with_name("poke_cards.txt")

BASE_SET_CARDS = [
    (1, "Alakazam"), (2, "Blastoise"), (3, "Chansey"), (4, "Charizard"),
    (5, "Clefairy"), (6, "Gyarados"), (7, "Hitmonchan"), (8, "Machamp"),
    (9, "Magneton"), (10, "Mewtwo"), (11, "Nidoking"), (12, "Ninetales"),
    (13, "Poliwrath"), (14, "Raichu"), (15, "Venusaur"), (16, "Zapdos"),
    (17, "Beedrill"), (18, "Dragonair"), (19, "Dugtrio"), (20, "Electabuzz"),
    (21, "Electrode"), (22, "Pidgeotto"), (23, "Arcanine"), (24, "Charmeleon"),
    (25, "Dewgong"), (26, "Dratini"), (27, "Farfetch'd"), (28, "Growlithe"),
    (29, "Haunter"), (30, "Ivysaur"), (31, "Jynx"), (32, "Kadabra"),
    (33, "Kakuna"), (34, "Machoke"), (35, "Magikarp"), (36, "Magmar"),
    (37, "Nidorino"), (38, "Poliwhirl"), (39, "Porygon"), (40, "Raticate"),
    (41, "Seel"), (42, "Wartortle"), (43, "Abra"), (44, "Bulbasaur"),
    (45, "Caterpie"), (46, "Charmander"), (47, "Diglett"), (48, "Doduo"),
    (49, "Drowzee"), (50, "Gastly"), (51, "Koffing"), (52, "Machop"),
    (53, "Magnemite"), (54, "Metapod"), (55, "Nidoran Male"), (56, "Onix"),
    (57, "Pidgey"), (58, "Pikachu"), (59, "Poliwag"), (60, "Ponyta"),
    (61, "Rattata"), (62, "Sandshrew"), (63, "Squirtle"), (64, "Starmie"),
    (65, "Staryu"), (66, "Tangela"), (67, "Voltorb"), (68, "Vulpix"),
    (69, "Weedle"),
]

JUNGLE_SET_CARDS = [
    (1, "Clefable"), (2, "Electrode"), (3, "Flareon"), (4, "Jolteon"),
    (5, "Kangaskhan"), (6, "Mr. Mime"), (7, "Nidoqueen"), (8, "Pidgeot"),
    (9, "Pinsir"), (10, "Scyther"), (11, "Snorlax"), (12, "Vaporeon"),
    (13, "Venomoth"), (14, "Victreebel"), (15, "Vileplume"), (16, "Wigglytuff"),
    (17, "Clefable"), (18, "Electrode"), (19, "Flareon"), (20, "Jolteon"),
    (21, "Kangaskhan"), (22, "Mr. Mime"), (23, "Nidoqueen"), (24, "Pidgeot"),
    (25, "Pinsir"), (26, "Scyther"), (27, "Snorlax"), (28, "Vaporeon"),
    (29, "Venomoth"), (30, "Victreebel"), (31, "Vileplume"), (32, "Wigglytuff"),
    (33, "Butterfree"), (34, "Dodrio"), (35, "Exeggutor"), (36, "Fearow"),
    (37, "Gloom"), (38, "Lickitung"), (39, "Marowak"), (40, "Nidorina"),
    (41, "Parasect"), (42, "Persian"), (43, "Primeape"), (44, "Rapidash"),
    (45, "Rhydon"), (46, "Seaking"), (47, "Tauros"), (48, "Weepinbell"),
    (49, "Bellsprout"), (50, "Cubone"), (51, "Eevee"), (52, "Exeggcute"),
    (53, "Goldeen"), (54, "Jigglypuff"), (55, "Mankey"), (56, "Meowth"),
]

TYPE_CYCLE = [
    ["Grass"],
    ["Fire"],
    ["Water"],
    ["Lightning"],
    ["Psychic"],
    ["Fighting"],
    ["Colorless"],
    ["Darkness"],
    ["Metal"],
]

RARITY_CYCLE = [
    "Common",
    "Uncommon",
    "Rare",
    "Rare Holo",
    "Rare Ultra",
]

LOCAL_SEED_SOURCES = ("local_fallback", "local_fixed_seed")


def _get_json(url: str, headers: dict[str, str] | None = None) -> dict:
    request_headers = {"User-Agent": "PokeVaultSeeder/1.0"}
    request_headers.update(headers or {})
    request = urllib.request.Request(url, headers=request_headers)
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.loads(response.read().decode("utf-8"))


def _extract_price(card: dict) -> float:
    tcgplayer_prices = card.get("tcgplayer", {}).get("prices", {})
    for price_group in tcgplayer_prices.values():
        if isinstance(price_group, dict):
            for key in ("market", "mid", "low"):
                value = price_group.get(key)
                if isinstance(value, int | float):
                    return float(value)

    for key in ("price", "marketPrice", "market_price"):
        value = card.get(key)
        if isinstance(value, int | float):
            return float(value)
    return 0.0


def _map_pokemon_tcg_card(card: dict, source: str = "pokemon_tcg_api") -> Pokemon:
    images = card.get("images") or {}
    set_data = card.get("set") or {}
    description = None
    if isinstance(card.get("flavorText"), str):
        description = card["flavorText"]
    elif isinstance(card.get("rules"), list):
        description = " ".join(card["rules"])

    return Pokemon(
        id=None,
        external_id=card["id"],
        name=card["name"],
        image=images.get("large") or images.get("small") or "",
        rarity=card.get("rarity"),
        price=_extract_price(card),
        description=description,
        type=card.get("types"),
        set_name=set_data.get("name"),
        number=card.get("number"),
        artist=card.get("artist"),
        source=source,
        raw_json=json.dumps(card, ensure_ascii=False),
    )


def _map_tcggo_card(card: dict) -> Pokemon:
    raw_type = card.get("types") or card.get("type")
    types = raw_type if isinstance(raw_type, list) else [raw_type] if raw_type else None

    return Pokemon(
        id=None,
        external_id=str(card.get("id") or card.get("cardId") or card.get("productId")),
        name=str(card.get("name") or card.get("cardName")),
        image=str(card.get("image") or card.get("imageUrl") or card.get("image_url") or ""),
        rarity=card.get("rarity"),
        price=_extract_price(card),
        description=card.get("description") or card.get("flavorText"),
        type=types,
        set_name=card.get("setName") or card.get("expansion") or card.get("set"),
        number=str(card.get("number")) if card.get("number") is not None else None,
        artist=card.get("artist"),
        source="tcggo",
        raw_json=json.dumps(card),
    )


def fetch_from_tcggo(limit: int) -> list[Pokemon]:
    if not settings.tcggo_api_key:
        return []

    query = urllib.parse.urlencode({"limit": limit})
    url = f"{settings.tcggo_api_base_url.rstrip('/')}/{settings.tcggo_game}/cards?{query}"
    payload = _get_json(
        url,
        headers={
            "X-RapidAPI-Key": settings.tcggo_api_key,
            "X-RapidAPI-Host": urllib.parse.urlparse(settings.tcggo_api_base_url).netloc,
        },
    )
    rows = payload.get("data") if isinstance(payload, dict) else payload
    if not isinstance(rows, list):
        return []
    return [_map_tcggo_card(card) for card in rows[:limit] if card.get("id")]


def fetch_from_pokemon_tcg(limit: int) -> list[Pokemon]:
    query = urllib.parse.urlencode(
        {
            "page": 1,
            "pageSize": limit,
            "q": "supertype:Pokémon",
            "orderBy": "name",
        }
    )
    url = f"{settings.pokemon_tcg_api_base_url.rstrip('/')}/cards?{query}"
    headers = {}
    if settings.pokemon_tcg_api_key:
        headers["X-Api-Key"] = settings.pokemon_tcg_api_key
    payload = _get_json(url, headers=headers)
    return [_map_pokemon_tcg_card(card) for card in payload.get("data", [])]


def load_from_poke_cards_file(path: Path = POKE_CARDS_FILE) -> list[Pokemon]:
    if not path.exists():
        return []

    payload = json.loads(path.read_text(encoding="utf-8"))
    rows = payload.get("data") if isinstance(payload, dict) else payload
    if not isinstance(rows, list):
        raise ValueError(f"{path} must contain a list or a JSON object with a data list")

    return [
        _map_pokemon_tcg_card(card, source="poke_cards_file")
        for card in rows
        if isinstance(card, dict) and card.get("id") and card.get("name")
    ]


def fallback_cards() -> list[Pokemon]:
    cards = []
    card_specs = [
        ("base1", number, name)
        for number, name in BASE_SET_CARDS
    ] + [
        ("jungle", number, name)
        for number, name in JUNGLE_SET_CARDS
    ]

    for index, (set_id, card_number, name) in enumerate(card_specs, start=1):
        cards.append(
            Pokemon(
                id=None,
                external_id=f"{set_id}-{card_number}",
                name=name,
                image=(
                    f"https://images.pokemontcg.io/{set_id}/{card_number}_hires.png"
                ),
                rarity=RARITY_CYCLE[index % len(RARITY_CYCLE)],
                price=round(0.75 + (index % 37) * 0.42, 2),
                description=f"Local seed TCG card for {name}.",
                type=TYPE_CYCLE[index % len(TYPE_CYCLE)],
                set_name="Base Set" if set_id == "base1" else "Jungle",
                number=str(card_number),
                source="local_fixed_seed",
            )
        )
    return cards


def remove_old_local_seed() -> None:
    return
    placeholders = ", ".join(["%s"] * len(LOCAL_SEED_SOURCES))
    with connection() as conn:
        with conn.cursor() as cursor:
            cursor.execute(
                f"""
                DELETE vault_items
                FROM vault_items
                INNER JOIN pokemon_cards ON pokemon_cards.id = vault_items.pokemon_id
                WHERE pokemon_cards.source IN ({placeholders})
                """,
                LOCAL_SEED_SOURCES,
            )
            cursor.execute(
                f"DELETE FROM pokemon_cards WHERE source IN ({placeholders})",
                LOCAL_SEED_SOURCES,
            )


async def main() -> None:
    recreate_db()
    repository = MySQLPokemonRepository()
    remove_old_local_seed()

    cards = load_from_poke_cards_file()
    if cards:
        print(f"Loaded {len(cards)} pokemon cards from {POKE_CARDS_FILE}")

    if not cards:
        try:
            cards = fetch_from_tcggo(limit=125)
        except (urllib.error.URLError, TimeoutError, ValueError) as error:
            print(f"TCGGO import skipped: {error}")

    if not cards:
        try:
            cards = fetch_from_pokemon_tcg(limit=125)
        except (urllib.error.URLError, TimeoutError, ValueError) as error:
            print(f"Pokemon TCG API import skipped: {error}")

    if not cards:
        cards = fallback_cards()

    count = await repository.upsert_many(cards)
    print(f"Seeded {count} pokemon cards into MySQL database {settings.mysql_database}")


if __name__ == "__main__":
    import asyncio

    asyncio.run(main())
