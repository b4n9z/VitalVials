package io.github.b4n9z.vitalVials.Listeners;

import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final VitalVials plugin;

    public PlayerJoinListener (VitalVials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            plugin.getDataManager().setScoreboardEnabled(player.getUniqueId(), plugin.getConfigManager().isDefaultShowScoreboardEnabled());
        } else {
            plugin.getDataManager().loadScoreboardStatusFromFile(player.getUniqueId());
        }
        if (plugin.getDataManager().isScoreboardEnabled(player.getUniqueId())) {
            plugin.getCustomScoreboardManager().createScoreboard(player);
        }
        plugin.getEffectManager().applyAllEffects(player);
    }
}