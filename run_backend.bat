@echo off
setlocal

set "ROOT_DIR=%~dp0"
set "BACKEND_DIR=%ROOT_DIR%backend.Api"
set "VENV_PY=%BACKEND_DIR%\.venv\Scripts\python.exe"
set "SEED_MARKER=%BACKEND_DIR%\.pokevault_seeded"
set "CODEX_PY=C:\Users\lauta\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe"

if not exist "%BACKEND_DIR%\README.md" (
    echo Could not find backend.Api from %ROOT_DIR%.
    exit /b 1
)

cd /d "%BACKEND_DIR%" || exit /b 1

if not exist ".env" (
    if exist ".env.example" (
        echo Creating .env from .env.example...
        copy ".env.example" ".env" >nul
    ) else (
        echo Missing .env and .env.example.
        exit /b 1
    )
)

if not exist "%VENV_PY%" (
    call :find_python
    if errorlevel 1 exit /b 1

    echo Creating Python virtual environment...
    "%PY_BOOTSTRAP%" -m venv ".venv"
    if errorlevel 1 exit /b 1
)

echo Installing backend dependencies...
"%VENV_PY%" -m pip install -r requirements.txt
if errorlevel 1 exit /b 1

if /i "%~1"=="--seed" (
    call :seed_database
    if errorlevel 1 exit /b 1
) else if not exist "%SEED_MARKER%" (
    call :seed_database
    if errorlevel 1 exit /b 1
) else (
    echo Database seed already done. Use run_backend.bat --seed to recreate and reseed it.
)

echo.
echo Starting PokeVault API at http://127.0.0.1:8000
echo Swagger docs: http://127.0.0.1:8000/docs
echo Press Ctrl+C to stop the backend.
echo.

"%VENV_PY%" -m uvicorn main:app --reload --host 127.0.0.1 --port 8000
exit /b %ERRORLEVEL%

:seed_database
echo Seeding MySQL database...
"%VENV_PY%" scripts\seed_pokemon_cards.py
if errorlevel 1 (
    echo.
    echo Seed failed. Make sure MySQL is running and .env has valid credentials.
    echo If you use Docker, run this from backend.Api first: docker compose up -d mysql
    exit /b 1
)
type nul > "%SEED_MARKER%"
exit /b 0

:find_python
where python >nul 2>nul
if not errorlevel 1 (
    set "PY_BOOTSTRAP=python"
    exit /b 0
)

where py >nul 2>nul
if not errorlevel 1 (
    py --version >nul 2>nul
    if not errorlevel 1 (
        set "PY_BOOTSTRAP=py"
        exit /b 0
    )
)

if exist "%CODEX_PY%" (
    set "PY_BOOTSTRAP=%CODEX_PY%"
    exit /b 0
)

echo Python was not found. Install Python 3.12+ or add it to PATH, then run this again.
exit /b 1
