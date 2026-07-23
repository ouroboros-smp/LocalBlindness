# Publishing

How to get Blindfold onto Modrinth (and optionally CurseForge). Being on those two covers essentially every mod search site and launcher.

The Modrinth project already exists: https://modrinth.com/project/blindfold

## One-time setup

### Modrinth
1. Fill in the project settings (values below) at https://modrinth.com/project/blindfold/settings.
2. Go to **Settings > Access tokens** (PATs) and create a token with the **Create versions** scope.
3. In the GitHub repo: **Settings > Secrets and variables > Actions** and add a secret `MODRINTH_TOKEN`.

### CurseForge (optional)
1. Make an account on the CurseForge author portal and **create a mod project** (it needs manual approval before uploads work).
2. Create an API token at [legacy.curseforge.com/account/api-tokens](https://legacy.curseforge.com/account/api-tokens).
3. Add repo secrets `CURSEFORGE_TOKEN` and `CURSEFORGE_ID` (the numeric project id). If you skip this, the workflow just skips CurseForge.

## Modrinth project settings

| Field | Value |
|---|---|
| Name | Blindfold |
| Slug / URL | `blindfold` |
| Project type | Mod |
| Summary | see below |
| Description | see below |
| Categories | Utility, Game Mechanics |
| Environments | Client: **Optional**, Server: **Optional** |
| License | MIT |
| Source / Issues | https://github.com/ouroboros-smp/blindfold |
| Mod loaders | Fabric |
| Game versions | 1.21.11 |
| Dependencies | Fabric API (Required) |
| Icon | (add one; 512x512 PNG recommended) |

### Summary (short, ~256 char max)

```
Toggle the real vanilla Blindness or Darkness effect on yourself. Client-side as a private toggle with no sprint penalty, or server-side so any player can opt in with /blindfold - no client mod needed.
```

### Description (paste into the description editor)

```markdown
Blindfold puts the **real** vanilla Blindness (or Warden-style Darkness) effect on you - only ever by your own choice. You get the genuine blindness fog and darkening, not a screen filter. One jar, two ways to use it:

## Client-side (install it yourself)
A private, local toggle. The sprint/swim penalty is removed while the effect is on, so you move normally. It is entirely local: the server is never contacted and never knows. Works on any server or in singleplayer.

- Toggle with a keybind (default **B**, rebindable in Controls) or the `/blindfold` command.
- Two effects, switchable in config or on the fly: **Blindness** (steady heavy fog) and **Darkness** (Warden pulse).
- **Visual only**: the vanilla Blindness sprint/swim penalty is dropped while the effect is active. Real gameplay blindness is untouched when the toggle is off.
- **Session-only**: it always starts off when the game launches; nothing is forced on you.

## Server-side (install it on a Fabric server)
Every player can opt in with `/blindfold on` - no OP needed, and no client mod needed. The server applies the genuine effect to just that player until they opt out. Opt-in lasts for the server session and is never forced on anyone.

- `/blindfold style blindness | darkness` picks a personal style; the server config sets the default.
- Self-healing by design: the server applies a short, self-refreshing effect, so if the server crashes or the mod is removed nothing permanent is left on any player.
- Vanilla clients keep vanilla rules with the `blindness` style (no sprinting while blind); pick `darkness` for no sprint penalty, or install the client mod for the penalty-free local toggle.

## Commands
`/blindfold` — show state
`/blindfold toggle | on | off`
`/blindfold style blindness | darkness`
`/blindfold reload` (client: reload local config; server: OP only)

## Requirements
Minecraft 1.21.11, Fabric Loader 0.19.2+, Fabric API, Java 21+.
```

## Releasing a version (automated)

Once `MODRINTH_TOKEN` is set, publishing is a tag away:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The `publish` workflow builds the jar and pushes it to Modrinth (and CurseForge if configured, and the GitHub release). Version, loader, game version, and the Fabric API dependency are read from `fabric.mod.json`.

To publish the **current** build the first time, either run the `publish` workflow manually from the Actions tab (workflow_dispatch) after the token is set, or upload `build/libs/blindfold-1.0.0.jar` by hand on the Modrinth version page.

## Notes
- The workflow lives at `.github/workflows/publish.yml` and targets Modrinth slug `blindfold`.
- CurseForge is optional and skipped automatically when `CURSEFORGE_TOKEN` is absent.
