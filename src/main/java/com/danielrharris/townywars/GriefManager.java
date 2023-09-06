package com.danielrharris.townywars;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;

//Make into a class that stores the blocks in data files per town among other uses
public class GriefManager
{
	private TownyWars plugin;
	private File townDataFolder;
	private File townData;
	private FileConfiguration blocks;
	
	public GriefManager(TownyWars plugin){
		this.plugin = plugin;
	}
	
}