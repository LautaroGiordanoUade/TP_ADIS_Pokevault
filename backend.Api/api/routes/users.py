from fastapi import APIRouter, Depends

from api.dependencies import current_user
from models.user import User
from schemas.user import UserRead

router = APIRouter(prefix="/users", tags=["users"])


@router.get("/me", response_model=UserRead)
async def get_me(user: User = Depends(current_user)) -> UserRead:
    return UserRead.model_validate(user)
