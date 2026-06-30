# Arena Setup Guide

Setting up a HideNSeek arena is extremely easy. You do not need to run many commands. All configurations happen from a clickable hotbar.

---

## Setup Walkthrough

### Step 1: Create the Arena
Run the command:
```bash
/hns create City
```
*(Replace `City` with your preferred arena name).*
**Note**: This command automatically enters Setup Mode for you!

---

### Step 2: Setup Mode Features
When entering Setup Mode:
- Your inventory is temporarily backed up and cleared.
- You are placed in Creative mode with flying enabled.
- A visual **Setup GUI** chest menu opens automatically.
- A real-time **Setup Checklist** sidebar scoreboard appears on the right side of your screen.
- You receive setup hotbar items (Beacon, Seeker Torch, Emerald, Wand, Nether Star, Iron Door) to configure settings.

If you ever exit and need to enter setup again, run:
```bash
/hns setup City
```

---

### Step 3: Setup GUI & Hotbar Configuration

You can click items inside the visual **Setup GUI** chest menu (or right-click hotbar items) to configure spawns and boundaries. The GUI items display dynamic `✔` or `✖` indicators showing configuration progress:

#### Spawns Configuration
* **Lobby Spawn** (Beacon): Stand at the lobby spawn location and click this item to set it.
* **Seeker Spawn** (Redstone Torch): Stand at the Seeker spawn point and click this item to set it.
* **Hider Spawn** (Emerald): Stand at a hiding spot and click this item to add a Hider spawn point. (You can add multiple spawns. Spawns are randomized. Right-click this item inside the GUI to clear all hider spawns).

#### Arena Boundary Configuration
The Arena Boundary must be configured using the **Selection Wand** (Golden Axe):
1. Click the **Selection Wand** icon in the GUI (or select it from your hotbar).
2. **Left-click** a block to set **Position 1**.
3. **Right-click** a block to set **Position 2**.
4. Open the Setup GUI and click the **Arena Boundary** (Iron Bars) item to assign your selection, OR run the command:
   * `/hns setboundary`

#### Actions
* **Save Arena** (Nether Star): Click to validate and save configuration. The arena will be auto-enabled only if all spawns and boundaries are set. If incomplete, it displays a list of exactly what is missing.
* **Finish Setup** (Iron Door): Click to save, exit setup mode, and restore your original inventory.
* **Cancel Setup** (Barrier): Click to cancel changes and exit setup mode.

---

### Step 4: Verification
Verify setup status using the info command:
```bash
/hns info City
```
The command will print a clean, human-readable settings coordinate list:
```text
========================================
  Arena Information: City

  Status: WAITING
  Enabled: Yes
  Players: 0/20

  Lobby Spawn: 10, 64, -20 (world)
  Seeker Spawn: 24, 64, 5 (world)
  Hider Spawns: 3 registered

  Arena Boundary: (5, 60, -25) to (150, 90, 150) (world)

  Timer: 300s
  Minimum Players: 2
  Maximum Players: 20
  Auto Start Players: 8
========================================
```
You are now ready to play! Players can join using `/hns join` or by clicking the Map icon in the Join GUI.

