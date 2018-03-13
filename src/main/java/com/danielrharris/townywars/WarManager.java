package com.danielrharris.townywars;

import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.util.FileMgmt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarManager
{

  private static String fileSeparator = System.getProperty("file.separator");
  private static Set<War> activeWars = new HashSet<War>();
  private static Set<String> requestedPeace = new HashSet<String>();
  public static Map<String, Double> neutral = new HashMap<String, Double>();
  public static Town townremove;
  //private static final int SAVING_VERSION = 1;
  
  public static void save()
    throws Exception
  {
	  FileMgmt.CheckYMLExists(new File("plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml"));
	    if(!WarManager.getWars().isEmpty()){
		    String s = new String("");
		    
		    for(War w : WarManager.getWars())
		    	s += w.objectToString() + "\n";
		    
		    s = s.substring(0, s.length()-1);
		    
		    FileMgmt.stringToFile(s, "plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml");
		 } else
	    	FileMgmt.stringToFile("", "plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml");
    
    //save Rebellions
    //tripple space to separate rebellion objects
    FileMgmt.CheckYMLExists(new File("plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml"));
    if(!Rebellion.getAllRebellions().isEmpty()){
	    String s = new String("");
	    
	    for(Rebellion r : Rebellion.getAllRebellions())
	    	s += r.objectToString() + "\n";
	    
	    s = s.substring(0, s.length()-1);
	    
	    FileMgmt.stringToFile(s, "plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml");
	 } else
    	FileMgmt.stringToFile("", "plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml");
  }
  
  public static void load(File dataFolder)
    throws Exception
  {
	  	String folders[] = {"plugins" + fileSeparator + "TownyWars"};
	  	FileMgmt.checkFolders(folders);
	  	
	  	 //load rebellions
	    FileMgmt.CheckYMLExists(new File("plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml"));
	    String s = FileMgmt.convertFileToString(new File("plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml"));
	    
	    if(!s.isEmpty()){
		    ArrayList<String> slist = new ArrayList<String>();
		    
		    for(String temp : s.split("\n"))
		    	slist.add(temp);
		    
		    for(String s2 : slist)
		    	Rebellion.getAllRebellions().add(new Rebellion(s2));
	    }
	    
	    //load wars
	  	FileMgmt.CheckYMLExists(new File("plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml"));
	    String sw = FileMgmt.convertFileToString(new File("plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml"));
	    
	    if(!sw.isEmpty()){
		    ArrayList<String> slist = new ArrayList<String>();
		    
		    for(String temp : sw.split("\n"))
		    	slist.add(temp);
		    
		    for(String s2 : slist)
		    	WarManager.getWars().add(new War(s2));
	    }
  }
  
  public static Set<War> getWars()
  {
    return activeWars;
  }
  
  public static War getWarForNation(Nation onation)
  {
    for (War w : activeWars) {
      if (w.hasNation(onation)) {
        return w;
      }
    }
    return null;
  }
  
  public static void createWar(Nation nat, Nation onat, CommandSender cs){
	  createWar(nat, onat, cs, null);
  }
  
  @SuppressWarnings("deprecation")
public static void createWar(Nation nat, Nation onat, CommandSender cs, Rebellion r)
  { 
    if ((getWarForNation(nat) != null) || (getWarForNation(onat) != null))
    {
      cs.sendMessage(ChatColor.RED + "Your nation is already at war with another nation!");
    }
    else
    {
      try
      {
        try
        {
          TownyUniverse.getDataSource().getNation(nat.getName()).addEnemy(onat);
          TownyUniverse.getDataSource().getNation(onat.getName()).addEnemy(nat);
        }
        catch (AlreadyRegisteredException ex)
        {
          Logger.getLogger(WarManager.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      catch (NotRegisteredException ex)
      {
        Logger.getLogger(WarManager.class.getName()).log(Level.SEVERE, null, ex);
      }
      War war = new War(nat, onat, r);
      activeWars.add(war);
      for (Resident re : nat.getResidents())
      {
        Player plr = Bukkit.getPlayer(re.getName());
        if (plr != null) {
          plr.sendMessage(ChatColor.RED + "Your nation is now at war with " + onat.getName() + "!");
        }
      }
      for (Resident re : onat.getResidents())
      {
        Player plr = Bukkit.getPlayer(re.getName());
        if (plr != null) {
          plr.sendMessage(ChatColor.RED + "Your nation is now at war with " + nat.getName() + "!");
        }
      }
      for (Town t : nat.getTowns()) {
        t.setPVP(true);
      }
      for (Town t : onat.getTowns()) {
        t.setPVP(true);
      }
    }
    
    TownyUniverse.getDataSource().saveTowns();
    TownyUniverse.getDataSource().saveNations();
    try {
		WarManager.save();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  @SuppressWarnings("deprecation")
public static boolean requestPeace(Nation nat, Nation onat, boolean admin)
  {
	  
    if ((admin) || (requestedPeace.contains(onat.getName())))
    {
      if(getWarForNation(nat).getRebellion() != null)
    	  getWarForNation(nat).getRebellion().peace();
      endWar(nat, onat, true);
      
      try
      {
        nat.collect(TownyWars.endCost);
        onat.collect(TownyWars.endCost);
      }
      catch (EconomyException ex)
      {
        Logger.getLogger(WarManager.class.getName()).log(Level.SEVERE, null, ex);
      }
      return true;
    }
    if (admin)
    {
      endWar(nat, onat, true);
      return true;
    }
    requestedPeace.add(nat.getName());
    for (Resident re : onat.getResidents()) {
      if ((re.isKing()) || (onat.hasAssistant(re)))
      {
        Player plr = Bukkit.getPlayer(re.getName());
        if (plr != null) {
          plr.sendMessage(ChatColor.GREEN + nat.getName() + " has requested peace!");
        }
      }
    }
    return false;
  }
  
  public static void endWar(Nation winner, Nation looser, boolean peace)
  {
	boolean isRebelWar = WarManager.getWarForNation(winner).getRebellion() != null;
	Rebellion rebellion = WarManager.getWarForNation(winner).getRebellion();
	
	try
	{
	   TownyUniverse.getDataSource().getNation(winner.getName()).removeEnemy(looser);
	   TownyUniverse.getDataSource().getNation(looser.getName()).removeEnemy(winner);
	    }
	    catch (NotRegisteredException ex)
	    {
	      Logger.getLogger(WarManager.class.getName()).log(Level.SEVERE, null, ex);
	    }
    
    activeWars.remove(getWarForNation(winner));
    requestedPeace.remove(looser.getName());
    War.broadcast(winner, ChatColor.GREEN + "You are now at peace!");
    War.broadcast(looser, ChatColor.GREEN + "You are now at peace!");
    for (Town t : winner.getTowns()) {
      t.setPVP(false);
    }
    
    //rebels win
    if(!peace && isRebelWar && winner == rebellion.getRebelnation()){
		War.broadcast(looser, ChatColor.RED + winner.getName() + " won the rebellion and are now free!");
		War.broadcast(winner, ChatColor.GREEN + winner.getName() + " won the rebellion and are now free!");
    	rebellion.success();
    	Rebellion.getAllRebellions().remove(rebellion);
    	TownyUniverse.getDataSource().removeNation(winner);
        winner.clear();
        TownyWars.tUniverse.getNationsMap().remove(winner.getName());
    }
    
    //rebelwar white peace
    if(isRebelWar && peace){
    	if(winner != rebellion.getMotherNation()){
	    	TownyUniverse.getDataSource().removeNation(winner);
		    TownyWars.tUniverse.getNationsMap().remove(winner.getName());
    	} else{
    		TownyUniverse.getDataSource().removeNation(looser);
		    TownyWars.tUniverse.getNationsMap().remove(looser.getName());
    	}
    }
    
    //TODO risk of concurrentmodificationexception please fix or something
    for (Town t : looser.getTowns())
    {
      if (!peace && !isRebelWar) {
        try
        {
          WarManager.townremove = t;
          looser.removeTown(t);
          winner.addTown(t);
        }
        catch (Exception ex)
        {
          Logger.getLogger(WarManager.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      t.setPVP(false);
    }
    if (!peace && isRebelWar && winner != rebellion.getRebelnation())
    {
      TownyUniverse.getDataSource().removeNation(looser);
      looser.clear();
      TownyWars.tUniverse.getNationsMap().remove(looser.getName());
    }
    Rebellion.getAllRebellions().remove(rebellion);
    
    if(looser.getTowns().size() == 0)
    	TownyUniverse.getDataSource().removeNation(looser);
    if(winner.getTowns().size() == 0)
    	TownyUniverse.getDataSource().removeNation(winner);
    
    TownyUniverse.getDataSource().saveTowns();
    TownyUniverse.getDataSource().saveNations();
    try {
		WarManager.save();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  public static boolean hasBeenOffered(War ww, Nation nation)
  {
    try {
		return requestedPeace.contains(ww.getEnemy(nation));
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    return false;
  }
}