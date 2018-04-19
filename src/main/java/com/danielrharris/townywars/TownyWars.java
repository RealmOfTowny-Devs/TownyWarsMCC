package com.danielrharris.townywars;

import com.danielrharris.townywars.listeners.*;
import com.danielrharris.townywars.tasks.SaveTask;
import com.danielrharris.townywars.trades.TradeFile;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
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

public class TownyWars
  extends JavaPlugin
{
  public static TownyUniverse tUniverse;
  public static Towny towny;
  public static double pPlayer;
  public static double pPlot;
  public static double pKill;
  public static double pKillPoints;
  public static double pMayorKill;
  public static double pKingKill;
  public static double pBlock;
  public static double pBlockPoints;
  public static double declareCost;
  public static double endCost;
  public static boolean allowGriefing;
  public static boolean allowRollback;
  public static int timer;
  public static boolean warExplosions;
  public static boolean realisticExplosions;
  public static int debrisChance;
  public static ArrayList<String> worldBlackList;
  private ArrayList<String> blockStringBlackList;
  public static File idConfigFile = null;
  public static YamlConfiguration idConfig = null;
  public static Set<Material> blockBlackList;
  public static boolean isBossBar = false;
  private static TownyWars plugin;
  private GriefManager gm;
  private TradeFile tradeFile;

  File wallConfigFile = new File(this.getDataFolder(), "walls.yml");

  public static HashMap<Chunk, List<Location>> wallBlocks = new HashMap<Chunk, List<Location>>();

  public Map<String,TownyWarsResident> allTownyWarsResidents = new HashMap<String,TownyWarsResident>();

  public static List<String> messagedPlayers = new ArrayList<String>();

  //set up the date conversion spec and the character set for file writing
  private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd zzz HH:mm:ss");
  private static final Charset utf8 = StandardCharsets.UTF_8;

  private static final String deathsFile="deaths.txt";


    @Override
  public void onDisable()
  {
	gm.saveData(GriefListener.getGriefedBlocks());
    try
    {
      WarManager.save();
    }
    catch (Exception ex)
    {
      Logger.getLogger(TownyWars.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void onEnable()
  {
	plugin = this;
    try
    {
      WarManager.load(getDataFolder());
    }
    catch (Exception ex)
    {
      Logger.getLogger(TownyWars.class.getName()).log(Level.SEVERE, null, ex);
    }
    PluginManager pm = getServer().getPluginManager();
    idConfigFile = new File(getDataFolder(), "ideology.yml");
    tradeFile =new TradeFile(this);
    gm = new GriefManager(this);
    pm.registerEvents(new GriefListener(this, gm), this);
    pm.registerEvents(new WarListener(this), this);
    pm.registerEvents(new PvPListener(this), this);
    pm.registerEvents(new NationWalkEvent(),this);
    pm.registerEvents(new EnemyWalkWWar(),this);
    getCommand("twar").setExecutor(new WarExecutor(this));
    getCommand("ideology").setExecutor(this);
    getCommand("townideology").setExecutor(this);
    towny = ((Towny)Bukkit.getPluginManager().getPlugin("Towny"));
    tUniverse = towny.getTownyUniverse();
    if(Bukkit.getPluginManager().getPlugin("BossBarAPI")!=null){
    	isBossBar = true;
    }
    for(Town town : TownyUniverse.getDataSource().getTowns()){
    	town.setAdminEnabledPVP(false);
    	town.setAdminDisabledPVP(false);
    	town.setPVP(false);
    }
    for (War w : WarManager.getWars()) {
      for (Nation nation : w.getNationsInWar()) {
          for (Town t : nation.getTowns()) {
            t.setPVP(true);
          }
      }
    }

    TownyUniverse.getDataSource().saveTowns();

    this.saveDefaultConfig();

    if(!(wallConfigFile.exists())){
	      try {
	    	  wallConfigFile.createNewFile();
		} catch (IOException e) {

			e.printStackTrace();
		}
    } if (!idConfigFile.exists()) {
      try
      {
          idConfigFile.createNewFile();
      }
      catch (IOException e)
      {
          e.printStackTrace();
      }
  }  idConfig = YamlConfiguration.loadConfiguration(idConfigFile);
      try
      {
          idConfig.save(idConfigFile);
      }
      catch (IOException e)
      {
          e.printStackTrace();
      }

    YamlConfiguration wallConfig = YamlConfiguration.loadConfiguration(wallConfigFile);

    pPlayer = getConfig().getDouble("pper-player");
    pPlot = getConfig().getDouble("pper-plot");
    declareCost = getConfig().getDouble("declare-cost");
    endCost = getConfig().getDouble("end-cost");
    pKill = getConfig().getDouble("death-cost");
    pKillPoints = getConfig().getDouble("pper-player-kill");
    pMayorKill = getConfig().getDouble("pper-mayor-kill");
    pKingKill = getConfig().getDouble("pper-king-kill");
    String allowGriefingS;
    allowGriefingS = getConfig().getString("griefing.allow-griefing");
    String allowRollbackS;
    allowRollbackS = getConfig().getString("griefing.allow-rollback");
    allowGriefing = Boolean.valueOf(allowGriefingS.toUpperCase());
    allowRollback = Boolean.valueOf(allowRollbackS.toUpperCase());
    if(allowRollback){
    	timer = ((getConfig().getInt("griefing.save-timer"))*60)*20;
    }
    pBlock = getConfig().getDouble("griefing.per-block-cost");
    pBlockPoints = getConfig().getDouble("griefing.per-block-points");
    String tempExplosions = getConfig().getString("griefing.allow-explosions-war");
    warExplosions = Boolean.valueOf(tempExplosions.toUpperCase());
    String realExplosions = getConfig().getString("griefing.explosions.realistic-explosions");
    realisticExplosions = Boolean.valueOf(realExplosions.toUpperCase());
    debrisChance = getConfig().getInt("griefing.explosions.debris-chance");
    for(String string : (ArrayList<String>) getConfig().getStringList("griefing.worldBlackList")){
    	worldBlackList.add(string.toLowerCase());
    }
    this.blockStringBlackList = (ArrayList<String>) getConfig().getStringList("griefing.blockBlackList");
    blockBlackList = convertBanList(this.blockStringBlackList);
    if(TownyWars.allowRollback){
    	new SaveTask(this.gm).runTaskTimer(plugin, TownyWars.timer, TownyWars.timer);
    }
    try{
    	for (Resident re : tUniverse.getActiveResidents()){
    		if (allTownyWarsResidents.get(re.getName())==null){
    			addTownyWarsResident(re.getName());
    		}
    	}
    }catch (Exception ex)
    {
        System.out.println("failed to add residents!");
        ex.printStackTrace();
    }

  } public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    if (cmd.getName().equalsIgnoreCase("ideology"))
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "This command is only usable by players, sorry!");
            return true;
        }
        Player player = (Player)sender;
        try
        {
            Resident res = TownyUniverse.getDataSource().getResident(player.getName());
            if (res.hasTown())
            {
                Town t = res.getTown();
                String name = t.getName();
                if (idConfig.contains(name))
                {
                    player.sendMessage(ChatColor.RED + "Your town already has an ideology set!");
                    return true;
                }
                if (t.getMayor() != res)
                {
                    player.sendMessage(ChatColor.RED + "You cannot set your town's ideology if you are not the mayor!");
                    return true;
                }
                if (args.length != 1)
                {
                    player.sendMessage(ChatColor.RED + "/ideology <ideology>");
                    return true;
                }
                String id = args[0].toLowerCase();
                if ((!id.equals("economic")) && (!id.equals("religious")) && (!id.equals("militaristic")))
                {
                    player.sendMessage(ChatColor.RED + "Valid ideologies are: Economic, Religious, Militaristic");
                    return true;
                }
                idConfig.set(t.getName(), id);
                try
                {
                    idConfig.save(idConfigFile);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                player.sendMessage(ChatColor.GREEN + "Ideology set!");
                return true;
            }else {
                player.sendMessage(ChatColor.RED.toString() + "You do not have a town!");
            }
        }
        catch (NotRegisteredException localNotRegisteredException) {}
    }
    if(cmd.getName().equalsIgnoreCase("townideology")){
        Player player = (Player) sender;
        try {
            Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
            if(resident.hasTown()){
                Town town = resident.getTown();
                String tname = town.getName();
                if(idConfig.contains(tname)){
                    player.sendMessage(ChatColor.GREEN.toString() + "Your ideology is: " + ChatColor.DARK_GREEN.toString() + idConfig.getString(tname));
                    return true;
                }else {
                    player.sendMessage(ChatColor.RED.toString() + "Your town hasn't chosen an ideology.");
                    return true;
                }
            }else {
                player.sendMessage(ChatColor.RED.toString() + "You do not have a town!");
            }
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }
    }
    return false;
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

  	public Set<Material> convertBanList(List<String> banList2){
  		Set<Material> newBanList = new HashSet<Material>();
		if(!banList2.equals(null)){
			for(String s : banList2){
				Material mat = Material.valueOf(s.toUpperCase());
				if(!mat.equals(null)){
					newBanList.add(mat);
				}
			}
		}
		return newBanList;
	}

  	/*
	 * Takes a player and a location, the player is someone who wants to find out if the location
	 * interacted with is located in a town that their nation is at war With
	 */
	public static boolean atWar(Player p, Location loc){
		try
		{
			if(TownyUniverse.getDataSource().getResident(p.getName())!=null)
			{
				Resident re = TownyUniverse.getDataSource().getResident(p.getName());
				if(re.getTown()!=null){
					if(re.getTown().getNation()!=null){
						Nation nation = re.getTown().getNation();
						// add the player to the master list if they don't exist in it yet
						if (plugin.getTownyWarsResident(re.getName())==null){
							plugin.addTownyWarsResident(re.getName());
							System.out.println("resident added!");
						}
						War ww = WarManager.getWarForNation(nation);
						if (ww != null)
						{
							if(TownyUniverse.getTownBlock(loc)!=null){
								TownBlock townBlock = TownyUniverse.getTownBlock(loc);
								Town otherTown = townBlock.getTown();
								if(otherTown!=re.getTown()){
									if(otherTown.getNation()!=null){
										Nation otherNation = otherTown.getNation();
										if(otherNation!=nation){
											Set<Nation> nationsInWar = ww.getNationsInWar();
											if(nationsInWar.contains(otherNation)){
												//nations are at war with each other
												return true;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}catch (Exception ex) {
			return false;
		}
		return false;
	}

	public static TownyWars getInstance(){
		return plugin;
	}

    public TradeFile getTradeFile() {
        return tradeFile;
    }
}