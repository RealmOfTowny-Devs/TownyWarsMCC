package com.danielrharris.townywars.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import com.danielrharris.townywars.TownyWars;

public class SaveTask extends BukkitRunnable{
	
	///////////// This just needs reworked to Use Townies Snapshot system
	public SaveTask(){
	}
	
	@Override
	public void run() {
		try {
			TownyWars.getInstance().getDataManager().save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}