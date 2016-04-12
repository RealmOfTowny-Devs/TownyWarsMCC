package com.danielrharris.townywars;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.Attachable;
import org.bukkit.material.PistonExtensionMaterial;

import com.palmergames.bukkit.towny.object.Town;

import me.drkmatr1984.BlocksAPI.tasks.DelayedRegenTask;
import me.drkmatr1984.BlocksAPI.utils.BlockSerialization;
import me.drkmatr1984.BlocksAPI.utils.SBlock;
import me.drkmatr1984.BlocksAPI.utils.Utils;

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
	
	public Set<SBlock> loadData(Town town){
		Set<SBlock> blocksBroken = new HashSet<SBlock>();
		String listSerial = "";
		int size;
		townDataFolder = new File(plugin.getDataFolder().toString() + "/towndata");
		townData = new File(townDataFolder, (town.getName().toLowerCase() + ".yml"));  
		if (!townDataFolder.exists()) {
			Bukkit.getServer().getLogger().info("Directory Doesn't Exist, Creating...");
			townDataFolder.mkdir();
		}
		if (!townData.exists()) {
			return null;
		}else{
			blocks = YamlConfiguration.loadConfiguration(townData);
			if(blocks!=null){
				if(!(blocks.getString("blocks") == "") && (blocks.getString("blocks") != null)){
					listSerial = blocks.getString("blocks");
					size = blocks.getInt("size");
					try {
						if(!listSerial.equals("") && !listSerial.equals(null)){
							blocksBroken = BlockSerialization.fromBase64(listSerial, size);
						}			
					} catch (IOException e) {
						Bukkit.getServer().getLogger().info("Can't load BlocksFile");
						e.printStackTrace();
					}
				}		
			}
		}
		return blocksBroken;
	}
	
	public boolean saveData(Town town, Set<SBlock> blocksBroken){
		int size;
		townDataFolder = new File(plugin.getDataFolder().toString() + "/towndata");
		townData = new File(townDataFolder, (town.getName().toLowerCase() + ".yml"));
		if (!townDataFolder.exists()) {
			Bukkit.getServer().getLogger().info("Directory Doesn't Exist, Creating...");
			townDataFolder.mkdir();
		}
		if (!townData.exists()) {
			try {
				townData.createNewFile();
			} catch (IOException e) {
				plugin.getServer().getLogger().info("An Error has occured. Please see the stacktrace below.");
				e.printStackTrace();
				return false;
			}
		}else{
			townData.delete();
			try {
				townData.createNewFile();
			} catch (IOException e) {
				plugin.getServer().getLogger().info("An Error has occured. Please see the stacktrace below.");
				e.printStackTrace();
				return false;
			}
		}
		blocks = YamlConfiguration.loadConfiguration(townData);
		if(blocksBroken.isEmpty()){
			size = 0;
		}else{
			size = blocksBroken.size();
		}
		if(blocksBroken!=null){
			String listSerial = BlockSerialization.toBase64(blocksBroken);
			blocks.set("blocks", listSerial);
			blocks.set("size", size);
		}
		try {
			blocks.save(townData);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@SuppressWarnings({ "deprecation" })
	public void rollbackBlocks(Town town){
		Set<SBlock> sBlocks = loadData(town);
		townDataFolder = new File(plugin.getDataFolder().toString() + "/towndata");
		townData = new File(townDataFolder, (town.getName().toLowerCase() + ".yml"));
		int delay = 1;
    	for(SBlock sb : sBlocks){
    		Location l = new Location(Bukkit.getServer().getWorld(sb.world),sb.x,sb.y,sb.z);
    		Block bl = l.getBlock();
    		Material mat = Material.valueOf(sb.mat);
    		bl.setTypeIdAndData(mat.getId(), sb.data, true);
    		BlockState blockState = bl.getState();
    		if(Utils.isOtherAttachable(mat) || mat.equals(Material.CACTUS) || mat.equals(Material.SUGAR_CANE_BLOCK) || blockState.getData() instanceof PistonExtensionMaterial || blockState instanceof Attachable){
    			new DelayedRegenTask(sb).runTaskLaterAsynchronously(plugin, delay+20);
    		}else{
    			new DelayedRegenTask(sb).runTaskLaterAsynchronously(plugin, delay);
    		}
    		delay++;
    	}
    	if(townData.exists()){
    		townData.delete();
    	}	
	}	
}
