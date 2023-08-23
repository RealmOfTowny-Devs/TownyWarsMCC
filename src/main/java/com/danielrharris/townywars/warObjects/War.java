package com.danielrharris.townywars.warObjects;

import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.danielrharris.townywars.WarManager;
import com.danielrharris.townywars.warObjects.WarParticipant.TownOrNationNotFoundException;
import com.palmergames.bukkit.towny.TownyUniverse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//import main.java.com.danielrharris.townywars.War.MutableInteger;

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

	public War(WarParticipant participant1, WarParticipant participant2, Rebellion rebellion) throws Exception {
		this.participant1 = participant1;
		this.participant2 = participant2;
		this.rebelwar = rebellion;
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
	
	public War(Nation nat, Town town, Rebellion rebellion) throws Exception {
		this(WarParticipant.createWarParticipant(nat), WarParticipant.createWarParticipant(town), rebellion);
	}
	
	public War(Town town, Nation nat, Rebellion rebellion) throws Exception {
		this(WarParticipant.createWarParticipant(town), WarParticipant.createWarParticipant(nat), rebellion);
	}
	
	public War(Town town, Town oTown, Rebellion rebellion) throws Exception {
		this(WarParticipant.createWarParticipant(town), WarParticipant.createWarParticipant(oTown), rebellion);
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
	public String encodeWarToString() throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream( baos );
	    oos.writeObject( this );
	    oos.close();
	    return Base64.getEncoder().encodeToString(baos.toByteArray()); 
	}
	
	public Rebellion getRebellion() {
		return this.rebelwar;
	}

	public int getParticipantPoints(String participant) throws ParticipantNotFoundException {
		if(participant1.getName().equalsIgnoreCase(participant)) {
			return participant1.getPoints();
		}else if (participant2.getName().equalsIgnoreCase(participant)) {
			return participant2.getPoints();
		}else {
			throw new ParticipantNotFoundException("Participant " + participant + " cannot be found!");
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

	public String getEnemy(String participant) throws Exception {
			if (participant1.getName() == participant) {
				return participant2.getName();
			}
			if (participant2.getName() == participant) {
				return participant1.getName();
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
	
	
	public void chargeTownPoints(Town town, double i) {
		double value = towns.get(town) - i;
		if(value > 0){
			towns.replace(town, value);
		}
		if (value <= 0) {
			try {
				if(nnation.getTowns().size() > 1 && nnation.getCapital() == town){
					if(nnation.getTowns().get(0) != town){
						nnation.setCapital(nnation.getTowns().get(0));
					}else{
						nnation.setCapital(nnation.getTowns().get(1));
					}
				}
					
					
				towns.remove(town);
				Nation nation = WarManager.getWarForNation(nnation).getEnemy(nnation);
				removeNationPoint(nnation);
				addNationPoint(nation, town);
				try {	
						WarManager.townremove = town;
						nnation.removeTown(town);
				} catch (Exception ex) {
				}
				nation.addTown(town);
				town.setNation(nation);
				TownyUniverse.getDataSource().saveNation(nation);
				TownyUniverse.getDataSource().saveNation(nnation);
				try {
					WarManager.save();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				broadcast(
						nation,
						ChatColor.GREEN
								+ town.getName()
								+ " has been conquered and joined your nation in the war!");
			} catch (Exception ex) {
				Logger.getLogger(War.class.getName()).log(Level.SEVERE, null,
						ex);
			}
		}
		try {
			if (this.getNationPoints(nnation) <= 0) {
				try {
						Nation winner = getEnemy(nnation);
						Nation looser = nnation;
						boolean endWarTransfersDone = false;
						for(Rebellion r : Rebellion.getAllRebellions()){
							if(r.getRebelnation() == winner){
								winner.getCapital().collect(winner.getHoldingBalance());
								winner.pay(winner.getHoldingBalance(), "You are disbanded. You don't need money.");
								endWarTransfersDone = true;
								break;
							}
						}
						
						if(!endWarTransfersDone){
							winner.collect(looser.getHoldingBalance());
							looser.pay(looser.getHoldingBalance(), "Conquered. Tough luck!");
						}
						WarManager.endWar(winner, looser, false);

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
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void chargeNationTownPoints(Nation nnation, Town town, int i) {
		int value = towns.get(town) - i;
		if(value > 0){
			towns.replace(town, value);
		}
		if (value <= 0) {
			try {
				if(nnation.getTowns().size() > 1 && nnation.getCapital() == town){
					if(nnation.getTowns().get(0) != town){
						nnation.setCapital(nnation.getTowns().get(0));
					}else{
						nnation.setCapital(nnation.getTowns().get(1));
					}
				}				
				if(TownyUniverse.getInstance().hasNation(WarManager.getWarForNation(nnation).getEnemy(nnation.getName()))) {
					towns.remove(town);
					Nation nation = TownyUniverse.getInstance().getNation(WarManager.getWarForNation(nnation).getEnemy(nnation.getName()));
					removeNationPoint(nnation);
					addNationPoint(nation, town);
					
					nation.addTown(town);
					town.setNation(nation);
					try {
						WarManager.save();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					broadcast(
							nation,
							ChatColor.GREEN
									+ town.getName()
									+ " has been conquered and joined your nation in the war!");
				}			
			} catch (Exception ex) {
				Logger.getLogger(War.class.getName()).log(Level.SEVERE, null,
						ex);
			}
		}
		try {
			if (this.getNationPoints(nnation) <= 0) {
				try {
						Nation winner = getEnemy(nnation);
						Nation looser = nnation;
						boolean endWarTransfersDone = false;
						for(Rebellion r : Rebellion.getAllRebellions()){
							if(r.getRebelnation() == winner){
								winner.getCapital().collect(winner.getHoldingBalance());
								winner.pay(winner.getHoldingBalance(), "You are disbanded. You don't need money.");
								endWarTransfersDone = true;
								break;
							}
						}
						
						if(!endWarTransfersDone){
							winner.collect(looser.getHoldingBalance());
							looser.pay(looser.getHoldingBalance(), "Conquered. Tough luck!");
						}
						WarManager.endWar(winner, looser, false);

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
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	///// MAAAAAKKKEEEE THIS WORK
	
	public void chargePoints(String township, int points) {
		if(TownyUniverse.getInstance().hasTown(township)) {
			Town town = TownyUniverse.getInstance().getTown(township);
			int value = getParticipantPoints(township) - points;
			if(value > 0){
				towns.replace(town, value);
			}
			if (value <= 0) { // The charged town has lost the war. If to a nation, the nation gains the town. If to another town, the other town gains the contents of their treasury
				towns.remove(town);
			    if(town.hasNation()) { //town has a nation
				    try {
				    	Nation nation1 = town.getNation();
				    	try {
				    		if(TownyUniverse.getInstance().hasNation(getEnemy(township))) {
				    			Nation nation2 = TownyUniverse.getInstance().getNation(getEnemy(township));
				    			//both are a nation
				    		}else { // enemy winner is a town. Merge town into nation but make the towns mayor new king of the nation
							
				    		}
				    	} catch (Exception e) {
				    		// TODO Auto-generated catch block
				    		e.printStackTrace();
				    	}
				    } catch (NotRegisteredException e) {
				    	// TODO Auto-generated catch block
				    	e.printStackTrace();
				    }
			    }else {  //town being charged points does not have a nation.. Therefore their totals points are all they have and losing their town loses their war.
					try {
						if(TownyUniverse.getInstance().hasTown(getEnemy(township))) {
							Town oTown = TownyUniverse.getInstance().getTown(getEnemy(township));
							if(oTown.hasNation()) { //winning participant is a nation   Town vs Nation ---> Nation Victory
								Nation nation = oTown.getNation();
								try {	
										WarManager.townremove = town;
								} catch (Exception ex) {
								}
								nation.addTown(town);
								town.setNation(nation);
								TownyUniverse.getInstance().getDataSource().saveTown(town);
								TownyUniverse.getInstance().getDataSource().saveNation(nation);								
								try {
									WarManager.save();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								broadcast(
										nation.getName(),
										ChatColor.GREEN
												+ town.getName()
												+ " has been conquered and joined your nation in the war!");
								broadcast(
										town.getName(),
										ChatColor.RED
												+ nation.getName()
												+ " has conquered your town and your town has joined their nation!");
								WarManager.endWar(winner, looser, false);
							}else { //winning town does not have nation, so winning town merges the losing town into its own - Town Vs. Town
								double winnings = town.getAccount().getHoldingBalance();
								oTown.getAccount().setBalance(oTown.getAccount().getHoldingBalance() + winnings, "Won the War!");
								oTown.addBonusBlocks(town.getMaxTownBlocks());
								town.getAccount().setBalance(0, "Lost the War!");							
								TownyUniverse.getInstance().getDataSource().mergeTown(oTown, town);
								broadcast(
										oTown.getName(),
										ChatColor.GREEN
												+ town.getName()
												+ " has been conquered and their loot has been plundered!");
								broadcast(
										town.getName(),
										ChatColor.RED
												+ oTown.getName()
												+ " has conquered your town and plundered your loot!");
								WarManager.endWar(winner, looser, false);
							}
						}else { //winning participant is a nation   Town vs Nation ---> Nation Victory
							Nation nation = TownyUniverse.getInstance().getNation(getEnemy(township));
							try {	
									WarManager.townremove = town;
							} catch (Exception ex) {
							}
							nation.addTown(town);
							town.setNation(nation);
							TownyUniverse.getInstance().getDataSource().saveTown(town);
							TownyUniverse.getInstance().getDataSource().saveNation(nation);
							try {
								WarManager.save();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							broadcast(
									nation.getName(),
									ChatColor.GREEN
											+ town.getName()
											+ " has been conquered and joined your nation in the war!");
							broadcast(
									town.getName(),
									ChatColor.RED
											+ nation.getName()
											+ " has conquered your town and your town has joined their nation!");
							WarManager.endWar(winner, looser, false);
						}
					} catch (NotRegisteredException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (AlreadyRegisteredException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		}else {   // participant is a nation, charge the nation points and check to see if they lose town
			
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
	
	public void mergeTowns(Town winningTown, Town losingTown) {
		double winnings = losingTown.getAccount().getHoldingBalance();
		winningTown.getAccount().setBalance(winningTown.getAccount().getHoldingBalance() + winnings, "Won the War!");
		winningTown.addBonusBlocks(losingTown.getMaxTownBlocks());
		losingTown.getAccount().setBalance(0, "Lost the War!");							
		TownyUniverse.getInstance().getDataSource().mergeTown(winningTown, losingTown);
		TownyUniverse.getInstance().getDataSource().saveTown(winningTown);
		TownyUniverse.getInstance().getDataSource().removeTown(losingTown);
		TownyUniverse.getInstance().getDataSource().saveTown(losingTown);
		Bukkit.getServer().getConsoleSender().sendMessage("Merging " + losingTown.getName() + " into " + winningTown.getName());		
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