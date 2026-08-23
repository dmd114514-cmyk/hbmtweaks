# HBM Tweaks (hbmtweaks)

**为 HBM 核科技（NTM-CE）打造的全功能数值调整工具 —— 不预设任何数值，全部由你的配置决定。**

---

## 简介

HBM Tweaks 是一个面向 HBM 核科技（HBM's Nuclear Tech - Community Edition）的数值调整附属模组。它可以让你**逐弹种、逐把枪、逐只怪、逐件装备**地调整 HBM 的几乎一切数值——从 .22 子弹的伤害，到面具人 BOSS 的血量，再到炮塔的锁敌范围。

**设计哲学：**
- 🔒 **默认不改动任何数值**——所有开关默认关闭、所有倍率默认 1.0
- 📝 **零硬编码**——模组内不含任何预设数值，你想怎么改就怎么改
- 🧩 **模组与整合包分离**——模组只提供能力，你的数值写在配置里，更新模组不影响整合包
- ⚡ **无需重编译**——改配置重启即可，调整数值像编辑文本一样简单

---

## 核心特性

| 功能 | 说明 |
|---|---|
| 🎯 **弹种级武器调整** | 157 个 sedna 弹种逐一调整：伤害、穿甲、护甲阈值穿透、击退、爆头倍率、散布、弹速、穿透 |
| 🔫 **枪械本体调整** | 按整把枪覆写基础伤害（per-gun），解决同口径共享倍率的偏差，tooltip 同步显示 |
| 💣 **手榴弹调整** | 万能手榴弹 13 种装药（火药/高爆/集束/EMP/等离子/核…）+ 炸药棒，逐装药调整 |
| 👹 **怪物调整** | 血量（倍率或绝对值）、造成伤害、受到伤害、速度、击退抗性、单次受伤上限、火焰/魔法免疫 |
| ⚔️ **近战/工具调整** | 剑/镐/斧/铲/电锯/扳手逐件调整，**tooltip 与实际伤害完全一致** |
| 🏰 **炮台调整** | 锁敌范围、耗电、转速 + 陶子炮/微波炮直接伤害倍率 |
| 💥 **爆炸调整** | 炮弹（Arty/HIMARS）爆炸伤害倍率 |
| 🍞 **食物调整** | 饥饿值、饱和度 |
| ☢️ **核弹调整** | BombConfig 全部字段（爆炸半径等），开关式接管 |
| 📖 **自动 ID 文档** | 每次启动自动生成完整可调整 ID 参考列表（hbm_tweaks_ids.txt） |

---

## 为什么选择 HBM Tweaks？

- **配置驱动，所见即所得**：所有数值在 `config/hbmtweak/hbmtweaks.json` 中显式配置，支持两种写法——绝对目标值（`damageSet: 2.1` = 每次攻击 2.1 血，零计算）和倍率（`damageMult: 0.5` = 减半）
- **tooltip 一致**：近战武器配置后，游戏内物品标签显示修改后的伤害，不再有"标签 7 实际 2.1"的困惑
- **安全降级**：Mixin 注入全部 `required=false`——即使 HBM 版本变化导致某项失效，也只是该功能静默降级，游戏不崩溃
- **彻底分离**：模组是通用工具，整合包作者把数值写进自己的配置文件——两者互不干扰

---

## 安装

1. 安装依赖：Forge **1.12.2**（14.23.5.2864+）、HBM NTM-CE **2.5.0.5**、MixinBooter
2. 将 `hbmtweaks-1.3.0.jar` 放入 `mods/` 文件夹
3. 启动游戏，模组会自动生成：
   - `config/hbmtweak/hbmtweaks.json` —— 配置文件
   - `config/hbmtweak/hbm_tweaks_ids.txt` —— 全部可调整 ID 参考列表
4. 编辑配置，重启游戏生效

**依赖要求：**
- Minecraft 1.12.2
- Forge 14.23.5.2864+
- HBM's Nuclear Tech - Community Edition 2.5.0.5（`required-after:hbm@[2.5.0.5,)`）
- MixinBooter

---

## 配置示例

```jsonc
// config/hbmtweak/hbmtweaks.json
{
  // 弹种：12号霰弹伤害减半
  "weapons": [ { "bulletName": "g12", "damageMult": 0.5 } ],

  // 枪械：AM180 基础伤害 ×5
  "guns": [ { "item": "hbm:gun_am180", "damageMult": 5.0 } ],

  // 怪物：UFO 血量固定 100（绝对值，无需反算倍率）
  "mobs": [ { "entityClass": "com.hbm.entity.mob.EntityUFO", "healthSet": 100 } ],

  // 近战：陨石剑每次攻击 4 血（tooltip 同步显示）
  "tools": [ { "item": "hbm:meteorite_sword", "damageSet": 4 } ],

  // 手榴弹：高爆装药爆炸 ×0.2
  "enableGrenadeOverride": true,
  "grenades": [ { "filling": "HE", "damageMult": 0.2 } ],

  // 食物：面包回满饥饿
  "foods": [ { "item": "minecraft:bread", "hunger": 20 } ]
}
```

完整字段说明见配置文件内的 `_usage` 注释和 `hbm_tweaks_ids.txt`。

---

## 兼容性

- 适用于 HBM NTM-CE **2.5.0.5**（其它版本可能因内部结构变化导致部分功能失效，失效时自动降级）
- 与 CraftTweaker / GameStages / JEI 等常用模组无冲突
- 不改动 HBM 的任何源文件，通过反射 / 事件 / Mixin 在运行时调整

## 许可

HBM Tweaks 是独立附属模组，**非 HBM 官方作品**。仅基于 HBM NTM-CE 的开源许可（LGPL/GPL）进行运行时互操作。模组本体代码独立编写，不含 HBM 代码副本。

---

## 支持与反馈

遇到问题或想了解更详细的调整方式，欢迎在评论区反馈（附上 `logs/latest.log` 和相关配置片段）。
