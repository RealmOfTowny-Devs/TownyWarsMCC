package com.danielrharris.townywars.events;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;
import com.palmergames.bukkit.towny.object.Resident;

public class WarPVPEvent extends Event implements Cancellable
{
	private War war;
	private String time;
	private Resident resident, enemy;
	private Location loc;
	private boolean cancelled;
	
	public WarPVPEvent(War war, Resident resident, Resident enemy, Location loc) {
		cancelled = false;
		this.war = war;
		this.resident = resident;
		this.enemy = enemy;
		this.loc = loc;
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		this.time = "[" + format.format(now) + "]";
	}
	
	public War getWar() {
		return this.war;
	}
	
	public Set<WarParticipant> getWarParticipants()
	{
		return this.war.getWarParticipants();
	}

	@Override
	public HandlerList getHandlers() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTime() {
		return time;
	}

	public Resident getResident() {
		return resident;
	}

	public Resident getEnemy() {
		return enemy;
	}
	
	public Player getPlayer() {
		return getResident().getPlayer();
	}
	
	public Player getEnemyPlayer() {
		return getEnemy().getPlayer();
	}

	public Location getLocation() {
		return loc;
	}

	@Override
	public boolean isCancelled() {
	    return cancelled; 
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}
	
}