#include <jni.h>
#include <android/log.h>
#include "../renderer/renderer_wrapper.h"
#include "../audio/audio_wrapper.h"
#include "../game_engine/game_engine_wrapper.h"

#define LOG_TAG "TrashPiles-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global engine instances
static TrashPiles::RendererWrapper* g_renderer = nullptr;
static TrashPiles::AudioWrapper* g_audio = nullptr;
static TrashPiles::GameEngineWrapper* g_gameEngine = nullptr;

extern "C" {

// Called when the native library is loaded
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("=================================================");
    LOGI("Trash Piles Native Library Loading...");
    LOGI("=================================================");
    LOGI("Engines: Skia + libGDX + Oboe");
    LOGI("Version: 1.0.0");
    LOGI("=================================================");
    
    // Create engine instances
    g_renderer = new TrashPiles::RendererWrapper();
    g_audio = new TrashPiles::AudioWrapper();
    g_gameEngine = new TrashPiles::GameEngineWrapper();
    
    LOGI("Engine instances created successfully");
    LOGI("Native library loaded successfully");
    
    return JNI_VERSION_1_6;
}

// Called when the native library is unloaded
JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved) {
    LOGI("Unloading native library...");
    
    // Cleanup engine instances
    if (g_renderer) {
        delete g_renderer;
        g_renderer = nullptr;
    }
    
    if (g_audio) {
        delete g_audio;
        g_audio = nullptr;
    }
    
    if (g_gameEngine) {
        delete g_gameEngine;
        g_gameEngine = nullptr;
    }
    
    LOGI("Native library unloaded");
}

} // extern "C"