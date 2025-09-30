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

public class BuyEffectCommand implements CommandExecutor {
    private final VitalVials plugin;

    public BuyEffectCommand(VitalVials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((sender instanceof Player player) && plugin.getConfigManager().canUse(player, "buyEffect")) {
            if (args.length < 2) {
                sender.sendMessage("§fUsage:§c /VitalVials§b buyEffect§f <effectName> [level]");
                return true;
            }

            TransactionManager.closeTransaction(player);

            String effectName = args[1];
            EffectDataManager effectInfo = plugin.getConfigManager().getEffects().get(effectName);
            if (effectInfo == null || !effectInfo.isEnabled()) {
                player.sendMessage(ChatColor.WHITE + "Invalid effect name: " + ChatColor.RED + effectName);
                return true;
            }

            //current level
            int currentLevel = plugin.getDataManager().getPlayerEffectLevel(player.getUniqueId(), effectName);

            //target level
            int targetLevel;
            if (args.length == 3) {
                try {
                    targetLevel = Integer.parseInt(args[2])-1;
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid level number: " + args[2]);
                    return true;
                }
            } else {
                targetLevel = currentLevel + 1; // default if not specified
            }

            //validate target level
            if (targetLevel <= currentLevel) {
                player.sendMessage(ChatColor.RED + "You already have level " + currentLevel+1 + " or higher for this effect!");
                return true;
            }
            if (targetLevel > (effectInfo.getMaxLevel()-1)) {
                player.sendMessage(ChatColor.RED + "The maximum level for this effect is " + effectInfo.getMaxLevel() + "!");
                return true;
            }

            confirmBuy(player, effectInfo, targetLevel);
            return true;
        }
        return true;
    }

    private void confirmBuy(Player player, EffectDataManager effectInfo, int targetLevel) {
        if (plugin.getConfigManager().canUse(player, "buyEffect")) {
            if (!effectInfo.isEnabled()) return;

            String transactionId = TransactionManager.generateTransactionId(player,"buyEffect");
            TransactionManager.openTransaction(plugin,player, transactionId);

            String effectKey = effectInfo.getId();
            int currentLevel = plugin.getDataManager().getPlayerEffectLevel(player.getUniqueId(), effectKey);
            int price = effectInfo.getCostFromTo(currentLevel, targetLevel);
            String healthType = effectInfo.getPriceType().equals("MAX_HEALTH") ? " Max" : "";
            int cooldown = effectInfo.getCooldownPerUpgrade(targetLevel);
            int duration = effectInfo.getDurationPerUpgrade(targetLevel);
            String durationString = (duration == -1) ? "infinite" : duration + "s";

            player.sendMessage(ChatColor.GREEN + "||===]>"+ChatColor.RED + effectInfo.getName() + ChatColor.GREEN + "<[===||");
            player.sendMessage(ChatColor.WHITE + effectInfo.getLore());
            player.sendMessage(ChatColor.BLUE + "》" + ChatColor.WHITE + "Level : "+ ChatColor.LIGHT_PURPLE + (targetLevel+1));
            player.sendMessage(ChatColor.BLUE + "》" + ChatColor.WHITE + "Price : "+ ChatColor.LIGHT_PURPLE + (price/2) + healthType + " Hearts ❤ §f/§c " + price + healthType + " Health Points ♥");
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

            TextComponent message = new TextComponent("§fAre§b you§f sure§b you§f want to buy/upgrade§e " + effectKey + " §f to level§b " + ChatColor.RED + (targetLevel+1) + ChatColor.WHITE + " §f for§b " + ChatColor.RED + (price/2) + healthType + " Hearts ❤ §f/§c " + price + healthType + " Health Points ♥§f? ");
            TextComponent yes = new TextComponent("[YES]");
            yes.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vitalvials confirmBuy " + player.getUniqueId() + " " + effectKey + " " + transactionId + " " + targetLevel));

            TextComponent no = new TextComponent("[NO]");
            no.setColor(net.md_5.bungee.api.ChatColor.RED);
            no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vitalvials cancelBuy " + transactionId));

            TextComponent newline = new TextComponent("\n");

            message.addExtra(newline);
            message.addExtra(yes);
            message.addExtra(" - ");
            message.addExtra(no);

            player.spigot().sendMessage(message);
        }
    }

    public boolean confirmBuyCommand(CommandSender sender, String[] args) {
        if (!plugin.getConfigManager().canUse(sender, "buyEffect")) {
            sender.sendMessage("§fYou§c don't have permission§f to use this command.");
            return false;
        }
        if (args.length != 5) {
            sender.sendMessage("§fUsage:§c /VitalVials§b confirmBuy§f <playerUUID> <effectName> <transactionId> <targetLevel>");
            return false;
        }

        UUID playerId = UUID.fromString(args[1]);
        Player player = plugin.getServer().getPlayer(playerId);
        assert player != null;

        if (!(TransactionManager.isValidTransaction(player, args[3]))) {
            sender.sendMessage("§cThis confirmation has expired or is invalid. Please try again.");
            return false;
        }

        String effectName = (args[2]);
        int targetLevel = Integer.parseInt(args[4]);

        EffectDataManager effectInfo = plugin.getConfigManager().getEffects().get(effectName);
        Map<String, Integer> playerEffects = plugin.getDataManager().getPlayerEffectData(playerId);
        try {
            if (effectInfo == null || !effectInfo.isEnabled()) {
                player.sendMessage(ChatColor.WHITE + "Invalid effect name: " + ChatColor.RED + effectName);
                return true;
            }
            int currentLevel = plugin.getDataManager().getPlayerEffectLevel(playerId, effectName);
            if (targetLevel <= currentLevel) {
                player.sendMessage(ChatColor.RED + "You already have this level or higher!");
                return true;
            }

            int price = effectInfo.getCostFromTo(currentLevel, targetLevel);
            String healthType = effectInfo.getPriceType().equals("MAX_HEALTH") ? " Max" : "";
            double currentHealth = HealthManager.getHealth(player);
            double currentMaxHealth = HealthManager.getMaxHealth(player);
            double newHealth = currentHealth - price;
            double newMaxHealth = currentMaxHealth - price;
            if ((effectInfo.getPriceType().equals("MAX_HEALTH") && (currentMaxHealth < price || newMaxHealth <= plugin.getConfigManager().getMinHPValue())) || (!effectInfo.getPriceType().equals("MAX_HEALTH") && (currentHealth < price || newHealth <= plugin.getConfigManager().getMinHPValue()))) {
                player.sendMessage(ChatColor.RED + "You don't have enough"+healthType+" health to buy/upgrade " + ChatColor.AQUA + effectName + ChatColor.WHITE + " for " + ChatColor.RED + (price/2) + healthType + " Hearts ❤ §f/§c " + price + healthType + " Health Points ♥§f!");
                return true;
            }
            if (targetLevel > (effectInfo.getMaxLevel()-1)) {
                player.sendMessage(ChatColor.RED + "You have reached the maximum level for this effect!");
                return true;
            }
            playerEffects.put(effectName, targetLevel);

            if (effectInfo.getPriceType().equals("MAX_HEALTH")) {
                HealthManager.setMaxHealth(newMaxHealth, player);
            } else {
                HealthManager.setHealth(newHealth, player);
            }

            if (effectInfo.isSaveEffectData()) {
                plugin.getDataManager().savePlayerEffectData(playerId, playerEffects);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getEffectManager().applyAllEffects(player), 5L);
            } else {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getEffectManager().applyEffect(player, effectName, targetLevel), 5L);
            }

            player.sendMessage(ChatColor.GREEN + "You bought/upgraded " + ChatColor.AQUA + effectName + ChatColor.WHITE + " to level " + ChatColor.LIGHT_PURPLE + (targetLevel+1) + ChatColor.WHITE + " for " + ChatColor.RED + (price/2) + healthType + " Hearts ❤ §f/§c " + price + healthType + " Health Points ♥§f!");
            plugin.sendColoredMessageToConsole("§b" + player.getName() + "§f has bought/upgraded " + ChatColor.AQUA + effectName + ChatColor.WHITE + " to level " + ChatColor.LIGHT_PURPLE + (targetLevel+1) + ChatColor.WHITE + " for " + ChatColor.RED + (price/2) + healthType + " Hearts ❤ §f/§c " + price + healthType + " Health Points ♥§f!");

            TransactionManager.closeTransaction(player);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Failed to buy/upgrade effect: " + ChatColor.WHITE + e.getMessage());
        }
        return true;
    }

    public boolean cancelBuyCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§fUsage:§c /VitalVials§b cancelBuy§f <transactionId>");
            return false;
        }
        String transactionId = args[1];
        if (!transactionId.isEmpty() && TransactionManager.isValidTransaction(sender, transactionId)) {
            TransactionManager.closeTransaction(sender);
            sender.sendMessage("§bBuy Effect§f has been§c cancelled.");
        } else {
            sender.sendMessage("§cNo active transaction to cancel or invalid transaction ID.");
        }
        return true;
    }
}