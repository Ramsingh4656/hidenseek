package me.antigravity.hidenseek.bossbar;

import me.antigravity.hidenseek.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

/**
 * Handles the display and progress updates of a BossBar for match timers.
 */
public class HNSBossBar {

    private final BossBar bossBar;

    public HNSBossBar(String title, String colorStr, String styleStr) {
        BarColor color = BarColor.RED;
        BarStyle style = BarStyle.SOLID;

        try {
            if (colorStr != null) {
                color = BarColor.valueOf(colorStr.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            // Fallback to RED
        }

        try {
            if (styleStr != null) {
                style = BarStyle.valueOf(styleStr.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            // Fallback to SOLID
        }

        this.bossBar = Bukkit.createBossBar(MessageUtils.colorLegacy(title), color, style);
        this.bossBar.setVisible(true);
    }

    /**
     * Updates the BossBar title text.
     */
    public void updateTitle(String title) {
        bossBar.setTitle(MessageUtils.colorLegacy(title));
    }

    /**
     * Updates the BossBar progress percentage (0.0 to 1.0).
     */
    public void updateProgress(double progress) {
        // Clamp progress to [0.0, 1.0]
        double clamped = Math.max(0.0, Math.min(1.0, progress));
        bossBar.setProgress(clamped);
    }

    /**
     * Adds a player to see this BossBar.
     */
    public void addPlayer(Player player) {
        bossBar.addPlayer(player);
    }

    /**
     * Removes a player from seeing this BossBar.
     */
    public void removePlayer(Player player) {
        bossBar.removePlayer(player);
    }

    /**
     * Clears all players and removes the BossBar.
     */
    public void remove() {
        bossBar.removeAll();
        bossBar.setVisible(false);
    }
}
