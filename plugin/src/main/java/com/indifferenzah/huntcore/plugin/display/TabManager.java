package com.indifferenzah.huntcore.plugin.display;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.api.model.GameState;
import com.indifferenzah.huntcore.api.model.PlayerData;
import com.indifferenzah.huntcore.api.model.Team;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import com.indifferenzah.huntcore.plugin.game.GameManager;
import com.indifferenzah.huntcore.plugin.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class TabManager {

    private final GameManager gameManager;
    private final PlayerDataLoader dataLoader;
    private final HuntCoreConfig config;

    public TabManager(GameManager gameManager, PlayerDataLoader dataLoader, HuntCoreConfig config) {
        this.gameManager = gameManager;
        this.dataLoader = dataLoader;
        this.config = config;
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTab(player);
            updatePlayerName(player);
        }
    }

    public void updateTab(Player viewer) {
        GameState state = gameManager.getGameState();

        List<String> headerLines;
        List<String> footerLines;

        switch (state) {
            case IDLE, LOBBY, COUNTDOWN -> {
                if (config.isLobbyTabEnabled()) {
                    headerLines = config.getLobbyTabHeader();
                    footerLines = config.getLobbyTabFooter();
                } else if (config.isTabEnabled()) {
                    headerLines = config.getTabHeader();
                    footerLines = config.getTabFooter();
                } else {
                    return;
                }
            }
            case RUNNING -> {
                if (!config.isTabEnabled()) return;
                headerLines = config.getTabHeader();
                footerLines = config.getTabFooter();
            }
            case ENDED -> {
                if (config.isEndedTabEnabled()) {
                    headerLines = config.getEndedTabHeader();
                    footerLines = config.getEndedTabFooter();
                } else {
                    return;
                }
            }
            default -> {
                return;
            }
        }

        Component header = buildComponent(headerLines, viewer);
        Component footer = buildComponent(footerLines, viewer);
        viewer.sendPlayerListHeaderAndFooter(header, footer);
    }

    public void updatePlayerName(Player player) {
        GameState state = gameManager.getGameState();
        PlayerData data = dataLoader.getPlayerData(player);
        Team team = data.getTeam();

        String format;

        if (state == GameState.IDLE || state == GameState.LOBBY || state == GameState.COUNTDOWN) {
            format = switch (team) {
                case RUNNER -> config.getLobbyTabNameFormatRunner();
                case HUNTER -> config.getLobbyTabNameFormatHunter();
                default -> config.getLobbyTabNameFormatNoTeam();
            };
        } else {
            format = switch (team) {
                case RUNNER -> config.getTabNameFormatRunner();
                case HUNTER -> config.getTabNameFormatHunter();
                default -> config.getTabNameFormatNoTeam();
            };
        }

        String processed = MessageUtils.replacePlaceholders(format, player, gameManager);
        Component nameComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(processed);
        // Only set list name, not display name (avoid double-prefix in chat)
        player.playerListName(nameComponent);
    }

    private Component buildComponent(List<String> lines, Player viewer) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append("\n");
            sb.append(MessageUtils.replacePlaceholders(lines.get(i), viewer, gameManager));
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(sb.toString());
    }
}
