package com.trashpiles.gcms

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * GCMS Events - What flows through the brake relay
 * These are READ-ONLY notifications that subsystems listen to
 * Subsystems react to events but CANNOT modify them
 */
@Serializable
sealed class GCMSEvent {
    abstract val timestamp: Long // Unix timestamp in milliseconds
    abstract val eventId: String
    
    companion object {
        fun generateId(): String = "${System.currentTimeMillis()}_${UUID.randomUUID()}"
    }
}

// ============================================================================
// GAME LIFECYCLE EVENTS
// ============================================================================

@Serializable
data class GameInitializedEvent(
    val playerCount: Int,
    val playerNames: List<String>,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class GameStartedEvent(
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class GamePausedEvent(
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class GameResumedEvent(
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class GameOverEvent(
    val winnerId: Int,
    val winnerName: String,
    val finalScores: Map<Int, Int>,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

// ============================================================================
// TURN EVENTS
// ============================================================================

@Serializable
data class TurnStartedEvent(
    val playerId: Int,
    val playerName: String,
    val turnNumber: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class TurnEndedEvent(
    val playerId: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

// ============================================================================
// CARD EVENTS
// ============================================================================

@Serializable
data class CardDealtEvent(
    val cardId: String,
    val toPlayerId: Int,
    val slotIndex: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class CardDrawnEvent(
    val cardId: String,
    val fromPile: String, // 'deck' or 'discard'
    val byPlayerId: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class CardPlacedEvent(
    val cardId: String,
    val playerId: Int,
    val slotIndex: Int,
    val replacedCardId: String? = null,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class CardDiscardedEvent(
    val cardId: String,
    val byPlayerId: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class CardFlippedEvent(
    val cardId: String,
    val isFaceUp: Boolean,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

// ============================================================================
// UI EVENTS
// ============================================================================

@Serializable
data class InputLockedEvent(
    val reason: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class InputUnlockedEvent(
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

// ============================================================================
// AUDIO EVENTS
// ============================================================================

@Serializable
data class PlaySoundEvent(
    val soundId: String,
    val volume: Float = 1.0f,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

// ============================================================================
// ANIMATION EVENTS
// ============================================================================

@Serializable
data class AnimateCardEvent(
    val cardId: String,
    val animationType: String, // 'flip', 'slide', 'deal'
    val animationData: Map<String, String>, // Simplified for serialization
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

// ============================================================================
// VALIDATION EVENTS
// ============================================================================

@Serializable
data class InvalidMoveEvent(
    val reason: String,
    val attemptedAction: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

// ============================================================================
// STATE SYNC EVENTS
// ============================================================================

@Serializable
data class StateChangedEvent(
    val stateSnapshot: String, // JSON string of state
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

// ============================================================================
// SKILL &amp; ABILITY SYSTEM EVENTS
// ============================================================================

@Serializable
data class MatchCompletedEvent(
    val matchNumber: Int,
    val winnerId: String,
    val spEarned: Int,
    val apEarned: Int,
    val penalties: List<String>, // Serialized penalty descriptions
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class NodeUnlockedEvent(
    val playerId: String,
    val nodeId: String,
    val nodeName: String,
    val pointType: String, // "SKILL" or "ABILITY"
    val pointsSpent: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class AbilityUsedEvent(
    val playerId: String,
    val abilityId: String,
    val abilityName: String,
    val effectDescription: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class PointsEarnedEvent(
    val playerId: String,
    val spEarned: Int,
    val apEarned: Int,
    val totalSP: Int,
    val totalAP: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class LevelUpEvent(
    val playerId: String,
    val newLevel: Int,
    val totalXP: Int,
    val xpToNextLevel: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

// ============================================================================
// CHALLENGE EVENTS
// ============================================================================

@Serializable
data class ChallengeAssignedEvent(
    val level: Int,
    val challengeIds: List<String>,
    val requiredToComplete: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class ChallengeProgressUpdatedEvent(
    val challengeId: String,
    val challengeName: String,
    val progress: Map<String, Any>,
    val isComplete: Boolean,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class ChallengeCompletedEvent(
    val challengeId: String,
    val challengeName: String,
    val achievement: String,
    val pointsBonus: Int,
    val xpBonus: Int,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class AllChallengesCompletedEvent(
    val level: Int,
    val completedChallenges: List<String>,
    val newAchievements: List<String>,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()

@Serializable
data class LevelUnlockedEvent(
    val unlockedLevel: Int,
    val completedChallenges: List<String>,
    val newAchievements: List<String>,
    override val timestamp: Long = System.currentTimeMillis(),
    override val eventId: String = generateId()
) : GCMSEvent()
