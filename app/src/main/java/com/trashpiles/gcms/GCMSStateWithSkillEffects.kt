package com.trashpiles.gcms

/**
 * Extended GCMSState with skill/ability effect tracking fields
 * These fields should be added to the main GCMSState class
 */

/**
 * Properties to add to GCMSState:
 * 
 * val protectedCards: Set<String> = emptySet()           // Cards protected from penalties
 * val bonusCards: Set<String> = emptySet()               // Cards with bonus points
 * val doubledCards: Set<String> = emptySet()             // Cards with double points
 * val skipDrawPhase: Boolean = false                     // Skip draw phase flag
 * val skipAnimations: Boolean = false                   // Skip animations flag
 * val endRoundImmediately: Boolean = false               // End round immediately flag
 * val immuneToDistraction: Boolean = false               // Immune to distraction effects
 * val immuneToOffensiveAbilities: Boolean = false        // Immune to offensive abilities
 * val immuneToPenalties: Boolean = false                 // Immune to penalties
 * val avoidPenalties: Int? = null                        // Number of penalties to avoid
 * val nextAbilityFree: Boolean = false                   // Next ability costs 0 AP
 * val currentDiceMultiplier: Int = 1                     // Current dice multiplier
 * val revealedDeckCards: List<Card> = emptyList()        // Revealed deck cards for peek
 * val currentTurnTimerBonus: Int = 0                     // Turn timer bonus from skills
 * val streakCount: Int = 0                               // Current streak count
 * val flippedCardsThisRound: Int = 0                     // Cards flipped this round
 * val opponentRevealedDiscards: List<Card> = emptyList() // Revealed opponent discards
 * val opponentRevealedSlots: Map<Int, Card> = emptyMap() // Revealed opponent slots
 */

/**
 * Example of how to integrate into GCMSState:
 * 
 * data class GCMSState(
 *     // ... existing fields ...
 *     
 *     // Skill/Ability effect tracking
 *     val protectedCards: Set<String> = emptySet(),
 *     val bonusCards: Set<String> = emptySet(),
 *     val doubledCards: Set<String> = emptySet(),
 *     val skipDrawPhase: Boolean = false,
 *     val skipAnimations: Boolean = false,
 *     val endRoundImmediately: Boolean = false,
 *     val immuneToDistraction: Boolean = false,
 *     val immuneToOffensiveAbilities: Boolean = false,
 *     val immuneToPenalties: Boolean = false,
 *     val avoidPenalties: Int? = null,
 *     val nextAbilityFree: Boolean = false,
 *     val currentDiceMultiplier: Int = 1,
 *     val revealedDeckCards: List<Card> = emptyList(),
 *     val currentTurnTimerBonus: Int = 0,
 *     val streakCount: Int = 0,
 *     val flippedCardsThisRound: Int = 0,
 *     val opponentRevealedDiscards: List<Card> = emptyList(),
 *     val opponentRevealedSlots: Map<Int, Card> = emptyMap()
 * )
 */

/**
 * Helper functions for managing skill/ability effects in GCMSState
 */

/**
 * Check if a card is protected from penalties
 */
fun GCMSState.isCardProtected(cardId: String): Boolean {
    return cardId in protectedCards
}

/**
 * Get bonus points for a card
 */
fun GCMSState.getCardBonus(cardId: String): Int {
    var bonus = 0
    
    if (cardId in bonusCards) {
        // Check for Heavy Strike ability (+5 points)
        bonus += 5
    }
    
    // Check for Card Mastery skill (+1 point for numbered cards)
    if (cardHasSkillEffect(cardId, "CARD_MASTERY")) {
        bonus += 1
    }
    
    return bonus
}

/**
 * Check if card should score double points
 */
fun GCMSState.shouldDoublePoints(cardId: String): Boolean {
    return cardId in doubledCards
}

/**
 * Get total turn time with bonuses
 */
fun GCMSState.getTotalTurnTime(): Int {
    val baseTime = 30 // Base turn time in seconds
    val skillBonuses = currentTurnTimerBonus
    val streakBonus = if (streakCount >= 3) 2 else 0 // Momentum skill
    
    return baseTime + skillBonuses + streakBonus
}

/**
 * Get dice roll with bonuses
 */
fun GCMSState.getDiceRollWithBonuses(): Int {
    val baseRoll = diceRoll
    val bonus = when {
        hasSkillEffect("CRITICAL_FOCUS") -> 1
        hasSkillEffect("TITANS_WRATH") -> 5 // Guaranteed 6
        else -> 0
    }
    
    val chainComboBonus = if (streakCount >= 3 && hasSkillEffect("CHAIN_COMBO")) 3 else 0
    
    return (baseRoll + bonus + chainComboBonus).coerceAtMost(6)
}

/**
 * Check if player has a specific skill effect
 */
fun GCMSState.hasSkillEffect(skillId: String): Boolean {
    return skillId in skillAbilitySystem.playerProgress.unlockedSkills
}

/**
 * Calculate penalty with reductions
 */
fun GCMSState.calculatePenalty(cardRank: CardRank, basePenalty: Int): Int {
    var penalty = basePenalty
    
    // Check for immunity
    if (immuneToPenalties) return 0
    
    // Check for avoid penalty
    if (avoidPenalties != null && avoidPenalties!! > 0) {
        return 0
    }
    
    // Apply penalty reductions
    when (cardRank) {
        CardRank.JACK -> {
            if (hasSkillEffect("TOUGH_SKIN")) penalty -= 1
            if (hasSkillEffect("MAGIC_RESISTANCE")) penalty -= 1
            if (hasSkillEffect("STONE_SKIN") || hasSkillEffect("UNBREAKABLE_WILL")) penalty = 0
        }
        CardRank.QUEEN -> {
            if (hasSkillEffect("ENDURANCE")) penalty -= 1
            if (hasSkillEffect("MAGIC_RESISTANCE")) penalty -= 1
            if (hasSkillEffect("STONE_SKIN") || hasSkillEffect("UNBREAKABLE_WILL")) penalty = 0
        }
        CardRank.KING -> {
            if (hasSkillEffect("IRON_WILL")) penalty -= 1
            if (hasSkillEffect("MAGIC_RESISTANCE")) penalty -= 1
            if (hasSkillEffect("STONE_SKIN") || hasSkillEffect("UNBREAKABLE_WILL")) penalty = 0
        }
        CardRank.JOKER -> {
            if (hasSkillEffect("PROTECTIVE_AURA")) penalty /= 2
            if (hasSkillEffect("MAGIC_RESISTANCE")) penalty -= 1
        }
    }
    
    // Apply Fortify ability (50% reduction)
    if (hasActiveAbilityEffect("FORTIFY")) {
        penalty = (penalty * 0.5).toInt()
    }
    
    return penalty.coerceAtLeast(0)
}

/**
 * Check if has active ability effect
 */
fun GCMSState.hasActiveAbilityEffect(abilityId: String): Boolean {
    // Check if ability is in active effects
    return skillAbilitySystem.activeEffects.any { 
        it.nodeId == abilityId 
    }
}

/**
 * Get XP bonus percentage
 */
fun GCMSState.getXPBonusPercentage(): Int {
    var bonus = 0
    
    if (hasSkillEffect("QUICK_LEARNER")) bonus += 10
    if (hasSkillEffect("ENCOURAGING_AURA")) bonus += 15
    
    return bonus
}

/**
 * Get SP bonus percentage
 */
fun GCMSState.getSPBonusPercentage(): Int {
    var bonus = 0
    
    if (hasSkillEffect("RESOURCE_HOARDER")) bonus += 5
    if (hasSkillEffect("ETERNAL_CHAMPION")) bonus += 50
    
    return bonus
}

/**
 * Get AP bonus percentage
 */
fun GCMSState.getAPBonusPercentage(): Int {
    var bonus = 0
    
    if (hasSkillEffect("RESOURCE_MANAGEMENT")) bonus += 5
    if (hasSkillEffect("SUPPORTIVE_PRESENCE")) bonus += 10
    if (hasSkillEffect("ETERNAL_CHAMPION")) bonus += 50
    
    return bonus
}

/**
 * Get draw bonus percentage
 */
fun GCMSState.getDrawBonusPercentage(): Int {
    var bonus = 0
    
    if (hasSkillEffect("DEAD_EYE")) bonus += 10
    if (hasSkillEffect("SNIPER_FOCUS")) bonus += 20
    
    return bonus
}

/**
 * Get ability cost modifier
 */
fun GCMSState.getAbilityCostModifier(abilityId: String): Int {
    var modifier = 0
    
    if (hasSkillEffect("INSPIRE_ALLIES")) modifier -= 5
    if (hasSkillEffect("ULTIMATE_MASTERY")) modifier -= 10
    
    return modifier
}

/**
 * Get extra actions available
 */
fun GCMSState.getExtraActions(): List<ActionType> {
    val actions = mutableListOf<ActionType>()
    
    if (hasSkillEffect("COMBO_MASTERY") && hasPlacedCardThisTurn()) {
        actions.add(ActionType.FLIP_CARD)
    }
    
    return actions
}

/**
 * Check if has placed card this turn
 */
fun GCMSState.hasPlacedCardThisTurn(): Boolean {
    // This would be tracked during turn processing
    return false
}

/**
 * Apply level-based XP and point bonuses
 */
fun GCMSState.applyLevelBonuses(matchRewards: MatchRewards): MatchRewards {
    val level = skillAbilitySystem.playerProgress.level
    
    // Apply level multiplier
    val levelMultiplier = 1.0 + (level * 0.05)
    
    return matchRewards.copy(
        xp = (matchRewards.xp * levelMultiplier).toInt(),
        skillPoints = (matchRewards.skillPoints * levelMultiplier).toInt(),
        abilityPoints = (matchRewards.abilityPoints * levelMultiplier).toInt()
    )
}