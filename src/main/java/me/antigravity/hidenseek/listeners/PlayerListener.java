package me.antigravity.hidenseek.listeners;

import me.antigravity.hidenseek.HideNSeek;
import me.antigravity.hidenseek.arena.Arena;
import me.antigravity.hidenseek.arena.ArenaState;
import me.antigravity.hidenseek.arena.SetupManager;
import me.antigravity.hidenseek.arena.SetupManager.SetupSession;
import me.antigravity.hidenseek.game.GameSession;
import me.antigravity.hidenseek.game.PlayerRole;
import me.antigravity.hidenseek.gui.ArenaSelectorGUI;
import me.antigravity.hidenseek.utils.MessageUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Enforces all game rules (no building/breaking, boundary control, melee tagging, lobby items).
 * Handles setup wand usage and GUI interactions.
 */
public class PlayerListener implements Listener {

    private final HideNSeek plugin;
    private final Map<UUID, Long> boundaryMessageCooldown = new HashMap<>();

    public PlayerListener(HideNSeek plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if a player can bypass game rules (OP, Creative, and has permission).
     */
    private boolean canBypass(Player player) {
        return player.hasPermission("hns.bypass") && player.getGameMode() == GameMode.CREATIVE;
    }

    // --- Lobby Items & Setup Wand Intercept ---

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // Setup Mode Logic
        if (plugin.getSetupManager().isInSetupMode(player)) {
            event.setCancelled(true);
            Material type = item.getType();
            SetupSession session = plugin.getSetupManager().getSession(player);
            Arena arena = session.getArena();

            if (type == Material.GOLDEN_AXE) {
                Block block = event.getClickedBlock();
                if (block != null) {
                    Location loc = block.getLocation();
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        arena.setPos1(loc);
                        MessageUtils.sendMessage(player, "&a&l✅ &ePosition 1 set to &6" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                        MessageUtils.playSound(player, "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.2f);
                        plugin.getSetupManager().printChecklist(player, arena);
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        arena.setPos2(loc);
                        MessageUtils.sendMessage(player, "&a&l✅ &ePosition 2 set to &6" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                        MessageUtils.playSound(player, "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.2f);
                        plugin.getSetupManager().printChecklist(player, arena);
                    }
                }
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (type == Material.BEACON) {
                    arena.setLobbySpawn(player.getLocation());
                    
                    // Set global lobby fallback automatically if not set
                    if (plugin.getArenaManager().getGlobalLobby() == null) {
                        plugin.getArenaManager().setGlobalLobby(player.getLocation());
                    }
                    
                    MessageUtils.sendMessage(player, "&a&l✅ &eLobby spawn set to your current location.");
                    MessageUtils.playSound(player, "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.0f);
                    plugin.getSetupManager().printChecklist(player, arena);
                } else if (type == Material.REDSTONE_TORCH) {
                    arena.setSeekerSpawn(player.getLocation());
                    MessageUtils.sendMessage(player, "&a&l✅ &eSeeker spawn set to your current location.");
                    MessageUtils.playSound(player, "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.0f);
                    plugin.getSetupManager().printChecklist(player, arena);
                } else if (type == Material.EMERALD) {
                    arena.addHiderSpawn(player.getLocation());
                    int index = arena.getHiderSpawns().size();
                    MessageUtils.sendMessage(player, "&a&l✅ &eHider spawn point #" + index + " added at your location.");
                    MessageUtils.playSound(player, "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.0f);
                    plugin.getSetupManager().printChecklist(player, arena);
                } else if (type == Material.NETHER_STAR) {
                    List<String> missing = new ArrayList<>();
                    if (arena.getLobbySpawn() == null) missing.add("Lobby Spawn");
                    if (arena.getSeekerSpawn() == null) missing.add("Seeker Spawn");
                    if (arena.getHiderSpawns().isEmpty()) missing.add("Hider Spawn");
                    if (arena.getPos1() == null || arena.getPos2() == null) missing.add("Region Boundary");

                    if (missing.isEmpty()) {
                        arena.setEnabled(true);
                        plugin.getArenaManager().saveArena(arena);
                        MessageUtils.sendMessage(player, "&8&m========================================");
                        MessageUtils.sendMessage(player, "  &a&lSUCCESS! &eArena has been validated, saved, and auto-enabled!");
                        MessageUtils.sendMessage(player, "  &eStatus: &2Enabled, Configured, Ready");
                        MessageUtils.sendMessage(player, "&8&m========================================");
                        MessageUtils.playSound(player, "UI_TOAST_CHALLENGE_COMPLETE", 1.0f, 1.0f);
                        plugin.getSetupManager().printChecklist(player, arena);
                    } else {
                        MessageUtils.sendMessage(player, "&8&m========================================");
                        MessageUtils.sendMessage(player, "  &c&lVALIDATION FAILED!");
                        MessageUtils.sendMessage(player, "  &cThe arena cannot be enabled because setup is incomplete.");
                        MessageUtils.sendMessage(player, "  &7Missing elements:");
                        for (String m : missing) {
                            MessageUtils.sendMessage(player, "  &c• " + m);
                        }
                        MessageUtils.sendMessage(player, "&8&m========================================");
                        MessageUtils.playSound(player, "ENTITY_WITHER_DEATH", 1.0f, 1.0f);
                    }
                } else if (type == Material.IRON_DOOR) {
                    plugin.getSetupManager().exitSetupMode(player, true);
                }
            }
            return;
        }

        // Lobby Items Logic
        GameSession session = plugin.getPlayerSession(player);
        if (session != null) {
            event.setCancelled(true); // Disable placing/interacting with items in lobby/game
            
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (item.getType() == Material.NETHER_STAR) {
                    plugin.openArenaSelector(player);
                } else if (item.getType() == Material.BARRIER) {
                    session.leave(player);
                } else if (plugin.getBazookaManager().isBazooka(item)) {
                    // Seeker firing the Bazooka
                    if (session.getRole(player) == PlayerRole.SEEKER) {
                        plugin.getBazookaManager().fireBazooka(player, item);
                    }
                }
            }
        }
    }

    // --- GUI Interactions ---

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Check if viewing Arena Selector GUI
        if (event.getInventory().getHolder() instanceof ArenaSelectorGUI) {
            event.setCancelled(true); // Stop standard clicking
            
            ArenaSelectorGUI gui = (ArenaSelectorGUI) event.getInventory().getHolder();
            int slot = event.getRawSlot();
            String arenaName = gui.getArenaNameAt(slot);
            
            if (arenaName != null) {
                player.closeInventory();
                
                GameSession session = plugin.getGameSession(arenaName);
                if (session != null) {
                    session.join(player);
                } else {
                    MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.arena-not-found", true).replace("%arena%", arenaName));
                }
            }
            return;
        }

        // Prevent moving items inside active game sessions or setup sessions
        if (plugin.isPlayerInGame(player) || plugin.getSetupManager().isInSetupMode(player)) {
            event.setCancelled(true);
        }
    }

    // --- Block Placement & Break Rules ---

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerInGame(player) || plugin.getSetupManager().isInSetupMode(player)) {
            if (!canBypass(player)) {
                event.setCancelled(true);
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.no-bypass", true));
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerInGame(player) || plugin.getSetupManager().isInSetupMode(player)) {
            if (!canBypass(player)) {
                event.setCancelled(true);
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.no-bypass", true));
            }
        }
    }

    // --- PvP & Damage Control (Melee Tagging) ---

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        GameSession victimSession = plugin.getPlayerSession(victim);
        if (victimSession == null) return;

        // Victim is in the game. Check damager.
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            GameSession attackerSession = plugin.getPlayerSession(attacker);

            // Cancel PvP if not in the same session, or not active
            if (attackerSession != victimSession || victimSession.getArena().getState() != ArenaState.IN_GAME) {
                event.setCancelled(true);
                return;
            }

            PlayerRole attackerRole = attackerSession.getRole(attacker);
            PlayerRole victimRole = victimSession.getRole(victim);

            // Tag check (Seeker hitting Hider)
            if (attackerRole == PlayerRole.SEEKER && victimRole == PlayerRole.HIDER) {
                event.setCancelled(true); // Cancel vanilla damage
                victimSession.eliminateHider(victim, attacker);
            } else {
                event.setCancelled(true); // Block all other hits (hider hitting hider, etc.)
            }
        } else {
            // General PvE damage inside game (mobs, projectiles not from players, fall damage, etc.)
            // Prevent hiders/seekers from dying by cancelling regular PvE damage.
            event.setCancelled(true);
        }
    }

    // --- Item Drop & Pickup Restrict ---

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerInGame(player) || plugin.getSetupManager().isInSetupMode(player)) {
            if (!canBypass(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.isPlayerInGame(player)) {
                if (!canBypass(player)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // --- Hunger Cancellation ---

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.isPlayerInGame(player)) {
                event.setCancelled(true);
                player.setFoodLevel(20);
            }
        }
    }

    // --- Arena Boundary Enforcement ---

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        GameSession session = plugin.getPlayerSession(player);
        if (session == null || session.getArena().getState() != ArenaState.IN_GAME) {
            return;
        }

        Location to = event.getTo();
        Arena arena = session.getArena();

        if (!arena.isInside(to)) {
            // Cancel boundary breach
            event.setTo(event.getFrom());

            // Teleport slightly back to prevent getting stuck
            Location back = event.getFrom().clone().subtract(event.getFrom().getDirection().multiply(0.2));
            if (arena.isInside(back)) {
                player.teleport(back);
            }

            // Warning message with cooldown to prevent chat spam
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            if (!boundaryMessageCooldown.containsKey(uuid) || now - boundaryMessageCooldown.get(uuid) > 2000L) {
                boundaryMessageCooldown.put(uuid, now);
                String msg = plugin.getConfigManager().getMessage("errors.out-of-bounds", true);
                MessageUtils.sendMessage(player, msg);
            }
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerInGame(player) || plugin.getSetupManager().isInSetupMode(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (plugin.isPlayerInGame(player)) {
            event.getDrops().clear();
            event.setKeepInventory(true);
        }
    }

    // --- Player Quit Cleanups ---

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Restore setup mode state if quitting
        if (plugin.getSetupManager().isInSetupMode(player)) {
            plugin.getSetupManager().exitSetupMode(player, true);
        }

        GameSession session = plugin.getPlayerSession(player);
        if (session != null) {
            session.leave(player);
        }
    }
}
