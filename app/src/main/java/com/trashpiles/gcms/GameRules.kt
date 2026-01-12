package com.trashpiles.gcms

/**
 * Game Rules - Trash Card Game Logic
 * 
 * TRASH CARD GAME RULES:
 * - Players try to arrange cards Ace(1) through 10 in order
 * - Each player has 10 slots (starts with 10 face-down cards)
 * - Draw from deck or discard pile
 * - Place drawn card in matching position (Ace=slot 0, 2=slot 1, etc.)
 * - If card doesn't fit, discard it
 * - First to flip all 10 cards in order wins the round
 * - Winner starts next round with fewer cards
 */
object GameRules {
    
    /**
     * Check if a card can be placed in a specific slot
     */
    fun canPlaceCard(card: CardState, slotIndex: Int): Boolean {
        // Trash game: Card value must match slot position
        // Ace (value 1) goes in slot 0
        // 2 (value 2) goes in slot 1
        // ...
        // 10 (value 10) goes in slot 9
        
        if (slotIndex !in 0..9) return false
        
        val expectedValue = slotIndex + 1
        return card.value == expectedValue
    }
    
    /**
     * Check if a player has won the round
     */
    fun hasPlayerWon(player: PlayerState): Boolean {
        // Player wins if all cards are face up and in correct order
        if (player.hand.size != 10) return false
        
        for (i in 0 until 10) {
            val card = player.hand[i]
            if (!card.isFaceUp) return false // Card not revealed
            if (card.value != i + 1) return false // Card in wrong position
        }
        
        return true
    }
    
    /**
     * Calculate player's score for the round with skill bonuses
     */
    fun calculateScore(player: PlayerState, gameState: GCMSState): Int {
        var score = 0
        
        // Count face-down cards (penalty points)
        for (card in player.hand) {
            if (!card.isFaceUp) {
                score += 1 // Each face-down card = 1 penalty point
            }
        }
        
        // Apply active skill effects
        var finalScore = score
        gameState.activeSkillEffects.forEach { effect ->
            if (effect.playerId == player.id) {
                when (effect.effectType) {
                    SkillEffectType.SCORE_MULTIPLIER -> {
                        finalScore = (finalScore * effect.value).toInt()
                    }
                    SkillEffectType.DOUBLE_POINTS -> {
                        finalScore *= 2
                    }
                    SkillEffectType.SHIELD -> {
                        finalScore = maxOf(0, finalScore - 1) // Reduce penalty by 1
                    }
                    else -> { /* No score effect */ }
                }
            }
        }
        
        return finalScore
    }
    
    /**
     * Determine number of cards for next round
     */
    fun getCardsForNextRound(player: PlayerState, currentRound: Int): Int {
        // Winner gets one less card in next round
        // Start with 10, reduce each time they win
        return (10 - currentRound + 1).coerceIn(1, 10)
    }
    
    /**
     * Check if drawn card is wild (Jack, Queen, King)
     */
    fun isWildCard(card: CardState): Boolean {
        // In Trash, Jacks/Queens/Kings are wild cards
        // They can be placed in any empty slot
        return card.value in 11..13
    }
    
    /**
     * Get valid slots for a wild card
     */
    fun getValidSlotsForWild(player: PlayerState): List<Int> {
        val validSlots = mutableListOf<Int>()
        
        for (i in player.hand.indices) {
            val card = player.hand[i]
            if (!card.isFaceUp) {
                validSlots.add(i)
            }
        }
        
        return validSlots
    }
    
    /**
     * Check if a move is legal
     */
    fun validateMove(
        state: GCMSState,
        playerId: Int,
        card: CardState,
        targetSlot: Int
    ): MoveValidation {
        val player = state.players.firstOrNull { it.id == playerId }
            ?: return MoveValidation.invalid("Player not found")
        
        // Check if it's player's turn
        if (state.currentPlayerIndex != playerId) {
            return MoveValidation.invalid("Not your turn")
        }
        
        // Check if slot is valid
        if (targetSlot !in player.hand.indices) {
            return MoveValidation.invalid("Invalid slot")
        }
        
        // Check if slot is already face up
        if (player.hand[targetSlot].isFaceUp) {
            return MoveValidation.invalid("Slot already filled")
        }
        
        // Check if card fits in slot (or is wild)
        if (!isWildCard(card) && !canPlaceCard(card, targetSlot)) {
            return MoveValidation.invalid("Card does not match slot")
        }
        
        return MoveValidation.valid()
    }
    
    /**
     * Get current game winner (if any)
     */
    fun determineWinner(state: GCMSState): Int? {
        for (player in state.players) {
            if (hasPlayerWon(player)) {
                return player.id
            }
        }
        return null
    }
    
    /**
     * Check if game should end
     */
    fun shouldEndGame(state: GCMSState): Boolean {
        // Game ends when a player completes all rounds
        // OR when all players except one have quit
        val activePlayers = state.players.count { !it.hasFinished }
        return activePlayers <= 1 || determineWinner(state) != null
    }
    
    /**
     * Get next player index
     */
    fun getNextPlayer(state: GCMSState): Int {
        var nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        
        // Skip players who have finished
        var attempts = 0
        while (state.players[nextIndex].hasFinished && attempts < state.players.size) {
            nextIndex = (nextIndex + 1) % state.players.size
            attempts++
        }
        
        return nextIndex
    }
    
    /**
     * Initialize round (set up cards for each player)
     */
    fun initializeRound(state: GCMSState, roundNumber: Int): GCMSState {
        val cardsThisRound = (11 - roundNumber).coerceIn(1, 10)
        
        val updatedPlayers = state.players.map { player ->
            if (!player.hasFinished) {
                // Adjust hand size for this round
                val adjustedHand = if (player.hand.size > cardsThisRound) {
                    player.hand.take(cardsThisRound)
                } else {
                    player.hand
                }
                player.copy(hand = adjustedHand)
            } else {
                player
            }
        }
        
        return state.copyWith(players = updatedPlayers)
    }
    
    /**
     * Check if deck needs reshuffling
     */
    fun needsReshuffle(state: GCMSState): Boolean {
        // Reshuffle if deck has fewer than 10 cards
        // (need at least enough for one player's turn)
        return state.deck.size < 10
    }
    
    /**
     * Reshuffle discard pile into deck
     */
    fun reshuffleDiscardIntoDeck(state: GCMSState): GCMSState {
        if (state.discardPile.size <= 1) return state // Keep top card
        
        val topCard = state.discardPile.last()
        val cardsToShuffle = state.discardPile.dropLast(1)
        
        // Move all discard cards to deck and shuffle
        val newDeck = DeckBuilder.shuffleDeck(state.deck + cardsToShuffle)
        val newDiscardPile = listOf(topCard)
        
        return state.copyWith(
            deck = newDeck,
            discardPile = newDiscardPile
        )
    }
    
    /**
     * Process match completion and award skill/ability points
     */
    fun processMatchCompletion(state: GCMSState, winnerId: Int): MatchResult {
        val winner = state.players.firstOrNull { it.id == winnerId }
            ?: throw IllegalArgumentException("Winner not found: $winnerId")
        
        // Use the skill/ability system logic
        return processMatchCompletion(state, winnerId.toString(), state.skillAbilitySystem.currentMatch)
    }
    
    /**
     * Get AI hint for next move
     */
    fun getAIHint(state: GCMSState, aiPlayerId: Int): AIHint {
        val player = state.players.firstOrNull { it.id == aiPlayerId }
            ?: return AIHint(
                action = "draw",
                source = "deck",
                targetSlot = null,
                confidence = 0.5
            )
        
        // Simple AI: Draw from discard if top card fits, otherwise draw from deck
        if (state.discardPile.isNotEmpty()) {
            val topCard = state.discardPile.last()
            
            // Check if top discard fits any empty slot
            for (i in player.hand.indices) {
                if (!player.hand[i].isFaceUp) {
                    if (isWildCard(topCard) || canPlaceCard(topCard, i)) {
                        return AIHint(
                            action = "draw",
                            source = "discard",
                            targetSlot = i,
                            confidence = 0.9
                        )
                    }
                }
            }
        }
        
        // Default: draw from deck
        return AIHint(
            action = "draw",
            source = "deck",
            targetSlot = null,
            confidence = 0.5
        )
    }
}

/**
 * Move validation result
 */
data class MoveValidation(
    val isValid: Boolean,
    val reason: String? = null
) {
    companion object {
        fun valid() = MoveValidation(isValid = true)
        fun invalid(reason: String) = MoveValidation(isValid = false, reason = reason)
    }
}

/**
 * AI hint for next move
 */
data class AIHint(
    val action: String, // 'draw', 'place', 'discard'
    val source: String, // 'deck', 'discard'
    val targetSlot: Int? = null,
    val confidence: Double // 0.0 to 1.0
)

/**
 * Apply skill effect from ability usage
 */
fun applySkillEffect(
    gameState: GCMSState,
    playerId: Int,
    skillId: String,
    effectType: SkillEffectType,
    value: Float,
    duration: Int = -1
): GCMSState {
    val newEffect = SkillEffect(
        skillId = skillId,
        playerId = playerId,
        effectType = effectType,
        value = value,
        duration = duration
    )
    
    val updatedEffects = gameState.activeSkillEffects.toMutableList()
    
    // Remove expired effects for this player and effect type
    updatedEffects.removeAll { it.playerId == playerId && it.effectType == effectType && it.remainingTurns <= 0 }
    
    // Add new effect
    updatedEffects.add(newEffect)
    
    return gameState.copyWith(activeSkillEffects = updatedEffects)
}

/**
 * Update skill effects at start of turn
 */
fun updateSkillEffects(gameState: GCMSState): GCMSState {
    val updatedEffects = gameState.activeSkillEffects.map { effect ->
        if (effect.duration > 0) {
            effect.copy(remainingTurns = effect.remainingTurns - 1)
        } else {
            effect // Permanent effects don't change
        }
    }.filter { it.remainingTurns > 0 || it.duration == -1 }
    
    return gameState.copyWith(activeSkillEffects = updatedEffects)
}

/**
 * Check if player has specific skill effect active
 */
fun hasSkillEffect(
    gameState: GCMSState,
    playerId: Int,
    effectType: SkillEffectType
): Boolean {
    return gameState.activeSkillEffects.any { 
        it.playerId == playerId && it.effectType == effectType && 
        (it.remainingTurns > 0 || it.duration == -1)
    }
}

/**
 * Get skill effect value for player
 */
fun getSkillEffectValue(
    gameState: GCMSState,
    playerId: Int,
    effectType: SkillEffectType
): Float {
    return gameState.activeSkillEffects
        .filter { it.playerId == playerId && it.effectType == effectType && 
                 (it.remainingTurns > 0 || it.duration == -1) }
        .map { it.value }
        .firstOrNull() ?: 1.0f
}