#include <jni.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "audio/audio_wrapper.h"

#define LOG_TAG "TrashPiles-AudioJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

// Global audio instance
static TrashPiles::AudioWrapper* g_audio = nullptr;

JNIEXPORT jlong JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeCreateAudioEngine(JNIEnv* env, jobject thiz) {
    if (g_audio) {
        LOGE("Audio engine already created");
        return 0;
    }
    
    g_audio = new TrashPiles::AudioWrapper();
    LOGI("Audio engine created");
    return reinterpret_cast<jlong>(g_audio);
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeDestroyAudioEngine(JNIEnv* env, jobject thiz, jlong audio_ptr) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (audio) {
        delete audio;
        if (audio == g_audio) {
            g_audio = nullptr;
        }
        LOGI("Audio engine destroyed");
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeSetAssetManager(JNIEnv* env, jobject thiz, jlong audio_ptr, jobject asset_manager) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (!audio) return;
    
    AAssetManager* assetManager = AAssetManager_fromJava(env, asset_manager);
    TrashPiles::AudioWrapper::setAssetManager(assetManager);
    LOGI("Asset manager set for audio engine");
}

JNIEXPORT jboolean JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeInitialize(JNIEnv* env, jobject thiz, jlong audio_ptr) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (!audio) {
        LOGE("Cannot initialize audio - engine is null");
        return JNI_FALSE;
    }
    
    bool result = audio->initialize();
    LOGI("Audio engine initialization: %s", result ? "SUCCESS" : "FAILED");
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeCleanup(JNIEnv* env, jobject thiz, jlong audio_ptr) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (audio) {
        audio->cleanup();
        LOGI("Audio engine cleanup completed");
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativePlaySound(JNIEnv* env, jobject thiz, jlong audio_ptr, jstring sound_name) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (!audio) return;
    
    const char* soundNameStr = env->GetStringUTFChars(sound_name, nullptr);
    if (soundNameStr) {
        audio->playSound(soundNameStr);
        env->ReleaseStringUTFChars(sound_name, soundNameStr);
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeStopSound(JNIEnv* env, jobject thiz, jlong audio_ptr, jstring sound_name) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (!audio) return;
    
    const char* soundNameStr = env->GetStringUTFChars(sound_name, nullptr);
    if (soundNameStr) {
        audio->stopSound(soundNameStr);
        env->ReleaseStringUTFChars(sound_name, soundNameStr);
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeStopAllSounds(JNIEnv* env, jobject thiz, jlong audio_ptr) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (audio) {
        audio->stopAllSounds();
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativePlayMusic(JNIEnv* env, jobject thiz, jlong audio_ptr, jstring music_name, jboolean loop) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (!audio) return;
    
    const char* musicNameStr = env->GetStringUTFChars(music_name, nullptr);
    if (musicNameStr) {
        audio->playMusic(musicNameStr, loop);
        env->ReleaseStringUTFChars(music_name, musicNameStr);
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeStopMusic(JNIEnv* env, jobject thiz, jlong audio_ptr) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (audio) {
        audio->stopMusic();
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativePauseMusic(JNIEnv* env, jobject thiz, jlong audio_ptr) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (audio) {
        audio->pauseMusic();
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeResumeMusic(JNIEnv* env, jobject thiz, jlong audio_ptr) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (audio) {
        audio->resumeMusic();
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeSetSoundVolume(JNIEnv* env, jobject thiz, jlong audio_ptr, jfloat volume) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (audio) {
        audio->setSoundVolume(volume);
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeSetMusicVolume(JNIEnv* env, jobject thiz, jlong audio_ptr, jfloat volume) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (audio) {
        audio->setMusicVolume(volume);
    }
}

JNIEXPORT void JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeSetMasterVolume(JNIEnv* env, jobject thiz, jlong audio_ptr, jfloat volume) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (audio) {
        audio->setMasterVolume(volume);
    }
}

JNIEXPORT jboolean JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeIsMusicPlaying(JNIEnv* env, jobject thiz, jlong audio_ptr) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (audio) {
        return audio->isMusicPlaying() ? JNI_TRUE : JNI_FALSE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_trashpiles_AudioEngineBridge_nativeIsSoundPlaying(JNIEnv* env, jobject thiz, jlong audio_ptr, jstring sound_name) {
    TrashPiles::AudioWrapper* audio = reinterpret_cast<TrashPiles::AudioWrapper*>(audio_ptr);
    if (!audio) return JNI_FALSE;
    
    const char* soundNameStr = env->GetStringUTFChars(sound_name, nullptr);
    if (soundNameStr) {
        bool result = audio->isSoundPlaying(soundNameStr);
        env->ReleaseStringUTFChars(sound_name, soundNameStr);
        return result ? JNI_TRUE : JNI_FALSE;
    }
    
    return JNI_FALSE;
}

} // extern "C"