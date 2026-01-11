package com.trashpiles.gcms

import kotlin.random.Random

/**
 * Deck Builder - Creates standard 52-card deck
 * Handles deck initialization, shuffling, and card creation
 */
object DeckBuilder {
    
    /**
     * Card ranks in order
     */
    val ranks = listOf(
        "ace", "two", "three", "four", "five", "six",
        "seven", "eight", "nine", "ten", "jack", "queen", "king"
    )
    
    /**
     * Card suits
     */
    val suits = listOf("spades", "hearts", "clubs", "diamonds")
    
    /**
     * Card values for game logic (Ace=1, 2-10=face, J=11, Q=12, K=13)
     */
    val values = mapOf(
        "ace" to 1,
        "two" to 2,
        "three" to 3,
        "four" to 4,
        "five" to 5,
        "six" to 6,
        "seven" to 7,
        "eight" to 8,
        "nine" to 9,
        "ten" to 10,
        "jack" to 11,
        "queen" to 12,
        "king" to 13
    )
    
    /**
     * Create a standard 52-card deck
     */
    fun createDeck(): List<CardState> {
        val deck = mutableListOf<CardState>()
        
        for (suit in suits) {
            for (rank in ranks) {
                deck.add(
                    CardState(
                        rank = rank,
                        suit = suit,
                        value = values[rank] ?: 0,
                        isFaceUp = false,
                        position = "deck"
                    )
                )
            }
        }
        
        return deck
    }
    
    /**
     * Create and shuffle a deck
     */
    fun createShuffledDeck(seed: Int? = null): List<CardState> {
        val deck = createDeck()
        return shuffleDeck(deck, seed)
    }
    
    /**
     * Shuffle a deck (Fisher-Yates algorithm)
     */
    fun shuffleDeck(deck: List<CardState>, seed: Int? = null): List<CardState> {
        val random = seed?.let { Random(it) } ?: Random.Default
        val shuffled = deck.toMutableList()
        
        for (i in shuffled.size - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = shuffled[i]
            shuffled[i] = shuffled[j]
            shuffled[j] = temp
        }
        
        return shuffled
    }
    
    /**
     * Deal cards to players
     */
    fun dealCards(
        deck: List<CardState>,
        playerCount: Int,
        cardsPerPlayer: Int
    ): DealResult {
        if (deck.size < playerCount * cardsPerPlayer) {
            throw IllegalArgumentException("Not enough cards in deck to deal")
        }
        
        val playerHands = mutableListOf<List<CardState>>()
        val remainingDeck = deck.toMutableList()
        
        // Deal cards to each player
        for (p in 0 until playerCount) {
            val hand = mutableListOf<CardState>()
            for (c in 0 until cardsPerPlayer) {
                val card = remainingDeck.removeLast().copy(
                    position = "player_${p}_slot_$c",
                    isFaceUp = false // Start face down
                )
                hand.add(card)
            }
            playerHands.add(hand)
        }
        
        // First card in discard pile (face up)
        val discardPile = mutableListOf<CardState>()
        if (remainingDeck.isNotEmpty()) {
            val firstDiscard = remainingDeck.removeLast().copy(
                isFaceUp = true,
                position = "discard"
            )
            discardPile.add(firstDiscard)
        }
        
        return DealResult(
            playerHands = playerHands,
            remainingDeck = remainingDeck,
            discardPile = discardPile
        )
    }
    
    /**
     * Get card by ID
     */
    fun findCard(deck: List<CardState>, cardId: String): CardState? {
        return deck.firstOrNull { it.id == cardId }
    }
    
    /**
     * Create a specific card (for testing)
     */
    fun createCard(rank: String, suit: String): CardState {
        require(rank in ranks) { "Invalid rank: $rank" }
        require(suit in suits) { "Invalid suit: $suit" }
        
        return CardState(
            rank = rank,
            suit = suit,
            value = values[rank] ?: 0,
            isFaceUp = false
        )
    }
    
    /**
     * Get all cards of a specific rank
     */
    fun getCardsOfRank(deck: List<CardState>, rank: String): List<CardState> {
        return deck.filter { it.rank == rank }
    }
    
    /**
     * Get all cards of a specific suit
     */
    fun getCardsOfSuit(deck: List<CardState>, suit: String): List<CardState> {
        return deck.filter { it.suit == suit }
    }
    
    /**
     * Verify deck integrity (all 52 unique cards present)
     */
    fun verifyDeck(deck: List<CardState>): Boolean {
        if (deck.size != 52) return false
        
        val cardIds = deck.map { it.id }.toSet()
        if (cardIds.size != 52) return false // Duplicates exist
        
        // Check all ranks and suits exist
        for (suit in suits) {
            for (rank in ranks) {
                val expectedId = "${rank}_of_$suit"
                if (expectedId !in cardIds) return false
            }
        }
        
        return true
    }
}

/**
 * Result of dealing cards
 */
data class DealResult(
    val playerHands: List<List<CardState>>,
    val remainingDeck: List<CardState>,
    val discardPile: List<CardState>
)