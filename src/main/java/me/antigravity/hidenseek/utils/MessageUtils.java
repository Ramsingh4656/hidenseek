package me.antigravity.hidenseek.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;

/**
 * Utility class to handle color translation, message sending, title display,
 * action bar messages, and play audio using the modern Adventure API.
 */
public class MessageUtils {

    /**
     * Translates '&' color codes (including hex) into a Component.
     */
    public static Component color(String message) {
        if (message == null) {
            return Component.empty();
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    /**
     * Translates '&' color codes to a legacy string.
     */
    public static String colorLegacy(String message) {
        if (message == null) {
            return "";
        }
        return LegacyComponentSerializer.legacyAmpersand().serialize(color(message));
    }

    /**
     * Sends a colored message to a CommandSender (Player or Console).
     */
    public static void sendMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        sender.sendMessage(color(message));
    }

    /**
     * Sends a colored action bar message to a player.
     */
    public static void sendActionBar(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        player.sendActionBar(color(message));
    }

    /**
     * Sends a title to a player.
     */
    public static void sendTitle(Player player, String titleText, String subtitleText, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        Component titleComponent = color(titleText);
        Component subtitleComponent = color(subtitleText);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeInTicks * 50L),
                Duration.ofMillis(stayTicks * 50L),
                Duration.ofMillis(fadeOutTicks * 50L)
        );

        Title title = Title.title(titleComponent, subtitleComponent, times);
        player.showTitle(title);
    }

    /**
     * Plays a sound to a player.
     */
    public static void playSound(Player player, String soundName, float volume, float pitch) {
        if (soundName == null || soundName.trim().isEmpty()) {
            return;
        }
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            // Log or ignore invalid sound
        }
    }

    /**
     * Plays a sound at a location for all nearby players.
     */
    public static void playSoundAt(Location loc, String soundName, float volume, float pitch) {
        if (loc == null || loc.getWorld() == null || soundName == null || soundName.trim().isEmpty()) {
            return;
        }
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            loc.getWorld().playSound(loc, sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            // Log or ignore
        }
    }
}
