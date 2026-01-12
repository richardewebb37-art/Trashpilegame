package com.trashpiles.gcms

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * GCMS Commands - What subsystems can REQUEST
 * Commands are validated and processed by GCMS Controller
 * If valid, they trigger events. If invalid, they're rejected.
 */
@Serializable
sealed class GCMSCommand {
    abstract val timestamp: Long // Unix timestamp in milliseconds
    abstract val commandId: String
    
    companion object {
        fun generateId(): String = "${System.currentTimeMillis()}_${UUID.randomUUID()}"
    }
}

// ============================================================================
// GAME CONTROL COMMANDS
// ============================================================================

@Serializable
data class InitializeGameCommand(
    val playerCount: Int,
    val playerNames: List<String>,
    val isAI: List<Boolean>, // Which players are AI
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class StartGameCommand(
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class PauseGameCommand(
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class ResumeGameCommand(
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class EndGameCommand(
    val reason: String, // 'completed', 'forfeited', 'error'
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

// ============================================================================
// PLAYER ACTION COMMANDS
// ============================================================================

@Serializable
data class DrawCardCommand(
    val playerId: Int,
    val fromPile: String, // 'deck' or 'discard'
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class PlaceCardCommand(
    val playerId: Int,
    val cardId: String,
    val slotIndex: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class DiscardCardCommand(
    val playerId: Int,
    val cardId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class FlipCardCommand(
    val playerId: Int,
    val slotIndex: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

// ============================================================================
// TURN CONTROL COMMANDS
// ============================================================================

@Serializable
data class EndTurnCommand(
    val playerId: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class SkipTurnCommand(
    val playerId: Int,
    val reason: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

// ============================================================================
// AI COMMANDS
// ============================================================================

@Serializable
data class RequestAIActionCommand(
    val aiPlayerId: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

// ============================================================================
// SAVE/LOAD COMMANDS
// ============================================================================

@Serializable
data class SaveGameCommand(
    val saveName: String? = null,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class LoadGameCommand(
    val saveId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

// ============================================================================
// NETWORK COMMANDS
// ============================================================================

@Serializable
data class SyncStateCommand(
    val remoteState: String, // JSON string of remote state
    val fromPlayerId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

// ============================================================================
// DEBUGGING COMMANDS
// ============================================================================

@Serializable
data class DumpStateCommand(
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class ResetGameCommand(
    val keepPlayers: Boolean = false,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

// ============================================================================
// SKILL &amp; ABILITY SYSTEM COMMANDS
// ============================================================================

@Serializable
data class UnlockNodeCommand(
    val playerId: String,
    val nodeId: String,
    val pointType: String, // "SKILL" or "ABILITY"
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class UseAbilityCommand(
    val playerId: String,
    val abilityId: String,
    val targetData: Map<String, String>? = null, // Serializable map
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

// ============================================================================
// CHALLENGE SYSTEM COMMANDS
// ============================================================================

@Serializable
data class ViewChallengesCommand(
    val playerId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class CheckLevelUpCommand(
    val playerId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()

@Serializable
data class ClaimChallengeRewardsCommand(
    val playerId: String,
    val challengeId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val commandId: String = generateId()
) : GCMSCommand()