package me.antigravity.hidenseek.gui;

import me.antigravity.hidenseek.HideNSeek;
import me.antigravity.hidenseek.arena.Arena;
import me.antigravity.hidenseek.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom inventory GUI showing Arena Setup buttons and checklist statuses.
 */
public class SetupGUI implements InventoryHolder {

    private final HideNSeek plugin;
    private final Arena arena;
    private final Inventory inventory;

    public SetupGUI(HideNSeek plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.inventory = Bukkit.createInventory(this, 27, MessageUtils.color("&6Setup: &e" + arena.getName()));
        refresh();
    }

    /**
     * Rebuilds and updates the inventory items dynamically.
     */
    public void refresh() {
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

        // Add setup elements in row 2 (slots 10 to 14)
        inventory.setItem(10, buildChecklistItem(Material.BEACON, "&a&lLobby Spawn", 
                arena.getLobbySpawn() != null, "Sets the Arena Lobby Spawn to your location."));
        
        inventory.setItem(11, buildChecklistItem(Material.REDSTONE_TORCH, "&c&lSeeker Spawn", 
                arena.getSeekerSpawn() != null, "Sets the Seeker Spawn point to your location."));
        
        boolean hidersSet = !arena.getHiderSpawns().isEmpty();
        String hiderSpawnDesc = "Adds a Hider Spawn point at your location.\n&7Spawns set: &e" + arena.getHiderSpawns().size() + "\n&7(Right-click to clear all spawns)";
        inventory.setItem(12, buildChecklistItem(Material.EMERALD, "&a&lHider Spawn", hidersSet, hiderSpawnDesc));
        
        inventory.setItem(13, buildChecklistItem(Material.IRON_BARS, "&b&lArena Boundary", 
                arena.getPos1() != null && arena.getPos2() != null, "Assigns your current Wand Selection as the Arena Boundary."));

        // Selection Wand
        ItemStack wand = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta wandMeta = wand.getItemMeta();
        if (wandMeta != null) {
            wandMeta.displayName(MessageUtils.color("&6&lSelection Wand"));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtils.color("&7Click to receive the selection wand tool."));
            lore.add(Component.empty());
            lore.add(MessageUtils.color("&7Left-click block: Set Position 1"));
            lore.add(MessageUtils.color("&7Right-click block: Set Position 2"));
            wandMeta.lore(lore);
            wand.setItemMeta(wandMeta);
        }
        inventory.setItem(14, wand);

        // Row 3 Action Buttons (slots 20, 22, 24)
        inventory.setItem(20, buildActionButton(Material.NETHER_STAR, "&b&lSave Arena", "&7Validates, saves, and auto-enables this arena."));
        inventory.setItem(22, buildActionButton(Material.IRON_DOOR, "&e&lFinish Setup", "&7Saves, closes the setup, and restores your inventory."));
        inventory.setItem(24, buildActionButton(Material.BARRIER, "&c&lCancel Setup", "&7Exit setup mode without saving changes."));
    }

    private ItemStack buildChecklistItem(Material material, String name, boolean checked, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtils.color(name));
            List<Component> lore = new ArrayList<>();
            for (String line : description.split("\n")) {
                lore.add(MessageUtils.color("&7" + line));
            }
            lore.add(Component.empty());
            lore.add(MessageUtils.color(checked ? "&a✔ Configured" : "&c✖ Missing"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildActionButton(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtils.color(name));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtils.color(description));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
