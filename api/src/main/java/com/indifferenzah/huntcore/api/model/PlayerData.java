package com.indifferenzah.huntcore.api.model;

import org.bukkit.Location;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private Team team;
    private boolean eliminated;
    private boolean frozen;
    private Location spawnLocation;
    private int kills;
    private int deaths;
    private int wins;
    private int losses;
    private int games;
    private int runnerGames;
    private int hunterGames;
    private String lastTeam;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.team = Team.NONE;
        this.eliminated = false;
        this.frozen = false;
        this.kills = 0;
        this.deaths = 0;
        this.wins = 0;
        this.losses = 0;
        this.games = 0;
        this.runnerGames = 0;
        this.hunterGames = 0;
    }

    public UUID getUuid() { return uuid; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    public boolean isEliminated() { return eliminated; }
    public void setEliminated(boolean eliminated) { this.eliminated = eliminated; }
    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }
    public Location getSpawnLocation() { return spawnLocation; }
    public void setSpawnLocation(Location spawnLocation) { this.spawnLocation = spawnLocation; }
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public void addKill() { this.kills++; }
    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void addDeath() { this.deaths++; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public void addWin() { this.wins++; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    public void addLoss() { this.losses++; }
    public int getGames() { return games; }
    public void setGames(int games) { this.games = games; }
    public void addGame() { this.games++; }
    public int getRunnerGames() { return runnerGames; }
    public void setRunnerGames(int runnerGames) { this.runnerGames = runnerGames; }
    public void addRunnerGame() { this.runnerGames++; }
    public int getHunterGames() { return hunterGames; }
    public void setHunterGames(int hunterGames) { this.hunterGames = hunterGames; }
    public void addHunterGame() { this.hunterGames++; }
    public String getLastTeam() { return lastTeam; }
    public void setLastTeam(String lastTeam) { this.lastTeam = lastTeam; }

    public double getKDRatio() {
        if (deaths == 0) return kills;
        return (double) kills / deaths;
    }

    /** Resets only session-only state. Lifetime stats (kills, deaths, wins, etc.) are preserved. */
    public void resetSessionStats() {
        this.team = Team.NONE;
        this.eliminated = false;
        this.frozen = false;
        this.spawnLocation = null;
    }
}
