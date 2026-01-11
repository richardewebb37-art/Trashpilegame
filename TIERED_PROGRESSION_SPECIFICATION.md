# Tiered Progression System - Full Specification

## Overview

Complete tiered progression system for levels 1-200 with:
- 6 distinct tiers with unique design philosophies
- Dynamic XP requirements per level
- Randomized skill/ability generation
- Tier-dependent point costs
- Soft caps controlled by content releases

## Tier Definitions

### Tier 1: Life (Levels 1-5)
**Design Philosophy:** Introduction to the game, minimal commitment
- **Skills:** 4
- **Abilities per Skill:** 8-10 (randomized)
- **Points per Ability:** 10-20 (randomized)
- **Total Abilities:** 32-40
- **XP Range per Level:** 50-200
- **Cumulative XP:** 0-675

**Sample Skills:**
1. Basic Card Manipulation
2. Early Game Strategy
3. Card Vision
4. Defensive Play

**Unlocks:**
- Tutorial completions
- Basic card operations
- Introduction to mechanics
- Low-risk experimentation

### Tier 2: Beginner (Levels 6-20)
**Design Philosophy:** Foundation building, unlocking XP progression
- **Skills:** 6
- **Abilities per Skill:** 10-12 (randomized)
- **Points per Ability:** 15-30 (randomized)
- **Total Abilities:** 60-72
- **XP Range per Level:** 200-500
- **Cumulative XP:** 1,045-21,275

**Sample Skills:**
1. Dice Mastery
2. Hand Optimization
3. Risk Assessment
4. Card Counting Basics
5. Timing Strategies
6. Resource Management

**Unlocks:**
- First meaningful abilities
- XP-driven progression starts
- Introduction to combos
- Moderate point costs

### Tier 3: Novice (Levels 21-50)
**Design Philosophy:** Intermediate play, skill combinations
- **Skills:** 8
- **Abilities per Skill:** 12-15 (randomized)
- **Points per Ability:** 20-40 (randomized)
- **Total Abilities:** 96-120
- **XP Range per Level:** 500-1,000
- **Cumulative XP:** 24,195-258,225

**Sample Skills:**
1. Advanced Dice Control
2. Pattern Recognition
3. Psychological Tactics
4. Multi-Turn Planning
5. Adaptive Strategies
6. Hand Analysis
7. Probability Mastery
8. Risk Mitigation

**Unlocks:**
- Skill synergy required
- More complex abilities
- Higher point costs
- Strategic depth

### Tier 4: Hard (Levels 51-80)
**Design Philosophy:** Advanced gameplay, powerful abilities
- **Skills:** 10
- **Abilities per Skill:** 15-18 (randomized)
- **Points per Ability:** 30-60 (randomized)
- **Total Abilities:** 150-180
- **XP Range per Level:** 1,000-2,000
- **Cumulative XP:** 272,995-981,175

**Sample Skills:**
1. Expert Timing
2. Card Prediction
3. Bluff Mastery
4. Counter Strategies
5. Multi-Combo Systems
6. Advanced Probability
7. Pattern Manipulation
8. Risk Engineering
9. Strategic Adaptation
10. Ultimate Optimization

**Unlocks:**
- Powerful game-changing abilities
- Complex skill trees
- High point investments
- Expert-level gameplay

### Tier 5: Expert (Levels 81-140)
**Design Philosophy:** Mastery territory, advanced combinations
- **Skills:** 12
- **Abilities per Skill:** 18-22 (randomized)
- **Points per Ability:** 40-80 (randomized)
- **Total Abilities:** 216-264
- **XP Range per Level:** 2,000-3,500
- **Cumulative XP:** 1,016,795-4,965,075

**Sample Skills:**
1. Perfect Predictions
2. Time Manipulation (gameplay)
3. Card Telepathy
4. Quantum Strategies
5. Dimensional Tactics
6. Infinite Combos
7. Probability Bending
8. Reality Warping (mechanics)
9. Meta-Strategy
10. Hyper-Optimization
11. Tactical Singularity
12. Ultimate Mastery

**Unlocks:**
- Breakthrough abilities
- Extreme power levels
- Massive point requirements
- Near-perfect play

### Tier 6: Master (Levels 141-200)
**Design Philosophy:** God-tier gameplay, ultimate abilities
- **Skills:** 15
- **Abilities per Skill:** 22-28 (randomized)
- **Points per Ability:** 50-100 (randomized)
- **Total Abilities:** 330-420
- **XP Range per Level:** 3,500-5,000
- **Cumulative XP:** 5,069,395-14,132,975

**Sample Skills:**
1. Transcendent Vision
2. Omni-Presence (card awareness)
3. Temporal Mastery
4. Reality Reconstruction
5. Probability Control
6. Strategic Omniscience
7. Dimensional Manipulation
8. Quantum Entanglement
9. Causality Manipulation
10. Infinite Potential
11. Absolute Control
12. Transcendence
13. God-Mode (balanced)
14. Ultimate Singularity
15. Legendary Status

**Unlocks:**
- Ultimate abilities
- Reality-bending powers
- Maximum point costs
- Legendary achievements

## Progression Flow

### Level Progression
1. **Start at Level 1** (Tier: Life)
2. **Earn points** from matches and gameplay
3. **Purchase abilities** using points
4. **Gain XP** from each ability purchase
5. **Level up** when XP reaches threshold
6. **Unlock new tier** when entering next level range
7. **Access more powerful abilities** in higher tiers
8. **Repeat** through all 200 levels

### XP Flow
```
Match → Points → Buy Ability → Gain XP → Level Up → Unlock Tier → More Abilities
```

### Point Flow
```
Gameplay → Points → Spend on Abilities → Unlock → Gain XP → Repeat
```

## Randomization System

### XP per Level
```kotlin
fun calculateXPForLevel(level: Int): Int {
    val baseXP = 65.0 * Math.pow(level.toDouble(), 1.1)
    val variance = 1.0 + (Math.random() * 0.1 - 0.05) // ±5%
    return (baseXP * variance).toInt()
}
```

### Points per Ability
```kotlin
fun calculatePointsForAbility(tier: Tier): Int {
    val basePoints = when (tier) {
        Tier.LIFE -> 15.0
        Tier.BEGINNER -> 22.5
        Tier.NOVICE -> 30.0
        Tier.HARD -> 45.0
        Tier.EXPERT -> 60.0
        Tier.MASTER -> 75.0
    }
    val variance = 1.0 + (Math.random() * 0.2 - 0.1) // ±10%
    return (basePoints * variance).toInt()
}
```

### Abilities per Skill
```kotlin
fun calculateAbilitiesPerSkill(tier: Tier): Int {
    val baseAbilities = when (tier) {
        Tier.LIFE -> 9.0
        Tier.BEGINNER -> 11.0
        Tier.NOVICE -> 13.5
        Tier.HARD -> 16.5
        Tier.EXPERT -> 20.0
        Tier.MASTER -> 25.0
    }
    val variance = (Math.random() * 4).toInt() // 0-3 extra
    return baseAbilities.toInt() + variance
}
```

## Skill Tree Structure

### Interdependencies
1. **Tier 1 Skills**: No prerequisites
2. **Tier 2 Skills**: Require 1 Tier 1 skill
3. **Tier 3 Skills**: Require 2 Tier 2 skills (different)
4. **Tier 4 Skills**: Require 1 Tier 3 skill + 2 Tier 2 skills
5. **Tier 5 Skills**: Require 2 Tier 4 skills + 3 Tier 3 skills
6. **Tier 6 Skills**: Require 3 Tier 5 skills + 5 previous tier skills

### Ability Unlock Rules
1. Must unlock parent skill first
2. Abilities unlock in sequence within skill
3. Some abilities require other abilities (cross-skill)
4. Ultimate abilities require full skill completion

### Example Skill Tree Path

**Tier 1 (Life):**
```
Basic Card Manipulation (Parent)
├── Peek Ability
├── Draw Ability
├── Swap Ability
└── Discard Ability
```

**Tier 2 (Beginner):**
```
Dice Mastery (Requires: Basic Card Manipulation)
├── Loaded Dice Ability
├── Reroll Ability
├── Perfect Roll Ability
└── Fortune Ability
```

**Tier 3 (Novice):**
```
Advanced Dice Control (Requires: Dice Mastery + Hand Optimization)
├── Multi-Reroll Ability
├── Dice Prediction Ability
├── Temporal Dice Ability
└── Quantum Dice Ability
```

## Content Expansion System

### Soft Cap Mechanism
- **No hard level cap** (can go beyond 200)
- **Limited by available abilities/skills**
- **New content releases enable higher levels**
- **Natural progression ceiling**

### Example Content Update
1. **Release:** New Tier 7 (Levels 201-250)
2. **Add:** 18 new skills
3. **Add:** 400-500 new abilities
4. **Result:** Players can now level beyond 200
5. **XP Potential:** Increases by 50,000+ XP

### Gradual Release Strategy
```
Phase 1: Tier 1-2 (Launch)
Phase 2: Tier 3 (Month 1)
Phase 3: Tier 4 (Month 2)
Phase 4: Tier 5 (Month 3)
Phase 5: Tier 6 (Month 4)
Phase 6+: Beyond (Ongoing)
```

## Balance Considerations

### Point Economy
- **Points earned per match:** 10-100 (varies by level)
- **Points per ability:** 10-100 (varies by tier)
- **Average abilities per level:** 2-5
- **Time to unlock all:** 200-500 hours (estimated)

### XP Economy
- **XP per ability:** 15-100 (varies by tier)
- **XP per level:** 65-206,980 (exponential)
- **Average abilities to level up:** 5-20
- **Total XP to Level 200:** 14,132,975

### Play Balance
- **Early game:** Fast progression, low cost
- **Mid game:** Moderate progression, balanced cost
- **Late game:** Slow progression, high cost
- **End game:** Mastery achievement, ultimate rewards

## Data Structure Recommendations

### Tier Configuration (JSON)
```json
{
  "tiers": [
    {
      "name": "Life",
      "levelRange": [1, 5],
      "skills": 4,
      "abilitiesPerSkill": {"min": 8, "max": 10},
      "pointsPerAbility": {"min": 10, "max": 20},
      "xpMultiplier": 1.0
    },
    {
      "name": "Beginner",
      "levelRange": [6, 20],
      "skills": 6,
      "abilitiesPerSkill": {"min": 10, "max": 12},
      "pointsPerAbility": {"min": 15, "max": 30},
      "xpMultiplier": 1.2
    }
  ]
}
```

### Skill Configuration (JSON)
```json
{
  "skills": [
    {
      "id": "basic_card_manipulation",
      "name": "Basic Card Manipulation",
      "tier": "Life",
      "prerequisites": [],
      "abilities": [
        {
          "id": "peek",
          "name": "Peek",
          "description": "Look at 1 face-down card",
          "cost": 12,
          "xpReward": 15,
          "prerequisites": []
        }
      ]
    }
  ]
}
```

## Implementation Priority

### Phase 1: Core System
1. ✅ Tier data structure
2. ✅ XP calculation formulas
3. ✅ Randomization functions
4. ✅ Skill/ability generation

### Phase 2: Content
1. ✅ Tier 1-2 abilities/skills
2. ✅ Tier 3-4 abilities/skills
3. ✅ Tier 5-6 abilities/skills
4. ✅ Skill tree interdependencies

### Phase 3: Integration
1. ✅ UI for tier progression
2. ✅ XP display and tracking
3. ✅ Ability purchase system
4. ✅ Level-up rewards

### Phase 4: Expansion
1. ⏳ Content release system
2. ⏳ Tier 7+ planning
3. ⏳ Balance tuning
4. ⏳ Player feedback integration

## Summary

This tiered progression system provides:
- ✅ **6 distinct tiers** with unique gameplay philosophies
- ✅ **200 levels** of progression
- ✅ **55 skills** across all tiers
- ✅ **884-1,096 abilities** total
- ✅ **Dynamic XP system** with soft caps
- ✅ **Randomized content** for variety
- ✅ **Expandable architecture** for future content
- ✅ **Balanced economy** for long-term engagement

The system is designed to be:
- **Data-driven** - Easy to modify and balance
- **Scalable** - Can extend beyond Level 200
- **Flexible** - Randomization keeps it fresh
- **Fair** - Progression feels rewarding
- **Deep** - Strategic choices matter