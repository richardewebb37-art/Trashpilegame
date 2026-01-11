cmake_minimum_required(VERSION 3.22.1)
project("trash-piles-native")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add third-party engine paths
set(SKIA_DIR ${CMAKE_CURRENT_SOURCE_DIR}/third_party/skia)
set(LIBGDX_DIR ${CMAKE_CURRENT_SOURCE_DIR}/third_party/libgdx)
set(OBOE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/third_party/oboe)

# ============================================
# OBOE AUDIO ENGINE (Google)
# ============================================
add_subdirectory(${OBOE_DIR} oboe)

# ============================================
# SKIA RENDERER (Google)
# ============================================
# Note: Skia requires pre-built binaries or custom build
# For now, we'll link against Android's Skia
# TODO: Build Skia separately or use pre-built binaries

# ============================================
# LIBGDX GAME ENGINE
# ============================================
# Note: libGDX is primarily Java/Kotlin
# We'll use it from the Kotlin layer
# Only native components needed here

# ============================================
# CUSTOM WRAPPERS
# ============================================

# Renderer wrapper (uses Skia)
add_library(renderer_wrapper STATIC
    renderer/renderer_wrapper.cpp
    renderer/skia_renderer.cpp
)

target_include_directories(renderer_wrapper PRIVATE
    ${SKIA_DIR}/include
)

# Game engine wrapper (uses libGDX from Kotlin)
add_library(game_engine_wrapper STATIC
    game_engine/game_engine_wrapper.cpp
)

# Audio wrapper (uses Oboe)
add_library(audio_wrapper STATIC
    audio/audio_wrapper.cpp
    audio/oboe_audio.cpp
)

target_link_libraries(audio_wrapper
    oboe
)

# ============================================
# JNI BRIDGE - Connects Kotlin to C++ Engines
# ============================================
add_library(trash-piles-native SHARED
    jni/jni_bridge.cpp
    jni/renderer_jni.cpp
    jni/audio_jni.cpp
    jni/game_engine_jni.cpp
)

# Link all libraries together
target_link_libraries(trash-piles-native
    renderer_wrapper
    game_engine_wrapper
    audio_wrapper
    oboe
    android
    log
    GLESv3
    EGL
)

# Include directories
target_include_directories(trash-piles-native PRIVATE
    ${SKIA_DIR}/include
    ${OBOE_DIR}/include
)