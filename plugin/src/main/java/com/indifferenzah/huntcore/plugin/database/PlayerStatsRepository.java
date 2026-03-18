package com.indifferenzah.huntcore.plugin.database;

import com.indifferenzah.huntcore.api.model.PlayerData;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerStatsRepository {

    private final DatabaseManager databaseManager;

    public PlayerStatsRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void save(Player player, PlayerData data) {
        databaseManager.saveStats(data, player.getName());
    }

    public void load(PlayerData data) {
        databaseManager.loadStats(data);
    }

    public void clearAll() {
        databaseManager.clearAll();
    }
}
