package com.indifferenzah.huntcore.plugin.display;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.api.model.GameState;
import com.indifferenzah.huntcore.plugin.data.PlayerDataLoader;
import com.indifferenzah.huntcore.plugin.game.GameManager;
import com.indifferenzah.huntcore.plugin.utils.MessageUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;

public class ScoreboardManager {

    private final GameManager gameManager;
    private final HuntCoreConfig config;

    public ScoreboardManager(GameManager gameManager, HuntCoreConfig config) {
        this.gameManager = gameManager;
        this.config = config;
    }

    public void createScoreboard(Player player) {
        updateScoreboard(player);
    }

    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }

    public void updateScoreboard(Player player) {
        GameState state = gameManager.getGameState();

        String title;
        List<String> lines;

        switch (state) {
            case IDLE, LOBBY, COUNTDOWN -> {
                if (config.isLobbyScoreboardEnabled()) {
                    title = config.getLobbyScoreboardTitle();
                    lines = config.getLobbyScoreboardLines();
                } else if (config.isScoreboardEnabled()) {
                    title = config.getScoreboardTitle();
                    lines = config.getScoreboardLines();
                } else {
                    removeScoreboard(player);
                    return;
                }
            }
            case RUNNING -> {
                if (config.isScoreboardEnabled()) {
                    title = config.getScoreboardTitle();
                    lines = config.getScoreboardLines();
                } else {
                    removeScoreboard(player);
                    return;
                }
            }
            case ENDED -> {
                if (config.isEndedScoreboardEnabled()) {
                    title = config.getEndedScoreboardTitle();
                    lines = config.getEndedScoreboardLines();
                } else {
                    removeScoreboard(player);
                    return;
                }
            }
            default -> {
                removeScoreboard(player);
                return;
            }
        }

        if (lines.isEmpty()) {
            removeScoreboard(player);
            return;
        }

        buildBoard(player, title, lines);
    }

    private void buildBoard(Player player, String titleRaw, List<String> lines) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        String processedTitle = MessageUtils.replacePlaceholders(titleRaw, player, gameManager);
        Objective objective = board.registerNewObjective(
                "huntcore",
                Criteria.DUMMY,
                LegacyComponentSerializer.legacyAmpersand().deserialize(processedTitle)
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = lines.size();
        for (String line : lines) {
            String processed = MessageUtils.replacePlaceholders(line, player, gameManager);
            String colorized = MessageUtils.colorizeToString(processed);
            if (colorized.length() > 40) colorized = colorized.substring(0, 40);

            Team team = board.registerNewTeam("line_" + score);
            String entry = getUniqueEntry(score);
            team.addEntry(entry);
            team.prefix(LegacyComponentSerializer.legacyAmpersand().deserialize(colorized));
            objective.getScore(entry).setScore(score);
            score--;
        }

        // Nametag team prefixes (all players see everyone's team tag)
        Team runnerTag = board.registerNewTeam("tag_runner");
        runnerTag.prefix(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(config.getNametagRunnerPrefix()));
        Team hunterTag = board.registerNewTeam("tag_hunter");
        hunterTag.prefix(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(config.getNametagHunterPrefix()));
        for (Player p : Bukkit.getOnlinePlayers()) {
            com.indifferenzah.huntcore.api.model.Team t =
                    (com.indifferenzah.huntcore.api.model.Team) gameManager.getDataLoader().getPlayerData(p.getUniqueId()).getTeam();
            if (t == com.indifferenzah.huntcore.api.model.Team.RUNNER) {
                runnerTag.addEntry(p.getName());
            } else if (t == com.indifferenzah.huntcore.api.model.Team.HUNTER) {
                hunterTag.addEntry(p.getName());
            }
        }

        player.setScoreboard(board);
    }

    private String getUniqueEntry(int index) {
        String[] colors = {"§0","§1","§2","§3","§4","§5","§6","§7","§8","§9",
                "§a","§b","§c","§d","§e","§f"};
        if (index < colors.length) return colors[index] + "§r";
        return "§" + (char)('a' + (index % 26)) + "§r";
    }
}
