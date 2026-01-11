package com.trashpiles.native

/**
 * JNI Bridge to Oboe Audio Engine
 * Handles all audio playback
 */
class AudioEngineBridge {
    
    // Initialization
    external fun initAudioEngine(): Boolean
    external fun cleanup()
    
    // Sound effects
    external fun playSound(soundName: String)
    external fun stopSound(soundName: String)
    external fun stopAllSounds()
    
    // Background music
    external fun playMusic(musicName: String, loop: Boolean)
    external fun stopMusic()
    external fun pauseMusic()
    external fun resumeMusic()
    
    // Volume control
    external fun setSoundVolume(volume: Float)  // 0.0 to 1.0
    external fun setMusicVolume(volume: Float)  // 0.0 to 1.0
    external fun setMasterVolume(volume: Float) // 0.0 to 1.0
    
    // State
    external fun isMusicPlaying(): Boolean
    
    companion object {
        init {
            // Library loaded by NativeEngineWrapper
        }
    }
}