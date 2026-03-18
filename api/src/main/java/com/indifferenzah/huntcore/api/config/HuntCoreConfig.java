package com.indifferenzah.huntcore.api.config;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class HuntCoreConfig {

    /** Immutable snapshot of a title/subtitle with timing. */
    public record TitleConfig(String title, String subtitle, int fadeIn, int stay, int fadeOut) {}

    // General
    private int spawnRadius;
    private int hunterFreezeSeconds;
    private int postVoteDelaySeconds;
    private int cageX;
    private int cageY;
    private int cageZ;
    private int compassUpdateInterval;
    private double maxHunterRatio;
    private boolean pvpEnabled;

    // Spawns
    private String blockSpawnWorld;
    private double blockSpawnX;
    private double blockSpawnY;
    private double blockSpawnZ;
    private String endSpawnWorld;
    private double endSpawnX;
    private double endSpawnY;
    private double endSpawnZ;

    // Chat
    private String chatFormatNoTeam;
    private String chatFormatRunner;
    private String chatFormatHunter;

    // Running Tab
    private boolean tabEnabled;
    private int tabUpdateInterval;
    private List<String> tabHeader;
    private List<String> tabFooter;
    private boolean tabShowHealth;
    private boolean tabShowTeamPrefix;
    private String tabNameFormatNoTeam;
    private String tabNameFormatRunner;
    private String tabNameFormatHunter;
    private String tabHealthSuffixFormat;

    // Lobby Tab
    private boolean lobbyTabEnabled;
    private List<String> lobbyTabHeader;
    private List<String> lobbyTabFooter;
    private String lobbyTabNameFormatNoTeam;
    private String lobbyTabNameFormatRunner;
    private String lobbyTabNameFormatHunter;

    // Ended Tab
    private boolean endedTabEnabled;
    private List<String> endedTabHeader;
    private List<String> endedTabFooter;

    // Running Scoreboard
    private boolean scoreboardEnabled;
    private int scoreboardUpdateInterval;
    private String scoreboardTitle;
    private List<String> scoreboardLines;

    // Lobby Scoreboard
    private boolean lobbyScoreboardEnabled;
    private String lobbyScoreboardTitle;
    private List<String> lobbyScoreboardLines;

    // Ended Scoreboard
    private boolean endedScoreboardEnabled;
    private String endedScoreboardTitle;
    private List<String> endedScoreboardLines;

    // Bossbar
    private boolean bossbarEnabled;
    private String bossbarText;
    private String bossbarColor;
    private String bossbarStyle;

    // Titles
    private TitleConfig titleGameStart;
    private TitleConfig titleRunnersWin;
    private TitleConfig titleHuntersWin;
    private TitleConfig titleRunnerEliminated;

    // Sounds
    private String soundGameStart;
    private String soundRunnersWin;
    private String soundHuntersWin;
    private String soundRunnerEliminated;
    private float soundVolume;
    private float soundPitch;

    // Glow
    private int glowRunnersAfterMinutes;

    // Nametag
    private String nametagRunnerPrefix;
    private String nametagHunterPrefix;

    // Messages
    private String prefix;
    private String msgGameStart;
    private String msgHuntersWin;
    private String msgRunnersWin;
    private String msgHunterFreeze;
    private String msgHunterUnfreeze;
    private String msgRunnerEliminated;
    private String msgRunnerRespawned;
    private String msgVoteReminder;
    private String msgVoteCountdown;
    private String msgVoteChanged;
    private String msgPvpEnabled;
    private String msgPvpDisabled;
    private String msgTeamSet;
    private String msgTeamCleared;
    private String msgTeamForcedRunner;
    private String msgGameAlreadyRunning;
    private String msgGameNotRunning;
    private String msgNoBlockSpawnSet;
    private String msgSpawnSet;
    private String msgDbCleared;
    private String msgPlayerNotFound;
    private String msgPlayerNotRunner;
    private String msgReloadSuccess;

    private final JavaPlugin plugin;

    public HuntCoreConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        spawnRadius = cfg.getInt("spawn-radius", 2000);
        hunterFreezeSeconds = cfg.getInt("hunter-freeze-seconds", 30);
        postVoteDelaySeconds = cfg.getInt("post-vote-delay-seconds", 15);
        cageX = cfg.getInt("cage-x", 0);
        cageY = cfg.getInt("cage-y", 300);
        cageZ = cfg.getInt("cage-z", 0);
        compassUpdateInterval = cfg.getInt("compass-update-interval", 40);
        maxHunterRatio = cfg.getDouble("max-hunter-ratio", 0.75);
        pvpEnabled = cfg.getBoolean("pvp-enabled", true);

        blockSpawnWorld = cfg.getString("spawns.block.world", "world");
        blockSpawnX = cfg.getDouble("spawns.block.x", 0.0);
        blockSpawnY = cfg.getDouble("spawns.block.y", 64.0);
        blockSpawnZ = cfg.getDouble("spawns.block.z", 0.0);
        endSpawnWorld = cfg.getString("spawns.end.world", "world");
        endSpawnX = cfg.getDouble("spawns.end.x", 0.0);
        endSpawnY = cfg.getDouble("spawns.end.y", 64.0);
        endSpawnZ = cfg.getDouble("spawns.end.z", 0.0);

        chatFormatNoTeam = cfg.getString("chat.format-no-team", "&7%player_name% &8» &f%message%");
        chatFormatRunner = cfg.getString("chat.format-runner", "%team_prefix% &b%player_name% &8» &f%message%");
        chatFormatHunter = cfg.getString("chat.format-hunter", "%team_prefix% &c%player_name% &8» &f%message%");

        // Running Tab
        tabEnabled = cfg.getBoolean("tab.enabled", true);
        tabUpdateInterval = cfg.getInt("tab.update-interval", 40);
        tabHeader = nonEmptyList(cfg.getStringList("tab.header"), List.of("&6&lHuntCore"));
        tabFooter = cfg.getStringList("tab.footer");
        tabShowHealth = cfg.getBoolean("tab.show-health", true);
        tabShowTeamPrefix = cfg.getBoolean("tab.show-team-prefix", true);
        tabNameFormatNoTeam = cfg.getString("tab.name-format-no-team", "&7%player_name%");
        tabNameFormatRunner = cfg.getString("tab.name-format-runner", "&b[RUNNER] &b%player_name%%health_suffix%");
        tabNameFormatHunter = cfg.getString("tab.name-format-hunter", "&c[HUNTER] &c%player_name%%health_suffix%");
        tabHealthSuffixFormat = cfg.getString("tab.health-suffix-format", " &c❤ &f%health%");

        // Lobby Tab
        lobbyTabEnabled = cfg.getBoolean("tab-lobby.enabled", true);
        lobbyTabHeader = nonEmptyList(cfg.getStringList("tab-lobby.header"), List.of("&6&lHuntCore &8- &7Lobby"));
        lobbyTabFooter = cfg.getStringList("tab-lobby.footer");
        lobbyTabNameFormatNoTeam = cfg.getString("tab-lobby.name-format-no-team", "&7%player_name% &8(&7non ha votato&8)");
        lobbyTabNameFormatRunner = cfg.getString("tab-lobby.name-format-runner", "&b[RUNNER] &b%player_name%");
        lobbyTabNameFormatHunter = cfg.getString("tab-lobby.name-format-hunter", "&c[HUNTER] &c%player_name%");

        // Ended Tab
        endedTabEnabled = cfg.getBoolean("tab-ended.enabled", true);
        endedTabHeader = nonEmptyList(cfg.getStringList("tab-ended.header"), List.of("&6&lHuntCore", "&aPartita terminata!"));
        endedTabFooter = cfg.getStringList("tab-ended.footer");

        // Running Scoreboard
        scoreboardEnabled = cfg.getBoolean("scoreboard.enabled", true);
        scoreboardUpdateInterval = cfg.getInt("scoreboard.update-interval", 20);
        scoreboardTitle = cfg.getString("scoreboard.title", "&6&lHUNTCORE");
        scoreboardLines = cfg.getStringList("scoreboard.lines");

        // Lobby Scoreboard
        lobbyScoreboardEnabled = cfg.getBoolean("scoreboard-lobby.enabled", true);
        lobbyScoreboardTitle = cfg.getString("scoreboard-lobby.title", "&e&lLOBBY");
        lobbyScoreboardLines = cfg.getStringList("scoreboard-lobby.lines");

        // Ended Scoreboard
        endedScoreboardEnabled = cfg.getBoolean("scoreboard-ended.enabled", true);
        endedScoreboardTitle = cfg.getString("scoreboard-ended.title", "&a&lFINE PARTITA");
        endedScoreboardLines = cfg.getStringList("scoreboard-ended.lines");

        // Bossbar
        bossbarEnabled = cfg.getBoolean("bossbar.enabled", true);
        bossbarText = cfg.getString("bossbar.text",
                "&c⚔ Manhunt &8| &eRunner: &b%runners_alive%&8/&b%runners_total% &8| &cHunters: &c%hunters_total% &8| &7%game_timer%");
        bossbarColor = cfg.getString("bossbar.color", "RED");
        bossbarStyle = cfg.getString("bossbar.style", "SOLID");

        // Titles
        titleGameStart = loadTitle(cfg, "titles.game-start",
                "&c&lMANHUNT", "&7La caccia ha inizio!", 10, 50, 10);
        titleRunnersWin = loadTitle(cfg, "titles.runners-win",
                "&b&l\u00bb RUNNERS WIN \u00ab", "&7Il blocco \u00e8 stato minato!", 10, 70, 20);
        titleHuntersWin = loadTitle(cfg, "titles.hunters-win",
                "&c&l\u00bb HUNTERS WIN \u00ab", "&7Tutti i runners sono stati eliminati!", 10, 70, 20);
        titleRunnerEliminated = loadTitle(cfg, "titles.runner-eliminated",
                "&c&lELIMINATO", "&7Sei ora in spectator.", 5, 40, 10);

        // Sounds
        soundGameStart = cfg.getString("sounds.game-start", "ENTITY_ENDER_DRAGON_GROWL");
        soundRunnersWin = cfg.getString("sounds.runners-win", "UI_TOAST_CHALLENGE_COMPLETE");
        soundHuntersWin = cfg.getString("sounds.hunters-win", "ENTITY_WITHER_SPAWN");
        soundRunnerEliminated = cfg.getString("sounds.runner-eliminated", "ENTITY_PLAYER_DEATH");
        soundVolume = (float) cfg.getDouble("sounds.volume", 1.0);
        soundPitch = (float) cfg.getDouble("sounds.pitch", 1.0);

        // Glow
        glowRunnersAfterMinutes = cfg.getInt("glow-runners-after-minutes", 30);

        // Nametag
        nametagRunnerPrefix = cfg.getString("nametag.runner-prefix", "&b[RUNNER] ");
        nametagHunterPrefix = cfg.getString("nametag.hunter-prefix", "&c[HUNTER] ");

        // Messages
        prefix = cfg.getString("messages.prefix", "&8[&6HuntCore&8] ");
        msgGameStart = cfg.getString("messages.game-start", "&aLa partita e iniziata!");
        msgHuntersWin = cfg.getString("messages.hunters-win", "&cGli Hunters hanno vinto!");
        msgRunnersWin = cfg.getString("messages.runners-win", "&aI Runners hanno vinto!");
        msgHunterFreeze = cfg.getString("messages.hunter-freeze", "&cSei bloccato per &e{seconds} &csecondi!");
        msgHunterUnfreeze = cfg.getString("messages.hunter-unfreeze", "&aVia! Caccia!");
        msgRunnerEliminated = cfg.getString("messages.runner-eliminated", "&c{player} e stato eliminato!");
        msgRunnerRespawned = cfg.getString("messages.runner-respawned", "&a{player} e stato rispawnato!");
        msgVoteReminder = cfg.getString("messages.vote-reminder", "&eClicca la bussola per scegliere il tuo team!");
        msgVoteCountdown = cfg.getString("messages.vote-countdown", "&aLa partita inizia tra &e{seconds} &asecondi...");
        msgVoteChanged = cfg.getString("messages.vote-changed", "&e{player} &7ha cambiato team. Countdown riavviato!");
        msgPvpEnabled = cfg.getString("messages.pvp-enabled", "&aIl PvP e stato abilitato.");
        msgPvpDisabled = cfg.getString("messages.pvp-disabled", "&cIl PvP e stato disabilitato.");
        msgTeamSet = cfg.getString("messages.team-set", "&aTeam di &e{player} &aimpostato a &e{team}&a.");
        msgTeamCleared = cfg.getString("messages.team-cleared", "&aTeam di &e{player} &areimpostato.");
        msgTeamForcedRunner = cfg.getString("messages.team-forced-runner",
                "&eNon c'era spazio tra gli Hunters, sei stato assegnato ai &bRunner&e.");
        msgGameAlreadyRunning = cfg.getString("messages.game-already-running", "&cUna partita e gia in corso.");
        msgGameNotRunning = cfg.getString("messages.game-not-running", "&cNessuna partita in corso.");
        msgNoBlockSpawnSet = cfg.getString("messages.no-block-spawn-set", "&cPunto spawn blocco non impostato.");
        msgSpawnSet = cfg.getString("messages.spawn-set", "&aSpawn &e{type} &aimpostato.");
        msgDbCleared = cfg.getString("messages.db-cleared", "&aDatabase resettato con successo.");
        msgPlayerNotFound = cfg.getString("messages.player-not-found", "&cGiocatore non trovato.");
        msgPlayerNotRunner = cfg.getString("messages.player-not-runner", "&c{player} non e un runner.");
        msgReloadSuccess = cfg.getString("messages.reload-success", "&aConfigurazione ricaricata.");
    }

    private List<String> nonEmptyList(List<String> list, List<String> fallback) {
        return list.isEmpty() ? new ArrayList<>(fallback) : list;
    }

    // ── Persist methods ──────────────────────────────────────────

    public void saveBlockSpawn(Location loc) {
        plugin.getConfig().set("spawns.block.world", loc.getWorld().getName());
        plugin.getConfig().set("spawns.block.x", loc.getX());
        plugin.getConfig().set("spawns.block.y", loc.getY());
        plugin.getConfig().set("spawns.block.z", loc.getZ());
        plugin.saveConfig();
        blockSpawnWorld = loc.getWorld().getName();
        blockSpawnX = loc.getX();
        blockSpawnY = loc.getY();
        blockSpawnZ = loc.getZ();
    }


    public void savePvpEnabled(boolean enabled) {
        plugin.getConfig().set("pvp-enabled", enabled);
        plugin.saveConfig();
        this.pvpEnabled = enabled;
    }

    // ── Location helpers ─────────────────────────────────────────

    public Location getBlockSpawnLocation(org.bukkit.Server server) {
        World w = server.getWorld(blockSpawnWorld);
        if (w == null) return null;
        return new Location(w, blockSpawnX, blockSpawnY, blockSpawnZ);
    }

    public Location getEndSpawnLocation(org.bukkit.Server server) {
        World w = server.getWorld(endSpawnWorld);
        if (w == null) return null;
        return new Location(w, endSpawnX, endSpawnY, endSpawnZ);
    }

    public void saveEndSpawn(Location loc) {
        plugin.getConfig().set("spawns.end.world", loc.getWorld().getName());
        plugin.getConfig().set("spawns.end.x", loc.getX());
        plugin.getConfig().set("spawns.end.y", loc.getY());
        plugin.getConfig().set("spawns.end.z", loc.getZ());
        plugin.saveConfig();
        endSpawnWorld = loc.getWorld().getName();
        endSpawnX = loc.getX();
        endSpawnY = loc.getY();
        endSpawnZ = loc.getZ();
    }

    public boolean isEndSpawnSet() {
        return endSpawnX != 0 || endSpawnY != 64 || endSpawnZ != 0;
    }

    public Location getCageLocation(org.bukkit.Server server) {
        World w = server.getWorld(blockSpawnWorld);
        if (w == null) return null;
        return new Location(w, cageX, cageY, cageZ);
    }

    public boolean isBlockSpawnSet() {
        return blockSpawnX != 0 || blockSpawnY != 64 || blockSpawnZ != 0;
    }

    // ── Getters ──────────────────────────────────────────────────

    public int getSpawnRadius() { return spawnRadius; }
    public int getHunterFreezeSeconds() { return hunterFreezeSeconds; }
    public int getPostVoteDelaySeconds() { return postVoteDelaySeconds; }
    public int getCageX() { return cageX; }
    public int getCageY() { return cageY; }
    public int getCageZ() { return cageZ; }
    public int getCompassUpdateInterval() { return compassUpdateInterval; }
    public double getMaxHunterRatio() { return maxHunterRatio; }
    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvpEnabled) { this.pvpEnabled = pvpEnabled; }

    public String getChatFormatNoTeam() { return chatFormatNoTeam; }
    public String getChatFormatRunner() { return chatFormatRunner; }
    public String getChatFormatHunter() { return chatFormatHunter; }

    public boolean isTabEnabled() { return tabEnabled; }
    public int getTabUpdateInterval() { return tabUpdateInterval; }
    public List<String> getTabHeader() { return tabHeader; }
    public List<String> getTabFooter() { return tabFooter; }
    public boolean isTabShowHealth() { return tabShowHealth; }
    public boolean isTabShowTeamPrefix() { return tabShowTeamPrefix; }
    public String getTabNameFormatNoTeam() { return tabNameFormatNoTeam; }
    public String getTabNameFormatRunner() { return tabNameFormatRunner; }
    public String getTabNameFormatHunter() { return tabNameFormatHunter; }
    public String getTabHealthSuffixFormat() { return tabHealthSuffixFormat; }

    public boolean isLobbyTabEnabled() { return lobbyTabEnabled; }
    public List<String> getLobbyTabHeader() { return lobbyTabHeader; }
    public List<String> getLobbyTabFooter() { return lobbyTabFooter; }
    public String getLobbyTabNameFormatNoTeam() { return lobbyTabNameFormatNoTeam; }
    public String getLobbyTabNameFormatRunner() { return lobbyTabNameFormatRunner; }
    public String getLobbyTabNameFormatHunter() { return lobbyTabNameFormatHunter; }

    public boolean isEndedTabEnabled() { return endedTabEnabled; }
    public List<String> getEndedTabHeader() { return endedTabHeader; }
    public List<String> getEndedTabFooter() { return endedTabFooter; }

    public boolean isScoreboardEnabled() { return scoreboardEnabled; }
    public int getScoreboardUpdateInterval() { return scoreboardUpdateInterval; }
    public String getScoreboardTitle() { return scoreboardTitle; }
    public List<String> getScoreboardLines() { return scoreboardLines; }

    public boolean isLobbyScoreboardEnabled() { return lobbyScoreboardEnabled; }
    public String getLobbyScoreboardTitle() { return lobbyScoreboardTitle; }
    public List<String> getLobbyScoreboardLines() { return lobbyScoreboardLines; }

    public boolean isEndedScoreboardEnabled() { return endedScoreboardEnabled; }
    public String getEndedScoreboardTitle() { return endedScoreboardTitle; }
    public List<String> getEndedScoreboardLines() { return endedScoreboardLines; }

    public boolean isBossbarEnabled() { return bossbarEnabled; }
    public String getBossbarText() { return bossbarText; }
    public String getBossbarColor() { return bossbarColor; }
    public String getBossbarStyle() { return bossbarStyle; }

    public String getPrefix() { return prefix; }
    public String getMsgGameStart() { return msgGameStart; }
    public String getMsgHuntersWin() { return msgHuntersWin; }
    public String getMsgRunnersWin() { return msgRunnersWin; }
    public String getMsgHunterFreeze() { return msgHunterFreeze; }
    public String getMsgHunterUnfreeze() { return msgHunterUnfreeze; }
    public String getMsgRunnerEliminated() { return msgRunnerEliminated; }
    public String getMsgRunnerRespawned() { return msgRunnerRespawned; }
    public String getMsgVoteReminder() { return msgVoteReminder; }
    public String getMsgVoteCountdown() { return msgVoteCountdown; }
    public String getMsgVoteChanged() { return msgVoteChanged; }
    public String getMsgPvpEnabled() { return msgPvpEnabled; }
    public String getMsgPvpDisabled() { return msgPvpDisabled; }
    public String getMsgTeamSet() { return msgTeamSet; }
    public String getMsgTeamCleared() { return msgTeamCleared; }
    public String getMsgTeamForcedRunner() { return msgTeamForcedRunner; }
    public String getMsgGameAlreadyRunning() { return msgGameAlreadyRunning; }
    public String getMsgGameNotRunning() { return msgGameNotRunning; }
    public String getMsgNoBlockSpawnSet() { return msgNoBlockSpawnSet; }
    public String getMsgSpawnSet() { return msgSpawnSet; }
    public String getMsgDbCleared() { return msgDbCleared; }
    public String getMsgPlayerNotFound() { return msgPlayerNotFound; }
    public String getMsgPlayerNotRunner() { return msgPlayerNotRunner; }
    public String getMsgReloadSuccess() { return msgReloadSuccess; }

    public TitleConfig getTitleGameStart() { return titleGameStart; }
    public TitleConfig getTitleRunnersWin() { return titleRunnersWin; }
    public TitleConfig getTitleHuntersWin() { return titleHuntersWin; }
    public TitleConfig getTitleRunnerEliminated() { return titleRunnerEliminated; }

    public String getSoundGameStart() { return soundGameStart; }
    public String getSoundRunnersWin() { return soundRunnersWin; }
    public String getSoundHuntersWin() { return soundHuntersWin; }
    public String getSoundRunnerEliminated() { return soundRunnerEliminated; }
    public float getSoundVolume() { return soundVolume; }
    public float getSoundPitch() { return soundPitch; }

    public int getGlowRunnersAfterMinutes() { return glowRunnersAfterMinutes; }

    public String getNametagRunnerPrefix() { return nametagRunnerPrefix; }
    public String getNametagHunterPrefix() { return nametagHunterPrefix; }

    private TitleConfig loadTitle(FileConfiguration cfg, String path,
                                  String defaultTitle, String defaultSub,
                                  int defaultFadeIn, int defaultStay, int defaultFadeOut) {
        return new TitleConfig(
                cfg.getString(path + ".title", defaultTitle),
                cfg.getString(path + ".subtitle", defaultSub),
                cfg.getInt(path + ".fade-in", defaultFadeIn),
                cfg.getInt(path + ".stay", defaultStay),
                cfg.getInt(path + ".fade-out", defaultFadeOut)
        );
    }
}
