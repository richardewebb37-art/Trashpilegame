# Unlimited Leveling System - Quick Reference

## Core Formulas

### Level from XP
```
Level = floor(log(XP + 1) / log(1.5)) + 1
```

### XP Earned
```
XP = (SP + AP) × (1 + Level × 0.05) + (Matches × 10) + (Rounds × 50)
```

### XP for Target Level
```
XP = Base × (1.5 ^ (Level - 1))
```

## Quick Examples

### Level 1 Player Wins Match
- SP: 10, AP: 10
- Matches: 1, Rounds: 1
- **XP = 81** → **Level 6**

### Level 10 Player Wins Match
- SP: 100, AP: 100
- Matches: 50, Rounds: 5
- **XP = 650** → **Progress toward Level 11**

### Level 50 Player Wins Match
- SP: 500, AP: 500
- Matches: 500, Rounds: 50
- **XP = 11,000** → **Significant progress**

## Level Thresholds (Approximate)

| Level | XP Needed |
|-------|-----------|
| 1     | 0         |
| 5     | 3         |
| 10    | 19        |
| 20    | 150       |
| 30    | 500       |
| 50    | 3,000     |
| 100   | 50,000    |

## Key Features

✅ **No Level Cap** - Unlimited progression
✅ **Match Bonus** - +10 XP per match
✅ **Round Bonus** - +50 XP per round
✅ **Level Scaling** - Higher levels earn more XP
✅ **Logarithmic Growth** - Smooth difficulty curve

## Configuration

```kotlin
LevelConfig(
    baseXP = 0,              // Starting XP
    xpMultiplier = 1.5,      // Growth rate (1.2-2.0)
    matchBonus = 10,         // XP per match
    roundBonus = 50          // XP per round
)
```

## Tuning Guide

**Faster Leveling:**
- Increase `xpMultiplier` to 2.0
- Increase `matchBonus` to 20
- Increase `roundBonus` to 100

**Slower Leveling:**
- Decrease `xpMultiplier` to 1.2
- Decrease `matchBonus` to 5
- Decrease `roundBonus` to 25

## Testing

```kotlin
// Calculate level from XP
val level = LevelSystem.calculateLevel(totalXP)

// Calculate XP from match
val xp = LevelSystem.calculateXP(
    spEarned, apEarned, 
    currentLevel, 
    totalMatches, 
    totalRounds
)

// Get XP needed for next level
val xpNeeded = LevelSystem.getXPToNextLevel(currentXP, currentLevel)
```

## Files Updated

1. `SkillAbilitySystem.kt` - LevelSystem object
2. `SkillAbilityLogic.kt` - processMatchCompletion()
3. `SkillAbilitySystemTest.kt` - 13 new tests

## Summary

Players can now **level up indefinitely** by:
- Playing matches (+10 XP each)
- Completing rounds (+50 XP each)
- Earning points (SP/AP scaled by level)

The system balances progression so higher levels earn more XP but require proportionally more XP to advance, maintaining challenge while rewarding long-term play.