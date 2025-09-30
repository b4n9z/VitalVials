package io.github.b4n9z.vitalVials.Commands;

import io.github.b4n9z.vitalVials.Managers.EffectDataManager;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoreboardCommand implements CommandExecutor {
    private final VitalVials plugin;
    private static final String TITLE = ChatColor.BLACK + "VitalVials Scoreboard Sorting";
    private final NamespacedKey itemKey;

    public ScoreboardCommand(VitalVials plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "vvShortItem_b4n9z");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§fCan't execute this command as console.");
            return true;
        }

        if (!plugin.getConfigManager().canUse(player, "scoreboard")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§fUsage:§c /VitalVials§b scoreboard§f <on|off|editShort>");
            return true;
        }

        String sub = args[1].toLowerCase();
        if (args.length == 2) {
            if (sub.equals("on") || sub.equals("off")) {
                boolean status = sub.equals("on");
                plugin.getDataManager().setScoreboardEnabled(player.getUniqueId(), status);
                if (plugin.getDataManager().isScoreboardEnabled(player.getUniqueId())) {
                    plugin.getCustomScoreboardManager().createScoreboard(player);
                }
                plugin.getCustomScoreboardManager().updateScoreboard(player);
                player.sendMessage(ChatColor.WHITE + "Scoreboard " + (status ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                return true;
            }

            if (sub.equalsIgnoreCase("editShort")) {
                openShortGUI(player);
                return true;
            }
        }

        player.sendMessage(ChatColor.WHITE + "Unknown subcommand.");
        return true;
    }

    private void openShortGUI(Player player) {
        if (!plugin.getConfigManager().canUse(player, "scoreboard")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to edit scoreboard order.");
            return;
        }

        String transactionId = TransactionManager.generateTransactionId(player,"shortScoreboardEffect");
        TransactionManager.openTransaction(plugin,player, transactionId);

        // Get all effects the player have
        List<String> order = plugin.getDataManager().getScoreboardOrder(player.getUniqueId());
        Map<String, Integer> playerData = plugin.getDataManager().getPlayerEffectData(player.getUniqueId());
        if (playerData == null) playerData = Map.of();

        List<EffectDataManager> effects = new ArrayList<>();
        for (String effectName : order) {
            if (!playerData.containsKey(effectName)) continue;

            EffectDataManager ed = plugin.getConfigManager().getEffects().get(effectName);
            if (ed == null || !ed.isEnabled() ||
                    ed.getAutoActivate() ||
                    ed.getCooldownPerUpgrade(playerData.get(effectName)) <= 0) {
                continue;
            }
            effects.add(ed);
        }

        // slots needed = effects + 1 slot for Save
        int slotsNeeded = effects.size() + 1;
        int rows = (int) Math.ceil(slotsNeeded / 9.0);
        rows = Math.max(1, Math.min(rows, 6)); // clamp 1..6 rows
        int inventorySize = rows * 9;

        Inventory inv = Bukkit.createInventory(null, inventorySize, TITLE);

        // put effect items into slots 0 . . n-1
        int idx = 0;
        for (EffectDataManager ed : effects) {
            ItemStack item = createShortItem(ed, player);
            if (item != null) {
                inv.setItem(idx++, item);
            }
        }

        // Add Save Button
        ItemStack saveItem = new ItemStack(Material.CHEST);
        ItemMeta saveMeta = saveItem.getItemMeta();
        if (saveMeta != null) {
            saveMeta.setDisplayName("§cSave");
            saveMeta.setLore(List.of("§cClick to save shorting."));
            // tag transaction
            NamespacedKey transactionKey = new NamespacedKey(plugin, "vvSaveItemTransaction_b4n9z");
            saveMeta.getPersistentDataContainer().set(transactionKey, PersistentDataType.STRING, transactionId);

            saveItem.setItemMeta(saveMeta);
        }
        inv.setItem(inventorySize - 1, saveItem);

        // Add glass panes to fill empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }

        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }

        player.openInventory(inv);
    }

    private ItemStack createShortItem(EffectDataManager effect, Player player) {
        // Create potion item
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return null;

        // Set display name and lore
        int currentLevel = plugin.getDataManager().getPlayerEffectLevel(player.getUniqueId(), effect.getId());
        int duration = effect.getDurationPerUpgrade(currentLevel);
        int durationTick = (duration <= -1) ? duration : duration * 20;

        // Set potion type based on effect
        try {
            PotionEffectType pet = effect.getPotionEffectType();
            if (pet != null) {
                PotionEffect pe = new PotionEffect(pet, durationTick, currentLevel, !effect.hasParticles(), effect.hasParticles());
                meta.addCustomEffect(pe, true);
            }
        } catch (Exception ignored) {
        }

        meta.setColor(effect.getPotionEffectType().getColor());

        String displayName = ChatColor.translateAlternateColorCodes('&', effect.getName());
        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        lore.add(effect.getLore());
        lore.add(ChatColor.YELLOW + "》" + ChatColor.WHITE + "Cooldown : " + ChatColor.YELLOW + effect.getCooldownPerUpgrade(currentLevel) + "s");
        lore.add(ChatColor.GRAY + "―――――――――");
        lore.add(ChatColor.GREEN + "Left-Click to short upward " + ChatColor.YELLOW + "◈" + ChatColor.GREEN + " Right-Click to short downward");
        lore.add(ChatColor.GRAY + "―――――――――");
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, effect.getId());
        item.setItemMeta(meta);
        return item;
    }
}

