# 🎮 TrashPiles UI Layout Design

## 📐 Target Design Style
**Inspiration:** Fairway Solitaire, Grand Harvest Solitaire
**Vibe:** Clean, colorful, engaging with playful elements

---

## 🎯 Core Layout Requirements

### **Fixed Elements:**
- ✅ **Player Orientation:** Same as reference (4 corners)
- ✅ **Deck & Trash:** Center position, same as reference
- ✅ **Player Slots:** Arranged around edges, same as reference

### **New Feature:**
- 😊 **Smiley Face:** Between Deck and Trash piles
  - Changes based on game state
  - Shows player color
  - Reacts to player actions

---

## 🎨 Layout Structure

### **Background:**
- Green felt table texture (solitaire-style)
- Subtle lighting effects
- Professional casino/parlor feel

### **Center Area:**
```
[Deck]  😊  [Trash]
```
- **Deck:** Face-down card stack
- **Smiley Face:** Dynamic emoji-style face
- **Trash:** Discard pile showing top card

### **Player Positions (4 Players):**

#### **Top-Left (Player 1):**
```
┌─────────────────────────┐
│  Ability Button        │
│  [1][2][3][4][5]       │
│                        │
│  [Skill Button]        │
│  [6][7][8][9][10]      │
│                        │
│  Player Slots (10)     │
│  [1][2][3][4][5]       │
│  [6][7][8][9][10]      │
│                        │
│  P1                    │
└─────────────────────────┘
```

#### **Top-Right (Player 4):**
```
┌─────────────────────────┐
│  Ability Button        │
│  [1][2][3][4][5]       │
│                        │
│  [Skill Button]        │
│  [6][7][8][9][10]      │
│                        │
│  Player Slots (10)     │
│  [1][2][3][4][5]       │
│  [6][7][8][9][10]      │
│                        │
│  P4                    │
└─────────────────────────┘
```

#### **Bottom-Left (Player 2/5):**
```
┌─────────────────────────┐
│  P5                    │
│                        │
│  Player Slots (10)     │
│  [1][2][3][4][5]       │
│  [6][7][8][9][10]      │
│                        │
│  [Skill Button]        │
│  [6][7][8][9][10]      │
│                        │
│  Ability Button        │
│  [1][2][3][4][5]       │
└─────────────────────────┘
```

#### **Bottom-Right (Player 3/6):**
```
┌─────────────────────────┐
│  P6                    │
│                        │
│  Player Slots (10)     │
│  [1][2][3][4][5]       │
│  [6][7][8][9][10]      │
│                        │
│  [Skill Button]        │
│  [6][7][8][9][10]      │
│                        │
│  Ability Button        │
│  [1][2][3][4][5]       │
└─────────────────────────┘
```

---

## 😊 Smiley Face Feature

### **Position:**
- Centered between Deck and Trash piles
- Approximately 80x80 pixels
- Prominent but not intrusive

### **Behaviors:**

#### **1. Color Changes:**
- **Matches current player's color**
- Player 1: Red
- Player 2: Blue
- Player 3: Green
- Player 4: Yellow
- Transition: Smooth color animation (200ms)

#### **2. Facial Expressions:**

**Normal Play:**
```
😊 Happy face
- Eyes: Open circles
- Mouth: Slight smile
- Color: Current player color
```

**Player Wins Round:**
```
😄 Big smile
- Eyes: Happy squint
- Mouth: Big grin
- Animation: Bounce effect
- Confetti particles
```

**Player Loses Round:**
```
😢 Frowning face
- Eyes: Sad droopy
- Mouth: Frown
- Color: Gray/dimmed
- Animation: Shake effect
```

**Wrong Card Placement:**
```
😕 Confused/Concerned
- Eyes: One eyebrow raised
- Mouth: Concerned line
- Animation: Quick head shake
- Color: Flashes red briefly
```

**Good Card Placement:**
```
😃 Encouraging
- Eyes: Bright
- Mouth: Approval smile
- Animation: Subtle nod
- Sparkle effect
```

**Player Turn Change:**
```
🤔 Thinking
- Eyes: Looking around
- Mouth: Neutral
- Animation: Subtle breathing
- Color: Fading to next player
```

**Game Over - Victory:**
```
🎉 Celebrating
- Eyes: Stars/sparkles
- Mouth: Big grin
- Animation: Bouncing + confetti
- Color: Rainbow cycle
```

**Game Over - Defeat:**
```
😞 Disappointed
- Eyes: Closed
- Mouth: Sad
- Animation: Slow nod down
- Color: Desaturated
```

### **Animation System:**

#### **Transitions:**
- Color change: 200ms linear interpolation
- Expression change: 150ms easing
- Size changes: 100ms with bounce effect

#### **Special Effects:**
- **Confetti:** When winning round/game
- **Sparkles:** When good move made
- **Shake:** When wrong move attempted
- **Fade:** Between player turns

### **Implementation Details:**

**States:**
```kotlin
enum class SmileyState {
    NORMAL,           // Regular gameplay
    HAPPY,            // Good move
    CONFUSED,         // Wrong move
    THINKING,         // Player's turn
    CELEBRATING,      // Won round/game
    DISAPPOINTED,     // Lost round/game
    TRANSITIONING     // Between players
}

enum class SmileyExpression {
    NEUTRAL,          // 😐
    HAPPY,            // 😊
    BIG_SMILE,        // 😄
    FROWN,            // 😢
    CONFUSED,         // 😕
    THINKING,         // 🤔
    CELEBRATING,      // 🎉
    DISAPPOINTED      // 😞
}
```

**Triggers:**
```kotlin
// Player turn starts
onTurnStart(playerIndex) {
    smileyState = THINKING
    smileyColor = playerColors[playerIndex]
}

// Good card placed
onValidMove() {
    smileyState = HAPPY
    showSparkles()
}

// Invalid move
onInvalidMove() {
    smileyState = CONFUSED
    shakeFace()
}

// Player wins round
onRoundWin(playerIndex) {
    smileyState = CELEBRATING
    showConfetti()
}

// Player loses round
onRoundLoss(playerIndex) {
    smileyState = DISAPPOINTED
    dimColor()
}
```

---

## 🎨 Visual Style Reference

### **Color Palette:**
```css
/* Background */
Table Surface: #2E7D32 (Green felt)
Card Slots: #1B5E20 (Darker green)
Card Backs: #F5F5DC (Beige/tan)

/* Player Colors */
Player 1: #E53935 (Red)
Player 2: #1E88E5 (Blue)
Player 3: #43A047 (Green)
Player 4: #FDD835 (Yellow)

/* UI Elements */
Buttons: #FF6F00 (Orange)
Text: #FFFFFF (White)
Card Borders: #FFD700 (Gold)
```

### **Card Design:**
- **Size:** 60x90 pixels (standard playing card ratio)
- **Face-up:** White background with suit/rank
- **Face-down:** Patterned design (like reference)
- **Border:** Gold with slight shadow
- **Font:** Clear, readable sans-serif

### **Button Design:**
- **Shape:** Rounded rectangles
- **Style:** Gradient fill with hover effects
- **Text:** White, centered
- **Feedback:** Press animation

---

## 📐 Screen Layout (Portrait Mode)

```
┌─────────────────────────────────┐
│                                 │
│  [P1]                   [P4]    │
│  Ability   ...   Ability        │
│  Skill     ...   Skill          │
│  [1][2]...[5]   [1][2]...[5]   │
│  [6][7]...[10]  [6][7]...[10]  │
│                                 │
│         ┌───────────┐           │
│         │  [DECK]   │           │
│         │           │           │
│         │    😊     │           │
│         │           │           │
│         │ [TRASH]   │           │
│         └───────────┘           │
│                                 │
│  [P5]                   [P6]    │
│  Ability   ...   Ability        │
│  Skill     ...   Skill          │
│  [1][2]...[5]   [1][2]...[5]   │
│  [6][7]...[10]  [6][7]...[10]  │
│                                 │
└─────────────────────────────────┘
```

---

## 🔄 Responsive Design

### **Screen Sizes:**
- **Small (phones):** Compact layout, smaller cards
- **Medium (tablets):** Reference layout
- **Large (desktop):** Spacious layout, larger cards

### **Orientation:**
- **Portrait:** Primary mode (as shown)
- **Landscape:** Stretched horizontally

---

## 🎮 Interaction Design

### **Player Feedback:**
1. **Valid Move:** Smiley happy, sparkle effect
2. **Invalid Move:** Smiley confused, shake effect
3. **Turn Change:** Smiley thinking, color transition
4. **Round Win:** Smiley celebrating, confetti
5. **Round Loss:** Smiley disappointed, fade

### **Visual Feedback:**
- **Card Highlight:** Selected card glows
- **Valid Slot:** Target slot highlights
- **Invalid Slot:** Red X indicator
- **Win Animation:** Cards bounce/scale

---

## 🚀 Implementation Priority

### **Phase 1: Core Layout**
1. ✅ Basic 4-player positioning
2. ✅ Deck/Trash placement
3. ✅ Player slot arrangement
4. ✅ Background and table design

### **Phase 2: Smiley Face**
1. ⏳ Basic smiley rendering
2. ⏳ Color changing system
3. ⏳ Expression animations
4. ⏳ Game state integration

### **Phase 3: Polish**
1. ⏳ Smooth transitions
2. ⏳ Particle effects (confetti, sparkles)
3. ⏳ Sound integration
4. ⏳ Performance optimization

---

## 📦 Assets Required

### **UI Elements:**
- Background texture (green felt)
- Card face images (52 cards)
- Card back pattern
- Button backgrounds
- Smiley face expressions (8 variations)

### **Animations:**
- Confetti particles
- Sparkle effects
- Shake effect
- Fade transitions

### **Audio:**
- Sound for valid move
- Sound for invalid move
- Sound for round win
- Sound for round loss
- Background music

---

## 🎯 Success Criteria

- ✅ Layout matches reference exactly
- ✅ Player positions fixed in corners
- ✅ Deck/Trash in center
- ✅ Smiley face between them
- ✅ Smooth color transitions
- ✅ Appropriate expressions
- ✅ Engaging animations
- ✅ Responsive to screen size
- ✅ Performance at 60 FPS

---

*Design captures the Fairway Solitaire/Grand Harvest Solitaire aesthetic with playful personality through the dynamic smiley face feature.*