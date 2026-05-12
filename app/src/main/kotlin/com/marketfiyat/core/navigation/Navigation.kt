package com.marketfiyat.core.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marketfiyat.feature.addprice.ui.AddPriceScreen
import com.marketfiyat.feature.archive.ui.ArchiveScreen
import com.marketfiyat.feature.barcode.ui.BarcodeScannerScreen
import com.marketfiyat.feature.compare.ui.MarketCompareScreen
import com.marketfiyat.feature.home.ui.HomeScreen
import com.marketfiyat.feature.ocr.ui.OcrScannerScreen
import com.marketfiyat.feature.settings.ui.SettingsScreen
import com.marketfiyat.feature.shoppinglist.ui.ShoppingListDetailScreen
import com.marketfiyat.feature.shoppinglist.ui.ShoppingListScreen
import com.marketfiyat.feature.statistics.ui.StatisticsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Archive : Screen("archive")
    data object Statistics : Screen("statistics")
    data object ShoppingList : Screen("shopping_list")
    data object Settings : Screen("settings")
    data object AddPrice : Screen("add_price?productId={productId}&barcode={barcode}") {
        fun createRoute(productId: Long? = null, barcode: String? = null): String {
            val pid = productId?.let { "?productId=$it" } ?: ""
            val bc = barcode?.let { "&barcode=$it" } ?: ""
            return "add_price$pid$bc"
        }
    }
    data object MarketCompare : Screen("market_compare/{productId}") {
        fun createRoute(productId: Long) = "market_compare/$productId"
    }
    data object ShoppingListDetail : Screen("shopping_list_detail/{listId}") {
        fun createRoute(listId: Long) = "shopping_list_detail/$listId"
    }
    data object BarcodeScanner : Screen("barcode_scanner")
    data object OcrScanner : Screen("ocr_scanner")
}

@Composable
fun MarketFiyatNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddPrice = { navController.navigate(Screen.AddPrice.createRoute()) },
                onNavigateToArchive = { navController.navigate(Screen.Archive.route) },
                onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                onNavigateToShoppingList = { navController.navigate(Screen.ShoppingList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToCompare = { productId ->
                    navController.navigate(Screen.MarketCompare.createRoute(productId))
                }
            )
        }

        composable(
            route = Screen.AddPrice.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("barcode") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId")
                ?.takeIf { it != -1L }
            val barcode = backStackEntry.arguments?.getString("barcode")
            val ocrResult = backStackEntry.savedStateHandle.get<String>("ocr_result")
            val barcodeResult = backStackEntry.savedStateHandle.get<String>("barcode")
            AddPriceScreen(
                productId = productId,
                initialBarcode = barcode ?: barcodeResult,
                initialOcrResult = ocrResult,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBarcode = { navController.navigate(Screen.BarcodeScanner.route) },
                onNavigateToOcr = { navController.navigate(Screen.OcrScanner.route) }
            )
        }

        composable(Screen.Archive.route) {
            ArchiveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddPrice = { productId ->
                    navController.navigate(Screen.AddPrice.createRoute(productId = productId))
                },
                onNavigateToCompare = { productId ->
                    navController.navigate(Screen.MarketCompare.createRoute(productId))
                }
            )
        }

        composable(
            route = Screen.MarketCompare.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            MarketCompareScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ShoppingList.route) {
            ShoppingListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { listId ->
                    navController.navigate(Screen.ShoppingListDetail.createRoute(listId))
                }
            )
        }

        composable(
            route = Screen.ShoppingListDetail.route,
            arguments = listOf(
                navArgument("listId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong("listId") ?: 0L
            ShoppingListDetailScreen(
                listId = listId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BarcodeScanner.route) {
            BarcodeScannerScreen(
                onBarcodeDetected = { barcode ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("barcode", barcode)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OcrScanner.route) {
            OcrScannerScreen(
                onResultReceived = { result ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("ocr_result", result)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
