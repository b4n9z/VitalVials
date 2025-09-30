package io.github.b4n9z.vitalVials.Listeners;

import io.github.b4n9z.vitalVials.Commands.ShopCommand;
import io.github.b4n9z.vitalVials.Managers.TransactionManager;
import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class InventoryGUIClickListener implements Listener {
    private final VitalVials plugin;
    private final ShopCommand shopCommand;
    private final NamespacedKey itemKey;
    private final NamespacedKey saveTxnKey;

    public InventoryGUIClickListener(VitalVials plugin) {
        this.plugin = plugin;
        this.shopCommand = new ShopCommand(plugin);
        this.itemKey = new NamespacedKey(plugin, "vvShortItem_b4n9z");
        this.saveTxnKey = new NamespacedKey(plugin, "vvSaveItemTransaction_b4n9z");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryView view = event.getView();

        // Check if this is our shop inventory
        if (view.getTitle().equals(ChatColor.BLACK + "VitalVials Shop") || view.getTitle().equals(ChatColor.BLACK + "VitalVials Confirm Buy")) {
            event.setCancelled(true); // Prevent taking items

            // Handle the click
            shopCommand.handleShopClick(player, event.getCurrentItem());
        }

        if (view.getTitle().equals(ChatColor.BLACK + "VitalVials Scoreboard Sorting")) {
            event.setCancelled(true); // Prevent taking items

            Inventory inv = event.getInventory();

            int rawSlot = event.getRawSlot();
            int size = inv.getSize();
            int saveSlot = size - 1;

            if (rawSlot < 0 || rawSlot >= size) return;

            if (rawSlot == saveSlot) {
                ItemStack item = event.getCurrentItem();
                if (item == null || !item.hasItemMeta()) return;
                ItemMeta meta = item.getItemMeta();
                if (meta == null) return;

                String txnId = meta.getPersistentDataContainer().get(saveTxnKey, PersistentDataType.STRING);
                if (txnId != null && TransactionManager.isValidTransaction(player, txnId)) {
                    saveOrderAndClose(player, inv);
                } else {
                    player.sendMessage("§cNo active transaction to cancel or invalid transaction ID.");
                }
                TransactionManager.closeTransaction(player);
                return;
            }

            ClickType click = event.getClick();
            if (!(click == ClickType.LEFT || click == ClickType.RIGHT)) {
                return;
            }

            ItemStack clicked = inv.getItem(rawSlot);
            if (clicked == null) return;
            ItemMeta meta = clicked.getItemMeta();
            if (meta == null) return;

            String effectId = meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
            if (effectId == null) return; // Not item effect, so ignore

            if (click == ClickType.LEFT) {
                // move left (swap with slot-1) but don't swap to save slot
                int target = rawSlot - 1;
                if (target >= 0 && target != saveSlot) {
                    swap(inv, rawSlot, target);
                }
            } else {
                int target = rawSlot + 1;
                if (target < saveSlot) { // Don't swap to save slot
                    swap(inv, rawSlot, target);
                }
            }
        }
    }

    private void swap(Inventory inv, int a, int b) {
        ItemStack ia = inv.getItem(a);
        ItemStack ib = inv.getItem(b);
        inv.setItem(a, ib);
        inv.setItem(b, ia);
    }

    private void saveOrderAndClose(Player player, Inventory inv) {
        List<String> newOrder = new ArrayList<>();
        int size = inv.getSize();
        int saveSlot = size - 1;
        for (int i = 0; i < size; i++) {
            if (i == saveSlot) continue;
            ItemStack it = inv.getItem(i);
            if (it == null) continue;
            ItemMeta meta = it.getItemMeta();
            if (meta == null) continue;
            String effectId = meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
            if (effectId != null && !effectId.isBlank()) {
                newOrder.add(effectId);
            }
        }

        // Save order to DataManager
        plugin.getDataManager().setScoreboardOrder(player.getUniqueId(), newOrder);
        // Sync order with effects (remove refunded and add new effects if needed)
        plugin.getDataManager().syncOrderWithEffects(player.getUniqueId());

        // update scoreboard
        plugin.getCustomScoreboardManager().updateScoreboard(player);

        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Scoreboard order saved.");
    }
}