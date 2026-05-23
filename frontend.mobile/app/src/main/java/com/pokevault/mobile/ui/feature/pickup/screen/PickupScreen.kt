package com.pokevault.mobile.ui.feature.pickup.screen

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted

@Composable
fun PickupScreen(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101116))
            .padding(contentPadding)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = Color.White) }
            Column {
                Text("RETIRO EN PAQUETERIA", color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text("ORDEN #PKM-A7X2", color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.weight(1f))
            Text("LISTO EN SEDE", color = MarketOrange, style = MaterialTheme.typography.labelSmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = MarketOrange, contentColor = Color.Black),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f),
            ) { Text("Mapa Vectorialmente\nSimulado", style = MaterialTheme.typography.labelSmall) }
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1D23), contentColor = Color.White),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f),
            ) { Text("Google Maps\nActivar", style = MaterialTheme.typography.labelSmall) }
        }
        Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(Color(0xFF17181F), RoundedCornerShape(2.dp))) {
            SimulatedMap(Modifier.fillMaxSize())
            Card(
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(50),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
            ) {
                Text("Distancia al punto de 420\nmetros", color = Color.White, modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp), style = MaterialTheme.typography.labelSmall)
            }
            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MarketOrange, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 70.dp).size(42.dp))
        }
        Spacer(Modifier.height(18.dp))
        Row {
            Column(modifier = Modifier.weight(1f)) {
                Text("PUNTO OFICIAL DE RETIRO UADE", color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text("UADE (Sede Principal Monserrat - Edificio Lima)", color = Color.White)
                Text("Lima 757, Ciudad Autonoma de Buenos Aires, Argentina", color = MarketOrange, fontWeight = FontWeight.ExtraBold)
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
                    "Requisitos para Retiro:\nPresenta DNI del interesado y el codigo QR de autorizacion o el numero de transaccion #PKM-A7X2.",
                    color = Color.White,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = MarketOrange, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Icon(Icons.Outlined.Route, null)
            Text("SIMULAR RUTA MAS CORTA", fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun SimulatedMap(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val grid = Color.White.copy(alpha = 0.08f)
        for (x in 0..size.width.toInt() step 28) {
            drawLine(grid, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height))
        }
        for (y in 0..size.height.toInt() step 28) {
            drawLine(grid, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
        }
        drawLine(
            color = Color(0xFFFFB000),
            start = Offset(40f, size.height * 0.72f),
            end = Offset(size.width * 0.72f, size.height * 0.45f),
            strokeWidth = 4f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
        )
        drawCircle(Color(0xFF00C08B), radius = 14f, center = Offset(42f, size.height * 0.72f))
        drawCircle(MarketOrange, radius = 18f, center = Offset(size.width * 0.72f, size.height * 0.45f), style = Stroke(width = 5f))
    }
}
