package com.hbmtweaks.mixin;

import com.hbmtweaks.GrenadeFlag;
import com.hbm.entity.grenade.EntityGrenadeUniversal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 手榴弹（grenade_universal）爆炸标志位 Mixin（配合 EntityProcessorCrossSmoothMixin）。
 *
 * 在 EntityGrenadeUniversal.explode()（装药 filling.explode + 附加 extra.onExplode 的入口）
 * 执行期间置起/清除 GrenadeFlag，并记录当前装药枚举名（供 per-装药倍率查询）。
 * 装药爆炸（standardExplode/tinyExplode/explodeStandardEnergy/DEMO/NUKE 等）均在此方法内同步触发。
 * 注意：Mixin 0.7 不允许 mixin 类添加 public static 方法，标志位放在普通类 GrenadeFlag。
 */
@Mixin(EntityGrenadeUniversal.class)
public abstract class EntityGrenadeUniversalMixin {

    @Inject(method = "explode", at = @At("HEAD"), remap = false)
    private void hbmtweaks$flagOn(CallbackInfo ci) {
        GrenadeFlag.setActive(true);
        try {
            com.hbm.items.weapon.grenade.ItemGrenadeFilling.EnumGrenadeFilling filling =
                    ((EntityGrenadeUniversal) (Object) this).getFilling();
            if (filling != null) {
                GrenadeFlag.setFilling(filling.name());
            }
        } catch (Exception e) {
            GrenadeFlag.setFilling(null);
        }
    }

    @Inject(method = "explode", at = @At("RETURN"), remap = false)
    private void hbmtweaks$flagOff(CallbackInfo ci) {
        GrenadeFlag.setActive(false);
    }
}
