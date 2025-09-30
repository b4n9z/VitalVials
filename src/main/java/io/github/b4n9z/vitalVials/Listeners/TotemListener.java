package io.github.b4n9z.vitalVials.Listeners;

import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

public class TotemListener implements Listener {
    private final VitalVials plugin;

    public TotemListener(VitalVials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUseTotem(EntityResurrectEvent event) {
        // Make sure the entity is a player
        if (!(event.getEntity() instanceof Player player)) return;

        // Make sure the event is not cancelled
        if (event.isCancelled()) return;

        // Re-enable effects
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getEffectManager().applyAllEffects(player), 5L);

        // Send message to the player
        player.sendMessage("§aEffects of VitalVials have been re-enabled after using Totem of Undying!");
    }
}
