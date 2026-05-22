from fastapi import FastAPI
from fastapi import Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pymysql import MySQLError

from api.routes import health, pokemon
from core.config import settings


def create_app() -> FastAPI:
    app = FastAPI(
        title=settings.app_name,
        version="0.1.0",
        docs_url="/docs",
        openapi_url="/openapi.json",
    )

    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.cors_origins,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.include_router(health.router)
    app.include_router(pokemon.router, prefix=settings.api_v1_prefix)

    @app.exception_handler(MySQLError)
    async def mysql_exception_handler(
        _request: Request,
        _exception: MySQLError,
    ) -> JSONResponse:
        return JSONResponse(
            status_code=503,
            content={
                "detail": (
                    "MySQL is not available. Check MYSQL_HOST, MYSQL_PORT, "
                    "MYSQL_USER, MYSQL_PASSWORD and MYSQL_DATABASE."
                )
            },
        )

    return app


app = create_app()
