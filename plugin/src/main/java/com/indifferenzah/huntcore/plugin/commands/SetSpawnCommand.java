package com.indifferenzah.huntcore.plugin.commands;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("setspawn")
@Description("Set spawn locations")
@CommandPermission("huntcore.setspawn")
public class SetSpawnCommand {

    private final HuntCoreConfig config;

    public SetSpawnCommand(HuntCoreConfig config) {
        this.config = config;
    }

    @Subcommand("block")
    public void setBlock(Player sender) {
        config.saveBlockSpawn(sender.getLocation());
        String msg = config.getMsgSpawnSet().replace("{type}", "block");
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(config.getPrefix() + msg));
    }

    @Subcommand("end")
    public void setEnd(Player sender) {
        config.saveEndSpawn(sender.getLocation());
        String msg = config.getMsgSpawnSet().replace("{type}", "end");
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(config.getPrefix() + msg));
    }
}
