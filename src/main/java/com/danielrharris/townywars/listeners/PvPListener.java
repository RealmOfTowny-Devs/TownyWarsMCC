package com.danielrharris.townywars.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.TownyWarsResident;
import com.danielrharris.townywars.War;
import com.danielrharris.townywars.WarManager;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class PvPListener implements Listener{
	
	private TownyWars mplugin;
	
	public PvPListener(TownyWars aThis)
	{ 
		this.mplugin=aThis;
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event){
		// get the current system time
		long hitTime=System.currentTimeMillis();
	  
		// check if the entity damaged was a player
		if (event.getEntity() instanceof Player){
			String attacker=null;
		  
			// check if the damaging entity was a player
			if (event.getDamager() instanceof Player) {
			  attacker=((Player)event.getDamager()).getName();
			}	  
			// check if the damaging entity was an arrow shot by a player
			else if (event.getDamager() instanceof Projectile){
				if (((Projectile)event.getDamager()).getShooter() instanceof Player){
					attacker=((Player)((Projectile)event.getDamager()).getShooter()).getName();
				}
			}else if (event.getDamager() instanceof TNTPrimed){
				TNTPrimed tnt = (TNTPrimed) event.getDamager();
				if(tnt.getSource() instanceof TNTPrimed){
					Entity ent = event.getDamager();
					do{
						TNTPrimed tnts = (TNTPrimed) ent;
						ent = tnts.getSource();
						if(ent instanceof Player){
							attacker = ((Player)ent).getName();
						}
					}while(ent instanceof TNTPrimed && !(ent instanceof Player));
				}else if(tnt.getSource() instanceof Projectile){
					Projectile proj = (Projectile) tnt.getSource();
					if(proj.getShooter() instanceof Player){
						attacker = ((Player)proj.getShooter()).getName();
					}else if(proj.getShooter() instanceof LivingEntity){
						LivingEntity ent = (LivingEntity) proj.getShooter();
						if(ent.getPassenger() instanceof Player){
							attacker = ent.getPassenger().getName();
						}
					}
				}			
			}
		  
			// if neither was true, then no need to update the player's stats
			if (attacker==null)
			{ 
				return;
			}
		  
			String playerName=((Player)event.getEntity()).getName();
			if (mplugin.getTownyWarsResident(playerName)!=null) {
				// update the player's stats
				mplugin.getTownyWarsResident(playerName).setLastHitTime(hitTime);
				mplugin.getTownyWarsResident(playerName).setLastAttacker(attacker);
			}	  
		}
	}
  
  
	// here we want to differentiate between player deaths due solely to environmental damage
	// and due to environmental damage in combination with player hits

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		// record the timestamp immediately
		long deathTime = System.currentTimeMillis();
	  
		// get the name of the dead resident
		String playerName = event.getEntity().getName();
	  
		// get the name of the cause of death
		DamageCause damageCause=event.getEntity().getLastDamageCause().getCause();
	  
		String playerKiller=null;
	  
		// here, the kill was not done by a player, so we need to look up who to credit, if anyone
		if (event.getEntity().getKiller()==null) {		  
			// let's look up who hit them last, and how long ago
			long lastHitTime=0;
			String lastAttacker=null;
			TownyWarsResident cre= mplugin.getTownyWarsResident(playerName);
			if (cre!=null){
				lastHitTime=cre.getLastHitTime();
				lastAttacker=cre.getLastAttacker();
				// reset dead player's stats
				cre.setLastAttacker(null);
				cre.setLastHitTime(0);
			}
			// if the player has been hit by another player within the past 30 seconds, credit the killer
			if (lastAttacker!=null && deathTime-lastHitTime<30000){
				playerKiller=lastAttacker;
				// give the killer credit in chat :-)
				event.setDeathMessage(event.getDeathMessage()+" to escape "+playerKiller);
			}
		}
		// kill was done by another player
		else {
			playerKiller=event.getEntity().getKiller().getName();
		}
	  
		// we need to record the kill in all its glory to a log file for moderation use
		// takes in the time of death in milliseconds, the player that was killed, the killer, the final cause of death, and the death message
		int status = mplugin.writeKillRecord(deathTime,event.getEntity().getName(),playerKiller,damageCause.name(),event.getDeathMessage());
		if (status==0){
	  		System.out.println("death recorded!");
	  	}else {
	  		System.out.println("[ERROR] death recording failed! you should check on this!");
	  	}
	  
		// if the player actually wasn't killed by another player, we can stop
		if (playerKiller==null)
		{ 
			return; 
		}

		// if we've made it this far, it means that the death should affect Towny
		// now we know who to credit, so let's adjust Towny to match		  
		try {
			Resident resi = TownyUniverse.getDataSource().getResident(playerKiller);
			Resident otherRes = TownyUniverse.getDataSource().getResident(playerName);
			if(resi.hasTown()){
				Town tdamagerr = resi.getTown();
				Nation damagerr = tdamagerr.getNation();
				if(otherRes.hasTown()){
					Town tdamagedd = TownyUniverse.getDataSource().getResident(playerName).getTown();
					Nation damagedd = tdamagedd.getNation();
			      
					War war = WarManager.getWarForNation(damagerr);
					if ((war.hasNation(damagedd)) && (!damagerr.getName().equals(damagedd.getName())))
					{
						tdamagedd.pay(TownyWars.pKill, "Death cost");
						tdamagerr.collect(TownyWars.pKill);
					}
					if ((war.hasNation(damagedd)) && (!damagerr.getName().equals(damagedd.getName()))) {
						try
						{
							if(tdamagedd.hasResident(playerName)){
								Resident res = TownyUniverse.getDataSource().getResident(playerName);
								Resident killer = TownyUniverse.getDataSource().getResident(playerKiller);
								String dmessage = "";
								String kmessage = "";
								String resName = res.getFormattedName();
								String killerName = killer.getFormattedName();
								if(res.isMayor() && !res.isKing()){
									if(killer.isKing() || killer.isMayor()){
										dmessage = ChatColor.YELLOW + resName + ChatColor.RED + " has been slain in Combat by the vile " + ChatColor.YELLOW + killerName + ChatColor.RED + "!";
										kmessage = ChatColor.YELLOW + killerName + ChatColor.RED + " has brought down the corrupt " + ChatColor.YELLOW + resName + ChatColor.RED + " in Combat!";
									}else{
										dmessage = ChatColor.YELLOW + resName + ChatColor.RED + " has been slain in Combat by slimy " + ChatColor.YELLOW + killerName + ChatColor.RED + "!";
										kmessage = ChatColor.YELLOW + killerName + ChatColor.RED + " has butchered the sneaky " + ChatColor.YELLOW + resName + ChatColor.RED + " in Combat!";
									}
									War.broadcast(damagedd, dmessage);
									War.broadcast(damagerr, kmessage);
									war.chargeTownPoints(damagedd, tdamagedd, TownyWars.pMayorKill);
								}else if(res.isKing()){
									if(killer.isKing() || killer.isMayor()){
										dmessage = ChatColor.YELLOW + resName + ChatColor.RED + " has been slain in Combat by the vile " + ChatColor.YELLOW + killerName + ChatColor.RED + "!";
										kmessage = ChatColor.YELLOW + killerName + ChatColor.RED + " has brought down the corrupt " + ChatColor.YELLOW + resName + ChatColor.RED + " in Combat!";
									}else{
										dmessage = ChatColor.YELLOW + resName + ChatColor.RED + " has been slain in Combat by slimy " + ChatColor.YELLOW + killerName + ChatColor.RED + "!";
										kmessage = ChatColor.YELLOW + killerName + ChatColor.RED + " has butchered the sneaky " + ChatColor.YELLOW + resName + ChatColor.RED + " in Combat!";
									}
									War.broadcast(damagedd, dmessage);
									War.broadcast(damagerr, kmessage);
									war.chargeTownPoints(damagedd, tdamagedd, TownyWars.pKingKill);
								}else{
									war.chargeTownPoints(damagedd, tdamagedd, TownyWars.pKillPoints);
								}
							}
							double lP = war.getTownPoints(tdamagedd);
							if (lP <= 10 && lP != -1 && WarManager.getWars().contains(war)) {
								event.getEntity().sendMessage(ChatColor.RED + "Be careful! Your town only has a " + lP + " points left!");
							}
						}catch (Exception ex)
						{
							event.getEntity().sendMessage(ChatColor.RED + "An error occured, check the console!");
							ex.printStackTrace();
						}
					}
				}			
			}	
	    }catch (Exception ex) {
	    	ex.printStackTrace();
	    }    
	    try {
			WarManager.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}