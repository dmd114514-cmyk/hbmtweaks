package com.hbmtweaks.mixin;

import com.hbmtweaks.ArtilleryFlag;
import com.hbm.entity.projectile.EntityArtilleryShell;
import com.hbm.items.weapon.ItemAmmoArty;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 炮击炮弹爆炸标志位 Mixin（配合 EntityProcessorStandardMixin 使用）。
 *
 * 在 ItemAmmoArty.standardExplosion（炮弹落地爆炸的公共入口）执行期间
 * 置起/清除 ArtilleryFlag，EntityProcessorStandard 结算实体伤害时据此应用
 * 配置的 artilleryDamageMult 炮弹爆炸伤害倍率（模组不预设任何数值）。
 * 注意：mixin 类中不允许添加 public static 方法，标志位放在普通类 ArtilleryFlag 中。
 */
@Mixin(ItemAmmoArty.class)
public abstract class ItemAmmoArtyMixin {

    @Inject(method = "standardExplosion", at = @At("HEAD"), remap = false)
    private static void hbmtweaks$flagOn(EntityArtilleryShell shell, RayTraceResult mop,
                                         float size, float rangeMod, boolean breaksBlocks, CallbackInfo ci) {
        ArtilleryFlag.setActive(true);
    }

    @Inject(method = "standardExplosion", at = @At("RETURN"), remap = false)
    private static void hbmtweaks$flagOff(EntityArtilleryShell shell, RayTraceResult mop,
                                          float size, float rangeMod, boolean breaksBlocks, CallbackInfo ci) {
        ArtilleryFlag.setActive(false);
    }
}
