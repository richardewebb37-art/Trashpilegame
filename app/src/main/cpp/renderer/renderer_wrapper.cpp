#include "renderer_wrapper.h"
#include <skia/core/SkCanvas.h>
#include <skia/core/SkPaint.h>
#include <skia/core/SkBitmap.h>
#include <skia/core/SkImage.h>
#include <skia/core/SkSurface.h>
#include <skia/core/SkFont.h>
#include <skia/core/SkTextBlob.h>
#include <skia/core/SkMatrix.h>
#include <skia/core/SkPath.h>
#include <skia/effects/SkGradientShader.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <memory>
#include <map>

namespace TrashPiles {

// Card animation state
struct CardState {
    float rotation = 0.0f;
    float scaleX = 1.0f;
    float scaleY = 1.0f;
    float alpha = 1.0f;
    float x = 0.0f;
    float y = 0.0f;
};

// Static instance for asset manager access
static AAssetManager* g_assetManager = nullptr;

RendererWrapper::RendererWrapper() 
    : m_width(0), m_height(0), m_initialized(false) {
    LOGI("RendererWrapper created");
}

RendererWrapper::~RendererWrapper() {
    cleanup();
    LOGI("RendererWrapper destroyed");
}

void RendererWrapper::setAssetManager(AAssetManager* assetManager) {
    g_assetManager = assetManager;
}

bool RendererWrapper::initialize(int width, int height) {
    LOGI("Initializing renderer: %dx%d", width, height);
    
    m_width = width;
    m_height = height;
    
    // Create Skia surface
    SkImageInfo info = SkImageInfo::MakeN32Premul(width, height);
    m_surface = SkSurface::MakeRenderTarget(info, SkBudgeted::kYes);
    
    if (!m_surface) {
        LOGE("Failed to create Skia surface");
        return false;
    }
    
    // Initialize fonts and paints
    m_cardPaint.setAntiAlias(true);
    m_cardPaint.setStyle(SkPaint::kFill_Style);
    m_cardPaint.setColor(SK_ColorWHITE);
    m_cardPaint.setStrokeWidth(2.0f);
    
    m_cardBorderPaint.setAntiAlias(true);
    m_cardBorderPaint.setStyle(SkPaint::kStroke_Style);
    m_cardBorderPaint.setColor(SK_ColorBLACK);
    m_cardBorderPaint.setStrokeWidth(2.0f);
    
    m_textPaint.setAntiAlias(true);
    m_textPaint.setColor(SK_ColorBLACK);
    m_textPaint.setTextSize(24.0f);
    
    m_cardBackPaint.setAntiAlias(true);
    m_cardBackPaint.setStyle(SkPaint::kFill_Style);
    m_cardBackPaint.setColor(SK_ColorBLUE);
    
    m_initialized = true;
    LOGI("Renderer initialized successfully");
    return true;
}

void RendererWrapper::cleanup() {
    if (!m_initialized) return;
    
    LOGI("Cleaning up renderer");
    
    m_surface.reset();
    m_cardStates.clear();
    
    m_initialized = false;
}

void RendererWrapper::beginFrame() {
    if (!m_initialized || !m_surface) return;
    
    m_canvas = m_surface->getCanvas();
    if (m_canvas) {
        m_canvas->save();
    }
}

void RendererWrapper::endFrame() {
    if (!m_canvas) return;
    
    m_canvas->restore();
    m_surface->flush();
    m_canvas = nullptr;
}

void RendererWrapper::clear(float r, float g, float b, float a) {
    if (!m_canvas) return;
    
    SkColor color = SkColorSetARGB(
        static_cast<U8CPU>(a * 255),
        static_cast<U8CPU>(r * 255),
        static_cast<U8CPU>(g * 255),
        static_cast<U8CPU>(b * 255)
    );
    
    m_canvas->clear(color);
}

void RendererWrapper::renderCard(int cardId, float x, float y, float width, float height, bool faceUp) {
    if (!m_canvas) return;
    
    // Get card animation state
    CardState& state = m_cardStates[cardId];
    state.x = x;
    state.y = y;
    
    // Apply transformations
    SkMatrix matrix;
    matrix.setTranslate(x + width/2, y + height/2);
    matrix.preRotate(state.rotation);
    matrix.preScale(state.scaleX, state.scaleY);
    matrix.preTranslate(-width/2, -height/2);
    
    SkAutoCanvasRestore autoRestore(m_canvas, true);
    m_canvas->concat(matrix);
    
    // Apply alpha
    m_cardPaint.setAlpha(static_cast<U8CPU>(state.alpha * 255));
    m_cardBorderPaint.setAlpha(static_cast<U8CPU>(state.alpha * 255));
    
    SkRect rect = SkRect::MakeWH(width, height);
    
    if (faceUp) {
        // Draw card face
        m_canvas->drawRect(rect, m_cardPaint);
        m_canvas->drawRect(rect, m_cardBorderPaint);
        
        // Draw card value
        drawCardValue(cardId, 0, 0, width, height);
    } else {
        // Draw card back
        m_canvas->drawRect(rect, m_cardBackPaint);
        m_canvas->drawRect(rect, m_cardBorderPaint);
        drawCardBackPattern(0, 0, width, height);
    }
}

void RendererWrapper::renderCardBack(float x, float y, float width, float height) {
    if (!m_canvas) return;
    
    SkRect rect = SkRect::MakeXYWH(x, y, width, height);
    m_canvas->drawRect(rect, m_cardBackPaint);
    m_canvas->drawRect(rect, m_cardBorderPaint);
    drawCardBackPattern(x, y, width, height);
}

void RendererWrapper::renderButton(const char* buttonId, float x, float y, float width, float height) {
    if (!m_canvas) return;
    
    SkRect rect = SkRect::MakeXYWH(x, y, width, height);
    
    // Button gradient effect
    SkColor colors[] = {SK_ColorLTGRAY, SK_ColorGRAY};
    SkPoint points[] = {{0, y}, {0, y + height}};
    auto shader = SkGradientShader::MakeLinear(points, colors, nullptr, 2, SkTileMode::kClamp);
    
    SkPaint buttonPaint;
    buttonPaint.setAntiAlias(true);
    buttonPaint.setStyle(SkPaint::kFill_Style);
    buttonPaint.setShader(std::move(shader));
    
    // Draw button
    SkAutoCanvasRestore autoRestore(m_canvas, true);
    m_canvas->drawRoundRect(rect, 8.0f, 8.0f, buttonPaint);
    
    // Draw border
    SkPaint borderPaint;
    borderPaint.setAntiAlias(true);
    borderPaint.setStyle(SkPaint::kStroke_Style);
    borderPaint.setColor(SK_ColorDKGRAY);
    borderPaint.setStrokeWidth(2.0f);
    m_canvas->drawRoundRect(rect, 8.0f, 8.0f, borderPaint);
    
    // Draw text
    if (buttonId && strlen(buttonId) > 0) {
        SkPaint textPaint;
        textPaint.setAntiAlias(true);
        textPaint.setColor(SK_ColorBLACK);
        textPaint.setTextSize(16.0f);
        textPaint.setTextAlign(SkTextAlign::kCenter);
        
        SkFont font(nullptr, 16.0f);
        m_canvas->drawSimpleText(buttonId, strlen(buttonId), SkTextEncoding::kUTF8, 
                               x + width/2, y + height/2 + 8, font, textPaint);
    }
}

void RendererWrapper::renderText(const char* text, float x, float y, float size) {
    if (!m_canvas || !text) return;
    
    SkPaint textPaint;
    textPaint.setAntiAlias(true);
    textPaint.setColor(SK_ColorBLACK);
    textPaint.setTextSize(size);
    
    SkFont font(nullptr, size);
    m_canvas->drawSimpleText(text, strlen(text), SkTextEncoding::kUTF8, x, y, font, textPaint);
}

void RendererWrapper::setCardRotation(int cardId, float angle) {
    m_cardStates[cardId].rotation = angle;
}

void RendererWrapper::setCardScale(int cardId, float scaleX, float scaleY) {
    m_cardStates[cardId].scaleX = scaleX;
    m_cardStates[cardId].scaleY = scaleY;
}

void RendererWrapper::setCardAlpha(int cardId, float alpha) {
    m_cardStates[cardId].alpha = alpha;
}

void RendererWrapper::drawCardValue(int cardId, float x, float y, float width, float height) {
    if (!m_canvas) return;
    
    // Extract card value (1-13 for standard deck)
    int value = (cardId % 13) + 1;
    int suit = cardId / 13; // 0=Spades, 1=Hearts, 2=Diamonds, 3=Clubs
    
    // Set color based on suit
    SkColor textColor = (suit == 1 || suit == 2) ? SK_ColorRED : SK_ColorBLACK;
    
    SkPaint textPaint;
    textPaint.setAntiAlias(true);
    textPaint.setColor(textColor);
    textPaint.setTextSize(height * 0.3f);
    textPaint.setTextAlign(SkTextAlign::kCenter);
    
    // Draw value
    const char* valueText = getCardValueText(value);
    SkFont font(nullptr, height * 0.3f);
    m_canvas->drawSimpleText(valueText, strlen(valueText), SkTextEncoding::kUTF8, 
                           x + width/2, y + height * 0.35f, font, textPaint);
    
    // Draw suit symbol
    const char* suitText = getSuitSymbol(suit);
    m_canvas->drawSimpleText(suitText, strlen(suitText), SkTextEncoding::kUTF8, 
                           x + width/2, y + height * 0.65f, font, textPaint);
}

void RendererWrapper::drawCardBackPattern(float x, float y, float width, float height) {
    if (!m_canvas) return;
    
    SkPaint patternPaint;
    patternPaint.setAntiAlias(true);
    patternPaint.setColor(SK_ColorWHITE);
    patternPaint.setStrokeWidth(1.5f);
    
    // Draw diamond pattern
    float centerX = x + width/2;
    float centerY = y + height/2;
    float diamondSize = width * 0.3f;
    
    SkPath diamondPath;
    diamondPath.moveTo(centerX, centerY - diamondSize);
    diamondPath.lineTo(centerX + diamondSize, centerY);
    diamondPath.lineTo(centerX, centerY + diamondSize);
    diamondPath.lineTo(centerX - diamondSize, centerY);
    diamondPath.close();
    
    m_canvas->drawPath(diamondPath, patternPaint);
}

const char* RendererWrapper::getCardValueText(int value) {
    switch (value) {
        case 1: return "A";
        case 11: return "J";
        case 12: return "Q";
        case 13: return "K";
        default: {
            static char buffer[8];
            snprintf(buffer, sizeof(buffer), "%d", value);
            return buffer;
        }
    }
}

const char* RendererWrapper::getSuitSymbol(int suit) {
    switch (suit) {
        case 0: return "♠"; // Spades
        case 1: return "♥"; // Hearts
        case 2: return "♦"; // Diamonds
        case 3: return "♣"; // Clubs
        default: return "?";
    }
}

} // namespace TrashPiles