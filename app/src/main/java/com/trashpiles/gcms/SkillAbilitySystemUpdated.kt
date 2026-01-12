package com.trashpiles.gcms

/**
 * Complete Skills & Abilities System with Level 1-200 Progression
 * Updated with tier system and comprehensive skill/ability database
 */

// Import tier and category enums from SkillAbilityDatabase
// (These should be in the same package)

/**
 * Base class for all skill and ability nodes in the progression tree
 */
sealed class TreeNode(
    val id: String,
    val name: String,
    val cost: Int,
    val levelRequired: Int,
    val pointType: PointType,
    val description: String,
    val xpReward: Int = 0,
    val trophyId: String? = null,
    val prerequisites: List<String> = emptyList(),
    val tier: Tier = Tier.NEWBIE,
    val category: SkillCategory = SkillCategory.GENERAL_PROGRESSION
) {
    abstract fun isUnlocked(unlockedNodes: Set<String>): Boolean
    
    fun canUnlock(playerProgress: PlayerProgress, unlockedNodes: Set<String>): Boolean {
        if (isUnlocked(unlockedNodes)) return false
        if (playerProgress.level < levelRequired) return false
        
        // Check prerequisites
        if (prerequisites.isNotEmpty()) {
            if (!prerequisites.all { it in unlockedNodes }) return false
        }
        
        // Check point cost
        val points = when (pointType) {
            PointType.SKILL -> playerProgress.skillPoints
            PointType.ABILITY -> playerProgress.abilityPoints
            PointType.INVALID -> return false
        }
        
        return points >= cost
    }
}

/**
 * Skill node - passive abilities that provide permanent bonuses
 */
data class SkillNode(
    override val id: String,
    override val name: String,
    override val cost: Int,
    override val levelRequired: Int,
    override val tier: Tier,
    override val category: SkillCategory,
    override val description: String,
    val effect: SkillEffect,
    override val xpReward: Int = 0,
    override val trophyId: String? = null,
    override val prerequisites: List<String> = emptyList()
) : TreeNode(id, name, cost, levelRequired, PointType.SKILL, description, xpReward, trophyId, prerequisites, tier, category) {
    
    override fun isUnlocked(unlockedNodes: Set<String>): Boolean = id in unlockedNodes
}

/**
 * Ability node - active abilities that can be used during gameplay
 */
data class AbilityNode(
    override val id: String,
    override val name: String,
    override val cost: Int,
    override val levelRequired: Int,
    override val tier: Tier,
    override val category: SkillCategory,
    override val description: String,
    val usesPerMatch: Int,
    val usesPerRound: Int,
    val effect: AbilityEffect,
    override val xpReward: Int = 0,
    override val trophyId: String? = null,
    override val prerequisites: List<String> = emptyList()
) : TreeNode(id, name, cost, levelRequired, PointType.ABILITY, description, xpReward, trophyId, prerequisites, tier, category) {
    
    override fun isUnlocked(unlockedNodes: Set<String>): Boolean = id in unlockedNodes
}

/**
 * Point type for skills and abilities
 */
enum class PointType {
    SKILL,
    ABILITY,
    INVALID
}

/**
 * Player progress tracking for skill/ability system
 */
data class PlayerProgress(
    val skillPoints: Int = 0,
    val abilityPoints: Int = 0,
    val level: Int = 1,
    val xp: Int = 0,
    val hasPurchasedAny: Boolean = false,
    val accumulatedXP: Int = 0,
    val lostXP: Int = 0,
    val unlockedSkills: Set<String> = emptySet(),
    val unlockedAbilities: Set<String> = emptySet(),
    val abilityUsageCounts: Map<String, Int> = emptyMap()
) {
    fun recalculateLevel(): Int {
        if (!hasPurchasedAny) return 1
        
        val newLevel = kotlin.math.floor(kotlin.math.ln(xp.toDouble() + 1.0) / kotlin.math.ln(1.2)).toInt() + 1
        return newLevel
    }
    
    fun addXP(amount: Int, isPenalty: Boolean = false): PlayerProgress {
        val newLostXP = if (isPenalty) lostXP + amount else lostXP
        val newAccumulatedXP = if (!isPenalty) accumulatedXP + amount else accumulatedXP
        
        return copy(
            xp = xp + amount,
            lostXP = newLostXP,
            accumulatedXP = newAccumulatedXP,
            level = recalculateLevel()
        )
    }
    
    fun unlockNodeWithXP(nodeId: String, xpReward: Int): PlayerProgress {
        val isSkill = SkillAbilityDatabase.getNodeById(nodeId)?.pointType == PointType.SKILL
        
        val newProgress = copy(
            unlockedSkills = if (isSkill) unlockedSkills + nodeId else unlockedSkills,
            unlockedAbilities = if (!isSkill) unlockedAbilities + nodeId else unlockedAbilities,
            hasPurchasedAny = true,
            xp = xp + xpReward,
            accumulatedXP = accumulatedXP + xpReward,
            level = recalculateLevel()
        )
        
        return newProgress
    }
}

/**
 * Result of unlocking a node
 */
data class UnlockResult(
    val success: Boolean,
    val nodeId: String,
    val pointsRemaining: Int,
    val xpEarned: Int = 0,
    val newLevel: Int = 1,
    val errorMessage: String? = null
)

/**
 * Result of using an ability
 */
data class AbilityUseResult(
    val success: Boolean,
    val abilityId: String,
    val usesRemaining: Int,
    val errorMessage: String? = null
)

/**
 * Result of selling a node
 */
data class SellResult(
    val success: Boolean,
    val nodeId: String,
    val pointsRefunded: Int,
    val xPLost: Int,
    val newLevel: Int = 1,
    val errorMessage: String? = null
)

/**
 * Result of applying XP penalty
 */
data class PenaltyResult(
    val xPLost: Int,
    val newLevel: Int,
    val message: String
)

/**
 * Complete skill and ability system state
 */
data class SkillAbilitySystemState(
    val playerProgress: PlayerProgress = PlayerProgress(),
    val skillTree: SkillTree = SkillTree(),
    val abilityTree: AbilityTree = AbilityTree(),
    val activeEffects: List<ActiveEffect> = emptyList()
)

/**
 * Active effect during gameplay
 */
data class ActiveEffect(
    val nodeId: String,
    val effect: AbilityEffect,
    val duration: EffectDuration,
    val startTime: Long = System.currentTimeMillis()
)

/**
 * Effect duration
 */
sealed class EffectDuration {
    object Permanent : EffectDuration()
    object ThisRound : EffectDuration()
    object ThisMatch : EffectDuration()
    data class ThisTurn(val turnsRemaining: Int) : EffectDuration()
}

/**
 * Skill tree containing all available skills
 */
data class SkillTree(
    val skills: Map<String, SkillNode> = SkillAbilityDatabase.allSkillsAndAbilities
        .filterValues { it.pointType == PointType.SKILL }
        .mapValues { it.value as SkillNode }
)

/**
 * Ability tree containing all available abilities
 */
data class AbilityTree(
    val abilities: Map<String, AbilityNode> = SkillAbilityDatabase.allSkillsAndAbilities
        .filterValues { it.pointType == PointType.ABILITY }
        .mapValues { it.value as AbilityNode }
)

/**
 * Main Skill & Ability System object
 */
object SkillAbilitySystem {
    
    /**
     * Get all available skills for a given level
     */
    fun getAvailableSkills(level: Int): List<SkillNode> {
        return SkillAbilityDatabase.getNodesForLevel(level)
            .filter { it.pointType == PointType.SKILL }
            .map { it as SkillNode }
    }
    
    /**
     * Get all available abilities for a given level
     */
    fun getAvailableAbilities(level: Int): List<AbilityNode> {
        return SkillAbilityDatabase.getNodesForLevel(level)
            .filter { it.pointType == PointType.ABILITY }
            .map { it as AbilityNode }
    }
    
    /**
     * Get all unlocked skills
     */
    fun getUnlockedSkills(progress: PlayerProgress): List<SkillNode> {
        return progress.unlockedSkills.mapNotNull { id ->
            SkillAbilityDatabase.getNodeById(id) as? SkillNode
        }
    }
    
    /**
     * Get all unlocked abilities
     */
    fun getUnlockedAbilities(progress: PlayerProgress): List<AbilityNode> {
        return progress.unlockedAbilities.mapNotNull { id ->
            SkillAbilityDatabase.getNodeById(id) as? AbilityNode
        }
    }
    
    /**
     * Get active passive skill effects
     */
    fun getActivePassiveEffects(progress: PlayerProgress): List<SkillEffect> {
        return getUnlockedSkills(progress).map { it.effect }
    }
    
    /**
     * Check if player can use an ability
     */
    fun canUseAbility(abilityId: String, progress: PlayerProgress, currentMatch: Int = 0): AbilityUseResult {
        val ability = SkillAbilityDatabase.getNodeById(abilityId) as? AbilityNode
            ?: return AbilityUseResult(false, abilityId, 0, "Ability not found")
        
        if (abilityId !in progress.unlockedAbilities) {
            return AbilityUseResult(false, abilityId, 0, "Ability not unlocked")
        }
        
        val usesUsed = progress.abilityUsageCounts[abilityId] ?: 0
        val usesRemaining = ability.usesPerMatch - usesUsed
        
        if (usesRemaining <= 0) {
            return AbilityUseResult(false, abilityId, 0, "No uses remaining this match")
        }
        
        return AbilityUseResult(true, abilityId, usesRemaining, null)
    }
    
    /**
     * Apply skill effects to game state
     */
    fun applySkillEffects(state: GCMSState): GCMSState {
        val progress = state.skillAbilitySystem.playerProgress
        val passiveEffects = getActivePassiveEffects(progress)
        
        // Apply passive effects to state
        // This would be implemented based on specific effect types
        // For now, return state unchanged
        return state
    }
    
    /**
     * Get skill/ability statistics
     */
    fun getStatistics(progress: PlayerProgress): SkillAbilityStatistics {
        val unlockedSkills = getUnlockedSkills(progress)
        val unlockedAbilities = getUnlockedAbilities(progress)
        
        return SkillAbilityStatistics(
            totalSkillsUnlocked = unlockedSkills.size,
            totalAbilitiesUnlocked = unlockedAbilities.size,
            totalSkillPointsSpent = unlockedSkills.sumOf { it.cost },
            totalAbilityPointsSpent = unlockedAbilities.sumOf { it.cost },
            highestTierUnlocked = Tier.entries.minByOrNull { 
                unlockedSkills.any { s -> s.tier <= it } || unlockedAbilities.any { a -> a.tier <= it }
            } ?: Tier.NEWBIE,
            totalXPEarned = progress.accumulatedXP,
            currentLevel = progress.level
        )
    }
}

/**
 * Statistics for skill/ability progression
 */
data class SkillAbilityStatistics(
    val totalSkillsUnlocked: Int,
    val totalAbilitiesUnlocked: Int,
    val totalSkillPointsSpent: Int,
    val totalAbilityPointsSpent: Int,
    val highestTierUnlocked: Tier,
    val totalXPEarned: Int,
    val currentLevel: Int
)