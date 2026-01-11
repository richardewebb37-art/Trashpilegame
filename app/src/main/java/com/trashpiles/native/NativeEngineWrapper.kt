package com.trashpiles.native

/**
 * Main wrapper - loads the native library
 * Individual engine bridges are in separate files
 */
object NativeEngineWrapper {
    init {
        System.loadLibrary("trash-piles-native")
    }
}