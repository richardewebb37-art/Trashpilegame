package com.trashpiles.gcms.handlers

import com.trashpiles.gcms.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for MatchCommandHandler
 */
class MatchCommandHandlerTest {
    
    private val handler = MatchCommandHandler()
    
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
    fun `handle InitializeGameCommand creates players and deck`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.InitializeGameCommand(
            playerCount = 2,
            playerNames = listOf("Alice", "Bob"),
            isAI = listOf(false, true),
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(2, result.state.players.size)
        assertEquals("Alice", result.state.players[0].name)
        assertEquals("Bob", result.state.players[1].name)
        assertEquals(false, result.state.players[0].isAI)
        assertEquals(true, result.state.players[1].isAI)
        assertEquals(GamePhase.SETUP, result.state.currentPhase)
        assertEquals(1, result.events.size)
        
        val event = result.events[0] as? GCMSEvent.GameInitializedEvent
        assertNotNull(event)
        assertEquals(2, event.playerCount)
        assertEquals(listOf("Alice", "Bob"), event.playerNames)
    }
    
    @Test
    fun `handle StartGameCommand deals cards to all players`() = runBlocking {
        val deck = (1..20).map { i ->
            CardState(
                id = "card$i",
                rank = (i % 13).toString(),
                suit = "hearts",
                value = i % 13,
                faceUp = false
            )
        }
        val state = createTestState().copy(deck = deck)
        val command = GCMSCommand.StartGameCommand(
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(GamePhase.PLAYING, result.state.currentPhase)
        
        // Each player should have 10 cards
        assertEquals(10, result.state.players[0].hand.size)
        assertEquals(10, result.state.players[1].hand.size)
        
        // All cards should be face down initially
        result.state.players.forEach { player ->
            player.hand.forEach { card ->
                assertEquals(false, card.faceUp)
            }
        }
        
        // Should have GameStartedEvent + CardDealtEvent for each card (20 events total)
        assertTrue(result.events.size >= 2)
        
        val gameStartedEvent = result.events.find { it is GCMSEvent.GameStartedEvent }
        assertNotNull(gameStartedEvent)
        
        val cardDealtEvents = result.events.filterIsInstance<GCMSEvent.CardDealtEvent>()
        assertTrue(cardDealtEvents.size >= 20)
    }
    
    @Test
    fun `handle EndGameCommand with completed reason emits game over event`() = runBlocking {
        val state = createTestState().copy(
            players = listOf(
                PlayerState(
                    id = 0,
                    name = "Player 1",
                    hand = emptyList(),
                    score = 10,
                    isAI = false,
                    hasFinished = false
                ),
                PlayerState(
                    id = 1,
                    name = "Player 2",
                    hand = emptyList(),
                    score = 5,
                    isAI = true,
                    hasFinished = false
                )
            ),
            currentPhase = GamePhase.PLAYING
        )
        val command = GCMSCommand.EndGameCommand(
            reason = "completed",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(GamePhase.GAME_OVER, result.state.currentPhase)
        
        val gameOverEvent = result.events.find { it is GCMSEvent.GameOverEvent }
        assertNotNull(gameOverEvent)
        
        val event = gameOverEvent as GCMSEvent.GameOverEvent
        assertEquals(0, event.winnerId)
        assertEquals("Player 1", event.winnerName)
        assertEquals(10, event.finalScores[0])
        assertEquals(5, event.finalScores[1])
    }
    
    @Test
    fun `handle EndGameCommand emits skill/ability reward events`() = runBlocking {
        val skillSystem = SkillAbilitySystemState()
        val progress = PlayerProgress(
            playerId = "0",
            totalSP = 0,
            totalAP = 0,
            level = 1,
            totalXP = 0,
            matchesPlayed = 0,
            roundsPlayed = 0
        )
        skillSystem.playerProgress["0"] = progress
        
        val faceUpCards = (1..5).map { i ->
            CardState(
                id = "card$i",
                rank = i.toString(),
                suit = "hearts",
                value = i,
                faceUp = true
            )
        }
        
        val state = createTestState().copy(
            players = listOf(
                PlayerState(
                    id = 0,
                    name = "Player 1",
                    hand = faceUpCards,
                    score = 10,
                    isAI = false,
                    hasFinished = false
                )
            ),
            skillAbilitySystem = skillSystem
        )
        val command = GCMSCommand.EndGameCommand(
            reason = "completed",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        // Should have GameOverEvent, MatchCompletedEvent, PointsEarnedEvent
        val matchCompletedEvent = result.events.find { it is GCMSEvent.MatchCompletedEvent }
        assertNotNull(matchCompletedEvent)
        
        val matchEvent = matchCompletedEvent as GCMSEvent.MatchCompletedEvent
        assertEquals(5, matchEvent.spEarned) // 5 face-up cards
        
        val pointsEarnedEvent = result.events.find { it is GCMSEvent.PointsEarnedEvent }
        assertNotNull(pointsEarnedEvent)
    }
    
    @Test
    fun `handle ResetGameCommand resets state to initial`() = runBlocking {
        val state = createTestState().copy(
            currentPhase = GamePhase.PLAYING,
            players = listOf(
                PlayerState(
                    id = 0,
                    name = "Player 1",
                    hand = listOf(
                        CardState(id = "card1", rank = "A", suit = "hearts", value = 1, faceUp = true)
                    ),
                    score = 10,
                    isAI = false,
                    hasFinished = false
                )
            ),
            currentPlayerIndex = 1
        )
        val command = GCMSCommand.ResetGameCommand(
            keepPlayers = false,
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(GamePhase.SETUP, result.state.currentPhase)
        assertEquals(0, result.state.currentPlayerIndex)
        
        // Players should be reset
        result.state.players.forEach { player ->
            assertEquals(0, player.score)
            assertEquals(emptyList(), player.hand)
        }
        
        // Deck should be reshuffled
        assertTrue(result.state.deck.isNotEmpty())
    }
    
    @Test
    fun `handle SaveGameCommand returns empty events (TODO)`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.SaveGameCommand(
            saveName = "test_save",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(0, result.events.size) // TODO: Not implemented yet
    }
    
    @Test
    fun `handle LoadGameCommand returns empty events (TODO)`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.LoadGameCommand(
            saveId = "save123",
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(0, result.events.size) // TODO: Not implemented yet
    }
    
    @Test
    fun `handle RequestAIActionCommand returns empty events (TODO)`() = runBlocking {
        val state = createTestState()
        val command = GCMSCommand.RequestAIActionCommand(
            aiPlayerId = 1,
            timestamp = System.currentTimeMillis(),
            commandId = GCMSCommand.generateId()
        )
        
        val result = handler.handle(command, state)
        
        assertEquals(0, result.events.size) // TODO: Not implemented yet
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
            assertTrue(e.message?.contains("MatchCommandHandler cannot handle") == true)
        }
    }
}
</create_file>