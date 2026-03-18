package com.indifferenzah.huntcore.plugin.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CageBuilder {

    private final List<Location> cageBlocks = new ArrayList<>();
    private final Set<Long> cageBlockKeys = new HashSet<>();

    /** Builds a 9x9x5 hollow glass cage centered at (x, y, z). */
    public void buildCage(Location center) {
        cageBlocks.clear();
        cageBlockKeys.clear();

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int dy = 0; dy <= 4; dy++) {
                    boolean isWall = dx == -4 || dx == 4 || dz == -4 || dz == 4 || dy == 0 || dy == 4;
                    if (isWall) {
                        Location loc = new Location(center.getWorld(), cx + dx, cy + dy, cz + dz);
                        loc.getBlock().setType(Material.GLASS, false);
                        cageBlocks.add(loc);
                        cageBlockKeys.add(blockKey(cx + dx, cy + dy, cz + dz));
                    }
                }
            }
        }
    }

    public void destroyCage() {
        for (Location loc : cageBlocks) {
            Block block = loc.getBlock();
            if (block.getType() == Material.GLASS) {
                block.setType(Material.AIR, false);
            }
        }
        cageBlocks.clear();
        cageBlockKeys.clear();
    }

    public boolean isCageBlock(Location loc) {
        return cageBlockKeys.contains(blockKey(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    private long blockKey(int x, int y, int z) {
        return ((long) x & 0xFFFFFFL) | (((long) y & 0xFFFFL) << 24) | (((long) z & 0xFFFFFFL) << 40);
    }

    public List<Location> getCageBlocks() { return cageBlocks; }
}
