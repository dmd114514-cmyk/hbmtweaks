# HBM Tweaks (hbmtweaks) — Mod User Guide

**English** | [简体中文](README.md)

> Version: 1.3.0 | For: Forge 1.12.2 (14.23.5.2864) + HBM Nuclear Tech NTM-CE 2.5.0.5
> A value tweaking add-on for HBM's Nuclear Tech — never touches any HBM file; it adjusts weapons, grenades, explosions, turrets, mobs, melee tools, food and nukes at runtime via reflection, events and Mixin. **Everything is config-driven; the mod itself changes nothing by default (all toggles off).**
>
> **Neutrality statement:** this mod ships with **no preset values** — multipliers, health/damage caps etc. are entirely defined by you in `hbmtweaks.json`. Any example values in this document only illustrate the config format, never a recommendation.

---

## Table of Contents

1. [Installation](#1-installation)
2. [Config file location & format](#2-config-file-location--format)
3. [Priority rules](#3-priority-rules)
4. [Modules in detail](#4-modules-in-detail)
   - [4.1 Weapons & bullets weapons](#41-weapons--bullets-weapons)
   - [4.2 Guns guns](#42-guns-guns)
   - [4.3 Grenades grenades](#43-grenades-grenades)
   - [4.4 Mobs mobs](#44-mobs-mobs)
   - [4.5 Melee & tools (tools)](#45-melee--tools-tools)
   - [4.6 Turrets + direct-damage multiplier](#46-turrets--direct-damage-multiplier)
   - [4.7 Artillery artillery](#47-artillery-artillery)
   - [4.8 Food foods](#48-food-foods)
   - [4.9 Nukes nukes](#49-nukes-nukes)
   - [4.10 Global values](#410-global-values)
5. [ID reference file](#5-id-reference-file)
6. [Config template](#6-config-template)
7. [FAQ](#7-faq)
8. [Changelog](#8-changelog)

---

## 1. Installation

1. Put `hbmtweaks-1.3.0.jar` into the `mods/` folder.
2. Dependencies (must be installed):
   - Forge 1.12.2 (14.23.5.2864+)
   - HBM Nuclear Tech NTM-CE **2.5.0.5** (modid `hbm`)
   - MixinBooter (Mixin library, 11.x for 1.12.2)
3. On first launch the mod generates:
   - `config/hbmtweak/hbmtweaks.json` — the config (empty template)
   - `config/hbmtweak/hbm_tweaks_ids.txt` — full reference of adjustable IDs (regenerated every launch)
4. Edit the config, then **restart the game**.

> Compatibility: every toggle is **off by default** — the mod changes nothing until you explicitly enable/configure it. Ideal for modpack authors: put your numbers in your own hbmtweaks.json, keep the mod generic.

---

## 2. Config file location & format

- Path: `config/hbmtweak/hbmtweaks.json`
- Format: standard JSON (UTF-8 **without BOM**)
- Each module has a `"_xxx_usage"` doc field (ignored by Gson, kept for reference)
- Value semantics: **multipliers** (`damageMult`/`healthMult` etc.) = factors (1.0 = unchanged, 0.5 = halved, 2.0 = doubled); **absolute** values (`damageSet`/`healthSet`/`damageCap`/`hunger` etc.) = direct overrides

**Note:** use an editor that writes UTF-8 without BOM (VS Code, Notepad++ → "UTF-8 without BOM"). A BOM at the start makes the JSON unparseable (the mod falls back to defaults).

---

## 3. Priority rules

```
Custom entries (weapons / guns / grenades / mobs / tools / turrets / foods / nukes)
    >
Category globals (globalDamageMult / globalMobHealthMult / turretDamageMult / etc.)
    >
HBM original values
```

Extra rules:
- **If a category has any custom entry, that category's global is disabled** (fill `weapons` → `globalDamageMult` ignored; fill `mobs` → `globalMobHealthMult` ignored)
- Per-item/per-filling entries (**guns/tools/grenades**) **multiply with category globals** — but note the standalone multipliers below are gated by their own toggles
- Standalone multipliers (`turretDamageMult` / `grenadeDamageMult`… ) require their `enable*Override` toggle = true

> As of 1.3.0 the melee category has no global anymore — `tools` entries are explicit final values only.

---

## 4. Modules in detail

### 4.1 Weapons & bullets weapons

Adjusts HBM sedna bullets. Bullet names = XFactory field names (157, see `weapons` section of the ID file).

```json
"weapons": [
  { "bulletName": "g12", "damageMult": 1.5, "penetrate": true },
  { "bulletName": "m357_ap", "damageSet": 2.0 }
]
```

| Field | Type | Description |
|---|---|---|
| bulletName | string | bullet name (required) |
| damageMult | double | damage multiplier |
| damageSet | double | set bullet damage factor directly (absolute) |
| armorPiercing | double | armor piercing 0~1 |
| thresholdNegation | double | armor threshold negation (absolute) |
| knockback | double | knockback multiplier |
| headshotMult | double | headshot multiplier |
| spreadMult | double | spread multiplier (<1 tighter) |
| velocityMult | double | bullet velocity multiplier |
| penetrate | boolean | does it penetrate |
| rateOfFireMult | double | fire-rate multiplier (legacy ItemGunBase only) |
| ammoCapMult | double | magazine multiplier (legacy only) |

**Shared bullets:** the same bullet is used by several guns (e.g. p22 feeds am180/star_f) — tweaking it affects every gun using it. Use `guns` for per-gun differences.

**Array bullets:** rockets (`rocket_rpzb`/`rocket_qd`/`rocket_ml`) are bullet *arrays*; configuring the array name applies to all its elements.

### 4.2 Guns guns

Overrides a whole gun's base damage (fixes caliber-sharing quirks). Names in `guns` section (74 guns).

```json
"guns": [
  { "item": "hbm:gun_lag", "damageMult": 0.73 }
]
```

| Field | Type | Description |
|---|---|---|
| item | string | gun registry name (required) |
| damageMult | double | damage multiplier for all bullets of this gun (multiplies with bullet tweaks) |

Final damage = gun base × gun multiplier × bullet multiplier.

### 4.3 Grenades grenades

Adjusts universal-grenade fillings (13) and dynamite.

```json
"enableGrenadeOverride": true,
"dynamiteDamageMult": 0.1,
"grenades": [
  { "filling": "HE", "damageMult": 0.25 },
  { "item": "hbm:stick_dynamite", "damageMult": 0.2 }
]
```

| Field | Type | Description |
|---|---|---|
| filling | string | POWDER/HE/DEMO/INC/WP/CLUSTER/EMP/PLASMA/LASER/CLUSTER_HEAVY/NUCLEAR/NUCLEAR_DEMO/SCHRAB |
| item | string | thrown-item registry name (stick_dynamite / stick_dynamite_fishing) |
| damageMult | double | explicit final explosion multiplier (unset filling = vanilla) |

Grenade fragment/beam bullets (`fragmentation`/`pellets`/`pellets_heavy`/`laser`) are tuned under `weapons`.

### 4.4 Mobs mobs

Adjusts HBM mobs (33 classes incl. glyphids / BOT Prime / Mask Man / UFO). Class names in `mobs` section.

```json
"mobs": [
  { "entityClass": "com.hbm.entity.mob.EntityUFO", "healthSet": 100 }
]
```

| Field | Type | Description |
|---|---|---|
| entityClass | string | entity class name (required, exact runtime class) |
| healthMult | double | health multiplier |
| healthSet | double | **absolute max health** (e.g. 100 = fixed 100 HP; no math) |
| damageMult | double | melee attack attribute multiplier |
| outgoingDamageMult | double | multiplier for **all damage this mob deals** (melee/projectile/explosion) |
| speedMult | double | movement speed multiplier |
| knockbackResist | double | knockback resistance 0~1 |
| damageCap | double | max damage per hit **taken** by this mob |
| projectileDamageMult | double | projectile damage multiplier against this mob |
| explosionDamageMult | double | explosion damage multiplier against this mob |
| fireImmune | boolean | immune to fire |
| magicImmune | boolean | immune to magic |

> `entityClass` is matched **exactly** (`entity.getClass().getName()`). Subclasses need their own entry (e.g. EntityTaintCrab extends EntityCyberCrab — configure both).

### 4.5 Melee & tools (tools)

Adjusts HBM swords/pickaxes/axes/shovels/chainsaws/wrenches. **Tooltips and actual damage match** (attribute edited in place; vanilla swords/tools unaffected unless configured).

```json
"tools": [
  { "item": "hbm:dnt_sword", "damageSet": 3.0 },
  { "item": "hbm:chainsaw", "damageMult": 0.5 }
]
```

| Field | Type | Description |
|---|---|---|
| item | string | registry name (71 items, see `tools` section) |
| damageSet | double | absolute damage per hit (e.g. 2.1 = exactly 2.1 damage) |
| damageMult | double | multiplier (optional; final value = original × mult) |

### 4.6 Turrets + direct-damage multiplier

Two kinds:

**a) Turret attributes** (range / consumption / yawSpeed):
```json
"turrets": [
  { "entityClass": "com.hbm.tileentity.turret.TileEntityTurretSentry", "range": 48.0 }
]
```
Bullet-firing turrets scale their damage automatically with the bullet tweaks in 4.1.

**b) Turret direct-damage multiplier** (Tauon electricity / microwave / Tesla-type):
```json
"enableTurretDamageOverride": true,
"turretDamageMult": 0.1
```

### 4.7 Artillery artillery

Arty / HIMARS shell blast damage (vanilla center damage is 160–240).

```json
"enableArtilleryOverride": true,
"artilleryDamageMult": 0.1
```

### 4.8 Food foods

```json
"foods": [
  { "item": "hbm:cheese", "hunger": 5, "saturation": 0.8 }
]
```

| Field | Type | Description |
|---|---|---|
| item | string | registry name (canned items use meta: `hbm:canned_conserve:8`) |
| hunger | int | hunger in half-shanks (5 = 2.5 shanks) |
| saturation | double | saturation modifier |

### 4.9 Nukes nukes

Adjusts BombConfig fields. **Requires `enableNukeOverride = true`** (avoids clashing with hbm.cfg's `03_nukes` section — when enabled, this list takes over).

```json
"enableNukeOverride": true,
"nukes": [
  { "field": "tsarRadius", "value": 800 }
]
```

| Field | Type | Description |
|---|---|---|
| field | string | BombConfig field name (32, same names as hbm.cfg 03_nukes) |
| value | int | new value |

### 4.10 Global values

Only active when their category has **no custom entries** (see §3).

```json
"globalDamageMult": 1.0,
"globalRateOfFireMult": 1.0,
"globalAmmoCapMult": 1.0,
"globalMobHealthMult": 1.0,
"globalMobDamageMult": 1.0
```

---

## 5. ID reference file

`config/hbmtweak/hbm_tweaks_ids.txt` is regenerated every launch and contains every usable ID:
- 【weapons】157 bullet names
- 【guns】74 gun registry names
- 【grenades】13 grenade filling names
- 【mobs】HBM entity class names
- 【turrets】13 turret class names
- 【tools】71 melee/tool registry names
- 【foods】food registry names
- 【nukes】32 BombConfig field names

> If an ID stops working, the HBM version may have changed field names — check the mod log or HBM sources.

---

## 6. Config template

The first launch generates the full template (with all doc fields):

```json
{
  "globalDamageMult": 1.0, "globalRateOfFireMult": 1.0, "globalAmmoCapMult": 1.0,
  "weapons": [], "guns": [], "tools": [],
  "enableGrenadeOverride": false, "dynamiteDamageMult": 1.0, "grenades": [],
  "enableTurretDamageOverride": false, "turretDamageMult": 1.0,
  "enableArtilleryOverride": false, "artilleryDamageMult": 1.0,
  "globalMobHealthMult": 1.0, "globalMobDamageMult": 1.0, "mobs": [],
  "foods": [], "enableNukeOverride": false, "nukes": [], "turrets": []
}
```

---

## 7. FAQ

**Q1: Config changes don't apply?**
Restart the game. All values are read/applied at startup.

**Q2: JSON parse failure?**
The file must be UTF-8 **without BOM**. A BOM (written by some editors) breaks parsing and the mod falls back to defaults — look for `config JSON syntax error` in the log.

**Q3: A bullet name does nothing?**
Check the exact name in the ID file. A typo logs `unknown bullet name`.

**Q4: Mob health changed but not in-game?**
- `entityClass` must exactly match the runtime class (subclasses need their own entry)
- globals are disabled when the category has custom entries
- mobs only covers HBM entities (`com.hbm.entity.*`); SRP and other mods' mobs use their own config

**Q5: Why does the mod change nothing by default?**
It is designed as a generic tool: all toggles default off, all multipliers 1.0. Modpack authors put their numbers in their own config, so updating the mod never affects existing modpacks.

**Q6: Relation to hbm.cfg?**
hbm.cfg is HBM's own config. Except for the nuke section (gated by `enableNukeOverride`), this mod adjusts at runtime instead of editing hbm.cfg. When the nuke override is enabled, the `nukes` list takes precedence.

**Q7: Mixin warnings / load warnings?**
This mod Mixin-injects a few HBM internals (turret attributes, gun base damage, explosion/grenade settlement). It needs MixinBooter. If a Mixin fails due to an HBM update, it logs a WARN and only that feature degrades — the game never crashes.

**Q8: Performance impact?**
All event paths are O(1) HashMap lookups; Mixin injections only run when the injected method is called, and pass straight through when unconfigured (nanoseconds).

---

## 8. Changelog

| Version | Content |
|---|---|
| 1.0.0 | Initial: bullets / legacy guns / mobs / food / nukes / turret attributes, ID reference file |
| 1.1.0 | Mob damage-cap, projectile/explosion multipliers, fire/magic immunity, TurretMixin, config templates |
| 1.2.0 | Per-gun damage, array bullets (rockets), outgoingDamageMult, turret direct damage, artillery compression, grenades (fillings/fragments/dynamite), melee & tools compression, glyphid/BOT-Prime support, Mixin expansion |
| 1.3.0 | **Full mod/modpack separation**: everything toggle-gated & config-driven (enable*Override), per-grenade & per-filling overrides, melee `damageSet` with matching tooltips, zero hardcoded values |

---

*Maintained with the hbmtweaks project. Released under the MIT License.*
