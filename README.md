# HideNSeek - Premium PaperMC Minigame Plugin

HideNSeek is a highly customizable, production-ready, infection-style Hide and Seek minigame plugin built specifically for the **PaperMC** platform.

---

## Features
- **Infection Game Flow**:
  1. Players join the lobby.
  2. Countdown starts when minimum player counts are reached.
  3. A random player is chosen as the Seeker; all other players become Hiders.
  4. Hiders become **very small** (using Minecraft's native `Attribute.GENERIC_SCALE` attribute, no resource pack required!).
  5. The Seeker receives Blindness for a grace period (default 30 seconds) while Hiders run and hide.
  6. When the grace period ends, the Seeker receives a custom **Bazooka** rocket launcher.
  7. When a Seeker tags (melee or Bazooka blast) a Hider, the Hider instantly becomes a Seeker, gets a Bazooka, and joins the hunt.
  8. Game ends when all Hiders are infected (Seekers Win) or the match timer expires (Hiders Win).
- **Custom Bazooka Weapon**: Fires custom rocket projectiles (Snowballs with smoke/flame trails) that detonate on hit. Deals splash damage to Hiders with realistic distance falloff, without damaging any block terrain.
- **Robust Arena System**: Support for unlimited arenas, each with its own YAML configuration. Stores region boundaries, lobby spawn, seeker spawn, multiple hider spawns, and min/max player counts.
- **Boundary Enforcement**: Prevents players from leaving the arena bounding box during active games.
- **Premium Aesthetics**: Integrated custom Scoreboard sidebar, Boss Bar match timer progress bar, Action Bar updates, and Full Screen titles with custom sound routing.
- **Inventory Restoration**: Caches players' inventories, levels, potion effects, and properties upon join, and restores them when they leave or the game ends.
- **Matchmaking GUI**: Nethestar item in hotbar slot 1 opens a custom chest GUI displaying arena player counts and states.

---

## Target Platform
- **Server Platform**: PaperMC (Minecraft 1.21.x)
- **Java Version**: Java 21
- **Build System**: Gradle

---

## Installation
1. Compile the plugin using Gradle (see building section below).
2. Copy `build/libs/HideNSeek-1.0.0.jar` into your Minecraft server's `plugins/` directory.
3. Start or reload the server.
4. Set the global lobby location using `/hns setlobby`.
5. Create and configure arenas (see commands below).

---

## Commands & Permissions

### Operator/Setup Commands (`hns.setup` or `hns.admin`)
- `/hns create <arena>` - Creates a new arena configuration.
- `/hns delete <arena>` - Deletes an arena configuration.
- `/hns edit <arena>` - Toggles editing session for an arena.
- `/hns setlobby` - Sets the global lobby spawn point.
- `/hns setseekerspawn [arena]` - Sets the Seeker spawn point.
- `/hns addhiderspawn [arena]` - Adds a Hider spawn point.
- `/hns wand` - Gives the golden axe region setup wand.
- `/hns pos1` / `/hns pos2` - Sets selection bounds to your current position.
- `/hns setregion [arena]` - Sets the boundary region based on selected pos1/pos2.
- `/hns save` - Saves all loaded arenas.
- `/hns info <arena>` - Prints setup details for an arena.

### Game Start/Stop Commands (`hns.start` / `hns.stop` / `hns.admin`)
- `/hns start <arena>` - Forces the countdown to begin.
- `/hns stop <arena>` - Forces the active game to stop.
- `/hns restart <arena>` - Forces the active game to restart.

### Reload Config (`hns.reload` or `hns.admin`)
- `/hns reload` - Reloads `config.yml` and `messages.yml` configurations.

### Player Commands (`hns.join`)
- `/hns join <arena>` - Join an arena match.
- `/hns leave` - Leave your current lobby or active match.
- `/hns list` - Lists all available arenas and their statuses.

---

## Permissions Checklist
- `hns.admin` - Access to all administrative functions (bypasses all rules).
- `hns.setup` - Allows creating, editing, and saving arenas.
- `hns.start` - Allows starting matches manually.
- `hns.stop` - Allows stopping matches manually.
- `hns.reload` - Allows reloading plugin configuration files.
- `hns.join` - Allows joining matches and using lobby items.
- `hns.bypass` - Allows breaking/placing blocks within active arenas (requires Creative Mode).

---

## Storage & Configuration Files
The plugin generates the following file hierarchy under `plugins/HideNSeek/`:
- `config.yml` - Contains gameplay parameters, bazooka weapon configs, visual/audio elements, and scoreboards.
- `messages.yml` - Stores all game localized chat messages, titles, actionbar subtitles, and alerts.
- `arenas/` - Folder containing individual YAML configurations for each created arena (e.g. `arenas/lobby_arena.yml`).
- `data/` - Holds internal data like the coordinates of the global lobby spawn point (`data/lobby.yml`).

---

## Building the Plugin
Run the Gradle build command using the provided Gradle wrapper:
```bash
./gradlew build
```
Once the compilation succeeds, the compiled jar will be created at:
```
build/libs/HideNSeek-1.0.0.jar
```
