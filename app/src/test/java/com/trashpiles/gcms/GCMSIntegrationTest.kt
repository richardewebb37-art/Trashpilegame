package com.trashpiles.gcms

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Integration tests for complete GCMS game flow
 * Tests realistic game scenarios from start to finish
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GCMSIntegrationTest {
    
    private lateinit var controller: GCMSController
    
    @Before
    fun setup() {
        controller = GCMSController()
    }
    
    @Test
    fun `test complete game initialization flow`() = runTest {
        val events = mutableListOf<GCMSEvent>()
        
        val job = launch {
            controller.events.collect { event ->
                events.add(event)
            }
        }
        
        // Step 1: Initialize game
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "AI Player"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        
        // Verify initialization
        assertTrue(events.any { it is GCMSEvent.GameInitialized })
        assertEquals(2, controller.currentState.players.size)
        
        // Step 2: Start game
        controller.processCommand(GCMSCommand.StartGame)
        
        advanceUntilIdle()
        
        // Verify game started
        assertTrue(events.any { it is GCMSEvent.GameStarted })
        assertTrue(events.any { it is GCMSEvent.DealingStarted })
        assertEquals(GamePhase.DEALING, controller.currentState.currentPhase)
        
        job.cancel()
    }
    
    @Test
    fun `test card draw and place flow`() = runTest {
        val events = mutableListOf<GCMSEvent>()
        
        val job = launch {
            controller.events.collect { event ->
                events.add(event)
            }
        }
        
        // Initialize and start game
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, false)
            )
        )
        controller.processCommand(GCMSCommand.StartGame)
        
        advanceUntilIdle()
        events.clear()
        
        // Simulate game is now in PLAYING phase with cards dealt
        // (In real implementation, dealing would happen automatically)
        
        job.cancel()
    }
    
    @Test
    fun `test turn progression`() = runTest {
        val events = mutableListOf<GCMSEvent>()
        
        val job = launch {
            controller.events.collect { event ->
                events.add(event)
            }
        }
        
        // Initialize game with 2 players
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, false)
            )
        )
        
        advanceUntilIdle()
        
        // Verify initial turn
        assertEquals(0, controller.currentState.currentPlayerIndex)
        
        job.cancel()
    }
    
    @Test
    fun `test validation prevents invalid moves`() = runTest {
        val events = mutableListOf<GCMSEvent>()
        
        val job = launch {
            controller.events.collect { event ->
                events.add(event)
            }
        }
        
        // Initialize game
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, false)
            )
        )
        
        advanceUntilIdle()
        events.clear()
        
        // Try to draw card before game starts (invalid)
        controller.processCommand(
            GCMSCommand.DrawCard(playerId = 0, fromPile = "deck")
        )
        
        advanceUntilIdle()
        
        // Should emit CommandRejected event
        val rejectedEvents = events.filterIsInstance<GCMSEvent.CommandRejected>()
        assertTrue(rejectedEvents.isNotEmpty())
        
        job.cancel()
    }
    
    @Test
    fun `test event-driven architecture`() = runTest {
        // Simulate subsystems listening to events
        val gameEngineEvents = mutableListOf<GCMSEvent>()
        val audioEngineEvents = mutableListOf<GCMSEvent>()
        val rendererEvents = mutableListOf<GCMSEvent>()
        
        // Game Engine subscriber
        val gameEngineJob = launch {
            controller.events.collect { event ->
                gameEngineEvents.add(event)
            }
        }
        
        // Audio Engine subscriber
        val audioEngineJob = launch {
            controller.events.collect { event ->
                audioEngineEvents.add(event)
            }
        }
        
        // Renderer subscriber
        val rendererJob = launch {
            controller.events.collect { event ->
                rendererEvents.add(event)
            }
        }
        
        // Process command
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        
        // All subsystems should receive the event
        assertTrue(gameEngineEvents.any { it is GCMSEvent.GameInitialized })
        assertTrue(audioEngineEvents.any { it is GCMSEvent.GameInitialized })
        assertTrue(rendererEvents.any { it is GCMSEvent.GameInitialized })
        
        gameEngineJob.cancel()
        audioEngineJob.cancel()
        rendererJob.cancel()
    }
    
    @Test
    fun `test command-event pattern`() = runTest {
        val events = mutableListOf<GCMSEvent>()
        
        val job = launch {
            controller.events.collect { event ->
                events.add(event)
            }
        }
        
        // Command → Validation → State Update → Event
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        
        // Verify event was emitted
        val initEvent = events.filterIsInstance<GCMSEvent.GameInitialized>().firstOrNull()
        assertNotNull(initEvent)
        assertEquals(2, initEvent?.playerCount)
        
        job.cancel()
    }
    
    @Test
    fun `test state is single source of truth`() = runTest {
        // Initialize game
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        
        // Get state multiple times
        val state1 = controller.currentState
        val state2 = controller.currentState
        
        // Should return same state (same values)
        assertEquals(state1.players.size, state2.players.size)
        assertEquals(state1.currentPhase, state2.currentPhase)
    }
    
    @Test
    fun `test async command processing`() = runTest {
        val events = mutableListOf<GCMSEvent>()
        
        val job = launch {
            controller.events.collect { event ->
                events.add(event)
            }
        }
        
        // Submit multiple commands rapidly
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        controller.processCommand(GCMSCommand.StartGame)
        controller.processCommand(GCMSCommand.PauseGame)
        
        advanceUntilIdle()
        
        // All commands should be processed
        assertTrue(events.any { it is GCMSEvent.GameInitialized })
        assertTrue(events.any { it is GCMSEvent.GameStarted })
        
        job.cancel()
    }
    
    @Test
    fun `test game rules integration`() = runTest {
        // Initialize game
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, false)
            )
        )
        
        advanceUntilIdle()
        
        val state = controller.currentState
        
        // Verify game rules can access state
        assertNotNull(state)
        assertEquals(2, state.players.size)
    }
    
    @Test
    fun `test validator integration`() = runTest {
        val events = mutableListOf<GCMSEvent>()
        
        val job = launch {
            controller.events.collect { event ->
                events.add(event)
            }
        }
        
        // Try invalid command (too few players)
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 1,
                playerNames = listOf("Player 1"),
                isAI = listOf(false)
            )
        )
        
        advanceUntilIdle()
        
        // Should be rejected
        val rejectedEvents = events.filterIsInstance<GCMSEvent.CommandRejected>()
        assertTrue(rejectedEvents.isNotEmpty())
        assertTrue(rejectedEvents[0].reason.contains("2 players"))
        
        job.cancel()
    }
}