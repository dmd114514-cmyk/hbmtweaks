package com.hbmtweaks;

/**
 * 炮台属性配置加载器。
 *
 * 将配置中的炮台覆盖值填入 TurretTweakCache，
 * 由 TurretMixin 在运行时读取（零解析开销）。
 */
public class TurretTweaker {

    public static void apply(TweaksConfig config) {
        if (config.turrets == null) return;
        for (TweaksConfig.TurretTweak tt : config.turrets) {
            if (tt.entityClass == null || tt.entityClass.isEmpty()) continue;
            if (tt.range != null) TurretTweakCache.putRange(tt.entityClass, tt.range);
            if (tt.consumption != null) TurretTweakCache.putConsumption(tt.entityClass, tt.consumption);
            if (tt.yawSpeed != null) TurretTweakCache.putYawSpeed(tt.entityClass, tt.yawSpeed);
        }
        HbmTweaks.logger.info("HBM Tweaks: {} turret overrides cached", TurretTweakCache.size());
    }
}
