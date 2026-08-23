package com.hbmtweaks;

import com.hbm.config.BombConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 核弹数值修改器。
 *
 * HBM 的核弹半径/算法等全部集中在 BombConfig 的 public static int 字段：
 *   gadgetRadius / boyRadius / manRadius / mikeRadius / tsarRadius /
 *   prototypeRadius / fleijaRadius / soliniumRadius / n2Radius /
 *   missileRadius / mirvRadius / fatmanRadius / nukaRadius / aSchrabRadius /
 *   falloutRange / blastSpeed / mk5 / limitExplosionLifespan 等
 *
 * 爆炸实体每次爆炸时都会重新读取这些静态字段，因此修改立即生效。
 */
public class NukeTweaker {

    public static void apply(TweaksConfig config) {
        try {
            for (TweaksConfig.NukeTweak nt : config.nukes) {
                if (nt.field == null || nt.value == null) continue;
                applyOne(nt);
            }
        } catch (Exception e) {
            HbmTweaks.logger.error("HBM Tweaks: nuke tweak failed", e);
        }
    }

    private static void applyOne(TweaksConfig.NukeTweak nt) throws Exception {
        Field f = BombConfig.class.getDeclaredField(nt.field);
        if (f.getType() != int.class) {
            HbmTweaks.logger.warn("HBM Tweaks: nuke field '{}' is not int", nt.field);
            return;
        }
        f.setAccessible(true);
        int old = f.getInt(null);
        f.setInt(null, nt.value);
        HbmTweaks.logger.info("HBM Tweaks: nuke '{}' {} -> {}", nt.field, old, nt.value);
    }
}
