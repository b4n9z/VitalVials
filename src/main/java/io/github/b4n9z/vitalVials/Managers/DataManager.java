package io.github.b4n9z.vitalVials.Managers;

import com.google.gson.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

public class DataManager {
    private final JavaPlugin plugin;
    private final Gson gson;
    private final File dataFolder;
    private final Map<UUID, Boolean> scoreboardStatusCache = new HashMap<>();

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataFolder = new File(plugin.getDataFolder(), "EffectPlayers");

        // Make folder if it doesn't exist
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                throw new RuntimeException("Failed to create data directory: " + dataFolder.getAbsolutePath());
            }
        }
    }

    /**
     * Get player data in map format
     * @param playerId UUID player
     * @return Map containing effect and level
     */
    public Map<String, Integer> getPlayerEffectData(UUID playerId) {
        File playerFile = getPlayerFile(playerId);
        Map<String, Integer> effects = new HashMap<>();

        if (playerFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(playerFile), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                if (json.has("effects")) {
                    JsonObject effectsObj = json.getAsJsonObject("effects");
                    for (String effect : effectsObj.keySet()) {
                        effects.put(effect, effectsObj.get(effect).getAsInt());
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load data player: " + playerId, e);
            }
        }

        return effects;
    }

    public boolean isScoreboardEnabled(UUID playerId) {
        return scoreboardStatusCache.getOrDefault(playerId, true);
    }

    public void loadScoreboardStatusFromFile(UUID playerId) {
        File playerFile = getPlayerFile(playerId);

        syncOrderWithEffects(playerId);

        boolean enabled = true;

        if (playerFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(playerFile), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                if (json.has("scoreboard")) {
                    JsonObject scoreboardObj = json.getAsJsonObject("scoreboard");
                    if (scoreboardObj.has("enabled")) {
                        enabled = scoreboardObj.get("enabled").getAsBoolean();
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to read scoreboard for player: " + playerId, e);
            }
        }
        scoreboardStatusCache.put(playerId, enabled);
    }

    public List<String> getScoreboardOrder(UUID playerId) {
        File playerFile = getPlayerFile(playerId);
        List<String> order = new ArrayList<>();

        if (playerFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(playerFile), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("scoreboard")) {
                    JsonObject scoreboardObj = json.getAsJsonObject("scoreboard");
                    if (scoreboardObj.has("order")) {
                        for (JsonElement el : scoreboardObj.getAsJsonArray("order")) {
                            order.add(el.getAsString());
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to read scoreboard order for player: " + playerId, e);
            }
        }
        return order;
    }

    /**
     * Save player data
     * @param playerId UUID player
     * @param effects Map containing effect and level
     */
    public void savePlayerEffectData(UUID playerId, Map<String, Integer> effects) {
        File playerFile = getPlayerFile(playerId);
        JsonObject root = new JsonObject();
        JsonObject effectsObj = new JsonObject();

        // Convert Map to JsonObject
        for (Map.Entry<String, Integer> entry : effects.entrySet()) {
            effectsObj.addProperty(entry.getKey(), entry.getValue());
        }

        root.add("effects", effectsObj);

        if (playerFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(playerFile), StandardCharsets.UTF_8)) {
                JsonObject existingJson = JsonParser.parseReader(reader).getAsJsonObject();
                if (existingJson.has("scoreboard")) {
                    root.add("scoreboard", existingJson.get("scoreboard"));
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to preserve scoreboard data when saving effects for player: " + playerId, e);
            }
        }

        // Write to file
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(playerFile), StandardCharsets.UTF_8)) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save data player: " + playerId, e);
        }
        syncOrderWithEffects(playerId);
    }

    public void setScoreboardEnabled(UUID playerId, boolean enabled) {
        File playerFile = getPlayerFile(playerId);
        JsonObject root;

        // Read old file if exists
        if (playerFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(playerFile), StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load existing data to set scoreboard", e);
                root = new JsonObject();
            }
        } else {
            root = new JsonObject();
        }

        // Keep other data such as “effects”
        if (!root.has("effects")) {
            root.add("effects", new JsonObject()); // make sure it is not null
        }

        // Set scoreboard
        JsonObject scoreboardObj = root.has("scoreboard")
                ? root.getAsJsonObject("scoreboard")
                : new JsonObject();

        scoreboardObj.addProperty("enabled", enabled);

        root.add("scoreboard", scoreboardObj);

        // Save again
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(playerFile), StandardCharsets.UTF_8)) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save scoreboard data for player: " + playerId, e);
        }
        scoreboardStatusCache.put(playerId, enabled);
    }

    public void setScoreboardOrder(UUID playerId, List<String> order) {
        File playerFile = getPlayerFile(playerId);
        JsonObject root;

        // Load existing JSON
        if (playerFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(playerFile), StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load existing data to set scoreboard order", e);
                root = new JsonObject();
            }
        } else {
            root = new JsonObject();
        }

        if (!root.has("effects")) {
            root.add("effects", new JsonObject());
        }

        JsonObject scoreboardObj = root.has("scoreboard") ? root.getAsJsonObject("scoreboard") : new JsonObject();

        JsonArray orderArr = new JsonArray();
        for (String effect : order) {
            orderArr.add(effect);
        }
        scoreboardObj.add("order", orderArr);

        root.add("scoreboard", scoreboardObj);

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(playerFile), StandardCharsets.UTF_8)) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save scoreboard order for player: " + playerId, e);
        }
    }

    public void syncOrderWithEffects(UUID playerId) {
        Map<String, Integer> effects = getPlayerEffectData(playerId);
        List<String> order = getScoreboardOrder(playerId);

        // Make set
        Set<String> effectNames = effects.keySet();

        // Add new effects that are not in order
        for (String effect : effectNames) {
            if (!order.contains(effect)) {
                order.add(effect);
            }
        }

        // Remove effects that are not in effects
        order.removeIf(effect -> !effectNames.contains(effect));

        // Save order
        setScoreboardOrder(playerId, order);
    }

    /**
     * Update effect level for certain player
     * @param playerId UUID player
     * @param effectName Name effect
     * @param level Level effect
     */
    public void updatePlayerEffect(UUID playerId, String effectName, int level) {
        Map<String, Integer> effects = getPlayerEffectData(playerId);
        effects.put(effectName, level);
        savePlayerEffectData(playerId, effects);
    }

    /**
     * Get player effect level
     * @param playerId UUID player
     * @param effectName Name effect
     * @return Level effect (0 if not found)
     */
    public int getPlayerEffectLevel(UUID playerId, String effectName) {
        Map<String, Integer> effects = getPlayerEffectData(playerId);
        return effects.getOrDefault(effectName, -1);
    }

    /**
     * Remove effect from player
     * @param playerId UUID player
     * @param effectName Name of the effect to remove
     */
    public void removePlayerEffect(UUID playerId, String effectName) {
        Map<String, Integer> effects = getPlayerEffectData(playerId);
        if (effects.containsKey(effectName)) {
            effects.remove(effectName);
            savePlayerEffectData(playerId, effects);
        }
    }

    /**
     * Get file data player
     * @param playerId UUID player
     * @return File data player
     */
    private File getPlayerFile(UUID playerId) {
        return new File(dataFolder, playerId.toString() + ".json");
    }

    /**
     * Remove file data player
     * @param playerId UUID player
     * @return true if file is deleted
     */
    public boolean deletePlayerData(UUID playerId) {
        File playerFile = getPlayerFile(playerId);
        if (playerFile.exists()) {
            return playerFile.delete();
        }
        return false;
    }

    public boolean removeAllData() {
        try {
            File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                int count = files.length;
                int deleted = 0;
                for (File file : files) {
                    if (file.delete()) {
                        deleted++;
                    }
                }
                return deleted == count;
            }
            return true; // Successful deletion
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to remove all death data", e);
            return false;
        }
    }
}