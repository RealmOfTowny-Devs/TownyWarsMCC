/*
This code has been written through a collection of developers and was finalized and completed by Carson Rhodes

You may find the majority of the plugin's functionality within the WarExecutor class.  (facilitates war declaration, creation, and management)
 */

package com.danielrharris.townywars;

import com.danielrharris.townywars.listeners.*;
import com.danielrharris.townywars.tasks.SaveTask;
import com.danielrharris.townywars.trades.TradeFile;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.*;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
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
        extends JavaPlugin {
    //set up the date conversion spec and the character set for file writing
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd zzz HH:mm:ss");
    private static final Charset utf8 = StandardCharsets.UTF_8;
    private static final String deathsFile = "deaths.txt";
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
    public static File idConfigFile = null;
    public static YamlConfiguration idConfig = null;
    public static Set<Material> blockBlackList;
    public static boolean isBossBar = false;
    public static HashMap<Chunk, List<Location>> wallBlocks = new HashMap<Chunk, List<Location>>();
    public static List<String> messagedPlayers = new ArrayList<String>();
    public static CoreProtectAPI coreProtectAPI;
    private static TownyWars plugin;
    public Map<String, TownyWarsResident> allTownyWarsResidents = new HashMap<String, TownyWarsResident>();
    File wallConfigFile = new File(this.getDataFolder(), "walls.yml");
    private ArrayList<String> blockStringBlackList;
    private GriefManager gm;
    private TradeFile tradeFile;

    /*
     * Takes a player and a location, the player is someone who wants to find out if the location
     * interacted with is located in a town that their nation is at war with
     */
    public static boolean atWar(Player p, Location loc) {
        try {
            if (TownyUniverse.getInstance().getResident(p.getName()) != null) {
                Resident re = TownyUniverse.getInstance().getResident(p.getName());
                assert re != null;
                if (re.getTown() != null) {
                    if (re.getTown().getNation() != null) {
                        Nation nation = re.getTown().getNation();
                        // add the player to the master list if they don't exist in it yet
                        if (plugin.getTownyWarsResident(re.getName()) == null) {
                            plugin.addTownyWarsResident(re.getName());
                            System.out.println("resident added!");
                        }
                        War ww = WarManager.getWarForNation(nation);
                        if (ww != null) {
                            if (TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(loc)) != null) {
                                try {
                                    TownBlock townBlock = TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(loc));
                                    Town otherTown = townBlock.getTown();
                                    if (otherTown != re.getTown()) {
                                        if (otherTown.getNation() != null) {
                                            Nation otherNation = otherTown.getNation();
                                            if (otherNation != nation) {
                                                Set<Nation> nationsInWar = ww.getNationsInWar();
                                                if (nationsInWar.contains(otherNation)) {
                                                    //nations are at war with each other
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                } catch (NotRegisteredException ex) {return false;}
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    public static TownyWars getInstance() {
        return plugin;
    }

    @Override
    public void onDisable() {
        gm.saveData(GriefListener.getGriefedBlocks());
        try {
            WarManager.save();
        } catch (Exception ex) {
            Logger.getLogger(TownyWars.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        try {
            WarManager.load(getDataFolder());
        } catch (Exception ex) {
            Logger.getLogger(TownyWars.class.getName()).log(Level.SEVERE, null, ex);
        }
        PluginManager pm = getServer().getPluginManager();
        idConfigFile = new File(getDataFolder(), "ideology.yml");
        tradeFile = new TradeFile(this);
        gm = new GriefManager(this);
        coreProtectAPI = ((CoreProtect) getServer().getPluginManager().getPlugin("CoreProtect")).getAPI();
        pm.registerEvents(new GriefListener(this, gm), this);
        pm.registerEvents(new WarListener(this), this);
        pm.registerEvents(new PvPListener(this), this);
        pm.registerEvents(new NationWalkEvent(), this);
        pm.registerEvents(new EnemyWalkWWar(), this);
        Objects.requireNonNull(getCommand("twar")).setExecutor(new WarExecutor(this));
        towny = ((Towny) Bukkit.getPluginManager().getPlugin("Towny"));
        tUniverse = TownyUniverse.getInstance();
        if (Bukkit.getPluginManager().getPlugin("BossBarAPI") != null) {
            isBossBar = true;
        }
        for (Town town : TownyUniverse.getInstance().getTowns()) {
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

        TownyUniverse.getInstance().getDataSource().saveTowns();

        this.saveDefaultConfig();

        if (!(wallConfigFile.exists())) {
            try {
                wallConfigFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!idConfigFile.exists()) {
            try {
                idConfigFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        idConfig = YamlConfiguration.loadConfiguration(idConfigFile);
        try {
            idConfig.save(idConfigFile);
        } catch (IOException e) {
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
        assert allowGriefingS != null;
        allowGriefing = Boolean.parseBoolean(allowGriefingS.toUpperCase());
        allowRollback = Boolean.parseBoolean(allowRollbackS.toUpperCase());
        if (allowRollback) {
            timer = ((getConfig().getInt("griefing.save-timer")) * 60) * 20;
        }
        pBlock = getConfig().getDouble("griefing.per-block-cost");
        pBlockPoints = getConfig().getDouble("griefing.per-block-points");
        String tempExplosions = getConfig().getString("griefing.allow-explosions-war");
        assert tempExplosions != null;
        warExplosions = Boolean.parseBoolean(tempExplosions.toUpperCase());
        String realExplosions = getConfig().getString("griefing.explosions.realistic-explosions");
        realisticExplosions = Boolean.parseBoolean(realExplosions.toUpperCase());
        debrisChance = getConfig().getInt("griefing.explosions.debris-chance");
        for (String string : (ArrayList<String>) getConfig().getStringList("griefing.worldBlackList")) {
            worldBlackList.add(string.toLowerCase());
        }
        this.blockStringBlackList = (ArrayList<String>) getConfig().getStringList("griefing.blockBlackList");
        blockBlackList = convertBanList(this.blockStringBlackList);
        if (TownyWars.allowRollback) {
            new SaveTask(this.gm).runTaskTimer(plugin, TownyWars.timer, TownyWars.timer);
        }
        try {
            for (Resident re : tUniverse.getResidents()) {
                if (allTownyWarsResidents.get(re.getName()) == null) {
                    addTownyWarsResident(re.getName());
                }
            }
        } catch (Exception ex) {
            System.out.println("failed to add residents!");
            ex.printStackTrace();
        }
    }

    public void addTownyWarsResident(String playerName) {
        TownyWarsResident newPlayer = new TownyWarsResident(playerName);
        allTownyWarsResidents.put(playerName, newPlayer);
    }

    public TownyWarsResident getTownyWarsResident(String playerName) {
        return allTownyWarsResidents.get(playerName);
    }

    // takes in information about the death that just happened and writes it to a file
    public int writeKillRecord(long deathTime, String playerName, String killerName, String damageCause, String deathMessage) {

        // convert the time in milliseconds to a date and then convert it to a string in a useful format (have to tack on the milliseconds)
        // format example: 2014-08-29 EDT 10:05:25:756
        Date deathDate = new Date(deathTime);
        String deathDateString = format.format(deathDate) + ":" + deathTime % 1000;

        if (killerName == null) {
            killerName = "nonplayer";
        }

        // prepare the death record string that will be written to file
        List<String> deathRecord = Arrays.asList(deathDateString + ": " + playerName + " died to " + killerName + " via " + damageCause + "; '" + deathMessage + "'");


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

    public Set<Material> convertBanList(List<String> banList2) {
        Set<Material> newBanList = new HashSet<Material>();
        if (banList2 != null) {
            for (String s : banList2) {
                Material mat = Material.valueOf(s.toUpperCase());
                newBanList.add(mat);
            }
        }
        return newBanList;
    }

    public TradeFile getTradeFile() {
        return tradeFile;
    }
}