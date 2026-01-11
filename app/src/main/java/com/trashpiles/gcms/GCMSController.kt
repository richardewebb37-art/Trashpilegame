package com.trashpiles.gcms

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * GCMS Controller - The authoritative brain of the game
 * 
 * This is the ONLY system that can modify game state.
 * All game actions flow through this controller.
 * 
 * Flow: Commands → Validation → Execution → State Update → Events
 */
class GCMSController {
    
    // Current game state (single source of truth)
    private var _state: GCMSState = GCMSState()
    val currentState: GCMSState
        get() = _state
    
    // Event broadcaster (SharedFlow for multiple subscribers)
    private val _events = MutableSharedFlow<GCMSEvent>(replay = 0)
    val events: SharedFlow<GCMSEvent> = _events.asSharedFlow()
    
    // Command queue with thread-safe access
    private val commandQueue = mutableListOf<GCMSCommand>()
    private val queueMutex = Mutex()
    
    // State history for undo functionality
    private val stateHistory = mutableListOf<GCMSState>()
    private val maxHistorySize = 50
    
    // Coroutine scope for async operations
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    /**
     * Process a command asynchronously
     */
    fun processCommand(command: GCMSCommand) {
        scope.launch {
            queueMutex.withLock {
                commandQueue.add(command)
            }
            processQueue()
        }
    }
    
    /**
     * Process all commands in the queue
     */
    private suspend fun processQueue() {
        queueMutex.withLock {
            while (commandQueue.isNotEmpty()) {
                val command = commandQueue.removeAt(0)
                handleCommand(command)
            }
        }
    }
    
    /**
     * Handle a single command
     */
    private suspend fun handleCommand(command: GCMSCommand) {
        // Step 1: Validate command
        val validationResult = GCMSValidator.validate(command, _state)
        
        if (!validationResult.isValid) {
            // Emit rejection event
            emitEvent(GCMSEvent.CommandRejected(
                command = command::class.simpleName ?: "Unknown",
                reason = validationResult.reason
            ))
            return
        }
        
        // Step 2: Save state to history (for undo)
        saveStateToHistory()
        
        // Step 3: Execute command and update state
        executeCommand(command)
    }
    
    /**
     * Execute a validated command and update state
     */
    private suspend fun executeCommand(command: GCMSCommand) {
        when (command) {
            is GCMSCommand.InitializeGame -> executeInitializeGame(command)
            is GCMSCommand.StartGame -> executeStartGame()
            is GCMSCommand.DrawCard -> executeDrawCard(command)
            is GCMSCommand.PlaceCard -> executePlaceCard(command)
            is GCMSCommand.DiscardCard -> executeDiscardCard(command)
            is GCMSCommand.FlipCard -> executeFlipCard(command)
            is GCMSCommand.EndTurn -> executeEndTurn(command)
            is GCMSCommand.SkipTurn -> executeSkipTurn(command)
            is GCMSCommand.PauseGame -> executePauseGame()
            is GCMSCommand.ResumeGame -> executeResumeGame()
            is GCMSCommand.EndGame -> executeEndGame()
            is GCMSCommand.ResetGame -> executeResetGame()
            is GCMSCommand.SaveGame -> executeSaveGame(command)
            is GCMSCommand.LoadGame -> executeLoadGame(command)
            is GCMSCommand.UndoMove -> executeUndoMove()
            is GCMSCommand.RequestAIMove -> executeRequestAIMove(command)
            is GCMSCommand.UpdateSettings -> executeUpdateSettings(command)
            is GCMSCommand.UnlockNode -> executeUnlockNode(command)
            is GCMSCommand.UseAbility -> executeUseAbility(command)
        }
    }
    
    // ========================================================================
    // COMMAND EXECUTION IMPLEMENTATIONS
    // ========================================================================
    
    /**
     * Initialize game with players
     */
    private suspend fun executeInitializeGame(command: GCMSCommand.InitializeGame) {
        // Create players
        val players = command.playerNames.mapIndexed { index, name ->
            PlayerState(
                id = index,
                name = name,
                isAI = command.isAI[index],
                hand = emptyList(),
                score = 0,
                isActive = true
            )
        }
        
        // Update state
        _state = _state.copy(
            currentPhase = GamePhase.SETUP,
            players = players,
            currentPlayerIndex = 0,
            currentRound = 1
        )
        
        // Emit event
        emitEvent(GCMSEvent.GameInitialized(
            playerCount = command.playerCount,
            playerNames = command.playerNames
        ))
    }
    
    /**
     * Start game - deal cards and begin first turn
     */
    private suspend fun executeStartGame() {
        // Transition to dealing phase
        _state = _state.copy(currentPhase = GamePhase.DEALING)
        emitEvent(GCMSEvent.GameStarted)
        emitEvent(GCMSEvent.DealingStarted)
        
        // Create and shuffle deck
        val deck = DeckBuilder.shuffle(DeckBuilder.createDeck())
        
        // Calculate cards per player for this round
        val cardsPerPlayer = GameRules.getCardsForRound(_state.currentRound)
        
        // Deal cards to each player
        var remainingDeck = deck
        val updatedPlayers = _state.players.map { player ->
            val (dealtCards, newDeck) = DeckBuilder.deal(remainingDeck, cardsPerPlayer)
            remainingDeck = newDeck
            
            // Emit card dealt event for each card
            dealtCards.forEach { card ->
                emitEvent(GCMSEvent.CardDealt(
                    cardId = card.id,
                    toPlayerId = player.id
                ))
            }
            
            player.copy(hand = dealtCards)
        }
        
        // Update state with dealt cards
        _state = _state.copy(
            players = updatedPlayers,
            deck = remainingDeck,
            currentPhase = GamePhase.PLAYING
        )
        
        emitEvent(GCMSEvent.DealingCompleted)
        
        // Start first player's turn
        emitEvent(GCMSEvent.TurnStarted(
            playerId = _state.currentPlayerIndex,
            playerName = _state.players[_state.currentPlayerIndex].name
        ))
    }
    
    /**
     * Draw card from deck or discard pile
     */
    private suspend fun executeDrawCard(command: GCMSCommand.DrawCard) {
        val card = if (command.fromPile == "deck") {
            // Draw from deck
            val drawnCard = _state.deck.first()
            _state = _state.copy(
                deck = _state.deck.drop(1)
            )
            drawnCard
        } else {
            // Draw from discard pile
            val drawnCard = _state.discardPile.last()
            _state = _state.copy(
                discardPile = _state.discardPile.dropLast(1)
            )
            drawnCard
        }
        
        // Emit event
        emitEvent(GCMSEvent.CardDrawn(
            cardId = card.id,
            fromPile = command.fromPile,
            byPlayerId = command.playerId
        ))
        
        // Play sound
        emitEvent(GCMSEvent.PlaySound(soundId = "card_draw"))
    }
    
    /**
     * Place card in player's hand slot
     */
    private suspend fun executePlaceCard(command: GCMSCommand.PlaceCard) {
        val player = _state.players[command.playerId]
        val card = _state.deck.find { it.id == command.cardId }
            ?: _state.discardPile.find { it.id == command.cardId }
            ?: return
        
        // Get the card that was in the slot (to discard)
        val replacedCard = player.hand[command.slotIndex]
        
        // Update player's hand
        val newHand = player.hand.toMutableList()
        newHand[command.slotIndex] = card.copy(isFaceUp = true)
        
        val updatedPlayer = player.copy(hand = newHand)
        val updatedPlayers = _state.players.toMutableList()
        updatedPlayers[command.playerId] = updatedPlayer
        
        // Remove card from deck/discard
        val updatedDeck = _state.deck.filter { it.id != command.cardId }
        val updatedDiscard = _state.discardPile.filter { it.id != command.cardId }
        
        // Add replaced card to discard pile (if it wasn't already face up)
        val finalDiscard = if (!replacedCard.isFaceUp) {
            updatedDiscard + replacedCard.copy(isFaceUp = true)
        } else {
            updatedDiscard
        }
        
        // Update state
        _state = _state.copy(
            players = updatedPlayers,
            deck = updatedDeck,
            discardPile = finalDiscard
        )
        
        // Emit events
        emitEvent(GCMSEvent.CardPlaced(
            cardId = card.id,
            playerId = command.playerId,
            slotIndex = command.slotIndex
        ))
        emitEvent(GCMSEvent.PlaySound(soundId = "card_place"))
        
        // Check win condition
        if (GameRules.checkWinCondition(_state, command.playerId)) {
            emitEvent(GCMSEvent.RoundWon(
                playerId = command.playerId,
                playerName = updatedPlayer.name
            ))
            _state = _state.copy(currentPhase = GamePhase.ROUND_END)
            emitEvent(GCMSEvent.PlaySound(soundId = "victory"))
        }
    }
    
    /**
     * Discard a card
     */
    private suspend fun executeDiscardCard(command: GCMSCommand.DiscardCard) {
        val card = _state.deck.find { it.id == command.cardId }
            ?: return
        
        // Remove from deck and add to discard pile
        _state = _state.copy(
            deck = _state.deck.filter { it.id != command.cardId },
            discardPile = _state.discardPile + card.copy(isFaceUp = true)
        )
        
        // Emit event
        emitEvent(GCMSEvent.CardDiscarded(
            cardId = card.id,
            byPlayerId = command.playerId
        ))
        emitEvent(GCMSEvent.PlaySound(soundId = "card_place"))
    }
    
    /**
     * Flip a card face up
     */
    private suspend fun executeFlipCard(command: GCMSCommand.FlipCard) {
        val player = _state.players[command.playerId]
        val card = player.hand[command.slotIndex]
        
        // Update card to face up
        val newHand = player.hand.toMutableList()
        newHand[command.slotIndex] = card.copy(isFaceUp = true)
        
        val updatedPlayer = player.copy(hand = newHand)
        val updatedPlayers = _state.players.toMutableList()
        updatedPlayers[command.playerId] = updatedPlayer
        
        _state = _state.copy(players = updatedPlayers)
        
        // Emit events
        emitEvent(GCMSEvent.CardFlipped(
            cardId = card.id,
            isFaceUp = true
        ))
        emitEvent(GCMSEvent.PlaySound(soundId = "card_flip"))
    }
    
    /**
     * End current player's turn
     */
    private suspend fun executeEndTurn(command: GCMSCommand.EndTurn) {
        // Emit turn ended event
        emitEvent(GCMSEvent.TurnEnded(playerId = command.playerId))
        
        // Move to next player
        val nextPlayerIndex = (_state.currentPlayerIndex + 1) % _state.players.size
        _state = _state.copy(currentPlayerIndex = nextPlayerIndex)
        
        // Emit turn started event for next player
        emitEvent(GCMSEvent.TurnStarted(
            playerId = nextPlayerIndex,
            playerName = _state.players[nextPlayerIndex].name
        ))
    }
    
    /**
     * Skip current player's turn
     */
    private suspend fun executeSkipTurn(command: GCMSCommand.SkipTurn) {
        emitEvent(GCMSEvent.TurnSkipped(playerId = command.playerId))
        
        // Move to next player
        val nextPlayerIndex = (_state.currentPlayerIndex + 1) % _state.players.size
        _state = _state.copy(currentPlayerIndex = nextPlayerIndex)
        
        emitEvent(GCMSEvent.TurnStarted(
            playerId = nextPlayerIndex,
            playerName = _state.players[nextPlayerIndex].name
        ))
    }
    
    /**
     * Pause game
     */
    private suspend fun executePauseGame() {
        _state = _state.copy(isInputLocked = true)
        emitEvent(GCMSEvent.GamePaused)
    }
    
    /**
     * Resume game
     */
    private suspend fun executeResumeGame() {
        _state = _state.copy(isInputLocked = false)
        emitEvent(GCMSEvent.GameResumed)
    }
    
    /**
     * End game
     */
    private suspend fun executeEndGame() {
        _state = _state.copy(currentPhase = GamePhase.GAME_OVER)
        emitEvent(GCMSEvent.GameEnded)
    }
    
    /**
     * Reset game to initial state
     */
    private suspend fun executeResetGame() {
        _state = GCMSState()
        stateHistory.clear()
        emitEvent(GCMSEvent.GameReset)
    }
    
    /**
     * Save game
     */
    private suspend fun executeSaveGame(command: GCMSCommand.SaveGame) {
        // TODO: Implement actual save to storage
        // For now, just emit event
        emitEvent(GCMSEvent.GameSaved(saveId = command.saveId))
    }
    
    /**
     * Load game
     */
    private suspend fun executeLoadGame(command: GCMSCommand.LoadGame) {
        // TODO: Implement actual load from storage
        // For now, just emit event
        emitEvent(GCMSEvent.GameLoaded(saveId = command.saveId))
    }
    
    /**
     * Undo last move
     */
    private suspend fun executeUndoMove() {
        if (stateHistory.isNotEmpty()) {
            _state = stateHistory.removeAt(stateHistory.size - 1)
            emitEvent(GCMSEvent.MoveUndone)
            emitEvent(GCMSEvent.StateChanged(stateSnapshot = _state))
        }
    }
    
    /**
     * Request AI to make a move
     */
    private suspend fun executeRequestAIMove(command: GCMSCommand.RequestAIMove) {
        // Emit event for AI controller to handle
        emitEvent(GCMSEvent.AITurnStarted(playerId = command.playerId))
    }
    
    /**
     * Update game settings
     */
    private suspend fun executeUpdateSettings(command: GCMSCommand.UpdateSettings) {
        // TODO: Implement settings storage
        emitEvent(GCMSEvent.SettingsUpdated(settings = command.settings))
    }
    
    /**
     * Unlock a skill or ability node
     */
    private suspend fun executeUnlockNode(command: GCMSCommand.UnlockNode) {
        val pointType = when (command.pointType) {
            "SKILL" -> PointType.SKILL
            "ABILITY" -> PointType.ABILITY
            else -> {
                emitEvent(GCMSEvent.InvalidMove(
                    reason = "Invalid point type: ${command.pointType}",
                    attemptedAction = "UnlockNode"
                ))
                return
            }
        }
        
        val result = unlockNode(_state, command.playerId, command.nodeId, pointType)
        
        if (result.success) {
            // Emit success event
            emitEvent(NodeUnlockedEvent(
                playerId = command.playerId,
                nodeId = result.nodeId!!,
                nodeName = result.nodeName!!,
                pointType = command.pointType,
                pointsSpent = result.pointsSpent!!
            ))
            
            // Update player's total points display
            val progress = _state.skillAbilitySystem.getPlayerProgress(command.playerId)
            emitEvent(PointsEarnedEvent(
                playerId = command.playerId,
                spEarned = 0,
                apEarned = 0,
                totalSP = progress.totalSP,
                totalAP = progress.totalAP
            ))
        } else {
            // Emit failure event
            emitEvent(GCMSEvent.InvalidMove(
                reason = result.message,
                attemptedAction = "UnlockNode: ${command.nodeId}"
            ))
        }
    }
    
    /**
     * Process match completion with XP and leveling
     * This should be called when a match ends
     */
    private suspend fun executeMatchCompletion(winnerId: String, matchHistory: List<List<CardState>>) {
        val result = processMatchCompletion(_state, winnerId, matchHistory)
        val progress = _state.skillAbilitySystem.getPlayerProgress(winnerId)
        
        // Emit match completed event
        emitEvent(MatchCompletedEvent(
            matchNumber = result.matchNumber,
            winnerId = winnerId,
            spEarned = result.spEarned,
            apEarned = result.apEarned,
            penalties = result.penalties.map { "${it.cardRank} in slot ${it.slotNumber}: ${it.penalty}" }
        ))
        
        // Emit points earned event
        emitEvent(PointsEarnedEvent(
            playerId = winnerId,
            spEarned = result.spEarned,
            apEarned = result.apEarned,
            totalSP = progress.totalSP,
            totalAP = progress.totalAP
        ))
        
        // Check if player leveled up
        val oldLevel = progress.level - 1 // Subtract 1 because addMatchResult already updated it
        if (progress.level > oldLevel) {
            val xpToNext = LevelSystem.getXPToNextLevel(progress.totalXP, progress.level)
            emitEvent(LevelUpEvent(
                playerId = winnerId,
                newLevel = progress.level,
                totalXP = progress.totalXP,
                xpToNextLevel = xpToNext
            ))
        }
    }
    
    /**
     * Use an ability
     */
    private suspend fun executeUseAbility(command: GCMSCommand.UseAbility) {
        // Convert string map to Any map for internal use
        val targetData = command.targetData?.mapValues { it.value as Any }
        
        val result = useAbility(_state, command.playerId, command.abilityId, targetData)
        
        if (result.success) {
            // Emit success event
            emitEvent(AbilityUsedEvent(
                playerId = command.playerId,
                abilityId = result.abilityId!!,
                abilityName = result.abilityName!!,
                effectDescription = result.message
            ))
            
            // Play sound effect
            emitEvent(GCMSEvent.PlaySound(
                soundId = "ability_used",
                volume = 1.0f
            ))
        } else {
            // Emit failure event
            emitEvent(GCMSEvent.InvalidMove(
                reason = result.message,
                attemptedAction = "UseAbility: ${command.abilityId}"
            ))
        }
    }
    
    // ========================================================================
    // HELPER METHODS
    // ========================================================================
    
    /**
     * Save current state to history
     */
    private fun saveStateToHistory() {
        stateHistory.add(_state)
        
        // Limit history size
        if (stateHistory.size > maxHistorySize) {
            stateHistory.removeAt(0)
        }
    }
    
    /**
     * Emit an event to all subscribers
     */
    private suspend fun emitEvent(event: GCMSEvent) {
        _events.emit(event)
    }
    
    /**
     * Clean up resources
     */
    fun destroy() {
        scope.cancel()
    }
}