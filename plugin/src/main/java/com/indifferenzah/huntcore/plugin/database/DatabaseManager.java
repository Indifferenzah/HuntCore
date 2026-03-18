package com.indifferenzah.huntcore.plugin.database;

import com.indifferenzah.huntcore.api.model.PlayerData;
import com.indifferenzah.huntcore.api.model.Team;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        String dbPath = new File(dataFolder, "huntcore").getAbsolutePath();
        initPool(dbPath);
        createSchema();
    }

    private void initPool(String dbPath) {
        try {
            HikariConfig cfg = new HikariConfig();
            cfg.setDriverClassName("org.h2.Driver");
            cfg.setJdbcUrl("jdbc:h2:" + dbPath + ";AUTO_SERVER=FALSE;DB_CLOSE_ON_EXIT=TRUE");
            cfg.setUsername("sa");
            cfg.setPassword("");
            cfg.setMaximumPoolSize(5);
            cfg.setMinimumIdle(1);
            cfg.setConnectionTimeout(10_000);
            cfg.setIdleTimeout(300_000);
            cfg.setMaxLifetime(600_000);
            cfg.setPoolName("HuntCore-DB");
            dataSource = new HikariDataSource(cfg);
            plugin.getLogger().info("[HuntCore] Database pool initialised.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[HuntCore] Failed to initialise database pool.", e);
        }
    }

    private void createSchema() {
        String sql = """
                CREATE TABLE IF NOT EXISTS player_stats (
                    uuid        VARCHAR(36)  PRIMARY KEY,
                    name        VARCHAR(16),
                    kills       INT          DEFAULT 0,
                    deaths      INT          DEFAULT 0,
                    wins        INT          DEFAULT 0,
                    losses      INT          DEFAULT 0,
                    games       INT          DEFAULT 0,
                    runner_games INT         DEFAULT 0,
                    hunter_games INT         DEFAULT 0,
                    last_team   VARCHAR(10),
                    last_seen   BIGINT
                )""";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "[HuntCore] Failed to create schema.", e);
        }
    }

    /** Thread-safe upsert. Safe to call from async threads. */
    public void saveStats(PlayerData data, String playerName) {
        String sql = """
                MERGE INTO player_stats (uuid, name, kills, deaths, wins, losses,
                    games, runner_games, hunter_games, last_team, last_seen)
                KEY(uuid) VALUES (?,?,?,?,?,?,?,?,?,?,?)""";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data.getUuid().toString());
            ps.setString(2, playerName);
            ps.setInt(3, data.getKills());
            ps.setInt(4, data.getDeaths());
            ps.setInt(5, data.getWins());
            ps.setInt(6, data.getLosses());
            ps.setInt(7, data.getGames());
            ps.setInt(8, data.getRunnerGames());
            ps.setInt(9, data.getHunterGames());
            ps.setString(10, data.getTeam().name());
            ps.setLong(11, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "[HuntCore] Failed to save stats for " + data.getUuid(), e);
        }
    }

    /** Load stats into an existing PlayerData object. */
    public void loadStats(PlayerData data) {
        String sql = "SELECT * FROM player_stats WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data.getUuid().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) applyRow(data, rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "[HuntCore] Failed to load stats for " + data.getUuid(), e);
        }
    }

    /** Load stats for an offline player by name (case-insensitive). Returns null if not found. */
    public PlayerData loadStatsByName(String name) {
        String sql = "SELECT * FROM player_stats WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PlayerData data = new PlayerData(UUID.fromString(rs.getString("uuid")));
                    applyRow(data, rs);
                    return data;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "[HuntCore] Failed to load stats by name: " + name, e);
        }
        return null;
    }

    public void clearAll() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM player_stats");
            plugin.getLogger().info("[HuntCore] Database cleared.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "[HuntCore] Failed to clear database.", e);
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("[HuntCore] Database pool closed.");
        }
    }

    private void applyRow(PlayerData data, ResultSet rs) throws SQLException {
        data.setKills(rs.getInt("kills"));
        data.setDeaths(rs.getInt("deaths"));
        data.setWins(rs.getInt("wins"));
        data.setLosses(rs.getInt("losses"));
        data.setGames(rs.getInt("games"));
        data.setRunnerGames(rs.getInt("runner_games"));
        data.setHunterGames(rs.getInt("hunter_games"));
        String lastTeam = rs.getString("last_team");
        if (lastTeam != null) {
            data.setLastTeam(lastTeam);
            try { data.setTeam(Team.valueOf(lastTeam)); } catch (IllegalArgumentException ignored) {}
        }
    }
}
