#include <jni.h>
#include <android/log.h>
#include "../renderer/renderer_wrapper.h"

#define LOG_TAG "TrashPiles-Renderer-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern TrashPiles::RendererWrapper* g_renderer;

extern "C" {

/**
 * Initialize the renderer
 */
JNIEXPORT jboolean JNICALL
Java_com_trashpiles_native_RendererBridge_initRenderer(
    JNIEnv* env, jobject obj, jint width, jint height) {
    
    LOGI("JNI: initRenderer called with %dx%d", width, height);
    
    if (!g_renderer) {
        LOGE("Renderer instance is null!");
        return JNI_FALSE;
    }
    
    bool success = g_renderer->initialize(width, height);
    return success ? JNI_TRUE : JNI_FALSE;
}

/**
 * Begin rendering a frame
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_RendererBridge_beginFrame(
    JNIEnv* env, jobject obj) {
    
    if (g_renderer) {
        g_renderer->beginFrame();
    }
}

/**
 * End rendering a frame
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_RendererBridge_endFrame(
    JNIEnv* env, jobject obj) {
    
    if (g_renderer) {
        g_renderer->endFrame();
    }
}

/**
 * Clear the screen
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_RendererBridge_clear(
    JNIEnv* env, jobject obj, jfloat r, jfloat g, jfloat b, jfloat a) {
    
    if (g_renderer) {
        g_renderer->clear(r, g, b, a);
    }
}

/**
 * Render a card
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_RendererBridge_renderCard(
    JNIEnv* env, jobject obj, 
    jint cardId, jfloat x, jfloat y, jfloat width, jfloat height, jboolean faceUp) {
    
    if (g_renderer) {
        g_renderer->renderCard(cardId, x, y, width, height, faceUp == JNI_TRUE);
    }
}

/**
 * Render card back
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_RendererBridge_renderCardBack(
    JNIEnv* env, jobject obj, jfloat x, jfloat y, jfloat width, jfloat height) {
    
    if (g_renderer) {
        g_renderer->renderCardBack(x, y, width, height);
    }
}

/**
 * Render a button
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_RendererBridge_renderButton(
    JNIEnv* env, jobject obj, jstring buttonId, jfloat x, jfloat y, jfloat width, jfloat height) {
    
    if (!g_renderer) return;
    
    const char* id = env->GetStringUTFChars(buttonId, nullptr);
    g_renderer->renderButton(id, x, y, width, height);
    env->ReleaseStringUTFChars(buttonId, id);
}

/**
 * Render text
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_RendererBridge_renderText(
    JNIEnv* env, jobject obj, jstring text, jfloat x, jfloat y, jfloat size) {
    
    if (!g_renderer) return;
    
    const char* txt = env->GetStringUTFChars(text, nullptr);
    g_renderer->renderText(txt, x, y, size);
    env->ReleaseStringUTFChars(text, txt);
}

/**
 * Set card rotation for animation
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_RendererBridge_setCardRotation(
    JNIEnv* env, jobject obj, jint cardId, jfloat angle) {
    
    if (g_renderer) {
        g_renderer->setCardRotation(cardId, angle);
    }
}

/**
 * Cleanup renderer
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_RendererBridge_cleanup(
    JNIEnv* env, jobject obj) {
    
    LOGI("JNI: cleanup called");
    
    if (g_renderer) {
        g_renderer->cleanup();
    }
}

} // extern "C"