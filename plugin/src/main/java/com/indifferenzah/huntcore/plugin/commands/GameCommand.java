package com.indifferenzah.huntcore.plugin.commands;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.api.model.GameState;
import com.indifferenzah.huntcore.plugin.game.GameManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("game")
@Description("Game management commands")
@CommandPermission("huntcore.game")
public class GameCommand {

    private final GameManager gameManager;
    private final HuntCoreConfig config;

    public GameCommand(GameManager gameManager, HuntCoreConfig config) {
        this.gameManager = gameManager;
        this.config = config;
    }

    @Subcommand("start")
    public void start(CommandSender sender) {
        if (gameManager.getGameState() != GameState.IDLE) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(config.getPrefix() + config.getMsgGameAlreadyRunning()));
            return;
        }

        if (!config.isBlockSpawnSet()) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(config.getPrefix() + config.getMsgNoBlockSpawnSet()));
            return;
        }

        gameManager.startGame();
    }

    @Subcommand("stop")
    public void stop(CommandSender sender) {
        if (gameManager.getGameState() == GameState.IDLE) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(config.getPrefix() + config.getMsgGameNotRunning()));
            return;
        }

        gameManager.stopGame();
    }
}
