package com.trashpiles.gcms

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChallengeSystemTest {
    
    @Test
    fun `test challenge system initialization`() {
        ChallengeSystem.initialize()
        
        // Verify challenges were generated for all levels
        val level10Challenges = ChallengeSystem.getChallengesForLevel(10)
        assertNotNull(level10Challenges)
        assertEquals(10, level10Challenges.level)
        assertTrue(level10Challenges.challenges.isNotEmpty())
    }
    
    @Test
    fun `test challenge count varies by level`() {
        ChallengeSystem.initialize()
        
        // Life tier (1-5): 2 challenges
        val level1Challenges = ChallengeSystem.getChallengesForLevel(1)
        assertEquals(2, level1Challenges?.challenges?.size)
        
        // Beginner tier (6-20): 3 challenges
        val level10Challenges = ChallengeSystem.getChallengesForLevel(10)
        assertEquals(3, level10Challenges?.challenges?.size)
        
        // Novice tier (21-50): 4 challenges
        val level30Challenges = ChallengeSystem.getChallengesForLevel(30)
        assertEquals(4, level30Challenges?.challenges?.size)
        
        // Hard tier (51-80): 5 challenges
        val level60Challenges = ChallengeSystem.getChallengesForLevel(60)
        assertEquals(5, level60Challenges?.challenges?.size)
        
        // Expert tier (81-140): 6 challenges
        val level100Challenges = ChallengeSystem.getChallengesForLevel(100)
        assertEquals(6, level100Challenges?.challenges?.size)
        
        // Master tier (141+): 7 challenges
        val level150Challenges = ChallengeSystem.getChallengesForLevel(150)
        assertEquals(7, level150Challenges?.challenges?.size)
    }
    
    @Test
    fun `test required challenges vary by tier`() {
        ChallengeSystem.initialize()
        
        // Early levels: all required
        val level10Challenges = ChallengeSystem.getChallengesForLevel(10)
        assertEquals(3, level10Challenges?.requiredToComplete)
        
        // Mid levels: all-1 required
        val level30Challenges = ChallengeSystem.getChallengesForLevel(30)
        assertEquals(3, level30Challenges?.requiredToComplete)  // 4 total, 3 required
        
        // High levels: all-2 required
        val level60Challenges = ChallengeSystem.getChallengesForLevel(60)
        assertEquals(3, level60Challenges?.requiredToComplete)  // 5 total, 3 required
        
        // Master levels: more flexible
        val level150Challenges = ChallengeSystem.getChallengesForLevel(150)
        assertEquals(4, level150Challenges?.requiredToComplete)  // 7 total, 4 required
    }
    
    @Test
    fun `test challenge types vary by level`() {
        ChallengeSystem.initialize()
        
        val level1Challenges = ChallengeSystem.getChallengesForLevel(1)
        val types1 = level1Challenges?.challenges?.map { it.type }?.toSet()
        
        // Early levels should have basic types
        assertTrue(types1?.contains(ChallengeType.SCORE) ?: false)
        assertTrue(types1?.contains(ChallengeType.WIN_STREAK) ?: false)
        
        val level100Challenges = ChallengeSystem.getChallengesForLevel(100)
        val types100 = level100Challenges?.challenges?.map { it.type }?.toSet()
        
        // High levels should have all types
        assertTrue(types100?.contains(ChallengeType.SCORE) ?: false)
        assertTrue(types100?.contains(ChallengeType.COMBO) ?: false)
        assertTrue(types100?.contains(ChallengeType.SPEED_RUN) ?: false)
        assertTrue(types100?.contains(ChallengeType.EXPLORER) ?: false)
    }
    
    @Test
    fun `test challenge requirements increase with level`() {
        ChallengeSystem.initialize()
        
        val level10Challenges = ChallengeSystem.getChallengesForLevel(10)
        val scoreChallenge10 = level10Challenges?.challenges?.find { it.type == ChallengeType.SCORE }
        
        val level100Challenges = ChallengeSystem.getChallengesForLevel(100)
        val scoreChallenge100 = level100Challenges?.challenges?.find { it.type == ChallengeType.SCORE }
        
        assertTrue((scoreChallenge100?.requirements?.score ?: 0) > (scoreChallenge10?.requirements?.score ?: 0))
    }
    
    @Test
    fun `test challenge progress tracking`() {
        val progress = ChallengeProgress(
            currentScore = 50,
            abilitiesUsed = mutableMapOf("ability_1" to 2),
            skillsUnlocked = mutableSetOf("skill_1"),
            pointsEarned = 100,
            comboCount = 3,
            winStreak = 2,
            cardsPlaced = 10
        )
        
        val requirements = ChallengeRequirements(
            score = 100,
            abilitiesUsed = mapOf("ability_1" to 3),
            skillsUnlocked = listOf("skill_1"),
            pointsEarned = 50,
            comboCount = 3,
            winStreak = 2,
            cardsPlaced = 10
        )
        
        // Not complete yet (score and ability usage insufficient)
        assertFalse(progress.isComplete(requirements))
    }
    
    @Test
    fun `test challenge progress completion`() {
        val progress = ChallengeProgress(
            currentScore = 150,
            abilitiesUsed = mutableMapOf("ability_1" to 5),
            skillsUnlocked = mutableSetOf("skill_1", "skill_2"),
            pointsEarned = 200,
            comboCount = 5,
            winStreak = 3,
            cardsPlaced = 25
        )
        
        val requirements = ChallengeRequirements(
            score = 100,
            abilitiesUsed = mapOf("ability_1" to 3),
            skillsUnlocked = listOf("skill_1"),
            pointsEarned = 50,
            comboCount = 3,
            winStreak = 2,
            cardsPlaced = 10
        )
        
        // Complete!
        assertTrue(progress.isComplete(requirements))
    }
    
    @Test
    fun `test assign challenges for level`() {
        ChallengeSystem.initialize()
        
        val playerData = PlayerChallengeData()
        val updatedData = ChallengeSystem.assignChallengesForLevel(10, playerData)
        
        assertNotNull(updatedData.currentLevelChallenges)
        assertEquals(10, updatedData.currentLevelChallenges.level)
        assertEquals(3, updatedData.currentLevelChallenges.challenges.size)
    }
    
    @Test
    fun `test update challenge progress with score event`() {
        ChallengeSystem.initialize()
        
        val playerData = PlayerChallengeData()
        val updatedData = ChallengeSystem.assignChallengesForLevel(10, playerData)
        
        // Simulate scoring points
        val scoreEventData = mapOf("score" to 150)
        val afterScore = ChallengeSystem.updateChallengeProgress(updatedData, "score_earned", scoreEventData)
        
        val scoreChallenge = afterScore.currentLevelChallenges?.challenges?.find { it.type == ChallengeType.SCORE }
        assertEquals(150, scoreChallenge?.progress?.currentScore)
    }
    
    @Test
    fun `test update challenge progress with ability usage`() {
        ChallengeSystem.initialize()
        
        val playerData = PlayerChallengeData()
        val updatedData = ChallengeSystem.assignChallengesForLevel(10, playerData)
        
        // Simulate using an ability
        val abilityEventData = mapOf("abilityId" to "ability_1")
        val afterAbility = ChallengeSystem.updateChallengeProgress(updatedData, "ability_used", abilityEventData)
        
        val abilityChallenge = afterAbility.currentLevelChallenges?.challenges?.find { it.type == ChallengeType.ABILITY_USAGE }
        assertEquals(1, abilityChallenge?.progress?.abilitiesUsed?.get("ability_1"))
    }
    
    @Test
    fun `test cannot unlock next level without completing challenges`() {
        ChallengeSystem.initialize()
        
        val playerData = PlayerChallengeData()
        val updatedData = ChallengeSystem.assignChallengesForLevel(10, playerData)
        
        val result = ChallengeSystem.canUnlockNextLevel(10, updatedData)
        
        assertFalse(result.success)
        assertTrue(result.message.contains("Complete"))
    }
    
    @Test
    fun `test can unlock next level with completed challenges`() {
        ChallengeSystem.initialize()
        
        val playerData = PlayerChallengeData()
        val updatedData = ChallengeSystem.assignChallengesForLevel(10, playerData)
        
        // Manually complete all challenges
        val completedSet = updatedData.currentLevelChallenges?.copy(
            challenges = updatedData.currentLevelChallenges.challenges.map { it.copy(isCompleted = true) }
        )
        val completedData = updatedData.copy(currentLevelChallenges = completedSet)
        
        val result = ChallengeSystem.canUnlockNextLevel(10, completedData)
        
        assertTrue(result.success)
        assertEquals(11, result.levelUnlocked)
        assertTrue(result.message.contains("unlocked"))
    }
    
    @Test
    fun `test level completion percentage calculation`() {
        ChallengeSystem.initialize()
        
        val playerData = PlayerChallengeData()
        val updatedData = ChallengeSystem.assignChallengesForLevel(10, playerData)
        
        val currentSet = updatedData.currentLevelChallenges
        assertEquals(0.0, currentSet?.getCompletionPercentage())
        
        // Complete 1 of 3 challenges
        val partiallyCompleted = currentSet?.copy(
            challenges = currentSet.challenges.mapIndexed { index, challenge ->
                challenge.copy(isCompleted = index == 0)
            }
        )
        
        assertEquals(33.333333333333336, partiallyCompleted?.getCompletionPercentage())
    }
    
    @Test
    fun `test challenge statistics`() {
        ChallengeSystem.initialize()
        
        val playerData = PlayerChallengeData(
            totalChallengesCompleted = 50,
            achievementUnlocks = mutableSetOf("Achievement1", "Achievement2")
        )
        
        val updatedData = ChallengeSystem.assignChallengesForLevel(10, playerData)
        
        val stats = ChallengeSystem.getChallengeStatistics(updatedData)
        
        assertEquals(50, stats["totalCompleted"])
        assertEquals(2, stats["totalAchievements"])
        assertEquals(10, stats["currentLevel"])
        assertEquals(0, stats["currentCompleted"])
        assertEquals(3, stats["currentTotal"])
        assertEquals(0.0, stats["completionPercentage"])
    }
    
    @Test
    fun `test challenge reward generation`() {
        ChallengeSystem.initialize()
        
        val level10Challenges = ChallengeSystem.getChallengesForLevel(10)
        val challenge = level10Challenges?.challenges?.first()
        
        assertNotNull(challenge?.reward)
        assertTrue(challenge?.reward?.pointsBonus ?: 0 > 0)
        assertTrue(challenge?.reward?.xpBonus ?: 0 > 0)
        assertNotNull(challenge?.reward?.achievement)
    }
    
    @Test
    fun `test challenge name generation varies by tier`() {
        ChallengeSystem.initialize()
        
        val level10Challenges = ChallengeSystem.getChallengesForLevel(10)
        val level10Names = level10Challenges?.challenges?.map { it.name }
        
        val level100Challenges = ChallengeSystem.getChallengesForLevel(100)
        val level100Names = level100Challenges?.challenges?.map { it.name }
        
        // Level 10 should have "Novice" or "Skilled" adjectives
        assertTrue(level10Names?.any { it.contains("Novice") || it.contains("Skilled") } ?: false)
        
        // Level 100 should have "Expert" or "Master" adjectives
        assertTrue(level100Names?.any { it.contains("Expert") || it.contains("Master") } ?: false)
    }
    
    @Test
    fun `test combo challenge tracking`() {
        val progress = ChallengeProgress(comboCount = 0)
        
        val eventData = mapOf("comboCount" to 5)
        val updated = ChallengeSystem.updateProgressForEvent(progress, "combo_achieved", eventData)
        
        assertEquals(5, updated.comboCount)
    }
    
    @Test
    fun `test win streak challenge tracking`() {
        val progress = ChallengeProgress(winStreak = 2)
        
        val eventData = mapOf("currentStreak" to 5)
        val updated = ChallengeSystem.updateProgressForEvent(progress, "game_won", eventData)
        
        assertEquals(5, updated.winStreak)
    }
    
    @Test
    fun `test card placement challenge tracking`() {
        val progress = ChallengeProgress(cardsPlaced = 10)
        
        val eventData = emptyMap<String, Any>()
        val updated = ChallengeSystem.updateProgressForEvent(progress, "card_placed", eventData)
        
        assertEquals(11, updated.cardsPlaced)
    }
    
    @Test
    fun `test perfect round challenge tracking`() {
        val progress = ChallengeProgress(perfectRounds = 1)
        
        val eventData = emptyMap<String, Any>()
        val updated = ChallengeSystem.updateProgressForEvent(progress, "perfect_round", eventData)
        
        assertEquals(2, updated.perfectRounds)
    }
    
    @Test
    fun `test skill unlock challenge tracking`() {
        val progress = ChallengeProgress(skillsUnlocked = mutableSetOf("skill_1"))
        
        val eventData = mapOf("skillId" to "skill_2")
        val updated = ChallengeSystem.updateProgressForEvent(progress, "skill_unlocked", eventData)
        
        assertTrue(updated.skillsUnlocked.contains("skill_2"))
        assertEquals(2, updated.skillsUnlocked.size)
    }
    
    @Test
    fun `test time tracking for speed run challenge`() {
        val progress = ChallengeProgress(timeElapsedSeconds = 60)
        
        val eventData = mapOf("seconds" to 120)
        val updated = ChallengeSystem.updateProgressForEvent(progress, "time_elapsed", eventData)
        
        assertEquals(120, updated.timeElapsedSeconds)
    }
    
    @Test
    fun `test speed run challenge completion`() {
        val progress = ChallengeProgress(timeElapsedSeconds = 45)
        
        val requirements = ChallengeRequirements(maxTimeSeconds = 60)
        
        // 45 seconds is less than 60 second limit - complete!
        assertTrue(progress.isComplete(requirements))
    }
    
    @Test
    fun `test speed run challenge failure`() {
        val progress = ChallengeProgress(timeElapsedSeconds = 75)
        
        val requirements = ChallengeRequirements(maxTimeSeconds = 60)
        
        // 75 seconds is more than 60 second limit - not complete
        assertFalse(progress.isComplete(requirements))
    }
}