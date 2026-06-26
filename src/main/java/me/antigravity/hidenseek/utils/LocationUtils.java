package me.antigravity.hidenseek.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Utility class to serialize and deserialize Bukkit Locations to strings.
 */
public class LocationUtils {

    /**
     * Serializes a Location to a string: "worldName;x;y;z;yaw;pitch"
     *
     * @param loc The location to serialize
     * @return The serialized string representation
     */
    public static String serialize(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return "";
        }
        return loc.getWorld().getName() + ";" +
               loc.getX() + ";" +
               loc.getY() + ";" +
               loc.getZ() + ";" +
               loc.getYaw() + ";" +
               loc.getPitch();
    }

    /**
     * Deserializes a string back to a Location.
     *
     * @param str The serialized location string
     * @return The Location, or null if invalid
     */
    public static Location deserialize(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        String[] parts = str.split(";");
        if (parts.length < 4) {
            return null;
        }
        try {
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) {
                return null;
            }
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0.0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0.0f;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
