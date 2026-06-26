package me.antigravity.hidenseek.commands;

import me.antigravity.hidenseek.HideNSeek;
import me.antigravity.hidenseek.arena.Arena;
import me.antigravity.hidenseek.arena.ArenaManager;
import me.antigravity.hidenseek.arena.ArenaState;
import me.antigravity.hidenseek.game.GameSession;
import me.antigravity.hidenseek.utils.LocationUtils;
import me.antigravity.hidenseek.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all commands matching `/hns [subcommand]`.
 */
public class HNSCommand implements CommandExecutor, TabCompleter {

    private final HideNSeek plugin;

    public HNSCommand(HideNSeek plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            // Default: Send version/info or join list
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("hns.join")) {
                    plugin.openArenaSelector(player);
                } else {
                    MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.no-permission", true));
                }
            } else {
                sender.sendMessage("HideNSeek plugin version 1.0.0. Run /hns help for commands.");
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
            case "list":
                handleList(sender);
                break;
            case "info":
                handleInfo(sender, args);
                break;
            case "edit":
                handleEdit(sender, args);
                break;
            case "setlobby":
                handleSetLobby(sender);
                break;
            case "setseekerspawn":
                handleSetSeekerSpawn(sender, args);
                break;
            case "addhiderspawn":
                handleAddHiderSpawn(sender, args);
                break;
            case "wand":
                handleWand(sender);
                break;
            case "pos1":
                handlePos1(sender);
                break;
            case "pos2":
                handlePos2(sender);
                break;
            case "setregion":
                handleSetRegion(sender, args);
                break;
            case "start":
                handleStart(sender, args);
                break;
            case "stop":
                handleStop(sender, args);
                break;
            case "restart":
                handleRestart(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "save":
                handleSave(sender);
                break;
            case "join":
                handleJoin(sender, args);
                break;
            case "leave":
                handleLeave(sender);
                break;
            default:
                MessageUtils.sendMessage(sender, "&cUnknown subcommand. Use &e/hns help &cfor list.");
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
        MessageUtils.sendMessage(sender, "&6&lHideNSeek Commands:");
        if (sender.hasPermission("hns.join") || sender.hasPermission("hns.admin")) {
            MessageUtils.sendMessage(sender, " &e/hns join <arena> &7- Joins an arena");
            MessageUtils.sendMessage(sender, " &e/hns leave &7- Leaves your current game");
            MessageUtils.sendMessage(sender, " &e/hns list &7- Lists all arenas");
        }
        if (sender.hasPermission("hns.setup") || sender.hasPermission("hns.admin")) {
            MessageUtils.sendMessage(sender, " &e/hns create <arena> &7- Creates an arena");
            MessageUtils.sendMessage(sender, " &e/hns delete <arena> &7- Deletes an arena");
            MessageUtils.sendMessage(sender, " &e/hns edit <arena> &7- Toggles editing session for an arena");
            MessageUtils.sendMessage(sender, " &e/hns setlobby &7- Sets global game lobby");
            MessageUtils.sendMessage(sender, " &e/hns setseekerspawn [arena] &7- Sets seeker spawn");
            MessageUtils.sendMessage(sender, " &e/hns addhiderspawn [arena] &7- Adds a hider spawn point");
            MessageUtils.sendMessage(sender, " &e/hns wand &7- Gives region setup wand");
            MessageUtils.sendMessage(sender, " &e/hns pos1 &7- Sets pos1 to current location");
            MessageUtils.sendMessage(sender, " &e/hns pos2 &7- Sets pos2 to current location");
            MessageUtils.sendMessage(sender, " &e/hns setregion [arena] &7- Sets region boundary based on selection");
            MessageUtils.sendMessage(sender, " &e/hns save &7- Saves all arena data files");
            MessageUtils.sendMessage(sender, " &e/hns info <arena> &7- View config info of an arena");
        }
        if (sender.hasPermission("hns.start") || sender.hasPermission("hns.admin")) {
            MessageUtils.sendMessage(sender, " &e/hns start <arena> &7- Forces a match to start");
            MessageUtils.sendMessage(sender, " &e/hns restart <arena> &7- Restarts an arena match");
        }
        if (sender.hasPermission("hns.stop") || sender.hasPermission("hns.admin")) {
            MessageUtils.sendMessage(sender, " &e/hns stop <arena> &7- Forces a match to stop");
        }
        if (sender.hasPermission("hns.reload") || sender.hasPermission("hns.admin")) {
            MessageUtils.sendMessage(sender, " &e/hns reload &7- Reloads config.yml and messages.yml");
        }
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
            plugin.createGameSession(arena); // Register session
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "hns.setup")) return;
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.invalid-args", true).replace("%usage%", "/hns delete <arena_name>"));
            return;
        }

        String name = args[1];
        // Stop session if active
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

    private void handleList(CommandSender sender) {
        if (!checkPermission(sender, "hns.join")) return;
        Collection<Arena> arenas = plugin.getArenaManager().getArenas();
        if (arenas.isEmpty()) {
            MessageUtils.sendMessage(sender, "&eNo arenas found. Use &7/hns create &eto add one.");
            return;
        }

        MessageUtils.sendMessage(sender, "&6&lHideNSeek Arenas:");
        for (Arena arena : arenas) {
            GameSession session = plugin.getGameSession(arena.getName());
            int pCount = session != null ? session.getPlayers().size() : 0;
            String status = arena.isEnabled() ? (arena.getState().toString()) : "DISABLED";
            MessageUtils.sendMessage(sender, " &7- &e" + arena.getName() + " &a(" + pCount + "/" + arena.getMaxPlayers() + ") &7- Status: &b" + status);
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "hns.setup")) return;
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.invalid-args", true).replace("%usage%", "/hns info <arena_name>"));
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.arena-not-found", true).replace("%arena%", args[1]));
            return;
        }

        MessageUtils.sendMessage(sender, "&6&lArena Information for &e" + arena.getName() + ":");
        MessageUtils.sendMessage(sender, " &eEnabled: " + (arena.isEnabled() ? "&aYes" : "&cNo"));
        MessageUtils.sendMessage(sender, " &eConfigured: " + (arena.isConfigured() ? "&aYes" : "&cNo"));
        MessageUtils.sendMessage(sender, " &eState: &b" + arena.getState());
        MessageUtils.sendMessage(sender, " &eLobby Spawn: &7" + (arena.getLobbySpawn() != null ? LocationUtils.serialize(arena.getLobbySpawn()) : "&cNot Set"));
        MessageUtils.sendMessage(sender, " &eSeeker Spawn: &7" + (arena.getSeekerSpawn() != null ? LocationUtils.serialize(arena.getSeekerSpawn()) : "&cNot Set"));
        MessageUtils.sendMessage(sender, " &eHider Spawns Count: &b" + arena.getHiderSpawns().size());
        MessageUtils.sendMessage(sender, " &eMin/Max Players: &b" + arena.getMinPlayers() + "/" + arena.getMaxPlayers());
        MessageUtils.sendMessage(sender, " &eMatch Timer: &b" + arena.getTimer() + "s");
        MessageUtils.sendMessage(sender, " &eRegion Pos1: &7" + (arena.getPos1() != null ? LocationUtils.serialize(arena.getPos1()) : "&cNot Set"));
        MessageUtils.sendMessage(sender, " &eRegion Pos2: &7" + (arena.getPos2() != null ? LocationUtils.serialize(arena.getPos2()) : "&cNot Set"));
    }

    private void handleEdit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.only-players", false));
            return;
        }
        if (!checkPermission(sender, "hns.setup")) return;
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.invalid-args", true).replace("%usage%", "/hns edit <arena_name>"));
            return;
        }

        Player player = (Player) sender;
        String name = args[1];
        Arena arena = plugin.getArenaManager().getArena(name);
        if (arena == null) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.open-arena-selector", true).replace("%arena%", name));
            return;
        }

        String currentlyEditing = plugin.getArenaManager().getEditingArena(player.getUniqueId());
        if (name.equalsIgnoreCase(currentlyEditing)) {
            plugin.getArenaManager().setEditingArena(player.getUniqueId(), null);
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("admin.edit-mode-off", true));
        } else {
            plugin.getArenaManager().setEditingArena(player.getUniqueId(), arena.getName());
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("admin.edit-mode-on", true).replace("%arena%", arena.getName()));
        }
    }

    private void handleSetLobby(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.only-players", false));
            return;
        }
        if (!checkPermission(sender, "hns.setup")) return;

        Player player = (Player) sender;
        plugin.getArenaManager().setGlobalLobby(player.getLocation());
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("admin.lobby-set", true));
    }

    private void handleSetSeekerSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.only-players", false));
            return;
        }
        if (!checkPermission(sender, "hns.setup")) return;

        Player player = (Player) sender;
        Arena arena = getTargetArena(player, args);
        if (arena == null) return;

        arena.setSeekerSpawn(player.getLocation());
        plugin.getArenaManager().saveArena(arena);
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("admin.seeker-spawn-set", true).replace("%arena%", arena.getName()));
    }

    private void handleAddHiderSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.only-players", false));
            return;
        }
        if (!checkPermission(sender, "hns.setup")) return;

        Player player = (Player) sender;
        Arena arena = getTargetArena(player, args);
        if (arena == null) return;

        arena.addHiderSpawn(player.getLocation());
        plugin.getArenaManager().saveArena(arena);
        int index = arena.getHiderSpawns().size();
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("admin.hider-spawn-added", true)
                .replace("%index%", String.valueOf(index))
                .replace("%arena%", arena.getName()));
    }

    private void handleWand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.only-players", false));
            return;
        }
        if (!checkPermission(sender, "hns.setup")) return;

        Player player = (Player) sender;
        ItemStack wand = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtils.color("&6&lHideNSeek Setup Wand"));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtils.color("&eLeft-click Block: &7Set Pos 1"));
            lore.add(MessageUtils.color("&eRight-click Block: &7Set Pos 2"));
            meta.lore(lore);
            wand.setItemMeta(meta);
        }
        player.getInventory().addItem(wand);
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("admin.wand-given", true));
    }

    private void handlePos1(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.only-players", false));
            return;
        }
        if (!checkPermission(sender, "hns.setup")) return;

        Player player = (Player) sender;
        Location loc = player.getLocation();
        plugin.getArenaManager().setPos1(player.getUniqueId(), loc);
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("admin.pos1-set", true)
                .replace("%x%", String.format("%.1f", loc.getX()))
                .replace("%y%", String.format("%.1f", loc.getY()))
                .replace("%z%", String.format("%.1f", loc.getZ()))
                .replace("%world%", loc.getWorld().getName()));
    }

    private void handlePos2(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.only-players", false));
            return;
        }
        if (!checkPermission(sender, "hns.setup")) return;

        Player player = (Player) sender;
        Location loc = player.getLocation();
        plugin.getArenaManager().setPos2(player.getUniqueId(), loc);
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("admin.pos2-set", true)
                .replace("%x%", String.format("%.1f", loc.getX()))
                .replace("%y%", String.format("%.1f", loc.getY()))
                .replace("%z%", String.format("%.1f", loc.getZ()))
                .replace("%world%", loc.getWorld().getName()));
    }

    private void handleSetRegion(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.only-players", false));
            return;
        }
        if (!checkPermission(sender, "hns.setup")) return;

        Player player = (Player) sender;
        Arena arena = getTargetArena(player, args);
        if (arena == null) return;

        Location[] selection = plugin.getArenaManager().getSelection(player.getUniqueId());
        if (selection[0] == null || selection[1] == null) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.pos-not-set", true));
            return;
        }

        arena.setPos1(selection[0]);
        arena.setPos2(selection[1]);
        plugin.getArenaManager().saveArena(arena);
        MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("admin.region-set", true).replace("%arena%", arena.getName()));
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

        if (session.getArena().getState() == ArenaState.IN_GAME) {
            MessageUtils.sendMessage(sender, "&cArena is already in progress.");
            return;
        }

        // Force join lobby if they aren't in, or just force game to start
        if (session.getPlayers().size() < session.getArena().getMinPlayers()) {
            MessageUtils.sendMessage(sender, "&cCannot force start. Arena has " + session.getPlayers().size() + "/" + session.getArena().getMinPlayers() + " players.");
            return;
        }

        // Trigger starting flow
        session.updateScoreboards(); // Sync
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

    private void handleRestart(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "hns.start")) return;
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.invalid-args", true).replace("%usage%", "/hns restart <arena_name>"));
            return;
        }

        String name = args[1];
        GameSession session = plugin.getGameSession(name);
        if (session == null) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.arena-not-found", true).replace("%arena%", name));
            return;
        }

        session.stop();
        MessageUtils.sendMessage(sender, "&aArena stopped, starting countdown in 2 seconds...");
        
        // Wait and start lobby countdown again if players join
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Nothing to do if they left on stop, otherwise players would be empty
        }, 40L);
    }

    private void handleReload(CommandSender sender) {
        if (!checkPermission(sender, "hns.reload")) return;
        plugin.getConfigManager().reload();
        plugin.getArenaManager().loadArenas();
        
        // Re-synchronize configs inside sessions
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            GameSession session = plugin.getGameSession(arena.getName());
            if (session != null) {
                session.updateScoreboards();
            }
        }
        MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("admin.reload", true));
    }

    private void handleSave(CommandSender sender) {
        if (!checkPermission(sender, "hns.setup")) return;
        plugin.getArenaManager().saveArenas();
        MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("admin.save", true));
    }

    private void handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.only-players", false));
            return;
        }
        if (!checkPermission(sender, "hns.join")) return;
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("errors.invalid-args", true).replace("%usage%", "/hns join <arena_name>"));
            return;
        }

        Player player = (Player) sender;
        String name = args[1];
        GameSession session = plugin.getGameSession(name);
        if (session == null) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.arena-not-found", true).replace("%arena%", name));
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

    /**
     * Resolves the target Arena, either from argument, or falling back to the editing session of the player.
     */
    private Arena getTargetArena(Player player, String[] args) {
        if (args.length >= 2) {
            Arena arena = plugin.getArenaManager().getArena(args[1]);
            if (arena == null) {
                MessageUtils.sendMessage(player, plugin.getConfigManager().getMessage("errors.arena-not-found", true).replace("%arena%", args[1]));
            }
            return arena;
        }

        String editing = plugin.getArenaManager().getEditingArena(player.getUniqueId());
        if (editing == null) {
            MessageUtils.sendMessage(player, "&cSpecify the arena or toggle edit mode first: &7/hns edit <arena>");
            return null;
        }

        Arena arena = plugin.getArenaManager().getArena(editing);
        if (arena == null) {
            MessageUtils.sendMessage(player, "&cThe arena you were editing no longer exists!");
        }
        return arena;
    }

    // --- Tab Completer Implementation ---

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("help");
            if (sender.hasPermission("hns.join") || sender.hasPermission("hns.admin")) {
                list.add("join");
                list.add("leave");
                list.add("list");
            }
            if (sender.hasPermission("hns.setup") || sender.hasPermission("hns.admin")) {
                list.add("create");
                list.add("delete");
                list.add("info");
                list.add("edit");
                list.add("setlobby");
                list.add("setseekerspawn");
                list.add("addhiderspawn");
                list.add("wand");
                list.add("pos1");
                list.add("pos2");
                list.add("setregion");
                list.add("save");
            }
            if (sender.hasPermission("hns.start") || sender.hasPermission("hns.admin")) {
                list.add("start");
                list.add("restart");
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
                sub.equals("restart") || sub.equals("info") || sub.equals("edit") ||
                sub.equals("delete") || sub.equals("setseekerspawn") ||
                sub.equals("addhiderspawn") || sub.equals("setregion")) {
                
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
