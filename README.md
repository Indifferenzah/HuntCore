<div align="center">
# HuntCore

  <img src="https://i.imgur.com/oJ5GiU0.png" alt="HuntCore Logo"/>

**Manhunt game plugin for Paper 1.21.1**

![Version](https://img.shields.io/badge/version-1.0.0-red?style=flat-square)
![Paper](https://img.shields.io/badge/Paper-1.21.1-orange?style=flat-square)
![Java](https://img.shields.io/badge/Java-21-blue?style=flat-square)
![License](https://img.shields.io/badge/license-MIT-green?style=flat-square)

</div>

---

## 📖 Overview

**HuntCore** is a fully-featured **Manhunt** game plugin for Paper 1.21.1.
Runners try to mine a hidden block of crying obsidian — Hunters must eliminate them all before they succeed.

---

## ✨ Features

### 🎮 Gameplay
- **Runner vs Hunter** team system with lobby vote via compass
- **Crying obsidian** win condition for runners (mine it with a diamond pickaxe to win)
- **Hunter freeze phase** at game start — hunters can't move, break blocks, or attack until the timer runs out; inventory is cleared
- **Random spawn** per game — all players teleport to the same random location (never inside water), different every game
- **Runner glow** — runners begin glowing after a configurable amount of minutes (default: 30)
- **Compass tracking** — hunters' compass always points to the nearest alive runner, with distance on the action bar
- **Mid-game reconnect** — if a hunter/runner crashes and rejoins while the game is running, their team and role are fully restored

### 📊 Stats & Display
- **Persistent stats** — kills, deaths, K/D ratio, wins, losses, win-rate, total games (stored in H2 via HikariCP)
- **Per-state scoreboard** — different sidebar layout for lobby, running, and post-game
- **Per-state tab list** — different header/footer/name format for lobby, running, and post-game
- **Boss bar** — live game info displayed in the boss bar during the game
- **Team nametag** — `[RUNNER]` / `[HUNTER]` prefix shown above every player's head

### 🛠️ Admin Tools
- Start/stop the game at any time with `/game start` or `/game stop`
- Set spawn points with `/setspawn block` and `/setspawn end`
- Manually assign teams, respawn eliminated runners, toggle PvP
- Reload `config.yml` live with `/huntcore reload`
- Reset all statistics with `/huntcore cleardb`

---

## 🗂️ Commands

| Command | Description | Permission |
|---|---|---|
| `/game` | Show current game status | `huntcore.game` |
| `/game start` | Start the game (enters lobby) | `huntcore.game` |
| `/game stop` | Force-stop the game | `huntcore.game` |
| `/teams set <player> <runner\|hunter>` | Assign a player to a team | `huntcore.teams` |
| `/teams clear <player>` | Remove a player from their team | `huntcore.teams` |
| `/pvp enable\|disable` | Toggle PvP | `huntcore.pvp` |
| `/setspawn block` | Set the crying obsidian spawn point | `huntcore.setspawn` |
| `/setspawn end` | Set the post-game teleport location | `huntcore.setspawn` |
| `/respawn <player>` | Respawn an eliminated runner | `huntcore.respawn` |
| `/stats [player]` | View your own or another player's stats | — |
| `/huntcore` | Plugin info | — |
| `/huntcore help` | Full command list | — |
| `/huntcore reload` | Reload `config.yml` | `huntcore.admin` |
| `/huntcore cleardb` | Wipe all stored statistics | `huntcore.admin` |

---

## ⚙️ Configuration

All settings live in `plugins/HuntCore/config.yml`.
The file is generated automatically on first run.

<details>
<summary>Key settings</summary>

```yaml
# How far from the block spawn players are teleported
spawn-radius: 2000

# How long hunters are frozen at game start (seconds)
hunter-freeze-seconds: 30

# Time between voting and launch (seconds)
post-vote-delay-seconds: 15

# Maximum fraction of players that can be hunters (0.75 = 75%)
max-hunter-ratio: 0.75

# After how many minutes runners start glowing (0 = disabled)
glow-runners-after-minutes: 30

# Fixed cage position used during lobby
cage-x: 0
cage-y: 300
cage-z: 0

# Nametag prefixes above players' heads
nametag:
  runner-prefix: "&b[RUNNER] "
  hunter-prefix: "&c[HUNTER] "
```

</details>

---

## 🎯 How to Play

1. **Admin runs `/game start`** — all online players are teleported into the lobby cage
2. **Players vote their team** by right-clicking the compass
3. **Once everyone votes**, a countdown begins — then the game launches
4. **Hunters are frozen** for the configured number of seconds while runners scatter
5. **Runners** must find and mine the crying obsidian block to win
6. **Hunters** must eliminate all runners before that happens
7. After the game, everyone is teleported to the end spawn and stats are saved

---

## 📦 Installation

1. Download the latest `HuntCore-x.x.x.jar` from [Releases](../../releases)
2. Drop it into your server's `plugins/` folder
3. Start the server — `config.yml` is generated automatically
4. Set your spawn points:
   - Stand at the desired crying obsidian position → `/setspawn block`
   - Stand at the post-game lobby → `/setspawn end`
5. Run `/game start` and enjoy

**Requirements:** Paper 1.21.1 · Java 21

---

## 🏗️ Building from source

```bash
git clone https://github.com/indifferenzah/huntcore
cd huntcore
./gradlew shadowJar
# Output: plugin/build/libs/HuntCore-x.x.x.jar
```

---

## 📜 License

This project is licensed under the **MIT License** — see [`LICENSE`](LICENSE) for details.

---

<div align="center">

Made with ❤️ by **Indifferenzah**

</div>
