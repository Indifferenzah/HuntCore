package com.indifferenzah.huntcore.plugin.commands;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.api.model.Team;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import com.indifferenzah.huntcore.plugin.display.ScoreboardManager;
import com.indifferenzah.huntcore.plugin.display.TabManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("teams")
@Description("Team management commands")
@CommandPermission("huntcore.teams")
public class TeamsCommand {

    private final PlayerDataLoader dataLoader;
    private final HuntCoreConfig config;
    private final ScoreboardManager scoreboardManager;
    private final TabManager tabManager;

    public TeamsCommand(PlayerDataLoader dataLoader, HuntCoreConfig config,
                        ScoreboardManager scoreboardManager, TabManager tabManager) {
        this.dataLoader = dataLoader;
        this.config = config;
        this.scoreboardManager = scoreboardManager;
        this.tabManager = tabManager;
    }

    @Subcommand("set")
    public void setTeam(CommandSender sender, String playerName, String teamName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(config.getPrefix() + config.getMsgPlayerNotFound()));
            return;
        }

        Team team;
        try {
            team = Team.valueOf(teamName.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(config.getPrefix() + "&cTeam non valido. Usa: runner o hunter"));
            return;
        }

        if (team == Team.NONE) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(config.getPrefix() + "&cTeam non valido. Usa: runner o hunter"));
            return;
        }

        dataLoader.getPlayerData(target).setTeam(team);
        scoreboardManager.updateAll();
        tabManager.updateAll();

        String msg = config.getMsgTeamSet()
                .replace("{player}", target.getName())
                .replace("{team}", team.name());
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(config.getPrefix() + msg));
    }

    @Subcommand("clear")
    public void clearTeam(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(config.getPrefix() + config.getMsgPlayerNotFound()));
            return;
        }

        dataLoader.getPlayerData(target).setTeam(Team.NONE);
        scoreboardManager.updateAll();
        tabManager.updateAll();

        String msg = config.getMsgTeamCleared().replace("{player}", target.getName());
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(config.getPrefix() + msg));
    }
}
