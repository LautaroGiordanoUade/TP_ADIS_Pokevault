from fastapi import APIRouter, Depends, status

from api.dependencies import auth_service, current_token
from schemas.user import AuthResponse, GoogleLoginRequest

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/google", response_model=AuthResponse)
async def google_login(payload: GoogleLoginRequest) -> AuthResponse:
    return await auth_service.login_with_google(payload.id_token)


@router.post("/logout", status_code=status.HTTP_204_NO_CONTENT)
async def logout(token: str = Depends(current_token)) -> None:
    await auth_service.logout(token)
