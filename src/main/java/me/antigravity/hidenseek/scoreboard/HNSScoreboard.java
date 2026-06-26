package me.antigravity.hidenseek.scoreboard;

import me.antigravity.hidenseek.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;

/**
 * Wraps a player's scoreboard sidebar displaying match metrics.
 * Uses unique invisible suffixes to prevent lines with identical content from collapsing.
 */
public class HNSScoreboard {

    private final Player player;
    private final Scoreboard scoreboard;
    private final Objective objective;

    public HNSScoreboard(Player player, String title) {
        this.player = player;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("hns_board", Criteria.DUMMY, MessageUtils.color(title));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
    }

    /**
     * Updates the scoreboard lines dynamically.
     *
     * @param lines The list of strings to show on the scoreboard.
     */
    public void update(List<String> lines) {
        // Reset old scores
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        int scoreValue = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            
            // Append a unique invisible color code suffix to ensure line uniqueness
            String formattedLine = MessageUtils.colorLegacy(line + getInvisibleSuffix(i));
            
            // Limit line length to 128 characters for legacy/packet compatibility, though 1.21+ has no strict limit
            if (formattedLine.length() > 128) {
                formattedLine = formattedLine.substring(0, 128);
            }
            
            Score score = objective.getScore(formattedLine);
            score.setScore(scoreValue);
            scoreValue--;
        }
    }

    /**
     * Removes the custom scoreboard and restores the player back to the main server scoreboard.
     */
    public void remove() {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    /**
     * Generates a unique invisible sequence based on the line index.
     */
    private String getInvisibleSuffix(int index) {
        char colorChar = Integer.toHexString(index % 16).charAt(0);
        return "§" + colorChar + "§r";
    }
}
