package com.trashpiles.gcms.handlers

import com.trashpiles.gcms.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for CardCommandHandler
 */
class CardCommandHandlerTest {
    
    private val handler = CardCommandHandler()
    
    private fun createTestState(): GCMSState {
        return GCMSState(
            currentPhase = GamePhase.PLAYING,
            players = listOf(
                PlayerState(
                    id = 0,
                    name = "Player 1",
                    hand = listOf(
                        CardState(id = "card1", rank = "A", suit = "hearts", value = 1, faceUp = false),
                        CardState(id = "card2", rank = "2", suit = "spades", value = 2, faceUp = false),
                        CardState(id = "card3", rank = "3", suit = "diamonds", value = 3, faceUp = false)
                    ),
                    score = 0,
                    isAI = false,
                    hasFinished = false
                )
            ),
            currentPlayerIndex = 0,
            currentRound = 1,
            deck = listOf(
                CardState(id = "deck1", rank = "A", suit = "hearts", value = 1, faceUp = false)
            ),
            discardPile = listOf(
                CardState(id = "discard1", rank = "2", suit = "spades", value = 2, faceUp = true)
            )
        )
    }
    
    @Test
    fun `handle DrawCardCommand from deck removes card from deck`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.DrawCardCommand(
            playerId = 0,
            fromPile = "deck",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(0, result.state.deck.size) // Card removed
        assertEquals(1, result.events.size)
        
        val event = result.events[0] as? GCMSEvent.CardDrawnEvent
        assertNotNull(event)
        assertEquals("deck1", event.cardId)
        assertEquals("deck", event.fromPile)
        assertEquals(0, event.byPlayerId)
    }
    
    @Test
    fun `handle DrawCardCommand from discard removes card from discard`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.DrawCardCommand(
            playerId = 0,
            fromPile = "discard",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(0, result.state.discardPile.size) // Card removed
        assertEquals(1, result.events.size)
        
        val event = result.events[0] as? GCMSEvent.CardDrawnEvent
        assertNotNull(event)
        assertEquals("discard1", event.cardId)
        assertEquals("discard", event.fromPile)
    }
    
    @Test
    fun `handle DrawCardCommand with empty deck returns error`() = runBlocking {
        val state = createTestState().copy(deck = emptyList())
        val command = GCMSCommand.DrawCardCommand(
            playerId = 0,
            fromPile = "deck",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(1, result.events.size)
        val event = result.events[0] as? GCMSEvent.InvalidMoveEvent
        assertNotNull(event)
        assertEquals("Deck is empty", event.reason)
    }
    
    @Test
    fun `handle DrawCardCommand with invalid pile returns error`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.DrawCardCommand(
            playerId = 0,
            fromPile = "invalid",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(1, result.events.size)
        val event = result.events[0] as? GCMSEvent.InvalidMoveEvent
        assertNotNull(event)
        assertTrue(event.reason?.contains("Invalid pile") == true)
    }
    
    @Test
    fun `handle PlaceCardCommand places card in correct slot`() = runBlocking {
        val state = createTestState()
        val cardToPlace = CardState(id = "deck1", rank = "A", suit = "hearts", value = 1, faceUp = false)
        val updatedDeck = state.deck + cardToPlace
        val stateWithCard = state.copy(deck = updatedDeck)
        
        val command = GCMSCommand.PlaceCardCommand(
            playerId = 0,
            cardId = "deck1",
            slotIndex = 0, // Ace goes in slot 0
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, stateWithCard)
        
        val player = result.state.players[0]
        assertEquals(true, player.hand[0].faceUp)
        assertEquals("A", player.hand[0].rank)
        assertEquals(1, result.events.size)
        
        val event = result.events[0] as? GCMSEvent.CardPlacedEvent
        assertNotNull(event)
        assertEquals("deck1", event.cardId)
        assertEquals(0, event.slotIndex)
    }
    
    @Test
    fun `handle PlaceCardCommand rejects invalid slot index`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.PlaceCardCommand(
            playerId = 0,
            cardId = "deck1",
            slotIndex = 99, // Invalid
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(1, result.events.size)
        val event = result.events[0] as? GCMSEvent.InvalidMoveEvent
        assertNotNull(event)
        assertTrue(event.reason?.contains("Invalid slot index") == true)
    }
    
    @Test
    fun `handle PlaceCardCommand rejects card not in deck or discard`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.PlaceCardCommand(
            playerId = 0,
            cardId = "nonexistent",
            slotIndex = 0,
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(1, result.events.size)
        val event = result.events[0] as? GCMSEvent.InvalidMoveEvent
        assertNotNull(event)
        assertTrue(event.reason?.contains("Card not found") == true)
    }
    
    @Test
    fun `handle PlaceCardCommand rejects mismatched card`() = runBlocking {
        val state = createTestState()
        val cardToPlace = CardState(id = "deck1", rank = "5", suit = "hearts", value = 5, faceUp = false)
        val updatedDeck = state.deck + cardToPlace
        val stateWithCard = state.copy(deck = updatedDeck)
        
        val command = GCMSCommand.PlaceCardCommand(
            playerId = 0,
            cardId = "deck1",
            slotIndex = 0, // Slot 0 expects Ace (value 1)
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, stateWithCard)
        
        assertEquals(1, result.events.size)
        val event = result.events[0] as? GCMSEvent.InvalidMoveEvent
        assertNotNull(event)
        assertTrue(event.reason?.contains("cannot be placed") == true)
    }
    
    @Test
    fun `handle DiscardCardCommand adds card to discard pile`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.DiscardCardCommand(
            playerId = 0,
            cardId = "deck1",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(1, result.state.discardPile.size)
        assertEquals("deck1", result.state.discardPile[0].id)
        assertEquals(1, result.events.size)
        
        val event = result.events[0] as? GCMSEvent.CardDiscardedEvent
        assertNotNull(event)
        assertEquals("deck1", event.cardId)
    }
    
    @Test
    fun `handle FlipCardCommand flips card face up`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.FlipCardCommand(
            playerId = 0,
            slotIndex = 0,
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        val player = result.state.players[0]
        assertEquals(true, player.hand[0].faceUp)
        assertEquals(1, result.events.size)
        
        val event = result.events[0] as? GCMSEvent.CardFlippedEvent
        assertNotNull(event)
        assertEquals("card1", event.cardId)
        assertEquals(true, event.isFaceUp)
    }
    
    @Test
    fun `handle FlipCardCommand flips card face down`() = runBlocking {
        val faceUpCard = CardState(id = "card1", rank = "A", suit = "hearts", value = 1, faceUp = true)
        val state = createTestState().copy(
            players = listOf(
                createTestState().players[0].copy(
                    hand = listOf(faceUpCard, CardState(id = "card2", rank = "2", suit = "spades", value = 2, faceUp = false))
                )
            )
        )
        val command = GCMSCommand.FlipCardCommand(
            playerId = 0,
            slotIndex = 0,
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        val player = result.state.players[0]
        assertEquals(false, player.hand[0].faceUp)
        
        val event = result.events[0] as? GCMSEvent.CardFlippedEvent
        assertNotNull(event)
        assertEquals(false, event.isFaceUp)
    }
    
    @Test
    fun `handle throws exception for invalid command type`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.EndTurnCommand(
            playerId = 0,
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        try {
            handler.handle(command, state)
            assertTrue(false, "Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("CardCommandHandler cannot handle") == true)
        }
    }
}