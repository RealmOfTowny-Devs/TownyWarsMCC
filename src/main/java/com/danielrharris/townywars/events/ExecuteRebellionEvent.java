package com.danielrharris.townywars.events;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.danielrharris.townywars.warObjects.Rebellion;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

public class ExecuteRebellionEvent extends Event implements Cancellable
{
	private Rebellion rebellion;
	private boolean cancelled;
	private String time;
	
	public ExecuteRebellionEvent(Rebellion rebellion) {
		cancelled = false;
		this.rebellion = rebellion;
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		this.time = "[" + format.format(now) + "]";
	}
	
	public Rebellion getRebellion() {
		return this.rebellion;
	}
	
	public Town getLeaderTown()
	{
		return this.rebellion.getLeaderTown();
	}
	
	public Set<Town> getRebelTowns(){
		return this.rebellion.getRebelTowns();
	}
	
	public Nation getMotherNation(){
		return this.rebellion.getMotherNation();
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