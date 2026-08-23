package com.hbmtweaks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * 怪物修改器（事件方式）。
 *
 * 属性修改：HBM 实体的属性在 applyEntityAttributes() 中设置（实体构造时执行），
 * EntityJoinWorldEvent 在其后触发，此时可以安全地覆盖属性。
 *
 * 受击修改：LivingHurtEvent（护甲结算后、扣血前，可改数值）用于弹道/爆炸倍率与伤害上限；
 * LivingAttackEvent（护甲结算前，可取消）用于火焰/魔法免疫。
 *
 * 性能优化：配置在首次加载时预处理为"类名 -> 规则"缓存，
 * 事件触发时只做一次 HashMap 查找（模板中大量全 null 条目不会造成遍历开销）。
 */
public class MobTweakHandler {

    /** 类名 -> 已启用的怪物规则（仅含非 null 字段的条目） */
    private static final Map<String, TweaksConfig.MobTweak> activeRules = new HashMap<>();

    /** 由 HbmTweaks 在配置加载后调用 */
    public static void buildCache(TweaksConfig config) {
        activeRules.clear();
        if (config == null || config.mobs == null) return;
        for (TweaksConfig.MobTweak mt : config.mobs) {
            if (mt == null || mt.entityClass == null || mt.entityClass.isEmpty()) continue;
            // 跳过全 null 的模板条目
            if (mt.healthMult == null && mt.healthSet == null && mt.damageMult == null
                    && mt.speedMult == null && mt.knockbackResist == null
                    && mt.damageCap == null && mt.projectileDamageMult == null
                    && mt.explosionDamageMult == null && mt.outgoingDamageMult == null
                    && mt.fireImmune == null && mt.magicImmune == null) continue;
            activeRules.put(mt.entityClass, mt);
        }
        HbmTweaks.logger.info("HBM Tweaks: {} active mob rules cached", activeRules.size());
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getEntity() == null || event.getWorld() == null || event.getWorld().isRemote) {
            return;
        }
        if (!(event.getEntity() instanceof EntityLivingBase)) {
            return;
        }
        // 新生成/刷怪蛋路径（spawnEntity → EntityJoinWorldEvent）应用属性规则
        applyAttributesOnConstruct((EntityLivingBase) event.getEntity());
    }

    /**
     * 应用怪物属性规则（血量/攻击/速度/击退抗性）。
     *
     * 两个调用点（互为补充，幂等）：
     *  1. EntityJoinWorldEvent（新生成/刷怪蛋路径）
     *  2. Mixin EntityLivingBase.readEntityFromNBT RETURN（存档加载/chunk 恢复路径——
     *     1.12.2 中 chunk 恢复实体不走 spawnEntity，EntityJoinWorldEvent 不会触发，
     *     必须在这里应用，否则重进存档后血量等属性恢复原版）
     * 注意：不能在 applyEntityAttributes / EntityConstructing 应用——HBM 子类构造体
     * （如 glyphid 的 variant 属性设置）在其后执行，会覆盖修改。
     */
    public static void applyAttributesOnConstruct(EntityLivingBase entity) {
        if (entity == null || entity.world == null || entity.world.isRemote) return;
        TweaksConfig config = HbmTweaks.config;
        if (config == null) return;

        String className = entity.getClass().getName();

        // 全局倍率（仅对 HBM 的怪物生效，避免影响原版；且仅在玩家无自定义 mob 条目时生效）
        if (className.startsWith("com.hbm.entity.") && config.isGlobalMobActive()) {
            if (config.globalMobHealthMult != 1.0) {
                IAttributeInstance health = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
                if (health != null) {
                    double base = health.getBaseValue();
                    health.setBaseValue(base * config.globalMobHealthMult);
                }
            }
            if (config.globalMobDamageMult != 1.0) {
                IAttributeInstance dmg = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
                if (dmg != null) {
                    double base = dmg.getBaseValue();
                    dmg.setBaseValue(base * config.globalMobDamageMult);
                }
            }
        }

        // 按类名单独调整（O(1) 查找）
        TweaksConfig.MobTweak mt = activeRules.get(className);
        if (mt != null) {
            applyMobTweak(entity, mt);
        }
    }

    /**
     * 受击伤害修改：护甲结算后、扣血前触发。
     * 顺序：先按来源类型乘倍率（弹道/爆炸），再套用单次伤害上限；
     * 另处理两类独立倍率：怪打出的伤害（outgoingDamageMult）、炮台直接电/微波伤害（turretDamageMult）。
     */
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity == null || entity.world == null || entity.world.isRemote) return;
        TweaksConfig config = HbmTweaks.config;
        DamageSource src = event.getSource();
        if (src == null) return;

        double amount = event.getAmount();

        // ============ 炸药棒/钓鱼炸药（MC 原版爆炸，exploder = 手榴弹实体） ============
        // stick_dynamite 等用 world.newExplosion(grenade, ...)，伤害来源实体是 com.hbm.entity.grenade.*
        // 倍率优先级：per-物品（grenades 列表按 item 名）> 全局 dynamiteDamageMult
        // 倍率来自配置 enableGrenadeOverride 开启才生效（模组不硬编码数值）
        if (config != null && config.enableGrenadeOverride && src.isExplosion()
                && src.getImmediateSource() != null
                && src.getImmediateSource().getClass().getName().startsWith("com.hbm.entity.grenade.")) {
            double dynMult = -1.0D;
            try {
                if (src.getImmediateSource() instanceof com.hbm.entity.grenade.EntityGrenadeBouncyGeneric) {
                    net.minecraft.item.Item gItem = ((com.hbm.entity.grenade.EntityGrenadeBouncyGeneric) src.getImmediateSource()).getGrenade();
                    if (gItem != null && gItem.getRegistryName() != null) {
                        dynMult = GrenadeTweakCache.getItemMult(gItem.getRegistryName().toString());
                    }
                } else if (src.getImmediateSource() instanceof com.hbm.entity.grenade.EntityGrenadeImpactGeneric) {
                    net.minecraft.item.Item gItem = ((com.hbm.entity.grenade.EntityGrenadeImpactGeneric) src.getImmediateSource()).getGrenade();
                    if (gItem != null && gItem.getRegistryName() != null) {
                        dynMult = GrenadeTweakCache.getItemMult(gItem.getRegistryName().toString());
                    }
                }
            } catch (Exception ignored) { }
            if (dynMult < 0) {
                dynMult = config.dynamiteDamageMult;
            }
            if (dynMult != 1.0D) {
                amount *= dynMult;
            }
        }

        // ============ 炮台直接伤害倍率：electricity(陶子炮/特斯拉类) 与 microwave(微波炮) ============
        // 这两个 ModDamageSource 无实体来源（immediateSource == null），其余 HBM 电击源也归属此类，
        // 按 enableTurretDamageOverride + turretDamageMult 压缩（玩家更安全，防御设施伤害同步纳入压缩体系）。
        if (config != null && config.enableTurretDamageOverride && config.turretDamageMult != 1.0D
                && src.getImmediateSource() == null) {
            String type = src.getDamageType();
            if ("electricity".equals(type) || "microwave".equals(type)) {
                amount *= config.turretDamageMult;
            }
        }

        // 注：近战武器/工具（tools）不再在此事件层处理——MeleeTweaker 在 postInit 直接改写
        // Item.attackDamage 字段，tooltip 与实际伤害完全一致，无需运行时拦截。

        // ============ 该怪造成的伤害倍率（outgoingDamageMult）：攻击者是配置了规则的怪物 ============
        // 覆盖近战/弹道/爆炸全部来源（trueSource 即攻击者本体）；受害者不必是 HBM 怪。
        if (src.getTrueSource() instanceof EntityLivingBase) {
            TweaksConfig.MobTweak attackerRule = activeRules.get(src.getTrueSource().getClass().getName());
            if (attackerRule != null && attackerRule.outgoingDamageMult != null) {
                amount *= attackerRule.outgoingDamageMult;
            }
        }

        // ============ 该怪受到的伤害（受击规则，作用于配置了规则的 HBM 怪本体） ============
        TweaksConfig.MobTweak mt = activeRules.get(entity.getClass().getName());
        if (mt != null) {
            if (mt.projectileDamageMult != null && src.isProjectile()) {
                amount *= mt.projectileDamageMult;
            }
            if (mt.explosionDamageMult != null && src.isExplosion()) {
                amount *= mt.explosionDamageMult;
            }
            if (mt.damageCap != null && amount > mt.damageCap) {
                amount = mt.damageCap;
            }
        }
        event.setAmount((float) amount);
    }

    /** 受击取消：护甲结算前触发，用于火焰/魔法免疫（完全抵消该次伤害） */
    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity == null || entity.world == null || entity.world.isRemote) return;
        TweaksConfig.MobTweak mt = activeRules.get(entity.getClass().getName());
        if (mt == null) return;

        DamageSource src = event.getSource();
        if (src == null) return;
        if (Boolean.TRUE.equals(mt.fireImmune) && src.isFireDamage()) {
            event.setCanceled(true);
            return;
        }
        if (Boolean.TRUE.equals(mt.magicImmune) && src.isMagicDamage()) {
            event.setCanceled(true);
        }
    }

    private static void applyMobTweak(EntityLivingBase entity, TweaksConfig.MobTweak mt) {
        IAttributeInstance health = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (mt.healthSet != null) {
            // 绝对值设定：直接覆盖最大血量（玩家无需反算倍率，如 healthSet:100 = 血量上限固定 100）
            if (health != null) {
                double oldMax = health.getBaseValue();
                health.setBaseValue(mt.healthSet);
                // 仅满血时同步到新满血；残血保持当前绝对值（比例自动随新上限变化）
                if (entity.getHealth() >= oldMax - 0.01) {
                    entity.setHealth((float) (entity.getMaxHealth()));
                }
            }
        } else if (mt.healthMult != null) {
            if (health != null) {
                double base = health.getBaseValue();
                health.setBaseValue(base * mt.healthMult);
                // 若实体血量已满，则按比例同步当前血量
                if (entity.getHealth() >= base - 0.01) {
                    entity.setHealth((float) (entity.getMaxHealth()));
                }
            }
        }
        if (mt.damageMult != null) {
            IAttributeInstance dmg = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
            if (dmg != null) {
                dmg.setBaseValue(dmg.getBaseValue() * mt.damageMult);
            }
        }
        if (mt.speedMult != null) {
            IAttributeInstance speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.setBaseValue(speed.getBaseValue() * mt.speedMult);
            }
        }
        if (mt.knockbackResist != null) {
            IAttributeInstance kb = entity.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
            if (kb != null) {
                kb.setBaseValue(mt.knockbackResist);
            }
        }
        HbmTweaks.logger.info("HBM Tweaks: mob '{}' tweaked (health x{}/set {}, outgoing x{}, speed x{})",
                mt.entityClass, mt.healthMult, mt.healthSet, mt.outgoingDamageMult, mt.speedMult);
    }
}
