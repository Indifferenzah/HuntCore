package com.indifferenzah.huntcore.plugin.commands;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import com.indifferenzah.huntcore.plugin.database.DatabaseManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class HuntCoreCommand {

    private final HuntCoreConfig config;
    private final DatabaseManager databaseManager;
    private final PlayerDataLoader dataLoader;

    private static final LegacyComponentSerializer SERIAL = LegacyComponentSerializer.legacyAmpersand();

    public HuntCoreCommand(HuntCoreConfig config, DatabaseManager databaseManager, PlayerDataLoader dataLoader) {
        this.config = config;
        this.databaseManager = databaseManager;
        this.dataLoader = dataLoader;
    }

    private void send(CommandSender sender, String text) {
        sender.sendMessage(SERIAL.deserialize(text));
    }

    @Command("huntcore")
    @Description("Info sul plugin")
    public void root(CommandSender sender) {
        send(sender, "&7Made with \u2764 by &cIndifferenzah&7.");
    }

    @Command("huntcore help")
    @Description("Lista comandi")
    public void help(CommandSender sender) {
        send(sender, "&8&m                    ");
        send(sender, "  &c&lHuntCore &8- &7Comandi disponibili");
        send(sender, "&8&m                    ");
        send(sender, "  &e/game start|stop &8» &7Avvia/ferma la partita");
        send(sender, "  &e/game &8» &7Stato della partita");
        send(sender, "  &e/teams set <giocatore> <runner|hunter> &8» &7Assegna team");
        send(sender, "  &e/teams clear <giocatore> &8» &7Rimuovi team");
        send(sender, "  &e/pvp enable|disable &8» &7Abilita/disabilita PvP");
        send(sender, "  &e/setspawn block|end &8» &7Imposta spawn");
        send(sender, "  &e/respawn <giocatore> &8» &7Riporta un eliminato in partita");
        send(sender, "  &e/stats [giocatore] &8» &7Statistiche di un giocatore");
        send(sender, "  &e/huntcore reload &8» &7Ricarica config.yml");
        send(sender, "  &e/huntcore cleardb &8» &7Resetta il database");
        send(sender, "&8&m                    ");
    }

    @Command("huntcore reload")
    @Description("Ricarica la configurazione")
    @CommandPermission("huntcore.admin")
    public void reload(CommandSender sender) {
        config.load();
        send(sender, config.getPrefix() + config.getMsgReloadSuccess());
    }

    @Command("huntcore cleardb")
    @Description("Resetta il database")
    @CommandPermission("huntcore.admin")
    public void clearDb(CommandSender sender) {
        databaseManager.clearAll();
        dataLoader.reloadAll();
        send(sender, config.getPrefix() + config.getMsgDbCleared());
    }
}
