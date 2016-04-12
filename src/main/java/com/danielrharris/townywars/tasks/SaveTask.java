package com.danielrharris.townywars.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.danielrharris.townywars.GriefManager;
import com.palmergames.bukkit.towny.object.Town;

import me.drkmatr1984.BlocksAPI.utils.SBlock;
import me.drkmatr1984.BlocksAPI.utils.Utils;

public class SaveTask extends BukkitRunnable{
	
	private GriefManager manager;
	private Town town;
	private Set<SBlock> sBlocks;
	
	public SaveTask(GriefManager manager, Town town, Set<SBlock> sBlocks){
		this.manager = manager;
		this.town = town;
		this.sBlocks = sBlocks;
	}
	
	public SaveTask(GriefManager manager, Town town, SBlock sBlocks){
		this.manager = manager;
		this.town = town;
		this.sBlocks = new HashSet<SBlock>();
		this.sBlocks.add(sBlocks);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Set<SBlock> sblocks = new HashSet<SBlock>();
		ArrayList<SBlock> tempSet = new ArrayList<SBlock>();
		if(manager.loadData(town)!=null){
			sblocks = manager.loadData(town);
			tempSet = (ArrayList<SBlock>) Utils.setToList(sblocks);
		}
		for(SBlock sb : sBlocks){
			if(sblocks.isEmpty()){
				tempSet.add(sb);
			}else{
				for(SBlock b : sblocks){
					if(sb.getLocation()!=b.getLocation()){
						tempSet.add(sb);
					}
				}
			}				
		}
		if(!manager.saveData(town, (Set<SBlock>) Utils.listToSet(tempSet))){
			Bukkit.getServer().getLogger().info("An Error has occured. Please see the stacktrace below.");
		}
	}
}