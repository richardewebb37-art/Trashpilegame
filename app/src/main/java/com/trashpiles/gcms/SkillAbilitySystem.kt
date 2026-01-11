package com.trashpiles.gcms

/**
 * Skill & Ability Points System
 * 
 * This system rewards players for winning matches with two types of points:
 * - Skill Points (SP): Earned by winning matches, NEVER penalized
 * - Ability Points (AP): Earned by winning matches, reduced by face-DOWN card penalties
 * 
 * SCORING SYSTEM:
 * - Round Score: Face-UP cards only (slot numbers + dice bonus)
 * - Match Score: Sum of 10 round scores (highest wins)
 * - SP/AP Rewards: Winner only
 * 
 * AP PENALTIES (face-DOWN cards across entire match):
 * - King: -3 AP
 * - Queen: -2 AP
 * - Jack: -1 AP
 * - Joker: -20 AP
 * 
 * Players use SP/AP to unlock nodes in skill and ability trees.
 */

/**
 * Represents a single match result in the progression system
 */
data class MatchResult(
    val matchNumber: Int,
    val roundNumber: Int,
    val won: Boolean,
    val spEarned: Int,
    val apEarned: Int,
    val xpEarned: Int,
    val penalties: List<CardPenalty>
)

/**
 * Represents a card score or penalty
 * 
 * For round scoring: positive values (points earned)
 * For AP penalties: negative values (penalties from face-DOWN cards)
 */
data class CardPenalty(
    val cardRank: String,  // "King", "Queen", "Jack", "Joker", or number
    val slotNumber: Int,   // 1-10
    val penalty: Int       // Can be positive (points) or negative (penalty)
)

/**
 * Result of round score calculation
 */
data class RoundScoreResult(
    val baseScore: Int,      // Sum of face-up card slot numbers
    val multiplier: Int,     // Winner: last card slot, Loser: face-up count
    val diceRoll: Int,       // Dice result (1-6)
    val bonus: Int,          // multiplier × diceRoll
    val finalScore: Int,     // baseScore + bonus
    val cardScores: List<CardPenalty>  // Individual card contributions
)

/**
 * Tracks a player's progression through the skill/ability system
 * 
 * DYNAMIC PROGRESSION SYSTEM:
 * - XP remains 0 until first ability/skill purchase
 * - XP can increase or decrease dynamically
 * - Level recalculates automatically when XP changes
 * - Level can drop if XP decreases below threshold
 */
data class PlayerProgress(
    val playerId: String,
    var totalSP: Int = 0,
    var totalAP: Int = 0,
    var totalXP: Int = 0,
    var level: Int = 1,
    val unlockedSkills: MutableList<String> = mutableListOf(),
    val unlockedAbilities: MutableList<String> = mutableListOf(),
    val matchHistory: MutableList<MatchResult> = mutableListOf(),
    
    // NEW: Dynamic progression fields
    var hasPurchasedAny: Boolean = false,  // Has unlocked any node? (XP starts after this)
    var accumulatedXP: Int = 0,            // Total XP ever earned (for penalty tracking)
    var lostXP: Int = 0                    // Total XP lost (for penalty calculation)
) {
    /**
     * Add a match result and update totals
     * Returns true if player leveled up
     * 
     * NOTE: In dynamic progression system, matches only grant SP/AP
     * XP is granted when unlocking abilities/skills
     */
    fun addMatchResult(result: MatchResult): Boolean {
        val oldLevel = level
        
        // Only grant SP/AP from matches (no XP until first purchase)
        totalSP += result.spEarned
        totalAP += result.apEarned
        
        matchHistory.add(result)
        
        // Level only changes if player has made a purchase
        if (hasPurchasedAny) {
            level = LevelSystem.calculateLevel(totalXP)
        }
        
        return level > oldLevel
    }
    
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
     * XP only accumulates after first purchase
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
     * First purchase enables XP accumulation
     */
    fun unlockNodeWithXP(nodeId: String, pointType: PointType, xpReward: Int) {
        // First purchase enables XP
        if (!hasPurchasedAny) {
            hasPurchasedAny = true
        }
        
        // Add to unlocked
        unlockNode(nodeId, pointType)
        
        // Grant XP
        addXP(xpReward)
    }
    
    /**
     * Check if player can afford a node
     */
    fun canAfford(cost: Int, pointType: PointType): Boolean {
        return when (pointType) {
            PointType.SKILL -> totalSP >= cost
            PointType.ABILITY -> totalAP >= cost
        }
    }
    
    /**
     * Deduct points for unlocking a node
     */
    fun deductPoints(cost: Int, pointType: PointType) {
        when (pointType) {
            PointType.SKILL -> totalSP -= cost
            PointType.ABILITY -> totalAP -= cost
        }
    }
    
    /**
     * Check if a node is already unlocked
     */
    fun isUnlocked(nodeId: String, pointType: PointType): Boolean {
        return when (pointType) {
            PointType.SKILL -> unlockedSkills.contains(nodeId)
            PointType.ABILITY -> unlockedAbilities.contains(nodeId)
        }
    }
    
    /**
     * Unlock a node
     */
    fun unlockNode(nodeId: String, pointType: PointType) {
        when (pointType) {
            PointType.SKILL -> unlockedSkills.add(nodeId)
            PointType.ABILITY -> unlockedAbilities.add(nodeId)
        }
    }
}

/**
 * Type of points (Skill or Ability)
 */
enum class PointType {
    SKILL,
    ABILITY
}

/**
 * Type of node effect
 */
enum class EffectType {
    PASSIVE,    // Always active
    ACTIVE,     // Must be activated
    TRIGGERED   // Activates on specific conditions
}

/**
 * Represents an effect that a skill/ability provides
 */
data class NodeEffect(
    val type: EffectType,
    val description: String,
    val value: Any? = null  // Can be Int, Float, Boolean, etc.
)

/**
 * Base class for skill/ability tree nodes
 * 
 * Each node grants XP when unlocked, enabling level progression
 */
sealed class TreeNode(
    open val id: String,
    open val name: String,
    open val description: String,
    open val cost: Int,
    open val prerequisites: List<String>,
    open val effect: NodeEffect,
    open val xpReward: Int = 0  // XP granted when this node is unlocked
)

/**
 * Skill tree node (uses SP)
 */
data class SkillNode(
    override val id: String,
    override val name: String,
    override val description: String,
    override val cost: Int,
    val minLevel: Int = 1,
    override val prerequisites: List<String> = emptyList(),
    override val effect: NodeEffect
) : TreeNode(id, name, description, cost, prerequisites, effect)

/**
 * Ability tree node (uses AP)
 */
data class AbilityNode(
    override val id: String,
    override val name: String,
    override val description: String,
    override val cost: Int,
    val minLevel: Int = 1,
    override val prerequisites: List<String> = emptyList(),
    override val effect: NodeEffect
) : TreeNode(id, name, description, cost, prerequisites, effect)

/**
 * Configuration for match rewards
 */
/**
 * Level system configuration - UNLIMITED PROGRESSION
 * 
 * Players can level up indefinitely based on:
 * - Total accumulated XP
 * - Number of matches played
 * - Number of rounds completed
 * 
 * Level calculation uses a progressive formula with no cap.
 */
object LevelSystem {
    /**
     * Configuration for unlimited level progression
     * 
     * DYNAMIC PROGRESSION:
     * - XP only comes from unlocking abilities/skills
     * - XP can increase or decrease
     * - Level recalculates dynamically
     * - Penalty multiplier applies when regaining lost levels
     */
    data class LevelConfig(
        val baseXP: Int = 100,            // XP threshold for level 2
        val xpMultiplier: Double = 1.2,   // Exponential growth factor
        val matchBonus: Int = 10,         // Match completion bonus (legacy)
        val roundBonus: Int = 50,         // Round completion bonus (legacy)
        val penaltyMultiplier: Double = 0.1  // 10% penalty when regaining lost XP
    )
    
    private val config = LevelConfig()
    
    /**
     * Calculate level from total XP using progressive formula
     * 
     * Formula: Level = floor(log(XP + 1) / log(multiplier)) + 1
     * 
     * This ensures unlimited progression with increasing difficulty.
     */
    fun calculateLevel(xp: Int): Int {
        if (xp <= config.baseXP) return 1
        
        // Using logarithmic formula for smooth progression
        // Level = floor(log(xp + 1) / log(multiplier)) + 1
        val normalizedXP = xp.toDouble() + 1.0
        val level = (Math.log(normalizedXP) / Math.log(config.xpMultiplier)).toInt()
        
        return maxOf(1, level + 1)
    }
    
    /**
     * Get XP required for a specific level
     * 
     * Formula: XP = Base × (Multiplier ^ (Level - 1))
     */
    fun getXPForLevel(level: Int): Int {
        if (level <= 1) return config.baseXP
        
        val xp = config.baseXP * Math.pow(config.xpMultiplier, (level - 1).toDouble())
        return xp.toInt()
    }
    
    /**
     * Get XP needed to reach next level
     */
    fun getXPToNextLevel(currentXP: Int, currentLevel: Int): Int {
        val nextLevelXP = getXPForLevel(currentLevel + 1)
        return maxOf(0, nextLevelXP - currentXP)
    }
    
    /**
     * Calculate XP earned from match completion
     * 
     * LEGACY FUNCTION - NO LONGER USED IN DYNAMIC PROGRESSION
     * 
     * In dynamic system, XP comes from unlocking abilities/skills, not matches.
     * This function is kept for backward compatibility.
     */
    fun calculateXP(
        spEarned: Int,
        apEarned: Int,
        currentLevel: Int,
        totalMatches: Int,
        totalRounds: Int
    ): Int {
        val baseXP = spEarned + apEarned
        val levelMultiplier = 1.0 + currentLevel * 0.05
        
        val pointsXP = (baseXP * levelMultiplier).toInt()
        val matchXP = totalMatches * config.matchBonus
        val roundXP = totalRounds * config.roundBonus
        
        return pointsXP + matchXP + roundXP
    }
    
    /**
     * Get XP needed to regain a lost level
     * Includes penalty multiplier to prevent rapid level cycling
     * 
     * Example: Lose 10 XP → Need 10 + (10 × 0.1) = 11 XP to regain
     */
    fun getXPToRegainLevel(lostXP: Int): Int {
        val penalty = (lostXP * config.penaltyMultiplier).toInt()
        return lostXP + penalty
    }
}

/**
 * Match rewards configuration - HYBRID SYSTEM
 * 
 * Level 1-3: Use old match table (1,1,2,2,3,3,4,4,5,5)
 * Level 4+: Use round-based system (10 × Round#)
 */
object MatchRewards {
    // Old system for Level 1-3
    private val oldRewards = mapOf(
        1 to Pair(1, 0),
        2 to Pair(1, 1),
        3 to Pair(2, 2),
        4 to Pair(2, 2),
        5 to Pair(3, 3),
        6 to Pair(3, 3),
        7 to Pair(4, 4),
        8 to Pair(4, 4),
        9 to Pair(5, 5),
        10 to Pair(5, 5)
    )
    
    /**
     * Get base SP/AP for a match
     * Uses hybrid system based on player level
     */
    fun getBaseRewards(playerLevel: Int, roundNumber: Int, matchInRound: Int): Pair<Int, Int> {
        return if (playerLevel <= 3) {
            // Level 1-3: Use old match table
            oldRewards[matchInRound] ?: Pair(0, 0)
        } else {
            // Level 4+: Use round-based system
            val value = 10 * roundNumber
            Pair(value, value)
        }
    }
    
    /**
     * Get base SP for a match (legacy compatibility)
     */
    fun getBaseSP(playerLevel: Int, roundNumber: Int, matchInRound: Int): Int {
        return getBaseRewards(playerLevel, roundNumber, matchInRound).first
    }
    
    /**
     * Get base AP for a match (legacy compatibility)
     */
    fun getBaseAP(playerLevel: Int, roundNumber: Int, matchInRound: Int): Int {
        return getBaseRewards(playerLevel, roundNumber, matchInRound).second
    }
    
    /**
     * Get total possible SP/AP for a round (Level 4+)
     */
    fun getTotalPossiblePerRound(roundNumber: Int): Int {
        return 10 * roundNumber * 10 // (10 × Round#) × 10 matches
    }
    
    /**
     * Get total possible for old system (Level 1-3)
     */
    fun getTotalPossibleOldSystem(): Pair<Int, Int> {
        val totalSP = oldRewards.values.sumOf { it.first }
        val totalAP = oldRewards.values.sumOf { it.second }
        return Pair(totalSP, totalAP)
    }
}

/**
 * AP penalty configuration for face-DOWN cards
 * 
 * These penalties apply ONLY to face-DOWN cards at the end of rounds.
 * Face-UP cards contribute to round score instead.
 */
object CardPenalties {
    private val apPenalties = mapOf(
        "King" to -3,
        "Queen" to -2,
        "Jack" to -1,
        "Joker" to -20
    )
    
    /**
     * Get AP penalty for a face-DOWN card
     */
    fun getAPPenalty(rank: String): Int {
        return apPenalties[rank] ?: 0
    }
    
    /**
     * Check if a card rank has an AP penalty
     */
    fun hasAPPenalty(rank: String): Boolean {
        return apPenalties.containsKey(rank)
    }
    
    /**
     * Get all penalty values
     */
    fun getAllPenalties(): Map<String, Int> {
        return apPenalties
    }
}

/**
 * Skill tree definition
 */
object SkillTree {
    val nodes = listOf(
        // Tier 1 - Level 1-3 Skills
        SkillNode(
            id = "peek",
            name = "Peek",
            description = "Look at 1 of your face-down cards",
            cost = 3,
            minLevel = 1,
            prerequisites = emptyList(),
            effect = NodeEffect(
                type = EffectType.ACTIVE,
                description = "Reveal 1 face-down card temporarily",
                value = 1
            ),
            xpReward = 10  // Grants 10 XP when unlocked
        ),
        SkillNode(
            id = "second_chance",
            name = "Second Chance",
            description = "Redraw from deck once per match",
            cost = 4,
            minLevel = 1,
            prerequisites = emptyList(),
            effect = NodeEffect(
                type = EffectType.ACTIVE,
                description = "Discard and draw new card",
                value = 1
            ),
            xpReward = 15
        ),
        SkillNode(
            id = "quick_draw",
            name = "Quick Draw",
            description = "Draw cards 20% faster",
            cost = 5,
            minLevel = 2,
            prerequisites = emptyList(),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "Reduce draw animation time",
                value = 0.2f
            ),
            xpReward = 15
        ),
        SkillNode(
            id = "card_memory",
            name = "Card Memory",
            description = "See the last 2 discarded cards",
            cost = 5,
            minLevel = 2,
            prerequisites = emptyList(),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "Display last 2 discarded cards",
                value = 2
            ),
            xpReward = 20
        ),
        SkillNode(
            id = "loaded_dice",
            name = "Loaded Dice",
            description = "+1 to all dice rolls",
            cost = 6,
            minLevel = 2,
            prerequisites = emptyList(),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "Add 1 to dice results",
                value = 1
            ),
            xpReward = 20
        ),
        SkillNode(
            id = "slot_vision",
            name = "Slot Vision",
            description = "See which slots are most valuable",
            cost = 7,
            minLevel = 3,
            prerequisites = listOf("card_memory"),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "Highlight optimal card placements",
                value = true
            ),
            xpReward = 25
        ),
        
        // Tier 2 - Level 4-6 Skills
        SkillNode(
            id = "card_swap",
            name = "Card Swap",
            description = "Swap 2 cards in your hand once per match",
            cost = 8,
            minLevel = 4,
            prerequisites = listOf("peek"),
            effect = NodeEffect(
                type = EffectType.ACTIVE,
                description = "Exchange positions of 2 cards",
                value = 2
            ),
            xpReward = 30
        ),
        SkillNode(
            id = "lucky_start",
            name = "Lucky Start",
            description = "Start with 2 cards face-up",
            cost = 10,
            minLevel = 4,
            prerequisites = listOf("second_chance"),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "Reveal 2 random cards at match start",
                value = 2
            ),
            xpReward = 40
        ),
        SkillNode(
            id = "speed_play",
            name = "Speed Play",
            description = "All animations 30% faster",
            cost = 10,
            minLevel = 5,
            prerequisites = listOf("quick_draw"),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "Reduce all animation times",
                value = 0.3f
            ),
            xpReward = 40
        ),
        SkillNode(
            id = "scavenge",
            name = "Scavenge",
            description = "Draw from discard pile even if empty",
            cost = 12,
            minLevel = 5,
            prerequisites = listOf("card_memory"),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "Access previously discarded cards",
                value = true
            ),
            xpReward = 45
        ),
        SkillNode(
            id = "dice_master",
            name = "Dice Master",
            description = "Reroll dice once per match",
            cost = 15,
            minLevel = 6,
            prerequisites = listOf("loaded_dice"),
            effect = NodeEffect(
                type = EffectType.ACTIVE,
                description = "Reroll your dice result",
                value = 1
            ),
            xpReward = 50
        ),
        
        // Tier 3 - Level 7-10 Skills
        SkillNode(
            id = "master_swap",
            name = "Master Swap",
            description = "Swap up to 3 cards in your hand",
            cost = 18,
            minLevel = 7,
            prerequisites = listOf("card_swap"),
            effect = NodeEffect(
                type = EffectType.ACTIVE,
                description = "Exchange positions of up to 3 cards",
                value = 3
            ),
            xpReward = 60
        ),
        SkillNode(
            id = "fortune_teller",
            name = "Fortune Teller",
            description = "See top 3 cards of deck",
            cost = 20,
            minLevel = 7,
            prerequisites = listOf("slot_vision"),
            effect = NodeEffect(
                type = EffectType.ACTIVE,
                description = "Preview upcoming draws",
                value = 3
            ),
            xpReward = 65
        ),
        SkillNode(
            id = "wild_card_master",
            name = "Wild Card Master",
            description = "All face cards act as wilds",
            cost = 25,
            minLevel = 8,
            prerequisites = listOf("master_swap"),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "Kings, Queens, Jacks can go anywhere",
                value = true
            ),
            xpReward = 75
        ),
        SkillNode(
            id = "perfect_roll",
            name = "Perfect Roll",
            description = "Choose your dice result once per match",
            cost = 30,
            minLevel = 9,
            prerequisites = listOf("dice_master"),
            effect = NodeEffect(
                type = EffectType.ACTIVE,
                description = "Set dice to any value (1-6)",
                value = 6
            ),
            xpReward = 100
        )
    )
    
    /**
     * Get a node by ID
     */
    fun getNode(nodeId: String): SkillNode? {
        return nodes.find { it.id == nodeId }
    }
    
    /**
     * Get all nodes that a player can unlock
     */
    fun getAvailableNodes(progress: PlayerProgress): List<SkillNode> {
        return nodes.filter { node ->
            !progress.isUnlocked(node.id, PointType.SKILL) &&
            progress.canAfford(node.cost, PointType.SKILL) &&
            node.prerequisites.all { prereq -> 
                progress.isUnlocked(prereq, PointType.SKILL) 
            }
        }
    }
}

/**
 * Ability tree definition
 */
object AbilityTree {
    val nodes = listOf(
        // Tier 1 - Level 1-3 Abilities
        AbilityNode(
            id = "jacks_favor",
            name = "Jack's Favor",
            description = "Reduce Jack penalty by 1",
            cost = 3,
            minLevel = 1,
            prerequisites = emptyList(),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "Jack penalty becomes 0 instead of -1",
                value = 1
            ),
            xpReward = 15
        ),
        AbilityNode(
            id = "queens_grace",
            name = "Queen's Grace",
            description = "Reduce Queen penalty by 1",
            cost = 4,
            minLevel = 2,
            prerequisites = emptyList(),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "Queen penalty becomes -1 instead of -2",
                value = 1
            ),
            xpReward = 20
        ),
        AbilityNode(
            id = "kings_mercy",
            name = "King's Mercy",
            description = "Reduce King penalty by 1",
            cost = 5,
            minLevel = 2,
            prerequisites = emptyList(),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "King penalty becomes -2 instead of -3",
                value = 1
            ),
            xpReward = 20
        ),
        AbilityNode(
            id = "royal_pardon",
            name = "Royal Pardon",
            description = "Ignore all face card penalties once per match",
            cost = 8,
            minLevel = 3,
            prerequisites = listOf("jacks_favor", "queens_grace", "kings_mercy"),
            effect = NodeEffect(
                type = EffectType.ACTIVE,
                description = "One match with no K/Q/J penalties",
                value = 1
            ),
            xpReward = 30
        ),
        
        // Tier 2 - Level 4-6 Abilities
        AbilityNode(
            id = "royal_shield",
            name = "Royal Shield",
            description = "Reduce all face card penalties by 1",
            cost = 10,
            minLevel = 4,
            prerequisites = listOf("royal_pardon"),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "K:-2, Q:-1, J:0",
                value = 1
            ),
            xpReward = 40
        ),
        AbilityNode(
            id = "jokers_escape",
            name = "Joker's Escape",
            description = "Avoid Joker penalty once",
            cost = 12,
            minLevel = 4,
            prerequisites = emptyList(),
            effect = NodeEffect(
                type = EffectType.ACTIVE,
                description = "One-time Joker penalty immunity",
                value = 1
            ),
            xpReward = 45
        ),
        AbilityNode(
            id = "jokers_bargain",
            name = "Joker's Bargain",
            description = "Reduce Joker penalty to -10",
            cost = 15,
            minLevel = 5,
            prerequisites = listOf("jokers_escape"),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "Joker penalty becomes -10 instead of -20",
                value = 10
            ),
            xpReward = 50
        ),
        
        // Tier 3 - Level 7-10 Abilities
        AbilityNode(
            id = "face_card_immunity",
            name = "Face Card Immunity",
            description = "No penalties from K, Q, J",
            cost = 25,
            minLevel = 7,
            prerequisites = listOf("royal_shield"),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "All face card penalties = 0",
                value = true
            ),
            xpReward = 80
        ),
        AbilityNode(
            id = "jokers_ally",
            name = "Joker's Ally",
            description = "Joker gives +10 AP instead of -20",
            cost = 30,
            minLevel = 8,
            prerequisites = listOf("jokers_bargain"),
            effect = NodeEffect(
                type = EffectType.PASSIVE,
                description = "Joker becomes beneficial",
                value = 30
            ),
            xpReward = 100
        )
    )
    
    /**
     * Get a node by ID
     */
    fun getNode(nodeId: String): AbilityNode? {
        return nodes.find { it.id == nodeId }
    }
    
    /**
     * Get all nodes that a player can unlock
     */
    fun getAvailableNodes(progress: PlayerProgress): List<AbilityNode> {
        return nodes.filter { node ->
            !progress.isUnlocked(node.id, PointType.ABILITY) &&
            progress.canAfford(node.cost, PointType.ABILITY) &&
            node.prerequisites.all { prereq -> 
                progress.isUnlocked(prereq, PointType.ABILITY) 
            }
        }
    }
}

/**
 * Main skill/ability system state
 */
data class SkillAbilitySystemState(
    var currentRound: Int = 1,
    var matchInRound: Int = 1,
    val playerProgress: MutableMap<String, PlayerProgress> = mutableMapOf()
) {
    /**
     * Get or create player progress
     */
    fun getPlayerProgress(playerId: String): PlayerProgress {
        return playerProgress.getOrPut(playerId) { PlayerProgress(playerId) }
    }
    
    /**
     * Check if round is complete (10 matches played)
     */
    fun isRoundComplete(): Boolean {
        return matchInRound > 10
    }
    
    /**
     * Advance to next match
     */
    fun advanceMatch() {
        matchInRound++
        if (matchInRound > 10) {
            currentRound++
            matchInRound = 1
        }
    }
    
    /**
     * Reset for new round
     */
    fun resetRound() {
        matchInRound = 1
        // Note: Player progress (SP/AP/XP/Level/unlocks) persists
    }
}