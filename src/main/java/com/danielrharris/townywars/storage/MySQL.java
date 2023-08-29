package com.danielrharris.townywars.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Bukkit;

import com.danielrharris.townywars.TownyWars;

public class MySQL {
  public static Connection con;
  
  private String host = "127.0.0.1";
  private String port = "3306";
  private String database = "MinevoltGems";
  private String username = "root";
  private String password = "example";
  private Boolean useSSL = Boolean.valueOf(false);
  
  public MySQL(TownyWars plugin) {
    this.host = (TownyWars.getConfigInstance()).host;
    this.port = (TownyWars.getConfigInstance()).port;
    this.database = (TownyWars.getConfigInstance()).database;
    this.username = (TownyWars.getConfigInstance()).username;
    this.password = (TownyWars.getConfigInstance()).password;
    this.useSSL = Boolean.valueOf((TownyWars.getConfigInstance()).useSSL);
    //Start "keepAlive" task to keep connection active
    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> keepAlive(), 20*60*60*7, 20*60*60*7);
  }
  
  public boolean isConnected() {
    return (con != null);
  }
  
  public void connect() {
    if (!isConnected())
      try {
        String connection = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?autoReconnect=true&useSSL=" + this.useSSL.toString().toLowerCase();
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
    	  //Bukkit.getConsoleSender().sendMessage(GemsCommandExecutor.getFormattedMessage(Bukkit.getConsoleSender(), (MinevoltGems.getConfigInstance()).pr + " &ccould not get ping from MySQL Database..."));
      }              
  }
  
  public PreparedStatement getStatement(String sql) {
    if (isConnected())
      try {
        PreparedStatement ps = con.prepareStatement(sql);
        return ps;
      } catch (SQLException e) {
        e.printStackTrace();
      }  
    return null;
  }
  
  public ResultSet getResult(String sql) {
    if (isConnected())
      try {
        PreparedStatement ps = getStatement(sql);
        ResultSet rs = ps.executeQuery();
        return rs;
      } catch (SQLException e) {
        e.printStackTrace();
      }  
    return null;
  }
}
