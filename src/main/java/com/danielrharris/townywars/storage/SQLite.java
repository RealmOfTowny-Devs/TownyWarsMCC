package com.danielrharris.townywars.storage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.warObjects.Rebellion;
import com.danielrharris.townywars.warObjects.War;
import com.zp4rker.localdb.Column;
import com.zp4rker.localdb.DataType;
import com.zp4rker.localdb.Database;
import com.zp4rker.localdb.Table;

public class SQLite {
	
	private TownyWars plugin;
	private Database database;
	private Set<War> activeWars;
    private Set<Rebellion> activeRebellions;
	
	public SQLite(TownyWars plugin) {
		this.plugin = plugin;
		initialize();
	}
	
	public void initialize() {
		Column uuid = new Column("uuid", DataType.STRING, 64);
		Column base64 = new Column("base64", DataType.STRING, 64);
		// Create the table
		Table warsTable = new Table("wars", Arrays.asList(uuid, base64));
		// Create the database
		database = new Database(this.plugin, "wars", warsTable, "TownyWars/data");
		Column rebeluuid = new Column("uuid", DataType.STRING, 64);
		Column rebelbase64 = new Column("base64", DataType.STRING, 64);
		// Create the table
		Table rebellionsTable = new Table("rebellions", Arrays.asList(rebeluuid, rebelbase64));
		// Create the database
		database.addTable(rebellionsTable);
	}
	
	public void loadWars() {
		for(Table table : database.getTables()) {
			if(table.getName().equalsIgnoreCase("wars")) {
				for(Column column : table.getColumns()) {
					try {
						War w = War.decodeWar((String)column.getValue());
						activeWars.add(w);
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
    public void saveWars() {
		
	}
	
    public void loadRebellions() {
		
	}
	
    public void saveRebellions() {
		
	}
    
    public Set<War> getActiveWars() {
		return activeWars;
	}

	public void setActiveWars(Set<War> activeWars) {
		this.activeWars = activeWars;
	}

	public Set<Rebellion> getActiveRebellions() {
		return activeRebellions;
	}

	public void setActiveRebellions(Set<Rebellion> activeRebellions) {
		this.activeRebellions = activeRebellions;
	}
}