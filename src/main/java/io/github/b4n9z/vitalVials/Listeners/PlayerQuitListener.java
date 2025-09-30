package io.github.b4n9z.vitalVials.Listeners;

import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final VitalVials plugin;

    public PlayerQuitListener (VitalVials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getCustomScoreboardManager().stopUpdating(player.getUniqueId());
        // Clean up cooldowns from memory after 3 minutes if player has quit
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getEffectManager().cleanupPlayer(player.getUniqueId()), 20*180);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        plugin.getCustomScoreboardManager().stopUpdating(player.getUniqueId());
        plugin.getEffectManager().cleanupPlayer(player.getUniqueId());
    }
}