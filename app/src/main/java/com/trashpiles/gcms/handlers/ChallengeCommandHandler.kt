package com.trashpiles.gcms.handlers

import com.trashpiles.gcms.*

/**
 * Handler for challenge system commands
 * 
 * Handles:
 * - ViewChallengesCommand
 * - CheckLevelUpCommand
 * - ClaimChallengeRewardsCommand
 */
class ChallengeCommandHandler : CommandHandler {
    
    override suspend fun handle(command: GCMSCommand, currentState: GCMSState): CommandResult {
        return when (command) {
            is GCMSCommand.ViewChallengesCommand -> handleViewChallenges(command, currentState)
            is GCMSCommand.CheckLevelUpCommand -> handleCheckLevelUp(command, currentState)
            is GCMSCommand.ClaimChallengeRewardsCommand -> handleClaimRewards(command, currentState)
            else -> throw IllegalArgumentException(
                "ChallengeCommandHandler cannot handle ${command::class.simpleName}")
        }
    }
    
    private fun handleViewChallenges(
        command: GCMSCommand.ViewChallengesCommand,
        state: GCMSState
    ): CommandResult {
        val challenges = ChallengeIntegration.getCurrentChallenges(state.challengeSystem)
        val stats = ChallengeIntegration.getChallengeStatistics(state.challengeSystem)
        val completionPercentage = ChallengeIntegration.getCompletionPercentage(state.challengeSystem)
        
        // Emit event with challenge information
        val event = GCMSEvent.ChallengeProgressUpdatedEvent(
            challengeId = "view",
            challengeName = "Current Challenges",
            progress = mapOf(
                "challenges" to challenges,
                "statistics" to stats,
                "completionPercentage" to completionPercentage
            ),
            isComplete = false
        )
        
        return CommandResult(
            state = state,
            events = listOf(event)
        )
    }
    
    private fun handleCheckLevelUp(
        command: GCMSCommand.CheckLevelUpCommand,
        state: GCMSState
    ): CommandResult {
        // Find player progress
        val playerProgress = state.skillAbilitySystem.playerProgress
        val currentLevel = playerProgress.level
        val playerXP = playerProgress.accumulatedXP
        
        // Check level unlock eligibility
        val unlockResult = ChallengeIntegration.checkLevelUnlockEligibility(
            currentLevel = currentLevel,
            playerXP = playerXP,
            playerChallengeData = state.challengeSystem
        )
        
        val events = mutableListOf<GCMSEvent>()
        
        if (unlockResult.success) {
            // Level up!
            val newLevel = unlockResult.levelUnlocked
            
            // Calculate new level requirements
            val xpToNextLevel = SkillAbilityLogic.calculateXPForLevel(newLevel + 1)
            
            // Emit level up event
            events.add(GCMSEvent.LevelUpEvent(
                playerId = command.playerId,
                newLevel = newLevel,
                totalXP = playerXP,
                xpToNextLevel = xpToNextLevel
            ))
            
            // Emit level unlocked event
            events.add(GCMSEvent.LevelUnlockedEvent(
                unlockedLevel = newLevel,
                completedChallenges = unlockResult.completedChallenges,
                newAchievements = unlockResult.newAchievements
            ))
            
            // Assign new challenges for next level
            val (newChallengeData, challengeEvent) = ChallengeIntegration.handleLevelUp(
                newLevel = newLevel,
                playerChallengeData = state.challengeSystem
            )
            
            events.add(challengeEvent)
            
            // Update state
            val updatedState = state.copyWith(
                skillAbilitySystem = state.skillAbilitySystem.copy(
                    playerProgress = playerProgress.copy(
                        level = newLevel
                    )
                ),
                challengeSystem = newChallengeData
            )
            
            return CommandResult(state = updatedState, events = events)
        } else {
            // Cannot level up yet
            events.add(GCMSEvent.InvalidMoveEvent(
                reason = unlockResult.message,
                playerId = command.playerId
            ))
            
            return CommandResult(state = state, events = events)
        }
    }
    
    private fun handleClaimRewards(
        command: GCMSCommand.ClaimChallengeRewardsCommand,
        state: GCMSState
    ): CommandResult {
        val events = mutableListOf<GCMSEvent>()
        var updatedState = state
        
        // Find the challenge
        val currentSet = state.challengeSystem.currentLevelChallenges
        val challenge = currentSet?.challenges?.find { it.id == command.challengeId }
        
        if (challenge == null) {
            events.add(GCMSEvent.InvalidMoveEvent(
                reason = "Challenge not found: ${command.challengeId}",
                playerId = command.playerId
            ))
            return CommandResult(state = state, events = events)
        }
        
        if (!challenge.isCompleted) {
            events.add(GCMSEvent.InvalidMoveEvent(
                reason = "Challenge not yet completed: ${challenge.name}",
                playerId = command.playerId
            ))
            return CommandResult(state = state, events = events)
        }
        
        // Check if rewards already claimed
        if (state.challengeSystem.achievementUnlocks.contains(challenge.reward.achievement)) {
            events.add(GCMSEvent.InvalidMoveEvent(
                reason = "Rewards already claimed for: ${challenge.name}",
                playerId = command.playerId
            ))
            return CommandResult(state = state, events = events)
        }
        
        // Claim rewards - add points and XP to player
        val playerProgress = state.skillAbilitySystem.playerProgress
        val newXP = playerProgress.accumulatedXP + challenge.reward.xpBonus
        
        val updatedProgress = playerProgress.copy(
            accumulatedXP = newXP,
            totalSP = playerProgress.totalSP + challenge.reward.pointsBonus
        )
        
        // Recalculate level based on new XP
        updatedProgress.recalculateLevel()
        
        // Add achievement to unlocks
        val updatedAchievements = state.challengeSystem.achievementUnlocks.apply {
            add(challenge.reward.achievement)
        }
        
        updatedState = state.copyWith(
            skillAbilitySystem = state.skillAbilitySystem.copy(
                playerProgress = updatedProgress
            ),
            challengeSystem = state.challengeSystem.copy(
                achievementUnlocks = updatedAchievements
            )
        )
        
        // Emit events
        events.add(GCMSEvent.PointsEarnedEvent(
            playerId = command.playerId,
            spEarned = challenge.reward.pointsBonus,
            apEarned = 0,
            totalSP = updatedProgress.totalSP,
            totalAP = updatedProgress.totalAP
        ))
        
        // Check if level increased from XP bonus
        if (updatedProgress.level > playerProgress.level) {
            events.add(GCMSEvent.LevelUpEvent(
                playerId = command.playerId,
                newLevel = updatedProgress.level,
                totalXP = newXP,
                xpToNextLevel = SkillAbilityLogic.calculateXPForLevel(updatedProgress.level + 1)
            ))
        }
        
        return CommandResult(state = updatedState, events = events)
    }
}