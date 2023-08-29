package com.danielrharris.townywars;

import com.danielrharris.townywars.config.TownyWarsConfig;
import com.danielrharris.townywars.listeners.*;
import com.danielrharris.townywars.tasks.SaveTask;
import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.*;

import me.drkmatr1984.MinevoltGems.GemsConfig;

import com.danielrharris.townywars.storage.MySQL;
import com.danielrharris.townywars.storage.SQLite;
import com.danielrharris.townywars.storage.YMLFile;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//import main.java.com.danielrharris.townywars.War.MutableInteger;

public class TownyWars extends JavaPlugin
{
    private TownyUniverse tUniverse;
    private Towny towny;
    private static TownyWars plugin;
    private TownyWarsConfig config;
    private GriefManager gm;
    private WarManager warManager;
    File wallConfigFile = new File(this.getDataFolder(), "walls.yml");
    public static HashMap<Chunk, List<Location>> wallBlocks = new HashMap<Chunk, List<Location>>();
    public Map<String,TownyWarsResident> allTownyWarsResidents = new HashMap<String,TownyWarsResident>();
    public static List<String> messagedPlayers = new ArrayList<String>();
    //set up the date conversion spec and the character set for file writing
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd zzz HH:mm:ss");
    private static final Charset utf8 = StandardCharsets.UTF_8;
	
    private static final String deathsFile="deaths.txt";

    @Override
    public void onEnable()
    {
	  	this.plugin = this;
        this.config = new TownyWarsConfig(this.plugin);
        this.warManager = new WarManager(this.plugin);
	  	try
	  	{
	  		warManager.load();
	  	}
        catch (Exception ex)
        {
        	Logger.getLogger(TownyWars.class.getName()).log(Level.SEVERE, null, ex);
        }
	  	PluginManager pm = getServer().getPluginManager();
	  	gm = new GriefManager(this);
	  	pm.registerEvents(new GriefListener(this, gm), this);
	  	pm.registerEvents(new WarListener(this), this);
	  	pm.registerEvents(new PvPListener(this), this);
	  	pm.registerEvents(new NationWalkEvent(),this);
	  	pm.registerEvents(new EnemyWalkWWar(),this);
	  	getCommand("twar").setExecutor(new WarExecutor(this));
	  	towny = ((Towny)Bukkit.getPluginManager().getPlugin("Towny"));
	  	tUniverse = TownyUniverse.getInstance();
	  	for(Town town : tUniverse.getTowns()){
	  		town.setAdminEnabledPVP(false);
	  		town.setAdminDisabledPVP(false);
	  		town.setPVP(false);
	  	}
	  	for (War w : warManager.getWars()) {
	  		for (WarParticipant p : w.getWarParticipants()) {
	  			for (Town t : p.getTownsList()) {
	  				t.setPVP(true);
	  			}
	  		}
	  	}
    
	  	tUniverse.getDataSource().saveTowns();
    
	  	if(!(wallConfigFile.exists())){
	  		try {
	  			wallConfigFile.createNewFile();
	  			YamlConfiguration wallConfig = YamlConfiguration.loadConfiguration(wallConfigFile);
	  		}catch (IOException e) {
	  				e.printStackTrace();
	  		}
	  	}  
    
	  	try{
	  		for (Resident re : tUniverse.getResidents()){
	  			if (allTownyWarsResidents.get(re.getName())==null){
	  				addTownyWarsResident(re.getName());
	  			}
	  		}
	  	}catch (Exception ex)
	  	{
	  		System.out.println("failed to add residents!");
	  		ex.printStackTrace();
	  	}   
    }
  
  @Override
  public void onDisable()
  {
	  
    try
    {
      WarManager.save();  
    }
    catch (Exception ex)
    {
      Logger.getLogger(TownyWars.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public void addTownyWarsResident(String playerName){
	  TownyWarsResident newPlayer = new TownyWarsResident(playerName);
	  allTownyWarsResidents.put(playerName,newPlayer);
  }
  
  public TownyWarsResident getTownyWarsResident(String playerName){
	  return allTownyWarsResidents.get(playerName);
  }
  

	
  // takes in information about the death that just happened and writes it to a file
  public int writeKillRecord(long deathTime, String playerName, String killerName, String damageCause, String deathMessage){
		
		// convert the time in milliseconds to a date and then convert it to a string in a useful format (have to tack on the milliseconds)
		// format example: 2014-08-29 EDT 10:05:25:756
		Date deathDate = new Date(deathTime);
	    String deathDateString = format.format(deathDate)+":"+deathTime%1000;
		
	    if (killerName==null) {
	    	killerName="nonplayer";
	    }
	    
	    // prepare the death record string that will be written to file
		List<String> deathRecord = Arrays.asList(deathDateString+": "+playerName+" died to "+killerName+" via "+damageCause+"; '"+deathMessage+"'");
		
		
		// append the death record to the specified file
		try {	
				Files.write(Paths.get(deathsFile), deathRecord, utf8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}
		// some kind of error occurred . . . .
		catch (IOException e) {
			e.printStackTrace();
			return 1;
		}
		// all good!
		return 0;
	}
  	
  	/*
	 * Takes a player and a location, the player is someone who wants to find out if the location
	 * interacted with is located in a town that their nation is at war With
	 */
	public static boolean atWar(Player p, Location loc){
		try
		{
			if(TownyUniverse.getDataSource().getResident(p.getName())!=null)
			{
				Resident re = TownyUniverse.getDataSource().getResident(p.getName());
				if(re.getTown()!=null){
					if(re.getTown().getNation()!=null){
						Nation nation = re.getTown().getNation();
						// add the player to the master list if they don't exist in it yet
						if (plugin.getTownyWarsResident(re.getName())==null){
							plugin.addTownyWarsResident(re.getName());
							System.out.println("resident added!");
						}
						War ww = WarManager.getWarForNation(nation);
						if (ww != null)
						{
							if(TownyUniverse.getTownBlock(loc)!=null){
								TownBlock townBlock = TownyUniverse.getTownBlock(loc);
								Town otherTown = townBlock.getTown();
								if(otherTown!=re.getTown()){
									if(otherTown.getNation()!=null){
										Nation otherNation = otherTown.getNation();
										if(otherNation!=nation){									
											Set<Nation> nationsInWar = ww.getNationsInWar();
											if(nationsInWar.contains(otherNation)){
												//nations are at war with each other
												return true;
											}
										}
									}
								}
							}
						}
					}
				}
			}				
		}catch (Exception ex) {
			return false;
		}
		return false;
	}
	
	public static TownyWars getInstance(){
		return plugin;
	}
	
	public TownyWarsConfig getTWConfig() {
	    return config;
	}
}