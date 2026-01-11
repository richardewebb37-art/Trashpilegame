package com.trashpiles.game

import com.trashpiles.gcms.*
import com.trashpiles.native.RendererBridge
import com.trashpiles.utils.AssetLoader
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import android.util.Log

/**
 * Game Renderer - Connects GCMS events to Skia renderer
 * 
 * Subscribes to GCMS events and triggers rendering operations
 * Maps game state changes to visual updates
 */
class GameRenderer(
    private val gcms: GCMSController,
    private val rendererBridge: RendererBridge,
    private val assetLoader: AssetLoader
) {
    
    companion object {
        private const val TAG = "GameRenderer"
    }
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var eventJob: Job? = null
    
    // Card positions cache
    private val cardPositions = mutableMapOf<String, CardPosition>()
    
    /**
     * Start listening to GCMS events
     */
    fun start() {
        eventJob = scope.launch {
            gcms.events.collect { event ->
                handleEvent(event)
            }
        }
        
        // Initial render
        renderGameState(gcms.currentState)
    }
    
    /**
     * Stop listening to events
     */
    fun stop() {
        eventJob?.cancel()
    }
    
    /**
     * Handle GCMS events and trigger rendering
     */
    private fun handleEvent(event: GCMSEvent) {
        when (event) {
            is GCMSEvent.GameInitialized -> {
                Log.d(TAG, "Game initialized, preparing render")
                renderGameState(gcms.currentState)
            }
            
            is GCMSEvent.GameStarted -> {
                Log.d(TAG, "Game started")
                renderGameState(gcms.currentState)
            }
            
            is GCMSEvent.CardDealt -> {
                Log.d(TAG, "Card dealt: ${event.cardId} to player ${event.toPlayerId}")
                animateCardDeal(event.cardId, event.toPlayerId)
            }
            
            is GCMSEvent.CardDrawn -> {
                Log.d(TAG, "Card drawn: ${event.cardId} from ${event.fromPile}")
                animateCardDraw(event.cardId, event.fromPile, event.byPlayerId)
            }
            
            is GCMSEvent.CardPlaced -> {
                Log.d(TAG, "Card placed: ${event.cardId} at slot ${event.slotIndex}")
                animateCardPlace(event.cardId, event.playerId, event.slotIndex)
            }
            
            is GCMSEvent.CardFlipped -> {
                Log.d(TAG, "Card flipped: ${event.cardId}")
                animateCardFlip(event.cardId, event.isFaceUp)
            }
            
            is GCMSEvent.CardDiscarded -> {
                Log.d(TAG, "Card discarded: ${event.cardId}")
                animateCardDiscard(event.cardId, event.byPlayerId)
            }
            
            is GCMSEvent.TurnStarted -> {
                Log.d(TAG, "Turn started for player ${event.playerId}")
                highlightPlayer(event.playerId)
            }
            
            is GCMSEvent.TurnEnded -> {
                Log.d(TAG, "Turn ended for player ${event.playerId}")
                unhighlightPlayer(event.playerId)
            }
            
            is GCMSEvent.RoundWon -> {
                Log.d(TAG, "Round won by player ${event.playerId}")
                showVictoryAnimation(event.playerId)
            }
            
            is GCMSEvent.StateChanged -> {
                Log.d(TAG, "State changed, re-rendering")
                renderGameState(event.stateSnapshot)
            }
            
            is GCMSEvent.GameEnded -> {
                Log.d(TAG, "Game ended")
                showGameOverScreen()
            }
            
            else -> {
                // Other events don't need rendering
            }
        }
    }
    
    /**
     * Render the complete game state
     */
    private fun renderGameState(state: GCMSState) {
        // Clear screen
        // rendererBridge.clear()
        
        // Render background
        renderBackground()
        
        // Render deck and discard pile
        renderDeck(state.deck.size)
        renderDiscardPile(state.discardPile)
        
        // Render each player's hand
        state.players.forEachIndexed { index, player ->
            renderPlayerHand(player, index)
        }
        
        // Render UI elements
        renderUI(state)
        
        // Present frame
        rendererBridge.present()
    }
    
    /**
     * Render background
     */
    private fun renderBackground() {
        val background = assetLoader.getBackgroundImage("game_table")
        // TODO: Render background with Skia
        // rendererBridge.renderBackground(background)
    }
    
    /**
     * Render deck pile
     */
    private fun renderDeck(cardCount: Int) {
        if (cardCount > 0) {
            val cardBack = assetLoader.getCardBackImage()
            val position = getDeckPosition()
            // TODO: Render card back at deck position
            // rendererBridge.renderCard("deck", position.x, position.y, 0f, false)
        }
    }
    
    /**
     * Render discard pile
     */
    private fun renderDiscardPile(discardPile: List<CardState>) {
        if (discardPile.isNotEmpty()) {
            val topCard = discardPile.last()
            val position = getDiscardPilePosition()
            // TODO: Render top card of discard pile
            // rendererBridge.renderCard(topCard.id, position.x, position.y, 0f, true)
        }
    }
    
    /**
     * Render player's hand
     */
    private fun renderPlayerHand(player: PlayerState, playerIndex: Int) {
        player.hand.forEachIndexed { slotIndex, card ->
            val position = calculateCardPosition(playerIndex, slotIndex)
            cardPositions[card.id] = position
            
            // TODO: Render card
            // rendererBridge.renderCard(
            //     card.id,
            //     position.x,
            //     position.y,
            //     position.rotation,
            //     card.isFaceUp
            // )
        }
    }
    
    /**
     * Render UI elements (scores, turn indicator, etc.)
     */
    private fun renderUI(state: GCMSState) {
        // Render current player indicator
        val currentPlayer = state.players[state.currentPlayerIndex]
        // TODO: Render turn indicator
        
        // Render scores
        state.players.forEach { player ->
            // TODO: Render player score
        }
        
        // Render round number
        // TODO: Render round indicator
    }
    
    /**
     * Animate card dealing
     */
    private fun animateCardDeal(cardId: String, toPlayerId: Int) {
        scope.launch {
            val startPos = getDeckPosition()
            val endPos = calculateCardPosition(toPlayerId, 0) // Approximate position
            
            // TODO: Animate card movement from deck to player hand
            // animateCardMovement(cardId, startPos, endPos, 300)
            
            renderGameState(gcms.currentState)
        }
    }
    
    /**
     * Animate card draw
     */
    private fun animateCardDraw(cardId: String, fromPile: String, byPlayerId: Int) {
        scope.launch {
            val startPos = if (fromPile == "deck") getDeckPosition() else getDiscardPilePosition()
            val endPos = getPlayerDrawPosition(byPlayerId)
            
            // TODO: Animate card movement
            // animateCardMovement(cardId, startPos, endPos, 300)
            
            renderGameState(gcms.currentState)
        }
    }
    
    /**
     * Animate card placement
     */
    private fun animateCardPlace(cardId: String, playerId: Int, slotIndex: Int) {
        scope.launch {
            val startPos = getPlayerDrawPosition(playerId)
            val endPos = calculateCardPosition(playerId, slotIndex)
            
            // TODO: Animate card movement
            // animateCardMovement(cardId, startPos, endPos, 300)
            
            renderGameState(gcms.currentState)
        }
    }
    
    /**
     * Animate card flip
     */
    private fun animateCardFlip(cardId: String, isFaceUp: Boolean) {
        scope.launch {
            // TODO: Animate 180° rotation
            // animateCardRotation(cardId, if (isFaceUp) 0f else 180f, 300)
            
            renderGameState(gcms.currentState)
        }
    }
    
    /**
     * Animate card discard
     */
    private fun animateCardDiscard(cardId: String, byPlayerId: Int) {
        scope.launch {
            val startPos = getPlayerDrawPosition(byPlayerId)
            val endPos = getDiscardPilePosition()
            
            // TODO: Animate card movement
            // animateCardMovement(cardId, startPos, endPos, 300)
            
            renderGameState(gcms.currentState)
        }
    }
    
    /**
     * Highlight current player
     */
    private fun highlightPlayer(playerId: Int) {
        // TODO: Add visual highlight around player's area
        renderGameState(gcms.currentState)
    }
    
    /**
     * Remove player highlight
     */
    private fun unhighlightPlayer(playerId: Int) {
        // TODO: Remove visual highlight
        renderGameState(gcms.currentState)
    }
    
    /**
     * Show victory animation
     */
    private fun showVictoryAnimation(playerId: Int) {
        scope.launch {
            // TODO: Show celebration animation (particles, etc.)
            delay(2000)
            renderGameState(gcms.currentState)
        }
    }
    
    /**
     * Show game over screen
     */
    private fun showGameOverScreen() {
        // TODO: Render game over screen with final scores
    }
    
    // ========================================================================
    // POSITION CALCULATION HELPERS
    // ========================================================================
    
    /**
     * Calculate card position based on player and slot
     */
    private fun calculateCardPosition(playerIndex: Int, slotIndex: Int): CardPosition {
        // TODO: Calculate actual positions based on screen size and layout
        val baseX = 100f + (slotIndex * 120f)
        val baseY = 100f + (playerIndex * 300f)
        
        return CardPosition(baseX, baseY, 0f)
    }
    
    /**
     * Get deck position
     */
    private fun getDeckPosition(): CardPosition {
        return CardPosition(50f, 500f, 0f)
    }
    
    /**
     * Get discard pile position
     */
    private fun getDiscardPilePosition(): CardPosition {
        return CardPosition(200f, 500f, 0f)
    }
    
    /**
     * Get player draw position (temporary position for drawn card)
     */
    private fun getPlayerDrawPosition(playerId: Int): CardPosition {
        return CardPosition(400f, 100f + (playerId * 300f), 0f)
    }
    
    /**
     * Clean up resources
     */
    fun destroy() {
        stop()
        scope.cancel()
    }
    
    /**
     * Card position data class
     */
    data class CardPosition(
        val x: Float,
        val y: Float,
        val rotation: Float
    )
}