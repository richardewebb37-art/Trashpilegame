# Skill & Ability System - Architecture Diagram

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         TRASH PILES GAME                             │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │                    GCMS CONTROLLER                          │    │
│  │                  (Single Source of Truth)                   │    │
│  │                                                             │    │
│  │  Commands:                    Events:                       │    │
│  │  • InitializeGame             • GameInitialized             │    │
│  │  • StartGame                  • GameStarted                 │    │
│  │  • DrawCard                   • CardDrawn                   │    │
│  │  • PlaceCard                  • CardPlaced                  │    │
│  │  • EndTurn                    • TurnEnded                   │    │
│  │  • UnlockNode        ←NEW     • NodeUnlocked       ←NEW    │    │
│  │  • UseAbility        ←NEW     • AbilityUsed       ←NEW    │    │
│  │                               • MatchCompleted     ←NEW    │    │
│  │                               • PointsEarned       ←NEW    │    │
│  └──────────────────┬───────────────────────┬─────────────────┘    │
│                     │                       │                       │
│                     ↓                       ↓                       │
│  ┌──────────────────────────────┐  ┌──────────────────────────┐   │
│  │      GCMS STATE              │  │    EVENT SUBSCRIBERS      │   │
│  │                              │  │                           │   │
│  │  • players                   │  │  • GameRenderer           │   │
│  │  • deck                      │  │  • GameAudio              │   │
│  │  • discardPile               │  │  • GameFlow               │   │
│  │  • currentPhase              │  │  • GameActivity (UI)      │   │
│  │  • skillAbilitySystem ←NEW  │  │  • SkillTreeUI     ←NEW  │   │
│  │    ├─ currentMatch           │  │  • AbilityTreeUI   ←NEW  │   │
│  │    └─ playerProgress         │  │  • MatchDialog     ←NEW  │   │
│  │       ├─ totalSP             │  │                           │   │
│  │       ├─ totalAP             │  │                           │   │
│  │       ├─ unlockedSkills      │  │                           │   │
│  │       ├─ unlockedAbilities   │  │                           │   │
│  │       └─ matchHistory        │  │                           │   │
│  └──────────────────────────────┘  └──────────────────────────┘   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Data Flow: Match Completion

```
┌─────────────────────────────────────────────────────────────────────┐
│                      MATCH COMPLETION FLOW                           │
└─────────────────────────────────────────────────────────────────────┘

1. MATCH ENDS
   ↓
   Player wins match #5
   Hand: [K♠, Q♥, J♣, 4♦, 5♠, 6♥, 7♣, 8♦, 9♠, 10♥]

2. CALCULATE PENALTIES
   ↓
   calculateCardPenalties(hand)
   ├─ King in slot 1:  3 + 1 = 4
   ├─ Queen in slot 2: 2 + 2 = 4
   └─ Jack in slot 3:  1 + 3 = 4
   Total Penalty: 12

3. CALCULATE POINTS
   ↓
   processMatchCompletion(state, winnerId, matchNumber)
   ├─ Base SP for Match 5: 3
   ├─ Base AP for Match 5: 3
   ├─ SP Earned: 3 (no penalty)
   └─ AP Earned: 3 - 12 = 0 (capped at 0)

4. UPDATE STATE
   ↓
   state.skillAbilitySystem.playerProgress[winnerId]
   ├─ totalSP += 3
   ├─ totalAP += 0
   ├─ matchHistory.add(result)
   └─ currentMatch++

5. EMIT EVENT
   ↓
   MatchCompletedEvent {
     matchNumber: 5,
     winnerId: "0",
     spEarned: 3,
     apEarned: 0,
     penalties: ["King in slot 1: 4", "Queen in slot 2: 4", "Jack in slot 3: 4"]
   }

6. UI REACTS
   ↓
   ├─ Show match completion dialog
   ├─ Display points earned
   ├─ Show penalty breakdown
   ├─ Update SP/AP display
   └─ Play celebration animation
```

---

## 🔓 Data Flow: Unlock Node

```
┌─────────────────────────────────────────────────────────────────────┐
│                        NODE UNLOCK FLOW                              │
└─────────────────────────────────────────────────────────────────────┘

1. PLAYER CLICKS NODE
   ↓
   UI: Player clicks "Quick Draw" in skill tree

2. SUBMIT COMMAND
   ↓
   gcms.submitCommand(UnlockNodeCommand(
     playerId: "0",
     nodeId: "quick_draw",
     pointType: "SKILL"
   ))

3. VALIDATE
   ↓
   unlockNode(state, playerId, nodeId, pointType)
   ├─ Check: Player has 5 SP? ✓
   ├─ Check: Prerequisites met (starter_boost)? ✓
   └─ Check: Not already unlocked? ✓

4. UPDATE STATE
   ↓
   state.skillAbilitySystem.playerProgress[playerId]
   ├─ totalSP -= 5
   └─ unlockedSkills.add("quick_draw")

5. EMIT EVENT
   ↓
   NodeUnlockedEvent {
     playerId: "0",
     nodeId: "quick_draw",
     nodeName: "Quick Draw",
     pointType: "SKILL",
     pointsSpent: 5
   }

6. UI REACTS
   ↓
   ├─ Play unlock animation
   ├─ Play success sound
   ├─ Update tree visual
   ├─ Update SP display
   └─ Show notification: "Unlocked: Quick Draw!"

7. APPLY EFFECT
   ↓
   Game engine applies passive effect:
   └─ Draw animations now 20% faster
```

---

## ⚡ Data Flow: Use Ability

```
┌─────────────────────────────────────────────────────────────────────┐
│                        ABILITY USE FLOW                              │
└─────────────────────────────────────────────────────────────────────┘

1. PLAYER ACTIVATES ABILITY
   ↓
   UI: Player clicks "Peek" ability button

2. SELECT TARGET
   ↓
   UI: Player selects card in slot 3

3. SUBMIT COMMAND
   ↓
   gcms.submitCommand(UseAbilityCommand(
     playerId: "0",
     abilityId: "peek",
     targetData: {"cardIndex": "3"}
   ))

4. VALIDATE
   ↓
   useAbility(state, playerId, abilityId, targetData)
   ├─ Check: Ability unlocked? ✓
   ├─ Check: Valid target? ✓
   └─ Check: Can use now? ✓

5. APPLY EFFECT
   ↓
   Based on abilityId:
   └─ "peek": Temporarily reveal card at index 3

6. EMIT EVENT
   ↓
   AbilityUsedEvent {
     playerId: "0",
     abilityId: "peek",
     abilityName: "Peek",
     effectDescription: "Peeked at card in slot 4: 7 of Clubs"
   }

7. UI REACTS
   ↓
   ├─ Play ability animation
   ├─ Play ability sound
   ├─ Show card temporarily (3 seconds)
   ├─ Show effect description
   └─ Update ability cooldown (if applicable)
```

---

## 🌳 Tree Structure

```
┌─────────────────────────────────────────────────────────────────────┐
│                          SKILL TREE                                  │
└─────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────────┐
                    │  Skill Mastery      │
                    │  Cost: 10 SP        │
                    │  Effect: +1 hand    │
                    └──────────┬──────────┘
                               │
                ┌──────────────┴──────────────┐
                │                             │
        ┌───────▼────────┐          ┌────────▼───────┐
        │  Quick Draw    │          │  Card Sight    │
        │  Cost: 5 SP    │          │  Cost: 5 SP    │
        │  Effect: 20%   │          │  Effect: Peek  │
        │  faster draw   │          │  opponent card │
        └───────┬────────┘          └────────┬───────┘
                │                            │
        ┌───────▼────────┐          ┌────────▼───────┐
        │ Starter Boost  │          │    Memory      │
        │  Cost: 2 SP    │          │  Cost: 2 SP    │
        │  Effect: 1     │          │  Effect: See   │
        │  card face-up  │          │  last discard  │
        └────────────────┘          └────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                         ABILITY TREE                                 │
└─────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────────┐
                    │ Ultimate Power      │
                    │  Cost: 10 AP        │
                    │  Effect: Reshuffle  │
                    │  all face-down      │
                    └──────────┬──────────┘
                               │
                ┌──────────────┴──────────────┐
                │                             │
        ┌───────▼────────┐          ┌────────▼───────┐
        │   Wild Card    │          │  Swap Master   │
        │  Cost: 6 AP    │          │  Cost: 6 AP    │
        │  Effect: Make  │          │  Effect: Swap  │
        │  card wild     │          │  2 cards       │
        └───────┬────────┘          └────────┬───────┘
                │                            │
        ┌───────▼────────┐          ┌────────▼───────┐
        │     Peek       │          │    Reveal      │
        │  Cost: 2 AP    │          │  Cost: 2 AP    │
        │  Effect: Look  │          │  Effect: Flip  │
        │  at 1 card     │          │  opponent card │
        └────────────────┘          └────────────────┘
```

---

## 📊 State Management

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SKILL/ABILITY SYSTEM STATE                        │
└─────────────────────────────────────────────────────────────────────┘

GCMSState
└─ skillAbilitySystem: SkillAbilitySystemState
   ├─ currentMatch: Int (1-10)
   │  └─ Tracks which match in the 10-match session
   │
   └─ playerProgress: Map<String, PlayerProgress>
      └─ ["player_0"]: PlayerProgress
         ├─ playerId: "player_0"
         ├─ totalSP: 15
         ├─ totalAP: 8
         ├─ unlockedSkills: ["starter_boost", "memory", "quick_draw"]
         ├─ unlockedAbilities: ["peek", "reveal"]
         └─ matchHistory: [
            MatchResult(matchNumber=1, won=true, spEarned=1, apEarned=0, ...),
            MatchResult(matchNumber=2, won=true, spEarned=1, apEarned=1, ...),
            MatchResult(matchNumber=3, won=true, spEarned=2, apEarned=0, ...),
            MatchResult(matchNumber=4, won=true, spEarned=2, apEarned=2, ...),
            MatchResult(matchNumber=5, won=true, spEarned=3, apEarned=0, ...)
         ]
```

---

## 🔄 Command & Event Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                   COMMAND → VALIDATION → EVENT                       │
└─────────────────────────────────────────────────────────────────────┘

UI/Input
   │
   ↓ Submit Command
   │
GCMSController
   │
   ├─→ Validate Command
   │   ├─ Check prerequisites
   │   ├─ Check resources
   │   └─ Check game state
   │
   ├─→ Execute Command
   │   ├─ Update state
   │   └─ Apply effects
   │
   └─→ Emit Event(s)
       │
       ↓ Broadcast to all subscribers
       │
       ├─→ GameRenderer (visual updates)
       ├─→ GameAudio (sound effects)
       ├─→ GameFlow (orchestration)
       └─→ UI (display updates)

Example Flow:

UnlockNodeCommand
   ↓
Validate: Has points? Prerequisites met?
   ↓
Execute: Deduct points, add to unlocked list
   ↓
Emit: NodeUnlockedEvent
   ↓
UI: Play animation, update display
```

---

## 🎯 Integration Points

```
┌─────────────────────────────────────────────────────────────────────┐
│                      INTEGRATION POINTS                              │
└─────────────────────────────────────────────────────────────────────┘

1. MATCH END
   GameRules.hasPlayerWon() → true
   ↓
   GameRules.processMatchCompletion(state, winnerId)
   ↓
   MatchCompletedEvent emitted

2. SKILL TREE UI
   Player clicks node
   ↓
   Submit UnlockNodeCommand
   ↓
   Listen for NodeUnlockedEvent
   ↓
   Update tree visual

3. ABILITY BAR UI
   Player clicks ability
   ↓
   Submit UseAbilityCommand
   ↓
   Listen for AbilityUsedEvent
   ↓
   Apply visual effect

4. POINTS DISPLAY
   Listen for PointsEarnedEvent
   ↓
   Update SP/AP counters
   ↓
   Animate point increase

5. GAME EFFECTS
   Listen for NodeUnlockedEvent
   ↓
   Check node.effect.type
   ↓
   Apply passive/active effect to gameplay
```

---

## 🧪 Testing Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         TEST STRUCTURE                               │
└─────────────────────────────────────────────────────────────────────┘

SkillAbilitySystemTest (36 tests)
│
├─ Penalty Calculation Tests (6)
│  ├─ No face cards → 0 penalty
│  ├─ King in slot 1 → 4 penalty
│  ├─ King in slot 10 → 13 penalty
│  ├─ Multiple face cards → accumulated
│  ├─ Queen < King penalty
│  └─ Jack < Queen penalty
│
├─ Match Rewards Tests (6)
│  ├─ Match 1 → 1 SP, 0 AP
│  ├─ Match 5 → 3 SP, 3 AP
│  ├─ Match 10 → 5 SP, 5 AP
│  ├─ Total possible → 30 SP, 29 AP
│  └─ Progression increases
│
├─ Match Completion Tests (6)
│  ├─ Perfect match → full points
│  ├─ High penalties → 0 AP
│  ├─ Strategic placement → minimized
│  ├─ Progress updated
│  └─ Match number advanced
│
├─ Node Unlock Tests (6)
│  ├─ Sufficient points → success
│  ├─ Insufficient points → fail
│  ├─ Unmet prerequisites → fail
│  ├─ Prerequisites met → success
│  ├─ Already unlocked → fail
│  └─ AP vs SP validation
│
├─ Tree Structure Tests (6)
│  ├─ Skill tree has 5 nodes
│  ├─ Starter nodes no prereqs
│  ├─ Mastery requires tier 2
│  ├─ Ability tree has 5 nodes
│  ├─ Ultimate requires tier 2
│  └─ Available nodes filtered
│
├─ Player Progress Tests (4)
│  ├─ Starts with zero
│  ├─ addMatchResult updates
│  ├─ canAfford checks correct type
│  └─ deductPoints reduces total
│
└─ Integration Tests (2)
   ├─ Full 10-match session
   └─ Complete tree unlock
```

---

**Architecture Status: ✅ Complete and Production-Ready**