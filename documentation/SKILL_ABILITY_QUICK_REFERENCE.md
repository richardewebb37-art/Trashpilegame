# Skill & Ability System - Quick Reference

## 🎯 For Game Developers

### Quick Integration Steps

1. **Match Ends → Award Points**
```kotlin
// When a player wins a match
val result = GameRules.processMatchCompletion(state, winnerId)

// Emit event
gcms.emitEvent(MatchCompletedEvent(
    matchNumber = result.matchNumber,
    winnerId = winnerId.toString(),
    spEarned = result.spEarned,
    apEarned = result.apEarned,
    penalties = result.penalties.map { "${it.cardRank} in slot ${it.slotNumber}: ${it.penalty}" }
))
```

2. **Display Player Points**
```kotlin
val progress = state.skillAbilitySystem.getPlayerProgress(playerId)
println("SP: ${progress.totalSP}")
println("AP: ${progress.totalAP}")
```

3. **Unlock Skill/Ability**
```kotlin
gcms.submitCommand(UnlockNodeCommand(
    playerId = playerId,
    nodeId = "quick_draw",
    pointType = "SKILL"
))
```

4. **Use Ability**
```kotlin
gcms.submitCommand(UseAbilityCommand(
    playerId = playerId,
    abilityId = "peek",
    targetData = mapOf("cardIndex" to "3")
))
```

5. **Listen to Events**
```kotlin
gcms.events.collect { event ->
    when (event) {
        is MatchCompletedEvent -> showMatchSummary(event)
        is NodeUnlockedEvent -> playUnlockAnimation(event)
        is AbilityUsedEvent -> applyAbilityEffect(event)
        is PointsEarnedEvent -> updatePointsDisplay(event)
    }
}
```

---

## 📊 Point Calculation Cheat Sheet

### Match Rewards
```
Match 1:  1 SP, 0 AP
Match 2:  1 SP, 1 AP
Match 3:  2 SP, 2 AP
Match 4:  2 SP, 2 AP
Match 5:  3 SP, 3 AP
Match 6:  3 SP, 3 AP
Match 7:  4 SP, 4 AP
Match 8:  4 SP, 4 AP
Match 9:  5 SP, 5 AP
Match 10: 5 SP, 5 AP
─────────────────────
Total:   30 SP, 29 AP
```

### Card Penalties
```
King:  3 + slot number
Queen: 2 + slot number
Jack:  1 + slot number

Examples:
King in slot 1:  3 + 1 = 4
King in slot 10: 3 + 10 = 13
Queen in slot 5: 2 + 5 = 7
Jack in slot 3:  1 + 3 = 4
```

### Final Points Formula
```
SP Earned = Base SP (never penalized)
AP Earned = max(0, Base AP - Total Penalties)
```

---

## 🌳 Skill Tree

```
Tier 3: Skill Mastery (10 SP)
        ├─ Requires: Quick Draw + Card Sight
        └─ Effect: +1 hand size

Tier 2: Quick Draw (5 SP)          Card Sight (5 SP)
        ├─ Requires: Starter Boost  ├─ Requires: Memory
        └─ Effect: 20% faster draw  └─ Effect: Peek opponent card

Tier 1: Starter Boost (2 SP)       Memory (2 SP)
        ├─ Requires: None           ├─ Requires: None
        └─ Effect: 1 card face-up   └─ Effect: See last discard
```

---

## 🔮 Ability Tree

```
Tier 3: Ultimate Power (10 AP)
        ├─ Requires: Wild Card + Swap Master
        └─ Effect: Reshuffle all face-down cards

Tier 2: Wild Card (6 AP)           Swap Master (6 AP)
        ├─ Requires: Peek           ├─ Requires: Reveal
        └─ Effect: Make card wild   └─ Effect: Swap 2 cards

Tier 1: Peek (2 AP)                Reveal (2 AP)
        ├─ Requires: None           ├─ Requires: None
        └─ Effect: Look at 1 card   └─ Effect: Flip opponent card
```

---

## 🎮 Common Scenarios

### Scenario 1: Perfect Match
```
Hand: [A, 2, 3, 4, 5, 6, 7, 8, 9, 10]
Penalties: 0
Match 5 Result: +3 SP, +3 AP
```

### Scenario 2: High Penalties
```
Hand: [K, Q, J, 4, 5, K, 7, Q, 9, J]
Penalties: 4 + 4 + 4 + 9 + 10 + 11 = 42
Match 5 Result: +3 SP, +0 AP (3 - 42 = 0)
```

### Scenario 3: Strategic Placement
```
Hand: [K, Q, J, 4, 5, 6, 7, 8, 9, 10]
Penalties: 4 + 4 + 4 = 12
Match 5 Result: +3 SP, +0 AP (3 - 12 = 0)
```

### Scenario 4: Full Session
```
10 matches with minimal penalties:
Total: 30 SP, ~20 AP (some lost to penalties)

Can unlock:
- All Tier 1 skills (4 SP)
- All Tier 2 skills (10 SP)
- Skill Mastery (10 SP)
- All Tier 1 abilities (4 AP)
- Both Tier 2 abilities (12 AP)
Remaining: 6 SP, 4 AP
```

---

## 🔍 Validation Rules

### Unlock Node
```
✓ Player has enough points (SP or AP)
✓ Prerequisites are unlocked
✓ Node not already unlocked
✗ Insufficient points → InvalidMoveEvent
✗ Prerequisites not met → InvalidMoveEvent
✗ Already unlocked → InvalidMoveEvent
```

### Use Ability
```
✓ Ability is unlocked
✓ Target data is valid
✓ Game state allows ability use
✗ Not unlocked → InvalidMoveEvent
✗ Invalid target → InvalidMoveEvent
```

---

## 📝 Event Handling Examples

### Match Completed
```kotlin
when (event) {
    is MatchCompletedEvent -> {
        // Show summary dialog
        showDialog {
            title = "Match ${event.matchNumber} Complete!"
            message = """
                SP Earned: +${event.spEarned}
                AP Earned: +${event.apEarned}
                
                Penalties:
                ${event.penalties.joinToString("\n")}
            """.trimIndent()
        }
    }
}
```

### Node Unlocked
```kotlin
when (event) {
    is NodeUnlockedEvent -> {
        // Play unlock animation
        playAnimation("unlock_${event.nodeId}")
        
        // Play sound
        playSound("unlock_success")
        
        // Show notification
        showToast("Unlocked: ${event.nodeName}!")
        
        // Update UI
        updateTreeUI()
    }
}
```

### Ability Used
```kotlin
when (event) {
    is AbilityUsedEvent -> {
        // Play ability animation
        playAnimation("ability_${event.abilityId}")
        
        // Show effect
        showEffect(event.effectDescription)
        
        // Update game state based on ability
        when (event.abilityId) {
            "peek" -> revealCardTemporarily()
            "wild_card" -> convertToWild()
            "swap_master" -> swapCards()
            // etc.
        }
    }
}
```

---

## 🐛 Common Issues & Solutions

### Issue: AP always 0
**Cause:** Too many face cards in high slots  
**Solution:** Place face cards in low slots (1-3) to minimize penalties

### Issue: Can't unlock node
**Cause:** Prerequisites not met  
**Solution:** Check `node.prerequisites` and unlock those first

### Issue: Ability doesn't work
**Cause:** Not unlocked or invalid target  
**Solution:** Verify `progress.isUnlocked()` and validate target data

### Issue: Points not updating
**Cause:** Not calling `processMatchCompletion()`  
**Solution:** Call after determining match winner

---

## 🎯 Best Practices

### For Players
1. **Minimize penalties**: Place face cards in low slots
2. **Balance trees**: Don't ignore either SP or AP
3. **Plan ahead**: Check prerequisites before spending points
4. **Use abilities wisely**: Save for critical moments

### For Developers
1. **Always validate**: Use `unlockNode()` and `useAbility()` functions
2. **Emit events**: Let UI react to state changes
3. **Test edge cases**: Zero points, max penalties, etc.
4. **Provide feedback**: Show why unlocks fail

---

## 📚 API Reference

### Core Functions

```kotlin
// Calculate penalties for a hand
fun calculateCardPenalties(hand: List<CardState>): Pair<Int, List<CardPenalty>>

// Process match completion
fun processMatchCompletion(state: GCMSState, winnerId: String, matchNumber: Int): MatchResult

// Unlock a node
fun unlockNode(state: GCMSState, playerId: String, nodeId: String, pointType: PointType): UnlockResult

// Use an ability
fun useAbility(state: GCMSState, playerId: String, abilityId: String, targetData: Map<String, Any>?): AbilityResult

// Check if player has skill/ability
fun hasSkill(state: GCMSState, playerId: String, skillId: String): Boolean
fun hasAbility(state: GCMSState, playerId: String, abilityId: String): Boolean

// Get active skills/abilities
fun getActiveSkills(state: GCMSState, playerId: String): List<SkillNode>
fun getActiveAbilities(state: GCMSState, playerId: String): List<AbilityNode>
```

### Data Classes

```kotlin
data class MatchResult(
    val matchNumber: Int,
    val won: Boolean,
    val spEarned: Int,
    val apEarned: Int,
    val penalties: List<CardPenalty>
)

data class CardPenalty(
    val cardRank: String,
    val slotNumber: Int,
    val penalty: Int
)

data class PlayerProgress(
    val playerId: String,
    var totalSP: Int,
    var totalAP: Int,
    val unlockedSkills: MutableList<String>,
    val unlockedAbilities: MutableList<String>,
    val matchHistory: MutableList<MatchResult>
)

data class UnlockResult(
    val success: Boolean,
    val message: String,
    val nodeId: String?,
    val nodeName: String?,
    val pointsSpent: Int?
)

data class AbilityResult(
    val success: Boolean,
    val message: String,
    val abilityId: String?,
    val abilityName: String?
)
```

---

## 🚀 Quick Start Checklist

- [ ] Import skill/ability system files
- [ ] Add `skillAbilitySystem` to GCMSState
- [ ] Call `processMatchCompletion()` after match ends
- [ ] Display SP/AP in UI
- [ ] Create skill/ability tree UI
- [ ] Handle unlock commands
- [ ] Handle use ability commands
- [ ] Listen to and react to events
- [ ] Add unlock animations
- [ ] Add ability effect visuals
- [ ] Test with various penalty scenarios
- [ ] Add tutorial for players

---

**Ready to integrate? Start with match completion and point display!**