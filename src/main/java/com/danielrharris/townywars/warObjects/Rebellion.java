package com.danielrharris.townywars.warObjects;

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

//import main.java.com.danielrharris.townywars.War.MutableInteger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.danielrharris.townywars.WarManager;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.TownyUniverse;

public class Rebellion implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6682239184047641293L;
	private Nation motherNation;
	private Nation rebelnation;
	private String name;
	private UUID id;
	private Town leader;
	private List<Town> originalMotherTowns = new ArrayList<Town>();
	private List<Town> rebels = new ArrayList<Town>();
	
	public Rebellion(Nation mn, String n, Town l){
		this.motherNation = mn;
		this.name = n;
		this.setUuid(UUID.randomUUID());
		this.leader = l;
		WarManager.getAllRebellions().add(this);
	}
	
	public Town getLeader() {
		return leader;
	}
	
	public List<Town> getRebels() {
		return rebels;
	}
	
	public void Execute(CommandSender cs){
		try {
			TownyUniverse.getInstance().getDataSource().newNation(name + "-rebels");
		} catch (AlreadyRegisteredException e2) {
			cs.sendMessage(ChatColor.RED + "Error: A nation with the name of your rebellion already exists.");
			return;
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rebelnation = TownyUniverse.getInstance().getNation(name + "-rebels");
		War.removeTownFromNationAndAddToAnotherNation(leader, motherNation, rebelnation);	
		for(Town town : rebels){
			War.removeTownFromNationAndAddToAnotherNation(town, motherNation, rebelnation);
		}
		for(Town town : motherNation.getTowns()){
			originalMotherTowns.add(town);
		}
		
		rebelnation.setCapital(leader);
		try {
			rebelnation.setKing(leader.getMayor());
		} catch (TownyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		WarManager.createWar(rebelnation, motherNation, cs, this);
		TownyUniverse.getInstance().getDataSource().saveTown(leader);
		TownyUniverse.getInstance().getDataSource().saveNation(rebelnation);
		TownyUniverse.getInstance().getDataSource().saveNations();
		try {
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cs.sendMessage(ChatColor.RED + "You executed your rebellion and are now at war with your nation!");
	}
	
	public void success(){
		ArrayList<Town> townsToBeMoved = new ArrayList<Town>();
		ArrayList<Town> townsToBeRemoved = new ArrayList<Town>();
		for(Town town : rebelnation.getTowns()){
			if(originalMotherTowns.contains(town))
				townsToBeMoved.add(town);
			else
				townsToBeRemoved.add(town);
		}
		
		for(Town town : townsToBeMoved){
			War.removeTownFromNationAndAddToAnotherNation(town, rebelnation, motherNation);
		}
		
		for(Town town : townsToBeRemoved){
			if (town.hasNation()) {
				Nation n;
				try {
					n = town.getNation();
					town.removeNation();
				    TownyUniverse.getInstance().getDataSource().saveTown(town);
				    TownyUniverse.getInstance().getDataSource().saveNation(n);
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			    
			}
		}		
		TownyUniverse.getInstance().getDataSource().saveNation(motherNation);
		TownyUniverse.getInstance().getDataSource().saveNation(rebelnation);
		try {
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void peace(){
		motherNation.collect(rebelnation.getAccount().getHoldingBalance());
		rebelnation.getAccount().setBalance((double)0.00, "Lost rebellion. Tough luck!");
		
		ArrayList<Town> l = new ArrayList<Town>(rebelnation.getTowns());
		for(Town town : l) {
			WarManager.townremove = town;
			War.removeTownFromNationAndAddToAnotherNation(town, rebelnation, motherNation);
		}
		
		try {
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Nation getRebelnation() {
		return rebelnation;
	}

	public boolean isRebelTown(Town town){
		for(Town rebel : rebels)
			if(town == rebel)
				return true;
		return false;
	}
	
	public boolean isRebelLeader(Town town){
		return town == leader;
	}
	
	public String getName(){
		return name;
	}
	
	public Nation getMotherNation(){
		return motherNation;
	}
	
	public void addRebell(Town town){
		rebels.add(town);
	}
	
	public void removeRebell(Town town){
		rebels.remove(town);
	}
	
	/** Read the object from Base64 string. */
	public static Rebellion decodeRebellion(String s) throws IOException, ClassNotFoundException {	                                                       
	    byte [] data = Base64.getDecoder().decode( s );
	    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
	    Rebellion o  = (Rebellion) ois.readObject();
	    ois.close();
	    return o;
	}

	/** Write the object to a Base64 string. */
	public String encodeRebellion() throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream( baos );
	    oos.writeObject( this );
	    oos.close();
	    return Base64.getEncoder().encodeToString(baos.toByteArray()); 
	}

	public UUID getUuid() {
		return id;
	}

	public void setUuid(UUID id) {
		this.id = id;
	}
}