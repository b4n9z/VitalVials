package io.github.b4n9z.vitalVials.Listeners;

import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChangeListener implements Listener {
    private final VitalVials plugin;

    public WorldChangeListener(VitalVials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        // Give a short delay to ensure the player has fully changed dimensions
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Apply effects after a short delay to ensure the player has fully changed dimensionsA
            plugin.getEffectManager().applyAllEffects(player);

            // Send a message to the player
            player.sendMessage("§aYour VitalVials effects have been reapplied after changing dimensions!");
        }, 5L); // Delay 5 ticks (0.25 seconds)
    }
}
