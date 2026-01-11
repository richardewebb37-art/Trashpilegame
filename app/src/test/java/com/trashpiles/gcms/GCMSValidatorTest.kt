package com.trashpiles.gcms

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for GCMSValidator
 * Tests command validation logic
 */
class GCMSValidatorTest {
    
    private fun createTestState(
        currentPhase: GamePhase = GamePhase.PLAYING,
        currentPlayerIndex: Int = 0,
        isInputLocked: Boolean = false,
        players: List<PlayerState> = emptyList(),
        deck: List<CardState> = emptyList(),
        discardPile: List<CardState> = emptyList()
    ): GCMSState {
        return GCMSState(
            currentPhase = currentPhase,
            currentPlayerIndex = currentPlayerIndex,
            currentRound = 1,
            players = players,
            deck = deck,
            discardPile = discardPile,
            isInputLocked = isInputLocked
        )
    }
    
    @Test
    fun `test validate InitializeGame - valid`() {
        val state = createTestState(currentPhase = GamePhase.SETUP)
        val command = GCMSCommand.InitializeGame(
            playerCount = 2,
            playerNames = listOf("Player 1", "Player 2"),
            isAI = listOf(false, true)
        )
        
        val result = GCMSValidator.validate(command, state)
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `test validate InitializeGame - too few players`() {
        val state = createTestState(currentPhase = GamePhase.SETUP)
        val command = GCMSCommand.InitializeGame(
            playerCount = 1,
            playerNames = listOf("Player 1"),
            isAI = listOf(false)
        )
        
        val result = GCMSValidator.validate(command, state)
        
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("2 players"))
    }
    
    @Test
    fun `test validate InitializeGame - too many players`() {
        val state = createTestState(currentPhase = GamePhase.SETUP)
        val command = GCMSCommand.InitializeGame(
            playerCount = 7,
            playerNames = List(7) { "Player $it" },
            isAI = List(7) { false }
        )
        
        val result = GCMSValidator.validate(command, state)
        
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("6 players"))
    }
    
    @Test
    fun `test validate InitializeGame - mismatched player names`() {
        val state = createTestState(currentPhase = GamePhase.SETUP)
        val command = GCMSCommand.InitializeGame(
            playerCount = 2,
            playerNames = listOf("Player 1"), // Only 1 name for 2 players
            isAI = listOf(false, true)
        )
        
        val result = GCMSValidator.validate(command, state)
        
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("mismatch"))
    }
    
    @Test
    fun `test validate StartGame - valid`() {
        val players = listOf(
            PlayerState(0, "Player 1", false, emptyList(), 0, true),
            PlayerState(1, "Player 2", true, emptyList(), 0, true)
        )
        val state = createTestState(
            currentPhase = GamePhase.SETUP,
            players = players
        )
        val command = GCMSCommand.StartGame
        
        val result = GCMSValidator.validate(command, state)
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `test validate StartGame - no players`() {
        val state = createTestState(currentPhase = GamePhase.SETUP)
        val command = GCMSCommand.StartGame
        
        val result = GCMSValidator.validate(command, state)
        
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("No players"))
    }
    
    @Test
    fun `test validate DrawCard - valid`() {
        val players = listOf(
            PlayerState(0, "Player 1", false, emptyList(), 0, true)
        )
        val deck = DeckBuilder.createDeck()
        val state = createTestState(
            currentPhase = GamePhase.PLAYING,
            currentPlayerIndex = 0,
            players = players,
            deck = deck
        )
        val command = GCMSCommand.DrawCard(playerId = 0, fromPile = "deck")
        
        val result = GCMSValidator.validate(command, state)
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `test validate DrawCard - not player's turn`() {
        val players = listOf(
            PlayerState(0, "Player 1", false, emptyList(), 0, true),
            PlayerState(1, "Player 2", true, emptyList(), 0, true)
        )
        val deck = DeckBuilder.createDeck()
        val state = createTestState(
            currentPhase = GamePhase.PLAYING,
            currentPlayerIndex = 0, // Player 0's turn
            players = players,
            deck = deck
        )
        val command = GCMSCommand.DrawCard(playerId = 1, fromPile = "deck") // Player 1 tries to draw
        
        val result = GCMSValidator.validate(command, state)
        
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("turn"))
    }
    
    @Test
    fun `test validate DrawCard - empty deck`() {
        val players = listOf(
            PlayerState(0, "Player 1", false, emptyList(), 0, true)
        )
        val state = createTestState(
            currentPhase = GamePhase.PLAYING,
            currentPlayerIndex = 0,
            players = players,
            deck = emptyList() // Empty deck
        )
        val command = GCMSCommand.DrawCard(playerId = 0, fromPile = "deck")
        
        val result = GCMSValidator.validate(command, state)
        
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("empty"))
    }
    
    @Test
    fun `test validate DrawCard - from discard pile`() {
        val players = listOf(
            PlayerState(0, "Player 1", false, emptyList(), 0, true)
        )
        val discardPile = listOf(
            CardState("ace_of_hearts", "ace", Suit.HEARTS, true)
        )
        val state = createTestState(
            currentPhase = GamePhase.PLAYING,
            currentPlayerIndex = 0,
            players = players,
            discardPile = discardPile
        )
        val command = GCMSCommand.DrawCard(playerId = 0, fromPile = "discard")
        
        val result = GCMSValidator.validate(command, state)
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `test validate PlaceCard - valid`() {
        val hand = MutableList(10) { 
            CardState("card_$it", "unknown", Suit.HEARTS, false) 
        }
        val players = listOf(
            PlayerState(0, "Player 1", false, hand, 0, true)
        )
        val aceCard = CardState("ace_of_hearts", "ace", Suit.HEARTS, false)
        val deck = listOf(aceCard)
        val state = createTestState(
            currentPhase = GamePhase.PLAYING,
            currentPlayerIndex = 0,
            players = players,
            deck = deck
        )
        val command = GCMSCommand.PlaceCard(
            playerId = 0,
            cardId = "ace_of_hearts",
            slotIndex = 0
        )
        
        val result = GCMSValidator.validate(command, state)
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `test validate PlaceCard - slot already filled`() {
        val filledCard = CardState("existing_ace", "ace", Suit.SPADES, true)
        val hand = MutableList(10) { 
            CardState("card_$it", "unknown", Suit.HEARTS, false) 
        }
        hand[0] = filledCard // Slot 0 is filled
        
        val players = listOf(
            PlayerState(0, "Player 1", false, hand, 0, true)
        )
        val aceCard = CardState("ace_of_hearts", "ace", Suit.HEARTS, false)
        val deck = listOf(aceCard)
        val state = createTestState(
            currentPhase = GamePhase.PLAYING,
            currentPlayerIndex = 0,
            players = players,
            deck = deck
        )
        val command = GCMSCommand.PlaceCard(
            playerId = 0,
            cardId = "ace_of_hearts",
            slotIndex = 0
        )
        
        val result = GCMSValidator.validate(command, state)
        
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("filled"))
    }
    
    @Test
    fun `test validate EndTurn - valid`() {
        val players = listOf(
            PlayerState(0, "Player 1", false, emptyList(), 0, true)
        )
        val state = createTestState(
            currentPhase = GamePhase.PLAYING,
            currentPlayerIndex = 0,
            players = players
        )
        val command = GCMSCommand.EndTurn(playerId = 0)
        
        val result = GCMSValidator.validate(command, state)
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `test validate EndTurn - not player's turn`() {
        val players = listOf(
            PlayerState(0, "Player 1", false, emptyList(), 0, true),
            PlayerState(1, "Player 2", true, emptyList(), 0, true)
        )
        val state = createTestState(
            currentPhase = GamePhase.PLAYING,
            currentPlayerIndex = 0,
            players = players
        )
        val command = GCMSCommand.EndTurn(playerId = 1)
        
        val result = GCMSValidator.validate(command, state)
        
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("turn"))
    }
    
    @Test
    fun `test input lock blocks most commands`() {
        val players = listOf(
            PlayerState(0, "Player 1", false, emptyList(), 0, true)
        )
        val state = createTestState(
            currentPhase = GamePhase.PLAYING,
            currentPlayerIndex = 0,
            isInputLocked = true, // Input is locked
            players = players
        )
        
        // DrawCard should be blocked
        val drawCommand = GCMSCommand.DrawCard(playerId = 0, fromPile = "deck")
        val drawResult = GCMSValidator.validate(drawCommand, state)
        assertFalse(drawResult.isValid)
        assertTrue(drawResult.reason.contains("locked"))
        
        // EndTurn should be blocked
        val endTurnCommand = GCMSCommand.EndTurn(playerId = 0)
        val endTurnResult = GCMSValidator.validate(endTurnCommand, state)
        assertFalse(endTurnResult.isValid)
        assertTrue(endTurnResult.reason.contains("locked"))
    }
    
    @Test
    fun `test SaveGame allowed when input locked`() {
        val state = createTestState(isInputLocked = true)
        val command = GCMSCommand.SaveGame(saveId = "save1")
        
        val result = GCMSValidator.validate(command, state)
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `test wrong phase blocks commands`() {
        val state = createTestState(currentPhase = GamePhase.DEALING)
        val command = GCMSCommand.DrawCard(playerId = 0, fromPile = "deck")
        
        val result = GCMSValidator.validate(command, state)
        
        assertFalse(result.isValid)
        assertTrue(result.reason.contains("phase"))
    }
}