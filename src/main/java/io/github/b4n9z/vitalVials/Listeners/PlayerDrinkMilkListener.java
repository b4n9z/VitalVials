package io.github.b4n9z.vitalVials.Listeners;

import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerDrinkMilkListener implements Listener {
    private final VitalVials plugin;

    public PlayerDrinkMilkListener(VitalVials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDrinkMilk(PlayerItemConsumeEvent event) {
        // Check if the player is drinking milk
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            // Schedule a task to apply the effects after a short delay
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getEffectManager().applyAllEffects(event.getPlayer()), 5L);
        }
    }
}