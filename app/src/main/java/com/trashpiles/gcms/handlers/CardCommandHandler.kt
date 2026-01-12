package com.trashpiles.gcms.handlers

import com.trashpiles.gcms.*

/**
 * Handler for card-related commands
 * 
 * Handles:
 * - DrawCardCommand
 * - PlaceCardCommand
 * - DiscardCardCommand
 * - FlipCardCommand
 */
class CardCommandHandler : CommandHandler {
    
    override suspend fun handle(command: GCMSCommand, currentState: GCMSState): CommandResult {
        return when (command) {
            is GCMSCommand.DrawCardCommand -> handleDrawCard(command, currentState)
            is GCMSCommand.PlaceCardCommand -> handlePlaceCard(command, currentState)
            is GCMSCommand.DiscardCardCommand -> handleDiscardCard(command, currentState)
            is GCMSCommand.FlipCardCommand -> handleFlipCard(command, currentState)
            else -> throw IllegalArgumentException(
                "CardCommandHandler cannot handle ${command::class.simpleName}")
        }
    }
    
    private fun handleDrawCard(command: GCMSCommand.DrawCardCommand, state: GCMSState): CommandResult {
        val currentPlayer = state.players[state.currentPlayerIndex]
        
        // Determine source
        val (card, updatedDeck, updatedDiscard) = when (command.fromPile) {
            "deck" -> {
                if (state.deck.isEmpty()) {
                    return CommandResult(state, listOf(
                        GCMSEvent.InvalidMoveEvent(
                            reason = "Deck is empty",
                            attemptedAction = "DrawCard"
                        )
                    ))
                }
                Triple(state.deck.first(), state.deck.drop(1), state.discardPile)
            }
            "discard" -> {
                if (state.discardPile.isEmpty()) {
                    return CommandResult(state, listOf(
                        GCMSEvent.InvalidMoveEvent(
                            reason = "Discard pile is empty",
                            attemptedAction = "DrawCard"
                        )
                    ))
                }
                Triple(state.discardPile.last(), state.deck, state.discardPile.dropLast(1))
            }
            else -> {
                return CommandResult(state, listOf(
                    GCMSEvent.InvalidMoveEvent(
                        reason = "Invalid pile: ${command.fromPile}",
                        attemptedAction = "DrawCard"
                    )
                ))
            }
        }
        
        val updatedState = state.copy(
            deck = updatedDeck,
            discardPile = updatedDiscard
        )
        
        val events = listOf(
            GCMSEvent.CardDrawnEvent(
                cardId = card.id,
                fromPile = command.fromPile,
                byPlayerId = command.playerId
            )
        )
        
        return CommandResult(updatedState, events)
    }
    
    private fun handlePlaceCard(command: GCMSCommand.PlaceCardCommand, state: GCMSState): CommandResult {
        val currentPlayer = state.players[state.currentPlayerIndex]
        
        // Validate slot index
        if (command.slotIndex < 0 || command.slotIndex >= currentPlayer.hand.size) {
            return CommandResult(state, listOf(
                GCMSEvent.InvalidMoveEvent(
                    reason = "Invalid slot index: ${command.slotIndex}",
                    attemptedAction = "PlaceCard"
                )
            ))
        }
        
        // Find the card
        val card = state.deck.find { it.id == command.cardId }
            ?: state.discardPile.find { it.id == command.cardId }
        
        if (card == null) {
            return CommandResult(state, listOf(
                GCMSEvent.InvalidMoveEvent(
                    reason = "Card not found: ${command.cardId}",
                    attemptedAction = "PlaceCard"
                )
            ))
        }
        
        // Validate card can be placed in slot
        val canPlace = GameRules.canPlaceCard(card, command.slotIndex)
        if (!canPlace) {
            return CommandResult(state, listOf(
                GCMSEvent.InvalidMoveEvent(
                    reason = "Card ${card.rank} cannot be placed in slot ${command.slotIndex + 1}",
                    attemptedAction = "PlaceCard"
                )
            ))
        }
        
        // Place the card
        val updatedHand = currentPlayer.hand.toMutableList()
        val replacedCard = updatedHand[command.slotIndex]
        updatedHand[command.slotIndex] = card.copy(faceUp = true)
        
        val updatedPlayer = currentPlayer.copy(hand = updatedHand)
        val updatedPlayers = state.players.map { 
            if (it.id == currentPlayer.id) updatedPlayer else it 
        }
        
        // Add replaced card to discard pile if it wasn't already face up
        val updatedDiscard = if (!replacedCard.faceUp) {
            state.discardPile + replacedCard.copy(faceUp = true)
        } else {
            state.discardPile
        }
        
        // Remove card from deck/discard
        val updatedDeck = state.deck.filter { it.id != command.cardId }
        
        var updatedState = state.copy(
            players = updatedPlayers,
            deck = updatedDeck,
            discardPile = updatedDiscard
        )
        
        val events = mutableListOf<GCMSEvent>(
            GCMSEvent.CardPlacedEvent(
                cardId = command.cardId,
                playerId = currentPlayer.id,
                slotIndex = command.slotIndex,
                replacedCardId = if (replacedCard.faceUp) replacedCard.id else null
            )
        )
        
        // Update challenge progress with card placement
        val cardPlacedEvent = GCMSEvent.CardPlacedEvent(
            cardId = command.cardId,
            playerId = currentPlayer.id,
            slotIndex = command.slotIndex,
            replacedCardId = if (replacedCard.faceUp) replacedCard.id else null
        )
        val (updatedChallengeData, challengeEvents) = ChallengeIntegration.handleGameEvent(
            cardPlacedEvent,
            updatedState.challengeSystem
        )
        updatedState = updatedState.copyWith(challengeSystem = updatedChallengeData)
        events.addAll(challengeEvents)
        
        // Check if player won the round
        if (GameRules.hasPlayerWon(updatedPlayer)) {
            events.add(GCMSEvent.GameOverEvent(
                winnerId = currentPlayer.id,
                winnerName = currentPlayer.name,
                finalScores = updatedPlayers.associate { it.id to it.score }
            ))
        }
        
        return CommandResult(updatedState, events)
    }
    
    private fun handleDiscardCard(command: GCMSCommand.DiscardCardCommand, state: GCMSState): CommandResult {
        val currentPlayer = state.players[state.currentPlayerIndex]
        
        // Find the card
        val card = state.deck.find { it.id == command.cardId }
            ?: state.discardPile.find { it.id == command.cardId }
        
        if (card == null) {
            return CommandResult(state, listOf(
                GCMSEvent.InvalidMoveEvent(
                    reason = "Card not found: ${command.cardId}",
                    attemptedAction = "DiscardCard"
                )
            ))
        }
        
        val updatedDiscard = state.discardPile + card.copy(faceUp = true)
        val updatedDeck = state.deck.filter { it.id != command.cardId }
        
        val updatedState = state.copy(
            deck = updatedDeck,
            discardPile = updatedDiscard
        )
        
        val events = listOf(
            GCMSEvent.CardDiscardedEvent(
                cardId = command.cardId,
                byPlayerId = command.playerId
            )
        )
        
        return CommandResult(updatedState, events)
    }
    
    private fun handleFlipCard(command: GCMSCommand.FlipCardCommand, state: GCMSState): CommandResult {
        val currentPlayer = state.players[state.currentPlayerIndex]
        
        // Validate slot index
        if (command.slotIndex < 0 || command.slotIndex >= currentPlayer.hand.size) {
            return CommandResult(state, listOf(
                GCMSEvent.InvalidMoveEvent(
                    reason = "Invalid slot index: ${command.slotIndex}",
                    attemptedAction = "FlipCard"
                )
            ))
        }
        
        // Flip the card
        val updatedHand = currentPlayer.hand.toMutableList()
        val card = updatedHand[command.slotIndex]
        updatedHand[command.slotIndex] = card.copy(faceUp = !card.faceUp)
        
        val updatedPlayer = currentPlayer.copy(hand = updatedHand)
        val updatedPlayers = state.players.map { 
            if (it.id == currentPlayer.id) updatedPlayer else it 
        }
        
        val updatedState = state.copy(players = updatedPlayers)
        
        val events = listOf(
            GCMSEvent.CardFlippedEvent(
                cardId = card.id,
                isFaceUp = updatedHand[command.slotIndex].faceUp
            )
        )
        
        return CommandResult(updatedState, events)
    }
}