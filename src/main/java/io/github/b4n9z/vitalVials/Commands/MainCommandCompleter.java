package io.github.b4n9z.vitalVials.Commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MainCommandCompleter implements TabCompleter {
    private final VitalVials plugin;

    public MainCommandCompleter(VitalVials plugin) {
        this.plugin = plugin;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();

        if (args.length == 1) {
            if (plugin.getConfigManager().canUse(sender, "reload")) {
                commands.add("reload");
            }
            if (plugin.getConfigManager().canUse(sender, "shop")) {
                commands.add("shop");
            }
            if (plugin.getConfigManager().canUse(sender, "buyEffect")) {
                commands.add("buyEffect");
            }
            if (plugin.getConfigManager().canUse(sender, "scoreboard")) {
                commands.add("scoreboard");
            }
            if (plugin.getConfigManager().canUse(sender, "viewStatus")) {
                commands.add("viewStatus");
            }
            if (plugin.getConfigManager().canUse(sender, "refundEffect")) {
                commands.add("refundEffect");
            }
            if (plugin.getConfigManager().canUse(sender, "removeData")) {
                commands.add("removeData");
            }
            for (String commandOption : commands) {
                if (commandOption.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(commandOption);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("refundEffect") || args[0].equalsIgnoreCase("buyEffect")) {
                completions.addAll(plugin.getConfigManager().getAllEffectsEnabled());
            }
            if (args[0].equalsIgnoreCase("scoreboard")) {
                completions.add("on");
                completions.add("off");
                completions.add("editShort");
            }
            if (args[0].equalsIgnoreCase("removeData")) {
                completions.add("allPlayer");
                for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                    if (offlinePlayer.isOnline()) {
                        continue;
                    }
                    completions.add(offlinePlayer.getUniqueId().toString());
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}
