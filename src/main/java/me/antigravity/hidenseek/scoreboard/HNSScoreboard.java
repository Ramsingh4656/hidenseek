package me.antigravity.hidenseek.scoreboard;

import me.antigravity.hidenseek.utils.MessageUtils;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a player's scoreboard sidebar displaying match metrics.
 * Uses a Team-based approach with unique invisible entries to avoid line duplication/collapsing
 * and prevent flickering on Pocket Edition / Bedrock (Geyser/Floodgate).
 */
public class HNSScoreboard {

    private final Player player;
    private final Scoreboard scoreboard;
    private final Objective objective;
    private final List<Team> teams = new ArrayList<>();
    private final List<String> entries = new ArrayList<>();
    private int activeLinesCount = 0;

    public HNSScoreboard(Player player, String title) {
        this.player = player;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("hns_board", Criteria.DUMMY, MessageUtils.color(title));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Hide scores (numbers on the right side) for modern MC versions (1.20.3+)
        try {
            this.objective.numberFormat(NumberFormat.blank());
        } catch (Throwable t) {
            // Fallback for older versions if any, though 1.21.1 is guaranteed to have it
        }

        // Initialize 15 teams for sidebar lines
        for (int i = 0; i < 15; i++) {
            Team team = scoreboard.registerNewTeam("hns_line_" + i);
            String entry = getInvisibleSuffix(i);
            team.addEntry(entry);
            teams.add(team);
            entries.add(entry);
        }

        player.setScoreboard(scoreboard);
    }

    /**
     * Updates the scoreboard lines dynamically.
     *
     * @param lines The list of strings to show on the scoreboard.
     */
    public void update(List<String> lines) {
        int linesToShow = Math.min(lines.size(), 15);

        // Reset scores for lines that are no longer needed
        if (activeLinesCount > linesToShow) {
            for (int i = linesToShow; i < activeLinesCount; i++) {
                scoreboard.resetScores(entries.get(i));
            }
        }

        // Update scores and team prefixes for visible lines
        int scoreValue = linesToShow;
        for (int i = 0; i < linesToShow; i++) {
            String line = lines.get(i);
            String entry = entries.get(i);
            Team team = teams.get(i);

            // Set prefix (modern adventure Component support avoids legacy length limits and raw color codes)
            team.prefix(MessageUtils.color(line));

            // Set the score if not set or incorrect to maintain order
            Score score = objective.getScore(entry);
            if (!score.isScoreSet() || score.getScore() != scoreValue) {
                score.setScore(scoreValue);
            }
            scoreValue--;
        }

        activeLinesCount = linesToShow;
    }

    /**
     * Removes the custom scoreboard and restores the player back to the main server scoreboard.
     */
    public void remove() {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        // Clean up registered teams
        for (Team team : teams) {
            try {
                team.unregister();
            } catch (IllegalStateException e) {
                // Ignore if already unregistered
            }
        }
    }

    /**
     * Generates a unique invisible sequence based on the line index.
     */
    private String getInvisibleSuffix(int index) {
        char colorChar = Integer.toHexString(index % 16).charAt(0);
        return "§" + colorChar + "§r";
    }
}
