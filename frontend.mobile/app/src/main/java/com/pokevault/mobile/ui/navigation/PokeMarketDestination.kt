package com.pokevault.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

sealed class PokeMarketDestination(
    val route: String,
    val label: String,
    val icon: ImageVector? = null,
) {
    data object Home : PokeMarketDestination("home", "Inicio", Icons.Outlined.Home)
    data object Search : PokeMarketDestination("search", "Buscar", Icons.Outlined.Explore)
    data object Cart : PokeMarketDestination("cart", "Carrito", Icons.Outlined.ShoppingBag)
    data object Profile : PokeMarketDestination("profile", "Ingresar", Icons.Outlined.Person)
    data object Pickup : PokeMarketDestination("pickup", "Retiro", Icons.Outlined.LocationOn)
    data object Detail : PokeMarketDestination("detail/{cardId}", "Detalle") {
        fun createRoute(cardId: Int) = "detail/$cardId"
    }
}

val bottomDestinations = listOf(
    PokeMarketDestination.Home,
    PokeMarketDestination.Search,
    PokeMarketDestination.Cart,
    PokeMarketDestination.Profile,
)
