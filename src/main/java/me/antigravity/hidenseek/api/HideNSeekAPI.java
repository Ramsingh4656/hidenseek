package me.antigravity.hidenseek.api;

import me.antigravity.hidenseek.arena.ArenaManager;
import me.antigravity.hidenseek.bazooka.BazookaManager;
import me.antigravity.hidenseek.player.PlayerManager;

/**
 * API interface exposing HideNSeek core managers for external integration.
 */
public interface HideNSeekAPI {

    /**
     * Gets the active ArenaManager.
     */
    ArenaManager getArenaManager();

    /**
     * Gets the active PlayerManager.
     */
    PlayerManager getPlayerManager();

    /**
     * Gets the active BazookaManager.
     */
    BazookaManager getBazookaManager();
}
