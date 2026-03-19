package com.indifferenzah.huntcore.plugin.game;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.api.config.HuntCoreConfig.TitleConfig;
import com.indifferenzah.huntcore.api.model.GameState;
import com.indifferenzah.huntcore.api.model.PlayerData;
import com.indifferenzah.huntcore.api.model.Team;
import com.indifferenzah.huntcore.api.service.IGameManager;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import com.indifferenzah.huntcore.plugin.display.BossBarManager;
import com.indifferenzah.huntcore.plugin.task.CompassTask;
import com.indifferenzah.huntcore.plugin.task.FreezeTask;
import com.indifferenzah.huntcore.plugin.utils.BeaconBuilder;
import com.indifferenzah.huntcore.plugin.utils.CageBuilder;
import com.indifferenzah.huntcore.plugin.utils.LocationUtils;
import com.indifferenzah.huntcore.plugin.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GameManager implements IGameManager {

    private static final int COMPASS_SLOT = 8; // slot 9 (0-indexed)

    private final JavaPlugin plugin;
    private final PlayerDataLoader dataLoader;
    private final HuntCoreConfig config;
    private BossBarManager bossBarManager;

    private GameState gameState = GameState.IDLE;
    private boolean pvpEnabled;
    private long gameStartTime = -1;

    private final CageBuilder cageBuilder = new CageBuilder();
    private final BeaconBuilder beaconBuilder = new BeaconBuilder();

    private Location cryingObsidianLocation;
    private Location gameSpawnLocation;
    private CompassTask compassTask;
    private FreezeTask freezeTask;
    private BukkitRunnable glowTask;
    private boolean glowActive = false;

    // UUID → chosen team (lobby votes)
    private final Map<UUID, Team> lobbyVotes = new HashMap<>();

    // Players who disconnected mid-game and should be restored on rejoin
    private final Set<UUID> disconnectedRunners  = new HashSet<>();
    private final Set<UUID> disconnectedHunters  = new HashSet<>();

    // Frozen counts shown in tab after game ends (reset when new lobby starts)
    private int lastGameVoteCount   = 0;
    private int lastGameOnlineCount = 0;
    private BukkitRunnable countdownTask;

    public GameManager(JavaPlugin plugin, PlayerDataLoader dataLoader, HuntCoreConfig config) {
        this.plugin = plugin;
        this.dataLoader = dataLoader;
        this.config = config;
        this.pvpEnabled = config.isPvpEnabled();
    }

    public void setBossBarManager(BossBarManager bossBarManager) {
        this.bossBarManager = bossBarManager;
    }

    // ── Game lifecycle ────────────────────────────────────────────

    @Override
    public void startGame() {
        if (gameState != GameState.IDLE) return;

        Location blockSpawn = config.getBlockSpawnLocation(plugin.getServer());
        if (blockSpawn == null) {
            plugin.getLogger().warning("[HuntCore] Block spawn not set! Cannot start game.");
            return;
        }

        gameState = GameState.LOBBY;
        lobbyVotes.clear();
        lastGameVoteCount   = 0;
        lastGameOnlineCount = 0;

        // Build cage at fixed location (0, 300, 0 by default)
        Location cageLoc = new Location(blockSpawn.getWorld(),
                config.getCageX(), config.getCageY(), config.getCageZ());

        cageBuilder.buildCage(cageLoc);

        Location inside = cageLoc.clone().add(0.5, 1, 0.5);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(inside);
            player.setGameMode(GameMode.SURVIVAL);
            giveVoteCompass(player);
            player.sendMessage(MessageUtils.colorize(config.getMsgVoteReminder()));
        }
    }

    @Override
    public void stopGame() {
        if (gameState == GameState.IDLE) return;

        gameState = GameState.IDLE;
        gameStartTime = -1;

        cancelTasks();
        cageBuilder.destroyCage();
        beaconBuilder.removePyramid();
        cryingObsidianLocation = null;
        gameSpawnLocation = null;
        disconnectedRunners.clear();
        disconnectedHunters.clear();

        if (bossBarManager != null) bossBarManager.hide();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = dataLoader.getPlayerData(player);
            if (data.getTeam() == Team.RUNNER || data.getTeam() == Team.HUNTER) {
                dataLoader.savePlayerData(player);
            }
            data.resetSessionStats();
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
        }

        broadcast(config.getPrefix() + "&cPartita terminata dall'admin.");
    }

    public void onPlayerVote(Player player, Team team) {
        if (gameState != GameState.LOBBY && gameState != GameState.COUNTDOWN) return;

        PlayerData data = dataLoader.getPlayerData(player);
        Team previousTeam = data.getTeam();
        data.setTeam(team);
        lobbyVotes.put(player.getUniqueId(), team);

        player.closeInventory();
        player.sendMessage(MessageUtils.colorize(
                config.getPrefix() + "&7Hai scelto il team: " + team.getColoredName()));

        if (gameState == GameState.LOBBY) {
            checkAllVoted();
        } else if (gameState == GameState.COUNTDOWN && previousTeam != team) {
            // Restart countdown on vote change
            if (countdownTask != null) {
                countdownTask.cancel();
                countdownTask = null;
            }
            String msg = config.getMsgVoteChanged().replace("{player}", player.getName());
            broadcast(config.getPrefix() + msg);
            gameState = GameState.LOBBY;
            checkAllVoted();
        }
    }

    private void checkAllVoted() {
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        if (online.isEmpty()) return;
        boolean allVoted = online.stream().allMatch(p -> lobbyVotes.containsKey(p.getUniqueId()));
        if (allVoted) startCountdown();
    }

    private void startCountdown() {
        gameState = GameState.COUNTDOWN;
        int seconds = config.getPostVoteDelaySeconds();

        countdownTask = new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (remaining <= 0) {
                    this.cancel();
                    countdownTask = null;
                    launchGamePhase1();
                    return;
                }
                if (remaining <= 5 || remaining == 10 || remaining == seconds) {
                    String msg = config.getMsgVoteCountdown().replace("{seconds}", String.valueOf(remaining));
                    broadcast(config.getPrefix() + msg);
                }
                remaining--;
            }
        };
        countdownTask.runTaskTimer(plugin, 0L, 20L);
    }

    /** Phase 1: enforce hunter cap, pick spawn locations, pre-generate chunks. */
    private void launchGamePhase1() {
        Location blockSpawn = config.getBlockSpawnLocation(plugin.getServer());
        if (blockSpawn == null) { stopGame(); return; }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        enforceHunterCap(players);

        // Assign default team to non-voters
        for (Player p : players) {
            if (dataLoader.getPlayerData(p).getTeam() == Team.NONE) {
                dataLoader.getPlayerData(p).setTeam(Team.RUNNER);
            }
        }

        // Pick ONE shared random spawn for all players
        Location sharedSpawn = LocationUtils.getRandomSpawnLocation(blockSpawn, config.getSpawnRadius());
        Map<UUID, Location> spawnMap = new LinkedHashMap<>();
        for (Player p : players) {
            spawnMap.put(p.getUniqueId(), sharedSpawn);
        }

        // Pre-generate the single spawn chunk async, then execute on main thread
        World world = blockSpawn.getWorld();
        List<CompletableFuture<Chunk>> futures = List.of(
                world.getChunkAtAsync(sharedSpawn.getBlockX() >> 4, sharedSpawn.getBlockZ() >> 4));

        broadcast(config.getPrefix() + "&7Pre-generazione del mondo in corso...");

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () ->
                        launchGamePhase2(blockSpawn, players, spawnMap)));
    }

    /** Phase 2: teleport players and start the game. */
    private void launchGamePhase2(Location blockSpawn, List<Player> players, Map<UUID, Location> spawnMap) {
        gameState = GameState.RUNNING;
        gameStartTime = System.currentTimeMillis();
        gameSpawnLocation = players.isEmpty() ? null : spawnMap.values().iterator().next();
        lastGameVoteCount   = lobbyVotes.size();
        lastGameOnlineCount = players.size();

        cageBuilder.destroyCage();
        cryingObsidianLocation = beaconBuilder.buildPyramid(blockSpawn);

        List<Player> hunters = new ArrayList<>();
        List<Player> runners = new ArrayList<>();

        for (Player player : players) {
            PlayerData data = dataLoader.getPlayerData(player);
            Location spawnLoc = spawnMap.getOrDefault(player.getUniqueId(), gameSpawnLocation);
            data.setSpawnLocation(spawnLoc);
            player.teleport(spawnLoc);

            data.addGame();
            if (data.getTeam() == Team.RUNNER) { data.addRunnerGame(); runners.add(player); }
            else { data.addHunterGame(); hunters.add(player); }
        }

        // Start compass task
        compassTask = new CompassTask(plugin, this, dataLoader, config);
        compassTask.runTaskTimer(plugin, 0L, config.getCompassUpdateInterval());

        // Freeze hunters (clears their inventory)
        freezeTask = new FreezeTask(dataLoader, config, hunters);
        freezeTask.freezeAll();
        freezeTask.runTaskTimer(plugin, 20L, 20L); // every second

        // Clear runners' inventory and give compass → crying obsidian
        for (Player runner : runners) {
            runner.getInventory().clear();
            giveHuntCompass(runner);
        }
        // Give compass to hunters AFTER freeze cleared their inventory
        for (Player hunter : hunters) {
            giveHuntCompass(hunter);
        }

        // Bossbar
        if (bossBarManager != null && config.isBossbarEnabled()) {
            bossBarManager.show();
        }

        // Heal all players to full health and saturation
        for (Player p : players) {
            p.setHealth(p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
            p.setFoodLevel(20);
            p.setSaturation(20f);
        }

        // Title + sound
        broadcastTitle(config.getTitleGameStart());
        broadcastSound(config.getSoundGameStart());

        broadcast(config.getPrefix() + config.getMsgGameStart());

        // Schedule glow on runners after X minutes
        int glowMinutes = config.getGlowRunnersAfterMinutes();
        if (glowMinutes > 0) {
            glowTask = new BukkitRunnable() {
                @Override
                public void run() {
                    glowActive = true;
                    Bukkit.getOnlinePlayers().stream()
                            .filter(p -> {
                                PlayerData d = dataLoader.getPlayerData(p);
                                return d.getTeam() == Team.RUNNER && !d.isEliminated();
                            })
                            .forEach(p -> p.setGlowing(true));
                    broadcast(config.getPrefix() + "&7I runners ora brillano nel buio!");
                    glowTask = null;
                }
            };
            glowTask.runTaskLater(plugin, (long) glowMinutes * 60 * 20);
        }
    }

    private void enforceHunterCap(List<Player> players) {
        // Always allow at least 1 hunter
        int maxHunters = Math.max(1, (int) Math.floor(players.size() * config.getMaxHunterRatio()));

        List<Player> hunterList = players.stream()
                .filter(p -> dataLoader.getPlayerData(p).getTeam() == Team.HUNTER)
                .collect(Collectors.toCollection(ArrayList::new));

        if (hunterList.size() <= maxHunters) return;

        Collections.shuffle(hunterList);
        for (int i = maxHunters; i < hunterList.size(); i++) {
            Player p = hunterList.get(i);
            dataLoader.getPlayerData(p).setTeam(Team.RUNNER);
            p.sendMessage(MessageUtils.colorize(config.getPrefix() + config.getMsgTeamForcedRunner()));
        }
    }

    // ── Game events ───────────────────────────────────────────────

    @Override
    public void eliminateRunner(Player player) {
        PlayerData data = dataLoader.getPlayerData(player);
        if (data.getTeam() != Team.RUNNER || data.isEliminated()) return;

        data.setEliminated(true);
        data.addDeath();
        if (glowActive) player.setGlowing(false);
        player.setGameMode(GameMode.SPECTATOR);

        // Title only for the eliminated player
        sendTitle(player, config.getTitleRunnerEliminated());

        String msg = config.getMsgRunnerEliminated().replace("{player}", player.getName());
        broadcast(config.getPrefix() + msg);
        broadcastSound(config.getSoundRunnerEliminated());
        checkWinCondition();
    }

    @Override
    public void checkWinCondition() {
        if (gameState != GameState.RUNNING) return;
        if (getAliveRunnerCount() == 0) endGame(false);
    }

    public void onCryingObsidianBreak() {
        if (gameState != GameState.RUNNING) return;
        endGame(true);
    }

    private void endGame(boolean runnersWin) {
        gameState = GameState.ENDED;

        cancelTasks();

        if (bossBarManager != null) bossBarManager.hide();

        String winMessage = runnersWin ? config.getMsgRunnersWin() : config.getMsgHuntersWin();
        broadcast(config.getPrefix() + winMessage);
        broadcastTitle(runnersWin ? config.getTitleRunnersWin() : config.getTitleHuntersWin());
        broadcastSound(runnersWin ? config.getSoundRunnersWin() : config.getSoundHuntersWin());

        // Update stats
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = dataLoader.getPlayerData(player);
            if (data.getTeam() == Team.RUNNER) {
                if (runnersWin) data.addWin(); else data.addLoss();
            } else if (data.getTeam() == Team.HUNTER) {
                if (!runnersWin) data.addWin(); else data.addLoss();
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            dataLoader.savePlayerData(player);
        }

        // Immediately: disable PvP and make everyone invincible for the end phase
        pvpEnabled = false;
        Bukkit.getOnlinePlayers().forEach(p -> p.setInvulnerable(true));

        // Teleport everyone to end spawn after 3 seconds
        final Location endSpawn = config.getEndSpawnLocation(plugin.getServer());
        new BukkitRunnable() {
            @Override
            public void run() {
                beaconBuilder.removePyramid();
                cryingObsidianLocation = null;
                gameStartTime = -1;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData data = dataLoader.getPlayerData(player);
                    data.resetSessionStats();
                    player.setGameMode(GameMode.SURVIVAL);
                    player.getInventory().clear();

                    if (endSpawn != null) {
                        player.teleport(endSpawn);
                    }
                }

                // Restore PvP, remove invincibility, and reset state
                pvpEnabled = config.isPvpEnabled();
                Bukkit.getOnlinePlayers().forEach(p -> p.setInvulnerable(false));
                disconnectedRunners.clear();
                disconnectedHunters.clear();
                gameState = GameState.IDLE;
            }
        }.runTaskLater(plugin, 60L); // 3 seconds
    }

    private void cancelTasks() {
        if (compassTask != null) { compassTask.cancel(); compassTask = null; }
        if (freezeTask != null) { freezeTask.unFreezeAll(); freezeTask.cancel(); freezeTask = null; }
        if (countdownTask != null) { countdownTask.cancel(); countdownTask = null; }
        if (glowTask != null) { glowTask.cancel(); glowTask = null; }
        if (glowActive) {
            Bukkit.getOnlinePlayers().forEach(p -> p.setGlowing(false));
            glowActive = false;
        }
    }

    // ── Compass helpers ───────────────────────────────────────────

    private void giveVoteCompass(Player player) {
        // Clear any existing compass first
        clearCompasses(player);
        ItemStack compass = buildCompass("&eScegli il tuo team");
        player.getInventory().setItem(COMPASS_SLOT, compass);
    }

    public void giveHuntCompass(Player player) {
        // Remove vote compass before giving hunt compass
        clearCompasses(player);
        ItemStack compass = buildCompass("&eBussola");
        player.getInventory().setItem(COMPASS_SLOT, compass);
    }

    private void clearCompasses(Player player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == Material.COMPASS) {
                inv.setItem(i, null);
            }
        }
    }

    private ItemStack buildCompass(String name) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
            compass.setItemMeta(meta);
        }
        return compass;
    }

    // ── Player reconnect ─────────────────────────────────────────

    /**
     * Called on player join. If the player was an active game participant who
     * crashed mid-game, restores their team, game mode, compass, and spawn.
     * @return true if the player was restored (skip the spectator fallback)
     */
    public boolean tryRestoreReconnecting(Player player) {
        if (gameState != GameState.RUNNING) return false;

        Team team = null;
        if (disconnectedRunners.remove(player.getUniqueId())) {
            team = Team.RUNNER;
        } else if (disconnectedHunters.remove(player.getUniqueId())) {
            team = Team.HUNTER;
        }
        if (team == null) return false;

        PlayerData data = dataLoader.getPlayerData(player);
        data.setTeam(team);
        player.setGameMode(GameMode.SURVIVAL);
        giveHuntCompass(player);
        if (gameSpawnLocation != null) player.teleport(gameSpawnLocation);

        // Re-apply glow if active and runner
        if (glowActive && team == Team.RUNNER) player.setGlowing(true);

        return true;
    }

    // ── Player disconnect ─────────────────────────────────────────

    public void onPlayerQuit(Player player) {
        PlayerData data = dataLoader.getPlayerData(player);

        if (gameState == GameState.LOBBY || gameState == GameState.COUNTDOWN) {
            lobbyVotes.remove(player.getUniqueId());
            if (gameState == GameState.LOBBY) checkAllVoted();
        } else if (gameState == GameState.RUNNING) {
            if (data.getTeam() == Team.RUNNER && !data.isEliminated()) {
                // Track for reconnect instead of eliminating immediately
                disconnectedRunners.add(player.getUniqueId());
                checkWinCondition();
            } else if (data.getTeam() == Team.HUNTER) {
                disconnectedHunters.add(player.getUniqueId());
            }
            dataLoader.savePlayerData(player);
        }

        if (data.isFrozen() && freezeTask != null) {
            freezeTask.unFreeze(player);
        }

        if (bossBarManager != null) bossBarManager.removePlayer(player);
        dataLoader.unloadPlayerData(player.getUniqueId());
    }

    // ── Broadcast helper ──────────────────────────────────────────

    private void broadcast(String message) {
        Component component = MessageUtils.colorize(message);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(component));
    }

    // ── IGameManager implementation ───────────────────────────────

    @Override
    public GameState getGameState() { return gameState; }

    @Override
    public boolean isPvpEnabled() { return pvpEnabled; }

    @Override
    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
        config.savePvpEnabled(pvpEnabled);
    }

    @Override
    public List<PlayerData> getAliveRunners() {
        return Bukkit.getOnlinePlayers().stream()
                .map(p -> dataLoader.getPlayerData(p))
                .filter(d -> d.getTeam() == Team.RUNNER && !d.isEliminated())
                .collect(Collectors.toList());
    }

    @Override
    public List<PlayerData> getAllRunners() {
        return Bukkit.getOnlinePlayers().stream()
                .map(p -> dataLoader.getPlayerData(p))
                .filter(d -> d.getTeam() == Team.RUNNER)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlayerData> getAllHunters() {
        return Bukkit.getOnlinePlayers().stream()
                .map(p -> dataLoader.getPlayerData(p))
                .filter(d -> d.getTeam() == Team.HUNTER)
                .collect(Collectors.toList());
    }

    @Override
    public int getAliveRunnerCount() {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> { PlayerData d = dataLoader.getPlayerData(p);
                    return d.getTeam() == Team.RUNNER && !d.isEliminated(); })
                .count();
    }

    @Override
    public int getTotalRunnerCount() {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> dataLoader.getPlayerData(p).getTeam() == Team.RUNNER).count();
    }

    @Override
    public int getTotalHunterCount() {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> dataLoader.getPlayerData(p).getTeam() == Team.HUNTER).count();
    }

    // ── Package-level accessors ───────────────────────────────────

    public PlayerDataLoader getDataLoader() { return dataLoader; }
    public HuntCoreConfig getConfig() { return config; }
    public Location getCryingObsidianLocation() { return cryingObsidianLocation; }
    public Location getGameSpawnLocation() { return gameSpawnLocation; }
    public CageBuilder getCageBuilder() { return cageBuilder; }
    public long getGameStartTime() { return gameStartTime; }
    public int getLastGameVoteCount()   { return lastGameVoteCount; }
    public int getLastGameOnlineCount() { return lastGameOnlineCount; }

    /** How many players have cast a lobby vote. */
    public int getVoteCount() { return lobbyVotes.size(); }

    public boolean isGlowActive() { return glowActive; }

    // ── Title / Sound helpers ─────────────────────────────────────

    private void broadcastTitle(TitleConfig cfg) {
        if (cfg == null) return;
        Title title = Title.title(
                MessageUtils.colorize(cfg.title()),
                MessageUtils.colorize(cfg.subtitle()),
                Title.Times.times(Ticks.duration(cfg.fadeIn()), Ticks.duration(cfg.stay()), Ticks.duration(cfg.fadeOut()))
        );
        Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(title));
    }

    private void sendTitle(Player player, TitleConfig cfg) {
        if (cfg == null) return;
        Title title = Title.title(
                MessageUtils.colorize(cfg.title()),
                MessageUtils.colorize(cfg.subtitle()),
                Title.Times.times(Ticks.duration(cfg.fadeIn()), Ticks.duration(cfg.stay()), Ticks.duration(cfg.fadeOut()))
        );
        player.showTitle(title);
    }

    private void broadcastSound(String soundName) {
        if (soundName == null || soundName.isBlank()) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float vol = config.getSoundVolume();
            float pitch = config.getSoundPitch();
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), sound, vol, pitch));
        } catch (IllegalArgumentException ignored) {
            // Invalid sound name in config — silently skip
        }
    }
}
