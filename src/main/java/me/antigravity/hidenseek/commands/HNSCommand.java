package me.antigravity.hidenseek.commands;

import me.antigravity.hidenseek.HideNSeek;
import me.antigravity.hidenseek.arena.Arena;
import me.antigravity.hidenseek.arena.ArenaState;
import me.antigravity.hidenseek.game.GameSession;
import me.antigravity.hidenseek.utils.MessageUtils;
import me.antigravity.hidenseek.arena.SetupManager.SetupSession;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Redesigned command processor for all `/hns` commands.
 * Keeps commands minimal and delegates configuration to the interactive setup GUI items.
 */
public class HNSCommand implements CommandExecutor, TabCompleter {

    private final HideNSeek plugin;

    public HNSCommand(HideNSeek plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            // Default player action: Open Match Join GUI
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("hns.join")) {
                    plugin.openArenaSelector(player);
                } else {
                    MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.no-permission", true));
                }
            } else {
                sender.sendMessage("HideNSeek plugin version 1.0.1. Run /hns help for commands.");
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help":
                sendHelp(sender);
                break;
            case "create":
                handleCreate(sender, args);
                break;
            case "delete":
                handleDelete(sender, args);
                break;
            case "setup":
                handleSetup(sender, args);
                break;
            case "start":
                handleStart(sender, args);
                break;
            case "stop":
                handleStop(sender, args);
                break;
            case "join":
                handleJoin(sender, args);
                break;
            case "leave":
                handleLeave(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "info":
                handleInfo(sender, args);
                break;
            case "setlobbyboundary":
                handleSetBoundary(sender, args, "lobby");
                break;
            case "setseekerboundary":
                handleSetBoundary(sender, args, "seeker");
                break;
            case "sethiderboundary":
                handleSetBoundary(sender, args, "hider");
                break;
            default:
                MessageUtils.sendMessage(sender, "&c&lERROR! &cUnknown subcommand. Run &e/hns help &cfor a list of commands.");
                break;
        }

        return true;
    }

    private boolean checkPermission(CommandSender sender, String node) {
        if (sender.hasPermission("hns.admin") || sender.hasPermission(node)) {
            return true;
        }
        MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.no-permission", true));
        return false;
    }

    private void sendHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "&8&m========================================");
        MessageUtils.sendMessage(sender, "  &6&lHideNSeek Commands &7(v1.0.1):");
        MessageUtils.sendMessage(sender, "");
        if (sender.hasPermission("hns.join") || sender.hasPermission("hns.admin")) {
            MessageUtils.sendMessage(sender, "  &e/hns join [arena] &7- Opens GUI selector or joins directly");
            MessageUtils.sendMessage(sender, "  &e/hns leave &7- Leaves the current game session");
        }
        if (sender.hasPermission("hns.setup") || sender.hasPermission("hns.admin")) {
            MessageUtils.sendMessage(sender, "  &e/hns create <arena> &7- Creates a new empty arena");
            MessageUtils.sendMessage(sender, "  &e/hns delete <arena> &7- Deletes an arena and its data");
            MessageUtils.sendMessage(sender, "  &e/hns setup <arena> &7- Enters interactive setup mode");
            MessageUtils.sendMessage(sender, "  &e/hns info <arena> &7- Prints detailed arena config status");
        }
        if (sender.hasPermission("hns.start") || sender.hasPermission("hns.admin")) {
            MessageUtils.sendMessage(sender, "  &e/hns start <arena> &7- Forces the match to start countdown");
        }
        if (sender.hasPermission("hns.stop") || sender.hasPermission("hns.admin")) {
            MessageUtils.sendMessage(sender, "  &e/hns stop <arena> &7- Forces the active match to stop");
        }
        if (sender.hasPermission("hns.reload") || sender.hasPermission("hns.admin")) {
            MessageUtils.sendMessage(sender, "  &e/hns reload &7- Reloads config and arena configuration files");
        }
        MessageUtils.sendMessage(sender, "&8&m========================================");
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "hns.setup")) return;
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.invalid-args", true).replace("%usage%", "/hns create <arena_name>"));
            return;
        }

        String name = args[1];
        Arena arena = plugin.getArenaManager().createArena(name);
        if (arena == null) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.arena-already-exists", true).replace("%arena%", name));
        } else {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("admin.arena-created", true).replace("%arena%", name));
            plugin.createGameSession(arena); // Register live session
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "hns.setup")) return;
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.invalid-args", true).replace("%usage%", "/hns delete <arena_name>"));
            return;
        }

        String name = args[1];
        GameSession session = plugin.getGameSession(name);
        if (session != null) {
            session.stop();
            plugin.removeGameSession(name);
        }

        boolean success = plugin.getArenaManager().deleteArena(name);
        if (success) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("admin.arena-deleted", true).replace("%arena%", name));
        } else {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.arena-not-found", true).replace("%arena%", name));
        }
    }

    private void handleSetup(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.only-players", false));
            return;
        }
        if (!checkPermission(sender, "hns.setup")) return;
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.invalid-args", true).replace("%usage%", "/hns setup <arena_name>"));
            return;
        }

        Player player = (Player) sender;
        String name = args[1];
        Arena arena = plugin.getArenaManager().getArena(name);
        if (arena == null) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.arena-not-found", true).replace("%arena%", name));
            return;
        }

        // Toggle Setup Mode via SetupManager
        if (plugin.getSetupManager().isInSetupMode(player)) {
            plugin.getSetupManager().exitSetupMode(player, true);
        } else {
            plugin.getSetupManager().enterSetupMode(player, arena);
        }
    }

    private void handleStart(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "hns.start")) return;
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.invalid-args", true).replace("%usage%", "/hns start <arena_name>"));
            return;
        }

        String name = args[1];
        GameSession session = plugin.getGameSession(name);
        if (session == null) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.arena-not-found", true).replace("%arena%", name));
            return;
        }

        Arena arena = session.getArena();
        if (!arena.isEnabled()) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.arena-disabled", true).replace("%arena%", name));
            return;
        }

        if (arena.getState() == ArenaState.IN_GAME) {
            MessageUtils.sendMessage(sender, "&c&lERROR! &cThe match is already in progress.");
            return;
        }

        if (session.getPlayers().size() < arena.getMinPlayers()) {
            MessageUtils.sendMessage(sender, "&c&lERROR! &cCannot start. The lobby only has " + session.getPlayers().size() + "/" + arena.getMinPlayers() + " players.");
            return;
        }

        session.updateScoreboards();
        MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("admin.start-success", true).replace("%arena%", name));
    }

    private void handleStop(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "hns.stop")) return;
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.invalid-args", true).replace("%usage%", "/hns stop <arena_name>"));
            return;
        }

        String name = args[1];
        GameSession session = plugin.getGameSession(name);
        if (session == null) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.arena-not-found", true).replace("%arena%", name));
            return;
        }

        session.stop();
        MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("admin.stop-success", true).replace("%arena%", name));
    }

    private void handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.only-players", false));
            return;
        }
        if (!checkPermission(sender, "hns.join")) return;

        Player player = (Player) sender;
        if (args.length < 2) {
            // Open matching selector chest UI
            plugin.openArenaSelector(player);
            return;
        }

        String name = args[1];
        GameSession session = plugin.getGameSession(name);
        if (session == null) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.arena-not-found", true).replace("%arena%", name));
            return;
        }

        if (!session.getArena().isEnabled()) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.arena-disabled-join", true).replace("%arena%", name));
            return;
        }

        session.join(player);
    }

    private void handleLeave(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.only-players", false));
            return;
        }

        Player player = (Player) sender;
        GameSession session = plugin.getPlayerSession(player);
        if (session == null) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.not-in-game", true));
            return;
        }

        session.leave(player);
    }

    private void handleReload(CommandSender sender) {
        if (!checkPermission(sender, "hns.reload")) return;

        // Safely shut down all live sessions to protect cached player data
        if (plugin.getGameService() != null) {
            plugin.getGameService().stopAll();
        }

        plugin.getConfigManager().reload();
        plugin.getArenaManager().loadArenas();

        // Re-register clean sessions for reloaded arenas
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            plugin.createGameSession(arena);
        }

        MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("admin.reload", true));
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "hns.setup")) return;
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.invalid-args", true).replace("%usage%", "/hns info <arena_name>"));
            return;
        }

        String name = args[1];
        Arena arena = plugin.getArenaManager().getArena(name);
        if (arena == null) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.arena-not-found", true).replace("%arena%", name));
            return;
        }

        GameSession session = plugin.getGameSession(arena.getName());
        int currentPlayers = session != null ? session.getPlayers().size() : 0;
        String stateStr = arena.getState() != null ? arena.getState().toString() : "DISABLED";

        MessageUtils.sendMessage(sender, "&8&m========================================");
        MessageUtils.sendMessage(sender, "  &6&lArena Information: &e" + arena.getName());
        MessageUtils.sendMessage(sender, "");
        MessageUtils.sendMessage(sender, "  &eStatus: &b" + stateStr);
        MessageUtils.sendMessage(sender, "  &eEnabled: " + (arena.isEnabled() ? "&2Yes" : "&cNo"));
        MessageUtils.sendMessage(sender, "  &ePlayers: &b" + currentPlayers + "&7/&b" + arena.getMaxPlayers());
        MessageUtils.sendMessage(sender, "");
        MessageUtils.sendMessage(sender, "  &eLobby Spawn: " + formatLoc(arena.getLobbySpawn()));
        MessageUtils.sendMessage(sender, "  &eLobby Boundary: " + formatBound(arena.getLobbyPos1(), arena.getLobbyPos2()));
        MessageUtils.sendMessage(sender, "");
        MessageUtils.sendMessage(sender, "  &eSeeker Spawn: " + formatLoc(arena.getSeekerSpawn()));
        MessageUtils.sendMessage(sender, "  &eSeeker Boundary: " + formatBound(arena.getSeekerPos1(), arena.getSeekerPos2()));
        MessageUtils.sendMessage(sender, "");
        MessageUtils.sendMessage(sender, "  &eHider Spawns: &b" + arena.getHiderSpawns().size() + " registered");
        MessageUtils.sendMessage(sender, "  &eHider Boundary: " + formatBound(arena.getHiderPos1(), arena.getHiderPos2()));
        MessageUtils.sendMessage(sender, "");
        MessageUtils.sendMessage(sender, "  &eTimer: &b" + arena.getTimer() + "s");
        MessageUtils.sendMessage(sender, "  &eMinimum Players: &b" + arena.getMinPlayers());
        MessageUtils.sendMessage(sender, "  &eMaximum Players: &b" + arena.getMaxPlayers());
        MessageUtils.sendMessage(sender, "  &eAuto Start Players: &b" + arena.getAutoStartPlayers());
        MessageUtils.sendMessage(sender, "&8&m========================================");
    }

    private String formatLoc(Location loc) {
        if (loc == null) return "&cNot Set";
        return String.format("&a%d, %d, %d &7(%s)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
    }

    private String formatBound(Location p1, Location p2) {
        if (p1 == null || p2 == null) return "&cNot Set";
        return String.format("&a(%d, %d, %d) &7to &a(%d, %d, %d) &7(%s)", 
                p1.getBlockX(), p1.getBlockY(), p1.getBlockZ(),
                p2.getBlockX(), p2.getBlockY(), p2.getBlockZ(),
                p1.getWorld().getName());
    }

    private void handleSetBoundary(CommandSender sender, String[] args, String boundaryType) {
        if (!checkPermission(sender, "hns.setup")) return;
        
        Player player = null;
        Arena arena = null;
        
        if (sender instanceof Player) {
            player = (Player) sender;
            if (plugin.getSetupManager().isInSetupMode(player)) {
                arena = plugin.getSetupManager().getSession(player).getArena();
            }
        }
        
        if (arena == null) {
            if (args.length < 2) {
                MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.invalid-args", true)
                        .replace("%usage%", "/hns set" + boundaryType + "boundary <arena_name>"));
                return;
            }
            String name = args[1];
            arena = plugin.getArenaManager().getArena(name);
            if (arena == null) {
                MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.arena-not-found", true).replace("%arena%", name));
                return;
            }
        }
        
        if (player == null) {
            sender.sendMessage("Only players can set boundaries because selections are player-specific.");
            return;
        }
        
        Location pos1 = plugin.getSetupManager().getSelectionPos1(player);
        Location pos2 = plugin.getSetupManager().getSelectionPos2(player);
        
        if (pos1 == null || pos2 == null) {
            MessageUtils.sendMessage(player, "&c&lERROR! &cYou must set both pos1 and pos2 first using the Region Wand.");
            return;
        }
        
        switch (boundaryType) {
            case "lobby":
                arena.setLobbyPos1(pos1);
                arena.setLobbyPos2(pos2);
                break;
            case "seeker":
                arena.setSeekerPos1(pos1);
                arena.setSeekerPos2(pos2);
                break;
            case "hider":
                arena.setHiderPos1(pos1);
                arena.setHiderPos2(pos2);
                break;
        }
        
        MessageUtils.sendMessage(player, "&a&lSUCCESS! &e" + boundaryType.substring(0, 1).toUpperCase() + boundaryType.substring(1) + " boundary set for arena '&6" + arena.getName() + "&e'.");
        plugin.getSetupManager().printChecklist(player, arena);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("help");
            if (sender.hasPermission("hns.join") || sender.hasPermission("hns.admin")) {
                list.add("join");
                list.add("leave");
            }
            if (sender.hasPermission("hns.setup") || sender.hasPermission("hns.admin")) {
                list.add("create");
                list.add("delete");
                list.add("setup");
                list.add("info");
                list.add("setlobbyboundary");
                list.add("setseekerboundary");
                list.add("sethiderboundary");
            }
            if (sender.hasPermission("hns.start") || sender.hasPermission("hns.admin")) {
                list.add("start");
            }
            if (sender.hasPermission("hns.stop") || sender.hasPermission("hns.admin")) {
                list.add("stop");
            }
            if (sender.hasPermission("hns.reload") || sender.hasPermission("hns.admin")) {
                list.add("reload");
            }
            return filterList(list, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("join") || sub.equals("start") || sub.equals("stop") ||
                sub.equals("setup") || sub.equals("info") || sub.equals("delete") ||
                sub.equals("setlobbyboundary") || sub.equals("setseekerboundary") || sub.equals("sethiderboundary")) {
                
                List<String> arenaNames = plugin.getArenaManager().getArenas().stream()
                        .map(Arena::getName)
                        .collect(Collectors.toList());
                return filterList(arenaNames, args[1]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterList(List<String> list, String query) {
        if (query == null || query.isEmpty()) {
            return list;
        }
        String lower = query.toLowerCase();
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
