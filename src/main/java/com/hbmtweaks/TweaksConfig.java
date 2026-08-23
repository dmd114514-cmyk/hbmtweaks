package com.hbmtweaks;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置文件模型，对应 config/hbmtweak/hbmtweaks.json
 *
 * 优先级规则：
 *  1. 玩家在列表中自定义的项 > 全局数值 > HBM 原版数值
 *  2. 若某一类别中玩家自定义了【任意一个】条目，则该类别的全局数值调整自动禁用
 *     （全局数值仅在没有自定义项时作为批量兜底）
 *  3. 玩家未覆盖的任何数值保持 HBM 原版
 *
 * 用法示例（写进 JSON 即可，重启游戏生效）：
 *  weapons: [ { "bulletName": "g12", "damageMult": 1.5, "penetrate": true } ]
 *  mobs:    [ { "entityClass": "...EntityMaskMan", "healthMult": 0.1, "damageCap": 25.0, "projectileDamageMult": 0.25 } ]
 *  foods:   [ { "item": "hbm:cheese", "hunger": 5 } ]
 *  nukes:   [ { "field": "tsarRadius", "value": 800 } ]   // 需 enableNukeOverride=true
 *  turrets: [ { "entityClass": "...TurretSentry", "range": 48.0 } ]
 *
 * 完整可用 ID 列表见同目录下的 hbm_tweaks_ids.txt（模组启动时自动释放）。
 */
public class TweaksConfig {

    // ==================== 全局数值（优先级低于自定义项） ====================

    /** 全局武器伤害倍率：仅当 weapons 列表为空时生效 */
    public double globalDamageMult = 1.0;
    /** 全局射速倍率（仅当 weapons 列表为空时生效） */
    public double globalRateOfFireMult = 1.0;
    /** 全局弹匣容量倍率（仅当 weapons 列表为空时生效） */
    public double globalAmmoCapMult = 1.0;
    /** 全局怪物血量倍率（仅当 mobs 列表为空时生效，只作用于 com.hbm.* 生物） */
    public double globalMobHealthMult = 1.0;
    /** 全局怪物伤害倍率（仅当 mobs 列表为空时生效） */
    public double globalMobDamageMult = 1.0;

    /**
     * 炮台直接伤害倍率（对走"直接伤害"而非弹种的炮台生效）：
     * 目前作用于 electricity(陶子炮Tauon) 与 microwave(微波炮Maxwell) 两种无实体来源的伤害，
     * 以及特斯拉线圈等同类环境电击伤害。弹种型炮台（哨戒/切科夫/霍华德等）不受此影响——
     * 它们的伤害随对应弹种的 damageMult 自动压缩。
     * 注意：必须 enableTurretDamageOverride = true 才生效（模组默认不改动任何数值）。
     */
    public boolean enableTurretDamageOverride = false;
    public double turretDamageMult = 1.0;

    /**
     * 投掷物（手榴弹体系）伤害倍率：
     *  - grenades: 按装药枚举名（POWDER/HE/DEMO/INC/WP/CLUSTER/EMP/PLASMA/LASER/CLUSTER_HEAVY/
     *    NUCLEAR/NUCLEAR_DEMO/SCHRAB）或物品注册名（stick_dynamite/stick_dynamite_fishing）显式覆写
     *  - dynamiteDamageMult: 炸药棒 / 钓鱼炸药（MC 原版爆炸）单点倍率
     *  - 集束/激光破片弹种（fragmentation/pellets/pellets_heavy/laser）在 weapons 列表按弹种名调整
     * 注意：装药伤害为显式最终倍率（未配置的装药保持原版）；必须 enableGrenadeOverride = true 才生效。
     */
    public boolean enableGrenadeOverride = false;
    public double dynamiteDamageMult = 1.0;
    public List<GrenadeTweak> grenades = new ArrayList<>();

    /**
     * 炮击炮弹（Arty 炮兵阵地 / HIMARS）爆炸伤害倍率：
     * 注意：必须 enableArtilleryOverride = true 才生效（模组默认不改动任何数值）。
     */
    public boolean enableArtilleryOverride = false;
    public double artilleryDamageMult = 1.0;

    // ==================== 玩家自定义项（优先级最高） ====================

    /** 单个弹种调整（详见 ids 文件 weapons 部分） */
    public List<WeaponTweak> weapons = new ArrayList<>();
    /** 单个武器（枪械本体）调整：按物品注册名覆写整把枪的伤害（详见 ids 文件 guns 部分） */
    public List<GunTweak> guns = new ArrayList<>();
    /** 单个近战武器/工具调整：按物品注册名覆写攻击伤害（详见 ids 文件 tools 部分） */
    public List<MeleeTweak> tools = new ArrayList<>();
    /** 单个怪物调整（详见 ids 文件 mobs 部分） */
    public List<MobTweak> mobs = new ArrayList<>();
    /** 单个食物调整（详见 ids 文件 foods 部分） */
    public List<FoodTweak> foods = new ArrayList<>();
    /** 单个核弹字段调整（详见 ids 文件 nukes 部分） */
    public List<NukeTweak> nukes = new ArrayList<>();
    /** 单个炮台调整（详见 ids 文件 turrets 部分） */
    public List<TurretTweak> turrets = new ArrayList<>();

    // ==================== 核弹开关 ====================

    /**
     * 核弹修改总开关（避免与 hbm.cfg 的 03_nukes 段撞车）：
     *  - true:  使用 nukes 列表的值覆盖 BombConfig（hbm.cfg 中的对应段失效）
     *  - false: 完全不动 BombConfig，以 hbm.cfg 原配置为准（默认）
     */
    public boolean enableNukeOverride = false;

    // ==================== 工具方法 ====================

    /** weapons 列表是否有任何已启用的自定义项 */
    public boolean hasCustomWeapon() {
        for (WeaponTweak w : weapons) {
            if (w.damageMult != null || w.rateOfFireMult != null || w.ammoCapMult != null
                    || w.damageSet != null || w.armorPiercing != null || w.thresholdNegation != null
                    || w.knockback != null || w.headshotMult != null || w.spreadMult != null
                    || w.velocityMult != null || w.penetrate != null) return true;
        }
        return false;
    }

    /** mobs 列表是否有任何已启用的自定义项 */
    public boolean hasCustomMob() {
        for (MobTweak m : mobs) {
            if (m.healthMult != null || m.healthSet != null || m.damageMult != null || m.speedMult != null || m.knockbackResist != null
                    || m.damageCap != null || m.projectileDamageMult != null || m.explosionDamageMult != null
                    || m.outgoingDamageMult != null || m.fireImmune != null || m.magicImmune != null) return true;
        }
        return false;
    }

    /** guns 列表是否有任何已启用的自定义项 */
    public boolean hasCustomGun() {
        for (GunTweak g : guns) {
            if (g.item != null && !g.item.isEmpty() && g.damageMult != null) return true;
        }
        return false;
    }

    /** tools 列表是否有任何已启用的自定义项 */
    public boolean hasCustomTool() {
        for (MeleeTweak t : tools) {
            if (t.item != null && !t.item.isEmpty() && (t.damageMult != null || t.damageSet != null)) return true;
        }
        return false;
    }

    /** 全局武器倍率是否应生效（无自定义项时才生效） */
    public boolean isGlobalWeaponActive() {
        return !hasCustomWeapon();
    }

    /** 全局怪物倍率是否应生效（无自定义项时才生效） */
    public boolean isGlobalMobActive() {
        return !hasCustomMob();
    }

    public boolean isAnyWeaponTweak() {
        return isGlobalWeaponActive() && (globalDamageMult != 1.0 || globalRateOfFireMult != 1.0 || globalAmmoCapMult != 1.0)
                || hasCustomWeapon() || hasCustomGun() || hasCustomTool();
    }

    public boolean isAnyMobTweak() {
        return isGlobalMobActive() && (globalMobHealthMult != 1.0 || globalMobDamageMult != 1.0)
                || hasCustomMob();
    }

    public boolean isAnyFoodTweak() {
        for (FoodTweak f : foods) {
            if (f.hunger != null || f.saturation != null) return true;
        }
        return false;
    }

    public boolean isAnyNukeTweak() {
        // 必须显式开启 enableNukeOverride 才会接管核弹数值（避免与 hbm.cfg 撞车）
        if (!enableNukeOverride) return false;
        for (NukeTweak n : nukes) {
            if (n.value != null) return true;
        }
        return false;
    }

    public boolean isAnyTurretTweak() {
        for (TurretTweak t : turrets) {
            if (t.range != null || t.consumption != null || t.yawSpeed != null) return true;
        }
        return false;
    }

    public void normalize() {
        if (weapons == null) weapons = new ArrayList<>();
        if (guns == null) guns = new ArrayList<>();
        if (tools == null) tools = new ArrayList<>();
        if (mobs == null) mobs = new ArrayList<>();
        if (foods == null) foods = new ArrayList<>();
        if (nukes == null) nukes = new ArrayList<>();
        if (turrets == null) turrets = new ArrayList<>();
        if (grenades == null) grenades = new ArrayList<>();
    }

    /** 生成空模板（所有列表为空，玩家自行填写） */
    public static TweaksConfig createEmpty() {
        return new TweaksConfig();
    }

    // ==================== 内部模型 ====================

    /** 武器/弹种调整项 */
    public static class WeaponTweak {        /** 弹种名（对应 sedna XFactory 字段，如 "g12"、"m357_fmj"，见 ids 文件） */
        public String bulletName;
        /** 或武器物品注册名（如 "hbm:gun_supershotgun"） */
        public String itemName;
        /** 伤害倍率（1.5 = 1.5 倍） */
        public Double damageMult;
        /** 直接设定弹种伤害系数（绝对值覆盖，如 0.5 = 弹种伤害系数固定 0.5） */
        public Double damageSet;
        /** 穿甲百分比（绝对值 0~1，如 0.5 = 无视 50% 护甲） */
        public Double armorPiercing;
        /** 护甲阈值穿透（绝对值，穿透厚重护甲所需的伤害阈值） */
        public Double thresholdNegation;
        /** 击退倍率（绝对值，如 0.1 = 几乎不击退，1.0 = 强烈击退） */
        public Double knockback;
        /** 爆头倍率（绝对值，如 2.0 = 爆头 2 倍伤害） */
        public Double headshotMult;
        /** 散布倍率（<1 更准，如 0.5 = 散布减半） */
        public Double spreadMult;
        /** 弹速倍率（如 2.0 = 弹速翻倍，弹道更平直） */
        public Double velocityMult;
        /** 是否穿透（true = 子弹可穿透目标继续飞行） */
        public Boolean penetrate;
        /** 射速倍率（<1 更快，如 0.8；仅旧系统 ItemGunBase 生效） */
        public Double rateOfFireMult;
        /** 弹匣容量倍率（如 2.0；仅旧系统 ItemGunBase 生效） */
        public Double ammoCapMult;
    }

    /** 武器本体（枪械）调整项：按物品注册名覆写整把枪的基础伤害 */
    public static class GunTweak {
        /** 武器物品注册名（如 "hbm:gun_lag" / "hbm:gun_missile_launcher"，见 ids 文件 guns 部分） */
        public String item;
        /** 该枪所有弹种的伤害倍率（如 0.5 = 该枪伤害减半；与弹种 damageMult 相乘） */
        public Double damageMult;
    }

    /** 近战武器/工具调整项：按物品注册名覆写攻击伤害（显式最终值，模组不做隐式乘法） */
    public static class MeleeTweak {
        /** 物品注册名（如 "hbm:dnt_sword" / "hbm:chainsaw"，见 ids 文件 tools 部分） */
        public String item;
        /** 直接设定每次攻击的目标伤害（绝对值，如 2.1 = 每次攻击打 2.1 血；与 damageMult 二选一，damageSet 优先） */
        public Double damageSet;
        /** 攻击伤害倍率（如 0.3 = 攻击伤害减到 30%；与 damageSet 二选一） */
        public Double damageMult;
    }

    /** 手榴弹调整项：按装药枚举名或物品注册名覆写爆炸伤害（显式最终倍率） */
    public static class GrenadeTweak {
        /** 装药枚举名（POWDER/HE/DEMO/INC/WP/CLUSTER/EMP/PLASMA/LASER/CLUSTER_HEAVY/NUCLEAR/NUCLEAR_DEMO/SCHRAB） */
        public String filling;
        /** 或投掷物物品注册名（如 "hbm:stick_dynamite"） */
        public String item;
        /** 爆炸伤害倍率（显式最终倍率，如 0.25 = 该装药爆炸伤害减到 25%；未配置的装药保持原版） */
        public Double damageMult;
    }

    /** 怪物调整项 */
    public static class MobTweak {
        /** 实体类名（见 ids 文件 mobs 部分） */
        public String entityClass;
        /** 血量倍率（与 healthSet 二选一；两者都填时 healthSet 优先） */
        public Double healthMult;
        /** 直接设定最大血量（绝对值覆盖，如 100 = 该怪血量上限固定 100，无需反算倍率） */
        public Double healthSet;
        /** 攻击伤害倍率 */
        public Double damageMult;
        /** 移动速度倍率 */
        public Double speedMult;
        /** 击退抗性（绝对值 0~1） */
        public Double knockbackResist;
        /** 单次受到的伤害上限（绝对值，如 25 = 每次最多扣 25 血，防止被秒杀） */
        public Double damageCap;
        /** 弹道伤害倍率（0.25 = 枪械只造成 25% 伤害） */
        public Double projectileDamageMult;
        /** 爆炸伤害倍率（0.5 = 爆炸只造成 50% 伤害） */
        public Double explosionDamageMult;
        /** 该怪造成的一切伤害倍率（0.1 = 该怪打出的近战/弹道/爆炸全部减到 10%，覆盖远程怪如火箭、爆炸怪） */
        public Double outgoingDamageMult;
        /** 火焰免疫（true = 火焰伤害无效） */
        public Boolean fireImmune;
        /** 魔法免疫（true = 魔法伤害无效） */
        public Boolean magicImmune;
    }

    /** 食物调整项 */
    public static class FoodTweak {
        /** 物品注册名（如 "hbm:cheese" 或 "hbm:canned_conserve:8"，见 ids 文件） */
        public String item;
        /** 新饥饿值（半鸡腿数，如 5 = 2.5 鸡腿） */
        public Integer hunger;
        /** 新饱和度系数 */
        public Double saturation;
    }

    /** 核弹调整项（对应 BombConfig 静态字段） */
    public static class NukeTweak {
        /** BombConfig 字段名（如 "tsarRadius"/"fatmanRadius"/"falloutRange"，见 ids 文件） */
        public String field;
        /** 新的整数值 */
        public Integer value;
    }

    /** 炮台调整项 */
    public static class TurretTweak {
        /** 炮台实体类名（见 ids 文件 turrets 部分） */
        public String entityClass;
        /** 检测范围（方块） */
        public Double range;
        /** 每 tick 耗电（HE/t） */
        public Long consumption;
        /** 转速（度/tick） */
        public Double yawSpeed;
    }
}
