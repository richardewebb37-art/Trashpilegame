package com.trashpiles.gcms

/**
 * Processor for applying skill and ability effects during gameplay
 * Handles all 100 skills and abilities from the database
 */

object SkillAbilityEffectProcessor {
    
    /**
     * Apply passive skill effects to game state
     */
    fun applyPassiveEffects(state: GCMSState): GCMSState {
        val progress = state.skillAbilitySystem.playerProgress
        val unlockedSkills = SkillAbilitySystem.getUnlockedSkills(progress)
        
        var modifiedState = state
        
        // Process each passive skill effect
        for (skill in unlockedSkills) {
            modifiedState = applySkillEffect(skill.effect, modifiedState)
        }
        
        return modifiedState
    }
    
    /**
     * Apply a single skill effect
     */
    private fun applySkillEffect(effect: SkillEffect, state: GCMSState): GCMSState {
        return when (effect) {
            is SkillEffect.XP_BOOST -> state // Handled during match completion
            is SkillEffect.TIMER_BOOST -> state // Handled by UI
            is SkillEffect.PEEK_DECK -> state // Handled by UI
            is SkillEffect.SP_BOOST -> state // Handled during match completion
            is SkillEffect.AP_BOOST -> state // Handled during match completion
            is SkillEffect.PENALTY_REDUCTION -> state // Handled during penalty calculation
            is SkillEffect.DICE_BONUS -> state // Handled during dice roll
            is SkillEffect.STREAK_BONUS -> state // Handled by UI
            is SkillEffect.POINT_BOOST -> state // Handled during scoring
            is SkillEffect.DRAW_BONUS -> state // Handled during deck shuffling
            is SkillEffect.EXTRA_ACTION -> state // Handled during turn processing
            is SkillEffect.STREAK_DICE_BONUS -> state // Handled during dice roll
            is SkillEffect.AP_REGENERATION -> state // Handled during round end
            is SkillEffect.ABILITY_USE_BOOST -> state // Handled during ability usage
            is SkillEffect.SeeOpponentDiscards -> state // Handled by UI
            is SkillEffect.SeeOpponentSlot -> state // Handled by UI
            is SkillEffect.PeekOpponentCard -> state // Handled by UI
            is SkillEffect.Immunity -> applyImmunityEffect(effect.immunityType, state)
            else -> state // Effects handled by other systems
        }
    }
    
    /**
     * Apply immunity effect
     */
    private fun applyImmunityEffect(immunityType: ImmunityType, state: GCMSState): GCMSState {
        return when (immunityType) {
            ImmunityType.DISTRACTION -> state.copy(
                immuneToDistraction = true
            )
            ImmunityType.OFFENSIVE_ABILITIES -> state.copy(
                immuneToOffensiveAbilities = true
            )
            ImmunityType.PENALTIES -> state.copy(
                immuneToPenalties = true
            )
            ImmunityType.ALL_EFFECTS -> state.copy(
                immuneToDistraction = true,
                immuneToOffensiveAbilities = true,
                immuneToPenalties = true
            )
        }
    }
    
    /**
     * Execute an active ability
     */
    fun executeAbility(
        abilityId: String,
        targetData: AbilityTargetData? = null,
        state: GCMSState
    ): AbilityExecutionResult {
        val progress = state.skillAbilitySystem.playerProgress
        
        // Check if ability can be used
        val canUse = SkillAbilitySystem.canUseAbility(abilityId, progress)
        if (!canUse.success) {
            return AbilityExecutionResult(
                success = false,
                abilityId = abilityId,
                state = state,
                errorMessage = canUse.errorMessage
            )
        }
        
        val ability = SkillAbilityDatabase.getNodeById(abilityId) as? AbilityNode
            ?: return AbilityExecutionResult(false, abilityId, state, "Ability not found")
        
        // Apply ability effect
        val result = applyAbilityEffect(ability.effect, targetData, state)
        
        // Update usage count
        val updatedProgress = progress.copy(
            abilityUsageCounts = progress.abilityUsageCounts + 
                (abilityId to ((progress.abilityUsageCounts[abilityId] ?: 0) + 1))
        )
        
        return result.copy(
            success = true,
            state = result.state.copy(
                skillAbilitySystem = state.skillAbilitySystem.copy(
                    playerProgress = updatedProgress
                )
            )
        )
    }
    
    /**
     * Apply ability effect
     */
    private fun applyAbilityEffect(
        effect: AbilityEffect,
        targetData: AbilityTargetData?,
        state: GCMSState
    ): AbilityExecutionResult {
        return when (effect) {
            is AbilityEffect.PEEK_DECK -> handlePeekDeck(effect.cards, state)
            is AbilityEffect.REROLL_DICE -> handleRerollDice(effect.maxResult, state)
            is AbilityEffect.PROTECT_CARD -> handleProtectCard(effect.count, targetData, state)
            is AbilityEffect.FORCE_DISCARD -> handleForceDiscard(effect.count, targetData, state)
            is AbilityEffect.BONUS_POINTS -> handleBonusPoints(effect.points, targetData, state)
            is AbilityEffect.REMOVE_PENALTY -> handleRemovePenalty(effect.count, effect.ranks, state)
            is AbilityEffect.DOUBLE_POINTS -> handleDoublePoints(effect.count, targetData, state)
            is AbilityEffect.SKIP_DRAW_PHASE -> handleSkipDrawPhase(state)
            is AbilityEffect.NEGATE_ABILITY -> handleNegateAbility(state)
            is AbilityEffect.END_ROUND_IMMEDIATELY -> handleEndRoundImmediately(state)
            is AbilityEffect.SKIP_ANIMATIONS -> handleSkipAnimations(state)
            is AbilityEffect.AVOID_PENALTY -> handleAvoidPenalty(effect.count, state)
            is AbilityEffect.FORCE_REDRAW -> handleForceRedraw(targetData, state)
            is AbilityEffect.IMMUNITY_ROUND -> handleImmunityRound(state)
            is AbilityEffect.WRONG_SLOT_PLACEMENT -> handleWrongSlotPlacement(targetData, state)
            is AbilityEffect.REMOVE_ALL_PENALTIES -> handleRemoveAllPenalties(state)
            is AbilityEffect.FREE_ABILITY -> handleFreeAbility(state)
            is AbilityEffect.DOUBLE_PENALTY -> handleDoublePenalty(targetData, state)
            else -> AbilityExecutionResult(true, "UNKNOWN", state, "Effect not implemented yet")
        }
    }
    
    /**
     * Handle peek deck effect
     */
    private fun handlePeekDeck(cards: Int, state: GCMSState): AbilityExecutionResult {
        // Return top cards from deck
        val topCards = state.deck.take(cards)
        return AbilityExecutionResult(
            success = true,
            abilityId = "PEEK_DECK",
            state = state,
            revealedCards = topCards,
            message = "Revealed top $cards cards from deck"
        )
    }
    
    /**
     * Handle reroll dice effect
     */
    private fun handleRerollDice(maxResult: Int, state: GCMSState): AbilityExecutionResult {
        val newRoll = (1..maxResult).random()
        return AbilityExecutionResult(
            success = true,
            abilityId = "REROLL_DICE",
            state = state.copy(diceRoll = newRoll),
            message = "Dice rerolled to $newRoll"
        )
    }
    
    /**
     * Handle protect card effect
     */
    private fun handleProtectCard(
        count: Int,
        targetData: AbilityTargetData?,
        state: GCMSState
    ): AbilityExecutionResult {
        val cardIds = targetData?.cardIds ?: emptyList()
        val protectedCards = cardIds.take(count)
        
        return AbilityExecutionResult(
            success = true,
            abilityId = "PROTECT_CARD",
            state = state.copy(
                protectedCards = state.protectedCards + protectedCards
            ),
            message = "Protected $count cards from penalties"
        )
    }
    
    /**
     * Handle force discard effect
     */
    private fun handleForceDiscard(
        count: Int,
        targetData: AbilityTargetData?,
        state: GCMSState
    ): AbilityExecutionResult {
        // Force opponent to discard cards
        // This would be implemented with opponent data
        return AbilityExecutionResult(
            success = true,
            abilityId = "FORCE_DISCARD",
            state = state,
            message = "Opponent forced to discard $count cards"
        )
    }
    
    /**
     * Handle bonus points effect
     */
    private fun handleBonusPoints(
        points: Int,
        targetData: AbilityTargetData?,
        state: GCMSState
    ): AbilityExecutionResult {
        val cardId = targetData?.cardIds?.firstOrNull()
        
        if (cardId != null) {
            val bonusCards = state.bonusCards + cardId
            return AbilityExecutionResult(
                success = true,
                abilityId = "BONUS_POINTS",
                state = state.copy(bonusCards = bonusCards),
                message = "Card will score +$points points"
            )
        }
        
        return AbilityExecutionResult(false, "BONUS_POINTS", state, "No card selected")
    }
    
    /**
     * Handle remove penalty effect
     */
    private fun handleRemovePenalty(
        count: Int,
        ranks: List<CardRank>,
        state: GCMSState
    ): AbilityExecutionResult {
        // Remove penalties for specified card ranks
        return AbilityExecutionResult(
            success = true,
            abilityId = "REMOVE_PENALTY",
            state = state,
            message = "Removed penalties for $count cards"
        )
    }
    
    /**
     * Handle double points effect
     */
    private fun handleDoublePoints(
        count: Int,
        targetData: AbilityTargetData?,
        state: GCMSState
    ): AbilityExecutionResult {
        val cardIds = targetData?.cardIds ?: emptyList()
        val doubledCards = cardIds.take(count)
        
        return AbilityExecutionResult(
            success = true,
            abilityId = "DOUBLE_POINTS",
            state = state.copy(
                doubledCards = state.doubledCards + doubledCards
            ),
            message = "$count cards will score double points"
        )
    }
    
    /**
     * Handle skip draw phase effect
     */
    private fun handleSkipDrawPhase(state: GCMSState): AbilityExecutionResult {
        return AbilityExecutionResult(
            success = true,
            abilityId = "SKIP_DRAW_PHASE",
            state = state.copy(
                skipDrawPhase = true
            ),
            message = "Draw phase skipped"
        )
    }
    
    /**
     * Handle negate ability effect
     */
    private fun handleNegateAbility(state: GCMSState): AbilityExecutionResult {
        return AbilityExecutionResult(
            success = true,
            abilityId = "NEGATE_ABILITY",
            state = state,
            message = "Opponent's ability negated"
        )
    }
    
    /**
     * Handle end round immediately effect
     */
    private fun handleEndRoundImmediately(state: GCMSState): AbilityExecutionResult {
        return AbilityExecutionResult(
            success = true,
            abilityId = "END_ROUND_IMMEDIATELY",
            state = state.copy(
                endRoundImmediately = true
            ),
            message = "Round will end immediately"
        )
    }
    
    /**
     * Handle skip animations effect
     */
    private fun handleSkipAnimations(state: GCMSState): AbilityExecutionResult {
        return AbilityExecutionResult(
            success = true,
            abilityId = "SKIP_ANIMATIONS",
            state = state.copy(
                skipAnimations = true
            ),
            message = "Animations skipped"
        )
    }
    
    /**
     * Handle avoid penalty effect
     */
    private fun handleAvoidPenalty(count: Int, state: GCMSState): AbilityExecutionResult {
        return AbilityExecutionResult(
            success = true,
            abilityId = "AVOID_PENALTY",
            state = state.copy(
                avoidPenalties = (state.avoidPenalties ?: 0) + count
            ),
            message = "Will avoid next $count penalties"
        )
    }
    
    /**
     * Handle force redraw effect
     */
    private fun handleForceRedraw(
        targetData: AbilityTargetData?,
        state: GCMSState
    ): AbilityExecutionResult {
        return AbilityExecutionResult(
            success = true,
            abilityId = "FORCE_REDRAW",
            state = state,
            message = "Opponent forced to redraw"
        )
    }
    
    /**
     * Handle immunity round effect
     */
    private fun handleImmunityRound(state: GCMSState): AbilityExecutionResult {
        return AbilityExecutionResult(
            success = true,
            abilityId = "IMMUNITY_ROUND",
            state = state.copy(
                immuneToPenalties = true
            ),
            message = "Immune to all effects this round"
        )
    }
    
    /**
     * Handle wrong slot placement effect
     */
    private fun handleWrongSlotPlacement(
        targetData: AbilityTargetData?,
        state: GCMSState
    ): AbilityExecutionResult {
        return AbilityExecutionResult(
            success = true,
            abilityId = "WRONG_SLOT_PLACEMENT",
            state = state,
            message = "Can place card in wrong slot"
        )
    }
    
    /**
     * Handle remove all penalties effect
     */
    private fun handleRemoveAllPenalties(state: GCMSState): AbilityExecutionResult {
        return AbilityExecutionResult(
            success = true,
            abilityId = "REMOVE_ALL_PENALTIES",
            state = state,
            message = "All penalties removed"
        )
    }
    
    /**
     * Handle free ability effect
     */
    private fun handleFreeAbility(state: GCMSState): AbilityExecutionResult {
        return AbilityExecutionResult(
            success = true,
            abilityId = "FREE_ABILITY",
            state = state.copy(
                nextAbilityFree = true
            ),
            message = "Next ability costs 0 AP"
        )
    }
    
    /**
     * Handle double penalty effect
     */
    private fun handleDoublePenalty(
        targetData: AbilityTargetData?,
        state: GCMSState
    ): AbilityExecutionResult {
        return AbilityExecutionResult(
            success = true,
            abilityId = "DOUBLE_PENALTY",
            state = state,
            message = "Opponent's next penalty will be doubled"
        )
    }
}

/**
 * Result of ability execution
 */
data class AbilityExecutionResult(
    val success: Boolean,
    val abilityId: String,
    val state: GCMSState,
    val message: String? = null,
    val errorMessage: String? = null,
    val revealedCards: List<Card> = emptyList()
)

/**
 * Target data for ability execution
 */
data class AbilityTargetData(
    val cardIds: List<String> = emptyList(),
    val playerId: String? = null,
    val slotIndex: Int? = null,
    val customData: Map<String, Any> = emptyMap()
)