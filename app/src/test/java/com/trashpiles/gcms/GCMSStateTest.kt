package com.trashpiles.gcms

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for GCMSState
 * Tests immutability and state management
 */
class GCMSStateTest {
    
    @Test
    fun `test initial state creation`() {
        val state = GCMSState()
        
        assertEquals(GamePhase.SETUP, state.currentPhase)
        assertEquals(0, state.currentPlayerIndex)
        assertEquals(0, state.currentRound)
        assertFalse(state.isInputLocked)
        assertTrue(state.players.isEmpty())
        assertTrue(state.deck.isEmpty())
        assertTrue(state.discardPile.isEmpty())
    }
    
    @Test
    fun `test state immutability - players list`() {
        val player1 = PlayerState(
            id = 0,
            name = "Player 1",
            isAI = false,
            hand = emptyList(),
            score = 0,
            isActive = true
        )
        
        val state = GCMSState(
            players = listOf(player1)
        )
        
        // Verify we can't modify the original list
        assertEquals(1, state.players.size)
        
        // Create new state with modified players
        val newState = state.copy(
            players = state.players + PlayerState(
                id = 1,
                name = "Player 2",
                isAI = true,
                hand = emptyList(),
                score = 0,
                isActive = true
            )
        )
        
        // Original state unchanged
        assertEquals(1, state.players.size)
        // New state has both players
        assertEquals(2, newState.players.size)
    }
    
    @Test
    fun `test state copy creates new instance`() {
        val state1 = GCMSState(currentPhase = GamePhase.SETUP)
        val state2 = state1.copy(currentPhase = GamePhase.PLAYING)
        
        // States are different
        assertNotEquals(state1.currentPhase, state2.currentPhase)
        assertEquals(GamePhase.SETUP, state1.currentPhase)
        assertEquals(GamePhase.PLAYING, state2.currentPhase)
    }
    
    @Test
    fun `test player state immutability`() {
        val card = CardState(
            id = "ace_of_spades",
            rank = "ace",
            suit = Suit.SPADES,
            isFaceUp = false
        )
        
        val player = PlayerState(
            id = 0,
            name = "Test Player",
            isAI = false,
            hand = listOf(card),
            score = 0,
            isActive = true
        )
        
        // Verify hand is immutable
        assertEquals(1, player.hand.size)
        
        // Create new player with modified hand
        val newPlayer = player.copy(
            hand = player.hand + CardState(
                id = "two_of_hearts",
                rank = "two",
                suit = Suit.HEARTS,
                isFaceUp = false
            )
        )
        
        // Original unchanged
        assertEquals(1, player.hand.size)
        // New player has both cards
        assertEquals(2, newPlayer.hand.size)
    }
    
    @Test
    fun `test card state properties`() {
        val card = CardState(
            id = "king_of_diamonds",
            rank = "king",
            suit = Suit.DIAMONDS,
            isFaceUp = true
        )
        
        assertEquals("king_of_diamonds", card.id)
        assertEquals("king", card.rank)
        assertEquals(Suit.DIAMONDS, card.suit)
        assertTrue(card.isFaceUp)
    }
    
    @Test
    fun `test game phase enum`() {
        val phases = GamePhase.values()
        
        assertEquals(5, phases.size)
        assertTrue(phases.contains(GamePhase.SETUP))
        assertTrue(phases.contains(GamePhase.DEALING))
        assertTrue(phases.contains(GamePhase.PLAYING))
        assertTrue(phases.contains(GamePhase.ROUND_END))
        assertTrue(phases.contains(GamePhase.GAME_OVER))
    }
    
    @Test
    fun `test suit enum`() {
        val suits = Suit.values()
        
        assertEquals(4, suits.size)
        assertTrue(suits.contains(Suit.HEARTS))
        assertTrue(suits.contains(Suit.DIAMONDS))
        assertTrue(suits.contains(Suit.CLUBS))
        assertTrue(suits.contains(Suit.SPADES))
    }
    
    @Test
    fun `test state with full game setup`() {
        val players = listOf(
            PlayerState(0, "Player 1", false, emptyList(), 0, true),
            PlayerState(1, "Player 2", true, emptyList(), 0, true)
        )
        
        val deck = DeckBuilder.createDeck()
        
        val state = GCMSState(
            currentPhase = GamePhase.PLAYING,
            currentPlayerIndex = 0,
            currentRound = 1,
            players = players,
            deck = deck,
            discardPile = emptyList(),
            isInputLocked = false
        )
        
        assertEquals(GamePhase.PLAYING, state.currentPhase)
        assertEquals(2, state.players.size)
        assertEquals(52, state.deck.size)
        assertTrue(state.discardPile.isEmpty())
        assertFalse(state.isInputLocked)
    }
}