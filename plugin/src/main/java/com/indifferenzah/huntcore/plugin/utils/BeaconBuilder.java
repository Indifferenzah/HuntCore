package com.indifferenzah.huntcore.plugin.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class BeaconBuilder {

    private Location pyramidCenter;
    private Location cryingObsidianLocation;
    private final List<Location> builtBlocks = new ArrayList<>();

    /**
     * Builds a 3-layer iron pyramid with Crying Obsidian on top.
     * Returns the location of the Crying Obsidian (the game objective).
     */
    public Location buildPyramid(Location center) {
        this.pyramidCenter = center.clone();
        builtBlocks.clear();

        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // Layer 0: 7x7
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                Location loc = new Location(world, cx + dx, cy, cz + dz);
                loc.getBlock().setType(Material.IRON_BLOCK);
                builtBlocks.add(loc);
            }
        }

        // Layer 1: 5x5
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                Location loc = new Location(world, cx + dx, cy + 1, cz + dz);
                loc.getBlock().setType(Material.IRON_BLOCK);
                builtBlocks.add(loc);
            }
        }

        // Layer 2: 3x3
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Location loc = new Location(world, cx + dx, cy + 2, cz + dz);
                loc.getBlock().setType(Material.IRON_BLOCK);
                builtBlocks.add(loc);
            }
        }

        // Top: Crying Obsidian
        cryingObsidianLocation = new Location(world, cx, cy + 3, cz);
        cryingObsidianLocation.getBlock().setType(Material.CRYING_OBSIDIAN);
        builtBlocks.add(cryingObsidianLocation);

        return cryingObsidianLocation;
    }

    public void removePyramid() {
        for (Location loc : builtBlocks) {
            loc.getBlock().setType(Material.AIR);
        }
        builtBlocks.clear();
        cryingObsidianLocation = null;
    }

    public Location getCryingObsidianLocation() {
        return cryingObsidianLocation;
    }

    public List<Location> getBuiltBlocks() {
        return builtBlocks;
    }

    public Location getPyramidCenter() {
        return pyramidCenter;
    }
}
