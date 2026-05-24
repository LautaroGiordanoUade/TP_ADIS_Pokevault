from fastapi import HTTPException, status

from core.config import settings
from models.user import User
from repositories.mysql_user_repository import MySQLUserRepository
from schemas.user import AuthResponse, UserRead


class AuthService:
    def __init__(self, repository: MySQLUserRepository) -> None:
        self.repository = repository

    async def login_with_google(self, token: str) -> AuthResponse:
        if not settings.google_web_client_id:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="GOOGLE_WEB_CLIENT_ID is not configured.",
            )
        try:
            from google.auth.transport import requests
            from google.oauth2 import id_token
        except (ImportError, ModuleNotFoundError) as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="google-auth requests transport dependency is not installed.",
            ) from exc

        try:
            payload = id_token.verify_oauth2_token(
                token,
                requests.Request(),
                settings.google_web_client_id,
            )
        except ValueError as exc:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid Google ID token.",
            ) from exc
        except Exception as exc:
            module = exc.__class__.__module__
            if module.startswith("google.auth.transport"):
                raise HTTPException(
                    status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                    detail="Could not reach Google to verify the ID token.",
                ) from exc
            if module.startswith("google.auth"):
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Invalid Google ID token.",
                ) from exc
            raise

        email = payload.get("email")
        google_sub = payload.get("sub")
        if not email or not google_sub:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Google token is missing required user information.",
            )

        user = await self.repository.upsert_google_user(
            google_sub=google_sub,
            email=email,
            name=payload.get("name") or email,
            avatar_url=payload.get("picture"),
        )
        session_token = await self.repository.create_session(
            user_id=user.id,
            ttl_hours=settings.session_token_ttl_hours,
        )
        return AuthResponse(token=session_token, user=UserRead.model_validate(user))

    async def user_from_token(self, token: str) -> User | None:
        return await self.repository.get_user_by_session(token)

    async def logout(self, token: str) -> None:
        await self.repository.revoke_session(token)
