package com.danielrharris.townywars.warObjects;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.danielrharris.townywars.WarManager;
import com.danielrharris.townywars.exceptions.Exceptions.ParticipantNotFoundException;
import com.danielrharris.townywars.exceptions.Exceptions.NotInWarException;
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

public class War implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6100034513248686580L;
	private Set<WarParticipant> participants;
	private Rebellion rebelwar;
	private UUID warUUID;

	public War(WarParticipant participant1, WarParticipant participant2, Rebellion rebellion) throws Exception {
		this.participants = new HashSet<WarParticipant>();
		participants.add(participant1);
		participants.add(participant2);
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
	
	public Set<WarParticipant> getWarParticipants(){
		return this.participants;
	}
	
	public WarParticipant[] getWarParticipantsAsArray() {
		WarParticipant[] participants = new WarParticipant[2];
		int i = 0;
		for(WarParticipant part : getWarParticipants()) {
			participants[i] = part;
			i++;
		}
		return participants;
	}
	
	public Rebellion getRebellion() {
		return this.rebelwar;
	}
	
	public boolean isRebelWar() {
		if(getRebellion()!=null)
			return true;
		return false;
	}

	public int getParticipantPoints(UUID id) throws ParticipantNotFoundException, NotInWarException {
		if(WarManager.getWarParticipant(id)!=null) {
			return getParticipantPoints(WarManager.getWarParticipant(id));
		}
		throw new ParticipantNotFoundException("Participant UUID " + id.toString() + " cannot be found!");
	}
	
	public int getParticipantPoints(WarParticipant participant) throws ParticipantNotFoundException {
		for(WarParticipant part : getWarParticipants()) {
			if(part == participant)
				return part.getPoints();
		}
		throw new ParticipantNotFoundException("Participant cannot be found!");
	}
	
	public int getParticipantMaxPoints(WarParticipant participant) throws ParticipantNotFoundException {
		for(WarParticipant part : getWarParticipants()) {
			if(part == participant)
				return part.getMaxPoints();
		}
		throw new ParticipantNotFoundException("Participant cannot be found!");
	}
	
	public int getParticipantMaxPoints(UUID id) throws ParticipantNotFoundException, NotInWarException {
		if(WarManager.getWarParticipant(id)!=null) {
			return getParticipantMaxPoints(WarManager.getWarParticipant(id));
		}
		throw new ParticipantNotFoundException("Participant UUID " + id.toString() + " cannot be found!");
	}

	public WarParticipant getEnemy(WarParticipant participant) throws NotInWarException {
		WarParticipant[] participants = getWarParticipantsAsArray();
		if(participants[0]!=null) {
			if(participants[1]!=null) {
				if(participant == participants[0])
					return  participants[1];
				if(participant == participants[1])
					return  participants[0];
			}
		}
	    throw new NotInWarException("Specified participant is not in war.");
	}
	
	public void setWarParticipants(Set<WarParticipant> participants) {
		this.participants = participants;
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