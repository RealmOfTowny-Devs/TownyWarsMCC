package com.danielrharris.townywars.tasks;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.danielrharris.townywars.GriefManager;
import com.danielrharris.townywars.listeners.GriefListener;
import net.md_5.bungee.api.ChatColor;

public class SaveTask extends BukkitRunnable{
	
	private GriefManager manager;
	
	public SaveTask(GriefManager manager){
		this.manager = manager;
	}
	
	@Override
	public void run() {
		if(GriefListener.getGriefedBlocks()!=null){
			if(!GriefListener.getGriefedBlocks().isEmpty()){
				this.manager.saveData(GriefListener.getGriefedBlocks());
				Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7[&2TownyWars&7] &eSaving Data..."));
			}
		}
	}
}