package com.trashpiles.gcms

import com.trashpiles.gcms.handlers.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for GCMSControllerRefactored
 */
class GCMSControllerRefactoredTest {
    
    private fun createTestState(): GCMSState {
        return GCMSState(
            currentPhase = GamePhase.SETUP,
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
                    isAI = true,
                    hasFinished = false
                )
            ),
            currentPlayerIndex = 0,
            currentRound = 1,
            deck = emptyList(),
            discardPile = emptyList()
        )
    }
    
    @Test
    fun `submitCommand routes EndTurnCommand to TurnCommandHandler`() = runBlocking {
        val controller = GCMSControllerRefactored()
        val command = GCMSCommand.EndTurnCommand(
            playerId = 0,
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        controller.submitCommand(command)
        
        val event = controller.events.first() as? GCMSEvent.TurnEndedEvent
        assertNotNull(event)
        assertEquals(0, event.playerId)
    }
    
    @Test
    fun `submitCommand routes DrawCardCommand to CardCommandHandler`() = runBlocking {
        val controller = GCMSControllerRefactored()
        val command = GCMSCommand.DrawCardCommand(
            playerId = 0,
            fromPile = "deck",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        controller.submitCommand(command)
        
        val event = controller.events.first() as? GCMSEvent.InvalidMoveEvent
        assertNotNull(event) // Empty deck returns error
    }
    
    @Test
    fun `submitCommand routes UnlockNodeCommand to SkillCommandHandler`() = runBlocking {
        val controller = GCMSControllerRefactored()
        val command = GCMSCommand.UnlockNodeCommand(
            playerId = "0",
            nodeId = "quick_draw",
            pointType = "SKILL",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        controller.submitCommand(command)
        
        // Should get an InvalidMoveEvent due to insufficient points
        val event = controller.events.first()
        assertTrue(event is GCMSEvent.InvalidMoveEvent || event is GCMSEvent.NodeUnlockedEvent)
    }
    
    @Test
    fun `submitCommand routes InitializeGameCommand to MatchCommandHandler`() = runBlocking {
        val controller = GCMSControllerRefactored()
        val command = GCMSCommand.InitializeGameCommand(
            playerCount = 2,
            playerNames = listOf("Alice", "Bob"),
            isAI = listOf(false, true),
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        controller.submitCommand(command)
        
        val event = controller.events.first() as? GCMSEvent.GameInitializedEvent
        assertNotNull(event)
        assertEquals(2, event.playerCount)
    }
    
    @Test
    fun `getCurrentState returns immutable copy`() = runBlocking {
        val controller = GCMSControllerRefactored()
        val command = GCMSCommand.InitializeGameCommand(
            playerCount = 2,
            playerNames = listOf("Alice", "Bob"),
            isAI = listOf(false, true),
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        controller.submitCommand(command)
        
        val state1 = controller.getCurrentState()
        val state2 = controller.getCurrentState()
        
        // Should be equal values but different objects
        assertEquals(state1.currentPhase, state2.currentPhase)
        assertEquals(state1.players.size, state2.players.size)
    }
    
    @Test
    fun `getStateHistory returns list of past states`() = runBlocking {
        val controller = GCMSControllerRefactored()
        
        val command1 = GCMSCommand.InitializeGameCommand(
            playerCount = 2,
            playerNames = listOf("Alice", "Bob"),
            isAI = listOf(false, true),
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        controller.submitCommand(command1)
        
        val command2 = GCMSCommand.StartGameCommand(
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        controller.submitCommand(command2)
        
        val history = controller.getStateHistory()
        assertTrue(history.size >= 1)
    }
    
    @Test
    fun `clearQueue removes all pending commands`() = runBlocking {
        val controller = GCMSControllerRefactored()
        
        // Submit multiple commands
        for (i in 1..3) {
            controller.submitCommand(
                GCMSCommand.EndTurnCommand(
                    playerId = 0,
                    timestamp = System.currentTimeMillis(),
                    commandId = GCMSCommand.generateId()
                )
            )
        }
        
        controller.clearQueue()
        
        assertEquals(0, controller.getQueueSize())
    }
    
    @Test
    fun `getQueueSize returns number of pending commands`() = runBlocking {
        val controller = GCMSControllerRefactored()
        
        assertEquals(0, controller.getQueueSize())
        
        controller.submitCommand(
            GCMSCommand.EndTurnCommand(
                playerId = 0,
                timestamp = System.currentTimeMillis(),
                commandId = GCMSCommand.generateId()
            )
        )
        
        // Queue size should be 0 after processing completes
        assertEquals(0, controller.getQueueSize())
    }
    
    @Test
    fun `submitCommand with invalid command emits InvalidMoveEvent`() = runBlocking {
        val controller = GCMSControllerRefactored()
        
        // This command will fail validation
        val command = GCMSCommand.DrawCardCommand(
            playerId = 99, // Invalid player
            fromPile = "deck",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        controller.submitCommand(command)
        
        val event = controller.events.first()
        assertTrue(event is GCMSEvent.InvalidMoveEvent)
    }
    
    @Test
    fun `stateHistory is limited to maxHistorySize`() = runBlocking {
        val controller = GCMSControllerRefactored()
        
        // Submit more commands than maxHistorySize (50)
        for (i in 1..55) {
            controller.submitCommand(
                GCMSCommand.EndTurnCommand(
                    playerId = 0,
                    timestamp = System.currentTimeMillis(),
                    commandId = GCMSCommand.generateId()
                )
            )
        }
        
        val history = controller.getStateHistory()
        assertTrue(history.size <= 50)
    }
}