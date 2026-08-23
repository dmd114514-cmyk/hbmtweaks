package com.hbmtweaks.mixin;

import com.hbmtweaks.TurretTweakCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 炮台属性修改 Mixin。
 *
 * 目标：HBM 的 14 个炮台 TileEntity 类。
 * 方法：getDecetorRange() / getConsumption() / getTurretYawSpeed()
 *
 * 设计原则（避免多 mod mixin 冲突与性能问题）：
 *  - 全部使用 @Inject(HEAD, cancellable) 而非 @Overwrite：不破坏其他 mixin 的注入
 *  - 配置查询结果在 TurretTweakCache 中按类名缓存为 primitive，每次调用仅一次 Map 查找 + null 判断
 *  - 无配置时直接放行（返回原值），开销为一次 HashMap 查找（纳秒级）
 *  - 不修改任何字段，不改变方法签名
 */
@Mixin({
        com.hbm.tileentity.turret.TileEntityTurretSentry.class,
        com.hbm.tileentity.turret.TileEntityTurretSentryDamaged.class,
        com.hbm.tileentity.turret.TileEntityTurretChekhov.class,
        com.hbm.tileentity.turret.TileEntityTurretFriendly.class,
        com.hbm.tileentity.turret.TileEntityTurretFritz.class,
        com.hbm.tileentity.turret.TileEntityTurretHIMARS.class,
        com.hbm.tileentity.turret.TileEntityTurretHoward.class,
        com.hbm.tileentity.turret.TileEntityTurretHowardDamaged.class,
        com.hbm.tileentity.turret.TileEntityTurretJeremy.class,
        com.hbm.tileentity.turret.TileEntityTurretMaxwell.class,
        com.hbm.tileentity.turret.TileEntityTurretRichard.class,
        com.hbm.tileentity.turret.TileEntityTurretTauon.class,
        com.hbm.tileentity.turret.TileEntityTurretArty.class
})
public abstract class TurretMixin {

    @Inject(method = "getDecetorRange", at = @At("HEAD"), cancellable = true, remap = false)
    private void hbmtweaks$range(CallbackInfoReturnable<Double> cir) {
        Double override = TurretTweakCache.getRange(getClass());
        if (override != null) cir.setReturnValue(override);
    }

    @Inject(method = "getConsumption", at = @At("HEAD"), cancellable = true, remap = false)
    private void hbmtweaks$consumption(CallbackInfoReturnable<Long> cir) {
        Long override = TurretTweakCache.getConsumption(getClass());
        if (override != null) cir.setReturnValue(override);
    }

    @Inject(method = "getTurretYawSpeed", at = @At("HEAD"), cancellable = true, remap = false)
    private void hbmtweaks$yaw(CallbackInfoReturnable<Double> cir) {
        Double override = TurretTweakCache.getYawSpeed(getClass());
        if (override != null) cir.setReturnValue(override);
    }
}
