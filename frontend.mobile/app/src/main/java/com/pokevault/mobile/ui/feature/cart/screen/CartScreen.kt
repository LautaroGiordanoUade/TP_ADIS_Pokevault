package com.pokevault.mobile.ui.feature.cart.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokevault.mobile.R
import com.pokevault.mobile.domain.model.CartItem
import com.pokevault.mobile.ui.feature.cart.state.CartEvent
import com.pokevault.mobile.ui.feature.cart.state.CartUiState
import com.pokevault.mobile.ui.feature.cart.viewmodel.CartViewModel
import com.pokevault.mobile.ui.feature.components.CardArt
import com.pokevault.mobile.ui.feature.components.money

@Composable
fun CartScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: CartViewModel,
    onExploreCards: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CartContent(
        state = state,
        contentPadding = contentPadding,
        onEvent = { event ->
            if (event == CartEvent.OnExploreCards) onExploreCards()
            viewModel.onEvent(event)
        },
    )
}

@Composable
fun CartContent(
    state: CartUiState,
    contentPadding: PaddingValues,
    onEvent: (CartEvent) -> Unit,
) {
    if (state.isLoading) {
        LoadingCart()
    } else if (state.items.isEmpty()) {
        EmptyCart(onExploreCards = { onEvent(CartEvent.OnExploreCards) })
    } else {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 18.dp, bottom = 18.dp),
            ) {
                item {
                    Text(
                        stringResource(R.string.cart_items_selected, state.totalQuantity),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                items(state.items, key = { it.card.id }) { item ->
                    CartItemRow(
                        item = item,
                        onIncrement = { onEvent(CartEvent.OnIncrement(item.card.id)) },
                        onDecrement = { onEvent(CartEvent.OnDecrement(item.card.id)) },
                        onEdit = { onEvent(CartEvent.OnEditQuantity(item)) },
                        onRemove = { onEvent(CartEvent.OnRemove(item.card.id)) },
                    )
                }
            }
            CheckoutPanel(
                state = state, 
                contentPadding = contentPadding,
                onConfirm = { onEvent(CartEvent.OnConfirmPayment) }
            )
        }
    }

    state.editingItem?.let { editingItem ->
        QuantityEditorDialog(
            item = editingItem,
            quantityInput = state.editingQuantityInput,
            validationMessage = state.errorMessage,
            onValueChange = { onEvent(CartEvent.OnQuantityInputChanged(it)) },
            onDismiss = { onEvent(CartEvent.OnDismissQuantityEditor) },
            onSave = { onEvent(CartEvent.OnSaveEditedQuantity) },
        )
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    OutlinedCard(border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardArt(
                card = item.card,
                modifier = Modifier.width(56.dp).height(78.dp),
                showBadge = false,
            )
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    item.card.name,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.cart_unit_price, item.card.price.money()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.Remove,
                            contentDescription = stringResource(R.string.cart_decrease_quantity, item.card.name),
                        )
                    }
                    Text(item.quantity.toString(), fontWeight = FontWeight.Bold)
                    IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.cart_increase_quantity, item.card.name),
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    stringResource(R.string.cart_item_total),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text((item.card.price * item.quantity).money(), fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.cart_edit_quantity),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.cart_remove),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckoutPanel(
    state: CartUiState, 
    contentPadding: PaddingValues,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = contentPadding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            stringResource(R.string.cart_delivery_address_label),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
        OutlinedCard(shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
            Text(state.deliveryAddress, modifier = Modifier.padding(12.dp))
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row { Text(stringResource(R.string.cart_subtotal_label), color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.weight(1f)); Text(state.subtotal.money()) }
                Row { Text(stringResource(R.string.cart_shipping_label), color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.weight(1f)); Text(stringResource(R.string.cart_shipping_free)) }
                Row { Text(stringResource(R.string.cart_final_total_label), fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text(state.finalTotal.money(), fontWeight = FontWeight.ExtraBold) }
            }
        }
        Button(
            onClick = onConfirm,
            enabled = !state.isSubmitting,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text(if (state.isSubmitting) stringResource(R.string.cart_confirming) else stringResource(R.string.cart_confirm_payment), fontWeight = FontWeight.ExtraBold)
            Icon(Icons.Outlined.Security, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
        }
        state.errorMessage?.let { message ->
            val displayError = when (message) {
                "No se pudo confirmar la compra" -> stringResource(R.string.cart_error_confirm_payment)
                "Ingresá una cantidad válida mayor a cero" -> stringResource(R.string.cart_error_invalid_quantity)
                else -> message
            }
            Text(displayError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun QuantityEditorDialog(
    item: CartItem,
    quantityInput: String,
    validationMessage: String?,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.cart_edit_quantity_title),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.cart_edit_quantity_message, item.card.name))
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = onValueChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(stringResource(R.string.cart_edit_quantity_label)) },
                )
                if (validationMessage == "IngresÃ¡ una cantidad vÃ¡lida mayor a cero") {
                    Text(
                        text = stringResource(R.string.cart_error_invalid_quantity),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(stringResource(R.string.cart_save_quantity), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cart_cancel_quantity))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
private fun LoadingCart() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.cart_loading), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyCart(onExploreCards: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedCard(shape = RoundedCornerShape(50)) {
                Icon(
                    Icons.Outlined.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp).size(36.dp),
                )
            }
            Text(stringResource(R.string.cart_empty_title), fontWeight = FontWeight.ExtraBold)
            Text(stringResource(R.string.cart_empty_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = onExploreCards,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.cart_explore), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
