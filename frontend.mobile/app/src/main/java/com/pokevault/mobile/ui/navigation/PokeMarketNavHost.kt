package com.pokevault.mobile.ui.navigation

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pokevault.mobile.ui.feature.cart.screen.CartScreen
import com.pokevault.mobile.ui.feature.cart.viewmodel.CartViewModel
import com.pokevault.mobile.ui.feature.home.screen.HomeScreen
import com.pokevault.mobile.ui.feature.pickup.screen.PickupScreen
import com.pokevault.mobile.ui.feature.profile.screen.ProfileScreen
import com.pokevault.mobile.ui.feature.search.screen.SearchScreen
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted

@Composable
fun PokeMarketNavHost() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = hiltViewModel()
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    
    // Observamos la entrada actual de la pila para reaccionar a cambios de ruta
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                bottomDestinations.forEach { screen ->
                    // Selección precisa: coincide la ruta actual o estamos en una sub-pantalla (como Pickup)
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true ||
                                   (screen == PokeMarketDestination.Profile && currentDestination?.route == PokeMarketDestination.Pickup.route)
                    
                    NavigationBarItem(
                        selected = isSelected,
                        label = { 
                            Text(
                                text = screen.label, 
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (screen == PokeMarketDestination.Cart && cartState.totalQuantity > 0) {
                                        Badge(containerColor = MarketOrange, contentColor = Color.Black) {
                                            Text(cartState.totalQuantity.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = screen.icon, 
                                    contentDescription = screen.label
                                )
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color.Black,
                            indicatorColor = MarketOrange, // Sombreado naranja
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                // Limpia la pila para evitar duplicados y que el sombreado se "trabe"
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PokeMarketDestination.Home.route,
        ) {
            composable(PokeMarketDestination.Home.route) {
                HomeScreen(
                    contentPadding = innerPadding,
                    onOpenSearch = { 
                        navController.navigate(PokeMarketDestination.Search.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(PokeMarketDestination.Search.route) {
                SearchScreen(contentPadding = innerPadding)
            }
            composable(PokeMarketDestination.Cart.route) {
                CartScreen(
                    contentPadding = innerPadding,
                    viewModel = cartViewModel,
                    onExploreCards = { 
                        navController.navigate(PokeMarketDestination.Search.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(PokeMarketDestination.Profile.route) {
                ProfileScreen(
                    contentPadding = innerPadding,
                    onOpenPickup = { 
                        // El retiro es un detalle del perfil, se navega simple
                        navController.navigate(PokeMarketDestination.Pickup.route) 
                    }
                )
            }
            composable(PokeMarketDestination.Pickup.route) {
                PickupScreen(contentPadding = innerPadding)
            }
        }
    }
}
