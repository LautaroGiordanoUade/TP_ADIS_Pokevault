package com.pokevault.mobile.ui.feature.profile.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.pokevault.mobile.BuildConfig
import com.pokevault.mobile.domain.model.Order
import com.pokevault.mobile.domain.model.OrderStatus
import com.pokevault.mobile.ui.feature.components.money
import com.pokevault.mobile.ui.feature.profile.state.ProfileEffect
import com.pokevault.mobile.ui.feature.profile.state.ProfileEvent
import com.pokevault.mobile.ui.feature.profile.state.ProfileUiState
import com.pokevault.mobile.ui.feature.profile.viewmodel.ProfileViewModel
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted
import com.pokevault.mobile.ui.theme.SuccessGreen

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onOpenPickup: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateToPickup -> onOpenPickup()
                ProfileEffect.RequestGoogleSignIn -> {
                    if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
                        viewModel.onEvent(ProfileEvent.OnLoginFailed("Falta configurar GOOGLE_WEB_CLIENT_ID"))
                    } else {
                        runCatching {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credentialManager.getCredential(context, request)
                            GoogleIdTokenCredential.createFrom(result.credential.data).idToken
                        }.onSuccess { idToken ->
                            viewModel.onEvent(ProfileEvent.OnGoogleIdTokenReceived(idToken))
                        }.onFailure { error ->
                            viewModel.onEvent(
                                ProfileEvent.OnLoginFailed(error.message ?: "No se pudo iniciar sesion con Google")
                            )
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(top = contentPadding.calculateTopPadding())
    ) {
        if (state.profile == null) {
            LoginContent(state = state, onEvent = viewModel::onEvent)
        } else {
            ProfileContent(
                state = state, 
                contentPadding = contentPadding,
                onEvent = viewModel::onEvent
            )
        }
    }
}

@Composable
private fun LoginContent(
    state: ProfileUiState,
    onEvent: (ProfileEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(CircleShape).background(MarketOrange),
            contentAlignment = Alignment.Center,
        ) {
            Text("P", fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(14.dp))
        Text("INGRESAR A POKEMARKET", fontWeight = FontWeight.ExtraBold)
        Text(
            "Continua con Google para guardar favoritos y ver tu historial de compras.",
            color = Muted,
            modifier = Modifier.padding(vertical = 10.dp),
        )
        Button(
            onClick = { onEvent(ProfileEvent.OnGoogleLoginClick) },
            enabled = !state.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MarketOrange, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text(if (state.isLoading) "CONECTANDO..." else "CONTINUAR CON GOOGLE", fontWeight = FontWeight.ExtraBold)
        }
        state.errorMessage?.let { message ->
            Text(message, color = Color(0xFFB00020), modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    contentPadding: PaddingValues,
    onEvent: (ProfileEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(
            top = 22.dp, 
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFFE8E8EA)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.Black, modifier = Modifier.size(42.dp))
                }
                Text(state.profile?.name.orEmpty(), fontWeight = FontWeight.ExtraBold)
                Text(state.profile?.email.orEmpty().uppercase(), color = Muted, style = MaterialTheme.typography.labelSmall)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.padding(top = 14.dp),
                ) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AccountBalanceWallet, null, tint = MarketOrange)
                        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                            Text("CUENTA:", color = Muted, style = MaterialTheme.typography.labelSmall)
                            Text("Google", fontWeight = FontWeight.ExtraBold)
                        }
                        Text("VIP", color = MarketOrange, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        item { Text("HISTORIAL DE COMPRAS (${state.orders.size})", style = MaterialTheme.typography.labelSmall) }
        items(state.orders, key = { it.id }) { order ->
            OrderCard(order = order, onPickupClick = { onEvent(ProfileEvent.OnPickupClick(order.id)) })
        }
        item {
            OutlinedButton(
                onClick = { onEvent(ProfileEvent.OnLogoutClick) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("CERRAR SESION", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onPickupClick: () -> Unit) {
    OutlinedCard(border = BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ORDEN #${order.id}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.weight(1f))
                val statusColor = if (order.status == OrderStatus.ReadyForPickup) MarketOrange else SuccessGreen
                Text(
                    if (order.status == OrderStatus.ReadyForPickup) "PARA RETIRAR" else "ENTREGADO",
                    color = statusColor,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Row {
                Text("${order.title} x${order.quantity}", color = Muted)
                Spacer(Modifier.weight(1f))
                Text(order.amount.money())
            }
            if (order.status == OrderStatus.ReadyForPickup) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3DE)), border = BorderStroke(1.dp, MarketOrange)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("LISTO PARA RETIRAR!\nSucursal: UADE (Lima 757, CABA)", color = MarketOrange, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = onPickupClick) {
                            Text("VER MAPA", style = MaterialTheme.typography.labelSmall)
                            Icon(Icons.Outlined.ArrowForward, null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            } else {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Text("ENTREGADO CON EXITO EL 10 MAY, 2026", color = Muted, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
            Row {
                Text("Formula: ${order.paymentMethod}", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.weight(1f))
                Text("Total: ${order.total.money()}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
