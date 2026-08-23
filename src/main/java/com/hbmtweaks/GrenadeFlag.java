package com.hbmtweaks;

/**
 * 手榴弹（万能手榴弹 grenade_universal）爆炸标志位（普通类，供 mixin 跨类调用）。
 *
 * 由 EntityGrenadeUniversalMixin 在 EntityGrenadeUniversal.explode()（装药/附加爆炸入口）
 * 执行期间置起/清除，并记录当前爆炸的装药枚举名（currentFilling）。
 * EntityProcessorCrossSmooth 结算实体伤害时据此应用手榴弹伤害倍率：
 * 先查 GrenadeTweakCache 的 per-装药倍率，未配置则用全局 grenadeDamageMult。
 */
public class GrenadeFlag {

    private static boolean active = false;
    private static String currentFilling = null;

    public static void setActive(boolean active) {
        GrenadeFlag.active = active;
        if (!active) {
            GrenadeFlag.currentFilling = null;
        }
    }

    public static void setFilling(String fillingName) {
        GrenadeFlag.currentFilling = fillingName;
    }

    public static boolean isActive() {
        return active;
    }

    public static String getCurrentFilling() {
        return currentFilling;
    }
}
