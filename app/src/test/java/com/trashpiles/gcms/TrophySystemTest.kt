package com.trashpiles.gcms

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Tests for the Trophy System
 * 
 * Verifies:
 * - Trophy definitions are initialized correctly
 * - Tier determination for levels
 * - Trophy range calculation
 * - Prerequisite checking
 * - Trophy eligibility
 * - Trophy awarding on level-up
 * - Trophy collection management
 * - Bonus calculation
 */
class TrophySystemTest {
    
    private lateinit var testProgress: PlayerProgress
    private val testPlayerId = "test_player"
    
    @Before
    fun setup() {
        // Initialize trophy system for each test
        TrophySystem.initializeTrophies()
        
        // Create test player progress
        testProgress = PlayerProgress(
            playerId = testPlayerId,
            totalSP = 0,
            totalAP = 0,
            totalXP = 0,
            level = 1,
            unlockedSkills = mutableListOf(),
            unlockedAbilities = mutableListOf(),
            matchHistory = mutableListOf(),
            hasPurchasedAny = false
        )
    }
    
    @Test
    fun `Tier determination is correct for all levels`() {
        assertEquals(Tier.LIFE, TrophySystem.getTierForLevel(1))
        assertEquals(Tier.LIFE, TrophySystem.getTierForLevel(3))
        assertEquals(Tier.LIFE, TrophySystem.getTierForLevel(5))
        
        assertEquals(Tier.BEGINNER, TrophySystem.getTierForLevel(6))
        assertEquals(Tier.BEGINNER, TrophySystem.getTierForLevel(10))
        assertEquals(Tier.BEGINNER, TrophySystem.getTierForLevel(20))
        
        assertEquals(Tier.NOVICE, TrophySystem.getTierForLevel(21))
        assertEquals(Tier.NOVICE, TrophySystem.getTierForLevel(35))
        assertEquals(Tier.NOVICE, TrophySystem.getTierForLevel(50))
        
        assertEquals(Tier.HARD, TrophySystem.getTierForLevel(51))
        assertEquals(Tier.HARD, TrophySystem.getTierForLevel(65))
        assertEquals(Tier.HARD, TrophySystem.getTierForLevel(80))
        
        assertEquals(Tier.EXPERT, TrophySystem.getTierForLevel(81))
        assertEquals(Tier.EXPERT, TrophySystem.getTierForLevel(110))
        assertEquals(Tier.EXPERT, TrophySystem.getTierForLevel(140))
        
        assertEquals(Tier.MASTER, TrophySystem.getTierForLevel(141))
        assertEquals(Tier.MASTER, TrophySystem.getTierForLevel(170))
        assertEquals(Tier.MASTER, TrophySystem.getTierForLevel(200))
    }
    
    @Test
    fun `Trophy ranges are correct for each tier`() {
        // Tier 1: Life (1-3 trophies)
        assertTrue(TrophySystem.getTrophyRangeForLevel(1).start >= 1)
        assertTrue(TrophySystem.getTrophyRangeForLevel(1).endInclusive <= 3)
        
        // Tier 2: Beginner (2-5 trophies)
        assertTrue(TrophySystem.getTrophyRangeForLevel(10).start >= 2)
        assertTrue(TrophySystem.getTrophyRangeForLevel(10).endInclusive <= 5)
        
        // Tier 3: Novice (3-7 trophies)
        assertTrue(TrophySystem.getTrophyRangeForLevel(30).start >= 3)
        assertTrue(TrophySystem.getTrophyRangeForLevel(30).endInclusive <= 7)
        
        // Tier 4: Hard (5-10 trophies)
        assertTrue(TrophySystem.getTrophyRangeForLevel(60).start >= 5)
        assertTrue(TrophySystem.getTrophyRangeForLevel(60).endInclusive <= 10)
        
        // Tier 5: Expert (8-15 trophies)
        assertTrue(TrophySystem.getTrophyRangeForLevel(100).start >= 8)
        assertTrue(TrophySystem.getTrophyRangeForLevel(100).endInclusive <= 15)
        
        // Tier 6: Master (10-25 trophies)
        assertTrue(TrophySystem.getTrophyRangeForLevel(150).start >= 10)
        assertTrue(TrophySystem.getTrophyRangeForLevel(150).endInclusive <= 25)
    }
    
    @Test
    fun `Trophy definitions are initialized`() {
        val definitions = TrophySystem.getAllTrophyDefinitions()
        assertTrue(definitions.isNotEmpty())
        assertTrue(definitions.size >= 20) // At least basic trophies
    }
    
    @Test
    fun `Can retrieve trophy definition by ID`() {
        val def = TrophySystem.getTrophyDefinition("first_steps")
        assertNotNull(def)
        assertEquals("First Steps", def.trophy.name)
        assertEquals(Tier.LIFE, def.trophy.tier)
    }
    
    @Test
    fun `Returns null for non-existent trophy`() {
        val def = TrophySystem.getTrophyDefinition("non_existent_trophy")
        assertNull(def)
    }
    
    @Test
    fun `Prerequisite checking works correctly`() {
        // Level requirement
        val levelPrereq = TrophyPrerequisite(requiredLevel = 5)
        assertFalse(levelPrereq.meetsRequirements(testProgress))
        
        testProgress.level = 5
        assertTrue(levelPrereq.meetsRequirements(testProgress))
        
        // Points requirement
        val pointsPrereq = TrophyPrerequisite(minTotalPoints = 100)
        testProgress.level = 1
        testProgress.totalSP = 50
        testProgress.totalAP = 50
        assertTrue(pointsPrereq.meetsRequirements(testProgress))
        
        testProgress.totalSP = 30
        testProgress.totalAP = 40
        assertFalse(pointsPrereq.meetsRequirements(testProgress))
        
        // Abilities requirement
        val abilitiesPrereq = TrophyPrerequisite(minAbilitiesUnlocked = 3)
        testProgress.totalSP = 50
        testProgress.totalAP = 50
        testProgress.unlockedAbilities.addAll(listOf("ability1", "ability2"))
        assertFalse(abilitiesPrereq.meetsRequirements(testProgress))
        
        testProgress.unlockedAbilities.add("ability3")
        assertTrue(abilitiesPrereq.meetsRequirements(testProgress))
        
        // Skills requirement
        val skillsPrereq = TrophyPrerequisite(minSkillsUnlocked = 2)
        testProgress.unlockedSkills.addAll(listOf("skill1"))
        assertFalse(skillsPrereq.meetsRequirements(testProgress))
        
        testProgress.unlockedSkills.add("skill2")
        assertTrue(skillsPrereq.meetsRequirements(testProgress))
    }
    
    @Test
    fun `Eligibility checking identifies eligible trophies`() {
        // Player at Level 2 with no abilities
        testProgress.level = 2
        testProgress.totalSP = 10
        testProgress.totalAP = 10
        
        val eligibility = TrophySystem.checkEligibility(testPlayerId, testProgress)
        
        // Should have at least "First Steps" trophy eligible
        val firstSteps = eligibility.find { it.trophy.id == "first_steps" }
        assertNotNull(firstSteps)
        assertTrue(firstSteps.eligible)
    }
    
    @Test
    fun `Eligibility checking identifies missing requirements`() {
        // Player at Level 1 with no progress
        testProgress.level = 1
        testProgress.totalSP = 0
        testProgress.totalAP = 0
        
        val eligibility = TrophySystem.checkEligibility(testPlayerId, testProgress)
        
        // Most trophies should not be eligible
        val ineligibleTrophies = eligibility.filter { !it.eligible }
        assertTrue(ineligibleTrophies.isNotEmpty())
    }
    
    @Test
    fun `Awards trophies on level up`() {
        // Prepare player for level 2
        testProgress.level = 2
        testProgress.totalSP = 10
        testProgress.totalAP = 10
        testProgress.hasPurchasedAny = true
        
        val result = TrophySystem.awardTrophiesOnLevelUp(testPlayerId, testProgress, 2)
        
        assertEquals(2, result.level)
        assertNotNull(result.trophiesAwarded)
        assertTrue(result.trophiesAwarded.isNotEmpty())
    }
    
    @Test
    fun `Tracks new trophies vs duplicates`() {
        // Prepare player for level 2
        testProgress.level = 2
        testProgress.totalSP = 10
        testProgress.totalAP = 10
        testProgress.hasPurchasedAny = true
        
        // First level-up
        val result1 = TrophySystem.awardTrophiesOnLevelUp(testPlayerId, testProgress, 2)
        val newTrophies1 = result1.newTrophies
        val duplicates1 = result1.duplicateTrophies
        
        // Should have only new trophies (first level-up)
        assertTrue(newTrophies1 > 0)
        assertEquals(0, duplicates1)
        
        // Second level-up (might award duplicates)
        testProgress.level = 3
        val result2 = TrophySystem.awardTrophiesOnLevelUp(testPlayerId, testProgress, 3)
        
        // Total trophies should increase or stay same
        val totalTrophies = TrophySystem.getPlayerTrophyCount(testPlayerId)
        assertTrue(totalTrophies >= newTrophies1)
    }
    
    @Test
    fun `Calculates total XP bonus correctly`() {
        // Prepare player for level 5 (Life Complete trophy)
        testProgress.level = 5
        testProgress.totalSP = 30
        testProgress.totalAP = 30
        testProgress.unlockedAbilities.addAll(listOf("ability1", "ability2"))
        testProgress.hasPurchasedAny = true
        
        val result = TrophySystem.awardTrophiesOnLevelUp(testPlayerId, testProgress, 5)
        
        // Should award Life Complete trophy (50 XP)
        assertTrue(result.totalXPBonus >= 50)
    }
    
    @Test
    fun `Calculates total point bonus correctly`() {
        // Prepare player for level 20 (Beginner Master trophy - 20 AP)
        testProgress.level = 20
        testProgress.totalSP = 100
        testProgress.totalAP = 100
        testProgress.unlockedAbilities.addAll((1..10).map { "ability$it" })
        testProgress.hasPurchasedAny = true
        
        val result = TrophySystem.awardTrophiesOnLevelUp(testPlayerId, testProgress, 20)
        
        // Should award Beginner Master trophy (20 AP)
        assertTrue(result.totalPointBonus >= 20)
    }
    
    @Test
    fun `Player trophy collection is tracked`() {
        assertEquals(0, TrophySystem.getPlayerTrophyCount(testPlayerId))
        
        // Award some trophies
        testProgress.level = 2
        testProgress.totalSP = 10
        testProgress.totalAP = 10
        testProgress.hasPurchasedAny = true
        
        TrophySystem.awardTrophiesOnLevelUp(testPlayerId, testProgress, 2)
        
        assertTrue(TrophySystem.getPlayerTrophyCount(testPlayerId) > 0)
    }
    
    @Test
    fun `Can retrieve player trophies`() {
        // Award trophies
        testProgress.level = 3
        testProgress.totalSP = 20
        testProgress.totalAP = 20
        testProgress.hasPurchasedAny = true
        
        TrophySystem.awardTrophiesOnLevelUp(testPlayerId, testProgress, 3)
        
        val trophies = TrophySystem.getPlayerTrophies(testPlayerId)
        assertTrue(trophies.isNotEmpty())
        assertEquals(Tier.LIFE, trophies.first().tier)
    }
    
    @Test
    fun `Can check if player has specific trophy`() {
        assertFalse(TrophySystem.hasTrophy(testPlayerId, "first_steps"))
        
        // Award trophy
        testProgress.level = 2
        testProgress.totalSP = 10
        testProgress.totalAP = 10
        testProgress.hasPurchasedAny = true
        
        TrophySystem.awardTrophiesOnLevelUp(testPlayerId, testProgress, 2)
        
        assertTrue(TrophySystem.hasTrophy(testPlayerId, "first_steps"))
    }
    
    @Test
    fun `Can remove trophy from player collection`() {
        // Award trophy
        testProgress.level = 2
        testProgress.totalSP = 10
        testProgress.totalAP = 10
        testProgress.hasPurchasedAny = true
        
        TrophySystem.awardTrophiesOnLevelUp(testPlayerId, testProgress, 2)
        
        assertTrue(TrophySystem.hasTrophy(testPlayerId, "first_steps"))
        
        // Remove trophy
        TrophySystem.removeTrophyFromPlayer(testPlayerId, "first_steps")
        
        assertFalse(TrophySystem.hasTrophy(testPlayerId, "first_steps"))
    }
    
    @Test
    fun `Trophy count by rarity is calculated correctly`() {
        // Award multiple trophies at different levels
        testProgress.level = 2
        testProgress.totalSP = 10
        testProgress.totalAP = 10
        testProgress.hasPurchasedAny = true
        
        TrophySystem.awardTrophiesOnLevelUp(testPlayerId, testProgress, 2)
        
        testProgress.level = 5
        testProgress.totalSP = 30
        testProgress.totalAP = 30
        testProgress.unlockedAbilities.addAll(listOf("ability1", "ability2"))
        
        TrophySystem.awardTrophiesOnLevelUp(testPlayerId, testProgress, 5)
        
        val counts = TrophySystem.getPlayerTrophyCountByRarity(testPlayerId)
        assertTrue(counts.isNotEmpty())
        assertTrue(counts.values.sum() > 0)
    }
    
    @Test
    fun `High level trophies have high requirements`() {
        // Prepare player for level 200 (Master Complete)
        testProgress.level = 200
        testProgress.totalSP = 2500
        testProgress.totalAP = 2500
        testProgress.unlockedAbilities.addAll((1..250).map { "ability$it" })
        testProgress.unlockedSkills.addAll((1..25).map { "skill$it" })
        testProgress.hasPurchasedAny = true
        
        val eligibility = TrophySystem.checkEligibility(testPlayerId, testProgress)
        val masterComplete = eligibility.find { it.trophy.id == "master_complete" }
        
        assertNotNull(masterComplete)
        assertTrue(masterComplete.eligible)
    }
    
    @Test
    fun `Legendary trophy requires maximum progress`() {
        // Prepare player with maximum stats
        testProgress.level = 200
        testProgress.totalSP = 3000
        testProgress.totalAP = 3000
        testProgress.unlockedAbilities.addAll((1..300).map { "ability$it" })
        testProgress.unlockedSkills.addAll((1..30).map { "skill$it" })
        testProgress.hasPurchasedAny = true
        
        val eligibility = TrophySystem.checkEligibility(testPlayerId, testProgress)
        val legendary = eligibility.find { it.trophy.id == "trash_piles_legend" }
        
        assertNotNull(legendary)
        assertTrue(legendary.eligible)
        assertEquals(TrophyRarity.LEGENDARY, legendary.trophy.rarity)
    }
}