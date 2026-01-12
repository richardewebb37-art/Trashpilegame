package com.trashpiles.gcms

/**
 * Trophy System
 * 
 * Rewards players with trophies on every level-up based on:
 * - Current tier
 * - Points accumulated (SP/AP)
 * - Abilities unlocked
 * - Skills unlocked
 * - Combinations of the above
 * 
 * Trophy allocation is randomized within tier-based ranges,
 * and certain trophies have specific prerequisites.
 */

/**
 * Represents a single trophy that can be awarded
 */
data class Trophy(
    val id: String,
    val name: String,
    val description: String,
    val rarity: TrophyRarity,
    val tier: Tier,
    val icon: String? = null,
    val xpReward: Int = 0,  // Optional XP bonus
    val pointReward: Int = 0,  // Optional point bonus (SP or AP)
    val pointType: PointType? = null  // null = split evenly between SP and AP
)

/**
 * Trophy rarity levels
 */
enum class TrophyRarity(val displayName: String, val color: String) {
    COMMON("Common", "#CCCCCC"),
    UNCOMMON("Uncommon", "#1EFF00"),
    RARE("Rare", "#0070DD"),
    EPIC("Epic", "#A335EE"),
    LEGENDARY("Legendary", "#FF8000")
}

/**
 * Tier definition for trophy system
 */
enum class Tier(val displayName: String, val levelRange: IntRange, val trophyRange: IntRange) {
    LIFE("Life", 1..5, 1..3),
    BEGINNER("Beginner", 6..20, 2..5),
    NOVICE("Novice", 21..50, 3..7),
    HARD("Hard", 51..80, 5..10),
    EXPERT("Expert", 81..140, 8..15),
    MASTER("Master", 141..200, 10..25)
}

/**
 * Trophy prerequisite definition
 * 
 * Trophies can require one or more of:
 * - Specific level
 * - Minimum total points (SP + AP)
 * - Specific abilities unlocked
 * - Specific skills unlocked
 * - Combinations of the above
 */
data class TrophyPrerequisite(
    val requiredLevel: Int? = null,
    val minTotalPoints: Int? = null,
    val minSP: Int? = null,
    val minAP: Int? = null,
    val requiredAbilities: List<String> = emptyList(),
    val requiredSkills: List<String> = emptyList(),
    val minAbilitiesUnlocked: Int? = null,
    val minSkillsUnlocked: Int? = null
) {
    /**
     * Check if player meets all prerequisites
     */
    fun meetsRequirements(progress: PlayerProgress): Boolean {
        // Check level requirement
        if (requiredLevel != null && progress.level < requiredLevel) {
            return false
        }
        
        // Check points requirements
        val totalPoints = progress.totalSP + progress.totalAP
        if (minTotalPoints != null && totalPoints < minTotalPoints) {
            return false
        }
        
        if (minSP != null && progress.totalSP < minSP) {
            return false
        }
        
        if (minAP != null && progress.totalAP < minAP) {
            return false
        }
        
        // Check specific abilities
        val missingAbilities = requiredAbilities.filter { ability ->
            !progress.isUnlocked(ability, PointType.ABILITY)
        }
        if (missingAbilities.isNotEmpty()) {
            return false
        }
        
        // Check specific skills
        val missingSkills = requiredSkills.filter { skill ->
            !progress.isUnlocked(skill, PointType.SKILL)
        }
        if (missingSkills.isNotEmpty()) {
            return false
        }
        
        // Check minimum counts
        if (minAbilitiesUnlocked != null && progress.unlockedAbilities.size < minAbilitiesUnlocked) {
            return false
        }
        
        if (minSkillsUnlocked != null && progress.unlockedSkills.size < minSkillsUnlocked) {
            return false
        }
        
        return true
    }
}

/**
 * Trophy definition with prerequisites
 */
data class TrophyDefinition(
    val trophy: Trophy,
    val prerequisites: TrophyPrerequisite
)

/**
 * Result of checking trophy eligibility
 */
data class TrophyEligibilityResult(
    val trophy: Trophy,
    val eligible: Boolean,
    val missingRequirements: List<String>
)

/**
 * Trophy reward result
 */
data class TrophyRewardResult(
    val level: Int,
    val trophiesAwarded: List<Trophy>,
    val totalXPBonus: Int,
    val totalPointBonus: Int,
    val newTrophies: Int,
    val duplicateTrophies: Int
)

/**
 * Main Trophy System
 * 
 * Manages trophy definitions, eligibility checking, and reward distribution
 */
object TrophySystem {
    
    // All trophy definitions
    private val trophyDefinitions: MutableList<TrophyDefinition> = mutableListOf()
    
    // Player trophy collections
    private val playerTrophies: MutableMap<String, MutableSet<String>> = mutableMapOf()
    
    init {
        initializeTrophies()
    }
    
    /**
     * Initialize all trophy definitions
     * Called on system startup
     */
    fun initializeTrophies() {
        trophyDefinitions.clear()
        
        // Tier 1: Life (Level 1-5) - Basic progression
        addTrophyDefinition(
            Trophy(
                id = "first_steps",
                name = "First Steps",
                description = "Reach Level 2",
                rarity = TrophyRarity.COMMON,
                tier = Tier.LIFE,
                xpReward = 10
            ),
            TrophyPrerequisite(requiredLevel = 2)
        )
        
        addTrophyDefinition(
            Trophy(
                id = "card_handler",
                name = "Card Handler",
                description = "Unlock your first ability",
                rarity = TrophyRarity.COMMON,
                tier = Tier.LIFE,
                xpReward = 15
            ),
            TrophyPrerequisite(minAbilitiesUnlocked = 1)
        )
        
        addTrophyDefinition(
            Trophy(
                id = "early_adopter",
                name = "Early Adopter",
                description = "Unlock your first skill",
                rarity = TrophyRarity.COMMON,
                tier = Tier.LIFE,
                xpReward = 20
            ),
            TrophyPrerequisite(minSkillsUnlocked = 1)
        )
        
        addTrophyDefinition(
            Trophy(
                id = "life_complete",
                name = "Life Complete",
                description = "Reach Level 5 and unlock 2 abilities",
                rarity = TrophyRarity.UNCOMMON,
                tier = Tier.LIFE,
                xpReward = 50
            ),
            TrophyPrerequisite(
                requiredLevel = 5,
                minAbilitiesUnlocked = 2
            )
        )
        
        // Tier 2: Beginner (Level 6-20) - Foundation building
        addTrophyDefinition(
            Trophy(
                id = "dice_enthusiast",
                name = "Dice Enthusiast",
                description = "Unlock 3 abilities in the Beginner tier",
                rarity = TrophyRarity.COMMON,
                tier = Tier.BEGINNER,
                xpReward = 30
            ),
            TrophyPrerequisite(
                requiredLevel = 6,
                minAbilitiesUnlocked = 3
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "point_collector",
                name = "Point Collector",
                description = "Accumulate 50 total points",
                rarity = TrophyRarity.COMMON,
                tier = Tier.BEGINNER,
                xpReward = 35
            ),
            TrophyPrerequisite(
                minTotalPoints = 50
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "skill_seeker",
                name = "Skill Seeker",
                description = "Unlock 3 different skills",
                rarity = TrophyRarity.UNCOMMON,
                tier = Tier.BEGINNER,
                xpReward = 40
            ),
            TrophyPrerequisite(
                requiredLevel = 10,
                minSkillsUnlocked = 3
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "jack_of_all_trades",
                name = "Jack of All Trades",
                description = "Unlock 5 abilities across different skills",
                rarity = TrophyRarity.UNCOMMON,
                tier = Tier.BEGINNER,
                xpReward = 50
            ),
            TrophyPrerequisite(
                requiredLevel = 15,
                minAbilitiesUnlocked = 5,
                minSkillsUnlocked = 2
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "beginner_master",
                name = "Beginner Master",
                description = "Reach Level 20 with 10 abilities unlocked",
                rarity = TrophyRarity.RARE,
                tier = Tier.BEGINNER,
                xpReward = 100,
                pointReward = 20,
                pointType = PointType.ABILITY
            ),
            TrophyPrerequisite(
                requiredLevel = 20,
                minAbilitiesUnlocked = 10
            )
        )
        
        // Tier 3: Novice (Level 21-50) - Intermediate play
        addTrophyDefinition(
            Trophy(
                id = "combo_starter",
                name = "Combo Starter",
                description = "Unlock 2 abilities from the same skill",
                rarity = TrophyRarity.COMMON,
                tier = Tier.NOVICE,
                xpReward = 45
            ),
            TrophyPrerequisite(
                requiredLevel = 21,
                minAbilitiesUnlocked = 2
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "strategy_developer",
                name = "Strategy Developer",
                description = "Unlock 5 skills",
                rarity = TrophyRarity.UNCOMMON,
                tier = Tier.NOVICE,
                xpReward = 60
            ),
            TrophyPrerequisite(
                requiredLevel = 30,
                minSkillsUnlocked = 5
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "ability_collector",
                name = "Ability Collector",
                description = "Unlock 15 abilities",
                rarity = TrophyRarity.UNCOMMON,
                tier = Tier.NOVICE,
                xpReward = 70
            ),
            TrophyPrerequisite(
                requiredLevel = 35,
                minAbilitiesUnlocked = 15
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "point_hoarder",
                name = "Point Hoarder",
                description = "Accumulate 200 total points",
                rarity = TrophyRarity.RARE,
                tier = Tier.NOVICE,
                xpReward = 80,
                pointReward = 30
            ),
            TrophyPrerequisite(
                requiredLevel = 40,
                minTotalPoints = 200
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "novice_complete",
                name = "Novice Complete",
                description = "Reach Level 50 with 25 abilities and 8 skills",
                rarity = TrophyRarity.EPIC,
                tier = Tier.NOVICE,
                xpReward = 150,
                pointReward = 50
            ),
            TrophyPrerequisite(
                requiredLevel = 50,
                minAbilitiesUnlocked = 25,
                minSkillsUnlocked = 8
            )
        )
        
        // Tier 4: Hard (Level 51-80) - Advanced gameplay
        addTrophyDefinition(
            Trophy(
                id = "skill_master",
                name = "Skill Master",
                description = "Unlock all abilities in one skill",
                rarity = TrophyRarity.UNCOMMON,
                tier = Tier.HARD,
                xpReward = 100
            ),
            TrophyPrerequisite(
                requiredLevel = 51,
                minSkillsUnlocked = 3
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "dedicated_player",
                name = "Dedicated Player",
                description = "Unlock 30 abilities",
                rarity = TrophyRarity.RARE,
                tier = Tier.HARD,
                xpReward = 120
            ),
            TrophyPrerequisite(
                requiredLevel = 60,
                minAbilitiesUnlocked = 30
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "wealthy_player",
                name = "Wealthy Player",
                description = "Accumulate 500 total points",
                rarity = TrophyRarity.RARE,
                tier = Tier.HARD,
                xpReward = 130,
                pointReward = 50
            ),
            TrophyPrerequisite(
                requiredLevel = 65,
                minTotalPoints = 500
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "hard_complete",
                name = "Hard Complete",
                description = "Reach Level 80 with 50 abilities and 10 skills",
                rarity = TrophyRarity.EPIC,
                tier = Tier.HARD,
                xpReward = 250,
                pointReward = 100
            ),
            TrophyPrerequisite(
                requiredLevel = 80,
                minAbilitiesUnlocked = 50,
                minSkillsUnlocked = 10
            )
        )
        
        // Tier 5: Expert (Level 81-140) - Mastery territory
        addTrophyDefinition(
            Trophy(
                id = "expert_learner",
                name = "Expert Learner",
                description = "Unlock 12 skills",
                rarity = TrophyRarity.RARE,
                tier = Tier.EXPERT,
                xpReward = 200
            ),
            TrophyPrerequisite(
                requiredLevel = 81,
                minSkillsUnlocked = 12
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "ability_specialist",
                name = "Ability Specialist",
                description = "Unlock 75 abilities",
                rarity = TrophyRarity.EPIC,
                tier = Tier.EXPERT,
                xpReward = 250
            ),
            TrophyPrerequisite(
                requiredLevel = 100,
                minAbilitiesUnlocked = 75
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "point_magnate",
                name = "Point Magnate",
                description = "Accumulate 1,000 total points",
                rarity = TrophyRarity.EPIC,
                tier = Tier.EXPERT,
                xpReward = 300,
                pointReward = 100
            ),
            TrophyPrerequisite(
                requiredLevel = 110,
                minTotalPoints = 1000
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "expert_complete",
                name = "Expert Complete",
                description = "Reach Level 140 with 120 abilities and 15 skills",
                rarity = TrophyRarity.LEGENDARY,
                tier = Tier.EXPERT,
                xpReward = 500,
                pointReward = 200
            ),
            TrophyPrerequisite(
                requiredLevel = 140,
                minAbilitiesUnlocked = 120,
                minSkillsUnlocked = 15
            )
        )
        
        // Tier 6: Master (Level 141-200) - God-tier gameplay
        addTrophyDefinition(
            Trophy(
                id = "ultimate_collector",
                name = "Ultimate Collector",
                description = "Unlock 200 abilities",
                rarity = TrophyRarity.LEGENDARY,
                tier = Tier.MASTER,
                xpReward = 400
            ),
            TrophyPrerequisite(
                requiredLevel = 141,
                minAbilitiesUnlocked = 200
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "skill_master",
                name = "Skill Master",
                description = "Unlock all skills",
                rarity = TrophyRarity.LEGENDARY,
                tier = Tier.MASTER,
                xpReward = 500
            ),
            TrophyPrerequisite(
                requiredLevel = 160,
                minSkillsUnlocked = 20
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "point_legend",
                name = "Point Legend",
                description = "Accumulate 2,500 total points",
                rarity = TrophyRarity.LEGENDARY,
                tier = Tier.MASTER,
                xpReward = 600,
                pointReward = 250
            ),
            TrophyPrerequisite(
                requiredLevel = 170,
                minTotalPoints = 2500
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "master_complete",
                name = "Master Complete",
                description = "Reach Level 200 with maximum abilities and skills",
                rarity = TrophyRarity.LEGENDARY,
                tier = Tier.MASTER,
                xpReward = 1000,
                pointReward = 500
            ),
            TrophyPrerequisite(
                requiredLevel = 200,
                minAbilitiesUnlocked = 250,
                minSkillsUnlocked = 25
            )
        )
        
        addTrophyDefinition(
            Trophy(
                id = "trash_piles_legend",
                name = "Trash Piles Legend",
                description = "Reach Level 200 and unlock everything",
                rarity = TrophyRarity.LEGENDARY,
                tier = Tier.MASTER,
                xpReward = 2000,
                pointReward = 1000
            ),
            TrophyPrerequisite(
                requiredLevel = 200,
                minAbilitiesUnlocked = 300,
                minSkillsUnlocked = 30,
                minTotalPoints = 5000
            )
        )
    }
    
    /**
     * Add a trophy definition to the system
     */
    private fun addTrophyDefinition(trophy: Trophy, prerequisites: TrophyPrerequisite) {
        trophyDefinitions.add(TrophyDefinition(trophy, prerequisites))
    }
    
    /**
     * Get the tier for a given level
     */
    fun getTierForLevel(level: Int): Tier {
        return Tier.values().find { level in it.levelRange } ?: Tier.MASTER
    }
    
    /**
     * Get trophy range for a given level
     */
    fun getTrophyRangeForLevel(level: Int): IntRange {
        return getTierForLevel(level).trophyRange
    }
    
    /**
     * Get all trophy definitions
     */
    fun getAllTrophyDefinitions(): List<TrophyDefinition> {
        return trophyDefinitions.toList()
    }
    
    /**
     * Get trophy definition by ID
     */
    fun getTrophyDefinition(trophyId: String): TrophyDefinition? {
        return trophyDefinitions.find { it.trophy.id == trophyId }
    }
    
    /**
     * Check if a player is eligible for a specific trophy
     */
    fun isEligibleForTrophy(playerId: String, trophyId: String): Boolean {
        val def = getTrophyDefinition(trophyId) ?: return false
        
        // Check if player already has this trophy
        if (hasTrophy(playerId, trophyId)) {
            return false
        }
        
        // Check prerequisites
        // (Would need player progress - implement based on your game state)
        return true
    }
    
    /**
     * Check trophy eligibility for a player
     */
    fun checkEligibility(playerId: String, progress: PlayerProgress): List<TrophyEligibilityResult> {
        val results = mutableListOf<TrophyEligibilityResult>()
        
        trophyDefinitions.forEach { def ->
            val playerHasTrophy = hasTrophy(playerId, def.trophy.id)
            val meetsPrereqs = def.prerequisites.meetsRequirements(progress)
            
            if (!playerHasTrophy) {
                if (meetsPrereqs) {
                    results.add(TrophyEligibilityResult(
                        trophy = def.trophy,
                        eligible = true,
                        missingRequirements = emptyList()
                    ))
                } else {
                    // Calculate missing requirements
                    val missing = calculateMissingRequirements(def.prerequisites, progress)
                    results.add(TrophyEligibilityResult(
                        trophy = def.trophy,
                        eligible = false,
                        missingRequirements = missing
                    ))
                }
            }
        }
        
        return results
    }
    
    /**
     * Calculate missing requirements for a trophy
     */
    private fun calculateMissingRequirements(prereqs: TrophyPrerequisite, progress: PlayerProgress): List<String> {
        val missing = mutableListOf<String>()
        
        if (prereqs.requiredLevel != null && progress.level < prereqs.requiredLevel) {
            missing.add("Level ${prereqs.requiredLevel} (current: ${progress.level})")
        }
        
        if (prereqs.minTotalPoints != null) {
            val totalPoints = progress.totalSP + progress.totalAP
            if (totalPoints < prereqs.minTotalPoints) {
                missing.add("${prereqs.minTotalPoints} total points (current: $totalPoints)")
            }
        }
        
        if (prereqs.minSP != null && progress.totalSP < prereqs.minSP) {
            missing.add("${prereqs.minSP} SP (current: ${progress.totalSP})")
        }
        
        if (prereqs.minAP != null && progress.totalAP < prereqs.minAP) {
            missing.add("${prereqs.minAP} AP (current: ${progress.totalAP})")
        }
        
        if (prereqs.minAbilitiesUnlocked != null && progress.unlockedAbilities.size < prereqs.minAbilitiesUnlocked) {
            missing.add("${prereqs.minAbilitiesUnlocked} abilities (current: ${progress.unlockedAbilities.size})")
        }
        
        if (prereqs.minSkillsUnlocked != null && progress.unlockedSkills.size < prereqs.minSkillsUnlocked) {
            missing.add("${prereqs.minSkillsUnlocked} skills (current: ${progress.unlockedSkills.size})")
        }
        
        return missing
    }
    
    /**
     * Award trophies on level up
     * 
     * @param playerId ID of the player
     * @param progress Player progress data
     * @param newLevel The level the player just reached
     * @return TrophyRewardResult with all awarded trophies and bonuses
     */
    fun awardTrophiesOnLevelUp(playerId: String, progress: PlayerProgress, newLevel: Int): TrophyRewardResult {
        val tier = getTierForLevel(newLevel)
        val trophyRange = tier.trophyRange
        
        // Randomize number of trophies to award
        val numTrophies = trophyRange.random()
        
        // Get eligible trophies
        val eligibleTrophies = checkEligibility(playerId, progress)
            .filter { it.eligible }
            .map { it.trophy }
        
        // Select trophies to award
        val awardedTrophies = mutableListOf<Trophy>()
        val newTrophies = mutableListOf<Trophy>()
        val duplicateTrophies = mutableListOf<Trophy>()
        
        // Award random trophies from eligible pool
        repeat(min(numTrophies, eligibleTrophies.size)) {
            if (eligibleTrophies.isNotEmpty()) {
                val randomIndex = (Math.random() * eligibleTrophies.size).toInt()
                val trophy = eligibleTrophies.removeAt(randomIndex)
                
                if (!hasTrophy(playerId, trophy.id)) {
                    newTrophies.add(trophy)
                    addTrophyToPlayer(playerId, trophy.id)
                } else {
                    duplicateTrophies.add(trophy)
                }
                
                awardedTrophies.add(trophy)
            }
        }
        
        // Calculate bonuses
        val totalXPBonus = awardedTrophies.sumOf { it.xpReward }
        val totalPointBonus = awardedTrophies.sumOf { it.pointReward }
        
        // Apply point rewards
        if (totalPointBonus > 0) {
            applyPointRewards(playerId, awardedTrophies)
        }
        
        return TrophyRewardResult(
            level = newLevel,
            trophiesAwarded = awardedTrophies,
            totalXPBonus = totalXPBonus,
            totalPointBonus = totalPointBonus,
            newTrophies = newTrophies.size,
            duplicateTrophies = duplicateTrophies.size
        )
    }
    
    /**
     * Check if player has a specific trophy
     */
    fun hasTrophy(playerId: String, trophyId: String): Boolean {
        return playerTrophies[playerId]?.contains(trophyId) ?: false
    }
    
    /**
     * Get all trophies for a player
     */
    fun getPlayerTrophies(playerId: String): List<Trophy> {
        val trophyIds = playerTrophies[playerId] ?: return emptyList()
        return trophyIds.mapNotNull { id -> getTrophyDefinition(id)?.trophy }
    }
    
    /**
     * Add trophy to player's collection
     */
    private fun addTrophyToPlayer(playerId: String, trophyId: String) {
        playerTrophies.getOrPut(playerId) { mutableSetOf() }.add(trophyId)
    }
    
    /**
     * Remove trophy from player's collection (if implementing sell/refund)
     */
    fun removeTrophyFromPlayer(playerId: String, trophyId: String) {
        playerTrophies[playerId]?.remove(trophyId)
    }
    
    /**
     * Apply point rewards from trophies
     */
    private fun applyPointRewards(playerId: String, trophies: List<Trophy>) {
        trophies.forEach { trophy ->
            if (trophy.pointReward > 0 && trophy.pointType != null) {
                // (Would need to update player progress - implement based on your game state)
                // progress.addPoints(trophy.pointReward, trophy.pointType)
            }
        }
    }
    
    /**
     * Get trophy count for a player
     */
    fun getPlayerTrophyCount(playerId: String): Int {
        return playerTrophies[playerId]?.size ?: 0
    }
    
    /**
     * Get trophy count by rarity for a player
     */
    fun getPlayerTrophyCountByRarity(playerId: String): Map<TrophyRarity, Int> {
        val playerTrophies = getPlayerTrophies(playerId)
        return TrophyRarity.values().associateWith { rarity ->
            playerTrophies.count { it.rarity == rarity }
        }
    }
}