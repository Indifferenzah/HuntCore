package com.indifferenzah.huntcore.plugin.utils;

import com.indifferenzah.huntcore.api.model.PlayerData;
import com.indifferenzah.huntcore.api.model.Team;
import com.indifferenzah.huntcore.plugin.game.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageUtils {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static Component colorize(String text) {
        return SERIALIZER.deserialize(text);
    }

    public static String colorizeToString(String text) {
        return text.replace("&", "§");
    }

    public static String replacePlaceholders(String text, Player player, GameManager gameManager) {
        if (text == null) return "";

        PlayerData data = gameManager.getDataLoader().getPlayerData(player);
        Team team = data.getTeam();

        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String time = new SimpleDateFormat("HH:mm").format(new Date());

        double health = Math.max(0, player.getHealth());
        String healthFormatted = String.format("%.1f", health);

        String kd = data.getDeaths() == 0
                ? String.format("%.1f", (double) data.getKills())
                : String.format("%.1f", (double) data.getKills() / data.getDeaths());

        String healthSuffix = "";
        if (gameManager.getConfig().isTabShowHealth()) {
            healthSuffix = gameManager.getConfig().getTabHealthSuffixFormat()
                    .replace("%health%", healthFormatted);
        }

        String pvpStatus = gameManager.isPvpEnabled() ? "&aAttivo" : "&cDisattivo";

        // Game timer
        String gameTimer = "--:--";
        long startTime = gameManager.getGameStartTime();
        if (startTime > 0) {
            long elapsed = System.currentTimeMillis() - startTime;
            long minutes = elapsed / 60000;
            long seconds = (elapsed / 1000) % 60;
            gameTimer = String.format("%02d:%02d", minutes, seconds);
        }

        // Lobby placeholders — freeze at game-start values during/after game
        com.indifferenzah.huntcore.api.model.GameState state = gameManager.getGameState();
        boolean inLobby = state == com.indifferenzah.huntcore.api.model.GameState.LOBBY
                       || state == com.indifferenzah.huntcore.api.model.GameState.COUNTDOWN
                       || state == com.indifferenzah.huntcore.api.model.GameState.IDLE;
        int onlineCount = inLobby ? org.bukkit.Bukkit.getOnlinePlayers().size()
                                  : gameManager.getLastGameOnlineCount();
        int votesCount  = inLobby ? gameManager.getVoteCount()
                                  : gameManager.getLastGameVoteCount();

        return text
                .replace("%player%", player.getName())
                .replace("%player_name%", player.getName())
                .replace("%player_display_name%", player.getDisplayName())
                .replace("%team%", team.name())
                .replace("%team_colored%", team.getColoredName())
                .replace("%team_prefix%", team.getPrefix())
                .replace("%game_state%", gameManager.getGameState().name())
                .replace("%game_timer%", gameTimer)
                .replace("%runners_alive%", String.valueOf(gameManager.getAliveRunnerCount()))
                .replace("%runners_total%", String.valueOf(gameManager.getTotalRunnerCount()))
                .replace("%hunters_total%", String.valueOf(gameManager.getTotalHunterCount()))
                .replace("%player_kills%", String.valueOf(data.getKills()))
                .replace("%player_deaths%", String.valueOf(data.getDeaths()))
                .replace("%player_kd%", kd)
                .replace("%player_wins%", String.valueOf(data.getWins()))
                .replace("%player_losses%", String.valueOf(data.getLosses()))
                .replace("%player_games%", String.valueOf(data.getGames()))
                .replace("%pvp_status%", pvpStatus)
                .replace("%health%", healthFormatted)
                .replace("%health_suffix%", healthSuffix)
                .replace("%date%", date)
                .replace("%time%", time)
                .replace("%online_count%", String.valueOf(onlineCount))
                .replace("%votes_count%", String.valueOf(votesCount));
    }

    public static Component parse(String text, Player player, GameManager gameManager) {
        return colorize(replacePlaceholders(text, player, gameManager));
    }
}
