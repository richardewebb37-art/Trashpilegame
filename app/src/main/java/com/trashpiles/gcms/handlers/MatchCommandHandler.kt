package com.trashpiles.gcms.handlers

import com.trashpiles.gcms.*

/**
 * Handler for match-level commands
 * 
 * Handles:
 * - InitializeGameCommand
 * - StartGameCommand
 * - EndGameCommand
 * - ResetGameCommand
 * - SaveGameCommand
 * - LoadGameCommand
 * - RequestAIActionCommand
 */
class MatchCommandHandler : CommandHandler {
    
    override suspend fun handle(command: GCMSCommand, currentState: GCMSState): CommandResult {
        return when (command) {
            is GCMSCommand.InitializeGameCommand -> handleInitializeGame(command, currentState)
            is GCMSCommand.StartGameCommand -> handleStartGame(command, currentState)
            is GCMSCommand.EndGameCommand -> handleEndGame(command, currentState)
            is GCMSCommand.ResetGameCommand -> handleResetGame(command, currentState)
            is GCMSCommand.SaveGameCommand -> handleSaveGame(command, currentState)
            is GCMSCommand.LoadGameCommand -> handleLoadGame(command, currentState)
            is GCMSCommand.RequestAIActionCommand -> handleRequestAIAction(command, currentState)
            else -> throw IllegalArgumentException(
                "MatchCommandHandler cannot handle ${command::class.simpleName}")
        }
    }
    
    private fun handleInitializeGame(command: GCMSCommand.InitializeGameCommand, state: GCMSState): CommandResult {
        // Create players
        val players = command.playerNames.mapIndexed { index, name ->
            PlayerState(
                id = index,
                name = name,
                hand = emptyList(),
                score = 0,
                isAI = command.isAI[index],
                hasFinished = false
            )
        }
        
        // Create deck
        val deck = DeckBuilder.createDeck()
        val shuffledDeck = DeckBuilder.shuffle(deck)
        
        val updatedState = state.copy(
            currentPhase = GamePhase.SETUP,
            players = players,
            currentPlayerIndex = 0,
            deck = shuffledDeck,
            discardPile = emptyList()
        )
        
        val events = listOf(
            GCMSEvent.GameInitializedEvent(
                playerCount = players.size,
                playerNames = command.playerNames
            )
        )
        
        return CommandResult(updatedState, events)
    }
    
    private fun handleStartGame(command: GCMSCommand.StartGameCommand, state: GCMSState): CommandResult {
        // Deal cards to all players
        val cardsPerPlayer = 10
        var remainingDeck = state.deck
        
        val updatedPlayers = state.players.map { player ->
            val hand = remainingDeck.take(cardsPerPlayer).map { card ->
                card.copy(faceUp = false)
            }
            remainingDeck = remainingDeck.drop(cardsPerPlayer)
            player.copy(hand = hand)
        }
        
        val updatedState = state.copy(
            currentPhase = GamePhase.PLAYING,
            players = updatedPlayers,
            deck = remainingDeck
        )
        
        val events = listOf(
            GCMSEvent.GameStartedEvent(),
            // Emit CardDealtEvent for each card
            *updatedPlayers.flatMap { player ->
                player.hand.mapIndexed { slotIndex, card ->
                    GCMSEvent.CardDealtEvent(
                        cardId = card.id,
                        toPlayerId = player.id,
                        slotIndex = slotIndex
                    )
                }
            }.toTypedArray()
        )
        
        return CommandResult(updatedState, events)
    }
    
    private fun handleEndGame(command: GCMSCommand.EndGameCommand, state: GCMSState): CommandResult {
        val winner = state.players.maxByOrNull { it.score }
        
        val updatedState = state.copy(
            currentPhase = GamePhase.GAME_OVER
        )
        
        val events = mutableListOf<GCMSEvent>(
            GCMSEvent.GameOverEvent(
                winnerId = winner?.id ?: -1,
                winnerName = winner?.name ?: "Unknown",
                finalScores = state.players.associate { it.id to it.score }
            )
        )
        
        // If match completed, process skill/ability rewards
        if (winner != null && command.reason == "completed") {
            val progress = state.skillAbilitySystem.getPlayerProgress(winner.id.toString())
            
            // Calculate rewards based on face-up cards
            val faceUpCards = winner.hand.filter { it.faceUp }
            val baseReward = faceUpCards.size
            
            events.add(GCMSEvent.MatchCompletedEvent(
                matchNumber = state.currentRound,
                winnerId = winner.id.toString(),
                spEarned = baseReward,
                apEarned = 0, // Will be calculated with penalties
                penalties = emptyList()
            ))
            
            // Emit points earned event
            events.add(GCMSEvent.PointsEarnedEvent(
                playerId = winner.id.toString(),
                spEarned = baseReward,
                apEarned = 0,
                totalSP = progress.totalSP,
                totalAP = progress.totalAP
            ))
            
            // Add match result for XP
            progress.addMatchResult(
                MatchResult(
                    matchNumber = state.currentRound,
                    winnerId = winner.id.toString(),
                    spEarned = baseReward,
                    apEarned = 0,
                    penalties = emptyList()
                )
            )
            
            // Check if player leveled up
            if (progress.level > 1) {
                val xpToNext = SkillAbilityLogic.LevelSystem.getXPForLevel(progress.level + 1)
                events.add(GCMSEvent.LevelUpEvent(
                    playerId = winner.id.toString(),
                    newLevel = progress.level,
                    totalXP = progress.totalXP,
                    xpToNextLevel = xpToNext - progress.totalXP
                ))
            }
        }
        
        return CommandResult(updatedState, events)
    }
    
    private fun handleResetGame(command: GCMSCommand.ResetGameCommand, state: GCMSState): CommandResult {
        // Reset to initial state
        val resetPlayers = state.players.map { player ->
            player.copy(
                hand = emptyList(),
                score = 0,
                hasFinished = false
            )
        }
        
        val deck = DeckBuilder.createDeck()
        val shuffledDeck = DeckBuilder.shuffle(deck)
        
        val updatedState = state.copy(
            currentPhase = GamePhase.SETUP,
            players = resetPlayers,
            currentPlayerIndex = 0,
            deck = shuffledDeck,
            discardPile = emptyList()
        )
        
        val events = emptyList<GCMSEvent>()
        
        return CommandResult(updatedState, events)
    }
    
    private fun handleSaveGame(command: GCMSCommand.SaveGameCommand, state: GCMSState): CommandResult {
        // TODO: Implement save game logic
        // For now, just emit event
        val events = emptyList<GCMSEvent>()
        
        return CommandResult(state, events)
    }
    
    private fun handleLoadGame(command: GCMSCommand.LoadGameCommand, state: GCMSState): CommandResult {
        // TODO: Implement load game logic
        // For now, just emit event
        val events = emptyList<GCMSEvent>()
        
        return CommandResult(state, events)
    }
    
    private fun handleRequestAIAction(command: GCMSCommand.RequestAIActionCommand, state: GCMSState): CommandResult {
        // TODO: Implement AI move logic
        // For now, just emit event
        val events = emptyList<GCMSEvent>()
        
        return CommandResult(state, events)
    }
}