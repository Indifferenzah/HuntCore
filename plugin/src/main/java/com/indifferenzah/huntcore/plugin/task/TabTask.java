package com.indifferenzah.huntcore.plugin.task;

import com.indifferenzah.huntcore.plugin.display.TabManager;
import org.bukkit.scheduler.BukkitRunnable;

public class TabTask extends BukkitRunnable {

    private final TabManager tabManager;

    public TabTask(TabManager tabManager) {
        this.tabManager = tabManager;
    }

    @Override
    public void run() {
        tabManager.updateAll();
    }
}
