# HBM Tweaks (hbmtweaks) 模组使用指南

**[English](README_EN.md)** | **简体中文**

> 版本：1.3.0  |  适用：Forge 1.12.2（14.23.5.2864）+ HBM 核科技 NTM-CE 2.5.0.5
> 简介：HBM 核科技的数值调整附属模组——不改动任何 HBM 代码，通过反射/事件/Mixin 在运行时调整
> 武器、手榴弹、爆炸、炮台、怪物、近战工具、食物、核弹的数值。**所有数值全部配置化，模组本身不改动任何东西（默认开关全关）**。
>
> **数值中性声明**：本模组**不预设任何数值体系**——压缩倍率、生物血量/伤害上限等全部由
> 玩家（整合包作者）在 `hbmtweaks.json` 中自行定义。文档中的示例数值仅为展示字段格式，不代表任何推荐值。

---

## 目录

1. [安装](#1-安装)
2. [配置文件位置与格式](#2-配置文件位置与格式)
3. [总优先级规则](#3-总优先级规则)
4. [功能模块详解](#4-功能模块详解)
   - [4.1 武器/弹种 weapons](#41-武器弹种-weapons)
   - [4.2 枪械本体 guns](#42-枪械本体-guns)
   - [4.3 手榴弹 grenades](#43-手榴弹-grenades)
   - [4.4 怪物 mobs](#44-怪物-mobs)
   - [4.5 近战武器/工具（meleeDamageMult + tools）](#45-近战武器工具-meleedamagemult--tools)
   - [4.6 炮台 turrets + 直接伤害倍率](#46-炮台-turrets--直接伤害倍率)
   - [4.7 炮弹爆炸 artillery](#47-炮弹爆炸-artillery)
   - [4.8 食物 foods](#48-食物-foods)
   - [4.9 核弹 nukes](#49-核弹-nukes)
   - [4.10 全局数值](#410-全局数值)
5. [ID 参考文档](#5-id-参考文档)
6. [完整配置模板](#6-完整配置模板)
7. [常见问题](#7-常见问题)
8. [版本历史](#8-版本历史)

---

## 1. 安装

1. 将 `hbmtweaks-1.3.0.jar` 放入 `mods/` 文件夹
2. 依赖（必须已安装）：
   - Forge 1.12.2（14.23.5.2864 及以上）
   - HBM 核科技 NTM-CE **2.5.0.5**（modid `hbm`）
   - MixinBooter（mixin 支持库，1.12.2 常用版本 11.x）
3. 首次启动自动生成：
   - `config/hbmtweak/hbmtweaks.json` —— 配置文件（空模板）
   - `config/hbmtweak/hbm_tweaks_ids.txt` —— 可调整 ID 完整参考列表（每次启动自动更新）
4. 编辑配置后**重启游戏**生效

> 兼容性：模组默认**全部开关关闭、不改动任何数值**；只有你在配置里显式开启/填写后才会生效。
> 适合整合包作者：把具体数值写进自己的 hbmtweaks.json，模组保持通用。

---

## 2. 配置文件位置与格式

- 路径：`config/hbmtweak/hbmtweaks.json`
- 格式：标准 JSON（UTF-8 无 BOM）
- 每个模块有对应的 `"_xxx_usage"` 说明字段（Gson 解析时忽略，仅作文档）
- 数值字段：**倍率类**（damageMult/healthMult 等）= 乘数（1.0 = 不变，0.5 = 减半，2.0 = 翻倍）；**绝对类**（damageSet/damageCap/hunger 等）= 直接覆盖

**编辑注意**：请使用支持 UTF-8 无 BOM 的编辑器（VS Code / Notepad++ 选 UTF-8 without BOM）。带 BOM 的 JSON 会导致解析失败（模组回退默认值）。

---

## 3. 总优先级规则

```
玩家自定义项（weapons/guns/grenades/mobs/tools/turrets/foods/nukes 列表里的条目）
        >
类别全局数值（globalDamageMult / globalMobHealthMult / meleeDamageMult / turretDamageMult / grenadeDamageMult 等）
        >
HBM 原版数值
```

附加规则：
- **某类别只要填写了任意一个自定义项，该类别的全局数值自动禁用**（weapons 填了 → globalDamageMult 失效；mobs 填了 → globalMobHealthMult 失效）。用于"我要精确控制，别让全局兜底"
- per-item/per-filling（guns/tools/grenades）**与类别全局相乘**，不互斥：例如 `meleeDamageMult=0.1` + `tools:[{item:"hbm:dnt_sword", damageMult:0.5}]` → DNT 剑实际 0.05
- 所有"独立倍率"类（turretDamageMult/grenadeDamageMult/artilleryDamageMult/meleeDamageMult）必须配合对应布尔开关（enable*Override）为 true 才生效

---

## 4. 功能模块详解

### 4.1 武器/弹种 weapons

修改 HBM sedna 武器系统的**弹药**属性。弹种名 = 源码 XFactory 字段名（157 个，见 ids 文件 weapons 部分）。

```json
"weapons": [
  { "bulletName": "g12", "damageMult": 1.5, "penetrate": true },
  { "bulletName": "m357_ap", "damageSet": 2.0 }
]
```

| 字段 | 类型 | 说明 |
|---|---|---|
| bulletName | string | 弹种名（必须） |
| damageMult | double | 伤害倍率（乘在弹种原伤害系数上） |
| damageSet | double | 直接设定弹种伤害系数（绝对覆盖） |
| armorPiercing | double | 穿甲百分比 0~1（无视护甲比例） |
| thresholdNegation | double | 护甲阈值穿透（绝对） |
| knockback | double | 击退倍率（绝对） |
| headshotMult | double | 爆头倍率（绝对） |
| spreadMult | double | 散布倍率（<1 更准） |
| velocityMult | double | 弹速倍率 |
| penetrate | boolean | 是否穿透目标 |
| rateOfFireMult | double | 射速倍率（仅旧系统 ItemGunBase 生效） |
| ammoCapMult | double | 弹匣倍率（仅旧系统） |

**同口径共享**：同一弹种被多把枪共用（如 p22 供 am180/star_f），修改影响所有使用它的枪。枪械本体差异用 4.2 guns 调整。

**数组弹种**：火箭（rocket_rpzb/rocket_qd/rocket_ml）是弹种数组，按数组名配置会作用于该阵列全部弹种（如 `{"bulletName":"rocket_rpzb","damageMult":0.15}` 作用于 5 种火箭弹）。

### 4.2 枪械本体 guns

按**整把枪**覆写基础伤害（解决同口径共享倍率导致的偏差）。注册名见 ids 文件 guns 部分（74 把）。

```json
"guns": [
  { "item": "hbm:gun_lag", "damageMult": 0.73 },
  { "item": "hbm:gun_stg77", "damageMult": 1.6 }
]
```

| 字段 | 类型 | 说明 |
|---|---|---|
| item | string | 枪械物品注册名（必须） |
| damageMult | double | 该枪所有弹种的基础伤害倍率（与弹种倍率相乘） |

最终伤害 = 枪基础伤害 × 枪倍率 × 弹种倍率。

### 4.3 手榴弹 grenades

修改手榴弹（万能手榴弹 grenade_universal 的 13 种装药 + 炸药棒）爆炸伤害。

```json
"enableGrenadeOverride": true,          // 总开关
"grenadeDamageMult": 0.2,               // 万能手榴弹装药爆炸 全局兜底
"dynamiteDamageMult": 0.1,              // 炸药棒/钓鱼炸药 全局兜底
"grenades": [
  { "filling": "HE", "damageMult": 0.25 },            // 按装药名
  { "item": "hbm:stick_dynamite", "damageMult": 0.2 } // 按物品名
]
```

| 字段 | 类型 | 说明 |
|---|---|---|
| filling | string | 装药枚举名：POWDER/HE/DEMO/INC/WP/CLUSTER/EMP/PLASMA/LASER/CLUSTER_HEAVY/NUCLEAR/NUCLEAR_DEMO/SCHRAB |
| item | string | 投掷物物品注册名（如 hbm:stick_dynamite / hbm:stick_dynamite_fishing） |
| damageMult | double | 爆炸伤害倍率（与全局 grenadeDamageMult 或 dynamiteDamageMult 相乘） |

优先级：per-装药 > 全局 grenadeDamageMult；炸药棒 per-物品 > 全局 dynamiteDamageMult。
集束/激光破片弹种（fragmentation/pellets/pellets_heavy/laser）在 4.1 weapons 里按弹种名调整。

### 4.4 怪物 mobs

修改 HBM 怪物（33 类，含 glyphid 甲虫系 / BOT Prime / 面具人 / UFO 等）。类名见 ids 文件 mobs 部分。

```json
"mobs": [
  { "entityClass": "com.hbm.entity.mob.EntityGlowingOne", "healthMult": 1.5, "outgoingDamageMult": 0.8 },
  { "entityClass": "com.hbm.entity.mob.EntityUFO", "healthSet": 100 },
  { "entityClass": "com.hbm.entity.mob.glyphid.EntityGlyphid", "healthMult": 1.2 }
]
```

| 字段 | 类型 | 说明 |
|---|---|---|
| entityClass | string | 实体类名（必须，精确匹配运行时类） |
| healthMult | double | 血量倍率（生成时修改最大血量并同步当前血） |
| healthSet | double | **直接设定血量上限**（绝对值覆盖，如 100 = 血量固定 100，无需反算倍率；与 healthMult 二选一，healthSet 优先） |
| damageMult | double | 近战攻击属性倍率 |
| outgoingDamageMult | double | **该怪造成的一切伤害倍率**（近战/弹道/爆炸全源，覆盖远程怪如火箭怪/爆炸怪） |
| speedMult | double | 移动速度倍率 |
| knockbackResist | double | 击退抗性 0~1（绝对） |
| damageCap | double | 该怪**单次受到的伤害上限**（防秒杀） |
| projectileDamageMult | double | 该怪受到的弹道伤害倍率 |
| explosionDamageMult | double | 该怪受到的爆炸伤害倍率 |
| fireImmune | boolean | 火焰免疫 |
| magicImmune | boolean | 魔法免疫 |

> 注意：entityClass 是**精确匹配**（entity.getClass().getName()）。子类必须单独配置
> （如 EntityTaintCrab 继承 EntityCyberCrab，两者都要各自写一条）。

### 4.5 近战武器/工具（meleeDamageMult + tools）

修改 HBM 剑/镐/斧/铲/电锯/扳手等**可当武器的物品**的攻击伤害（原版剑/工具不受影响）。

```json
"meleeDamageMult": 0.1,     // 全部 HBM 近战/工具攻击 ×0.1
"tools": [
  { "item": "hbm:dnt_sword", "damageMult": 0.5 }  // 单件再乘
]
```

| 字段 | 类型 | 说明 |
|---|---|---|
| meleeDamageMult | double | 全局近战/工具倍率 |
| tools[].item | string | 物品注册名（71 件，见 ids 文件 tools 部分） |
| tools[].damageMult | double | 单件倍率（与全局相乘） |

### 4.6 炮台 turrets + 直接伤害倍率

两类调整：

**a) 炮台属性**（range 检测范围 / consumption 耗电 / yawSpeed 转速）：
```json
"turrets": [
  { "entityClass": "com.hbm.tileentity.turret.TileEntityTurretSentry", "range": 48.0, "consumption": 10 }
]
```
弹种型炮台（哨戒/切科夫/霍华德/战列/导弹）的**伤害随对应弹药倍率自动压缩**（4.1），无需单独配置。

**b) 炮台直接伤害倍率**（陶子炮 Tauon 的电击 / 微波炮 Maxwell / 特斯拉类环境电伤）：
```json
"enableTurretDamageOverride": true,   // 必须 true 才生效
"turretDamageMult": 0.1
```

### 4.7 炮弹爆炸 artillery

Arty 炮兵阵地 / HIMARS 的炮弹爆炸伤害（原版中心伤害 160-240，极高）。

```json
"enableArtilleryOverride": true,   // 必须 true 才生效
"artilleryDamageMult": 0.1
```

### 4.8 食物 foods

修改食物饥饿值/饱和度。物品注册名见 ids 文件 foods 部分（罐头带 meta）。

```json
"foods": [
  { "item": "hbm:cheese", "hunger": 5, "saturation": 0.8 },
  { "item": "hbm:canned_conserve:8", "hunger": 4 }
]
```

| 字段 | 类型 | 说明 |
|---|---|---|
| item | string | 物品注册名（必须） |
| hunger | int | 饥饿值（半鸡腿数，5 = 2.5 鸡腿） |
| saturation | double | 饱和度系数 |

### 4.9 核弹 nukes

修改 BombConfig 字段（爆炸半径等）。**必须 enableNukeOverride=true**（避免与 hbm.cfg 的 03_nukes 段冲突——开启后本列表覆盖 hbm.cfg）。

```json
"enableNukeOverride": true,
"nukes": [
  { "field": "tsarRadius", "value": 800 },
  { "field": "fatmanRadius", "value": 15 }
]
```

| 字段 | 类型 | 说明 |
|---|---|---|
| field | string | BombConfig 字段名（32 个，见 ids 文件 nukes 部分，与 hbm.cfg 03_nukes 同名） |
| value | int | 新值 |

### 4.10 全局数值

仅当对应类别**没有任何自定义项**时生效（见 §3 优先级）。

```json
"globalDamageMult": 1.0,        // 全部弹种伤害倍率（weapons 为空时生效）
"globalRateOfFireMult": 1.0,    // 旧系统射速
"globalAmmoCapMult": 1.0,       // 旧系统弹匣
"globalMobHealthMult": 1.0,     // HBM 怪血量（mobs 为空时生效，只作用 com.hbm.* 生物）
"globalMobDamageMult": 1.0      // HBM 怪近战伤害
```

---

## 5. ID 参考文档

`config/hbmtweak/hbm_tweaks_ids.txt` 每次启动自动重新生成，包含全部可用 ID：
- 【weapons】157 个弹种名
- 【guns】74 把枪械注册名
- 【grenades】13 个手榴弹装药枚举名
- 【mobs】HBM 生物实体类名
- 【turrets】13 座炮台类名
- 【tools】71 件近战/工具物品注册名
- 【foods】食物注册名
- 【nukes】32 个 BombConfig 字段名

> 若某 ID 失效，可能是 HBM 版本更新导致字段名变化，对照源码或模组日志。

---

## 6. 完整配置模板

首次启动自动生成（含全部说明字段），结构如下：

```json
{
  "globalDamageMult": 1.0,
  "globalRateOfFireMult": 1.0,
  "globalAmmoCapMult": 1.0,
  "weapons": [],
  "guns": [],
  "meleeDamageMult": 1.0,
  "tools": [],
  "enableGrenadeOverride": false,
  "grenadeDamageMult": 1.0,
  "dynamiteDamageMult": 1.0,
  "grenades": [],
  "enableTurretDamageOverride": false,
  "turretDamageMult": 1.0,
  "enableArtilleryOverride": false,
  "artilleryDamageMult": 1.0,
  "globalMobHealthMult": 1.0,
  "globalMobDamageMult": 1.0,
  "mobs": [],
  "foods": [],
  "enableNukeOverride": false,
  "nukes": [],
  "turrets": []
}
```

---

## 7. 常见问题

**Q1：修改配置后不生效？**
重启游戏。所有数值在启动时读取/应用。

**Q2：JSON 解析失败？**
配置文件必须是 UTF-8 **无 BOM**。带 BOM（记事本/PS 的 UTF8 编码可能写入）会导致解析失败，模组回退默认值。日志出现 `config JSON syntax error` 即此问题。

**Q3：某个弹种名配置了没反应？**
检查弹种名是否准确（ids 文件里查）。拼写不对会输出 `unknown bullet name` 警告。

**Q4：怪物血量改了但进游戏没变？**
- entityClass 必须精确匹配运行时类（子类单独配）
- 全局倍率在 mobs 列表非空时失效
- 确认该怪是 HBM 的（com.hbm.entity.*），SRP 等其它 mod 的怪不受 mobs 模块影响（SRP 用其自身配置的 Global Multiplier）

**Q5：为什么默认什么都不改？**
模组设计为"通用工具"：所有开关默认 false、所有倍率默认 1.0。整合包作者把数值写进自己的配置。这样模组升级不会影响已有整合包。

**Q6：与 hbm.cfg 的关系？**
hbm.cfg 是 HBM 自身的配置。除核弹段（需 enableNukeOverride 接管）外，本模组通过运行时调整而非改 hbm.cfg，两者互不干扰。核弹段开启接管后以本模组 nukes 列表为准。

**Q7：Mixin 报错/模组加载警告？**
本模组使用 Mixin 注入 HBM 的少量内部方法（炮台属性/枪械基础伤害/爆炸结算/手榴弹爆炸）。需要 MixinBooter。若某个 mixin 因 HBM 版本变化失败，会输出 WARN 且**仅该项功能失效**（其余功能正常），游戏不崩溃。

**Q8：性能影响？**
事件路径均为 O(1) HashMap 查找；mixin 注入仅在被注入方法调用时生效，无配置时直接放行（纳秒级开销）。

---

## 8. 版本历史

| 版本 | 内容 |
|---|---|
| 1.0.0 | 基础版：弹种/旧枪械/怪物/食物/核弹/炮台属性调整，ids 参考文档 |
| 1.1.0 | 怪物受击规则扩展（damageCap/弹道/爆炸倍率/火免/魔免），TurretMixin，模板生成 |
| 1.2.0 | per-gun 枪械倍率、数组弹种（火箭）、怪物造成伤害倍率 outgoingDamageMult、炮台直接伤害、炮弹爆炸压缩、手榴弹体系（装药/破片/炸药棒）、近战工具压缩、怪物全量补全（glyphid/BOT Prime 等）、Mixin 扩展 |
| 1.3.0 | **模组/整合包彻底分离重构**：全部数值开关化+配置化（enable*Override）、手榴弹 per-装药/per-物品自定义（grenades 数组）、模组零硬编码 |

---

*文档由 hbmtweaks 项目维护，随模组发布。*
