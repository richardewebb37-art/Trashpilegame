package com.trashpiles.native

/**
 * JNI Bridge to Game Engine
 * Provides native support functions for libGDX
 */
class GameEngineBridge {
    
    // Initialization
    external fun initGameEngine(): Boolean
    external fun cleanup()
    
    // Game loop
    external fun update(deltaTime: Float)
    
    // Input handling
    external fun handleTouchDown(x: Float, y: Float)
    external fun handleTouchUp(x: Float, y: Float)
    external fun handleTouchMove(x: Float, y: Float)
    
    // Utility
    external fun getDeltaTime(): Float
    external fun getFPS(): Int
    
    companion object {
        init {
            // Library loaded by NativeEngineWrapper
        }
    }
}