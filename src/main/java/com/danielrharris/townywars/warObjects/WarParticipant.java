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
import com.palmergames.bukkit.towny.object.Nation;
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
    		return new WarParticipant((Town)object);
    	}else if (object instanceof Nation) {
    		return new WarParticipant((Nation)object);
    	}else if(object instanceof String){
    		String s = (String)object;
    		if(TownyUniverse.getInstance().hasTown(s)){
    			Town town = TownyUniverse.getInstance().getTown(s);
    			return new WarParticipant(town);
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
    
    public WarParticipant(Nation nation) {
		setType("nation");
		setUuid(nation.getUUID());
		setMaxPoints(getNationMaxPoints(nation));
		initializeTowns(nation.getTowns());
	}
	
    public WarParticipant(Town town) {
    	setType("town");
    	setUuid(town.getUUID());
    	setMaxPoints(getTownMaxPoints(town));
		List<Town> list = new ArrayList<Town>();
		list.add(town);
		initializeTowns(list);
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

	public void initializeTownsByNames(List<String> towns) {
		Map<UUID, Integer> t = new HashMap<UUID, Integer>();
		for(String town : towns) {
			if(TownyUniverse.getInstance().hasTown(town)) {
				Town to = TownyUniverse.getInstance().getTown(town);
				t.put(to.getUUID(), getTownMaxPoints(to));
			}
		}
		this.towns = t;
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

	private void initializeTowns(List<Town> towns) {
		Map<UUID, Integer> t = new HashMap<UUID, Integer>();
		for(Town town : towns) {
			t.put(town.getUUID(), getTownMaxPoints(town));
		}
		this.towns = t;
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

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	@SuppressWarnings("serial")
	public class TownOrNationNotFoundException extends Exception{
		public TownOrNationNotFoundException(String errorMessage) {
	        super(errorMessage);
	    }
	}
	
}