package com.indifferenzah.huntcore.plugin.listener;

import com.indifferenzah.huntcore.api.model.GameState;
import com.indifferenzah.huntcore.api.model.Team;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import com.indifferenzah.huntcore.plugin.display.BossBarManager;
import com.indifferenzah.huntcore.plugin.display.ScoreboardManager;
import com.indifferenzah.huntcore.plugin.display.TabManager;
import com.indifferenzah.huntcore.plugin.game.GameManager;
import com.indifferenzah.huntcore.plugin.utils.MessageUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class LobbyListener implements Listener {

    private final GameManager gameManager;
    private final PlayerDataLoader dataLoader;
    private final ScoreboardManager scoreboardManager;
    private final TabManager tabManager;
    private final BossBarManager bossBarManager;

    public LobbyListener(GameManager gameManager, PlayerDataLoader dataLoader,
                         ScoreboardManager scoreboardManager, TabManager tabManager,
                         BossBarManager bossBarManager) {
        this.gameManager = gameManager;
        this.dataLoader = dataLoader;
        this.scoreboardManager = scoreboardManager;
        this.tabManager = tabManager;
        this.bossBarManager = bossBarManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dataLoader.loadPlayerData(player);
        scoreboardManager.createScoreboard(player);
        tabManager.updatePlayerName(player);
        if (bossBarManager != null) bossBarManager.addPlayer(player);

        GameState state = gameManager.getGameState();
        // Guarantee no stale team from a previous game when joining outside of RUNNING
        if (state != GameState.RUNNING) {
            dataLoader.getPlayerData(player).setTeam(com.indifferenzah.huntcore.api.model.Team.NONE);
        }
        if (state == GameState.RUNNING) {
            if (!gameManager.tryRestoreReconnecting(player)) {
                // Brand-new joiner during an active game → spectator
                player.setGameMode(org.bukkit.GameMode.SPECTATOR);
                player.sendMessage(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                        .legacyAmpersand().deserialize("&8[&cHuntCore&8] &7Partita in corso &8— &7sei in spectator."));
            }
        } else if (state == GameState.COUNTDOWN) {
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
            player.sendMessage(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().deserialize("&8[&cHuntCore&8] &7Partita in corso &8— &7sei in spectator."));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        gameManager.onPlayerQuit(player);
        scoreboardManager.removeScoreboard(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GameState state = gameManager.getGameState();

        // Allow opening vote menu during LOBBY and COUNTDOWN (vote change)
        if (state != GameState.LOBBY && state != GameState.COUNTDOWN) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        openTeamMenu(player);
        event.setCancelled(true);
    }

    private void openTeamMenu(Player player) {
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(
                null, 27,
                LegacyComponentSerializer.legacyAmpersand().deserialize("&eScegli il tuo team")
        );

        ItemStack runnerItem = new ItemStack(Material.YELLOW_WOOL);
        var runnerMeta = runnerItem.getItemMeta();
        if (runnerMeta != null) {
            runnerMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&eRunner"));
            runnerItem.setItemMeta(runnerMeta);
        }
        inv.setItem(11, runnerItem);

        ItemStack hunterItem = new ItemStack(Material.RED_WOOL);
        var hunterMeta = hunterItem.getItemMeta();
        if (hunterMeta != null) {
            hunterMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&cHunter"));
            hunterItem.setItemMeta(hunterMeta);
        }
        inv.setItem(15, hunterItem);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        GameState state = gameManager.getGameState();
        if (state != GameState.LOBBY && state != GameState.COUNTDOWN) return;

        String title = LegacyComponentSerializer.legacyAmpersand()
                .serialize(event.getView().title());
        if (!title.contains("Scegli il tuo team")) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        if (slot == 11) gameManager.onPlayerVote(player, Team.RUNNER);
        else if (slot == 15) gameManager.onPlayerVote(player, Team.HUNTER);
    }

    /** Prevent players from dropping the compass. */
    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == Material.COMPASS) {
            event.setCancelled(true);
        }
    }
}
