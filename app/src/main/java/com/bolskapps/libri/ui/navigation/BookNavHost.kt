package com.bolskapps.libri.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bolskapps.libri.R
import com.bolskapps.libri.ui.addbook.AddBookScreen
import com.bolskapps.libri.ui.components.LibriBottomBar
import com.bolskapps.libri.ui.components.LibriNavItem
import com.bolskapps.libri.ui.dashboard.DashboardScreen
import com.bolskapps.libri.ui.pending.PendingScreen
import com.bolskapps.libri.ui.search.SearchScreen
import com.bolskapps.libri.ui.shelf.ShelfScreen
import com.bolskapps.libri.ui.theme.Libri

@Composable
fun BookNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navItems = listOf(
        LibriNavItem(
            route = BookDestinations.DASHBOARD_ROUTE,
            label = stringResource(R.string.nav_dashboard),
            icon = Icons.Outlined.Dashboard
        ),
        LibriNavItem(
            route = BookDestinations.WISHLIST_ROUTE,
            label = stringResource(R.string.nav_wishlist),
            icon = Icons.Outlined.BookmarkAdd
        ),
        LibriNavItem(
            route = BookDestinations.PENDING_ROUTE,
            label = stringResource(R.string.nav_pending),
            icon = Icons.Outlined.AutoStories
        ),
        LibriNavItem(
            route = BookDestinations.HISTORY_ROUTE,
            label = stringResource(R.string.nav_history),
            icon = Icons.Outlined.HistoryEdu
        )
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    // Route patterns carry their optional query placeholder; strip it so the bar can
    // match on the plain route the nav items are declared with.
    val currentRoute = backStackEntry?.destination?.route?.substringBefore('?')

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Libri.Background,
        bottomBar = {
            LibriBottomBar(
                items = navItems,
                currentRoute = currentRoute,
                onSelect = { route -> navController.navigateToTab(route) }
            )
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = BookDestinations.DASHBOARD_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(route = BookDestinations.DASHBOARD_ROUTE) {
                DashboardScreen(
                    onSearch = { navController.navigate(BookDestinations.searchRoute()) },
                    onSeeCollection = { navController.navigateToTab(BookDestinations.PENDING_ROUTE) },
                    // Category tiles are shortcuts into Open Library subject searches.
                    onCategory = { subject ->
                        navController.navigate(BookDestinations.searchRoute(subject))
                    }
                )
            }

            composable(route = BookDestinations.PENDING_ROUTE) {
                PendingScreen(
                    onSearch = { navController.navigate(BookDestinations.searchRoute()) }
                )
            }

            composable(
                route = BookDestinations.WISHLIST_PATTERN,
                arguments = listOf(
                    navArgument(BookDestinations.STATUS_ARG) {
                        type = NavType.StringType
                        defaultValue = BookDestinations.WISHLIST_STATUS
                    }
                )
            ) {
                ShelfScreen(
                    onSearch = { navController.navigate(BookDestinations.searchRoute()) }
                )
            }

            composable(
                route = BookDestinations.HISTORY_PATTERN,
                arguments = listOf(
                    navArgument(BookDestinations.STATUS_ARG) {
                        type = NavType.StringType
                        defaultValue = BookDestinations.HISTORY_STATUS
                    }
                )
            ) {
                ShelfScreen(
                    onSearch = { navController.navigate(BookDestinations.searchRoute()) }
                )
            }

            composable(
                route = BookDestinations.SEARCH_PATTERN,
                arguments = listOf(
                    navArgument(BookDestinations.QUERY_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAddManually = { navController.navigate(BookDestinations.ADD_BOOK_ROUTE) }
                )
            }

            composable(route = BookDestinations.ADD_BOOK_ROUTE) {
                AddBookScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/** Standard bottom-bar behaviour: one entry per tab, state preserved across switches. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
