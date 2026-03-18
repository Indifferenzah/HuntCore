package com.indifferenzah.huntcore.plugin.data;

import com.indifferenzah.huntcore.api.model.PlayerData;
import com.indifferenzah.huntcore.api.service.IPlayerDataLoader;
import com.indifferenzah.huntcore.plugin.database.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataLoader implements IPlayerDataLoader {

    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public PlayerDataLoader(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, PlayerData::new);
    }

    @Override
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    @Override
    public void loadPlayerData(Player player) {
        PlayerData data = new PlayerData(player.getUniqueId());
        databaseManager.loadStats(data);
        data.setEliminated(false);
        data.setFrozen(false);
        data.setSpawnLocation(null);
        playerDataMap.put(player.getUniqueId(), data);
    }

    /** Fire-and-forget async save. Captures values on the calling (main) thread. */
    @Override
    public void savePlayerData(Player player) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        if (data == null) return;
        String name = player.getName();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> databaseManager.saveStats(data, name));
    }

    /** Synchronous save for shutdown where async is not safe. */
    public void savePlayerDataSync(Player player) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        if (data == null) return;
        databaseManager.saveStats(data, player.getName());
    }

    /** Reload stats for all online players from DB (call after clearAll). */
    public void reloadAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            loadPlayerData(player);
        }
    }

    public void unloadPlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public Map<UUID, PlayerData> getAllPlayerData() {
        return playerDataMap;
    }
}
