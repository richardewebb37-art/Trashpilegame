package com.trashpiles.gcms

import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Challenge Integration Layer
 * 
 * This module provides integration between the Challenge System and the GCMS Controller.
 * It handles challenge assignment, progress tracking, and level unlocking.
 */

object ChallengeIntegration {
    
    /**
     * Initialize the challenge system and assign first level challenges
     */
    fun initializeChallengeSystem(): List<GCMSEvent> {
        ChallengeSystem.initialize()
        
        val events = mutableListOf<GCMSEvent>()
        
        // Assign level 1 challenges
        val initialChallengeData = ChallengeSystem.assignChallengesForLevel(
            level = 1,
            playerChallengeData = PlayerChallengeData()
        )
        
        events.add(ChallengeAssignedEvent(
            level = 1,
            challengeIds = initialChallengeData.currentLevelChallenges?.challenges?.map { it.id } ?: emptyList(),
            requiredToComplete = initialChallengeData.currentLevelChallenges?.requiredToComplete ?: 0
        ))
        
        return events
    }
    
    /**
     * Update challenge progress based on game events
     */
    fun handleGameEvent(
        event: GCMSEvent,
        currentChallengeData: PlayerChallengeData
    ): Pair<PlayerChallengeData, List<GCMSEvent>> {
        val updatedChallengeData = when (event) {
            is CardPlacedEvent -> {
                val eventData = mapOf("card_placed" to event.cardId)
                ChallengeSystem.updateChallengeProgress(currentChallengeData, "card_placed", eventData)
            }
            
            is AbilityUsedEvent -> {
                val eventData = mapOf("abilityId" to event.abilityId)
                ChallengeSystem.updateChallengeProgress(currentChallengeData, "ability_used", eventData)
            }
            
            is NodeUnlockedEvent -> {
                val eventData = mapOf("skillId" to event.nodeId)
                ChallengeSystem.updateChallengeProgress(currentChallengeData, "skill_unlocked", eventData)
            }
            
            is GameOverEvent -> {
                // Calculate score from final scores
                val playerScore = event.finalScores.values.maxOrNull() ?: 0
                val scoreEventData = mapOf("score" to playerScore)
                val scoreUpdated = ChallengeSystem.updateChallengeProgress(currentChallengeData, "score_earned", scoreEventData)
                
                // Check for win streak (simplified - would need actual streak tracking)
                val streakEventData = mapOf("currentStreak" to 1)
                ChallengeSystem.updateChallengeProgress(scoreUpdated, "game_won", streakEventData)
            }
            
            else -> currentChallengeData
        }
        
        // Check for newly completed challenges
        val newEvents = checkForCompletedChallenges(currentChallengeData, updatedChallengeData)
        
        return Pair(updatedChallengeData, newEvents)
    }
    
    /**
     * Check for newly completed challenges and emit events
     */
    private fun checkForCompletedChallenges(
        oldData: PlayerChallengeData,
        newData: PlayerChallengeData
    ): List<GCMSEvent> {
        val events = mutableListOf<GCMSEvent>()
        val currentSet = newData.currentLevelChallenges ?: return events
        
        currentSet.challenges.forEach { challenge ->
            if (challenge.isCompleted && !oldData.completedChallenges.contains(challenge.id)) {
                events.add(ChallengeCompletedEvent(
                    challengeId = challenge.id,
                    challengeName = challenge.name,
                    achievement = challenge.reward.achievement,
                    pointsBonus = challenge.reward.pointsBonus,
                    xpBonus = challenge.reward.xpBonus
                ))
            }
        }
        
        // Check if all challenges for current level are complete
        if (currentSet.areChallengesComplete() && 
            !oldData.completedChallenges.containsAll(currentSet.challenges.map { it.id })) {
            events.add(AllChallengesCompletedEvent(
                level = currentSet.level,
                completedChallenges = currentSet.challenges.map { it.id },
                newAchievements = currentSet.challenges.map { it.reward.achievement }
            ))
        }
        
        return events
    }
    
    /**
     * Check if player can unlock next level (requires both XP and challenges)
     */
    fun checkLevelUnlockEligibility(
        currentLevel: Int,
        playerXP: Int,
        playerChallengeData: PlayerChallengeData
    ): LevelUnlockResult {
        // First check if XP is sufficient (this would be checked by Skill/Ability system too)
        val xpRequired = calculateXPForLevel(currentLevel + 1)
        if (playerXP < xpRequired) {
            return LevelUnlockResult(
                success = false,
                levelUnlocked = currentLevel,
                completedChallenges = emptyList(),
                newAchievements = emptyList(),
                message = "Insufficient XP: Need $xpRequired, have $playerXP"
            )
        }
        
        // Then check challenge requirements
        return ChallengeSystem.canUnlockNextLevel(currentLevel, playerChallengeData)
    }
    
    /**
     * Calculate XP required for a level (simplified - actual formula in SkillAbilityLogic)
     */
    private fun calculateXPForLevel(level: Int): Int {
        // This is a placeholder - the actual calculation is in SkillAbilityLogic
        // Using the same formula: floor(log(XP + 1) / log(1.2)) + 1 = level
        // So: XP = (1.2 ^ (level - 1)) - 1 (approximately)
        return ((1.2).pow(level - 1).toInt() * 100).coerceAtLeast(100)
    }
    
    /**
     * Handle level up - assign new challenges for next level
     */
    fun handleLevelUp(
        newLevel: Int,
        playerChallengeData: PlayerChallengeData
    ): Pair<PlayerChallengeData, GCMSEvent> {
        val updatedChallengeData = ChallengeSystem.assignChallengesForLevel(
            level = newLevel,
            playerChallengeData = playerChallengeData
        )
        
        val event = ChallengeAssignedEvent(
            level = newLevel,
            challengeIds = updatedChallengeData.currentLevelChallenges?.challenges?.map { it.id } ?: emptyList(),
            requiredToComplete = updatedChallengeData.currentLevelChallenges?.requiredToComplete ?: 0
        )
        
        return Pair(updatedChallengeData, event)
    }
    
    /**
     * Get current challenge statistics for display
     */
    fun getChallengeStatistics(playerChallengeData: PlayerChallengeData): Map<String, Any> {
        return ChallengeSystem.getChallengeStatistics(playerChallengeData)
    }
    
    /**
     * Get challenges for current level for display
     */
    fun getCurrentChallenges(playerChallengeData: PlayerChallengeData): List<Challenge> {
        return playerChallengeData.currentLevelChallenges?.challenges ?: emptyList()
    }
    
    /**
     * Calculate challenge completion percentage
     */
    fun getCompletionPercentage(playerChallengeData: PlayerChallengeData): Double {
        return playerChallengeData.currentLevelChallenges?.getCompletionPercentage() ?: 0.0
    }
}

// Extension function for Int.power
private fun Int.pow(exponent: Int): Double {
    return kotlin.math.pow(this.toDouble(), exponent.toDouble())
}