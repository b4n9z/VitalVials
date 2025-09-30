package io.github.b4n9z.vitalVials.Commands;

import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadPluginCommand implements CommandExecutor {
    private final VitalVials plugin;

    public ReloadPluginCommand(VitalVials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player){
            if (plugin.getConfigManager().canUse(player, "reload")) {
                plugin.reloadConfig();
                plugin.loadConfigManager();
                player.sendMessage("§fPlugin reloaded§a successfully.");
            } else {
                player.sendMessage("§fYou§c don't have permission§f to use this command.");
            }
        } else {
            plugin.reloadConfig();
            plugin.loadConfigManager();
            sender.sendMessage("§fPlugin reloaded§a successfully.");
        }
        return true;
    }
}

