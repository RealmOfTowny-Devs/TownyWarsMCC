package com.danielrharris.townywars;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import me.drkmatr1984.BlocksAPI.utils.SBlock;
import org.bukkit.block.Block;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WallManager {
    private static ConcurrentHashMap<Town, Set<SBlock>> sBlocks = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Boolean> blockCache = new ConcurrentHashMap<>();


    public static ConcurrentHashMap<Town, Set<SBlock>> getGriefedBlocks() {
        return sBlocks;
    }
    public static void setGriefedBlocks(ConcurrentHashMap<Town, Set<SBlock>> blocks) {
        sBlocks = blocks;
    }

    public static void removeTownGriefedBlocks(Town town) {
        sBlocks.remove(town);
    }

    public boolean isWallDetected(Block block) {
        int count = 0;
        int maxCount = 384;
        boolean checked = false;
        while (count <= maxCount) {
            if (block.getY() + count <= 300) {
                Block targetAbove = block.getWorld().getBlockAt(block.getX(), block.getY() + count, block.getZ());
                if (isValidWallBlock(targetAbove)) return true;
                checked = true;
            }

            if (block.getY() - count >= -64) {
                Block targetBelow = block.getWorld().getBlockAt(block.getX(), block.getY() - count, block.getZ());
                if (isValidWallBlock(targetBelow)) return true;
                checked = true;
            }

            if(!checked) return false;
            count += 2;
        }
        return false;
    }

    public boolean isNatural(Block target, PlotBlockData snapshot) {
        String serializedTarget = GriefManager.serializeBlock(target);
        return snapshot.getBlockList().contains(serializedTarget);
    }

    private boolean isValidWallBlock(Block block) {
        // Assuming you are passing PlotBlockData snapshot to the isNatural method
        PlotBlockData snapshot = /* retrieve snapshot for the block's location */;
        return block.getType().isSolid() && !block.getType().isTransparent() && !isNatural(block, snapshot) && isOnlyWallDetected(block);
    }

}
