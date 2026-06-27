# HideNSeek Configuration Guide

This guide documents every settings block and option configured inside the `config.yml` file.

---

## 1. Game Settings (`game-settings`)

Settings governing match metrics, countdown durations, and player scaling properties:

```yaml
game-settings:
  min-players: 2
  max-players: 20
  countdown-seconds: 10
  match-timer-seconds: 300
  blindness-duration-seconds: 30
  hider-scale: 0.3
```

* **`min-players`**: Minimum number of players required in the lobby before the start countdown begins.
* **`max-players`**: Maximum number of players allowed inside a single match session.
* **`countdown-seconds`**: Duration (in seconds) of the start countdown once minimum players are reached.
* **`match-timer-seconds`**: Total match duration (in seconds). Hiders win if the timer expires.
* **`blindness-duration-seconds`**: The grace hiding phase. Seekers are blinded and frozen at spawn for this duration while Hiders hide.
* **`hider-scale`**: The physical scale factor applied to Hiders using Minecraft's native `GENERIC_SCALE` attribute. `0.3` reduces Hiders to 30% of normal size.

---

## 2. Bazooka Settings (`bazooka-settings`)

Weapon configuration given to Seekers after the grace hide phase ends:

```yaml
bazooka-settings:
  item:
    material: "BLAZE_ROD"
    name: "&e&lBazooka Rocket Launcher"
    lore:
      - "&7Right-click to fire a destructive rocket!"
      - "&7Ammo: &a%ammo%&7/&a%max_ammo%"
      - "&7Cooldown: &c%cooldown%s"
  cooldown-seconds: 3.0
  max-ammo: 10
  explosion-radius: 4.0
  damage: 8.0
```

* **`item.material`**: Material item representing the Bazooka rocket launcher.
* **`item.name`**: Display name of the Bazooka launcher item. Supports color codes.
* **`item.lore`**: Description lines displayed on the Bazooka item.
* **`cooldown-seconds`**: Cooldown delay (in seconds) between firing rocket projectiles.
* **`max-ammo`**: Total rockets given to each Seeker. Rockets are lost on fire; when ammo runs out, they can no longer fire.
* **`explosion-radius`**: Block radius of the custom splash damage explosion.
* **`damage`**: Maximum damage dealt to Hiders at the absolute center of the splash zone. Damage falls off linearly toward the edges of the explosion.

---

## 3. Lobby Items (`lobby-items`)

Hotbar items distributed to players when waiting inside a game lobby:

* **`join-selector`**: Right-clicking this item opens the Match Join chest GUI. (Default: `NETHER_STAR`)
* **`leave-lobby`**: Right-clicking this item exits the game session. (Default: `BARRIER`)

---

## 4. Custom Sounds (`sounds`)

Defines Minecraft sound identifiers, volume levels, and pitches played during game events:

* **`countdown`**: Played on each tick of the lobby countdown. (Default: `BLOCK_NOTE_BLOCK_PLING`)
* **`game-start`**: Played when the match starts. (Default: `ENTITY_WITHER_SPAWN`)
* **`player-found`**: Played to all players when a Hider is found/eliminated. (Default: `ENTITY_LIGHTNING_BOLT_THUNDER`)
* **`game-end`**: Played when the match ends. (Default: `ENTITY_ENDER_DRAGON_GROWL`)
* **`victory`**: Played to winning players. (Default: `UI_TOAST_CHALLENGE_COMPLETE`)
* **`defeat`**: Played to losing players. (Default: `ENTITY_WITHER_DEATH`)

---

## 5. Scoreboard & Bossbar (`scoreboard` and `bossbar`)

Configuration templates for the sidebar scoreboard and the timer progress bossbar:

* **Scoreboard lines placeholder syntax**:
  * `%arena%`: Current arena name
  * `%time%`: Match time remaining formatted as MM:SS
  * `%seekers_count%`: Current Seekers
  * `%hiders_count%`: Current Hiders
  * `%players_count%`: Total players inside the session
  * `%max_players%`: Max players limit
* **Bossbar colors and styles**:
  * Colors: `PINK`, `BLUE`, `RED`, `GREEN`, `YELLOW`, `PURPLE`, `WHITE`
  * Styles: `PROGRESS`, `NOTCHED_6`, `NOTCHED_10`, `NOTCHED_12`, `NOTCHED_20`
