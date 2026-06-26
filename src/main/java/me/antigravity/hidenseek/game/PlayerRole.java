package me.antigravity.hidenseek.game;

/**
 * Represents the game role of a player within an active match.
 */
public enum PlayerRole {
    /**
     * Currently in the lobby waiting for the game to start.
     */
    LOBBY,

    /**
     * Hiding player. Tiny size.
     */
    HIDER,

    /**
     * Seeker player. Regular size, carries the Bazooka.
     */
    SEEKER
}
