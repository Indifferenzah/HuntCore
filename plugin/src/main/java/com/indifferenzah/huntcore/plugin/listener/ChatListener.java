package com.indifferenzah.huntcore.plugin.listener;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.api.model.PlayerData;
import com.indifferenzah.huntcore.api.model.Team;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import com.indifferenzah.huntcore.plugin.game.GameManager;
import com.indifferenzah.huntcore.plugin.utils.MessageUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final GameManager gameManager;
    private final PlayerDataLoader dataLoader;
    private final HuntCoreConfig config;

    public ChatListener(GameManager gameManager, PlayerDataLoader dataLoader, HuntCoreConfig config) {
        this.gameManager = gameManager;
        this.dataLoader = dataLoader;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        PlayerData data = dataLoader.getPlayerData(player);
        Team team = data.getTeam();

        String format;
        if (team == Team.RUNNER) {
            format = config.getChatFormatRunner();
        } else if (team == Team.HUNTER) {
            format = config.getChatFormatHunter();
        } else {
            format = config.getChatFormatNoTeam();
        }

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        format = format.replace("%message%", message);
        format = MessageUtils.replacePlaceholders(format, player, gameManager);

        Component finalMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(format);

        for (Player recipient : org.bukkit.Bukkit.getOnlinePlayers()) {
            recipient.sendMessage(finalMessage);
        }
    }
}
