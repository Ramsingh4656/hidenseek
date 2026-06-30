package me.antigravity.hidenseek.arena;

import me.antigravity.hidenseek.HideNSeek;
import me.antigravity.hidenseek.gui.SetupGUI;
import me.antigravity.hidenseek.utils.MessageUtils;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.*;
import net.kyori.adventure.text.Component;

import java.util.*;

/**
 * Manages administrative arena setup sessions.
 * Distributes interactive hotbar items and displays setup checklists.
 */
public class SetupManager {

    private final HideNSeek plugin;
    private final Map<UUID, SetupSession> sessions = new HashMap<>();
    private final Map<UUID, Location[]> selections = new HashMap<>();
    private final Map<UUID, Scoreboard> setupScoreboards = new HashMap<>();

    public SetupManager(HideNSeek plugin) {
        this.plugin = plugin;
    }

    public boolean isInSetupMode(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public SetupSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    // Temporary selection management
    public Location getSelectionPos1(Player player) {
        Location[] sel = selections.get(player.getUniqueId());
        return sel != null ? sel[0] : null;
    }

    public void setSelectionPos1(Player player, Location loc) {
        Location[] sel = selections.computeIfAbsent(player.getUniqueId(), k -> new Location[2]);
        sel[0] = loc;
    }

    public Location getSelectionPos2(Player player) {
        Location[] sel = selections.get(player.getUniqueId());
        return sel != null ? sel[1] : null;
    }

    public void setSelectionPos2(Player player, Location loc) {
        Location[] sel = selections.computeIfAbsent(player.getUniqueId(), k -> new Location[2]);
        sel[1] = loc;
    }

    /**
     * Enters interactive setup mode for a specific arena, giving them the hotbar.
     */
    public void enterSetupMode(Player player, Arena arena) {
        UUID uuid = player.getUniqueId();
        if (sessions.containsKey(uuid)) {
            exitSetupMode(player, false);
        }

        SetupSession session = new SetupSession(player, arena);
        sessions.put(uuid, session);

        player.getInventory().clear();
        giveSetupHotbar(player);
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);

        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("admin.edit-mode-on", true).replace("%arena%", arena.getName()));
        MessageUtils.playSound(player, "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.0f);
        
        printChecklist(player, arena);
        
        // Open the setup visual GUI
        new SetupGUI(plugin, arena).open(player);
    }

    /**
     * Exits interactive setup mode and restores the player's inventory.
     */
    public void exitSetupMode(Player player, boolean restoreState) {
        UUID uuid = player.getUniqueId();
        SetupSession session = sessions.remove(uuid);
        selections.remove(uuid);
        
        // Clean up setup scoreboard
        setupScoreboards.remove(uuid);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        if (session == null) return;

        if (restoreState) {
            session.restore(player);
        }
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("admin.edit-mode-off", true));
        MessageUtils.playSound(player, "ENTITY_ITEM_BREAK", 1.0f, 0.8f);
    }

    /**
     * Gives the clickable setup items to the player's hotbar.
     */
    public void giveSetupHotbar(Player player) {
        player.getInventory().setItem(0, createSetupItem(Material.BEACON, "&a&lSet Lobby Spawn &7(Right Click)", "&7Click to set the arena lobby spawn at your location."));
        player.getInventory().setItem(1, createSetupItem(Material.REDSTONE_TORCH, "&c&lSet Seeker Spawn &7(Right Click)", "&7Click to set the Seeker spawn at your location."));
        player.getInventory().setItem(2, createSetupItem(Material.EMERALD, "&a&lAdd Hider Spawn &7(Right Click)", "&7Click to add a Hider spawn point at your location."));
        player.getInventory().setItem(3, createSetupItem(Material.GOLDEN_AXE, "&6&lRegion Wand &7(Left/Right Click)", "&7Left-click block: Set Position 1\n&7Right-click block: Set Position 2"));
        player.getInventory().setItem(4, createSetupItem(Material.NETHER_STAR, "&b&lSave Arena &7(Right Click)", "&7Click to validate and save this arena configuration."));
        player.getInventory().setItem(5, createSetupItem(Material.IRON_DOOR, "&e&lFinish Setup &7(Right Click)", "&7Click to exit setup mode and restore your items."));
        player.updateInventory();
    }

    private ItemStack createSetupItem(Material material, String name, String loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtils.color(name));
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines.split("\n")) {
                lore.add(MessageUtils.color(line));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Prints the visual setup checklist in the chat window and updates scoreboard sidebar.
     */
    public void printChecklist(Player player, Arena arena) {
        boolean lobbySet = arena.getLobbySpawn() != null;
        boolean lobbyBoundSet = arena.getLobbyPos1() != null && arena.getLobbyPos2() != null;
        boolean seekerSet = arena.getSeekerSpawn() != null;
        boolean seekerBoundSet = arena.getSeekerPos1() != null && arena.getSeekerPos2() != null;
        boolean hidersSet = !arena.getHiderSpawns().isEmpty();
        boolean hiderBoundSet = arena.getHiderPos1() != null && arena.getHiderPos2() != null;
        boolean saved = arena.isEnabled();
        boolean finished = !isInSetupMode(player);

        MessageUtils.sendMessage(player, "&8&m========================================");
        MessageUtils.sendMessage(player, "    &6&lArena Setup Checklist: &e" + arena.getName());
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, (lobbySet ? "  &a✔ &eLobby Spawn" : "  &c✖ &eLobby Spawn &8(Need to set)"));
        MessageUtils.sendMessage(player, (lobbyBoundSet ? "  &a✔ &eLobby Boundary" : "  &c✖ &eLobby Boundary &8(Need to select bounds)"));
        MessageUtils.sendMessage(player, (seekerSet ? "  &a✔ &eSeeker Spawn" : "  &c✖ &eSeeker Spawn &8(Need to set)"));
        MessageUtils.sendMessage(player, (seekerBoundSet ? "  &a✔ &eSeeker Boundary" : "  &c✖ &eSeeker Boundary &8(Need to select bounds)"));
        MessageUtils.sendMessage(player, (hidersSet ? "  &a✔ &eHider Spawns &a(" + arena.getHiderSpawns().size() + ")" : "  &c✖ &eHider Spawns &8(Need to add)"));
        MessageUtils.sendMessage(player, (hiderBoundSet ? "  &a✔ &eHider Boundary" : "  &c✖ &eHider Boundary &8(Need to select bounds)"));
        MessageUtils.sendMessage(player, (saved ? "  &a✔ &eSaved" : "  &c✖ &eSaved"));
        MessageUtils.sendMessage(player, (finished ? "  &a✔ &eFinished" : "  &c✖ &eFinished"));
        MessageUtils.sendMessage(player, "&8&m========================================");

        // Also update setup scoreboard
        updateSetupScoreboard(player, arena);
    }

    public void updateSetupScoreboard(Player player, Arena arena) {
        Scoreboard sb = setupScoreboards.computeIfAbsent(player.getUniqueId(), k -> Bukkit.getScoreboardManager().getNewScoreboard());
        
        Objective obj = sb.getObjective("hns_setup");
        if (obj != null) {
            obj.unregister();
        }
        obj = sb.registerNewObjective("hns_setup", Criteria.DUMMY, MessageUtils.color("&6&lSetup: &e" + arena.getName()));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        try {
            obj.numberFormat(NumberFormat.blank());
        } catch (Throwable t) {}

        boolean lobbySet = arena.getLobbySpawn() != null;
        boolean lobbyBoundSet = arena.getLobbyPos1() != null && arena.getLobbyPos2() != null;
        boolean seekerSet = arena.getSeekerSpawn() != null;
        boolean seekerBoundSet = arena.getSeekerPos1() != null && arena.getSeekerPos2() != null;
        boolean hidersSet = !arena.getHiderSpawns().isEmpty();
        boolean hiderBoundSet = arena.getHiderPos1() != null && arena.getHiderPos2() != null;
        boolean saved = arena.isEnabled();
        boolean finished = !isInSetupMode(player);

        List<String> lines = new ArrayList<>();
        lines.add("&7---------------------");
        lines.add((lobbySet ? "&a✔ &fLobby Spawn" : "&c✖ &fLobby Spawn"));
        lines.add((lobbyBoundSet ? "&a✔ &fLobby Boundary" : "&c✖ &fLobby Boundary"));
        lines.add((seekerSet ? "&a✔ &fSeeker Spawn" : "&c✖ &fSeeker Spawn"));
        lines.add((seekerBoundSet ? "&a✔ &fSeeker Boundary" : "&c✖ &fSeeker Boundary"));
        lines.add((hidersSet ? "&a✔ &fHider Spawns &a(" + arena.getHiderSpawns().size() + ")" : "&c✖ &fHider Spawns"));
        lines.add((hiderBoundSet ? "&a✔ &fHider Boundary" : "&c✖ &fHider Boundary"));
        lines.add((saved ? "&a✔ &fSaved" : "&c✖ &fSaved"));
        lines.add((finished ? "&a✔ &fFinished" : "&c✖ &fFinished"));
        lines.add("&7---------------------");

        int score = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String entry = MessageUtils.colorLegacy(line + getInvisibleSuffix(i));
            Score s = obj.getScore(entry);
            s.setScore(score--);
        }

        player.setScoreboard(sb);
    }

    private String getInvisibleSuffix(int index) {
        char colorChar = Integer.toHexString(index % 16).charAt(0);
        return "§" + colorChar + "§r";
    }

    public static class SetupSession {
        private final Arena arena;
        private final ItemStack[] contents;
        private final ItemStack[] armorContents;
        private final ItemStack[] extraContents;
        private final ItemStack offHandItem;
        private final GameMode gameMode;
        private final boolean allowFlight;
        private final boolean isFlying;

        public SetupSession(Player player, Arena arena) {
            this.arena = arena;
            this.contents = player.getInventory().getContents().clone();
            this.armorContents = player.getInventory().getArmorContents().clone();
            this.extraContents = player.getInventory().getExtraContents().clone();
            this.offHandItem = player.getInventory().getItemInOffHand() != null ? player.getInventory().getItemInOffHand().clone() : null;
            this.gameMode = player.getGameMode();
            this.allowFlight = player.getAllowFlight();
            this.isFlying = player.isFlying();
        }

        public Arena getArena() {
            return arena;
        }

        public void restore(Player player) {
            player.getInventory().clear();
            player.getInventory().setContents(this.contents);
            player.getInventory().setArmorContents(this.armorContents);
            player.getInventory().setExtraContents(this.extraContents);
            player.getInventory().setItemInOffHand(this.offHandItem);
            player.setGameMode(this.gameMode);
            player.setAllowFlight(this.allowFlight);
            player.setFlying(this.isFlying);
            player.updateInventory();
        }
    }
}
