package com.hbmtweaks;

import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

/**
 * 食物数值修改器。
 *
 * HBM 的食物分两类：
 *  1. 普通 ItemFood 子类（ItemLemon/ItemPill/ItemFoodBase 等）：数值存在私有 final 字段
 *     healAmount / saturationModifier 中，可反射修改。
 *  2. ItemEnumMultiFood 子类（canned_conserve 罐头）：覆写了 getHealAmount(ItemStack)，
 *     数值存在枚举常量 (food, sat) 中，需反射修改枚举实例的 food/sat 字段。
 *
 * 配置写法：
 *  - "hbm:cheese"          普通食物
 *  - "hbm:canned_conserve:8"  罐头按 meta 指定风味
 */
public class FoodTweaker {

    public static void apply(TweaksConfig config) {
        for (TweaksConfig.FoodTweak ft : config.foods) {
            if (ft.item == null || ft.item.isEmpty()) continue;
            // 模板条目：hunger 和 saturation 全 null = 未启用，跳过
            if (ft.hunger == null && ft.saturation == null) continue;
            try {
                applyOne(ft);
            } catch (Exception e) {
                HbmTweaks.logger.error("HBM Tweaks: food tweak failed for '{}': {}", ft.item, e.getMessage());
            }
        }
    }

    private static void applyOne(TweaksConfig.FoodTweak ft) throws Exception {
        // 解析 "modid:item" 或 "modid:item:meta"
        String[] parts = ft.item.split(":");
        if (parts.length < 2) {
            HbmTweaks.logger.warn("HBM Tweaks: invalid food item '{}'", ft.item);
            return;
        }
        String registry = parts[0] + ":" + parts[1];
        int meta = -1;
        if (parts.length >= 3) {
            try { meta = Integer.parseInt(parts[2]); } catch (NumberFormatException ignored) { }
        }

        Item item = ForgeRegistries.ITEMS.getValue(new net.minecraft.util.ResourceLocation(registry));
        if (item == null) {
            HbmTweaks.logger.warn("HBM Tweaks: unknown food item '{}'", ft.item);
            return;
        }

        // 情况2：罐头/枚举食物 —— 反射修改枚举常量字段
        if (tryTweakEnumFood(item, ft, meta)) {
            return;
        }

        // 情况1：普通 ItemFood —— 反射修改 healAmount / saturationModifier
        if (item instanceof ItemFood) {
            // 模板条目：hunger 和 saturation 全 null = 未启用，跳过
            if (ft.hunger == null && ft.saturation == null) return;
            ItemFood food = (ItemFood) item;
            if (ft.hunger != null) {
                setHealAmount(food, ft.hunger);
            }
            if (ft.saturation != null) {
                setSaturationModifier(food, ft.saturation.floatValue());
            }
            HbmTweaks.logger.info("HBM Tweaks: food '{}' set hunger={} sat={}",
                    ft.item, ft.hunger, ft.saturation);
        } else {
            HbmTweaks.logger.warn("HBM Tweaks: '{}' is not an ItemFood", ft.item);
        }
    }

    /** 尝试修改枚举类食物（罐头）。成功返回 true。 */
    private static boolean tryTweakEnumFood(Item item, TweaksConfig.FoodTweak ft, int meta) throws Exception {
        // 找到 getHealAmount(ItemStack) 覆写类中的枚举
        Field[] fields = item.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.getType().isEnum() && f.getType().getDeclaringClass() == item.getClass()) {
                f.setAccessible(true);
                Object[] constants = (Object[]) f.getType().getEnumConstants();
                // meta 匹配：枚举 ordinal == meta
                if (meta >= 0 && meta < constants.length) {
                    modifyEnumConstant(constants[meta], ft);
                    HbmTweaks.logger.info("HBM Tweaks: enum food '{}' meta {} ({} {}) set hunger={} sat={}",
                            ft.item, meta, f.getType().getSimpleName(), constants[meta], ft.hunger, ft.saturation);
                    return true;
                }
            }
        }
        // 罐头枚举在内部类（ItemConserve.EnumFoodType），需要搜索嵌套类
        for (Class<?> nested : item.getClass().getDeclaredClasses()) {
            if (nested.isEnum()) {
                Object[] constants = nested.getEnumConstants();
                if (meta >= 0 && meta < constants.length) {
                    modifyEnumConstant(constants[meta], ft);
                    HbmTweaks.logger.info("HBM Tweaks: enum food '{}' meta {} ({}) set hunger={} sat={}",
                            ft.item, meta, nested.getSimpleName(), ft.hunger, ft.saturation);
                    return true;
                }
            }
        }
        return false;
    }

    /** 修改枚举常量（如 ItemConserve.EnumFoodType.BEEF）的 food/sat 字段 */
    private static void modifyEnumConstant(Object constant, TweaksConfig.FoodTweak ft) throws Exception {
        for (Field f : constant.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue; // 跳过 $VALUES 等
            f.setAccessible(true);
            if (f.getType() == int.class && ft.hunger != null && f.getName().equals("food")) {
                f.setInt(constant, ft.hunger);
            } else if (f.getType() == float.class && ft.saturation != null && f.getName().equals("sat")) {
                f.setFloat(constant, ft.saturation.floatValue());
            }
        }
    }

    /**
     * 在 ItemFood 类自身查找第一个 private final 且类型匹配的字段。
     * ItemFood 的字段布局（1.12.2）：
     *   public final int itemUseDuration = 32;   <- public，排除
     *   private final int healAmount;            <- 第一个 private final int（目标）
     *   private final float saturationModifier;  <- 第一个 private final float（目标）
     *   private final boolean isWolfsFavoriteMeat;
     *   private boolean alwaysEdible;
     *   private PotionEffect potionId;
     *   private float potionEffectProbability;
     * 必须排除 public 的 itemUseDuration（否则 int 会误中它而不是 healAmount）。
     * 运行时字段名是 SRG 名，因此按"类型 + private final 修饰符"匹配。
     */
    private static Field findItemFoodFieldByType(Class<?> clazz, Class<?> type) {
        Class<?> itemFoodClass = null;
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            if (c.getName().equals("net.minecraft.item.ItemFood")) {
                itemFoodClass = c;
                break;
            }
            c = c.getSuperclass();
        }
        if (itemFoodClass == null) return null;
        for (Field f : itemFoodClass.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (f.getType() == type
                    && Modifier.isPrivate(mod)
                    && Modifier.isFinal(mod)
                    && !Modifier.isStatic(mod)) {
                return f;
            }
        }
        return null;
    }

    /** 设置 ItemFood.healAmount（第一个 private final int 实例字段） */
    private static void setHealAmount(ItemFood food, int value) throws Exception {
        Field f = findItemFoodFieldByType(food.getClass(), int.class);
        if (f == null) throw new NoSuchFieldException("ItemFood int field (healAmount)");
        f.setAccessible(true);
        f.setInt(food, value);
    }

    /** 设置 ItemFood.saturationModifier（第一个 private final float 实例字段） */
    private static void setSaturationModifier(ItemFood food, float value) throws Exception {
        Field f = findItemFoodFieldByType(food.getClass(), float.class);
        if (f == null) throw new NoSuchFieldException("ItemFood float field (saturationModifier)");
        f.setAccessible(true);
        f.setFloat(food, value);
    }
}
