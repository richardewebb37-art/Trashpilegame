package com.trashpiles

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for Trash Piles
 * Initializes Hilt dependency injection
 */
@HiltAndroidApp
class TrashPilesApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize app-wide components here
    }
}