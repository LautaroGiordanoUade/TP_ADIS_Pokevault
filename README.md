# TP_ADIS_Pokevault

This repository currently contains the migrated backend in `backend.Api/`.
Python FastAPI structure inside `backend.Api/` while preserving PokeVault API behavior:

## Documentacion de entrega

- [Documentacion final de entrega y defensa](docs/entrega-final.md)

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

Para usar la API local que corre en tu PC, pegar esto en `frontend.mobile/api.properties`:

```properties
API_TARGET=local
API_BASE_URL=
```

La API local corre en:

```text
http://127.0.0.1:8000
```

Luego levantar el backend local:

```bash
cd backend.Api
uvicorn main:app --reload --host 127.0.0.1 --port 8000
```

Con esa config, el emulador Android apunta a esta URL de API:

```text
http://10.0.2.2:8000/api/
```

Endpoints locales utiles para probar en el navegador:

```text
http://127.0.0.1:8000/health
http://127.0.0.1:8000/docs
http://127.0.0.1:8000/api/pokemon?page=1&pageSize=10
```

### Usar servidor deployado

Para usar la API deployada en Back4app, pegar esto en `frontend.mobile/api.properties`:

```properties
API_TARGET=server
API_BASE_URL=
```

La API deployada corre en:

```text
https://pokevaultapi-we8cuobk.b4a.run
```

Con esa config, la app apunta a esta URL de API:

```text
https://pokevaultapi-we8cuobk.b4a.run/api/
```

Endpoints del servidor para probar en el navegador:

```text
https://pokevaultapi-we8cuobk.b4a.run/health
https://pokevaultapi-we8cuobk.b4a.run/docs
https://pokevaultapi-we8cuobk.b4a.run/api/pokemon?page=1&pageSize=10
```

### Usar una URL manual

Si necesitas apuntar a otra API, pegar una URL completa con `/api/` al final:

```properties
API_TARGET=local
API_BASE_URL=https://otra-api.example.com/api/
```

`API_BASE_URL` tiene prioridad sobre `API_TARGET`.

### Google Sign-In

El `GOOGLE_WEB_CLIENT_ID` sigue configurandose en:

```text
frontend.mobile/local.properties
```

Ejemplo:

```properties
GOOGLE_WEB_CLIENT_ID=your-google-web-client-id.apps.googleusercontent.com
```
