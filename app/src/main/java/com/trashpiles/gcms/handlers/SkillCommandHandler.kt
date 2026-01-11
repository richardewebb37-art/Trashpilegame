package com.trashpiles.gcms.handlers

import com.trashpiles.gcms.*

/**
 * Handler for skill and ability commands
 * 
 * Handles:
 * - UnlockNodeCommand
 * - UseAbilityCommand
 */
class SkillCommandHandler : CommandHandler {
    
    override suspend fun handle(command: GCMSCommand, currentState: GCMSState): CommandResult {
        return when (command) {
            is GCMSCommand.UnlockNodeCommand -> handleUnlockNode(command, currentState)
            is GCMSCommand.UseAbilityCommand -> handleUseAbility(command, currentState)
            else -> throw IllegalArgumentException(
                "SkillCommandHandler cannot handle ${command::class.simpleName}")
        }
    }
    
    private fun handleUnlockNode(command: GCMSCommand.UnlockNodeCommand, state: GCMSState): CommandResult {
        // Convert string point type to enum
        val pointType = when (command.pointType) {
            "SKILL" -> PointType.SKILL
            "ABILITY" -> PointType.ABILITY
            else -> {
                return CommandResult(state, listOf(
                    GCMSEvent.InvalidMoveEvent(
                        reason = "Invalid point type: ${command.pointType}",
                        attemptedAction = "UnlockNode"
                    )
                ))
            }
        }
        
        // Unlock the node
        val result = SkillAbilityLogic.unlockNode(
            state = state,
            playerId = command.playerId,
            nodeId = command.nodeId,
            pointType = pointType
        )
        
        if (!result.success) {
            return CommandResult(state, listOf(
                GCMSEvent.InvalidMoveEvent(
                    reason = result.message,
                    attemptedAction = "UnlockNode: ${command.nodeId}"
                )
            ))
        }
        
        val events = listOf(
            GCMSEvent.NodeUnlockedEvent(
                playerId = command.playerId,
                nodeId = command.nodeId,
                nodeName = result.nodeName ?: "",
                pointType = command.pointType,
                pointsSpent = result.pointsSpent ?: 0
            )
        )
        
        return CommandResult(state, events)
    }
    
    private fun handleUseAbility(command: GCMSCommand.UseAbilityCommand, state: GCMSState): CommandResult {
        // Convert string map to Any map
        val targetData = command.targetData?.mapValues { it.value as Any }
        
        // Use the ability
        val result = SkillAbilityLogic.useAbility(
            state = state,
            playerId = command.playerId,
            abilityId = command.abilityId,
            targetData = targetData
        )
        
        if (!result.success) {
            return CommandResult(state, listOf(
                GCMSEvent.InvalidMoveEvent(
                    reason = result.message,
                    attemptedAction = "UseAbility: ${command.abilityId}"
                )
            ))
        }
        
        val events = listOf(
            GCMSEvent.AbilityUsedEvent(
                playerId = command.playerId,
                abilityId = command.abilityId,
                abilityName = result.abilityName ?: "",
                effectDescription = result.message
            )
        )
        
        return CommandResult(state, events)
    }
}