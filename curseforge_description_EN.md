# HBM Tweaks

**Fine-tune every number in HBM's Nuclear Tech — per bullet, per gun, per mob, per tool. Your modpack, your numbers.**

HBM Tweaks is a configuration-driven value tweaking add-on for **HBM's Nuclear Tech - Community Edition (NTM-CE) 2.5.0.5** on Minecraft **1.12.2**. It exposes HBM's internals as a clean, documented JSON config — so modpack authors (and players who love tinkering) can rebalance anything without touching a single line of Java.

**The short version:** install, open `config/hbmtweak/hbmtweaks.json`, change a number, restart. No preset values, no forced meta — the mod itself does nothing until *you* say so.

---

##  Key Features

| | |
|---|---|
|  **157 per-bullet configs** | Damage, armor piercing, threshold negation, knockback, headshot multiplier, spread, velocity, penetration — per individual bullet type |
|  **Per-gun overrides** | Fix caliber-sharing quirks (e.g. one shared .22 bullet across several guns) by overriding a whole gun's base damage |
|  **Grenades** | All 13 universal-grenade fillings (gunpowder → nuclear) plus stick dynamite |
|  **Mobs** | Health (multiplier **or absolute value**), damage dealt, damage taken, speed, knockback resistance, per-hit damage cap, fire/magic immunity |
|  **Melee & tools** | Swords, pickaxes, axes, shovels, chainsaws, wrenches — **tooltip and actual damage always match** |
|  **Turrets** | Targeting range, power consumption, rotation speed, and direct-damage (Tauon/microwave) multiplier |
|  **Explosions** | Artillery (Arty / HIMARS) blast damage |
|  **Food** | Hunger & saturation for any item |
|  **Nukes** | Every BombConfig field (blast radius, etc.), safely toggle-gated |
|  **Auto ID reference** | `hbm_tweaks_ids.txt` regenerates on every launch with the full list of adjustable IDs |

---

##  Design Principles

1. **Default = vanilla.** Every toggle is off, every multiplier is 1.0. Install it and nothing changes.
2. **Zero hardcoded values.** The mod ships with no opinion about what numbers you *should* use.
3. **Mod ≠ modpack.** The mod is a generic tool; your values live in *your* config. Updating the mod never touches your balance.
4. **Explicit over implicit.** Every configured value is a *final* value — no hidden math, no surprise multipliers.
5. **Safe by construction.** All Mixin injections are `required=false`; if a future HBM update breaks one, only that feature silently degrades. The game never crashes because of HBM Tweaks.

---

##  Installation

1. **Dependencies**
   - Minecraft **1.12.2** (Forge **14.23.5.2864+**)
   - **HBM's Nuclear Tech - Community Edition 2.5.0.5** (required)
   - **MixinBooter** (required for the Mixin-based features)
2. Drop `hbmtweaks-1.3.0.jar` into your `mods/` folder.
3. Launch the game once. The mod generates:
   - `config/hbmtweak/hbmtweaks.json` — your config file
   - `config/hbmtweak/hbm_tweaks_ids.txt` — every adjustable ID, auto-updated
4. Edit the config, restart. Done.

>  **Use a UTF-8 *without BOM* editor** (VS Code, Notepad++, etc.). A BOM at the start of the JSON will make the game fall back to defaults.

---

##  Configuration Quick Start

```jsonc
// config/hbmtweak/hbmtweaks.json
{
  // Halve 12-gauge buckshot damage
  "weapons": [ { "bulletName": "g12", "damageMult": 0.5 } ],

  // AM180: base damage ×5
  "guns": [ { "item": "hbm:gun_am180", "damageMult": 5.0 } ],

  // UFO: health fixed at exactly 100 (absolute — no math)
  "mobs": [ { "entityClass": "com.hbm.entity.mob.EntityUFO", "healthSet": 100 } ],

  // Meteorite sword: deals exactly 4 per hit (tooltip matches)
  "tools": [ { "item": "hbm:meteorite_sword", "damageSet": 4 } ],

  // HE grenade filling: explosion ×0.2
  "enableGrenadeOverride": true,
  "grenades": [ { "filling": "HE", "damageMult": 0.2 } ],

  // Bread: restores full hunger
  "foods": [ { "item": "minecraft:bread", "hunger": 20 } ]
}
```

**Two value styles everywhere:**
- `damageSet` / `healthSet` — **absolute target values** (write `4` to deal 4 damage; zero math)
- `damageMult` / `healthMult` — **multipliers** (write `0.5` to halve)

Every category has a `"_xxx_usage"` comment in the config explaining its fields, and `hbm_tweaks_ids.txt` lists every valid ID.

---

##  FAQ

**Q: Does this mod change anything out of the box?**
No. Every toggle defaults to off and every multiplier to 1.0. It is a blank tool until you configure it.

**Q: I set a value but nothing changed.**
- Did you restart the game? All values load at startup.
- Check the bullet/item/class name against `hbm_tweaks_ids.txt` (typos are logged as warnings).
- Mob `entityClass` must match the *runtime* class exactly — subclasses need their own entry.

**Q: The tooltip still shows the old number on my sword.**
Melee tools are updated in-place, so tooltips match. If you mean a **gun** — gun tooltips show base damage × ammo multiplier, which reflects `guns`/`weapons` configs correctly.

**Q: Will this break when HBM updates?**
Possible for individual features, but never fatal: failed injections degrade silently (logged as warnings) and the rest keeps working.

**Q: Does this touch HBM's files?**
No. Everything happens at runtime via reflection, Forge events, and Mixin. HBM's jar is never modified.

---

##  Changelog

**1.3.0**
- Full mod/modpack separation: all values config-driven, all toggles default off
- `healthSet` (absolute mob health), `damageSet` (absolute melee damage)
- Melee tooltips now match actual damage (in-place attribute editing)
- Grenade per-filling overrides; turret direct-damage & artillery multipliers
- Per-gun, array-bullet (rockets), grenade & melee caching; auto ID docs

**1.2.0**
- Per-gun damage, outgoing mob damage, turret/artillery/grenade compression, glyphid & BOT-Prime support, Mixin expansion

**1.1.0**
- Mob damage caps, projectile/explosion multipliers, fire/magic immunity, turret Mixin, config templates

**1.0.0**
- Initial release: bullets, legacy guns, mobs, food, nukes, turret attributes

---

##  License & Credits

**HBM Tweaks is open source under the MIT License.** [Source code](https://github.com/dmd114514-cmyk/hbmtweaks) is available on GitHub.

This is an **independent add-on**, not an official HBM project. It interoperates with HBM's Nuclear Tech - Community Edition at runtime under that project's open license (GPL/LGPL) and contains **no copy of HBM code**. All credit for the base game goes to the HBM mod team and the NTM-CE community.

---

##  Support

Found a bug or have a balance question? Leave a comment below with:
- `logs/latest.log` (from your game folder)
- The relevant section of your `hbmtweaks.json`

Happy tuning!
