# Local Blindness

A client-side Fabric mod that gives you a toggleable blindness (or Warden-style darkness) screen effect. It is entirely local: the server is never contacted and never knows. Works on any server or in singleplayer, and nobody else has to install anything.

- **Toggle** with a keybind (default `B`, rebindable in Controls) or the `/localblindness` command.
- **Two styles**, switchable in config or on the fly: `blindness` (constant heavy dark) and `darkness` (slow pulsing dim).
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
   - `localblindness-1.0.0.jar` (this mod)
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
| `/localblindness style blindness` | Switch to constant-dark style (saved to config) |
| `/localblindness style darkness` | Switch to pulsing style (saved to config) |
| `/localblindness reload` | Reload the config file from disk |

## Configuration

Config lives at `.minecraft/config/localblindness.json` and is written with sane defaults on first run:

```json
{
  "style": "BLINDNESS",
  "blindnessIntensity": 0.9,
  "darknessBase": 0.55,
  "darknessAmplitude": 0.3,
  "darknessPeriodSeconds": 3.0,
  "toggleKeyCode": 66
}
```

- `style`: `BLINDNESS` or `DARKNESS`.
- `blindnessIntensity`: overlay opacity for the blindness style, 0.0 to 1.0.
- `darknessBase`, `darknessAmplitude`: the pulsing style's midpoint opacity and swing (the opacity rides a sine wave between `base - amplitude` and `base + amplitude`, clamped to 0..1).
- `darknessPeriodSeconds`: seconds per pulse.
- `toggleKeyCode`: GLFW key code the keybind defaults to (66 is `B`). Rebinding in-game does not rewrite this; it is only the default.

Opacities are clamped to 0..1 and the period to 0.1..60s on load, so a bad edit will not crash anything.

## How it works

The effect is a single full-screen dark quad drawn as the last HUD element (`HudElementRegistry.addLast`), so it paints over the rest of the HUD. Because it is a HUD element, the darkening lifts automatically while a screen is open (chat, inventory, pause), which keeps menus usable, and it hides while the HUD is hidden with `F1`. The opacity math is isolated in `EffectMath` with no Minecraft dependencies, which is what the unit tests cover.

This is a rendered overlay, not the vanilla blindness fog, so it does not depend on any potion effect and cannot be stripped by the server.

## Build from source

Requires JDK 21.

```bash
./gradlew build
```

The built jar lands in `build/libs/localblindness-1.0.0.jar`. Run `./gradlew runClient` to smoke-test it in a dev client.

## License

MIT. See [LICENSE](LICENSE).
