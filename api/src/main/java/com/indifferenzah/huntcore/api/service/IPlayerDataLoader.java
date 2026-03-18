package com.indifferenzah.huntcore.api.service;

import com.indifferenzah.huntcore.api.model.PlayerData;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface IPlayerDataLoader {

    PlayerData getPlayerData(UUID uuid);

    PlayerData getPlayerData(Player player);

    void loadPlayerData(Player player);

    void savePlayerData(Player player);
}
