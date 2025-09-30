package io.github.b4n9z.vitalVials;

import io.github.b4n9z.vitalVials.bStats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.b4n9z.vitalVials.Managers.*;
import io.github.b4n9z.vitalVials.Listeners.*;
import io.github.b4n9z.vitalVials.Commands.*;

import java.util.Objects;

public final class VitalVials extends JavaPlugin {
    private ConfigManager configManager;
    private DataManager dataManager;
    private EffectManager effectManager;
    private CustomScoreboardManager customScoreboardManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        loadMetrics(27411);
        loadConfigManager();
        loadDataManager();
        loadEffectManager();
        loadCustomScoreboardManager();

        registerEvents();
        registerCommands();

        getLogger().info("VitalVials has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("VitalVials has been disabled!");
    }

    public void loadMetrics(int pluginId) {
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));
    }

    public void loadConfigManager() {
        configManager = new ConfigManager(this);
    }
    public ConfigManager getConfigManager() {
        if (configManager == null) {
            throw new IllegalStateException("ConfigManager not initialized");
        }
        return configManager;
    }

    public void loadDataManager() {
        dataManager = new DataManager(this);
    }
    public DataManager getDataManager() {
        if (dataManager == null) {
            throw new IllegalStateException("DataManager not initialized");
        }
        return dataManager;
    }

    public void loadEffectManager() {
        effectManager = new EffectManager(this);
    }
    public EffectManager getEffectManager() {
        if (effectManager == null) {
            throw new IllegalStateException("EffectManager not initialized");
        }
        return effectManager;
    }

    public void loadCustomScoreboardManager() {
        customScoreboardManager = new CustomScoreboardManager(this);
    }
    public CustomScoreboardManager getCustomScoreboardManager() {
        if (customScoreboardManager == null) {
            throw new IllegalStateException("CustomScoreboardManager not initialized");
        }
        return customScoreboardManager;
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldChangeListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDrinkMilkListener(this), this);
        getServer().getPluginManager().registerEvents(new TotemListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
        getServer().getPluginManager().registerEvents(new ClickItemListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryGUIClickListener(this), this);
    }

    private void registerCommands() {
        // Register Command
        CommandExecutor mainCommand = new MainCommand(this);
        Objects.requireNonNull(this.getCommand("VitalVials")).setExecutor(mainCommand);
        Objects.requireNonNull(this.getCommand("vv")).setExecutor(mainCommand);
        // Register Completer
        Objects.requireNonNull(this.getCommand("VitalVials")).setTabCompleter(new MainCommandCompleter(this));
        Objects.requireNonNull(this.getCommand("vv")).setTabCompleter(new MainCommandCompleter(this));
    }

    public void sendColoredMessageToConsole(String message) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        console.sendMessage(message);
    }
}