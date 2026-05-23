from typing import Annotated

from fastapi import Depends, Header, HTTPException, status

from models.user import User
from repositories.mysql_user_repository import user_repository
from services.auth_service import AuthService

auth_service = AuthService(repository=user_repository)


def _bearer_token(authorization: str | None) -> str:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing bearer token.",
        )
    return authorization.removeprefix("Bearer ").strip()


async def current_user(
    authorization: Annotated[str | None, Header()] = None,
) -> User:
    token = _bearer_token(authorization)
    user = await auth_service.user_from_token(token)
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired session.",
        )
    return user


async def current_token(
    authorization: Annotated[str | None, Header()] = None,
) -> str:
    return _bearer_token(authorization)
