// CRITICAL FIX: Add skill effect tracking to GCMSState.kt
// This shows exactly what needs to be added to make the Skills & Abilities system work

// ADD THESE IMPORTS to GCMSState.kt:
import com.trashpiles.gcms.SkillAbilitySystemState

// ADD THESE FIELDS to the GCMSState data class:

// Existing GCMSState.kt data class needs these additional fields:
data class GCMSState(
    // ... ALL EXISTING FIELDS REMAIN THE SAME ...
    
    // === NEW SKILL/ABILITY EFFECT TRACKING FIELDS ===
    val skillAbilitySystem: SkillAbilitySystemState = SkillAbilitySystemState(),
    
    // Card effect tracking
    val protectedCards: Set<String> = emptySet(),           // Cards protected from penalties
    val bonusCards: Set<String> = emptySet(),               // Cards with bonus points  
    val doubledCards: Set<String> = emptySet(),             // Cards with double points
    
    // Turn and round effects
    val skipDrawPhase: Boolean = false,                     // Skip draw phase this turn
    val skipAnimations: Boolean = false,                   // Skip UI animations
    val endRoundImmediately: Boolean = false,               // End round now
    
    // Immunity flags
    val immuneToDistraction: Boolean = false,               // Immune to visual effects
    val immuneToOffensiveAbilities: Boolean = false,        // Immune to opponent abilities
    val immuneToPenalties: Boolean = false,                 // Immune to all penalties
    val avoidPenalties: Int? = null,                        // Number of penalties to avoid
    
    // Ability effects
    val nextAbilityFree: Boolean = false,                   // Next ability costs 0 AP
    val currentDiceMultiplier: Int = 1,                     // Current dice multiplier
    
    // Information effects
    val revealedDeckCards: List<Card> = emptyList(),        // Revealed deck cards
    val currentTurnTimerBonus: Int = 0,                     // Bonus seconds for turn timer
    val streakCount: Int = 0,                               // Current streak count
    val flippedCardsThisRound: Int = 0,                     // Cards flipped this round
    
    // Opponent information (for multiplayer)
    val opponentRevealedDiscards: List<Card> = emptyList(), // Revealed opponent discards
    val opponentRevealedSlots: Map<Int, Card> = emptyMap()  // Revealed opponent slots
    
    // === END NEW FIELDS ===
)

// CRITICAL: These helper functions need to be added to GameRules.kt:

// ADD to GameRules.kt:
private fun applySkillBonuses(state: GCMSState, playerId: String, baseScore: Int): Int {
    var score = baseScore
    
    // Apply Quick Learner XP bonus (handled in match completion)
    // Apply Card Mastery +1 point for numbered cards
    if (state.hasSkillEffect("CARD_MASTERY")) {
        val numberedCards = getPlayerCards(state, playerId).count { it.isFaceUp && it.isNumbered }
        score += numberedCards
    }
    
    // Apply Heavy Strike ability (+5 points for specific card)
    if (state.bonusCards.isNotEmpty()) {
        score += state.bonusCards.size * 5
    }
    
    // Apply Enchantment ability (double points)
    if (state.doubledCards.isNotEmpty()) {
        score += state.doubledCards.size * getCardValue(state.doubledCards.first())
    }
    
    return score
}

private fun applySkillPenalties(state: GCMSState, playerId: String, basePenalties: Int): Int {
    var penalties = basePenalties
    
    // Apply Iron Will (King -1 AP reduction)
    if (state.hasSkillEffect("IRON_WILL")) {
        val kingCount = getPlayerCards(state, playerId).count { !it.isFaceUp && it.rank == "KING" }
        penalties -= kingCount
    }
    
    // Apply Tough Skin (Jack -1 AP reduction)  
    if (state.hasSkillEffect("TOUGH_SKIN")) {
        val jackCount = getPlayerCards(state, playerId).count { !it.isFaceUp && it.rank == "JACK" }
        penalties -= jackCount
    }
    
    // Apply Endurance (Queen -1 AP reduction)
    if (state.hasSkillEffect("ENDURANCE")) {
        val queenCount = getPlayerCards(state, playerId).count { !it.isFaceUp && it.rank == "QUEEN" }
        penalties -= queenCount
    }
    
    // Apply immunity
    if (state.immuneToPenalties) {
        penalties = 0
    }
    
    return penalties.coerceAtLeast(0)
}

private fun applyDiceBonuses(state: GCMSState, baseRoll: Int): Int {
    var roll = baseRoll
    
    // Apply Critical Focus (+1 dice bonus)
    if (state.hasSkillEffect("CRITICAL_FOCUS")) {
        roll = (roll + 1).coerceAtMost(6)
    }
    
    // Apply Chain Combo bonus (+3 if 3+ cards placed)
    if (state.hasSkillEffect("CHAIN_COMBO") && state.flippedCardsThisRound >= 3) {
        roll = (roll + 3).coerceAtMost(6)
    }
    
    return roll
}

// ADD these helper functions to GCMSState.kt or create separate extension file:
fun GCMSState.hasSkillEffect(skillId: String): Boolean {
    return skillId in skillAbilitySystem.playerProgress.unlockedSkills
}

fun GCMSState.getTotalTurnTime(): Int {
    val baseTime = 30
    val focusedMindBonus = if (hasSkillEffect("FOCUSED_MIND")) 2 else 0
    val efficiencyBonus = if (hasSkillEffect("EFFICIENCY")) 3 else 0
    val momentumBonus = if (streakCount >= 3 && hasSkillEffect("MOMENTUM")) 2 else 0
    
    return baseTime + focusedMindBonus + efficiencyBonus + momentumBonus + currentTurnTimerBonus
}

fun GCMSState.getXPBonusPercentage(): Int {
    var bonus = 0
    if (hasSkillEffect("QUICK_LEARNER")) bonus += 10
    if (hasSkillEffect("ENCOURAGING_AURA")) bonus += 15
    return bonus
}

// CRITICAL: Update calculateScore function in GameRules.kt:
fun calculateScore(state: GCMSState, playerId: String): Int {
    var score = 0
    
    // Calculate base score
    for (card in getPlayerCards(state, playerId)) {
        if (card.isFaceUp) {
            score += getCardValue(card)
        }
    }
    
    // Apply skill bonuses
    score = applySkillBonuses(state, playerId, score)
    
    // Apply dice bonus
    val diceRoll = applyDiceBonuses(state, state.diceRoll)
    score += diceRoll * state.currentDiceMultiplier
    
    return score
}

// CRITICAL: Update calculatePenalties function in GameRules.kt:
fun calculatePenalties(state: GCMSState, playerId: String): Int {
    var penalties = 0
    
    // Calculate base penalties
    for (card in getPlayerCards(state, playerId)) {
        if (!card.isFaceUp) {
            penalties += getBasePenalty(card.rank)
        }
    }
    
    // Apply skill reductions
    penalties = applySkillPenalties(state, playerId, penalties)
    
    return penalties
}

// CRITICAL: Update processMatchCompletion in SkillAbilityLogic.kt:
fun processMatchCompletion(
    state: GCMSState,
    winnerId: String,
    loserId: String,
    winnerScore: Int,
    loserScore: Int,
    winnerPenalties: Int,
    loserPenalties: Int,
    diceRoll: Int,
    diceMultiplier: Int
): GCMSState {
    // Calculate base rewards
    val baseRewards = calculateGameRewards(winnerScore, loserScore, winnerPenalties, loserPenalties, diceRoll, diceMultiplier)
    
    // Apply skill bonuses
    val xpBonus = state.getXPBonusPercentage()
    val spBonus = state.getSPBonusPercentage()
    val apBonus = state.getAPBonusPercentage()
    
    val finalRewards = baseRewards.copy(
        xp = (baseRewards.xp * (1 + xpBonus / 100.0)).toInt(),
        skillPoints = (baseRewards.skillPoints * (1 + spBonus / 100.0)).toInt(),
        abilityPoints = (baseRewards.abilityPoints * (1 + apBonus / 100.0)).toInt()
    )
    
    // Update player progress
    val updatedProgress = state.skillAbilitySystem.playerProgress.addXP(finalRewards.xp)
    
    return state.copy(
        skillAbilitySystem = state.skillAbilitySystem.copy(
            playerProgress = updatedProgress
        )
    )
}