package com.trashpiles.gcms

import com.trashpiles.gcms.handlers.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * GCMS Controller - Refactored Version
 * 
 * The "brain" and "brake relay" of the game.
 * 
 * Responsibilities:
 * 1. Command routing to domain-specific handlers
 * 2. Command queue management
 * 3. Event broadcasting
 * 4. State history management
 * 
 * This controller is now a lightweight router that delegates command execution
 * to specialized handlers, preventing it from becoming a "god class".
 */
class GCMSControllerRefactored {
    
    // Current game state
    private var currentState: GCMSState = GCMSState()
    
    // State history for undo functionality
    private val stateHistory = mutableListOf<GCMSState>()
    private val maxHistorySize = 50
    
    // Event broadcasting
    private val _events = MutableSharedFlow<GCMSEvent>(replay = 0)
    val events: SharedFlow<GCMSEvent> = _events.asSharedFlow()
    
    // Command queue
    private val commandQueue = mutableListOf<GCMSCommand>()
    private val queueMutex = Mutex()
    
    // Domain-specific command handlers
    private val turnHandler = TurnCommandHandler()
    private val cardHandler = CardCommandHandler()
    private val skillHandler = SkillCommandHandler()
    private val matchHandler = MatchCommandHandler()
    
    /**
     * Submit a command for execution
     * 
     * Commands are validated, queued, and executed in order.
     */
    suspend fun submitCommand(command: GCMSCommand) {
        queueMutex.withLock {
            commandQueue.add(command)
        }
        processNextCommand()
    }
    
    /**
     * Process the next command in the queue
     */
    private suspend fun processNextCommand() {
        val command = queueMutex.withLock {
            if (commandQueue.isEmpty()) return
            commandQueue.removeAt(0)
        }
        
        // Validate command
        val validationResult = GCMSValidator.validate(command, currentState)
        if (!validationResult.isValid) {
            _events.emit(GCMSEvent.InvalidMoveEvent(
                reason = validationResult.reason,
                attemptedAction = command::class.simpleName ?: "Unknown"
            ))
            return
        }
        
        // Save state for undo
        saveStateToHistory()
        
        // Route command to appropriate handler
        val result = routeCommand(command)
        
        // Update state
        currentState = result.state
        
        // Emit all events
        result.events.forEach { event ->
            _events.emit(event)
        }
    }
    
    /**
     * Route command to the appropriate domain handler
     */
    private suspend fun routeCommand(command: GCMSCommand): CommandResult {
        return when (command) {
            // Turn commands
            is GCMSCommand.EndTurnCommand,
            is GCMSCommand.SkipTurnCommand -> turnHandler.handle(command, currentState)
            
            // Card commands
            is GCMSCommand.DrawCardCommand,
            is GCMSCommand.PlaceCardCommand,
            is GCMSCommand.DiscardCardCommand,
            is GCMSCommand.FlipCardCommand -> cardHandler.handle(command, currentState)
            
            // Skill/Ability commands
            is GCMSCommand.UnlockNodeCommand,
            is GCMSCommand.UseAbilityCommand -> skillHandler.handle(command, currentState)
            
            // Match commands
            is GCMSCommand.InitializeGameCommand,
            is GCMSCommand.StartGameCommand,
            is GCMSCommand.EndGameCommand,
            is GCMSCommand.ResetGameCommand,
            is GCMSCommand.SaveGameCommand,
            is GCMSCommand.LoadGameCommand,
            is GCMSCommand.RequestAIActionCommand -> matchHandler.handle(command, currentState)
            
            // Unknown commands
            else -> CommandResult(currentState, listOf(
                GCMSEvent.InvalidMoveEvent(
                    reason = "Unknown command type: ${command::class.simpleName}",
                    attemptedAction = "SubmitCommand"
                )
            ))
        }
    }
    
    /**
     * Save current state to history for undo
     */
    private fun saveStateToHistory() {
        stateHistory.add(currentState.copy())
        
        // Limit history size
        if (stateHistory.size > maxHistorySize) {
            stateHistory.removeAt(0)
        }
    }
    
    /**
     * Get current game state (read-only)
     */
    fun getCurrentState(): GCMSState = currentState.copy()
    
    /**
     * Get state history (read-only)
     */
    fun getStateHistory(): List<GCMSState> = stateHistory.map { it.copy() }
    
    /**
     * Clear command queue
     */
    suspend fun clearQueue() {
        queueMutex.withLock {
            commandQueue.clear()
        }
    }
    
    /**
     * Get queue size
     */
    suspend fun getQueueSize(): Int {
        return queueMutex.withLock {
            commandQueue.size
        }
    }
}