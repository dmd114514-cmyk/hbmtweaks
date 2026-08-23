package com.hbmtweaks;

/**
 * 炮击炮弹爆炸标志位（普通类，供 mixin 跨类调用）。
 *
 * 由 ItemAmmoArtyMixin 在 ItemAmmoArty.standardExplosion（炮弹落地爆炸入口）
 * 执行期间置起/清除；EntityProcessorStandardMixin 结算实体伤害时据此把
 * 炮弹爆炸伤害 ×0.1。同步单线程执行，HEAD/RETURN 成对包裹，嵌套爆炸也安全。
 */
public class ArtilleryFlag {

    private static boolean active = false;

    public static void setActive(boolean active) {
        ArtilleryFlag.active = active;
    }

    public static boolean isActive() {
        return active;
    }
}
