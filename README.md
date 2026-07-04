# Local Blindness

A client-side Fabric mod that toggles the **real** vanilla Blindness (or Warden-style Darkness) effect on yourself. It applies the genuine status effect to your own player on the client, so you get the actual blindness fog and darkening, not a screen filter. It is entirely local: the server is never contacted and never knows. Works on any server or in singleplayer, and nobody else has to install anything.

- **Toggle** with a keybind (default `B`, rebindable in Controls) or the `/localblindness` command.
- **Two effects**, switchable in config or on the fly: `blindness` (the standard Blindness effect) and `darkness` (the Warden's Darkness effect).
- **Visual only.** The Blindness sprint penalty is dropped while the mod's effect is on, so you can still sprint normally. Only the visual applies. (Real blindness from gameplay is unaffected when the toggle is off.)
- **Session-only** on/off state: it always starts off when the game launches. Nothing about the toggle is persisted.

## Requirements

| | |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.19.2 or newer |
| Fabric API | 0.141.4+1.21.11 (or newer for 1.21.11) |
| Java | 21+ |

## Install

1. Install Fabric Loader for 1.21.11.
2. Drop these into your `mods/` folder:
   - `localblindness-1.1.1.jar` (this mod)
   - [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11
3. Launch the game.

## Usage

**Keybind.** Press `B` to toggle the effect on and off. Rebind it under Options > Controls > "Toggle Local Blindness" (category: Miscellaneous).

**Command.** `/localblindness` (client command, runs locally):

| Command | Effect |
|---|---|
| `/localblindness` | Print current on/off state |
| `/localblindness toggle` | Flip on/off |
| `/localblindness on` / `off` | Force on or off |
| `/localblindness style blindness` | Use the Blindness effect (saved to config) |
| `/localblindness style darkness` | Use the Darkness effect (saved to config) |
| `/localblindness reload` | Reload the config file from disk |

## Configuration

Config lives at `.minecraft/config/localblindness.json` and is written with sane defaults on first run:

```json
{
  "style": "BLINDNESS",
  "toggleKeyCode": 66,
  "showEffectIcon": false
}
```

- `style`: `BLINDNESS` or `DARKNESS`.
- `toggleKeyCode`: GLFW key code the keybind defaults to (66 is `B`). Rebinding in-game does not rewrite this; it is only the default.
- `showEffectIcon`: whether the effect's icon appears in the HUD/inventory while active. Off by default.

## How it works

While the toggle is on, the mod applies an infinite-duration Blindness (or Darkness) `StatusEffectInstance` to your local `ClientPlayerEntity` and re-asserts it every client tick. Re-asserting matters because the server periodically syncs your effects; if it ever clears the client copy (for example on respawn or dimension change), the next tick puts it straight back. Because it is the genuine status effect, it renders exactly like vanilla blindness.

The one gameplay side effect of vanilla Blindness, the inability to sprint, is removed by a small client Mixin: it redirects the `hasBlindnessEffect()` check inside `ClientPlayerEntity.canSprint` to report "no blindness" while the mod's effect is active. The fog and darkening are untouched, and the check falls back to vanilla behavior whenever the toggle is off.

This is client-side only. The server never sees the effect, and it applies only to you.

Note: because a real Blindness effect is applied and removed on your client, if you legitimately receive Blindness or Darkness from gameplay while the toggle is on, that will be swept up when you toggle off. It re-syncs from the server shortly after.

## Build from source

Requires JDK 21.

```bash
./gradlew build
```

The built jar lands in `build/libs/localblindness-1.1.1.jar`. Run `./gradlew runClient` to smoke-test it in a dev client.

## License

MIT. See [LICENSE](LICENSE).
