# ✅ CORRECTED Scoring & Progression System - Implementation Complete

## 🎯 System Overview

The Trash Piles game now has **TWO SEPARATE SYSTEMS** working together:

### System 1: Game Scoring (Determines Match Winner)
- **Round Score:** Face-UP cards only (slot numbers + dice bonus)
- **Match Score:** Sum of 10 round scores
- **Winner:** Highest total match score

### System 2: Skill & Ability Points (Long-term Progression)
- **SP (Skill Points):** Winner gets base SP (never penalized)
- **AP (Ability Points):** Winner gets base AP minus face-DOWN card penalties
- **Purpose:** Unlock skills and abilities in progression trees

---

## 📊 System 1: Game Scoring (CORRECT VERSION)

### Round Scoring Rules

**✅ ONLY Face-UP Cards Count for Points**

```
Base Score = Sum of face-up card slot numbers

Example:
Slot 1 face-up → 1 point
Slot 5 face-up → 5 points  
Slot 7 face-up → 7 points
Base = 1 + 5 + 7 = 13 points
```

**✅ Joker Face-UP Bonus**

```
Joker face-up = +20 BONUS POINTS

Example:
Slots 1, 5, Joker(7) face-up
Base = 1 + 5 + 7 + 20 = 33 points
```

**✅ Dice Bonus System**

```
Everyone rolls dice (1-6)

LOSER Multiplier = # of face-up cards
WINNER Multiplier = Last card slot flipped

Bonus = Multiplier × Dice Roll
Final Score = Base + Bonus
```

### Complete Round Formula

```kotlin
// Calculate base score
baseScore = 0
for (card in faceUpCards) {
    baseScore += card.slotNumber
    if (card.isJoker) baseScore += 20
}

// Determine multiplier
multiplier = if (isWinner) {
    lastCardFlippedSlot  // e.g., 10
} else {
    faceUpCards.size     // e.g., 3
}

// Roll dice and calculate bonus
diceRoll = random(1, 6)
bonus = multiplier × diceRoll
finalScore = baseScore + bonus
```

### Example Round Scores

**Winner (all 10 cards face-up, last card = slot 10):**
```
Base: 1+2+3+4+5+6+7+8+9+10 = 55
Multiplier: 10 (last card slot)
Dice: 6
Bonus: 10 × 6 = 60
Final: 55 + 60 = 115 points
```

**Loser (3 cards face-up):**
```
Base: 1 + 5 + 7 = 13
Multiplier: 3 (face-up count)
Dice: 4
Bonus: 3 × 4 = 12
Final: 13 + 12 = 25 points
```

### Match Scoring

```
Match Score = Sum of all 10 round scores

Round 1: 95 points
Round 2: 87 points
...
Round 10: 72 points
─────────────────
Match Total: 687 points

HIGHEST SCORE WINS THE MATCH
```

---

## 🎖️ System 2: Skill & Ability Points

### SP/AP Award Rules

**✅ Only the WINNER earns SP/AP**

**✅ SP (Skill Points):**
```
SP Earned = Base SP from match table
NEVER penalized
Always guaranteed
```

**✅ AP (Ability Points):**
```
AP Earned = Base AP - Face-DOWN Card Penalties
Can be reduced to 0
Encourages strategic play
```

### Match Rewards Table

| Match # | SP (Winner) | Base AP (Winner) |
|---------|-------------|------------------|
| 1       | 1           | 0                |
| 2       | 1           | 1                |
| 3       | 2           | 2                |
| 4       | 2           | 2                |
| 5       | 3           | 3                |
| 6       | 3           | 3                |
| 7       | 4           | 4                |
| 8       | 4           | 4                |
| 9       | 5           | 5                |
| 10      | 5           | 5                |
| **Total** | **30 SP** | **29 AP** | *Max possible* |

### AP Penalty System

**❌ Face-DOWN Cards Cause AP Penalties**

```
King face-down:   -3 AP (per occurrence)
Queen face-down:  -2 AP (per occurrence)
Jack face-down:   -1 AP (per occurrence)
Joker face-down:  -20 AP (per occurrence)
```

**Key Points:**
- Penalties apply ONLY to face-DOWN cards
- Penalties accumulate across all 10 rounds
- Face-UP cards don't cause penalties
- Joker face-DOWN is devastating (-20 AP!)

### AP Calculation Formula

```kotlin
// After match ends
totalAPPenalty = 0

for (round in allRounds) {
    for (card in round.faceDownCards) {
        when (card.rank) {
            "King" -> totalAPPenalty -= 3
            "Queen" -> totalAPPenalty -= 2
            "Jack" -> totalAPPenalty -= 1
            "Joker" -> totalAPPenalty -= 20
        }
    }
}

baseAP = MATCH_REWARDS[matchNumber].AP
apEarned = max(0, baseAP + totalAPPenalty)
```

---

## 🎮 Complete Examples

### Example 1: Perfect Match (No Penalties)

**Match 5 Winner:**

```
All 10 rounds: Only number cards face-down
No Kings, Queens, Jacks, or Jokers face-down

SP/AP Award:
  Base SP: 3
  SP Earned: 3 ✅
  
  Base AP: 3
  Penalties: 0
  AP Earned: 3 ✅

Final: +3 SP, +3 AP
```

### Example 2: Strategic Play (Minimal Penalties)

**Match 7 Winner:**

```
Round 1: King face-down (-3 AP)
Round 2-10: All face-up (no penalties)

Total Penalties: -3 AP

SP/AP Award:
  Base SP: 4
  SP Earned: 4 ✅
  
  Base AP: 4
  Penalties: -3
  AP Earned: 4 + (-3) = 1 ✅

Final: +4 SP, +1 AP
```

### Example 3: Joker Disaster

**Match 3 Winner:**

```
Round 1: Perfect (no penalties)
Round 2: JOKER FACE-DOWN (-20 AP) ❌
Round 3-10: Some face cards face-down (-5 AP)

Total Penalties: -25 AP

SP/AP Award:
  Base SP: 2
  SP Earned: 2 ✅
  
  Base AP: 2
  Penalties: -25
  AP Earned: 2 + (-25) = -23 → 0 ❌

Final: +2 SP, +0 AP (lost all AP!)
```

### Example 4: High Penalty Match

**Match 9 Winner:**

```
Multiple rounds with face cards face-down:
- 2 Kings: -6 AP
- 3 Queens: -6 AP
- 2 Jacks: -2 AP
Total Penalties: -14 AP

SP/AP Award:
  Base SP: 5
  SP Earned: 5 ✅
  
  Base AP: 5
  Penalties: -14
  AP Earned: 5 + (-14) = -9 → 0 ❌

Final: +5 SP, +0 AP
```

---

## 🔄 Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    MATCH STARTS                          │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│              PLAY 10 ROUNDS                              │
│                                                          │
│  Each Round:                                             │
│  1. Players flip cards                                   │
│  2. Round ends when someone completes hand               │
│  3. Calculate round scores (face-UP cards + dice)        │
│  4. Track face-DOWN cards for AP penalties               │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│              MATCH ENDS                                  │
│                                                          │
│  1. Sum all 10 round scores                              │
│  2. Highest score = WINNER                               │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│         AWARD SP/AP TO WINNER                            │
│                                                          │
│  SP = Base SP (guaranteed)                               │
│  AP = Base AP - Face-DOWN penalties                      │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│      PLAYER USES SP/AP TO UNLOCK SKILLS/ABILITIES        │
└─────────────────────────────────────────────────────────┘
```

---

## 🎯 Strategic Implications

### For Round Scoring (System 1)

**✅ Winners:**
- Try to make slot 10 your last card (max multiplier)
- Flip all cards face-up for maximum base score

**✅ Losers:**
- Flip as many cards as possible (higher multiplier)
- Every face-up card increases your score

**✅ Joker Strategy:**
- Get Joker face-up ASAP (+20 bonus to round score)

### For SP/AP Earning (System 2)

**✅ Minimize AP Penalties:**
- Flip Kings, Queens, Jacks face-up quickly
- NEVER leave Joker face-down (devastating -20 AP)
- Focus on completing rounds to avoid penalties

**✅ SP is Guaranteed:**
- Win the match = get full SP
- No way to lose SP

**✅ AP Requires Skill:**
- Good players minimize face-down face cards
- Strategic play = more AP earned
- Poor play = lose all AP

---

## 💻 Implementation Status

### ✅ Completed

1. **Core Scoring Logic**
   - `calculateRoundScore()` - Face-UP cards + dice bonus
   - `calculateMatchAPPenalties()` - Face-DOWN card penalties
   - `processMatchCompletion()` - SP/AP award system

2. **Data Structures**
   - `RoundScoreResult` - Complete round scoring data
   - `CardPenalty` - Individual card scores/penalties
   - `MatchResult` - Match completion data

3. **Testing**
   - 36+ comprehensive unit tests
   - Round scoring tests
   - AP penalty tests
   - Match completion tests
   - Integration tests

4. **Documentation**
   - Complete system documentation
   - Usage examples
   - Strategic guides

### ⏳ Pending (UI Only)

- Round score display
- Dice roll animation
- Match summary screen
- SP/AP award notification
- Skill/Ability tree UI

---

## 📝 Key Differences from Original

### What Changed

**❌ OLD (Incorrect):**
- Face-DOWN cards counted for points
- Penalties based on slot numbers
- Complex triple combo system
- Confusing scoring rules

**✅ NEW (Correct):**
- Face-UP cards count for points
- Simple, clear scoring
- Dice adds excitement
- Separate AP penalty system

### Why This is Better

1. **Clearer Rules:** Face-up = points, face-down = penalties
2. **More Strategic:** Players control their AP through play
3. **Exciting:** Dice rolls add randomness and drama
4. **Balanced:** SP guaranteed, AP requires skill
5. **Intuitive:** Higher score = better (not confusing negatives)

---

## 🎉 Summary

### System 1: Game Scoring
- ✅ Face-UP cards = points
- ✅ Dice bonus = excitement
- ✅ Highest score wins
- ✅ Simple and clear

### System 2: SP/AP Progression
- ✅ Winner gets rewards
- ✅ SP always earned
- ✅ AP penalized by face-DOWN cards
- ✅ Encourages strategic play

### Result
- ✅ Two systems work together perfectly
- ✅ Clear separation of concerns
- ✅ Strategic depth maintained
- ✅ Easy to understand and implement

---

**Implementation Status: ✅ Core Logic 100% Complete**  
**Next Step: UI Implementation**  
**Ready for: Testing and Integration**