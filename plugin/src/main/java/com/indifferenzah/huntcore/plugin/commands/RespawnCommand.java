package com.indifferenzah.huntcore.plugin.commands;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.api.model.PlayerData;
import com.indifferenzah.huntcore.api.model.Team;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class RespawnCommand {

    private final PlayerDataLoader dataLoader;
    private final HuntCoreConfig config;

    public RespawnCommand(PlayerDataLoader dataLoader, HuntCoreConfig config) {
        this.dataLoader = dataLoader;
        this.config = config;
    }

    @Command("respawn")
    @Description("Rispawna un runner eliminato")
    @CommandPermission("huntcore.respawn")
    public void respawn(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(config.getPrefix() + config.getMsgPlayerNotFound()));
            return;
        }

        PlayerData data = dataLoader.getPlayerData(target);

        if (data.getTeam() != Team.RUNNER) {
            String msg = config.getMsgPlayerNotRunner().replace("{player}", target.getName());
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(config.getPrefix() + msg));
            return;
        }

        if (!data.isEliminated()) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(config.getPrefix() + "&c" + target.getName() + " non e eliminato."));
            return;
        }

        data.setEliminated(false);
        target.setGameMode(GameMode.SURVIVAL);

        if (data.getSpawnLocation() != null) {
            target.teleport(data.getSpawnLocation());
        }

        String msg = config.getMsgRunnerRespawned().replace("{player}", target.getName());
        Component broadcast = LegacyComponentSerializer.legacyAmpersand()
                .deserialize(config.getPrefix() + msg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(broadcast);
        }
    }
}
