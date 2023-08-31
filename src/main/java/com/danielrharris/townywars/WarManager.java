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
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarManager
{

	private Set<War> activeWars;
	private Set<WarParticipant> requestedPeace;
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
  
  	public Set<War> getWars()
  	{
  		return activeWars;
  	}
  	
  	public WarParticipant findParticipant(Town town) {
  		for (War w : activeWars) {
    	    for(WarParticipant part : w.getWarParticipants()) {
    	    	if(part.getTownsList().contains(town))
    	    		return part;
    	    }
    	}
  		return null;
  	}
  	
  	public WarParticipant findParticipant(Nation nation) {
  		for (War w : activeWars) {
    	    for(WarParticipant part : w.getWarParticipants()) {
    	    	for(Town town : nation.getTowns()) {
    	    		if(part.getTownsList().contains(town))
        	    		return part;
    	    	} 	    	
    	    }
    	}
  		return null;
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
  	
  	public boolean isAtWar(Town town) {
		if(getWarForTown(town)!=null)
			return true;
  		return false;
  	}
  	
  	public boolean isAtWar(Nation nation) {
  		for(Town town : nation.getTowns()) {
  			if(getWarForTown(town)!=null)
  				return true;
  		}
  		return false;
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
    
    public War getWarForNation(Nation nation)
    {
    	for(Town t : nation.getTowns()) {
			if(getWarForTown(t)!=null) {
				return getWarForTown(t);
			}
		}
    	return null;
    }
    
    public boolean isLocationAtWar(Location loc) {
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
    
    public boolean isLocationAtWar(Block block) {
    	return isLocationAtWar(block.getLocation());
    }
    
    public WarParticipant getWarParticipantFromLocation(Location loc) {
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
    
    public WarParticipant getWarParticipantFromLocation(Block block) {
    	return getWarParticipantFromLocation(block.getLocation());
    }
    
    public War getWarAtLocation(Location loc) {
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
    
    public War getWarAtLocation(Block block) {
    	return getWarAtLocation(block.getLocation());
    }
    
    public WarParticipant createWarParticipant(Object object) throws AlreadyAtWarException
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
    
    public War createWar(WarParticipant participant1, WarParticipant participant2, CommandSender cs, Rebellion r) {
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
	    		this.save();
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
  
    public War createWar(Nation nat, Nation onat, CommandSender cs) throws AlreadyAtWarException{
    	return createWar(createWarParticipant(nat), createWarParticipant(onat), cs, null);
    }
  
    public War createWar(Nation nat, Nation onat, CommandSender cs, Rebellion r) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(nat), createWarParticipant(onat), cs, r);
    }
    
    public War createWar(Nation nat, Town town, CommandSender cs, Rebellion r) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(nat), createWarParticipant(town), cs, r);
    }
    
    public War createWar(Nation nat, Town town, CommandSender cs) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(nat), createWarParticipant(town), cs, null);
    }
    
    public War createWar(Town town, Nation nation, CommandSender cs, Rebellion r) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(town), createWarParticipant(nation), cs, r);
    }
    
    public War createWar(Town town, Nation nation, CommandSender cs) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(town), createWarParticipant(nation), cs, null);
    }
    
    public War createWar(Town town, Town otherTown, CommandSender cs, Rebellion r) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(town), createWarParticipant(otherTown), cs, r);
    }
    
    public War createWar(Town town, Town otherTown, CommandSender cs) throws AlreadyAtWarException
    {
    	return createWar(createWarParticipant(town), createWarParticipant(otherTown), cs, null);
    }
  
    public void endWar(WarParticipant winner, WarParticipant loser, boolean peace)
    {
    	if(!winner.getTownsList().isEmpty() && winner.getTownsList()!=null)
	    	for (Town t : winner.getTownsList()) {
	    		t.setPVP(false);
	    	}
    	if(!loser.getTownsList().isEmpty() && loser.getTownsList()!=null)
	    	for (Town t : loser.getTownsList()) {
	    		t.setPVP(false);
	    	}
    	War war = this.getWarForParticipant(winner);
    	if(peace) {
    		requestedPeace.remove(loser);
        	War.broadcast(winner, ChatColor.GREEN + "You are now at peace!");
        	War.broadcast(loser, ChatColor.GREEN + "You are now at peace!");
        	if(war.isRebelWar()) {
        		Rebellion rebellion = this.getWarForParticipant(winner).getRebellion();
        		rebellion.lost();
        	}
    	}else {
    		if(war.isRebelWar()) {
        		Rebellion rebellion = this.getWarForParticipant(winner).getRebellion();
        		if(rebellion!=null) {
            		//rebels win
                	if(winner == rebellion.getRebelWarParticipant()){
                		War.broadcast(loser, ChatColor.RED + winner.getName() + " won the rebellion and are now free!");
                		War.broadcast(winner, ChatColor.GREEN + winner.getName() + " won the rebellion and are now free!");
                		rebellion.success();
                	}      	
                	try {
    					if (winner == war.getEnemy(rebellion.getRebelWarParticipant()))
    					{
    						War.broadcast(loser, ChatColor.RED + winner.getName() + " won the rebellion and are now back under control of their mother nation!");
    						War.broadcast(winner, ChatColor.GREEN + loser.getName() + " lost the rebellion and are now back under control of their mother nation!");
    						rebellion.lost();
    					}
    				} catch (Exception e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
            	}
        	}else {
        		loser.pay(loser.getHoldingBalance(), winner);
        		War.broadcast(loser, ChatColor.RED + winner.getName() + " has won the war and your " + loser.getType() + " is gone!");
				War.broadcast(winner, ChatColor.GREEN + loser.getName() + " has lost the war and has been totally annihilated!");
        	}
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
  
    public boolean hasBeenOffered(War ww, WarParticipant participant)
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
    	if(!TownyWars.getInstance().getWarManager().isLocationAtWar(loc))
    		return false;
		try {
			WarParticipant part = TownyWars.getInstance().getWarManager().getWarParticipantFromLocation(loc);
	    	WarParticipant enemyPart = (TownyWars.getInstance().getWarManager().getWarAtLocation(loc)).getEnemy(part);
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
    	for(War w : TownyWars.getInstance().getWarManager().activeWars) {
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
    
    @SuppressWarnings("serial")
	public class AlreadyAtWarException extends Exception{
		public AlreadyAtWarException(String errorMessage) {
	        super(errorMessage);
	    }
	}
    
}