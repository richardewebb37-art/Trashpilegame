package com.trashpiles.game

import com.trashpiles.gcms.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Game Flow Controller - Orchestrates automatic game flow
 * 
 * Manages:
 * - Automatic card dealing
 * - Turn progression
 * - Round transitions
 * - Win detection
 * - Game over handling
 */
class GameFlowController(private val gcms: GCMSController) {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var eventJob: Job? = null
    
    /**
     * Start listening to GCMS events and managing game flow
     */
    fun start() {
        eventJob = scope.launch {
            gcms.events.collect { event ->
                handleEvent(event)
            }
        }
    }
    
    /**
     * Stop listening to events
     */
    fun stop() {
        eventJob?.cancel()
    }
    
    /**
     * Handle GCMS events and orchestrate game flow
     */
    private suspend fun handleEvent(event: GCMSEvent) {
        when (event) {
            is GCMSEvent.GameStarted -> {
                // Game started, dealing will happen automatically in GCMS
            }
            
            is GCMSEvent.DealingCompleted -> {
                // Cards dealt, first turn already started by GCMS
            }
            
            is GCMSEvent.CardPlaced -> {
                // Check if this was the last card needed
                val state = gcms.currentState
                val player = state.players.find { it.id == event.playerId }
                
                if (player != null && GameRules.checkWinCondition(state, event.playerId)) {
                    // Player won the round
                    handleRoundWin(event.playerId)
                }
            }
            
            is GCMSEvent.RoundWon -> {
                // Round won, prepare for next round
                delay(2000) // Give time for celebration
                handleRoundTransition(event.playerId)
            }
            
            is GCMSEvent.TurnStarted -> {
                // Check if this is an AI player
                val state = gcms.currentState
                val player = state.players[event.playerId]
                
                if (player.isAI) {
                    // Request AI to make a move
                    delay(500) // Small delay for realism
                    gcms.processCommand(GCMSCommand.RequestAIMove(playerId = event.playerId))
                }
            }
            
            is GCMSEvent.AITurnStarted -> {
                // AI turn started, AI controller will handle this
            }
            
            is GCMSEvent.GameEnded -> {
                // Game over
                handleGameOver()
            }
            
            else -> {
                // Other events don't need flow control
            }
        }
    }
    
    /**
     * Handle round win
     */
    private suspend fun handleRoundWin(winnerId: Int) {
        val state = gcms.currentState
        
        // Update winner's score
        val winner = state.players[winnerId]
        val newScore = winner.score + calculateRoundScore(state.currentRound)
        
        // Check if this was the final round
        if (state.currentRound >= 10) {
            // Game over - find overall winner
            val overallWinner = state.players.maxByOrNull { it.score }
            if (overallWinner != null) {
                gcms.processCommand(GCMSCommand.EndGame)
            }
        }
    }
    
    /**
     * Handle round transition
     */
    private suspend fun handleRoundTransition(winnerId: Int) {
        val state = gcms.currentState
        
        if (state.currentRound < 10) {
            // Start next round
            gcms.processCommand(GCMSCommand.StartGame)
        } else {
            // Game over
            gcms.processCommand(GCMSCommand.EndGame)
        }
    }
    
    /**
     * Handle game over
     */
    private suspend fun handleGameOver() {
        val state = gcms.currentState
        
        // Find winner (highest score)
        val winner = state.players.maxByOrNull { it.score }
        
        if (winner != null) {
            // Could emit a custom event or handle UI update here
        }
    }
    
    /**
     * Calculate score for completing a round
     */
    private fun calculateRoundScore(round: Int): Int {
        // Score decreases as rounds progress (fewer cards = harder)
        return when (round) {
            1 -> 100
            2 -> 200
            3 -> 300
            4 -> 400
            5 -> 500
            6 -> 600
            7 -> 700
            8 -> 800
            9 -> 900
            10 -> 1000
            else -> 100
        }
    }
    
    /**
     * Initialize a new game
     */
    fun initializeGame(
        playerCount: Int,
        playerNames: List<String>,
        isAI: List<Boolean>
    ) {
        gcms.processCommand(
            GCMSCommand.InitializeGame(
                playerCount = playerCount,
                playerNames = playerNames,
                isAI = isAI
            )
        )
    }
    
    /**
     * Start the game
     */
    fun startGame() {
        gcms.processCommand(GCMSCommand.StartGame)
    }
    
    /**
     * Pause the game
     */
    fun pauseGame() {
        gcms.processCommand(GCMSCommand.PauseGame)
    }
    
    /**
     * Resume the game
     */
    fun resumeGame() {
        gcms.processCommand(GCMSCommand.ResumeGame)
    }
    
    /**
     * End the game
     */
    fun endGame() {
        gcms.processCommand(GCMSCommand.EndGame)
    }
    
    /**
     * Reset the game
     */
    fun resetGame() {
        gcms.processCommand(GCMSCommand.ResetGame)
    }
    
    /**
     * Clean up resources
     */
    fun destroy() {
        stop()
        scope.cancel()
    }
}