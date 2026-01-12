# Skills & Abilities - Full Specification Table

## Overview

This document contains the complete specification of all skills and abilities for the Trash Piles game, organized by tier. Each entry includes:

- **Name**: Skill/Ability identifier
- **Description**: Effect description
- **Point Cost**: SP or AP cost to purchase
- **XP Reward**: XP granted when unlocked
- **Effect**: Detailed game mechanics
- **Trophy**: Associated trophy (if any)
- **Tier**: Progression tier
- **Prerequisites**: Required skills/abilities to unlock

---

## Tier 1: Life / Newbie (Levels 1-5)

### Skills

| ID | Name | Point Type | Cost | XP Reward | Description | Effect | Trophy | Prerequisites |
|----|------|------------|------|-----------|-------------|--------|--------|---------------|
| L1_S1 | Quick Learner | SP | 50 | 10 | Gain +5% bonus XP on first 3 actions | Multiplies XP gain by 1.05 for actions 1-3 of each game | Fast Learner | None |
| L1_S2 | Focused Mind | SP | 60 | 15 | Reduces point cost of first 2 abilities by 10% | Applies 0.9x multiplier to first 2 ability costs | Early Adopter | None |
| L1_S3 | Card Sense | SP | 70 | 20 | Start with knowledge of 1 random card in deck | Reveals 1 random card position at game start | Intuitive Player | None |

### Abilities

| ID | Name | Point Type | Cost | XP Reward | Description | Effect | Trophy | Prerequisites |
|----|------|------------|------|-----------|-------------|--------|--------|---------------|
| L1_A1 | Dash | AP | 40 | 15 | Move one space forward quickly | Avoids 1 minor hazard (first discard) | Swift Starter | None |
| L1_A2 | Guard | AP | 45 | 20 | Basic defense, reduces next incoming damage by 10% | Reduces opponent's next score gain by 10% | Defensive Mind | None |
| L1_A3 | Peek | AP | 35 | 10 | Look at the top 2 cards of deck | Reveals top 2 deck cards to player | Observer | None |

**Tier 1 Notes:**
- XP doesn't accumulate until first ability is purchased
- Challenges are basic: complete tutorial, earn first points
- All skills cost 50-70 SP
- All abilities cost 35-45 AP
- 3 skills, 3 abilities = 6 total

---

## Tier 2: Beginner (Levels 6-20)

### Skills

| ID | Name | Point Type | Cost | XP Reward | Description | Effect | Trophy | Prerequisites |
|----|------|------------|------|-----------|-------------|--------|--------|---------------|
| L2_S1 | Precision Strike | SP | 100 | 25 | Adds +5 damage to first attack of the turn | +5 to first card placement score calculation | Sharpshooter | L1_S1 |
| L2_S2 | Iron Will | SP | 110 | 30 | Reduces penalty for failed actions by 10% | Reduces score penalty for invalid moves by 10% | Resilient | L1_S2 |
| L2_S3 | Card Memory | SP | 120 | 35 | Remember last 3 cards in discard pile | Tracks last 3 discarded cards | Memory Master | L1_S3 |
| L2_S4 | Lucky Draw | SP | 90 | 20 | 10% chance to draw extra card | When drawing from deck, 10% chance to draw 2 cards | Fortune Favored | None |
| L2_S5 | Efficient Play | SP | 100 | 25 | Reduce point cost of abilities by 5% | All abilities cost 0.95x AP | Resourceful | None |

### Abilities

| ID | Name | Point Type | Cost | XP Reward | Description | Effect | Trophy | Prerequisites |
|----|------|------------|------|-----------|-------------|--------|--------|---------------|
| L2_A1 | First Aid | AP | 80 | 30 | Heal minor damage - restore 5 HP | Gain +5 points after winning a round | Healer | L1_A1 |
| L2_A2 | Quick Shot | AP | 70 | 25 | Basic ranged attack - Damage: 10, Stamina: 2 | Place card with +10 bonus score | Agile Attacker | L1_A2 |
| L2_A3 | Double Tap | AP | 90 | 35 | Place 2 cards in one turn | Allows placing 2 cards (costs 2 actions) | Dual Strike | L2_A2 |
| L2_A4 | Shield | AP | 85 | 30 | Temporarily protect a slot from being replaced | Protects 1 card slot for 1 turn | Guardian | L1_A3 |
| L2_A5 | Scan | AP | 60 | 20 | Look at next 3 cards in deck | Reveals next 3 deck cards | Scout | L1_A3 |

**Tier 2 Notes:**
- Players start seeing XP impact on level progression
- Some trophies unlocked for completing early challenges
- Skills cost 90-120 SP
- Abilities cost 60-90 AP
- 5 skills, 5 abilities = 10 total

---

## Tier 3: Novice (Levels 21-50)

### Skills

| ID | Name | Point Type | Cost | XP Reward | Description | Effect | Trophy | Prerequisites |
|----|------|------------|------|-----------|-------------|--------|--------|---------------|
| L3_S1 | Tactical Retreat | SP | 180 | 45 | Reduces damage taken when fleeing | -5 points penalty when discarding instead of placing | Tactical Genius | L2_S2 |
| L3_S2 | Resourceful | SP | 200 | 50 | +10% points gained from actions | Multiplies all point gains by 1.1 | Bountiful | L2_S5 |
| L3_S3 | Combo Starter | SP | 190 | 48 | +10% chance to start combo chain | When placing card, 10% chance for combo bonus | Chain Starter | L2_S1 |
| L3_S4 | Discard Mastery | SP | 170 | 42 | Gain +2 points when discarding | Discarding gives +2 bonus points | Wasteful | L2_S3 |
| L3_S5 | Wild Card Expert | SP | 210 | 52 | Wild cards (J, Q, K) worth +3 points | J, Q, K cards give +3 bonus when placed | Wild Master | None |
| L3_S6 | Early Bird | SP | 160 | 40 | +5% score for first 3 cards placed | First 3 placements get 1.05x multiplier | Early Riser | None |

### Abilities

| ID | Name | Point Type | Cost | XP Reward | Description | Effect | Trophy | Prerequisites |
|----|------|------------|------|-----------|-------------|--------|--------|---------------|
| L3_A1 | Fire Strike | AP | 150 | 45 | Small area-of-effect attack - Damage: 15, XP: 10 | Place card that affects adjacent slots | Flame Wielder | L2_A2 |
| L3_A2 | Shield Bash | AP | 140 | 42 | Stuns minor enemy for 1 turn | Opponent skips next turn | Stunner | L2_A4 |
| L3_A3 | Rapid Fire | AP | 160 | 48 | Attack 2 targets at once - Damage: 12 per target | Place 2 cards simultaneously (different slots) | Multi-Target | L3_A1 |
| L3_A4 | Heal Burst | AP | 130 | 40 | Heals allies for 10 HP in range | Gain +10 points after placing specific card type | Burst Healer | L2_A1 |
| L3_A5 | Swap | AP | 145 | 43 | Swap positions with opponent | Swap hand with opponent for 1 turn | Trickster | None |
| L3_A6 | Counter | AP | 155 | 46 | Counter opponent's next action | Negate opponent's next card placement | Counter-Attack | L3_A2 |

**Tier 3 Notes:**
- Unlock mid-tier trophies and side challenges
- Players must complete certain challenges to access higher-tier skills
- Skills cost 160-210 SP
- Abilities cost 130-160 AP
- 6 skills, 6 abilities = 12 total

---

## Tier 4: Hard (Levels 51-80)

### Skills

| ID | Name | Point Type | Cost | XP Reward | Description | Effect | Trophy | Prerequisites |
|----|------|------------|------|-----------|-------------|--------|--------|---------------|
| L4_S1 | Combo Mastery | SP | 300 | 75 | +10% damage when chaining 2+ attacks | Each consecutive placement adds +10% bonus | Chain Master | L3_S3 |
| L4_S2 | Tough Skin | SP | 320 | 80 | Reduces physical damage by 10% | -10% to opponent's score gains | Tank | L3_S1 |
| L4_S3 | Point Multiplier | SP | 290 | 72 | 15% chance to double points from action | When scoring, 15% chance for 2x points | Doubler | L3_S2 |
| L4_S4 | Card Prophet | SP | 310 | 77 | Predict next 5 cards in deck | Reveals next 5 deck cards | Seer | L3_S6 |
| L4_S5 | Perfect Timing | SP | 280 | 70 | +20% score for perfectly timed placements | +20% bonus for consecutive valid placements | Time Master | None |
| L4_S6 | Defensive Wall | SP | 330 | 82 | Reduce all incoming penalties by 15% | All opponent score penalties increased by 15% | Fortress | L4_S2 |
| L4_S7 | Critical Thinker | SP | 295 | 73 | 10% chance to avoid penalty completely | 10% chance invalid move has no penalty | Critical | L4_S5 |

### Abilities

| ID | Name | Point Type | Cost | XP Reward | Description | Effect | Trophy | Prerequisites |
|----|------|------------|------|-----------|-------------|--------|--------|---------------|
| L4_A1 | Barrier | AP | 250 | 75 | Temporary shield - absorbs 15 damage | Protect 2 cards from being replaced for 2 turns | Barrier Master | L3_A4 |
| L4_A2 | Rapid Fire | AP | 270 | 80 | Attack 2 targets at once - Damage: 12 per target | Place 3 cards simultaneously (different slots) | Triple Strike | L3_A3 |
| L4_A3 | Reflect | AP | 260 | 78 | Reflect 50% of damage back | When opponent scores, gain 50% of those points | Mirror | L3_A6 |
| L4_A4 | Overpower | AP | 280 | 85 | Ignore 20% enemy defense | Place card in protected slot (breaks protection) | Breaker | L4_A2 |
| L4_A5 | Revive | AP | 255 | 76 | Restore 1 discarded card to hand | Take 1 card from discard pile back to hand | Life Giver | L4_A1 |
| L4_A6 | Power Shot | AP | 275 | 82 | Heavy attack with knockback - Damage: 20 | Place card with +20 bonus, pushes opponent's card | Powerhouse | L4_A4 |
| L4_A7 | Time Stop | AP | 265 | 79 | Freeze time for 1 turn | Opponent cannot take actions for 1 turn | Chronos | None |

**Tier 4 Notes:**
- Side quests and challenges increase
- Certain trophies require specific skills to earn
- Skills cost 280-330 SP
- Abilities cost 250-280 AP
- 7 skills, 7 abilities = 14 total

---

## Tier 5: Expert (Levels 81-140)

### Skills

| ID | Name | Point Type | Cost | XP Reward | Description | Effect | Trophy | Prerequisites |
|----|------|------------|------|-----------|-------------|--------|--------|---------------|
| L5_S1 | Adaptive Strategy | SP | 450 | 112 | +5% effectiveness of all abilities for 3 turns | All abilities 1.05x effective for 3 turns | Adaptive | L4_S7 |
| L5_S2 | Team Strategist | SP | 480 | 120 | Allies gain +5% points from actions | In multiplayer, allies get +5% points | Commander | None |
| L5_S3 | Point Surge | SP | 470 | 117 | 20% chance for point surge (+50%) | When scoring, 20% chance for +50% bonus | Surger | L4_S3 |
| L5_S4 | Card Manipulator | SP | 490 | 122 | Can rearrange top 3 cards of deck | Reorder top 3 deck cards once per game | Manipulator | L4_S4 |
| L5_S5 | Perfect Defense | SP | 460 | 115 | Reduce all penalties by 25% | Opponent penalties increased by 25% | Perfect Defender | L4_S6 |
| L5_S6 | Chain Reaction | SP | 510 | 127 | Each combo increases next combo chance by 5% | Stacking +5% combo chance per combo | Reactor | L4_S1 |
| L5_S7 | Resource Master | SP | 440 | 110 | Reduce all point costs by 15% | All skills/abilities cost 0.85x points | Economist | L4_S5 |
| L5_S8 | Grand Master | SP | 520 | 130 | +30% XP from all abilities | All abilities grant 1.3x XP | Grandmaster | L5_S1 |

### Abilities

| ID | Name | Point Type | Cost | XP Reward | Description | Effect | Trophy | Prerequisites |
|----|------|------------|------|-----------|-------------|--------|--------|---------------|
| L5_A1 | Precision Aim | AP | 400 | 120 | Attack ignores 10% enemy defense | Place card ignoring 10% of slot restrictions | Sniper | L4_A6 |
| L5_A2 | Heal Pulse | AP | 380 | 114 | Heals allies for 10 HP in range | Gain +15 points, allies gain +5 (multiplayer) | Pulse Healer | L4_A5 |
| L5_A3 | Time Warp | AP | 420 | 126 | Skip enemy turn once | Opponent skips 2 turns | Warper | L4_A7 |
| L5_A4 | Meteor Strike | AP | 410 | 123 | Massive area damage - Damage: 30 | Place card affecting 3 adjacent slots with +25 each | Meteor | L5_A1 |
| L5_A5 | Resurrect | AP | 390 | 117 | Bring back 2 discarded cards | Take 2 cards from discard pile | Necromancer | L5_A2 |
| L5_A6 | Ultimate Guard | AP | 430 | 129 | Protect all cards for 2 turns | All cards protected from replacement for 2 turns | Guardian Angel | L5_A1 |
| L5_A7 | Double Trouble | AP | 415 | 124 | Place 2 cards with 50% bonus each | Place 2 cards simultaneously with +25 bonus each | Twin Strike | L5_A4 |
| L5_A8 | Reverse | AP | 405 | 121 | Reverse opponent's last action | Undo opponent's last card placement | Reverser | L5_A3 |

**Tier 5 Notes:**
- Mastery challenges introduced
- Trophies gated to combinations of points, skills, and achievements
- Skills cost 440-520 SP
- Abilities cost 380-430 AP
- 8 skills, 8 abilities = 16 total

---

## Tier 6: Master (Levels 141-200)

### Skills

| ID | Name | Point Type | Cost | XP Reward | Description | Effect | Trophy | Prerequisites |
|----|------|------------|------|-----------|-------------|--------|--------|---------------|
| L6_S1 | Apex Strategist | SP | 700 | 175 | Gain bonus XP from all abilities | All abilities grant +50% XP bonus | Apex | L5_S8 |
| L6_S2 | Unbreakable Will | SP | 750 | 187 | Reduce level loss from penalties by 1 | Cannot lose more than 1 level from XP penalties | Unbreakable | L5_S5 |
| L6_S3 | God Mode | SP | 800 | 200 | All abilities 20% more effective | All abilities 1.2x effective | Deity | L5_S7 |
| L6_S4 | Time Lord | SP | 780 | 195 | Reduce opponent action time by 30% | Opponent has 30% fewer turns | Chronos Lord | L5_S3 |
| L6_S5 | Point God | SP | 820 | 205 | 50% chance to triple points from action | When scoring, 50% chance for 3x points | Point Emperor | L5_S3 |
| L6_S6 | Card God | SP | 770 | 192 | Control entire deck order | Can view and reorder entire deck once per game | Card Emperor | L5_S4 |
| L6_S7 | Challenge Master | SP | 840 | 210 | Complete challenges 50% faster | Challenge requirements reduced by 50% | Challenge Lord | L5_S6 |
| L6_S8 | Grand Architect | SP | 860 | 215 | Design custom challenge | Create 1 custom challenge per week | Architect | L6_S7 |
| L6_S9 | Immortal | SP | 900 | 225 | Cannot lose levels | XP penalties cannot decrease level | Immortal | L6_S2 |
| L6_S10 | Supreme | SP | 950 | 237 | All previous skill effects combined | Gain benefits of all tier 1-5 skills | Supreme Being | L6_S1 |

### Abilities

| ID | Name | Point Type | Cost | XP Reward | Description | Effect | Trophy | Prerequisites |
|----|------|------------|------|-----------|-------------|--------|--------|---------------|
| L6_A1 | Dragon Strike | AP | 650 | 195 | Heavy attack - Damage: 25, Stamina: 5 | Place card with +50 bonus score | Dragon | L5_A8 |
| L6_A2 | Time Warp | AP | 700 | 210 | Skip enemy turn once | Opponent skips 3 turns | Time Lord | L5_A3 |
| L6_A3 | Phoenix Rise | AP | 670 | 201 | Resurrect with full health | Restore all 10 cards to face-up | Phoenix | L5_A5 |
| L6_A4 | Ragnarok | AP | 720 | 216 | Destroy everything - Damage: 50 | Reset game except your score +100 | Destroyer | L6_A1 |
| L6_A5 | Divine Shield | AP | 680 | 204 | Invulnerable for 3 turns | Cannot lose points for 3 turns | Divine | L5_A6 |
| L6_A6 | Multi-Strike | AP | 710 | 213 | Attack 5 targets at once | Place 5 cards simultaneously with +20 each | Multi-Master | L6_A4 |
| L6_A7 | Life Drain | AP | 690 | 207 | Steal points from opponent | Steal 20 points from opponent | Vampire | L6_A2 |
| L6_A8 | Teleport | AP | 700 | 210 | Instantly move to any position | Place any card in any slot immediately | Teleporter | L6_A5 |
| L6_A9 | Omnipotence | AP | 750 | 225 | Use any ability for free | One free ability use per game | Omnipotent | L6_A7 |
| L6_A10 | Ascension | AP | 800 | 240 | Reach godhood - All effects 50% better | All effects 1.5x for remainder of game | Ascended | L6_A9 |

**Tier 6 Notes:**
- Endgame content with ultimate challenges
- Trophies are rare and complex
- Players require mastery of skill trees and completion of high-tier challenges
- Skills cost 700-950 SP
- Abilities cost 650-800 AP
- 10 skills, 10 abilities = 20 total

---

## Summary Statistics

### Total Skills & Abilities by Tier

| Tier | Levels | Skills | Abilities | Total | SP Range | AP Range |
|------|--------|--------|-----------|-------|----------|----------|
| Life/Newbie | 1-5 | 3 | 3 | 6 | 50-70 | 35-45 |
| Beginner | 6-20 | 5 | 5 | 10 | 90-120 | 60-90 |
| Novice | 21-50 | 6 | 6 | 12 | 160-210 | 130-160 |
| Hard | 51-80 | 7 | 7 | 14 | 280-330 | 250-280 |
| Expert | 81-140 | 8 | 8 | 16 | 440-520 | 380-430 |
| Master | 141-200 | 10 | 10 | 20 | 700-950 | 650-800 |
| **TOTAL** | **1-200** | **39** | **39** | **78** | **50-950** | **35-800** |

### XP Rewards Summary

| Tier | Min XP | Max XP | Avg XP |
|------|--------|--------|--------|
| Life/Newbie | 10 | 20 | 15 |
| Beginner | 20 | 35 | 28 |
| Novice | 40 | 52 | 47 |
| Hard | 70 | 82 | 76 |
| Expert | 110 | 130 | 120 |
| Master | 175 | 240 | 212 |
| **TOTAL** | **10** | **240** | **83** |

### Trophy Count

| Tier | Skill Trophies | Ability Trophies | Total |
|------|----------------|------------------|-------|
| Life/Newbie | 3 | 3 | 6 |
| Beginner | 5 | 5 | 10 |
| Novice | 6 | 6 | 12 |
| Hard | 7 | 7 | 14 |
| Expert | 8 | 8 | 16 |
| Master | 10 | 10 | 20 |
| **TOTAL** | **39** | **39** | **78** |

### Implementation Notes

1. **Database Schema**: Each skill/ability should be stored with all properties
2. **Prerequisite Validation**: Check prerequisites before allowing unlock
3. **Progression Gating**: Ensure challenges are completed before tier access
4. **Balancing**: Point costs and XP rewards scale appropriately with tier
5. **Trophy System**: Each skill/ability awards a trophy when unlocked

### Recommended Implementation Order

1. **Phase 1**: Implement Tier 1 (Life/Newbie) - Tutorial progression
2. **Phase 2**: Implement Tiers 2-3 (Beginner/Novice) - Core gameplay
3. **Phase 3**: Implement Tier 4 (Hard) - Advanced mechanics
4. **Phase 4**: Implement Tiers 5-6 (Expert/Master) - Endgame content

This specification provides a complete, balanced, and progressive skill/ability system ready for direct implementation into the GCMS database.