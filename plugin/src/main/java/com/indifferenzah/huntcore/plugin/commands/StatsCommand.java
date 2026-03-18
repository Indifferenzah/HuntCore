package com.indifferenzah.huntcore.plugin.commands;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.api.model.PlayerData;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import com.indifferenzah.huntcore.plugin.database.DatabaseManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;

public class StatsCommand {

    private final JavaPlugin plugin;
    private final PlayerDataLoader dataLoader;
    private final DatabaseManager databaseManager;
    private final HuntCoreConfig config;

    private static final LegacyComponentSerializer SERIAL = LegacyComponentSerializer.legacyAmpersand();

    public StatsCommand(JavaPlugin plugin, PlayerDataLoader dataLoader,
                        DatabaseManager databaseManager, HuntCoreConfig config) {
        this.plugin = plugin;
        this.dataLoader = dataLoader;
        this.databaseManager = databaseManager;
        this.config = config;
    }

    /** /stats — shows the sender's own stats */
    @Command("stats")
    @Description("Visualizza le tue statistiche")
    public void ownStats(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(SERIAL.deserialize(config.getPrefix() + "&cUsa /stats <giocatore> dalla console."));
            return;
        }
        showStats(sender, player.getName(), dataLoader.getPlayerData(player));
    }

    /** /stats <player> — shows another player's stats (online or offline) */
    @Command("stats")
    @Description("Visualizza le statistiche di un giocatore")
    public void targetStats(CommandSender sender, String player) {
        // Try online first
        Player online = Bukkit.getPlayer(player);
        if (online != null) {
            showStats(sender, online.getName(), dataLoader.getPlayerData(online));
            return;
        }

        // Offline: query DB async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerData data = databaseManager.loadStatsByName(player);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (data == null) {
                    sender.sendMessage(SERIAL.deserialize(config.getPrefix() + config.getMsgPlayerNotFound()));
                } else {
                    showStats(sender, player, data);
                }
            });
        });
    }

    private void showStats(CommandSender sender, String name, PlayerData data) {
        String kd = data.getDeaths() == 0
                ? String.format("%.1f", (double) data.getKills())
                : String.format("%.2f", (double) data.getKills() / data.getDeaths());

        String winRate = data.getGames() == 0 ? "0%"
                : String.format("%.0f%%", (double) data.getWins() / data.getGames() * 100);

        sender.sendMessage(SERIAL.deserialize("&8&m                      "));
        sender.sendMessage(SERIAL.deserialize("  &c&l" + name + " &8— &7Statistiche"));
        sender.sendMessage(SERIAL.deserialize("&8&m                      "));
        sender.sendMessage(SERIAL.deserialize("  &ePartite totali  &8» &f" + data.getGames()
                + " &8(&bRunner: &f" + data.getRunnerGames()
                + "  &cHunter: &f" + data.getHunterGames() + "&8)"));
        sender.sendMessage(SERIAL.deserialize("  &eVittorie         &8» &a" + data.getWins()
                + " &8| &eSconfitte &8» &c" + data.getLosses()
                + "  &8(&7" + winRate + " win-rate&8)"));
        sender.sendMessage(SERIAL.deserialize("  &eKill             &8» &f" + data.getKills()));
        sender.sendMessage(SERIAL.deserialize("  &eMorti            &8» &f" + data.getDeaths()));
        sender.sendMessage(SERIAL.deserialize("  &eK/D              &8» &f" + kd));
        sender.sendMessage(SERIAL.deserialize("&8&m                      "));
    }
}
