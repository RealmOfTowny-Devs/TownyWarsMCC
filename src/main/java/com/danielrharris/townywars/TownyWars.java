package com.danielrharris.townywars;

import com.danielrharris.townywars.config.TownyWarsConfig;
import com.danielrharris.townywars.config.TownyWarsDataManager;
import com.danielrharris.townywars.config.TownyWarsLanguage;
import com.danielrharris.townywars.listeners.*;
import com.danielrharris.townywars.placeholders.PlaceholderAPI;
import com.danielrharris.townywars.placeholders.mvdwPlaceholderAPI;
import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//import main.java.com.danielrharris.townywars.War.MutableInteger;

public class TownyWars extends JavaPlugin
{
	private TaskScheduler scheduler;
    private TownyUniverse tUniverse;
    private static TownyWars plugin;
    private TownyWarsConfig config;
    private TownyWarsLanguage language;
    private TownyWarsDataManager dataManager;
    private GriefManager gm;
    private WarManager warManager;
    private PlaceholderAPI papi = null;
    private mvdwPlaceholderAPI mpapi = null;
    File wallConfigFile = new File(this.getDataFolder(), "walls.yml");
    public static HashMap<Chunk, List<Location>> wallBlocks = new HashMap<Chunk, List<Location>>();
    public Map<String,TownyWarsResident> allTownyWarsResidents = new HashMap<String,TownyWarsResident>();
    //set up the date conversion spec and the character set for file writing
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd zzz HH:mm:ss");
    private static final Charset utf8 = StandardCharsets.UTF_8;
    private WarExecutor executor;
    private CommandMap cmap;   
    private List<CCommand> commands;
    private PluginManager pm;
	
    private static final String deathsFile="deaths.txt";

    @Override
    public void onEnable()
    {
    	scheduler = UniversalScheduler.getScheduler(this);
    	this.papi = null;
	  	TownyWars.plugin = this;
        this.config = new TownyWarsConfig(this);
        this.language = new TownyWarsLanguage(this);
        this.dataManager = new TownyWarsDataManager(this);
        try {
			this.warManager = new WarManager();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	  	this.pm = getServer().getPluginManager();
	  	this.executor = new WarExecutor(this);
	  	this.RegisterCommands();
	  	gm = new GriefManager(this);
	  	pm.registerEvents(new GriefListener(this, gm), this);
	  	pm.registerEvents(new WarListener(this), this);
	  	pm.registerEvents(new PvPListener(this), this);
	  	pm.registerEvents(new NationWalkEvent(),this);
	  	pm.registerEvents(new EnemyWalkWWar(),this);
	  	
	  	tUniverse = TownyUniverse.getInstance();
	  	for(Town town : tUniverse.getTowns()){
	  		town.setAdminEnabledPVP(false);
	  		town.setAdminDisabledPVP(false);
	  		town.setPVP(false);
	  	}
	  	for (War w : WarManager.getWars()) {
	  		for (WarParticipant p : w.getWarParticipants()) {
	  			for (Town t : p.getTownsList()) {
	  				t.setPVP(true);
	  			}
	  		}
	  	}
    
	  	tUniverse.getDataSource().saveTowns();
        
	  	if(Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
	  		Bukkit.getServer().getLogger().info("Hooked into MVdWPlaceholderAPI!");
	      	this.mpapi = new mvdwPlaceholderAPI(plugin);
	    }
	    if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
	    	Bukkit.getServer().getLogger().info("Hooked into PlaceholderAPI!");
	      	this.papi = new PlaceholderAPI(plugin);
	      	papi.register();
	    }
	  	
	  	if(!(wallConfigFile.exists())){
	  		try {
	  			wallConfigFile.createNewFile();
	  			YamlConfiguration wallConfig = YamlConfiguration.loadConfiguration(wallConfigFile);
	  		}catch (IOException e) {
	  				e.printStackTrace();
	  		}
	  	}  
    
	  	try{
	  		for (Resident re : tUniverse.getResidents()){
	  			if (allTownyWarsResidents.get(re.getName())==null){
	  				addTownyWarsResident(re.getName());
	  			}
	  		}
	  	}catch (Exception ex)
	  	{
	  		System.out.println("failed to add residents!");
	  		ex.printStackTrace();
	  	}   
    }
  
  @Override
  public void onDisable()
  {
	  try
	  {
		  dataManager.save();  
	  }
	  catch (Exception ex)
	  {
		  Logger.getLogger(TownyWars.class.getName()).log(Level.SEVERE, null, ex);
	  }
	    
	  this.unRegisterCommands();
  }
  
  public boolean reload() {
	  try
	  {
	      dataManager.save();  
	  }
	  catch (Exception ex)
	  {
	      Logger.getLogger(TownyWars.class.getName()).log(Level.SEVERE, null, ex);
	      return false;
	  }
	  
	  this.unRegisterCommands();
	  pm.disablePlugin(TownyWars.plugin);
	  pm.enablePlugin(TownyWars.plugin);
	  return true;
  }
  
  public void addTownyWarsResident(String playerName){
	  TownyWarsResident newPlayer = new TownyWarsResident(playerName);
	  allTownyWarsResidents.put(playerName,newPlayer);
  }
  
  public TownyWarsResident getTownyWarsResident(String playerName){
	  return allTownyWarsResidents.get(playerName);
  }
  

	
  // takes in information about the death that just happened and writes it to a file
  public int writeKillRecord(long deathTime, String playerName, String killerName, String damageCause, String deathMessage){
		
		// convert the time in milliseconds to a date and then convert it to a string in a useful format (have to tack on the milliseconds)
		// format example: 2014-08-29 EDT 10:05:25:756
		Date deathDate = new Date(deathTime);
	    String deathDateString = format.format(deathDate)+":"+deathTime%1000;
		
	    if (killerName==null) {
	    	killerName="nonplayer";
	    }
	    
	    // prepare the death record string that will be written to file
		List<String> deathRecord = Arrays.asList(deathDateString+": "+playerName+" died to "+killerName+" via "+damageCause+"; '"+deathMessage+"'");
		
		
		// append the death record to the specified file
		try {	
				Files.write(Paths.get(deathsFile), deathRecord, utf8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}
		// some kind of error occurred . . . .
		catch (IOException e) {
			e.printStackTrace();
			return 1;
		}
		// all good!
		return 0;
	}
  
    public CommandMap getCommandMap() {
    	return cmap;
    }
    
    public class CCommand extends Command {
        private CommandExecutor exe = null;
      
        protected CCommand(String name) {
        	super(name);
        }
      
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        	if (this.exe != null)
        		this.exe.onCommand(sender, this, commandLabel, args); 
        	return false;
        }
      
      public void setExecutor(CommandExecutor exe) {
          this.exe = exe;
      }
      
    }
  
    private void RegisterCommands() {
    	String cbukkit = Bukkit.getServer().getClass().getPackage().getName() + ".CraftServer";
    	try {
    		Class<?> clazz = Class.forName(cbukkit);
    		try {
    			Field f = clazz.getDeclaredField("commandMap");
    			f.setAccessible(true);
    			cmap = (CommandMap)f.get(Bukkit.getServer());
    			boolean defalt = false;
    			for(String cmd : getLanguage().mainCommand.getNames()) { 				
    				if (!cmd.equals(null)) {
    					CCommand command = new CCommand(cmd);
        				this.commands.add(command);
        				if(!cmap.register("twars", command)) {
        					if(!defalt) {
        						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', getLanguage().messagePrefix + " &aCommand &e" + cmd + " &chas already been taken. Defaulting to &e'twars' &cfor TownyWars command."));
            					defalt = true;
        					}			
        				}else {
        	     	        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', getLanguage().messagePrefix + "&aCommand &e" + cmd + " &aRegistered!"));
        				}
        				command.setExecutor(executor);        
        			}
    			}   			 
    		} catch (Exception e) {
    			e.printStackTrace();
    		} 
    	} catch (ClassNotFoundException e) {
    		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', getLanguage().messagePrefix + " &ccould not be loaded, is this even Spigot or CraftBukkit?"));
    		setEnabled(false);
    	} 
    }
    
    private void unRegisterCommands() {
        String cbukkit = Bukkit.getServer().getClass().getPackage().getName() + ".CraftServer";
        try {
            Class<?> clazz = Class.forName(cbukkit);
            try {
                Field f = clazz.getDeclaredField("commandMap");
                f.setAccessible(true);
                cmap = (CommandMap)f.get(Bukkit.getServer());
                for(CCommand command : this.commands) {
                	command.unregister(cmap);
                    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', getLanguage().messagePrefix + " &aCommand " + command.getName() + " has been Unregistered!"));
                }
            } catch (Exception e) {
            e.printStackTrace();
            } 
        } catch (ClassNotFoundException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', getLanguage().messagePrefix + " &ccould not be unloaded, is this even Spigot or CraftBukkit?"));
            setEnabled(false);
        } 
  }
	
	public static TownyWars getInstance(){
		return plugin;
	}
	
	public WarManager getWarManager(){
		return warManager;
	}
	
	public TownyWarsDataManager getDataManager() {
		return dataManager;
	}
	
	public TownyWarsConfig getConfigInstance() {
	    return config;
	}

	public TownyWarsLanguage getLanguage() {
		return language;
	}
	
	public PlaceholderAPI getPapiInstance() {
		return papi;
	}

	public mvdwPlaceholderAPI getMpapiInstance() {
		return mpapi;
	}

	public TaskScheduler getScheduler() {
		return scheduler;
	}
	
	//Universal Scheduler examples
	
	/*
	 * Call it just like
		Main.getScheduler().runTaskLater(() -> { //Main there is your plugin Main
		        Bukkit.broadcastMessage("Wow, it was scheduled");
		});
		If you need to get the scheduled task for some reason
		MyScheduledTask task = Main.getScheduler().runTaskLater(() -> { //Main there is your plugin Main
		        Bukkit.broadcastMessage("Wow, it was scheduled");
		}, 10L);
	 */
	
}