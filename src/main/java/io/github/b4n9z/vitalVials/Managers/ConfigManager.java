package io.github.b4n9z.vitalVials.Managers;

import java.io.File;
import java.util.*;

import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public class ConfigManager {
    private final Plugin plugin;
    private final VitalVials pluginMain;

    private FileConfiguration effectsConfig;
    private final Map<String, EffectDataManager> effects = new LinkedHashMap<>();

    private FileConfiguration activationConfig;
    private final Map<String, ActivationDataManager> activations = new HashMap<>();

    private final Map<String, Map<String, String>> allActivationTypes = new HashMap<>();

    private boolean isMaxHPEnabled;
    private double maxHPValue;
    private double minHPValue;

    private boolean isDefaultShowScoreboardEnabled;
    private int periodUpdateScoreboard;
    private static final String PERM_PREFIX = "vv.";

    private final Map<String, Boolean> permissionOverrides = new HashMap<>();

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.pluginMain = (VitalVials) plugin;
        loadConfig();
    }

    public void loadConfig() {
        try {
            String effectFileName = plugin.getConfig().getString("effects", "effects.yml");
            File effectFile = new File(plugin.getDataFolder(), effectFileName);

            if (!effectFile.exists()) {
                plugin.saveResource(effectFileName, false);
            }

            effectsConfig = YamlConfiguration.loadConfiguration(effectFile);
            loadAllEffects();

            String activationFileName = plugin.getConfig().getString("activation", "activation.yml");
            File activationFile = new File(plugin.getDataFolder(), activationFileName);

            if (!activationFile.exists()) {
                plugin.saveResource(activationFileName, false);
            }

            activationConfig = YamlConfiguration.loadConfiguration(activationFile);
            loadAllActivations();
            loadAllActivationTypes();

            isMaxHPEnabled = plugin.getConfig().getBoolean("maxHP.enabled", true);
            maxHPValue = plugin.getConfig().getDouble("maxHP.value", 20);
            minHPValue = plugin.getConfig().getDouble("minHP", 2);

            isDefaultShowScoreboardEnabled = plugin.getConfig().getBoolean("scoreboard.defaultShowScoreboard", true);
            periodUpdateScoreboard = plugin.getConfig().getInt("scoreboard.periodUpdate", 10);

            permissionOverrides.put("reload", plugin.getConfig().getBoolean("permissionsAllPlayer.reload", false));
            permissionOverrides.put("shop", plugin.getConfig().getBoolean("permissionsAllPlayer.shop", true));
            permissionOverrides.put("buyEffect", plugin.getConfig().getBoolean("permissionsAllPlayer.buyEffect", true));
            permissionOverrides.put("scoreboard", plugin.getConfig().getBoolean("permissionsAllPlayer.scoreboard", true));
            permissionOverrides.put("viewStatus", plugin.getConfig().getBoolean("permissionsAllPlayer.viewStatus", true));
            permissionOverrides.put("refundEffect", plugin.getConfig().getBoolean("permissionsAllPlayer.refundEffect", true));
            permissionOverrides.put("removeData", plugin.getConfig().getBoolean("permissionsAllPlayer.removeData", false));
        } catch (Exception e) {
            Bukkit.getServer().getConsoleSender().sendMessage("§cFailed to load config:§f " + e.getMessage());
        }
    }

    private void loadAllEffects() {
        effects.clear();

        List<String> shortKey = new ArrayList<>(effectsConfig.getKeys(false));
        shortKey.sort(String.CASE_INSENSITIVE_ORDER);

        for (String key : shortKey) {
            ConfigurationSection section = effectsConfig.getConfigurationSection(key);
            if (section != null) {
                EffectDataManager data = new EffectDataManager(key, section);
                effects.put(key, data);
            }
        }
    }

    public Map<String, EffectDataManager> getEffects() {
        return effects;
    }

    public List<String> getAllEffectsEnabled() {
        List<String> enabledEffects = new ArrayList<>();
        for (EffectDataManager effect : effects.values()) {
            if (effect.isEnabled()) {
                enabledEffects.add(effect.getId());
            }
        }
        return enabledEffects;
    }

    private void loadAllActivations() {
        activations.clear();
        Set<String> allKeys = activationConfig.getKeys(false);

        for (String key : allKeys) {
            ConfigurationSection section = activationConfig.getConfigurationSection(key);
            if (section != null) {
                ActivationDataManager data = new ActivationDataManager(key, section);
                activations.put(key, data);
            }
        }
    }

    public Map<String, ActivationDataManager> getActivations() {
        return activations;
    }

    public boolean isRightToolForEffect(Material material, EffectDataManager effect, String type) {
        String materialString = material.name();
        if (!activations.containsKey(materialString) && materialString.contains("_")) {
            String[] split = materialString.split("_");
            if (activations.containsKey(split[split.length - 1])) {
                materialString = split[split.length - 1];
            } else {
                return false;
            }
        }
        ActivationDataManager activation = activations.get(materialString);
        if (activation == null) return false;
        if (type.equals("rightClick") && activation.getRightClick().contains(effect.getId())) return true;
        if (type.equals("leftClick") && activation.getLeftClick().contains(effect.getId())) return true;
        if (type.equals("YouAreHittingEnemy") && activation.getYouAreHittingEnemy().contains(effect.getId())) return true;
        return type.equals("enemyHitYou") && activation.getEnemyIsHittingYou().contains(effect.getId());
    }

    public boolean isAllToolsForEffect(EffectDataManager effect, String type) {
        ActivationDataManager activation = activations.get("ALL_ITEMS");
        if (type.equals("rightClick") && activation.getRightClick().contains(effect.getId())) return true;
        if (type.equals("leftClick") && activation.getLeftClick().contains(effect.getId())) return true;
        if (type.equals("YouAreHittingEnemy") && activation.getYouAreHittingEnemy().contains(effect.getId())) return true;
        return type.equals("enemyHitYou") && activation.getEnemyIsHittingYou().contains(effect.getId());
    }

    public void loadAllActivationTypes() {
        allActivationTypes.clear();
        for (EffectDataManager effect : effects.values()) {
            String effectName = effect.getId();
            Map<String, List<String>> activationTypes = new HashMap<>();
            activationTypes.put("rightClick", new ArrayList<>());
            activationTypes.put("leftClick", new ArrayList<>());
            activationTypes.put("YouAreHittingEnemy", new ArrayList<>());
            activationTypes.put("enemyHitYou", new ArrayList<>());

            for (Map.Entry<String, ActivationDataManager> entry : activations.entrySet()) {
                if (entry.getValue().getRightClick().contains(effectName)) {
                    activationTypes.get("rightClick").add(entry.getKey());
                }
                if (entry.getValue().getLeftClick().contains(effectName)) {
                    activationTypes.get("leftClick").add(entry.getKey());
                }
                if (entry.getValue().getYouAreHittingEnemy().contains(effectName)) {
                    activationTypes.get("YouAreHittingEnemy").add(entry.getKey());
                }
                if (entry.getValue().getEnemyIsHittingYou().contains(effectName)) {
                    activationTypes.get("enemyHitYou").add(entry.getKey());
                }
            }
            Map<String, String> stringMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : activationTypes.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    stringMap.put(entry.getKey(), String.join(", ", entry.getValue()));
                }
            }
            allActivationTypes.put(effectName, stringMap);
        }
    }

    public Map<String, String> getActivationTypes(EffectDataManager effect) {
        return allActivationTypes.get(effect.getId());
    }

    public boolean hasActivationTypes(EffectDataManager effect, String type) {
        Map<String, String> activationTypes = getActivationTypes(effect);
        return activationTypes.containsKey(type);
    }

    public List<EffectDataManager> effectsPlayerCanUpgrade(Player player) {
        Map<String, Integer> playerData = pluginMain.getDataManager().getPlayerEffectData(player.getUniqueId());
        return effects.values().stream()
                .filter(EffectDataManager::isEnabled)
                .filter(effect -> effect.getMaxLevel() - 1 > playerData.getOrDefault(effect.getId(), -1))
                .toList();
    }

    public boolean isMaxHPEnabled() {
        return isMaxHPEnabled;
    }

    public double getMaxHPValue() {
        return maxHPValue;
    }

    public double getMinHPValue() {
        return minHPValue;
    }

    public double getRefundCost(EffectDataManager effect, int level) {
        double upgradeCost = 0;
        for (int i = 0; i <= level; i++) {
            double cost = effect.getCostPerUpgrade(i);
            upgradeCost += cost;
        }
        return Math.floor(upgradeCost * effect.getRefundPercent());
    }

    public double getRefundCostFromTo(EffectDataManager effect, int from, int to) {
        return effect.getCostFromTo(to, from) * effect.getRefundPercent();
    }

    public boolean isDefaultShowScoreboardEnabled() {
        return isDefaultShowScoreboardEnabled;
    }

    public int getPeriodUpdateScoreboard() {
        return periodUpdateScoreboard;
    }

    // Permissions Commands
    public boolean canUse(CommandSender sender, String perm) {
        if (!permissionOverrides.containsKey(perm)) {
            plugin.getLogger().warning("Unknown permission key used in canUse(): " + perm);
        }
        if (permissionOverrides.getOrDefault(perm, false)) return true;
        return sender.hasPermission(PERM_PREFIX + perm) || sender.hasPermission(PERM_PREFIX + "admin");
    }

    public boolean isValidUUID(String str) {
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}