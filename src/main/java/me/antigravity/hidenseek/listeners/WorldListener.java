package me.antigravity.hidenseek.listeners;

import me.antigravity.hidenseek.HideNSeek;
import me.antigravity.hidenseek.bazooka.BazookaManager;
import me.antigravity.hidenseek.game.GameSession;
import me.antigravity.hidenseek.game.PlayerRole;
import me.antigravity.hidenseek.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.UUID;

/**
 * Listens for projectile hits to handle Bazooka rocket detonations and damage.
 */
public class WorldListener implements Listener {

    private final HideNSeek plugin;

    public WorldListener(HideNSeek plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball)) return;
        Snowball snowball = (Snowball) event.getEntity();

        if (snowball.hasMetadata(BazookaManager.ROCKET_METADATA)) {
            // Intercept rocket hit
            event.setCancelled(true);
            plugin.getBazookaManager().removeRocket(snowball);

            // Fetch shooter from metadata
            Player shooter = null;
            try {
                String uuidStr = snowball.getMetadata(BazookaManager.ROCKET_METADATA).get(0).asString();
                UUID shooterUuid = UUID.fromString(uuidStr);
                shooter = Bukkit.getPlayer(shooterUuid);
            } catch (Exception e) {
                // Shooter might be offline
            }

            Location hitLoc = snowball.getLocation();
            triggerExplosion(hitLoc, shooter);
        }
    }

    /**
     * Renders visual effects and applies custom damage logic around the rocket detonation point.
     */
    private void triggerExplosion(Location loc, Player shooter) {
        if (loc.getWorld() == null) return;

        // 1. Render Premium Custom Explosion Effects
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1, 0, 0, 0, 0);
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 25, 0.4, 0.4, 0.4, 0.15);
        loc.getWorld().spawnParticle(Particle.SMOKE, loc, 20, 0.4, 0.4, 0.4, 0.05);
        MessageUtils.playSoundAt(loc, "ENTITY_GENERIC_EXPLODE", 1.0f, 1.0f);

        // 2. Fetch config values
        double radius = plugin.getConfigManager().getConfig().getDouble("bazooka-settings.explosion-radius", 4.0);
        double maxDamage = plugin.getConfigManager().getConfig().getDouble("bazooka-settings.damage", 8.0);

        // If shooter is offline, we can check general sessions, but usually they are online
        GameSession session = shooter != null ? plugin.getPlayerSession(shooter) : null;

        // 3. Scan and damage nearby hiders
        for (Player target : loc.getWorld().getPlayers()) {
            Location targetLoc = target.getLocation();
            double distance = targetLoc.distance(loc);
            
            if (distance <= radius) {
                GameSession targetSession = plugin.getPlayerSession(target);
                
                // Only damage if they are in the same game session
                if (targetSession != null && targetSession == session) {
                    PlayerRole targetRole = targetSession.getRole(target);
                    
                    if (targetRole == PlayerRole.HIDER) {
                        // Calculate damage with falloff (deals full damage at center, 0 at edge)
                        double factor = (radius - distance) / radius;
                        double damage = maxDamage * factor;
                        
                        // Apply damage
                        applyRocketDamage(target, damage, targetSession, shooter);
                    }
                }
            }
        }
    }

    /**
     * Applies damage to a Hider, triggering role swap if health becomes zero.
     */
    private void applyRocketDamage(Player hider, double damage, GameSession session, Player shooter) {
        double currentHealth = hider.getHealth();
        double newHealth = currentHealth - damage;

        if (newHealth <= 0.0) {
            // Eliminated!
            hider.setHealth(20.0); // Reset health
            session.eliminateHider(hider, shooter);
        } else {
            // Damaged but not dead
            hider.setHealth(newHealth);
            
            // Visual damage effect (red flash)
            hider.damage(0.1); // Mini-shake
            
            MessageUtils.playSound(hider, "ENTITY_PLAYER_HURT", 0.8f, 1.0f);
            MessageUtils.sendMessage(hider, "&cYou were damaged by a Bazooka rocket blast! &7(" + String.format("%.1f", damage) + " HP)");
        }
    }
}
