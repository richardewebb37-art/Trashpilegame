package com.trashpiles.gcms

import com.trashpiles.gcms.SkillAbilitySystem.PointType

/**
 * Complete database of all 100 Skills & Abilities for Levels 1-200
 * 
 * Organization:
 * - 7 Tiers: Newbie (1-20), Beginner (21-50), Novice (51-80), 
 *   Intermediate (81-110), Hard (111-140), Expert (141-170), Master (171-200)
 * - 8 Categories: General Progression, Combat & Offensive, Defense & Survival,
 *   Support & Tactical, Magic & Arcane, Movement & Evasion, Precision & Technique,
 *   Power & Strength, Mental & Special, Advanced/Master
 */

object SkillAbilityDatabase {
    
    /**
     * All skills and abilities in the game
     * Key: Unique identifier (e.g., "QUICK_LEARNER", "INTUITION")
     */
    val allSkillsAndAbilities: Map<String, TreeNode> = generateAllNodes()
    
    /**
     * Get skill/ability by ID
     */
    fun getNodeById(id: String): TreeNode? = allSkillsAndAbilities[id]
    
    /**
     * Get all skills of a specific type
     */
    fun getSkillsByType(type: PointType): List<TreeNode> {
        return allSkillsAndAbilities.values.filter { it.pointType == type }
    }
    
    /**
     * Get all skills/abilities available at a specific level
     */
    fun getNodesForLevel(level: Int): List<TreeNode> {
        return allSkillsAndAbilities.values.filter { it.levelRequired == level }
    }
    
    /**
     * Get all skills/abilities up to a specific level
     */
    fun getNodesUpToLevel(level: Int): List<TreeNode> {
        return allSkillsAndAbilities.values.filter { it.levelRequired <= level }
    }
    
    /**
     * Get all skills/abilities in a tier
     */
    fun getNodesByTier(tier: Tier): List<TreeNode> {
        val range = when (tier) {
            Tier.NEWBIE -> 1..20
            Tier.BEGINNER -> 21..50
            Tier.NOVICE -> 51..80
            Tier.INTERMEDIATE -> 81..110
            Tier.HARD -> 111..140
            Tier.EXPERT -> 141..170
            Tier.MASTER -> 171..200
        }
        return allSkillsAndAbilities.values.filter { it.levelRequired in range }
    }
    
    /**
     * Check if prerequisites are met
     */
    fun arePrerequisitesMet(nodeId: String, unlockedNodes: Set<String>): Boolean {
        val node = allSkillsAndAbilities[nodeId] ?: return false
        return node.prerequisites.all { it in unlockedNodes }
    }
    
    /**
     * Generate all 100 skills and abilities
     */
    private fun generateAllNodes(): Map<String, TreeNode> {
        val nodes = mutableMapOf<String, TreeNode>()
        
        // ========================================
        // TIER 1: NEWBIE (Levels 1-20) - 15 items
        // ========================================
        
        // General Progression
        nodes["QUICK_LEARNER"] = SkillNode(
            id = "QUICK_LEARNER",
            name = "Quick Learner",
            cost = 3,
            levelRequired = 3,
            tier = Tier.NEWBIE,
            category = SkillCategory.GENERAL_PROGRESSION,
            description = "Bonus +10% XP from all matches",
            effect = SkillEffect.XP_BOOST(10),
            xpReward = 10,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["FOCUSED_MIND"] = SkillNode(
            id = "FOCUSED_MIND",
            name = "Focused Mind",
            cost = 5,
            levelRequired = 5,
            tier = Tier.NEWBIE,
            category = SkillCategory.GENERAL_PROGRESSION,
            description = "Turn timer +2 seconds (30s → 32s)",
            effect = SkillEffect.TIMER_BOOST(2),
            xpReward = 15,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["INTUITION"] = AbilityNode(
            id = "INTUITION",
            name = "Intuition",
            cost = 6,
            levelRequired = 8,
            tier = Tier.NEWBIE,
            category = SkillCategory.GENERAL_PROGRESSION,
            description = "Peek at top card of deck before deciding to draw",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.PEEK_DECK(1),
            xpReward = 20,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["RESOURCEFUL"] = SkillNode(
            id = "RESOURCEFUL",
            name = "Resourceful",
            cost = 8,
            levelRequired = 12,
            tier = Tier.NEWBIE,
            category = SkillCategory.GENERAL_PROGRESSION,
            description = "When drawing from deck, see top 2 cards and choose 1",
            effect = SkillEffect.PEEK_DECK(2),
            xpReward = 25,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["LUCKY_BREAK"] = AbilityNode(
            id = "LUCKY_BREAK",
            name = "Lucky Break",
            cost = 10,
            levelRequired = 15,
            tier = Tier.NEWBIE,
            category = SkillCategory.GENERAL_PROGRESSION,
            description = "Reroll dice once after seeing result",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.REROLL_DICE,
            xpReward = 30,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["RESOURCE_HOARDER"] = SkillNode(
            id = "RESOURCE_HOARDER",
            name = "Resource Hoarder",
            cost = 12,
            levelRequired = 18,
            tier = Tier.NEWBIE,
            category = SkillCategory.GENERAL_PROGRESSION,
            description = "+5% SP earned from matches",
            effect = SkillEffect.SP_BOOST(5),
            xpReward = 35,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Defense & Survival
        nodes["IRON_WILL"] = SkillNode(
            id = "IRON_WILL",
            name = "Iron Will",
            cost = 8,
            levelRequired = 10,
            tier = Tier.NEWBIE,
            category = SkillCategory.DEFENSE_SURVIVAL,
            description = "King penalty reduced from -3 AP to -2 AP",
            effect = SkillEffect.PENALTY_REDUCTION(CardRank.KING, 1),
            xpReward = 20,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["GUARD"] = AbilityNode(
            id = "GUARD",
            name = "Guard",
            cost = 5,
            levelRequired = 13,
            tier = Tier.NEWBIE,
            category = SkillCategory.DEFENSE_SURVIVAL,
            description = "Protect one face-down card from penalties (choose at end of round)",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.PROTECT_CARD(1),
            xpReward = 25,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["TOUGH_SKIN"] = SkillNode(
            id = "TOUGH_SKIN",
            name = "Tough Skin",
            cost = 12,
            levelRequired = 20,
            tier = Tier.NEWBIE,
            category = SkillCategory.DEFENSE_SURVIVAL,
            description = "Jack penalty reduced from -1 AP to 0 AP",
            effect = SkillEffect.PENALTY_REDUCTION(CardRank.JACK, 1),
            xpReward = 40,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Support & Tactical
        nodes["ALERTNESS"] = SkillNode(
            id = "ALERTNESS",
            name = "Alertness",
            cost = 7,
            levelRequired = 7,
            tier = Tier.NEWBIE,
            category = SkillCategory.SUPPORT_TACTICAL,
            description = "See opponent's hand size at all times",
            effect = SkillEffect.SEE_OPPONENT_HAND_SIZE,
            xpReward = 15,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Mental & Special
        nodes["WILLPOWER"] = SkillNode(
            id = "WILLPOWER",
            name = "Willpower",
            cost = 10,
            levelRequired = 11,
            tier = Tier.NEWBIE,
            category = SkillCategory.MENTAL_SPECIAL,
            description = "Immune to distraction (visual effects from opponent abilities don't affect your screen)",
            effect = SkillEffect.IMMUNITY(ImmunityType.DISTRACTION),
            xpReward = 25,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Movement & Evasion
        nodes["SPRINT"] = AbilityNode(
            id = "SPRINT",
            name = "Sprint",
            cost = 6,
            levelRequired = 16,
            tier = Tier.NEWBIE,
            category = SkillCategory.MOVEMENT_EVASION,
            description = "Skip waiting for animations, instantly resolve your turn",
            usesPerMatch = 3,
            usesPerRound = 3,
            effect = AbilityEffect.SKIP_ANIMATIONS,
            xpReward = 30,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Precision & Technique
        nodes["DEAD_EYE"] = SkillNode(
            id = "DEAD_EYE",
            name = "Dead Eye",
            cost = 12,
            levelRequired = 14,
            tier = Tier.NEWBIE,
            category = SkillCategory.PRECISION_TECHNIQUE,
            description = "When drawing, 10% higher chance of drawing cards you need",
            effect = SkillEffect.DRAW_BONUS(10),
            xpReward = 25,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Magic & Arcane
        nodes["MANA_SURGE"] = AbilityNode(
            id = "MANA_SURGE",
            name = "Mana Surge",
            cost = 10,
            levelRequired = 26,
            tier = Tier.BEGINNER,
            category = SkillCategory.MAGIC_ARCANE,
            description = "Next ability used costs 0 AP",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.FREE_ABILITY,
            xpReward = 40,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Power & Strength
        nodes["HEAVY_STRIKE"] = AbilityNode(
            id = "HEAVY_STRIKE",
            name = "Heavy Strike",
            cost = 12,
            levelRequired = 46,
            tier = Tier.BEGINNER,
            category = SkillCategory.POWER_STRENGTH,
            description = "Place card scores +5 points (one card, your choice)",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.BONUS_POINTS(5),
            xpReward = 50,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // ========================================
        // TIER 2: BEGINNER (Levels 21-50) - 20 items
        // ========================================
        
        nodes["EFFICIENCY"] = SkillNode(
            id = "EFFICIENCY",
            name = "Efficiency",
            cost = 15,
            levelRequired = 25,
            tier = Tier.BEGINNER,
            category = SkillCategory.GENERAL_PROGRESSION,
            description = "Turn timer +3 seconds (32s → 35s with Focused Mind)",
            effect = SkillEffect.TIMER_BOOST(3),
            xpReward = 50,
            trophyId = null,
            prerequisites = listOf("FOCUSED_MIND")
        )
        
        nodes["TACTICAL_RECALL"] = SkillNode(
            id = "TACTICAL_RECALL",
            name = "Tactical Recall",
            cost = 20,
            levelRequired = 35,
            tier = Tier.BEGINNER,
            category = SkillCategory.GENERAL_PROGRESSION,
            description = "See entire match history (all cards played/discarded by everyone)",
            effect = SkillEffect.SEE_MATCH_HISTORY,
            xpReward = 70,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["RESOURCE_MANAGEMENT"] = SkillNode(
            id = "RESOURCE_MANAGEMENT",
            name = "Resource Management",
            cost = 25,
            levelRequired = 42,
            tier = Tier.BEGINNER,
            category = SkillCategory.GENERAL_PROGRESSION,
            description = "+5% AP earned from matches",
            effect = SkillEffect.AP_BOOST(5),
            xpReward = 85,
            trophyId = null,
            prerequisites = listOf("RESOURCE_HOARDER")
        )
        
        // Combat & Offensive
        nodes["PRECISION_STRIKE"] = SkillNode(
            id = "PRECISION_STRIKE",
            name = "Precision Strike",
            cost = 10,
            levelRequired = 22,
            tier = Tier.BEGINNER,
            category = SkillCategory.COMBAT_OFFENSIVE,
            description = "See top card of deck at all times",
            effect = SkillEffect.PEEK_DECK(1),
            xpReward = 45,
            trophyId = null,
            prerequisites = listOf("FOCUSED_MIND")
        )
        
        nodes["SWIFT_FOOTWORK"] = AbilityNode(
            id = "SWIFT_FOOTWORK",
            name = "Swift Footwork",
            cost = 8,
            levelRequired = 28,
            tier = Tier.BEGINNER,
            category = SkillCategory.COMBAT_OFFENSIVE,
            description = "Skip your draw phase, immediately end turn (fast pass)",
            usesPerMatch = 2,
            usesPerRound = 1,
            effect = AbilityEffect.SKIP_DRAW_PHASE,
            xpReward = 55,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["POWER_SHOT"] = AbilityNode(
            id = "POWER_SHOT",
            name = "Power Shot",
            cost = 15,
            levelRequired = 32,
            tier = Tier.BEGINNER,
            category = SkillCategory.COMBAT_OFFENSIVE,
            description = "Force opponent to discard their top 2 cards from deck",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.FORCE_DISCARD(2),
            xpReward = 65,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["CRITICAL_FOCUS"] = SkillNode(
            id = "CRITICAL_FOCUS",
            name = "Critical Focus",
            cost = 18,
            levelRequired = 38,
            tier = Tier.BEGINNER,
            category = SkillCategory.COMBAT_OFFENSIVE,
            description = "Dice bonus +1 on all rolls (max still 6)",
            effect = SkillEffect.DICE_BONUS(1),
            xpReward = 75,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["MOMENTUM"] = SkillNode(
            id = "MOMENTUM",
            name = "Momentum",
            cost = 22,
            levelRequired = 45,
            tier = Tier.BEGINNER,
            category = SkillCategory.COMBAT_OFFENSIVE,
            description = "After flipping 3 cards in a row (consecutive turns), +2 seconds turn timer",
            effect = SkillEffect.STREAK_BONUS(3, 2),
            xpReward = 90,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Defense & Survival
        nodes["SHIELD_MASTERY"] = SkillNode(
            id = "SHIELD_MASTERY",
            name = "Shield Mastery",
            cost = 18,
            levelRequired = 30,
            tier = Tier.BEGINNER,
            category = SkillCategory.DEFENSE_SURVIVAL,
            description = "Guard ability can be used twice per match",
            effect = SkillEffect.ABILITY_USE_BOOST("GUARD", 1),
            xpReward = 60,
            trophyId = null,
            prerequisites = listOf("GUARD")
        )
        
        nodes["EVASION"] = AbilityNode(
            id = "EVASION",
            name = "Evasion",
            cost = 12,
            levelRequired = 36,
            tier = Tier.BEGINNER,
            category = SkillCategory.DEFENSE_SURVIVAL,
            description = "If opponent uses offensive ability, negate it",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.NEGATE_ABILITY,
            xpReward = 70,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["TACTICAL_RETREAT"] = AbilityNode(
            id = "TACTICAL_RETREAT",
            name = "Tactical Retreat",
            cost = 15,
            levelRequired = 48,
            tier = Tier.BEGINNER,
            category = SkillCategory.DEFENSE_SURVIVAL,
            description = "End round immediately (both players score current hands as-is)",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.END_ROUND_IMMEDIATELY,
            xpReward = 95,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Support & Tactical
        nodes["TEAM_STRATEGIST"] = SkillNode(
            id = "TEAM_STRATEGIST",
            name = "Team Strategist",
            cost = 15,
            levelRequired = 24,
            tier = Tier.BEGINNER,
            category = SkillCategory.SUPPORT_TACTICAL,
            description = "See how many of each card type (A, 2, 3, etc.) remain in deck",
            effect = SkillEffect.SEE_CARD_COUNTS,
            xpReward = 50,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["SUPPORTIVE_PRESENCE"] = SkillNode(
            id = "SUPPORTIVE_PRESENCE",
            name = "Supportive Presence",
            cost = 20,
            levelRequired = 40,
            tier = Tier.BEGINNER,
            category = SkillCategory.SUPPORT_TACTICAL,
            description = "+10% AP earned from matches",
            effect = SkillEffect.AP_BOOST(10),
            xpReward = 80,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["QUICK_HEALER"] = AbilityNode(
            id = "QUICK_HEALER",
            name = "Quick Healer",
            cost = 18,
            levelRequired = 50,
            tier = Tier.BEGINNER,
            category = SkillCategory.SUPPORT_TACTICAL,
            description = "Remove 1 face card penalty after round ends",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.REMOVE_PENALTY(1, listOf(CardRank.JACK, CardRank.QUEEN, CardRank.KING)),
            xpReward = 100,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Movement & Evasion
        nodes["DODGE"] = AbilityNode(
            id = "DODGE",
            name = "Dodge",
            cost = 10,
            levelRequired = 34,
            tier = Tier.BEGINNER,
            category = SkillCategory.MOVEMENT_EVASION,
            description = "Avoid next penalty (triggers automatically when penalty would occur)",
            usesPerMatch = 2,
            usesPerRound = 1,
            effect = AbilityEffect.AVOID_PENALTY(1),
            xpReward = 65,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Precision & Technique
        nodes["SNIPER_FOCUS"] = SkillNode(
            id = "SNIPER_FOCUS",
            name = "Sniper Focus",
            cost = 18,
            levelRequired = 38,
            tier = Tier.BEGINNER,
            category = SkillCategory.PRECISION_TECHNIQUE,
            description = "20% higher chance of drawing needed cards (stacks with Dead Eye for 30% total)",
            effect = SkillEffect.DRAW_BONUS(20),
            xpReward = 75,
            trophyId = null,
            prerequisites = listOf("DEAD_EYE")
        )
        
        // Mental & Special
        nodes["PSYCHIC_SHIELD"] = SkillNode(
            id = "PSYCHIC_SHIELD",
            name = "Psychic Shield",
            cost = 20,
            levelRequired = 54,
            tier = Tier.NOVICE,
            category = SkillCategory.MENTAL_SPECIAL,
            description = "Opponent cannot see which abilities you have unlocked",
            effect = SkillEffect.HIDE_ABILITIES,
            xpReward = 105,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Magic & Arcane
        nodes["ENCHANTMENT"] = AbilityNode(
            id = "ENCHANTMENT",
            name = "Enchantment",
            cost = 20,
            levelRequired = 44,
            tier = Tier.BEGINNER,
            category = SkillCategory.MAGIC_ARCANE,
            description = "Choose 1 card in hand, it scores double points",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.DOUBLE_POINTS(1),
            xpReward = 85,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // ========================================
        // TIER 3: NOVICE (Levels 51-80) - 15 items
        // ========================================
        
        // General Progression
        nodes["ADAPTIVE_STRATEGY"] = SkillNode(
            id = "ADAPTIVE_STRATEGY",
            name = "Adaptive Strategy",
            cost = 30,
            levelRequired = 55,
            tier = Tier.NOVICE,
            category = SkillCategory.GENERAL_PROGRESSION,
            description = "See opponent's last 3 discarded cards",
            effect = SkillEffect.SEE_OPPONENT_DISCARDS(3),
            xpReward = 110,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["ENDURANCE"] = SkillNode(
            id = "ENDURANCE",
            name = "Endurance",
            cost = 35,
            levelRequired = 65,
            tier = Tier.NOVICE,
            category = SkillCategory.GENERAL_PROGRESSION,
            description = "Queen penalty reduced from -2 AP to -1 AP",
            effect = SkillEffect.PENALTY_REDUCTION(CardRank.QUEEN, 1),
            xpReward = 130,
            trophyId = null,
            prerequisites = listOf("IRON_WILL")
        )
        
        // Combat & Offensive
        nodes["COMBO_MASTERY"] = SkillNode(
            id = "COMBO_MASTERY",
            name = "Combo Mastery",
            cost = 28,
            levelRequired = 58,
            tier = Tier.NOVICE,
            category = SkillCategory.COMBAT_OFFENSIVE,
            description = "If you place a card, you can flip another card in same turn",
            effect = SkillEffect.EXTRA_ACTION(ActionType.FLIP_CARD, 1),
            xpReward = 115,
            trophyId = null,
            prerequisites = listOf("PRECISION_STRIKE")
        )
        
        nodes["RUTHLESS_ASSAULT"] = AbilityNode(
            id = "RUTHLESS_ASSAULT",
            name = "Ruthless Assault",
            cost = 25,
            levelRequired = 62,
            tier = Tier.NOVICE,
            category = SkillCategory.COMBAT_OFFENSIVE,
            description = "Opponent's next draw is automatically discarded (they must draw again)",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.FORCE_REDRAW,
            xpReward = 125,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["CARD_MASTERY"] = SkillNode(
            id = "CARD_MASTERY",
            name = "Card Mastery",
            cost = 35,
            levelRequired = 70,
            tier = Tier.NOVICE,
            category = SkillCategory.COMBAT_OFFENSIVE,
            description = "All face-up numbered cards (A-10) score +1 point",
            effect = SkillEffect.POINT_BOOST(CardType.NUMBERED, 1),
            xpReward = 140,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["ARCHERY"] = SkillNode(
            id = "ARCHERY",
            name = "Archery",
            cost = 40,
            levelRequired = 75,
            tier = Tier.NOVICE,
            category = SkillCategory.COMBAT_OFFENSIVE,
            description = "When placing Ace, 2, or 3, see opponent's matching slot",
            effect = SkillEffect.SEE_OPPONENT_SLOT(listOf(1, 2, 3)),
            xpReward = 150,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Defense & Survival
        nodes["REGENERATIVE_FOCUS"] = SkillNode(
            id = "REGENERATIVE_FOCUS",
            name = "Regenerative Focus",
            cost = 25,
            levelRequired = 53,
            tier = Tier.NOVICE,
            category = SkillCategory.DEFENSE_SURVIVAL,
            description = "Regain 1 AP per round if you flip at least 2 cards",
            effect = SkillEffect.AP_REGENERATION(2, 1),
            xpReward = 105,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["SHIELD_WALL"] = AbilityNode(
            id = "SHIELD_WALL",
            name = "Shield Wall",
            cost = 25,
            levelRequired = 68,
            tier = Tier.NOVICE,
            category = SkillCategory.DEFENSE_SURVIVAL,
            description = "ALL face-down cards immune to penalties this round",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.IMMUNITY_ROUND,
            xpReward = 135,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["STEADFAST"] = SkillNode(
            id = "STEADFAST",
            name = "Steadfast",
            cost = 35,
            levelRequired = 78,
            tier = Tier.NOVICE,
            category = SkillCategory.DEFENSE_SURVIVAL,
            description = "Cannot be affected by opponent's offensive abilities",
            effect = SkillEffect.IMMUNITY(ImmunityType.OFFENSIVE_ABILITIES),
            xpReward = 155,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Support & Tactical
        nodes["BATTLEFIELD_AWARENESS"] = SkillNode(
            id = "BATTLEFIELD_AWARENESS",
            name = "Battlefield Awareness",
            cost = 28,
            levelRequired = 60,
            tier = Tier.NOVICE,
            category = SkillCategory.SUPPORT_TACTICAL,
            description = "See when opponent draws from deck vs. discard pile",
            effect = SkillEffect.SEE_DRAW_SOURCE,
            xpReward = 120,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["TACTICAL_PLANNING"] = SkillNode(
            id = "TACTICAL_PLANNING",
            name = "Tactical Planning",
            cost = 32,
            levelRequired = 72,
            tier = Tier.NOVICE,
            category = SkillCategory.SUPPORT_TACTICAL,
            description = "At start of round, see your entire hand face-up for 3 seconds, then cards flip face-down",
            effect = SkillEffect.PEEK_HAND(3),
            xpReward = 145,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Movement & Evasion
        nodes["ACROBATICS"] = AbilityNode(
            id = "ACROBATICS",
            name = "Acrobatics",
            cost = 15,
            levelRequired = 64,
            tier = Tier.NOVICE,
            category = SkillCategory.MOVEMENT_EVASION,
            description = "Place a card in wrong slot (e.g., place 5 in slot 7) but it still counts",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.WRONG_SLOT_PLACEMENT,
            xpReward = 130,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Precision & Technique
        nodes["AIM_STABILITY"] = SkillNode(
            id = "AIM_STABILITY",
            name = "Aim Stability",
            cost = 25,
            levelRequired = 52,
            tier = Tier.NOVICE,
            category = SkillCategory.PRECISION_TECHNIQUE,
            description = "Never draw Joker unless you specifically want it",
            effect = SkillEffect.AVOID_JOKER,
            xpReward = 105,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        nodes["CHAIN_COMBO"] = SkillNode(
            id = "CHAIN_COMBO",
            name = "Chain Combo",
            cost = 30,
            levelRequired = 74,
            tier = Tier.NOVICE,
            category = SkillCategory.PRECISION_TECHNIQUE,
            description = "If you place 3+ cards in one round, +3 dice multiplier on that round's dice roll",
            effect = SkillEffect.STREAK_DICE_BONUS(3, 3),
            xpReward = 150,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Mental & Special
        nodes["INSIGHT"] = SkillNode(
            id = "INSIGHT",
            name = "Insight",
            cost = 30,
            levelRequired = 76,
            tier = Tier.NOVICE,
            category = SkillCategory.MENTAL_SPECIAL,
            description = "At round start, see 1 random opponent card (face-up view for 3 seconds)",
            effect = SkillEffect.PEEK_OPPONENT_CARD(1, 3),
            xpReward = 155,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Magic & Arcane
        nodes["CURSE_BREAK"] = AbilityNode(
            id = "CURSE_BREAK",
            name = "Curse Break",
            cost = 25,
            levelRequired = 56,
            tier = Tier.NOVICE,
            category = SkillCategory.MAGIC_ARCANE,
            description = "Remove all current penalties before they apply (at round end)",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.REMOVE_ALL_PENALTIES,
            xpReward = 115,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        // Power & Strength
        nodes["CRUSHING_BLOW"] = AbilityNode(
            id = "CRUSHING_BLOW",
            name = "Crushing Blow",
            cost = 25,
            levelRequired = 66,
            tier = Tier.NOVICE,
            category = SkillCategory.POWER_STRENGTH,
            description = "Opponent's next penalty is doubled",
            usesPerMatch = 1,
            usesPerRound = 1,
            effect = AbilityEffect.DOUBLE_PENALTY,
            xpReward = 135,
            trophyId = null,
            prerequisites = emptyList()
        )
        
        return nodes
    }
}

/**
 * Tier definitions for skill/ability progression
 */
enum class Tier {
    NEWBIE,      // Levels 1-20
    BEGINNER,    // Levels 21-50
    NOVICE,      // Levels 51-80
    INTERMEDIATE,// Levels 81-110
    HARD,        // Levels 111-140
    EXPERT,      // Levels 141-170
    MASTER       // Levels 171-200
}

/**
 * Categories for organizing skills and abilities
 */
enum class SkillCategory {
    GENERAL_PROGRESSION,
    COMBAT_OFFENSIVE,
    DEFENSE_SURVIVAL,
    SUPPORT_TACTICAL,
    MAGIC_ARCANE,
    MOVEMENT_EVASION,
    PRECISION_TECHNIQUE,
    POWER_STRENGTH,
    MENTAL_SPECIAL,
    ADVANCED_MASTER
}

/**
 * Card rank for penalty effects
 */
enum class CardRank {
    JACK,
    QUEEN,
    KING,
    JOKER
}

/**
 * Card type for scoring effects
 */
enum class CardType {
    NUMBERED,  // A-10
    FACE_CARD, // J, Q, K
    WILD_CARD  // Joker
}

/**
 * Action type for extra action effects
 */
enum class ActionType {
    DRAW_CARD,
    PLACE_CARD,
    FLIP_CARD,
    DISCARD_CARD
}

/**
 * Immunity type for protection effects
 */
enum class ImmunityType {
    DISTRACTION,
    OFFENSIVE_ABILITIES,
    PENALTIES,
    ALL_EFFECTS
}

/**
 * Skill effect types
 */
sealed class SkillEffect {
    data class XP_BOOST(val percentage: Int) : SkillEffect()
    data class TIMER_BOOST(val seconds: Int) : SkillEffect()
    data class PEEK_DECK(val cards: Int) : SkillEffect()
    data class SP_BOOST(val percentage: Int) : SkillEffect()
    data class AP_BOOST(val percentage: Int) : SkillEffect()
    data class PENALTY_REDUCTION(val cardRank: CardRank, val amount: Int) : SkillEffect()
    data class DICE_BONUS(val bonus: Int) : SkillEffect()
    data class STREAK_BONUS(val streak: Int, val seconds: Int) : SkillEffect()
    data class POINT_BOOST(val cardType: CardType, val amount: Int) : SkillEffect()
    data class DRAW_BONUS(val percentage: Int) : SkillEffect()
    data class EXTRA_ACTION(val actionType: ActionType, val count: Int) : SkillEffect()
    data class STREAK_DICE_BONUS(val cardsPlaced: Int, val bonus: Int) : SkillEffect()
    data class AP_REGENERATION(val minFlips: Int, val apGained: Int) : SkillEffect()
    data class ABILITY_USE_BOOST(val abilityId: String, val extraUses: Int) : SkillEffect()
    
    object SEE_MATCH_HISTORY : SkillEffect()
    object SEE_OPPONENT_HAND_SIZE : SkillEffect()
    object SEE_CARD_COUNTS : SkillEffect()
    object SEE_OPPONENT_DISCARDS : SkillEffect()
    object SEE_DRAW_SOURCE : SkillEffect()
    object PEEK_HAND : SkillEffect()
    object PEEK_OPPONENT_CARD : SkillEffect()
    object HIDE_ABILITIES : SkillEffect()
    object SEE_OPPONENT_SLOT : SkillEffect()
    object IMMUNITY : SkillEffect()
    object AVOID_JOKER : SkillEffect()
    
    data class Immunity(val immunityType: ImmunityType) : SkillEffect()
    data class SeeOpponentDiscards(val count: Int) : SkillEffect()
    data class SeeOpponentSlot(val slots: List<Int>) : SkillEffect()
    data class PeekOpponentCard(val count: Int, val seconds: Int) : SkillEffect()
}

/**
 * Ability effect types
 */
sealed class AbilityEffect {
    data class PEEK_DECK(val cards: Int) : AbilityEffect()
    data class REROLL_DICE(val maxResult: Int = 6) : AbilityEffect()
    data class PROTECT_CARD(val count: Int) : AbilityEffect()
    data class FORCE_DISCARD(val count: Int) : AbilityEffect()
    data class BONUS_POINTS(val points: Int) : AbilityEffect()
    data class REMOVE_PENALTY(val count: Int, val ranks: List<CardRank>) : AbilityEffect()
    data class DOUBLE_POINTS(val count: Int) : AbilityEffect()
    data class EXTRA_ACTION(val actionType: ActionType, val count: Int) : AbilityEffect()
    data class FLIP_CARDS(val count: Int) : AbilityEffect()
    
    object SKIP_DRAW_PHASE : AbilityEffect()
    object NEGATE_ABILITY : AbilityEffect()
    object END_ROUND_IMMEDIATELY : AbilityEffect()
    object SKIP_ANIMATIONS : AbilityEffect()
    object AVOID_PENALTY : AbilityEffect()
    object FORCE_REDRAW : AbilityEffect()
    object IMMUNITY_ROUND : AbilityEffect()
    object WRONG_SLOT_PLACEMENT : AbilityEffect()
    object REMOVE_ALL_PENALTIES : AbilityEffect()
    object FREE_ABILITY : AbilityEffect()
    object DOUBLE_PENALTY : AbilityEffect()
    
    // Intermediate/Hard effects (placeholders for now)
    data class DRAW_MULTIPLE(val count: Int) : AbilityEffect()
    data class PLACE_MULTIPLE(val count: Int) : AbilityEffect()
    object UNLIMITED_TIMER : AbilityEffect()
    data class PIERCING_SHOT(val cardRank: CardRank) : AbilityEffect()
    data class MASS_FLIP(val count: Int) : AbilityEffect()
    
    // Expert/Master effects (placeholders for now)
    data class DISCARD_CHOICE(val count: Int) : AbilityEffect()
    object FREEZE_TURN : AbilityEffect()
    object INSTANT_FLIP_ALL : AbilityEffect()
    data class COMBINED_EFFECT(val effects: List<AbilityEffect>) : AbilityEffect()
    object TELEPORTATION : AbilityEffect()
    data class SWAP_CARDS(val card1: Int, val card2: Int) : AbilityEffect()
    data class TIME_WARP(val turns: Int) : AbilityEffect()
    data class SUIT_STACK(val suits: List<String>) : AbilityEffect()
    object SHADOW_STEP : AbilityEffect()
    object BLINK : AbilityEffect()
    object LAST_STAND : AbilityEffect()
    object MAGIC_WARD : AbilityEffect()
    object MAGIC_RESISTANCE : AbilityEffect()
    object PROTECTIVE_AURA : AbilityEffect()
    object PROTECTIVE_BUBBLE : AbilityEffect()
    object SWIFT_RECOVERY : AbilityEffect()
    object RALLY_CRY : AbilityEffect()
    object INSPIRE_ALLIES : AbilityEffect()
    object CLEANSE : AbilityEffect()
    object ARCANE_SHIELD : AbilityEffect()
    object DIPLOMACY : AbilityEffect()
    object PARKOUR : AbilityEffect()
    object LEAP : AbilityEffect()
    object EVASIVE_ROLL : AbilityEffect()
    object PERFECT_TIMING : AbilityEffect()
    object REFLEX_BOOST : AbilityEffect()
    object PRECISION_AIM : AbilityEffect()
    object MASTER_TACTICIAN : AbilityEffect()
    object TITAN_STRENGTH : AbilityEffect()
    object EARTH_SHATTER : AbilityEffect()
    object MIGHTY_ROAR : AbilityEffect()
    object TITANS_WRATH : AbilityEffect()
    object DREAM_WALK : AbilityEffect()
    object MEMORY_RECALL : AbilityEffect()
    object COSMIC_INSIGHT : AbilityEffect()
    object ETERNAL_CHAMPION : AbilityEffect()
    object APEX_STRATEGIST : AbilityEffect()
    object LEGENDARY_FOCUS : AbilityEffect()
    object SUPREME_EFFICIENCY : AbilityEffect()
    object UNBREAKABLE_WILL : AbilityEffect()
    object ULTIMATE_MASTERY : AbilityEffect()
    object SYNERGY_MASTER : AbilityEffect()
    object PHOENIX_DIVE : AbilityEffect()
    object DRAGON_STRIKE : AbilityEffect()
    object QUANTUM_BURST : AbilityEffect()
    object ECHO_STORM : AbilityEffect()
    object ULTIMATE_CHARGE : AbilityEffect()
    object PHASE_SHIFT : AbilityEffect()
    object SPIRIT_SUMMON : AbilityEffect()
    object ELEMENTAL_STORM : AbilityEffect()
}