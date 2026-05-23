# PokeVault API

FastAPI backend created as the next incremental step in the migration from the previous TypeScript/Express-style backend.

## Current Structure

- `main.py`: FastAPI application factory, CORS, route registration, OpenAPI docs.
- `core/config.py`: environment-based settings loaded from `.env`.
- `api/routes/`: HTTP controllers grouped by domain.
- `models/`: internal domain models.
- `schemas/`: Pydantic request and response schemas.
- `services/`: business logic.
- `repositories/`: persistence boundary. The current implementation uses MySQL through `repositories/mysql_pokemon_repository.py`.
- `db/`: database helpers and `schema.mysql.sql` for creating the MySQL tables.
- `scripts/seed_pokemon_cards.py`: creates the MySQL database/tables and seeds Pokemon cards. It loads `scripts/poke_cards.txt` first, then falls back to TCGGO, Pokemon TCG API, and finally a local seed list.

## Migrated TypeScript Concepts

- The old Express `pokemon_router.ts` is now `api/routes/pokemon.py`.
- The old `pokemon_service.ts` is now `services/pokemon_service.py`.
- The old Drizzle repository is now represented by `repositories/base.py` and `repositories/mysql_pokemon_repository.py`.
- The old Zod `PokemonSchema` is now `schemas/pokemon.py`.
- The previous `pokemon` and `vault_items` database entities are represented by domain models in `models/pokemon.py`.

## Run Locally

```bash
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
python scripts\seed_pokemon_cards.py
uvicorn main:app --reload --host 127.0.0.1 --port 8000
```

Before running the seed, make sure MySQL is running and `.env` has valid credentials:

```env
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_USER=root
MYSQL_PASSWORD_FILE=db/mysql_password.enc
MYSQL_PASSWORD_KEY_FILE=db/mysql_password.key
MYSQL_DATABASE=pokevault
GOOGLE_WEB_CLIENT_ID=your-google-web-client-id.apps.googleusercontent.com
```

Create the encrypted password file with:

```bash
python scripts\encrypt_mysql_password.py lautaro
```

This creates `db/mysql_password.enc` and `db/mysql_password.key`. Both are ignored by git.

If you use Docker, you can start MySQL with:

```bash
docker compose up -d mysql
```

## Available URLs

- Health check: `http://127.0.0.1:8000/health`
- Swagger docs: `http://127.0.0.1:8000/docs`
- OpenAPI JSON: `http://127.0.0.1:8000/openapi.json`

## Endpoints

- `GET /api/pokemon?page=1&pageSize=25`
- `GET /api/pokemon?name=char&page=1&pageSize=25`
- `GET /api/pokemon/search?name=char&page=1&pageSize=25`
- `GET /api/pokemon/{pokemon_id}`
- `POST /api/pokemon/sync`
- `GET /api/pokemon/vault/{user_id}`
- `POST /api/pokemon/vault/{user_id}/add`
- `DELETE /api/pokemon/vault/{user_id}/remove/{pokemon_id}`
- `POST /api/auth/google`
- `POST /api/auth/logout`
- `GET /api/users/me`
- `GET /api/vault/me`
- `POST /api/vault/me/items`
- `DELETE /api/vault/me/items/{pokemon_id}`
- `GET /api/orders/me`
- `POST /api/orders/me`

## Frontend Integration Example

```ts
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8000/api";

export async function listPokemon() {
  const response = await fetch(`${API_BASE_URL}/pokemon?page=1&pageSize=25`);
  if (!response.ok) throw new Error("Failed to load pokemon");
  return response.json();
}
```

The frontend should keep presentation and interaction state. Fetching pokemon, syncing cards, and vault mutations should go through this API.

For Android Google Sign-In, configure the same web client id in:

- `backend.Api/.env` as `GOOGLE_WEB_CLIENT_ID`
- `frontend.mobile/local.properties` as `GOOGLE_WEB_CLIENT_ID`
