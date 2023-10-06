package com.danielrharris.townywars.storage;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.warObjects.Rebellion;
import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;

public class SQLite {
	
    private String DATABASE_URL;
	
	public SQLite(TownyWars plugin) {
		File dataFolder = new File(plugin.getDataFolder().toString() + "/data");
		File warData = new File(dataFolder, "warData.db");
		DATABASE_URL = "jdbc:sqlite:"+ warData.toString();
		if (!dataFolder.exists())
            dataFolder.mkdir();
        if (!warData.exists()){
        	plugin.saveResource("data/warData.db", false);
        }
	}

    public Set<War> loadWars() {
    	Set<War> activeWars = new HashSet<War>();
    	createTable("wars");
    	if(getColumnKeys("wars")!=null)
    		if(!getColumnKeys("wars").isEmpty()) {
	    		for(int key : getColumnKeys("wars")) {    		
	        		try {
	            		String base64 = retrieveBase64("wars", key);
	    				activeWars.add(War.decodeWar(base64));
	    			} catch (ClassNotFoundException | IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	        	}
    	} 	
    	return activeWars;
    }
    
    public void saveWars(Set<War> activeWars) {
    	createTable("wars");
    	if(activeWars!=null)
    		if(!activeWars.isEmpty())
		    	for(War w : activeWars) {
		    		try {
						storeValues("wars", w.encodeWar());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
    }
    
    
    public void saveRebellions(Set<Rebellion> activeRebellions) {
    	createTable("rebellions");
    	if(activeRebellions!=null)
    		if(!activeRebellions.isEmpty())
		    	for(Rebellion r : activeRebellions) {
		    		try {
						storeValues("rebellions", r.encodeRebellion());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
    }
    
    public Set<Rebellion> loadRebellions() {
    	Set<Rebellion> activeRebellions = new HashSet<Rebellion>();
    	createTable("rebellions");
    	if(getColumnKeys("rebellions")!=null)
    		if(!getColumnKeys("rebellions").isEmpty()) {
	    		for(int key : getColumnKeys("rebellions")) {    		
	        		try {
	            		String base64 = retrieveBase64("rebellions", key);
	            		activeRebellions.add(Rebellion.decodeRebellion(base64));
	    			} catch (ClassNotFoundException | IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	        	}
    	} 	
    	return activeRebellions;
    }
    
    public void savePeace(Set<WarParticipant> peaceRequested) {
    	createTable("peace");
    	if(peaceRequested!=null)
    		if(!peaceRequested.isEmpty())
		    	for(WarParticipant w : peaceRequested) {
		    		try {
						storeValues("peace", w.encodeToString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
    }
    
    public Set<WarParticipant> loadPeace() {
    	Set<WarParticipant> peaceRequested = new HashSet<WarParticipant>();
    	createTable("peace");
    	if(getColumnKeys("peace")!=null)
    		if(!getColumnKeys("peace").isEmpty())
    			for(int key : getColumnKeys("peace")) {    		
    				try {
    					String base64 = retrieveBase64("peace", key);
    					peaceRequested.add(WarParticipant.decodeFromString(base64));
    				} catch (ClassNotFoundException | IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}  		 	
    	return peaceRequested;
    }
    
    public void saveNeutral(Set<UUID> neutral) {
    	createTable("neutral");
    	if(neutral!=null)
    		if(!neutral.isEmpty())
		    	for(UUID id : neutral) {
		    		storeValues("neutral", id.toString());
		    	}
    }
    
    public Set<UUID> loadNeutral() {
    	Set<UUID> neutral = new HashSet<UUID>();
    	createTable("neutral");
    	if(getColumnKeys("neutral")!=null)
    		if(!getColumnKeys("neutral").isEmpty())
    			for(int key : getColumnKeys("neutral")) {    		
    				String uuid = retrieveUUID("neutral", key);
					neutral.add(UUID.fromString(uuid));
    			}  		 	
    	return neutral;
    }
    
    public void createTable(String table) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS " + table + " (id INTEGER AUTO_INCREMENT PRIMARY KEY, base64 TEXT)"
             )) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	public void storeValues(String table, String base64) {
		try (Connection connection = DriverManager.getConnection(DATABASE_URL);
			PreparedStatement statement = connection.prepareStatement(
					"INSERT INTO " + table + " (base64) VALUES (?)"
					)) {
	            statement.setString(1, base64);
	            statement.executeUpdate();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	public String retrieveBase64(String table, int id) {
		try (Connection connection = DriverManager.getConnection(DATABASE_URL);
				PreparedStatement statement = connection.prepareStatement(
						"SELECT base64 FROM " + table + " WHERE id = ?"
						)) {
			statement.setInt(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getString("base64");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String retrieveUUID(String table, int id) {
		try (Connection connection = DriverManager.getConnection(DATABASE_URL);
				PreparedStatement statement = connection.prepareStatement(
						"SELECT uuid FROM " + table + " WHERE id = ?"
						)) {
			statement.setInt(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getString("uuid");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	    
	public Set<Integer> getColumnKeys(String table) {
		Set<Integer> keys = new HashSet<Integer>();
		try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
			DatabaseMetaData metaData = connection.getMetaData();
			try (ResultSet columns = metaData.getColumns(null, null, table, null)) {
				while (columns.next()) {
					keys.add(columns.getInt("COLUMN_NAME"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keys;
	}
}