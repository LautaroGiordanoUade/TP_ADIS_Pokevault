package com.pokevault.mobile.ui.feature.pickup.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val STORE_LATITUDE = -34.618279
private const val STORE_LONGITUDE = -58.381565
private const val STORE_ADDRESS = "Lima 757, Ciudad Autonoma de Buenos Aires, Argentina"

@Composable
fun PickupScreen(
    contentPadding: PaddingValues,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        locationPermissionGranted = permissions.values.any { it }
        if (locationPermissionGranted) {
            currentLocation = findLastKnownLocation(context)
        }
    }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            currentLocation = findLastKnownLocation(context)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    val distanceText = currentLocation?.let { location ->
        formatDistance(distanceInMeters(location.latitude, location.longitude, STORE_LATITUDE, STORE_LONGITUDE))
    } ?: "Ubicacion pendiente"

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
                Text("RETIRO EN PAQUETERIA", color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text("ORDEN #PKM-A7X2", color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.weight(1f))
            Text("LISTO EN SEDE", color = MarketOrange, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(Color(0xFF17181F), RoundedCornerShape(8.dp))) {
            SimulatedMap(Modifier.fillMaxSize())
            Card(
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(50),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
            ) {
                Text("Distancia al punto\n$distanceText", color = Color.White, modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp), style = MaterialTheme.typography.labelSmall)
            }
            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MarketOrange, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 54.dp, top = 28.dp).size(42.dp))
        }
        Spacer(Modifier.height(18.dp))
        Row {
            Column(modifier = Modifier.weight(1f)) {
                Text("PUNTO OFICIAL DE RETIRO UADE", color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text("UADE (Sede Principal Monserrat - Edificio Lima)", color = Color.White)
                Text(STORE_ADDRESS, color = MarketOrange, fontWeight = FontWeight.ExtraBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Horario de Entrega", color = Muted, style = MaterialTheme.typography.labelSmall)
                Text("Lun a Vie 08:30 -\n21:30 hs", color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        }
        Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF1C1D23)), modifier = Modifier.padding(top = 18.dp)) {
            Row(modifier = Modifier.padding(14.dp)) {
                Icon(Icons.Outlined.Info, null, tint = MarketOrange)
                Text(
                    if (locationPermissionGranted) {
                        "Requisitos para Retiro:\nPresenta DNI del interesado y el codigo QR de autorizacion o el numero de transaccion #PKM-A7X2."
                    } else {
                        "Activa el permiso de ubicacion para calcular la distancia desde tu posicion actual."
                    },
                    color = Color.White,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { openDirections(context, currentLocation) },
            colors = ButtonDefaults.buttonColors(containerColor = MarketOrange, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Icon(Icons.Outlined.Route, null)
            Text("COMO LLEGAR AL LOCAL", fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun findLastKnownLocation(context: Context): Location? {
    if (!hasLocationPermission(context)) return null
    val manager = context.getSystemService(LocationManager::class.java)
    return manager.getProviders(true)
        .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull { it.time }
}

private fun distanceInMeters(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): Double {
    val earthRadius = 6_371_000.0
    val dLat = Math.toRadians(toLat - fromLat)
    val dLng = Math.toRadians(toLng - fromLng)
    val lat1 = Math.toRadians(fromLat)
    val lat2 = Math.toRadians(toLat)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(lat1) * cos(lat2) * sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}

private fun formatDistance(meters: Double): String =
    if (meters < 1_000) {
        "${meters.roundToInt()} metros"
    } else {
        "${String.format("%.1f", meters / 1_000)} km"
    }

private fun openDirections(context: Context, origin: Location?) {
    val originParam = origin?.let { "&origin=${it.latitude},${it.longitude}" }.orEmpty()
    val uri = Uri.parse(
        "https://www.google.com/maps/dir/?api=1$originParam&destination=$STORE_LATITUDE,$STORE_LONGITUDE&travelmode=walking",
    )
    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
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
