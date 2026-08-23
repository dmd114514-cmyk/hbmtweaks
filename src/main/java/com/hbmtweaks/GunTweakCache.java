package com.hbmtweaks;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * 武器本体（per-gun）伤害倍率缓存。
 *
 * 在 postInit 时由 WeaponTweaker 从 config.guns 填充；
 * ReceiverMixin 在每次取枪械基础伤害（Receiver.getBaseDamage）时只读查询。
 * 匹配键为物品注册名（如 "hbm:gun_lag"），未配置的枪返回 1.0（不干预）。
 */
public class GunTweakCache {

    private static final Map<String, Double> damageMultByGun = new HashMap<>();

    public static void put(String registryName, double mult) {
        if (registryName != null && !registryName.isEmpty()) {
            damageMultByGun.put(registryName, mult);
        }
    }

    /** 查询某把枪的伤害倍率（未配置返回 1.0） */
    public static double getPerGunMult(ItemStack stack) {
        if (stack == null || stack.isEmpty() || damageMultByGun.isEmpty()) return 1.0D;
        ResourceLocation rl = stack.getItem().getRegistryName();
        if (rl == null) return 1.0D;
        Double mult = damageMultByGun.get(rl.toString());
        return mult == null ? 1.0D : mult;
    }

    public static void clear() {
        damageMultByGun.clear();
    }

    public static int size() {
        return damageMultByGun.size();
    }
}
