package com.danielrharris.townywars.storage;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.warObjects.War;

public class SQLite {
	
    private String DATABASE_URL;
	
	public SQLite(TownyWars plugin) {
		File dataFolder = new File(plugin.getDataFolder().toString() + "/data");
		File warData = new File(dataFolder, "warData.db");
		DATABASE_URL = "jdbc:sqlite:"+ warData.toString();
		if (!dataFolder.exists())
            dataFolder.mkdir();
        if (!warData.exists()){
            try {
                warData.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: " + warData.toString());
            }
        }
	}

    public Set<War> loadWars() {
    	Set<War> activeWars = new HashSet<War>();
    	createTable("wars");
    	if(getColumnKeys("wars")!=null && !getColumnKeys("wars").isEmpty()) {
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
    	for(War w : activeWars) {
    		try {
				storeValues("wars", w.encodeWar());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
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