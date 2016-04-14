package com.danielrharris.townywars.listeners;

import com.danielrharris.townywars.GriefManager;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.War;
import com.danielrharris.townywars.WarManager;
import com.danielrharris.townywars.tasks.BossBarTask;
import com.danielrharris.townywars.tasks.SaveTask;
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
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.PlayerCache.TownBlockStatus;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import com.palmergames.bukkit.util.BukkitTools;

import me.drkmatr1984.BlocksAPI.utils.SBlock;
import me.drkmatr1984.BlocksAPI.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private GriefManager m;
	private final int DEBRIS_CHANCE;
	
	public GriefListener(TownyWars aThis, GriefManager m)
	{ 
		this.mplugin=aThis;
		this.m = m;
		this.DEBRIS_CHANCE = TownyWars.debrisChance;
	}
	
	//Here's where I'll grab the block break event and make it record broken blocks
	//during war
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void onWarTownDamage(BlockBreakEvent event){
		if(TownyWars.allowGriefing){
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
				HashSet<SBlock> sBlocks = new HashSet<SBlock>();
				Player p = event.getPlayer();
				Entity entity = (Entity) p;				
				if(TownyWars.atWar(p, block.getLocation())){					
					if(TownyWars.allowRollback){
						if(TownyUniverse.getTownBlock(block.getLocation())!=null){
							TownBlock townBlock = TownyUniverse.getTownBlock(block.getLocation());
							Town otherTown = null;
							Nation otherNation = null;
							try {
								otherTown = townBlock.getTown();
								otherNation = otherTown.getNation();
							} catch (NotRegisteredException e) {
								e.printStackTrace();
								p.sendMessage("An error has occurred. Please get an Admin to check the logs.");
							}
							//griefing is allowed and so is the rollback feature, so lets record the blocks and add them to the list
							for(BlockFace face : BlockFace.values()){
								if(!face.equals(BlockFace.SELF)){
									if((block.getRelative(face)).getState().getData() instanceof Attachable){
										Block b = (block.getRelative(face));
										Attachable att = (Attachable) (block.getRelative(face)).getState().getData();
										if(b.getRelative(att.getAttachedFace()).equals(block)){
											if(entity!=null){
												sBlocks.add(new SBlock((block.getRelative(face)), entity));
											}else{
												sBlocks.add(new SBlock((block.getRelative(face))));
											}
										}
									}
									if(block.getRelative(face).getState().getData() instanceof Vine){
										Vine vine = (Vine) block.getRelative(face).getState().getData();
										if(vine.isOnFace(face)){
											if(entity!=null){
												sBlocks.add(new SBlock((block.getRelative(face)), entity));
											}else{
												sBlocks.add(new SBlock((block.getRelative(face))));
											}
										}
									}
									if((block.getRelative(face)).getType().equals(Material.CHORUS_PLANT)){
										if(entity!=null){
											sBlocks.add(new SBlock((block.getRelative(face)), entity));
										}else{
											sBlocks.add(new SBlock((block.getRelative(face))));
										}
									}
									if((block.getRelative(face)).getType().equals(Material.CHORUS_FLOWER)){
										if(entity!=null){
											sBlocks.add(new SBlock((block.getRelative(face)), entity));
										}else{
											sBlocks.add(new SBlock((block.getRelative(face))));
										}
									}
								}
							}
							if(Utils.isOtherAttachable((block.getRelative(BlockFace.UP)).getType())){
								if(entity!=null){
									sBlocks.add(new SBlock((block.getRelative(BlockFace.UP)), entity));
								}else{
									sBlocks.add(new SBlock((block.getRelative(BlockFace.UP))));
								}
							}
							if((block.getRelative(BlockFace.UP)).getType().equals(Material.CACTUS) || (block.getRelative(BlockFace.UP)).getType().equals(Material.SUGAR_CANE_BLOCK) || (block.getRelative(BlockFace.UP)).getType().equals(Material.CHORUS_PLANT) || (block.getRelative(BlockFace.UP)).getType().equals(Material.CHORUS_FLOWER)){
								Block up = block.getRelative(BlockFace.UP);
								do
								{
									if(up.getType().equals(Material.CACTUS) || up.getType().equals(Material.SUGAR_CANE_BLOCK) || up.getType().equals(Material.CHORUS_PLANT) || up.getType().equals(Material.CHORUS_FLOWER)){
										if(entity!=null){
											sBlocks.add(new SBlock(up, entity));
										}else{
											sBlocks.add(new SBlock(up));
										}
									}
									up = ((up.getLocation()).add(0,1,0)).getBlock();
								}while(up.getType().equals(Material.CACTUS) || up.getType().equals(Material.SUGAR_CANE_BLOCK) || up.getType().equals(Material.CHORUS_PLANT) || up.getType().equals(Material.CHORUS_FLOWER));
							}
							if(entity!=null){
								sBlocks.add(new SBlock(block, entity));
							}else{
								sBlocks.add(new SBlock(block));
							}
							if(otherNation!=null && otherTown!=null){
								War wwar = WarManager.getWarForNation(otherNation);
								double points = (Math.round(((double)(sBlocks.size() * TownyWars.pBlockPoints))*1e2)/1e2);
								wwar.chargeTownPoints(otherNation, otherTown, points);
								new BossBarTask(otherTown, mplugin).runTask(mplugin);
								new SaveTask(m, otherTown, sBlocks).runTaskLaterAsynchronously(mplugin, 100L);
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
		if(TownyWars.allowGriefing){
			event.setCancelled(true);
		}
	}
	
	@SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onWarBuild(BlockPlaceEvent event) {
		if(TownyWars.allowGriefing){
			Block block = event.getBlock();
			if(event.getPlayer()!=null){		
				Player p = event.getPlayer();
				Entity entity = (Entity) p;
				if(TownyWars.atWar(p, block.getLocation())){				
					if(TownyWars.allowRollback){
						if(TownyUniverse.getTownBlock(block.getLocation())!=null){
							TownBlock townBlock = TownyUniverse.getTownBlock(block.getLocation());
							Town otherTown = null;
							Nation otherNation = null;
							try {
								otherTown = townBlock.getTown();
								otherNation = otherTown.getNation();
							} catch (NotRegisteredException e) {
								e.printStackTrace();
								p.sendMessage("An error has occurred. Please get an Admin to check the logs.");
							}
							SBlock sb;
							if(entity!=null){
								sb = new SBlock(block, entity);
							}else{
								sb = new SBlock(block);
							}
							sb.mat = "AIR";
							new SaveTask(m, otherTown, sb).runTaskLaterAsynchronously(mplugin, 100L);
						}
						
					}
					event.setBuild(true);
					event.setCancelled(false);				
				}else{
					event.setBuild(true);
					event.setCancelled(false);
					Towny plugin = TownyWars.towny;
					if (plugin.isError()) {
						event.setCancelled(true);
						return;
					}

					Player player = event.getPlayer();
					WorldCoord worldCoord;
					try {				
						TownyWorld world = TownyUniverse.getDataSource().getWorld(block.getWorld().getName());
						worldCoord = new WorldCoord(world.getName(), Coord.parseCoord(block));

						//Get build permissions (updates if none exist)
						boolean bBuild = PlayerCacheUtil.getCachePermission(player, block.getLocation(), BukkitTools.getTypeId(block), BukkitTools.getData(block), TownyPermission.ActionType.BUILD);

						// Allow build if we are permitted
						if (bBuild)
							return;
						
						/*
						 * Fetch the players cache
						 */
						PlayerCache cache = plugin.getCache(player);
						TownBlockStatus status = cache.getStatus();

						/*
						 * Flag war
						 */
						if (((status == TownBlockStatus.ENEMY) && TownyWarConfig.isAllowingAttacks()) && (event.getBlock().getType() == TownyWarConfig.getFlagBaseMaterial())) {

							try {
								if (TownyWar.callAttackCellEvent(plugin, player, block, worldCoord))
									return;
							} catch (TownyException e) {
								TownyMessaging.sendErrorMsg(player, e.getMessage());
							}

							event.setBuild(false);
							event.setCancelled(true);

						} else if (status == TownBlockStatus.WARZONE) {
							if (!TownyWarConfig.isEditableMaterialInWarZone(block.getType())) {
								event.setBuild(false);
								event.setCancelled(true);
								TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_warzone_cannot_edit_material"), "build", block.getType().toString().toLowerCase()));
							}
							return;
						} else {
							event.setBuild(false);
							event.setCancelled(true);
						}

						/* 
						 * display any error recorded for this plot
						 */
						if ((cache.hasBlockErrMsg()) && (event.isCancelled()))
							TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());

					} catch (NotRegisteredException e1) {
						TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_not_configured"));
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onExplode(EntityExplodeEvent ev) {
		List<Block> blocks = ev.blockList();
		Location center = ev.getLocation();
		TownBlock townBlock = null;
		if(TownyWars.allowGriefing){
			if(TownyWars.warExplosions){
				try{
					townBlock = TownyUniverse.getTownBlock(center);
					if(townBlock.hasTown()){
						if(townBlock.getTown().hasNation()){
							Nation nation = townBlock.getTown().getNation();
							if(WarManager.getWarForNation(nation)!=null){
								//war so do explosion. Don't forget to save the blocks
								if(TownyWars.allowRollback){
									ArrayList<SBlock> sBlocks = new ArrayList<SBlock>();
									for(Block block : blocks){
										if(TownyWars.worldBlackList.contains(block.getWorld().getName().toString().toLowerCase())){
											break;
										}
										if(TownyWars.blockBlackList.contains(block.getType())){
											break;
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
										
									}
									
									//This won't happen unless rollback is on... Need to fix in other listeners
									War wwar = WarManager.getWarForNation(nation);
									double points = (Math.round(((double)(sBlocks.size() * TownyWars.pBlockPoints))*1e2)/1e2);
									wwar.chargeTownPoints(nation, townBlock.getTown(), points);
									new BossBarTask(townBlock.getTown(), mplugin).runTask(mplugin);
									new SaveTask(m, townBlock.getTown(), (Set<SBlock>)Utils.listToSet(sBlocks)).runTaskLaterAsynchronously(mplugin, 100L);
								}
								ev.setCancelled(false);
								if(TownyWars.realisticExplosions){
									Explode.explode(ev.getEntity(), blocks, center, DEBRIS_CHANCE);
								}	
							}
						}else{
							if(townBlock.getPermissions().explosion){
								ev.setCancelled(false);
								if(TownyWars.realisticExplosions){
									Explode.explode(ev.getEntity(), blocks, center, DEBRIS_CHANCE);
								}	
							}
						}		
					}
				} catch (NotRegisteredException e) {
					if(TownyUniverse.isWilderness(center.getBlock()) && TownySettings.isExplosions()){
						ev.setCancelled(false);
						Explode.explode(ev.getEntity(), blocks, center, 75);
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
}