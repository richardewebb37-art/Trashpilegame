#ifndef TRASHPILES_AUDIO_WRAPPER_H
#define TRASHPILES_AUDIO_WRAPPER_H

#include <oboe/Oboe.h>
#include <android/log.h>
#include <string>
#include <map>

#define LOG_TAG "TrashPiles-Audio"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace TrashPiles {

/**
 * Audio Wrapper - Interfaces with Oboe Audio Engine
 * Handles all audio playback for the game
 */
class AudioWrapper {
public:
    AudioWrapper();
    ~AudioWrapper();
    
    // Initialization
    bool initialize();
    void cleanup();
    
    // Sound effects
    void playSound(const char* soundName);
    void stopSound(const char* soundName);
    void stopAllSounds();
    
    // Background music
    void playMusic(const char* musicName, bool loop = true);
    void stopMusic();
    void pauseMusic();
    void resumeMusic();
    
    // Volume control
    void setSoundVolume(float volume);  // 0.0 to 1.0
    void setMusicVolume(float volume);  // 0.0 to 1.0
    void setMasterVolume(float volume); // 0.0 to 1.0
    
    // State
    bool isMusicPlaying() const;
    bool isSoundPlaying(const char* soundName) const;
    
private:
    std::shared_ptr<oboe::AudioStream> m_soundStream;
    std::shared_ptr<oboe::AudioStream> m_musicStream;
    
    float m_soundVolume;
    float m_musicVolume;
    float m_masterVolume;
    
    bool m_initialized;
    bool m_musicPlaying;
    
    // Asset management
    static void setAssetManager(AAssetManager* assetManager);
    
    // Audio data structures
    struct AudioData;
    std::map<std::string, AudioData*> m_loadedSounds;
    std::map<std::string, AudioData*> m_loadedMusic;
    
    // Loading methods
    bool loadSound(const std::string& soundName);
    bool loadMusic(const std::string& musicName);
};

} // namespace TrashPiles

#endif // TRASHPILES_AUDIO_WRAPPER_H