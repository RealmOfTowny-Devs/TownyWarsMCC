package com.danielrharris.townywars.listeners;

import com.danielrharris.townywars.Rebellion;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.War;
import com.danielrharris.townywars.WarManager;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.NationAddTownEvent;
import com.palmergames.bukkit.towny.event.NationRemoveTownEvent;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class WarListener implements Listener
{
	private TownyWars mplugin=null;
	
	public WarListener(TownyWars aThis)
	{ 
		this.mplugin=aThis;
	}
	
	@EventHandler
	public void onNationDeleteAttempt(PlayerCommandPreprocessEvent event){	
		String command = event.getMessage().toLowerCase();
		if (command.startsWith("/n") && command.contains("delete"))
		{
			for(War w : WarManager.getWars()){
				for(Nation n : w.getNationsInWar()){
					if(n.hasResident(event.getPlayer().getName())){
						event.setCancelled(true);
						event.getPlayer().sendMessage(ChatColor.RED + "You cannot delete a nation while at war!");
					}
				}
			}
		}
		
		if (command.startsWith("/n") && command.contains("leave"))
		{
			for(War w : WarManager.getWars()){
				for(Nation n : w.getNationsInWar()){
					if(n.hasResident(event.getPlayer().getName())){
						event.setCancelled(true);
						event.getPlayer().sendMessage(ChatColor.RED + "You cannot leave a nation while at war!");
					}
				}
			}
		}
	}
  
	@EventHandler
	public void onNationDelete(DeleteNationEvent event){	  
		Nation nation = null;
		War war = null;
		for(War w : WarManager.getWars()){
			for(Nation n : w.getNationsInWar()){
				if(n.getName().equals(event.getNationName())){
					nation = n;
					war = w;
					break;
				}
			}
		}
		if(war == null){
			for(Rebellion r : Rebellion.getAllRebellions()){
				if(r.getMotherNation().getName().equals(event.getNationName())){
					Rebellion.getAllRebellions().remove(r);
				}			
			}
			return;
		}
		WarManager.getWars().remove(war);	  
		if(war.getRebellion() != null){
			Rebellion.getAllRebellions().remove(war.getRebellion());
			if(war.getRebellion().getRebelnation() != nation){
				TownyUniverse.getDataSource().deleteNation(war.getRebellion().getRebelnation());
			}else if(war.getRebellion().getMotherNation() != nation){
				war.getRebellion().peace();
			}
		}	  
		TownyUniverse.getDataSource().saveNations();
		try {
			WarManager.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		try
		{
			Resident re = TownyUniverse.getDataSource().getResident(player.getName());
			if(re!=null && re.hasTown()){
				Town town = re.getTown();
				if(town!=null && town.hasNation()){
					Nation nation = town.getNation();
					if(nation!=null){
						War ww = WarManager.getWarForNation(nation);
						if (ww != null)
						{
							player.sendMessage(ChatColor.RED + "Warning: Your nation is at war with " + ww.getEnemy(nation));
							if ((WarManager.hasBeenOffered(ww, nation)) && ((nation.hasAssistant(re)) || (re.isKing()))) {
								player.sendMessage(ChatColor.GREEN + "The other nation has offered peace!");
							}
						}
					}
				}
			}
			//Player plr = Bukkit.getPlayer(re.getName());
      
			// add the player to the master list if they don't exist in it yet
			if (mplugin.getTownyWarsResident(re.getName())==null){
				mplugin.addTownyWarsResident(re.getName());
				System.out.println("resident added!");
			}
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}
  
	@EventHandler
	public void onResidentLeave(TownRemoveResidentEvent event)
	{
		Nation n;
		try
		{
			n = event.getTown().getNation();
		}catch (NotRegisteredException ex){
			return;
		}
		War war = WarManager.getWarForNation(n);
		if (war == null) {
			return;
		}
		try {
			if(WarManager.getWarForNation(event.getTown().getNation()).getTownPoints(event.getTown()) > TownyWars.pPlayer){
				war.chargeTownPoints(n, event.getTown(), TownyWars.pPlayer);
			}
		} catch (NotRegisteredException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    
		try {
			WarManager.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  
	@EventHandler
	public void onResidentAdd(TownAddResidentEvent event)
	{
		Nation n;
		try
		{
			n = event.getTown().getNation();
		}catch (NotRegisteredException ex)
		{
			return;
		}
		War war = WarManager.getWarForNation(n);
		if (war == null) {
			return;
		}
		war.chargeTownPoints(n, event.getTown(), -TownyWars.pPlayer);
		try {
			WarManager.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  
	@EventHandler
	public void onNationAdd(NationAddTownEvent event)
	{
		War war = WarManager.getWarForNation(event.getNation());
		if (war == null) {
			return;
		}
		war.addNationPoint(event.getNation(), event.getTown());
		war.addNewTown(event.getTown());
		try {
			WarManager.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  
	@EventHandler
	public void onNationRemove(NationRemoveTownEvent event)
	{
		War war = WarManager.getWarForNation(event.getNation());
		if (war == null) {
			return;
		}
      
		war.removeTown(event.getTown(), event.getNation());
      
		try {
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
		//MAKE FUCKING WORK when a town is disbanded because of lack of funds
		/*if (event.getTown() != WarManager.townremove)
    	{
      	War war = WarManager.getWarForNation(event.getNation());
      	if (war == null) {
        	return;
      	}
     	townadd = event.getTown();
      	try
      	{
    	  	if(event.getNation().getNumTowns() != 0){
    		  	event.getNation().addTown(event.getTown());
    		}
      	}
      	catch (AlreadyRegisteredException ex){
        	Logger.getLogger(WarListener.class.getName()).log(Level.SEVERE, null, ex);
      	}
    	} else{
    	 	for(Rebellion r : Rebellion.getAllRebellions())
    	    	if(r.isRebelLeader(event.getTown())){
    	    		Rebellion.getAllRebellions().remove(r);
    	    		break;
    	    	}
    	    	else if(r.isRebelTown(event.getTown())){
    	    		r.removeRebell(event.getTown());
    	    		break;
    	    	}
    	}    
    	TownyUniverse.getDataSource().saveNations();
    	WarManager.townremove = null;*/
	}
}