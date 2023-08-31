package com.danielrharris.townywars;

import com.danielrharris.townywars.storage.MySQL;
import com.danielrharris.townywars.storage.SQLite;
import com.danielrharris.townywars.storage.YMLFile;
import com.danielrharris.townywars.warObjects.Rebellion;
import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;
import com.danielrharris.townywars.exceptions.Exceptions.NotInWarException;
import com.danielrharris.townywars.exceptions.Exceptions.AlreadyAtWarException;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarManager
{

	private static Set<War> activeWars;
	private static Set<WarParticipant> requestedPeace;
	public Town townremove;
	private TownyWars townywars;
	private YMLFile ymlfile;
	private SQLite sqlite;
	private MySQL mysql;
	//private static final int SAVING_VERSION = 1;
  
	public WarManager(TownyWars townywars) {
		this.townywars = townywars;
		activeWars = new HashSet<War>();
		requestedPeace = new HashSet<WarParticipant>();
	}
  
	public void save() throws Exception {
		switch (this.townywars.getConfigInstance().method) {
    		case file:
    			this.ymlfile.saveWars(activeWars);
    		case mysql:
    		    this.mysql.saveWars(activeWars);
    		case sqlite:
    			this.sqlite.saveWars(activeWars);
    		default:
    			this.ymlfile.saveWars(activeWars);
		}
	}
  
  	public void load() throws Exception {
  		switch (this.townywars.getConfigInstance().method) {
	    	case file:
	      	    this.ymlfile = new YMLFile(townywars);
	      	    activeWars = ymlfile.loadWars();
	    	case mysql:
	    		this.mysql = new MySQL(townywars);
	    		activeWars = mysql.loadWars();
	    	case sqlite:
	            this.sqlite = new SQLite(townywars);
	            activeWars = sqlite.loadWars();
	    	default:
	    		this.ymlfile = new YMLFile(townywars);
	      	    activeWars = ymlfile.loadWars();
  		}	    
  	}
  
  	public static Set<War> getWars()
  	{
  		return activeWars;
  	}
  	
  	public static WarParticipant getEnemy(WarParticipant  part) throws NotInWarException {
  		return getWarForParticipant(part).getEnemy(part);
  	}
  	
  	public static WarParticipant getWarParticipant(Town town) throws NotInWarException{
  		for (War w : activeWars) {
    	    for(WarParticipant part : w.getWarParticipants()) {
    	    	if(part.getTownsList().contains(town))
    	    		return part;
    	    }
    	}
  		throw new NotInWarException("Specified participant is not in war.");
  	}
  	
  	public static WarParticipant getWarParticipant(Nation nation) throws NotInWarException {
  		for(Town town : nation.getTowns()) {
    		if(getWarParticipant(town)!=null)
    			return getWarParticipant(town);
    	}
  		throw new NotInWarException("Specified participant is not in war.");
  	}
  
  	public static War getWarForParticipant(WarParticipant part) throws NotInWarException {
  		for (War w : activeWars) {
  			if(w.getWarParticipants().contains(part)) {
  				return w;
  			}
  		}
  		throw new NotInWarException("Specified participant is not in war.");
  	}
  	
  	public static boolean isAtWar(Town town) {
		if(getWarForTown(town)!=null)
			return true;
  		return false;
  	}
  	
  	public static boolean isAtWar(Nation nation) {
  		for(Town town : nation.getTowns()) {
  			if(getWarForTown(town)!=null)
  				return true;
  		}
  		return false;
  	}
  
    public static War getWarForTown(Town town)
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
    
    public static War getWarForNation(Nation nation)
    {
    	for(Town t : nation.getTowns()) {
			if(getWarForTown(t)!=null) {
				return getWarForTown(t);
			}
		}
    	return null;
    }
    
    public static boolean isLocationAtWar(Location loc) {
    	WorldCoord coord = WorldCoord.parseWorldCoord(loc);
    	for(War w : activeWars) {
    		for(WarParticipant part : w.getWarParticipants()) {
    			for(Town town : part.getTownsList()) {
    				if(town.hasTownBlock(coord))
    					return true;
    			}
    		}
    	}
    	return false;
    }
    
    public static boolean isLocationAtWar(Block block) {
    	return isLocationAtWar(block.getLocation());
    }
    
    public static WarParticipant getWarParticipantFromLocation(Location loc) {
    	if(isLocationAtWar(loc)) {
    		WorldCoord coord = WorldCoord.parseWorldCoord(loc);
        	for(War w : activeWars) {
        		for(WarParticipant part : w.getWarParticipants()) {
        			for(Town town : part.getTownsList()) {
        				if(town.hasTownBlock(coord)) {
        					return part;
        				}
        			}
        		}
        	}
    	}
    	return null;
    }
    
    public static WarParticipant getWarParticipantFromLocation(Block block) {
    	return getWarParticipantFromLocation(block.getLocation());
    }
    
    public static War getWarAtLocation(Location loc) {
    	if(isLocationAtWar(loc)) {
    		WorldCoord coord = WorldCoord.parseWorldCoord(loc);
        	for(War w : activeWars) {
        		for(WarParticipant part : w.getWarParticipants()) {
        			for(Town town : part.getTownsList()) {
        				if(town.hasTownBlock(coord)) {
        					return w;
        				}
        			}
        		}
        	}
    	}
    	return null;
    }
    
    public static War getWarAtLocation(Block block) {
    	return getWarAtLocation(block.getLocation());
    }
    
    public static WarParticipant createWarParticipant(Object object) throws AlreadyAtWarException
    {
    	if(object instanceof Town) {
    		if(!isAtWar((Town)object))
    			return WarParticipant.createWarParticipant((Town)object);
    	}else if(object instanceof Nation) {
    		if(!isAtWar((Nation)object))
    			return WarParticipant.createWarParticipant((Nation)object);
    	}else if(object instanceof String){
    		String s = (String)object;
    		if(TownyUniverse.getInstance().hasTown(s)){
    			Town town = TownyUniverse.getInstance().getTown(s);
    			if(!isAtWar(town))
    				return WarParticipant.createWarParticipant(town);
    		}
    		if(TownyUniverse.getInstance().hasNation(s)) {
    			Nation nation = TownyUniverse.getInstance().getNation(s);
    			if(!isAtWar(nation))
    				return WarParticipant.createWarParticipant(nation);
    		}
    	}
    	throw new AlreadyAtWarException("One of these participants is already at war!");
    }
    
    public static War createWar(WarParticipant participant1, WarParticipant participant2, CommandSender cs, Rebellion r) {
    	War war = null;
		try {
			war = new War(participant1, participant2, r);
			activeWars.add(war);
	    	for (Resident re : participant1.getResidents())
	    	{
	    		Player plr = Bukkit.getPlayer(re.getName());
	            if (plr != null) {
	            	plr.sendMessage(ChatColor.RED + "Your faction is now at war with " + participant2.getName() + "!");
	            }
	    	}
	    	for (Resident re : participant2.getResidents())
	    	{
	    		Player plr = Bukkit.getPlayer(re.getName());
	            if (plr != null) {
	            	plr.sendMessage(ChatColor.RED + "Your faction is now at war with " + participant1.getName() + "!");
	            }
	    	}
	    	for (Town t : participant1.getTownsList()) {
	    		t.setPVP(true);
	    	}
	    	for (Town t : participant2.getTownsList()) {
	    		t.setPVP(true);
	    	}
	                
	        TownyUniverse.getInstance().getDataSource().saveTowns();
	        TownyUniverse.getInstance().getDataSource().saveNations();
	        try {
	    		TownyWars.getInstance().getWarManager().save();
	    	} catch (Exception e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return war;
    }
  
    public static War createWar(Nation nat, Nation onat, CommandSender cs) throws AlreadyAtWarException{
    	return createWar(createWarParticipant(nat), createWarParticipant(onat), cs, null);
    }
  
    public static War createWar(Nation nat, Nation onat, CommandSender cs, Rebellion r) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(nat), createWarParticipant(onat), cs, r);
    }
    
    public static War createWar(Nation nat, Town town, CommandSender cs, Rebellion r) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(nat), createWarParticipant(town), cs, r);
    }
    
    public static War createWar(Nation nat, Town town, CommandSender cs) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(nat), createWarParticipant(town), cs, null);
    }
    
    public static War createWar(Town town, Nation nation, CommandSender cs, Rebellion r) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(town), createWarParticipant(nation), cs, r);
    }
    
    public static War createWar(Town town, Nation nation, CommandSender cs) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(town), createWarParticipant(nation), cs, null);
    }
    
    public static War createWar(Town town, Town otherTown, CommandSender cs, Rebellion r) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(town), createWarParticipant(otherTown), cs, r);
    }
    
    public static War createWar(Town town, Town otherTown, CommandSender cs) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(town), createWarParticipant(otherTown), cs, null);
    }
  
    public static void endWar(WarParticipant winner, WarParticipant loser, boolean peace)
    {  	
		try {
			War war = WarManager.getWarForParticipant(winner);
			if(!winner.getTownsList().isEmpty() && winner.getTownsList()!=null)
		    	for (Town t : winner.getTownsList()) {
		    		t.setPVP(false);
		    	}
	    	if(!loser.getTownsList().isEmpty() && loser.getTownsList()!=null)
		    	for (Town t : loser.getTownsList()) {
		    		t.setPVP(false);
		    	}
	    	if(peace) {
	    		requestedPeace.remove(loser);
	        	broadcast(winner, ChatColor.GREEN + "You are now at peace!");
	        	broadcast(loser, ChatColor.GREEN + "You are now at peace!");
	        	if(war.isRebelWar()) {
	        		Rebellion rebellion = WarManager.getWarForParticipant(winner).getRebellion();
	        		rebellion.lost();
	        	}
	    	}else {
	    		if(war.isRebelWar()) {
	        		Rebellion rebellion = WarManager.getWarForParticipant(winner).getRebellion();
	        		if(rebellion!=null) {
	            		//rebels win
	                	if(winner == rebellion.getRebelWarParticipant()){
	                		broadcast(loser, ChatColor.RED + winner.getName() + " won the rebellion and are now free!");
	                		broadcast(winner, ChatColor.GREEN + winner.getName() + " won the rebellion and are now free!");
	                		rebellion.success();
	                	}      	
	                	try {
	    					if (winner == war.getEnemy(rebellion.getRebelWarParticipant()))
	    					{
	    						broadcast(loser, ChatColor.RED + winner.getName() + " won the rebellion and are now back under control of their mother nation!");
	    						broadcast(winner, ChatColor.GREEN + loser.getName() + " lost the rebellion and are now back under control of their mother nation!");
	    						rebellion.lost();
	    					}
	    				} catch (Exception e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    				}
	            	}
	        	}else {
	        		loser.pay(loser.getHoldingBalance(), winner);
	        		broadcast(loser, ChatColor.RED + winner.getName() + " has won the war and your " + loser.getType() + " is gone!");
					broadcast(winner, ChatColor.GREEN + loser.getName() + " has lost the war and has been totally annihilated!");
	        	}
	    		
	    		if(loser.getTownsList().size() == 0) {
	        		if(TownyUniverse.getInstance().hasNation(loser.getUuid())) {
	        			Nation nation = TownyUniverse.getInstance().getNation(loser.getUuid());
	            		TownyUniverse.getInstance().getDataSource().deleteNation(nation);
	        		}
	        		if(TownyUniverse.getInstance().hasTown(loser.getUuid())) {
	        			Town town = TownyUniverse.getInstance().getTown(loser.getUuid());
	            		TownyUniverse.getInstance().getDataSource().deleteTown(town);
	        		}
	        	}
	        		
	        	if(winner.getTownsList().size() == 0) {
	        		if(TownyUniverse.getInstance().hasNation(winner.getUuid())) {
	        			Nation nation = TownyUniverse.getInstance().getNation(winner.getUuid());
	        			TownyUniverse.getInstance().getDataSource().deleteNation(nation);
	        		}
	        		if(TownyUniverse.getInstance().hasTown(winner.getUuid())) {
	        			Town town = TownyUniverse.getInstance().getTown(winner.getUuid());
	            		TownyUniverse.getInstance().getDataSource().deleteTown(town);
	        		}
	        	}
	        	
	        	TownyUniverse.getInstance().getDataSource().saveTowns();
	        	TownyUniverse.getInstance().getDataSource().saveNations();
	        			
	        	activeWars.remove(war);
	        	try {
	    			TownyWars.getInstance().getWarManager().save();
	    		} catch (Exception e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	    	}
		} catch (NotInWarException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
  
    public static boolean hasBeenOffered(War ww, WarParticipant participant)
    {
        try {
		    return requestedPeace.contains(ww.getEnemy(participant));
	    } catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
	    }   
        return false;
    }
    
    public static boolean isPlayerAtWarWithLocation(Player p, Location loc) {
    	return isPlayerAtWarWithLocation(getResidentFromPlayer(p),loc);
    }
    
    public static boolean isPlayerAtWarWithLocation(Resident resident, Location loc) {
    	TownyWars.getInstance().getWarManager();
		if(!WarManager.isLocationAtWar(loc))
    		return false;
		try {
			TownyWars.getInstance().getWarManager();
			WarParticipant part = WarManager.getWarParticipantFromLocation(loc);
	    	WarParticipant enemyPart = (WarManager.getWarAtLocation(loc)).getEnemy(part);
	    	for(Resident res : part.getResidents()) {
	    		if(res==resident) {
	    			return false;
	    		}
	    		for(Resident enemy : enemyPart.getResidents()) {
	    			if(resident == enemy) {
	    				return true;
	    			}
	    		}
	    	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  	
    	return false;
    }
    
    public static boolean isPlayerAtWarWithLocation(Player p, Block block) {
    	return isPlayerAtWarWithLocation(p, block.getLocation());
    }
    
    public static boolean isPlayerAtWarWithLocation(Resident r, Block block) {
    	return isPlayerAtWarWithLocation(r, block.getLocation());
    }
    
    public static boolean isPlayerAtWarWithLocation(Player p) {
    	return isPlayerAtWarWithLocation(p, p.getLocation());
    }
    
    public static boolean isPlayerAtWarWithLocation(Resident r) {
    	return isPlayerAtWarWithLocation(r.getPlayer());
    }
    
    public static boolean isPlayerAtWarWithOtherPlayer(Player player, Player enemy) {
    	return isPlayerAtWarWithOtherPlayer(getResidentFromPlayer(player), getResidentFromPlayer(enemy));
    }
    
    public static boolean isPlayerAtWarWithOtherPlayer(Resident player, Resident enemy) {
    	if(player==enemy)
    		return false;
    	TownyWars.getInstance().getWarManager();
		for(War w : WarManager.activeWars) {
    		for(WarParticipant part : w.getWarParticipants()) {
    			if(part.getResidents().contains(player)) {
    				try {
						WarParticipant enemyPart = w.getEnemy(part);
						if(enemyPart.getResidents().contains(enemy)) {
							return true;
						}							
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
    		}
    	}
    	return false;
    }

    public static Resident getResidentFromPlayer(Player p) {
    	if(TownyAPI.getInstance().getResident(p)!=null) {
    		return TownyAPI.getInstance().getResident(p);
    	} else {
			try {
				return TownyAPI.getInstance().getDataSource().newResident(p.getName(), p.getUniqueId());
			} catch (AlreadyRegisteredException | NotRegisteredException e) {
				return null;
			}
    	}
    }
    
    public static void broadcast(WarParticipant participant, String message) {
		for(Resident resident : participant.getResidents()) {
			Player player = resident.getPlayer();
			if(player!=null)
				if(player.isOnline())
					player.sendMessage(message);
		}
	}
    
    public static int getTownMaxPoints(Town town){
		double d = (town.getNumResidents()
				* TownyWars.getInstance().getConfigInstance().pPlayer) + (TownyWars.getInstance().getConfigInstance().pPlot
				* town.getTownBlocks().size());
		return Math.round((float)d);
	}
	
	public static int getNationMaxPoints(Nation nation) {
		int i = 0;
		for(Town town : nation.getTowns()) {
			i = i + getTownMaxPoints(town);
		}
		return i;
	}
	
	public static void removeTownFromNationAndAddToAnotherNation(Town town, Nation nation, Nation newNation) {
        try {
			if (town.hasNation() && town.getNation() == nation) {
			    town.removeNation();
			    TownyUniverse.getInstance().getDataSource().saveTown(town);
			    TownyUniverse.getInstance().getDataSource().saveNation(nation);
			}
			newNation.addTown(town);
			TownyUniverse.getInstance().getDataSource().saveTown(town);
			TownyUniverse.getInstance().getDataSource().saveNation(newNation);
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
	
	public static void moveSoloTownIntoNation(Town town, Nation newNation) {
        if (town.hasNation()) {
		    town.removeNation();
		    TownyUniverse.getInstance().getDataSource().saveTown(town);
		    TownyUniverse.getInstance().getDataSource().saveNations();
		}
		newNation.addTown(town);
		TownyUniverse.getInstance().getDataSource().saveTown(town);
		TownyUniverse.getInstance().getDataSource().saveNation(newNation);
        
    }
	
	public static Nation upgradeTownToNation(WarParticipant winner, Town town, Town townToAdd) {		
		try {
			War war = getWarForParticipant(winner);
			Nation nation = null;
			try {
				TownyUniverse.getInstance().getDataSource().newNation(town.getName() + "-Nation");
				nation = TownyUniverse.getInstance().getNation(town.getName() + "-Nation");
				nation.addTown(town);
				TownyUniverse.getInstance().getDataSource().saveTown(town);
				if(townToAdd.hasNation()) {
					Nation n = townToAdd.getNation();
					townToAdd.removeNation();
					TownyUniverse.getInstance().getDataSource().saveNation(n);
				}			
				nation.addTown(townToAdd);
				nation.setKing(town.getMayor());
	            nation.setCapital(town);
				TownyUniverse.getInstance().getDataSource().saveTown(townToAdd);
				TownyUniverse.getInstance().getDataSource().saveNation(nation);
			} catch (TownyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<UUID, Integer> towns = new HashMap<UUID, Integer>();
			for(UUID id : winner.getTownsMap().keySet()) {
				towns.put(id, winner.getTownsMap().get(id));
			}
			towns.put(townToAdd.getUUID(), WarManager.getTownMaxPoints(townToAdd));
			
			if(winner == war.getWarParticipant1()) {
				war.setWarParticipant1(WarParticipant.createWarParticipant(nation, nation.getUUID(), towns));			
			}
			if(winner == war.getWarParticipant2()) {
				war.setWarParticipant2(WarParticipant.createWarParticipant(nation, nation.getUUID(), towns));
			}
			return nation;
		} catch (NotInWarException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
    
}