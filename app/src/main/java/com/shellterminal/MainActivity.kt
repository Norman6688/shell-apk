package com.shellterminal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.shellterminal.domain.model.SSHHost
import com.shellterminal.presentation.home.HomeScreen
import com.shellterminal.presentation.hosteditor.HostEditorScreen
import com.shellterminal.presentation.theme.ShellTerminalTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShellTerminalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val gson = Gson()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToEditor = { host ->
                    if (host != null) {
                        val json = java.net.URLEncoder.encode(gson.toJson(host), "UTF-8")
                        navController.navigate("editor/$json")
                    } else {
                        navController.navigate("editor/new")
                    }
                }
            )
        }

        composable(
            route = "editor/{hostJson}",
            arguments = listOf(navArgument("hostJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val hostJson = backStackEntry.arguments?.getString("hostJson") ?: "new"
            val host = if (hostJson != "new") {
                try {
                    val decoded = java.net.URLDecoder.decode(hostJson, "UTF-8")
                    gson.fromJson(decoded, SSHHost::class.java)
                } catch (e: Exception) {
                    null
                }
            } else null

            HostEditorScreen(
                initialHost = host,
                onSave = { savedHost ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("saved_host", gson.toJson(savedHost))
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}