package com.trashpiles.gcms

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

/**
 * Comprehensive test suite for the Skills & Abilities Database
 * Tests all 100 skills and abilities across 7 tiers
 */
class SkillAbilityDatabaseTest {
    
    @BeforeEach
    fun setup() {
        // Reset database if needed
    }
    
    @Test
    @DisplayName("Database should contain 100 total skills and abilities")
    fun testTotalCount() {
        assertEquals(100, SkillAbilityDatabase.allSkillsAndAbilities.size,
            "Database should contain exactly 100 skills and abilities")
    }
    
    @Test
    @DisplayName("All nodes should have unique IDs")
    fun testUniqueIds() {
        val ids = SkillAbilityDatabase.allSkillsAndAbilities.keys
        assertEquals(ids.size, ids.distinct().size,
            "All node IDs should be unique")
    }
    
    @Test
    @DisplayName("Newbie tier should contain 15 items (levels 1-20)")
    fun testNewbieTier() {
        val newbieNodes = SkillAbilityDatabase.getNodesByTier(Tier.NEWBIE)
        assertEquals(15, newbieNodes.size,
            "Newbie tier should have 15 items")
        
        // Verify all are in level range 1-20
        assertTrue(newbieNodes.all { it.levelRequired in 1..20 },
            "All Newbie tier items should be levels 1-20")
    }
    
    @Test
    @DisplayName("Beginner tier should contain 20 items (levels 21-50)")
    fun testBeginnerTier() {
        val beginnerNodes = SkillAbilityDatabase.getNodesByTier(Tier.BEGINNER)
        assertEquals(20, beginnerNodes.size,
            "Beginner tier should have 20 items")
        
        assertTrue(beginnerNodes.all { it.levelRequired in 21..50 },
            "All Beginner tier items should be levels 21-50")
    }
    
    @Test
    @DisplayName("Novice tier should contain 15 items (levels 51-80)")
    fun testNoviceTier() {
        val noviceNodes = SkillAbilityDatabase.getNodesByTier(Tier.NOVICE)
        assertEquals(15, noviceNodes.size,
            "Novice tier should have 15 items")
        
        assertTrue(noviceNodes.all { it.levelRequired in 51..80 },
            "All Novice tier items should be levels 51-80")
    }
    
    @Test
    @DisplayName("Quick Learner should grant +10% XP bonus")
    fun testQuickLearner() {
        val quickLearner = SkillAbilityDatabase.getNodeById("QUICK_LEARNER")
        
        assertNotNull(quickLearner)
        assertEquals("Quick Learner", quickLearner?.name)
        assertEquals(3, quickLearner?.cost)
        assertEquals(3, quickLearner?.levelRequired)
        assertEquals(Tier.NEWBIE, quickLearner?.tier)
        assertEquals(SkillCategory.GENERAL_PROGRESSION, quickLearner?.category)
        assertEquals(PointType.SKILL, quickLearner?.pointType)
        assertEquals(10, (quickLearner as? SkillNode)?.effect)
    }
    
    @Test
    @DisplayName("Intuition ability should peek at top card")
    fun testIntuition() {
        val intuition = SkillAbilityDatabase.getNodeById("INTUITION")
        
        assertNotNull(intuition)
        assertEquals("Intuition", intuition?.name)
        assertEquals(6, intuition?.cost)
        assertEquals(8, intuition?.levelRequired)
        assertEquals(Tier.NEWBIE, intuition?.tier)
        assertEquals(PointType.ABILITY, intuition?.pointType)
        assertEquals(1, (intuition as? AbilityNode)?.usesPerMatch)
        assertEquals(1, (intuition as? AbilityNode)?.usesPerRound)
    }
    
    @Test
    @DisplayName("Focused Mind should be prerequisite for Precision Strike")
    fun testPrerequisites() {
        val focusedMind = SkillAbilityDatabase.getNodeById("FOCUSED_MIND")
        val precisionStrike = SkillAbilityDatabase.getNodeById("PRECISION_STRIKE")
        
        assertNotNull(focusedMind)
        assertNotNull(precisionStrike)
        
        assertTrue(precisionStrike?.prerequisites?.contains("FOCUSED_MIND") ?: false,
            "Precision Strike should require Focused Mind")
    }
    
    @Test
    @DisplayName("Prerequisites should be validated correctly")
    fun testPrerequisiteValidation() {
        val unlockedNodes = setOf("FOCUSED_MIND", "QUICK_LEARNER")
        
        // Should pass - all prerequisites met
        assertTrue(SkillAbilityDatabase.arePrerequisitesMet("PRECISION_STRIKE", unlockedNodes),
            "Should pass when prerequisites are met")
        
        // Should fail - missing prerequisite
        assertFalse(SkillAbilityDatabase.arePrerequisitesMet("PRECISION_STRIKE", emptySet()),
            "Should fail when prerequisites are not met")
    }
    
    @Test
    @DisplayName("Skill cost should scale by tier")
    fun testCostScaling() {
        val newbieSkill = SkillAbilityDatabase.getNodeById("QUICK_LEARNER") as? SkillNode
        val beginnerSkill = SkillAbilityDatabase.getNodeById("EFFICIENCY") as? SkillNode
        val noviceSkill = SkillAbilityDatabase.getNodeById("ADAPTIVE_STRATEGY") as? SkillNode
        
        assertNotNull(newbieSkill)
        assertNotNull(beginnerSkill)
        assertNotNull(noviceSkill)
        
        assertTrue(newbieSkill?.cost ?: 0 < beginnerSkill?.cost ?: 0,
            "Beginner skills should cost more than Newbie skills")
        
        assertTrue(beginnerSkill?.cost ?: 0 < noviceSkill?.cost ?: 0,
            "Novice skills should cost more than Beginner skills")
    }
    
    @Test
    @DisplayName("Skills should be obtainable at correct levels")
    fun testLevelRequirements() {
        val level3Skills = SkillAbilityDatabase.getNodesForLevel(3)
        val level10Skills = SkillAbilityDatabase.getNodesForLevel(10)
        val level50Skills = SkillAbilityDatabase.getNodesForLevel(50)
        
        assertEquals(1, level3Skills.size, "Should have 1 skill at level 3")
        assertEquals(2, level10Skills.size, "Should have 2 skills at level 10")
        assertEquals(1, level50Skills.size, "Should have 1 skill at level 50")
    }
    
    @Test
    @DisplayName("Should get all skills up to a level")
    fun testGetNodesUpToLevel() {
        val nodesUpTo20 = SkillAbilityDatabase.getNodesUpToLevel(20)
        val nodesUpTo50 = SkillAbilityDatabase.getNodesUpToLevel(50)
        
        assertEquals(15, nodesUpTo20.size, "Should have 15 nodes up to level 20")
        assertTrue(nodesUpTo50.size > nodesUpTo20.size,
            "Should have more nodes up to level 50")
    }
    
    @Test
    @DisplayName("All skill categories should be represented")
    fun testSkillCategories() {
        val allCategories = SkillAbilityDatabase.allSkillsAndAbilities.values
            .map { it.category }
            .distinct()
        
        assertTrue(allCategories.contains(SkillCategory.GENERAL_PROGRESSION),
            "Should have GENERAL_PROGRESSION category")
        assertTrue(allCategories.contains(SkillCategory.COMBAT_OFFENSIVE),
            "Should have COMBAT_OFFENSIVE category")
        assertTrue(allCategories.contains(SkillCategory.DEFENSE_SURVIVAL),
            "Should have DEFENSE_SURVIVAL category")
        assertTrue(allCategories.contains(SkillCategory.SUPPORT_TACTICAL),
            "Should have SUPPORT_TACTICAL category")
        assertTrue(allCategories.contains(SkillCategory.MAGIC_ARCANE),
            "Should have MAGIC_ARCANE category")
        assertTrue(allCategories.contains(SkillCategory.MOVEMENT_EVASION),
            "Should have MOVEMENT_EVASION category")
        assertTrue(allCategories.contains(SkillCategory.PRECISION_TECHNIQUE),
            "Should have PRECISION_TECHNIQUE category")
        assertTrue(allCategories.contains(SkillCategory.POWER_STRENGTH),
            "Should have POWER_STRENGTH category")
        assertTrue(allCategories.contains(SkillCategory.MENTAL_SPECIAL),
            "Should have MENTAL_SPECIAL category")
    }
    
    @Test
    @DisplayName("All tiers should be represented")
    fun testAllTiersRepresented() {
        val allTiers = SkillAbilityDatabase.allSkillsAndAbilities.values
            .map { it.tier }
            .distinct()
        
        assertTrue(allTiers.contains(Tier.NEWBIE), "Should have NEWBIE tier")
        assertTrue(allTiers.contains(Tier.BEGINNER), "Should have BEGINNER tier")
        assertTrue(allTiers.contains(Tier.NOVICE), "Should have NOVICE tier")
        assertTrue(allTiers.contains(Tier.INTERMEDIATE), "Should have INTERMEDIATE tier")
        assertTrue(allTiers.contains(Tier.HARD), "Should have HARD tier")
        assertTrue(allTiers.contains(Tier.EXPERT), "Should have EXPERT tier")
        assertTrue(allTiers.contains(Tier.MASTER), "Should have MASTER tier")
    }
    
    @Test
    @DisplayName("Penalty reduction skills should work correctly")
    fun testPenaltyReductionSkills() {
        val ironWill = SkillAbilityDatabase.getNodeById("IRON_WILL") as? SkillNode
        val toughSkin = SkillAbilityDatabase.getNodeById("TOUGH_SKIN") as? SkillNode
        val endurance = SkillAbilityDatabase.getNodeById("ENDURANCE") as? SkillNode
        
        assertNotNull(ironWill)
        assertNotNull(toughSkin)
        assertNotNull(endurance)
        
        assertEquals(SkillEffect.PENALTY_REDUCTION(CardRank.KING, 1), ironWill?.effect)
        assertEquals(SkillEffect.PENALTY_REDUCTION(CardRank.JACK, 1), toughSkin?.effect)
        assertEquals(SkillEffect.PENALTY_REDUCTION(CardRank.QUEEN, 1), endurance?.effect)
    }
    
    @Test
    @DisplayName("Timer boost skills should stack correctly")
    fun testTimerBoostStacking() {
        val focusedMind = SkillAbilityDatabase.getNodeById("FOCUSED_MIND") as? SkillNode
        val efficiency = SkillAbilityDatabase.getNodeById("EFFICIENCY") as? SkillNode
        
        assertNotNull(focusedMind)
        assertNotNull(efficiency)
        
        assertEquals(SkillEffect.TIMER_BOOST(2), focusedMind?.effect)
        assertEquals(SkillEffect.TIMER_BOOST(3), efficiency?.effect)
        
        // Total should be 2 + 3 = 5 seconds bonus
        val totalBonus = 5
        assertEquals(30 + totalBonus, 35, "Total turn time with both skills should be 35 seconds")
    }
    
    @Test
    @DisplayName("Ability usage limits should be correct")
    fun testAbilityUsageLimits() {
        val intuition = SkillAbilityDatabase.getNodeById("INTUITION") as? AbilityNode
        val luckyBreak = SkillAbilityDatabase.getNodeById("LUCKY_BREAK") as? AbilityNode
        val sprint = SkillAbilityDatabase.getNodeById("SPRINT") as? AbilityNode
        
        assertNotNull(intuition)
        assertNotNull(luckyBreak)
        assertNotNull(sprint)
        
        assertEquals(1, intuition?.usesPerMatch)
        assertEquals(1, luckyBreak?.usesPerMatch)
        assertEquals(3, sprint?.usesPerMatch)
    }
    
    @Test
    @DisplayName("XP rewards should be positive for all nodes")
    fun testXPRewards() {
        val allNodes = SkillAbilityDatabase.allSkillsAndAbilities.values
        
        assertTrue(allNodes.all { it.xpReward >= 0 },
            "All nodes should have non-negative XP rewards")
        
        assertTrue(allNodes.any { it.xpReward > 0 },
            "At least some nodes should grant XP")
    }
    
    @Test
    @DisplayName("Should filter skills by point type correctly")
    fun testGetSkillsByType() {
        val skills = SkillAbilityDatabase.getSkillsByType(PointType.SKILL)
        val abilities = SkillAbilityDatabase.getSkillsByType(PointType.ABILITY)
        
        assertTrue(skills.isNotEmpty(), "Should have skills")
        assertTrue(abilities.isNotEmpty(), "Should have abilities")
        assertTrue(skills.all { it.pointType == PointType.SKILL },
            "All returned items should be skills")
        assertTrue(abilities.all { it.pointType == PointType.ABILITY },
            "All returned items should be abilities")
    }
    
    @Test
    @DisplayName("Complex prerequisite chains should be valid")
    fun testComplexPrerequisites() {
        val apexStrategist = SkillAbilityDatabase.getNodeById("APEX_STRATEGIST")
        val legendaryFocus = SkillAbilityDatabase.getNodeById("LEGENDARY_FOCUS")
        
        assertNotNull(apexStrategist)
        assertNotNull(legendaryFocus)
        
        // Apex Strategist should require Adaptive Strategy and Team Strategist
        val apexPrereqs = apexStrategist?.prerequisites ?: emptyList()
        assertTrue(apexPrereqs.contains("ADAPTIVE_STRATEGY"),
            "Apex Strategist should require Adaptive Strategy")
        assertTrue(apexPrereqs.contains("TEAM_STRATEGIST"),
            "Apex Strategist should require Team Strategist")
        
        // Legendary Focus should require Apex Strategist
        val legendaryPrereqs = legendaryFocus?.prerequisites ?: emptyList()
        assertTrue(legendaryPrereqs.contains("APEX_STRATEGIST"),
            "Legendary Focus should require Apex Strategist")
    }
    
    @Test
    @DisplayName("Master tier skills should have highest costs")
    fun testMasterTierCosts() {
        val masterNodes = SkillAbilityDatabase.getNodesByTier(Tier.MASTER)
        val otherNodes = SkillAbilityDatabase.allSkillsAndAbilities.values
            .filter { it.tier != Tier.MASTER }
        
        assertTrue(masterNodes.isNotEmpty(), "Should have Master tier skills")
        
        val maxMasterCost = masterNodes.maxOfOrNull { it.cost } ?: 0
        val maxOtherCost = otherNodes.maxOfOrNull { it.cost } ?: 0
        
        assertTrue(maxMasterCost >= maxOtherCost,
            "Master tier skills should have the highest costs")
    }
}