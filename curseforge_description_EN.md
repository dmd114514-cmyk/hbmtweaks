# HBM Tweaks (hbmtweaks)

**A full-featured value tweaking utility for HBM's Nuclear Tech Mod — no preset values, everything is decided by your config.**

---

## Overview

HBM Tweaks is an add-on mod for HBM's Nuclear Tech - Community Edition (NTM-CE) that lets you adjust nearly every value in HBM — per-bullet, per-gun, per-mob, per-tool. From the damage of a .22 round to the health of the Mask Man boss, from turret targeting range to explosion radii.

**Design philosophy:**
- 🔒 **Changes nothing by default** — all toggles off, all multipliers at 1.0
- 📝 **Zero hardcoded values** — no preset numbers anywhere in the mod; you decide everything
- 🧩 **Mod / modpack separation** — the mod provides capability, your values live in your config; updating the mod never touches your modpack
- ⚡ **No recompiling** — edit config, restart, done

---

## Features

| Feature | Description |
|---|---|
| 🎯 **Per-bullet weapon tuning** | Adjust all 157 sedna bullet configs individually: damage, armor piercing, threshold negation, knockback, headshot multiplier, spread, velocity, penetration |
| 🔫 **Per-gun tuning** | Override base damage of whole guns, fixing caliber-sharing quirks; tooltip updates live |
| 💣 **Grenade tuning** | All 13 universal grenade fillings (gunpowder/HE/cluster/EMP/plasma/nuclear…) plus dynamite |
| 👹 **Mob tuning** | Health (multiplier or absolute), damage dealt, damage taken, speed, knockback resistance, per-hit damage cap, fire/magic immunity |
| ⚔️ **Melee/tool tuning** | Swords/pickaxes/axes/shovels/chainsaws/wrenches — **tooltip matches actual damage** |
| 🏰 **Turret tuning** | Targeting range, power consumption, rotation speed + direct-damage multiplier (Tauon/microwave) |
| 💥 **Explosion tuning** | Artillery (Arty/HIMARS) blast damage multiplier |
| 🍞 **Food tuning** | Hunger & saturation |
| ☢️ **Nuke tuning** | All BombConfig fields (blast radii, etc.), toggle-gated |
| 📖 **Auto-generated ID docs** | A complete reference of every adjustable ID is written to `hbm_tweaks_ids.txt` on every launch |

---

## Why HBM Tweaks?

- **Config-driven, what-you-see-is-what-you-get**: everything lives in `config/hbmtweak/hbmtweaks.json`, supporting both absolute values (`"damageSet": 2.1` = exactly 2.1 damage per hit, zero math) and multipliers (`"damageMult": 0.5` = halve it)
- **Tooltip consistency**: tuned melee weapons show their new damage on the item tooltip — no more "label says 7, actually hits 2.1"
- **Safe degradation**: every Mixin injection is `required=false` — if a future HBM update breaks one, only that feature silently degrades; the game never crashes
- **Clean separation**: the mod stays generic; modpack authors put their numbers in their own config

---

## Installation

1. Dependencies: Forge **1.12.2** (14.23.5.2864+), HBM NTM-CE **2.5.0.5**, MixinBooter
2. Drop `hbmtweaks-1.3.0.jar` into the `mods/` folder
3. Launch once — the mod generates:
   - `config/hbmtweak/hbmtweaks.json` — the config file
   - `config/hbmtweak/hbm_tweaks_ids.txt` — full ID reference
4. Edit config, restart the game

**Requirements:**
- Minecraft 1.12.2
- Forge 14.23.5.2864+
- HBM's Nuclear Tech - Community Edition 2.5.0.5 (`required-after:hbm@[2.5.0.5,)`)
- MixinBooter

---

## Config examples

```jsonc
// config/hbmtweak/hbmtweaks.json
{
  // Bullets: halve 12-gauge damage
  "weapons": [ { "bulletName": "g12", "damageMult": 0.5 } ],

  // Guns: AM180 base damage ×5
  "guns": [ { "item": "hbm:gun_am180", "damageMult": 5.0 } ],

  // Mobs: UFO health fixed at 100 (absolute, no math needed)
  "mobs": [ { "entityClass": "com.hbm.entity.mob.EntityUFO", "healthSet": 100 } ],

  // Melee: meteorite sword deals exactly 4 per hit (tooltip matches)
  "tools": [ { "item": "hbm:meteorite_sword", "damageSet": 4 } ],

  // Grenades: HE filling explosion ×0.2
  "enableGrenadeOverride": true,
  "grenades": [ { "filling": "HE", "damageMult": 0.2 } ],

  // Food: bread restores full hunger
  "foods": [ { "item": "minecraft:bread", "hunger": 20 } ]
}
```

Full field documentation lives in the `_usage` comments inside the config and in `hbm_tweaks_ids.txt`.

---

## Compatibility

- Built for HBM NTM-CE **2.5.0.5** — other versions may lose individual features (they degrade gracefully)
- No conflicts with CraftTweaker, GameStages, JEI and other common mods
- Never modifies any HBM file — all changes happen at runtime via reflection, events and Mixin

## License

**HBM Tweaks is open source under the MIT License.** It is an independent add-on, not an official HBM project. It interoperates with HBM NTM-CE at runtime under that project's open license (LGPL/GPL); it contains no copy of HBM code.

---

## Support & Feedback

Questions or feature ideas? Leave a comment with your `logs/latest.log` and the relevant config snippet.
