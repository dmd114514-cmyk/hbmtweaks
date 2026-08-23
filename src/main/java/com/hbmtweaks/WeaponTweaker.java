package com.hbmtweaks;

import com.hbm.items.weapon.sedna.BulletConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 武器数值修改器（HBM CE 2.5 sedna 系统）。
 *
 * HBM CE 2.5 的现代枪械使用 sedna 武器系统：
 *  - 每个弹种是 com.hbm.items.weapon.sedna.BulletConfig 的实例
 *  - 定义在 com.hbm.items.weapon.sedna.factory.XFactory* 的 public static 字段中
 *  - BulletConfig.configs 静态列表保存全部已注册弹种
 *  - 关键字段全 public：damageMult / velocity / spread / projectilesMin-Max / gravity / expires
 *
 * 修改方式：
 *  1. 全局：遍历 BulletConfig.configs（全部弹种）按倍率修改
 *  2. 按弹种名：反射扫描 XFactory* 类的 public static BulletConfig 字段（字段名即弹种名）
 *  3. 旧系统（ItemGunBase）兼容保留
 */
public class WeaponTweaker {

    /** XFactory* 类中所有 public static BulletConfig 字段名 -> 字段引用 */
    private static Map<String, Field> bulletFieldsByName = new HashMap<>();

    /** 旧系统 BulletConfigSyncingUtil.configSet 字段（private static HashMap） */
    private static Field configSetField;

    /** 是否成功初始化 sedna 系统反射 */
    private static boolean sednaReady = false;

    /** 供配置模板生成使用：返回全部已发现的弹种字段名（按字母序） */
    public static java.util.List<String> getKnownBulletNames() {
        java.util.List<String> names = new ArrayList<>(bulletFieldsByName.keySet());
        java.util.Collections.sort(names);
        return names;
    }

    static {
        try {
            // 旧系统 configSet（供按物品名修改旧枪械弹药时使用）
            try {
                configSetField = com.hbm.handler.BulletConfigSyncingUtil.class.getDeclaredField("configSet");
                configSetField.setAccessible(true);
            } catch (NoSuchFieldException ignored) { }
            // 找到所有 XFactory* 类（在 com.hbm.items.weapon.sedna.factory 包）
            String pkg = "com.hbm.items.weapon.sedna.factory";
            ClassLoader cl = WeaponTweaker.class.getClassLoader();
            // 通过 BulletConfig.configs 的声明类获取包路径（更可靠）
            Class<?> bulletClass = BulletConfig.class;
            String pkgPath = bulletClass.getPackage().getName() + ".factory";
            // 使用 classpath 扫描：从 BulletConfig 的类加载器加载已知 XFactory 类
            String[] knownFactories = {
                    "XFactory12ga", "XFactory10ga", "XFactory22lr", "XFactory357", "XFactory35800",
                    "XFactory40mm", "XFactory44", "XFactory45", "XFactory50", "XFactory556mm",
                    "XFactory75Bolt", "XFactory762mm", "XFactory9mm", "XFactoryAccelerator",
                    "XFactoryBlackPowder", "XFactoryCatapult", "XFactoryDrill", "XFactoryEnergy",
                    "XFactoryFlamer", "XFactoryFolly", "XFactoryPA", "XFactoryRocket", "XFactoryTool",
                    "XFactoryTurret", "XFactoryGrenade", "XFactoryChaingun", "XFactoryEgun",
                    // 手榴弹破片/光束弹种（grenade_universal 的装药集束/激光用）
                    "com.hbm.items.weapon.grenade.ItemGrenadeFilling"
            };
            for (String name : knownFactories) {
                try {
                    // 支持完全限定名（如手榴弹装药类）与工厂简称（XFactory*）
                    String fqcn = name.contains(".") ? name : pkgPath + "." + name;
                    Class<?> factory = Class.forName(fqcn, false, cl);
                    for (Field f : factory.getDeclaredFields()) {
                        if (!Modifier.isStatic(f.getModifiers()) || !Modifier.isPublic(f.getModifiers())) continue;
                        // 单个 BulletConfig 字段，或 BulletConfig[] 数组字段（火箭等：数组元素即各弹种）
                        boolean isSingle = BulletConfig.class.isAssignableFrom(f.getType());
                        boolean isArray = f.getType().isArray()
                                && BulletConfig.class.isAssignableFrom(f.getType().getComponentType());
                        if (isSingle || isArray) {
                            f.setAccessible(true);
                            bulletFieldsByName.put(f.getName(), f);
                        }
                    }
                } catch (ClassNotFoundException ignored) {
                    // 该类可能不存在，跳过
                }
            }
            sednaReady = true;
            HbmTweaks.logger.info("HBM Tweaks: sedna weapon system initialized, found {} bullet fields",
                    bulletFieldsByName.size());
        } catch (Exception e) {
            HbmTweaks.logger.error("HBM Tweaks: sedna reflection init failed", e);
        }
    }

    public static void apply(TweaksConfig config) {
        try {
            // ============ 按武器本体（per-gun）覆写基础伤害：填充查询缓存 ============
            // 注意：per-gun 不与弹种互斥，两者相乘（弹种 mult 先作用于 BulletConfig，
            // 枪械倍率在发射时乘在 baseDamage 上，最终伤害 = baseDmg × gunMult × bulletMult）
            GunTweakCache.clear();
            if (config.guns != null) {
                for (TweaksConfig.GunTweak gt : config.guns) {
                    if (gt.item != null && !gt.item.isEmpty() && gt.damageMult != null) {
                        GunTweakCache.put(gt.item, gt.damageMult);
                    }
                }
                if (GunTweakCache.size() > 0) {
                    HbmTweaks.logger.info("HBM Tweaks: {} per-gun damage overrides cached", GunTweakCache.size());
                }
            }



            // ============ 按手榴弹装药/物品覆写爆炸伤害：填充查询缓存 ============
            GrenadeTweakCache.clear();
            if (config.grenades != null) {
                for (TweaksConfig.GrenadeTweak gt : config.grenades) {
                    if (gt.damageMult == null) continue;
                    if (gt.filling != null && !gt.filling.isEmpty()) {
                        GrenadeTweakCache.putFilling(gt.filling, gt.damageMult);
                    } else if (gt.item != null && !gt.item.isEmpty()) {
                        GrenadeTweakCache.putItem(gt.item, gt.damageMult);
                    }
                }
                if (GrenadeTweakCache.size() > 0) {
                    HbmTweaks.logger.info("HBM Tweaks: {} per-grenade damage overrides cached", GrenadeTweakCache.size());
                }
            }

            // ============ 全局弹药伤害倍率（仅当玩家未自定义任何弹种时生效） ============
            if (config.isGlobalWeaponActive()) {
                if (config.globalDamageMult != 1.0) {
                    applyGlobalDamage(config.globalDamageMult);
                }
                if (config.globalRateOfFireMult != 1.0 || config.globalAmmoCapMult != 1.0) {
                    applyGunConfigs(config.globalRateOfFireMult, config.globalAmmoCapMult);
                }
            }

            // ============ 按弹种名修改（玩家自定义，最高优先级） ============
            for (TweaksConfig.WeaponTweak tw : config.weapons) {
                if (tw.bulletName != null && !tw.bulletName.isEmpty()) {
                    applyBulletTweak(tw);
                } else if (tw.itemName != null && !tw.itemName.isEmpty()) {
                    applyGunTweakByItem(tw);
                }
            }

            HbmTweaks.logger.info("HBM Tweaks: weapon tweaks applied ({} bullet fields available)",
                    bulletFieldsByName.size());
        } catch (Exception e) {
            HbmTweaks.logger.error("HBM Tweaks: weapon tweak failed", e);
        }
    }

    /** 全局伤害倍率：遍历 BulletConfig.configs 静态列表 */
    private static void applyGlobalDamage(double mult) throws Exception {
        if (!sednaReady) return;
        int count = 0;
        try {
            Field configsField = BulletConfig.class.getDeclaredField("configs");
            configsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<BulletConfig> configs = (List<BulletConfig>) configsField.get(null);
            for (BulletConfig bc : configs) {
                if (bc != null) {
                    bc.damageMult *= mult;
                    count++;
                }
            }
        } catch (NoSuchFieldException e) {
            HbmTweaks.logger.warn("HBM Tweaks: BulletConfig.configs not found, fallback to field scan");
        }
        // 兜底：反射扫描所有 XFactory 字段（数组字段跳过——全局倍率只作用于单个弹种）
        if (count == 0) {
            for (Field f : bulletFieldsByName.values()) {
                if (f.getType().isArray()) continue;
                BulletConfig bc = (BulletConfig) f.get(null);
                if (bc != null) {
                    bc.damageMult *= mult;
                    count++;
                }
            }
        }
        HbmTweaks.logger.info("HBM Tweaks: global damage mult {} applied to {} sedna bullet configs",
                mult, count);
    }

    /** 按弹种名修改单个弹药配置 */
    private static void applyBulletTweak(TweaksConfig.WeaponTweak tw) throws Exception {
        if (!sednaReady) return;
        // 模板条目：全 null = 未启用，直接跳过（避免无谓日志与操作）
        if (tw.damageMult == null && tw.rateOfFireMult == null && tw.ammoCapMult == null
                && tw.damageSet == null && tw.armorPiercing == null && tw.thresholdNegation == null
                && tw.knockback == null && tw.headshotMult == null && tw.spreadMult == null
                && tw.velocityMult == null && tw.penetrate == null) return;
        Field f = bulletFieldsByName.get(tw.bulletName);
        if (f == null) {
            HbmTweaks.logger.warn("HBM Tweaks: unknown bullet name '{}' (available: {} fields)",
                    tw.bulletName, bulletFieldsByName.size());
            return;
        }
        // 数组字段（如 rocket_rpzb / rocket_qd / rocket_ml）：遍历全部元素应用
        if (f.getType().isArray()) {
            BulletConfig[] arr = (BulletConfig[]) f.get(null);
            if (arr == null || arr.length == 0) {
                HbmTweaks.logger.warn("HBM Tweaks: bullet array field '{}' is empty (not initialized)", tw.bulletName);
                return;
            }
            int applied = 0;
            for (BulletConfig bc : arr) {
                if (bc == null) continue;
                if (tw.damageMult != null) {
                    bc.damageMult *= tw.damageMult;
                }
                if (tw.damageSet != null) {
                    bc.damageMult = tw.damageSet.floatValue();
                }
                if (tw.armorPiercing != null) {
                    bc.armorPiercingPercent = tw.armorPiercing.floatValue();
                }
                if (tw.thresholdNegation != null) {
                    bc.armorThresholdNegation = tw.thresholdNegation.floatValue();
                }
                if (tw.knockback != null) {
                    bc.knockbackMult = tw.knockback.floatValue();
                }
                if (tw.headshotMult != null) {
                    bc.headshotMult = tw.headshotMult.floatValue();
                }
                if (tw.spreadMult != null) {
                    bc.spread *= tw.spreadMult.floatValue();
                }
                if (tw.velocityMult != null) {
                    bc.velocity *= tw.velocityMult.floatValue();
                }
                if (tw.penetrate != null) {
                    bc.doesPenetrate = tw.penetrate;
                }
                applied++;
            }
            HbmTweaks.logger.info("HBM Tweaks: bullet array '{}' tweaked ({} elements, dmgMult x{})",
                    tw.bulletName, applied, tw.damageMult);
            return;
        }
        BulletConfig bc = (BulletConfig) f.get(null);
        if (bc == null) {
            HbmTweaks.logger.warn("HBM Tweaks: bullet field '{}' is null (not initialized)", tw.bulletName);
            return;
        }
        StringBuilder applied = new StringBuilder();
        if (tw.damageMult != null) {
            bc.damageMult *= tw.damageMult;
            applied.append(String.format("dmgMult x%.2f (now %.2f); ", tw.damageMult, bc.damageMult));
        }
        if (tw.damageSet != null) {
            bc.damageMult = tw.damageSet.floatValue();
            applied.append(String.format("dmgSet %.2f; ", bc.damageMult));
        }
        if (tw.armorPiercing != null) {
            bc.armorPiercingPercent = tw.armorPiercing.floatValue();
            applied.append(String.format("AP %.2f; ", bc.armorPiercingPercent));
        }
        if (tw.thresholdNegation != null) {
            bc.armorThresholdNegation = tw.thresholdNegation.floatValue();
            applied.append(String.format("thrNeg %.2f; ", bc.armorThresholdNegation));
        }
        if (tw.knockback != null) {
            bc.knockbackMult = tw.knockback.floatValue();
            applied.append(String.format("kb %.2f; ", bc.knockbackMult));
        }
        if (tw.headshotMult != null) {
            bc.headshotMult = tw.headshotMult.floatValue();
            applied.append(String.format("hs %.2f; ", bc.headshotMult));
        }
        if (tw.spreadMult != null) {
            bc.spread *= tw.spreadMult.floatValue();
            applied.append(String.format("spread x%.2f (now %.3f); ", tw.spreadMult, bc.spread));
        }
        if (tw.velocityMult != null) {
            bc.velocity *= tw.velocityMult.floatValue();
            applied.append(String.format("vel x%.2f (now %.2f); ", tw.velocityMult, bc.velocity));
        }
        if (tw.penetrate != null) {
            bc.doesPenetrate = tw.penetrate;
            applied.append("penetrate=").append(tw.penetrate).append("; ");
        }
        if (tw.rateOfFireMult != null) {
            // sedna 弹种没有 rateOfFire（在 GunConfig 的 Receiver 里），这里只处理弹种属性
            applied.append("rateOfFireMult ignored (sedna); ");
        }
        if (applied.length() > 0) {
            HbmTweaks.logger.info("HBM Tweaks: bullet '{}' {}", tw.bulletName, applied.toString().trim());
        }
    }

    /** 全局射速/弹匣：遍历 ModItems.ALL_ITEMS 找 ItemGunBase（旧系统） */
    private static void applyGunConfigs(double rateMult, double ammoMult) throws Exception {
        int count = 0;
        for (Object item : com.hbm.items.ModItems.ALL_ITEMS) {
            if (item instanceof com.hbm.items.weapon.ItemGunBase) {
                com.hbm.items.weapon.ItemGunBase gun = (com.hbm.items.weapon.ItemGunBase) item;
                if (gun.mainConfig != null) {
                    if (rateMult != 1.0) gun.mainConfig.rateOfFire = Math.max(1, (int) (gun.mainConfig.rateOfFire / rateMult));
                    if (ammoMult != 1.0) gun.mainConfig.ammoCap = Math.max(1, (int) (gun.mainConfig.ammoCap * ammoMult));
                }
                if (gun.altConfig != null) {
                    if (rateMult != 1.0) gun.altConfig.rateOfFire = Math.max(1, (int) (gun.altConfig.rateOfFire / rateMult));
                    if (ammoMult != 1.0) gun.altConfig.ammoCap = Math.max(1, (int) (gun.altConfig.ammoCap * ammoMult));
                }
                count++;
            }
        }
        HbmTweaks.logger.info("HBM Tweaks: global gun rate/ammo applied to {} legacy guns", count);
    }

    /** 按武器物品注册名修改（旧系统 ItemGunBase 的射速/弹匣/弹药伤害） */
    private static void applyGunTweakByItem(TweaksConfig.WeaponTweak tw) throws Exception {
        for (Object item : com.hbm.items.ModItems.ALL_ITEMS) {
            if (!(item instanceof com.hbm.items.weapon.ItemGunBase)) continue;
            com.hbm.items.weapon.ItemGunBase gun = (com.hbm.items.weapon.ItemGunBase) item;
            if (gun.getRegistryName() == null || !gun.getRegistryName().toString().equals(tw.itemName)) continue;
            if (gun.mainConfig != null) {
                if (tw.rateOfFireMult != null) gun.mainConfig.rateOfFire = Math.max(1, (int) (gun.mainConfig.rateOfFire / tw.rateOfFireMult));
                if (tw.ammoCapMult != null) gun.mainConfig.ammoCap = Math.max(1, (int) (gun.mainConfig.ammoCap * tw.ammoCapMult));
                // 弹药伤害：遍历该枪引用的全部弹种ID
                if (tw.damageMult != null && gun.mainConfig.config != null) {
                    @SuppressWarnings("unchecked")
                    Map<Integer, com.hbm.handler.BulletConfiguration> set =
                            (Map<Integer, com.hbm.handler.BulletConfiguration>) configSetField.get(null);
                    for (Integer id : gun.mainConfig.config) {
                        com.hbm.handler.BulletConfiguration bc = set.get(id);
                        if (bc != null) {
                            bc.dmgMin *= tw.damageMult;
                            bc.dmgMax *= tw.damageMult;
                        }
                    }
                }
            }
            HbmTweaks.logger.info("HBM Tweaks: gun '{}' tweaked (rateMult={}, ammoMult={}, dmgMult={})",
                    tw.itemName, tw.rateOfFireMult, tw.ammoCapMult, tw.damageMult);
            return;
        }
        HbmTweaks.logger.warn("HBM Tweaks: unknown gun item '{}'", tw.itemName);
    }
}
