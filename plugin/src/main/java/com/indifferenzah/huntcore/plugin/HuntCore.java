package com.indifferenzah.huntcore.plugin;

import com.indifferenzah.huntcore.plugin.database.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HuntCore extends JavaPlugin {

    private HuntCoreAPIImpl api;

    @Override
    public void onEnable() {
        getLogger().info("HuntCore enabling...");
        api = new HuntCoreAPIImpl(this);
        getLogger().info("HuntCore enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("HuntCore disabling...");
        if (api != null) {
            api.getDatabaseManager().close();
        }
        getLogger().info("HuntCore disabled.");
    }
}
