package com.trashpiles.data.models

/**
 * Data model representing a playing card
 */
data class Card(
    val id: Int,
    val suit: Suit,
    val rank: Rank,
    val isFaceUp: Boolean = false,
    val position: Position = Position.DECK
)

enum class Suit {
    SPADES,
    HEARTS,
    DIAMONDS,
    CLUBS
}

enum class Rank(val value: Int) {
    ACE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(11),
    QUEEN(12),
    KING(13)
}

enum class Position {
    DECK,
    DISCARD,
    PLAYER_HAND,
    PLAYER_PILE
}