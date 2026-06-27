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

---

### Step 2: Enter Setup Mode
Run the command:
```bash
/hns setup City
```
You will enter **Interactive Setup Mode**:
- Your inventory is temporarily backed up and cleared.
- You are placed in Creative mode with flying enabled.
- Your hotbar is populated with 6 setup items.

---

### Step 3: Setup Hotbar Configuration

![Setup Hotbar Layout](https://via.placeholder.com/600x100.png?text=Lobby+Beacon+|+Torch+Seeker+|+Emerald+Hider+|+Gold+Axe+Wand+|+Star+Save+|+Door+Exit)

You have the following items in your hotbar:

#### 1. Set Lobby (Slot 1 - Beacon)
* Walk to the location where players should wait for the match countdown to start.
* **Right-click** the Beacon item.
* This sets the arena-specific lobby spawn.

#### 2. Set Seeker Spawn (Slot 2 - Redstone Torch)
* Stand at the starting point where the Seeker should be released.
* **Right-click** the Redstone Torch item.
* This sets the Seeker's spawn location.

#### 3. Add Hider Spawn (Slot 3 - Emerald)
* Go to places in the arena map where Hiders should hide.
* **Right-click** the Emerald item.
* This adds a Hider spawn point. You can add as many as you want! Spawns are randomized.

#### 4. Region Wand (Slot 4 - Golden Axe)
* Set boundaries so players cannot escape:
  * **Left-click** a block to set **Position 1**.
  * **Right-click** a block to set **Position 2**.
* Once both points are clicked, the bounding region box is defined.

#### 5. Save Arena (Slot 5 - Nether Star)
* **Right-click** the Nether Star.
* This runs an automatic checklist check:
  * ✅ Lobby exists
  * ✅ Seeker spawn exists
  * ✅ Hider spawn exists
  * ✅ Region bounds exist
* If complete, the arena is saved, auto-enabled, and ready for games immediately!
* If incomplete, you will see a list of missing items (e.g. `❌ Region`).

#### 6. Finish Setup (Slot 6 - Iron Door)
* **Right-click** the Iron Door item.
* This exits setup mode, disables creative flight, and restores your original inventory.

---

### Step 4: Verification
Verify setup status using the info command:
```bash
/hns info City
```
You should see:
```text
Arena: City
Status: Enabled
Players: 0/20
Lobby: Set
Seeker Spawn: Set
Hider Spawns: 1
Region: Set
Timer: 300 seconds
```
You are now ready to play! Players can join using `/hns join` or by clicking the Map icon in the Join GUI.
