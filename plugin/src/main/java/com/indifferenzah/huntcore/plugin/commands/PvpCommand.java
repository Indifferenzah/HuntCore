package com.indifferenzah.huntcore.plugin.commands;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.plugin.game.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("pvp")
@Description("PvP management")
@CommandPermission("huntcore.pvp")
public class PvpCommand {

    private final GameManager gameManager;
    private final HuntCoreConfig config;

    public PvpCommand(GameManager gameManager, HuntCoreConfig config) {
        this.gameManager = gameManager;
        this.config = config;
    }

    @Subcommand("enable")
    public void enable(CommandSender sender) {
        gameManager.setPvpEnabled(true);
        Component msg = LegacyComponentSerializer.legacyAmpersand()
                .deserialize(config.getPrefix() + config.getMsgPvpEnabled());
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(msg);
        }
    }

    @Subcommand("disable")
    public void disable(CommandSender sender) {
        gameManager.setPvpEnabled(false);
        Component msg = LegacyComponentSerializer.legacyAmpersand()
                .deserialize(config.getPrefix() + config.getMsgPvpDisabled());
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(msg);
        }
    }
}
