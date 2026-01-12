# TrashPiles Game - QA Checklist Template

**Version**: 1.0  
**Last Updated**: 2024  
**Purpose**: Ensure all systems work correctly after every update

---

## 🎯 SYSTEM OVERVIEW

### Core Components to Test:
1. **GCMS (Game Content Management System)**
2. **Game Engine Core Logic**
3. **Native Renderer (Skia)**
4. **Native Audio (Oboe)**
5. **Skills & Abilities System**
6. **Trophy & Challenge System**
7. **Leveling & Progression**
8. **JNI Bridge Integration**
9. **End-to-End Game Flow**
10. **Performance & Memory**

---

## 📋 COMPREHENSIVE TEST LIST

### 1. GCMS TESTING (15 Tests)

#### Command Processing
```
□ InitializeGameCommand creates proper game state
□ StartGameCommand deals cards correctly
□ DrawCardCommand removes card from deck
□ PlaceCardCommand places card in correct slot
□ DiscardCardCommand adds card to discard pile
□ FlipCardCommand flips card face-up
□ EndTurnCommand switches to next player
□ UndoMoveCommand restores previous state
□ SaveGameCommand persists state correctly
□ LoadGameCommand restores saved state
```

#### Event System
```
□ GameInitializedEvent triggers UI updates
□ CardDrawnEvent updates deck count
□ CardPlacedEvent triggers animations
□ TurnEndedEvent switches player UI
□ MatchCompletedEvent awards rewards
```

#### State Management
```
□ Game state immutable (no side effects)
□ State history tracked for undo
□ Concurrent access thread-safe
□ Memory usage reasonable
□ Performance acceptable (<10ms per command)
```

---

### 2. GAME ENGINE TESTING (20 Tests)

#### Core Rules
```
□ Card placement rules enforced (Ace=slot 0, 2=slot 1, etc.)
□ Wild card placement (J, Q, K work anywhere)
□ Win condition detection (10 slots completed)
□ Draw pile management (reshuffle when empty)
□ Discard pile tracking
□ Turn order maintained correctly
□ Round advancement logic
□ Match progression tracking
```

#### Scoring System
```
□ Face-up card values calculated correctly
□ Dice multiplier applied properly
□ Score multipliers (winner vs loser)
□ Penalty calculations (face-down cards)
□ Final score computation
□ SP/AP/XP reward calculations
□ Level-up detection
□ Level-down detection (XP loss)
```

#### Player Actions
```
□ Draw from deck/discard choice
□ Card placement validation
□ Card flipping mechanics
□ Turn skipping (no playable cards)
□ Game ending conditions
□ Tie-breaking logic
```

---

### 3. SKILLS & ABILITIES TESTING (25 Tests)

#### Database Access
```
□ All 100 skills/abilities load correctly
□ Tier distribution (7 tiers, levels 1-200)
□ Category organization (10 categories)
□ Prerequisite validation
□ Cost validation by tier
□ Level requirements enforced
□ Effect descriptions accurate
```

#### Skill Effects (Passive)
```
□ Quick Learner grants +10% XP
□ Focused Mind adds +2 seconds timer
□ Resourceful shows top 2 deck cards
□ Critical Focus adds +1 dice bonus
□ Card Mastery gives +1 point per numbered card
□ Penalty reductions work correctly
□ Timer bonuses stack properly
□ XP bonuses stack properly
□ Immunity flags work correctly
□ Draw bonuses improve odds correctly
```

#### Ability Effects (Active)
```
□ Intuition peeks at top deck card
□ Lucky Break rerolls dice successfully
□ Guard protects card from penalties
□ Power Shot forces opponent discard
□ Skip Draw Phase ends turn immediately
□ Negate Ability blocks opponent
□ Double Points multiplies card score
□ Remove Penalty clears face-card penalties
□ Shield Wall blocks all penalties
□ Free Ability makes next ability cost 0 AP
```

#### Integration
```
□ Skill effects apply to gameplay calculations
□ Ability usage counts tracked correctly
□ Usage limits enforced (per match/round)
□ Ability effects persist for correct duration
□ Skill/ability interactions work correctly
□ Prerequisite chains enforced
□ Cost validation prevents insufficient points
□ XP rewards granted on unlock
□ Level-based availability enforced
□ Trophy conditions met on unlock
```

---

### 4. TROPHY & CHALLENGE TESTING (15 Tests)

#### Trophy System
```
□ 20+ trophies load correctly
□ Rarity system (Common → Legendary)
□ Trophy conditions evaluated correctly
□ Trophy awarded on level-up
□ Duplicate trophies prevented
□ Trophy collection tracked
□ Trophy display in UI
□ Trophy statistics accurate
```

#### Challenge System
```
□ 10 challenge types load correctly
□ Level gating enforced correctly
□ Challenge progress tracked
□ Challenge completion detected
□ Challenge rewards applied correctly
□ New challenges assigned on level-up
□ Challenge difficulty scales properly
□ Challenge UI displays current progress
□ Challenge notifications shown
□ Challenge completion celebrates correctly
```

---

### 5. LEVELING & PROGRESSION TESTING (10 Tests)

#### Level Calculations
```
□ Level formula: Level = floor(log(XP + 1) / log(1.2)) + 1
□ Level 1 requires 0 XP
□ Level 10 requires ~500 XP
□ Level 50 requires ~5000 XP
□ Level 100 requires ~12000 XP
□ Level 200 requires ~30000 XP
□ Level recalculation on XP change
□ Level up animations trigger
□ Level down detection (XP loss)
```

#### Progression Flow
```
□ New players start at Level 1
□ Skills/abilities unlock at correct levels
□ Tier advancement tracked correctly
□ XP multipliers apply correctly
□ SP/AP bonuses apply correctly
□ Dynamic progression unlimited
□ Experience accumulation tracked
```

---

### 6. NATIVE RENDERER TESTING (20 Tests)

#### Initialization
```
□ Skia renderer initializes successfully
□ Display surface created correctly
□ Rendering context established
□ Memory allocated for textures
□ Shader programs compiled
□ Font loading works correctly
□ Asset loading successful
```

#### Card Rendering
```
□ Card backs rendered correctly
□ Card faces rendered correctly
□ Card suits displayed properly
□ Card values readable
□ Card borders and shadows
□ Card highlighting on hover
□ Card selection indicators
□ Card placement validation visuals
```

#### Animations
```
□ Card flip animations smooth (60 FPS)
□ Card deal animations work
□ Card draw animations
□ Card placement animations
□ Dice roll animations
□ Score update animations
□ Level up animations
□ Ability activation animations
```

#### UI Rendering
```
□ Game board rendered correctly
□ Player hands displayed properly
□ Score counters update correctly
□ Timer displays accurately
□ Button states (enabled/disabled)
□ Skill tree rendered properly
□ Trophy collection displayed
```

#### Performance
```
□ Rendering maintains 60 FPS
□ Memory usage under 100MB
□ No visual glitches or tearing
```

---

### 7. NATIVE AUDIO TESTING (15 Tests)

#### Initialization
```
□ Oboe audio initializes successfully
□ Audio device detected
□ Sample rate set correctly (44.1kHz)
□ Buffer size optimal (512 samples)
□ Audio threads created
□ No audio latency issues
□ Volume controls work
```

#### Sound Effects
```
□ Card draw sound plays
□ Card place sound plays
□ Card flip sound plays
□ Dice roll sound plays
□ Ability activation sounds
□ Level up fanfare
□ Trophy unlock chime
□ Error notification sound
```

#### Music System
```
□ Background music loads
□ Music plays continuously
□ Music volume adjustable
□ Music track transitions
□ Music pauses correctly
□ Music resumes correctly
□ Music stops on exit
```

#### Performance
```
□ No audio clipping or distortion
□ Sound effects overlap correctly
□ Audio latency under 50ms
□ Memory usage reasonable (<50MB)
```

---

### 8. JNI BRIDGE TESTING (10 Tests)

#### Native Method Calls
```
□ Java to C++ method calls work
□ C++ to Java callbacks work
□ Data passed correctly (primitives)
□ Objects passed correctly (strings/arrays)
□ Memory management correct
□ No memory leaks
□ Exception handling works
□ Thread safety maintained
□ Performance acceptable (<1ms per call)
```

#### Error Handling
```
□ Null pointer exceptions handled
□ Invalid parameters rejected
□ Native crashes caught
□ Graceful error recovery
```

---

### 9. END-TO-END GAME FLOW TESTING (10 Tests)

#### Complete Game Scenario
```
□ Start new game successfully
□ Cards dealt automatically
□ Player takes turn (draw, place, flip)
□ AI opponent takes turn
□ Score calculated correctly
□ Round ends and advances
□ Match completes correctly
□ XP awarded correctly
□ Level up triggered
□ New skills unlocked
□ Game saves correctly
□ Game loads correctly
```

#### Multi-Player Scenario
```
□ Two-player game works
□ Turn alternation correct
□ Shared state synchronized
□ Network latency handled
□ Disconnection recovery
```

---

### 10. PERFORMANCE & MEMORY TESTING (5 Tests)

#### Performance Benchmarks
```
□ Game loop maintains 60 FPS
□ Command processing <10ms
□ Render frame <16ms
□ Audio processing <5ms
□ Memory usage <200MB
□ Battery usage reasonable
```

#### Stress Testing
```
□ 1000+ turns without crashes
□ Maximum skill points handled
□ Maximum XP levels handled
□ Memory fragmentation acceptable
```

---

## 🚨 CRITICAL PATH TESTS

### Must Pass for Release:
1. **Game completes without crashes**
2. **Cards render and animations work**
3. **Sounds play correctly**
4. **Skills affect gameplay**
5. **Save/load works**
6. **Performance acceptable**

### Performance Requirements:
- **Frame Rate**: Minimum 30 FPS, Target 60 FPS
- **Memory**: Maximum 200MB usage
- **Battery**: 2+ hours continuous play
- **Load Time**: <5 seconds initial load

---

## 📊 TEST RESULTS TRACKING

### Test Execution Log:
```
Date: ____________
Tester: __________
Build Version: _______

Component | Pass | Fail | Blocker | Notes
----------|------|------|---------|-------
GCMS      |      |      |         |
Game Engine|      |      |         |
Renderer  |      |      |         |
Audio     |      |      |         |
Skills    |      |      |         |
Trophies  |      |      |         |
Leveling  |      |      |         |
JNI       |      |      |         |
End-to-End|      |      |         |
Performance|      |      |         |

Total: __/145 tests passing
Status: [ ] APPROVED [ ] NEEDS WORK [ ] BLOCKED
```

---

## 🎯 RELEASE READINESS CHECKLIST

### Pre-Release Requirements:
```
□ All 145 tests pass
□ No critical bugs
□ Performance meets requirements
□ Memory usage acceptable
□ Audio/Visual complete
□ Documentation updated
□ User guide ready
□ Store assets prepared
```

### Sign-off:
```
QA Engineer: _______________ Date: _________
Lead Developer: _____________ Date: _________
Product Owner: ______________ Date: _________
Release Approved: [ ] YES [ ] NO
```

---

## 🔧 AUTOMATION NOTES

### Automated Tests:
- **Unit Tests**: 145 tests run automatically
- **Integration Tests**: 25 tests run on build
- **Performance Tests**: 10 tests run nightly
- **Memory Tests**: 5 tests run on commit

### Manual Tests:
- **Visual QA**: Check animations and effects
- **Audio QA**: Verify sound quality and timing
- **Usability QA**: Test user experience flow
- **Device QA**: Test on multiple devices

---

## 📝 TEST EXECUTION INSTRUCTIONS

### Running Tests:
```bash
# Run all unit tests
./gradlew test

# Run integration tests  
./gradlew connectedAndroidTest

# Run performance tests
./gradlew performanceTest

# Generate test report
./gradlew testReport
```

### Test Environment:
- **Device**: Test on 3+ different devices
- **OS**: Test on 2+ Android versions
- **Memory**: Test with limited memory
- **Network**: Test offline/online scenarios

---

## 🚨 ISSUE REPORTING

### Bug Report Template:
```
Title: [COMPONENT] Brief description
Steps to Reproduce:
1. Step one
2. Step two
3. Step three

Expected Result: What should happen
Actual Result: What actually happens
Device: Device model and OS
Severity: [Critical/High/Medium/Low]
Attachments: Screenshots, logs, videos
```

---

**This checklist should be run before every release to ensure quality!**

**Last Updated**: 2024  
**Next Review**: After each major feature update