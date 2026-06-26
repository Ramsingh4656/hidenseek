package me.antigravity.hidenseek;

import me.antigravity.hidenseek.api.HideNSeekAPI;
import me.antigravity.hidenseek.arena.Arena;
import me.antigravity.hidenseek.arena.ArenaManager;
import me.antigravity.hidenseek.bazooka.BazookaManager;
import me.antigravity.hidenseek.commands.HNSCommand;
import me.antigravity.hidenseek.config.ConfigManager;
import me.antigravity.hidenseek.game.GameSession;
import me.antigravity.hidenseek.gui.ArenaSelectorGUI;
import me.antigravity.hidenseek.listeners.PlayerListener;
import me.antigravity.hidenseek.listeners.WorldListener;
import me.antigravity.hidenseek.player.PlayerManager;
import me.antigravity.hidenseek.services.GameService;
import me.antigravity.hidenseek.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Main plugin class for HideNSeek. Tying configs, managers, commands, services, and listeners together.
 */
public final class HideNSeek extends JavaPlugin implements HideNSeekAPI {

    private static HideNSeekAPI apiInstance;

    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private PlayerManager playerManager;
    private BazookaManager bazookaManager;
    private GameService gameService;

    private ItemStack lobbyItemJoin;
    private ItemStack lobbyItemLeave;

    @Override
    public void onEnable() {
        // Set API Instance
        apiInstance = this;

        // 1. Initialize Configuration
        configManager = new ConfigManager(this);

        // 2. Initialize Core Managers and Services
        arenaManager = new ArenaManager(this);
        playerManager = new PlayerManager(this);
        bazookaManager = new BazookaManager(this);
        gameService = new GameService(this);

        // 3. Register Active Game Sessions via GameService
        for (Arena arena : arenaManager.getArenas()) {
            createGameSession(arena);
        }

        // 4. Load Custom Matchmaking Lobby Items
        loadLobbyItems();

        // 5. Register Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);

        // 6. Register Commands and Tab Completers
        HNSCommand commandExecutor = new HNSCommand(this);
        Objects.requireNonNull(getCommand("hns")).setExecutor(commandExecutor);
        Objects.requireNonNull(getCommand("hns")).setTabCompleter(commandExecutor);

        getLogger().info("HideNSeek Plugin Enabled Successfully!");
    }

    @Override
    public void onDisable() {
        // Terminate all sessions and restore players safely via GameService
        if (gameService != null) {
            gameService.stopAll();
        }

        // Save all arena configuration files
        if (arenaManager != null) {
            arenaManager.saveArenas();
        }

        apiInstance = null;
        getLogger().info("HideNSeek Plugin Disabled.");
    }

    /**
     * Gets the static API instance.
     */
    public static HideNSeekAPI getAPI() {
        return apiInstance;
    }

    /**
     * Re-creates the Lobby Items when configurations are reloaded.
     */
    public void loadLobbyItems() {
        // Load Join Nether Star Item
        try {
            Material joinMat = Material.valueOf(configManager.getConfig().getString("lobby-items.join-selector.material", "NETHER_STAR"));
            ItemStack joinItem = new ItemStack(joinMat);
            ItemMeta joinMeta = joinItem.getItemMeta();
            if (joinMeta != null) {
                String name = configManager.getConfig().getString("lobby-items.join-selector.name", "&a&lJoin Game &7(Right Click)");
                joinMeta.displayName(MessageUtils.color(name));
                
                List<String> configLore = configManager.getConfig().getStringList("lobby-items.join-selector.lore");
                List<Component> coloredLore = new ArrayList<>();
                for (String line : configLore) {
                    coloredLore.add(MessageUtils.color(line));
                }
                joinMeta.lore(coloredLore);
                joinItem.setItemMeta(joinMeta);
            }
            this.lobbyItemJoin = joinItem;
        } catch (Exception e) {
            getLogger().warning("Failed to load lobby join item from config: " + e.getMessage());
            this.lobbyItemJoin = new ItemStack(Material.NETHER_STAR);
        }

        // Load Leave Barrier Item
        try {
            Material leaveMat = Material.valueOf(configManager.getConfig().getString("lobby-items.leave-lobby.material", "BARRIER"));
            ItemStack leaveItem = new ItemStack(leaveMat);
            ItemMeta leaveMeta = leaveItem.getItemMeta();
            if (leaveMeta != null) {
                String name = configManager.getConfig().getString("lobby-items.leave-lobby.name", "&c&lLeave Lobby &7(Right Click)");
                leaveMeta.displayName(MessageUtils.color(name));
                
                List<String> configLore = configManager.getConfig().getStringList("lobby-items.leave-lobby.lore");
                List<Component> coloredLore = new ArrayList<>();
                for (String line : configLore) {
                    coloredLore.add(MessageUtils.color(line));
                }
                leaveMeta.lore(coloredLore);
                leaveItem.setItemMeta(leaveMeta);
            }
            this.lobbyItemLeave = leaveItem;
        } catch (Exception e) {
            getLogger().warning("Failed to load lobby leave item from config: " + e.getMessage());
            this.lobbyItemLeave = new ItemStack(Material.BARRIER);
        }
    }

    // --- Session Manager Gateways ---

    public void createGameSession(Arena arena) {
        gameService.registerSession(arena);
    }

    public void removeGameSession(String arenaName) {
        gameService.removeSession(arenaName);
    }

    public GameSession getGameSession(String arenaName) {
        if (gameService == null) return null;
        return gameService.getSession(arenaName);
    }

    /**
     * Resolves which GameSession a player is currently in.
     */
    public GameSession getPlayerSession(Player player) {
        if (gameService == null) return null;
        return gameService.getPlayerSession(player);
    }

    /**
     * Checks if a player is in any active game session (lobby or in progress).
     */
    public boolean isPlayerInGame(Player player) {
        if (gameService == null) return false;
        return gameService.isPlayerInGame(player);
    }

    /**
     * Opens the arena selector GUI menu for a player.
     */
    public void openArenaSelector(Player player) {
        ArenaSelectorGUI gui = new ArenaSelectorGUI(this);
        gui.open(player);
    }

    // --- Accessors ---

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public BazookaManager getBazookaManager() {
        return bazookaManager;
    }

    public GameService getGameService() {
        return gameService;
    }

    public ItemStack getLobbyItemJoin() {
        return lobbyItemJoin;
    }

    public ItemStack getLobbyItemLeave() {
        return lobbyItemLeave;
    }
}

