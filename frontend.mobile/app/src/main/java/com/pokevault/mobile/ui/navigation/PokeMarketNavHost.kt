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
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pokevault.mobile.ui.feature.cart.screen.CartScreen
import com.pokevault.mobile.ui.feature.cart.viewmodel.CartViewModel
import com.pokevault.mobile.ui.feature.detail.screen.DetailScreen
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            // Mostrar BottomBar en destinos principales y también en el detalle para navegación libre
            val currentRoute = currentDestination?.route
            val isDetail = currentRoute?.contains("detail") == true
            val isBottomDest = bottomDestinations.any { it.route == currentRoute }

            if (isBottomDest || isDetail) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomDestinations.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true ||
                                       (screen == PokeMarketDestination.Profile && currentRoute == PokeMarketDestination.Pickup.route)

                        NavigationBarItem(
                            selected = isSelected,
                            label = {
                                Text(
                                    text = stringResource(screen.labelRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
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
                                    Icon(screen.icon!!, contentDescription = stringResource(screen.labelRes))
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color.Black,
                                indicatorColor = MarketOrange,
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
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
                        navController.navigate(PokeMarketDestination.Search.route)
                    },
                    onOpenLogin = {
                        navController.navigate(PokeMarketDestination.Profile.route)
                    },
                    onCardClick = { cardId ->
                        navController.navigate(PokeMarketDestination.Detail.createRoute(cardId))
                    }
                )
            }
            composable(PokeMarketDestination.Search.route) {
                SearchScreen(
                    contentPadding = innerPadding,
                    onOpenLogin = {
                        navController.navigate(PokeMarketDestination.Profile.route)
                    },
                    onCardClick = { cardId ->
                        navController.navigate(PokeMarketDestination.Detail.createRoute(cardId))
                    }
                )
            }
            composable(
                route = PokeMarketDestination.Detail.route,
                arguments = listOf(navArgument("cardId") { type = NavType.IntType })
            ) {
                DetailScreen(
                    contentPadding = innerPadding,
                    onOpenLogin = {
                        navController.navigate(PokeMarketDestination.Profile.route)
                    },
                )
            }
            composable(PokeMarketDestination.Cart.route) {
                CartScreen(
                    contentPadding = innerPadding,
                    viewModel = cartViewModel,
                    onExploreCards = {
                        navController.navigate(PokeMarketDestination.Search.route)
                    }
                )
            }
            composable(PokeMarketDestination.Profile.route) {
                ProfileScreen(
                    contentPadding = innerPadding,
                    onOpenPickup = {
                        navController.navigate(PokeMarketDestination.Pickup.route)
                    }
                )
            }
            composable(PokeMarketDestination.Pickup.route) {
                PickupScreen(
                    contentPadding = innerPadding,
                    onClose = { navController.popBackStack() },
                )
            }
        }
    }
}
