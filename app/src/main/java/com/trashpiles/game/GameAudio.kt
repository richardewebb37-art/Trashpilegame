package com.trashpiles.game

import com.trashpiles.gcms.*
import com.trashpiles.native.AudioEngineBridge
import com.trashpiles.utils.AssetLoader
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import android.util.Log

/**
 * Game Audio - Connects GCMS events to Oboe audio engine
 * 
 * Subscribes to GCMS events and triggers audio playback
 * Maps game events to sound effects and music
 */
class GameAudio(
    private val gcms: GCMSController,
    private val audioBridge: AudioEngineBridge,
    private val assetLoader: AssetLoader
) {
    
    companion object {
        private const val TAG = "GameAudio"
    }
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var eventJob: Job? = null
    
    // Audio settings
    private var soundEnabled = true
    private var musicEnabled = true
    private var soundVolume = 1.0f
    private var musicVolume = 0.7f
    
    /**
     * Start listening to GCMS events
     */
    fun start() {
        // Load all sounds
        loadSounds()
        
        // Start background music
        if (musicEnabled) {
            playBackgroundMusic()
        }
        
        // Subscribe to events
        eventJob = scope.launch {
            gcms.events.collect { event ->
                handleEvent(event)
            }
        }
    }
    
    /**
     * Stop listening to events
     */
    fun stop() {
        eventJob?.cancel()
        stopBackgroundMusic()
    }
    
    /**
     * Load all sound files
     */
    private fun loadSounds() {
        Log.d(TAG, "Loading sounds...")
        
        val sounds = mapOf(
            "card_flip" to assetLoader.getAudioPath("card_flip"),
            "card_deal" to assetLoader.getAudioPath("card_deal"),
            "card_place" to assetLoader.getAudioPath("card_place"),
            "button_click" to assetLoader.getAudioPath("button_click"),
            "victory" to assetLoader.getAudioPath("victory"),
            "background_music" to assetLoader.getAudioPath("background_music")
        )
        
        sounds.forEach { (soundId, path) ->
            if (path != null) {
                audioBridge.loadSound(soundId, path)
                Log.d(TAG, "Loaded sound: $soundId")
            } else {
                Log.w(TAG, "Sound path not found: $soundId")
            }
        }
    }
    
    /**
     * Handle GCMS events and trigger audio
     */
    private fun handleEvent(event: GCMSEvent) {
        if (!soundEnabled) return
        
        when (event) {
            is GCMSEvent.GameStarted -> {
                Log.d(TAG, "Game started")
                playSound("button_click")
            }
            
            is GCMSEvent.CardDealt -> {
                playSound("card_deal", volume = 0.5f)
            }
            
            is GCMSEvent.CardDrawn -> {
                Log.d(TAG, "Card drawn")
                playSound("card_draw")
            }
            
            is GCMSEvent.CardPlaced -> {
                Log.d(TAG, "Card placed")
                playSound("card_place")
            }
            
            is GCMSEvent.CardFlipped -> {
                Log.d(TAG, "Card flipped")
                playSound("card_flip")
            }
            
            is GCMSEvent.CardDiscarded -> {
                Log.d(TAG, "Card discarded")
                playSound("card_place", volume = 0.7f)
            }
            
            is GCMSEvent.TurnStarted -> {
                Log.d(TAG, "Turn started")
                playSound("button_click", volume = 0.3f)
            }
            
            is GCMSEvent.RoundWon -> {
                Log.d(TAG, "Round won!")
                playSound("victory")
            }
            
            is GCMSEvent.GameEnded -> {
                Log.d(TAG, "Game ended")
                playSound("victory", volume = 0.8f)
            }
            
            is GCMSEvent.PlaySound -> {
                Log.d(TAG, "Play sound: ${event.soundId}")
                playSound(event.soundId, event.volume)
            }
            
            is GCMSEvent.CommandRejected -> {
                Log.d(TAG, "Invalid move")
                // Play error sound (if we have one)
                // playSound("error")
            }
            
            else -> {
                // Other events don't have sounds
            }
        }
    }
    
    /**
     * Play a sound effect
     */
    private fun playSound(soundId: String, volume: Float = soundVolume) {
        if (!soundEnabled) return
        
        try {
            audioBridge.playSound(soundId, volume)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play sound: $soundId", e)
        }
    }
    
    /**
     * Play background music
     */
    private fun playBackgroundMusic() {
        if (!musicEnabled) return
        
        try {
            // TODO: Implement looping background music
            // audioBridge.playMusic("background_music", musicVolume, loop = true)
            Log.d(TAG, "Background music started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play background music", e)
        }
    }
    
    /**
     * Stop background music
     */
    private fun stopBackgroundMusic() {
        try {
            // TODO: Implement music stop
            // audioBridge.stopMusic()
            Log.d(TAG, "Background music stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop background music", e)
        }
    }
    
    /**
     * Pause background music
     */
    fun pauseMusic() {
        try {
            // TODO: Implement music pause
            // audioBridge.pauseMusic()
            Log.d(TAG, "Background music paused")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause background music", e)
        }
    }
    
    /**
     * Resume background music
     */
    fun resumeMusic() {
        if (!musicEnabled) return
        
        try {
            // TODO: Implement music resume
            // audioBridge.resumeMusic()
            Log.d(TAG, "Background music resumed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume background music", e)
        }
    }
    
    /**
     * Enable/disable sound effects
     */
    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
        Log.d(TAG, "Sound effects ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Enable/disable background music
     */
    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        
        if (enabled) {
            playBackgroundMusic()
        } else {
            stopBackgroundMusic()
        }
        
        Log.d(TAG, "Background music ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Set sound effects volume
     */
    fun setSoundVolume(volume: Float) {
        soundVolume = volume.coerceIn(0f, 1f)
        Log.d(TAG, "Sound volume set to $soundVolume")
    }
    
    /**
     * Set music volume
     */
    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        // TODO: Update current music volume
        // audioBridge.setMusicVolume(musicVolume)
        Log.d(TAG, "Music volume set to $musicVolume")
    }
    
    /**
     * Get current audio settings
     */
    fun getSettings(): AudioSettings {
        return AudioSettings(
            soundEnabled = soundEnabled,
            musicEnabled = musicEnabled,
            soundVolume = soundVolume,
            musicVolume = musicVolume
        )
    }
    
    /**
     * Apply audio settings
     */
    fun applySettings(settings: AudioSettings) {
        setSoundEnabled(settings.soundEnabled)
        setMusicEnabled(settings.musicEnabled)
        setSoundVolume(settings.soundVolume)
        setMusicVolume(settings.musicVolume)
    }
    
    /**
     * Clean up resources
     */
    fun destroy() {
        stop()
        scope.cancel()
    }
    
    /**
     * Audio settings data class
     */
    data class AudioSettings(
        val soundEnabled: Boolean = true,
        val musicEnabled: Boolean = true,
        val soundVolume: Float = 1.0f,
        val musicVolume: Float = 0.7f
    )
}