package me.antigravity.hidenseek.arena;

/**
 * Represents the current state of an Arena session.
 */
public enum ArenaState {
    /**
     * Waiting for players to join.
     */
    WAITING,

    /**
     * Players are in lobby and countdown is running.
     */
    STARTING,

    /**
     * Game is currently in progress.
     */
    IN_GAME,

    /**
     * Arena is disabled (admin setup, reload, etc.).
     */
    DISABLED
}
