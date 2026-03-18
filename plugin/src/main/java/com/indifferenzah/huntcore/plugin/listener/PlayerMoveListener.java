package com.indifferenzah.huntcore.plugin.listener;

import com.indifferenzah.huntcore.api.model.PlayerData;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final PlayerDataLoader dataLoader;

    public PlayerMoveListener(PlayerDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData data = dataLoader.getPlayerData(player);
        if (!data.isFrozen()) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        // Allow head rotation but block position change
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Location blocked = from.clone();
        blocked.setYaw(to.getYaw());
        blocked.setPitch(to.getPitch());
        event.setTo(blocked);
    }
}
