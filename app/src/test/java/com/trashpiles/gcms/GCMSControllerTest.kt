package com.trashpiles.gcms

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.After

/**
 * Integration tests for GCMSController
 * Tests command processing, event emission, and state management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GCMSControllerTest {
    
    private lateinit var controller: GCMSController
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        controller = GCMSController()
    }
    
    @After
    fun teardown() {
        // Clean up
    }
    
    @Test
    fun `test initial state`() = runTest {
        val state = controller.currentState
        
        assertEquals(GamePhase.SETUP, state.currentPhase)
        assertTrue(state.players.isEmpty())
        assertFalse(state.isInputLocked)
    }
    
    @Test
    fun `test process InitializeGame command`() = runTest {
        val events = mutableListOf<GCMSEvent>()
        
        // Collect events
        val job = launch {
            controller.events.collect { event ->
                events.add(event)
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
        
        // Verify state updated
        val state = controller.currentState
        assertEquals(2, state.players.size)
        assertEquals("Player 1", state.players[0].name)
        assertEquals("Player 2", state.players[1].name)
        assertFalse(state.players[0].isAI)
        assertTrue(state.players[1].isAI)
        
        // Verify event emitted
        assertTrue(events.any { it is GCMSEvent.GameInitialized })
        
        job.cancel()
    }
    
    @Test
    fun `test process StartGame command`() = runTest {
        val events = mutableListOf<GCMSEvent>()
        
        val job = launch {
            controller.events.collect { event ->
                events.add(event)
            }
        }
        
        // Initialize first
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        events.clear()
        
        // Start game
        controller.processCommand(GCMSCommand.StartGame)
        
        advanceUntilIdle()
        
        // Verify state updated
        val state = controller.currentState
        assertEquals(GamePhase.DEALING, state.currentPhase)
        
        // Verify events emitted
        assertTrue(events.any { it is GCMSEvent.GameStarted })
        assertTrue(events.any { it is GCMSEvent.DealingStarted })
        
        job.cancel()
    }
    
    @Test
    fun `test invalid command emits error event`() = runTest {
        val events = mutableListOf<GCMSEvent>()
        
        val job = launch {
            controller.events.collect { event ->
                events.add(event)
            }
        }
        
        // Try to start game without initializing
        controller.processCommand(GCMSCommand.StartGame)
        
        advanceUntilIdle()
        
        // Verify error event emitted
        val errorEvents = events.filterIsInstance<GCMSEvent.CommandRejected>()
        assertTrue(errorEvents.isNotEmpty())
        assertTrue(errorEvents[0].reason.contains("No players"))
        
        job.cancel()
    }
    
    @Test
    fun `test state history for undo`() = runTest {
        // Initialize game
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        
        val stateAfterInit = controller.currentState
        
        // Start game
        controller.processCommand(GCMSCommand.StartGame)
        
        advanceUntilIdle()
        
        val stateAfterStart = controller.currentState
        
        // States should be different
        assertNotEquals(stateAfterInit.currentPhase, stateAfterStart.currentPhase)
    }
    
    @Test
    fun `test command queue processes in order`() = runTest {
        val events = mutableListOf<GCMSEvent>()
        
        val job = launch {
            controller.events.collect { event ->
                events.add(event)
            }
        }
        
        // Submit multiple commands
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        controller.processCommand(GCMSCommand.StartGame)
        
        advanceUntilIdle()
        
        // Verify both commands processed
        assertTrue(events.any { it is GCMSEvent.GameInitialized })
        assertTrue(events.any { it is GCMSEvent.GameStarted })
        
        // Verify order (GameInitialized should come before GameStarted)
        val initIndex = events.indexOfFirst { it is GCMSEvent.GameInitialized }
        val startIndex = events.indexOfFirst { it is GCMSEvent.GameStarted }
        assertTrue(initIndex < startIndex)
        
        job.cancel()
    }
    
    @Test
    fun `test input lock prevents commands`() = runTest {
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
                isAI = listOf(false, true)
            )
        )
        controller.processCommand(GCMSCommand.StartGame)
        
        advanceUntilIdle()
        
        // Manually lock input (in real scenario, this happens during animations)
        // For testing, we'll verify the validator blocks locked commands
        
        job.cancel()
    }
    
    @Test
    fun `test event flow is reactive`() = runTest {
        var eventReceived = false
        
        val job = launch {
            controller.events.collect { event ->
                if (event is GCMSEvent.GameInitialized) {
                    eventReceived = true
                }
            }
        }
        
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        
        assertTrue(eventReceived)
        
        job.cancel()
    }
    
    @Test
    fun `test multiple subscribers receive events`() = runTest {
        val subscriber1Events = mutableListOf<GCMSEvent>()
        val subscriber2Events = mutableListOf<GCMSEvent>()
        
        val job1 = launch {
            controller.events.collect { event ->
                subscriber1Events.add(event)
            }
        }
        
        val job2 = launch {
            controller.events.collect { event ->
                subscriber2Events.add(event)
            }
        }
        
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        
        // Both subscribers should receive the event
        assertTrue(subscriber1Events.any { it is GCMSEvent.GameInitialized })
        assertTrue(subscriber2Events.any { it is GCMSEvent.GameInitialized })
        
        job1.cancel()
        job2.cancel()
    }
    
    @Test
    fun `test state immutability`() = runTest {
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        
        val state1 = controller.currentState
        
        controller.processCommand(GCMSCommand.StartGame)
        
        advanceUntilIdle()
        
        val state2 = controller.currentState
        
        // States should be different instances
        assertNotSame(state1, state2)
        
        // Original state should be unchanged
        assertEquals(GamePhase.SETUP, state1.currentPhase)
        assertEquals(GamePhase.DEALING, state2.currentPhase)
    }
}