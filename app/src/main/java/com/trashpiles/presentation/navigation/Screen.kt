package com.trashpiles.presentation.navigation

/**
 * Sealed class representing all navigation destinations
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Game : Screen("game")
    object Settings : Screen("settings")
    object Rules : Screen("rules")
    object Stats : Screen("stats")
}