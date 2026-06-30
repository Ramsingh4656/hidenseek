package me.antigravity.hidenseek.arena;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a HideNSeek Arena.
 */
public class Arena {

    private final String name;
    private Location lobbySpawn;
    private Location seekerSpawn;
    private final List<Location> hiderSpawns = new ArrayList<>();
    
    // The single manual region boundary for match play
    private Location pos1;
    private Location pos2;

    private int minPlayers = 2;
    private int maxPlayers = 20;
    private int autoStartPlayers = 8;
    private int timer = 300; // Match duration in seconds
    
    private boolean enabled = false;
    private ArenaState state = ArenaState.DISABLED;

    public Arena(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public Location getSeekerSpawn() {
        return seekerSpawn;
    }

    public void setSeekerSpawn(Location seekerSpawn) {
        this.seekerSpawn = seekerSpawn;
    }

    public List<Location> getHiderSpawns() {
        return hiderSpawns;
    }

    public void addHiderSpawn(Location loc) {
        if (loc != null) {
            hiderSpawns.add(loc);
        }
    }

    public void clearHiderSpawns() {
        hiderSpawns.clear();
    }

    // Arena Boundary positions getters/setters
    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getAutoStartPlayers() {
        return autoStartPlayers;
    }

    public void setAutoStartPlayers(int autoStartPlayers) {
        this.autoStartPlayers = autoStartPlayers;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled && isConfigured()) {
            this.state = ArenaState.WAITING;
        } else {
            this.state = ArenaState.DISABLED;
            this.enabled = false;
        }
    }

    public ArenaState getState() {
        return state;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }

    /**
     * Identifies all missing elements required to complete setup.
     */
    public List<String> getMissingSetupElements() {
        List<String> missing = new ArrayList<>();
        if (lobbySpawn == null) missing.add("Lobby Spawn");
        if (seekerSpawn == null) missing.add("Seeker Spawn");
        if (hiderSpawns.isEmpty()) missing.add("Hider Spawn");
        if (pos1 == null || pos2 == null) missing.add("Arena Boundary");
        return missing;
    }

    /**
     * Checks if all critical locations and settings are configured.
     */
    public boolean isConfigured() {
        return getMissingSetupElements().isEmpty();
    }

    /**
     * Checks if a specific location falls inside the bounding box of the Arena Boundary.
     */
    public boolean isInside(Location loc) {
        if (pos1 == null || pos2 == null || loc == null) {
            return false;
        }
        if (!loc.getWorld().equals(pos1.getWorld())) {
            return false;
        }
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return loc.getX() >= minX && loc.getX() <= maxX &&
               loc.getY() >= minY && loc.getY() <= maxY &&
               loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
}
