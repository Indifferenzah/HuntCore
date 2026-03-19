package com.indifferenzah.huntcore.plugin.task;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.api.model.GameState;
import com.indifferenzah.huntcore.api.model.PlayerData;
import com.indifferenzah.huntcore.api.model.Team;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import com.indifferenzah.huntcore.plugin.game.GameManager;
import com.indifferenzah.huntcore.plugin.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CompassTask extends BukkitRunnable {

    private final GameManager gameManager;
    private final PlayerDataLoader dataLoader;
    private final HuntCoreConfig config;

    public CompassTask(org.bukkit.plugin.java.JavaPlugin plugin,
                       GameManager gameManager,
                       PlayerDataLoader dataLoader,
                       HuntCoreConfig config) {
        this.gameManager = gameManager;
        this.dataLoader = dataLoader;
        this.config = config;
    }

    @Override
    public void run() {
        if (gameManager.getGameState() != GameState.RUNNING) return;

        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            PlayerData data = dataLoader.getPlayerData(player);

            if (data.getTeam() == Team.HUNTER && !data.isFrozen()) {
                // Hunters: compass → nearest alive runner
                Player target = getNearestAliveRunner(player);
                if (target != null) {
                    player.setCompassTarget(target.getLocation());
                    player.sendActionBar(MessageUtils.colorize(
                            "&c» &f" + target.getName() + " &c«  &7(" +
                            (int) player.getLocation().distance(target.getLocation()) + "m)"));
                } else {
                    // No runners alive — point to crying obsidian anyway
                    Location obj = gameManager.getCryingObsidianLocation();
                    if (obj != null) player.setCompassTarget(obj);
                }

            } else if (data.getTeam() == Team.RUNNER) {
                // Runners: compass → crying obsidian (objective)
                Location obj = gameManager.getCryingObsidianLocation();
                if (obj != null) player.setCompassTarget(obj);
            }
        }
    }

    private Player getNearestAliveRunner(Player hunter) {
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            PlayerData d = dataLoader.getPlayerData(p);
            if (d.getTeam() == Team.RUNNER && !d.isEliminated()) {
                double dist = p.getLocation().distanceSquared(hunter.getLocation());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = p;
                }
            }
        }
        return nearest;
    }


}
