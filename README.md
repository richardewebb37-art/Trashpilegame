# 🎮 Trash Piles - Native Android Card Game

A native Android implementation of the Trash Piles card game, built with Kotlin, Jetpack Compose, and integrated with custom C++ game engines.

## 🏗️ Architecture

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Build System**: Gradle (Kotlin DSL)
- **Native Code**: C++ with JNI/NDK integration

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
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- NDK 25.2.9519653 or later
- CMake 3.22.1 or later

### Building the Project

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd TrashPiles
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the TrashPiles directory

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for dependencies to download

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run on device/emulator**
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio

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