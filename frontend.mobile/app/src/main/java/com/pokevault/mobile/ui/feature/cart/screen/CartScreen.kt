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
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokevault.mobile.domain.model.CartItem
import com.pokevault.mobile.ui.feature.cart.state.CartEvent
import com.pokevault.mobile.ui.feature.cart.state.CartUiState
import com.pokevault.mobile.ui.feature.cart.viewmodel.CartViewModel
import com.pokevault.mobile.ui.feature.components.CardArt
import com.pokevault.mobile.ui.feature.components.money
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted

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
    if (state.items.isEmpty()) {
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
                item { Text("ARTICULOS SELECCIONADOS (${state.totalQuantity})", color = Muted, style = MaterialTheme.typography.labelSmall) }
                items(state.items, key = { it.card.id }) { item ->
                    CartItemRow(
                        item = item,
                        onIncrement = { onEvent(CartEvent.OnIncrement(item.card.id)) },
                        onDecrement = { onEvent(CartEvent.OnDecrement(item.card.id)) },
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
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
) {
    OutlinedCard(border = BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(8.dp)) {
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
                    "Precio unitario: ${item.card.price.money()}",
                    color = Muted,
                    style = MaterialTheme.typography.labelSmall,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) { Icon(Icons.Outlined.Remove, null) }
                    Text(item.quantity.toString(), fontWeight = FontWeight.Bold)
                    IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) { Icon(Icons.Outlined.Add, null) }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Total", color = Muted, style = MaterialTheme.typography.labelSmall)
                Text((item.card.price * item.quantity).money(), fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = onRemove) { Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = Muted) }
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
        Text("DIRECCION DE DESPACHO ASEGURADO", color = Muted, style = MaterialTheme.typography.labelSmall)
        OutlinedCard(shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
            Text(state.deliveryAddress, modifier = Modifier.padding(12.dp))
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.Black)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row { Text("Subtotal de cartas:", color = Muted); Spacer(Modifier.weight(1f)); Text(state.subtotal.money()) }
                Row { Text("Envio Asegurado PSA:", color = Muted); Spacer(Modifier.weight(1f)); Text("Gratis!") }
                Row { Text("Monto final liquidado:", fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text(state.finalTotal.money(), fontWeight = FontWeight.ExtraBold) }
            }
        }
        Button(
            onClick = onConfirm,
            enabled = !state.isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = MarketOrange, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text(if (state.isSubmitting) "CONFIRMANDO..." else "CONFIRMAR PAGO ASEGURADO", fontWeight = FontWeight.ExtraBold)
            Icon(Icons.Outlined.Security, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
        }
        state.errorMessage?.let { message ->
            Text(message, color = Color(0xFFB00020), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun EmptyCart(onExploreCards: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedCard(shape = RoundedCornerShape(50)) {
                Icon(Icons.Outlined.ShoppingBag, contentDescription = null, tint = Muted, modifier = Modifier.padding(14.dp).size(36.dp))
            }
            Text("TU CARRITO ESTA VACIO", fontWeight = FontWeight.ExtraBold)
            Text("Aun no reservaste cartas\ncoleccionables de Pokemon.", color = Muted)
            Button(
                onClick = onExploreCards,
                colors = ButtonDefaults.buttonColors(containerColor = MarketOrange, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("EXPLORAR CARTAS", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
