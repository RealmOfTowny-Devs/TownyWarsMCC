package com.danielrharris.townywars.warObjects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.danielrharris.townywars.TownyWars;
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
	private UUID motherNation;
	private UUID rebelNation;
	private String name;
	private UUID id;
	private UUID leader;
	private Set<UUID> rebels = new HashSet<UUID>();
	
	public Rebellion(Nation motherNation, Town leader){	
		this.motherNation = motherNation.getUUID();
		this.name = leader.getName();
		this.id = leader.getUUID();
		this.leader = leader.getUUID();
		WarManager.addPlannedRebellion(this);
	}
	
	public Town getLeaderTown() {
		if(TownyUniverse.getInstance().hasTown(leader))
			return TownyUniverse.getInstance().getTown(leader);
		return null;
	}
	
	public Nation getRebelNation() {
		if(TownyUniverse.getInstance().hasNation(rebelNation))
			return TownyUniverse.getInstance().getNation(rebelNation);
		return null;
	}
	
	public Nation getMotherNation() {
		if(TownyUniverse.getInstance().hasNation(motherNation))
			return TownyUniverse.getInstance().getNation(motherNation);
		return null;
	}
	
	public Set<Town> getRebelTowns() {
		Set<Town> rebelsList = new HashSet<Town>();
		if(rebels.isEmpty())
			return null;
		for(UUID uuid : rebels) {
			if(TownyUniverse.getInstance().hasTown(uuid))
				rebelsList.add(TownyUniverse.getInstance().getTown(uuid));
		}
		return rebelsList;
	}
	
	public void execute() throws Exception{
		Player player = getLeaderTown().getMayor().getPlayer();
		try {
			TownyUniverse.getInstance().getDataSource().newNation(name + "-rebels");
			TownyUniverse.getInstance().getDataSource().saveNation(TownyUniverse.getInstance().getNation(name + "-rebels"));
		} catch (AlreadyRegisteredException e2) {
			if(player!=null)
				player.sendMessage(ChatColor.RED + "Error: A nation with the name of your rebellion already exists.");
			return;
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(TownyUniverse.getInstance().hasNation(name + "-rebels")) {
			Nation rebelnation = TownyUniverse.getInstance().getNation(name + "-rebels");
			this.id = rebelnation.getUUID();
			this.name = rebelnation.getName();
			WarManager.removeTownFromNationAndAddToAnotherNation(getLeaderTown(), getMotherNation(), rebelnation);	
			
			for(Town town : getRebelTowns()){
				WarManager.removeTownFromNationAndAddToAnotherNation(town, getMotherNation(), rebelnation);
			}			
			rebelnation.setCapital(getLeaderTown());
			rebelnation.setKing(getLeaderTown().getMayor());
			TownyUniverse.getInstance().getDataSource().saveNation(rebelnation);
			WarManager.createWar(rebelnation, getMotherNation(), this);
			TownyUniverse.getInstance().getDataSource().saveTown(getLeaderTown());
			TownyUniverse.getInstance().getDataSource().saveNation(rebelnation);
			TownyUniverse.getInstance().getDataSource().saveNations();
			if(player!=null)
				player.sendMessage(ChatColor.RED + "You executed your rebellion and are now at war with your nation!");		
			WarManager.removePlannedRebellion(this);
			TownyWars.getInstance().getDataManager().save();
		}
		
	}
	
	public void success(){
		try {
			WarParticipant participant = getRebelWarParticipant();
			if(participant!=null) {
				WarParticipant enemy = WarManager.getWar(participant).getEnemy(participant);
				for(Town town : participant.getTownsList()) {
					WarManager.removeTownFromNationAndAddToAnotherNation(town, getRebelNation(), getMotherNation());
				}
				try {
					WarManager.pay(participant.getHoldingBalance(), participant, enemy);
					getMotherNation().setKing(getLeaderTown().getMayor());
					getMotherNation().setCapital(getLeaderTown());
					TownyUniverse.getInstance().getDataSource().removeNation(getRebelNation());
					TownyUniverse.getInstance().getDataSource().saveNation(getMotherNation());
					TownyUniverse.getInstance().getDataSource().saveNation(getRebelNation());
				} catch (TownyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}
	
	public void lost(){
		try {
			WarParticipant participant = getRebelWarParticipant();
			if(participant!=null) {
				WarParticipant enemy = WarManager.getWar(participant).getEnemy(participant);
				for(Town town : participant.getTownsList()) {
					WarManager.removeTownFromNationAndAddToAnotherNation(town, getRebelNation(), getMotherNation());
				}
				WarManager.pay(participant.getHoldingBalance(), participant, enemy);
				TownyUniverse.getInstance().getDataSource().removeNation(getRebelNation());
				TownyUniverse.getInstance().getDataSource().saveNation(getMotherNation());
				TownyUniverse.getInstance().getDataSource().saveNation(getRebelNation());
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public boolean isRebelTown(Town town){
		for(Town rebel : getRebelTowns())
			if(town == rebel)
				return true;
		return false;
	}
	
	public boolean isRebelLeader(Town town){
		return (town == getLeaderTown());
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void addRebel(Town town){
		rebels.add(town.getUUID());
	}
	
	public void addRebel(UUID uuid){
		rebels.add(uuid);
	}
	
	public void removeRebel(Town town){
		rebels.remove(town.getUUID());
	}
	
	public void removeRebel(UUID uuid){
		rebels.remove(uuid);
	}
	
	public WarParticipant getRebelWarParticipant() {
		for(War war : WarManager.getWars()) {
			if(war.isRebelWar())
				if(war.getRebellion() == this) {
					for(WarParticipant part : war.getWarParticipants()) {
						for(Town town : part.getTownsList()) {
							if(getLeaderTown() == town)
								return part;
						}
					}
				}
		}
		return null;
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

	public void peace(WarParticipant loser) {
				
	}

}