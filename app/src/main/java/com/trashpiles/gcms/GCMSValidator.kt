package com.trashpiles.game.gcms

/**
 * GCMS Validator - Separates validation logic from controller
 * 
 * This keeps the controller clean and makes validation testable.
 * All command validation happens here before execution.
 */
object GCMSValidator {
    
    /**
     * Validate any command against current state
     */
    fun validate(command: GCMSCommand, state: GCMSState): ValidationResult {
        // Input lock check (most commands blocked when locked)
        if (state.isInputLocked && !isAllowedWhenLocked(command)) {
            return ValidationResult.Invalid("Input is locked")
        }
        
        // Game phase checks
        if (!isValidForPhase(command, state.currentPhase)) {
            return ValidationResult.Invalid(
                "Cannot perform this action in ${state.currentPhase.name} phase"
            )
        }
        
        // Command-specific validation
        return when (command) {
            is GCMSCommand.InitializeGameCommand -> validateInitializeGame(command, state)
            is GCMSCommand.StartGameCommand -> validateStartGame(command, state)
            is GCMSCommand.DrawCardCommand -> validateDrawCard(command, state)
            is GCMSCommand.PlaceCardCommand -> validatePlaceCard(command, state)
            is GCMSCommand.DiscardCardCommand -> validateDiscardCard(command, state)
            is GCMSCommand.FlipCardCommand -> validateFlipCard(command, state)
            is GCMSCommand.EndTurnCommand -> validateEndTurn(command, state)
            is GCMSCommand.SkipTurnCommand -> validateSkipTurn(command, state)
            is GCMSCommand.SaveGameCommand -> validateSaveGame(command, state)
            is GCMSCommand.LoadGameCommand -> validateLoadGame(command, state)
            is GCMSCommand.ViewChallengesCommand -> validateViewChallenges(command, state)
            is GCMSCommand.CheckLevelUpCommand -> validateCheckLevelUp(command, state)
            is GCMSCommand.ClaimChallengeRewardsCommand -> validateClaimRewards(command, state)
            // Default: allow unlisted commands
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Check if command is allowed when input is locked
     */
    private fun isAllowedWhenLocked(command: GCMSCommand): Boolean {
        return command is GCMSCommand.SaveGame ||
               command is GCMSCommand.PauseGame ||
               command is GCMSCommand.EndGame
    }
    
    /**
     * Check if command is valid for current game phase
     */
    private fun isValidForPhase(command: GCMSCommand, phase: GamePhase): Boolean {
        return when (phase) {
            GamePhase.SETUP -> {
                command is GCMSCommand.InitializeGame ||
                command is GCMSCommand.StartGame ||
                command is GCMSCommand.LoadGame
            }
            
            GamePhase.DEALING -> {
                false // No player actions during dealing
            }
            
            GamePhase.PLAYING -> {
                command is GCMSCommand.DrawCard ||
                command is GCMSCommand.PlaceCard ||
                command is GCMSCommand.DiscardCard ||
                command is GCMSCommand.FlipCard ||
                command is GCMSCommand.EndTurn ||
                command is GCMSCommand.PauseGame ||
                command is GCMSCommand.SaveGame
            }
            
            GamePhase.ROUND_END -> {
                command is GCMSCommand.StartGame || // Start next round
                command is GCMSCommand.EndGame
            }
            
            GamePhase.GAME_OVER -> {
                command is GCMSCommand.ResetGame ||
                command is GCMSCommand.LoadGame
            }
        }
    }
    
    /**
     * Validate InitializeGameCommand
     */
    private fun validateInitializeGame(
        cmd: GCMSCommand.InitializeGame,
        state: GCMSState
    ): ValidationResult {
        if (cmd.playerCount < 2) {
            return ValidationResult.Invalid("Need at least 2 players")
        }
        if (cmd.playerCount > 6) {
            return ValidationResult.Invalid("Maximum 6 players")
        }
        if (cmd.playerNames.size != cmd.playerCount) {
            return ValidationResult.Invalid("Player names count mismatch")
        }
        if (cmd.isAI.size != cmd.playerCount) {
            return ValidationResult.Invalid("AI flags count mismatch")
        }
        return ValidationResult.Valid
    }
    
    /**
     * Validate StartGameCommand
     */
    private fun validateStartGame(
        cmd: GCMSCommand.StartGame,
        state: GCMSState
    ): ValidationResult {
        if (state.players.isEmpty()) {
            return ValidationResult.Invalid("No players initialized")
        }
        if (state.currentPhase != GamePhase.SETUP &&
            state.currentPhase != GamePhase.ROUND_END) {
            return ValidationResult.Invalid("Game already in progress")
        }
        return ValidationResult.Valid
    }
    
    /**
     * Validate DrawCardCommand
     */
    private fun validateDrawCard(
        cmd: GCMSCommand.DrawCard,
        state: GCMSState
    ): ValidationResult {
        // Check if it's this player's turn
        if (state.currentPlayerIndex != cmd.playerId) {
            return ValidationResult.Invalid("Not your turn")
        }
        
        // Check if player exists
        val player = state.players.find { it.id == cmd.playerId }
            ?: return ValidationResult.Invalid("Player not found")
        
        // Check if pile has cards
        when (cmd.fromPile) {
            "deck" -> {
                if (state.deck.isEmpty()) {
                    return ValidationResult.Invalid("Deck is empty")
                }
            }
            "discard" -> {
                if (state.discardPile.isEmpty()) {
                    return ValidationResult.Invalid("Discard pile is empty")
                }
            }
            else -> {
                return ValidationResult.Invalid("Invalid pile: ${cmd.fromPile}")
            }
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Validate PlaceCardCommand
     */
    private fun validatePlaceCard(
        cmd: GCMSCommand.PlaceCard,
        state: GCMSState
    ): ValidationResult {
        // Check if player exists
        val player = state.players.find { it.id == cmd.playerId }
            ?: return ValidationResult.Invalid("Player not found")
        
        // Check if slot index is valid
        if (cmd.slotIndex < 0 || cmd.slotIndex >= player.hand.size) {
            return ValidationResult.Invalid("Invalid slot index")
        }
        
        // Check if slot is already filled
        if (player.hand[cmd.slotIndex].isFaceUp) {
            return ValidationResult.Invalid("Slot already filled")
        }
        
        // Find the card being placed
        val card = state.deck.find { it.id == cmd.cardId }
            ?: return ValidationResult.Invalid("Card not found")
        
        // Use game rules to validate placement
        val moveValidation = GameRules.validateMove(
            state = state,
            playerId = cmd.playerId,
            card = card,
            targetSlot = cmd.slotIndex
        )
        
        if (!moveValidation.isValid) {
            return ValidationResult.Invalid(moveValidation.reason)
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Validate DiscardCardCommand
     */
    private fun validateDiscardCard(
        cmd: GCMSCommand.DiscardCard,
        state: GCMSState
    ): ValidationResult {
        if (state.currentPlayerIndex != cmd.playerId) {
            return ValidationResult.Invalid("Not your turn")
        }
        
        // Check if card exists
        val card = state.deck.find { it.id == cmd.cardId }
            ?: return ValidationResult.Invalid("Card not found")
        
        return ValidationResult.Valid
    }
    
    /**
     * Validate FlipCardCommand
     */
    private fun validateFlipCard(
        cmd: GCMSCommand.FlipCard,
        state: GCMSState
    ): ValidationResult {
        // Check if player exists
        val player = state.players.find { it.id == cmd.playerId }
            ?: return ValidationResult.Invalid("Player not found")
        
        // Check if slot is valid
        if (cmd.slotIndex < 0 || cmd.slotIndex >= player.hand.size) {
            return ValidationResult.Invalid("Invalid slot index")
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Validate EndTurnCommand
     */
    private fun validateEndTurn(
        cmd: GCMSCommand.EndTurn,
        state: GCMSState
    ): ValidationResult {
        if (state.currentPlayerIndex != cmd.playerId) {
            return ValidationResult.Invalid("Not your turn")
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Validate SkipTurnCommand
     */
    private fun validateSkipTurn(
        cmd: GCMSCommand.SkipTurn,
        state: GCMSState
    ): ValidationResult {
        if (state.currentPlayerIndex != cmd.playerId) {
            return ValidationResult.Invalid("Cannot skip another player's turn")
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Validate SaveGameCommand
     */
    private fun validateSaveGame(
        cmd: GCMSCommand.SaveGame,
        state: GCMSState
    ): ValidationResult {
        if (state.currentPhase == GamePhase.SETUP) {
            return ValidationResult.Invalid("Cannot save before game starts")
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Validate LoadGameCommand
     */
    private fun validateLoadGame(
        cmd: GCMSCommand.LoadGameCommand,
        state: GCMSState
    ): ValidationResult {
        if (cmd.saveId.isEmpty()) {
            return ValidationResult.Invalid("Invalid save ID")
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Validate ViewChallengesCommand
     */
    private fun validateViewChallenges(
        cmd: GCMSCommand.ViewChallengesCommand,
        state: GCMSState
    ): ValidationResult {
        return ValidationResult.Valid
    }
    
    /**
     * Validate CheckLevelUpCommand
     */
    private fun validateCheckLevelUp(
        cmd: GCMSCommand.CheckLevelUpCommand,
        state: GCMSState
    ): ValidationResult {
        return ValidationResult.Valid
    }
    
    /**
     * Validate ClaimChallengeRewardsCommand
     */
    private fun validateClaimRewards(
        cmd: GCMSCommand.ClaimChallengeRewardsCommand,
        state: GCMSState
    ): ValidationResult {
        if (cmd.challengeId.isEmpty()) {
            return ValidationResult.Invalid("Invalid challenge ID")
        }
        
        return ValidationResult.Valid
    }
}

/**
 * Validation result sealed class
 */
sealed class ValidationResult {
    abstract val isValid: Boolean
    abstract val reason: String
    
    /**
     * Valid result
     */
    object Valid : ValidationResult() {
        override val isValid = true
        override val reason = ""
    }
    
    /**
     * Invalid result with reason
     */
    data class Invalid(override val reason: String) : ValidationResult() {
        override val isValid = false
    }
}