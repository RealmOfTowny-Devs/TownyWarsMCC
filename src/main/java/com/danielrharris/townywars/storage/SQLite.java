package com.danielrharris.townywars.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import com.danielrharris.townywars.TownyWars;


public class SQLite extends SQLiteDatabase{
    private File saveFile;    
    private File dataFolder;
    private String fileName;
    
    public SQLite(TownyWars instance, String fileName){
        super(instance);
        this.dataFolder = new File(this.plugin.getDataFolder().toString() + "/data");
        this.fileName = fileName;
    }

    public String SQLiteCreateTokensTableWars = "CREATE TABLE IF NOT EXISTS wars (" + // make sure to put your table name in here too.
            "`uuid` varchar(32) NOT NULL," + // This creates the different colums you will save data too. varchar(32) Is a string, int = integer
            "`base64` varchar(64) NOT NULL," + 
            ");";

    public String SQLiteCreateTokensTableRebellions = "CREATE TABLE IF NOT EXISTS rebellions (" + // make sure to put your table name in here too.
            "`uuid` varchar(32) NOT NULL," + // This creates the different colums you will save data too. varchar(32) Is a string, int = integer
            "`base64` varchar(64) NOT NULL," + 
            ");";
    
    // SQL creation stuff, You can leave the blow stuff untouched.
    public Connection getSQLConnection() {   	
    	if (!this.dataFolder.exists())
    	      this.dataFolder.mkdir(); 
        if(saveFile == null) {
        	this.saveFile = new File(this.dataFolder, fileName); 
        }
        if (!this.saveFile.exists())
            this.plugin.saveResource("data/" + fileName, false); 
        
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + saveFile);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTokensTableWars);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTokensTableRebellions);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
    
    public static class Error {
        public static void execute(TownyWars plugin, Exception ex){
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        }
        public static void close(TownyWars plugin, Exception ex){
            plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
        }
    }
    
    public static class Errors {
        public static String sqlConnectionExecute(){
            return "Couldn't execute MySQL statement: ";
        }
        public static String sqlConnectionClose(){
            return "Failed to close MySQL connection: ";
        }
        public static String noSQLConnection(){
            return "Unable to retreive MYSQL connection: ";
        }
        public static String noTableFound(){
            return "Database Error: No Table Found";
        }
    }
}