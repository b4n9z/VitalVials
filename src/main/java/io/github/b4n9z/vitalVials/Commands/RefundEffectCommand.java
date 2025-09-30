package io.github.b4n9z.vitalVials.Commands;

import io.github.b4n9z.vitalVials.Managers.EffectDataManager;
import io.github.b4n9z.vitalVials.Managers.HealthManager;
import io.github.b4n9z.vitalVials.Managers.TransactionManager;
import io.github.b4n9z.vitalVials.VitalVials;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.UUID;

public class RefundEffectCommand implements CommandExecutor {
    private final VitalVials plugin;

    public RefundEffectCommand(VitalVials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && plugin.getConfigManager().canUse(player, "refundEffect")) {
            if (args.length < 2) {
                sender.sendMessage("§fUsage:§c /VitalVials§b refundEffect§f <effectName> [level]");
                return true;
            }

            TransactionManager.closeTransaction(player);

            UUID playerId = player.getUniqueId();
            Map<String, Integer> playerData = plugin.getDataManager().getPlayerEffectData(playerId);
            if (playerData.isEmpty()) {
                player.sendMessage(ChatColor.RED + "You don't have any active effects.");
                return true;
            }

            String effectName = args[1];
            EffectDataManager effectInfo = plugin.getConfigManager().getEffects().get(effectName);
            if (effectInfo == null) {
                player.sendMessage(ChatColor.WHITE + "Invalid effect name: " + ChatColor.RED + effectName);
                return true;
            }

            if (effectInfo.getRefundPercentage() < 0) {
                sender.sendMessage("§cRefund for " + effectName + " is disabled.");
                return true;
            }

            int level = playerData.getOrDefault(effectName, -1);
            if (level == -1) { // -1 = not have this effect
                player.sendMessage(ChatColor.RED + "You don't have this effect.");
                return true;
            }

            int newLevel;
            if (args.length == 3) {
                try {
                    int refundLevels = Integer.parseInt(args[2]);

                    if (refundLevels <= 0) { // refundLevels <= 0 mean they refund nothing
                        player.sendMessage(ChatColor.RED + "Refund level must be greater than 0.");
                        return true;
                    }

                    newLevel = level - refundLevels;

                    if (newLevel < 0) { // all level bellow 0 mean remove this effect
                        newLevel = -1;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid level number: " + args[2]);
                    return true;
                }
            } else {
                newLevel = level-1;
            }

            if (newLevel < -1) { // if somehow player refund more than their level
                player.sendMessage(ChatColor.RED + "Invalid level you want to refund: " + ChatColor.WHITE + (args.length >= 3 ? args[2] : "1") + " is is higher than your level!");
                return true;
            }

            confirmRefund(player, effectInfo, newLevel);
            return true;
        }
        return true;
    }

    private void confirmRefund(Player player, EffectDataManager effectInfo, Integer newLevel) {
        if (plugin.getConfigManager().canUse(player, "refundEffect")) {
            String transactionId = TransactionManager.generateTransactionId(player,"refundEffect");
            TransactionManager.openTransaction(plugin,player, transactionId);

            int level = plugin.getDataManager().getPlayerEffectLevel(player.getUniqueId(), effectInfo.getId());
            int levelRefund = level - newLevel;
            double refundAmount = plugin.getConfigManager().getRefundCostFromTo(effectInfo, level, newLevel);
            String healthType = effectInfo.getPriceType().equals("MAX_HEALTH") ? " Max" : "";
            String levelRefundText = newLevel == -1 ?
                    "§f all level back to§a 0§f(refunded§a " + levelRefund + "§f levels, will remove this effect entirely)" :
                    "§f back to level§a " + (newLevel+1) + "§f(§a" + (level+1) + " → "+levelRefund+"§f, refunded§a " + levelRefund + "§f levels)";
            TextComponent message = new TextComponent("§fAre§b you§f sure§b you§f want to refund§e " + effectInfo.getId() + levelRefundText + " for§b " + ChatColor.RED + (refundAmount/2) + healthType + " Hearts ❤ §f/§c " + refundAmount + healthType + " Health Points ♥§f? ");
            TextComponent yes = new TextComponent("[YES]");
            yes.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vitalvials confirmRefund " + player.getUniqueId() + " " + effectInfo.getId() + " " + transactionId + " " + newLevel));

            TextComponent no = new TextComponent("[NO]");
            no.setColor(net.md_5.bungee.api.ChatColor.RED);
            no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vitalvials cancelRefund " + transactionId));

            TextComponent newline = new TextComponent("\n");

            message.addExtra(newline);
            message.addExtra(yes);
            message.addExtra(" ");
            message.addExtra(no);

            player.spigot().sendMessage(message);
        }
    }

    public boolean confirmRefundCommand(CommandSender sender, String[] args) {
        if (!plugin.getConfigManager().canUse(sender, "refundEffect")) {
            sender.sendMessage("§fYou§c don't have permission§f to use this command.");
            return false;
        }
        if (args.length != 5) {
            sender.sendMessage("§fUsage:§c /VitalVials§b confirmRefund§f <playerUUID> <effectName> <transactionId> <newLevel>");
            return false;
        }
        UUID playerId = UUID.fromString(args[1]);
        Player player = plugin.getServer().getPlayer(playerId);
        if (player == null) {
            sender.sendMessage("§cPlayer is not online.");
            return true;
        }

        if (!(TransactionManager.isValidTransaction(player, args[3]))) {
            sender.sendMessage("§cThis confirmation has expired or is invalid. Please try again.");
            return false;
        }

        String effectName = (args[2]);
        int newLevel = Integer.parseInt(args[4]); // it completely new level

        EffectDataManager effectInfo = plugin.getConfigManager().getEffects().get(effectName);
        try {
            int level = plugin.getDataManager().getPlayerEffectLevel(playerId, effectName);
            if (level == -1) { // player don't have this effect
                player.sendMessage(ChatColor.RED + "You don't have this effect or this effect not save in player data.");
                return true;
            }
            if (newLevel == level) {
                player.sendMessage(ChatColor.RED + "You must refund at least 1 level.");
                return true;
            }
            if (newLevel > level) {
                player.sendMessage(ChatColor.RED + "Invalid target level: higher than current level.");
                return true;
            }
            if (newLevel < -1) { // player new level is lower than -1 will stop
                player.sendMessage(ChatColor.RED + "Invalid level you want to refund: " + ChatColor.WHITE + newLevel+1 + " is lower than 0!");
                return true;
            }
            int levelRefund = level - newLevel; // back calculate level refund
            double refundAmount = plugin.getConfigManager().getRefundCostFromTo(effectInfo, level, newLevel);
            String healthType = effectInfo.getPriceType().equals("MAX_HEALTH") ? " Max" : "";
            double currentHealth = HealthManager.getMaxHealth(player);
            double currentHealthPoints = HealthManager.getHealth(player);
            double maxHPSetting = plugin.getConfigManager().getMaxHPValue();
            double newMaxHealth = currentHealth + refundAmount;
            double newHealthPoints = currentHealthPoints + refundAmount;
            if (plugin.getConfigManager().isMaxHPEnabled() && (newMaxHealth > maxHPSetting || newHealthPoints > maxHPSetting || newHealthPoints > currentHealth)) {
                if (healthType.isEmpty() && newHealthPoints > currentHealth) {
                    player.sendMessage(ChatColor.RED + "You cannot refund this effect because your HP can't be higher than " + (currentHealth / 2) + " Hearts ❤ §f/§c " + currentHealth + " Health Points ♥§f!");
                    return true;
                } else if (!healthType.isEmpty() && newMaxHealth > maxHPSetting) {
                    player.sendMessage(ChatColor.RED + "You cannot refund this effect because your" + healthType + " HP can't be higher than " + (maxHPSetting / 2) + " Hearts ❤ §f/§c " + maxHPSetting + " Health Points ♥§f!");
                    return true;
                }
            }
            if (effectInfo.getPriceType().equals("MAX_HEALTH")) {
                HealthManager.setMaxHealth(currentHealth + refundAmount, player);
            } else {
                HealthManager.setHealth(currentHealthPoints + refundAmount, player);
            }

            if (newLevel == -1) { // if new level is -1, remove this effect entirely
                plugin.getDataManager().removePlayerEffect(playerId, effectName);
            } else { // if new level is not -1, update this effect level
                plugin.getDataManager().updatePlayerEffect(playerId, effectName, newLevel);
            }

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getEffectManager().applyAllEffects(player), 5L);

            // player refund message
            String strEnd = newLevel == -1 ?
                    "(refund "+(levelRefund)+" level, remove this effect entirely)" :
                    (" back to level " + ChatColor.LIGHT_PURPLE + (newLevel + 1) + "(" + (level+1) + " → " + (newLevel+1) + ", refunded "+(levelRefund)+" level)");
            player.sendMessage(ChatColor.GREEN + "You have refunded " + ChatColor.AQUA + effectName + ChatColor.WHITE + strEnd + ChatColor.WHITE + " for " + ChatColor.RED + (refundAmount/2) + healthType + " Hearts ❤ §f/§c " + refundAmount + healthType + " Health Points ♥§f!");
            plugin.sendColoredMessageToConsole("§b" + player.getName() + "§f has refunded " + ChatColor.AQUA + effectName + ChatColor.WHITE + strEnd + ChatColor.WHITE + " for " + ChatColor.RED + (refundAmount/2) + healthType + " Hearts ❤ §f/§c " + refundAmount + healthType + " Health Points ♥§f!");
            plugin.getCustomScoreboardManager().updateScoreboard(player);

            TransactionManager.closeTransaction(player);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Failed to refund effect: " + ChatColor.WHITE + e.getMessage());
        }
        return true;
    }

    public boolean cancelRefundCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§fUsage:§c /VitalVials§b cancelRefund§f <transactionId>");
            return false;
        }
        String transactionId = args[1];
        if (!transactionId.isEmpty() && TransactionManager.isValidTransaction(sender, transactionId)) {
            TransactionManager.closeTransaction(sender);
            sender.sendMessage("§bRefund Effect§f has been§c cancelled.");
        } else {
            sender.sendMessage("§cNo active transaction to cancel or invalid transaction ID.");
        }
        return true;
    }
}