package com.danielrharris.townywars.listeners;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.Vector;

import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.WarManager;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class Explode {
	
	@SuppressWarnings("deprecation")
	public static void explode(Entity ent, List<Block> blocks, Location center, int DEBRIS_CHANCE){
		
		float yield = 3.0f;
		
		if(ent instanceof Explosive) {
			Explosive e = (Explosive) ent;
			yield = e.getYield();
		} else if (ent instanceof Creeper) {
			Creeper c = (Creeper) ent;
			if(c.isPowered()) {
				yield = 6.0f;
			} else {
				yield = 3.0f;
			}
		}
		Iterator<Block> itr = blocks.iterator();
		while(itr.hasNext()) { //Iterates through blocks
			Block b = itr.next();
			TownBlock townBlock = null;
			townBlock = TownyUniverse.getTownBlock(b.getLocation());
			if(!(b.getState() instanceof InventoryHolder)){
				b.getDrops().clear();
			}
			if((Math.random() * 100.0F) <= DEBRIS_CHANCE) { //Randomly chooses the blocks based on chance
				Location loc = b.getLocation();
				//Spawn the "debris"
				FallingBlock debris = loc.getWorld().spawnFallingBlock(loc, b.getType(), b.getData()); 
				Location d = debris.getLocation();
				Location relative = new Location(loc.getWorld(), d.getX() - center.getX(),
						d.getY() - center.getY(), d.getZ() - center.getZ());
				
				//Set the vector
				Vector vec = new Vector(yield / relative.getX(), 
						yield / relative.getY(), 
						yield / relative.getZ());			
				vec.normalize();
				vec.multiply(d.distance(center) / 2.5F);
				debris.setVelocity(vec);			
			}
			if(townBlock!=null){
				if(TownyWars.allowGriefing){
					if(TownyWars.warExplosions){
						if(townBlock.hasTown()){
							try {
								if(townBlock.getTown().hasNation()){
									Nation nation = townBlock.getTown().getNation();
									if(WarManager.getWarForNation(nation)!=null){
										if(!(b.getState() instanceof InventoryHolder)){
											b.setType(Material.AIR);
										}
										
									}
								}
							} catch (NotRegisteredException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
								
					}
				}
			}			
		}
	}
}