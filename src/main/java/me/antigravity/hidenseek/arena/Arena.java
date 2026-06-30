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
    
    // Boundary regions
    private Location lobbyPos1;
    private Location lobbyPos2;
    private Location seekerPos1;
    private Location seekerPos2;
    private Location hiderPos1;
    private Location hiderPos2;

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

    // Lobby boundary getters/setters
    public Location getLobbyPos1() {
        return lobbyPos1;
    }

    public void setLobbyPos1(Location lobbyPos1) {
        this.lobbyPos1 = lobbyPos1;
    }

    public Location getLobbyPos2() {
        return lobbyPos2;
    }

    public void setLobbyPos2(Location lobbyPos2) {
        this.lobbyPos2 = lobbyPos2;
    }

    // Seeker boundary getters/setters
    public Location getSeekerPos1() {
        return seekerPos1;
    }

    public void setSeekerPos1(Location seekerPos1) {
        this.seekerPos1 = seekerPos1;
    }

    public Location getSeekerPos2() {
        return seekerPos2;
    }

    public void setSeekerPos2(Location seekerPos2) {
        this.seekerPos2 = seekerPos2;
    }

    // Hider boundary getters/setters
    public Location getHiderPos1() {
        return hiderPos1;
    }

    public void setHiderPos1(Location hiderPos1) {
        this.hiderPos1 = hiderPos1;
    }

    public Location getHiderPos2() {
        return hiderPos2;
    }

    public void setHiderPos2(Location hiderPos2) {
        this.hiderPos2 = hiderPos2;
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
        if (lobbyPos1 == null || lobbyPos2 == null) missing.add("Lobby Boundary");
        if (seekerSpawn == null) missing.add("Seeker Spawn");
        if (seekerPos1 == null || seekerPos2 == null) missing.add("Seeker Boundary");
        if (hiderSpawns.isEmpty()) missing.add("Hider Spawn");
        if (hiderPos1 == null || hiderPos2 == null) missing.add("Hider Boundary");
        return missing;
    }

    /**
     * Checks if all critical locations and settings are configured.
     */
    public boolean isConfigured() {
        return getMissingSetupElements().isEmpty();
    }

    /**
     * Checks if a specific location falls inside the bounding box of a region.
     */
    private boolean isInside(Location pos1, Location pos2, Location loc) {
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

    public boolean isInsideLobby(Location loc) {
        return isInside(lobbyPos1, lobbyPos2, loc);
    }

    public boolean isInsideSeeker(Location loc) {
        return isInside(seekerPos1, seekerPos2, loc);
    }

    public boolean isInsideHider(Location loc) {
        return isInside(hiderPos1, hiderPos2, loc);
    }

    /**
     * Retains compatibility with legacy checks referencing the main region (now hiders region).
     */
    public boolean isInside(Location loc) {
        return isInsideHider(loc);
    }
}
