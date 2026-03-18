package com.indifferenzah.huntcore.plugin.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

public class LocationUtils {

    private static final Random RANDOM = new Random();

    /**
     * Returns a random surface location within [minRadius, maxRadius] from the given center.
     */
    public static Location getRandomSurfaceLocation(Location center, int minRadius, int maxRadius) {
        World world = center.getWorld();
        double angle = RANDOM.nextDouble() * 2 * Math.PI;
        int radius = minRadius + RANDOM.nextInt(maxRadius - minRadius);
        double x = center.getX() + radius * Math.cos(angle);
        double z = center.getZ() + radius * Math.sin(angle);
        int y = world.getHighestBlockYAt((int) x, (int) z);
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }

    /**
     * Returns a random non-liquid surface location.
     * Retries up to 20 times to avoid spawning inside water or lava.
     */
    public static Location getRandomSpawnLocation(Location center, int spawnRadius) {
        for (int attempt = 0; attempt < 20; attempt++) {
            Location loc = getRandomSurfaceLocation(center, spawnRadius, spawnRadius + 500);
            Block surface = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
            if (!surface.isLiquid()) return loc;
        }
        return getRandomSurfaceLocation(center, spawnRadius, spawnRadius + 500);
    }
}
