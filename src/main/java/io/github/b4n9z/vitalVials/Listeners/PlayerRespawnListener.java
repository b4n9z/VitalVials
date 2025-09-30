package io.github.b4n9z.vitalVials.Listeners;

import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {
    private final VitalVials plugin;

    public PlayerRespawnListener(VitalVials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        plugin.getEffectManager().cleanupPlayer(player.getUniqueId());
        // Apply effects after a short delay to ensure the player has fully respawned
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getEffectManager().applyAllEffects(player), 5L);
    }
}
