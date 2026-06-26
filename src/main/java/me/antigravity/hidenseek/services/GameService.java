package me.antigravity.hidenseek.services;

import me.antigravity.hidenseek.HideNSeek;
import me.antigravity.hidenseek.arena.Arena;
import me.antigravity.hidenseek.game.GameSession;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service class handling registration, management, and lookups of live Game Sessions.
 */
public class GameService {

    private final HideNSeek plugin;
    private final Map<String, GameSession> sessions = new HashMap<>();

    public GameService(HideNSeek plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers a new GameSession for an Arena.
     */
    public void registerSession(Arena arena) {
        sessions.put(arena.getName().toLowerCase(), new GameSession(plugin, arena));
    }

    /**
     * Removes an registered GameSession.
     */
    public void removeSession(String arenaName) {
        sessions.remove(arenaName.toLowerCase());
    }

    /**
     * Gets a GameSession by arena name.
     */
    public GameSession getSession(String arenaName) {
        if (arenaName == null) return null;
        return sessions.get(arenaName.toLowerCase());
    }

    /**
     * Resolves which GameSession a player is currently in.
     */
    public GameSession getPlayerSession(Player player) {
        UUID uuid = player.getUniqueId();
        for (GameSession session : sessions.values()) {
            if (session.getPlayers().contains(uuid)) {
                return session;
            }
        }
        return null;
    }

    /**
     * Checks if a player is in any active game session.
     */
    public boolean isPlayerInGame(Player player) {
        return getPlayerSession(player) != null;
    }

    /**
     * Gets all active game sessions.
     */
    public Collection<GameSession> getSessions() {
        return sessions.values();
    }

    /**
     * Stops all active match sessions and clears the registry.
     */
    public void stopAll() {
        for (GameSession session : sessions.values()) {
            session.stop();
        }
        sessions.clear();
    }
}
