#include <jni.h>
#include <android/log.h>
#include "../game_engine/game_engine_wrapper.h"

#define LOG_TAG "TrashPiles-GameEngine-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern TrashPiles::GameEngineWrapper* g_gameEngine;

extern "C" {

/**
 * Initialize the game engine
 */
JNIEXPORT jboolean JNICALL
Java_com_trashpiles_native_GameEngineBridge_initGameEngine(
    JNIEnv* env, jobject obj) {
    
    LOGI("JNI: initGameEngine called");
    
    if (!g_gameEngine) {
        LOGE("Game engine instance is null!");
        return JNI_FALSE;
    }
    
    bool success = g_gameEngine->initialize();
    return success ? JNI_TRUE : JNI_FALSE;
}

/**
 * Update game engine
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_GameEngineBridge_update(
    JNIEnv* env, jobject obj, jfloat deltaTime) {
    
    if (g_gameEngine) {
        g_gameEngine->update(deltaTime);
    }
}

/**
 * Handle touch down
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_GameEngineBridge_handleTouchDown(
    JNIEnv* env, jobject obj, jfloat x, jfloat y) {
    
    if (g_gameEngine) {
        g_gameEngine->handleTouchDown(x, y);
    }
}

/**
 * Handle touch up
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_GameEngineBridge_handleTouchUp(
    JNIEnv* env, jobject obj, jfloat x, jfloat y) {
    
    if (g_gameEngine) {
        g_gameEngine->handleTouchUp(x, y);
    }
}

/**
 * Handle touch move
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_GameEngineBridge_handleTouchMove(
    JNIEnv* env, jobject obj, jfloat x, jfloat y) {
    
    if (g_gameEngine) {
        g_gameEngine->handleTouchMove(x, y);
    }
}

/**
 * Get delta time
 */
JNIEXPORT jfloat JNICALL
Java_com_trashpiles_native_GameEngineBridge_getDeltaTime(
    JNIEnv* env, jobject obj) {
    
    if (!g_gameEngine) return 0.0f;
    
    return g_gameEngine->getDeltaTime();
}

/**
 * Get FPS
 */
JNIEXPORT jint JNICALL
Java_com_trashpiles_native_GameEngineBridge_getFPS(
    JNIEnv* env, jobject obj) {
    
    if (!g_gameEngine) return 0;
    
    return g_gameEngine->getFPS();
}

/**
 * Cleanup game engine
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_GameEngineBridge_cleanup(
    JNIEnv* env, jobject obj) {
    
    LOGI("JNI: cleanup called");
    
    if (g_gameEngine) {
        g_gameEngine->cleanup();
    }
}

} // extern "C"