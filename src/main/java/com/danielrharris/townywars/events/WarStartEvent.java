package com.danielrharris.townywars.events;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;

public class WarStartEvent extends Event implements Cancellable
{
	private War war;
	private boolean cancelled;
	private String time;
	
	public WarStartEvent(War war) {
		cancelled = false;
		this.war = war;
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

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;	
	}

	public String getTime() {
		return time;
	}
	
}