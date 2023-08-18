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
import me.drkmatr1984.BlocksAPI.utils.SBlock;
import me.drkmatr1984.BlocksAPI.utils.Utils;
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

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.danielrharris.townywars.TownyUtils.townyVerification;
import static com.danielrharris.townywars.WallManager.isWallDetected;

public class GriefListener implements Listener {

    private TownyWars mplugin;
    private double DEBRIS_CHANCE;
    private GriefManager m;
    private WallManager wallManager = new WallManager(); // Create a new instance or pass it in as required.

    public GriefListener(TownyWars aThis, GriefManager m) {
        this.mplugin = aThis;
        this.DEBRIS_CHANCE = TownyWars.debrisChance;
        this.m = m;
        final int storeLimit = 10; // You can set this to an appropriate value
        final Map<Player, Stack<Location>> pastLocation = new HashMap<>();

        new BukkitRunnable() {
            long timeTrack = System.currentTimeMillis();

            @Override
            public void run() {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    Tuple<Boolean, Boolean> verifyTowny = townyVerification(target, target.getLocation());
                    boolean isWallDetected = isWallDetected(target.getLocation().getBlock());
                    boolean partOfTown = verifyTowny.a();
                    boolean playerInTown = verifyTowny.b();

                    if (!partOfTown || playerInTown) continue;

                    if (isWallDetected) {
                        for (int i = 1; i < storeLimit - 1; i++) {
                            if (!pastLocation.containsKey(target)) break;
                            if (i >= pastLocation.get(target).size()) break;
                            Block targetPosition = pastLocation.get(target).elementAt(pastLocation.get(target).size() - i).getBlock();
                            if (!isWallDetected(targetPosition) || i + 1 >= storeLimit - 1) {
                                Location targetLocation = targetPosition.getLocation();
                                targetLocation.setDirection(target.getLocation().getDirection());
                                Bukkit.getScheduler().runTask(aThis, () -> target.teleport(targetLocation));
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
        }.runTaskTimerAsynchronously(aThis, 0, 0);

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onWarTownDamage(BlockBreakEvent event) throws NotRegisteredException {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        WorldCoord coord = WorldCoord.parseWorldCoord(block.getLocation());
        TownBlock townBlock = TownyUniverse.getInstance().getTownBlock(coord);
        PlotBlockData snapshot = TownyRegenAPI.getPlotChunkSnapshot(townBlock);

        if (WallManager.isOnlyWallDetected(block) && (snapshot == null || !WallManager.isNatural(block, snapshot))) return;

        if (TownyWars.allowGriefing) {
            if (TownyWars.worldBlackList != null && TownyWars.worldBlackList.contains(block.getWorld().getName().toLowerCase())) {
                return;
            }
            if (TownyWars.blockBlackList != null && TownyWars.blockBlackList.contains(block.getType())) {
                return;
            }

            Player p = event.getPlayer();
            if (p != null && TownyWars.atWar(p, block.getLocation())) {
                try {
                    if (townBlock != null) {
                        // If there's no snapshot already, save one.
                        if (snapshot == null) {
                            snapshot = new PlotBlockData(townBlock);
                            TownyRegenAPI.addPlotChunkSnapshot(snapshot);
                        }

                        Town otherTown = townBlock.getTown();
                        Nation otherNation = otherTown.getNation();
                        Set<SBlock> sBlocks = getAttachedBlocks(block, new HashSet<SBlock>(), p);

                        SBlock check = new SBlock(block);
                        if (!GriefManager.containsBlock(GriefManager.getGriefedBlocks().get(otherTown), check) && block.getType() != Material.TNT) {
                            sBlocks.add(new SBlock(block, p));
                        }

                        if (TownyWars.allowRollback) {
                            Set<SBlock> temp = GriefManager.getGriefedBlocks().get(otherTown);
                            if (temp == null) {
                                temp = new HashSet<SBlock>();
                            }
                            temp.addAll(sBlocks);
                            GriefManager.getGriefedBlocks().put(otherTown, temp);
                        }

                        if (otherNation != null) {
                            War wwar = WarManager.getWarForNation(otherNation);
                            double points = Math.round(((double) sBlocks.size() * TownyWars.pBlockPoints) * 1e2) / 1e2;
                            wwar.chargeTownPoints(otherNation, otherTown, points);
                            new AttackWarnBarTask(otherTown, mplugin).runTask(mplugin);
                            event.setCancelled(true);
                            block.breakNaturally();
                        }
                    }
                } catch (NotRegisteredException ignored) {}
            }
        }
    }




//    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
//    public void suppressTownyBuildEvent(BlockPlaceEvent event) {
//        if (TownyWars.allowGriefing) {
//            event.setCancelled(true);
//        }
//    }

	/*
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent event) {
		if(plugin.recordBlockExplode){
			List<Block> blocks = event.blockList();
			for(Block block : blocks){
				ArrayList<SBlock> sBlocks = new ArrayList<SBlock>();
				if(plugin.worldBanList.contains(block.getWorld().getName().toString().toLowerCase())){
					return;
				}
				if(this.banList.contains(block.getType())){
					return;
				}
				for(BlockFace face : BlockFace.values()){
					if(!face.equals(BlockFace.SELF)){
						if((block.getRelative(face)).getState().getData() instanceof Attachable || (block.getRelative(face)).getType().equals(Material.VINE) || (block.getRelative(face)).getType().equals(Material.CHORUS_PLANT) || (block.getRelative(face)).getType().equals(Material.CHORUS_FLOWER)){
							sBlocks.add(new SBlock((block.getRelative(face))));
						}
					}
				}
				if(Utils.isOtherAttachable((block.getRelative(BlockFace.UP)).getType())){
					sBlocks.add(new SBlock((block.getRelative(BlockFace.UP))));
				}
				if((block.getRelative(BlockFace.UP)).getType().equals(Material.CACTUS) || (block.getRelative(BlockFace.UP)).getType().equals(Material.SUGAR_CANE_BLOCK) || (block.getRelative(BlockFace.UP)).getType().equals(Material.CHORUS_PLANT) || (block.getRelative(BlockFace.UP)).getType().equals(Material.CHORUS_FLOWER)){
					Block up = block.getRelative(BlockFace.UP);
					do
					{
						if(up.getType().equals(Material.CACTUS) || up.getType().equals(Material.SUGAR_CANE_BLOCK) || up.getType().equals(Material.CHORUS_PLANT) || up.getType().equals(Material.CHORUS_FLOWER)){
							sBlocks.add(new SBlock(up));
						}
						up = ((up.getLocation()).add(0,1,0)).getBlock();
					}while(up.getType().equals(Material.CACTUS) || up.getType().equals(Material.SUGAR_CANE_BLOCK) || up.getType().equals(Material.CHORUS_PLANT) || up.getType().equals(Material.CHORUS_FLOWER));
				}
				sBlocks.add(new SBlock(block));
				for(SBlock bL : sBlocks){
					if(bL!=null && !plugin.containsBlockLocation(bL)){
						if(!plugin.addToList(bL)){
							Bukkit.getServer().getLogger().info(ChatColor.DARK_RED + "Cannot add to List");
						}
						if(plugin.debugMessages){
							Bukkit.getServer().getLogger().info("BlockExplodeEvent");
							Bukkit.getServer().getLogger().info("Saved BlockLocation");
							Bukkit.getServer().getLogger().info("Location : " + "X:"+ bL.x + ", " + "Y:"+ bL.y + ", " + "Z:"+ bL.z);
							Bukkit.getServer().getLogger().info("BlockType : " + bL.mat);
							Bukkit.getServer().getLogger().info("Entity : " + bL.ent);
							if(block.getState() instanceof Skull){
								Bukkit.getServer().getLogger().info("SkullType: " + bL.skullType);
								Bukkit.getServer().getLogger().info("SkullOwner: " + bL.skullOwner);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onWaterPassThrough(BlockFromToEvent event){
		if(mplugin.recordBlockFromTo){
			if(mplugin.worldBanList.contains(event.getToBlock().getWorld().getName().toString().toLowerCase())){
				return;
			}
			if(Utils.isOtherAttachable(event.getToBlock().getType()) || event.getToBlock().getState() instanceof Attachable){
				SBlock bL = new SBlock(event.getToBlock());
				if(bL!=null && !mplugin.containsBlockLocation(bL)){
					if(!this.banList.contains(event.getToBlock().getType()))
						if(!plugin.addToList(bL)){
							Bukkit.getServer().getLogger().info(ChatColor.DARK_RED + "Cannot add to List");
						}
					if(mplugin.debugMessages){
						Bukkit.getServer().getLogger().info("BlockFromToEvent");
						Bukkit.getServer().getLogger().info("Saved BlockLocation");
						Bukkit.getServer().getLogger().info("Location : " + "X:"+ bL.x + ", " + "Y:"+ bL.y + ", " + "Z:"+ bL.z);
						Bukkit.getServer().getLogger().info("BlockType : " + bL.mat);
						Bukkit.getServer().getLogger().info("Entity : " + bL.ent);
					}
				}
			}
			for(Block b : Utils.getNearbyLiquids(event.getBlock())){
				SBlock breaker = new SBlock(b);
				if(!this.banList.contains(breaker.getType()))
					if(!plugin.addToList(breaker)){
						Bukkit.getServer().getLogger().info(ChatColor.DARK_RED + "Cannot add to List");
					}
			}
		}
	}
	
	@EventHandler
	public void onPlayerBucketEvent(PlayerBucketEmptyEvent event){
		Block block = event.getBlockClicked();
		if(TownyWars.worldBlackList!=(null)){
			if(TownyWars.worldBlackList.contains(block.getWorld().getName().toString().toLowerCase())){
				return;
			}
		}
		if(mplugin.recordPlayerBucketEmpty){
			Entity entity = (Entity) event.getPlayer();
			if (event.getBucket() != null){
				SBlock bL = null;
				SBlock uBL = null;
				Location waterBlock = block.getRelative(event.getBlockFace()).getLocation();
				for(BlockFace face : BlockFace.values()){
					if(!face.equals(BlockFace.SELF) && !face.equals(BlockFace.DOWN)){
						if(block.getRelative(face).getType().equals(Material.WATER) || block.getRelative(face).getType().equals(Material.LAVA)){
							waterBlock = block.getRelative(face).getLocation();
						}
					}
				}
				if(entity!=null){
					if(!TownyWars.blockBlackList.contains(event.getBlockClicked().getType()))
						bL = new SBlock(block, entity);
					if(!TownyWars.blockBlackList.contains((event.getBlockClicked().getLocation().add(0, 1, 0)).getBlock().getType()))
						uBL = new SBlock(waterBlock, entity);
				}else{
					if(!TownyWars.blockBlackList.contains(event.getBlockClicked().getType()))
						bL = new SBlock(block);
					if(!TownyWars.blockBlackList.contains((event.getBlockClicked().getLocation().add(0, 1, 0)).getBlock().getType()))
						uBL = new SBlock(waterBlock);
				}
				if(bL!=null && !plugin.containsBlockLocation(bL)){
					if(!plugin.addToList(bL)){
						Bukkit.getServer().getLogger().info(ChatColor.DARK_RED + "Cannot add to List");
					}
				}
				if(uBL!=null && !plugin.containsBlockLocation(uBL)){
					if(!plugin.addToList(uBL)){
						Bukkit.getServer().getLogger().info(ChatColor.DARK_RED + "Cannot add to List");
					}
				}
	
			}
		}
	}
	*/

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
        Tuple<Boolean, Boolean> verifyTowny = townyVerification(event.getPlayer(), event.getBlock().getLocation());
        boolean isWallDetected = isWallDetected(event.getBlock());
        boolean partOfTown = verifyTowny.a();
        boolean playerInTown = verifyTowny.b();

        if(isWallDetected && partOfTown && !playerInTown) {
            try {
                TownBlock townBlock = TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(event.getBlock().getLocation()));
                if (townBlock != null) {
                    // Capture a snapshot before the block is broken
                    PlotBlockData snapshot = new PlotBlockData(townBlock);

                    // Save the snapshot asynchronously to prevent potential lag on the main server thread
                    Bukkit.getScheduler().runTaskAsynchronously(TownyWars.getInstance(), snapshot::save);
                }
            } catch (NotRegisteredException ignored) {
                return;
            }

            // Cancel the block break event
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent event) {
        Tuple<Boolean, Boolean> verifyTowny = townyVerification(event.getPlayer(), event.getBlock().getLocation());
        boolean isWallDetected = isWallDetected(event.getBlock());
        boolean partOfTown = verifyTowny.a();
        boolean playerInTown = verifyTowny.b();

        if(isWallDetected && partOfTown && !playerInTown) {
            try {
                TownBlock townBlock = TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(event.getBlock().getLocation()));
                if (townBlock != null) {
                    // Capture a snapshot before the block is broken
                    PlotBlockData snapshot = new PlotBlockData(townBlock);

                    // Save the snapshot asynchronously to prevent potential lag on the main server thread
                    Bukkit.getScheduler().runTaskAsynchronously(TownyWars.getInstance(), snapshot::save);
                }
            } catch (NotRegisteredException ignored) {
                return;
            }

            // Cancel the block break event
            event.setCancelled(true);
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


    public Set<SBlock> getAttachedBlocks(Block block, Set<SBlock> sBlocks, Entity entity) {
        for (BlockFace face : BlockFace.values()) {
            if (!face.equals(BlockFace.SELF)) {
                Block relativeBlock = block.getRelative(face);
                Material type = relativeBlock.getType();
                if (type.isSolid()) {
                    addBlockToSet(sBlocks, relativeBlock, entity);
                }
                if (type == Material.VINE && ((Vine) relativeBlock.getState().getData()).isOnFace(face)) {
                    addBlockToSet(sBlocks, relativeBlock, entity);
                }
                if (type == Material.CHORUS_PLANT || type == Material.CHORUS_FLOWER) {
                    addBlockToSet(sBlocks, relativeBlock, entity);
                }
            }
        }

        Block aboveBlock = block.getRelative(BlockFace.UP);
        Material type = aboveBlock.getType();
        if (type.isSolid()) {
            addBlockToSet(sBlocks, aboveBlock, entity);
        }

        while (type == Material.CACTUS || type == Material.SUGAR_CANE || type == Material.CHORUS_PLANT || type == Material.CHORUS_FLOWER) {
            addBlockToSet(sBlocks, aboveBlock, entity);
            aboveBlock = aboveBlock.getRelative(BlockFace.UP);
            type = aboveBlock.getType();
        }

        return sBlocks;
    }

    private void addBlockToSet(Set<SBlock> sBlocks, Block block, Entity entity) {
        SBlock sBlock = (entity != null) ? new SBlock(block, entity) : new SBlock(block);
        if (!sBlocks.contains(sBlock)) {
            sBlocks.add(sBlock);
        }
    }
}