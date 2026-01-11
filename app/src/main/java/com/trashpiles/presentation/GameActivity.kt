package com.trashpiles.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.trashpiles.R
import com.trashpiles.gcms.*
import com.trashpiles.game.*
import com.trashpiles.native.AudioEngineBridge
import com.trashpiles.native.RendererBridge
import com.trashpiles.utils.AssetLoader
import kotlinx.coroutines.launch

/**
 * Game Activity - Main game screen
 * 
 * Coordinates all game components:
 * - GCMS Controller (game logic)
 * - Game Flow Controller (automatic flow)
 * - Game Renderer (visuals)
 * - Game Audio (sounds)
 * - Asset Loader (resources)
 */
class GameActivity : AppCompatActivity() {
    
    // Core components
    private lateinit var gcms: GCMSController
    private lateinit var gameFlow: GameFlowController
    private lateinit var assetLoader: AssetLoader
    
    // Rendering and audio (will be initialized when native libs are ready)
    private var renderer: GameRenderer? = null
    private var audio: GameAudio? = null
    private var rendererBridge: RendererBridge? = null
    private var audioBridge: AudioEngineBridge? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        // Initialize core components
        initializeComponents()
        
        // Load assets
        loadAssets()
        
        // Subscribe to GCMS events for UI updates
        subscribeToEvents()
        
        // Start a new game
        startNewGame()
    }
    
    /**
     * Initialize all game components
     */
    private fun initializeComponents() {
        // Create GCMS controller
        gcms = GCMSController()
        
        // Create asset loader
        assetLoader = AssetLoader(this)
        
        // Create game flow controller
        gameFlow = GameFlowController(gcms)
        gameFlow.start()
        
        // Initialize native bridges (when ready)
        try {
            // TODO: Uncomment when native libraries are built
            // rendererBridge = RendererBridge(1920, 1080)
            // audioBridge = AudioEngineBridge()
            
            // Create renderer and audio
            // renderer = GameRenderer(gcms, rendererBridge!!, assetLoader)
            // audio = GameAudio(gcms, audioBridge!!, assetLoader)
            
            // Start renderer and audio
            // renderer?.start()
            // audio?.start()
        } catch (e: UnsatisfiedLinkError) {
            Toast.makeText(
                this,
                "Native libraries not available. Running in test mode.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    /**
     * Load all game assets
     */
    private fun loadAssets() {
        lifecycleScope.launch {
            try {
                assetLoader.loadAllAssets { progress ->
                    // Update loading progress
                    runOnUiThread {
                        // TODO: Update loading bar
                    }
                }
                
                Toast.makeText(
                    this@GameActivity,
                    "Assets loaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@GameActivity,
                    "Failed to load assets: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    /**
     * Subscribe to GCMS events for UI updates
     */
    private fun subscribeToEvents() {
        lifecycleScope.launch {
            gcms.events.collect { event ->
                handleGCMSEvent(event)
            }
        }
    }
    
    /**
     * Handle GCMS events and update UI
     */
    private fun handleGCMSEvent(event: GCMSEvent) {
        when (event) {
            is GCMSEvent.GameInitialized -> {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Game initialized with ${event.playerCount} players",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            is GCMSEvent.GameStarted -> {
                runOnUiThread {
                    Toast.makeText(this, "Game started!", Toast.LENGTH_SHORT).show()
                }
            }
            
            is GCMSEvent.TurnStarted -> {
                runOnUiThread {
                    updateTurnIndicator(event.playerId, event.playerName)
                }
            }
            
            is GCMSEvent.RoundWon -> {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "${event.playerName} won the round!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            
            is GCMSEvent.GameEnded -> {
                runOnUiThread {
                    showGameOverDialog()
                }
            }
            
            is GCMSEvent.CommandRejected -> {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Invalid move: ${event.reason}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            else -> {
                // Other events handled by renderer/audio
            }
        }
    }
    
    /**
     * Start a new game
     */
    private fun startNewGame() {
        // Get player configuration from intent or use defaults
        val playerCount = intent.getIntExtra("playerCount", 2)
        val playerNames = intent.getStringArrayListExtra("playerNames")
            ?: listOf("Player 1", "Player 2")
        val isAI = intent.getBooleanArrayExtra("isAI")?.toList()
            ?: listOf(false, true)
        
        // Initialize game
        gameFlow.initializeGame(
            playerCount = playerCount,
            playerNames = playerNames,
            isAI = isAI
        )
        
        // Start game
        gameFlow.startGame()
    }
    
    /**
     * Update turn indicator UI
     */
    private fun updateTurnIndicator(playerId: Int, playerName: String) {
        // TODO: Update UI to show current player
        // findViewById<TextView>(R.id.turnIndicator).text = "$playerName's turn"
    }
    
    /**
     * Show game over dialog
     */
    private fun showGameOverDialog() {
        val state = gcms.currentState
        val winner = state.players.maxByOrNull { it.score }
        
        // TODO: Show proper dialog with scores
        Toast.makeText(
            this,
            "Game Over! Winner: ${winner?.name}",
            Toast.LENGTH_LONG
        ).show()
    }
    
    /**
     * Handle user card tap
     */
    fun onCardTapped(playerId: Int, slotIndex: Int) {
        // Send flip card command to GCMS
        gcms.processCommand(
            GCMSCommand.FlipCard(
                playerId = playerId,
                slotIndex = slotIndex
            )
        )
    }
    
    /**
     * Handle draw from deck button
     */
    fun onDrawFromDeck() {
        val currentPlayer = gcms.currentState.currentPlayerIndex
        gcms.processCommand(
            GCMSCommand.DrawCard(
                playerId = currentPlayer,
                fromPile = "deck"
            )
        )
    }
    
    /**
     * Handle draw from discard button
     */
    fun onDrawFromDiscard() {
        val currentPlayer = gcms.currentState.currentPlayerIndex
        gcms.processCommand(
            GCMSCommand.DrawCard(
                playerId = currentPlayer,
                fromPile = "discard"
            )
        )
    }
    
    /**
     * Handle end turn button
     */
    fun onEndTurn() {
        val currentPlayer = gcms.currentState.currentPlayerIndex
        gcms.processCommand(
            GCMSCommand.EndTurn(playerId = currentPlayer)
        )
    }
    
    /**
     * Handle pause button
     */
    fun onPause() {
        gameFlow.pauseGame()
        audio?.pauseMusic()
    }
    
    /**
     * Handle resume
     */
    fun onResume() {
        gameFlow.resumeGame()
        audio?.resumeMusic()
    }
    
    /**
     * Handle menu button
     */
    fun onMenu() {
        // TODO: Show menu dialog
    }
    
    override fun onPause() {
        super.onPause()
        audio?.pauseMusic()
    }
    
    override fun onResume() {
        super.onResume()
        audio?.resumeMusic()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up all components
        gameFlow.destroy()
        renderer?.destroy()
        audio?.destroy()
        rendererBridge?.destroy()
        audioBridge?.destroy()
        gcms.destroy()
        assetLoader.clearCache()
    }
}