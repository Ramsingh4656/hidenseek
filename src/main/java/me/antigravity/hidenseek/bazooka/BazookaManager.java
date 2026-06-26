package me.antigravity.hidenseek.bazooka;

import me.antigravity.hidenseek.HideNSeek;
import me.antigravity.hidenseek.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Handles the creation, usage, cooldown, and ammunition of the custom Bazooka weapon.
 */
public class BazookaManager {

    public static final String ROCKET_METADATA = "hns_rocket";
    
    private final HideNSeek plugin;
    private final NamespacedKey ammoKey;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<Snowball> activeRockets = Collections.synchronizedSet(new HashSet<>());

    public BazookaManager(HideNSeek plugin) {
        this.plugin = plugin;
        this.ammoKey = new NamespacedKey(plugin, "bazooka_ammo");
        startRocketTrailTask();
    }

    /**
     * Creates a custom Bazooka weapon item.
     *
     * @param ammo The starting ammo count.
     * @return The configured ItemStack.
     */
    public ItemStack createBazooka(int ammo) {
        Material material = Material.valueOf(plugin.getConfigManager().getConfig().getString("bazooka-settings.item.material", "BLAZE_ROD"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String name = plugin.getConfigManager().getConfig().getString("bazooka-settings.item.name", "&e&lBazooka Rocket Launcher");
            meta.displayName(MessageUtils.color(name));

            // Set Persistent Data Container ammo
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(ammoKey, PersistentDataType.INTEGER, ammo);

            // Set lore with formatting
            List<String> loreConfig = plugin.getConfigManager().getConfig().getStringList("bazooka-settings.item.lore");
            List<Component> coloredLore = new ArrayList<>();
            int maxAmmo = plugin.getConfigManager().getConfig().getInt("bazooka-settings.max-ammo", 10);
            double cooldown = plugin.getConfigManager().getConfig().getDouble("bazooka-settings.cooldown-seconds", 3.0);
            
            for (String line : loreConfig) {
                String formatted = line.replace("%ammo%", String.valueOf(ammo))
                                      .replace("%max_ammo%", String.valueOf(maxAmmo))
                                      .replace("%cooldown%", String.valueOf(cooldown));
                coloredLore.add(MessageUtils.color(formatted));
            }
            meta.lore(coloredLore);

            // Add enchantment glow
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Checks if an item is a Bazooka.
     */
    public boolean isBazooka(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(ammoKey, PersistentDataType.INTEGER);
    }

    /**
     * Gets remaining ammo of a Bazooka item.
     */
    public int getAmmo(ItemStack item) {
        if (!isBazooka(item)) return 0;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(ammoKey, PersistentDataType.INTEGER, 0);
    }

    /**
     * Fires a rocket from the player if cooldown and ammo requirements are met.
     */
    public void fireBazooka(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        
        // Cooldown Check
        double cooldownSecs = plugin.getConfigManager().getConfig().getDouble("bazooka-settings.cooldown-seconds", 3.0);
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid)) {
            long remaining = cooldowns.get(uuid) - now;
            if (remaining > 0) {
                double secondsLeft = Math.ceil(remaining / 100.0) / 10.0;
                String msg = plugin.getConfigManager().getMessage("game.bazooka-cooldown", true)
                        .replace("%time%", String.valueOf(secondsLeft));
                MessageUtils.sendMessage(player, msg);
                return;
            }
        }

        // Ammo Check
        int ammo = getAmmo(item);
        if (ammo <= 0) {
            String msg = plugin.getConfigManager().getMessage("game.bazooka-no-ammo", true);
            MessageUtils.sendMessage(player, msg);
            return;
        }

        // Update Ammo in PDC and Lore
        ammo--;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(ammoKey, PersistentDataType.INTEGER, ammo);
            List<String> loreConfig = plugin.getConfigManager().getConfig().getStringList("bazooka-settings.item.lore");
            List<Component> coloredLore = new ArrayList<>();
            int maxAmmo = plugin.getConfigManager().getConfig().getInt("bazooka-settings.max-ammo", 10);
            double cooldown = plugin.getConfigManager().getConfig().getDouble("bazooka-settings.cooldown-seconds", 3.0);
            
            for (String line : loreConfig) {
                String formatted = line.replace("%ammo%", String.valueOf(ammo))
                                      .replace("%max_ammo%", String.valueOf(maxAmmo))
                                      .replace("%cooldown%", String.valueOf(cooldown));
                coloredLore.add(MessageUtils.color(formatted));
            }
            meta.lore(coloredLore);
            item.setItemMeta(meta);
        }

        // Apply Cooldowns
        cooldowns.put(uuid, now + (long)(cooldownSecs * 1000));
        player.setCooldown(item.getType(), (int)(cooldownSecs * 20));

        // Launch Projectile (Snowball)
        Snowball snowball = player.launchProjectile(Snowball.class);
        snowball.setMetadata(ROCKET_METADATA, new FixedMetadataValue(plugin, uuid.toString()));
        activeRockets.add(snowball);

        // Sound effect
        MessageUtils.playSound(player, "ENTITY_FIREWORK_ROCKET_LAUNCH", 1.0f, 1.0f);
    }

    /**
     * Removes cooldown tracking for a player (e.g. on game quit/join).
     */
    public void clearCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    /**
     * Ticks particle effects for all flying rockets.
     */
    private void startRocketTrailTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeRockets.isEmpty()) return;

                Iterator<Snowball> iterator = activeRockets.iterator();
                while (iterator.hasNext()) {
                    Snowball rocket = iterator.next();
                    if (!rocket.isValid() || rocket.isDead() || rocket.isOnGround()) {
                        iterator.remove();
                        continue;
                    }

                    Location loc = rocket.getLocation();
                    if (loc.getWorld() != null) {
                        loc.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.05, 0.05, 0.05, 0.01);
                        loc.getWorld().spawnParticle(Particle.SMOKE, loc, 3, 0.05, 0.05, 0.05, 0.01);
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void removeRocket(Snowball rocket) {
        activeRockets.remove(rocket);
    }
}
