package com.vlr.gsheetsync.android

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.vlr.gsheetsync.SyncLog
import com.vlr.gsheetsync.android.screen.HomeScreen
import com.vlr.gsheetsync.android.screen.LaunchScreen
import com.vlr.gsheetsync.android.screen.model.Screen

@Composable
fun AppScaffold() {

    val navController = rememberNavController()

    Scaffold {
        AppNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize().padding(it)
        )
    }

}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val account = GoogleSignIn.getLastSignedInAccount(LocalContext.current)
    // The StartDestination here is not complete! We should setup the Library Service if account is not null
    val startDestination = Screen.LAUNCHER.route
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Screen.LAUNCHER.route) {
            LaunchScreen(
                onConnectionSuccess = {
                    navController.navigate(Screen.HOME.route) {
                        // This removes LAUNCHER from back stack so back button won't return to it
                        popUpTo(Screen.LAUNCHER.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.HOME.route) {
            SyncLog.print("Open HOME")
            HomeScreen()
        }
    }
}