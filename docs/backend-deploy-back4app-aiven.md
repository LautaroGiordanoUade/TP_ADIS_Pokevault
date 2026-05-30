# Backend deploy on Back4app and Aiven

This guide deploys the FastAPI API on Back4app Containers and uses Aiven MySQL as the managed database.

## 1. Back4app API settings

Create a Web Deployment from GitHub:

```text
Repository: LautaroGiordanoUade/TP_ADIS_Pokevault
Branch: dev/lau-deploy-back
Root Directory: backend.Api
Port: 8000
Health Check Path: /health
Autodeploy: Yes
```

The API is built with `backend.Api/Dockerfile`.

## 2. Aiven MySQL settings

Create a MySQL service on Aiven and copy these values from the service overview:

```text
Host
Port
User
Password
Database name
SSL mode
```

For Aiven free MySQL, SSL mode is usually required.

## 3. Back4app environment variables

Add these variables in the Back4app Web Deployment:

```env
ENVIRONMENT=production
MYSQL_HOST=<aiven-host>
MYSQL_PORT=<aiven-port>
MYSQL_USER=<aiven-user>
MYSQL_PASSWORD=<aiven-password>
MYSQL_DATABASE=<aiven-database-name>
MYSQL_SSL_MODE=REQUIRED
BACKEND_CORS_ORIGINS=*
GOOGLE_WEB_CLIENT_ID=<your-google-web-client-id.apps.googleusercontent.com>
```

Use the Aiven values exactly as shown in the console. Do not include the full `mysql://...` URI in `MYSQL_HOST`; use only the Host field.

Optional Pokemon API variables:

```env
POKEMON_TCG_API_KEY=<optional-key>
TCGGO_API_KEY=<optional-key>
```

## 4. Initialize data

The API creates tables lazily when endpoints use the repositories, but Pokemon cards must be seeded once.

Run this as a one-time command after the API can reach MySQL:

```bash
python scripts/seed_pokemon_cards.py
```

Warning: the current seed script calls `recreate_db()`, which drops and recreates the database. Use it only for first-time setup or when intentionally resetting data.

## 5. Verify the API

After deployment, open:

```text
https://<your-back4app-app-url>/health
https://<your-back4app-app-url>/docs
https://<your-back4app-app-url>/api/pokemon?page=1&pageSize=10
```

## 6. Connect the Android app

For deployed builds, update `BuildConfig.API_BASE_URL` to the Back4app URL with the `/api/` suffix:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://<your-back4app-app-url>/api/\"")
```
