package com.danielrharris.townywars;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//import main.java.com.danielrharris.townywars.War.MutableInteger;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TownyWars
  extends JavaPlugin
{
  public static TownyUniverse tUniverse;
  public static double pPlayer;
  public static double pPlot;
  public static double pKill;
  public static double pBlock;
  public static double declareCost;
  public static double endCost;
  public static boolean allowGriefing;
  public static boolean allowRollback;
  public static ArrayList<String> worldBlackList;
  private ArrayList<String> blockStringBlackList;
  public static Set<Material> blockBlackList;
  
  public Map<String,TownyWarsResident> allTownyWarsResidents = new HashMap<String,TownyWarsResident>();
  
  //set up the date conversion spec and the character set for file writing
  private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd zzz HH:mm:ss");
  private static final Charset utf8 = StandardCharsets.UTF_8;
	
  private static final String deathsFile="deaths.txt";

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
  
  @Override
  public void onEnable()
  {
    try
    {
      WarManager.load(getDataFolder());
    }
    catch (Exception ex)
    {
      Logger.getLogger(TownyWars.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    PluginManager pm = getServer().getPluginManager();
    pm.registerEvents(new WarListener(this), this);
    getCommand("twar").setExecutor(new WarExecutor(this));
    tUniverse = ((Towny)Bukkit.getPluginManager().getPlugin("Towny")).getTownyUniverse();
    for(Town town : TownyUniverse.getDataSource().getTowns()){
    	town.setAdminEnabledPVP(false);
    	town.setAdminDisabledPVP(false);
    	town.setPVP(false);
    }
    for (War w : WarManager.getWars()) {
      for (Nation nation : w.getNationsInWar()) {
          for (Town t : nation.getTowns()) {
            t.setPVP(true);
          }
      }
    }
    
    TownyUniverse.getDataSource().saveTowns();
    
    this.saveDefaultConfig();
    
    pPlayer = getConfig().getDouble("pper-player");
    pPlot = getConfig().getDouble("pper-plot");
    declareCost = getConfig().getDouble("declare-cost");
    endCost = getConfig().getDouble("end-cost");
    pKill = getConfig().getDouble("death-cost");
    String allowGriefingS;
    allowGriefingS = getConfig().getString("griefing.allow-griefing");
    String allowRollbackS;
    allowRollbackS = getConfig().getString("griefing.allow-rollback");
    allowGriefing = Boolean.valueOf(allowGriefingS.toUpperCase());
    allowRollback = Boolean.valueOf(allowRollbackS.toUpperCase());
    pBlock = getConfig().getDouble("griefing.per-block-cost");
    for(String string : (ArrayList<String>) getConfig().getStringList("griefing.worldBlackList")){
    	worldBlackList.add(string.toLowerCase());
    }   
    this.blockStringBlackList = (ArrayList<String>) getConfig().getStringList("griefing.blockBlackList");
    blockBlackList = convertBanList(this.blockStringBlackList);
     
    try{
    	for (Resident re : tUniverse.getActiveResidents()){
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
  	
  	public Set<Material> convertBanList(List<String> banList2){
  		Set<Material> newBanList = new HashSet<Material>();
		if(!banList2.equals(null)){
			for(String s : banList2){
				Material mat = Material.valueOf(s.toUpperCase());
				if(!mat.equals(null)){
					newBanList.add(mat);
				}
			}
		}
		return newBanList;
	}
}
