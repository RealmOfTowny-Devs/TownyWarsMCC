package com.danielrharris.townywars.warObjects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.danielrharris.townywars.TownyWars;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

public class WarParticipant implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2699563764161267277L;
	private Map<UUID, Integer> towns;
	private int maxPoints;
	private String type;
	private UUID uuid;
    
    public static WarParticipant createWarParticipant(Object object) {
    	if(object instanceof Town) {
    		Town town = (Town)object;
    		if(town.hasNation()) {
    			try {
					return new WarParticipant(town.getNation());
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}else {
    			return new WarParticipant((Town)object);
    		}    			
    	}else if (object instanceof Nation) {
    		return new WarParticipant((Nation)object);
    	}else if(object instanceof String){
    		String s = (String)object;
    		if(TownyUniverse.getInstance().hasTown(s)){
    			Town town = TownyUniverse.getInstance().getTown(s);
    			if(town.hasNation()) {
        			try {
    					return new WarParticipant(town.getNation());
    				} catch (NotRegisteredException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
        		}else {
        			return new WarParticipant((Town)object);
        		}
    		}
    		if(TownyUniverse.getInstance().hasNation(s)) {
    			Nation nation = TownyUniverse.getInstance().getNation(s);
    			return new WarParticipant(nation);
    		}
    	}
		return null;
    }
    
    public static WarParticipant createWarParticipant(Nation nation, UUID uuid, Map<UUID, Integer> towns) {
    	WarParticipant part = new WarParticipant(nation);
    	part.setType("nation");
		part.setUuid(uuid);
		part.setMaxPoints(getNationMaxPoints(nation));
		part.towns = new HashMap<UUID, Integer>();
		for(UUID id : towns.keySet()) {
			part.towns.put(id, towns.get(id));
		}
		return part;
    }
    
    public static WarParticipant createWarParticipant(Town town, UUID uuid) {
    	WarParticipant part = new WarParticipant(town);
    	part.setType("town");
		part.setUuid(uuid);
		part.setMaxPoints(getTownMaxPoints(town));
		part.towns = new HashMap<UUID, Integer>();
		part.towns.put(town.getUUID(), getTownMaxPoints(town));
		return part;
    }
    
    private WarParticipant(Nation nation) {
		setType("nation");
		setUuid(nation.getUUID());
		setMaxPoints(getNationMaxPoints(nation));
		initializeTowns(nation.getTowns());
	}
	
    private WarParticipant(Town town) {
    	setType("town");
    	setUuid(town.getUUID());
    	setMaxPoints(getTownMaxPoints(town));
		List<Town> list = new ArrayList<Town>();
		list.add(town);
		initializeTowns(list);
	}
    
    private void initializeTowns(List<Town> towns) {
		Map<UUID, Integer> t = new HashMap<UUID, Integer>();
		for(Town town : towns) {
			t.put(town.getUUID(), getTownMaxPoints(town));
		}
		this.towns = t;
	}
    
    public static int getTownMaxPoints(Town town){
		double d = (town.getNumResidents()
				* TownyWars.getInstance().getConfigInstance().pPlayer) + (TownyWars.getInstance().getConfigInstance().pPlot
				* town.getTownBlocks().size());
		return Math.round((float)d);
	}
	
	private static int getNationMaxPoints(Nation nation) {
		int i = 0;
		for(Town town : nation.getTowns()) {
			i = i + getTownMaxPoints(town);
		}
		return i;
	}
	
	public Map<UUID, Integer> getTownsMap() {
		return this.towns;
	}

	public List<String> getTownsAsNames() {
		List<String> towns = new ArrayList<String>();
		for(UUID s : this.towns.keySet()) {			
			towns.add(TownyUniverse.getInstance().getTown(s).getName());
		}
		return towns;
	}

	public int getTownPoints(Town town) throws TownOrNationNotFoundException {
		if(towns.containsKey(town.getUUID())) {
			return this.towns.get(town.getUUID());
		}
		throw new TownOrNationNotFoundException("Can't find town in record");
	}
	
	public int getTownPoints(String town) throws TownOrNationNotFoundException {
		if(TownyUniverse.getInstance().hasTown(town)) {
			Town t = TownyUniverse.getInstance().getTown(town);
			return getTownPoints(t);
		}
		throw new TownOrNationNotFoundException("Can't find town in record");
	}
	
	public void setTownPoints(Town town, int points) throws TownOrNationNotFoundException {
		if(towns.containsKey(town.getUUID())) {
			this.towns.replace(town.getUUID(), points);
		}
		throw new TownOrNationNotFoundException("Can't find town in record");
	}
	
	public void setTownPoints(String town, int points) throws TownOrNationNotFoundException {
		if(TownyUniverse.getInstance().hasTown(town)) {
			Town t = TownyUniverse.getInstance().getTown(town);
			setTownPoints(t, points);
		}
		throw new TownOrNationNotFoundException("Can't find town in record");
	}
	
	public List<Town> getTownsList() {
		List<Town> t = new ArrayList<Town>();
		for(UUID s : this.towns.keySet()) {
			t.add(TownyUniverse.getInstance().getTown(s));
		}
		return t;
	}
	
	public void addNewTown(Town town) throws TownOrNationNotFoundException {
		if(!this.towns.containsKey(town.getUUID())) {
			this.towns.put(town.getUUID(), getTownMaxPoints(town));
		}
		throw new TownOrNationNotFoundException("Can't find town in record");
	}
	
	public void addNewTown(String town) throws TownOrNationNotFoundException {
		if(TownyUniverse.getInstance().hasTown(town)) {
			Town t = TownyUniverse.getInstance().getTown(town);
			addNewTown(t);
		}
		throw new TownOrNationNotFoundException("Can't find town in record");
	}
	
	public void removeTown(Town town) throws TownOrNationNotFoundException {
		if(this.towns.containsKey(town.getUUID())) {
			this.towns.remove(town.getUUID());
		}
		throw new TownOrNationNotFoundException("Can't find town in record");
	}
	
	public void removeTown(String town) throws TownOrNationNotFoundException {
		if(TownyUniverse.getInstance().hasTown(town)) {
			Town t = TownyUniverse.getInstance().getTown(town);
			removeTown(t);
		}
		throw new TownOrNationNotFoundException("Can't find town in record");
	}

	public int getPoints() {
		int points = 0;
		for(UUID id : this.towns.keySet()) {
			int i = towns.get(id);
			points = points + i;
		}
		return points;
	}
	
	public int getMaxPoints() {
		return maxPoints;
	}

	public void setMaxPoints(int maxPoints) {
		this.maxPoints = maxPoints;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public List<Resident> getResidents() {
		List<Resident> residents = new ArrayList<Resident>();
		for(Town t : getTownsList()) {
			for(Resident r : t.getResidents())
				residents.add(r);
		}
		return residents;
	}
	
	/** Read the object from Base64 string. */
	public static WarParticipant decodeFromString(String s) throws IOException, ClassNotFoundException {	                                                       
	    byte [] data = Base64.getDecoder().decode( s );
	    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
	    WarParticipant o  = (WarParticipant) ois.readObject();
	    ois.close();
	    return o;
	}

	/** Write the object to a Base64 string. */
	public String encodeToString() throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream( baos );
	    oos.writeObject( this );
	    oos.close();
	    return Base64.getEncoder().encodeToString(baos.toByteArray()); 
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public double getHoldingBalance() {
		if(getType().equalsIgnoreCase("town")) {
			return TownyUniverse.getInstance().getTown(getUuid()).getAccount().getHoldingBalance();
		}else {
		    return TownyUniverse.getInstance().getNation(getUuid()).getAccount().getHoldingBalance();	
		}	
	}
	
	public void setHoldingBalance(double balance) {
		if(getType().equalsIgnoreCase("town")) {
			Town town = TownyUniverse.getInstance().getTown(getUuid());
			town.getAccount().setBalance(balance, "TownyWars transaction");
			town.save();
		}else {
		    Nation nation = TownyUniverse.getInstance().getNation(getUuid());
		    nation.getAccount().setBalance(balance, "TownyWars transaction");
		    nation.save();
		}	
	}
	
	public void pay(double amount, WarParticipant receiver) {
		if(getType().equalsIgnoreCase("town")) {
			Town town = TownyUniverse.getInstance().getTown(getUuid());
			if(town.getAccount().getHoldingBalance()>=amount) {
				if(receiver.getType().equalsIgnoreCase("town")) {
					Town receiverTown = TownyUniverse.getInstance().getTown(receiver.getUuid());
					receiverTown.getAccount().setBalance(receiverTown.getAccount().getHoldingBalance() + amount, "Got paid from " + getName() + " TownyWars!");
					town.getAccount().setBalance(town.getAccount().getHoldingBalance() - amount, "Paid " + receiver.getName() + " in TownyWars!");
					receiverTown.save();
				}else {
					Nation receiverNation = TownyUniverse.getInstance().getNation(receiver.getUuid());
					receiverNation.getAccount().setBalance(receiverNation.getAccount().getHoldingBalance() + amount, "Got paid from " + getName() + " TownyWars!");
					town.getAccount().setBalance(town.getAccount().getHoldingBalance() - amount, "Paid " + receiver.getName() + " in TownyWars!");
					receiverNation.save();
				}
			}else {
				if(receiver.getType().equalsIgnoreCase("town")) {
					Town receiverTown = TownyUniverse.getInstance().getTown(receiver.getUuid());
					receiverTown.getAccount().setBalance(receiverTown.getAccount().getHoldingBalance() + town.getAccount().getHoldingBalance(), "Got paid from " + getName() + " TownyWars!");
					town.getAccount().setBalance((double)0.00, "Paid " + receiver.getName() + " in TownyWars!");
					receiverTown.save();
				}else {
					Nation receiverNation = TownyUniverse.getInstance().getNation(receiver.getUuid());
					receiverNation.getAccount().setBalance(receiverNation.getAccount().getHoldingBalance() + town.getAccount().getHoldingBalance(), "Got paid from " + getName() + " TownyWars!");
					town.getAccount().setBalance((double)0.00, "Paid " + receiver.getName() + " in TownyWars!");
					receiverNation.save();
				}			
				if(town.getMayor().getPlayer().isOnline())
					town.getMayor().getPlayer().sendMessage("Your town did not have enough money to pay the full amount, so you paid as much as you had and are now broke...");
			}			
			town.save();
		}else {
		    Nation nation = TownyUniverse.getInstance().getNation(getUuid());
		    if(nation.getAccount().getHoldingBalance()>=amount) {
				if(receiver.getType().equalsIgnoreCase("town")) {
					Town receiverTown = TownyUniverse.getInstance().getTown(receiver.getUuid());
					receiverTown.getAccount().setBalance(receiverTown.getAccount().getHoldingBalance() + amount, "Got paid from " + getName() + " TownyWars!");
					nation.getAccount().setBalance(nation.getAccount().getHoldingBalance() - amount, "Paid " + receiver.getName() + " in TownyWars!");
					receiverTown.save();
				}else {
					Nation receiverNation = TownyUniverse.getInstance().getNation(receiver.getUuid());
					receiverNation.getAccount().setBalance(receiverNation.getAccount().getHoldingBalance() + amount, "Got paid from " + getName() + " TownyWars!");
					nation.getAccount().setBalance(nation.getAccount().getHoldingBalance() - amount, "Paid " + receiver.getName() + " in TownyWars!");
					receiverNation.save();
				}
			}else {
				if(receiver.getType().equalsIgnoreCase("town")) {
					Town receiverTown = TownyUniverse.getInstance().getTown(receiver.getUuid());
					receiverTown.getAccount().setBalance(receiverTown.getAccount().getHoldingBalance() + nation.getAccount().getHoldingBalance(), "Got paid from " + getName() + " TownyWars!");
					nation.getAccount().setBalance((double)0.00, "Paid " + receiver.getName() + " in TownyWars!");
					receiverTown.save();
				}else {
					Nation receiverNation = TownyUniverse.getInstance().getNation(receiver.getUuid());
					receiverNation.getAccount().setBalance(receiverNation.getAccount().getHoldingBalance() + nation.getAccount().getHoldingBalance(), "Got paid from " + getName() + " TownyWars!");
					nation.getAccount().setBalance((double)0.00, "Paid " + receiver.getName() + " in TownyWars!");
					receiverNation.save();
				}			
				if(nation.getKing().getPlayer().isOnline())
					nation.getKing().getPlayer().sendMessage("Your nation did not have enough money to pay the full amount, so you paid as much as you had and are now broke...");
			}			
			nation.save();
		}
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		if(this.getType().equalsIgnoreCase("town")) {
			return TownyUniverse.getInstance().getTown(getUuid()).getName();
		}else {
			return TownyUniverse.getInstance().getNation(getUuid()).getName();
		}
	}

	@SuppressWarnings("serial")
	public class TownOrNationNotFoundException extends Exception{
		public TownOrNationNotFoundException(String errorMessage) {
	        super(errorMessage);
	    }
	}
	
}