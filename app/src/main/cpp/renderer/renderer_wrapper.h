#ifndef TRASHPILES_RENDERER_WRAPPER_H
#define TRASHPILES_RENDERER_WRAPPER_H

#include <android/log.h>
#include <skia/core/SkSurface.h>
#include <skia/core/SkCanvas.h>
#include <skia/core/SkPaint.h>
#include <memory>
#include <map>

#define LOG_TAG "TrashPiles-Renderer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct AAssetManager;

namespace TrashPiles {

// Forward declaration for card animation state
struct CardState;

/**
 * Renderer Wrapper - Interfaces with Skia Graphics Engine
 * Handles all rendering operations for the game
 */
class RendererWrapper {
public:
    RendererWrapper();
    ~RendererWrapper();
    
    // Asset management
    static void setAssetManager(AAssetManager* assetManager);
    
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
    
    // Skia resources
    sk_sp<SkSurface> m_surface;
    SkCanvas* m_canvas;
    
    // Paints for different rendering tasks
    SkPaint m_cardPaint;
    SkPaint m_cardBorderPaint;
    SkPaint m_cardBackPaint;
    SkPaint m_textPaint;
    
    // Card animation states
    std::map<int, CardState> m_cardStates;
    
    // Helper methods
    void drawCardValue(int cardId, float x, float y, float width, float height);
    void drawCardBackPattern(float x, float y, float width, float height);
    const char* getCardValueText(int value);
    const char* getSuitSymbol(int suit);
};

// Card animation state structure
struct CardState {
    float rotation = 0.0f;
    float scaleX = 1.0f;
    float scaleY = 1.0f;
    float alpha = 1.0f;
    float x = 0.0f;
    float y = 0.0f;
};

} // namespace TrashPiles

#endif // TRASHPILES_RENDERER_WRAPPER_H