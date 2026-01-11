#include "game_engine_wrapper.h"

namespace TrashPiles {

GameEngineWrapper::GameEngineWrapper() 
    : m_initialized(false), m_deltaTime(0.0f), m_fps(60) {
    LOGI("GameEngineWrapper created");
}

GameEngineWrapper::~GameEngineWrapper() {
    cleanup();
    LOGI("GameEngineWrapper destroyed");
}

bool GameEngineWrapper::initialize() {
    LOGI("Initializing game engine wrapper");
    
    // Note: libGDX is primarily used from Kotlin
    // This wrapper provides native support if needed
    
    m_initialized = true;
    return true;
}

void GameEngineWrapper::cleanup() {
    if (!m_initialized) return;
    
    LOGI("Cleaning up game engine wrapper");
    
    m_initialized = false;
}

void GameEngineWrapper::update(float deltaTime) {
    m_deltaTime = deltaTime;
    
    // Native game loop update if needed
    // Most game logic will be in Kotlin GCMS
}

void GameEngineWrapper::handleTouchDown(float x, float y) {
    LOGI("Touch down at: (%.2f, %.2f)", x, y);
    
    // Handle touch input if needed from native side
}

void GameEngineWrapper::handleTouchUp(float x, float y) {
    LOGI("Touch up at: (%.2f, %.2f)", x, y);
    
    // Handle touch input if needed from native side
}

void GameEngineWrapper::handleTouchMove(float x, float y) {
    // Handle touch move if needed from native side
}

float GameEngineWrapper::getDeltaTime() const {
    return m_deltaTime;
}

int GameEngineWrapper::getFPS() const {
    return m_fps;
}

} // namespace TrashPiles