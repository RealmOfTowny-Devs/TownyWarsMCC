package com.danielrharris.townywars.storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;

import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.warObjects.Rebellion;
import com.danielrharris.townywars.warObjects.War;

public class MySQL {
	public static Connection con;
  
	private String host = "127.0.0.1";
	private String port = "3306";
	private String database = "MinevoltGems";
	private String username = "root";
	private String password = "example";
	private Boolean useSSL = Boolean.valueOf(false);
  
  	public MySQL(TownyWars plugin) {
  		this.host = plugin.getConfigInstance().host;
  		this.port = plugin.getConfigInstance().port;
  		this.database = plugin.getConfigInstance().database;
  		this.username = plugin.getConfigInstance().username;
  		this.password = plugin.getConfigInstance().password;
  		this.useSSL = Boolean.valueOf(plugin.getConfigInstance().useSSL);
  		connect();
  		//Start "keepAlive" task to keep connection active
  		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> keepAlive(), 20*60*60*7, 20*60*60*7);
  	}
  	
  	public Set<War> loadWars() {
  		Set<War> activeWars = new HashSet<War>();
  		try {
			for(String key : getTableColumnNames("wars")) {
				try {
				      PreparedStatement ps = getStatement("SELECT * FROM wars" + " WHERE UUID= ?");
				      ps.setString(1, key);
				      ResultSet rs = ps.executeQuery();
				      rs.next();
				      String base64 = rs.getString("base64");
				      rs.close();
				      ps.close();
				      activeWars.add(War.decodeWar(base64));
				  } catch (Exception ex) {
				      Bukkit.getServer().getConsoleSender().sendMessage("Couldn't load " + key);
				  }
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		return activeWars;
  	}
  	
  	public void saveWars(Set<War> activeWars) {
  		if(activeWars!=null && !activeWars.isEmpty()) {
  			for(War w : activeWars) {
  				try {
  					createTable("wars");
  			        PreparedStatement ps = getStatement("REPLACE INTO wars" + " SET base64= ? WHERE UUID= ?");
  			        ps.setString(1, w.encodeWar());
  			        ps.setString(2, w.getUuid().toString());
  			        ps.executeUpdate();
  			        ps.close();
  			      } catch (Exception ex) {
  			    	  //put error message here
  			      }
  	  		}
  		}
  	}
  	
  	public Set<Rebellion> loadRebellions() {
  		Set<Rebellion> activeRebellions = new HashSet<Rebellion>();
  		try {
			for(String key : getTableColumnNames("rebellions")) {
				try {
				      PreparedStatement ps = getStatement("SELECT * FROM rebellions" + " WHERE UUID= ?");
				      ps.setString(1, key);
				      ResultSet rs = ps.executeQuery();
				      rs.next();
				      String base64 = rs.getString("base64");
				      rs.close();
				      ps.close();
				      activeRebellions.add(Rebellion.decodeRebellion(base64));
				  } catch (Exception ex) {
				      Bukkit.getServer().getConsoleSender().sendMessage("Couldn't load " + key);
				  }
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		return activeRebellions;
  	}
  	
  	public void saveRebellions(Set<Rebellion> activeRebellions) {	
  		if(activeRebellions!=null && !activeRebellions.isEmpty()) {
  			for(Rebellion r : activeRebellions) {
  				try {
  					createTable("rebellions");
  			        PreparedStatement ps = getStatement("REPLACE INTO rebellions" + " SET base64= ? WHERE UUID= ?");
  			        ps.setString(1, r.encodeRebellion());
  			        ps.setString(2, r.getUuid().toString());
  			        ps.executeUpdate();
  			        ps.close();
  			      } catch (Exception ex) {
  			    	  //put error message here
  			      }
  	  		}
  		}
  	}
  
  	private boolean isConnected() {
  		return (con != null);
  	}
  
  	public void connect() {
  		if (!isConnected())
  			try {
  				String connection = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?useSSL=" + this.useSSL.toString().toLowerCase();
  				con = DriverManager.getConnection(connection, this.username, this.password);
  				//Bukkit.getConsoleSender().sendMessage(GemsCommandExecutor.getFormattedMessage(Bukkit.getConsoleSender(), (MinevoltGems.getConfigInstance()).pr + " &ahas successfully connected to MySQL Database!"));	
  			} catch (SQLException e) {
  				//Bukkit.getConsoleSender().sendMessage(GemsCommandExecutor.getFormattedMessage(Bukkit.getConsoleSender(), (MinevoltGems.getConfigInstance()).pr + " &c cannot connect to MySQL Database..."));
  			}  
 	}
  
  	public void disconnect() {
  		try {
  			con.close();
  			//Bukkit.getConsoleSender().sendMessage(GemsCommandExecutor.getFormattedMessage(Bukkit.getConsoleSender(), (MinevoltGems.getConfigInstance()).pr + " &ahas successfully disconnected from MySQL Database!"));
  		} catch (SQLException e) {
  			//Bukkit.getConsoleSender().sendMessage(GemsCommandExecutor.getFormattedMessage(Bukkit.getConsoleSender(), (MinevoltGems.getConfigInstance()).pr + " &ccould not disconnect from MySQL Database..."));
  		} 
  	}
  
  	private void keepAlive() {
  		try {
  			con.isValid(0);
  		} catch (SQLException e) {
  			if(!isConnected())
  				connect();
  		}              
  	}
  
  	private PreparedStatement getStatement(String sql) {
  		if (isConnected())
  			try {
  				PreparedStatement ps = con.prepareStatement(sql);
  				return ps;
  			} catch (SQLException e) {
  				e.printStackTrace();
  			}  
  		return null;
  	}
  
 	private void createTable(String tableName) throws SQLException {
 		if (isConnected()) {
 			try {
 	        	PreparedStatement ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (uuid VARCHAR(100) PRIMARY KEY, base64 VARCHAR(64))");
 		 			ps.executeUpdate();
 	        } catch (SQLException e) {
 	            throw e;
 	        }
 		}      
    }

    private List<String> getTableColumnNames(String tableName) throws SQLException {
    	if (isConnected()) {
    		try {
                DatabaseMetaData metaData = con.getMetaData();

                // Get columns of the specified table
                ResultSet resultSet = metaData.getColumns(null, null, tableName, null);
                resultSet.last();
                int columnCount = resultSet.getRow();
                resultSet.beforeFirst();

                if (columnCount > 0) {
                    String[] columnNames = new String[columnCount];
                    int index = 0;

                    while (resultSet.next()) {
                        columnNames[index++] = resultSet.getString("COLUMN_NAME");
                    }

                    return new ArrayList<String>(Arrays.asList(columnNames));
                } else {
                    return null; // Table not found or no columns
                }
            } catch (SQLException e) {
                throw e;
            }
    	}
        return null;
    }
}
