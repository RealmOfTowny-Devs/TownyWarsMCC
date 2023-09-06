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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.danielrharris.townywars.WarManager;
import com.danielrharris.townywars.exceptions.Exceptions.NotInWarException;
import com.danielrharris.townywars.exceptions.Exceptions.TownOrNationNotFoundException;
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
    
    public static WarParticipant createWarParticipant(Object object) throws NotRegisteredException {
    	if(object instanceof Town) {
    		Town town = (Town)object;
    		if(town.hasNation()) {
    			return new WarParticipant(town.getNation());
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
        			return new WarParticipant(town.getNation());
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
		part.setMaxPoints(WarManager.getNationMaxPoints(nation));
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
		part.setMaxPoints(WarManager.getTownMaxPoints(town));
		part.towns = new HashMap<UUID, Integer>();
		part.towns.put(town.getUUID(), WarManager.getTownMaxPoints(town));
		return part;
    }
    
    private WarParticipant(Nation nation) {
		setType("nation");
		setUuid(nation.getUUID());
		setMaxPoints(WarManager.getNationMaxPoints(nation));
		initializeTowns(nation.getTowns());
	}
	
    private WarParticipant(Town town) {
    	setType("town");
    	setUuid(town.getUUID());
    	setMaxPoints(WarManager.getTownMaxPoints(town));
		List<Town> list = new ArrayList<Town>();
		list.add(town);
		initializeTowns(list);
	}
    
    private void initializeTowns(List<Town> towns) {
		Map<UUID, Integer> t = new HashMap<UUID, Integer>();
		for(Town town : towns) {
			t.put(town.getUUID(), WarManager.getTownMaxPoints(town));
		}
		this.towns = t;
	}
	
	public Map<UUID, Integer> getTownsMap() {
		return this.towns;
	}

	public List<String> getTownNames() {
		List<String> towns = new ArrayList<String>();
		for(UUID s : this.towns.keySet()) {
			if(TownyUniverse.getInstance().hasTown(s))
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
			if(TownyUniverse.getInstance().hasTown(s)) {
				t.add(TownyUniverse.getInstance().getTown(s));
			}
		}
		return t;
	}
	
	public void addNewTown(Town town) throws TownOrNationNotFoundException {
		if(!this.towns.containsKey(town.getUUID())) {
			this.towns.put(town.getUUID(), WarManager.getTownMaxPoints(town));
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

	private void setMaxPoints(int maxPoints) {
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
			if(TownyUniverse.getInstance().hasTown(getUuid())){
				return TownyUniverse.getInstance().getTown(getUuid()).getAccount().getHoldingBalance();
			}			
		}else {
			if(TownyUniverse.getInstance().hasNation(getUuid())){
				 return TownyUniverse.getInstance().getNation(getUuid()).getAccount().getHoldingBalance();	
			}		   
		}
		return (double)0.00;
	}
	
	public void setHoldingBalance(double balance) {
		if(getType().equalsIgnoreCase("town")) {
			if(TownyUniverse.getInstance().hasTown(getUuid())){
				Town town = TownyUniverse.getInstance().getTown(getUuid());
				town.getAccount().setBalance(balance, "TownyWars transaction");
				town.save();
			}	
		}else {
			if(TownyUniverse.getInstance().hasNation(getUuid())){
		    Nation nation = TownyUniverse.getInstance().getNation(getUuid());
		    nation.getAccount().setBalance(balance, "TownyWars transaction");
		    nation.save();
			}
		}	
	}
	
	public Resident getLeader() {
		if(this.getType().equalsIgnoreCase("town")) {
			if(TownyUniverse.getInstance().hasTown(uuid)) {
				return TownyUniverse.getInstance().getTown(uuid).getMayor();
			}
		}else {
			if(TownyUniverse.getInstance().hasNation(uuid)) {
				return TownyUniverse.getInstance().getNation(uuid).getKing();
			}
		}
		return null;
	}
	
	public Set<Resident> getAssistants() {
		Set<Resident> assistants = new HashSet<Resident>();
		if(this.getType().equalsIgnoreCase("nation")) {
			if(TownyUniverse.getInstance().hasNation(uuid)) {
				for(Resident r : TownyUniverse.getInstance().getNation(uuid).getAssistants()) {
					assistants.add(r);
				}			
			}
		}else {
			if(TownyUniverse.getInstance().hasTown(uuid)) {
				for(Resident r: TownyUniverse.getInstance().getTown(uuid).getTrustedResidents()) {
					assistants.add(r);
				}
			}
		}
		return assistants;
	}
	
	public boolean isRebelWar() throws NotInWarException {
		return getWar().isRebelWar();
	}
	
	public Rebellion getRebellion() throws NotInWarException {
		return getWar().getRebellion();
	}
	
	public War getWar() throws NotInWarException {
		return WarManager.getWar(this);
	}
	
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		if(this.getType().equalsIgnoreCase("town")) {
			if(TownyUniverse.getInstance().hasTown(getUuid()))
				return TownyUniverse.getInstance().getTown(getUuid()).getName();
		}else 
			if(TownyUniverse.getInstance().hasNation(getUuid()))
				return TownyUniverse.getInstance().getNation(getUuid()).getName();
		return "";
	}
	
}