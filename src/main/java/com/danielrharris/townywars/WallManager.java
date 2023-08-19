package com.danielrharris.townywars;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import me.drkmatr1984.BlocksAPI.utils.SBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WallManager {
    private ConcurrentHashMap<Long, Boolean> blockCache = new ConcurrentHashMap<>();

    public static boolean isWallDetected(Block block) {
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

    public String generateUniqueBlockIdentifier(Location location) {
        return location.getWorld().getUID().toString() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    public static boolean isNatural(Block target, PlotBlockData snapshot) {
        String serializedTarget = GriefManager.serializeBlock(target);
        return snapshot.getBlockList().contains(serializedTarget);
    }

    public static boolean isValidWallBlock(Block block) {
        // Retrieve the snapshot for the block's location
        PlotBlockData snapshot = getSnapshotForBlockLocation(block.getLocation());

        return block.getType().isSolid() && !block.getType().isTransparent() && !isNatural(block, snapshot) && isOnlyWallDetected(block);
    }

    private static PlotBlockData getSnapshotForBlockLocation(Location location) {
        // This is a placeholder. You'll need to implement the logic to retrieve the snapshot.
        // This could involve looking up a cache or deserializing a file from disk.

        // For now, I'll return null to avoid compile errors, but you'll want to replace this with actual logic.
        return null;
    }

    public void checkCache(Block target) {
        long hash = Long.parseLong(generateUniqueBlockIdentifier(target.getLocation()));  // Assuming we're using a new hash function, or an identifier.

        // If the blockCache doesn't contain the info about this block.
        if (!blockCache.containsKey(hash)) {

            // Convert block's location to WorldCoord
            WorldCoord worldCoord = WorldCoord.parseWorldCoord(target.getLocation());

            // Convert WorldCoord to TownBlock
            TownBlock townBlock;
            try {
                townBlock = TownyUniverse.getInstance().getTownBlock(worldCoord);
            } catch (NotRegisteredException e) {
                blockCache.put(hash, true);  // Handle exception by caching block as "natural" (or another default if preferred)
                return;
            }

            // Get the snapshot using the TownBlock
            PlotBlockData snapshot = TownyRegenAPI.getPlotChunkSnapshot(townBlock);
            if (snapshot != null) {

                // Check if the block in the snapshot matches the state of the block in the world.
                // "isNatural" checks the block's state in the snapshot.
                boolean isBlockNatural = isNatural(target, snapshot);

                // Cache the result.
                blockCache.put(hash, isBlockNatural);
            } else {
                // If there's no snapshot, default to considering the block "natural".
                blockCache.put(hash, true);
            }
        }
    }

    private static int getWallDimension(Block startBlock, Material type, int dx, int dy, int dz) {
        int dimension = 1;  // Start with the current block
        for (int i = 1; i < 3; i++) {  // Start from 1 because we've already counted the current block
            // Check in the positive direction
            Block positive = startBlock.getWorld().getBlockAt(startBlock.getX() + dx * i, startBlock.getY() + dy * i, startBlock.getZ() + dz * i);
            if (positive.getType() == type) {
                dimension++;
            }

            // Check in the negative direction
            Block negative = startBlock.getWorld().getBlockAt(startBlock.getX() - dx * i, startBlock.getY() - dy * i, startBlock.getZ() - dz * i);
            if (negative.getType() == type) {
                dimension++;
            }
        }
        return dimension;
    }
    public static boolean isOnlyWallDetected(Block block) {
        Material blockType = block.getType();

        int height = getWallDimension(block, blockType, 0, 1, 0); // 0, 1, 0 checks vertically
        if (height < 3) {
            return false;
        }

        int widthX = getWallDimension(block, blockType, 1, 0, 0); // 1, 0, 0 checks horizontally (along x-axis)
        int widthZ = getWallDimension(block, blockType, 0, 0, 1); // 0, 0, 1 checks horizontally (along z-axis)

        return widthX >= 3 || widthZ >= 3;
    }
}
