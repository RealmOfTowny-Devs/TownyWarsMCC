package com.danielrharris.townywars.listeners;

import com.danielrharris.townywars.*;
import com.danielrharris.townywars.tasks.AttackWarnBarTask;
import com.palmergames.bukkit.towny.*;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.object.PlayerCache.TownBlockStatus;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.Vine;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.danielrharris.townywars.TownyUtils.townyVerification;

public class GriefListener implements Listener {

    private TownyWars mplugin;
    private double DEBRIS_CHANCE;
    private final File townyWarsDataDir = new File(TownyWars.getInstance().getDataFolder(), "snapshots");

    public GriefListener(TownyWars aThis, GriefManager m) {
        this.mplugin = aThis;
        this.DEBRIS_CHANCE = TownyWars.debrisChance;
    }

    /**
     * This event listener reacts to the BlockPlaceEvent, specifically when a player places a block during a war in Towny.
     *
     * Main functionality:
     * 1. If the town is at war, it captures a snapshot of the block data (for potential rollbacks later).
     * 2. It checks if the player has the required permissions to place blocks, considering Towny's rules.
     * 3. It verifies if the block being placed is part of a flag war, and reacts accordingly.
     * 4. If the player does not have permissions, or the block is restricted in a warzone, the event is cancelled.
     * 5. Errors and messages are communicated to the player to inform them about any restrictions.
     *
     * Note: The snapshot capturing is offloaded to an asynchronous task to prevent potential lag in the main server thread.
     */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onWarBuild(BlockPlaceEvent event) throws NotRegisteredException {
        if (TownyWars.allowGriefing) {
            Block block = event.getBlock();
            Player p = event.getPlayer();

            if (p != null && TownyWars.atWar(p, block.getLocation())) {
                try {
                    TownBlock townBlock = TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(block.getLocation()));
                    if (townBlock != null) {
                        // Capture the snapshot before the block is placed using Towny's snapshot system
                        CompletableFuture<PlotBlockData> futureSnapshot = TownyRegenAPI.createPlotSnapshot(townBlock);
                        futureSnapshot.thenAccept(snapshot -> {
                            Bukkit.getScheduler().runTaskAsynchronously(TownyWars.getInstance(), () -> TownyRegenAPI.addPlotChunkSnapshot(snapshot));
                        });
                    }
                } catch (NotRegisteredException ignored) {
                    return;
                }

                event.setBuild(true);
                event.setCancelled(false);

            } else {
                Towny plugin = TownyWars.towny;
                if (plugin.isError()) {
                    event.setCancelled(true);
                    return;
                }

                Player player = event.getPlayer();
                TownyWorld world = TownyUniverse.getInstance().getWorld(block.getWorld().getName());
                WorldCoord worldCoord = new WorldCoord(world.getName(), Coord.parseCoord(block));

                // Get build permissions (updates if none exist)
                boolean bBuild = PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.BUILD);

                // Allow build if we are permitted
                if (bBuild) return;

                PlayerCache cache = plugin.getCache(player);
                TownBlockStatus status = cache.getStatus();

                // Flag war
                if (status == TownBlockStatus.ENEMY && FlagWarConfig.isAllowingAttacks() && event.getBlock().getType() == FlagWarConfig.getFlagBaseMaterial()) {
                    try {
                        if (FlagWar.callAttackCellEvent(plugin, player, block, worldCoord))
                            return;
                    } catch (TownyException e) {
                        TownyMessaging.sendErrorMsg(player, e.getMessage());
                    }

                    event.setBuild(false);
                    event.setCancelled(true);
                } else if (status == TownBlockStatus.WARZONE) {
                    if (!FlagWarConfig.isEditableMaterialInWarZone(block.getType())) {
                        event.setBuild(false);
                        event.setCancelled(true);
                        TownyMessaging.sendErrorMsg(player, String.format(Translation.of("msg_err_warzone_cannot_edit_material"), "build", block.getType().toString().toLowerCase()));
                    }
                    return;
                } else {
                    event.setBuild(false);
                    event.setCancelled(true);
                }

                // Display any error recorded for this plot
                if (cache.hasBlockErrMsg() && event.isCancelled())
                    TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void ignoreProtections(EntityExplodeEvent ev) {
        Location center = ev.getLocation();
        TownBlock townBlock = null;
        try {
            townBlock = TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(center));
            if (townBlock != null && TownyWars.allowGriefing && TownyWars.warExplosions && townBlock.hasTown()) {
                if (townBlock.getTown().hasNation()) {
                    Nation nation = townBlock.getTown().getNation();
                    if (WarManager.getWarForNation(nation) != null) {
                        // Take a snapshot before the explosion is cancelled
                        CompletableFuture<PlotBlockData> futureSnapshot = TownyRegenAPI.createPlotSnapshot(townBlock);
                        futureSnapshot.thenAccept(snapshot -> {
                            Bukkit.getScheduler().runTaskAsynchronously(TownyWars.getInstance(), () -> TownyRegenAPI.addPlotChunkSnapshot(snapshot));
                        });

                        // Cancel the explosion
                        ev.setCancelled(true);
                    }
                }
            }
        } catch (NotRegisteredException ignored) {
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.AIR) {
            return; // Do not process AIR blocks
        }
    
        Tuple<Boolean, Boolean> verifyTowny = townyVerification(event.getPlayer(), block.getLocation());
        boolean isWallDetected = mplugin.getWallManager().isWallDetected(block, event.getPlayer());
        boolean partOfTown = verifyTowny.a();
        boolean playerInTown = verifyTowny.b();
    
        Bukkit.getLogger().info("BlockBreak called: isWallDetected=" + isWallDetected + ", partOfTown=" + partOfTown + ", playerInTown=" + playerInTown);
    
        if (isWallDetected && partOfTown && !playerInTown) {
            try {
                TownBlock townBlock = TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(block.getLocation()));
                if (townBlock != null) {
                    // Capture a snapshot before the block is broken
                    PlotBlockData snapshot = new PlotBlockData(townBlock);
    
                    // Asynchronously save the snapshot
                    Bukkit.getScheduler().runTaskAsynchronously(TownyWars.getInstance(), () -> {
                        try {
                            saveSnapshot(snapshot, townBlock.getName());
                        } catch (IOException e) {
                            e.printStackTrace(); // You might want to handle this better.
                        }
                    });
                }
            } catch (NotRegisteredException ignored) {
                return;
            }
    
            // Cancel the block break event
            event.setCancelled(true);
        }
    }
    

    private void saveSnapshot(PlotBlockData snapshot, String blockName) throws IOException {
        if (!townyWarsDataDir.exists()) {
            townyWarsDataDir.mkdirs();
        }

        File snapshotFile = new File(townyWarsDataDir, blockName + ".dat");
        try (FileOutputStream fileOut = new FileOutputStream(snapshotFile);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
            objectOut.writeObject(snapshot);
        }
    }


    @SuppressWarnings({"deprecation"})
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onExplode(EntityExplodeEvent ev) throws NotRegisteredException {
        ev.setCancelled(false);
        Location center = ev.getLocation();
        TownBlock townBlock = TownyAPI.getInstance().getTownBlock(center);

        if (townBlock != null && TownyWars.allowGriefing && TownyWars.warExplosions) {
            Town town = null;

            try {
                town = townBlock.getTown();
            } catch (NotRegisteredException e) {
                // Handle this if necessary
            }

            if (town != null) {
                Nation nation = null;

                try {
                    if (town.hasNation()) {
                        nation = town.getNation();
                    }
                } catch (NotRegisteredException e) {
                    // Handle this if necessary
                }

                if (nation != null && WarManager.getWarForNation(nation) != null) {
                    // Create a snapshot before making any changes
                    if (TownyWars.allowRollback) {
                        TownyRegenAPI.createPlotSnapshot(townBlock);
                    }

                    List<Block> blocks = ev.blockList();
                    if (blocks != null) {
                        if (TownyWars.realisticExplosions) {
                            Explode.explode(ev.getEntity(), blocks, center, (int) DEBRIS_CHANCE);
                        }

                        // You may also consider adding logic here to manipulate the block list or explosion characteristics based on the snapshot.
                    }

                    return;
                }
            }
        }

        // If you reach here, this means the explosion isn't inside a town at war.
        // Handle explosions in the wilderness or other contexts as per your previous logic.
        if (TownyAPI.getInstance().isWilderness(center.getBlock()) && TownySettings.isExplosions() && TownyWars.realisticExplosions) {
            List<Block> blocks = ev.blockList();
            if (blocks != null) {
                Explode.explode(ev.getEntity(), blocks, center, 75);
            }
            ev.setCancelled(false);
        }
    }
}