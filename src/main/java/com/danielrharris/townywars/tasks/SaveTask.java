package com.danielrharris.townywars.tasks;

import java.util.Set;

import org.bukkit.Bukkit;

import com.danielrharris.townywars.GriefManager;
import com.palmergames.bukkit.towny.object.Town;

import me.drkmatr1984.BlocksAPI.utils.SBlock;

public class SaveTask implements Runnable{
	
	private GriefManager manager;
	private Town town;
	private Set<SBlock> sBlocks;
	
	public SaveTask(GriefManager manager, Town town, Set<SBlock> sBlocks){
		this.manager = manager;
		this.town = town;
		this.sBlocks = sBlocks;
	}
	
	@Override
	public void run() {
		Set<SBlock> sblocks = manager.loadData(town);
		for(SBlock sb : sBlocks){
			sblocks.add(sb);
		}
		if(!manager.saveData(town, sblocks)){
			Bukkit.getServer().getLogger().info("An Error has occured. Please see the stacktrace below.");
		}
	}
}