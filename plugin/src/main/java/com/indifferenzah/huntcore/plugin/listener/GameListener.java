package com.indifferenzah.huntcore.plugin.listener;

import com.indifferenzah.huntcore.api.model.GameState;
import com.indifferenzah.huntcore.api.model.PlayerData;
import com.indifferenzah.huntcore.api.model.Team;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import com.indifferenzah.huntcore.plugin.game.GameManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class GameListener implements Listener {

    private final GameManager gameManager;
    private final PlayerDataLoader dataLoader;

    public GameListener(GameManager gameManager, PlayerDataLoader dataLoader) {
        this.gameManager = gameManager;
        this.dataLoader = dataLoader;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        // Frozen players cannot break blocks
        if (dataLoader.getPlayerData(event.getPlayer()).isFrozen()) {
            event.setCancelled(true);
            return;
        }
        // Protect cage blocks at all times
        if (gameManager.getCageBuilder().isCageBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }

        if (gameManager.getGameState() != GameState.RUNNING) return;

        Location cryingLoc = gameManager.getCryingObsidianLocation();
        if (cryingLoc == null) return;
        if (event.getBlock().getType() != Material.CRYING_OBSIDIAN) return;
        if (!isSameBlock(event.getBlock().getLocation(), cryingLoc)) return;

        Player player = event.getPlayer();
        PlayerData data = dataLoader.getPlayerData(player);

        if (data.getTeam() != Team.RUNNER) {
            event.setCancelled(true);
            return;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() != Material.DIAMOND_PICKAXE) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);
        gameManager.onCryingObsidianBreak();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (dataLoader.getPlayerData(event.getPlayer()).isFrozen()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Frozen players cannot hit
        if (event.getDamager() instanceof Player damager
                && dataLoader.getPlayerData(damager).isFrozen()) {
            event.setCancelled(true);
            return;
        }
        GameState state = gameManager.getGameState();
        // Block all PvP outside of RUNNING, or when PvP is disabled
        if (state == GameState.ENDED || state == GameState.IDLE || state == GameState.LOBBY || state == GameState.COUNTDOWN) {
            if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
                event.setCancelled(true);
                return;
            }
        }
        if (state == GameState.RUNNING && !gameManager.isPvpEnabled()) {
            if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
                event.setCancelled(true);
            }
        }
    }

    /** Prevent fall damage while frozen or in ENDED state. */
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageGeneric(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        GameState state = gameManager.getGameState();
        if (state == GameState.ENDED) {
            event.setCancelled(true);
            return;
        }

        PlayerData data = dataLoader.getPlayerData(player);
        if (data.isFrozen()) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (gameManager.getGameState() != GameState.RUNNING) return;

        Player player = event.getEntity();
        PlayerData data = dataLoader.getPlayerData(player);

        if (data.getTeam() == Team.RUNNER) {
            gameManager.eliminateRunner(player);

            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                dataLoader.getPlayerData(killer).addKill();
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (gameManager.getGameState() != GameState.RUNNING) return;
        Player player = event.getPlayer();
        PlayerData data = dataLoader.getPlayerData(player);
        if (data.getTeam() != Team.HUNTER) return;
        Location spawnLoc = gameManager.getGameSpawnLocation();
        if (spawnLoc != null) event.setRespawnLocation(spawnLoc);
    }

    private boolean isSameBlock(Location a, Location b) {
        return a.getBlockX() == b.getBlockX()
                && a.getBlockY() == b.getBlockY()
                && a.getBlockZ() == b.getBlockZ()
                && a.getWorld().equals(b.getWorld());
    }
}
