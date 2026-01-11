package com.trashpiles.gcms

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Tests GCMS integration with mock engines
 * Simulates Game Engine, Audio Engine, and Renderer
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MockEngineTest {
    
    private lateinit var controller: GCMSController
    private lateinit var mockGameEngine: MockGameEngine
    private lateinit var mockAudioEngine: MockAudioEngine
    private lateinit var mockRenderer: MockRenderer
    
    @Before
    fun setup() {
        controller = GCMSController()
        mockGameEngine = MockGameEngine(controller)
        mockAudioEngine = MockAudioEngine(controller)
        mockRenderer = MockRenderer(controller)
    }
    
    @Test
    fun `test game engine receives events`() = runTest {
        // Start collecting events
        val gameEngineJob = launch {
            mockGameEngine.startListening()
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
        
        // Verify game engine received event
        assertTrue(mockGameEngine.receivedEvents.any { it is GCMSEvent.GameInitialized })
        
        gameEngineJob.cancel()
    }
    
    @Test
    fun `test audio engine receives events`() = runTest {
        // Start collecting events
        val audioEngineJob = launch {
            mockAudioEngine.startListening()
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
        
        // Verify audio engine received event
        assertTrue(mockAudioEngine.receivedEvents.any { it is GCMSEvent.GameInitialized })
        
        audioEngineJob.cancel()
    }
    
    @Test
    fun `test renderer receives events`() = runTest {
        // Start collecting events
        val rendererJob = launch {
            mockRenderer.startListening()
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
        
        // Verify renderer received event
        assertTrue(mockRenderer.receivedEvents.any { it is GCMSEvent.GameInitialized })
        
        rendererJob.cancel()
    }
    
    @Test
    fun `test all engines receive same events`() = runTest {
        // Start all engines
        val gameEngineJob = launch { mockGameEngine.startListening() }
        val audioEngineJob = launch { mockAudioEngine.startListening() }
        val rendererJob = launch { mockRenderer.startListening() }
        
        // Process command
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        
        // All engines should receive the event
        assertTrue(mockGameEngine.receivedEvents.any { it is GCMSEvent.GameInitialized })
        assertTrue(mockAudioEngine.receivedEvents.any { it is GCMSEvent.GameInitialized })
        assertTrue(mockRenderer.receivedEvents.any { it is GCMSEvent.GameInitialized })
        
        gameEngineJob.cancel()
        audioEngineJob.cancel()
        rendererJob.cancel()
    }
    
    @Test
    fun `test engines react to events independently`() = runTest {
        val gameEngineJob = launch { mockGameEngine.startListening() }
        val audioEngineJob = launch { mockAudioEngine.startListening() }
        
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        
        // Game engine should have animated
        assertTrue(mockGameEngine.animationPlayed)
        
        // Audio engine should have played sound
        assertTrue(mockAudioEngine.soundPlayed)
        
        gameEngineJob.cancel()
        audioEngineJob.cancel()
    }
    
    @Test
    fun `test engine sends command back to GCMS`() = runTest {
        val events = mutableListOf<GCMSEvent>()
        
        val eventsJob = launch {
            controller.events.collect { event ->
                events.add(event)
            }
        }
        
        val gameEngineJob = launch {
            mockGameEngine.startListening()
        }
        
        // Initialize game
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        
        // Simulate user tapping a card in game engine
        mockGameEngine.simulateCardTap(playerId = 0, slotIndex = 0)
        
        advanceUntilIdle()
        
        // GCMS should have received the command
        // (In this test, it will be rejected because game isn't in PLAYING phase)
        assertTrue(events.any { it is GCMSEvent.CommandRejected })
        
        eventsJob.cancel()
        gameEngineJob.cancel()
    }
    
    @Test
    fun `test complete flow - command to event to engine reaction`() = runTest {
        val gameEngineJob = launch { mockGameEngine.startListening() }
        val audioEngineJob = launch { mockAudioEngine.startListening() }
        val rendererJob = launch { mockRenderer.startListening() }
        
        // 1. User action → Command
        controller.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = 2,
                playerNames = listOf("Player 1", "Player 2"),
                isAI = listOf(false, true)
            )
        )
        
        advanceUntilIdle()
        
        // 2. GCMS validates → Updates state → Emits event
        assertTrue(mockGameEngine.receivedEvents.any { it is GCMSEvent.GameInitialized })
        
        // 3. Engines react to event
        assertTrue(mockGameEngine.animationPlayed)
        assertTrue(mockAudioEngine.soundPlayed)
        assertTrue(mockRenderer.rendered)
        
        gameEngineJob.cancel()
        audioEngineJob.cancel()
        rendererJob.cancel()
    }
}

/**
 * Mock Game Engine for testing
 */
class MockGameEngine(private val gcms: GCMSController) {
    val receivedEvents = mutableListOf<GCMSEvent>()
    var animationPlayed = false
    
    suspend fun startListening() {
        gcms.events.collect { event ->
            receivedEvents.add(event)
            handleEvent(event)
        }
    }
    
    private fun handleEvent(event: GCMSEvent) {
        when (event) {
            is GCMSEvent.GameInitialized -> {
                // Simulate animation
                animationPlayed = true
            }
            is GCMSEvent.CardFlipped -> {
                // Simulate flip animation
                animationPlayed = true
            }
            else -> {}
        }
    }
    
    fun simulateCardTap(playerId: Int, slotIndex: Int) {
        // Simulate user tapping a card
        gcms.processCommand(
            GCMSCommand.FlipCard(playerId = playerId, slotIndex = slotIndex)
        )
    }
}

/**
 * Mock Audio Engine for testing
 */
class MockAudioEngine(private val gcms: GCMSController) {
    val receivedEvents = mutableListOf<GCMSEvent>()
    var soundPlayed = false
    
    suspend fun startListening() {
        gcms.events.collect { event ->
            receivedEvents.add(event)
            handleEvent(event)
        }
    }
    
    private fun handleEvent(event: GCMSEvent) {
        when (event) {
            is GCMSEvent.GameInitialized -> {
                // Play initialization sound
                soundPlayed = true
            }
            is GCMSEvent.CardFlipped -> {
                // Play flip sound
                soundPlayed = true
            }
            is GCMSEvent.PlaySound -> {
                // Play requested sound
                soundPlayed = true
            }
            else -> {}
        }
    }
}

/**
 * Mock Renderer for testing
 */
class MockRenderer(private val gcms: GCMSController) {
    val receivedEvents = mutableListOf<GCMSEvent>()
    var rendered = false
    
    suspend fun startListening() {
        gcms.events.collect { event ->
            receivedEvents.add(event)
            handleEvent(event)
        }
    }
    
    private fun handleEvent(event: GCMSEvent) {
        when (event) {
            is GCMSEvent.GameInitialized -> {
                // Render initial game state
                rendered = true
            }
            is GCMSEvent.StateChanged -> {
                // Re-render
                rendered = true
            }
            else -> {}
        }
    }
}