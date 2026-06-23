package com.pokevault.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector
import com.pokevault.mobile.R

sealed class PokeMarketDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector? = null,
) {
    data object Splash : PokeMarketDestination("splash", R.string.nav_splash)
    data object Onboarding : PokeMarketDestination("onboarding", R.string.nav_onboarding)
    data object Home : PokeMarketDestination("home", R.string.nav_home, Icons.Outlined.Home)
    data object Search : PokeMarketDestination("search", R.string.nav_search, Icons.Outlined.Explore)
    data object Cart : PokeMarketDestination("cart", R.string.nav_cart, Icons.Outlined.ShoppingBag)
    data object Profile : PokeMarketDestination("profile", R.string.nav_profile, Icons.Outlined.Person)
    data object Pickup : PokeMarketDestination("pickup", R.string.nav_pickup, Icons.Outlined.LocationOn)
    data object Detail : PokeMarketDestination("detail/{cardId}", R.string.nav_detail) {
        fun createRoute(cardId: Int) = "detail/$cardId"
    }
}

val bottomDestinations = listOf(
    PokeMarketDestination.Home,
    PokeMarketDestination.Search,
    PokeMarketDestination.Cart,
    PokeMarketDestination.Profile,
)
