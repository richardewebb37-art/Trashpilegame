#include "audio_wrapper.h"

namespace TrashPiles {

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

bool AudioWrapper::initialize() {
    LOGI("Initializing audio engine with Oboe");
    
    // Create audio stream for sound effects
    oboe::AudioStreamBuilder soundBuilder;
    soundBuilder.setDirection(oboe::Direction::Output);
    soundBuilder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
    soundBuilder.setSharingMode(oboe::SharingMode::Exclusive);
    soundBuilder.setFormat(oboe::AudioFormat::Float);
    soundBuilder.setChannelCount(oboe::ChannelCount::Stereo);
    
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
    
    result = musicBuilder.openStream(m_musicStream);
    if (result != oboe::Result::OK) {
        LOGE("Failed to create music stream: %s", oboe::convertToText(result));
        return false;
    }
    
    // Start streams
    m_soundStream->requestStart();
    m_musicStream->requestStart();
    
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
        m_soundStream->close();
        m_soundStream.reset();
    }
    
    if (m_musicStream) {
        m_musicStream->close();
        m_musicStream.reset();
    }
    
    m_loadedSounds.clear();
    m_initialized = false;
}

void AudioWrapper::playSound(const char* soundName) {
    if (!m_initialized) {
        LOGE("Cannot play sound - audio not initialized");
        return;
    }
    
    LOGI("Playing sound: %s", soundName);
    
    // TODO: Load and play sound file
    // This will be implemented when asset loading is ready
}

void AudioWrapper::stopSound(const char* soundName) {
    if (!m_initialized) return;
    
    LOGI("Stopping sound: %s", soundName);
    
    // TODO: Stop specific sound
}

void AudioWrapper::stopAllSounds() {
    if (!m_initialized) return;
    
    LOGI("Stopping all sounds");
    
    // TODO: Stop all playing sounds
}

void AudioWrapper::playMusic(const char* musicName, bool loop) {
    if (!m_initialized) {
        LOGE("Cannot play music - audio not initialized");
        return;
    }
    
    LOGI("Playing music: %s (loop: %s)", musicName, loop ? "yes" : "no");
    
    // TODO: Load and play music file
    m_musicPlaying = true;
}

void AudioWrapper::stopMusic() {
    if (!m_initialized) return;
    
    LOGI("Stopping music");
    
    // TODO: Stop music playback
    m_musicPlaying = false;
}

void AudioWrapper::pauseMusic() {
    if (!m_initialized || !m_musicPlaying) return;
    
    LOGI("Pausing music");
    
    // TODO: Pause music playback
}

void AudioWrapper::resumeMusic() {
    if (!m_initialized) return;
    
    LOGI("Resuming music");
    
    // TODO: Resume music playback
}

void AudioWrapper::setSoundVolume(float volume) {
    m_soundVolume = volume;
    LOGI("Sound volume set to: %.2f", volume);
    
    // TODO: Apply volume to sound stream
}

void AudioWrapper::setMusicVolume(float volume) {
    m_musicVolume = volume;
    LOGI("Music volume set to: %.2f", volume);
    
    // TODO: Apply volume to music stream
}

void AudioWrapper::setMasterVolume(float volume) {
    m_masterVolume = volume;
    LOGI("Master volume set to: %.2f", volume);
    
    // TODO: Apply master volume to all streams
}

bool AudioWrapper::isMusicPlaying() const {
    return m_musicPlaying;
}

bool AudioWrapper::isSoundPlaying(const char* soundName) const {
    // TODO: Check if specific sound is playing
    return false;
}

} // namespace TrashPiles