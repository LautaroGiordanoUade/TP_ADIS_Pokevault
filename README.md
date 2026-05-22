# TP_ADIS_Pokevault

This repository currently contains the migrated backend in `backend.Api/`.

The previous TypeScript backend was replaced by a Python FastAPI structure inside `backend.Api/` while preserving the existing PokeVault API behavior:

- `GET /api/pokemon`
- `GET /api/pokemon/{pokemon_id}`
- `POST /api/pokemon/sync`
- `GET /api/pokemon/vault/{user_id}`
- `POST /api/pokemon/vault/{user_id}/add`
- `DELETE /api/pokemon/vault/{user_id}/remove/{pokemon_id}`

## Run The API

```bash
cd backend.Api
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
python scripts\seed_pokemon_cards.py
uvicorn main:app --reload --host 127.0.0.1 --port 8000
```

Open:

- Health check: `http://127.0.0.1:8000/health`
- Swagger docs: `http://127.0.0.1:8000/docs`
- OpenAPI JSON: `http://127.0.0.1:8000/openapi.json`
