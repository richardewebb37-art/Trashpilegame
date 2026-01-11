package com.trashpiles.gcms.handlers

import com.trashpiles.gcms.*

/**
 * Handler for turn-related commands
 * 
 * Handles:
 * - EndTurnCommand
 * - SkipTurnCommand
 */
class TurnCommandHandler : CommandHandler {
    
    override suspend fun handle(command: GCMSCommand, currentState: GCMSState): CommandResult {
        return when (command) {
            is GCMSCommand.EndTurnCommand -> handleEndTurn(command, currentState)
            is GCMSCommand.SkipTurnCommand -> handleSkipTurn(command, currentState)
            else -> throw IllegalArgumentException(
                "TurnCommandHandler cannot handle ${command::class.simpleName}")
        }
    }
    
    private fun handleEndTurn(command: GCMSCommand.EndTurnCommand, state: GCMSState): CommandResult {
        val currentPlayer = state.players[state.currentPlayerIndex]
        
        // Move to next player
        val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
        val nextPlayer = state.players[nextPlayerIndex]
        
        val updatedState = state.copy(
            currentPlayerIndex = nextPlayerIndex
        )
        
        val events = listOf(
            GCMSEvent.TurnEndedEvent(
                playerId = command.playerId
            ),
            GCMSEvent.TurnStartedEvent(
                playerId = nextPlayer.id,
                playerName = nextPlayer.name,
                turnNumber = state.currentRound * state.players.size + nextPlayerIndex
            )
        )
        
        return CommandResult(updatedState, events)
    }
    
    private fun handleSkipTurn(command: GCMSCommand.SkipTurnCommand, state: GCMSState): CommandResult {
        val currentPlayer = state.players[state.currentPlayerIndex]
        
        // Move to next player
        val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
        val nextPlayer = state.players[nextPlayerIndex]
        
        val updatedState = state.copy(
            currentPlayerIndex = nextPlayerIndex
        )
        
        val events = listOf(
            GCMSEvent.TurnStartedEvent(
                playerId = nextPlayer.id,
                playerName = nextPlayer.name,
                turnNumber = state.currentRound * state.players.size + nextPlayerIndex
            )
        )
        
        return CommandResult(updatedState, events)
    }
}