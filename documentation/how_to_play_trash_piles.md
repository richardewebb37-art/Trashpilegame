# TRASH - Complete Game Guide
## From Beginner to Master

---

## Table of Contents
1. [What is Trash?](#what-is-trash)
2. [How to Play](#how-to-play)
3. [Scoring System](#scoring-system)
4. [Leveling System](#leveling-system)
5. [Skill Points (SP)](#skill-points-sp)
6. [Ability Points (AP)](#ability-points-ap)
7. [Skills Tree](#skills-tree)
8. [Abilities Tree](#abilities-tree)
9. [Strategy Guide](#strategy-guide)
10. [Quick Reference](#quick-reference)

---

## What is Trash?

**Trash** is a fast-paced card game where players race to complete their hand by flipping cards in order from Ace (1) to 10. The first player to flip all their cards wins the round!

### Game Features
- 🎴 Classic card game with modern progression
- 🎯 10 rounds per match
- ⬆️ Leveling system (unlimited progression)
- 💪 Skills & Abilities to unlock
- 🎲 Dice bonus system
- 🃏 Special Joker card

### Players
- 2-4 players
- Single player vs AI
- Online multiplayer

### Game Modes
- **Classic Mode:** Traditional Trash gameplay without progression
- **Advanced Mode:** Full progression system with levels, SP, AP, skills, and abilities

---

## How to Play

### Game Setup

**Round 1 Setup:**
```
Each player receives: 10 cards face-down
Layout:

Slot:  [1] [2] [3] [4] [5] [6] [7] [8] [9] [10]
Card:  [?] [?] [?] [?] [?] [?] [?] [?] [?] [?]

Remaining cards → Draw Pile (face-down)
Discard Pile starts empty
```

### Turn Structure

**On Your Turn:**

1. **Draw Phase**
   - Draw 1 card from Draw Pile OR Discard Pile
   
2. **Action Phase**
   - **Option A:** Place the card in its matching slot
     - Ace → Slot 1
     - 2 → Slot 2
     - ...
     - 10 → Slot 10
     - Joker → ANY slot (wild card)
   
   - **Option B:** Discard if card doesn't fit
   
3. **When You Place a Card:**
   - Flip the card FACE-UP in its slot
   - If there was a face-down card there, draw it
   - That card becomes your new card to place
   - Keep placing until you can't place anymore
   - Discard the final card

4. **End Turn**

### Winning a Round

**First player to flip ALL 10 cards face-up WINS the round!**

Example winning hand:
```
[A♠] [2♥] [3♣] [4♦] [5♠] [6♥] [7♣] [8♦] [9♠] [10♥]
 ✓    ✓    ✓    ✓    ✓    ✓    ✓    ✓    ✓    ✓
```

### Round Progression

Each round reduces hand size:

| Round | Cards per Player |
|-------|------------------|
| 1     | 10               |
| 2     | 9                |
| 3     | 8                |
| 4     | 7                |
| 5     | 6                |
| 6     | 5                |
| 7     | 4                |
| 8     | 3                |
| 9     | 2                |
| 10    | 1                |

### Match Complete

**After 10 rounds → Match ends**
- Player with highest total score WINS
- Winner earns Skill Points & Ability Points
- Winner earns XP and may level up
- Losers earn no SP, AP, or XP

### Skipped Turns

If you cannot place any card on your turn, you must skip your turn. This happens when:
- The card you drew doesn't match any empty slot
- All your cards are already face-up
- You choose to discard without placing

---

## Scoring System

### Round Scoring (Each Round)

**Only FACE-UP cards count for points!**

#### Base Score
```
Your Score = Sum of face-up card slot numbers

Example:
Slots 1, 5, 7 are face-up
Base Score = 1 + 5 + 7 = 13 points
```

#### Joker Bonus
```
Joker face-up = +20 BONUS points

Example:
Slots 1, 5, Joker(7) face-up
Base Score = 1 + 5 + 7 + 20 = 33 points
```

#### Dice Bonus System

**Everyone rolls a die (1-6) after each round!**

**For LOSERS:**
```
Multiplier = Number of face-up cards
Bonus = Multiplier × Dice Roll

Example:
3 cards face-up, rolled 4
Bonus = 3 × 4 = 12
```

**For WINNERS:**
```
Multiplier = Last card slot flipped
Bonus = Multiplier × Dice Roll

Example:
Last card was slot 8, rolled 6
Bonus = 8 × 6 = 48
```

**Final Round Score:**
```
Final Score = Base Score + Dice Bonus
```

### Match Scoring (After 10 Rounds)

```
Match Score = Sum of all 10 round scores

Round 1:  95 points
Round 2:  87 points
Round 3: 102 points
...
Round 10: 72 points
────────────────
Total:   847 points ← Match Score
```

**HIGHEST Match Score WINS!**

### Complete Scoring Example

```
Round 1 - Player A (WINNER):
  Face-up: All 10 slots (1-10)
  Base = 1+2+3+4+5+6+7+8+9+10 = 55
  Last card flipped: Slot 8
  Multiplier = 8
  Dice = 5
  Bonus = 8 × 5 = 40
  Round Score = 55 + 40 = 95 ✅

Round 1 - Player B (LOSER):
  Face-up: Slots 1, 5, 7 (3 cards)
  Base = 1 + 5 + 7 = 13
  Multiplier = 3 (face-up count)
  Dice = 4
  Bonus = 3 × 4 = 12
  Round Score = 13 + 12 = 25

After 10 rounds:
Player A Total: 847 points → WINNER! 🏆
Player B Total: 612 points
```

---

## Leveling System

### How Leveling Works

**Win matches → Earn XP → Gain Levels → Unlock new Skills/Abilities**

**Important:** The leveling system is unlimited! Players can continue leveling up indefinitely by playing more matches and rounds.

### XP Formula

```
XP Earned = (SP + AP) × Level Multiplier + Match Bonus + Round Bonus

Level Multiplier = 1 + (Current Level × 0.05)
Match Bonus = Total Matches Played × 10
Round Bonus = Total Rounds Completed × 50

Examples:
Level 1: 1.05× multiplier
Level 5: 1.25× multiplier
Level 10: 1.5× multiplier
Level 50: 3.5× multiplier
Level 100: 6.0× multiplier
```

### Level Progression

**Levels are calculated using a logarithmic formula for unlimited progression:**

```
Level = floor(log(XP + 1) / log(1.2)) + 1
```

This ensures:
- No upper level cap
- Smooth, balanced progression
- Higher levels require more XP but earn more per match

**Example Progression:**
- 1-10 matches: Levels 1-10
- 10-50 matches: Levels 10-20
- 50-200 matches: Levels 20-40
- 200-1000 matches: Levels 40-60
- 1000+ matches: Levels 60+ (unlimited!)

### What Levels Unlock

**Tier 1 (Levels 1-3):** Basic Skills & Abilities
**Tier 2 (Levels 4-6):** Advanced Powers
**Tier 3 (Levels 7-10):** Master Abilities
**Tier 4+ (Levels 11+):** Continued progression with same skill/ability trees

### Leveling Example

```
Player starts at Level 1

Round 1, Match 1 WIN:
  SP Earned: 10
  AP Earned: 8
  Level Multiplier: 1.05
  Match Bonus: 10
  Round Bonus: 50
  XP = (10 + 8) × 1.05 + 10 + 50 = 79
  Total XP: 79

After several wins:
  Total XP: 200+ → LEVEL 6! ✅
  
Now at Level 6 (1.3× multiplier)

Round 3, Match 1 WIN:
  SP: 30, AP: 25
  Matches: 15, Rounds: 2
  XP = (30 + 25) × 1.3 + 150 + 100 = 326
  Total XP: 500+ → LEVEL 10! ✅

Keeps progressing indefinitely...
Level 20 at ~3,000 XP
Level 50 at ~750,000 XP
Level 100 at ~7,000,000,000 XP
```

---

## Skill Points (SP)

### What are Skill Points?

**SP = Currency to unlock SKILLS**

Skills are strategic powers that improve your gameplay:
- See hidden information
- Manipulate cards
- Control dice
- Get tactical advantages

### How to Earn SP

**Only match WINNERS earn SP!**

```
SP Earned per Match = 10 × Round Number

Round 1: 10 SP
Round 2: 20 SP
Round 3: 30 SP
...
Round 10: 100 SP
```

**No Penalties on SP** - You always get full SP if you win!

### SP Earning Examples

```
Win Match in Round 1:
  SP Earned: 10 × 1 = 10 SP ✅

Win Match in Round 5:
  SP Earned: 10 × 5 = 50 SP ✅

Win all 10 matches in Round 3:
  SP per match: 30
  Total SP: 30 × 10 = 300 SP ✅
```

### SP Accumulation

```
Your SP accumulates across ALL matches!

Start: 0 SP
Win R1M1: +10 SP → Total: 10 SP
Win R1M2: +10 SP → Total: 20 SP
Win R1M5: +10 SP → Total: 30 SP
Win R2M1: +20 SP → Total: 50 SP
...

You keep ALL SP earned!
Spend SP to unlock skills in the Skill Tree
```

---

## Ability Points (AP)

### What are Ability Points?

**AP = Currency to unlock ABILITIES**

Abilities reduce penalties and protect you:
- Reduce King/Queen/Jack penalties
- Reduce Joker penalty
- One-time saves

### How to Earn AP

**Only match WINNERS earn AP!**

```
Base AP per Match = 10 × Round Number

BUT: AP is reduced by penalties!

AP Earned = Base AP - Penalties (minimum 0)
```

### AP Penalties

**Face-down cards at round end cause penalties:**

| Card (Face-Down) | Penalty |
|------------------|---------|
| King             | -3 AP   |
| Queen            | -2 AP   |
| Jack             | -1 AP   |
| Joker            | -20 AP  |
| Number cards     | 0 AP    |

**Important:** Only face-DOWN cards cause penalties!

### AP Earning Examples

```
Example 1: Clean Win (No Penalties)
  Round 3, all cards face-up
  Base AP: 30
  Penalties: 0
  AP Earned: 30 ✅

Example 2: Some Penalties
  Round 2, 1 King face-down
  Base AP: 20
  Penalties: -3
  AP Earned: 20 - 3 = 17 ✅

Example 3: Heavy Penalties
  Round 5, King + Queen + Jack face-down
  Base AP: 50
  Penalties: -3 + -2 + -1 = -6
  AP Earned: 50 - 6 = 44 ✅

Example 4: Joker Disaster
  Round 4, Joker face-down
  Base AP: 40
  Penalties: -20
  AP Earned: 40 - 20 = 20 ✅

Example 5: Total Loss
  Round 2, many face cards face-down
  Base AP: 20
  Penalties: -25
  AP Earned: 20 - 25 = -5 → 0 ❌
  (Capped at 0, can't go negative)
```

### Match-Wide Penalties

**Penalties accumulate across ALL rounds in a match!**

```
Match Example:
  Round 1: King face-down (-3 AP)
  Round 3: Queen face-down (-2 AP)
  Round 5: Jack face-down (-1 AP)
  Round 7: King face-down (-3 AP)
  
  Total Penalties: -9 AP
  
  Match 5 Base AP: 50
  AP Earned: 50 - 9 = 41 AP for the match
```

---

## Skills Tree

### Skill Overview

**Skills = Offensive Powers**

Use SP to unlock skills that give you strategic advantages!

### Tier 1 Skills (Levels 1-3)

**Basic Powers - Low Cost**

#### Peek (3 SP) - Level 1
```
Effect: Look at 1 face-down card per round
Usage: Active, once per round
Power: See your or opponent's hidden card
```

#### Second Chance (4 SP) - Level 1
```
Effect: Put a wrong card back face-down
Usage: Active, once per round
Power: Undo a bad flip
```

#### Quick Draw (5 SP) - Level 2
```
Effect: Draw 2 cards, choose which to use
Usage: Passive, always active
Power: Better card selection
```

#### Card Memory (5 SP) - Level 2
```
Effect: See all discarded cards this match
Usage: Passive, always active
Power: Track what's been played
```

#### Loaded Dice (6 SP) - Level 2
```
Effect: Reroll your dice once per round
Usage: Active, once per round
Power: Second chance at bonus points
```

#### Slot Vision (7 SP) - Level 3
```
Effect: See which slots opponents need
Usage: Passive, always active
Power: Know their weaknesses
```

### Tier 2 Skills (Levels 4-6)

**Advanced Powers - Medium Cost**

#### Card Swap (8 SP) - Level 4
```
Prerequisites: Peek
Effect: Swap 2 cards in your hand (positions)
Usage: Active, once per round
Power: Fix your slot arrangement
Example: Move card from slot 3 to slot 7
```

#### Lucky Start (10 SP) - Level 4
```
Effect: Start each round with 1 random card face-up
Usage: Passive, automatic each round
Power: Head start every round
```

#### Speed Play (10 SP) - Level 5
```
Effect: +2 seconds to turn timer
Usage: Passive, always active
Power: More time to think
```

#### Scavenge (12 SP) - Level 5
```
Prerequisites: Quick Draw
Effect: See top 2 cards when drawing from discard
Usage: Passive, always active
Power: Better discard pile choices
```

#### Dice Master (15 SP) - Level 6
```
Prerequisites: Loaded Dice
Effect: Roll 2 dice, choose better result
Usage: Passive, always active
Power: Consistent high dice rolls
```

### Tier 3 Skills (Levels 7-10)

**Master Powers - High Cost**

#### Master Swap (18 SP) - Level 7
```
Prerequisites: Card Swap
Effect: Swap up to 3 cards per round
Usage: Active, once per round
Power: Total hand control
```

#### Fortune Teller (20 SP) - Level 7
```
Prerequisites: Peek, Lucky Start
Effect: See top 3 cards of deck before drawing
Usage: Active, once per round
Power: Plan ahead perfectly
```

#### Wild Card Master (25 SP) - Level 8
```
Effect: Turn any card into a wild card once per match
Usage: Active, once per match
Power: Make any card fit anywhere
Example: Make a 5 act like an Ace
```

#### Perfect Roll (30 SP) - Level 9
```
Prerequisites: Dice Master
Effect: Set dice to 6 once per match
Usage: Active, once per match
Power: Guarantee maximum bonus
```

### Skill Tree Visual

```
                PERFECT ROLL (30 SP)
                      │
                 DICE MASTER (15 SP)
                      │
                 LOADED DICE (6 SP)


              WILD CARD MASTER (25 SP)


              FORTUNE TELLER (20 SP)
                     │
         ┌───────────┴───────────┐
    LUCKY START            PEEK
     (10 SP)              (3 SP)


              MASTER SWAP (18 SP)
                     │
                CARD SWAP (8 SP)
                     │
                  PEEK (3 SP)


              SCAVENGE (12 SP)
                    │
              QUICK DRAW (5 SP)


     SECOND CHANCE    CARD MEMORY    SLOT VISION    SPEED PLAY
        (4 SP)          (5 SP)         (7 SP)        (10 SP)
```

---

## Abilities Tree

### Ability Overview

**Abilities = Defensive Powers**

Use AP to unlock abilities that reduce penalties!

### Tier 1 Abilities (Levels 1-3)

**Basic Protection - Low Cost**

#### Jack's Favor (3 AP) - Level 1
```
Effect: Jack penalty -1 → 0
Description: Jacks no longer hurt you
Type: Passive, always active
Savings: 1 AP per face-down Jack
```

#### Queen's Grace (4 AP) - Level 2
```
Effect: Queen penalty -2 → -1
Description: Queens are kinder
Type: Passive, always active
Savings: 1 AP per face-down Queen
```

#### King's Mercy (5 AP) - Level 2
```
Effect: King penalty -3 → -2
Description: Reduce King's wrath
Type: Passive, always active
Savings: 1 AP per face-down King
```

#### Royal Pardon (8 AP) - Level 3
```
Effect: Erase ALL face card penalties from current match
Description: One-time salvation
Type: Active, one-time use per match
Use: After match ends, before AP calculation
```

### Tier 2 Abilities (Levels 4-6)

**Advanced Protection - Medium Cost**

#### Royal Shield (10 AP) - Level 4
```
Prerequisites: King's Mercy, Queen's Grace, Jack's Favor
Effect: All face cards get ANOTHER -1 penalty reduction
Description: Maximum royal protection
Type: Passive, stacks with Tier 1

Example with full stack:
  King: -3 → -2 (Mercy) → -1 (Shield)
  Queen: -2 → -1 (Grace) → 0 (Shield)
  Jack: -1 → 0 (Favor)
```

#### Joker's Escape (12 AP) - Level 4
```
Effect: Joker penalty = 0 for current match
Description: The Joker vanishes
Type: Active, one-time use
Use: After match with face-down Joker
```

#### Joker's Bargain (15 AP) - Level 5
```
Effect: Joker penalty -20 → -10
Description: Negotiate with chaos
Type: Passive, always active
Savings: 10 AP if Joker face-down
```

### Tier 3 Abilities (Levels 7-10)

**Master Protection - High Cost**

#### Face Card Immunity (25 AP) - Level 7
```
Prerequisites: Royal Shield
Effect: ALL face cards = 0 penalty (K, Q, J)
Description: Royalty cannot harm you
Type: Passive, always active
Note: Does NOT affect Joker
```

#### Joker's Ally (30 AP) - Level 8
```
Prerequisites: Joker's Bargain
Effect: Joker penalty -20 → -5
Description: Turn chaos into friend
Type: Passive, always active
Ultimate defense: Joker barely hurts
```

### Ability Tree Visual

```
            FACE CARD IMMUNITY (25 AP)
                      │
                 ROYAL SHIELD (10 AP)
                      │
         ┌────────────┼────────────┐
         │            │            │
    KING'S MERCY  QUEEN'S GRACE  JACK'S FAVOR
      (5 AP)        (4 AP)         (3 AP)


              JOKER'S ALLY (30 AP)
                      │
              JOKER'S BARGAIN (15 AP)


       ROYAL PARDON       JOKER'S ESCAPE
         (8 AP)              (12 AP)
      [One-time]          [One-time]
```

---

## Strategy Guide

### Beginner Strategy

**First 5 Matches:**
1. Focus on winning rounds
2. Learn card placement
3. Save points initially
4. Unlock Peek (3 SP) first
5. Get Jack's Favor (3 AP) early

**Early Build:**
```
SP: Peek (3), Quick Draw (5) = 8 SP
AP: Jack's Favor (3), Queen's Grace (4) = 7 AP
Total needed: 8 SP, 7 AP (achievable in ~3 wins)
```

### Intermediate Strategy

**Levels 3-6:**
1. Build toward Card Swap
2. Unlock Royal Shield
3. Start using active abilities
4. Time your one-time saves

**Mid Build:**
```
Skills: Peek → Card Swap → Lucky Start
Abilities: Jack → Queen → King → Royal Shield
Focus: Reduce penalties, improve draws
```

### Advanced Strategy

**Levels 7-10+:**
1. Unlock Tier 3 powers
2. Master dice manipulation
3. Perfect your card placement
4. Use strategic saves
5. Continue leveling for increased multipliers

**Late Build:**
```
Skills: Master Swap + Fortune Teller + Dice Master
Abilities: Face Card Immunity + Joker's Ally
Result: Nearly unstoppable
```

### Pro Tips

**Card Placement:**
```
✅ Try to make slot 10 your last card (max dice multiplier)
✅ Flip face cards ASAP to avoid penalties
✅ Use Joker in low slots if can't use immediately
```

**Point Management:**
```
✅ Balance SP and AP spending
✅ Unlock prerequisites early
✅ Save for big upgrades
✅ Don't hoard points unnecessarily
```

**Dice Strategy:**
```
✅ Loaded Dice early game (rerolls)
✅ Dice Master mid game (choose better)
✅ Perfect Roll late game (guarantee 6)
```

**Leveling Strategy:**
```
✅ Play consistently to accumulate match and round bonuses
✅ Higher levels earn more XP per match
✅ The unlimited system rewards long-term play
✅ Level multipliers increase from 1.05× to 6.0×+
```

---

## Quick Reference

### Win Condition
```
First to flip all cards face-up wins the round
Highest total score after 10 rounds wins the match
```

### Points per Match
```
SP = 10 × Round# (no penalties)
AP = 10 × Round# - penalties
XP = (SP + AP) × level multiplier + (matches × 10) + (rounds × 50)
```

### Penalties (Face-Down)
```
King: -3 AP
Queen: -2 AP
Jack: -1 AP
Joker: -20 AP
```

### Dice Bonus
```
Loser: face-up count × dice
Winner: last card slot × dice
```

### Joker Rules
```
Face-up: +20 points to round score
Face-down: -20 AP penalty
Wild card: Goes in any slot
One-use: Removed after first round used
```

### Level Tiers
```
1-3: Basic (Tier 1)
4-6: Advanced (Tier 2)
7-10: Master (Tier 3)
11+: Continued progression (unlimited)
```

### Level Multipliers
```
Level 1: 1.05×
Level 5: 1.25×
Level 10: 1.5×
Level 20: 2.0×
Level 50: 3.5×
Level 100: 6.0×
```

### Must-Have Early Unlocks
```
SP: Peek (3), Quick Draw (5)
AP: Jack's Favor (3), Queen's Grace (4)
```

---

## Summary

**TRASH is a game of:**
- 🎯 **Speed** - Be first to flip all cards
- 🎲 **Luck** - Dice and draws matter
- 🧠 **Strategy** - Card placement and timing
- 📈 **Progression** - Level up and get stronger (unlimited!)
- 💪 **Mastery** - Unlock powerful abilities

**The Journey:**
```
Play Matches → Win → Earn SP/AP/XP → Level Up → Unlock Powers → Dominate!
```

**Good luck, and may the cards be in your favor!** 🃏✨