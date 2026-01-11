package com.trashpiles.data.models

/**
 * Data model representing a player
 */
data class Player(
    val id: Int,
    val name: String,
    val score: Int = 0,
    val hand: List<Card> = emptyList(),
    val pile: List<Card> = emptyList(),
    val isCurrentTurn: Boolean = false
)