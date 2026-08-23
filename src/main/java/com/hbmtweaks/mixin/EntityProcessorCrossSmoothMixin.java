package com.hbmtweaks.mixin;

import com.hbmtweaks.GrenadeFlag;
import com.hbmtweaks.GrenadeTweakCache;
import com.hbmtweaks.HbmTweaks;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 手榴弹爆炸伤害 Mixin。
 *
 * 目标：EntityProcessorCrossSmooth.calculateDamage(distanceScaled, density, knockback, size)
 * —— 手榴弹装药爆炸（standardExplode 等）用 CrossSmooth(fixedDamage) 结算实体伤害，
 * 伤害 = fixedDamage × (1 - 距离衰减)，fixedDamage 是装药硬编码值（火药 10 / 高爆 25 /
 * EMP 30 / 等离子 50 / 核 100），不走弹种 damageMult，因此在这里按手榴弹倍率压缩。
 *
 * 倍率优先级：per-装药（grenades 列表按 filling 名）> 全局 grenadeDamageMult。
 * 倍率来自配置文件（enableGrenadeOverride 开启才生效），模组本身不硬编码任何数值。
 * 注意：40mm 榴弹/火箭也走 CrossSmooth（fixedDamage = 弹丸伤害，已随弹种倍率压缩），
 * 但它们不在 GrenadeFlag 期间结算，不受此注入影响（不会双压）。
 */
@Mixin(EntityProcessorCrossSmooth.class)
public abstract class EntityProcessorCrossSmoothMixin {

    @Inject(method = "calculateDamage", at = @At("RETURN"), cancellable = true, remap = false)
    private void hbmtweaks$grenadeDamage(CallbackInfoReturnable<Float> cir) {
        if (!GrenadeFlag.isActive() || HbmTweaks.config == null
                || !HbmTweaks.config.enableGrenadeOverride) {
            return;
        }
        // 纯显式：grenades 列表配置了该装药的倍率才生效（未配置 = 原版，无全局兜底）
        double mult = GrenadeTweakCache.getFillingMult(GrenadeFlag.getCurrentFilling());
        if (mult > 0 && mult != 1.0D) {
            cir.setReturnValue(cir.getReturnValue() * (float) mult);
        }
    }
}
