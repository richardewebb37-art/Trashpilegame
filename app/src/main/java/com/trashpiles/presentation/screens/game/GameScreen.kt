package com.trashpiles.presentation.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Game screen - Main gameplay area
 */
@Composable
fun GameScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Screen",
            style = MaterialTheme.typography.displayMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Game board will be rendered here using your native renderer")
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(onClick = { navController.popBackStack() }) {
            Text("Back to Menu")
        }
    }
}