package io.github.b4n9z.vitalVials.Managers;

import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EffectManager {
    private final VitalVials plugin;

    // Save cooldowns
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public EffectManager(VitalVials plugin) {
        this.plugin = plugin;
    }

    private void setCooldown(UUID playerId, String effectKey) {
        Player player = Bukkit.getPlayer(playerId);
        int level = plugin.getDataManager().getPlayerEffectLevel(playerId, effectKey);
        if (level < 0 && !plugin.getConfigManager().getEffects().get(effectKey).isSaveEffectData()) {
            level = 0;
        }
        long cooldownTime = plugin.getConfigManager().getEffects().get(effectKey).getCooldownPerUpgrade(level) * 1000L;
        if (cooldownTime <= 0) return;
        long cooldownEnd = System.currentTimeMillis() + cooldownTime;
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(effectKey, cooldownEnd);

        if (plugin.getDataManager().isScoreboardEnabled(playerId) && player != null) {
            plugin.getCustomScoreboardManager().updateScoreboard(player);

            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getCustomScoreboardManager().updateScoreboard(player), ((cooldownTime+100L) * 20L));
        }
    }
    protected boolean isOnCooldown(UUID playerId, String effectKey) {
        long currentTime = System.currentTimeMillis();

        Long cooldownUpTo = cooldowns.getOrDefault(playerId, new HashMap<>()).get(effectKey);

        if (cooldownUpTo == null) return false;

        return currentTime < cooldownUpTo;
    }

    public void applyAllEffects(Player player) {
        UUID playerId = player.getUniqueId();
        Map<String, Integer> playerEffects = plugin.getDataManager().getPlayerEffectData(playerId);
        removeInfiniteEffects(player, playerEffects);

        for (Map.Entry<String, Integer> entry : playerEffects.entrySet()) {
            String effectKey = entry.getKey();
            int level = entry.getValue();

            if (plugin.getConfigManager().getEffects().get(effectKey).getAutoActivate()) {
                plugin.getServer().getLogger().info("Applying effect " + effectKey + " to " + player.getName());
                applyEffect(player, effectKey, level);
            }
        }
    }

    public void applyEffect(Player player, String effectKey, int level) {
        if (isOnCooldown(player.getUniqueId(), effectKey)) return;

        EffectDataManager effectData = plugin.getConfigManager().getEffects().get(effectKey);
        if (effectData == null || level < 0) return;
        if (effectData.isSaveEffectData()) {
            if (!hasEffect(player, effectKey)) return;
        }
        if (level > (effectData.getMaxLevel()-1)) level = effectData.getMaxLevel()-1;

        if (!effectData.isEnabled()) return;

        int duration = effectData.getDurationPerUpgrade(level) *20;
        if (effectData.getDurationPerUpgrade(level) == -1) {
            duration = -1;
        }
        boolean ambient = !effectData.hasParticles();
        boolean particles = effectData.hasParticles();

        PotionEffect potionEffect = new PotionEffect(
                effectData.getPotionEffectType(),
                duration,
                level,
                ambient,
                particles
        );

        player.addPotionEffect(potionEffect);

        setCooldown(player.getUniqueId(), effectKey);
    }

    public void applyEffectToTarget(Player player, LivingEntity target, String effectKey, int level) {
        EffectDataManager effectData = plugin.getConfigManager().getEffects().get(effectKey);

        PotionEffectType effectType = effectData.getPotionEffectType();

        if (player != null) {
            UUID playerId = player.getUniqueId();

            if (!hasEffect(player, effectKey)) return;

            if (isOnCooldown(playerId, effectKey)) {
                return; // Skip if on cooldown
            }

            if (level < 0) return;

            if (!effectData.isEnabled()) return;

            if (canReceiveEffect(target, effectType)) {
                setCooldown(playerId, effectKey);
            }
        }

        if (level > (effectData.getMaxLevel()-1)) level = effectData.getMaxLevel()-1;

        int duration = effectData.getDurationPerUpgrade(level) * 20;
        if (effectData.getDurationPerUpgrade(level) == -1) {
            duration = -1;
        }
        boolean ambient = !effectData.hasParticles();
        boolean particles = effectData.hasParticles();

        PotionEffect potionEffect = new PotionEffect(
                effectType,
                duration,
                level,
                ambient,
                particles
        );

        target.addPotionEffect(potionEffect);
    }

    private boolean canReceiveEffect(LivingEntity target, PotionEffectType effectType) {
        if (target.getType() == EntityType.ENDER_DRAGON ||
                target.getType() == EntityType.WITHER) {
            return false;
        }

        if (effectType.equals(PotionEffectType.POISON) || effectType.equals(PotionEffectType.WITHER)) {
            return !isUndead(target.getType());
        } else if (effectType.equals(PotionEffectType.INSTANT_DAMAGE)) {
            return !isUndead(target.getType());
        } else if (effectType.equals(PotionEffectType.INSTANT_HEALTH)) {
            return isUndead(target.getType());
        } else {
            return true;
        }
    }

    private static final Set<EntityType> UNDEAD_TYPES = Set.of(
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.HUSK,
            EntityType.DROWNED,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.ZOGLIN,
            EntityType.ZOMBIE_HORSE,
            EntityType.SKELETON,
            EntityType.STRAY,
            EntityType.WITHER_SKELETON,
            EntityType.PHANTOM,
            EntityType.WITHER,
            EntityType.SKELETON_HORSE
    );

    private boolean isUndead(EntityType entityType) {
        return UNDEAD_TYPES.contains(entityType);
    }

    private void removeInfiniteEffects(Player player, Map<String, Integer> currentEffects) {
        Collection<PotionEffect> activeEffects = player.getActivePotionEffects();

        for (PotionEffect activeEffect : activeEffects) {
            String effectName = activeEffect.getType().toString();

            if (activeEffect.isInfinite() && !currentEffects.containsKey(effectName)) {
                player.removePotionEffect(activeEffect.getType());
            }
        }
    }

    public void cleanupPlayer(UUID playerId) {
        cooldowns.remove(playerId);
    }

    public boolean hasEffect(Player player, String effectKey) {
        Map<String, Integer> playerEffects = plugin.getDataManager().getPlayerEffectData(player.getUniqueId());
        return playerEffects.containsKey(effectKey) && playerEffects.get(effectKey) > -1;
    }
}
