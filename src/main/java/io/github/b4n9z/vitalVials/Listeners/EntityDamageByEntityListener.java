package io.github.b4n9z.vitalVials.Listeners;

import io.github.b4n9z.vitalVials.Managers.EffectDataManager;
import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;

import java.util.Map;

public class EntityDamageByEntityListener implements Listener {
    private final VitalVials plugin;

    public EntityDamageByEntityListener(VitalVials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Make sure the victim is a living entity (player, mob, etc.)
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        // Make sure the event is not cancelled
        if (event.isCancelled()) return;

        Entity attacker = event.getDamager();
        Player attackerPlayer = null;
        Material weapon = Material.AIR;

        if (attacker instanceof Player player) {
            attackerPlayer = player;
            weapon = player.getInventory().getItemInMainHand().getType();
        } else if (attacker instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                attackerPlayer = shooter;

                switch (projectile) {
                    case Arrow ignored -> weapon = Material.ARROW;
                    case Trident ignored -> weapon = Material.TRIDENT;
                    case Snowball ignored -> weapon = Material.SNOWBALL;
                    case Egg ignored -> weapon = Material.EGG;
                    default -> {
                    }
                }
            }
        }

        if (attackerPlayer != null) {
            // Player hitting any entity
            applyEffects(attackerPlayer, target, weapon, "YouAreHittingEnemy");

            // If the target is a player, check reverse effects
            if (target instanceof Player targetPlayer) {
                applyEffects(targetPlayer, attackerPlayer, weapon, "enemyHitYou");
            }

        } else if (attacker instanceof LivingEntity destroyer && target instanceof Player targetPlayer) {
            // Non-player (mob) hitting a player
            EntityEquipment equipment = destroyer.getEquipment();
            if (equipment != null) {
                weapon = equipment.getItemInMainHand().getType();
            }

            applyEffects(targetPlayer, destroyer, weapon, "enemyHitYou");
        }
    }

    private void applyEffects(Player sourcePlayer, LivingEntity target, Material weapon, String triggerType) {
        Map<String, Integer> playerEffects = plugin.getDataManager().getPlayerEffectData(sourcePlayer.getUniqueId());

        for (Map.Entry<String, Integer> entry : playerEffects.entrySet()) {
            String effectKey = entry.getKey();
            int level = entry.getValue();

            EffectDataManager effect = plugin.getConfigManager().getEffects().get(effectKey);
            if (effect.getAutoActivate()) continue;

            boolean allTools = plugin.getConfigManager().isAllToolsForEffect(effect, triggerType);
            boolean rightTool = plugin.getConfigManager().isRightToolForEffect(weapon, effect, triggerType);

            if (allTools || rightTool) {
                plugin.getEffectManager().applyEffectToTarget(sourcePlayer, target, effectKey, level);
            }
        }
    }
}
