package me.antigravity.hidenseek.gui;

import me.antigravity.hidenseek.HideNSeek;
import me.antigravity.hidenseek.arena.Arena;
import me.antigravity.hidenseek.arena.ArenaState;
import me.antigravity.hidenseek.game.GameSession;
import me.antigravity.hidenseek.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom inventory GUI showing all active arenas, their players, and status.
 */
public class ArenaSelectorGUI implements InventoryHolder {

    private final HideNSeek plugin;
    private final Inventory inventory;
    private final Map<Integer, String> slotToArena = new HashMap<>();

    public ArenaSelectorGUI(HideNSeek plugin) {
        this.plugin = plugin;
        
        int arenaCount = plugin.getArenaManager().getArenas().size();
        int rows = (int) Math.ceil(arenaCount / 7.0) + 2; // Add 2 rows for top/bottom padding
        rows = Math.max(3, Math.min(6, rows)); // Force between 3 and 6 rows
        
        this.inventory = Bukkit.createInventory(this, rows * 9, MessageUtils.color("&6&lHide &f&lN &6&lSeek Arenas"));
        setupItems();
    }

    /**
     * Renders items inside the selector GUI.
     */
    private void setupItems() {
        // Build filler item
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.empty());
            filler.setItemMeta(fillerMeta);
        }

        // Fill entire inventory with borders
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        // Add Arena selectors in the center slots
        int slot = 10;
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            // Skip borders (Column 0 and 8)
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            if (slot >= inventory.getSize() - 9) {
                break; // Out of bounds
            }

            inventory.setItem(slot, buildArenaItem(arena));
            slotToArena.put(slot, arena.getName());
            slot++;
        }
    }

    /**
     * Builds an ItemStack representing a specific Arena.
     */
    private ItemStack buildArenaItem(Arena arena) {
        Material material;
        String statusText;
        int currentPlayers = 0;

        GameSession session = plugin.getGameSession(arena.getName());
        if (session != null) {
            currentPlayers = session.getPlayers().size();
        }

        ArenaState state = arena.getState();
        if (!arena.isEnabled()) {
            state = ArenaState.DISABLED;
        }

        switch (state) {
            case WAITING:
                material = Material.GREEN_WOOL;
                statusText = "&aWaiting";
                break;
            case STARTING:
                material = Material.YELLOW_WOOL;
                statusText = "&eStarting";
                break;
            case IN_GAME:
                material = Material.RED_WOOL;
                statusText = "&cIn Game";
                break;
            case DISABLED:
            default:
                material = Material.BARRIER;
                statusText = "&8Disabled";
                break;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtils.color("&6&lArena: &e" + arena.getName()));
            
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtils.color("&7Status: " + statusText));
            lore.add(MessageUtils.color("&7Players: &b" + currentPlayers + "&7/&b" + arena.getMaxPlayers()));
            lore.add(MessageUtils.color("&7Minimum Players: &b" + arena.getMinPlayers()));
            lore.add(MessageUtils.color(""));
            if (state == ArenaState.WAITING || state == ArenaState.STARTING) {
                lore.add(MessageUtils.color("&aClick to join match!"));
            } else if (state == ArenaState.IN_GAME) {
                lore.add(MessageUtils.color("&cGame in progress. Can't join."));
            } else {
                lore.add(MessageUtils.color("&cArena is currently disabled."));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Gets the arena name associated with a clicked slot.
     */
    public String getArenaNameAt(int slot) {
        return slotToArena.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Opens the selector GUI for a player.
     */
    public void open(Player player) {
        player.openInventory(inventory);
    }
}
