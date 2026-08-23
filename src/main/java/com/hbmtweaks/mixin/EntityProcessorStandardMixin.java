package com.hbmtweaks.mixin;

import com.hbmtweaks.ArtilleryFlag;
import com.hbmtweaks.HbmTweaks;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * 炮击炮弹爆炸伤害 Mixin（Arty 炮兵阵地 / HIMARS）。
 *
 * 目标：EntityProcessorStandard.process() 中给实体结算爆炸伤害的
 * `entity.attackEntityFrom(...)` 调用（生产环境 Minecraft 方法为 srg 名 func_70097_a）。
 * 该伤害公式为 (击退²+击退)/2 × 8 × size + 1，size 已 ×2 —— 炮弹(size 10-15)中心伤害可达
 * 160-240，对 20 血玩家是秒杀级。而炮弹爆炸的 ExplosionVNT 没有 exploder（无实体来源），
 * 无法在 LivingHurtEvent 中通过来源实体区分，因此用 ArtilleryFlag 标志位标记：
 * 仅在"炮弹爆炸结算中"（ItemAmmoArty.standardExplosion 执行期间）按 artilleryDamageMult 压缩。
 *
 * 倍率来自配置文件（enableArtilleryOverride + artilleryDamageMult），模组本身不硬编码任何数值。
 * 注意：C4/油井/锅炉/苦力怕等其它使用 EntityProcessorStandard 的爆炸不受影响
 * （标志位只在炮弹爆炸期间为 true）。
 */
@Mixin(EntityProcessorStandard.class)
public abstract class EntityProcessorStandardMixin {

    @ModifyArg(method = "process",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;func_70097_a(Lnet/minecraft/util/DamageSource;F)Z",
                    remap = false),
            index = 1)
    private float hbmtweaks$artilleryDamage(float amount) {
        if (ArtilleryFlag.isActive() && HbmTweaks.config != null
                && HbmTweaks.config.enableArtilleryOverride
                && HbmTweaks.config.artilleryDamageMult != 1.0D) {
            return amount * (float) HbmTweaks.config.artilleryDamageMult;
        }
        return amount;
    }
}
