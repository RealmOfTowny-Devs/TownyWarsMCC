package com.danielrharris.townywars;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import static com.danielrharris.townywars.TownyUtils.townyVerification;

public class WallManager {
    private ConcurrentHashMap<String, Boolean> blockCache;
    private int MAX_CACHE_SIZE;  // arbitrary size limit to prevent excessive memory usage

    public WallManager(TownyWars plugin){
        blockCache  = new ConcurrentHashMap<>();
        MAX_CACHE_SIZE = 5000;
        movePlayerFromWall(plugin);
    }

    public boolean isWallDetected(Block block, Player player) {
        // Check if the block is within a claimed territory
        if (TownyAPI.getInstance().getTownBlock(block.getLocation()) == null) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + "The wall must be built in claimed territory.");
            }
            return false; // Exit if it's not within a claimed territory
        }

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

    public boolean isNatural(Block target, PlotBlockData snapshot) {
        String hash = this.generateUniqueBlockIdentifier(target.getLocation());

        if (this.blockCache.containsKey(hash)) {
            return this.blockCache.get(hash);
        } else {
            if (snapshot == null) {
                System.out.println("[TownyWars] Snapshot is null in isNatural() for block at location: " + target.getLocation());
                return false;  // Or handle this in another appropriate manner
            }

            String serializedTarget = GriefManager.serializeBlock(target);
            boolean isNatural = snapshot.getBlockList().contains(serializedTarget);
            this.blockCache.put(hash, isNatural);
            return isNatural;
        }
    }

    public boolean isValidWallBlock(Block block) {
        // Retrieve the snapshot for the block's location
        PlotBlockData snapshot = getSnapshotForBlockLocation(block.getLocation());

        return block.getType().isSolid() && !block.getType().isTransparent() && !isNatural(block, snapshot) && isOnlyWallDetected(block);
    }

    private PlotBlockData getSnapshotForBlockLocation(Location location) {
        // 1. Convert the location to a TownBlock using the provided Towny API method.
        TownBlock townBlock = TownyAPI.getInstance().getTownBlock(location);

        // If the TownBlock is null (which means the location isn't in any town),
        // return null or handle accordingly.
        if (townBlock == null) {
            System.out.println("[TownyWars] No TownBlock found for location: " + location);
            return null;
        }

        // 2. Fetch the snapshot data using Towny's API
        PlotBlockData snapshot = TownyRegenAPI.getPlotChunkSnapshot(townBlock);

        // Handle if the snapshot is null.
        if (snapshot == null) {
            System.out.println("[TownyWars] Snapshot is null for TownBlock at location: " + location);
        }

        return snapshot;
    }


    // New method to get the correct snapshot file based on location.
    private File getSnapshotFileForLocation(Location location) {
        // Convert location to chunk coordinates.
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        // Construct the snapshot filename based on chunk coordinates.
        String snapshotFileName = chunkX + "_" + chunkZ + "_16.zip";

        // Fetch the snapshots' folder.
        File snapshotsFolder = new File(TownyWars.getInstance().getDataFolder(), "snapshots");

        // Construct the full path to the snapshot file.
        File snapshotFile = new File(snapshotsFolder, snapshotFileName);

        return snapshotFile;
    }

    private String generateFileNameFromLocation(Location location) {
        // Print out the block's X and Z coordinates
        System.out.println("[DEBUG] Block X: " + location.getBlockX() + ", Block Z: " + location.getBlockZ());

        // Calculate chunk coordinates from block coordinates
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        // Print out the derived chunk coordinates
        System.out.println("[DEBUG] Chunk X: " + chunkX + ", Chunk Z: " + chunkZ);

        // Return the filename in the format used by Towny
        return chunkX + "_" + chunkZ + "_16.zip";
    }



    public void checkCache(Block target) {
        String hash = generateUniqueBlockIdentifier(target.getLocation());

        if (!blockCache.containsKey(hash)) {
            // Run cache checks asynchronously to avoid blocking the main thread
            Bukkit.getScheduler().runTaskAsynchronously(TownyWars.getInstance(), () -> {
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
                    boolean isBlockNatural = isNatural(target, snapshot);
                    blockCache.put(hash, isBlockNatural);
                } else {
                    blockCache.put(hash, true);  // Default to considering the block "natural".
                }

                // Prune the cache if it grows too big
                if (blockCache.size() > MAX_CACHE_SIZE) {
                    Iterator<String> iterator = blockCache.keySet().iterator();
                    if (iterator.hasNext()) {
                        iterator.next();
                        iterator.remove();
                    }
                }
            });
        }
    }

    private int getWallDimension(Block startBlock, Material type, int dx, int dy, int dz) {
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
    public boolean isOnlyWallDetected(Block block) {
        Material blockType = block.getType();

        int height = getWallDimension(block, blockType, 0, 1, 0); // 0, 1, 0 checks vertically
        if (height < 3) {
            return false;
        }

        int widthX = getWallDimension(block, blockType, 1, 0, 0); // 1, 0, 0 checks horizontally (along x-axis)
        int widthZ = getWallDimension(block, blockType, 0, 0, 1); // 0, 0, 1 checks horizontally (along z-axis)

        return widthX >= 3 || widthZ >= 3;
    }

    public void movePlayerFromWall(TownyWars plugin){
        final Map<Player, Stack<Location>> pastLocation = new HashMap<>();
        final int storeLimit = 10; // You can set this to an appropriate value

        new BukkitRunnable() {
            long timeTrack = System.currentTimeMillis();

            @Override
            public void run() {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    Tuple<Boolean, Boolean> verifyTowny = townyVerification(target, target.getLocation());
                    boolean isWallDetected = isWallDetected(target.getLocation().getBlock(), target);
                    boolean partOfTown = verifyTowny.a();
                    boolean playerInTown = verifyTowny.b();

                    if (!partOfTown || playerInTown) continue;

                    if (isWallDetected) {
                        for (int i = 1; i < storeLimit - 1; i++) {
                            if (!pastLocation.containsKey(target)) break;
                            if (i >= pastLocation.get(target).size()) break;
                            Block targetPosition = pastLocation.get(target).elementAt(pastLocation.get(target).size() - i).getBlock();
                            if (!isWallDetected(targetPosition, target) || i + 1 >= storeLimit - 1) {
                                Location targetLocation = targetPosition.getLocation();
                                targetLocation.setDirection(target.getLocation().getDirection());
                                Bukkit.getScheduler().runTask(plugin, () -> target.teleport(targetLocation));
                                break;
                            }
                        }
                    } else {
                        if (System.currentTimeMillis() - timeTrack >= 500) {
                            if (!pastLocation.containsKey(target)) pastLocation.put(target, new Stack<>());
                            Stack<Location> locationStack = pastLocation.get(target);
                            locationStack.push(target.getLocation());
                            if (locationStack.size() > storeLimit) locationStack.removeElementAt(0);
                            timeTrack = System.currentTimeMillis();
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 0);
    }
}
