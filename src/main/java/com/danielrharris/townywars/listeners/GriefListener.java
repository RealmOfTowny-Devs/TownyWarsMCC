package com.danielrharris.townywars.listeners;

import com.danielharris.townywars.util.Utils;
import com.danielrharris.townywars.GriefManager;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.WarManager;
import com.danielrharris.townywars.tasks.AttackWarnBarTask;
import com.danielrharris.townywars.warObjects.War;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.PlayerCache;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.PlayerCache.TownBlockStatus;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.util.BukkitTools;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.Vine;

public class GriefListener implements Listener{
		
	private TownyWars mplugin=null;
	private final int DEBRIS_CHANCE;
	private GriefManager m;
	private TownyUniverse universe;
	
	public GriefListener(TownyWars aThis, GriefManager m)
	{ 
		this.mplugin=aThis;
		this.DEBRIS_CHANCE = TownyWars.debrisChance;
		this.m = m;
		universe = TownyUniverse.getInstance();
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void onWarTownDamage(BlockBreakEvent event) throws NotRegisteredException{
		if(TownyWars.allowGriefing && TownyWars.allowRollback){
			Block block = event.getBlock();
			if(TownyWars.worldBlackList!=(null))
				if(TownyWars.worldBlackList.contains(block.getWorld().getName().toString().toLowerCase())){
					return;
				}
			if(TownyWars.blockBlackList!=(null))
				if(TownyWars.blockBlackList.contains(block.getType())){
					return;
				}
			if(event.getPlayer()!=null){
				Player p = event.getPlayer();			
				if(TownyWars.atWar(p, block.getLocation())){
					if(universe!=null) {
						if(universe.hasTownBlock(WorldCoord.parseWorldCoord(block))){
							TownBlock townBlock = universe.getTownBlock(WorldCoord.parseWorldCoord(block));
							Town otherTown = null;
							Nation otherNation = null;
							int numOfBlocks = 0;
							if(townBlock!=null){
								otherTown = townBlock.getTown();
								otherNation = otherTown.getNation();
							}
							numOfBlocks = numOfBlocks + getNumOfAttachedBlocks(block);
							if(block.getType()!=Material.TNT){
								numOfBlocks++;
							}	
							//griefing is allowed and so is the rollback feature, so lets record the blocks and add them to the list	
							if(otherNation!=null && otherTown!=null){
								War wwar = WarManager.getWarForNation(otherNation);
								double dPoints = ((numOfBlocks * TownyWars.pBlockPoints)*1e2)/1e2;
								wwar.chargePoints(townBlock.getTown().toString(), Math.round((float)dPoints));
								new AttackWarnBarTask(otherTown, mplugin).runTask(mplugin);
								event.setCancelled(true);
								block.breakNaturally();
							}
						}					
					}											
				}					
			}
		}
	}
	
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void suppressTownyBuildEvent(BlockPlaceEvent event) {
		if(event.getPlayer()!=null && TownyWars.allowGriefing){			
			if(TownyWars.atWar(event.getPlayer(), event.getBlock().getLocation())){
				event.setCancelled(true);
			}
		}
	}
	
	@SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void onWarBuild(BlockPlaceEvent event) {
		if(TownyWars.allowGriefing){
			Block block = event.getBlock();
			if(event.getPlayer()!=null){		
				Player p = event.getPlayer();
				if(TownyWars.atWar(p, block.getLocation())){
					event.setCancelled(false);	
					event.setBuild(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void ignoreProtections(EntityExplodeEvent ev){
		Location center = ev.getLocation();
		TownBlock townBlock = null;
		try {
			townBlock = universe.getTownBlock(WorldCoord.parseWorldCoord(center));
			if(townBlock!=null){
				if(TownyWars.allowGriefing){
					if(TownyWars.warExplosions){
						if(townBlock.hasTown()){
							if(townBlock.getTown().hasNation()){
								Nation nation = townBlock.getTown().getNation();
								if(WarManager.getWarForNation(nation)!=null){
									ev.setCancelled(true);
								}
							}else {
								Town town = townBlock.getTown();
								if(WarManager.getWarForTown(town)!=null){
									ev.setCancelled(true);
								}
							}
						}								
					}
				}
			}
		} catch (NotRegisteredException e) {
			// Do nothing, explosion in the wild
		}		
	}
	
	@SuppressWarnings({ "deprecation" })
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onExplode(EntityExplodeEvent ev) {
		ev.setCancelled(false);
		List<Block> blocks = ev.blockList();
		Location center = ev.getLocation();
		TownBlock townBlock = null;
		Player p = Bukkit.getPlayer("Myekaan");
		if(p!=null){
			p.sendMessage(ev.getEntityType().toString());
		}
		if(TownyWars.allowGriefing){
			if(TownyWars.warExplosions){
				try{
					townBlock = universe.getTownBlock(WorldCoord.parseWorldCoord(center));
					if(townBlock!=null){
						if(townBlock.hasTown()){
							if(townBlock.getTown().hasNation()){
								Nation nation = townBlock.getTown().getNation();
								if(WarManager.getWarForNation(nation)!=null){
									if(blocks!=null){
										int numOfBlocks = blocks.size();
										for(Block block : blocks){
											if(block!=null){
												if(TownyWars.worldBlackList == null || TownyWars.worldBlackList.isEmpty() || !TownyWars.worldBlackList.contains(block.getWorld().getName().toString().toLowerCase())){
													if(TownyWars.blockBlackList == null || TownyWars.blockBlackList.isEmpty() || !TownyWars.blockBlackList.contains(block.getType())){
														numOfBlocks = numOfBlocks + getNumOfAttachedBlocks(block);
													}
												}
											}
										}
										War wwar = WarManager.getWarForNation(nation);
										double dPoints = ((numOfBlocks * TownyWars.pBlockPoints)*1e2)/1e2;
										wwar.chargePoints(townBlock.getTown().toString(), Math.round((float)dPoints));
										new AttackWarnBarTask(townBlock.getTown(), mplugin).runTask(mplugin);
										ev.setCancelled(false);
									}
									if(TownyWars.realisticExplosions){
										//p.sendMessage("Doing Realistic Explosion");
										Explode.explode(ev.getEntity(), blocks, center, DEBRIS_CHANCE);
									}
								}
							}else{
								Town town = townBlock.getTown();
								if(WarManager.getWarForTown(town)!=null){
									if(blocks!=null){
										int numOfBlocks = blocks.size();
										for(Block block : blocks){
											if(block!=null){
												if(TownyWars.worldBlackList == null || TownyWars.worldBlackList.isEmpty() || !TownyWars.worldBlackList.contains(block.getWorld().getName().toString().toLowerCase())){
													if(TownyWars.blockBlackList == null || TownyWars.blockBlackList.isEmpty() || !TownyWars.blockBlackList.contains(block.getType())){
														numOfBlocks = numOfBlocks + getNumOfAttachedBlocks(block);
													}
												}
											}
										}
										War wwar = WarManager.getWarForTown(town);
										double dPoints = ((numOfBlocks * TownyWars.pBlockPoints)*1e2)/1e2;
										wwar.chargePoints(townBlock.getTown().toString(), Math.round((float)dPoints));
										new AttackWarnBarTask(townBlock.getTown(), mplugin).runTask(mplugin);
										ev.setCancelled(false);
									}
									if(TownyWars.realisticExplosions){
										//p.sendMessage("Doing Realistic Explosion");
										Explode.explode(ev.getEntity(), blocks, center, DEBRIS_CHANCE);
									}
								}
								if(townBlock.getPermissions().explosion){
									if(TownyWars.realisticExplosions){
										if(blocks!=null){
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
					if(!universe.hasTownBlock(WorldCoord.parseWorldCoord(center)) && TownySettings.isExplosions() && TownyWars.realisticExplosions){
						if(blocks!=null){
							Explode.explode(ev.getEntity(), blocks, center, 75);
						}
						ev.setCancelled(false);
					}
				}
			}
		}
	}
	
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
	
	public int getNumOfAttachedBlocks(Block block){
		int numOfBlocks = 0;
		for(BlockFace face : BlockFace.values()){
			if(!face.equals(BlockFace.SELF)){
				if((block.getRelative(face)).getState().getData() instanceof Attachable){
					Block b = (block.getRelative(face));
					Attachable att = (Attachable) (block.getRelative(face)).getState().getData();
					if(b.getRelative(att.getAttachedFace()).equals(block)){
						numOfBlocks++;		
					}
				}
				if(block.getRelative(face).getState().getData() instanceof Vine){
					Vine vine = (Vine) block.getRelative(face).getState().getData();
					if(vine.isOnFace(face)){
						numOfBlocks++;		
					}
				}
				if((block.getRelative(face)).getType().equals(Material.CHORUS_PLANT)){
					numOfBlocks++;	
				}
				if((block.getRelative(face)).getType().equals(Material.CHORUS_FLOWER)){
					numOfBlocks++;
				}
			}
		}
		if(Utils.isOtherAttachable((block.getRelative(BlockFace.UP)).getType())){
			numOfBlocks++;	
		}
		if((block.getRelative(BlockFace.UP)).getType().equals(Material.CACTUS) || (block.getRelative(BlockFace.UP)).getType().equals(Material.SUGAR_CANE) || (block.getRelative(BlockFace.UP)).getType().equals(Material.CHORUS_PLANT) || (block.getRelative(BlockFace.UP)).getType().equals(Material.CHORUS_FLOWER)){
			Block up = block.getRelative(BlockFace.UP);
			do
			{
				if(up.getType().equals(Material.CACTUS) || up.getType().equals(Material.SUGAR_CANE) || up.getType().equals(Material.CHORUS_PLANT) || up.getType().equals(Material.CHORUS_FLOWER)){
					numOfBlocks++;
				}
				up = ((up.getLocation()).add(0,1,0)).getBlock();
			}while(up.getType().equals(Material.CACTUS) || up.getType().equals(Material.SUGAR_CANE) || up.getType().equals(Material.CHORUS_PLANT) || up.getType().equals(Material.CHORUS_FLOWER));
		}
		return numOfBlocks;
	}
	
}