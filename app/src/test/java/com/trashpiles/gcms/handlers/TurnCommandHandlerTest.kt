package com.trashpiles.gcms.handlers

import com.trashpiles.gcms.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for TurnCommandHandler
 */
class TurnCommandHandlerTest {
    
    private val handler = TurnCommandHandler()
    
    private fun createTestState(): GCMSState {
        return GCMSState(
            currentPhase = GamePhase.PLAYING,
            players = listOf(
                PlayerState(
                    id = 0,
                    name = "Player 1",
                    hand = emptyList(),
                    score = 0,
                    isAI = false,
                    hasFinished = false
                ),
                PlayerState(
                    id = 1,
                    name = "Player 2",
                    hand = emptyList(),
                    score = 0,
                    isAI = false,
                    hasFinished = false
                )
            ),
            currentPlayerIndex = 0,
            currentRound = 1
        )
    }
    
    @Test
    fun `handle EndTurnCommand moves to next player`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.EndTurnCommand(
            playerId = 0,
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(1, result.state.currentPlayerIndex)
        assertEquals(2, result.events.size)
        
        val turnEndedEvent = result.events[0] as? GCMSEvent.TurnEndedEvent
        assertNotNull(turnEndedEvent)
        assertEquals(0, turnEndedEvent.playerId)
        
        val turnStartedEvent = result.events[1] as? GCMSEvent.TurnStartedEvent
        assertNotNull(turnStartedEvent)
        assertEquals(1, turnStartedEvent.playerId)
        assertEquals("Player 2", turnStartedEvent.playerName)
    }
    
    @Test
    fun `handle EndTurnCommand wraps around to first player`() = runBlocking {
        val state = createTestState().copy(currentPlayerIndex = 1)
        val command = GCMSCommand.EndTurnCommand(
            playerId = 1,
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(0, result.state.currentPlayerIndex)
        
        val turnStartedEvent = result.events[1] as? GCMSEvent.TurnStartedEvent
        assertNotNull(turnStartedEvent)
        assertEquals(0, turnStartedEvent.playerId)
        assertEquals("Player 1", turnStartedEvent.playerName)
    }
    
    @Test
    fun `handle SkipTurnCommand moves to next player`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.SkipTurnCommand(
            playerId = 0,
            reason = "Player timed out",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(1, result.state.currentPlayerIndex)
        assertEquals(1, result.events.size)
        
        val turnStartedEvent = result.events[0] as? GCMSEvent.TurnStartedEvent
        assertNotNull(turnStartedEvent)
        assertEquals(1, turnStartedEvent.playerId)
    }
    
    @Test
    fun `TurnStartedEvent includes correct turn number`() = runBlocking {
        val state = createTestState().copy(currentRound = 2)
        val command = GCMSCommand.EndTurnCommand(
            playerId = 0,
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        val turnStartedEvent = result.events[1] as? GCMSEvent.TurnStartedEvent
        assertNotNull(turnStartedEvent)
        assertEquals(3, turnStartedEvent.turnNumber) // Round 2 * 2 players + player index 1
    }
    
    @Test
    fun `handle throws exception for invalid command type`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.DrawCardCommand(
            playerId = 0,
            fromPile = "deck",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        try {
            handler.handle(command, state)
            assertTrue(false, "Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("TurnCommandHandler cannot handle") == true)
        }
    }
}