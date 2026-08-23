package com.hbmtweaks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 释放 ID 参考文档（config/hbmtweak/hbm_tweaks_ids.txt）。
 *
 * 每次启动都会重新生成（覆盖旧文件），保证与当前 HBM 版本同步。
 * 内容包含：全部弹种名、核弹字段、炮台类名、HBM 生物类名、食物注册名。
 */
public class IdListWriter {

    public static void release(File target) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("================================================================================\n");
            sb.append(" HBM Tweaks - 可调整数值的完整 ID 参考列表\n");
            sb.append(" 配合 config/hbmtweak/hbmtweaks.json 使用：把需要的 ID 填进对应数组即可\n");
            sb.append(" 优先级：玩家自定义项 > 全局数值 > HBM 原版数值\n");
            sb.append("================================================================================\n\n");

            // ---- 武器/弹种 ----
            sb.append("【weapons - 弹种名 bulletName】共 ").append(WeaponTweaker.getKnownBulletNames().size()).append(" 个\n");
            sb.append("用法: { \"bulletName\": \"xxx\", \"damageMult\": 1.5 }\n");
            sb.append("可用字段: bulletName(弹种名), damageMult(伤害倍率), damageSet(直接设定伤害系数), armorPiercing(穿甲0~1),\n");
            sb.append("  thresholdNegation(护甲阈值穿透), knockback(击退), headshotMult(爆头倍率), spreadMult(散布倍率<1更准),\n");
            sb.append("  velocityMult(弹速倍率), penetrate(true/false穿透), rateOfFireMult(仅旧枪械)\n");
            sb.append("说明: 弹种名来自 HBM sedna 武器系统工厂字段\n\n");
            List<String> bullets = WeaponTweaker.getKnownBulletNames();
            for (int i = 0; i < bullets.size(); i++) {
                sb.append(String.format("%-32s", bullets.get(i)));
                if ((i + 1) % 4 == 0) sb.append("\n");
            }
            sb.append("\n\n");

            // ---- 武器本体（per-gun）----
            sb.append("【guns - 枪械物品注册名 item】共 ").append(ConfigTemplateGenerator.getGunItems().size()).append(" 个\n");
            sb.append("用法: { \"item\": \"hbm:gun_lag\", \"damageMult\": 0.4 }\n");
            sb.append("说明: 按整把枪覆写基础伤害（与弹种倍率相乘）；用于修复同口径共享倍率导致的偏差\n\n");
            for (String g : ConfigTemplateGenerator.getGunItems()) {
                sb.append(g).append("\n");
            }
            sb.append("\n");

            // ---- 近战武器/工具（per-item）----
            sb.append("【tools - HBM 近战武器/工具物品注册名 item】共 ").append(ConfigTemplateGenerator.getMeleeItems().size()).append(" 个\n");
            sb.append("用法: { \"item\": \"hbm:dnt_sword\", \"damageMult\": 0.5 }\n");
            sb.append("说明: 按单件覆写攻击伤害（与 meleeDamageMult 相乘）；未配置单件的 HBM 近战/工具按全局倍率\n\n");
            for (String g : ConfigTemplateGenerator.getMeleeItems()) {
                sb.append(g).append("\n");
            }
            sb.append("\n");

            // ---- 手榴弹装药（per-filling）----
            sb.append("【grenades - 手榴弹装药枚举名 filling】共 ").append(ConfigTemplateGenerator.getGrenadeFillings().size()).append(" 个\n");
            sb.append("用法: { \"filling\": \"HE\", \"damageMult\": 0.25 }\n");
            sb.append("说明: 按装药单独覆写万能手榴弹爆炸伤害（与 grenadeDamageMult 相乘）；\n");
            sb.append("  也可用 { \"item\": \"hbm:stick_dynamite\", ... } 按物品名覆写炸药棒\n\n");
            for (String g : ConfigTemplateGenerator.getGrenadeFillings()) {
                sb.append(g).append("\n");
            }
            sb.append("\n");

            // ---- 核弹 ----
            sb.append("【nukes - BombConfig 字段名 field】共 ").append(ConfigTemplateGenerator.getBombConfigIntFields().size()).append(" 个\n");
            sb.append("用法: { \"field\": \"xxx\", \"value\": 800 }  （需 enableNukeOverride = true）\n");
            sb.append("说明: 修改核弹爆炸半径等数值；与 hbm.cfg 03_nukes 段同名\n\n");
            List<String> nukes = ConfigTemplateGenerator.getBombConfigIntFields();
            for (int i = 0; i < nukes.size(); i++) {
                sb.append(String.format("%-32s", nukes.get(i)));
                if ((i + 1) % 4 == 0) sb.append("\n");
            }
            sb.append("\n\n");

            // ---- 炮台 ----
            sb.append("【turrets - 炮台实体类名 entityClass】共 ").append(ConfigTemplateGenerator.getTurretClasses().size()).append(" 个\n");
            sb.append("用法: { \"entityClass\": \"xxx\", \"range\": 48.0, \"consumption\": 10 }\n");
            sb.append("说明: 修改炮台检测范围/耗电/转速\n\n");
            for (String c : ConfigTemplateGenerator.getTurretClasses()) {
                sb.append(c).append("\n");
            }
            sb.append("\n");

            // ---- 生物 ----
            sb.append("【mobs - HBM 生物实体类名 entityClass】\n");
            sb.append("用法: { \"entityClass\": \"xxx\", \"healthMult\": 0.5, \"damageCap\": 25.0 }\n");
            sb.append("可用字段: entityClass(实体类名), healthMult(血量倍率), damageMult(近战攻击倍率),\n");
            sb.append("  outgoingDamageMult(该怪造成的一切伤害倍率: 近战/弹道/爆炸), speedMult(速度倍率),\n");
            sb.append("  knockbackResist(击退抗性 0~1), damageCap(单次受伤上限), projectileDamageMult(弹道伤害倍率),\n");
            sb.append("  explosionDamageMult(爆炸伤害倍率), fireImmune(火焰免疫 true/false), magicImmune(魔法免疫 true/false)\n");
            sb.append("说明: 类名 = 实体注册类；projectile/explosion/damageCap 字段作用于该生物【受到】的伤害，\n");
            sb.append("  outgoingDamageMult 作用于该生物【造成】的伤害\n\n");
            for (String c : ConfigTemplateGenerator.getMobClasses()) {
                sb.append(c).append("\n");
            }
            sb.append("\n");

            // ---- 食物 ----
            sb.append("【foods - 食物物品注册名 item】\n");
            sb.append("用法: { \"item\": \"hbm:cheese\", \"hunger\": 5 }\n");
            sb.append("说明: hunger 为半鸡腿数，saturation 为饱和度系数；罐头用 hbm:canned_conserve:meta 指定风味\n\n");
            for (String f : ConfigTemplateGenerator.getFoodItems()) {
                sb.append(f).append("\n");
            }
            sb.append("\n");
            sb.append("================================================================================\n");
            sb.append(" 以上 ID 均来自 HBM 核科技(CE 2.5.0.5)源码，mod 启动时自动生成本文件。\n");
            sb.append(" 如果某 ID 失效，可能是 HBM 版本更新导致字段名变化，请对照源码或 mod 日志。\n");
            sb.append("================================================================================\n");

            Files.createDirectories(Paths.get(target.getParent()));
            Files.write(Paths.get(target.toURI()), sb.toString().getBytes(StandardCharsets.UTF_8));
            HbmTweaks.logger.info("HBM Tweaks: ID reference list released to {}", target.getAbsolutePath());
        } catch (IOException e) {
            HbmTweaks.logger.error("HBM Tweaks: failed to write ID list: {}", e.getMessage());
        }
    }
}
