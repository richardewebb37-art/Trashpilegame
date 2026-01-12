# Android Studio Setup Guide for Trash Piles

## Overview

This guide will help you set up the Trash Piles project in Android Studio and ensure everything is organized correctly.

## Project Structure

The Trash Piles project follows standard Android Studio conventions:

```
TrashPiles/
├── app/                          # Main application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/            # Kotlin source code
│   │   │   ├── res/             # Android resources
│   │   │   └── cpp/             # Native C++ code
│   │   ├── test/                # Unit tests
│   │   └── androidTest/         # Instrumented tests
│   └── build.gradle.kts         # App module build configuration
├── build.gradle.kts             # Root build configuration
├── settings.gradle.kts          # Project settings
├── gradle.properties            # Gradle properties
├── gradlew                      # Gradle wrapper (Unix/Linux/Mac)
├── gradlew.bat                  # Gradle wrapper (Windows)
└── documentation/               # Project documentation
```

## Opening the Project

### Step 1: Open Android Studio

Launch Android Studio on your computer.

### Step 2: Open the Project

1. Click **File** → **Open**
2. Navigate to the `TrashPiles` directory
3. Select the folder and click **OK**

### Step 3: Wait for Gradle Sync

Android Studio will automatically:
- Download Gradle (version 8.1)
- Download dependencies
- Index the project
- Run initial builds

**This may take 5-10 minutes on first open.**

### Step 4: Verify Project Structure

Once the sync completes, verify that:

✅ The project appears in the **Project** view
✅ All Kotlin files are under `app/src/main/java/com/trashpiles/`
✅ All resources are under `app/src/main/res/`
✅ Native code is under `app/src/main/cpp/`
✅ Tests are under `app/src/test/` and `app/src/androidTest/`
✅ No errors appear in the **Build** tab

## Building the Project

### Using Android Studio

1. Click **Build** → **Make Project** (or press `Ctrl+F9` / `Cmd+F9`)
2. Wait for the build to complete
3. Check the **Build** tab for any errors

### Using Command Line

From the project root directory:

```bash
# Unix/Linux/Mac
./gradlew build

# Windows
gradlew.bat build
```

## Running Tests

### Unit Tests

```bash
./gradlew test
```

### Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

## Building APK

### Debug APK

```bash
./gradlew assembleDebug
```

The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK

```bash
./gradlew assembleRelease
```

The APK will be located at: `app/build/outputs/apk/release/app-release.apk`

## Project Architecture

### Dual-Activity Design

This project uses **two activities** for different purposes:

1. **MainActivity** (Compose UI)
   - Package: `com.trashpiles.presentation.MainActivity`
   - Purpose: Main menu and navigation
   - Uses Jetpack Compose
   - Entry point of the app
   - Screens: Home, Game, Rules, Settings, Statistics

2. **GameActivity** (View-based UI)
   - Package: `com.trashpiles.presentation.GameActivity`
   - Purpose: Actual game gameplay
   - Uses traditional Android Views
   - Integrated with native engines (Skia, Oboe, libGDX)
   - Accessible from MainActivity

### Package Structure

```
com.trashpiles/
├── gcms/                           # Game Core Management System
│   ├── GCMSController.kt          # Game logic controller
│   ├── GCMSState.kt               # Game state
│   ├── GCMSEvent.kt               # Event system
│   ├── GCMSCommand.kt             # Command system
│   ├── GCMSValidator.kt           # Validation logic
│   ├── DeckBuilder.kt             # Card deck management
│   ├── GameRules.kt               # Game rules
│   ├── SkillAbilitySystem.kt      # Skill/Ability system
│   ├── SkillAbilityLogic.kt       # Skill/Ability logic
│   └── handlers/                  # Command handlers
│       ├── CommandHandler.kt
│       ├── TurnCommandHandler.kt
│       ├── CardCommandHandler.kt
│       ├── SkillCommandHandler.kt
│       └── MatchCommandHandler.kt
├── presentation/                   # UI Layer
│   ├── MainActivity.kt            # Menu navigation (Compose)
│   ├── GameActivity.kt            # Game screen (View-based)
│   ├── screens/                   # Compose screens
│   │   ├── home/
│   │   ├── game/
│   │   ├── rules/
│   │   ├── settings/
│   │   └── stats/
│   ├── navigation/                # Navigation graph
│   └── theme/                     # App theme
├── data/                          # Data Layer
│   ├── models/                    # Data models
│   │   ├── Player.kt
│   │   ├── GameState.kt
│   │   └── Card.kt
│   ├── local/                     # Local database
│   └── repository/                # Data repositories
├── game/                          # Game Components
│   ├── GameRenderer.kt            # Rendering system
│   ├── GameAudio.kt               # Audio system
│   └── GameFlowController.kt      # Game flow orchestration
├── native/                        # Native Engine Bridges
│   ├── RendererBridge.kt          # Skia renderer bridge
│   ├── AudioEngineBridge.kt       # Oboe audio bridge
│   ├── GameEngineBridge.kt        # libGDX game engine bridge
│   └── NativeEngineWrapper.kt     # Native wrapper
└── utils/                         # Utilities
    ├── AssetLoader.kt             # Asset loading
    └── AssetPaths.kt              # Asset paths
```

## Adding Assets

### Card Images

Place card images in appropriate drawable directories:

```
app/src/main/res/
├── drawable-mdpi/                 # Medium density (48x48dp)
├── drawable-hdpi/                 # High density (72x72dp)
├── drawable-xhdpi/                # Extra High (96x96dp)
├── drawable-xxhdpi/               # Extra Extra High (144x144dp)
└── drawable-xxxhdpi/              # Extra Extra Extra High (192x192dp)
```

**Naming Convention:**
- `card_ace_spades.png`
- `card_two_hearts.png`
- `card_king_diamonds.png`
- `card_back.png`

### Audio Files

Place audio files in the `raw` directory:

```
app/src/main/res/raw/
├── card_draw.mp3
├── card_place.mp3
├── card_flip.mp3
├── round_win.mp3
├── game_win.mp3
└── background_music.mp3
```

### Custom Fonts

Place fonts in the `font` directory (create if needed):

```
app/src/main/res/font/
├── custom_font_regular.ttf
└── custom_font_bold.ttf
```

## Native Code

The project includes native C++ code for premium engines:

### Skia Graphics Engine
- Location: `app/src/main/cpp/renderer/`
- Purpose: 2D rendering and animations

### Oboe Audio Engine
- Location: `app/src/main/cpp/audio/`
- Purpose: Low-latency audio playback

### libGDX Game Engine
- Location: `app/src/main/cpp/game_engine/`
- Purpose: Game framework and physics

### JNI Bridges
- Location: `app/src/main/cpp/jni/`
- Purpose: Bridge between Kotlin and C++

### Building Native Code

Native code is automatically built via CMake during the Gradle build process.

## Troubleshooting

### Gradle Sync Fails

1. Check internet connection (dependencies need to download)
2. Ensure Gradle version 8.1 is used
3. Clear Gradle cache: `File` → `Invalidate Caches / Restart`
4. Delete `.gradle` folder and `.idea` folder, then reopen

### Build Errors

1. Check the **Build** tab for specific errors
2. Ensure JDK 17 is configured: `File` → **Project Structure** → **SDK Location**
3. Verify NDK is installed: `File` → **Project Structure** → **SDK Location** → **NDK Location**
4. Clean and rebuild: `Build` → **Clean Project** → `Build` → **Rebuild Project`

### Native Build Errors

1. Ensure CMake is installed with Android SDK
2. Verify NDK is configured in `local.properties`
3. Check CMakeLists.txt for correct paths
4. Ensure third-party engines are present in `cpp/third_party/`

### Cannot Find R.java

1. Clean the project: `Build` → **Clean Project**
2. Rebuild the project: `Build` → **Rebuild Project**
3. Restart Android Studio

## Build Scripts

The project includes helpful build scripts:

### quick-build.sh
Quick build without tests:
```bash
./quick-build.sh
```

### build-and-test.sh
Build and run all tests:
```bash
./build-and-test.sh
```

### install-debug.sh
Build and install on connected device:
```bash
./install-debug.sh
```

## Documentation

Additional documentation is available in the `documentation/` folder:

- `COMPLETE_PROJECT_REPORT.md` - Full project overview
- `CONTROLLER_REFACTORING_GUIDE.md` - GCMS architecture
- `CORRECTED_SCORING_SYSTEM.md` - Scoring system details
- `INTEGRATION_GUIDE.md` - Native engine integration
- `SKILL_ABILITY_IMPLEMENTATION_SUMMARY.md` - Skill/Ability system
- `LEVELING_SYSTEM_IMPLEMENTATION.md` - Leveling and progression
- And more...

## Testing

The project includes comprehensive test coverage:

### Unit Tests
- Located: `app/src/test/java/com/trashpiles/gcms/`
- Run: `./gradlew test`
- Coverage: GCMS system, game logic, command handlers

### Integration Tests
- Located: `app/src/androidTest/java/com/trashpiles/`
- Run: `./gradlew connectedAndroidTest`
- Coverage: Full game flow, native integration

## Next Steps

1. ✅ Open project in Android Studio
2. ✅ Wait for Gradle sync to complete
3. ✅ Build the project to verify setup
4. ✅ Run tests to ensure everything works
5. ⏳ Add custom assets (cards, audio, fonts)
6. ⏳ Implement native engine C++ code
7. ⏳ Test on physical device

## Support

For issues or questions:
- Check the documentation in `documentation/`
- Review the build scripts for examples
- Ensure all dependencies are up to date
- Verify Android SDK and NDK are properly configured

## Summary

The Trash Piles project is organized as a standard Android Studio project with:
- ✅ Proper Gradle wrapper (gradlew/gradlew.bat)
- ✅ Correct package structure
- ✅ Standard resource directories
- ✅ Native C++ code integration
- ✅ Comprehensive testing
- ✅ Build scripts for easy deployment
- ✅ Complete documentation

**The project is ready to open in Android Studio without any manual rearrangement!**