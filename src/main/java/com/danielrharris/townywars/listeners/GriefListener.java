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

public class GriefListener implements Listener {

    private static ConcurrentHashMap<Town, Set<SBlock>> sBlocks;
    private TownyWars mplugin;
    private double DEBRIS_CHANCE;
    private GriefManager m;
    private WallManager wallManager = new WallManager(); // Create a new instance or pass it in as required.

    public GriefListener(TownyWars aThis, GriefManager m) {
        this.mplugin = aThis;
        this.DEBRIS_CHANCE = TownyWars.debrisChance;
        this.m = m;
        sBlocks = this.m.loadData();
        final int storeLimit = 10; // You can set this to an appropriate value
        final Map<Player, Stack<Location>> pastLocation = new HashMap<>();

        new BukkitRunnable() {
            long timeTrack = System.currentTimeMillis();

            @Override
            public void run() {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    Tuple<Boolean, Boolean> verifyTowny = townyVerification(target, target.getLocation());
                    boolean isWallDetected = wallManager.isWallDetected(target.getLocation().getBlock());
                    boolean partOfTown = verifyTowny.a();
                    boolean playerInTown = verifyTowny.b();

                    if (!partOfTown || playerInTown) continue;

                    if (isWallDetected) {
                        for (int i = 1; i < storeLimit - 1; i++) {
                            if (!pastLocation.containsKey(target)) break;
                            if (i >= pastLocation.get(target).size()) break;
                            Block targetPosition = pastLocation.get(target).elementAt(pastLocation.get(target).size() - i).getBlock();
                            if (!wallManager.isWallDetected(targetPosition) || i + 1 >= storeLimit - 1) {
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

    //Here's where I'll grab the block break event and make it record broken blocks
    //during war
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onWarTownDamage(BlockBreakEvent event) throws NotRegisteredException {
        if(event.isCancelled()) return;
        if(isOnlyWallDetected(event.getBlock()) && !isNatural(event.getBlock())) return;

        if (TownyWars.allowGriefing) {
            Block block = event.getBlock();
            if (TownyWars.worldBlackList != null && TownyWars.worldBlackList.contains(block.getWorld().getName().toLowerCase())) {
                return;
            }
            if (TownyWars.blockBlackList != null && TownyWars.blockBlackList.contains(block.getType())) {
                return;
            }

            if (event.getPlayer() != null) {
                Player p = event.getPlayer();
                if (TownyWars.atWar(p, block.getLocation())) {
                    try {
                        TownBlock townBlock = TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(block.getLocation()));
                        if (townBlock != null) {
                            // Save a snapshot of the block before it gets broken.
                            PlotBlockData snapshot = new PlotBlockData(townBlock);
                            TownyRegenAPI.addPlotChunkSnapshot(snapshot);

                            Town otherTown = townBlock.getTown();
                            Nation otherNation = otherTown.getNation();
                            Set<SBlock> sBlocks = getAttachedBlocks(block, new HashSet<SBlock>(), p);

                            SBlock check = new SBlock(block);
                            if (!WallManager.containsBlock(WallManager.getGriefedBlocks().get(otherTown), check) && block.getType() != Material.TNT) {
                                sBlocks.add(new SBlock(block, p));
                            }

                            if (TownyWars.allowRollback) {
                                Set<SBlock> temp = WallManager.getGriefedBlocks().get(otherTown);
                                if (temp == null) {
                                    temp = new HashSet<SBlock>();
                                }
                                temp.addAll(sBlocks);
                                WallManager.getGriefedBlocks().put(otherTown, temp);
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

    // first = is block in a town, second = is player part of town
    public Tuple<Boolean, Boolean> townyVerification(Player target, Location location) {
        TownBlock townBlock = TownyAPI.getInstance().getTownBlock(location);
        if(townBlock==null) return new Tuple<>(false, false);
        try {
            Town targetTown = townBlock.getTown();
            if(!targetTown.hasResident(target)) return new Tuple<>(true, false);
            else return new Tuple<>(true, true);
        } catch (NotRegisteredException ex) { return new Tuple<>(false, false); }
    }

    public void checkCache(Block target) {
        long hash = generateBlockHash(target.getLocation());  // Assuming we're using a new hash function, or an identifier.

        // If the blockCache doesn't contain the info about this block.
        if (!blockCache.containsKey(hash)) {

            // Get the snapshot for the block's plot.
            WorldCoord worldCoord = WorldCoord.parseWorldCoord(target.getLocation());
            PlotBlockData snapshot = TownyRegenAPI.getPlotChunkSnapshot(worldCoord);
            if (snapshot != null) {

                // Check if the block in the snapshot matches the state of the block in the world.
                // "isNatural" checks the block's state in the snapshot.
                boolean isBlockNatural = isNatural(target, snapshot);

                // Cache the result.
                blockCache.put(hash, isBlockNatural);
            } else {
                // If there's no snapshot, we could either default to considering the block "natural", or another default.
                blockCache.put(hash, true); // Setting as true by default, change based on your requirements.
            }
        }
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

    @SuppressWarnings({"deprecation"})
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onExplode(EntityExplodeEvent ev) throws NotRegisteredException {
        ev.setCancelled(false);
        List<Block> blocks = ev.blockList();
        Location center = ev.getLocation();
        TownBlock townBlock = null;
        if (TownyWars.allowGriefing) {
            if (TownyWars.warExplosions) {
                try {
                    townBlock = TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(center));
                    if (townBlock != null) {
                        if (townBlock.hasTown()) {
                            if (townBlock.getTown().hasNation()) {
                                Nation nation = townBlock.getTown().getNation();
                                if (WarManager.getWarForNation(nation) != null) {
                                    Set<SBlock> sBlocks = new HashSet<SBlock>();
                                    if (blocks != null) {
                                        for (Block block : blocks) {
                                            if (block != null) {
                                                if (TownyWars.worldBlackList == null || TownyWars.worldBlackList.isEmpty() || !TownyWars.worldBlackList.contains(block.getWorld().getName().toString().toLowerCase())) {
                                                    if (TownyWars.blockBlackList == null || TownyWars.blockBlackList.isEmpty() || !TownyWars.blockBlackList.contains(block.getType())) {
                                                        sBlocks = getAttachedBlocks(block, sBlocks, null);
                                                        if (!block.getType().equals(Material.TNT)) {
                                                            sBlocks.add(new SBlock(block));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (TownyWars.allowRollback) {
                                            WeakReference<Set<SBlock>> temp = new WeakReference<Set<SBlock>>(GriefListener.sBlocks.get(townBlock.getTown()));
                                            Set<SBlock> j = new HashSet<SBlock>();
                                            if (temp.get() == null || (temp.get().isEmpty())) {
                                                temp = new WeakReference<Set<SBlock>>(j);
                                            }
                                            for (SBlock s : sBlocks) {
                                                temp.get().add(s);
                                            }
                                            GriefListener.sBlocks.put(townBlock.getTown(), temp.get());
                                        }
                                        War wwar = WarManager.getWarForNation(nation);
                                        double points = (Math.round(((double) (sBlocks.size() * TownyWars.pBlockPoints)) * 1e2) / 1e2);
                                        wwar.chargeTownPoints(nation, townBlock.getTown(), points);
                                        new AttackWarnBarTask(townBlock.getTown(), mplugin).runTask(mplugin);
                                        ev.setCancelled(false);
                                    }
                                    if (TownyWars.realisticExplosions) {
                                        //p.sendMessage("Doing Realistic Explosion");
                                        Explode.explode(ev.getEntity(), blocks, center, DEBRIS_CHANCE);
                                    }
                                    return;
                                }
                            } else {
                                if (townBlock.getPermissions().explosion) {
                                    if (TownyWars.realisticExplosions) {
                                        if (blocks != null) {
                                            Explode.explode(ev.getEntity(), blocks, center, DEBRIS_CHANCE);
                                        }
                                    }
                                    ev.setCancelled(false);
                                    return;
                                }
                            }
                        }
                    }
                } catch (NotRegisteredException e) {
                    if (TownyAPI.getInstance().isWilderness(center.getBlock()) && TownySettings.isExplosions() && TownyWars.realisticExplosions) {
                        if (blocks != null) {
                            Explode.explode(ev.getEntity(), blocks, center, 75);
                        }
                        ev.setCancelled(false);
                        return;
                    }
                }
                if (TownyAPI.getInstance().isWilderness(center.getBlock()) && TownySettings.isExplosions() && TownyWars.realisticExplosions) {
                    if (blocks != null) {
                        Explode.explode(ev.getEntity(), blocks, center, 75);
                    }
                    ev.setCancelled(false);
                    return;
                }
            }
        }
    }

    public boolean containsBlock(Set<SBlock> sBlocks, SBlock sb) {
        if (sb != null) {
            if (sBlocks != null) {
                for (SBlock s : sBlocks) {
                    if (sb.getLocation() == s.getLocation()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Set<SBlock> getAttachedBlocks(Block block, Set<SBlock> sBlocks, Entity entity) {
        SBlock check;
        for (BlockFace face : BlockFace.values()) {
            if (!face.equals(BlockFace.SELF)) {
                if ((block.getRelative(face)).getState().getData() instanceof Attachable) {
                    Block b = (block.getRelative(face));
                    Attachable att = (Attachable) (block.getRelative(face)).getState().getData();
                    if (b.getRelative(att.getAttachedFace()).equals(block)) {
                        check = new SBlock(block.getRelative(face));
                        if (!containsBlock(sBlocks, check)) {
                            if (entity != null) {
                                sBlocks.add(new SBlock((block.getRelative(face)), entity));
                            } else {
                                sBlocks.add(check);
                            }
                        }
                    }
                }
                if (block.getRelative(face).getState().getData() instanceof Vine) {
                    Vine vine = (Vine) block.getRelative(face).getState().getData();
                    if (vine.isOnFace(face)) {
                        check = new SBlock(block.getRelative(face));
                        if (!containsBlock(sBlocks, check)) {
                            if (entity != null) {
                                sBlocks.add(new SBlock((block.getRelative(face)), entity));
                            } else {
                                sBlocks.add(check);
                            }
                        }
                    }
                }
                if ((block.getRelative(face)).getType().equals(Material.CHORUS_PLANT)) {
                    check = new SBlock(block.getRelative(face));
                    if (!containsBlock(sBlocks, check)) {
                        if (entity != null) {
                            sBlocks.add(new SBlock((block.getRelative(face)), entity));
                        } else {
                            sBlocks.add(check);
                        }
                    }
                }
                if ((block.getRelative(face)).getType().equals(Material.CHORUS_FLOWER)) {
                    check = new SBlock(block.getRelative(face));
                    if (!containsBlock(sBlocks, check)) {
                        if (entity != null) {
                            sBlocks.add(new SBlock((block.getRelative(face)), entity));
                        } else {
                            sBlocks.add(check);
                        }
                    }
                }
            }
        }
        if (Utils.isOtherAttachable((block.getRelative(BlockFace.UP)).getType())) {
            check = new SBlock(block.getRelative(BlockFace.UP));
            if (!containsBlock(sBlocks, check)) {
                if (entity != null) {
                    sBlocks.add(new SBlock((block.getRelative(BlockFace.UP)), entity));
                } else {
                    sBlocks.add(check);
                }
            }
        }
        if ((block.getRelative(BlockFace.UP)).getType().equals(Material.CACTUS) || (block.getRelative(BlockFace.UP)).getType().equals(Material.SUGAR_CANE) || (block.getRelative(BlockFace.UP)).getType().equals(Material.CHORUS_PLANT) || (block.getRelative(BlockFace.UP)).getType().equals(Material.CHORUS_FLOWER)) {
            Block up = block.getRelative(BlockFace.UP);
            do {
                if (up.getType().equals(Material.CACTUS) || up.getType().equals(Material.SUGAR_CANE) || up.getType().equals(Material.CHORUS_PLANT) || up.getType().equals(Material.CHORUS_FLOWER)) {
                    check = new SBlock(up);
                    if (!containsBlock(sBlocks, check)) {
                        if (entity != null) {
                            if (!containsBlock(sBlocks, new SBlock(up, entity))) {
                                sBlocks.add(new SBlock(up, entity));
                            }
                        } else {
                            if (!containsBlock(sBlocks, new SBlock(up))) {
                                sBlocks.add(check);
                            }
                        }
                    }
                }
                up = ((up.getLocation()).add(0, 1, 0)).getBlock();
            } while (up.getType().equals(Material.CACTUS) || up.getType().equals(Material.SUGAR_CANE) || up.getType().equals(Material.CHORUS_PLANT) || up.getType().equals(Material.CHORUS_FLOWER));
        }
        return sBlocks;
    }
}