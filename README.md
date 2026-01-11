# 🃏 Trash Piles - Native Android Card Game

![Android](https://img.shields.io/badge/Android-8.0+-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)

A native Android card game built with Kotlin, featuring a custom Game Core Management System (GCMS), skill/ability progression system, and premium C++ engines (Skia, Oboe, libGDX).

## 🎮 Game Overview

**Trash Piles** is a strategic card game where players race to arrange cards Ace through 10 in order. The game features:

- 🎴 **Classic Card Game Mechanics** - Draw, place, discard cards strategically
- 🧠 **Skill &amp; Ability System** - Unlock and use special abilities
- 📈 **Unlimited Leveling** - Progress through infinite levels with XP rewards
- 🤖 **AI Opponents** - Play against intelligent AI players
- 🏆 **Match Scoring** - Earn points based on card placement performance

## 🏗️ Architecture

### Core Systems

#### 1. GCMS (Game Core Management System)
The authoritative "brain" of the game that manages all game state and logic.

```kotlin
// Commands flow through GCMS
Player Input → Commands → GCMS → Validation → Execution → Events → All Subsystems
```

**Features:**
- Single source of truth for game state
- Command validation before execution
- Event-driven architecture
- Immutable state management
- Command history for undo functionality

#### 2. Refactored Controller Architecture
New lightweight controller design (2025 refactoring):

- **GCMSControllerRefactored**: 145 lines (75% reduction from 586 lines)
- **4 Domain Handlers**: Turn, Card, Skill, Match
- **Command Routing**: Automatic routing to appropriate handlers
- **Event Broadcasting**: SharedFlow for reactive updates

#### 3. Skill &amp; Ability System
Comprehensive progression system with two trees:

**Skill Tree:**
- 15 skills across 3 tiers
- Unlocked with Skill Points (SP)
- Permanent character improvements

**Ability Tree:**
- 9 abilities across 3 tiers
- Unlocked with Ability Points (AP)
- Active effects during gameplay

**Leveling:**
- Unlimited progression using logarithmic formula
- XP earned from matches and rounds
- Level-gated unlocks for skills/abilities

### Technology Stack
```
Kotlin (UI + GCMS + Integration)
    ↕ JNI Bridges
Premium Engines (C++):
├─ Skia (2D Graphics Rendering)
├─ Oboe (Low-latency Audio)
└─ libGDX (Game Engine Framework)
```

### Project Structure

```
TrashPiles/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/trashpiles/
│   │   │   │   ├── data/           # Data layer (models, repositories)
│   │   │   │   ├── domain/         # Business logic (use cases)
│   │   │   │   ├── presentation/   # UI layer (screens, components)
│   │   │   │   ├── native/         # JNI bridge to C++ engines
│   │   │   │   ├── di/             # Dependency injection modules
│   │   │   │   └── utils/          # Utility classes
│   │   │   ├── cpp/                # Your existing C++ engines
│   │   │   │   ├── renderer/       # Graphics rendering engine
│   │   │   │   ├── audio/          # Audio engine
│   │   │   │   ├── game/           # Game engine
│   │   │   │   ├── gcms/           # Game Card Management System
│   │   │   │   └── jni/            # JNI bridge implementation
│   │   │   └── res/                # Android resources
│   │   ├── androidTest/            # Instrumented tests
│   │   └── test/                   # Unit tests
│   └── build.gradle.kts
├── buildSrc/                       # Build configuration
└── build.gradle.kts
```

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Latest stable version
- **Android SDK**: API Level 24 (Android 7.0) or higher
- **JDK**: 11 or higher
- **Gradle**: 8.2 (included)
- **NDK**: r25 or higher (for native engines)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/richardewebb37-art/Trashpilethe-game.git
   cd Trashpilethe-game
   ```

2. **Open in Android Studio:**
   - File → Open → Select `TrashPiles` directory
   - Wait for Gradle sync to complete

3. **Build the project:**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on emulator or device:**
   - Connect Android device via USB
   - Enable USB debugging
   - Click "Run" in Android Studio or use:
     ```bash
     ./install-debug.sh
     ```

### Quick Build Scripts

Three convenient scripts are included:

```bash
# Quick build (no tests)
./quick-build.sh

# Build with tests
./build-and-test.sh

# Build and install on device
./install-debug.sh
```

## 🎯 Features

### Current Implementation
- ✅ Native Android architecture with Kotlin
- ✅ Jetpack Compose UI framework
- ✅ JNI bridge for C++ engine integration
- ✅ Clean architecture with MVVM pattern
- ✅ Hilt dependency injection
- ✅ Navigation system
- ✅ Material Design 3 theming

### Planned Features
- 🔄 Complete game logic implementation
- 🔄 Card animations
- 🔄 Sound effects and music
- 🔄 Statistics tracking
- 🔄 Settings persistence
- 🔄 Multiplayer support

## 🔧 Native Engine Integration

### Your Existing Engines

This project is designed to integrate with your existing C++ engines:

1. **Renderer** - Graphics rendering (OpenGL/Vulkan)
2. **Audio Engine** - Sound effects and music playback
3. **Game Engine** - Core game logic
4. **GCMS** - Game Card Management System

### JNI Bridge

The `NativeEngineWrapper.kt` class provides a Kotlin interface to your C++ code:

```kotlin
class NativeEngineWrapper {
    external fun initRenderer(width: Int, height: Int): Boolean
    external fun renderFrame()
    external fun playSound(soundId: Int)
    external fun updateGame(deltaTime: Float)
    // ... more methods
}
```

### Adding Your Engine Code

1. Place your existing C++ engine files in `app/src/main/cpp/`
2. Update `CMakeLists.txt` to include your source files
3. Implement JNI bridge functions in `jni/` directory
4. Build and test

## 📱 Screens

- **Home Screen** - Main menu with game options
- **Game Screen** - Main gameplay area
- **Settings Screen** - Game configuration
- **Rules Screen** - Game instructions
- **Stats Screen** - Player statistics

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

## 📦 Building Release APK

```bash
./gradlew assembleRelease
```

The APK will be generated in `app/build/outputs/apk/release/`

## 🎨 Theming

The app uses Material Design 3 with a custom green card table theme. Colors and styles can be customized in:
- `presentation/theme/Color.kt`
- `presentation/theme/Theme.kt`
- `res/values/colors.xml`

## 📄 License

[Your License Here]

## 👥 Contributors

[Your Name/Team]

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Support

For issues and questions, please open an issue on GitHub.

---

**Built with ❤️ using Kotlin and Jetpack Compose**