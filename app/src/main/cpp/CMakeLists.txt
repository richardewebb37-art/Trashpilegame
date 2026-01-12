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
# Find Skia - assume it's installed or use Android's built-in
find_package(PkgConfig QUIET)
if(PkgConfig_FOUND)
    pkg_check_modules(SKIA QUIET skia)
endif()

# Create include directory for Skia headers
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/third_party/skia/include)

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
)

target_include_directories(renderer_wrapper PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/renderer
    ${CMAKE_CURRENT_SOURCE_DIR}/third_party/skia/include
)

# Link Skia if available
if(SKIA_FOUND)
    target_link_libraries(renderer_wrapper ${SKIA_LIBRARIES})
    target_include_directories(renderer_wrapper PRIVATE ${SKIA_INCLUDE_DIRS})
endif()

# Game engine wrapper (uses libGDX from Kotlin)
add_library(game_engine_wrapper STATIC
    game_engine/game_engine_wrapper.cpp
)

# Audio wrapper (uses Oboe)
add_library(audio_wrapper STATIC
    audio/audio_wrapper.cpp
)

target_link_libraries(audio_wrapper
    oboe
)

target_include_directories(audio_wrapper PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/audio
    ${OBOE_DIR}/include
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
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/renderer
    ${CMAKE_CURRENT_SOURCE_DIR}/audio
    ${CMAKE_CURRENT_SOURCE_DIR}/game_engine
    ${CMAKE_CURRENT_SOURCE_DIR}/gcms
    ${CMAKE_CURRENT_SOURCE_DIR}/jni
    ${CMAKE_CURRENT_SOURCE_DIR}/third_party/skia/include
    ${OBOE_DIR}/include
)

# Compiler flags
target_compile_options(trash-piles-native PRIVATE
    -Wall
    -Wextra
    -O2
    -fno-rtti
    -fno-exceptions
)

# Add preprocessor macros
target_compile_definitions(trash-piles-native PRIVATE
    NDEBUG=1
    SKIA_IMPLEMENTATION=1
)

# Set output directory
set_target_properties(trash-piles-native PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
)