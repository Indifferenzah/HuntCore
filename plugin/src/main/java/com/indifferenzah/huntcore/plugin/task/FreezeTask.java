package com.indifferenzah.huntcore.plugin.task;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.api.model.PlayerData;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import com.indifferenzah.huntcore.plugin.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Runs every second during the hunter freeze phase.
 * Shows a countdown on the action bar and unfreezes hunters when time is up.
 */
public class FreezeTask extends BukkitRunnable {

    private final PlayerDataLoader dataLoader;
    private final HuntCoreConfig config;
    private final List<Player> frozenHunters;
    private int remaining;

    public FreezeTask(PlayerDataLoader dataLoader, HuntCoreConfig config, List<Player> frozenHunters) {
        this.dataLoader = dataLoader;
        this.config = config;
        this.frozenHunters = frozenHunters;
        this.remaining = config.getHunterFreezeSeconds();
    }

    /** Freezes all hunters in the list immediately. */
    public void freezeAll() {
        String msg = config.getMsgHunterFreeze()
                .replace("{seconds}", String.valueOf(config.getHunterFreezeSeconds()));
        for (Player hunter : frozenHunters) {
            if (!hunter.isOnline()) continue;
            PlayerData data = dataLoader.getPlayerData(hunter);
            data.setFrozen(true);
            hunter.setWalkSpeed(0f);
            hunter.setFlySpeed(0f);
            hunter.getInventory().clear();
            hunter.sendMessage(MessageUtils.colorize(config.getPrefix() + msg));
        }
    }

    public void unFreeze(Player player) {
        PlayerData data = dataLoader.getPlayerData(player);
        if (!data.isFrozen()) return;
        data.setFrozen(false);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
    }

    public void unFreezeAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            unFreeze(p);
        }
    }

    @Override
    public void run() {
        if (remaining <= 0) {
            for (Player hunter : frozenHunters) {
                if (!hunter.isOnline()) continue;
                unFreeze(hunter);
                hunter.sendMessage(MessageUtils.colorize(config.getPrefix() + config.getMsgHunterUnfreeze()));
            }
            this.cancel();
            return;
        }

        for (Player hunter : frozenHunters) {
            if (!hunter.isOnline()) continue;
            PlayerData data = dataLoader.getPlayerData(hunter);
            if (!data.isFrozen()) continue;
            hunter.sendActionBar(MessageUtils.colorize("&cFrozen! Via tra &e" + remaining + "s"));
        }

        remaining--;
    }
}
