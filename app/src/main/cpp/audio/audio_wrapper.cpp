#include "audio_wrapper.h"
#include <oboe/Oboe.h>
#include <android/log.h>
#include <string>
#include <map>
#include <vector>
#include <cstring>
#include <cmath>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

namespace TrashPiles {

// Audio data structure
struct AudioData {
    std::vector<float> samples;
    bool isLoaded = false;
    int currentSample = 0;
    bool isLooping = false;
    float volume = 1.0f;
};

// Static instance for asset manager access
static AAssetManager* g_assetManager = nullptr;

// Audio callback for sound effects
class SoundCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream* audioStream,
        void* audioData,
        int32_t numFrames) override {
        
        float* outputData = static_cast<float*>(audioData);
        
        // Clear buffer
        std::memset(outputData, 0, numFrames * audioStream->getChannelCount() * sizeof(float));
        
        // Mix all playing sounds
        for (auto& pair : m_playingSounds) {
            AudioData* audioData = pair.second;
            if (!audioData->isLoaded || audioData->samples.empty()) continue;
            
            for (int frame = 0; frame < numFrames; ++frame) {
                if (audioData->currentSample >= static_cast<int>(audioData->samples.size())) {
                    if (audioData->isLooping) {
                        audioData->currentSample = 0;
                    } else {
                        continue; // Sound finished
                    }
                }
                
                float sample = audioData->samples[audioData->currentSample++];
                sample *= audioData->volume * m_masterVolume;
                
                // Mix to stereo channels
                outputData[frame * 2] += sample;     // Left
                outputData[frame * 2 + 1] += sample; // Right
                
                // Prevent clipping
                outputData[frame * 2] = std::max(-1.0f, std::min(1.0f, outputData[frame * 2]));
                outputData[frame * 2 + 1] = std::max(-1.0f, std::min(1.0f, outputData[frame * 2 + 1]));
            }
        }
        
        return oboe::DataCallbackResult::Continue;
    }
    
    void addPlayingSound(const std::string& name, AudioData* data) {
        m_playingSounds[name] = data;
    }
    
    void removePlayingSound(const std::string& name) {
        m_playingSounds.erase(name);
    }
    
    void setMasterVolume(float volume) {
        m_masterVolume = volume;
    }
    
private:
    std::map<std::string, AudioData*> m_playingSounds;
    float m_masterVolume = 1.0f;
};

// Music callback for background music
class MusicCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream* audioStream,
        void* audioData,
        int32_t numFrames) override {
        
        float* outputData = static_cast<float*>(audioData);
        
        if (!m_currentMusic || !m_currentMusic->isLoaded || m_currentMusic->samples.empty()) {
            std::memset(outputData, 0, numFrames * audioStream->getChannelCount() * sizeof(float));
            return oboe::DataCallbackResult::Continue;
        }
        
        for (int frame = 0; frame < numFrames; ++frame) {
            if (m_currentMusic->currentSample >= static_cast<int>(m_currentMusic->samples.size())) {
                if (m_currentMusic->isLooping) {
                    m_currentMusic->currentSample = 0;
                } else {
                    m_currentMusic = nullptr;
                    std::memset(outputData + frame * 2, 0, (numFrames - frame) * 2 * sizeof(float));
                    break;
                }
            }
            
            float sample = m_currentMusic->samples[m_currentMusic->currentSample++];
            sample *= m_currentMusic->volume * m_masterVolume;
            
            outputData[frame * 2] = sample;     // Left
            outputData[frame * 2 + 1] = sample; // Right
        }
        
        return oboe::DataCallbackResult::Continue;
    }
    
    void setMusic(AudioData* data) {
        m_currentMusic = data;
    }
    
    void stop() {
        m_currentMusic = nullptr;
    }
    
    void setMasterVolume(float volume) {
        m_masterVolume = volume;
    }
    
private:
    AudioData* m_currentMusic = nullptr;
    float m_masterVolume = 1.0f;
};

static SoundCallback g_soundCallback;
static MusicCallback g_musicCallback;

AudioWrapper::AudioWrapper() 
    : m_soundVolume(1.0f), 
      m_musicVolume(0.7f), 
      m_masterVolume(1.0f),
      m_initialized(false),
      m_musicPlaying(false) {
    LOGI("AudioWrapper created");
}

AudioWrapper::~AudioWrapper() {
    cleanup();
    LOGI("AudioWrapper destroyed");
}

void AudioWrapper::setAssetManager(AAssetManager* assetManager) {
    g_assetManager = assetManager;
}

bool AudioWrapper::initialize() {
    LOGI("Initializing audio engine with Oboe");
    
    // Create audio stream for sound effects
    oboe::AudioStreamBuilder soundBuilder;
    soundBuilder.setDirection(oboe::Direction::Output);
    soundBuilder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
    soundBuilder.setSharingMode(oboe::SharingMode::Shared);
    soundBuilder.setFormat(oboe::AudioFormat::Float);
    soundBuilder.setChannelCount(oboe::ChannelCount::Stereo);
    soundBuilder.setSampleRate(44100);
    soundBuilder.setCallback(&g_soundCallback);
    
    oboe::Result result = soundBuilder.openStream(m_soundStream);
    if (result != oboe::Result::OK) {
        LOGE("Failed to create sound stream: %s", oboe::convertToText(result));
        return false;
    }
    
    // Create audio stream for music
    oboe::AudioStreamBuilder musicBuilder;
    musicBuilder.setDirection(oboe::Direction::Output);
    musicBuilder.setPerformanceMode(oboe::PerformanceMode::PowerSaving);
    musicBuilder.setSharingMode(oboe::SharingMode::Shared);
    musicBuilder.setFormat(oboe::AudioFormat::Float);
    musicBuilder.setChannelCount(oboe::ChannelCount::Stereo);
    musicBuilder.setSampleRate(44100);
    musicBuilder.setCallback(&g_musicCallback);
    
    result = musicBuilder.openStream(m_musicStream);
    if (result != oboe::Result::OK) {
        LOGE("Failed to create music stream: %s", oboe::convertToText(result));
        return false;
    }
    
    // Set buffer sizes for optimal performance
    m_soundStream->setBufferSizeInFrames(m_soundStream->getBufferSizeInFrames() * 2);
    m_musicStream->setBufferSizeInFrames(m_musicStream->getBufferSizeInFrames() * 2);
    
    // Start streams
    result = m_soundStream->requestStart();
    if (result != oboe::Result::OK) {
        LOGE("Failed to start sound stream: %s", oboe::convertToText(result));
        return false;
    }
    
    result = m_musicStream->requestStart();
    if (result != oboe::Result::OK) {
        LOGE("Failed to start music stream: %s", oboe::convertToText(result));
        return false;
    }
    
    m_initialized = true;
    LOGI("Audio engine initialized successfully");
    LOGI("Sound stream - Sample rate: %d, Buffer size: %d", 
         m_soundStream->getSampleRate(), 
         m_soundStream->getBufferSizeInFrames());
    LOGI("Music stream - Sample rate: %d, Buffer size: %d", 
         m_musicStream->getSampleRate(), 
         m_musicStream->getBufferSizeInFrames());
    
    return true;
}

void AudioWrapper::cleanup() {
    if (!m_initialized) return;
    
    LOGI("Cleaning up audio engine");
    
    stopAllSounds();
    stopMusic();
    
    if (m_soundStream) {
        m_soundStream->requestStop();
        m_soundStream->close();
        m_soundStream.reset();
    }
    
    if (m_musicStream) {
        m_musicStream->requestStop();
        m_musicStream->close();
        m_musicStream.reset();
    }
    
    // Clear loaded audio data
    for (auto& pair : m_loadedSounds) {
        delete pair.second;
    }
    for (auto& pair : m_loadedMusic) {
        delete pair.second;
    }
    
    m_loadedSounds.clear();
    m_loadedMusic.clear();
    m_initialized = false;
}

void AudioWrapper::playSound(const char* soundName) {
    if (!m_initialized) {
        LOGE("Cannot play sound - audio not initialized");
        return;
    }
    
    if (!soundName) return;
    
    LOGI("Playing sound: %s", soundName);
    
    std::string name(soundName);
    
    // Load sound if not already loaded
    if (m_loadedSounds.find(name) == m_loadedSounds.end()) {
        if (!loadSound(name)) {
            LOGE("Failed to load sound: %s", soundName);
            return;
        }
    }
    
    AudioData* audioData = m_loadedSounds[name];
    if (!audioData || !audioData->isLoaded) {
        LOGE("Sound data not available: %s", soundName);
        return;
    }
    
    // Reset to beginning and set volume
    audioData->currentSample = 0;
    audioData->volume = m_soundVolume;
    
    // Add to playing sounds
    g_soundCallback.addPlayingSound(name, audioData);
}

void AudioWrapper::stopSound(const char* soundName) {
    if (!m_initialized || !soundName) return;
    
    LOGI("Stopping sound: %s", soundName);
    
    std::string name(soundName);
    g_soundCallback.removePlayingSound(name);
}

void AudioWrapper::stopAllSounds() {
    if (!m_initialized) return;
    
    LOGI("Stopping all sounds");
    
    // Clear all playing sounds
    g_soundCallback = SoundCallback(); // Reset callback
}

void AudioWrapper::playMusic(const char* musicName, bool loop) {
    if (!m_initialized) {
        LOGE("Cannot play music - audio not initialized");
        return;
    }
    
    if (!musicName) return;
    
    LOGI("Playing music: %s (loop: %s)", musicName, loop ? "yes" : "no");
    
    std::string name(musicName);
    
    // Load music if not already loaded
    if (m_loadedMusic.find(name) == m_loadedMusic.end()) {
        if (!loadMusic(name)) {
            LOGE("Failed to load music: %s", musicName);
            return;
        }
    }
    
    AudioData* audioData = m_loadedMusic[name];
    if (!audioData || !audioData->isLoaded) {
        LOGE("Music data not available: %s", musicName);
        return;
    }
    
    // Set up music playback
    audioData->currentSample = 0;
    audioData->isLooping = loop;
    audioData->volume = m_musicVolume;
    
    g_musicCallback.setMusic(audioData);
    m_musicPlaying = true;
}

void AudioWrapper::stopMusic() {
    if (!m_initialized) return;
    
    LOGI("Stopping music");
    
    g_musicCallback.stop();
    m_musicPlaying = false;
}

void AudioWrapper::pauseMusic() {
    if (!m_initialized || !m_musicPlaying) return;
    
    LOGI("Pausing music");
    
    // TODO: Implement pause functionality
    // This would require storing current position and stopping playback
}

void AudioWrapper::resumeMusic() {
    if (!m_initialized) return;
    
    LOGI("Resuming music");
    
    // TODO: Implement resume functionality
    // This would require restoring stored position
}

void AudioWrapper::setSoundVolume(float volume) {
    m_soundVolume = std::max(0.0f, std::min(1.0f, volume));
    LOGI("Sound volume set to: %.2f", m_soundVolume);
    
    // Update currently playing sounds
    for (auto& pair : m_loadedSounds) {
        pair.second->volume = m_soundVolume;
    }
}

void AudioWrapper::setMusicVolume(float volume) {
    m_musicVolume = std::max(0.0f, std::min(1.0f, volume));
    LOGI("Music volume set to: %.2f", m_musicVolume);
    
    // Update current music
    for (auto& pair : m_loadedMusic) {
        pair.second->volume = m_musicVolume;
    }
}

void AudioWrapper::setMasterVolume(float volume) {
    m_masterVolume = std::max(0.0f, std::min(1.0f, volume));
    LOGI("Master volume set to: %.2f", m_masterVolume);
    
    // Update callbacks
    g_soundCallback.setMasterVolume(m_masterVolume);
    g_musicCallback.setMasterVolume(m_masterVolume);
}

bool AudioWrapper::isMusicPlaying() const {
    return m_musicPlaying;
}

bool AudioWrapper::isSoundPlaying(const char* soundName) const {
    if (!soundName) return false;
    
    // TODO: Check if specific sound is playing
    // This would require tracking playing sounds in the callback
    return false;
}

bool AudioWrapper::loadSound(const std::string& soundName) {
    if (!g_assetManager) {
        LOGE("Asset manager not set");
        return false;
    }
    
    std::string assetPath = "sounds/" + soundName + ".wav";
    AAsset* asset = AAssetManager_open(g_assetManager, assetPath.c_str(), AASSET_MODE_BUFFER);
    
    if (!asset) {
        LOGE("Failed to open sound asset: %s", assetPath.c_str());
        return false;
    }
    
    size_t assetSize = AAsset_getLength(asset);
    const void* assetData = AAsset_getBuffer(asset);
    
    // Simple WAV loading (assumes 44.1kHz, 16-bit, mono)
    // In a real implementation, you'd use a proper audio library
    AudioData* audioData = new AudioData();
    
    // Skip WAV header (44 bytes) and convert 16-bit to float
    const int16_t* pcmData = static_cast<const int16_t*>(static_cast<const void*>(
        static_cast<const char*>(assetData) + 44));
    int sampleCount = (assetSize - 44) / 2;
    
    audioData->samples.reserve(sampleCount);
    for (int i = 0; i < sampleCount; ++i) {
        float sample = static_cast<float>(pcmData[i]) / 32768.0f;
        audioData->samples.push_back(sample);
    }
    
    audioData->isLoaded = true;
    audioData->currentSample = 0;
    
    m_loadedSounds[soundName] = audioData;
    
    AAsset_close(asset);
    
    LOGI("Loaded sound: %s (%d samples)", soundName.c_str(), sampleCount);
    return true;
}

bool AudioWrapper::loadMusic(const std::string& musicName) {
    if (!g_assetManager) {
        LOGE("Asset manager not set");
        return false;
    }
    
    std::string assetPath = "music/" + musicName + ".wav";
    AAsset* asset = AAssetManager_open(g_assetManager, assetPath.c_str(), AASSET_MODE_BUFFER);
    
    if (!asset) {
        LOGE("Failed to open music asset: %s", assetPath.c_str());
        return false;
    }
    
    size_t assetSize = AAsset_getLength(asset);
    const void* assetData = AAsset_getBuffer(asset);
    
    // Simple WAV loading
    AudioData* audioData = new AudioData();
    
    const int16_t* pcmData = static_cast<const int16_t*>(static_cast<const void*>(
        static_cast<const char*>(assetData) + 44));
    int sampleCount = (assetSize - 44) / 2;
    
    audioData->samples.reserve(sampleCount);
    for (int i = 0; i < sampleCount; ++i) {
        float sample = static_cast<float>(pcmData[i]) / 32768.0f;
        audioData->samples.push_back(sample);
    }
    
    audioData->isLoaded = true;
    audioData->currentSample = 0;
    
    m_loadedMusic[musicName] = audioData;
    
    AAsset_close(asset);
    
    LOGI("Loaded music: %s (%d samples)", musicName.c_str(), sampleCount);
    return true;
}

} // namespace TrashPiles