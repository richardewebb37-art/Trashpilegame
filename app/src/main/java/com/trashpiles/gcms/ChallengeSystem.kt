package com.trashpiles.gcms

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.random.Random

/**
 * Challenge System for Side Quests and Level Unlocking
 * 
 * This system adds achievement-based challenges that gate level progression.
 * Players must complete all challenges for a level before advancing,
 * even if they have sufficient XP and points.
 */

/**
 * Challenge types that can be assigned to players
 */
@Serializable
enum class ChallengeType {
    SCORE,
    ABILITY_USAGE,
    SKILL_UNLOCK,
    POINT_ACCUMULATION,
    COMBO,
    WIN_STREAK,
    CARD_PLACEMENT,
    SPEED_RUN,
    PERFECTIONIST,
    EXPLORER
}

/**
 * Individual challenge that a player must complete
 */
@Serializable
data class Challenge(
    val id: String,
    val name: String,
    val description: String,
    val type: ChallengeType,
    val targetLevel: Int,
    val requirements: ChallengeRequirements,
    val reward: ChallengeReward,
    val isCompleted: Boolean = false,
    val progress: ChallengeProgress = ChallengeProgress()
)

/**
 * Requirements for completing a challenge
 */
@Serializable
data class ChallengeRequirements(
    val score: Int = 0,
    val abilitiesUsed: Map<String, Int> = emptyMap(),  // Ability ID -> required uses
    val skillsUnlocked: List<String> = emptyList(),
    val pointsEarned: Int = 0,
    val comboCount: Int = 0,
    val winStreak: Int = 0,
    val cardsPlaced: Int = 0,
    val maxTimeSeconds: Int = 0,  // For speed runs
    val perfectRounds: Int = 0    // Rounds without any mistakes
)

/**
 * Rewards for completing a challenge
 */
@Serializable
data class ChallengeReward(
    val achievement: String,
    val pointsBonus: Int = 0,
    val xpBonus: Int = 0,
    val specialReward: String? = null  // e.g., "UnlockAbilityX", "UnlockSkillY"
)

/**
 * Tracks progress on a challenge
 */
@Serializable
data class ChallengeProgress(
    val currentScore: Int = 0,
    val abilitiesUsed: MutableMap<String, Int> = mutableMapOf(),
    val skillsUnlocked: MutableSet<String> = mutableSetOf(),
    val pointsEarned: Int = 0,
    val comboCount: Int = 0,
    val winStreak: Int = 0,
    val cardsPlaced: Int = 0,
    val timeElapsedSeconds: Int = 0,
    val perfectRounds: Int = 0
) {
    fun isComplete(requirements: ChallengeRequirements): Boolean {
        return currentScore >= requirements.score &&
                abilitiesUsed.all { (abilityId, required) ->
                    (abilitiesUsed[abilityId] ?: 0) >= required
                } &&
                skillsUnlocked.containsAll(requirements.skillsUnlocked) &&
                pointsEarned >= requirements.pointsEarned &&
                comboCount >= requirements.comboCount &&
                winStreak >= requirements.winStreak &&
                cardsPlaced >= requirements.cardsPlaced &&
                (maxTimeSeconds == 0 || timeElapsedSeconds <= maxTimeSeconds) &&
                perfectRounds >= requirements.perfectRounds
    }
}

/**
 * Level-specific challenge set
 */
@Serializable
data class LevelChallengeSet(
    val level: Int,
    val challenges: List<Challenge>,
    val requiredToComplete: Int = -1  // -1 means all challenges required
) {
    fun areChallengesComplete(): Boolean {
        val required = if (requiredToComplete == -1) challenges.size else requiredToComplete
        return challenges.count { it.isCompleted } >= required
    }
    
    fun getCompletedCount(): Int = challenges.count { it.isCompleted }
    fun getTotalCount(): Int = challenges.size
    fun getCompletionPercentage(): Double = 
        if (challenges.isEmpty()) 0.0 else (getCompletedCount().toDouble() / challenges.size * 100)
}

/**
 * Player's challenge tracking
 */
@Serializable
data class PlayerChallengeData(
    val completedChallenges: MutableSet<String> = mutableSetOf(),
    val currentLevelChallenges: LevelChallengeSet? = null,
    val totalChallengesCompleted: Int = 0,
    val achievementUnlocks: MutableSet<String> = mutableSetOf()
)

/**
 * Result of attempting to complete a level with challenges
 */
@Serializable
data class LevelUnlockResult(
    val success: Boolean,
    val levelUnlocked: Int,
    val completedChallenges: List<String>,
    val newAchievements: List<String>,
    val message: String
)

/**
 * Main Challenge System object
 */
object ChallengeSystem {
    
    // Store challenge definitions per level (can be loaded from JSON in production)
    private val challengeDefinitions: MutableMap<Int, LevelChallengeSet> = mutableMapOf()
    
    // Cache for randomization
    private val random = Random.Default
    
    /**
     * Initialize the challenge system with pre-defined challenges
     */
    fun initialize() {
        generateChallengesForLevels(1..200)
    }
    
    /**
     * Generate challenges for a range of levels
     */
    fun generateChallengesForLevels(levelRange: IntRange) {
        for (level in levelRange) {
            challengeDefinitions[level] = generateChallengesForLevel(level)
        }
    }
    
    /**
     * Generate challenges for a specific level
     */
    fun generateChallengesForLevel(level: Int): LevelChallengeSet {
        val challengeCount = getChallengeCountForLevel(level)
        val challenges = mutableListOf<Challenge>()
        
        for (i in 0 until challengeCount) {
            val challenge = generateRandomChallenge(level, i)
            challenges.add(challenge)
        }
        
        val requiredToComplete = getRequiredCountForLevel(level, challengeCount)
        
        return LevelChallengeSet(
            level = level,
            challenges = challenges,
            requiredToComplete = requiredToComplete
        )
    }
    
    /**
     * Get the number of challenges for a level based on tier
     */
    private fun getChallengeCountForLevel(level: Int): Int {
        return when (level) {
            in 1..5 -> 2        // Life tier: 2 challenges
            in 6..20 -> 3       // Beginner tier: 3 challenges
            in 21..50 -> 4      // Novice tier: 4 challenges
            in 51..80 -> 5      // Hard tier: 5 challenges
            in 81..140 -> 6     // Expert tier: 6 challenges
            else -> 7           // Master tier: 7 challenges
        }
    }
    
    /**
     * Get the number of challenges required to complete for a level
     */
    private fun getRequiredCountForLevel(level: Int, totalChallenges: Int): Int {
        // Early levels: all required
        // Mid levels: all-1 required
        // High levels: optional (all-2 or more flexible)
        return when {
            level <= 20 -> totalChallenges
            level <= 50 -> totalChallenges - 1
            level <= 80 -> totalChallenges - 2
            else -> max(1, totalChallenges - 3)
        }
    }
    
    /**
     * Generate a random challenge for a level
     */
    private fun generateRandomChallenge(level: Int, index: Int): Challenge {
        val challengeType = selectChallengeType(level, index)
        val requirements = generateRequirements(level, challengeType)
        val reward = generateReward(level, challengeType)
        
        val challengeId = "L${level}_C${index}_${challengeType.name}"
        
        return Challenge(
            id = challengeId,
            name = generateChallengeName(challengeType, level),
            description = generateChallengeDescription(challengeType, requirements),
            type = challengeType,
            targetLevel = level,
            requirements = requirements,
            reward = reward
        )
    }
    
    /**
     * Select a challenge type based on level and variety
     */
    private fun selectChallengeType(level: Int, index: Int): ChallengeType {
        val availableTypes = when {
            level <= 5 -> listOf(
                ChallengeType.SCORE,
                ChallengeType.WIN_STREAK,
                ChallengeType.CARD_PLACEMENT
            )
            level <= 20 -> listOf(
                ChallengeType.SCORE,
                ChallengeType.ABILITY_USAGE,
                ChallengeType.WIN_STREAK,
                ChallengeType.CARD_PLACEMENT,
                ChallengeType.POINT_ACCUMULATION
            )
            level <= 50 -> ChallengeType.values().toList()
            else -> ChallengeType.values().toList()
        }
        
        // Ensure variety within a level
        return availableTypes[index % availableTypes.size]
    }
    
    /**
     * Generate requirements for a challenge based on level and type
     */
    private fun generateRequirements(level: Int, type: ChallengeType): ChallengeRequirements {
        val difficultyMultiplier = level * 0.5
        
        return when (type) {
            ChallengeType.SCORE -> ChallengeRequirements(
                score = (100 + difficultyMultiplier * 20).toInt()
            )
            
            ChallengeType.ABILITY_USAGE -> ChallengeRequirements(
                abilitiesUsed = mapOf(
                    "ability_${random.nextInt(3) + 1}" to (1 + (difficultyMultiplier * 0.1).toInt()).coerceAtLeast(1)
                )
            )
            
            ChallengeType.SKILL_UNLOCK -> ChallengeRequirements(
                skillsUnlocked = listOf("skill_${random.nextInt(5) + 1}")
            )
            
            ChallengeType.POINT_ACCUMULATION -> ChallengeRequirements(
                pointsEarned = (50 + difficultyMultiplier * 10).toInt()
            )
            
            ChallengeType.COMBO -> ChallengeRequirements(
                comboCount = (3 + (difficultyMultiplier * 0.05).toInt()).coerceAtLeast(3)
            )
            
            ChallengeType.WIN_STREAK -> ChallengeRequirements(
                winStreak = (2 + (difficultyMultiplier * 0.03).toInt()).coerceAtLeast(2)
            )
            
            ChallengeType.CARD_PLACEMENT -> ChallengeRequirements(
                cardsPlaced = (20 + difficultyMultiplier * 5).toInt()
            )
            
            ChallengeType.SPEED_RUN -> ChallengeRequirements(
                maxTimeSeconds = max(30, (180 - difficultyMultiplier * 2).toInt())
            )
            
            ChallengeType.PERFECTIONIST -> ChallengeRequirements(
                perfectRounds = (1 + (difficultyMultiplier * 0.02).toInt()).coerceAtLeast(1)
            )
            
            ChallengeType.EXPLORER -> ChallengeRequirements(
                score = (50 + difficultyMultiplier * 10).toInt(),
                abilitiesUsed = mapOf("ability_1" to 1),
                skillsUnlocked = listOf("skill_1")
            )
        }
    }
    
    /**
     * Generate rewards for completing a challenge
     */
    private fun generateReward(level: Int, type: ChallengeType): ChallengeReward {
        val pointsBonus = (10 + level * 2)
        val xpBonus = (20 + level * 3)
        
        val achievementName = when (type) {
            ChallengeType.SCORE -> "Score Master"
            ChallengeType.ABILITY_USAGE -> "Ability Wielder"
            ChallengeType.SKILL_UNLOCK -> "Skill Scholar"
            ChallengeType.POINT_ACCUMULATION -> "Point Collector"
            ChallengeType.COMBO -> "Combo King"
            ChallengeType.WIN_STREAK -> "Victory Streak"
            ChallengeType.CARD_PLACEMENT -> "Card Strategist"
            ChallengeType.SPEED_RUN -> "Swift Hand"
            ChallengeType.PERFECTIONIST -> "Perfect Player"
            ChallengeType.EXPLORER -> "Explorer"
        }
        
        return ChallengeReward(
            achievement = "$achievementName L$level",
            pointsBonus = pointsBonus,
            xpBonus = xpBonus
        )
    }
    
    /**
     * Generate a challenge name
     */
    private fun generateChallengeName(type: ChallengeType, level: Int): String {
        val adjectives = listOf("Novice", "Skilled", "Expert", "Master", "Legendary")
        val adjectiveIndex = when {
            level <= 20 -> 0
            level <= 50 -> 1
            level <= 80 -> 2
            level <= 140 -> 3
            else -> 4
        }
        
        val adjective = adjectives[adjectiveIndex]
        
        return when (type) {
            ChallengeType.SCORE -> "$adjective Scorer"
            ChallengeType.ABILITY_USAGE -> "$adjective Ability User"
            ChallengeType.SKILL_UNLOCK -> "$adjective Skill Seeker"
            ChallengeType.POINT_ACCUMULATION -> "$adjective Point Gatherer"
            ChallengeType.COMBO -> "$adjective Combo Master"
            ChallengeType.WIN_STREAK -> "$adjective Victor"
            ChallengeType.CARD_PLACEMENT -> "$adjective Strategist"
            ChallengeType.SPEED_RUN -> "$adjective Speedster"
            ChallengeType.PERFECTIONIST -> "$adjective Perfectionist"
            ChallengeType.EXPLORER -> "$adjective Explorer"
        }
    }
    
    /**
     * Generate a challenge description
     */
    private fun generateChallengeDescription(type: ChallengeType, requirements: ChallengeRequirements): String {
        return when (type) {
            ChallengeType.SCORE -> "Score ${requirements.score} points in a single game"
            ChallengeType.ABILITY_USAGE -> "Use abilities ${requirements.abilitiesUsed.values.sum()} times"
            ChallengeType.SKILL_UNLOCK -> "Unlock ${requirements.skillsUnlocked.size} skills"
            ChallengeType.POINT_ACCUMULATION -> "Earn ${requirements.pointsEarned} total points"
            ChallengeType.COMBO -> "Achieve a ${requirements.comboCount}-card combo"
            ChallengeType.WIN_STREAK -> "Win ${requirements.winStreak} games in a row"
            ChallengeType.CARD_PLACEMENT -> "Place ${requirements.cardsPlaced} cards correctly"
            ChallengeType.SPEED_RUN -> "Complete a game in under ${requirements.maxTimeSeconds} seconds"
            ChallengeType.PERFECTIONIST -> "Complete ${requirements.perfectRounds} rounds without mistakes"
            ChallengeType.EXPLORER -> "Explore multiple game mechanics"
        }
    }
    
    /**
     * Get challenges for a level
     */
    fun getChallengesForLevel(level: Int): LevelChallengeSet? {
        return challengeDefinitions[level]
    }
    
    /**
     * Check if a player can unlock the next level based on challenges
     */
    fun canUnlockNextLevel(
        currentLevel: Int,
        playerChallengeData: PlayerChallengeData
    ): LevelUnlockResult {
        val nextLevel = currentLevel + 1
        val challengeSet = getChallengesForLevel(currentLevel)
        
        if (challengeSet == null) {
            return LevelUnlockResult(
                success = true,
                levelUnlocked = nextLevel,
                completedChallenges = emptyList(),
                newAchievements = emptyList(),
                message = "No challenges defined for level $currentLevel"
            )
        }
        
        // Check if challenges are complete
        if (!challengeSet.areChallengesComplete()) {
            return LevelUnlockResult(
                success = false,
                levelUnlocked = currentLevel,
                completedChallenges = emptyList(),
                newAchievements = emptyList(),
                message = "Complete ${challengeSet.requiredToComplete}/${challengeSet.challenges.size} challenges to unlock level $nextLevel"
            )
        }
        
        // All challenges complete - can unlock next level
        val completedChallenges = challengeSet.challenges
            .filter { it.isCompleted }
            .map { it.id }
        
        val newAchievements = challengeSet.challenges
            .filter { it.isCompleted }
            .map { it.reward.achievement }
        
        return LevelUnlockResult(
            success = true,
            levelUnlocked = nextLevel,
            completedChallenges = completedChallenges,
            newAchievements = newAchievements,
            message = "All challenges complete! Level $nextLevel unlocked!"
        )
    }
    
    /**
     * Update challenge progress based on game events
     */
    fun updateChallengeProgress(
        challengeData: PlayerChallengeData,
        eventType: String,
        eventData: Map<String, Any>
    ): PlayerChallengeData {
        val currentSet = challengeData.currentLevelChallenges ?: return challengeData
        
        val updatedChallenges = currentSet.challenges.map { challenge ->
            if (challenge.isCompleted) return@map challenge
            
            val updatedProgress = updateProgressForEvent(
                challenge.progress,
                eventType,
                eventData
            )
            
            val isNowComplete = updatedProgress.isComplete(challenge.requirements)
            
            challenge.copy(
                progress = updatedProgress,
                isCompleted = isNowComplete
            )
        }
        
        val updatedSet = currentSet.copy(challenges = updatedChallenges)
        
        return challengeData.copy(
            currentLevelChallenges = updatedSet,
            completedChallenges = if (updatedSet.areChallengesComplete()) {
                challengeData.completedChallenges.apply { addAll(updatedSet.challenges.map { it.id }) }
            } else {
                challengeData.completedChallenges
            }
        )
    }
    
    /**
     * Update progress for a specific event
     */
    private fun updateProgressForEvent(
        progress: ChallengeProgress,
        eventType: String,
        eventData: Map<String, Any>
    ): ChallengeProgress {
        return when (eventType) {
            "score_earned" -> progress.copy(
                currentScore = progress.currentScore + (eventData["score"] as? Int ?: 0)
            )
            
            "ability_used" -> {
                val abilityId = eventData["abilityId"] as? String ?: return progress
                val currentUses = progress.abilitiesUsed.getOrDefault(abilityId, 0)
                val updatedUses = progress.abilitiesUsed.toMutableMap()
                updatedUses[abilityId] = currentUses + 1
                progress.copy(abilitiesUsed = updatedUses)
            }
            
            "skill_unlocked" -> {
                val skillId = eventData["skillId"] as? String ?: return progress
                progress.copy(skillsUnlocked = progress.skillsUnlocked.apply { add(skillId) })
            }
            
            "points_earned" -> progress.copy(
                pointsEarned = progress.pointsEarned + (eventData["points"] as? Int ?: 0)
            )
            
            "combo_achieved" -> {
                val comboCount = eventData["comboCount"] as? Int ?: 0
                progress.copy(comboCount = max(progress.comboCount, comboCount))
            }
            
            "game_won" -> {
                val currentStreak = eventData["currentStreak"] as? Int ?: 0
                progress.copy(winStreak = max(progress.winStreak, currentStreak))
            }
            
            "card_placed" -> progress.copy(
                cardsPlaced = progress.cardsPlaced + 1
            )
            
            "time_elapsed" -> progress.copy(
                timeElapsedSeconds = eventData["seconds"] as? Int ?: 0
            )
            
            "perfect_round" -> progress.copy(
                perfectRounds = progress.perfectRounds + 1
            )
            
            else -> progress
        }
    }
    
    /**
     * Assign challenges for a level to a player
     */
    fun assignChallengesForLevel(
        level: Int,
        playerChallengeData: PlayerChallengeData
    ): PlayerChallengeData {
        val challengeSet = generateChallengesForLevel(level)
        return playerChallengeData.copy(currentLevelChallenges = challengeSet)
    }
    
    /**
     * Get challenge completion statistics
     */
    fun getChallengeStatistics(playerChallengeData: PlayerChallengeData): Map<String, Any> {
        val currentSet = playerChallengeData.currentLevelChallenges
        
        return mapOf(
            "totalCompleted" to playerChallengeData.totalChallengesCompleted,
            "totalAchievements" to playerChallengeData.achievementUnlocks.size,
            "currentLevel" to (currentSet?.level ?: 0),
            "currentCompleted" to (currentSet?.getCompletedCount() ?: 0),
            "currentTotal" to (currentSet?.getTotalCount() ?: 0),
            "completionPercentage" to (currentSet?.getCompletionPercentage() ?: 0.0)
        )
    }
}