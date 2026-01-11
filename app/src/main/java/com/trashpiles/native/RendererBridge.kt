package com.trashpiles.native

/**
 * JNI Bridge to Skia Renderer
 * Handles all rendering operations
 */
class RendererBridge {
    
    // Initialization
    external fun initRenderer(width: Int, height: Int): Boolean
    external fun cleanup()
    
    // Frame rendering
    external fun beginFrame()
    external fun endFrame()
    external fun clear(r: Float, g: Float, b: Float, a: Float)
    
    // Card rendering
    external fun renderCard(
        cardId: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        faceUp: Boolean
    )
    
    external fun renderCardBack(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    )
    
    // UI rendering
    external fun renderButton(
        buttonId: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    )
    
    external fun renderText(
        text: String,
        x: Float,
        y: Float,
        size: Float
    )
    
    // Animation support
    external fun setCardRotation(cardId: Int, angle: Float)
    external fun setCardScale(cardId: Int, scaleX: Float, scaleY: Float)
    external fun setCardAlpha(cardId: Int, alpha: Float)
    
    companion object {
        init {
            // Library loaded by NativeEngineWrapper
        }
    }
}