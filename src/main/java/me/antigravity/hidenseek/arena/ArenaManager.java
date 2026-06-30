package me.antigravity.hidenseek.arena;

import me.antigravity.hidenseek.HideNSeek;
import me.antigravity.hidenseek.storage.YamlStorage;
import me.antigravity.hidenseek.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

/**
 * Manages loading, saving, creating, and deleting Arenas.
 * Also tracks active setup sessions and selections for admin setup commands.
 */
public class ArenaManager {

    private final HideNSeek plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private Location globalLobby;

    public ArenaManager(HideNSeek plugin) {
        this.plugin = plugin;
    }

    /**
     * Performs deferred loading of configurations on the first server tick.
     */
    public void load() {
        loadLobby();
        loadArenas();
    }

    /**
     * Loads all arenas from the plugins/HideNSeek/arenas/ directory.
     */
    public void loadArenas() {
        arenas.clear();
        File folder = new File(plugin.getDataFolder(), "arenas");
        if (!folder.exists()) {
            folder.mkdirs();
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            try {
                String arenaName = file.getName().replace(".yml", "");
                Arena arena = loadArena(arenaName);
                arenas.put(arenaName.toLowerCase(), arena);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load arena file: " + file.getName());
            }
        }
        plugin.getLogger().info("Loaded " + arenas.size() + " arenas.");
    }

    /**
     * Loads a single arena configuration by name.
     */
    private Arena loadArena(String name) {
        Arena arena = new Arena(name);
        YamlStorage storage = new YamlStorage(plugin, name, "arenas");
        FileConfiguration config = storage.getConfig();

        if (config.contains("lobby-spawn")) {
            arena.setLobbySpawn(LocationUtils.deserialize(config.getString("lobby-spawn")));
        }
        if (config.contains("seeker-spawn")) {
            arena.setSeekerSpawn(LocationUtils.deserialize(config.getString("seeker-spawn")));
        }
        if (config.contains("hider-spawns")) {
            List<String> list = config.getStringList("hider-spawns");
            for (String locStr : list) {
                Location loc = LocationUtils.deserialize(locStr);
                if (loc != null) {
                    arena.addHiderSpawn(loc);
                }
            }
        }
        if (config.contains("pos1")) {
            arena.setPos1(LocationUtils.deserialize(config.getString("pos1")));
        }
        if (config.contains("pos2")) {
            arena.setPos2(LocationUtils.deserialize(config.getString("pos2")));
        }

        arena.setMinPlayers(config.getInt("min-players", 2));
        arena.setMaxPlayers(config.getInt("max-players", 20));
        
        int defaultAutoStart = plugin.getConfigManager().getConfig().getInt("game-settings.auto-start-players", 8);
        arena.setAutoStartPlayers(config.getInt("auto-start-players", defaultAutoStart));
        
        arena.setTimer(config.getInt("timer", 300));
        
        // Load enabled state last, since setEnabled checks for isConfigured()
        boolean isEnabled = config.getBoolean("enabled", false);
        arena.setEnabled(isEnabled);

        return arena;
    }

    /**
     * Saves a single arena configuration to its YAML file.
     */
    public void saveArena(Arena arena) {
        YamlStorage storage = new YamlStorage(plugin, arena.getName(), "arenas");
        FileConfiguration config = storage.getConfig();

        config.set("enabled", arena.isEnabled());
        config.set("min-players", arena.getMinPlayers());
        config.set("max-players", arena.getMaxPlayers());
        config.set("auto-start-players", arena.getAutoStartPlayers());
        config.set("timer", arena.getTimer());

        config.set("lobby-spawn", LocationUtils.serialize(arena.getLobbySpawn()));
        config.set("seeker-spawn", LocationUtils.serialize(arena.getSeekerSpawn()));

        List<String> hiderSpawnStrings = new ArrayList<>();
        for (Location loc : arena.getHiderSpawns()) {
            hiderSpawnStrings.add(LocationUtils.serialize(loc));
        }
        config.set("hider-spawns", hiderSpawnStrings);

        config.set("pos1", LocationUtils.serialize(arena.getPos1()));
        config.set("pos2", LocationUtils.serialize(arena.getPos2()));

        storage.save();
    }

    /**
     * Saves all loaded arenas.
     */
    public void saveArenas() {
        for (Arena arena : arenas.values()) {
            saveArena(arena);
        }
    }

    /**
     * Creates a new arena and saves it.
     */
    public Arena createArena(String name) {
        if (arenas.containsKey(name.toLowerCase())) {
            return null;
        }
        Arena arena = new Arena(name);
        arenas.put(name.toLowerCase(), arena);
        saveArena(arena);
        return arena;
    }

    /**
     * Deletes an arena and its associated file.
     */
    public boolean deleteArena(String name) {
        Arena arena = arenas.remove(name.toLowerCase());
        if (arena == null) {
            return false;
        }
        YamlStorage storage = new YamlStorage(plugin, arena.getName(), "arenas");
        return storage.delete();
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public Arena getArena(String name) {
        if (name == null) return null;
        return arenas.get(name.toLowerCase());
    }

    // --- Global Lobby Storage ---

    private void loadLobby() {
        YamlStorage storage = new YamlStorage(plugin, "lobby", "data");
        FileConfiguration config = storage.getConfig();
        if (config.contains("location")) {
            globalLobby = LocationUtils.deserialize(config.getString("location"));
        }
    }

    public void saveLobby() {
        YamlStorage storage = new YamlStorage(plugin, "lobby", "data");
        FileConfiguration config = storage.getConfig();
        config.set("location", LocationUtils.serialize(globalLobby));
        storage.save();
    }

    public Location getGlobalLobby() {
        return globalLobby;
    }

    public void setGlobalLobby(Location globalLobby) {
        this.globalLobby = globalLobby;
        saveLobby();
    }


}
