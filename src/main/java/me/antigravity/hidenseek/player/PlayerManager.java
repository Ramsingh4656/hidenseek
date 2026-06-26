package me.antigravity.hidenseek.player;

import me.antigravity.hidenseek.HideNSeek;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

/**
 * Manages player status, roles, and saving/restoring player inventories,
 * levels, attributes (scale), and status effects upon joining and leaving.
 */
public class PlayerManager {

    private final HideNSeek plugin;
    private final Map<UUID, PlayerRestoreState> restoreStates = new HashMap<>();

    public PlayerManager(HideNSeek plugin) {
        this.plugin = plugin;
    }

    /**
     * Caches a player's current state (inventory, health, food, exp, active effects, and scale).
     */
    public void savePlayerState(Player player) {
        UUID uuid = player.getUniqueId();
        if (restoreStates.containsKey(uuid)) {
            return; // Already saved
        }
        
        restoreStates.put(uuid, new PlayerRestoreState(player));
    }

    /**
     * Restores a player's cached state.
     */
    public void restorePlayerState(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerRestoreState state = restoreStates.remove(uuid);
        if (state == null) {
            // Safe fallbacks to prevent glitches if no restore state is found
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
            setScale(player, 1.0);
            return;
        }

        state.restore(player);
    }

    /**
     * Checks if a player has a saved state.
     */
    public boolean hasSavedState(Player player) {
        return restoreStates.containsKey(player.getUniqueId());
    }

    /**
     * Gets the base scale of a player.
     */
    public double getScale(Player player) {
        AttributeInstance scaleAttr = player.getAttribute(Attribute.GENERIC_SCALE);
        return scaleAttr != null ? scaleAttr.getBaseValue() : 1.0;
    }

    /**
     * Sets the physical scale (size) of a player.
     */
    public void setScale(Player player, double scale) {
        AttributeInstance scaleAttr = player.getAttribute(Attribute.GENERIC_SCALE);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(scale);
        }
    }

    /**
     * Container class for holding a player's pre-game state.
     */
    private static class PlayerRestoreState {
        private final ItemStack[] contents;
        private final ItemStack[] armorContents;
        private final ItemStack[] extraContents;
        private final ItemStack offHandItem;
        private final GameMode gameMode;
        private final int level;
        private final float exp;
        private final double health;
        private final int foodLevel;
        private final Collection<PotionEffect> activeEffects;
        private final double scale;

        public PlayerRestoreState(Player player) {
            this.contents = player.getInventory().getContents().clone();
            this.armorContents = player.getInventory().getArmorContents().clone();
            this.extraContents = player.getInventory().getExtraContents().clone();
            this.offHandItem = player.getInventory().getItemInOffHand() != null ? player.getInventory().getItemInOffHand().clone() : null;
            this.gameMode = player.getGameMode();
            this.level = player.getLevel();
            this.exp = player.getExp();
            this.health = player.getHealth();
            this.foodLevel = player.getFoodLevel();
            this.activeEffects = new ArrayList<>(player.getActivePotionEffects());
            
            AttributeInstance scaleAttr = player.getAttribute(Attribute.GENERIC_SCALE);
            this.scale = scaleAttr != null ? scaleAttr.getBaseValue() : 1.0;
        }

        public void restore(Player player) {
            player.getInventory().clear();
            
            // Clear potion effects
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            // Restore items
            player.getInventory().setContents(this.contents);
            player.getInventory().setArmorContents(this.armorContents);
            player.getInventory().setExtraContents(this.extraContents);
            player.getInventory().setItemInOffHand(this.offHandItem);
            
            // Restore details
            player.setGameMode(this.gameMode);
            player.setLevel(this.level);
            player.setExp(this.exp);
            
            // Limit health bounds to max health attribute
            double maxHealth = 20.0;
            AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthAttr != null) {
                maxHealth = maxHealthAttr.getBaseValue();
            }
            player.setHealth(Math.min(this.health, maxHealth));
            player.setFoodLevel(this.foodLevel);

            // Restore active potion effects
            for (PotionEffect effect : this.activeEffects) {
                player.addPotionEffect(effect);
            }

            // Restore scale
            AttributeInstance scaleAttr = player.getAttribute(Attribute.GENERIC_SCALE);
            if (scaleAttr != null) {
                scaleAttr.setBaseValue(this.scale);
            }
        }
    }
}
