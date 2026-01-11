# Dynamic Progression System - Implementation Plan

## Overview

This update implements a **self-regulating, dynamic progression system** where:

1. **Points (SP/AP)** → Buy abilities/skills on the tree
2. **Abilities/Skills → Grant XP** when purchased
3. **XP → Determines Level** (can increase OR decrease)
4. **Level → Can drop** if XP is lost
5. **Score → Generates Points** (but not XP directly)
6. **Skill Tree Releases → Enable higher levels** (dynamic ceiling)

## Key Changes Required

### 1. XP Only Starts After Purchase
**Current Behavior**: XP accumulates from match results immediately
**New Behavior**: XP = 0 until player buys at least one ability or skill

**Implementation**:
- Add `hasPurchasedAny` flag to `PlayerProgress`
- Set `totalXP = 0` until first purchase
- Only calculate XP after first node is unlocked

### 2. Abilities/Skills Grant XP on Purchase
**Current Behavior**: XP comes from matches only
**New Behavior**: Each purchased node grants immediate XP

**Implementation**:
- Add `xpReward` property to `TreeNode` base class
- When unlocking a node: `totalXP += node.xpReward`
- Recalculate level after XP change

### 3. XP Can Increase or Decrease Dynamically
**Current Behavior**: XP only increases
**New Behavior**: XP can decrease (selling, penalties, etc.)

**Implementation**:
- Remove "XP only increases" assumption
- Allow negative XP adjustments
- Level recalculates automatically when XP changes
- Example: 200 XP = Level 19, 300 XP = Level 20

### 4. Level Drops with XP Loss
**Current Behavior**: Level only increases
**New Behavior**: Level can drop if XP decreases below threshold

**Implementation**:
- `level = LevelSystem.calculateLevel(totalXP)` is already dynamic
- Just need to ensure it's called after every XP change
- Add `recalculateLevel()` helper method

### 5. Score Generates Points, Not XP
**Current Behavior**: Matches grant both points AND XP
**New Behavior**: Matches grant points only (until first purchase)

**Implementation**:
- Modify `processMatchCompletion()` to only grant SP/AP
- XP calculation happens separately after purchase
- Match history still tracks for penalties

### 6. Dynamic Level Ceiling (No Hard Cap)
**Current Behavior**: Level can grow indefinitely (already implemented)
**New Behavior**: Same, but skill tree releases enable progression

**Implementation**:
- Already implemented in current `LevelSystem`
- No changes needed
- Adding more nodes to trees automatically enables higher levels

### 7. Optional: XP Loss Penalty (Sliding Multiplier)
**Current Behavior**: Not implemented
**New Behavior**: Losing XP requires earning MORE to regain level

**Implementation**:
- Add `penaltyMultiplier` to `LevelSystem` config
- Apply when regaining lost levels
- Example: Lose 10 XP → Need 10 + (10 × 0.1) = 11 XP to regain

## Data Structure Changes

### PlayerProgress Updates

```kotlin
data class PlayerProgress(
    val playerId: String,
    var totalSP: Int = 0,
    var totalAP: Int = 0,
    var totalXP: Int = 0,
    var level: Int = 1,
    val unlockedSkills: MutableList<String> = mutableListOf(),
    val unlockedAbilities: MutableList<String> = mutableListOf(),
    val matchHistory: MutableList<MatchResult> = mutableListOf(),
    
    // NEW FIELDS
    var hasPurchasedAny: Boolean = false,  // Has unlocked any node?
    var accumulatedXP: Int = 0,            // Total XP ever earned (for penalty tracking)
    var lostXP: Int = 0                    // Total XP lost (for penalty calculation)
) {
    /**
     * Recalculate level based on current XP
     * Called after any XP change
     */
    fun recalculateLevel() {
        level = if (!hasPurchasedAny) {
            1  // Always level 1 until first purchase
        } else {
            LevelSystem.calculateLevel(totalXP)
        }
    }
    
    /**
     * Add XP with penalty tracking
     */
    fun addXP(amount: Int, isPenalty: Boolean = false) {
        if (!hasPurchasedAny) return  // No XP until first purchase
        
        totalXP += amount
        accumulatedXP += if (isPenalty) 0 else amount
        if (isPenalty) lostXP += -amount
        
        recalculateLevel()
    }
    
    /**
     * Unlock a node and grant XP reward
     */
    fun unlockNodeWithXP(nodeId: String, pointType: PointType, xpReward: Int) {
        // First purchase enables XP
        if (!hasPurchasedAny) {
            hasPurchasedAny = true
        }
        
        // Deduct points
        deductPoints(node.cost, pointType)
        
        // Add to unlocked
        unlockNode(nodeId, pointType)
        
        // Grant XP
        addXP(xpReward)
    }
}
```

### TreeNode Updates

```kotlin
sealed class TreeNode(
    open val id: String,
    open val name: String,
    open val description: String,
    open val cost: Int,
    open val prerequisites: List<String>,
    open val effect: NodeEffect,
    open val xpReward: Int = 0  // NEW: XP granted on purchase
)

// Example with XP rewards
SkillNode(
    id = "peek",
    name = "Peek",
    description = "Look at 1 of your face-down cards",
    cost = 3,
    xpReward = 10,  // Grants 10 XP when unlocked
    ...
)

AbilityNode(
    id = "jacks_favor",
    name = "Jack's Favor",
    description = "Reduce Jack penalty by 1",
    cost = 3,
    xpReward = 15,  // Grants 15 XP when unlocked
    ...
)
```

### LevelSystem Updates

```kotlin
object LevelSystem {
    data class LevelConfig(
        val baseXP: Int = 100,
        val xpMultiplier: Double = 1.2,
        val matchBonus: Int = 10,
        val roundBonus: Int = 50,
        val penaltyMultiplier: Double = 0.1  // NEW: 10% penalty on lost XP
    )
    
    private val config = LevelConfig()
    
    /**
     * Calculate level from XP (already dynamic)
     */
    fun calculateLevel(xp: Int): Int {
        if (xp <= config.baseXP) return 1
        
        val normalizedXP = xp.toDouble() + 1.0
        val level = (Math.log(normalizedXP) / Math.log(config.xpMultiplier)).toInt()
        
        return maxOf(1, level + 1)
    }
    
    /**
     * Get XP needed to regain a lost level
     * Includes penalty multiplier
     */
    fun getXPToRegainLevel(lostXP: Int): Int {
        val penalty = (lostXP * config.penaltyMultiplier).toInt()
        return lostXP + penalty
    }
    
    /**
     * Calculate match points (NOT XP)
     * XP only comes from unlocking nodes
     */
    fun calculateMatchPoints(
        spEarned: Int,
        apEarned: Int
    ): Pair<Int, Int> {
        return Pair(spEarned, apEarned)
    }
}
```

## Logic Changes

### unlockNode() Function

```kotlin
fun unlockNode(
    state: GCMSState,
    playerId: String,
    nodeId: String,
    pointType: PointType
): UnlockResult {
    val progress = state.skillAbilitySystem.getPlayerProgress(playerId)
    val node = when (pointType) {
        PointType.SKILL -> SkillTree.getNode(nodeId)
        PointType.ABILITY -> AbilityTree.getNode(nodeId)
    } ?: return UnlockResult(success = false, message = "Node not found")
    
    // Check level requirement
    val minLevel = when (pointType) {
        PointType.SKILL -> (node as? SkillNode)?.minLevel ?: 1
        PointType.ABILITY -> (node as? AbilityNode)?.minLevel ?: 1
    }
    
    if (progress.level < minLevel) {
        return UnlockResult(success = false, message = "Requires Level $minLevel")
    }
    
    // Check affordability
    if (!progress.canAfford(node.cost, pointType)) {
        return UnlockResult(success = false, message = "Insufficient points")
    }
    
    // Check prerequisites
    val unmetPrereqs = node.prerequisites.filter { 
        !progress.isUnlocked(it, pointType) 
    }
    
    if (unmetPrereqs.isNotEmpty()) {
        return UnlockResult(success = false, message = "Prerequisites not met")
    }
    
    // Unlock node and grant XP
    progress.unlockNodeWithXP(nodeId, pointType, node.xpReward)
    
    return UnlockResult(
        success = true,
        message = "Unlocked: ${node.name}",
        nodeId = nodeId,
        nodeName = node.name,
        pointsSpent = node.cost,
        xpEarned = node.xpReward,  // NEW
        newLevel = progress.level   // NEW
    )
}
```

### processMatchCompletion() Function

```kotlin
fun processMatchCompletion(
    state: GCMSState,
    winnerId: String,
    matchHistory: List<List<Card>>
): MatchResult {
    val winner = state.players.find { it.id.toString() == winnerId }
        ?: throw IllegalArgumentException("Winner not found")
    
    val progress = state.skillAbilitySystem.getPlayerProgress(winnerId)
    
    // Calculate AP penalties
    val totalAPPenalty = calculateMatchAPPenalties(matchHistory)
    
    // Get base points
    val currentRound = state.skillAbilitySystem.currentRound
    val matchInRound = state.skillAbilitySystem.matchInRound
    val (baseSP, baseAP) = MatchRewards.getBaseRewards(
        progress.level, currentRound, matchInRound
    )
    
    // Calculate points earned (NO XP from matches)
    val spEarned = baseSP
    val apEarned = maxOf(0, baseAP + totalAPPenalty)
    
    // Update SP/AP only
    progress.totalSP += spEarned
    progress.totalAP += apEarned
    
    // XP remains 0 until first purchase
    val xpEarned = 0
    
    // Create match result
    val result = MatchResult(
        matchNumber = matchInRound,
        roundNumber = currentRound,
        won = true,
        spEarned = spEarned,
        apEarned = apEarned,
        xpEarned = xpEarned,  // Always 0 until purchase
        penalties = calculatePenalties(matchHistory)
    )
    
    // Add to match history
    progress.matchHistory.add(result)
    
    // Advance match
    state.skillAbilitySystem.advanceMatch()
    
    return result
}
```

## XP Loss Scenarios

### Scenario 1: Selling an Ability

```kotlin
fun sellAbility(
    state: GCMSState,
    playerId: String,
    abilityId: String
): SellResult {
    val progress = state.skillAbilitySystem.getPlayerProgress(playerId)
    
    // Check if ability is unlocked
    if (!progress.isUnlocked(abilityId, PointType.ABILITY)) {
        return SellResult(success = false, message = "Ability not unlocked")
    }
    
    // Get ability node
    val ability = AbilityTree.getNode(abilityId)
        ?: return SellResult(success = false, message = "Ability not found")
    
    // Remove from unlocked
    progress.unlockedAbilities.remove(abilityId)
    
    // Lose XP (full amount granted)
    progress.addXP(-ability.xpReward, isPenalty = true)
    
    // Return partial points (e.g., 50%)
    val refund = (ability.cost * 0.5).toInt()
    when (abilityId) {
        // Ability node
        progress.totalAP += refund
        else -> {}
    }
    
    return SellResult(
        success = true,
        message = "Sold: ${ability.name}",
        xpLost = ability.xpReward,
        pointsRefunded = refund,
        newLevel = progress.level
    )
}
```

### Scenario 2: Penalties

```kotlin
fun applyXPPenalty(
    state: GCMSState,
    playerId: String,
    amount: Int
): PenaltyResult {
    val progress = state.skillAbilitySystem.getPlayerProgress(playerId)
    
    if (!progress.hasPurchasedAny) {
        return PenaltyResult(success = false, message = "No XP to lose")
    }
    
    // Apply penalty
    progress.addXP(-amount, isPenalty = true)
    
    // Calculate XP needed to regain level
    val needed = LevelSystem.getXPToRegainLevel(amount)
    
    return PenaltyResult(
        success = true,
        message = "Lost $amount XP",
        xpLost = amount,
        newLevel = progress.level,
        xpToRegainLevel = needed
    )
}
```

## Example Progression Flow

### Example 1: New Player

```
1. Player starts with Level 1, 0 SP, 0 AP, 0 XP
2. Plays match → Earns 10 SP, 10 AP (XP remains 0)
3. Unlocks "Peek" skill (costs 3 SP, grants 10 XP)
   - SP: 10 - 3 = 7
   - XP: 0 + 10 = 10
   - Level: 1 → 1 (still Level 1)
4. Plays more matches → Earns 20 SP, 20 AP
   - SP: 7 + 20 = 27
   - AP: 10 + 20 = 30
   - XP: 10 (no change)
5. Unlocks "Second Chance" skill (costs 4 SP, grants 15 XP)
   - SP: 27 - 4 = 23
   - XP: 10 + 15 = 25
   - Level: 1 → 2 (Level up!)
```

### Example 2: Losing XP

```
1. Player at Level 5 with 500 XP
2. Sells "Wild Card Master" ability (granted 100 XP)
   - XP: 500 - 100 = 400
   - Level: 5 → 4 (Level drops!)
3. To regain Level 5:
   - Normal: Need 100 XP
   - With penalty: Need 100 + (100 × 0.1) = 110 XP
```

## Implementation Steps

1. ✅ Update `PlayerProgress` data class
2. ✅ Add `xpReward` property to `TreeNode`
3. ✅ Update `LevelSystem` with penalty multiplier
4. ✅ Modify `unlockNode()` to grant XP
5. ✅ Modify `processMatchCompletion()` to NOT grant XP
6. ✅ Add `sellNode()` function (optional)
7. ✅ Add `applyXPPenalty()` function (optional)
8. ✅ Update tests to reflect new behavior
9. ✅ Update documentation

## Testing Requirements

- XP remains 0 until first purchase ✓
- Unlocking node grants XP immediately ✓
- Level recalculates when XP changes ✓
- Level drops when XP decreases ✓
- Score generates points, not XP ✓
- XP loss penalty works correctly ✓
- Dynamic level ceiling (unlimited) ✓

## Summary

This dynamic progression system creates a self-regulating ecosystem where:
- Players must spend points to start earning XP
- XP can increase or decrease
- Levels fluctuate with XP
- Skill tree releases enable higher levels
- No hard cap on progression

The system is natural, balanced, and provides clear progression paths while preventing abuse.