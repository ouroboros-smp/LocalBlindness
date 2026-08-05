# Blindfold

A Fabric mod that puts the **real** vanilla Blindness (or Warden-style Darkness) effect on you — only ever by your own choice. It works two ways, from the same jar:

- **Client-side** (install it yourself): a private, local toggle. You get the genuine blindness fog and darkening, not a screen filter, with the sprint/swim penalty removed. The server is never contacted and never knows. Works on any server or in singleplayer.
- **Server-side** (install it on a Fabric server): every player can opt in with `/blindfold` — no client mod needed. The server applies the genuine effect to just that player until they opt out. Nothing is ever forced on anyone.

## Requirements

| | |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | 0.156.0+26.2 (or newer for 26.2) |
| Java | 25+ |

## Install

**As a player (client-side):**

1. Install Fabric Loader for 26.2.
2. Drop these into your `mods/` folder:
   - `blindfold-1.4.0.jar` (this mod)
   - [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2
3. Launch the game.

**On a server (server-side opt-in):**

1. Drop `blindfold-1.4.0.jar` and Fabric API into the server's `mods/` folder and restart.
2. That's it — any player can now run `/blindfold on`. No OP needed, and players don't need the mod installed.

## Usage

### Client-side

**Keybind.** Press `B` to toggle the effect on and off. Rebind it under Options > Controls > "Toggle Blindfold" (category: Miscellaneous).

**Command.** `/blindfold` (runs locally on your client):

| Command | Effect |
|---|---|
| `/blindfold` | Print current on/off state |
| `/blindfold toggle` | Flip on/off |
| `/blindfold on` / `off` | Force on or off |
| `/blindfold style blindness` | Use the Blindness effect (saved to config) |
| `/blindfold style darkness` | Use the Darkness effect (saved to config) |
| `/blindfold reload` | Reload the config file from disk |

### Server-side

The same `/blindfold` command, run by any player, applies to that player only:

| Command | Effect |
|---|---|
| `/blindfold` | Print your current on/off state and style |
| `/blindfold toggle` | Flip your opt-in on/off |
| `/blindfold on` / `off` | Opt in or out |
| `/blindfold style blindness` / `darkness` | Pick your personal style (session-only) |
| `/blindfold reload` | Reload the server config (OP only) |

Opt-in state lasts for the server session (it survives a relog, resets on server restart) and is never forced on anyone.

Notes for server-side use:

- With the `blindness` style, vanilla clients get vanilla behavior — including the vanilla no-sprint rule while blind. Players who want the fog without the sprint penalty can either use the `darkness` style (no sprint penalty in vanilla) or install Blindfold on their client and use the local toggle instead.
- If a player has the client mod, their client command intercepts `/blindfold` before it reaches the server. That's fine: the local toggle gives them the same visuals with zero server involvement.

## Configuration

Config lives at `config/blindfold.json` (client or server) and is written with sane defaults on first run:

```json
{
  "style": "BLINDNESS",
  "toggleKeyCode": 66,
  "showEffectIcon": false
}
```

- `style`: `BLINDNESS` or `DARKNESS`. On a server this is the default style for players who haven't picked one with `/blindfold style`.
- `toggleKeyCode`: GLFW key code the client keybind defaults to (66 is `B`). Rebinding in-game does not rewrite this; it is only the default. Ignored on servers.
- `showEffectIcon`: whether the effect's icon appears in the HUD/inventory while active. Off by default.

## How it works

**Client-side:** while the toggle is on, the mod applies an infinite-duration Blindness (or Darkness) `MobEffectInstance` to your local `LocalPlayer` and re-asserts it every client tick. Re-asserting matters because the server periodically syncs your effects; if it ever clears the client copy (for example on respawn or dimension change), the next tick puts it straight back. Because it is the genuine status effect, it renders exactly like vanilla blindness. The one gameplay side effect of vanilla Blindness — the inability to sprint (and sprint-swim) — is removed by a small client Mixin that bypasses the blindness check inside `LocalPlayer.isSprintingPossible` only while the mod's own effect is active. The server never sees any of this.

**Server-side:** for every opted-in player, the server applies a short (20-second) genuine Blindness or Darkness effect and refreshes it every tick once it drops below 15 seconds, so it never visibly runs out. Using a short, self-refreshing effect instead of an infinite one makes the feature self-healing: if the server crashes or the mod is removed, the leftover effect expires within seconds and nothing permanent is ever written into player data. On join, any leftover managed effect is cleared immediately for players who are no longer opted in. Effects the mod did not apply (say, an infinite Blindness from a command block) are left alone.

Note: because a real effect is applied and removed, legitimately received gameplay Blindness/Darkness can be swept up when toggling off (client) or opting out (server). It re-syncs or re-applies from the game shortly after.

## Build from source

Requires JDK 25.

```bash
./gradlew build
./gradlew runClientGameTest
```

The built jar lands in `build/libs/blindfold-1.4.0.jar`. `runClientGameTest` launches Fabric's real client GameTest suite and verifies command toggling, both vanilla visual effects, re-assertion, cleanup, and the scoped sprint bypass. Run `./gradlew runClient` for an interactive smoke test.

## License

MIT. See [LICENSE](LICENSE).
