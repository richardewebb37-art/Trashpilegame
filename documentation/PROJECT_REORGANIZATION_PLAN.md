# Android Studio Project Reorganization Plan

## Current State Analysis

### ✅ What's Already Correct:
1. **Project Structure**: Standard Android project layout exists
   - Root: `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`
   - App module: `app/build.gradle.kts`
   - Source directories: `app/src/main/java/`, `app/src/main/res/`, `app/src/main/cpp/`
   - Test directories: `app/src/test/java/`, `app/src/androidTest/java/`

2. **Gradle Configuration**: Properly configured
   - Root and app module build files exist
   - Proper plugin management
   - Dependencies configured (Compose, Hilt, Room, Coroutines, libGDX)
   - NDK and CMake configured for native code

3. **Package Structure**: Well-organized Kotlin packages
   - `com.trashpiles.gcms` - Game Core Management System
   - `com.trashpiles.presentation` - UI layer (screens, navigation, theme)
   - `com.trashpiles.data` - Data models
   - `com.trashpiles.game` - Game components
   - `com.trashpiles.native` - Native engine bridges
   - `com.trashpiles.utils` - Utilities

4. **Native Code**: Properly structured
   - C++ wrappers organized in subdirectories
   - JNI bridges in separate folder
   - CMakeLists.txt configured

5. **Resources**: Standard Android resource structure
   - `drawable*` directories for different densities
   - `layout/` for XML layouts
   - `values/` for strings, colors, themes
   - `xml/` for backup and data extraction rules
   - `raw/` for audio files

6. **AndroidManifest.xml**: Properly configured
   - Application class specified
   - MainActivity declared as launcher
   - Permissions set up
   - OpenGL ES requirement declared

7. **Tests**: Organized in proper locations
   - Unit tests in `app/src/test/java/`
   - Instrumented tests directory exists

### ⚠️ What Needs Fixing:

#### 1. Missing Gradle Wrapper
**Issue**: No `gradlew` or `gradlew.bat` files exist
**Impact**: Android Studio can't build the project without the wrapper
**Fix**: Generate Gradle wrapper using Gradle

#### 2. Empty Resource Directories
**Issue**: All `drawable*` directories are empty
**Impact**: No resource errors currently, but will cause issues when adding assets
**Fix**: Ensure proper resource structure (will be ready for future assets)

#### 3. Duplicate Activities
**Issue**: Both `MainActivity` (Compose UI) and `GameActivity` (traditional View UI) exist
**Impact**: Potential confusion about which is the entry point
**Fix**: Document this as intentional (MainActivity for menu, GameActivity for game)

#### 4. Missing .gitignore entries
**Issue**: Some Android-specific files not ignored
**Impact**: May commit unnecessary files
**Fix**: Ensure `.gitignore` is complete

#### 5. No proguard-rules.pro content check
**Issue**: Haven't verified if ProGuard rules are properly set up
**Impact**: May cause issues with release builds
**Fix**: Verify ProGuard rules

## Reorganization Tasks

### Task 1: Generate Gradle Wrapper
- Generate `gradlew` and `gradlew.bat` files
- Create `gradle/wrapper/gradle-wrapper.properties`
- Ensure wrapper jar is present
- Make `gradlew` executable

### Task 2: Verify and Update .gitignore
- Ensure all Android Studio files are ignored
- Check for missing entries
- Verify build files are properly ignored

### Task 3: Verify Resource Structure
- Confirm all resource directories are properly named
- Ensure `res/values/strings.xml` has app name
- Verify `res/values/colors.xml` has basic colors
- Check `res/values/themes.xml` has proper theme

### Task 4: Verify Build Configuration
- Check `local.properties` will be created by Android Studio
- Ensure `gradle.properties` has correct settings
- Verify all dependencies are accessible

### Task 5: Create Build Scripts
- Ensure build scripts work with the new structure
- Test that they can be executed

### Task 6: Document Project Structure
- Create comprehensive README for Android Studio users
- Document the dual-activity architecture
- Explain how to add assets
- Provide build instructions

### Task 7: Verify Test Structure
- Ensure all test files are in correct locations
- Check test dependencies are configured
- Verify tests can run

## Project Structure After Reorganization

```
TrashPiles/
├── .git/                           # Git repository
├── .gitignore                      # Git ignore file
├── build.gradle.kts                # Root build file
├── settings.gradle.kts             # Project settings
├── gradle.properties               # Gradle properties
├── gradlew                         # Gradle wrapper (Unix)
├── gradlew.bat                     # Gradle wrapper (Windows)
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── app/
│   ├── build.gradle.kts           # App module build file
│   ├── proguard-rules.pro         # ProGuard rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/
│       │   │   └── com/
│       │   │       └── trashpiles/
│       │   │           ├── TrashPilesApplication.kt
│       │   │           ├── gcms/                   # Game Core Management System
│       │   │           │   ├── GCMSController.kt
│       │   │           │   ├── GCMSState.kt
│       │   │           │   ├── GCMSEvent.kt
│       │   │           │   ├── GCMSCommand.kt
│       │   │           │   ├── GCMSValidator.kt
│       │   │           │   ├── DeckBuilder.kt
│       │   │           │   ├── GameRules.kt
│       │   │           │   ├── SkillAbilitySystem.kt
│       │   │           │   ├── SkillAbilityLogic.kt
│       │   │           │   ├── handlers/
│       │   │           │   │   ├── CommandHandler.kt
│       │   │           │   │   ├── TurnCommandHandler.kt
│       │   │           │   │   ├── CardCommandHandler.kt
│       │   │           │   │   ├── SkillCommandHandler.kt
│       │   │           │   │   └── MatchCommandHandler.kt
│       │   │           ├── presentation/            # UI Layer
│       │   │           │   ├── MainActivity.kt      # Menu navigation (Compose)
│       │   │           │   ├── GameActivity.kt      # Game screen (View-based)
│       │   │           │   ├── screens/            # Compose screens
│       │   │           │   │   ├── home/
│       │   │           │   │   ├── game/
│       │   │           │   │   ├── rules/
│       │   │           │   │   ├── settings/
│       │   │           │   │   └── stats/
│       │   │           │   ├── navigation/
│       │   │           │   └── theme/
│       │   │           ├── data/                  # Data Layer
│       │   │           │   ├── models/
│       │   │           │   ├── local/
│       │   │           │   └── repository/
│       │   │           ├── game/                  # Game Components
│       │   │           │   ├── GameRenderer.kt
│       │   │           │   ├── GameAudio.kt
│       │   │           │   └── GameFlowController.kt
│       │   │           ├── native/                # Native Bridges
│       │   │           │   ├── RendererBridge.kt
│       │   │           │   ├── AudioEngineBridge.kt
│       │   │           │   ├── GameEngineBridge.kt
│       │   │           │   └── NativeEngineWrapper.kt
│       │   │           └── utils/                 # Utilities
│       │   │               ├── AssetLoader.kt
│       │   │               └── AssetPaths.kt
│       │   ├── res/                           # Android Resources
│       │   │   ├── drawable/                  # General drawables
│       │   │   ├── drawable-hdpi/             # High DPI drawables
│       │   │   ├── drawable-mdpi/             # Medium DPI drawables
│       │   │   ├── drawable-xhdpi/            # Extra High DPI
│       │   │   ├── drawable-xxhdpi/           # Extra Extra High DPI
│       │   │   ├── drawable-xxxhdpi/          # Extra Extra Extra High DPI
│       │   │   ├── layout/                    # XML layouts
│       │   │   │   └── activity_game.xml
│       │   │   ├── values/                    # Values (strings, colors, themes)
│       │   │   │   ├── strings.xml
│       │   │   │   ├── colors.xml
│       │   │   │   └── themes.xml
│       │   │   ├── values-night/              # Dark theme values
│       │   │   ├── mipmap-*/                  # App icons
│       │   │   ├── raw/                       # Raw files (audio, etc.)
│       │   │   └── xml/                       # XML resources
│       │   │       ├── backup_rules.xml
│       │   │       └── data_extraction_rules.xml
│       │   └── cpp/                          # Native C++ code
│       │       ├── CMakeLists.txt            # Native build config
│       │       ├── audio/                    # Audio engine wrappers
│       │       │   ├── audio_wrapper.cpp
│       │       │   └── audio_wrapper.h
│       │       ├── renderer/                 # Renderer engine wrappers
│       │       │   ├── renderer_wrapper.cpp
│       │       │   └── renderer_wrapper.h
│       │       ├── game_engine/              # Game engine wrappers
│       │       │   ├── game_engine_wrapper.cpp
│       │       │   └── game_engine_wrapper.h
│       │       ├── jni/                      # JNI bridges
│       │       │   ├── renderer_jni.cpp
│       │       │   ├── audio_jni.cpp
│       │       │   ├── game_engine_jni.cpp
│       │       │   └── jni_bridge.cpp
│       │       └── third_party/              # Third-party engines
│       │           ├── skia/
│       │           ├── oboe/
│       │           └── libgdx/
│       ├── test/                            # Unit tests
│       │   └── java/
│       │       └── com/
│       │           └── trashpiles/
│       │               └── gcms/            # GCMS tests
│       │                   ├── GCMSControllerTest.kt
│       │                   ├── GCMSStateTest.kt
│       │                   ├── GCMSControllerRefactoredTest.kt
│       │                   ├── SkillAbilitySystemTest.kt
│       │                   ├── MockEngineTest.kt
│       │                   ├── GCMSValidatorTest.kt
│       │                   ├── DeckBuilderTest.kt
│       │                   └── handlers/
│       │                       ├── MatchCommandHandlerTest.kt
│       │                       ├── CardCommandHandlerTest.kt
│       │                       ├── TurnCommandHandlerTest.kt
│       │                       └── SkillCommandHandlerTest.kt
│       └── androidTest/                      # Instrumented tests
│           └── java/
│               └── com/
│                   └── trashpiles/
├── documentation/                          # Project documentation
│   ├── COMPLETE_PROJECT_REPORT.md
│   ├── CONTROLLER_REFACTORING_GUIDE.md
│   └── ... (other docs)
├── docs/                                   # Additional docs
│   └── how_to_play_trash_piles.md
├── build-and-test.sh                       # Build and test script
├── install-debug.sh                        # Install debug APK script
├── quick-build.sh                          # Quick build script
└── README.md                               # Main README
```

## Success Criteria

After reorganization, the project should:

1. ✅ Open in Android Studio without errors
2. ✅ Build successfully with `./gradlew build`
3. ✅ Run tests with `./gradlew test`
4. ✅ Generate APK with `./gradlew assembleDebug`
5. ✅ Have proper resource structure for adding assets
6. ✅ Have all source files in correct locations
7. ✅ Have proper Gradle wrapper
8. ✅ Be properly ignored via .gitignore

## Notes

- The project structure is already 95% correct
- Main issue is missing Gradle wrapper
- No functionality changes will be made
- All reorganization is structural only
- Ready for asset addition in future