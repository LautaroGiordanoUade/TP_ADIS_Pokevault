# TP_ADIS_Pokevault

This repository currently contains the migrated backend in `backend.Api/`.
Python FastAPI structure inside `backend.Api/` while preserving PokeVault API behavior:

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

## Configurar La API Del Frontend Mobile

El frontend Android no se conecta directo a MySQL. Siempre apunta a una API FastAPI, y esa API es la que usa la base configurada.

La URL se elige desde:

```text
frontend.mobile/api.properties
```

### Usar backend local

Dejar:

```properties
API_TARGET=local
API_BASE_URL=
```

Luego levantar el backend local:

```bash
cd backend.Api
uvicorn main:app --reload --host 127.0.0.1 --port 8000
```

En emulador Android, la app usa:

```text
http://10.0.2.2:8000/api/
```

### Usar servidor deployado

Cambiar:

```properties
API_TARGET=server
API_BASE_URL=
```

La app usa:

```text
https://pokevaultapi-izqk2xgb.b4a.run/api/
```

### Usar una URL manual

Si necesitás apuntar a otra API, usar:

```properties
API_TARGET=local
API_BASE_URL=https://otra-api.example.com/api/
```

`API_BASE_URL` tiene prioridad sobre `API_TARGET`.

### Google Sign-In

El `GOOGLE_WEB_CLIENT_ID` sigue configurándose en:

```text
frontend.mobile/local.properties
```

Ejemplo:

```properties
GOOGLE_WEB_CLIENT_ID=362499068237-sslu5ih83fa3lkms4bfhke3oq9trp2f5.apps.googleusercontent.com
```
