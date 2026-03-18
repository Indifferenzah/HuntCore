package com.indifferenzah.huntcore.api.service;

import com.indifferenzah.huntcore.api.model.GameState;
import com.indifferenzah.huntcore.api.model.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;

public interface IGameManager {

    GameState getGameState();

    boolean isPvpEnabled();

    void setPvpEnabled(boolean pvpEnabled);

    void startGame();

    void stopGame();

    List<PlayerData> getAliveRunners();

    List<PlayerData> getAllRunners();

    List<PlayerData> getAllHunters();

    int getAliveRunnerCount();

    int getTotalRunnerCount();

    int getTotalHunterCount();

    void eliminateRunner(Player player);

    void checkWinCondition();
}
