package com.hbmtweaks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * HBM Tweaks - 一个用于修改 HBM 核科技(NTM-CE)武器/怪物/食物数值的附属 mod。
 *
 * 修改方式：
 *  - 武器：反射读取 HBM 的 GunConfiguration / BulletConfiguration（字段均为 public），
 *          通过 BulletConfigSyncingUtil.configSet（private static HashMap）拿到全部弹药配置。
 *  - 怪物：EntityJoinWorldEvent 事件中修改实体 SharedMonsterAttributes 属性。
 *  - 食物：反射修改 ItemFood.healAmount（private final int）。
 */
@Mod(modid = HbmTweaks.MODID, name = HbmTweaks.NAME, version = HbmTweaks.VERSION,
     dependencies = "required-after:hbm@[2.5.0.5,)")
public class HbmTweaks {

    public static final String MODID = "hbmtweaks";
    public static final String NAME = "HBM Tweaks";
    public static final String VERSION = "1.3.0";

    public static Logger logger;
    public static TweaksConfig config;
    /** 配置目录（preInit 时记录，postInit 释放 ids 文档时使用） */
    private static File configDir;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        configDir = new File(event.getModConfigurationDirectory(), "hbmtweak");
        loadConfig(new File(configDir, "hbmtweaks.json"));
        // ID 参考文档延迟到 postInit 释放（此时 HBM 全部物品/弹种已注册，guns 列表才完整）
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // 注册事件处理器（怪物属性修改在这里生效，每只实体生成时触发）
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new MobTweakHandler());
        // 预处理怪物规则缓存（模板中大量全 null 条目不进入事件路径）
        MobTweakHandler.buildCache(config);
        logger.info("HBM Tweaks: mob handler registered");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // 此时 HBM 的全部物品/弹药配置都已注册完毕，可以进行反射修改
        if (config.isAnyWeaponTweak()) {
            WeaponTweaker.apply(config);
        }
        // 近战武器/工具：反射改写 Item.attackDamage（tooltip 与实际伤害一致）
        if (config.hasCustomTool()) {
            MeleeTweaker.apply(config);
        }
        if (config.isAnyFoodTweak()) {
            FoodTweaker.apply(config);
        }
        if (config.isAnyNukeTweak()) {
            NukeTweaker.apply(config);
        }
        if (config.isAnyTurretTweak()) {
            TurretTweaker.apply(config);
        }
        // 释放 ID 参考文档（HBM 物品注册完成后，guns 段才有完整列表）
        IdListWriter.release(new File(configDir, "hbm_tweaks_ids.txt"));
        logger.info("HBM Tweaks: post-init tweaks applied");
    }

    public static void loadConfig(File file) {
        try {
            if (!file.exists()) {
                // 首次启动：生成空模板配置（玩家自行填写；说明见 JSON 内注释字段）
                config = TweaksConfig.createEmpty();
                saveConfig(file, config);
                logger.info("HBM Tweaks: created empty config template at {}", file.getAbsolutePath());
                logger.info("HBM Tweaks: see hbm_tweaks_ids.txt in the same folder for all usable IDs");
                return;
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (java.io.InputStreamReader reader =
                         new java.io.InputStreamReader(new java.io.FileInputStream(file), StandardCharsets.UTF_8)) {
                config = gson.fromJson(reader, TweaksConfig.class);
            }
            if (config == null) {
                config = TweaksConfig.createEmpty();
            }
            // 补齐缺失字段（允许部分配置）
            config.normalize();
            logger.info("HBM Tweaks: config loaded from {}", file.getAbsolutePath());
        } catch (JsonSyntaxException e) {
            logger.error("HBM Tweaks: config JSON syntax error, using defaults. Error: {}", e.getMessage());
            config = TweaksConfig.createEmpty();
        } catch (IOException e) {
            logger.error("HBM Tweaks: failed to read config, using defaults. Error: {}", e.getMessage());
            config = TweaksConfig.createEmpty();
        }
    }

    public static void saveConfig(File file, TweaksConfig cfg) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.createDirectories(Paths.get(file.getParent()));
            // 若文件不存在（首次生成），写入带中文说明的模板；否则只写纯配置
            if (!file.exists()) {
                String template = ConfigTemplateWriter.buildTemplateJson();
                Files.write(Paths.get(file.toURI()), template.getBytes(StandardCharsets.UTF_8));
            } else {
                try (java.io.OutputStreamWriter writer =
                             new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), StandardCharsets.UTF_8)) {
                    gson.toJson(cfg, writer);
                }
            }
        } catch (IOException e) {
            logger.error("HBM Tweaks: failed to save config: {}", e.getMessage());
        }
    }
}
