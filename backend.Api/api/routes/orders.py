from fastapi import APIRouter, Depends, HTTPException, status

from api.dependencies import current_user
from models.user import User
from repositories.mysql_user_repository import user_repository
from schemas.order import CreateOrderRequest, OrderRead

router = APIRouter(prefix="/orders", tags=["orders"])


@router.get("/me", response_model=list[OrderRead])
async def get_my_orders(user: User = Depends(current_user)) -> list[OrderRead]:
    orders = await user_repository.list_orders(user.id)
    return [OrderRead.model_validate(order) for order in orders]


@router.post("/me", response_model=OrderRead, status_code=status.HTTP_201_CREATED)
async def create_my_order(
    payload: CreateOrderRequest,
    user: User = Depends(current_user),
) -> OrderRead:
    try:
        order = await user_repository.create_order(
            user_id=user.id,
            items=[(item.pokemon_id, item.quantity) for item in payload.items],
            delivery_address=payload.delivery_address,
            payment_method=payload.payment_method,
        )
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(exc)) from exc
    return OrderRead.model_validate(order)
