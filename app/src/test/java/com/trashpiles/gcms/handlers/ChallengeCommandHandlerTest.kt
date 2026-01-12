package com.trashpiles.gcms.handlers

import com.trashpiles.gcms.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChallengeCommandHandlerTest {
    
    private val handler = ChallengeCommandHandler()
    
    @Test
    fun `test view challenges command`() {
        ChallengeSystem.initialize()
        
        val state = GCMSState().copy(
            challengeSystem = ChallengeSystem.assignChallengesForLevel(
                1,
                PlayerChallengeData()
            )
        )
        
        val command = GCMSCommand.ViewChallengesCommand(
            playerId = "player1"
        )
        
        val result = handler.handle(command, state)
        
        assertTrue(result.events.isNotEmpty())
        assertTrue(result.events.any { it is GCMSEvent.ChallengeProgressUpdatedEvent })
    }
    
    @Test
    fun `test check level up with insufficient challenges`() {
        ChallengeSystem.initialize()
        
        val state = GCMSState().copy(
            skillAbilitySystem = SkillAbilitySystemState(
                playerProgress = PlayerProgress(
                    level = 1,
                    accumulatedXP = 500
                )
            ),
            challengeSystem = ChallengeSystem.assignChallengesForLevel(
                1,
                PlayerChallengeData()
            )
        )
        
        val command = GCMSCommand.CheckLevelUpCommand(
            playerId = "player1"
        )
        
        val result = handler.handle(command, state)
        
        // Should fail because challenges aren't complete
        val invalidEvent = result.events.find { it is GCMSEvent.InvalidMoveEvent }
        assertNotNull(invalidEvent)
        assertFalse(result.events.any { it is GCMSEvent.LevelUpEvent })
    }
    
    @Test
    fun `test check level up with completed challenges`() {
        ChallengeSystem.initialize()
        
        val challengeData = ChallengeSystem.assignChallengesForLevel(1, PlayerChallengeData())
        val completedSet = challengeData.currentLevelChallenges?.copy(
            challenges = challengeData.currentLevelChallenges?.challenges?.map { it.copy(isCompleted = true) } ?: emptyList()
        )
        
        val state = GCMSState().copy(
            skillAbilitySystem = SkillAbilitySystemState(
                playerProgress = PlayerProgress(
                    level = 1,
                    accumulatedXP = 500
                )
            ),
            challengeSystem = challengeData.copy(
                currentLevelChallenges = completedSet
            )
        )
        
        val command = GCMSCommand.CheckLevelUpCommand(
            playerId = "player1"
        )
        
        val result = handler.handle(command, state)
        
        // Should succeed
        val levelUpEvent = result.events.find { it is GCMSEvent.LevelUpEvent }
        assertNotNull(levelUpEvent)
        assertEquals(2, (levelUpEvent as GCMSEvent.LevelUpEvent).newLevel)
    }
    
    @Test
    fun `test claim challenge rewards`() {
        ChallengeSystem.initialize()
        
        val challengeData = ChallengeSystem.assignChallengesForLevel(1, PlayerChallengeData())
        val completedChallenge = challengeData.currentLevelChallenges?.challenges?.first()?.copy(
            isCompleted = true
        )
        
        val state = GCMSState().copy(
            skillAbilitySystem = SkillAbilitySystemState(
                playerProgress = PlayerProgress(
                    level = 1,
                    accumulatedXP = 100,
                    totalSP = 50
                )
            ),
            challengeSystem = challengeData.copy(
                currentLevelChallenges = challengeData.currentLevelChallenges?.copy(
                    challenges = listOf(completedChallenge ?: challengeData.currentLevelChallenges!!.challenges.first())
                )
            )
        )
        
        val command = GCMSCommand.ClaimChallengeRewardsCommand(
            playerId = "player1",
            challengeId = completedChallenge?.id ?: ""
        )
        
        val result = handler.handle(command, state)
        
        // Should award points
        val pointsEvent = result.events.find { it is GCMSEvent.PointsEarnedEvent }
        assertNotNull(pointsEvent)
        assertTrue((pointsEvent as GCMSEvent.PointsEarnedEvent).spEarned > 0)
    }
    
    @Test
    fun `test claim rewards for incomplete challenge`() {
        ChallengeSystem.initialize()
        
        val challengeData = ChallengeSystem.assignChallengesForLevel(1, PlayerChallengeData())
        
        val state = GCMSState().copy(
            skillAbilitySystem = SkillAbilitySystemState(),
            challengeSystem = challengeData
        )
        
        val command = GCMSCommand.ClaimChallengeRewardsCommand(
            playerId = "player1",
            challengeId = challengeData.currentLevelChallenges?.challenges?.first()?.id ?: ""
        )
        
        val result = handler.handle(command, state)
        
        // Should fail
        val invalidEvent = result.events.find { it is GCMSEvent.InvalidMoveEvent }
        assertNotNull(invalidEvent)
        assertTrue(invalidEvent?.toString()?.contains("not yet completed") ?: false)
    }
    
    @Test
    fun `test claim rewards already claimed`() {
        ChallengeSystem.initialize()
        
        val challengeData = ChallengeSystem.assignChallengesForLevel(1, PlayerChallengeData())
        val completedChallenge = challengeData.currentLevelChallenges?.challenges?.first()?.copy(
            isCompleted = true
        )
        
        val state = GCMSState().copy(
            skillAbilitySystem = SkillAbilitySystemState(),
            challengeSystem = challengeData.copy(
                currentLevelChallenges = challengeData.currentLevelChallenges?.copy(
                    challenges = listOf(completedChallenge ?: challengeData.currentLevelChallenges!!.challenges.first())
                ),
                achievementUnlocks = mutableSetOf(completedChallenge?.reward?.achievement ?: "")
            )
        )
        
        val command = GCMSCommand.ClaimChallengeRewardsCommand(
            playerId = "player1",
            challengeId = completedChallenge?.id ?: ""
        )
        
        val result = handler.handle(command, state)
        
        // Should fail
        val invalidEvent = result.events.find { it is GCMSEvent.InvalidMoveEvent }
        assertNotNull(invalidEvent)
        assertTrue(invalidEvent?.toString()?.contains("already claimed") ?: false)
    }
}