package com.pokevault.mobile.ui.feature.profile.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.activity.compose.LocalActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.pokevault.mobile.BuildConfig
import com.pokevault.mobile.R
import com.pokevault.mobile.domain.model.Order
import com.pokevault.mobile.domain.model.OrderStatus
import com.pokevault.mobile.util.money
import com.pokevault.mobile.ui.feature.profile.state.ProfileEffect
import com.pokevault.mobile.ui.feature.profile.state.ProfileEvent
import com.pokevault.mobile.ui.feature.profile.state.ProfileUiState
import com.pokevault.mobile.ui.feature.profile.viewmodel.ProfileViewModel
import com.pokevault.mobile.ui.theme.AvatarBackground
import com.pokevault.mobile.ui.theme.ErrorRed
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted
import com.pokevault.mobile.ui.theme.VipBackground

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onOpenPickup: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val credentialManager = remember(activity) { CredentialManager.create(activity!!) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateToPickup -> onOpenPickup()
                ProfileEffect.ClearGoogleCredentialState -> {
                    runCatching {
                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                    }
                    Unit
                }
                ProfileEffect.RequestGoogleSignIn -> {
                    if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
                        viewModel.onEvent(ProfileEvent.OnLoginFailed("Falta configurar GOOGLE_WEB_CLIENT_ID"))
                    } else {
                        runCatching {
                            val googleIdOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credentialManager.getCredential(activity!!, request)
                            GoogleIdTokenCredential.createFrom(result.credential.data).idToken
                        }.onSuccess { idToken ->
                            if (idToken.isBlank()) {
                                viewModel.onEvent(ProfileEvent.OnLoginFailed("Google no devolvio un token valido"))
                            } else {
                                viewModel.onEvent(ProfileEvent.OnGoogleIdTokenReceived(idToken))
                            }
                        }.onFailure { error ->
                            when (error) {
                                is GetCredentialCancellationException -> viewModel.onEvent(ProfileEvent.OnLoginCanceled)
                                is NoCredentialException -> viewModel.onEvent(
                                    ProfileEvent.OnLoginFailed("No hay cuentas de Google disponibles en este dispositivo")
                                )
                                else -> viewModel.onEvent(
                                    ProfileEvent.OnLoginFailed(error.message ?: "No se pudo iniciar sesion con Google")
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(ProfileEvent.OnRefresh)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
        Text(text = stringResource(R.string.profile_login_title), fontWeight = FontWeight.ExtraBold)
        Text(
            text = stringResource(R.string.profile_login_subtitle),
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
            Text(
                text = if (state.isLoading) stringResource(R.string.profile_login_connecting) else stringResource(R.string.profile_login_continue), 
                fontWeight = FontWeight.ExtraBold
            )
        }
        state.errorMessage?.let { message ->
            val displayError = when (message) {
                "Falta configurar GOOGLE_WEB_CLIENT_ID" -> stringResource(R.string.profile_login_error_missing_client_id)
                "No hay cuentas de Google disponibles en este dispositivo" -> stringResource(R.string.profile_login_error_no_accounts)
                "Google no devolvio un token valido" -> stringResource(R.string.profile_login_error_invalid_token)
                "No se pudo iniciar sesion con Google" -> stringResource(R.string.profile_login_error_failed)
                "No se pudo actualizar el historial" -> stringResource(R.string.profile_login_error_refresh_failed)
                else -> message
            }
            Text(displayError, color = ErrorRed, modifier = Modifier.padding(top = 12.dp))
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
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(AvatarBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    val avatarUrl = state.profile?.avatarUrl
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.Black, modifier = Modifier.size(42.dp))
                    }
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
                            Text(text = stringResource(R.string.profile_account_title), color = Muted, style = MaterialTheme.typography.labelSmall)
                            Text("Google", fontWeight = FontWeight.ExtraBold)
                        }
                        Text("VIP", color = MarketOrange, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        item {
            var showLanguageDialog by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.profile_settings_title),
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted
                )

                // Dark mode toggle
                OutlinedCard(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val darkEnabled = state.isDarkMode ?: false
                        Icon(
                            imageVector = if (darkEnabled) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                            contentDescription = null,
                            tint = MarketOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                            Text(
                                text = stringResource(R.string.profile_dark_mode_label),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (darkEnabled) stringResource(R.string.profile_dark_mode_on) else stringResource(R.string.profile_dark_mode_off),
                                color = Muted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Switch(
                            checked = darkEnabled,
                            onCheckedChange = { onEvent(ProfileEvent.OnDarkModeChanged(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = MarketOrange,
                            )
                        )
                    }
                }

                // Language selector
                OutlinedCard(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.profile_language_label),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (state.currentLanguage == "es") "Español" else "English",
                                color = Muted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = {
                        Text(
                            text = stringResource(R.string.profile_language_dialog_title),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEvent(ProfileEvent.OnLanguageChanged("es"))
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.profile_language_spanish),
                                    fontWeight = if (state.currentLanguage == "es") FontWeight.ExtraBold else FontWeight.Normal,
                                    color = if (state.currentLanguage == "es") MarketOrange else Color.Unspecified,
                                    modifier = Modifier.weight(1f)
                                )
                                if (state.currentLanguage == "es") {
                                    Text("✓", color = MarketOrange, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEvent(ProfileEvent.OnLanguageChanged("en"))
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.profile_language_english),
                                    fontWeight = if (state.currentLanguage == "en") FontWeight.ExtraBold else FontWeight.Normal,
                                    color = if (state.currentLanguage == "en") MarketOrange else Color.Unspecified,
                                    modifier = Modifier.weight(1f)
                                )
                                if (state.currentLanguage == "en") {
                                    Text("✓", color = MarketOrange, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showLanguageDialog = false }) {
                            Text(
                                text = stringResource(R.string.profile_language_cancel),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        item { 
            Text(
                text = stringResource(R.string.profile_purchase_history, state.orders.size), 
                style = MaterialTheme.typography.labelSmall
            ) 
        }

        items(state.orders, key = { it.id }) { order ->
            OrderCard(order = order, onPickupClick = { onEvent(ProfileEvent.OnPickupClick(order.id)) })
        }

        item {
            OutlinedButton(
                onClick = { onEvent(ProfileEvent.OnLogoutClick) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(text = stringResource(R.string.profile_logout), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onPickupClick: () -> Unit) {
    OutlinedCard(border = BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val isDelivered = order.statusId == 2 || order.status == OrderStatus.Delivered
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.profile_order_number, order.id), 
                    fontWeight = FontWeight.ExtraBold, 
                    style = MaterialTheme.typography.labelSmall
                )
                if (!isDelivered) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.profile_to_pickup),
                        color = MarketOrange,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Row {
                Text("${order.title} x${order.quantity}", color = Muted)
                Spacer(Modifier.weight(1f))
                Text(
                    text = order.amount.money(),
                    maxLines = 1,
                    softWrap = false
                )
            }
            if (!isDelivered) {
                Card(colors = CardDefaults.cardColors(containerColor = VipBackground), border = BorderStroke(1.dp, MarketOrange)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.profile_ready_to_pickup), 
                            color = MarketOrange, 
                            fontWeight = FontWeight.Bold, 
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(onClick = onPickupClick) {
                            Text(text = stringResource(R.string.profile_view_map), style = MaterialTheme.typography.labelSmall)
                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Row {
                    Text(
                        text = stringResource(R.string.profile_payment_method, order.paymentMethod), 
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.profile_total, order.total.money()), 
                        fontWeight = FontWeight.ExtraBold, 
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
