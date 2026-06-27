# HideNSeek - Premium PaperMC Minigame Plugin

HideNSeek is a highly customizable, production-ready, infection-style Hide and Seek minigame plugin built specifically for the **PaperMC** server platform.

---

## Key Features
- **Infection Game Flow**: Hiders become tiny (using Minecraft's native `GENERIC_SCALE`, no client resource pack required). Seekers search and tag them. Eliminated Hiders join the hunt!
- **Interactive Setup Mode**: Command-free arena configurations! Run `/hns setup <arena>` to configure regions, spawns, and lobbies directly from your hotbar using clickable items.
- **Auto Validation & Auto Enable**: Real-time checklists detect missing config elements. Arenas are auto-enabled and saved the second validation succeeds.
- **Custom Bazooka Rocket Launcher**: Fired projectiles detonate on impact, dealing splash damage to Hiders with custom sound, flame, and smoke animations.
- **Premium Display Aesthetics**: Integrated Chest Join GUI (using Map icons), custom Scoreboard sidebar, Boss Bar match timer, action bar countdowns, and full-screen color titles.
- **Inventory Caching & Restoration**: Stores players' inventories, levels, and flight states upon joining/setting up, restoring them safely on leave/quit.
- **Multiverse Compatible**: Defer-loading ensures spawns and regions are loaded reliably even when worlds are loaded late on server startup.

---

## Target Platform
- **Server Platform**: PaperMC (Minecraft 1.21.x)
- **Java Version**: Java 21
- **Build System**: Gradle

---

## Installation
1. Put the compiled plugin jar (`HideNSeek-1.0.1.jar`) inside the server's `plugins/` folder.
2. Start the Paper server.
3. The plugin will automatically generate all necessary templates:
   - `plugins/HideNSeek/config.yml` - Gameplay parameters and weapons.
   - `plugins/HideNSeek/messages.yml` - Translatable colorized messages.
   - `plugins/HideNSeek/arenas/` - Created arena profiles.
   - `plugins/HideNSeek/data/` - Globals and lobby cache.
4. Create your first arena and run setup! (See [SETUP.md](SETUP.md)).

---

## Additional Documentation Directories

We provide comprehensive guides for setting up and administrating the plugin:
- **[SETUP.md](SETUP.md)**: Real-time walkthrough explaining how to construct and validate arenas using the Setup Hotbar.
- **[COMMANDS.md](COMMANDS.md)**: Explains the minimal set of administrator/player commands, syntax structures, and permission nodes.
- **[CONFIG.md](CONFIG.md)**: Exhaustive breakdown detailing all options, variables, and sound properties inside `config.yml`.
- **[CHANGELOG.md](CHANGELOG.md)**: Complete logs tracking additions, improvements, and bug fixes.

---

## Compilation

Build the plugin using the provided Gradle wrapper.

### Windows
```powershell
.\gradlew.bat build
```

### Linux/macOS
```bash
chmod +x gradlew
./gradlew build
```

The compiled plugin jar will be generated inside the output folder:
```text
build/libs/HideNSeek-1.0.1.jar
```
