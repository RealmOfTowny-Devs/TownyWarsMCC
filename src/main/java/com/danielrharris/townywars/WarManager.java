package com.danielrharris.townywars;

import com.danielrharris.townywars.storage.MySQL;
import com.danielrharris.townywars.storage.SQLite;
import com.danielrharris.townywars.storage.YMLFile;
import com.danielrharris.townywars.warObjects.Rebellion;
import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.TownyUniverse;
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
import org.bukkit.plugin.Plugin;

public class WarManager
{

	private Set<War> activeWars;
	private Set<Rebellion> activeRebellions;
	private Set<String> requestedPeace;
	private  Map<String, Double> neutral;
	public Town townremove;
	private TownyWars townywars;
	private YMLFile ymlfile;
	private SQLite sqlite;
	private MySQL mysql;
	//private static final int SAVING_VERSION = 1;
  
	public WarManager(TownyWars townywars) {
		this.townywars = townywars;
		activeWars = new HashSet<War>();
		activeRebellions = new HashSet<Rebellion>();
		requestedPeace = new HashSet<String>();
		neutral = new HashMap<String, Double>();
	}
  
	public void save() throws Exception {
		switch (this.townywars.getConfigInstance().method) {
    		case file:
    			this.ymlfile.saveWars(activeWars);
    			this.ymlfile.saveRebellions(activeRebellions);
    		case mysql:
    		    this.mysql.saveWars(activeWars);
                this.mysql.saveRebellions(activeRebellions);
    		case sqlite:
    			this.sqlite.saveWars(activeWars);
    			this.sqlite.saveRebellions(activeRebellions);
    		default:
    			this.ymlfile.saveWars(activeWars);
    			this.ymlfile.saveRebellions(activeRebellions);
		}
	}
  
  	public void load() throws Exception {
  		switch (this.townywars.getConfigInstance().method) {
	    	case file:
	      	    this.ymlfile = new YMLFile(townywars);
	      	    activeWars = ymlfile.loadWars();
	      	    activeRebellions = ymlfile.loadRebellions();
	    	case mysql:
	    		this.mysql = new MySQL(townywars);
	    		activeWars = mysql.loadWars();
	    		activeRebellions = mysql.loadRebellions();
	    	case sqlite:
	            this.sqlite = new SQLite(townywars);
	            activeWars = sqlite.loadWars();
	            activeRebellions = sqlite.loadRebellions();
	    	default:
	    		this.ymlfile = new YMLFile(townywars);
	      	    activeWars = ymlfile.loadWars();
	      	    activeRebellions = ymlfile.loadRebellions();
  		}	    
  	}
  
  public Set<War> getWars()
  {
    return activeWars;
  }
  
  public War getWarForParticipant(WarParticipant part)
  {
    for (War w : activeWars) {
        if(w.getWarParticipants().contains(part)) {
        	return w;
        }
    }
    return null;
  }
  
    public War getWarForNation(Nation nation)
    {
        for (War w : activeWars) {
    	    for(WarParticipant part : w.getWarParticipants()) {
    	        if(part.getType().equalsIgnoreCase("nation")) {
    	    	    for(Town town : part.getTownsList()) {
    	    		    if(town.hasNation()) {
    	    		        try {
						        if(town.getNation() == nation) {
							        return w;
						        }
					        } catch (NotRegisteredException e) {
						        // TODO Auto-generated catch block
						        e.printStackTrace();
					        }
    	    		    }
    	    	    }
    	        }	
    	    }
        }
        return null;
    }
  
    public War getWarForTown(Town town)
    {
        for (War w : activeWars) {
    	    for(WarParticipant part : w.getWarParticipants()) {
    	        for(Town t : part.getTownsList()) {
    	            if(t == town) {
				        return w;
				    }
    	        }
    	    }	
        }
        return null;
    }
  
  public void createWar(Nation nat, Nation onat, CommandSender cs){
	  createWar(nat, onat, cs, null);
  }
  
  @SuppressWarnings("deprecation")
public void createWar(Nation nat, Nation onat, CommandSender cs, Rebellion r)
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
public boolean requestPeace(Nation nat, Nation onat, boolean admin)
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
  
  public void endWar(WarParticipant winner, WarParticipant looser, boolean peace)
  {
	boolean isRebelWar = WarManager.getWarForParticipant(winner).getRebellion() != null;
	Rebellion rebellion = WarManager.getWarForParticipant(winner).getRebellion();
	
	try
	{
	   //uggh gotta check town or nation here again
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
  
    public boolean hasBeenOffered(War ww, Nation nation)
    {
        try {
		    return requestedPeace.contains(ww.getEnemy(nation));
	    } catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
	    }   
        return false;
    }

    public Set<Rebellion> getActiveRebellions() {
	    return activeRebellions;
    }

    public void setActiveRebellions(Set<Rebellion> allRebellions) {
	    activeRebellions = allRebellions;
    }
    
    public Rebellion getRebellionFromName(String s) throws Exception{
		for(Rebellion r : activeRebellions)
			if(r.getName().equals(s))
				return r;
		throw(new Exception("Rebellion not found!"));
	}
    
}