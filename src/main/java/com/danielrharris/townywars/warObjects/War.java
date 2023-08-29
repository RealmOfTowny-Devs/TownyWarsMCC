package com.danielrharris.townywars.warObjects;

import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.WarManager;
import com.danielrharris.townywars.warObjects.WarParticipant.TownOrNationNotFoundException;
import com.palmergames.bukkit.towny.TownyUniverse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class War implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6100034513248686580L;
	private WarParticipant participant1, participant2;
	private Rebellion rebelwar;
	private UUID warUUID;

	public War(WarParticipant participant1, WarParticipant participant2, Rebellion rebellion) throws Exception {
		this.participant1 = participant1;
		this.participant2 = participant2;
		this.rebelwar = rebellion;
		this.setUuid(UUID.randomUUID());
	}

	public War(Nation nat, Nation onat) throws Exception {
		this(WarParticipant.createWarParticipant(nat), WarParticipant.createWarParticipant(onat), null);
	}
	
	public War(Nation nat, Town town) throws Exception {
		this(WarParticipant.createWarParticipant(nat), WarParticipant.createWarParticipant(town), null);
	}
	
	public War(Town town, Nation nat) throws Exception {
		this(WarParticipant.createWarParticipant(town), WarParticipant.createWarParticipant(nat), null);
	}
	
	public War(Town town, Town oTown) throws Exception {
		this(WarParticipant.createWarParticipant(town), WarParticipant.createWarParticipant(oTown), null);
	}
	
	public War(Nation nat, Nation onat, Rebellion rebellion) throws Exception {
		this(WarParticipant.createWarParticipant(nat), WarParticipant.createWarParticipant(onat), rebellion);
	}
	
	public List<WarParticipant> getWarParticipants(){
		List<WarParticipant> list = new ArrayList<WarParticipant>();
		list.add(participant1);
		list.add(participant2);
		return list;
	}
	
	public Rebellion getRebellion() {
		return this.rebelwar;
	}

	public int getParticipantPoints(UUID id) throws ParticipantNotFoundException {
		if(participant1.getUuid() == id) {
			return participant1.getPoints();
		}else if (participant2.getUuid() == id) {
			return participant2.getPoints();
		}else {
			throw new ParticipantNotFoundException("Participant UUID " + id.toString() + " cannot be found!");
		}
	}
	
	public int getParticipantPoints(WarParticipant participant) throws ParticipantNotFoundException {
		if(participant1 == participant) {
			return participant1.getPoints();
		}else if (participant2 == participant) {
			return participant2.getPoints();
		}else {
			throw new ParticipantNotFoundException("Participant cannot be found!");
		}
	}
	
	public int getParticipantMaxPoints(WarParticipant participant) throws ParticipantNotFoundException {
		if(participant1 == participant) {
			return participant1.getMaxPoints();
		}else if (participant2 == participant) {
			return participant2.getMaxPoints();
		}else {
			throw new ParticipantNotFoundException("Participant cannot be found!");
		}
	}
	
	public int getParticipantMaxPoints(UUID id) throws ParticipantNotFoundException {
		if(participant1.getUuid() == id) {
			return participant1.getMaxPoints();
		}else if (participant2.getUuid() == id) {
			return participant2.getMaxPoints();
		}else {
			throw new ParticipantNotFoundException("Participant UUID " + id.toString() + " cannot be found!");
		}
	}

	public int getTownPoints(Town town) throws TownNotFoundException {
		try {
			for(Town t : participant1.getTownsList()) {
				if(t==town) {
					return participant1.getTownPoints(town);
				}
			}
			for(Town t : participant2.getTownsList()) {
				if(t==town) {
					return participant2.getTownPoints(town);
				}
			}
		} catch (TownOrNationNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new TownNotFoundException("Cannot find town");
	}
	
	public int getTownPoints(String town) throws TownNotFoundException {
		if(TownyUniverse.getInstance().hasTown(town)) {
			Town t = TownyUniverse.getInstance().getTown(town);
			return getTownPoints(t);
		}
		throw new TownNotFoundException("Cannot find town");
	}

	public UUID getEnemyUUIDFromUUID(UUID id) throws Exception {
			if (participant1.getUuid() == id) {
				return participant2.getUuid();
			}
			if (participant2.getUuid() == id) {
				return participant1.getUuid();
			}
		throw new Exception("War.getEnemy: Specified participant is not in war.");
	}
	
	public WarParticipant getEnemyParticipantFromUUID(UUID id) throws Exception {
		if (participant1.getUuid() == id) {
			return participant2;
		}
		if (participant2.getUuid() == id) {
			return participant1;
		}
	throw new Exception("War.getEnemy: Specified participant is not in war.");
}

	public WarParticipant getEnemy(WarParticipant participant) throws Exception {
		if (participant1 == participant) {
			return participant2;
		}
		if (participant2 == participant) {
			return participant1;
		}
	    throw new Exception("War.getEnemy: Specified participant is not in war.");
	}
	
	
	
	//charge town points. Write mini functions to handle each scenario and charge these points!!!
	///// MAAAAAKKKEEEE THIS WORK
	
	public void chargeTownPoints(WarParticipant participant, Town town, int points) {
		WarParticipant enemy = TownyWars.getInstance().getWarManager().getWarForParticipant(participant).getEnemy(participant);
		int value = getTownPoints(town) - points;
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
							removeTownFromNationAndAddToAnotherNation(town, nation, enemyNation);
							participant.removeTown(town);
							enemy.addNewTown(town);
							try {
								TownyWars.getInstance().getWarManager().save();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							broadcast(
									enemyNation.getName(),
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
							Nation newNation = upgradeTownToNation(enemy, enemyTown, town);
							broadcast(
									enemyTown.getName(),
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
						moveSoloTownIntoNation(town, enemyNation);
						participant.removeTown(town);
						enemy.addNewTown(town);
						broadcast(
								enemyNation.getName(),
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
						Nation newNation = upgradeTownToNation(enemy, enemyTown, town);
						broadcast(
								enemyTown.getName(),
								ChatColor.GREEN
										+ town.getName()
										+ " has been conquered and joined your Nation!");
						Bukkit.getServer().broadcastMessage("Upgraded " + enemyTown.getName() + " into nation: " + newNation.getName());
					}else {
						Bukkit.getServer().getConsoleSender().sendMessage("enemyTown is null");
					}
				}
				
			}

		} //figure out what happens with the towns/nations if war is ended - Probably have to fix WarManager first
		try {
			if (participant.getTownsMap().isEmpty()) {
				try {
						WarParticipant winner = getEnemy(participant);
						WarParticipant loser = participant;
						boolean endWarTransfersDone = false;
						if(loser.getType().equalsIgnoreCase("nation")) {
							Nation winnerNation = TownyUniverse.getInstance().getNation(winner.getUuid());
							if(winnerNation!=null) {
								for(Rebellion r : TownyWars.getInstance().getWarManager().getActiveRebellions()){
									if(r.getRebelnation() == winnerNation){
										winnerNation.getCapital().collect(winnerNation.getAccount().getHoldingBalance());
										winnerNation.pay(winnerNation.getAccount().getHoldingBalance(), "You are disbanded. You don't need money.");
										endWarTransfersDone = true;
										break;
									}
								}
							}		
						}					
						if(!endWarTransfersDone){
							winner.collect(loser.getHoldingBalance());
							looser.pay(looser.getHoldingBalance(), "Conquered. Tough luck!");
						}
						TownyWars.getInstance().getWarManager().endWar(winner, loser, false);

				} catch (Exception ex) {
					Logger.getLogger(War.class.getName()).log(Level.SEVERE, null,
							ex);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			TownyWars.getInstance().getWarManager().save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static void broadcast(String participant, String message) {
		if(TownyUniverse.getInstance().hasTown(participant)){
			Town town = TownyUniverse.getInstance().getTown(participant);
			if(town.hasNation()) {
				try {
					Nation n = town.getNation();
					for (Resident re : n.getResidents()) {
						Player plr = Bukkit.getPlayer(re.getName());
						if (plr != null) {
							plr.sendMessage(message);
						}
					}
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}else {
				for (Resident re : town.getResidents()) {
					Player plr = Bukkit.getPlayer(re.getName());
					if (plr != null) {
						plr.sendMessage(message);
					}
				}
			}	    		
		}
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
	
	public Nation upgradeTownToNation(WarParticipant winner, Town town, Town townToAdd) {
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
		towns.put(townToAdd.getUUID(), WarParticipant.getTownMaxPoints(townToAdd));
		if(winner == participant1) {
			participant1 = WarParticipant.createWarParticipant(nation, nation.getUUID(), towns);
		}
		if(winner == participant2) {
			participant2 = WarParticipant.createWarParticipant(nation, nation.getUUID(), towns);
		}
		return nation;
	}
	
	public WarParticipant findWarParticipantByTown(Town town){
		if(participant1.getTownsList().contains(town))
			return participant1;
		if(participant2.getTownsList().contains(town))
			return participant2;
		return null;
	}

	public UUID getUuid() {
		return warUUID;
	}

	public void setUuid(UUID warUUID) {
		this.warUUID = warUUID;
	}
	
	/** Read the object from Base64 string. */
	public static War decodeWar(String s) throws IOException, ClassNotFoundException {
	    byte [] data = Base64.getDecoder().decode( s );
	    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
	    War o  = (War) ois.readObject();
	    ois.close();
	    return o;
	}

	/** Write the object to a Base64 string. */
	public String encodeWar() throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream( baos );
	    oos.writeObject( this );
	    oos.close();
	    return Base64.getEncoder().encodeToString(baos.toByteArray()); 
	}

	@SuppressWarnings("serial")
	public class ParticipantNotFoundException extends Exception{
		public ParticipantNotFoundException(String errorMessage) {
	        super(errorMessage);
	    }
	}
	
	@SuppressWarnings("serial")
	public class TownNotFoundException extends Exception{
		public TownNotFoundException(String errorMessage) {
	        super(errorMessage);
	    }
	}
}