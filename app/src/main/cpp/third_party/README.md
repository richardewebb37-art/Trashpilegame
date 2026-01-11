# Third-Party Premium Engines

This directory contains the premium engines for Trash Piles:

## Engines Included

### 1. Skia Graphics Engine
- **Location**: `skia/`
- **Purpose**: High-performance 2D rendering
- **Source**: https://skia.googlesource.com/skia.git
- **License**: BSD 3-Clause
- **Used For**: Rendering cards, UI elements, animations

### 2. libGDX Game Framework
- **Location**: `libgdx/`
- **Purpose**: Game engine framework
- **Source**: https://github.com/libgdx/libgdx.git
- **License**: Apache 2.0
- **Used For**: Game loop, scene management, input handling

### 3. Oboe Audio Engine
- **Location**: `oboe/`
- **Purpose**: Low-latency audio playback
- **Source**: https://github.com/google/oboe.git
- **License**: Apache 2.0
- **Used For**: Sound effects, background music

## Integration

These engines are integrated via JNI bridges located in:
- `../jni/skia_bridge.cpp`
- `../jni/libgdx_bridge.cpp`
- `../jni/oboe_bridge.cpp`

## Build Configuration

See `../CMakeLists.txt` for build configuration.