# Changelog

All notable changes to this project will be documented in this file.

---

## [1.0.2] - 2026-06-30
### Added
- **Separate Boundary Regions**: Implemented separate Lobby Boundary, Seeker Boundary, and Hider Boundary configurations.
- **Visual Setup GUI**: Added a 27-slot Setup chest menu that reflects real-time status (✔/✖) and binds custom boundary regions directly.
- **Seeker Safe Area**: Added a configurable Seeker spawn protection region (default 5x5) preventing rushing during the hiding countdown.
- **Start Game Redesign**: Replaced the automatic start with a Nether Star start item. Manual starts require meeting the minimum player count (default 2).
- **Lobby Auto Start Threshold**: Added a configurable `auto-start-players` threshold (default 8) to automatically start countdowns.
- **Scoreboard Refactoring**: Completely rewritten sidebar using Team prefixes to eliminate duplicates and flicker, fully compatible with Java and Bedrock (Geyser/Floodgate) players.

### Fixed
- Fixed raw color codes and line duplication on scoreboards.
- Fixed numbers appearing on the right side of the scoreboard by utilizing NumberFormat.blank().
- Fixed Bukkit Location serialization leaks in `/hns info`, replacing them with formatted coordinates.

## [1.0.1] - 2026-06-27
### Added
- **Interactive Setup Mode**: Configure your arena entirely via hotbar items! Use Slot 1 (Set Lobby), Slot 2 (Set Seeker Spawn), Slot 3 (Add Hider Spawn), Slot 4 (Region Wand), Slot 5 (Save Arena), and Slot 6 (Finish Setup).
- **Auto Validation Checklist**: Setup mode automatically displays a real-time checklist (using `✅` and `❌` indicators). Attempting to save verifies that all required components are set.
- **Auto Enable**: Validation successes automatically save and toggle the arena to `Enabled`, making it ready for play immediately.
- **Map Join GUI Icons**: Revamped the chest Match Join GUI to use map indicators. Enabled arenas display as `FILLED_MAP` items while incomplete/disabled arenas display as empty `MAP` items.
- **Actionable Errors**: Restructured chat warnings and error messages, ensuring they guide players and admins on how to resolve configuration blocks.
- **Offhand Swap Interception**: Blocked hand-swapping (`PlayerSwapHandItemsEvent`) for active game/setup participants to prevent item persistence bypasses.
- **World Load Safety Fallback**: Deferred initial configuration load to the first server tick, preventing spawn resets when worlds are managed by Multiverse.
- **Clean /hns info Output**: Revamped the command print sheet to display human-readable status, spawns, and timers, replacing Bukkit Location serialization strings.
- **Documentation**: Added dedicated `COMMANDS.md`, `SETUP.md`, and `CONFIG.md` directories to simplify server setups.

### Fixed
- Fixed a bug where players leaving active sessions would receive a confusing `not-in-game` error message.
- Fixed a bug where server reloads could corrupt or leave stale Arena instances active inside registry memory.
- Fixed item loss bugs where players in setup mode could lose their items upon server quit or reload.

---

## [1.0.0] - 2026-06-27
### Added
- Initial release featuring standard infection-style HideNSeek minigame logic.
- Implemented scale attributes making Hiders extremely tiny without requiring resource packs.
- Integrated Boss Bar, Action Bar, Scoreboards, and screen title aesthetics.
- Added custom Bazooka weapon dealing falloff splash damage.
