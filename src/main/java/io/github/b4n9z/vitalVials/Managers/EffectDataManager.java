package io.github.b4n9z.vitalVials.Managers;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class EffectDataManager {
    private final String id;
    private final boolean enabled;
    private final String name;
    private final String effect;
    private final String keyValue;
    private final NamespacedKey effectKey;
    private final PotionEffectType potionEffectType;
    private final String lore;
    private final int maxLevel;
    private final boolean particles;
    private final boolean saveEffectData;
    private final String priceType;
    private final double refundPercentage;
    private final boolean autoActivate;
    private final List<Integer> durationPerUpgrade;
    private final List<Integer> cooldownPerUpgrade;
    private final List<Integer> costPerUpgrade;

    public EffectDataManager(String id, ConfigurationSection section) {
        this.id = id;
        this.enabled = section.getBoolean("enabled", true);
        this.name = section.getString("name", id);
        this.effect = section.getString("effect", "");
        this.keyValue = effect.toLowerCase();
        this.effectKey = NamespacedKey.minecraft(keyValue);
        this.potionEffectType = Registry.EFFECT.get(effectKey);
        this.lore = section.getString("lore", "");
        this.maxLevel = section.getInt("maxLevel", 1);
        this.particles = section.getBoolean("particles", false);
        this.saveEffectData = section.getBoolean("saveEffectData", true);
        this.priceType = section.getString("priceType", "MAX_HEALTH");
        this.refundPercentage = section.getDouble("refundPercentage", 50);
        this.autoActivate = section.getBoolean("autoActivate", true);
        this.durationPerUpgrade = section.getIntegerList("durationPerUpgrade");
        this.cooldownPerUpgrade = section.getIntegerList("cooldownPerUpgrade");
        this.costPerUpgrade = section.getIntegerList("costPerUpgrade");

        validateEffect();
    }

    public void validateEffect() {
        if (potionEffectType == null) throw new IllegalArgumentException("effect must be a valid potion effect:§c " + id + " (" + effect + ")");
        if (!saveEffectData && !autoActivate) throw new IllegalArgumentException("autoActivate must be true if saveEffectData is false:§c " + id);
        if (!priceType.equals("MAX_HEALTH") && !priceType.equals("HEALTH")) throw new IllegalArgumentException("priceType must be MAX_HEALTH or HEALTH:§c " + id + " (" + priceType + ")");
        if (durationPerUpgrade.size() != maxLevel) throw new IllegalArgumentException("durationPerUpgrade must have the same size as maxLevel:§c " + id + " (" + maxLevel + ")");
        if (cooldownPerUpgrade.size() != maxLevel) throw new IllegalArgumentException("cooldownPerUpgrade must have the same size as maxLevel:§c " + id + " (" + maxLevel + ")");
        if (costPerUpgrade.size() != maxLevel) throw new IllegalArgumentException("costPerUpgrade must have the same size as maxLevel:§c " + id + " (" + maxLevel + ")");
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getEffect() {
        return effect;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public NamespacedKey getEffectKey() {
        return effectKey;
    }

    public PotionEffectType getPotionEffectType() {
        return potionEffectType;
    }

    public String getLore() {
        return lore;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public boolean hasParticles() {
        return particles;
    }

    public boolean isSaveEffectData() {
        return saveEffectData;
    }

    public String getPriceType() {
        return priceType;
    }

    public double getRefundPercentage() {
        return refundPercentage;
    }

    public double getRefundPercent() {
        return refundPercentage /100;
    }

    public boolean getAutoActivate() {
        return autoActivate;
    }

    public List<Integer> getDurationUpgrade() {
        return durationPerUpgrade;
    }

    public int getDurationPerUpgrade(int level) {
        return durationPerUpgrade.get(level);
    }

    public List<Integer> getCooldownUpgrade() {
        return cooldownPerUpgrade;
    }

    public int getCooldownPerUpgrade(int level) {
        return cooldownPerUpgrade.get(level);
    }

    public List<Integer> getCostUpgrade() {
        return costPerUpgrade;
    }

    public int getCostPerUpgrade(int level) {
        return costPerUpgrade.get(level);
    }

    public int getCostFromTo(int from, int to) {
        int cost = 0;
        for (int i = from; i < to;) {
            i++;
            cost += costPerUpgrade.get(i);
        }
        return cost;
    }
}
