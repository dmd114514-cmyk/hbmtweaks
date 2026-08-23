package com.hbmtweaks;

import java.util.HashMap;
import java.util.Map;

/**
 * 手榴弹 per-装药 / per-物品 伤害倍率缓存。
 *
 * 在 postInit 时由 WeaponTweaker 从 config.grenades 填充；
 * mixin / MobTweakHandler 在爆炸结算时查询。
 * 未配置返回 -1（此时由全局 grenadeDamageMult / dynamiteDamageMult 兜底）。
 */
public class GrenadeTweakCache {

    /** 键前缀区分两种匹配方式 */
    private static final String PREFIX_FILLING = "filling:";
    private static final String PREFIX_ITEM = "item:";

    private static final Map<String, Double> multByKey = new HashMap<>();

    public static void putFilling(String fillingName, double mult) {
        if (fillingName != null && !fillingName.isEmpty()) {
            multByKey.put(PREFIX_FILLING + fillingName.toUpperCase(), mult);
        }
    }

    public static void putItem(String registryName, double mult) {
        if (registryName != null && !registryName.isEmpty()) {
            multByKey.put(PREFIX_ITEM + registryName, mult);
        }
    }

    /** 查询某装药的 per-filling 倍率（未配置返回 -1） */
    public static double getFillingMult(String fillingName) {
        if (fillingName == null || fillingName.isEmpty() || multByKey.isEmpty()) return -1.0D;
        Double mult = multByKey.get(PREFIX_FILLING + fillingName.toUpperCase());
        return mult == null ? -1.0D : mult;
    }

    /** 查询某投掷物物品的 per-item 倍率（未配置返回 -1） */
    public static double getItemMult(String registryName) {
        if (registryName == null || registryName.isEmpty() || multByKey.isEmpty()) return -1.0D;
        Double mult = multByKey.get(PREFIX_ITEM + registryName);
        return mult == null ? -1.0D : mult;
    }

    public static void clear() {
        multByKey.clear();
    }

    public static int size() {
        return multByKey.size();
    }
}
