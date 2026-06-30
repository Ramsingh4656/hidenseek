# HideNSeek Commands & Permissions

This file documents all available commands and permission nodes for HideNSeek v1.0.2.

---

## Commands Reference

The plugin uses the base command `/hns` (or `/hidenseek`). Subcommands are restricted by permissions.

### Player Commands

#### `/hns join [arena]`
* **Description**: Joins a HideNSeek game. If no arena name is specified, this opens the chest GUI **Join Selector**.
* **Usage**: `/hns join` or `/hns join <arena_name>`
* **Permission**: `hns.join` (Default: `true`)

#### `/hns leave`
* **Description**: Leaves the current match or lobby and teleports the player back to the global lobby.
* **Usage**: `/hns leave`
* **Permission**: `hns.join` (Default: `true`)

---

### Administrator Commands

#### `/hns create <arena>`
* **Description**: Creates a new, blank arena profile.
* **Usage**: `/hns create <arena_name>`
* **Permission**: `hns.setup` (Default: `op`)

#### `/hns delete <arena>`
* **Description**: Deletes an arena profile, stops any running matches on it, and removes its file.
* **Usage**: `/hns delete <arena_name>`
* **Permission**: `hns.setup` (Default: `op`)

#### `/hns setup <arena>`
* **Description**: Toggles **Interactive Setup Mode** and opens the visual Setup GUI for the specified arena. Gives the player the Setup Hotbar and the sidebar checklist.
* **Usage**: `/hns setup <arena_name>`
* **Permission**: `hns.setup` (Default: `op`)

#### `/hns setlobbyboundary [arena]`
* **Description**: Sets the Lobby Boundary of the arena to your current wand selection.
* **Usage**: `/hns setlobbyboundary` or `/hns setlobbyboundary <arena_name>`
* **Permission**: `hns.setup` (Default: `op`)

#### `/hns sethiderboundary [arena]`
* **Description**: Sets the Hider Boundary of the arena to your current wand selection.
* **Usage**: `/hns sethiderboundary` or `/hns sethiderboundary <arena_name>`
* **Permission**: `hns.setup` (Default: `op`)

#### `/hns setseekerboundary [arena]`
* **Description**: Sets the Seeker Boundary of the arena to your current wand selection.
* **Usage**: `/hns setseekerboundary` or `/hns setseekerboundary <arena_name>`
* **Permission**: `hns.setup` (Default: `op`)

#### `/hns start <arena>`
* **Description**: Forces the lobby countdown to start immediately for the specified arena.
* **Usage**: `/hns start <arena_name>`
* **Permission**: `hns.start` (Default: `op`)

#### `/hns stop <arena>`
* **Description**: Stops the active match immediately and teleports all players back to the global lobby.
* **Usage**: `/hns stop <arena_name>`
* **Permission**: `hns.stop` (Default: `op`)

#### `/hns info <arena>`
* **Description**: Prints a clean, colorized coordinate list, settings sheet, and setup checklist showing status details of the arena.
* **Usage**: `/hns info <arena_name>`
* **Permission**: `hns.setup` (Default: `op`)

#### `/hns reload`
* **Description**: Safely cancels active games, restores player state, and reloads `config.yml`, `messages.yml`, and all arena profiles.
* **Usage**: `/hns reload`
* **Permission**: `hns.reload` (Default: `op`)

---

## Permissions Checklist

| Permission Node | Description | Default Status |
|---|---|---|
| `hns.admin` | Access to all administrative commands and bypass configurations. | `op` |
| `hns.setup` | Allows creating, deleting, and using setup tools for arenas. | `op` |
| `hns.start` | Allows forcing lobby countdowns to start. | `op` |
| `hns.stop` | Allows forcing games to stop. | `op` |
| `hns.reload` | Allows reloading configuration files. | `op` |
| `hns.join` | Allows joining games and using match selector items. | `true` |
| `hns.bypass` | Bypasses boundary limits and build/break rules (requires Creative mode). | `op` |
