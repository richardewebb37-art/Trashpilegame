package com.trashpiles.game

import com.trashpiles.gcms.*
import com.trashpiles.presentation.components.SmileyFaceView
import kotlinx.coroutines.*

/**
 * Controller for managing the SmileyFace component
 * Reacts to game events and updates the smiley face accordingly
 */
class SmileyFaceController(
    private val smileyFaceView: SmileyFaceView
) {
    
    // Player colors
    private val playerColors = mapOf(
        0 to 0xFFE53935.toInt(), // Red - Player 1
        1 to 0xFF1E88E5.toInt(), // Blue - Player 2
        2 to 0xFF43A047.toInt(), // Green - Player 3
        3 to 0xFFFDD835.toInt()  // Yellow - Player 4
    )
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    /**
     * Process a game event and update smiley face accordingly
     */
    fun processEvent(event: GCMSEvent, gameState: GCMSState) {
        when (event) {
            // Turn events
            is TurnStartedEvent -> onTurnStarted(event.playerIndex)
            is TurnEndedEvent -> onTurnEnded()
            
            // Card events
            is CardPlacedEvent -> onCardPlaced(event)
            is CardDrawnEvent -> onCardDrawn()
            is InvalidMoveEvent -> onInvalidMove()
            
            // Game events
            is RoundEndedEvent -> onRoundEnded(event, gameState)
            is GameEndedEvent -> onGameEnded(event, gameState)
            
            // Skill events
            is AbilityUsedEvent -> onAbilityUsed()
            is SkillUnlockedEvent -> onSkillUnlocked()
            
            else -> { /* Other events don't affect smiley face */ }
        }
    }
    
    /**
     * Handle turn started event
     */
    private fun onTurnStarted(playerIndex: Int) {
        val playerColor = playerColors[playerIndex] ?: 0xFFFF6F00.toInt()
        
        smileyFaceView.setColor(playerColor, animate = true)
        smileyFaceView.setState(SmileyFaceView.State.THINKING, animate = true)
        
        // After 1 second, transition to normal
        scope.launch {
            delay(1000)
            smileyFaceView.setState(SmileyFaceView.State.NORMAL, animate = true)
        }
    }
    
    /**
     * Handle turn ended event
     */
    private fun onTurnEnded() {
        smileyFaceView.setState(SmileyFaceView.State.TRANSITIONING, animate = true)
    }
    
    /**
     * Handle card placed event
     */
    private fun onCardPlaced(event: CardPlacedEvent) {
        // If card was successfully placed, show happy face
        smileyFaceView.setState(SmileyFaceView.State.HAPPY, animate = true)
        
        // Check if player is winning
        if (event.isWinningMove) {
            smileyFaceView.setState(SmileyFaceView.State.CELEBRATING, animate = true)
            smileyFaceView.bounce()
        }
        
        // Return to normal after short delay
        scope.launch {
            delay(500)
            smileyFaceView.setState(SmileyFaceView.State.NORMAL, animate = true)
        }
    }
    
    /**
     * Handle card drawn event
     */
    private fun onCardDrawn() {
        // Briefly show happy face for successful draw
        smileyFaceView.setState(SmileyFaceView.State.HAPPY, animate = true)
        
        scope.launch {
            delay(300)
            smileyFaceView.setState(SmileyFaceView.State.NORMAL, animate = true)
        }
    }
    
    /**
     * Handle invalid move event
     */
    private fun onInvalidMove() {
        // Show confused face and shake
        smileyFaceView.setState(SmileyFaceView.State.CONFUSED, animate = true)
        smileyFaceView.shake()
        
        // Return to normal after shake
        scope.launch {
            delay(1000)
            smileyFaceView.setState(SmileyFaceView.State.NORMAL, animate = true)
        }
    }
    
    /**
     * Handle round ended event
     */
    private fun onRoundEnded(event: RoundEndedEvent, gameState: GCMSState) {
        if (event.isVictory) {
            // Player won the round - celebrate!
            smileyFaceView.setState(SmileyFaceView.State.CELEBRATING, animate = true)
            smileyFaceView.bounce()
            
            // Keep celebrating for 2 seconds
            scope.launch {
                delay(2000)
                smileyFaceView.setState(SmileyFaceView.State.NORMAL, animate = true)
            }
        } else {
            // Player lost the round - disappointed
            smileyFaceView.setState(SmileyFaceView.State.DISAPPOINTED, animate = true)
            
            scope.launch {
                delay(1500)
                smileyFaceView.setState(SmileyFaceView.State.NORMAL, animate = true)
            }
        }
    }
    
    /**
     * Handle game ended event
     */
    private fun onGameEnded(event: GameEndedEvent, gameState: GCMSState) {
        if (event.isVictory) {
            // Player won the game - big celebration!
            smileyFaceView.setState(SmileyFaceView.State.CELEBRATING, animate = true)
            smileyFaceView.bounce()
            
            // Bounce continuously for 3 seconds
            scope.launch {
                repeat(3) {
                    delay(500)
                    smileyFaceView.bounce()
                }
                delay(1000)
                smileyFaceView.setState(SmileyFaceView.State.NORMAL, animate = true)
            }
        } else {
            // Player lost the game - disappointed
            smileyFaceView.setState(SmileyFaceView.State.DISAPPOINTED, animate = true)
        }
    }
    
    /**
     * Handle ability used event
     */
    private fun onAbilityUsed() {
        // Show happy face for ability usage
        smileyFaceView.setState(SmileyFaceView.State.HAPPY, animate = true)
        
        scope.launch {
            delay(400)
            smileyFaceView.setState(SmileyFaceView.State.NORMAL, animate = true)
        }
    }
    
    /**
     * Handle skill unlocked event
     */
    private fun onSkillUnlocked() {
        // Show celebrating face for skill unlock
        smileyFaceView.setState(SmileyFaceView.State.CELEBRATING, animate = true)
        smileyFaceView.bounce()
        
        scope.launch {
            delay(800)
            smileyFaceView.setState(SmileyFaceView.State.NORMAL, animate = true)
        }
    }
    
    /**
     * Set player color for current player
     */
    fun setPlayerColor(playerIndex: Int) {
        val playerColor = playerColors[playerIndex] ?: 0xFFFF6F00.toInt()
        smileyFaceView.setColor(playerColor, animate = true)
    }
    
    /**
     * Update smiley face based on current game state
     */
    fun updateFromGameState(gameState: GCMSState) {
        val currentPlayer = gameState.currentPlayer
        
        // Update color to match current player
        currentPlayer?.let { player ->
            setPlayerColor(player.id)
            
            // Set state based on game phase
            val state = when (gameState.currentPhase) {
                GamePhase.PLAYING -> SmileyFaceView.State.NORMAL
                GamePhase.ROUND_END -> if (gameState.winnerId == player.id) {
                    SmileyFaceView.State.CELEBRATING
                } else {
                    SmileyFaceView.State.DISAPPOINTED
                }
                GamePhase.GAME_OVER -> SmileyFaceView.State.DISAPPOINTED
                else -> SmileyFaceView.State.NORMAL
            }
            
            smileyFaceView.setState(state, animate = true)
        }
    }
    
    /**
     * Reset smiley face to initial state
     */
    fun reset() {
        smileyFaceView.setColor(0xFFFF6F00.toInt(), animate = false)
        smileyFaceView.setState(SmileyFaceView.State.NORMAL, animate = false)
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        scope.cancel()
    }
}