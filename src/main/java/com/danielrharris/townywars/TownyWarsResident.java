package com.danielrharris.townywars;

// some extra resident fields needed to properly record deaths
public class TownyWarsResident{
	
	private String name;
	private long lastHitTime=0;
	private String lastAttacker=null;
	
	public TownyWarsResident(String name){
		// not much to construct here . . .
	}
	
	// get and set methods
	public String getName(){
		return name;
	}
	
	public long getLastHitTime(){
		return lastHitTime;
	}
	
	public void setLastHitTime(long newHitTime){
		lastHitTime=newHitTime;
	}
	
	public String getLastAttacker(){
		return lastAttacker;
	}
	
	public void setLastAttacker(String newAttacker){
		lastAttacker=newAttacker;
	}
	
}