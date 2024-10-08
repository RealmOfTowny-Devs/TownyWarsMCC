package com.danielrharris.townywars.listeners;

import com.danielrharris.townywars.Rebellion;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.War;
import com.danielrharris.townywars.WarManager;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.*;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

public class WarListener implements Listener {
    private TownyWars mplugin = null;

    public WarListener(TownyWars aThis) {
        this.mplugin = aThis;
    }

    @EventHandler
    public void onNationDeleteAttempt(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        String playerName = event.getPlayer().getName();

        for (War w : WarManager.getWars()) {
            for (Object o : w.getEntitiesInWar()) {
                if (o instanceof Town && ((Town) o).hasResident(playerName)) {
                    if (command.startsWith("/n") && command.contains("delete")) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(ChatColor.RED + "You cannot delete a town while at war!");
                        return;
                    }
                } else if (o instanceof Nation && ((Nation) o).hasResident(playerName)) {
                    if (command.startsWith("/n") && command.contains("delete")) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(ChatColor.RED + "You cannot delete a nation while at war!");
                        return;
                    } else if (command.startsWith("/n") && command.contains("leave")) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(ChatColor.RED + "You cannot leave a nation while at war!");
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onNationDelete(DeleteNationEvent event) {
        Nation nation = null;
        War war = null;
        String targetNationName = event.getNationName();

        for (War w : WarManager.getWars()) {
            for (Object o : w.getEntitiesInWar()) {
                if (o instanceof Nation && ((Nation) o).getName().equals(targetNationName)) {
                    nation = (Nation) o;
                    war = w;
                    break;
                }
            }
            if (nation != null) break;
        }

        if (war == null) {
            for (Rebellion r : Rebellion.getAllRebellions()) {
                if (r.getMotherNation().getName().equals(targetNationName)) {
                    Rebellion.getAllRebellions().remove(r);
                }
            }
            return;
        }

        WarManager.getWars().remove(war);
        if (war.getRebellion() != null) {
            Rebellion.getAllRebellions().remove(war.getRebellion());
            if (war.getRebellion().getRebelnation() != nation) {
                TownyUniverse.getInstance().getDataSource().deleteNation(war.getRebellion().getRebelnation());
            } else if (war.getRebellion().getMotherNation() != nation) {
                war.getRebellion().peace();
            }
        }
        TownyUniverse.getInstance().getDataSource().saveNations();

        try {
            WarManager.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            Resident re = TownyUniverse.getInstance().getResident(player.getName());
            if (re != null && re.hasTown()) {
                Town town = re.getTown();
                War townWar = WarManager.getWarForTown(town);

                if (townWar != null) {
                    Object enemy = townWar.getEnemy(town);
                    if (enemy instanceof Town) {
                        player.sendMessage(ChatColor.RED + "Warning: Your town is at war with " + ((Town) enemy).getName());
                    } else if (enemy instanceof Nation) {
                        player.sendMessage(ChatColor.RED + "Warning: Your town is at war with the nation of " + ((Nation) enemy).getName());
                    }

                    // Check for peace offers for towns here if you have that functionality
                }

                if (town != null && town.hasNation()) {
                    Nation nation = town.getNation();
                    War nationWar = WarManager.getWarForNation(nation);
                    if (nationWar != null) {
                        Object enemy = nationWar.getEnemy(nation);
                        if (enemy instanceof Town) {
                            player.sendMessage(ChatColor.RED + "Warning: Your nation is at war with " + ((Town) enemy).getName());
                        } else if (enemy instanceof Nation) {
                            player.sendMessage(ChatColor.RED + "Warning: Your nation is at war with the nation of " + ((Nation) enemy).getName());
                        }

                        if ((WarManager.hasBeenOffered(nationWar, nation)) && ((nation.hasAssistant(re)) || (re.isKing()))) {
                            player.sendMessage(ChatColor.GREEN + "The other nation has offered peace!");
                        }
                    }
                }

                // add the player to the master list if they don't exist in it yet
                if (mplugin.getTownyWarsResident(re.getName()) == null) {
                    mplugin.addTownyWarsResident(re.getName());
                    System.out.println("resident added!");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @EventHandler
    public void onResidentLeave(TownRemoveResidentEvent event) {
        Nation n;
        try {
            n = event.getTown().getNation();
        } catch (NotRegisteredException ex) {
            return;
        }
        War war = WarManager.getWarForNation(n);
        if (war == null) {
            return;
        }
        try {
            if (Objects.requireNonNull(WarManager.getWarForNation(event.getTown().getNation())).getPoints(event.getTown()) > TownyWars.pPlayer) {
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
    public void onResidentAdd(TownAddResidentEvent event) {
        Nation n;
        try {
            n = event.getTown().getNation();
        } catch (NotRegisteredException ex) {
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
    public void onNationAddTown(NationAddTownEvent event) {
        War war = WarManager.getWarForNation(event.getNation());
        if (war == null) {
            return;
        }
        war.addNationPoint(event.getNation(), event.getTown());
        war.addNewTown(event.getTown(), event.getNation());
        try {
            WarManager.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onNationRemove(NationRemoveTownEvent event) {
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