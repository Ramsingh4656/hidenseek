package me.antigravity.hidenseek.config;

import me.antigravity.hidenseek.HideNSeek;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Manages plugin configurations (config.yml and messages.yml) and their live reloading.
 */
public class ConfigManager {

    private final HideNSeek plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private final File configFile;
    private final File messagesFile;

    public ConfigManager(HideNSeek plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        loadConfigs();
    }

    /**
     * Loads or creates config.yml and messages.yml. Merges values with jar defaults.
     */
    public void loadConfigs() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Read defaults from resource inside jar and set fallback defaults
        try {
            InputStream configStream = plugin.getResource("config.yml");
            if (configStream != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(configStream, StandardCharsets.UTF_8)));
            }
            InputStream messagesStream = plugin.getResource("messages.yml");
            if (messagesStream != null) {
                messages.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(messagesStream, StandardCharsets.UTF_8)));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load default configuration resources.");
        }
    }

    /**
     * Reloads configuration and messages files.
     */
    public void reload() {
        loadConfigs();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    /**
     * Gets a message from messages.yml. Prepend the prefix if specified.
     *
     * @param path      The configuration path
     * @param usePrefix If true, prepends the prefix configured in messages.yml
     * @return The colorized/formatted message string, or a fallback string if missing
     */
    public String getMessage(String path, boolean usePrefix) {
        String msg = messages.getString(path);
        if (msg == null) {
            return "&c[Missing message: " + path + "]";
        }
        if (usePrefix) {
            String prefix = messages.getString("prefix", "");
            return prefix + msg;
        }
        return msg;
    }

    /**
     * Gets a message without prefix by default.
     */
    public String getMessage(String path) {
        return getMessage(path, false);
    }
}
