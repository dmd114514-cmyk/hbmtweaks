package com.hbmtweaks;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 近战武器/工具数值修改器（tooltip 与实际伤害一致）。
 *
 * 原理：HBM 的剑/镐/斧/铲/电锯/扳手等武器类（ItemSwordAbility / ItemToolAbility /
 * ItemToolingWeapon 等）都覆写了 getItemAttributeModifiers，用各自类里的
 * protected float damage 字段生成攻击伤害属性（tooltip 显示伤害 = damage + 1 空手基础）。
 * 反射改写该字段后，属性面板/tooltip 与实际攻击伤害完全一致。
 *
 * 注意：HBM 的 mod jar 是反混淆 MCP 名格式，运行时字段名就是 "damage"，
 * 因此按"float 类型 + 字段名 damage + 实例字段"在类继承链中定位。
 *
 * 配置：
 *  - damageSet（绝对目标伤害，如 4 = 每次攻击 4 血）→ damage = damageSet - 1
 *  - damageMult（倍率，按"显示伤害"缩放）→ damage = (原显示 × mult) - 1
 */
public class MeleeTweaker {

    public static void apply(TweaksConfig config) {
        if (config.tools == null) return;
        for (TweaksConfig.MeleeTweak mt : config.tools) {
            if (mt.item == null || mt.item.isEmpty()) continue;
            if (mt.damageSet == null && mt.damageMult == null) continue;
            try {
                applyOne(mt);
            } catch (Exception e) {
                HbmTweaks.logger.error("HBM Tweaks: melee tweak failed for '{}': {}", mt.item, e.getMessage());
            }
        }
    }

    private static void applyOne(TweaksConfig.MeleeTweak mt) throws Exception {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(mt.item));
        if (item == null) {
            HbmTweaks.logger.warn("HBM Tweaks: unknown melee item '{}'", mt.item);
            return;
        }
        Field f = findDamageField(item.getClass());
        if (f == null) {
            HbmTweaks.logger.warn("HBM Tweaks: '{}' has no attack damage field (not a melee item?)", mt.item);
            return;
        }
        f.setAccessible(true);
        double original = f.getType() == float.class ? f.getFloat(item) : f.getDouble(item);
        double originalDisplay = original + 1.0D;      // 原 tooltip 显示伤害

        double newVal;
        if (mt.damageSet != null) {
            // 绝对目标伤害：每次攻击 = damageSet
            newVal = mt.damageSet - 1.0D;
        } else {
            // 倍率：按显示伤害缩放
            newVal = originalDisplay * mt.damageMult - 1.0D;
        }
        if (f.getType() == float.class) {
            f.setFloat(item, (float) newVal);
        } else {
            f.setDouble(item, newVal);
        }

        HbmTweaks.logger.info("HBM Tweaks: melee '{}' damage {} -> {} (显示伤害 {} -> {})",
                mt.item, original, newVal,
                String.format("%.1f", originalDisplay), String.format("%.1f", newVal + 1.0D));
    }

    /**
     * 在类继承链中查找近战伤害字段：
     *  1. 优先：float 类型、名为 damage 的实例字段（HBM 的 ItemSwordAbility/ItemToolAbility 系覆写
     *     getItemAttributeModifiers，用各自的 damage 字段；HBM 字段名不混淆）
     *  2. 回退：Item 基类的攻击伤害字段（ItemSword 的其他子类如 WeaponSpecial/ModSword/锄等不覆写
     *     属性生成，走 Item.attackDamage）
     */
    private static Field findDamageField(Class<?> clazz) {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getType() == float.class && f.getName().equals("damage")
                        && !Modifier.isStatic(f.getModifiers())) {
                    return f;
                }
            }
            c = c.getSuperclass();
        }
        // 回退：Item 基类的攻击伤害字段（类型 double、实例字段、非 static）
        Class<?> itemClass = null;
        c = clazz;
        while (c != null && c != Object.class) {
            if (c.getName().equals("net.minecraft.item.Item")) {
                itemClass = c;
                break;
            }
            c = c.getSuperclass();
        }
        if (itemClass != null) {
            for (Field f : itemClass.getDeclaredFields()) {
                if (f.getType() == double.class && !Modifier.isStatic(f.getModifiers())) {
                    return f;
                }
            }
        }
        return null;
    }
}
