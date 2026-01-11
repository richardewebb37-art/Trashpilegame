package com.trashpiles.presentation.screens.rules

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Rules screen - Game instructions
 */
@Composable
fun RulesScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Rules",
            style = MaterialTheme.typography.displayMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Trash Piles game rules will be displayed here")
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}