package com.trashpiles.data.models

/**
 * Data model representing the current game state
 */
data class GameState(
    val players: List<Player> = emptyList(),
    val deck: List<Card> = emptyList(),
    val discardPile: List<Card> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val round: Int = 1,
    val gameStatus: GameStatus = GameStatus.NOT_STARTED
)

enum class GameStatus {
    NOT_STARTED,
    IN_PROGRESS,
    ROUND_COMPLETE,
    GAME_OVER
}