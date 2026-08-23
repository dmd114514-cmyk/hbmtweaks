package com.hbmtweaks;

import com.hbm.config.BombConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * 可调整 ID 列表提供者。
 *
 * 为 IdListWriter（hbm_tweaks_ids.txt）和配置文件模板提供全部可调整的 ID：
 *  - 弹种名（sedna XFactory 扫描）
 *  - 核弹字段（BombConfig）
 *  - 炮台类名
 *  - HBM 生物类名
 *  - 食物注册名
 */
public class ConfigTemplateGenerator {

    /** 枚举 BombConfig 中所有 public static int 字段名（按字母序） */
    public static List<String> getBombConfigIntFields() {
        TreeSet<String> names = new TreeSet<>();
        try {
            for (Field f : BombConfig.class.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())
                        && f.getType() == int.class) {
                    names.add(f.getName());
                }
            }
        } catch (Exception e) {
            HbmTweaks.logger.error("HBM Tweaks: BombConfig field scan failed", e);
        }
        return new ArrayList<>(names);
    }

    /** 全部炮台类名 */
    public static List<String> getTurretClasses() {
        List<String> list = new ArrayList<>();
        String p = "com.hbm.tileentity.turret.";
        list.add(p + "TileEntityTurretSentry");
        list.add(p + "TileEntityTurretSentryDamaged");
        list.add(p + "TileEntityTurretChekhov");
        list.add(p + "TileEntityTurretFriendly");
        list.add(p + "TileEntityTurretFritz");
        list.add(p + "TileEntityTurretHIMARS");
        list.add(p + "TileEntityTurretHoward");
        list.add(p + "TileEntityTurretHowardDamaged");
        list.add(p + "TileEntityTurretJeremy");
        list.add(p + "TileEntityTurretMaxwell");
        list.add(p + "TileEntityTurretRichard");
        list.add(p + "TileEntityTurretTauon");
        list.add(p + "TileEntityTurretArty");
        return list;
    }

    /** HBM 主要威胁生物类名 */
    public static List<String> getMobClasses() {
        List<String> list = new ArrayList<>();
        String p = "com.hbm.entity.mob.";
        list.add(p + "EntityMaskMan");        // 面具人（boss）
        list.add(p + "EntityGlowingOne");     // 发光者（高辐射僵尸）
        list.add(p + "EntityFBI");            // FBI 探员
        list.add(p + "EntityFBIDrone");       // FBI 无人机
        list.add(p + "EntityRADBeast");       // 辐射兽
        list.add(p + "EntityUFO");            // UFO
        list.add(p + "EntityHunterChopper");  // 猎杀直升机
        list.add(p + "EntityCreeperNuclear"); // 核苦力怕
        list.add(p + "EntityCreeperTainted"); // 污染苦力怕
        list.add(p + "EntityCreeperVolatile");// 不稳定苦力怕
        list.add(p + "EntityCreeperGold");    // 金苦力怕
        list.add(p + "EntityCyberCrab");      // 机械蟹
        list.add(p + "EntityTeslaCrab");      // 特斯拉蟹
        list.add(p + "EntityTaintCrab");      // 腐化蟹
        list.add(p + "EntityUndeadSoldier");  // 亡灵士兵
        list.add(p + "EntityQuackos");        // Quackos
        list.add(p + "EntityDuck");           // 鸭子
        list.add(p + "EntityPigeon");         // 鸽子
        list.add(p + "EntityParasiteMaggot"); // 寄生虫蛆
        list.add(p + "EntityDummy");          // 靶子
        return list;
    }

    /** sedna 枪械物品注册名（供 guns 段 per-gun 调整使用） */
    public static List<String> getGunItems() {
        List<String> list = new ArrayList<>();
        try {
            for (Object item : com.hbm.items.ModItems.ALL_ITEMS) {
                if (item == null) continue;
                Class<?> cls = item.getClass();
                // sedna 现代枪械：ItemGunBaseNT（或 ItemGunBaseSedna 子类）
                boolean isSednaGun = com.hbm.items.weapon.sedna.ItemGunBaseNT.class.isAssignableFrom(cls)
                        || com.hbm.items.weapon.sedna.ItemGunBaseSedna.class.isAssignableFrom(cls);
                // 旧系统枪械（B92/B93/超级霰弹枪/涡流炮）：也允许 per-gun 覆写
                boolean isLegacyGun = com.hbm.items.weapon.ItemGunBase.class.isAssignableFrom(cls);
                if (isSednaGun || isLegacyGun) {
                    net.minecraft.util.ResourceLocation rl =
                            ((net.minecraft.item.Item) item).getRegistryName();
                    if (rl != null) {
                        String name = rl.toString();
                        if (!list.contains(name)) list.add(name);
                    }
                }
            }
        } catch (Exception e) {
            HbmTweaks.logger.error("HBM Tweaks: gun item scan failed", e);
        }
        java.util.Collections.sort(list);
        return list;
    }

    /** 手榴弹装药枚举名（供 grenades 段 per-filling 调整使用） */
    public static List<String> getGrenadeFillings() {
        List<String> list = new ArrayList<>();
        try {
            Class<?> enumClass = Class.forName("com.hbm.items.weapon.grenade.ItemGrenadeFilling$EnumGrenadeFilling");
            Object[] constants = enumClass.getEnumConstants();
            if (constants != null) {
                for (Object c : constants) {
                    list.add(((Enum<?>) c).name());
                }
            }
        } catch (Exception e) {
            HbmTweaks.logger.error("HBM Tweaks: grenade filling scan failed", e);
        }
        return list;
    }

    /** HBM 近战武器/工具物品注册名（供 tools 段 per-item 调整使用） */
    public static List<String> getMeleeItems() {
        List<String> list = new ArrayList<>();
        try {
            for (Object item : com.hbm.items.ModItems.ALL_ITEMS) {
                if (item == null) continue;
                Class<?> cls = item.getClass();
                // 剑系（ItemSwordAbility 含陨石剑/切割者/电池剑子类）
                boolean isSword = com.hbm.items.tool.ItemSwordAbility.class.isAssignableFrom(cls);
                // 工具系（ItemToolAbility 含电锯/能源工具子类）
                boolean isTool = com.hbm.items.tool.ItemToolAbility.class.isAssignableFrom(cls);
                // 扳手武器
                boolean isWrench = com.hbm.items.tool.ItemToolingWeapon.class.isAssignableFrom(cls);
                if (isSword || isTool || isWrench) {
                    net.minecraft.util.ResourceLocation rl =
                            ((net.minecraft.item.Item) item).getRegistryName();
                    if (rl != null) {
                        String name = rl.toString();
                        if (!list.contains(name)) list.add(name);
                    }
                }
            }
        } catch (Exception e) {
            HbmTweaks.logger.error("HBM Tweaks: melee item scan failed", e);
        }
        java.util.Collections.sort(list);
        return list;
    }

    /** HBM 主要食物注册名（含罐头 meta 示例） */
    public static List<String> getFoodItems() {
        List<String> list = new ArrayList<>();
        String[] foods = {
                "hbm:cheese", "hbm:cheese_quesadilla", "hbm:twinkie", "hbm:loops",
                "hbm:loop_stew", "hbm:pudding", "hbm:static_sandwich", "hbm:lemon",
                "hbm:marshmallow", "hbm:marshmallow_roasted", "hbm:spongebob_macaroni",
                "hbm:definitelyfood", "hbm:glyphid_meat", "hbm:glyphid_meat_grilled",
                "hbm:ingot_smore", "hbm:pancake", "hbm:mucho_mango", "hbm:fooditem",
                "hbm:chocolate", "hbm:cotton_candy", "hbm:schnitzel_vegan",
                "hbm:apple_lead", "hbm:apple_schrabidium", "hbm:apple_euphemium",
                "hbm:canned_conserve:0", "hbm:canned_conserve:1", "hbm:canned_conserve:4",
                "hbm:canned_conserve:5", "hbm:canned_conserve:6", "hbm:canned_conserve:8",
                "hbm:canned_conserve:10", "hbm:canned_conserve:12", "hbm:canned_conserve:13",
                "hbm:canned_conserve:15", "hbm:canned_conserve:16", "hbm:canned_conserve:20",
                "hbm:canned_conserve:21", "hbm:glowing_stew", "hbm:balefire_scrambled",
                "hbm:balefire_and_ham",
                "minecraft:bread", "minecraft:apple", "minecraft:golden_apple",
                "minecraft:golden_carrot", "minecraft:cooked_beef", "minecraft:cooked_porkchop",
                "minecraft:cooked_chicken", "minecraft:carrot", "minecraft:baked_potato",
                "minecraft:cake"
        };
        for (String f : foods) list.add(f);
        return list;
    }
}
