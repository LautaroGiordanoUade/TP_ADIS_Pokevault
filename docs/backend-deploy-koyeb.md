# Backend deploy on Koyeb

This guide deploys the FastAPI backend and MySQL database using Koyeb's GitHub-driven deployment.

## Why Koyeb

- It supports FastAPI/Python apps deployed directly from GitHub.
- Git-driven deployment can rebuild and redeploy automatically on pushes to the selected branch.
- Koyeb Database Services include a free instance type, and MySQL is available.

## 1. Create the MySQL database

1. In Koyeb, create a new Database Service.
2. Select MySQL.
3. Use the free instance type if it is available in your account.
4. Create a database named `pokevault`.
5. Save the host, port, user, password, and database name.

## 2. Create the API service

1. Create a new Web Service from GitHub.
2. Select this repository.
3. Select the branch you want to deploy, usually `main`.
4. Set the project directory to:

```text
backend.Api
```

5. Use the Dockerfile builder. The Dockerfile is:

```text
backend.Api/Dockerfile
```

6. Keep autodeploy enabled so pushes to the selected branch trigger CD.

## 3. Environment variables

Set these variables in the Koyeb API service:

```env
ENVIRONMENT=production
MYSQL_HOST=<koyeb-mysql-host>
MYSQL_PORT=3306
MYSQL_USER=<koyeb-mysql-user>
MYSQL_PASSWORD=<koyeb-mysql-password>
MYSQL_DATABASE=pokevault
BACKEND_CORS_ORIGINS=http://localhost:5173,http://localhost:3000
GOOGLE_WEB_CLIENT_ID=<your-google-web-client-id.apps.googleusercontent.com>
```

Optional Pokemon API variables:

```env
POKEMON_TCG_API_KEY=<optional-key>
TCGGO_API_KEY=<optional-key>
```

Use `MYSQL_PASSWORD` in production. Do not upload `db/mysql_password.enc` or `db/mysql_password.key`.

## 4. Initialize data

The API creates tables lazily when endpoints use the repositories, but Pokemon cards must be seeded once.

Run the seed as a one-time job or temporary Koyeb console command:

```bash
python scripts/seed_pokemon_cards.py
```

Warning: the current seed script calls `recreate_db()`, which drops and recreates the database. Use it only for first-time setup or when intentionally resetting data.

## 5. Verify the API

After deployment, open:

```text
https://<your-koyeb-service>.koyeb.app/health
https://<your-koyeb-service>.koyeb.app/docs
https://<your-koyeb-service>.koyeb.app/api/pokemon?page=1&pageSize=10
```

## 6. Connect the Android app

The Android app currently points to the emulator backend URL. For deployed builds, update `BuildConfig.API_BASE_URL` to the Koyeb URL with the `/api/` suffix:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://<your-koyeb-service>.koyeb.app/api/\"")
```

For a cleaner setup, make this value read from `local.properties` or CI environment variables before producing release builds.
