from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class GoogleLoginRequest(BaseModel):
    id_token: str = Field(alias="idToken")

    model_config = ConfigDict(populate_by_name=True)


class UserRead(BaseModel):
    id: int
    email: str
    name: str
    avatar_url: str | None = Field(default=None, alias="avatarUrl")
    created_at: datetime = Field(alias="createdAt")
    updated_at: datetime = Field(alias="updatedAt")

    model_config = ConfigDict(from_attributes=True, populate_by_name=True)


class AuthResponse(BaseModel):
    token: str
    user: UserRead
