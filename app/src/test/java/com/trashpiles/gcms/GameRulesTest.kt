package com.trashpiles.gcms

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for GameRules
 * Tests Trash game logic and validation
 */
class GameRulesTest {
    
    private fun createTestState(
        players: List<PlayerState> = emptyList(),
        currentPlayerIndex: Int = 0,
        currentPhase: GamePhase = GamePhase.PLAYING
    ): GCMSState {
        return GCMSState(
            currentPhase = currentPhase,
            currentPlayerIndex = currentPlayerIndex,
            players = players,
            deck = emptyList(),
            discardPile = emptyList(),
            isInputLocked = false
        )
    }
    
    @Test
    fun `test ace goes in slot 0`() {
        val aceCard = CardState("ace_of_hearts", "ace", Suit.HEARTS, false)
        val player = PlayerState(
            id = 0,
            name = "Player",
            isAI = false,
            hand = List(10) { CardState("card_$it", "unknown", Suit.HEARTS, false) },
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        val result = GameRules.validateMove(state, 0, aceCard, 0)
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `test two goes in slot 1`() {
        val twoCard = CardState("two_of_hearts", "two", Suit.HEARTS, false)
        val player = PlayerState(
            id = 0,
            name = "Player",
            isAI = false,
            hand = List(10) { CardState("card_$it", "unknown", Suit.HEARTS, false) },
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        val result = GameRules.validateMove(state, 0, twoCard, 1)
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `test ten goes in slot 9`() {
        val tenCard = CardState("ten_of_hearts", "ten", Suit.HEARTS, false)
        val player = PlayerState(
            id = 0,
            name = "Player",
            isAI = false,
            hand = List(10) { CardState("card_$it", "unknown", Suit.HEARTS, false) },
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        val result = GameRules.validateMove(state, 0, tenCard, 9)
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `test card in wrong slot is invalid`() {
        val aceCard = CardState("ace_of_hearts", "ace", Suit.HEARTS, false)
        val player = PlayerState(
            id = 0,
            name = "Player",
            isAI = false,
            hand = List(10) { CardState("card_$it", "unknown", Suit.HEARTS, false) },
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        // Ace should go in slot 0, not slot 5
        val result = GameRules.validateMove(state, 0, aceCard, 5)
        
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("ace"))
    }
    
    @Test
    fun `test jack is wild card - can go anywhere`() {
        val jackCard = CardState("jack_of_hearts", "jack", Suit.HEARTS, false)
        val player = PlayerState(
            id = 0,
            name = "Player",
            isAI = false,
            hand = List(10) { CardState("card_$it", "unknown", Suit.HEARTS, false) },
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        // Jack can go in any slot
        for (slot in 0..9) {
            val result = GameRules.validateMove(state, 0, jackCard, slot)
            assertTrue("Jack should be valid in slot $slot", result.isValid)
        }
    }
    
    @Test
    fun `test queen is wild card - can go anywhere`() {
        val queenCard = CardState("queen_of_hearts", "queen", Suit.HEARTS, false)
        val player = PlayerState(
            id = 0,
            name = "Player",
            isAI = false,
            hand = List(10) { CardState("card_$it", "unknown", Suit.HEARTS, false) },
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        // Queen can go in any slot
        for (slot in 0..9) {
            val result = GameRules.validateMove(state, 0, queenCard, slot)
            assertTrue("Queen should be valid in slot $slot", result.isValid)
        }
    }
    
    @Test
    fun `test king is wild card - can go anywhere`() {
        val kingCard = CardState("king_of_hearts", "king", Suit.HEARTS, false)
        val player = PlayerState(
            id = 0,
            name = "Player",
            isAI = false,
            hand = List(10) { CardState("card_$it", "unknown", Suit.HEARTS, false) },
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        // King can go in any slot
        for (slot in 0..9) {
            val result = GameRules.validateMove(state, 0, kingCard, slot)
            assertTrue("King should be valid in slot $slot", result.isValid)
        }
    }
    
    @Test
    fun `test cannot place card in already filled slot`() {
        val aceCard = CardState("ace_of_hearts", "ace", Suit.HEARTS, false)
        val filledCard = CardState("existing_card", "ace", Suit.SPADES, true)
        
        val hand = MutableList(10) { CardState("card_$it", "unknown", Suit.HEARTS, false) }
        hand[0] = filledCard // Slot 0 is already filled
        
        val player = PlayerState(
            id = 0,
            name = "Player",
            isAI = false,
            hand = hand,
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        val result = GameRules.validateMove(state, 0, aceCard, 0)
        
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("filled") || result.reason.contains("occupied"))
    }
    
    @Test
    fun `test check win condition - all slots filled`() {
        val hand = List(10) { index ->
            CardState("card_$index", "rank_$index", Suit.HEARTS, true)
        }
        
        val player = PlayerState(
            id = 0,
            name = "Player",
            isAI = false,
            hand = hand,
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        val hasWon = GameRules.checkWinCondition(state, 0)
        
        assertTrue(hasWon)
    }
    
    @Test
    fun `test check win condition - not all slots filled`() {
        val hand = MutableList(10) { index ->
            CardState("card_$index", "rank_$index", Suit.HEARTS, index < 8) // Only 8 filled
        }
        
        val player = PlayerState(
            id = 0,
            name = "Player",
            isAI = false,
            hand = hand,
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        val hasWon = GameRules.checkWinCondition(state, 0)
        
        assertFalse(hasWon)
    }
    
    @Test
    fun `test get valid slots for card`() {
        val aceCard = CardState("ace_of_hearts", "ace", Suit.HEARTS, false)
        val player = PlayerState(
            id = 0,
            name = "Player",
            isAI = false,
            hand = List(10) { CardState("card_$it", "unknown", Suit.HEARTS, false) },
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        val validSlots = GameRules.getValidSlots(state, 0, aceCard)
        
        assertEquals(listOf(0), validSlots)
    }
    
    @Test
    fun `test get valid slots for wild card`() {
        val jackCard = CardState("jack_of_hearts", "jack", Suit.HEARTS, false)
        val player = PlayerState(
            id = 0,
            name = "Player",
            isAI = false,
            hand = List(10) { CardState("card_$it", "unknown", Suit.HEARTS, false) },
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        val validSlots = GameRules.getValidSlots(state, 0, jackCard)
        
        assertEquals((0..9).toList(), validSlots)
    }
    
    @Test
    fun `test get AI hint for best move`() {
        val aceCard = CardState("ace_of_hearts", "ace", Suit.HEARTS, false)
        val player = PlayerState(
            id = 0,
            name = "AI",
            isAI = true,
            hand = List(10) { CardState("card_$it", "unknown", Suit.HEARTS, false) },
            score = 0,
            isActive = true
        )
        val state = createTestState(players = listOf(player))
        
        val hint = GameRules.getAIHint(state, 0, aceCard)
        
        assertNotNull(hint)
        assertEquals(0, hint)
    }
    
    @Test
    fun `test calculate cards needed for round`() {
        // Round 1: 10 cards per player
        assertEquals(10, GameRules.getCardsForRound(1))
        
        // Round 2: 9 cards per player
        assertEquals(9, GameRules.getCardsForRound(2))
        
        // Round 10: 1 card per player
        assertEquals(1, GameRules.getCardsForRound(10))
    }
}