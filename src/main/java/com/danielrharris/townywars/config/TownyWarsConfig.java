package com.danielrharris.townywars.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.danielrharris.townywars.TownyWars;

public class TownyWarsConfig {
  private TownyWars plugin = null;
  
  private File fileConfig = null;
  
  public String language = "en";
  
  public String pr = "&8[&bMine&evolt&aGems&8]&r";
  
  private File dataFolder;
  
  private FileConfiguration config;
  
  public double pPlayer;
  public double pPlot;
  public double pKill;
  public double pKillPoints;
  public double pMayorKill;
  public double pKingKill;
  public double pBlock;
  public double pBlockPoints;
  public double declareCost;
  public double endCost;
  public boolean allowGriefing;
  public boolean allowRollback;
  public int timer;
  public boolean warExplosions;
  public boolean realisticExplosions;
  public int debrisChance;
  public ArrayList<String> worldBlackList;
  private ArrayList<String> blockStringBlackList;
  public Set<Material> blockBlackList;
  public boolean isBossBar = false;
  
  public StorageMethod method;
  
  public String host = "127.0.0.1";
  
  public String port = "3306";
  
  public String database = "MinevoltGems";
  
  public String table = "minecraft";
  
  public String username = "root";
  
  public String password = "example";
  
  public boolean useSSL = false;
  
  public int interval = 5;
  
  public TownyWarsConfig(Plugin plugin) {
    this.plugin = (TownyWars)plugin;
    this.dataFolder = new File(this.plugin.getDataFolder().toString());
    if (!this.dataFolder.exists())
        this.dataFolder.mkdir(); 
      if (this.fileConfig == null)
        this.fileConfig = new File(this.dataFolder, "config.yml"); 
      if (!this.fileConfig.exists())
        this.plugin.saveResource("config.yml", false); 
    loadConfig();
  }
  
  public void loadConfig() {
	  this.config = YamlConfiguration.loadConfiguration(fileConfig);
	  this.language = this.config.getString("language");
	  pPlayer = this.config.getDouble("pper-player");
	  pPlot = this.config.getDouble("pper-plot");
	  declareCost = this.config.getDouble("declare-cost");
	  endCost = this.config.getDouble("end-cost");
	  pKill = this.config.getDouble("death-cost");
	  pKillPoints = this.config.getDouble("pper-player-kill");
	  pMayorKill = this.config.getDouble("pper-mayor-kill");
	  pKingKill = this.config.getDouble("pper-king-kill");
	  String allowGriefingS;
	  allowGriefingS = this.config.getString("griefing.allow-griefing");
	  String allowRollbackS;
	  allowRollbackS = this.config.getString("griefing.allow-rollback");
	  allowGriefing = Boolean.valueOf(allowGriefingS.toUpperCase());
	  allowRollback = Boolean.valueOf(allowRollbackS.toUpperCase());
	  if(allowRollback){
	      timer = ((this.config.getInt("griefing.save-timer"))*60)*20;
	  }   
	  pBlock = this.config.getDouble("griefing.per-block-cost");
	  pBlockPoints = this.config.getDouble("griefing.per-block-points");
	  String tempExplosions = this.config.getString("griefing.allow-explosions-war");
	  warExplosions = Boolean.valueOf(tempExplosions.toUpperCase());
	  String realExplosions = this.config.getString("griefing.explosions.realistic-explosions");
	  realisticExplosions = Boolean.valueOf(realExplosions.toUpperCase());
	  debrisChance = this.config.getInt("griefing.explosions.debris-chance");
	  for(String string : (ArrayList<String>) this.config.getStringList("griefing.worldBlackList")){
	      worldBlackList.add(string.toLowerCase());
	  }   
	  this.blockStringBlackList = (ArrayList<String>) this.config.getStringList("griefing.blockBlackList");
	      blockBlackList = convertBanList(this.blockStringBlackList);  
	  if(this.allowRollback){
	      //new SaveTask(this.gm).runTaskTimer(plugin, TownyWars.timer, TownyWars.timer);
	  }
	  this.method = StorageMethod.valueOf(this.config.getString("Storage.StorageMethod"));
	  if (this.method.equals(StorageMethod.mysql)) {
	      this.host = this.config.getString("Storage.mysql.host");
	      this.port = this.config.getString("Storage.mysql.port");
	      this.database = this.config.getString("Storage.mysql.database");
	      this.table = this.config.getString("Storage.mysql.table");
	      this.username = this.config.getString("Storage.mysql.username");
	      this.password = this.config.getString("Storage.mysql.password");
	      this.useSSL = Boolean.valueOf(this.config.getString("Storage.mysql.useSSL").toUpperCase()).booleanValue();
	  } else {
	      this.interval = this.config.getInt("Storage.file.save-interval");
	  }
  }
  
  public FileConfiguration getGemsConfig() {
    return this.config;
  }
  
  public void saveConfig() {
    this.plugin.saveConfig();
  }
  
  public void reloadConfig() {
    this.plugin.reloadConfig();
  }
  
  public enum StorageMethod {
    file, mysql, sqlite;
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