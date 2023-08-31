package com.danielrharris.townywars.warObjects;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.WarManager;
import com.danielrharris.townywars.exceptions.Exceptions.ParticipantNotFoundException;
import com.danielrharris.townywars.exceptions.Exceptions.TownNotFoundException;
import com.danielrharris.townywars.exceptions.Exceptions.TownOrNationNotFoundException;
import com.danielrharris.townywars.exceptions.Exceptions.NotInWarException;
import com.palmergames.bukkit.towny.TownyUniverse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

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
	
	public boolean isRebelWar() {
		if(getRebellion()!=null)
			return true;
		return false;
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

	public WarParticipant getEnemy(WarParticipant participant) throws NotInWarException {
		if (participant1 == participant) {
			return participant2;
		}
		if (participant2 == participant) {
			return participant1;
		}
	    throw new NotInWarException("Specified participant is not in war.");
	}
		
	public void chargeTownPoints(WarParticipant participant, Town town, int points) {
		try {
			TownyWars.getInstance().getWarManager();
			WarParticipant enemy = WarManager.getWarForParticipant(participant).getEnemy(participant);
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
								WarManager.removeTownFromNationAndAddToAnotherNation(town, nation, enemyNation);
								participant.removeTown(town);
								enemy.addNewTown(town);
								try {
									TownyWars.getInstance().getWarManager().save();
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
        } catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage("Error War.java chargePoints");
		}			
		//war is over
		
		if (participant.getTownsMap().isEmpty()) {
			try {
					WarManager.endWar(getEnemy(participant), participant, false);
			} catch (Exception ex) {
				Logger.getLogger(War.class.getName()).log(Level.SEVERE, null,
						ex);
			}
		}
		
		try {
			TownyWars.getInstance().getWarManager().save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public WarParticipant getWarParticipant1() {
		return participant1;
	}
	
	public WarParticipant getWarParticipant2() {
		return participant2;
	}
	
	public void setWarParticipant1(WarParticipant part) {
		participant1 = part;
	}
	
	public void setWarParticipant2(WarParticipant part) {
		participant2 = part;
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
}