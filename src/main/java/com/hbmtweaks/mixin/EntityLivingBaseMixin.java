package com.hbmtweaks.mixin;

import com.hbmtweaks.MobTweakHandler;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 怪物属性规则覆盖存档加载路径的 Mixin。
 *
 * 问题：1.12.2 中从存档（chunk）恢复的实体走 Chunk.readEntitiesFromNBT，不经过
 * World.spawnEntity / World.loadEntities，因此 EntityJoinWorldEvent 不触发，
 * hbmtweaks 的 mobs 属性规则（血量/攻击/速度）对重进存档的怪物不生效（恢复原版血量）。
 *
 * 解决：mixin EntityLivingBase.readEntityFromNBT（srg: func_70037_a，1.12.2 生产环境名），
 * 在方法返回前应用属性规则。此时实体已完全构造（含 HBM 子类构造体的属性设置）且
 * NBT 恢复完成——时机正确，覆盖"从存档加载"路径；新生成/刷怪蛋路径仍由
 * EntityJoinWorldEvent 兜底（两者互补且幂等）。
 */
@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin {

    @Inject(method = "func_70037_a", at = @At("RETURN"), remap = false)
    private void hbmtweaks$attributesOnLoad(CallbackInfo ci) {
        MobTweakHandler.applyAttributesOnConstruct((EntityLivingBase) (Object) this);
    }
}
