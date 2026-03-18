package com.indifferenzah.huntcore.plugin.display;

import com.indifferenzah.huntcore.api.config.HuntCoreConfig;
import com.indifferenzah.huntcore.plugin.game.GameManager;
import com.indifferenzah.huntcore.plugin.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarManager {

    private final HuntCoreConfig config;
    private final GameManager gameManager;
    private BossBar bossBar;

    public BossBarManager(HuntCoreConfig config, GameManager gameManager) {
        this.config = config;
        this.gameManager = gameManager;
    }

    public void show() {
        if (!config.isBossbarEnabled()) return;
        if (bossBar != null) hide();

        BarColor color = parseColor(config.getBossbarColor());
        BarStyle style = parseStyle(config.getBossbarStyle());

        bossBar = Bukkit.createBossBar("", color, style);
        bossBar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
    }

    public void updateText(Player anyPlayer) {
        if (bossBar == null || anyPlayer == null) return;
        String raw = MessageUtils.replacePlaceholders(config.getBossbarText(), anyPlayer, gameManager);
        bossBar.setTitle(raw.replace("&", "§"));
    }

    public void addPlayer(Player player) {
        if (bossBar != null) bossBar.addPlayer(player);
    }

    public void removePlayer(Player player) {
        if (bossBar != null) bossBar.removePlayer(player);
    }

    public void hide() {
        if (bossBar == null) return;
        bossBar.removeAll();
        bossBar.setVisible(false);
        bossBar = null;
    }

    public boolean isActive() {
        return bossBar != null;
    }

    private BarColor parseColor(String name) {
        try {
            return BarColor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BarColor.RED;
        }
    }

    private BarStyle parseStyle(String name) {
        try {
            return BarStyle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BarStyle.SOLID;
        }
    }
}
