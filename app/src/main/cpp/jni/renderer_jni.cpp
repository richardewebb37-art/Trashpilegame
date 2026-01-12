#include <jni.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "renderer/renderer_wrapper.h"

#define LOG_TAG "TrashPiles-RendererJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

// Global renderer instance
static TrashPiles::RendererWrapper* g_renderer = nullptr;

JNIEXPORT jlong JNICALL
Java_com_trashpiles_RendererBridge_nativeCreateRenderer(JNIEnv* env, jobject thiz) {
    if (g_renderer) {
        LOGE("Renderer already created");
        return 0;
    }
    
    g_renderer = new TrashPiles::RendererWrapper();
    LOGI("Renderer created");
    return reinterpret_cast<jlong>(g_renderer);
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeDestroyRenderer(JNIEnv* env, jobject thiz, jlong renderer_ptr) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (renderer) {
        delete renderer;
        if (renderer == g_renderer) {
            g_renderer = nullptr;
        }
        LOGI("Renderer destroyed");
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeSetAssetManager(JNIEnv* env, jobject thiz, jlong renderer_ptr, jobject asset_manager) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (!renderer) return;
    
    AAssetManager* assetManager = AAssetManager_fromJava(env, asset_manager);
    TrashPiles::RendererWrapper::setAssetManager(assetManager);
    LOGI("Asset manager set for renderer");
}

JNIEXPORT jboolean JNICALL
Java_com_trashpiles_RendererBridge_nativeInitialize(JNIEnv* env, jobject thiz, jlong renderer_ptr, jint width, jint height) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (!renderer) {
        LOGE("Cannot initialize - renderer is null");
        return JNI_FALSE;
    }
    
    bool result = renderer->initialize(width, height);
    LOGI("Renderer initialization: %s", result ? "SUCCESS" : "FAILED");
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeCleanup(JNIEnv* env, jobject thiz, jlong renderer_ptr) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (renderer) {
        renderer->cleanup();
        LOGI("Renderer cleanup completed");
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeBeginFrame(JNIEnv* env, jobject thiz, jlong renderer_ptr) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (renderer) {
        renderer->beginFrame();
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeEndFrame(JNIEnv* env, jobject thiz, jlong renderer_ptr) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (renderer) {
        renderer->endFrame();
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeClear(JNIEnv* env, jobject thiz, jlong renderer_ptr, jfloat r, jfloat g, jfloat b, jfloat a) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (renderer) {
        renderer->clear(r, g, b, a);
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeRenderCard(JNIEnv* env, jobject thiz, jlong renderer_ptr, jint card_id, jfloat x, jfloat y, jfloat width, jfloat height, jboolean face_up) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (renderer) {
        renderer->renderCard(card_id, x, y, width, height, face_up);
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeRenderCardBack(JNIEnv* env, jobject thiz, jlong renderer_ptr, jfloat x, jfloat y, jfloat width, jfloat height) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (renderer) {
        renderer->renderCardBack(x, y, width, height);
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeRenderButton(JNIEnv* env, jobject thiz, jlong renderer_ptr, jstring button_id, jfloat x, jfloat y, jfloat width, jfloat height) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (!renderer) return;
    
    const char* buttonIdStr = env->GetStringUTFChars(button_id, nullptr);
    if (buttonIdStr) {
        renderer->renderButton(buttonIdStr, x, y, width, height);
        env->ReleaseStringUTFChars(button_id, buttonIdStr);
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeRenderText(JNIEnv* env, jobject thiz, jlong renderer_ptr, jstring text, jfloat x, jfloat y, jfloat size) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (!renderer) return;
    
    const char* textStr = env->GetStringUTFChars(text, nullptr);
    if (textStr) {
        renderer->renderText(textStr, x, y, size);
        env->ReleaseStringUTFChars(text, textStr);
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeSetCardRotation(JNIEnv* env, jobject thiz, jlong renderer_ptr, jint card_id, jfloat angle) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (renderer) {
        renderer->setCardRotation(card_id, angle);
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeSetCardScale(JNIEnv* env, jobject thiz, jlong renderer_ptr, jint card_id, jfloat scale_x, jfloat scale_y) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (renderer) {
        renderer->setCardScale(card_id, scale_x, scale_y);
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_RendererBridge_nativeSetCardAlpha(JNIEnv* env, jobject thiz, jlong renderer_ptr, jint card_id, jfloat alpha) {
    TrashPiles::RendererWrapper* renderer = reinterpret_cast<TrashPiles::RendererWrapper*>(renderer_ptr);
    if (renderer) {
        renderer->setCardAlpha(card_id, alpha);
    }
}

} // extern "C"