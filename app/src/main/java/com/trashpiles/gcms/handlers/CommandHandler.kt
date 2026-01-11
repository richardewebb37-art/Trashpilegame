package com.trashpiles.gcms.handlers

import com.trashpiles.gcms.GCMSCommand
import com.trashpiles.gcms.GCMSState

/**
 * Base interface for all command handlers
 * 
 * Command handlers are responsible for executing specific types of commands
 * and returning the updated state along with any events that should be emitted.
 */
interface CommandHandler {
    /**
     * Execute a command and return the updated state with events
     * 
     * @param command The command to execute
     * @param currentState The current game state
     * @return Pair of (updated state, list of events to emit)
     */
    suspend fun handle(command: GCMSCommand, currentState: GCMSState): CommandResult
}

/**
 * Result of command execution
 * 
 * @param state The updated game state after command execution
 * @param events List of events to emit as a result of the command
 */
data class CommandResult(
    val state: GCMSState,
    val events: List<com.trashpiles.gcms.GCMSEvent>
)