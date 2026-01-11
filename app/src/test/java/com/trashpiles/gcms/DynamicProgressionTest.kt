package com.trashpiles.gcms

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for the Dynamic Progression System
 * 
 * Verifies:
 * - XP remains 0 until first purchase
 * - Unlocking nodes grants XP
 * - Level recalculates dynamically
 * - Level drops when XP decreases
 * - Score generates points, not XP
 */
class DynamicProgressionTest {
    
    @Test
    fun `XP remains zero until first purchase`() {
        val state = createTestState()
        val progress = state.skillAbilitySystem.getPlayerProgress("player1")
        
        // Initially XP = 0
        assertEquals(0, progress.totalXP)
        assertEquals(1, progress.level)
        assertFalse(progress.hasPurchasedAny)
        
        // Play matches - should earn points, NOT XP
        repeat(5) {
            val result = processMatchCompletion(
                state,
                "player1",
                listOf(createTestHand())
            )
            assertEquals(0, result.xpEarned)  // XP should be 0
        }
        
        // After matches, XP should still be 0
        assertEquals(0, progress.totalXP)
        assertEquals(1, progress.level)  // Still level 1
        assertFalse(progress.hasPurchasedAny)
    }
    
    @Test
    fun `First purchase enables XP and levels player`() {
        val state = createTestState()
        val progress = state.skillAbilitySystem.getPlayerProgress("player1")
        
        // Earn some points
        progress.totalSP = 10
        progress.totalAP = 10
        
        // XP should be 0
        assertEquals(0, progress.totalXP)
        assertEquals(1, progress.level)
        assertFalse(progress.hasPurchasedAny)
        
        // Unlock "Peek" skill (costs 3 SP, grants 10 XP)
        val result = unlockNode(
            state,
            "player1",
            "peek",
            PointType.SKILL
        )
        
        assertTrue(result.success)
        assertEquals(10, result.xpEarned)
        assertEquals(1, result.newLevel)  // Still level 1 (need more XP)
        
        // Check progress
        assertEquals(10, progress.totalXP)
        assertEquals(1, progress.level)
        assertTrue(progress.hasPurchasedAny)
        assertEquals(7, progress.totalSP)  // 10 - 3 = 7
    }
    
    @Test
    fun `Multiple purchases increase level`() {
        val state = createTestState()
        val progress = state.skillAbilitySystem.getPlayerProgress("player1")
        
        // Give plenty of points
        progress.totalSP = 100
        progress.totalAP = 100
        
        // Unlock several nodes
        unlockNode(state, "player1", "peek", PointType.SKILL)        // +10 XP
        unlockNode(state, "player1", "second_chance", PointType.SKILL) // +15 XP
        unlockNode(state, "player1", "jacks_favor", PointType.ABILITY)  // +15 XP
        
        // Total XP: 10 + 15 + 15 = 40
        assertEquals(40, progress.totalXP)
        // Level should be higher than 1
        assertTrue(progress.level > 1)
    }
    
    @Test
    fun `Level recalculates when XP increases`() {
        val state = createTestState()
        val progress = state.skillAbilitySystem.getPlayerProgress("player1")
        
        progress.totalSP = 50
        progress.totalAP = 50
        
        // Unlock node (grants XP)
        unlockNode(state, "player1", "peek", PointType.SKILL)
        val level1 = progress.level
        
        // Unlock more nodes (more XP)
        unlockNode(state, "player1", "second_chance", PointType.SKILL)
        val level2 = progress.level
        
        // Level should increase with more XP
        assertTrue(level2 >= level1)
    }
    
    @Test
    fun `Level drops when XP decreases (selling)`() {
        val state = createTestState()
        val progress = state.skillAbilitySystem.getPlayerProgress("player1")
        
        progress.totalSP = 100
        progress.totalAP = 100
        
        // Unlock enough to reach level 2+
        unlockNode(state, "player1", "peek", PointType.SKILL)
        unlockNode(state, "player1", "second_chance", PointType.SKILL)
        unlockNode(state, "player1", "jacks_favor", PointType.ABILITY)
        unlockNode(state, "player1", "queens_grace", PointType.ABILITY)
        
        val levelBefore = progress.level
        val xpBefore = progress.totalXP
        
        // Sell a node (lose XP)
        val sellResult = sellNode(
            state,
            "player1",
            "queens_grace",
            PointType.ABILITY
        )
        
        assertTrue(sellResult.success)
        assertTrue(sellResult.xpLost!! > 0)
        
        // XP should decrease
        assertTrue(progress.totalXP < xpBefore)
        
        // Level may drop
        val levelAfter = progress.level
        assertTrue(levelAfter <= levelBefore)
    }
    
    @Test
    fun `XP penalty reduces level`() {
        val state = createTestState()
        val progress = state.skillAbilitySystem.getPlayerProgress("player1")
        
        progress.totalSP = 100
        progress.totalAP = 100
        
        // Unlock nodes to gain XP
        unlockNode(state, "player1", "peek", PointType.SKILL)
        unlockNode(state, "player1", "second_chance", PointType.SKILL)
        unlockNode(state, "player1", "jacks_favor", PointType.ABILITY)
        
        val levelBefore = progress.level
        val xpBefore = progress.totalXP
        
        // Apply XP penalty
        val penaltyResult = applyXPPenalty(state, "player1", 15)
        
        assertTrue(penaltyResult.success)
        assertEquals(15, penaltyResult.xpLost)
        
        // XP should decrease
        assertTrue(progress.totalXP < xpBefore)
        
        // Level may drop
        val levelAfter = progress.level
        assertTrue(levelAfter <= levelBefore)
    }
    
    @Test
    fun `Penalty multiplier applies when regaining lost level`() {
        val state = createTestState()
        val progress = state.skillAbilitySystem.getPlayerProgress("player1")
        
        progress.totalSP = 100
        progress.totalAP = 100
        
        // Unlock nodes to gain XP
        unlockNode(state, "player1", "peek", PointType.SKILL)        // +10 XP
        unlockNode(state, "player1", "second_chance", PointType.SKILL) // +15 XP
        
        val initialXP = progress.totalXP
        val initialLevel = progress.level
        
        // Apply penalty
        applyXPPenalty(state, "player1", 10)
        val penaltyXP = progress.totalXP
        val penaltyLevel = progress.level
        
        // Check penalty calculation
        val needed = LevelSystem.getXPToRegainLevel(10)
        // 10 + (10 * 0.1) = 11
        assertEquals(11, needed)
        
        // Regain XP (including penalty)
        progress.addXP(11)
        
        // Should be back at or above initial level
        assertTrue(progress.totalXP >= initialXP - 10)
    }
    
    @Test
    fun `No penalty possible without first purchase`() {
        val state = createTestState()
        val progress = state.skillAbilitySystem.getPlayerProgress("player1")
        
        // Try to apply penalty before any purchase
        val result = applyXPPenalty(state, "player1", 10)
        
        assertFalse(result.success)
        assertEquals("No XP to lose (no purchases made yet)", result.message)
        assertEquals(0, progress.totalXP)
        assertEquals(1, progress.level)
    }
    
    @Test
    fun `Matches grant points but not XP after purchase`() {
        val state = createTestState()
        val progress = state.skillAbilitySystem.getPlayerProgress("player1")
        
        // Make first purchase
        unlockNode(state, "player1", "peek", PointType.SKILL)
        
        val spBefore = progress.totalSP
        val xpBefore = progress.totalXP
        
        // Play match
        val result = processMatchCompletion(
            state,
            "player1",
            listOf(createTestHand())
        )
        
        // Should earn points
        assertTrue(progress.totalSP > spBefore)
        
        // But NOT XP (XP only from purchases)
        assertEquals(0, result.xpEarned)
        assertEquals(xpBefore, progress.totalXP)
    }
    
    @Test
    fun `Dynamic level ceiling (unlimited progression)`() {
        val state = createTestState()
        val progress = state.skillAbilitySystem.getPlayerProgress("player1")
        
        // Give huge amount of points
        progress.totalSP = 10000
        progress.totalAP = 10000
        
        // Unlock many nodes to gain XP
        SkillTree.nodes.forEach { node ->
            if (progress.canAfford(node.cost, PointType.SKILL)) {
                unlockNode(state, "player1", node.id, PointType.SKILL)
            }
        }
        
        AbilityTree.nodes.forEach { node ->
            if (progress.canAfford(node.cost, PointType.ABILITY)) {
                unlockNode(state, "player1", node.id, PointType.ABILITY)
            }
        }
        
        // Should have significant XP and level
        assertTrue(progress.totalXP > 0)
        assertTrue(progress.level > 1)
        
        // Can still level up by unlocking more nodes
        // (no hard cap)
        assertTrue(progress.level < 100)  // Reasonable for now
    }
    
    // Helper functions
    
    private fun createTestState(): GCMSState {
        return GCMSState(
            players = listOf(
                PlayerState(
                    id = 1,
                    name = "Player 1",
                    hand = createTestHand()
                )
            ),
            deck = createTestDeck(),
            discardPile = emptyList(),
            currentPhase = GamePhase.SETUP,
            currentTurn = 0
        )
    }
    
    private fun createTestHand(): List<CardState> {
        return (1..10).map { slot ->
            CardState(
                id = "card_$slot",
                rank = if (slot == 1) "Ace" else (slot - 1).toString(),
                suit = "Hearts",
                isFaceUp = slot <= 5,  // First 5 face up
                slot = slot
            )
        }
    }
    
    private fun createTestDeck(): List<CardState> {
        val suits = listOf("Hearts", "Diamonds", "Clubs", "Spades")
        val ranks = listOf("Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King")
        
        return suits.flatMap { suit ->
            ranks.map { rank ->
                CardState(
                    id = "${rank}_$suit",
                    rank = rank,
                    suit = suit,
                    isFaceUp = false,
                    slot = 0
                )
            }
        }
    }
}