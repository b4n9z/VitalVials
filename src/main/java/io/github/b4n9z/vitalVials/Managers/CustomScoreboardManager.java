package io.github.b4n9z.vitalVials.Managers;

import io.github.b4n9z.vitalVials.VitalVials;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomScoreboardManager {
    private final VitalVials plugin;
    private final Map<UUID, Objective> playerObjectives = new HashMap<>();
    private final Map<UUID, BukkitTask> updateTasks = new ConcurrentHashMap<>();

    // Constants text that always the same
    private static final String PREFIX = ChatColor.GOLD + "≫ " + ChatColor.YELLOW;
    private static final String HEADER = ChatColor.WHITE + "》―――――――――――――――――――――――――《";
    private static final String TITLE = ChatColor.YELLOW + "Effects " + ChatColor.GOLD + "≫";
    private static final String NO_EFFECTS = ChatColor.GOLD + "≫ " + ChatColor.RED + "No active effects";
    private static final String FOOTER = ChatColor.WHITE + "≫―――――――――――――――――――――――――≪";

    public CustomScoreboardManager(VitalVials plugin) {
        this.plugin = plugin;
    }

    public void createScoreboard(Player player) {
        stopUpdating(player.getUniqueId());
        // Make new scoreboard
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null) return;
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("vv_effects", Criteria.DUMMY,
                ChatColor.GOLD + "≫ " + ChatColor.YELLOW + "Server Stats" + ChatColor.GOLD + " ≪");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Save scoreboard and objective
        playerObjectives.put(player.getUniqueId(), objective);
        // Set scoreboard to player
        player.setScoreboard(scoreboard);
        // Start update period
        startUpdating(player);
    }

    public void updateScoreboard(Player player) {
        UUID playerId = player.getUniqueId();

        if (!plugin.getDataManager().isScoreboardEnabled(player.getUniqueId())) {
            removeScoreboard(player); // If scoreboard disabled, remove scoreboard
            return;
        }
        if (!player.isOnline()) return;
        // Get player data
        Map<String, Integer> playerData = plugin.getDataManager().getPlayerEffectData(playerId);
        if (playerData == null) return;

        List<String> order = plugin.getDataManager().getScoreboardOrder(playerId);

        // Proses di async thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<String, String> entries = new LinkedHashMap<>();

            // Header
            entries.put("header", HEADER);
            entries.put("title", TITLE);

            // Process Effects
            boolean hasEffects = false;
            for (String effectName : order) {
                if (!playerData.containsKey(effectName)) continue;

                int level = playerData.get(effectName);
                EffectDataManager effect = plugin.getConfigManager().getEffects().get(effectName);

                if (effect == null || !effect.isEnabled() ||
                        effect.getAutoActivate() ||
                        effect.getCooldownPerUpgrade(level) <= 0) {
                    continue;
                }

                boolean onCooldown = plugin.getEffectManager().isOnCooldown(player.getUniqueId(), effectName);
                String entryText = PREFIX + (onCooldown ? ChatColor.RED : "") + effectName + ": " + (onCooldown ? ChatColor.WHITE : ChatColor.GREEN) + (level + 1);

                entries.put("effect_" + effectName, entryText);
                hasEffects = true;
            }

            // If no effects active
            if (!hasEffects) {
                entries.put("no_effects", NO_EFFECTS);
            }

            // Footer
            entries.put("footer", FOOTER);

            // Update in main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                Objective objective = playerObjectives.get(playerId);
                if (objective == null || objective.getScoreboard() == null) return;

                // Remove previous entries
                for (String entry : objective.getScoreboard().getEntries()) {
                    objective.getScoreboard().resetScores(entry);
                }

                // Add new entries
                int currentScore = entries.size();
                for (Map.Entry<String, String> entry : entries.entrySet()) {
                    objective.getScore(entry.getValue()).setScore(currentScore--);
                }
            });
        });
    }

    public void removeScoreboard(Player player) {
        stopUpdating(player.getUniqueId());

        playerObjectives.remove(player.getUniqueId());

        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager != null) {
            player.setScoreboard(scoreboardManager.getNewScoreboard());
        }
    }

    public void startUpdating(Player player) {
        // Stop existing task if any
        stopUpdating(player.getUniqueId());

        updateScoreboard(player);

        updateTasks.put(player.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    stopUpdating(player.getUniqueId());
                    return;
                }
                updateScoreboard(player);
            }
        }.runTaskTimerAsynchronously(plugin, 0L, plugin.getConfigManager().getPeriodUpdateScoreboard() * 20L));
    }

    public void stopUpdating(UUID playerId) {
        BukkitTask task = updateTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }
}