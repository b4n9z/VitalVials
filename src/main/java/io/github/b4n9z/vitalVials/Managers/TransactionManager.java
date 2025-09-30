package io.github.b4n9z.vitalVials.Managers;

import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TransactionManager {
    private static final Map<UUID, String> activeTransactions = new HashMap<>();

    // Method for generate transaction ID
    public static String generateTransactionId(CommandSender sender, String prefix) {
        return sender instanceof Player ?
                prefix + "-" + ((Player) sender).getUniqueId() + "-" + System.currentTimeMillis() :
                prefix + "-CONSOLE-" + System.currentTimeMillis();
    }

    public static void openTransaction(VitalVials plugin, CommandSender sender, String transactionId) {
        if (sender instanceof Player player) {
            activeTransactions.put(player.getUniqueId(), transactionId);
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (isValidTransaction(sender, transactionId)) {
                closeTransaction(sender);
            }
        }, 20 * 60);
    }

    // Method for check valid transaction ID
    public static boolean isValidTransaction(CommandSender sender, String transactionId) {
        if (!(sender instanceof Player)) return true; // Console always valid

        UUID playerId = ((Player) sender).getUniqueId();
        String storedId = activeTransactions.get(playerId);
        return storedId != null && storedId.equals(transactionId);
    }

    // Method for clear transaction ID
    public static void closeTransaction(CommandSender sender) {
        if (sender instanceof Player) {
            activeTransactions.remove(((Player) sender).getUniqueId());
        }
    }
}
