package com.pokevault.mobile.ui.feature.pickup.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokevault.mobile.R
import com.pokevault.mobile.ui.feature.pickup.viewmodel.PickupEffect
import com.pokevault.mobile.ui.feature.pickup.viewmodel.PickupEvent
import com.pokevault.mobile.ui.feature.pickup.viewmodel.PickupViewModel
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted
import kotlin.math.roundToInt

@Composable
fun PickupScreen(
    contentPadding: PaddingValues,
    onClose: () -> Unit,
    viewModel: PickupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions.values.any { it }
        viewModel.onEvent(PickupEvent.OnPermissionResult(granted))
    }

    LaunchedEffect(state.locationPermissionGranted) {
        if (!state.locationPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PickupEffect.OpenDirections -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)))
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissionAndLoadLocation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val distanceText = state.distanceInMeters?.let { meters ->
        if (meters < 1_000) {
            stringResource(R.string.pickup_meters, meters.roundToInt())
        } else {
            stringResource(R.string.pickup_km, String.format("%.1f", meters / 1_000))
        }
    } ?: stringResource(R.string.pickup_pending_location)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101116))
            .padding(contentPadding)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = Color.White) }
            Column {
                Text(stringResource(R.string.pickup_title), color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(R.string.pickup_order_number_demo), color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.pickup_ready_label), color = MarketOrange, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(Color(0xFF17181F), RoundedCornerShape(8.dp))) {
            SimulatedMap(Modifier.fillMaxSize())
            Card(
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(50),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
            ) {
                Text(stringResource(R.string.pickup_distance_format, distanceText), color = Color.White, modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp), style = MaterialTheme.typography.labelSmall)
            }
            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MarketOrange, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 54.dp, top = 28.dp).size(42.dp))
        }
        Spacer(Modifier.height(18.dp))
        Row {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.pickup_location_name), color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(R.string.pickup_location_building), color = Color.White)
                Text(state.destinationAddress, color = MarketOrange, fontWeight = FontWeight.ExtraBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.pickup_hours_label), color = Muted, style = MaterialTheme.typography.labelSmall)
                Text(stringResource(R.string.pickup_hours), color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        }
        Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF1C1D23)), modifier = Modifier.padding(top = 18.dp)) {
            Row(modifier = Modifier.padding(14.dp)) {
                Icon(Icons.Outlined.Info, null, tint = MarketOrange)
                Text(
                    if (state.locationPermissionGranted) {
                        stringResource(R.string.pickup_requirements_granted)
                    } else {
                        stringResource(R.string.pickup_requirements_denied)
                    },
                    color = Color.White,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { viewModel.onEvent(PickupEvent.OnGetDirectionsClick) },
            colors = ButtonDefaults.buttonColors(containerColor = MarketOrange, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Icon(Icons.Outlined.Route, null)
            Text(stringResource(R.string.pickup_get_directions), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun SimulatedMap(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val grid = Color.White.copy(alpha = 0.08f)
        val street = Color.White.copy(alpha = 0.16f)
        val avenue = Color.White.copy(alpha = 0.26f)
        val route = Color(0xFFFFB000)

        for (x in 0..size.width.toInt() step 28) {
            drawLine(grid, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height))
        }
        for (y in 0..size.height.toInt() step 28) {
            drawLine(grid, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
        }

        listOf(0.18f, 0.42f, 0.68f, 0.86f).forEach { fraction ->
            val x = size.width * fraction
            drawLine(street, Offset(x, 0f), Offset(x - 44f, size.height), strokeWidth = 10f)
        }
        listOf(0.24f, 0.52f, 0.76f).forEach { fraction ->
            val y = size.height * fraction
            drawLine(street, Offset(0f, y), Offset(size.width, y - 24f), strokeWidth = 9f)
        }
        drawLine(avenue, Offset(0f, size.height * 0.72f), Offset(size.width, size.height * 0.55f), strokeWidth = 16f)
        drawLine(avenue, Offset(size.width * 0.64f, 0f), Offset(size.width * 0.5f, size.height), strokeWidth = 16f)

        drawLine(
            color = route,
            start = Offset(40f, size.height * 0.72f),
            end = Offset(size.width * 0.72f, size.height * 0.45f),
            strokeWidth = 4f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
        )
        drawCircle(Color(0xFF00C08B), radius = 14f, center = Offset(42f, size.height * 0.72f))
        drawCircle(MarketOrange, radius = 18f, center = Offset(size.width * 0.72f, size.height * 0.45f), style = Stroke(width = 5f))
        drawCircle(MarketOrange.copy(alpha = 0.18f), radius = 30f, center = Offset(size.width * 0.72f, size.height * 0.45f))
    }
}
