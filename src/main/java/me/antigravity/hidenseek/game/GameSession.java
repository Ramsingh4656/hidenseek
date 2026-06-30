package me.antigravity.hidenseek.game;

import me.antigravity.hidenseek.HideNSeek;
import me.antigravity.hidenseek.arena.Arena;
import me.antigravity.hidenseek.arena.ArenaState;
import me.antigravity.hidenseek.bossbar.HNSBossBar;
import me.antigravity.hidenseek.scoreboard.HNSScoreboard;
import me.antigravity.hidenseek.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Handles the state and running logic of an active game session for an arena.
 */
public class GameSession {

    private final HideNSeek plugin;
    private final Arena arena;
    private final List<UUID> players = new ArrayList<>();
    private final Map<UUID, PlayerRole> roles = new HashMap<>();
    private final Map<UUID, HNSScoreboard> scoreboards = new HashMap<>();
    private HNSBossBar bossBar;

    private BukkitTask countdownTask;
    private BukkitTask gameTask;

    private int countdownSecondsLeft;
    private int matchSecondsLeft;
    private int blindnessSecondsLeft;
    private boolean seekersReleased = false;

    public GameSession(HideNSeek plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
    }

    public Arena getArena() {
        return arena;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public Map<UUID, PlayerRole> getRoles() {
        return roles;
    }

    public PlayerRole getRole(Player player) {
        return roles.get(player.getUniqueId());
    }

    public boolean isSeekersReleased() {
        return seekersReleased;
    }

    /**
     * Attempts to add a player to this game session.
     */
    public boolean join(Player player) {
        UUID uuid = player.getUniqueId();
        if (players.contains(uuid)) {
            return false;
        }

        // Check if player is already in another session
        if (plugin.isPlayerInGame(player)) {
            String msg = plugin.getConfigManager().getMessage("errors.already-in-game", true);
            MessageUtils.sendMessage(player, msg);
            return false;
        }

        // Game state checks
        if (!arena.isEnabled()) {
            String msg = plugin.getConfigManager().getMessage("errors.arena-disabled", true);
            MessageUtils.sendMessage(player, msg);
            return false;
        }
        if (arena.getState() == ArenaState.IN_GAME) {
            String msg = plugin.getConfigManager().getMessage("errors.arena-in-progress", true);
            MessageUtils.sendMessage(player, msg);
            return false;
        }
        if (players.size() >= arena.getMaxPlayers()) {
            String msg = plugin.getConfigManager().getMessage("errors.arena-full", true);
            MessageUtils.sendMessage(player, msg);
            return false;
        }

        // Join successful
        players.add(uuid);
        roles.put(uuid, PlayerRole.LOBBY);
        
        // Save current inventory and state
        plugin.getPlayerManager().savePlayerState(player);

        // Teleport to lobby spawn
        player.teleport(arena.getLobbySpawn());
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0.0f);
        player.setLevel(0);

        // Clear active potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        // Give Lobby Items
        player.getInventory().clear();
        player.getInventory().setItem(0, plugin.getLobbyItemStart());
        player.getInventory().setItem(8, plugin.getLobbyItemLeave());
        player.updateInventory();

        // Initialize custom scoreboard
        String boardTitle = plugin.getConfigManager().getConfig().getString("scoreboard.title", "&6&lHide &f&lN &6&lSeek");
        scoreboards.put(uuid, new HNSScoreboard(player, boardTitle));

        // Broad-cast join message
        String joinMsg = plugin.getConfigManager().getMessage("game.join", true)
                .replace("%player%", player.getName())
                .replace("%count%", String.valueOf(players.size()))
                .replace("%max%", String.valueOf(arena.getMaxPlayers()));
        broadcastMessage(joinMsg);

        // Check if countdown should start automatically
        if (players.size() >= arena.getAutoStartPlayers() && arena.getState() == ArenaState.WAITING) {
            startLobbyCountdown();
        } else {
            // Update scoreboard right away
            updateScoreboards();
        }

        return true;
    }

    /**
     * Starts the countdown manually by a player using the Nether Star.
     */
    public void startLobbyCountdownManual(Player player) {
        if (arena.getState() != ArenaState.WAITING) {
            return;
        }
        if (players.size() < arena.getMinPlayers()) {
            MessageUtils.sendMessage(player, "&cNot enough players.");
            return;
        }
        broadcastMessage("&e" + player.getName() + " &a&lhas manually started the countdown!");
        startLobbyCountdown();
    }

    /**
     * Removes a player from the game session and restores their state.
     */
    public void leave(Player player) {
        UUID uuid = player.getUniqueId();
        if (!players.contains(uuid)) {
            return;
        }

        // Cleanup scoreboard and boss bar
        HNSScoreboard board = scoreboards.remove(uuid);
        if (board != null) {
            board.remove();
        }
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }

        // Remove from session
        players.remove(uuid);
        roles.remove(uuid);
        plugin.getBazookaManager().clearCooldown(player);

        // Restore original state
        plugin.getPlayerManager().restorePlayerState(player);

        // Teleport to global lobby
        Location lobby = plugin.getArenaManager().getGlobalLobby();
        if (lobby == null) {
            lobby = player.getWorld().getSpawnLocation();
        }
        player.teleport(lobby);

        String leaveMsg = plugin.getConfigManager().getMessage("game.leave", true)
                .replace("%player%", player.getName())
                .replace("%count%", String.valueOf(players.size()))
                .replace("%max%", String.valueOf(arena.getMaxPlayers()));
        broadcastMessage(leaveMsg);

        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("game.leave-success", true));

        // Check if game has enough players left
        if (arena.getState() == ArenaState.STARTING && players.size() < arena.getMinPlayers()) {
            cancelLobbyCountdown();
            broadcastMessage("&cNot enough players. Countdown cancelled!");
        }

        if (arena.getState() == ArenaState.IN_GAME) {
            checkWinConditions();
        } else {
            updateScoreboards();
        }
    }

    /**
     * Starts the countdown before the game begins.
     */
    private void startLobbyCountdown() {
        arena.setState(ArenaState.STARTING);
        countdownSecondsLeft = plugin.getConfigManager().getConfig().getInt("game-settings.countdown-seconds", 10);

        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdownSecondsLeft <= 0) {
                    startGame();
                    cancel();
                    return;
                }

                // Play sound
                String cdSound = plugin.getConfigManager().getConfig().getString("sounds.countdown.sound", "BLOCK_NOTE_BLOCK_PLING");
                float vol = (float) plugin.getConfigManager().getConfig().getDouble("sounds.countdown.volume", 1.0);
                float pitch = (float) plugin.getConfigManager().getConfig().getDouble("sounds.countdown.pitch", 1.0);
                
                String actionbarMsg = plugin.getConfigManager().getMessage("game.game-starting-actionbar")
                        .replace("%seconds%", String.valueOf(countdownSecondsLeft));

                for (UUID uuid : players) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        MessageUtils.sendActionBar(p, actionbarMsg);
                        MessageUtils.playSound(p, cdSound, vol, pitch);
                        
                        // Send screen titles for last 5 seconds
                        if (countdownSecondsLeft <= 5) {
                            MessageUtils.sendTitle(p, "&6" + countdownSecondsLeft, "", 0, 20, 0);
                        }
                    }
                }

                if (countdownSecondsLeft == 10 || countdownSecondsLeft <= 5) {
                    String cdMsg = plugin.getConfigManager().getMessage("game.countdown", true)
                            .replace("%seconds%", String.valueOf(countdownSecondsLeft));
                    broadcastMessage(cdMsg);
                }

                updateScoreboards();
                countdownSecondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void cancelLobbyCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        arena.setState(ArenaState.WAITING);
        updateScoreboards();
    }

    /**
     * Starts the game, distributes roles, and sets up timers.
     */
    private void startGame() {
        if (players.size() < arena.getMinPlayers()) {
            cancelLobbyCountdown();
            broadcastMessage("&cGame cancelled due to insufficient players.");
            return;
        }

        arena.setState(ArenaState.IN_GAME);
        seekersReleased = false;
        
        matchSecondsLeft = arena.getTimer();
        blindnessSecondsLeft = plugin.getConfigManager().getConfig().getInt("game-settings.blindness-duration-seconds", 30);

        // Select Random Seeker
        Random rand = new Random();
        int seekerIndex = rand.nextInt(players.size());
        UUID seekerUuid = players.get(seekerIndex);

        // Assign Roles and Teleport
        List<Location> hiderSpawns = arena.getHiderSpawns();
        int spawnIndex = 0;

        for (int i = 0; i < players.size(); i++) {
            UUID uuid = players.get(i);
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;

            p.getInventory().clear();
            p.setGameMode(GameMode.ADVENTURE);

            if (uuid.equals(seekerUuid)) {
                // Seeker Setup
                roles.put(uuid, PlayerRole.SEEKER);
                p.teleport(arena.getSeekerSpawn());
                
                // Blindness for hiding grace period
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessSecondsLeft * 20, 1, false, false));
                
                // Titles
                String title = plugin.getConfigManager().getMessage("game.role-seeker-title");
                String subtitle = plugin.getConfigManager().getMessage("game.role-seeker-subtitle");
                MessageUtils.sendTitle(p, title, subtitle, 10, 60, 10);
            } else {
                // Hider Setup
                roles.put(uuid, PlayerRole.HIDER);
                
                // Distribute hiders across the multiple spawns
                Location spawn = hiderSpawns.get(spawnIndex % hiderSpawns.size());
                p.teleport(spawn);
                spawnIndex++;

                // Make tiny using GENERIC_SCALE
                double hiderScale = plugin.getConfigManager().getConfig().getDouble("game-settings.hider-scale", 0.3);
                plugin.getPlayerManager().setScale(p, hiderScale);

                // Titles
                String title = plugin.getConfigManager().getMessage("game.role-hider-title");
                String subtitle = plugin.getConfigManager().getMessage("game.role-hider-subtitle");
                MessageUtils.sendTitle(p, title, subtitle, 10, 60, 10);
            }
        }

        // Setup BossBar
        String bbTitle = plugin.getConfigManager().getConfig().getString("bossbar.title", "&6Remaining Time: &e%time%");
        String bbColor = plugin.getConfigManager().getConfig().getString("bossbar.color", "RED");
        String bbStyle = plugin.getConfigManager().getConfig().getString("bossbar.style", "PROGRESS");
        bossBar = new HNSBossBar(bbTitle, bbColor, bbStyle);

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                bossBar.addPlayer(p);
            }
        }

        // Play Start Sound
        String startSound = plugin.getConfigManager().getConfig().getString("sounds.game-start.sound", "ENTITY_WITHER_SPAWN");
        float vol = (float) plugin.getConfigManager().getConfig().getDouble("sounds.game-start.volume", 1.0);
        float pitch = (float) plugin.getConfigManager().getConfig().getDouble("sounds.game-start.pitch", 1.0);
        
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                MessageUtils.playSound(p, startSound, vol, pitch);
            }
        }

        // Start Ticking task
        startGameTask();
    }

    /**
     * Ticks the active game duration and the grace blindness duration.
     */
    private void startGameTask() {
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (arena.getState() != ArenaState.IN_GAME) {
                    cancel();
                    return;
                }

                // Handle Match Expiration
                if (matchSecondsLeft <= 0) {
                    endGame(PlayerRole.HIDER); // Hiders Win!
                    cancel();
                    return;
                }

                // Handle Grace Period Countdown
                if (!seekersReleased) {
                    if (blindnessSecondsLeft <= 0) {
                        releaseSeekers();
                    } else {
                        // Warn hiders and announce seconds left
                        String actionbarMsg = plugin.getConfigManager().getMessage("game.blindness-countdown")
                                .replace("%seconds%", String.valueOf(blindnessSecondsLeft));
                        
                        for (UUID uuid : players) {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) {
                                MessageUtils.sendActionBar(p, actionbarMsg);
                            }
                        }

                        if (blindnessSecondsLeft == 15 || blindnessSecondsLeft == 10 || blindnessSecondsLeft <= 5) {
                            String msg = plugin.getConfigManager().getMessage("game.blindness-countdown", true)
                                    .replace("%seconds%", String.valueOf(blindnessSecondsLeft));
                            broadcastMessage(msg);
                        }
                        blindnessSecondsLeft--;
                    }
                }

                // Tick General Game Time
                String timeStr = formatTime(matchSecondsLeft);
                
                // Update Boss bar
                String bbTitle = plugin.getConfigManager().getConfig().getString("bossbar.title", "&6Remaining Time: &e%time%")
                        .replace("%time%", timeStr);
                bossBar.updateTitle(bbTitle);
                bossBar.updateProgress((double) matchSecondsLeft / arena.getTimer());

                // Update Action bar if released
                if (seekersReleased) {
                    String abMsg = plugin.getConfigManager().getMessage("game.time-remaining-actionbar")
                            .replace("%time%", timeStr);
                    for (UUID uuid : players) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            MessageUtils.sendActionBar(p, abMsg);
                        }
                    }
                }

                updateScoreboards();
                matchSecondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    /**
     * Releases Seekers, clears blindness, and gives them the Bazooka weapon.
     */
    private void releaseSeekers() {
        seekersReleased = true;

        String releaseMsg = plugin.getConfigManager().getMessage("game.seekers-released", true);
        broadcastMessage(releaseMsg);

        int maxAmmo = plugin.getConfigManager().getConfig().getInt("bazooka-settings.max-ammo", 10);
        ItemStack bazooka = plugin.getBazookaManager().createBazooka(maxAmmo);

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;

            p.removePotionEffect(PotionEffectType.BLINDNESS);

            if (roles.get(uuid) == PlayerRole.SEEKER) {
                // Give weapon to seeker
                p.getInventory().addItem(bazooka);
                
                // Play special trigger alert
                MessageUtils.playSound(p, "ENTITY_ENDER_DRAGON_GROWL", 1.0f, 1.0f);
                MessageUtils.sendTitle(p, "&c&lRELEASED", "&eFind the Hiders!", 10, 40, 10);
            } else {
                // Warn hiders
                MessageUtils.playSound(p, "ENTITY_WITHER_SPAWN", 0.5f, 1.2f);
                MessageUtils.sendTitle(p, "&c&lSEEKERS OUT!", "&eThey have bazookas!", 10, 40, 10);
            }
        }
    }

    /**
     * Handles the elimination of a Hider (either melee tag or projectile blast).
     * Turns the Hider into a Seeker and hands them a Bazooka.
     */
    public void eliminateHider(Player hider, Player seeker) {
        UUID hiderUuid = hider.getUniqueId();
        if (roles.get(hiderUuid) != PlayerRole.HIDER) {
            return;
        }

        // Change Role to Seeker
        roles.put(hiderUuid, PlayerRole.SEEKER);

        // Restore scale back to normal (1.0)
        plugin.getPlayerManager().setScale(hider, 1.0);

        // Give Bazooka
        hider.getInventory().clear();
        int maxAmmo = plugin.getConfigManager().getConfig().getInt("bazooka-settings.max-ammo", 10);
        hider.getInventory().addItem(plugin.getBazookaManager().createBazooka(maxAmmo));

        // Visual and Sound triggers
        String tagSound = plugin.getConfigManager().getConfig().getString("sounds.player-found.sound", "ENTITY_LIGHTNING_BOLT_THUNDER");
        float vol = (float) plugin.getConfigManager().getConfig().getDouble("sounds.player-found.volume", 1.0);
        float pitch = (float) plugin.getConfigManager().getConfig().getDouble("sounds.player-found.pitch", 1.0);

        String broadcastTag = plugin.getConfigManager().getMessage("game.player-found", true)
                .replace("%hider%", hider.getName())
                .replace("%seeker%", seeker != null ? seeker.getName() : "Bazooka");
        broadcastMessage(broadcastTag);

        String abMsg = plugin.getConfigManager().getMessage("game.player-found-actionbar")
                .replace("%hider%", hider.getName());

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                MessageUtils.sendActionBar(p, abMsg);
                MessageUtils.playSound(p, tagSound, vol, pitch);
            }
        }

        // Title to tagged player
        MessageUtils.sendTitle(hider, "&c&lELIMINATED", "&eYou are now a Seeker!", 10, 40, 10);

        // Check if game is over
        checkWinConditions();
    }

    /**
     * Evaluates active player roles to see if win conditions are met.
     */
    public void checkWinConditions() {
        if (arena.getState() != ArenaState.IN_GAME) {
            return;
        }

        int hidersCount = 0;
        int seekersCount = 0;
        UUID lastHider = null;

        for (UUID uuid : players) {
            PlayerRole r = roles.get(uuid);
            if (r == PlayerRole.HIDER) {
                hidersCount++;
                lastHider = uuid;
            } else if (r == PlayerRole.SEEKER) {
                seekersCount++;
            }
        }

        // No hiders left -> Seekers Win!
        if (hidersCount == 0) {
            endGame(PlayerRole.SEEKER);
            return;
        }

        // No seekers left -> Select a random Hider to become the Seeker!
        if (seekersCount == 0 && hidersCount > 0) {
            Player randomHider = Bukkit.getPlayer(lastHider);
            if (randomHider != null) {
                roles.put(lastHider, PlayerRole.SEEKER);
                plugin.getPlayerManager().setScale(randomHider, 1.0);
                
                if (seekersReleased) {
                    int maxAmmo = plugin.getConfigManager().getConfig().getInt("bazooka-settings.max-ammo", 10);
                    randomHider.getInventory().addItem(plugin.getBazookaManager().createBazooka(maxAmmo));
                } else {
                    randomHider.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessSecondsLeft * 20, 1, false, false));
                }

                broadcastMessage("&eAll Seekers left! &6" + randomHider.getName() + " &ehas been selected as the new Seeker.");
                MessageUtils.sendTitle(randomHider, "&c&lNEW SEEKER", "&eAll seekers left! Find the Hiders!", 10, 50, 10);
                
                // Recheck in case they were the only hider
                checkWinConditions();
            }
        }
    }

    /**
     * Ends the game, playing victory/defeat sounds and sending titles to players.
     * Restores all inventories and spawns them back at the global lobby.
     */
    private void endGame(PlayerRole winners) {
        arena.setState(ArenaState.WAITING);

        // Cancel tasks
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        // Load configs
        String winSound = plugin.getConfigManager().getConfig().getString("sounds.victory.sound", "UI_TOAST_CHALLENGE_COMPLETE");
        float winVol = (float) plugin.getConfigManager().getConfig().getDouble("sounds.victory.volume", 1.0);
        float winPitch = (float) plugin.getConfigManager().getConfig().getDouble("sounds.victory.pitch", 1.0);

        String lossSound = plugin.getConfigManager().getConfig().getString("sounds.defeat.sound", "ENTITY_WITHER_DEATH");
        float lossVol = (float) plugin.getConfigManager().getConfig().getDouble("sounds.defeat.volume", 1.0);
        float lossPitch = (float) plugin.getConfigManager().getConfig().getDouble("sounds.defeat.pitch", 0.8);

        String winTitle = "";
        String winSubtitle = "";

        if (winners == PlayerRole.HIDER) {
            winTitle = plugin.getConfigManager().getMessage("game.hiders-win-title");
            winSubtitle = plugin.getConfigManager().getMessage("game.hiders-win-subtitle");
            broadcastMessage("&a&lGame Over! Hiders Win!");
        } else if (winners == PlayerRole.SEEKER) {
            winTitle = plugin.getConfigManager().getMessage("game.seekers-win-title");
            winSubtitle = plugin.getConfigManager().getMessage("game.seekers-win-subtitle");
            broadcastMessage("&c&lGame Over! Seekers Win!");
        }

        // Capture players copy to prevent ConcurrentModificationException when leaving
        List<UUID> playersCopy = new ArrayList<>(players);
        for (UUID uuid : playersCopy) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;

            PlayerRole playerRole = roles.get(uuid);

            // Display Title
            MessageUtils.sendTitle(p, winTitle, winSubtitle, 10, 80, 10);

            // Play win/loss sound
            if (playerRole == winners) {
                MessageUtils.playSound(p, winSound, winVol, winPitch);
            } else {
                MessageUtils.playSound(p, lossSound, lossVol, lossPitch);
            }

            // Trigger leave cleanup (teleport to lobby, restore items, etc.)
            leave(p);
        }

        // Clean up boss bar
        if (bossBar != null) {
            bossBar.remove();
            bossBar = null;
        }

        players.clear();
        roles.clear();
        scoreboards.clear();
    }

    /**
     * Forces the match to stop (e.g. via command `/hns stop`).
     */
    public void stop() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        broadcastMessage("&cThe game was forcibly stopped by an administrator.");

        List<UUID> playersCopy = new ArrayList<>(players);
        for (UUID uuid : playersCopy) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                leave(p);
            }
        }

        if (bossBar != null) {
            bossBar.remove();
            bossBar = null;
        }

        players.clear();
        roles.clear();
        scoreboards.clear();
        
        arena.setState(ArenaState.WAITING);
    }

    /**
     * Updates the scoreboards of all active players in this session.
     */
    public void updateScoreboards() {
        int seekers = 0;
        int hiders = 0;
        for (PlayerRole role : roles.values()) {
            if (role == PlayerRole.HIDER) hiders++;
            if (role == PlayerRole.SEEKER) seekers++;
        }

        String timeStr = arena.getState() == ArenaState.IN_GAME ? formatTime(matchSecondsLeft) : "00:00";
        List<String> linesTemplate = plugin.getConfigManager().getConfig().getStringList("scoreboard.lines");

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            HNSScoreboard sb = scoreboards.get(uuid);
            if (p == null || sb == null) continue;

            List<String> formattedLines = new ArrayList<>();
            for (String line : linesTemplate) {
                String formatted = line.replace("%arena%", arena.getName())
                                      .replace("%time%", timeStr)
                                      .replace("%seekers_count%", String.valueOf(seekers))
                                      .replace("%hiders_count%", String.valueOf(hiders))
                                      .replace("%players_count%", String.valueOf(players.size()))
                                      .replace("%max_players%", String.valueOf(arena.getMaxPlayers()));
                formattedLines.add(formatted);
            }
            sb.update(formattedLines);
        }
    }

    /**
     * Formats an integer number of seconds to "MM:SS".
     */
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Broadcasts a colored chat message to all players in the session.
     */
    public void broadcastMessage(String message) {
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                MessageUtils.sendMessage(p, message);
            }
        }
    }
}
