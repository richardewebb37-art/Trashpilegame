package com.trashpiles.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.IOException

/**
 * Asset Loader - Centralized asset loading and caching
 * 
 * Loads and caches all game assets (images, audio files)
 * Provides efficient access to assets throughout the game
 */
class AssetLoader(private val context: Context) {
    
    companion object {
        private const val TAG = "AssetLoader"
    }
    
    // Caches
    private val imageCache = mutableMapOf<String, Bitmap>()
    private val audioPathCache = mutableMapOf<String, String>()
    
    // Loading state
    private var isLoaded = false
    
    /**
     * Load all game assets
     */
    suspend fun loadAllAssets(onProgress: (Float) -> Unit = {}) {
        if (isLoaded) {
            Log.d(TAG, "Assets already loaded")
            return
        }
        
        Log.d(TAG, "Loading all assets...")
        
        val totalAssets = AssetPaths.getAllAssetPaths().size + AssetPaths.getAllAudioPaths().size
        var loadedCount = 0
        
        // Load all images
        AssetPaths.getAllAssetPaths().forEach { path ->
            try {
                loadImage(path)
                loadedCount++
                onProgress(loadedCount.toFloat() / totalAssets)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load image: $path", e)
            }
        }
        
        // Cache audio paths (actual loading happens on demand)
        AssetPaths.getAllAudioPaths().forEach { path ->
            audioPathCache[extractFileName(path)] = path
            loadedCount++
            onProgress(loadedCount.toFloat() / totalAssets)
        }
        
        isLoaded = true
        Log.d(TAG, "All assets loaded successfully")
    }
    
    /**
     * Load a single image from assets
     */
    fun loadImage(path: String): Bitmap? {
        // Check cache first
        imageCache[path]?.let { return it }
        
        return try {
            val inputStream = context.assets.open(path)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            // Cache the bitmap
            imageCache[path] = bitmap
            
            Log.d(TAG, "Loaded image: $path")
            bitmap
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load image: $path", e)
            null
        }
    }
    
    /**
     * Get a card image by rank and suit
     */
    fun getCardImage(rank: String, suit: String): Bitmap? {
        val path = AssetPaths.getCardImage(rank, suit)
        return getImage(path)
    }
    
    /**
     * Get card back image
     */
    fun getCardBackImage(): Bitmap? {
        return getImage(AssetPaths.CARD_BACK_IMAGE)
    }
    
    /**
     * Get button image
     */
    fun getButtonImage(buttonName: String): Bitmap? {
        val path = when (buttonName) {
            "deal" -> AssetPaths.DEAL_BUTTON
            "draw" -> AssetPaths.DRAW_BUTTON
            "play" -> AssetPaths.PLAY_BUTTON
            "menu" -> AssetPaths.MENU_BUTTON
            else -> return null
        }
        return getImage(path)
    }
    
    /**
     * Get background image
     */
    fun getBackgroundImage(backgroundName: String): Bitmap? {
        val path = when (backgroundName) {
            "game_table" -> AssetPaths.GAME_TABLE
            "menu" -> AssetPaths.MENU_BACKGROUND
            else -> return null
        }
        return getImage(path)
    }
    
    /**
     * Get UI element image
     */
    fun getUIImage(elementName: String): Bitmap? {
        val path = when (elementName) {
            "chip" -> AssetPaths.CHIP_INDICATOR
            "slot" -> AssetPaths.CARD_SLOT
            "placeholder" -> AssetPaths.CARD_PLACEHOLDER
            else -> return null
        }
        return getImage(path)
    }
    
    /**
     * Get particle image
     */
    fun getParticleImage(particleName: String): Bitmap? {
        val path = when (particleName) {
            "star" -> AssetPaths.STAR_PARTICLE
            "sparkle" -> AssetPaths.SPARKLE_PARTICLE
            else -> return null
        }
        return getImage(path)
    }
    
    /**
     * Get image from cache or load it
     */
    fun getImage(path: String): Bitmap? {
        return imageCache[path] ?: loadImage(path)
    }
    
    /**
     * Get audio file path
     */
    fun getAudioPath(soundId: String): String? {
        return audioPathCache[soundId] ?: when (soundId) {
            "card_flip" -> AssetPaths.CARD_FLIP_SOUND
            "card_deal" -> AssetPaths.CARD_DEAL_SOUND
            "card_place" -> AssetPaths.CARD_PLACE_SOUND
            "button_click" -> AssetPaths.BUTTON_CLICK_SOUND
            "victory" -> AssetPaths.VICTORY_SOUND
            "background_music" -> AssetPaths.BACKGROUND_MUSIC
            else -> null
        }
    }
    
    /**
     * Check if an asset exists
     */
    fun assetExists(path: String): Boolean {
        return try {
            context.assets.open(path).use { true }
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * Get all loaded image paths
     */
    fun getLoadedImages(): List<String> {
        return imageCache.keys.toList()
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): CacheStats {
        val imageSizeBytes = imageCache.values.sumOf { 
            it.byteCount.toLong()
        }
        
        return CacheStats(
            imageCount = imageCache.size,
            audioCount = audioPathCache.size,
            totalImageSizeBytes = imageSizeBytes,
            totalImageSizeMB = imageSizeBytes / (1024 * 1024)
        )
    }
    
    /**
     * Clear all caches
     */
    fun clearCache() {
        imageCache.values.forEach { it.recycle() }
        imageCache.clear()
        audioPathCache.clear()
        isLoaded = false
        Log.d(TAG, "Cache cleared")
    }
    
    /**
     * Clear image cache only
     */
    fun clearImageCache() {
        imageCache.values.forEach { it.recycle() }
        imageCache.clear()
        Log.d(TAG, "Image cache cleared")
    }
    
    /**
     * Preload specific assets (for optimization)
     */
    fun preloadAssets(paths: List<String>) {
        paths.forEach { path ->
            loadImage(path)
        }
    }
    
    /**
     * Extract file name from path
     */
    private fun extractFileName(path: String): String {
        return path.substringAfterLast('/').substringBeforeLast('.')
    }
    
    /**
     * Cache statistics data class
     */
    data class CacheStats(
        val imageCount: Int,
        val audioCount: Int,
        val totalImageSizeBytes: Long,
        val totalImageSizeMB: Long
    )
}