package com.hbmtweaks.mixin;

import com.hbmtweaks.GunTweakCache;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 武器本体（per-gun）伤害倍率 Mixin。
 *
 * 目标：HBM sedna 枪械系统的 Receiver.getBaseDamage(ItemStack) ——
 * 每把枪的基础伤害（.dmg(x) 值）都在这里取出，且参数带当前枪的 ItemStack，
 * 因此可以在返回前乘上 config.guns 中按物品注册名配置的倍率。
 *
 * 效果：例如 { "item": "hbm:gun_lag", "damageMult": 0.73 } 只影响该枪，
 * 与弹种 damageMult 相乘（最终伤害 = 枪基础伤害 × 枪倍率 × 弹种倍率）。
 * 注意：@Inject handler 的参数顺序 = 目标方法参数在前，CallbackInfo 在最后（Mixin 0.7 规则）。
 */
@Mixin(com.hbm.items.weapon.sedna.Receiver.class)
public abstract class ReceiverMixin {

    @Inject(method = "getBaseDamage", at = @At("RETURN"), cancellable = true, remap = false)
    private void hbmtweaks$perGunDamage(ItemStack stack, CallbackInfoReturnable<Float> cir) {
        double mult = GunTweakCache.getPerGunMult(stack);
        if (mult != 1.0D) {
            cir.setReturnValue((float) (cir.getReturnValue() * mult));
        }
    }
}
