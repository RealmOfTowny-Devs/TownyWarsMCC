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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.WarManager.AlreadyAtWarException;
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
	private String motherNation;
	private String rebelNation;
	private String name;
	private UUID id;
	private String leader;
	private List<String> rebels = new ArrayList<String>();
	private WarParticipant participant;
	
	public Rebellion(Nation motherNation, Town leader){	
		this.motherNation = motherNation.getUUID().toString();
		this.name = leader.getName();
		this.setUuid(UUID.randomUUID());
		this.leader = leader.getUUID().toString();
	}
	
	public Town getLeader() {
		if(TownyUniverse.getInstance().hasTown(UUID.fromString(leader)))
			return TownyUniverse.getInstance().getTown(UUID.fromString(leader));
		return null;
	}
	
	public Nation getRebelNation() {
		if(TownyUniverse.getInstance().hasNation(UUID.fromString(rebelNation)))
			return TownyUniverse.getInstance().getNation(UUID.fromString(rebelNation));
		return null;
	}
	
	public Nation getMotherNation() {
		if(TownyUniverse.getInstance().hasNation(UUID.fromString(motherNation)))
			return TownyUniverse.getInstance().getNation(UUID.fromString(motherNation));
		return null;
	}
	
	public List<Town> getRebels() {
		List<Town> rebelsList = new ArrayList<Town>();
		if(rebels.isEmpty())
			return null;
		for(String s : rebels) {
			rebelsList.add(TownyUniverse.getInstance().getTown(UUID.fromString(s)));
		}
		return rebelsList;
	}
	
	public void Execute(CommandSender cs){
		try {
			TownyUniverse.getInstance().getDataSource().newNation(name + "-rebels");
			TownyUniverse.getInstance().getDataSource().saveNation(TownyUniverse.getInstance().getNation(name + "-rebels"));
		} catch (AlreadyRegisteredException e2) {
			cs.sendMessage(ChatColor.RED + "Error: A nation with the name of your rebellion already exists.");
			return;
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Nation rebelnation = TownyUniverse.getInstance().getNation(name + "-rebels");
		
		War.removeTownFromNationAndAddToAnotherNation(getLeader(), getMotherNation(), rebelnation);	
		
		for(Town town : getRebels()){
			War.removeTownFromNationAndAddToAnotherNation(town, getMotherNation(), rebelnation);
		}
		
		rebelnation.setCapital(getLeader());
		try {
			rebelnation.setKing(getLeader().getMayor());
		} catch (TownyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			participant = TownyWars.getInstance().getWarManager().createWarParticipant(rebelnation);
			TownyWars.getInstance().getWarManager().createWar(participant, TownyWars.getInstance().getWarManager().createWarParticipant(motherNation), cs, this);
			TownyUniverse.getInstance().getDataSource().saveTown(getLeader());
			TownyUniverse.getInstance().getDataSource().saveNation(rebelnation);
			TownyUniverse.getInstance().getDataSource().saveNations();
			try {
				TownyWars.getInstance().getWarManager().save();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cs.sendMessage(ChatColor.RED + "You executed your rebellion and are now at war with your nation!");
		} catch (AlreadyAtWarException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	public void success(){
		try {
			WarParticipant enemy = TownyWars.getInstance().getWarManager().getWarForParticipant(participant).getEnemy(participant);
			for(Town town : participant.getTownsList()) {
				War.removeTownFromNationAndAddToAnotherNation(town, getRebelNation(), getMotherNation());
			}
			try {
				participant.pay(participant.getHoldingBalance(), enemy);
				getMotherNation().setKing(getLeader().getMayor());
				getMotherNation().setCapital(getLeader());
				TownyUniverse.getInstance().getDataSource().removeNation(getRebelNation());
				TownyUniverse.getInstance().getDataSource().saveNation(getMotherNation());
				TownyUniverse.getInstance().getDataSource().saveNation(getRebelNation());
			} catch (TownyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}
	
	public void lost(){
		try {
			WarParticipant enemy = TownyWars.getInstance().getWarManager().getWarForParticipant(participant).getEnemy(participant);
			for(Town town : participant.getTownsList()) {
				War.removeTownFromNationAndAddToAnotherNation(town, getRebelNation(), getMotherNation());
			}
			participant.pay(participant.getHoldingBalance(), enemy);
			TownyUniverse.getInstance().getDataSource().removeNation(getRebelNation());
			TownyUniverse.getInstance().getDataSource().saveNation(getMotherNation());
			TownyUniverse.getInstance().getDataSource().saveNation(getRebelNation());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public boolean isRebelTown(Town town){
		for(Town rebel : getRebels())
			if(town == rebel)
				return true;
		return false;
	}
	
	public boolean isRebelLeader(Town town){
		return town == getLeader();
	}
	
	public String getName(){
		return name;
	}
	
	public void addRebel(Town town){
		rebels.add(town.getUUID().toString());
	}
	
	public void removeRebel(Town town){
		rebels.remove(town.getUUID().toString());
	}
	
	public WarParticipant getRebelWarParticipant() {
		return participant;
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