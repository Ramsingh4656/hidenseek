package me.antigravity.hidenseek.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Storage class wrapper for handling loading and saving of custom YAML files.
 */
public class YamlStorage {

    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration config;

    /**
     * Creates or loads a YAML file.
     *
     * @param plugin    The JavaPlugin instance
     * @param fileName  The name of the file (e.g. "config.yml")
     * @param subFolder Optional subfolder (e.g. "arenas")
     */
    public YamlStorage(JavaPlugin plugin, String fileName, String subFolder) {
        this.plugin = plugin;
        File folder = subFolder == null ? plugin.getDataFolder() : new File(plugin.getDataFolder(), subFolder);
        
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        this.file = new File(folder, fileName.endsWith(".yml") ? fileName : fileName + ".yml");
        reload();
    }

    /**
     * Reloads the configuration from the file.
     */
    public void reload() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create file: " + file.getName(), e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Gets the FileConfiguration instance.
     */
    public FileConfiguration getConfig() {
        if (config == null) {
            reload();
        }
        return config;
    }

    /**
     * Saves changes to the file.
     */
    public void save() {
        if (config == null || file == null) {
            return;
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save file: " + file.getName(), e);
        }
    }

    /**
     * Deletes the underlying file.
     *
     * @return True if deletion was successful
     */
    public boolean delete() {
        return file.exists() && file.delete();
    }

    /**
     * Gets the raw File object.
     */
    public File getFile() {
        return file;
    }
}
