package com.trashpiles.gcms

/**
 * Core logic for the Skill & Ability Points System
 * 
 * This file contains the business logic for:
 * - Calculating card penalties
 * - Processing match completion
 * - Validating node unlocks
 * - Applying ability effects
 */

// Type alias for convenience
typealias Card = CardState

/**
 * Calculate round score using CORRECT Trash game rules
 * 
 * RULES:
 * - Only FACE-UP cards count for points
 * - Base score = sum of face-up card slot numbers
 * - Joker face-up = +20 bonus points
 * - Dice bonus = multiplier × dice roll
 * - Winner multiplier = last card flipped slot
 * - Loser multiplier = count of face-up cards
 * 
 * @param hand List of cards in player's hand
 * @param isWinner Whether this player won the round
 * @param lastCardFlippedSlot Slot number of last card flipped (for winners)
 * @param diceRoll Dice roll result (1-6)
 * @return RoundScoreResult with all scoring details
 */
fun calculateRoundScore(
    hand: List<Card>,
    isWinner: Boolean,
    lastCardFlippedSlot: Int?,
    diceRoll: Int
): RoundScoreResult {
    // Only face-UP cards count
    val faceUpCards = hand.filter { it.isFaceUp }
    
    // Calculate base score (sum of slot numbers)
    var baseScore = 0
    val cardScores = mutableListOf<CardPenalty>()
    
    faceUpCards.forEachIndexed { index, card ->
        val slotNumber = index + 1
        baseScore += slotNumber
        cardScores.add(CardPenalty(card.rank, slotNumber, slotNumber))
        
        // Joker face-up gives +20 bonus
        if (card.rank.equals("Joker", ignoreCase = true)) {
            baseScore += 20
            cardScores.add(CardPenalty("Joker Bonus", slotNumber, 20))
        }
    }
    
    // Determine multiplier
    val multiplier = if (isWinner) {
        lastCardFlippedSlot ?: 10 // Default to 10 if not provided
    } else {
        faceUpCards.size
    }
    
    // Calculate dice bonus
    val bonus = multiplier * diceRoll
    val finalScore = baseScore + bonus
    
    return RoundScoreResult(
        baseScore = baseScore,
        multiplier = multiplier,
        diceRoll = diceRoll,
        bonus = bonus,
        finalScore = finalScore,
        cardScores = cardScores
    )
}

/**
 * Calculate AP penalties from face-DOWN cards across entire match
 * 
 * PENALTIES (face-DOWN cards only):
 * - King: -3 AP
 * - Queen: -2 AP
 * - Jack: -1 AP
 * - Joker: -20 AP
 * 
 * @param matchHistory List of all rounds in the match with player's hands
 * @return Total AP penalty (negative number)
 */
fun calculateMatchAPPenalties(matchHistory: List<List<Card>>): Int {
    var totalPenalty = 0
    
    for (roundHand in matchHistory) {
        val faceDownCards = roundHand.filter { !it.isFaceUp }
        
        for (card in faceDownCards) {
            when {
                card.rank.equals("King", ignoreCase = true) -> totalPenalty -= 3
                card.rank.equals("Queen", ignoreCase = true) -> totalPenalty -= 2
                card.rank.equals("Jack", ignoreCase = true) -> totalPenalty -= 1
                card.rank.equals("Joker", ignoreCase = true) -> totalPenalty -= 20
            }
        }
    }
    
    return totalPenalty
}

/**
 * Legacy function for backward compatibility
 * Now returns AP penalties for face-DOWN cards
 */
fun calculateCardPenalties(hand: List<Card>): Pair<Int, List<CardPenalty>> {
    val faceDownCards = hand.filter { !it.isFaceUp }
    val penalties = mutableListOf<CardPenalty>()
    var totalPenalty = 0
    
    faceDownCards.forEachIndexed { index, card ->
        val slotNumber = index + 1
        val penalty = when {
            card.rank.equals("King", ignoreCase = true) -> -3
            card.rank.equals("Queen", ignoreCase = true) -> -2
            card.rank.equals("Jack", ignoreCase = true) -> -1
            card.rank.equals("Joker", ignoreCase = true) -> -20
            else -> 0
        }
        
        if (penalty != 0) {
            totalPenalty += penalty
            penalties.add(CardPenalty(card.rank, slotNumber, penalty))
        }
    }
    
    return Pair(totalPenalty, penalties)
}

/**
 * Result of round score calculation
 */
data class RoundScoreResult(
    val baseScore: Int,
    val multiplier: Int,
    val diceRoll: Int,
    val bonus: Int,
    val finalScore: Int,
    val cardScores: List<CardPenalty>
)

/**
 * Process match completion and award SP/AP/XP to winner
 * 
 * RULES:
 * - Only the winner earns SP/AP/XP
 * - SP = base value (never penalized)
 * - AP = base value - penalties from face-DOWN cards
 * - XP = (SP + AP) × (1 + (Level - 1) × 0.1)
 * - Hybrid system: Level 1-3 uses old table, Level 4+ uses round-based
 * 
 * @param state Current GCMS state
 * @param winnerId ID of the winning player
 * @param matchHistory List of all round hands for penalty calculation
 * @return MatchResult with points earned and penalties
 */
fun processMatchCompletion(
    state: GCMSState,
    winnerId: String,
    matchHistory: List<List<Card>>
): MatchResult {
    // Get winner
    val winner = state.players.find { it.id.toString() == winnerId }
        ?: throw IllegalArgumentException("Winner not found: $winnerId")
    
    // Get player progress
    val progress = state.skillAbilitySystem.getPlayerProgress(winnerId)
    
    // Get current round and match info
    val currentRound = state.skillAbilitySystem.currentRound
    val matchInRound = state.skillAbilitySystem.matchInRound
    
    // Calculate AP penalties from face-DOWN cards
    val totalAPPenalty = calculateMatchAPPenalties(matchHistory)
    
    // Get base points using hybrid system
    val (baseSP, baseAP) = MatchRewards.getBaseRewards(
        playerLevel = progress.level,
        roundNumber = currentRound,
        matchInRound = matchInRound
    )
    
    // Calculate actual points earned
    val spEarned = baseSP  // SP is never penalized
    val apEarned = maxOf(0, baseAP + totalAPPenalty)  // AP reduced by penalties, can't go negative
    
    // Calculate XP using unlimited formula (includes match and round bonuses)
    val totalMatches = progress.matchHistory.size + 1
    val totalRounds = progress.matchHistory.maxOfOrNull { it.roundNumber } ?: 1
    val xpEarned = LevelSystem.calculateXP(
        spEarned,
        apEarned,
        progress.level,
        totalMatches,
        totalRounds
    )
    
    // Create penalty list for display
    val penalties = mutableListOf<CardPenalty>()
    for (roundHand in matchHistory) {
        val faceDownCards = roundHand.filter { !it.isFaceUp }
        faceDownCards.forEachIndexed { index, card ->
            val slotNumber = index + 1
            when {
                card.rank.equals("King", ignoreCase = true) -> 
                    penalties.add(CardPenalty("King", slotNumber, -3))
                card.rank.equals("Queen", ignoreCase = true) -> 
                    penalties.add(CardPenalty("Queen", slotNumber, -2))
                card.rank.equals("Jack", ignoreCase = true) -> 
                    penalties.add(CardPenalty("Jack", slotNumber, -1))
                card.rank.equals("Joker", ignoreCase = true) -> 
                    penalties.add(CardPenalty("Joker", slotNumber, -20))
            }
        }
    }
    
    // Create match result
    val result = MatchResult(
        matchNumber = matchInRound,
        roundNumber = currentRound,
        won = true,
        spEarned = spEarned,
        apEarned = apEarned,
        xpEarned = xpEarned,
        penalties = penalties
    )
    
    // Update player progress (returns true if leveled up)
    val leveledUp = progress.addMatchResult(result)
    
    // Advance to next match
    state.skillAbilitySystem.advanceMatch()
    
    return result
}

/**
 * Validate and unlock a skill/ability node
 * 
 * @param state Current GCMS state
 * @param playerId ID of the player
 * @param nodeId ID of the node to unlock
 * @param pointType Type of points (SKILL or ABILITY)
 * @return Result with success status and optional error message
 */
fun unlockNode(
    state: GCMSState,
    playerId: String,
    nodeId: String,
    pointType: PointType
): UnlockResult {
    // Get player progress
    val progress = state.skillAbilitySystem.getPlayerProgress(playerId)
    
    // Get the node
    val node = when (pointType) {
        PointType.SKILL -> SkillTree.getNode(nodeId)
        PointType.ABILITY -> AbilityTree.getNode(nodeId)
    } ?: return UnlockResult(
        success = false,
        message = "Node not found: $nodeId"
    )
    
    // Check level requirement
    val minLevel = when (pointType) {
        PointType.SKILL -> (node as? SkillNode)?.minLevel ?: 1
        PointType.ABILITY -> (node as? AbilityNode)?.minLevel ?: 1
    }
    
    if (progress.level < minLevel) {
        return UnlockResult(
            success = false,
            message = "Requires Level $minLevel (current: ${progress.level})"
        )
    }
    
    // Check if already unlocked
    if (progress.isUnlocked(nodeId, pointType)) {
        return UnlockResult(
            success = false,
            message = "Node already unlocked: ${node.name}"
        )
    }
    
    // Check if player can afford it
    if (!progress.canAfford(node.cost, pointType)) {
        val pointName = if (pointType == PointType.SKILL) "SP" else "AP"
        val current = if (pointType == PointType.SKILL) progress.totalSP else progress.totalAP
        return UnlockResult(
            success = false,
            message = "Insufficient points: need ${node.cost} $pointName, have $current $pointName"
        )
    }
    
    // Check prerequisites
    val unmetPrereqs = node.prerequisites.filter { prereq ->
        !progress.isUnlocked(prereq, pointType)
    }
    
    if (unmetPrereqs.isNotEmpty()) {
        return UnlockResult(
            success = false,
            message = "Prerequisites not met: ${unmetPrereqs.joinToString(", ")}"
        )
    }
    
    // All checks passed - unlock the node
    progress.deductPoints(node.cost, pointType)
    progress.unlockNode(nodeId, pointType)
    
    return UnlockResult(
        success = true,
        message = "Unlocked: ${node.name}",
        nodeId = nodeId,
        nodeName = node.name,
        pointsSpent = node.cost
    )
}

/**
 * Result of attempting to unlock a node
 */
data class UnlockResult(
    val success: Boolean,
    val message: String,
    val nodeId: String? = null,
    val nodeName: String? = null,
    val pointsSpent: Int? = null
)

/**
 * Apply an ability effect to the game state
 * 
 * @param state Current GCMS state
 * @param playerId ID of the player using the ability
 * @param abilityId ID of the ability to use
 * @param targetData Optional data for targeting (e.g., card index)
 * @return Result with success status and effect description
 */
fun useAbility(
    state: GCMSState,
    playerId: String,
    abilityId: String,
    targetData: Map<String, Any>? = null
): AbilityResult {
    // Get player progress
    val progress = state.skillAbilitySystem.getPlayerProgress(playerId)
    
    // Check if ability is unlocked
    if (!progress.isUnlocked(abilityId, PointType.ABILITY)) {
        return AbilityResult(
            success = false,
            message = "Ability not unlocked: $abilityId"
        )
    }
    
    // Get the ability node
    val ability = AbilityTree.getNode(abilityId)
        ?: return AbilityResult(
            success = false,
            message = "Ability not found: $abilityId"
        )
    
    // Get the player
    val player = state.players.find { it.id == playerId }
        ?: return AbilityResult(
            success = false,
            message = "Player not found: $playerId"
        )
    
    // Apply ability effect based on type
    val effectDescription = when (abilityId) {
        "peek" -> {
            // Peek at 1 face-down card
            val cardIndex = targetData?.get("cardIndex") as? Int
                ?: return AbilityResult(
                    success = false,
                    message = "Card index required for Peek ability"
                )
            
            if (cardIndex < 0 || cardIndex >= player.hand.size) {
                return AbilityResult(
                    success = false,
                    message = "Invalid card index: $cardIndex"
                )
            }
            
            val card = player.hand[cardIndex]
            "Peeked at card in slot ${cardIndex + 1}: ${card.rank} of ${card.suit}"
        }
        
        "reveal" -> {
            // Force opponent to flip 1 card
            val opponentId = targetData?.get("opponentId") as? String
                ?: return AbilityResult(
                    success = false,
                    message = "Opponent ID required for Reveal ability"
                )
            
            val cardIndex = targetData["cardIndex"] as? Int
                ?: return AbilityResult(
                    success = false,
                    message = "Card index required for Reveal ability"
                )
            
            val opponent = state.players.find { it.id == opponentId }
                ?: return AbilityResult(
                    success = false,
                    message = "Opponent not found: $opponentId"
                )
            
            if (cardIndex < 0 || cardIndex >= opponent.hand.size) {
                return AbilityResult(
                    success = false,
                    message = "Invalid card index: $cardIndex"
                )
            }
            
            // Flip the card face-up
            val updatedHand = opponent.hand.toMutableList()
            updatedHand[cardIndex] = updatedHand[cardIndex].copy(faceUp = true)
            
            val updatedOpponent = opponent.copy(hand = updatedHand)
            val updatedPlayers = state.players.map { 
                if (it.id == opponentId) updatedOpponent else it 
            }
            
            // Update state (this would be done by GCMS controller)
            "Revealed opponent's card in slot ${cardIndex + 1}"
        }
        
        "wild_card" -> {
            // Make any card act as a wild card
            val cardIndex = targetData?.get("cardIndex") as? Int
                ?: return AbilityResult(
                    success = false,
                    message = "Card index required for Wild Card ability"
                )
            
            if (cardIndex < 0 || cardIndex >= player.hand.size) {
                return AbilityResult(
                    success = false,
                    message = "Invalid card index: $cardIndex"
                )
            }
            
            "Converted card in slot ${cardIndex + 1} to wild card"
        }
        
        "swap_master" -> {
            // Swap 2 cards in hand
            val index1 = targetData?.get("index1") as? Int
                ?: return AbilityResult(
                    success = false,
                    message = "First card index required for Swap Master ability"
                )
            
            val index2 = targetData?.get("index2") as? Int
                ?: return AbilityResult(
                    success = false,
                    message = "Second card index required for Swap Master ability"
                )
            
            if (index1 < 0 || index1 >= player.hand.size || 
                index2 < 0 || index2 >= player.hand.size) {
                return AbilityResult(
                    success = false,
                    message = "Invalid card indices: $index1, $index2"
                )
            }
            
            "Swapped cards in slots ${index1 + 1} and ${index2 + 1}"
        }
        
        "ultimate_power" -> {
            // Reshuffle entire hand
            "Reshuffled all face-down cards"
        }
        
        else -> {
            return AbilityResult(
                success = false,
                message = "Unknown ability: $abilityId"
            )
        }
    }
    
    return AbilityResult(
        success = true,
        message = effectDescription,
        abilityId = abilityId,
        abilityName = ability.name
    )
}

/**
 * Result of using an ability
 */
data class AbilityResult(
    val success: Boolean,
    val message: String,
    val abilityId: String? = null,
    val abilityName: String? = null
)

/**
 * Check if a player has a specific skill unlocked
 */
fun hasSkill(state: GCMSState, playerId: String, skillId: String): Boolean {
    val progress = state.skillAbilitySystem.getPlayerProgress(playerId)
    return progress.isUnlocked(skillId, PointType.SKILL)
}

/**
 * Check if a player has a specific ability unlocked
 */
fun hasAbility(state: GCMSState, playerId: String, abilityId: String): Boolean {
    val progress = state.skillAbilitySystem.getPlayerProgress(playerId)
    return progress.isUnlocked(abilityId, PointType.ABILITY)
}

/**
 * Get all active skills for a player
 */
fun getActiveSkills(state: GCMSState, playerId: String): List<SkillNode> {
    val progress = state.skillAbilitySystem.getPlayerProgress(playerId)
    return SkillTree.nodes.filter { node ->
        progress.isUnlocked(node.id, PointType.SKILL)
    }
}

/**
 * Get all active abilities for a player
 */
fun getActiveAbilities(state: GCMSState, playerId: String): List<AbilityNode> {
    val progress = state.skillAbilitySystem.getPlayerProgress(playerId)
    return AbilityTree.nodes.filter { node ->
        progress.isUnlocked(node.id, PointType.ABILITY)
    }
}