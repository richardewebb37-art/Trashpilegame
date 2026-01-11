package com.trashpiles.gcms

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * GCMS State - Authoritative Game State
 * This is the single source of truth for all game state
 * NO subsystem should modify this directly - only GCMS Controller
 */
@Serializable
data class GCMSState(
    // Game phase
    val currentPhase: GamePhase = GamePhase.SETUP,
    
    // Players
    val players: List<PlayerState> = emptyList(),
    val currentPlayerIndex: Int = 0,
    
    // Deck and piles
    val deck: List<CardState> = emptyList(),
    val discardPile: List<CardState> = emptyList(),
    
    // Game rules
    val maxPlayers: Int = 4,
    val cardsPerPlayer: Int = 10,
    
    // Turn state
    val isInputLocked: Boolean = false,
    val lastAction: String? = null,
    val lastActionTime: Long? = null, // Unix timestamp in milliseconds
    
    // Round tracking
    val currentRound: Int = 1,
    val winnerId: Int? = null,
    
    // Skill &amp; Ability System (transient - not serialized by default)
    @kotlinx.serialization.Transient
    val skillAbilitySystem: SkillAbilitySystemState = SkillAbilitySystemState()
) {
    
    /**
     * Get current player
     */
    val currentPlayer: PlayerState?
        get() = players.getOrNull(currentPlayerIndex)
    
    /**
     * Check if game is over
     */
    val isGameOver: Boolean
        get() = currentPhase == GamePhase.GAME_OVER
    
    /**
     * Create a deep copy of the state (for history/undo)
     */
    fun copyWith(
        currentPhase: GamePhase? = null,
        players: List<PlayerState>? = null,
        currentPlayerIndex: Int? = null,
        deck: List<CardState>? = null,
        discardPile: List<CardState>? = null,
        maxPlayers: Int? = null,
        cardsPerPlayer: Int? = null,
        isInputLocked: Boolean? = null,
        lastAction: String? = null,
        lastActionTime: Long? = null,
        currentRound: Int? = null,
        winnerId: Int? = null,
        skillAbilitySystem: SkillAbilitySystemState? = null
    ): GCMSState {
        return GCMSState(
            currentPhase = currentPhase ?: this.currentPhase,
            players = players ?: this.players.map { it.copy() },
            currentPlayerIndex = currentPlayerIndex ?: this.currentPlayerIndex,
            deck = deck ?: this.deck.map { it.copy() },
            discardPile = discardPile ?: this.discardPile.map { it.copy() },
            maxPlayers = maxPlayers ?: this.maxPlayers,
            cardsPerPlayer = cardsPerPlayer ?: this.cardsPerPlayer,
            isInputLocked = isInputLocked ?: this.isInputLocked,
            lastAction = lastAction ?: this.lastAction,
            lastActionTime = lastActionTime ?: this.lastActionTime,
            currentRound = currentRound ?: this.currentRound,
            winnerId = winnerId ?: this.winnerId,
            skillAbilitySystem = skillAbilitySystem ?: this.skillAbilitySystem
        )
    }
}

/**
 * Game phases
 */
enum class GamePhase {
    SETUP,      // Setting up game
    DEALING,    // Dealing cards
    PLAYING,    // Active gameplay
    ROUND_END,  // Round finished
    GAME_OVER   // Game complete
}

/**
 * Player state
 */
@Serializable
data class PlayerState(
    val id: Int,
    val name: String,
    val hand: List<CardState> = emptyList(),
    val score: Int = 0,
    val isAI: Boolean = false,
    val hasFinished: Boolean = false
) {
    
    /**
     * Create a deep copy
     */
    fun copy(): PlayerState {
        return PlayerState(
            id = id,
            name = name,
            hand = hand.map { it.copy() },
            score = score,
            isAI = isAI,
            hasFinished = hasFinished
        )
    }
}

/**
 * Card state
 */
@Serializable
data class CardState(
    val rank: String,      // ace, two, three, ..., king
    val suit: String,      // spades, hearts, clubs, diamonds
    val value: Int,        // Numeric value for game logic
    val isFaceUp: Boolean = false,
    val position: String? = null  // deck, discard, player_0_slot_3, etc.
) {
    
    /**
     * Unique card identifier
     */
    val id: String
        get() = "${rank}_of_$suit"
    
    /**
     * Asset path for card image
     */
    val assetPath: String
        get() = "cards/$id.png"
    
    /**
     * Create a deep copy
     */
    fun copy(): CardState {
        return CardState(
            rank = rank,
            suit = suit,
            value = value,
            isFaceUp = isFaceUp,
            position = position
        )
    }
}