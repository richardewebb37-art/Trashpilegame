package com.trashpiles.gcms

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Unit tests for Skill & Ability Points System
 */
class SkillAbilitySystemTest {
    
    private lateinit var state: GCMSState
    private lateinit var player: PlayerState
    
    @Before
    fun setup() {
        player = PlayerState(
            id = 0,
            name = "Test Player",
            hand = emptyList(),
            score = 0,
            isAI = false,
            hasFinished = false
        )
        
        state = GCMSState(
            currentPhase = GamePhase.PLAYING,
            players = listOf(player),
            currentPlayerIndex = 0,
            deck = emptyList(),
            discardPile = emptyList()
        )
    }
    
    // ========================================================================
    // UNLIMITED LEVELING SYSTEM TESTS
    // ========================================================================
    
    @Test
    fun `calculateLevel - level 1 with 0 XP`() {
        assertEquals(1, LevelSystem.calculateLevel(0))
    }
    
    @Test
    fun `calculateLevel - level 2 with small XP`() {
        val xp = LevelSystem.getXPForLevel(2)
        val level = LevelSystem.calculateLevel(xp)
        assertTrue(level >= 2)
    }
    
    @Test
    fun `calculateLevel - level 10 with moderate XP`() {
        val xp = LevelSystem.getXPForLevel(10)
        val level = LevelSystem.calculateLevel(xp)
        assertTrue(level >= 10)
    }
    
    @Test
    fun `calculateLevel - level 50 with high XP`() {
        val xp = LevelSystem.getXPForLevel(50)
        val level = LevelSystem.calculateLevel(xp)
        assertTrue(level >= 50)
    }
    
    @Test
    fun `calculateLevel - level 100 with very high XP`() {
        val xp = LevelSystem.getXPForLevel(100)
        val level = LevelSystem.calculateLevel(xp)
        assertTrue(level >= 100)
    }
    
    @Test
    fun `calculateLevel - unlimited progression works`() {
        // Test that we can calculate levels for very high XP values
        val level1000 = LevelSystem.calculateLevel(1_000_000)
        assertTrue(level1000 > 100)
        
        val level10000 = LevelSystem.calculateLevel(10_000_000)
        assertTrue(level10000 > level1000)
    }
    
    @Test
    fun `calculateXP - includes match bonus`() {
        val xp = LevelSystem.calculateXP(
            spEarned = 10,
            apEarned = 10,
            currentLevel = 1,
            totalMatches = 5,
            totalRounds = 1
        )
        
        // Base: (10+10) × 1.05 = 21
        // Match bonus: 5 × 10 = 50
        // Round bonus: 1 × 50 = 50
        // Total: 121
        assertTrue(xp >= 120) // Base + match + round
    }
    
    @Test
    fun `calculateXP - includes round bonus`() {
        val xp1 = LevelSystem.calculateXP(10, 10, 1, 1, 1)
        val xp5 = LevelSystem.calculateXP(10, 10, 1, 1, 5)
        
        // Round 5 should have significantly more XP
        assertTrue(xp5 > xp1 + 150) // At least 4 extra rounds × 50
    }
    
    @Test
    fun `calculateXP - level multiplier increases XP`() {
        val xpLevel1 = LevelSystem.calculateXP(10, 10, 1, 1, 1)
        val xpLevel10 = LevelSystem.calculateXP(10, 10, 10, 1, 1)
        val xpLevel50 = LevelSystem.calculateXP(10, 10, 50, 1, 1)
        
        // Higher levels should earn more XP
        assertTrue(xpLevel10 > xpLevel1)
        assertTrue(xpLevel50 > xpLevel10)
    }
    
    @Test
    fun `PlayerProgress - levels up with match result`() {
        val progress = PlayerProgress("test_player")
        val initialLevel = progress.level
        
        // Create a match result with substantial XP
        val result = MatchResult(
            matchNumber = 1,
            roundNumber = 1,
            won = true,
            spEarned = 50,
            apEarned = 50,
            xpEarned = 200,
            penalties = emptyList()
        )
        
        val leveledUp = progress.addMatchResult(result)
        
        // Should have leveled up
        assertTrue(leveledUp || progress.level > initialLevel)
        assertEquals(50, progress.totalSP)
        assertEquals(50, progress.totalAP)
    }
    
    @Test
    fun `PlayerProgress - tracks matches and rounds`() {
        val progress = PlayerProgress("test_player")
        
        // Add multiple matches
        repeat(10) { matchNum ->
            val result = MatchResult(
                matchNumber = matchNum + 1,
                roundNumber = 1,
                won = true,
                spEarned = 10,
                apEarned = 10,
                xpEarned = 50,
                penalties = emptyList()
            )
            progress.addMatchResult(result)
        }
        
        assertEquals(10, progress.matchHistory.size)
        assertTrue(progress.totalXP > 0)
    }
    
    @Test
    fun `getXPToNextLevel - returns 0 for very high levels`() {
        // This test verifies the function handles any level
        val xpAtLevel50 = LevelSystem.getXPForLevel(50)
        val xpNeeded = LevelSystem.getXPToNextLevel(xpAtLevel50, 50)
        
        // Should return a positive value (or 0 if at max, which doesn't exist in unlimited system)
        assertTrue(xpNeeded >= 0)
    }
    
    @Test
    fun `Level progression - consistent scaling`() {
        val xpLevel1 = LevelSystem.getXPForLevel(1)
        val xpLevel10 = LevelSystem.getXPForLevel(10)
        val xpLevel20 = LevelSystem.getXPForLevel(20)
        val xpLevel50 = LevelSystem.getXPForLevel(50)
        
        // XP should increase exponentially
        assertTrue(xpLevel10 > xpLevel1)
        assertTrue(xpLevel20 > xpLevel10)
        assertTrue(xpLevel50 > xpLevel20)
        
        // Growth should be accelerating
        val growth1to10 = xpLevel10 - xpLevel1
        val growth10to20 = xpLevel20 - xpLevel10
        val growth20to50 = xpLevel50 - xpLevel20
        
        assertTrue(growth10to20 > growth1to10)
        assertTrue(growth20to50 > growth10to20)
    }

    // ========================================================================
    // ROUND SCORING TESTS (Face-UP Cards Only)
    // ========================================================================
    
    @Test
    fun `calculateRoundScore - winner with all cards face-up`() {
        val hand = listOf(
            CardState("Ace", "Spades", 1, isFaceUp = true),
            CardState("Two", "Hearts", 2, isFaceUp = true),
            CardState("Three", "Clubs", 3, isFaceUp = true),
            CardState("Four", "Diamonds", 4, isFaceUp = true),
            CardState("Five", "Spades", 5, isFaceUp = true),
            CardState("Six", "Hearts", 6, isFaceUp = true),
            CardState("Seven", "Clubs", 7, isFaceUp = true),
            CardState("Eight", "Diamonds", 8, isFaceUp = true),
            CardState("Nine", "Spades", 9, isFaceUp = true),
            CardState("Ten", "Hearts", 10, isFaceUp = true)
        )
        
        val result = calculateRoundScore(
            hand = hand,
            isWinner = true,
            lastCardFlippedSlot = 10,
            diceRoll = 6
        )
        
        assertEquals(55, result.baseScore) // 1+2+3+4+5+6+7+8+9+10
        assertEquals(10, result.multiplier) // Last card slot
        assertEquals(6, result.diceRoll)
        assertEquals(60, result.bonus) // 10 × 6
        assertEquals(115, result.finalScore) // 55 + 60
    }
    
    @Test
    fun `calculateRoundScore - loser with 3 face-up cards`() {
        val hand = listOf(
            CardState("Ace", "Spades", 1, isFaceUp = true),
            CardState("Two", "Hearts", 2, isFaceUp = false),
            CardState("Three", "Clubs", 3, isFaceUp = false),
            CardState("Four", "Diamonds", 4, isFaceUp = false),
            CardState("Five", "Spades", 5, isFaceUp = true),
            CardState("Six", "Hearts", 6, isFaceUp = false),
            CardState("Seven", "Clubs", 7, isFaceUp = true),
            CardState("Eight", "Diamonds", 8, isFaceUp = false),
            CardState("Nine", "Spades", 9, isFaceUp = false),
            CardState("Ten", "Hearts", 10, isFaceUp = false)
        )
        
        val result = calculateRoundScore(
            hand = hand,
            isWinner = false,
            lastCardFlippedSlot = null,
            diceRoll = 4
        )
        
        assertEquals(13, result.baseScore) // 1 + 5 + 7
        assertEquals(3, result.multiplier) // 3 face-up cards
        assertEquals(4, result.diceRoll)
        assertEquals(12, result.bonus) // 3 × 4
        assertEquals(25, result.finalScore) // 13 + 12
    }
    
    @Test
    fun `calculateRoundScore - joker face-up gives bonus`() {
        val hand = listOf(
            CardState("Ace", "Spades", 1, isFaceUp = true),
            CardState("Two", "Hearts", 2, isFaceUp = false),
            CardState("Three", "Clubs", 3, isFaceUp = false),
            CardState("Four", "Diamonds", 4, isFaceUp = false),
            CardState("Five", "Spades", 5, isFaceUp = true),
            CardState("Six", "Hearts", 6, isFaceUp = false),
            CardState("Joker", "Special", 7, isFaceUp = true), // Joker face-up
            CardState("Eight", "Diamonds", 8, isFaceUp = false),
            CardState("Nine", "Spades", 9, isFaceUp = false),
            CardState("Ten", "Hearts", 10, isFaceUp = false)
        )
        
        val result = calculateRoundScore(
            hand = hand,
            isWinner = false,
            lastCardFlippedSlot = null,
            diceRoll = 3
        )
        
        assertEquals(33, result.baseScore) // 1 + 5 + 7 + 20 (Joker bonus)
        assertEquals(3, result.multiplier) // 3 face-up cards
        assertEquals(3, result.diceRoll)
        assertEquals(9, result.bonus) // 3 × 3
        assertEquals(42, result.finalScore) // 33 + 9
    }
    
    @Test
    fun `calculateRoundScore - face-down cards dont count`() {
        val hand = listOf(
            CardState("Ace", "Spades", 1, isFaceUp = false),
            CardState("Two", "Hearts", 2, isFaceUp = false),
            CardState("Three", "Clubs", 3, isFaceUp = false),
            CardState("Four", "Diamonds", 4, isFaceUp = false),
            CardState("Five", "Spades", 5, isFaceUp = false),
            CardState("Six", "Hearts", 6, isFaceUp = false),
            CardState("Seven", "Clubs", 7, isFaceUp = false),
            CardState("Eight", "Diamonds", 8, isFaceUp = false),
            CardState("Nine", "Spades", 9, isFaceUp = false),
            CardState("Ten", "Hearts", 10, isFaceUp = false)
        )
        
        val result = calculateRoundScore(
            hand = hand,
            isWinner = false,
            lastCardFlippedSlot = null,
            diceRoll = 6
        )
        
        assertEquals(0, result.baseScore) // No face-up cards
        assertEquals(0, result.multiplier) // 0 face-up cards
        assertEquals(6, result.diceRoll)
        assertEquals(0, result.bonus) // 0 × 6
        assertEquals(0, result.finalScore)
    }
    
    @Test
    fun `calculateRoundScore - winner multiplier uses last card slot`() {
        val hand = listOf(
            CardState("Ace", "Spades", 1, isFaceUp = true),
            CardState("Two", "Hearts", 2, isFaceUp = true),
            CardState("Three", "Clubs", 3, isFaceUp = true),
            CardState("Four", "Diamonds", 4, isFaceUp = true),
            CardState("Five", "Spades", 5, isFaceUp = true),
            CardState("Six", "Hearts", 6, isFaceUp = true),
            CardState("Seven", "Clubs", 7, isFaceUp = true),
            CardState("Eight", "Diamonds", 8, isFaceUp = true),
            CardState("Nine", "Spades", 9, isFaceUp = true),
            CardState("Ten", "Hearts", 10, isFaceUp = true)
        )
        
        // Test with last card = slot 1 (low multiplier)
        val result1 = calculateRoundScore(
            hand = hand,
            isWinner = true,
            lastCardFlippedSlot = 1,
            diceRoll = 6
        )
        
        assertEquals(55, result1.baseScore)
        assertEquals(1, result1.multiplier) // Last card slot 1
        assertEquals(6, result1.bonus) // 1 × 6
        assertEquals(61, result1.finalScore)
        
        // Test with last card = slot 10 (high multiplier)
        val result10 = calculateRoundScore(
            hand = hand,
            isWinner = true,
            lastCardFlippedSlot = 10,
            diceRoll = 6
        )
        
        assertEquals(55, result10.baseScore)
        assertEquals(10, result10.multiplier) // Last card slot 10
        assertEquals(60, result10.bonus) // 10 × 6
        assertEquals(115, result10.finalScore)
    }
    
    // ========================================================================
    // AP PENALTY TESTS (Face-DOWN Cards Only)
    // ========================================================================
    
    @Test
    fun `calculateMatchAPPenalties - no face-down face cards`() {
        val round1 = listOf(
            CardState("Ace", "Spades", 1, isFaceUp = true),
            CardState("Two", "Hearts", 2, isFaceUp = true),
            CardState("Three", "Clubs", 3, isFaceUp = false),
            CardState("Four", "Diamonds", 4, isFaceUp = false)
        )
        
        val matchHistory = listOf(round1)
        val penalty = calculateMatchAPPenalties(matchHistory)
        
        assertEquals(0, penalty) // No face cards face-down
    }
    
    @Test
    fun `calculateMatchAPPenalties - King face-down gives -3 AP`() {
        val round1 = listOf(
            CardState("King", "Spades", 13, isFaceUp = false),
            CardState("Two", "Hearts", 2, isFaceUp = true)
        )
        
        val matchHistory = listOf(round1)
        val penalty = calculateMatchAPPenalties(matchHistory)
        
        assertEquals(-3, penalty)
    }
    
    @Test
    fun `calculateMatchAPPenalties - Joker face-down gives -20 AP`() {
        val round1 = listOf(
            CardState("Joker", "Special", 7, isFaceUp = false),
            CardState("Two", "Hearts", 2, isFaceUp = true)
        )
        
        val matchHistory = listOf(round1)
        val penalty = calculateMatchAPPenalties(matchHistory)
        
        assertEquals(-20, penalty) // Joker is devastating!
    }
    
    @Test
    fun `calculateMatchAPPenalties - multiple rounds accumulate`() {
        val round1 = listOf(
            CardState("King", "Spades", 13, isFaceUp = false), // -3
            CardState("Two", "Hearts", 2, isFaceUp = true)
        )
        
        val round2 = listOf(
            CardState("Queen", "Hearts", 12, isFaceUp = false), // -2
            CardState("Jack", "Clubs", 11, isFaceUp = false)    // -1
        )
        
        val round3 = listOf(
            CardState("Joker", "Special", 5, isFaceUp = false)  // -20
        )
        
        val matchHistory = listOf(round1, round2, round3)
        val penalty = calculateMatchAPPenalties(matchHistory)
        
        assertEquals(-26, penalty) // -3 + -2 + -1 + -20
    }
    
    @Test
    fun `calculateMatchAPPenalties - face-up cards dont penalize`() {
        val round1 = listOf(
            CardState("King", "Spades", 13, isFaceUp = true),   // No penalty (face-up)
            CardState("Queen", "Hearts", 12, isFaceUp = true),  // No penalty (face-up)
            CardState("Jack", "Clubs", 11, isFaceUp = false)    // -1 (face-down)
        )
        
        val matchHistory = listOf(round1)
        val penalty = calculateMatchAPPenalties(matchHistory)
        
        assertEquals(-1, penalty) // Only Jack counts
    }
    
    // ========================================================================
    // MATCH REWARDS TESTS
    // ========================================================================
    
    @Test
    fun `MatchRewards - match 1 gives 1 SP and 0 AP`() {
        assertEquals(1, MatchRewards.getBaseSP(1))
        assertEquals(0, MatchRewards.getBaseAP(1))
    }
    
    @Test
    fun `MatchRewards - match 5 gives 3 SP and 3 AP`() {
        assertEquals(3, MatchRewards.getBaseSP(5))
        assertEquals(3, MatchRewards.getBaseAP(5))
    }
    
    @Test
    fun `MatchRewards - match 10 gives 5 SP and 5 AP`() {
        assertEquals(5, MatchRewards.getBaseSP(10))
        assertEquals(5, MatchRewards.getBaseAP(10))
    }
    
    @Test
    fun `MatchRewards - total possible SP is 30`() {
        assertEquals(30, MatchRewards.getTotalPossibleSP())
    }
    
    @Test
    fun `MatchRewards - total possible AP is 29`() {
        assertEquals(29, MatchRewards.getTotalPossibleAP())
    }
    
    @Test
    fun `MatchRewards - progression increases over matches`() {
        val sp1 = MatchRewards.getBaseSP(1)
        val sp5 = MatchRewards.getBaseSP(5)
        val sp10 = MatchRewards.getBaseSP(10)
        
        assertTrue(sp5 > sp1)
        assertTrue(sp10 > sp5)
    }
    
    // ========================================================================
    // MATCH COMPLETION TESTS
    // ========================================================================
    
    @Test
    fun `processMatchCompletion - perfect match with no penalties`() {
        val matchHistory = listOf(
            // Round 1: All face-up (no penalties)
            listOf(
                CardState("Ace", "Spades", 1, isFaceUp = true),
                CardState("Two", "Hearts", 2, isFaceUp = true),
                CardState("Three", "Clubs", 3, isFaceUp = true)
            ),
            // Round 2: All face-up (no penalties)
            listOf(
                CardState("Four", "Diamonds", 4, isFaceUp = true),
                CardState("Five", "Spades", 5, isFaceUp = true)
            )
        )
        
        val result = processMatchCompletion(state, "0", 5, matchHistory)
        
        assertEquals(5, result.matchNumber)
        assertEquals(true, result.won)
        assertEquals(3, result.spEarned) // Match 5 base SP
        assertEquals(3, result.apEarned) // Match 5 base AP - 0 penalty
        assertTrue(result.penalties.isEmpty())
    }
    
    @Test
    fun `processMatchCompletion - match with high penalties loses all AP`() {
        val matchHistory = listOf(
            // Round 1: King and Queen face-down
            listOf(
                CardState("King", "Spades", 13, isFaceUp = false),    // -3
                CardState("Queen", "Hearts", 12, isFaceUp = false),   // -2
                CardState("Three", "Clubs", 3, isFaceUp = true)
            ),
            // Round 2: Jack and another King face-down
            listOf(
                CardState("Jack", "Clubs", 11, isFaceUp = false),     // -1
                CardState("King", "Hearts", 13, isFaceUp = false),    // -3
                CardState("Five", "Spades", 5, isFaceUp = true)
            ),
            // Round 3: Joker face-down (devastating!)
            listOf(
                CardState("Joker", "Special", 7, isFaceUp = false)    // -20
            )
        )
        
        val result = processMatchCompletion(state, "0", 3, matchHistory)
        
        assertEquals(3, result.matchNumber)
        assertEquals(2, result.spEarned) // Match 3 base SP (not affected)
        assertEquals(0, result.apEarned) // Match 3 base AP (2) + (-29 penalty) = 0
        assertEquals(5, result.penalties.size) // K, Q, J, K, Joker
    }
    
    @Test
    fun `processMatchCompletion - strategic play minimizes penalties`() {
        val matchHistory = listOf(
            // Round 1: Only one King face-down
            listOf(
                CardState("King", "Spades", 13, isFaceUp = false),    // -3
                CardState("Two", "Hearts", 2, isFaceUp = true),
                CardState("Three", "Clubs", 3, isFaceUp = true)
            ),
            // Round 2: All face-up (good play!)
            listOf(
                CardState("Four", "Diamonds", 4, isFaceUp = true),
                CardState("Five", "Spades", 5, isFaceUp = true)
            )
        )
        
        val result = processMatchCompletion(state, "0", 9, matchHistory)
        
        assertEquals(9, result.matchNumber)
        assertEquals(5, result.spEarned) // Match 9 base SP
        assertEquals(2, result.apEarned) // Match 9 base AP (5) + (-3 penalty) = 2
        assertEquals(1, result.penalties.size)
        
        // Verify minimal penalty
        val totalPenalty = result.penalties.sumOf { it.penalty }
        assertEquals(-3, totalPenalty) // Only one King
    }
    
    @Test
    fun `processMatchCompletion - updates player progress`() {
        val matchHistory = listOf(
            listOf(CardState("Ace", "Spades", 1, isFaceUp = true))
        )
        
        // Process match
        processMatchCompletion(state, "0", 5, matchHistory)
        
        // Check progress was updated
        val progress = state.skillAbilitySystem.getPlayerProgress("0")
        assertEquals(3, progress.totalSP)
        assertEquals(3, progress.totalAP)
        assertEquals(1, progress.matchHistory.size)
    }
    
    @Test
    fun `processMatchCompletion - advances match number`() {
        val matchHistory = listOf(
            listOf(CardState("Ace", "Spades", 1, isFaceUp = true))
        )
        
        assertEquals(1, state.skillAbilitySystem.currentMatch)
        
        processMatchCompletion(state, "0", 1, matchHistory)
        
        assertEquals(2, state.skillAbilitySystem.currentMatch)
    }
    
    @Test
    fun `processMatchCompletion - joker disaster scenario`() {
        val matchHistory = listOf(
            // Round 1: Perfect
            listOf(
                CardState("Ace", "Spades", 1, isFaceUp = true),
                CardState("Two", "Hearts", 2, isFaceUp = true)
            ),
            // Round 2: JOKER FACE-DOWN (disaster!)
            listOf(
                CardState("Joker", "Special", 5, isFaceUp = false),   // -20 AP!
                CardState("Three", "Clubs", 3, isFaceUp = true)
            )
        )
        
        val result = processMatchCompletion(state, "0", 3, matchHistory)
        
        assertEquals(3, result.matchNumber)
        assertEquals(2, result.spEarned) // Match 3 base SP
        assertEquals(0, result.apEarned) // Match 3 base AP (2) + (-20) = 0
        assertEquals(1, result.penalties.size)
        assertEquals("Joker", result.penalties[0].cardRank)
        assertEquals(-20, result.penalties[0].penalty)
    }
    
    // ========================================================================
    // NODE UNLOCK TESTS
    // ========================================================================
    
    @Test
    fun `unlockNode - successful unlock with sufficient points`() {
        val progress = state.skillAbilitySystem.getPlayerProgress("0")
        progress.totalSP = 5
        
        val result = unlockNode(state, "0", "quick_draw", PointType.SKILL)
        
        assertTrue(result.success)
        assertEquals("quick_draw", result.nodeId)
        assertEquals("Quick Draw", result.nodeName)
        assertEquals(5, result.pointsSpent)
        assertEquals(0, progress.totalSP) // 5 - 5 = 0
        assertTrue(progress.unlockedSkills.contains("quick_draw"))
    }
    
    @Test
    fun `unlockNode - fails with insufficient points`() {
        val progress = state.skillAbilitySystem.getPlayerProgress("0")
        progress.totalSP = 2
        
        val result = unlockNode(state, "0", "quick_draw", PointType.SKILL)
        
        assertFalse(result.success)
        assertTrue(result.message.contains("Insufficient points"))
        assertEquals(2, progress.totalSP) // Unchanged
        assertFalse(progress.unlockedSkills.contains("quick_draw"))
    }
    
    @Test
    fun `unlockNode - fails with unmet prerequisites`() {
        val progress = state.skillAbilitySystem.getPlayerProgress("0")
        progress.totalSP = 10
        
        val result = unlockNode(state, "0", "quick_draw", PointType.SKILL)
        
        assertFalse(result.success)
        assertTrue(result.message.contains("Prerequisites not met"))
        assertEquals(10, progress.totalSP) // Unchanged
    }
    
    @Test
    fun `unlockNode - succeeds with prerequisites met`() {
        val progress = state.skillAbilitySystem.getPlayerProgress("0")
        progress.totalSP = 10
        progress.unlockedSkills.add("starter_boost") // Prerequisite
        
        val result = unlockNode(state, "0", "quick_draw", PointType.SKILL)
        
        assertTrue(result.success)
        assertEquals(5, progress.totalSP) // 10 - 5 = 5
        assertTrue(progress.unlockedSkills.contains("quick_draw"))
    }
    
    @Test
    fun `unlockNode - fails if already unlocked`() {
        val progress = state.skillAbilitySystem.getPlayerProgress("0")
        progress.totalSP = 10
        progress.unlockedSkills.add("starter_boost")
        
        // Unlock once
        unlockNode(state, "0", "starter_boost", PointType.SKILL)
        
        // Try to unlock again
        val result = unlockNode(state, "0", "starter_boost", PointType.SKILL)
        
        assertFalse(result.success)
        assertTrue(result.message.contains("already unlocked"))
    }
    
    @Test
    fun `unlockNode - ability nodes use AP not SP`() {
        val progress = state.skillAbilitySystem.getPlayerProgress("0")
        progress.totalAP = 5
        progress.totalSP = 0
        
        val result = unlockNode(state, "0", "peek", PointType.ABILITY)
        
        assertTrue(result.success)
        assertEquals(3, progress.totalAP) // 5 - 2 = 3
        assertEquals(0, progress.totalSP) // Unchanged
        assertTrue(progress.unlockedAbilities.contains("peek"))
    }
    
    // ========================================================================
    // SKILL/ABILITY TREE TESTS
    // ========================================================================
    
    @Test
    fun `SkillTree - has 5 nodes`() {
        assertEquals(5, SkillTree.nodes.size)
    }
    
    @Test
    fun `SkillTree - starter nodes have no prerequisites`() {
        val starterBoost = SkillTree.getNode("starter_boost")
        val memory = SkillTree.getNode("memory")
        
        assertNotNull(starterBoost)
        assertNotNull(memory)
        assertTrue(starterBoost!!.prerequisites.isEmpty())
        assertTrue(memory!!.prerequisites.isEmpty())
    }
    
    @Test
    fun `SkillTree - skill_mastery requires both tier 2 skills`() {
        val skillMastery = SkillTree.getNode("skill_mastery")
        
        assertNotNull(skillMastery)
        assertEquals(2, skillMastery!!.prerequisites.size)
        assertTrue(skillMastery.prerequisites.contains("quick_draw"))
        assertTrue(skillMastery.prerequisites.contains("card_sight"))
    }
    
    @Test
    fun `AbilityTree - has 5 nodes`() {
        assertEquals(5, AbilityTree.nodes.size)
    }
    
    @Test
    fun `AbilityTree - ultimate_power requires both tier 2 abilities`() {
        val ultimatePower = AbilityTree.getNode("ultimate_power")
        
        assertNotNull(ultimatePower)
        assertEquals(2, ultimatePower!!.prerequisites.size)
        assertTrue(ultimatePower.prerequisites.contains("wild_card"))
        assertTrue(ultimatePower.prerequisites.contains("swap_master"))
    }
    
    @Test
    fun `getAvailableNodes - returns only affordable unlockable nodes`() {
        val progress = state.skillAbilitySystem.getPlayerProgress("0")
        progress.totalSP = 3
        
        val available = SkillTree.getAvailableNodes(progress)
        
        // Should only return starter_boost and memory (both cost 2)
        assertEquals(2, available.size)
        assertTrue(available.any { it.id == "starter_boost" })
        assertTrue(available.any { it.id == "memory" })
    }
    
    @Test
    fun `getAvailableNodes - excludes already unlocked nodes`() {
        val progress = state.skillAbilitySystem.getPlayerProgress("0")
        progress.totalSP = 10
        progress.unlockedSkills.add("starter_boost")
        
        val available = SkillTree.getAvailableNodes(progress)
        
        // Should not include starter_boost
        assertFalse(available.any { it.id == "starter_boost" })
    }
    
    // ========================================================================
    // PLAYER PROGRESS TESTS
    // ========================================================================
    
    @Test
    fun `PlayerProgress - starts with zero points`() {
        val progress = PlayerProgress("test")
        
        assertEquals(0, progress.totalSP)
        assertEquals(0, progress.totalAP)
        assertTrue(progress.unlockedSkills.isEmpty())
        assertTrue(progress.unlockedAbilities.isEmpty())
        assertTrue(progress.matchHistory.isEmpty())
    }
    
    @Test
    fun `PlayerProgress - addMatchResult updates totals`() {
        val progress = PlayerProgress("test")
        val result = MatchResult(
            matchNumber = 1,
            won = true,
            spEarned = 1,
            apEarned = 0,
            penalties = emptyList()
        )
        
        progress.addMatchResult(result)
        
        assertEquals(1, progress.totalSP)
        assertEquals(0, progress.totalAP)
        assertEquals(1, progress.matchHistory.size)
    }
    
    @Test
    fun `PlayerProgress - canAfford checks correct point type`() {
        val progress = PlayerProgress("test")
        progress.totalSP = 5
        progress.totalAP = 2
        
        assertTrue(progress.canAfford(5, PointType.SKILL))
        assertFalse(progress.canAfford(6, PointType.SKILL))
        assertTrue(progress.canAfford(2, PointType.ABILITY))
        assertFalse(progress.canAfford(3, PointType.ABILITY))
    }
    
    @Test
    fun `PlayerProgress - deductPoints reduces correct total`() {
        val progress = PlayerProgress("test")
        progress.totalSP = 10
        progress.totalAP = 5
        
        progress.deductPoints(3, PointType.SKILL)
        progress.deductPoints(2, PointType.ABILITY)
        
        assertEquals(7, progress.totalSP)
        assertEquals(3, progress.totalAP)
    }
    
    // ========================================================================
    // INTEGRATION TESTS
    // ========================================================================
    
    @Test
    fun `Full session - player completes 10 matches with varying penalties`() {
        val progress = state.skillAbilitySystem.getPlayerProgress("0")
        
        // Simulate 10 matches with different AP penalty scenarios
        val matchScenarios = listOf(
            Pair(1, 0),    // Match 1: No penalties
            Pair(2, -1),   // Match 2: 1 Jack face-down
            Pair(3, -5),   // Match 3: King + Queen face-down
            Pair(4, 0),    // Match 4: No penalties
            Pair(5, -10),  // Match 5: Multiple face cards
            Pair(6, -2),   // Match 6: 1 Queen face-down
            Pair(7, 0),    // Match 7: No penalties
            Pair(8, -3),   // Match 8: 1 King face-down
            Pair(9, -20),  // Match 9: JOKER face-down!
            Pair(10, 0)    // Match 10: No penalties
        )
        
        var totalSP = 0
        var totalAP = 0
        
        matchScenarios.forEach { (matchNum, apPenalty) ->
            val baseSP = MatchRewards.getBaseSP(matchNum)
            val baseAP = MatchRewards.getBaseAP(matchNum)
            
            val spEarned = baseSP
            val apEarned = maxOf(0, baseAP + apPenalty) // apPenalty is negative
            
            totalSP += spEarned
            totalAP += apEarned
            
            val result = MatchResult(
                matchNumber = matchNum,
                won = true,
                spEarned = spEarned,
                apEarned = apEarned,
                penalties = emptyList()
            )
            
            progress.addMatchResult(result)
        }
        
        assertEquals(30, progress.totalSP) // All SP earned
        assertTrue(progress.totalAP < 29) // Some AP lost to penalties
        assertEquals(10, progress.matchHistory.size)
    }
    
    @Test
    fun `Full progression - unlock entire skill tree`() {
        val progress = state.skillAbilitySystem.getPlayerProgress("0")
        progress.totalSP = 30 // Max possible
        
        // Unlock tier 1
        unlockNode(state, "0", "starter_boost", PointType.SKILL)
        unlockNode(state, "0", "memory", PointType.SKILL)
        
        // Unlock tier 2
        unlockNode(state, "0", "quick_draw", PointType.SKILL)
        unlockNode(state, "0", "card_sight", PointType.SKILL)
        
        // Unlock tier 3
        val result = unlockNode(state, "0", "skill_mastery", PointType.SKILL)
        
        assertTrue(result.success)
        assertEquals(5, progress.unlockedSkills.size)
        assertEquals(6, progress.totalSP) // 30 - 2 - 2 - 5 - 5 - 10 = 6
    }
}