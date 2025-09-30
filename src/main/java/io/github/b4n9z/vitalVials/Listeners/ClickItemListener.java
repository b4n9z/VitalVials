package io.github.b4n9z.vitalVials.Listeners;

import io.github.b4n9z.vitalVials.Managers.EffectDataManager;
import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

public class ClickItemListener implements Listener {
    private final VitalVials plugin;

    public ClickItemListener(VitalVials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (!(action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR ||
                action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR)) {
            return;
        }

        Player player = event.getPlayer();
        Material item = player.getInventory().getItemInMainHand().getType();
        Map<String, Integer> effects = plugin.getDataManager().getPlayerEffectData(player.getUniqueId());
        String triggerType = (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) ? "rightClick" : "leftClick";

        for (Map.Entry<String, Integer> entry : effects.entrySet()) {
            String effectKey = entry.getKey();
            int level = entry.getValue();

            EffectDataManager effect = plugin.getConfigManager().getEffects().get(effectKey);
            if (effect.getAutoActivate()) continue;

            boolean rightTool = plugin.getConfigManager().isRightToolForEffect(item, effect, triggerType);
            boolean allTools = plugin.getConfigManager().isAllToolsForEffect(effect, triggerType);

            if (rightTool || allTools) {
                plugin.getEffectManager().applyEffect(player, effectKey, level);
            }
        }
    }
}
