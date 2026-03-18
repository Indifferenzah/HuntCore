package com.indifferenzah.huntcore.plugin.task;

import com.indifferenzah.huntcore.plugin.display.BossBarManager;
import com.indifferenzah.huntcore.plugin.display.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardTask extends BukkitRunnable {

    private final ScoreboardManager scoreboardManager;
    private final BossBarManager bossBarManager;

    public ScoreboardTask(ScoreboardManager scoreboardManager, BossBarManager bossBarManager) {
        this.scoreboardManager = scoreboardManager;
        this.bossBarManager = bossBarManager;
    }

    @Override
    public void run() {
        scoreboardManager.updateAll();

        // Update bossbar text using any online player as context
        if (bossBarManager != null && bossBarManager.isActive()) {
            Bukkit.getOnlinePlayers().stream().findFirst()
                    .ifPresent(bossBarManager::updateText);
        }
    }
}
