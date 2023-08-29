package com.danielrharris.townywars.storage;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.danielrharris.townywars.warObjects.Rebellion;
import com.danielrharris.townywars.warObjects.War;

public class YMLFile {
	
    private File warFile;  
    private File dataFolder;
    private FileConfiguration war;
    private Set<War> activeWars = new HashSet<War>();
    private Set<Rebellion> activeRebellions;
  
    private Plugin plugin;
  
    public YMLFile(Plugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(this.plugin.getDataFolder().toString() + "/data");
    }
  
    public void initLists() {
        saveDefaultFiles();
        loadWars();
        //Bukkit.getConsoleSender().sendMessage(GemsCommandExecutor.getFormattedMessage(Bukkit.getConsoleSender(), (MinevoltGems.getConfigInstance()).pr + " &aFile storage successfully initialized!"));
    }
  
    public void saveDefaultFiles() {
        if (!this.dataFolder.exists())
            this.dataFolder.mkdir(); 
        if (this.warFile == null)
           this.warFile = new File(this.dataFolder, "activeWars.yml"); 
        if (!this.warFile.exists())
           this.plugin.saveResource("data/activeWars.yml", false); 
    }
  
    public void loadWars() {
        this.activeWars = new HashSet<War>();
        this.war = (FileConfiguration)YamlConfiguration.loadConfiguration(this.warFile);
        if (this.war.getKeys(false) != null)
            for (String s : this.war.getKeys(false)) {	  
		        try {
			        War w = War.decodeWar(this.war.getString(s));
			        this.activeWars.add(w);
		        } catch (ClassNotFoundException | IOException e) {
		        	e.printStackTrace();
		        }
            }
    }
  
    public void saveWars() {
        if (this.activeWars != null && !this.activeWars.isEmpty())
            for (War war : activeWars) {
            	try {
					this.war.set(war.getWarUUID().toString(), war.encodeWarToString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
            } 
        if (this.warFile.exists())
            this.warFile.delete(); 
        try {
            this.war.save(this.warFile);
        } catch (IOException e) {
            e.printStackTrace();
        } 
        try {
            this.warFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
  
    public Set<War> getActiveWars(){
	    return activeWars;
    }
    
    public void setActiveWars(Set<War> wars){
    	this.activeWars = wars;
    }
}
