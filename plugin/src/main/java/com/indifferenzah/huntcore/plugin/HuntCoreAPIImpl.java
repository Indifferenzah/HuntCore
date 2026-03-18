package com.indifferenzah.huntcore.plugin;

import com.indifferenzah.huntcore.api.HuntCoreAPI;
import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.api.service.IGameManager;
import com.indifferenzah.huntcore.api.service.IPlayerDataLoader;
import com.indifferenzah.huntcore.plugin.commands.*;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import com.indifferenzah.huntcore.plugin.database.DatabaseManager;
import com.indifferenzah.huntcore.plugin.display.BossBarManager;
import com.indifferenzah.huntcore.plugin.display.ScoreboardManager;
import com.indifferenzah.huntcore.plugin.display.TabManager;
import com.indifferenzah.huntcore.plugin.game.GameManager;
import com.indifferenzah.huntcore.plugin.listener.ChatListener;
import com.indifferenzah.huntcore.plugin.listener.GameListener;
import com.indifferenzah.huntcore.plugin.listener.LobbyListener;
import com.indifferenzah.huntcore.plugin.listener.PlayerMoveListener;
import com.indifferenzah.huntcore.plugin.task.ScoreboardTask;
import com.indifferenzah.huntcore.plugin.task.TabTask;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.Lamp;

public class HuntCoreAPIImpl extends HuntCoreAPI {

    private final DatabaseManager databaseManager;
    private final PlayerDataLoader dataLoader;
    private final GameManager gameManager;
    private final ScoreboardManager scoreboardManager;
    private final TabManager tabManager;
    private final BossBarManager bossBarManager;
    private final HuntCoreConfig config;

    public HuntCoreAPIImpl(JavaPlugin plugin) {
        // 1. Config
        plugin.saveDefaultConfig();
        this.config = new HuntCoreConfig(plugin);

        // 2. Database
        this.databaseManager = new DatabaseManager(plugin);

        // 3. Data loader
        this.dataLoader = new PlayerDataLoader(plugin, databaseManager);

        // 4. Game manager
        this.gameManager = new GameManager(plugin, dataLoader, config);

        // 5. Display managers
        this.bossBarManager = new BossBarManager(config, gameManager);
        this.scoreboardManager = new ScoreboardManager(gameManager, config);
        this.tabManager = new TabManager(gameManager, dataLoader, config);

        // Link bossbar to game manager
        gameManager.setBossBarManager(bossBarManager);

        // 6. Register listeners
        var pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new LobbyListener(gameManager, dataLoader, scoreboardManager, tabManager, bossBarManager), plugin);
        pm.registerEvents(new GameListener(gameManager, dataLoader), plugin);
        pm.registerEvents(new ChatListener(gameManager, dataLoader, config), plugin);
        pm.registerEvents(new PlayerMoveListener(dataLoader), plugin);

        // 7. Start periodic tasks (always running — managers decide what to show per state)
        new ScoreboardTask(scoreboardManager, bossBarManager)
                .runTaskTimer(plugin, 20L, config.getScoreboardUpdateInterval());
        new TabTask(tabManager)
                .runTaskTimer(plugin, 20L, config.getTabUpdateInterval());

        // 8. Register Lamp commands
        Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(plugin).build();
        lamp.register(
                new HuntCoreCommand(config, databaseManager, dataLoader),
                new GameCommand(gameManager, config),
                new TeamsCommand(dataLoader, config, scoreboardManager, tabManager),
                new PvpCommand(gameManager, config),
                new SetSpawnCommand(config),
                new RespawnCommand(dataLoader, config),
                new StatsCommand(plugin, dataLoader, databaseManager, config)
        );

        // 9. Set API instance
        HuntCoreAPI.set(this);
    }

    @Override
    public IGameManager getGameManager() { return gameManager; }

    @Override
    public IPlayerDataLoader getDataLoader() { return dataLoader; }

    public DatabaseManager getDatabaseManager() { return databaseManager; }
}
