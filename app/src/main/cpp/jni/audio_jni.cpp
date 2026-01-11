#include <jni.h>
#include <android/log.h>
#include "../audio/audio_wrapper.h"

#define LOG_TAG "TrashPiles-Audio-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern TrashPiles::AudioWrapper* g_audio;

extern "C" {

/**
 * Initialize the audio engine
 */
JNIEXPORT jboolean JNICALL
Java_com_trashpiles_native_AudioEngineBridge_initAudioEngine(
    JNIEnv* env, jobject obj) {
    
    LOGI("JNI: initAudioEngine called");
    
    if (!g_audio) {
        LOGE("Audio instance is null!");
        return JNI_FALSE;
    }
    
    bool success = g_audio->initialize();
    return success ? JNI_TRUE : JNI_FALSE;
}

/**
 * Play a sound effect
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_AudioEngineBridge_playSound(
    JNIEnv* env, jobject obj, jstring soundName) {
    
    if (!g_audio) return;
    
    const char* sound = env->GetStringUTFChars(soundName, nullptr);
    LOGI("JNI: Playing sound: %s", sound);
    g_audio->playSound(sound);
    env->ReleaseStringUTFChars(soundName, sound);
}

/**
 * Stop a sound effect
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_AudioEngineBridge_stopSound(
    JNIEnv* env, jobject obj, jstring soundName) {
    
    if (!g_audio) return;
    
    const char* sound = env->GetStringUTFChars(soundName, nullptr);
    g_audio->stopSound(sound);
    env->ReleaseStringUTFChars(soundName, sound);
}

/**
 * Stop all sounds
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_AudioEngineBridge_stopAllSounds(
    JNIEnv* env, jobject obj) {
    
    if (g_audio) {
        g_audio->stopAllSounds();
    }
}

/**
 * Play background music
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_AudioEngineBridge_playMusic(
    JNIEnv* env, jobject obj, jstring musicName, jboolean loop) {
    
    if (!g_audio) return;
    
    const char* music = env->GetStringUTFChars(musicName, nullptr);
    LOGI("JNI: Playing music: %s (loop: %s)", music, loop ? "yes" : "no");
    g_audio->playMusic(music, loop == JNI_TRUE);
    env->ReleaseStringUTFChars(musicName, music);
}

/**
 * Stop background music
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_AudioEngineBridge_stopMusic(
    JNIEnv* env, jobject obj) {
    
    if (g_audio) {
        g_audio->stopMusic();
    }
}

/**
 * Pause background music
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_AudioEngineBridge_pauseMusic(
    JNIEnv* env, jobject obj) {
    
    if (g_audio) {
        g_audio->pauseMusic();
    }
}

/**
 * Resume background music
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_AudioEngineBridge_resumeMusic(
    JNIEnv* env, jobject obj) {
    
    if (g_audio) {
        g_audio->resumeMusic();
    }
}

/**
 * Set sound effects volume
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_AudioEngineBridge_setSoundVolume(
    JNIEnv* env, jobject obj, jfloat volume) {
    
    if (g_audio) {
        g_audio->setSoundVolume(volume);
    }
}

/**
 * Set music volume
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_AudioEngineBridge_setMusicVolume(
    JNIEnv* env, jobject obj, jfloat volume) {
    
    if (g_audio) {
        g_audio->setMusicVolume(volume);
    }
}

/**
 * Set master volume
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_AudioEngineBridge_setMasterVolume(
    JNIEnv* env, jobject obj, jfloat volume) {
    
    if (g_audio) {
        g_audio->setMasterVolume(volume);
    }
}

/**
 * Check if music is playing
 */
JNIEXPORT jboolean JNICALL
Java_com_trashpiles_native_AudioEngineBridge_isMusicPlaying(
    JNIEnv* env, jobject obj) {
    
    if (!g_audio) return JNI_FALSE;
    
    return g_audio->isMusicPlaying() ? JNI_TRUE : JNI_FALSE;
}

/**
 * Cleanup audio engine
 */
JNIEXPORT void JNICALL
Java_com_trashpiles_native_AudioEngineBridge_cleanup(
    JNIEnv* env, jobject obj) {
    
    LOGI("JNI: cleanup called");
    
    if (g_audio) {
        g_audio->cleanup();
    }
}

} // extern "C"