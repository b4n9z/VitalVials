package io.github.b4n9z.vitalVials.Commands;

import io.github.b4n9z.vitalVials.Managers.EffectDataManager;
import io.github.b4n9z.vitalVials.Managers.HealthManager;
import io.github.b4n9z.vitalVials.Managers.TransactionManager;
import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopCommand implements CommandExecutor {
    private final VitalVials plugin;
    private final NamespacedKey shopEffectKey;
    private final NamespacedKey cancelBuyItemEffectKey;
    private final NamespacedKey cancelBuyItemTransactionKey;
    private final NamespacedKey confirmBuyItemEffectKey;
    private final NamespacedKey confirmBuyItemTransactionKey;
    private final NamespacedKey nextLevelItemEffectKey;
    private final NamespacedKey nextLevelItemTransactionKey;
    private final NamespacedKey prevLevelItemEffectKey;
    private final NamespacedKey prevLevelItemTransactionKey;

    public ShopCommand(VitalVials plugin) {
        this.plugin = plugin;
        this.shopEffectKey = new NamespacedKey(plugin, "vvShopItem_b4n9z");
        this.cancelBuyItemEffectKey = new NamespacedKey(plugin, "vvCancelBuyItemEffect_b4n9z");
        this.cancelBuyItemTransactionKey = new NamespacedKey(plugin, "vvCancelBuyItemTransaction_b4n9z");
        this.confirmBuyItemEffectKey = new NamespacedKey(plugin, "vvConfirmBuyItemEffect_b4n9z");
        this.confirmBuyItemTransactionKey = new NamespacedKey(plugin, "vvConfirmBuyItemTransaction_b4n9z");
        this.nextLevelItemEffectKey = new NamespacedKey(plugin, "vvNextLevelItemEffect_b4n9z");
        this.nextLevelItemTransactionKey = new NamespacedKey(plugin, "vvNextLevelItemTransaction_b4n9z");
        this.prevLevelItemEffectKey = new NamespacedKey(plugin, "vvPrevLevelItemEffect_b4n9z");
        this.prevLevelItemTransactionKey = new NamespacedKey(plugin, "vvPrevLevelItemTransaction_b4n9z");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (!plugin.getConfigManager().canUse(player, "shop")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        openShopGUI(player);
        return true;
    }

    private void openShopGUI(Player player) {
        // Get all effects the player can upgrade
        List<EffectDataManager> upgradeableEffects = plugin.getConfigManager().effectsPlayerCanUpgrade(player);

        // Create inventory with appropriate size (round up to nearest multiple of 9)
        int inventorySize = (int) (Math.ceil((upgradeableEffects.size() + 8) / 9.0) * 9);
        Inventory shop = Bukkit.createInventory(null, Math.min(54, Math.max(9, inventorySize)),
                ChatColor.BLACK + "VitalVials Shop");

        // Add effects to the shop
        for (EffectDataManager effect : upgradeableEffects) {
            int currentLevel = plugin.getDataManager().getPlayerEffectLevel(player.getUniqueId(), effect.getId());
            int nextLevel = currentLevel + 1;
            ItemStack item = createShopItem(effect, currentLevel, nextLevel);
            if (item != null) {
                shop.addItem(item);
            }
        }

        // Add glass panes to fill empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }

        for (int i = 0; i < shop.getSize(); i++) {
            if (shop.getItem(i) == null) {
                shop.setItem(i, filler);
            }
        }

        player.openInventory(shop);
    }

    private void openBuyGUI(Player player, EffectDataManager effectInfo, int nextLevel) {
        if (plugin.getConfigManager().canUse(player, "shop")) {
            if (!effectInfo.isEnabled()) return;

            String transactionId = TransactionManager.generateTransactionId(player,"shopEffect");
            TransactionManager.openTransaction(plugin,player, transactionId);

            // Create inventory
            Inventory shop = Bukkit.createInventory(null, 27, ChatColor.BLACK + "VitalVials Confirm Buy");

            int currentLevel = plugin.getDataManager().getPlayerEffectLevel(player.getUniqueId(), effectInfo.getId());

            // Add effects to the shop
            ItemStack item = createShopItem(effectInfo, currentLevel, nextLevel);
            if (item != null) {
                shop.setItem(13, item);
            }

            ItemStack nextLevelItem = new ItemStack(Material.FIRE_CHARGE);
            ItemMeta nextLevelItemMeta = nextLevelItem.getItemMeta();
            if (nextLevelItemMeta != null) {
                nextLevelItemMeta.setDisplayName("§aNext Level");
                nextLevelItemMeta.setLore(List.of("§aClick to upgrade to the next level."));
                nextLevelItemMeta.getPersistentDataContainer().set(nextLevelItemEffectKey, PersistentDataType.STRING, effectInfo.getId());
                nextLevelItemMeta.getPersistentDataContainer().set(nextLevelItemTransactionKey, PersistentDataType.STRING, transactionId);
                NamespacedKey nextLevelItemNextLevelKey = new NamespacedKey(plugin, effectInfo.getId()+"nextLevel"+player.getUniqueId());
                nextLevelItemMeta.getPersistentDataContainer().set(nextLevelItemNextLevelKey, PersistentDataType.INTEGER, nextLevel+1);
                nextLevelItemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DYE, ItemFlag.HIDE_PLACED_ON);
                nextLevelItem.setItemMeta(nextLevelItemMeta);
            }

            ItemStack prevLevelItem = new ItemStack(Material.SNOWBALL);
            ItemMeta prevLevelItemMeta = prevLevelItem.getItemMeta();
            if (prevLevelItemMeta != null) {
                prevLevelItemMeta.setDisplayName("§cPrevious Level");
                prevLevelItemMeta.setLore(List.of("§cClick to downgrade to the previous level."));
                prevLevelItemMeta.getPersistentDataContainer().set(prevLevelItemEffectKey, PersistentDataType.STRING, effectInfo.getId());
                prevLevelItemMeta.getPersistentDataContainer().set(prevLevelItemTransactionKey, PersistentDataType.STRING, transactionId);
                NamespacedKey prevLevelItemNextLevelKey = new NamespacedKey(plugin, effectInfo.getId()+"prevLevel"+player.getUniqueId());
                prevLevelItemMeta.getPersistentDataContainer().set(prevLevelItemNextLevelKey, PersistentDataType.INTEGER, nextLevel-1);
                prevLevelItemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DYE, ItemFlag.HIDE_PLACED_ON);
                prevLevelItem.setItemMeta(prevLevelItemMeta);
            }

            ItemStack cancelItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta cancelItemMeta = cancelItem.getItemMeta();
            if (cancelItemMeta != null) {
                cancelItemMeta.setDisplayName("§cCancel");
                cancelItemMeta.setLore(List.of("§cClick to cancel the transaction."));
                cancelItemMeta.getPersistentDataContainer().set(cancelBuyItemEffectKey, PersistentDataType.STRING, effectInfo.getId());
                cancelItemMeta.getPersistentDataContainer().set(cancelBuyItemTransactionKey, PersistentDataType.STRING, transactionId);
                cancelItem.setItemMeta(cancelItemMeta);
            }

            ItemStack confirmItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta confirmItemMeta = confirmItem.getItemMeta();
            if (confirmItemMeta != null) {
                confirmItemMeta.setDisplayName("§aConfirm Buy");
                confirmItemMeta.setLore(List.of("§aClick to confirm the transaction."));
                confirmItemMeta.getPersistentDataContainer().set(confirmBuyItemEffectKey, PersistentDataType.STRING, effectInfo.getId());
                confirmItemMeta.getPersistentDataContainer().set(confirmBuyItemTransactionKey, PersistentDataType.STRING, transactionId);
                NamespacedKey confirmBuyItemNextLevelKey = new NamespacedKey(plugin, effectInfo.getId()+"nextLevel"+player.getUniqueId());
                confirmItemMeta.getPersistentDataContainer().set(confirmBuyItemNextLevelKey, PersistentDataType.INTEGER, nextLevel);
                confirmItem.setItemMeta(confirmItemMeta);
            }

            if (nextLevel+1 <= (effectInfo.getMaxLevel()-1)) {
                shop.setItem(15, nextLevelItem);
            }

            if (currentLevel < nextLevel-1) { // error in here
                shop.setItem(11, prevLevelItem);
            }

            shop.setItem(21, cancelItem);
            shop.setItem(23, confirmItem);

            // Add glass panes to fill empty slots
            ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(" ");
                filler.setItemMeta(fillerMeta);
            }

            for (int i = 0; i < shop.getSize(); i++) {
                if (shop.getItem(i) == null) {
                    shop.setItem(i, filler);
                }
            }

            player.openInventory(shop);
        }
    }

    private ItemStack createShopItem(EffectDataManager effect, int currentLevel, int nextLevel) {
        // Create potion item
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return null;

        if (nextLevel > (effect.getMaxLevel()-1)){
            return null;
        }

        // Set display name and lore
        int duration = effect.getDurationPerUpgrade(nextLevel);
        double cost = effect.getCostFromTo(currentLevel, nextLevel);

        // Set potion type based on effect
        try {
            PotionEffect potionEffect = new PotionEffect(effect.getPotionEffectType(), (duration == -1 ? duration : duration*20), nextLevel, !effect.hasParticles(), effect.hasParticles());
            meta.addCustomEffect(potionEffect, true);
        } catch (IllegalArgumentException e) {
            // If effect doesn't match a potion type, use a default one
            meta.setBasePotionType(PotionType.STRENGTH);
        }

        meta.setColor(effect.getPotionEffectType().getColor());

        String displayName = ChatColor.translateAlternateColorCodes('&', effect.getName());
        String healthType = effect.getPriceType().equals("MAX_HEALTH") ? " Max" : "";
        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        lore.add(effect.getLore());
        lore.add(ChatColor.YELLOW + "》" + ChatColor.WHITE + "Price : " + ChatColor.RED + (cost/2) + healthType + " Hearts ❤ / " + cost + healthType + " Health Points ♥");
        lore.add(ChatColor.YELLOW + "》" + ChatColor.WHITE + "Cooldown : " + ChatColor.YELLOW + effect.getCooldownPerUpgrade(nextLevel) + "s");
        lore.add(ChatColor.YELLOW + "》" + ChatColor.WHITE + "Max Level Upgrade : " + ChatColor.YELLOW + effect.getMaxLevel());
        lore.add(ChatColor.RED + " (" + effect.getPriceType() + " ◈ " + (effect.isSaveEffectData() ? "Data Saved" : "Data Not Saved") + " ◈ " + (effect.getRefundPercentage() < 0 ? "Cannot Refund" : effect.getRefundPercentage() + "% Refund") + ")");
        lore.add("―――――――――");
        if (effect.getAutoActivate()) lore.add(ChatColor.BLUE + " ≫ " + ChatColor.AQUA + " Auto Activated : " + ChatColor.LIGHT_PURPLE + "true");
        if (plugin.getConfigManager().hasActivationTypes(effect, "rightClick")) lore.add(ChatColor.BLUE + " ≫ " + ChatColor.AQUA + " Applied to Yourself When Right Click Using: " + ChatColor.LIGHT_PURPLE + plugin.getConfigManager().getActivationTypes(effect).get("rightClick"));
        if (plugin.getConfigManager().hasActivationTypes(effect, "leftClick")) lore.add(ChatColor.BLUE + " ≫ " + ChatColor.AQUA + " Applied to Yourself When Left Click Using: " + ChatColor.LIGHT_PURPLE + plugin.getConfigManager().getActivationTypes(effect).get("leftClick"));
        if (plugin.getConfigManager().hasActivationTypes(effect, "YouAreHittingEnemy")) lore.add(ChatColor.BLUE + " ≫ " + ChatColor.AQUA + " Applied to Enemies When You Hit an Enemy Using : " + ChatColor.LIGHT_PURPLE + plugin.getConfigManager().getActivationTypes(effect).get("YouAreHittingEnemy"));
        if (plugin.getConfigManager().hasActivationTypes(effect, "enemyHitYou")) lore.add(ChatColor.BLUE + " ≫ " + ChatColor.AQUA + " Applied to Enemy When Enemy Hits You Using : " + ChatColor.LIGHT_PURPLE + plugin.getConfigManager().getActivationTypes(effect).get("enemyHitYou"));
        lore.add("―――――――――");
        lore.add(ChatColor.YELLOW + "Click to purchase!");

        meta.setLore(lore);

        meta.getPersistentDataContainer().set(shopEffectKey, PersistentDataType.STRING, effect.getId());
        item.setItemMeta(meta);
        return item;
    }

    public void handleShopClick(Player player, ItemStack clickedItem) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        Map<String, Integer> playerEffects = plugin.getDataManager().getPlayerEffectData(player.getUniqueId());

        String effectId = meta.getPersistentDataContainer().get(shopEffectKey, PersistentDataType.STRING);

        if (effectId != null) {
            player.closeInventory();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> openBuyGUI(player, plugin.getConfigManager().getEffects().get(effectId), (playerEffects.getOrDefault(effectId,-1)+1)), 5L);
        }

        String nextLevelTransactionId = meta.getPersistentDataContainer().get(nextLevelItemTransactionKey, PersistentDataType.STRING);
        String nextLevelEffectId = meta.getPersistentDataContainer().get(nextLevelItemEffectKey, PersistentDataType.STRING);

        if (nextLevelEffectId != null) {
            EffectDataManager effectInfo = plugin.getConfigManager().getEffects().get(nextLevelEffectId);
            int playerLevel = playerEffects.getOrDefault(nextLevelEffectId,-1);
            NamespacedKey nextLevelItemNextLevelKey = new NamespacedKey(plugin, effectInfo.getId()+"nextLevel"+player.getUniqueId());
            Integer nextLevel = meta.getPersistentDataContainer().get(nextLevelItemNextLevelKey, PersistentDataType.INTEGER);
            if (nextLevel != null && !(nextLevel > (effectInfo.getMaxLevel()-1)) && !(nextLevel <= playerLevel)) {
                if (nextLevelTransactionId != null && TransactionManager.isValidTransaction(player, nextLevelTransactionId)) {
//                    player.closeInventory();
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> openBuyGUI(player, plugin.getConfigManager().getEffects().get(nextLevelEffectId), nextLevel), 5L);
                }
            }
        }

        String prevLevelTransactionId = meta.getPersistentDataContainer().get(prevLevelItemTransactionKey, PersistentDataType.STRING);
        String prevLevelEffectId = meta.getPersistentDataContainer().get(prevLevelItemEffectKey, PersistentDataType.STRING);

        if (prevLevelEffectId != null) {
            EffectDataManager effectInfo = plugin.getConfigManager().getEffects().get(prevLevelEffectId);
            int playerLevel = playerEffects.getOrDefault(prevLevelEffectId,-1);
            NamespacedKey prevLevelItemNextLevelKey = new NamespacedKey(plugin, effectInfo.getId()+"prevLevel"+player.getUniqueId());
            Integer prevLevel = meta.getPersistentDataContainer().get(prevLevelItemNextLevelKey, PersistentDataType.INTEGER);
            if (prevLevel != null && !(prevLevel <= playerLevel) && !(prevLevel > (effectInfo.getMaxLevel()-1))) {
                if (prevLevelTransactionId != null && TransactionManager.isValidTransaction(player, prevLevelTransactionId)) {
//                    player.closeInventory();
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> openBuyGUI(player, plugin.getConfigManager().getEffects().get(prevLevelEffectId), prevLevel), 5L);
                }
            }
        }

        String cancelTransactionId = meta.getPersistentDataContainer().get(cancelBuyItemTransactionKey, PersistentDataType.STRING);
        String cancelEffectId = meta.getPersistentDataContainer().get(cancelBuyItemEffectKey, PersistentDataType.STRING);

        if (cancelEffectId != null) {
            if (cancelTransactionId != null && TransactionManager.isValidTransaction(player, cancelTransactionId)) {
                TransactionManager.closeTransaction(player);
                player.sendMessage("§bBuy Effect§f has been§c cancelled.");
            } else {
                player.sendMessage("§cNo active transaction to cancel or invalid transaction ID.");
            }
            runShopCommandLater(player);
        }

        String confirmTransactionId = meta.getPersistentDataContainer().get(confirmBuyItemTransactionKey, PersistentDataType.STRING);
        String confirmEffectId = meta.getPersistentDataContainer().get(confirmBuyItemEffectKey, PersistentDataType.STRING);

        if (confirmEffectId != null) {
            EffectDataManager effectInfo = plugin.getConfigManager().getEffects().get(confirmEffectId);
            int playerLevel = playerEffects.getOrDefault(confirmEffectId,-1);
            NamespacedKey confirmBuyItemNextLevelKey = new NamespacedKey(plugin, effectInfo.getId()+"nextLevel"+player.getUniqueId());
            Integer nextLevel = meta.getPersistentDataContainer().get(confirmBuyItemNextLevelKey, PersistentDataType.INTEGER);
            if (nextLevel != null && nextLevel > playerLevel && nextLevel <= (effectInfo.getMaxLevel()-1)) {
                if (confirmTransactionId != null && TransactionManager.isValidTransaction(player, confirmTransactionId)) {
                    confirmBuy(player, confirmEffectId, confirmTransactionId, nextLevel);
                }
            }
        }
    }

    private void runShopCommandLater(Player player) {
        player.closeInventory();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                player.performCommand("vitalvials shop");
            } catch (Exception ignored) {
            }
        }, 5L);
    }

    public void confirmBuy(Player player, String effectId, String transactionId, int nextLevel) {
        if (!plugin.getConfigManager().canUse(player, "shop")) {
            player.sendMessage("§fYou§c don't have permission§f to use this command.");
            return;
        }

        if (!(TransactionManager.isValidTransaction(player, transactionId))) {
            player.sendMessage("§cThis confirmation has expired or is invalid. Please try again.");
            runShopCommandLater(player);
            return;
        }

        UUID playerId = player.getUniqueId();
        EffectDataManager effectInfo = plugin.getConfigManager().getEffects().get(effectId);
        Map<String, Integer> playerEffects = plugin.getDataManager().getPlayerEffectData(playerId);
        try {
            if (effectInfo == null || !effectInfo.isEnabled()) {
                player.sendMessage(ChatColor.WHITE + "Invalid effect name: " + ChatColor.RED + effectId);
                runShopCommandLater(player);
                return;
            }
            int price = effectInfo.getCostFromTo(playerEffects.getOrDefault(effectId,-1), nextLevel);
            String healthType = effectInfo.getPriceType().equals("MAX_HEALTH") ? " Max" : "";
            double currentHealth = HealthManager.getHealth(player);
            double currentMaxHealth = HealthManager.getMaxHealth(player);
            double newHealth = currentHealth - price;
            double newMaxHealth = currentMaxHealth - price;
            if ((effectInfo.getPriceType().equals("MAX_HEALTH") && (currentMaxHealth < price || newMaxHealth <= plugin.getConfigManager().getMinHPValue())) || (!effectInfo.getPriceType().equals("MAX_HEALTH") && (currentHealth < price || newHealth <= plugin.getConfigManager().getMinHPValue()))) {
                player.sendMessage(ChatColor.RED + "You don't have enough"+healthType+" health to buy/upgrade " + ChatColor.AQUA + effectId + ChatColor.WHITE + " for " + ChatColor.RED + (price/2) + healthType + " Hearts ❤ §f/§c " + price + healthType + " Health Points ♥§f!");
                runShopCommandLater(player);
                return;
            }
            if (nextLevel > (effectInfo.getMaxLevel()-1)) {
                player.sendMessage(ChatColor.RED + "You have reached the maximum level for this effect!");
                runShopCommandLater(player);
                return;
            }
            playerEffects.put(effectId, nextLevel);

            if (effectInfo.getPriceType().equals("MAX_HEALTH")) {
                HealthManager.setMaxHealth(newMaxHealth, player);
            } else {
                HealthManager.setHealth(newHealth, player);
            }

            if (effectInfo.isSaveEffectData()) {
                plugin.getDataManager().savePlayerEffectData(playerId, playerEffects);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getEffectManager().applyAllEffects(player), 5L);
            } else {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getEffectManager().applyEffect(player, effectId, nextLevel), 5L);
            }

            plugin.getCustomScoreboardManager().updateScoreboard(player);

            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "You bought/upgraded " + ChatColor.AQUA + effectId + ChatColor.WHITE + " for " + ChatColor.RED + (price/2) + healthType + " Hearts ❤ §f/§c " + price + healthType + " Health Points ♥§f!");
            plugin.sendColoredMessageToConsole("§b" + player.getName() + "§f has bought/upgraded " + ChatColor.AQUA + effectId + ChatColor.WHITE + " for " + ChatColor.RED + (price/2) + healthType + " Hearts ❤ §f/§c " + price + healthType + " Health Points ♥§f!");

            TransactionManager.closeTransaction(player);
            runShopCommandLater(player);
        } catch (Exception e) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "Failed to buy/upgrade effect: " + ChatColor.WHITE + e.getMessage());
            runShopCommandLater(player);
        }
    }
}