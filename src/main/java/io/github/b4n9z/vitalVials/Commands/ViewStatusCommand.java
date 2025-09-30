package io.github.b4n9z.vitalVials.Commands;

import io.github.b4n9z.vitalVials.Managers.EffectDataManager;
import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.UUID;

public class ViewStatusCommand implements CommandExecutor {
    private final VitalVials plugin;

    public ViewStatusCommand(VitalVials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && plugin.getConfigManager().canUse(player, "viewStatus")) {
            UUID uuidPlayer = player.getUniqueId();
            Map<String, Integer> playerEffects = plugin.getDataManager().getPlayerEffectData(uuidPlayer);
            for (Map.Entry<String, Integer> entry : playerEffects.entrySet()) {
                String effectKey = entry.getKey();
                EffectDataManager effectInfo = plugin.getConfigManager().getEffects().get(effectKey);
                if (!effectInfo.isEnabled()) continue;
                int level = entry.getValue();
                int cooldown = effectInfo.getCooldownPerUpgrade(level);
                int duration = effectInfo.getDurationPerUpgrade(level);
                String durationString = duration + "s";

                if (duration == -1) {
                    durationString = "infinite";
                }

                player.sendMessage(ChatColor.GREEN + "||===]>"+ChatColor.RED + effectKey + ChatColor.GREEN + "<[===||");
                player.sendMessage(ChatColor.WHITE + effectInfo.getLore());
                player.sendMessage(ChatColor.BLUE + "》" + ChatColor.WHITE + "Level : "+ ChatColor.LIGHT_PURPLE + (level+1));
                player.sendMessage(ChatColor.BLUE + "》" + ChatColor.WHITE + "Duration : "+ ChatColor.LIGHT_PURPLE + durationString);
                player.sendMessage(ChatColor.BLUE + "》" + ChatColor.WHITE + "Cooldown : "+ ChatColor.LIGHT_PURPLE + cooldown + "s");
                player.sendMessage(ChatColor.BLUE + "》" + ChatColor.WHITE + "Max Level Upgrade : " + ChatColor.LIGHT_PURPLE + effectInfo.getMaxLevel());
                player.sendMessage(ChatColor.RED + " (" + effectInfo.getPriceType() + " ◈ " + (effectInfo.isSaveEffectData() ? "Data Saved" : "Data Not Saved") + " ◈ " + (effectInfo.getRefundPercentage() < 0 ? "Cannot Refund" : effectInfo.getRefundPercentage() + "% Refund") + ")");
                player.sendMessage(ChatColor.WHITE + "[+]======[ACTIVATION]======[+]");
                if (plugin.getConfigManager().getEffects().get(effectKey).getAutoActivate()) player.sendMessage(ChatColor.BLUE + " ≫ " + ChatColor.AQUA + " Auto Activated : " + ChatColor.LIGHT_PURPLE + "true");
                if (plugin.getConfigManager().hasActivationTypes(effectInfo, "rightClick")) player.sendMessage(ChatColor.BLUE + " ≫ " + ChatColor.AQUA + " Applied to Yourself When Right Click Using: " + ChatColor.LIGHT_PURPLE + plugin.getConfigManager().getActivationTypes(effectInfo).get("rightClick"));
                if (plugin.getConfigManager().hasActivationTypes(effectInfo, "leftClick")) player.sendMessage(ChatColor.BLUE + " ≫ " + ChatColor.AQUA + " Applied to Yourself When Left Click Using: " + ChatColor.LIGHT_PURPLE + plugin.getConfigManager().getActivationTypes(effectInfo).get("leftClick"));
                if (plugin.getConfigManager().hasActivationTypes(effectInfo, "YouAreHittingEnemy")) player.sendMessage(ChatColor.BLUE + " ≫ " + ChatColor.AQUA + " Applied to Enemies When You Hit an Enemy Using : " + ChatColor.LIGHT_PURPLE + plugin.getConfigManager().getActivationTypes(effectInfo).get("YouAreHittingEnemy"));
                if (plugin.getConfigManager().hasActivationTypes(effectInfo, "enemyHitYou")) player.sendMessage(ChatColor.BLUE + " ≫ " + ChatColor.AQUA + " Applied to Enemy When Enemy Hits You Using : " + ChatColor.LIGHT_PURPLE + plugin.getConfigManager().getActivationTypes(effectInfo).get("enemyHitYou"));
                player.sendMessage(ChatColor.WHITE + "||==========================||");
                player.sendMessage("");
            }
            return true;
        }
        return true;
    }
}