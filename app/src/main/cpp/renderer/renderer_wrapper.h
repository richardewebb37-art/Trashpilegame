#ifndef TRASHPILES_RENDERER_WRAPPER_H
#define TRASHPILES_RENDERER_WRAPPER_H

#include <android/log.h>

#define LOG_TAG "TrashPiles-Renderer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace TrashPiles {

/**
 * Renderer Wrapper - Interfaces with Skia Graphics Engine
 * Handles all rendering operations for the game
 */
class RendererWrapper {
public:
    RendererWrapper();
    ~RendererWrapper();
    
    // Initialization
    bool initialize(int width, int height);
    void cleanup();
    
    // Rendering
    void beginFrame();
    void endFrame();
    void clear(float r, float g, float b, float a);
    
    // Card rendering
    void renderCard(int cardId, float x, float y, float width, float height, bool faceUp);
    void renderCardBack(float x, float y, float width, float height);
    
    // UI rendering
    void renderButton(const char* buttonId, float x, float y, float width, float height);
    void renderText(const char* text, float x, float y, float size);
    
    // Animation support
    void setCardRotation(int cardId, float angle);
    void setCardScale(int cardId, float scaleX, float scaleY);
    void setCardAlpha(int cardId, float alpha);
    
private:
    int m_width;
    int m_height;
    bool m_initialized;
};

} // namespace TrashPiles

#endif // TRASHPILES_RENDERER_WRAPPER_H