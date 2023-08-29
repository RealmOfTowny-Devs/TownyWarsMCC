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
    private File rebellionFile;
    private FileConfiguration rebel;
  
    private Plugin plugin;
  
    public YMLFile(Plugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(this.plugin.getDataFolder().toString() + "/data");
        saveDefaultFiles();
        //Put message here saying file storage has been set up
    }
  
    public void saveDefaultFiles() {
        if (!this.dataFolder.exists())
            this.dataFolder.mkdir(); 
        if (this.warFile == null)
           this.warFile = new File(this.dataFolder, "activeWars.yml"); 
        if (!this.warFile.exists())
           this.plugin.saveResource("data/activeWars.yml", false); 
        if (this.rebellionFile == null)
            this.rebellionFile = new File(this.dataFolder, "activeRebellions.yml"); 
         if (!this.rebellionFile.exists())
            this.plugin.saveResource("data/activeRebellions.yml", false);
    }
  
    public Set<War> loadWars() {
    	Set<War> activeWars = new HashSet<War>();
        this.war = (FileConfiguration)YamlConfiguration.loadConfiguration(this.warFile);
        if (this.war.getKeys(false) != null && !this.war.getKeys(false).isEmpty())
            for (String s : this.war.getKeys(false)) {	  
		        try {
			        War w = War.decodeWar(this.war.getString(s));
			        activeWars.add(w);
		        } catch (ClassNotFoundException | IOException e) {
		        	e.printStackTrace();
		        }
            }
        return activeWars;
    }
  
    public void saveWars(Set<War> activeWars) {
        if (activeWars != null && !activeWars.isEmpty())
            for (War war : activeWars) {
            	try {
					this.war.set(war.getUuid().toString(), war.encodeWar());
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
    
    public Set<Rebellion> loadRebellions() {
    	Set<Rebellion> activeRebellions = new HashSet<Rebellion>();
        this.rebel = (FileConfiguration)YamlConfiguration.loadConfiguration(this.warFile);
        if (this.rebel.getKeys(false) != null && !this.rebel.getKeys(false).isEmpty())
            for (String s : this.rebel.getKeys(false)) {	  
		        try {
			        Rebellion r = Rebellion.decodeRebellion(this.rebel.getString(s));
			        activeRebellions.add(r);
		        } catch (ClassNotFoundException | IOException e) {
		        	e.printStackTrace();
		        }
            }
        return activeRebellions;
    }
    
    public void saveRebellions(Set<Rebellion> activeRebellions) {
        if (activeRebellions != null && !activeRebellions.isEmpty())
            for (Rebellion r : activeRebellions) {
            	try {
					this.rebel.set(r.getUuid().toString(), r.encodeRebellion());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
            } 
        if (this.rebellionFile.exists())
            this.rebellionFile.delete(); 
        try {
            this.rebel.save(this.rebellionFile);
        } catch (IOException e) {
            e.printStackTrace();
        } 
        try {
            this.rebellionFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
}
