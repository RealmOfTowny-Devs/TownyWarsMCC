package com.danielrharris.townywars.storage;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.danielrharris.townywars.warObjects.Rebellion;
import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;

public class YMLFile {
	
    private File dataFolder;
    private File warFile = null;  
    private FileConfiguration war;
    private File rebellionFile = null;
    private FileConfiguration rebel;
    private File peaceFile = null;
    private FileConfiguration peace;
    private File neutralFile = null;
    private FileConfiguration neutral;
  
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
        if (this.peaceFile == null)
            this.peaceFile = new File(this.dataFolder, "peaceRequested.yml"); 
        if (!this.peaceFile.exists())
            this.plugin.saveResource("data/peaceRequested.yml", false);
        if (this.neutralFile == null)
            this.neutralFile = new File(this.dataFolder, "neutral.yml"); 
        if (!this.neutralFile.exists())
            this.plugin.saveResource("data/neutral.yml", false);
    }
  
    public Set<War> loadWars() {
    	Set<War> activeWars = new HashSet<War>();
        this.war = (FileConfiguration)YamlConfiguration.loadConfiguration(this.warFile);
        if(this.war.getKeys(false) != null)
        	if(!this.war.getKeys(false).isEmpty())
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
        if (activeWars != null)
        	if(!activeWars.isEmpty())
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
        this.rebel = (FileConfiguration)YamlConfiguration.loadConfiguration(this.rebellionFile);
        if(this.rebel.getKeys(false) != null)
        	if(!this.rebel.getKeys(false).isEmpty())
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
        if(activeRebellions != null)
        	if(!activeRebellions.isEmpty())
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
    
    public Set<WarParticipant> loadPeace() {
    	Set<WarParticipant> peaceRequested = new HashSet<WarParticipant>();
        this.peace = (FileConfiguration)YamlConfiguration.loadConfiguration(this.peaceFile);
        if (this.peace.getKeys(false) != null)
        	if(!this.peace.getKeys(false).isEmpty())
	            for (String s : this.peace.getKeys(false)) {	  
			        try {
			        	WarParticipant w = WarParticipant.decodeFromString(this.peace.getString(s));
			        	peaceRequested.add(w);
			        } catch (ClassNotFoundException | IOException e) {
			        	e.printStackTrace();
			        }
	            }
        return peaceRequested;
    }
    
    public void savePeace(Set<WarParticipant> peaceRequested) {
        if (peaceRequested != null)
        	if(!peaceRequested.isEmpty())
	            for (WarParticipant w : peaceRequested) {
	            	try {
						this.peace.set(w.getUuid().toString(), w.encodeToString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
	            } 
        if (this.peaceFile.exists())
            this.peaceFile.delete(); 
        try {
            this.peace.save(this.peaceFile);
        } catch (IOException e) {
            e.printStackTrace();
        } 
        try {
            this.peaceFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
    
    public Set<UUID> loadNeutral() {
    	Set<UUID> neutral = new HashSet<UUID>();
        this.neutral = (FileConfiguration)YamlConfiguration.loadConfiguration(this.neutralFile);
        if (this.neutral.getKeys(false) != null)
        	if(!this.neutral.getKeys(false).isEmpty())
	            for (String s : this.neutral.getKeys(false)) {
					neutral.add(UUID.fromString(s));
	            }
        return neutral;
    }
    
    public void saveNeutral(Set<UUID> neutral) {
        if (neutral != null)
        	if(!neutral.isEmpty())
	            for (UUID id : neutral) {
	            	this.neutral.set(id.toString(), id.toString()); 
	            } 
        if (this.neutralFile.exists())
            this.neutralFile.delete(); 
        try {
            this.neutral.save(this.neutralFile);
        } catch (IOException e) {
            e.printStackTrace();
        } 
        try {
            this.neutralFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
    
}
