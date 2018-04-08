package com.danielrharris.townywars.listeners;

import com.danielrharris.townywars.GriefManager;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.War;
import com.danielrharris.townywars.WarManager;
import com.danielrharris.townywars.tasks.AttackWarnBarTask;
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
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import com.palmergames.bukkit.util.BukkitTools;

import me.drkmatr1984.BlocksAPI.utils.SBlock;
import me.drkmatr1984.BlocksAPI.utils.Utils;
import net.md_5.bungee.api.ChatColor;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Attachable;
import org.bukkit.material.Vine;
import org.bukkit.plugin.PluginBase;

public class GriefListener implements Listener{
		
	private TownyWars mplugin=null;
	private final int DEBRIS_CHANCE;
	private static ConcurrentHashMap<Town, Set<SBlock>> sBlocks;
	private GriefManager m;
	
	public GriefListener(TownyWars aThis, GriefManager m)
	{ 
		this.mplugin=aThis;
		this.DEBRIS_CHANCE = TownyWars.debrisChance;
		this.m = m;
		sBlocks = this.m.loadData();
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
				Player p = event.getPlayer();
				Entity entity = (Entity) p;				
				if(TownyWars.atWar(p, block.getLocation())){
					TownBlock townBlock = TownyUniverse.getTownBlock(block.getLocation());
					Town otherTown = null;
					Nation otherNation = null;
					Set<SBlock> sBlocks = new HashSet<SBlock>();
					try {
						if(townBlock!=null){
							otherTown = townBlock.getTown();
							otherNation = otherTown.getNation();
						}
					} catch (NotRegisteredException e) {
						e.printStackTrace();
						p.sendMessage("An error has occurred. Please get an Admin to check the logs.");
						return;
					}
					sBlocks = getAttachedBlocks(block, sBlocks, entity);
					SBlock check = new SBlock(block);
					if(!containsBlock(GriefListener.sBlocks.get(otherTown), check) && block.getType()!=Material.TNT){
						if(entity!=null){
							sBlocks.add(new SBlock(block, entity));
						}else{
							sBlocks.add(check);
						}
					}	
					if(TownyWars.allowRollback){
						WeakReference<Set<SBlock>> temp = new WeakReference<Set<SBlock>>(GriefListener.sBlocks.get(otherTown)); 
						Set<SBlock> j = new HashSet<SBlock>();
						for(SBlock s : sBlocks){
							if(temp.get()!=null && !(temp.get().isEmpty())){
								temp.get().add(s);
							}else{
								j.add(s);
								temp = new WeakReference<Set<SBlock>>(j);
							}
						}
						GriefListener.sBlocks.put(otherTown, temp.get());						
					}
					//griefing is allowed and so is the rollback feature, so lets record the blocks and add them to the list	
					if(otherNation!=null && otherTown!=null){
						War wwar = WarManager.getWarForNation(otherNation);
						double points = (Math.round(((double)(sBlocks.size() * TownyWars.pBlockPoints))*1e2)/1e2);
						wwar.chargeTownPoints(otherNation, otherTown, points);
						new AttackWarnBarTask(otherTown, mplugin).runTask(mplugin);
						event.setCancelled(true);
						block.breakNaturally();
					}							
				}					
			}
		}
	}
	
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void suppressTownyBuildEvent(BlockPlaceEvent event) {
		if(TownyWars.allowGriefing){
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void wallBreakEvent(BlockBreakEvent event) {
		if (TownyWars.wallBlocks.containsKey(event.getBlock().getChunk()))
		{
			Player player = event.getPlayer();
			if (TownyWars.wallBlocks.get(event.getBlock().getChunk()).contains(event.getBlock().getLocation()))
			{
				Town town;
				try {
					town = WorldCoord.parseWorldCoord(event.getBlock().getLocation()).getTownBlock().getTown();
					
					 if (town != null)
					    {
					    	boolean found = false;
					    	for (Resident res : town.getResidents())
					    	{
					    		//Bukkit.getServer().broadcastMessage(res.getName());
					    		if (res.getName().contains(player.getName()))
					    		{
					    			found = true;
					    			player.sendMessage(ChatColor.GREEN + "Wall block removed");
					    			Chunk chunk = event.getBlock().getChunk();
					    			Location loc = event.getBlock().getLocation();
					    			List<String> list = TownyWars.wallConfig.getStringList(chunk.getX() + "," + chunk.getZ() + "," + loc.getWorld().getName());
					    			list.remove(loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
					    			TownyWars.wallConfig.set(chunk.getX() + "," + chunk.getZ() + "," + loc.getWorld().getName(), list);
					    			try {
										TownyWars.wallConfig.save(TownyWars.wallConfigFile);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
					    			if (TownyWars.wallBlocks.containsKey(chunk))
					    			{
					    				List<Location> locList = TownyWars.wallBlocks.get(chunk);
					    				locList.remove(event.getBlock().getLocation());
					    				TownyWars.wallBlocks.replace(chunk, locList);
					    			}
					    			
					    			return;
					    		}
					    	}
					    	if (!found)
					    	{
					    		event.setCancelled(true);
					    		player.sendMessage(ChatColor.RED + "You can only damage walls with explosives");
					    		return;
					    	}
					    }
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void onWarBuild(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if (item.hasItemMeta())
		{
			ItemMeta meta = item.getItemMeta();
			if (meta.hasDisplayName())
			{
				if (meta.getDisplayName().contains(TownyWars.townyBlockName))
				{
					Location loc = event.getBlock().getLocation();
					try {
					    //Try to get the town where the player is Standing
					    Town town = WorldCoord.parseWorldCoord(loc).getTownBlock().getTown();
					    if (town != null)
					    {
					    	boolean found = false;
					    	for (Resident res : town.getResidents())
					    	{
					    		//Bukkit.getServer().broadcastMessage(res.getName());
					    		if (res.getName().contains(player.getName()))
					    		{
					    			found = true;
					    			Chunk chunk = loc.getChunk();
					    			player.sendMessage(ChatColor.GREEN + "Wall block placed!");
					    			if (TownyWars.wallBlocks.containsKey(chunk))
					    			{
					    				List<Location> locList = TownyWars.wallBlocks.get(chunk);
					    				locList.add(loc);
					    				TownyWars.wallBlocks.replace(chunk, locList);
					    			}
					    			else
					    			{
					    				List<Location> locList = new ArrayList<Location>();
					    				locList.add(loc);
					    				TownyWars.wallBlocks.put(chunk, locList);
					    			}
					    			///TownyWars.wallConfig.set(arg0, arg1);

					    			List<String> list = TownyWars.wallConfig.getStringList(chunk.getX() + "," + chunk.getZ() + "," + loc.getWorld().getName());
					    			list.add(loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
					    			TownyWars.wallConfig.set(chunk.getX() + "," + chunk.getZ() + "," + loc.getWorld().getName(), list);
					    			//TownyWars.wallConfig.set(chunk.getX() + "," + chunk.getZ(), loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "," + loc.getWorld().getName());
					    			try {
										TownyWars.wallConfig.save(TownyWars.wallConfigFile);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										//e.printStackTrace();
									}
					    			
					    			event.setCancelled(false);
					    			return;
					    		}
					    	}
					    	if (!found)
					    	{
					    		player.sendMessage(ChatColor.RED + "You are not allowed to place this here, sorry!");
					    		event.setCancelled(true);
					    		return;
					    	}
					    }
					} catch (NotRegisteredException e) {
					    player.sendMessage(ChatColor.RED + "You must be in a town to place this!");
					    event.setCancelled(true);
					    return;
					}
				}
			}
		}
		if(TownyWars.allowGriefing){
			Block block = event.getBlock();
			if(event.getPlayer()!=null){		
				Player p = event.getPlayer();
				Entity entity = (Entity) p;
				Resident res = null;
				try {
					res = TownyUniverse.getDataSource().getResident(p.getName());
				} catch (NotRegisteredException e2) {
					// TODO Auto-generated catch block
					//eckTrace();
				}
				if(TownyWars.atWar(p, block.getLocation())){
					if(TownyWars.allowRollback){
						if(TownyUniverse.getTownBlock(block.getLocation())!=null){
							TownBlock townBlock = TownyUniverse.getTownBlock(block.getLocation());
							Town otherTown = null;
							Nation otherNation = null;
							Set<SBlock> sBlocks;
							try {
								otherTown = townBlock.getTown();
								otherNation = otherTown.getNation();
							} catch (NotRegisteredException e) {
								//e.printStackTrace();
								p.sendMessage("An error has occurred. Please get an Admin to check the logs.");
							}
							if(GriefListener.sBlocks.get(otherTown)==null){
								sBlocks = new HashSet<SBlock>();
							}else{
								sBlocks = GriefListener.sBlocks.get(otherTown);
							}
							SBlock sb;
							if(entity!=null){
								sb = new SBlock(block, entity);
							}else{
								sb = new SBlock(block);
							}
							sb.mat = "AIR";
							if(!containsBlock(sBlocks, sb)){			
								sBlocks.add(sb);
							}							
							GriefListener.sBlocks.put(otherTown, sBlocks);
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
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void onPearlThrown(ProjectileLaunchEvent ev) {
		if (ev.getEntityType() == EntityType.ENDER_PEARL)
		{
			EnderPearl e = (EnderPearl) ev.getEntity();
			if (e.getShooter() instanceof Player)
			{
				String name = ((Player) e.getShooter()).getName();
				TownyWars.enderpearls.put(name , ev.getEntity());
				
				Bukkit.getScheduler().scheduleAsyncDelayedTask(TownyWars.getInstance(), new Runnable() {
			        @Override
			        public void run() {
			        	if (TownyWars.enderpearls.containsKey(name))
			        	{
			        		TownyWars.enderpearls.remove(name);
			        	}
			        }
			    }, 60 * 20);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void ignoreProtections(EntityExplodeEvent ev) {
		List<Block> removeList = new ArrayList<Block>();
		for (Block b : ev.blockList())
		{
			Chunk c = b.getChunk();
			Location l = b.getLocation();
			if (TownyWars.wallBlocks.containsKey(c))
			{
				if (TownyWars.wallBlocks.get(c).contains(l))
				{
					if (TownyWars.wallHealth.containsKey(b))
					{
						if (TownyWars.wallHealth.get(b) > 0)
						{
							removeList.add(b);
						}
						if (TownyWars.wallHealth.get(b) <= 0)
						{
							Chunk chunk = b.getLocation().getChunk();
			    			Location loc = b.getLocation();
			    			List<String> list = TownyWars.wallConfig.getStringList(chunk.getX() + "," + chunk.getZ() + "," + loc.getWorld().getName());
			    			list.remove(loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
			    			TownyWars.wallConfig.set(chunk.getX() + "," + chunk.getZ() + "," + loc.getWorld().getName(), list);
			    			try {
								TownyWars.wallConfig.save(TownyWars.wallConfigFile);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			    			if (TownyWars.wallBlocks.containsKey(chunk))
			    			{
			    				List<Location> locList = TownyWars.wallBlocks.get(chunk);
			    				locList.remove(b.getLocation());
			    				TownyWars.wallBlocks.replace(chunk, locList);
			    			}
						}
					}
					if (!TownyWars.wallHealth.containsKey(b))
					{
						removeList.add(b);
					}
					if (TownyWars.wallHealth.containsKey(b))
					{
						TownyWars.wallHealth.replace(b, TownyWars.wallHealth.get(b) - 1);
					}
					else
					{
						TownyWars.wallHealth.put(b, TownyWars.getInstance().getConfig().getInt("townyWallHealth"));
					}
				}
			}
		}
		for (Block b : removeList)
		{
			ev.blockList().remove(b);
		}
		removeList.clear();
		Location center = ev.getLocation();
		TownBlock townBlock = null;
		townBlock = TownyUniverse.getTownBlock(center);
		if(townBlock!=null){
			if(TownyWars.allowGriefing){
				if(TownyWars.warExplosions){
					if(townBlock.hasTown()){
						try {
							if(townBlock.getTown().hasNation()){
								Nation nation = townBlock.getTown().getNation();
								if(WarManager.getWarForNation(nation)!=null){
									ev.setCancelled(true);
								}
							}
						} catch (NotRegisteredException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
					}
							
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onPlayerMoveWall(PlayerMoveEvent ev) {
		Location to = ev.getTo();
		Chunk c = to.getChunk();
		TownBlock tb = TownyUniverse.getTownBlock(to);
		try {
			if (tb != null)
			{
				if (tb.getTown() != null)
				{
					Town town = tb.getTown();
					 Resident resident = null;
				        try {
				            resident = TownyUniverse.getDataSource().getResident(ev.getPlayer().getName());
				            if (resident != null)
				            {
				            	if (resident.hasTown())
				            	{
					            	if (resident.getTown() == town)
					            	{
					            		return;
					            	}
				            	}
				            }
				        } catch (NotRegisteredException e) {
				            e.printStackTrace();
				        }
				}
			}
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		if (TownyWars.wallBlocks.containsKey(c))
		{
			List<Location> locs = TownyWars.wallBlocks.get(c);
			for (Location l : locs)
			{
				if (to.getBlockX() == l.getBlockX() && to.getBlockZ() == l.getBlockZ())
				{
					ev.getPlayer().sendMessage(ChatColor.RED + "You cannot cross this wall!");
					ev.setCancelled(true);
				}
			}
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
					townBlock = TownyUniverse.getTownBlock(center);
					if(townBlock!=null){
						if(townBlock.hasTown()){
							if(townBlock.getTown().hasNation()){
								Nation nation = townBlock.getTown().getNation();
								if(WarManager.getWarForNation(nation)!=null){
									Set<SBlock> sBlocks = new HashSet<SBlock>();
									if(blocks!=null){
										for(Block block : blocks){
											if(block!=null){
												if(TownyWars.worldBlackList == null || TownyWars.worldBlackList.isEmpty() || !TownyWars.worldBlackList.contains(block.getWorld().getName().toString().toLowerCase())){
													if(TownyWars.blockBlackList == null || TownyWars.blockBlackList.isEmpty() || !TownyWars.blockBlackList.contains(block.getType())){
														sBlocks = getAttachedBlocks(block, sBlocks, null);
														if(!block.getType().equals(Material.TNT)){
															sBlocks.add(new SBlock(block));
														}
													}
												}
											}
										}
										if(TownyWars.allowRollback){
											WeakReference<Set<SBlock>> temp = new WeakReference<Set<SBlock>>(GriefListener.sBlocks.get(townBlock.getTown())); 
											Set<SBlock> j = new HashSet<SBlock>();
											if(temp.get()==null || (temp.get().isEmpty())){
												temp = new WeakReference<Set<SBlock>>(j);
											}				
											for(SBlock s : sBlocks){
												temp.get().add(s);
											}
											GriefListener.sBlocks.put(townBlock.getTown(), temp.get());
										}
										War wwar = WarManager.getWarForNation(nation);
										double points = (Math.round(((double)(sBlocks.size() * TownyWars.pBlockPoints))*1e2)/1e2);
										wwar.chargeTownPoints(nation, townBlock.getTown(), points);
										new AttackWarnBarTask(townBlock.getTown(), mplugin).runTask(mplugin);
										ev.setCancelled(false);
									}
									if(TownyWars.realisticExplosions){
										//p.sendMessage("Doing Realistic Explosion");
										Explode.explode(ev.getEntity(), blocks, center, DEBRIS_CHANCE);
									}
									return;
								}
							}else{
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
					if(TownyUniverse.isWilderness(center.getBlock()) && TownySettings.isExplosions() && TownyWars.realisticExplosions){
						if(blocks!=null){
							Explode.explode(ev.getEntity(), blocks, center, 75);
						}
						ev.setCancelled(false);
						return;
					}
				}
				if(TownyUniverse.isWilderness(center.getBlock()) && TownySettings.isExplosions() && TownyWars.realisticExplosions){
					if(blocks!=null){
						Explode.explode(ev.getEntity(), blocks, center, 75);
					}
					ev.setCancelled(false);
					return;
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
	
	public boolean containsBlock(Set<SBlock> sBlocks, SBlock sb){
		if(sb!=null){
			if(sBlocks!=null){
				for(SBlock s : sBlocks){
					if(sb.getLocation()==s.getLocation()){
						return true;
					}
				}
			}
		}	
		return false;
	}
	
	public Set<SBlock> getAttachedBlocks(Block block, Set<SBlock> sBlocks, Entity entity){
		SBlock check;
		for(BlockFace face : BlockFace.values()){
			if(!face.equals(BlockFace.SELF)){
				if((block.getRelative(face)).getState().getData() instanceof Attachable){
					Block b = (block.getRelative(face));
					Attachable att = (Attachable) (block.getRelative(face)).getState().getData();
					if(b.getRelative(att.getAttachedFace()).equals(block)){
						check = new SBlock(block.getRelative(face));
						if(!containsBlock(sBlocks, check)){			
							if(entity!=null){
								sBlocks.add(new SBlock((block.getRelative(face)), entity));
							}else{
								sBlocks.add(check);
							}
						}		
					}
				}
				if(block.getRelative(face).getState().getData() instanceof Vine){
					Vine vine = (Vine) block.getRelative(face).getState().getData();
					if(vine.isOnFace(face)){
						check = new SBlock(block.getRelative(face));
						if(!containsBlock(sBlocks, check)){
							if(entity!=null){
								sBlocks.add(new SBlock((block.getRelative(face)), entity));
							}else{
								sBlocks.add(check);
							}
						}		
					}
				}
				if((block.getRelative(face)).getType().equals(Material.CHORUS_PLANT)){
					check = new SBlock(block.getRelative(face));
					if(!containsBlock(sBlocks, check)){
						if(entity!=null){
							sBlocks.add(new SBlock((block.getRelative(face)), entity));
						}else{
							sBlocks.add(check);
						}
					}	
				}
				if((block.getRelative(face)).getType().equals(Material.CHORUS_FLOWER)){
					check = new SBlock(block.getRelative(face));
					if(!containsBlock(sBlocks, check)){
						if(entity!=null){
							sBlocks.add(new SBlock((block.getRelative(face)), entity));
						}else{
							sBlocks.add(check);
						}
					}
				}
			}
		}
		if(Utils.isOtherAttachable((block.getRelative(BlockFace.UP)).getType())){
			check = new SBlock(block.getRelative(BlockFace.UP));
			if(!containsBlock(sBlocks, check)){
				if(entity!=null){
					sBlocks.add(new SBlock((block.getRelative(BlockFace.UP)), entity));
				}else{
					sBlocks.add(check);
				}
			}	
		}
		if((block.getRelative(BlockFace.UP)).getType().equals(Material.CACTUS) || (block.getRelative(BlockFace.UP)).getType().equals(Material.SUGAR_CANE_BLOCK) || (block.getRelative(BlockFace.UP)).getType().equals(Material.CHORUS_PLANT) || (block.getRelative(BlockFace.UP)).getType().equals(Material.CHORUS_FLOWER)){
			Block up = block.getRelative(BlockFace.UP);
			do
			{
				if(up.getType().equals(Material.CACTUS) || up.getType().equals(Material.SUGAR_CANE_BLOCK) || up.getType().equals(Material.CHORUS_PLANT) || up.getType().equals(Material.CHORUS_FLOWER)){
					check = new SBlock(up);
					if(!containsBlock(sBlocks, check)){
						if(entity!=null){
							if(!containsBlock(sBlocks, new SBlock(up, entity))){			
								sBlocks.add(new SBlock(up, entity));
							}
						}else{
							if(!containsBlock(sBlocks, new SBlock(up))){			
								sBlocks.add(check);
							}
						}
					}
				}
				up = ((up.getLocation()).add(0,1,0)).getBlock();
			}while(up.getType().equals(Material.CACTUS) || up.getType().equals(Material.SUGAR_CANE_BLOCK) || up.getType().equals(Material.CHORUS_PLANT) || up.getType().equals(Material.CHORUS_FLOWER));
		}
		return sBlocks;
	}
	
	public static ConcurrentHashMap<Town, Set<SBlock>> getGriefedBlocks(){
		return GriefListener.sBlocks;
	}
	
	public static void setGriefedBlocks(ConcurrentHashMap<Town, Set<SBlock>> sBlocks){
		GriefListener.sBlocks = sBlocks;
	}
	
	public static void removeTownGriefedBlocks(Town town){
		ConcurrentHashMap<Town, Set<SBlock>> blocks = getGriefedBlocks();
		blocks.remove(town);
		setGriefedBlocks(blocks);
	}
}