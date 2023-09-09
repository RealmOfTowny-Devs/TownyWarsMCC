package com.danielrharris.townywars;

import com.danielrharris.townywars.warObjects.Rebellion;
import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;
import com.danielrharris.townywars.exceptions.Exceptions.NotInWarException;
import com.danielrharris.townywars.exceptions.Exceptions.TownNotFoundException;
import com.danielrharris.townywars.exceptions.Exceptions.TownOrNationNotFoundException;
import com.danielrharris.townywars.events.PeaceAchievedEvent;
import com.danielrharris.townywars.events.WarEndEvent;
import com.danielrharris.townywars.events.WarStartEvent;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class WarManager
{

	private static Set<War> activeWars = new HashSet<War>();
	private static Set<Rebellion> plannedRebellions = new HashSet<Rebellion>();
	private static Set<WarParticipant> requestedPeace = new HashSet<WarParticipant>();
	public Town townremove;
	
	//private static final int SAVING_VERSION = 1;
  
	public WarManager() throws Exception {
		activeWars = TownyWars.getInstance().getDataManager().loadWars();
		plannedRebellions = TownyWars.getInstance().getDataManager().loadRebellions();
	}
  	
  	public static Set<War> getWars()
  	{
  		return WarManager.activeWars;
  	}
  	
  	public static War getWar(UUID war) throws NotInWarException {
  		for(War w : getWars()) {
  			if(w.getUuid() == war) {
  				return w;
  			}
  		}
  		throw new NotInWarException("War UUID " + war.toString() + " cannot be found!");
  	}
  	
  	public static War getWar(WarParticipant part) throws NotInWarException {
  		for (War w : activeWars) {
  			if(w.getWarParticipants().contains(part)) {
  				return w;
  			}
  		}
  		throw new NotInWarException("Specified participant is not in war.");
  	}
  	
  	public static War getWar(Town town) throws NotInWarException {
  		for (War w : activeWars) {
  			for(WarParticipant part : w.getWarParticipants()) {
  				for(Town t : part.getTownsList())
  					if(town == t)
  						return w;
  			}
  		}
  		throw new NotInWarException("Specified town is not in war.");
  	}
  	
  	public static War getWar(Nation nation) throws NotInWarException{
  		for(Town t : nation.getTowns())	{
  			for (War w : activeWars) {
  				for(WarParticipant part : w.getWarParticipants()) {
  					for(Town town : part.getTownsList()) {
  						if(t == town)
  							return w;
  					}
  				}
  			}
  		}
  		throw new NotInWarException("Specified nation is not in war.");
  	}
  	
  	public static WarParticipant getEnemy(WarParticipant  part) throws NotInWarException {
  		return getWar(part).getEnemy(part);
  	}
  	
  	public static WarParticipant getEnemy(Nation nation) throws NotInWarException {
  		if(getWarParticipant(nation)!=null) {
  			WarParticipant part = getWarParticipant(nation);
  			return WarManager.getEnemy(part);
  		}
  		throw new NotInWarException("Specified nation is not in war.");
  	}
  	
  	public static WarParticipant getEnemy(Town town) throws NotInWarException {
  		if(getWarParticipant(town)!=null) {
  			WarParticipant part = getWarParticipant(town);
  			return WarManager.getEnemy(part);
  		}
  		throw new NotInWarException("Specified nation is not in war.");
  	}
  	
  	public static WarParticipant getWarParticipant(UUID id) throws NotInWarException{
  		for (War w : activeWars) {
    	    for(WarParticipant part : w.getWarParticipants()) {
    	    	if(part.getUuid() == id)
    	    		return part;
    	    }
    	}
  		throw new NotInWarException("Specified participant is not in war.");
  	}
  	
  	public static WarParticipant getWarParticipant(Town town) throws NotInWarException{
  		for (War w : activeWars) {
    	    for(WarParticipant part : w.getWarParticipants()) {
    	    	if(part.getTownsList().contains(town))
    	    		return part;
    	    }
    	}
  		throw new NotInWarException("Specified town is not in war.");
  	}
  	
  	public static WarParticipant getWarParticipant(Nation nation) throws NotInWarException {
  		for(Town town : nation.getTowns()) {
    		if(getWarParticipant(town)!=null)
    			return getWarParticipant(town);
    	}
  		throw new NotInWarException("Specified nation is not in war.");
  	}
  	
  	public static WarParticipant getWarParticipant(Resident resident) throws NotInWarException {
  		try {
			if(getWarParticipant(resident.getTown())!=null)
				return getWarParticipant(resident.getTown());				
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		throw new NotInWarException("Specified participant is not in war.");
  	}
  	
  	public static boolean isAtWar(Town town) {
		try {
			getWar(town);
			return true;
		} catch (NotInWarException e) {
			return false;
		}
  	}
  	
  	public static boolean isAtWar(Player player) {
		for(War w : getWars()) {
			for(WarParticipant part : w.getWarParticipants()) {
				for(Resident r : part.getResidents()) {
					if(getPlayerFromResident(r) == player)
						return true;
				}
			}
		}
		return false;
  	}
  	
  	public static boolean isAtWar(Resident resident) {
		for(War w : getWars()) {
			for(WarParticipant part : w.getWarParticipants()) {
				for(Resident r : part.getResidents()) {
					if(r == resident)
						return true;
				}
			}
		}
		return false;
  	}
  	
  	public static boolean isAtWar(Nation nation) {
  		try {
			getWar(nation);
			return true;
		} catch (NotInWarException e) {
			return false;
		}
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
    
    public static WarParticipant createWarParticipant(Object object) throws AlreadyAtWarException, NotRegisteredException
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
    
    public static War createWar(WarParticipant participant1, WarParticipant participant2, Rebellion r){  	
		try {
			War war = new War(participant1, participant2, r);
			WarStartEvent event = new WarStartEvent(war);
			Bukkit.getServer().getPluginManager().callEvent(event);
			if(!event.isCancelled()) {
				activeWars.add(war);
		    	for (Resident re : participant1.getResidents())
		    	{
		    		Player plr = re.getPlayer();
		            if (plr != null) {
		            	plr.sendMessage(ChatColor.RED + "Your town is now at war with " + participant2.getName() + "!");
		            }
		    	}
		    	for (Resident re : participant2.getResidents())
		    	{
		    		Player plr = re.getPlayer();
		            if (plr != null) {
		            	plr.sendMessage(ChatColor.RED + "Your town is now at war with " + participant1.getName() + "!");
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
		    		TownyWars.getInstance().getDataManager().saveWars();
		    	} catch (Exception e) {
		    		// TODO Auto-generated catch block
		    		e.printStackTrace();
		    	}
		        return war;
			}			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
    }
  
    public static War createWar(Nation nat, Nation onat) throws AlreadyAtWarException, NotRegisteredException{
    	return createWar(createWarParticipant(nat), createWarParticipant(onat), null);
    }
  
    public static War createWar(Nation nat, Nation onat, Rebellion r) throws AlreadyAtWarException, NotRegisteredException
    {
    	return createWar(createWarParticipant(nat), createWarParticipant(onat), r);
    }
    
    public static War createWar(Nation nat, Town town, Rebellion r) throws AlreadyAtWarException, NotRegisteredException
    {
    	return createWar(createWarParticipant(nat), createWarParticipant(town), r);
    }
    
    public static War createWar(Nation nat, Town town) throws AlreadyAtWarException, NotRegisteredException
    {
    	return createWar(createWarParticipant(nat), createWarParticipant(town), null);
    }
    
    public static War createWar(Town town, Nation nation, Rebellion r) throws AlreadyAtWarException, NotRegisteredException
    {
    	return createWar(createWarParticipant(town), createWarParticipant(nation), r);
    }
    
    public static War createWar(Town town, Nation nation) throws AlreadyAtWarException, NotRegisteredException
    {
    	return createWar(createWarParticipant(town), createWarParticipant(nation), null);
    }
    
    public static War createWar(Town town, Town otherTown, Rebellion r) throws AlreadyAtWarException, NotRegisteredException
    {
    	return createWar(createWarParticipant(town), createWarParticipant(otherTown), r);
    }
    
    public static War createWar(Town town, Town otherTown) throws AlreadyAtWarException, NotRegisteredException
    {
    	return createWar(createWarParticipant(town), createWarParticipant(otherTown), null);
    }
  
    public static void endWar(WarParticipant winner, WarParticipant loser, boolean peace) throws NotInWarException
    {  	
    	War war = WarManager.getWar(winner);
    	WarEndEvent event = new WarEndEvent(war, winner, loser);
    	Bukkit.getServer().getPluginManager().callEvent(event);
    	if(!winner.getTownsList().isEmpty() && winner.getTownsList()!=null)
    		for (Town t : winner.getTownsList()) {
    			t.setPVP(false);
    		}
    	if(!loser.getTownsList().isEmpty() && loser.getTownsList()!=null)
    		for (Town t : loser.getTownsList()) {
    			t.setPVP(false);
    		}
    	if(peace) {
    		PeaceAchievedEvent peaceEvent = new PeaceAchievedEvent(war);
    		Bukkit.getServer().getPluginManager().callEvent(peaceEvent);
    		requestedPeace.remove(loser);
    		requestedPeace.remove(winner);
    		if(war.isRebelWar()) {
    			Rebellion rebellion = WarManager.getWar(winner).getRebellion();
    			if(winner == rebellion.getRebelWarParticipant()) {
    				broadcast(loser, ChatColor.RED + winner.getName() + " won the rebellion and is now free!");
    				broadcast(winner, ChatColor.GREEN + loser.getName() + " surrendered the rebellion and you are now free!");
    			}else {
    				broadcast(loser, ChatColor.RED + winner.getName() + " accepted terms and you are now back under their control!");
    				broadcast(winner, ChatColor.GREEN + loser.getName() + " surrendered the rebellion and are now back under your control!");
    				rebellion.lost();
    			}
    		}else {
    			broadcast(winner, ChatColor.GREEN + "You are now at peace!");
    			broadcast(loser, ChatColor.GREEN + "You are now at peace!");
    		}
    	}else {
    		if(war.isRebelWar()) {
    			Rebellion rebellion = WarManager.getWar(winner).getRebellion();
    			if(rebellion!=null) {
    				//rebels win
    				if(winner == rebellion.getRebelWarParticipant()){
    					broadcast(loser, ChatColor.RED + winner.getName() + " won the rebellion and you are now in control of " + loser.getName() + "!");
    					broadcast(winner, ChatColor.GREEN + loser.getName() + " lost the rebellion and a new king has been crowned!");
    					rebellion.success();
    				}else {
    					broadcast(loser, ChatColor.RED + winner.getName() + " won the rebellion and you are now back under their control!");
    					broadcast(winner, ChatColor.GREEN + loser.getName() + " lost the rebellion and are now back under your control!");
    					rebellion.lost();
			    	}
    			}
    		}else {
    			WarManager.pay(loser.getHoldingBalance(), loser, winner);
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
    	}
    	TownyUniverse.getInstance().getDataSource().saveTowns();
		TownyUniverse.getInstance().getDataSource().saveNations();
		
		activeWars.remove(war);
		try {
			TownyWars.getInstance().getDataManager().saveWars();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void requestPeace(WarParticipant participant1, boolean admin) throws NotInWarException
	{
    	WarParticipant participant2 = WarManager.getEnemy(participant1);
		if(admin) {
	    	endWar(participant1, participant2, true);
	    	return;
	    }			
		
	    if (requestedPeace.contains(participant2)) {
	    	
	    	if(WarManager.canPay(TownyWars.getInstance().getConfigInstance().peaceCost, participant1) && WarManager.canPay(TownyWars.getInstance().getConfigInstance().peaceCost, participant1)) {
    			WarManager.chargeParticipant(TownyWars.getInstance().getConfigInstance().peaceCost, participant1);
    			WarManager.chargeParticipant(TownyWars.getInstance().getConfigInstance().peaceCost, participant2);
    			endWar(participant1, participant2, true);
	    	}else {
	    		Set<Resident> importants = new HashSet<Resident>();
	    		if(!WarManager.canPay(TownyWars.getInstance().getConfigInstance().peaceCost, participant1)) {
	    			importants.add(participant1.getLeader());
	    			for(Resident r : participant1.getAssistants())
	    				importants.add(r);
	    		}
                if(!WarManager.canPay(TownyWars.getInstance().getConfigInstance().peaceCost, participant2)) {
                	importants.add(participant2.getLeader());
	    			for(Resident r : participant2.getAssistants())
	    				importants.add(r);
	    		}
	    		for(Resident r : importants) {
	    			Player p = r.getPlayer();
	    			if(p!=null)
	    				if(p.isOnline())
	    					p.sendMessage("Y'all are too broke to pay the peace costs of $" + TownyWars.getInstance().getConfigInstance().peaceCost);
	    		}
	    	}
	    }else {
	    	requestedPeace.add(participant1);
	    	broadcast(participant1, "You and your residents has asked " + participant2.getName() + " for peace");
	    	broadcast(participant2, participant1.getName() + "has asked you and your residents for peace");
	    }
	}
  
    public static boolean peaceHasBeenOffered(WarParticipant participant)
    {
        try {
		    return requestedPeace.contains(WarManager.getEnemy(participant));
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
    
    public static Player getPlayerFromResident(Resident r) {
    	return r.getPlayer();
    }
    
    public static void broadcast(WarParticipant participant, String message) {
		for(Resident resident : participant.getResidents()) {
			Player player = resident.getPlayer();
			if(player!=null)
				if(player.isOnline())
					player.sendMessage(message);
		}
	}
    
    public static void broadcast(Town town, String message) {
		for(Resident resident : town.getResidents()) {
			Player player = resident.getPlayer();
			if(player!=null)
				if(player.isOnline())
					player.sendMessage(message);
		}
	}
    
    public static void broadcast(Nation nation, String message) {
		for(Resident resident : nation.getResidents()) {
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
	
	public static boolean canPay(double amount, WarParticipant participant) {
		if(participant.getHoldingBalance()>=amount)
			return true;
		return false;
	}
	
	public static void chargeParticipant(double amount, WarParticipant participant) {
		participant.setHoldingBalance(participant.getHoldingBalance()-amount);
	}
	
	public static boolean pay(double amount, WarParticipant payer, WarParticipant receiver) {
		if(payer.getType().equalsIgnoreCase("town")) {
			if(TownyUniverse.getInstance().hasTown(payer.getUuid())){
				Town town = TownyUniverse.getInstance().getTown(payer.getUuid());
				if(town.getAccount().getHoldingBalance()>=amount) {
					if(receiver.getType().equalsIgnoreCase("town")) {
						Town receiverTown = TownyUniverse.getInstance().getTown(receiver.getUuid());
						receiverTown.getAccount().setBalance(receiverTown.getAccount().getHoldingBalance() + amount, "Got paid from " + payer.getName() + " TownyWars!");
						town.getAccount().setBalance(town.getAccount().getHoldingBalance() - amount, "Paid " + receiver.getName() + " in TownyWars!");
						receiverTown.save();
					}else {
						Nation receiverNation = TownyUniverse.getInstance().getNation(receiver.getUuid());
						receiverNation.getAccount().setBalance(receiverNation.getAccount().getHoldingBalance() + amount, "Got paid from " + payer.getName() + " TownyWars!");
						town.getAccount().setBalance(town.getAccount().getHoldingBalance() - amount, "Paid " + receiver.getName() + " in TownyWars!");
						receiverNation.save();
					}
				}else {
					if(receiver.getType().equalsIgnoreCase("town")) {
						Town receiverTown = TownyUniverse.getInstance().getTown(receiver.getUuid());
						receiverTown.getAccount().setBalance(receiverTown.getAccount().getHoldingBalance() + town.getAccount().getHoldingBalance(), "Got paid from " + payer.getName() + " TownyWars!");
						town.getAccount().setBalance((double)0.00, "Paid " + receiver.getName() + " in TownyWars!");
						receiverTown.save();
					}else {
						Nation receiverNation = TownyUniverse.getInstance().getNation(receiver.getUuid());
						receiverNation.getAccount().setBalance(receiverNation.getAccount().getHoldingBalance() + town.getAccount().getHoldingBalance(), "Got paid from " + payer.getName() + " TownyWars!");
						town.getAccount().setBalance((double)0.00, "Paid " + receiver.getName() + " in TownyWars!");
						receiverNation.save();
					}			
					if(town.getMayor().getPlayer().isOnline())
						town.getMayor().getPlayer().sendMessage("Your town did not have enough money to pay the full amount, so you paid as much as you had and are now broke...");
				}			
				town.save();
				return true;
			}
			return false;
		}else {
			if(TownyUniverse.getInstance().hasNation(payer.getUuid())){
				Nation nation = TownyUniverse.getInstance().getNation(payer.getUuid());
			    if(nation.getAccount().getHoldingBalance()>=amount) {
					if(receiver.getType().equalsIgnoreCase("town")) {
						Town receiverTown = TownyUniverse.getInstance().getTown(receiver.getUuid());
						receiverTown.getAccount().setBalance(receiverTown.getAccount().getHoldingBalance() + amount, "Got paid from " + payer.getName() + " TownyWars!");
						nation.getAccount().setBalance(nation.getAccount().getHoldingBalance() - amount, "Paid " + receiver.getName() + " in TownyWars!");
						receiverTown.save();
					}else {
						Nation receiverNation = TownyUniverse.getInstance().getNation(receiver.getUuid());
						receiverNation.getAccount().setBalance(receiverNation.getAccount().getHoldingBalance() + amount, "Got paid from " + payer.getName() + " TownyWars!");
						nation.getAccount().setBalance(nation.getAccount().getHoldingBalance() - amount, "Paid " + receiver.getName() + " in TownyWars!");
						receiverNation.save();
					}
				}else {
					if(receiver.getType().equalsIgnoreCase("town")) {
						Town receiverTown = TownyUniverse.getInstance().getTown(receiver.getUuid());
						receiverTown.getAccount().setBalance(receiverTown.getAccount().getHoldingBalance() + nation.getAccount().getHoldingBalance(), "Got paid from " + payer.getName() + " TownyWars!");
						nation.getAccount().setBalance((double)0.00, "Paid " + receiver.getName() + " in TownyWars!");
						receiverTown.save();
					}else {
						Nation receiverNation = TownyUniverse.getInstance().getNation(receiver.getUuid());
						receiverNation.getAccount().setBalance(receiverNation.getAccount().getHoldingBalance() + nation.getAccount().getHoldingBalance(), "Got paid from " + payer.getName() + " TownyWars!");
						nation.getAccount().setBalance((double)0.00, "Paid " + receiver.getName() + " in TownyWars!");
						receiverNation.save();
					}			
					if(nation.getKing().getPlayer().isOnline())
						nation.getKing().getPlayer().sendMessage("Your nation did not have enough money to pay the full amount, so you paid as much as you had and are now broke...");
				}			
				nation.save();
				return true;
			}
		    return false;
		}
	}
	
	public static void chargeTownPoints(Town town, int points) throws NotInWarException, TownNotFoundException, TownOrNationNotFoundException {
		WarParticipant participant = WarManager.getWarParticipant(town);
		WarParticipant enemy = WarManager.getEnemy(participant);
		int value = WarManager.getTownPoints(town) - points;
		if(value > 0){
			participant.setTownPoints(town, value);
		}
		if (value <= 0) {
			if(participant.getType().equalsIgnoreCase("nation")) { // Participant is nation
				Nation nation = TownyUniverse.getInstance().getNation(participant.getUuid());
				if(nation!=null) {
					if(participant.getTownsList().size() > 1 && nation.getCapital() == town){
						if(nation.getTowns().get(0) != town){
							nation.setCapital(nation.getTowns().get(0));
						}else{
							nation.setCapital(nation.getTowns().get(1));
						}
					}
					if(enemy.getType().equalsIgnoreCase("nation")) { // enemy is a nation
						Nation enemyNation = TownyUniverse.getInstance().getNation(enemy.getUuid());
						if(enemyNation!=null) {
							WarManager.removeTownFromNationAndAddToAnotherNation(town, nation, enemyNation);
							participant.removeTown(town);
							enemy.addNewTown(town);
							try {
								TownyWars.getInstance().getDataManager().saveWars();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							WarManager.broadcast(
									enemy,
									ChatColor.GREEN
											+ town.getName()
											+ " has been conquered and joined your nation in the war!");
						}else {
							Bukkit.getServer().getConsoleSender().sendMessage("enemyNation is null");
						}						
					}else { //enemy is a town
						Town enemyTown = TownyUniverse.getInstance().getTown(enemy.getUuid());
						if(enemyTown!=null) {
							participant.removeTown(town);
							enemy.addNewTown(town);
							Nation newNation = WarManager.upgradeTownToNation(enemy, enemyTown, town);
							WarManager.broadcast(
									enemy,
									ChatColor.GREEN
											+ town.getName()
											+ " has been conquered and joined your Nation!");
							Bukkit.getServer().broadcastMessage("Upgraded " + enemyTown.getName() + " into nation: " + newNation.getName());
						}else {
							Bukkit.getServer().getConsoleSender().sendMessage("enemyTown is null");
						}
					}					
				}else {
					Bukkit.getServer().getConsoleSender().sendMessage("nation is null");
				}			
			}else { //participant is a town. Figure out what happens to their town. Since they are just a town, this is a total loss. Do the end of the war here also
				if(enemy.getType().equalsIgnoreCase("nation")) {   //enemy is nation
					Nation enemyNation = TownyUniverse.getInstance().getNation(enemy.getUuid());
					if(enemyNation!=null) {
						WarManager.moveSoloTownIntoNation(town, enemyNation);
						participant.removeTown(town);
						enemy.addNewTown(town);
						WarManager.broadcast(
								enemy,
								ChatColor.GREEN
										+ town.getName()
										+ " has been conquered and joined your Nation!");
						Bukkit.getServer().broadcastMessage("Moved " + town.getName() + " into " + enemyNation.getName());
					}
				}else { //enemy is town
					Town enemyTown = TownyUniverse.getInstance().getTown(enemy.getUuid());
					if(enemyTown!=null) {
						participant.removeTown(town);
						enemy.addNewTown(town);
						Nation newNation = WarManager.upgradeTownToNation(enemy, enemyTown, town);
						WarManager.broadcast(
								enemy,
								ChatColor.GREEN
										+ town.getName()
										+ " has been conquered and joined your Nation!");
						Bukkit.getServer().broadcastMessage("Upgraded " + enemyTown.getName() + " into nation: " + newNation.getName());				
					}else {
						Bukkit.getServer().getConsoleSender().sendMessage("enemyTown is null");
					}
				}
				
			}
		}	
	
		if (participant.getTownsMap().isEmpty()) {
			try {
					WarManager.endWar(getEnemy(participant), participant, false);
			} catch (Exception ex) {
				Logger.getLogger(War.class.getName()).log(Level.SEVERE, null,
						ex);
			}
		}
		
		try {
			TownyWars.getInstance().getDataManager().saveWars();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static int getTownPoints(Town town) throws TownNotFoundException, NotInWarException, TownOrNationNotFoundException {
		return getWarParticipant(town).getTownPoints(town);
	}
	
	public static int getTownPoints(String town) throws TownNotFoundException, TownOrNationNotFoundException, NotInWarException {
		if(TownyUniverse.getInstance().hasTown(town)) {
			Town t = TownyUniverse.getInstance().getTown(town);
			return getTownPoints(t);
		}
		throw new TownNotFoundException("Cannot find town");
	}
	
	public static int getTownPoints(UUID town) throws TownNotFoundException, TownOrNationNotFoundException, NotInWarException {
		if(TownyUniverse.getInstance().hasTown(town)) {
			Town t = TownyUniverse.getInstance().getTown(town);
			return getTownPoints(t);
		}
		throw new TownNotFoundException("Cannot find town");
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
			War war = getWar(winner);
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
			WarParticipant[] participants = war.getWarParticipantsAsArray();
			Set<WarParticipant> parts = new HashSet<WarParticipant>();
			if(winner == participants[0]) {
				parts.add(WarParticipant.createWarParticipant(nation, nation.getUUID(), towns));
				parts.add(WarManager.getEnemy(winner));
			}
			if(winner == participants[1]) {
				parts.add(WarParticipant.createWarParticipant(nation, nation.getUUID(), towns));
				parts.add(WarManager.getEnemy(winner));
			}
			if(!parts.isEmpty() && parts.size() == 2)
			    war.setWarParticipants(parts);
			return nation;
		} catch (NotInWarException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	
	public static Set<Rebellion> getPlannedRebellions() {
	    return plannedRebellions;
    }

    public void setPlannedRebellions(Set<Rebellion> plannedRebellions) {
	    WarManager.plannedRebellions = plannedRebellions;
    }
    
    public static void addPlannedRebellion(Rebellion plannedRebellion) {
    	Set<Rebellion> rebels = new HashSet<Rebellion>();
    	for(Rebellion r : plannedRebellions) {
    		rebels.add(r);
    	}
	    rebels.add(plannedRebellion);
	    TownyWars.getInstance().getWarManager().setPlannedRebellions(rebels);
    }
    
    public static boolean removePlannedRebellion(Rebellion plannedRebellion) {
    	if(plannedRebellions.contains(plannedRebellion)) {
    		Set<Rebellion> rebels = new HashSet<Rebellion>();
        	for(Rebellion r : plannedRebellions) {
        		rebels.add(r);
        	}
        	if(rebels.contains(plannedRebellion)) {
        		rebels.remove(plannedRebellion);
        		TownyWars.getInstance().getWarManager().setPlannedRebellions(rebels);
        		return true;
        	}
    	}
    	return false;
    }
    
    public static Rebellion getRebellion(String s) throws Exception{
		for(Rebellion r : plannedRebellions)
			if(r.getName().equals(s))
				return r;
		throw(new Exception("Rebellion not found!"));
    }
    
    public static Rebellion getRebellion(UUID id) throws Exception{
		for(Rebellion r : plannedRebellions)
			if(r.getUuid().equals(id))
				return r;
		throw(new Exception("Rebellion not found!"));
    }
    
}