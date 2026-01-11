package com.trashpiles.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trashpiles.presentation.screens.home.HomeScreen
import com.trashpiles.presentation.screens.game.GameScreen
import com.trashpiles.presentation.screens.settings.SettingsScreen
import com.trashpiles.presentation.screens.rules.RulesScreen
import com.trashpiles.presentation.screens.stats.StatsScreen

/**
 * Main navigation graph for Trash Piles
 */
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Game.route) {
            GameScreen(navController = navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        
        composable(Screen.Rules.route) {
            RulesScreen(navController = navController)
        }
        
        composable(Screen.Stats.route) {
            StatsScreen(navController = navController)
        }
    }
}