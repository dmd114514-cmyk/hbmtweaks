package com.hbmtweaks;

import java.util.HashMap;
import java.util.Map;

/**
 * 炮台配置缓存。
 *
 * 在 postInit 时由 TurretTweaker 填充；Mixin 运行时只读。
 * 使用 primitive 值 + null 哨兵判断，避免每次调用解析 JSON。
 */
public class TurretTweakCache {

    private static final Map<String, Double> rangeOverrides = new HashMap<>();
    private static final Map<String, Long> consumptionOverrides = new HashMap<>();
    private static final Map<String, Double> yawSpeedOverrides = new HashMap<>();

    public static Double getRange(Class<?> clazz) {
        return rangeOverrides.get(clazz.getName());
    }

    public static Long getConsumption(Class<?> clazz) {
        return consumptionOverrides.get(clazz.getName());
    }

    public static Double getYawSpeed(Class<?> clazz) {
        return yawSpeedOverrides.get(clazz.getName());
    }

    public static void putRange(String className, double value) {
        rangeOverrides.put(className, value);
    }

    public static void putConsumption(String className, long value) {
        consumptionOverrides.put(className, value);
    }

    public static void putYawSpeed(String className, double value) {
        yawSpeedOverrides.put(className, value);
    }

    public static int size() {
        return rangeOverrides.size() + consumptionOverrides.size() + yawSpeedOverrides.size();
    }
}
