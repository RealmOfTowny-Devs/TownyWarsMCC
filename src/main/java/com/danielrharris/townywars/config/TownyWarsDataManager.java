package com.danielrharris.townywars.config;

import java.util.Set;

import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.WarManager;
import com.danielrharris.townywars.storage.MySQL;
import com.danielrharris.townywars.storage.SQLite;
import com.danielrharris.townywars.storage.YMLFile;
import com.danielrharris.townywars.warObjects.Rebellion;
import com.danielrharris.townywars.warObjects.War;

public class TownyWarsDataManager {
	
	private YMLFile ymlfile;
	private MySQL mysql;
	private SQLite sqlite;
	
	public TownyWarsDataManager(TownyWars plugin) {
		switch (plugin.getConfigInstance().method) {
		case file:
      	    this.ymlfile = new YMLFile(plugin);
    	case mysql:
    		this.mysql = new MySQL(plugin);
    	case sqlite:
            this.sqlite = new SQLite(plugin);
    	default:
    		this.ymlfile = new YMLFile(plugin);
		}
	}
	
	public void save() throws Exception {
		saveWars();
		saveRebellions();
	}
	
	public void saveWars() throws Exception {
		switch (TownyWars.getInstance().getConfigInstance().method) {
    		case file:
    			this.ymlfile.saveWars(WarManager.getWars());
    		case mysql:
    		    this.mysql.saveWars(WarManager.getWars());
    		case sqlite:
    			this.sqlite.saveWars(WarManager.getWars());
    		default:
    			this.ymlfile.saveWars(WarManager.getWars());
		}
	}
	
	public void saveRebellions() throws Exception {
		switch (TownyWars.getInstance().getConfigInstance().method) {
    		case file:
    			this.ymlfile.saveRebellions(WarManager.getPlannedRebellions());
    		case mysql:
                this.mysql.saveRebellions(WarManager.getPlannedRebellions());
    		case sqlite:
    			this.sqlite.saveRebellions(WarManager.getPlannedRebellions());
    		default:
    			this.ymlfile.saveRebellions(WarManager.getPlannedRebellions());
		}
	}
  
	public Set<War> loadWars() throws Exception {
  		switch (TownyWars.getInstance().getConfigInstance().method) {
	    	case file:
	      	    return ymlfile.loadWars();
	    	case mysql:
	    		return mysql.loadWars();
	    	case sqlite:
	            return sqlite.loadWars();
	    	default:
	      	    return ymlfile.loadWars();
  		}	    
  	}
	
	public Set<Rebellion> loadRebellions() throws Exception {
		switch (TownyWars.getInstance().getConfigInstance().method) {
    	case file:
      	    return ymlfile.loadRebellions();
    	case mysql:
    		return mysql.loadRebellions();
    	case sqlite:
            return sqlite.loadRebellions();
    	default:
      	    return ymlfile.loadRebellions();
		}
	}
}