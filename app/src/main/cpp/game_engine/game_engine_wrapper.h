#ifndef TRASHPILES_GAME_ENGINE_WRAPPER_H
#define TRASHPILES_GAME_ENGINE_WRAPPER_H

#include <android/log.h>

#define LOG_TAG "TrashPiles-GameEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace TrashPiles {

/**
 * Game Engine Wrapper - Interfaces with libGDX
 * Note: libGDX is primarily used from Kotlin layer
 * This wrapper provides native support functions if needed
 */
class GameEngineWrapper {
public:
    GameEngineWrapper();
    ~GameEngineWrapper();
    
    // Initialization
    bool initialize();
    void cleanup();
    
    // Game loop support (if needed from native side)
    void update(float deltaTime);
    
    // Input handling support
    void handleTouchDown(float x, float y);
    void handleTouchUp(float x, float y);
    void handleTouchMove(float x, float y);
    
    // Utility functions
    float getDeltaTime() const;
    int getFPS() const;
    
private:
    bool m_initialized;
    float m_deltaTime;
    int m_fps;
};

} // namespace TrashPiles

#endif // TRASHPILES_GAME_ENGINE_WRAPPER_H