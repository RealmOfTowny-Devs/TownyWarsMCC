package com.danielrharris.townywars.listeners;

import com.danielrharris.townywars.Title;
import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class NationWalkEvent implements Listener{

    @EventHandler
    public void onEnter(PlayerChangePlotEvent event)  {
        Player player = event.getPlayer();
        Resident resident = null;
        WorldCoord blockTo = event.getTo();
        try {
            resident = TownyUniverse.getDataSource().getResident(player.getName());
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }

        if (!Objects.requireNonNull(resident).hasTown()) debug("The resident doesn't have a town.");
        if (resident.hasTown() && !resident.hasNation()) debug("Resident has town but doesn't have a Nation.");
        if (blockTo == null) return;
        if (TownyUniverse.getTownBlock(event.getMoveEvent().getTo()) != null) {
            TownBlock townBlock = TownyUniverse.getTownBlock(event.getMoveEvent().getTo());


            Town rTown = null;
            Nation rNation = null;
            try {
                rTown = resident.getTown();
                rNation = rTown.getNation();
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
            Town townTo;

            Nation nationTo;

            try {
                townTo = townBlock.getTown();
                if(!townTo.hasNation()){
                    Title.sendTitle(player,20,20,20,ChatColor.WHITE.toString() + townTo.getName(),"doesn't have a nation.");
                }else {
                    nationTo = townTo.getNation();


                    if (rNation.hasEnemy(nationTo)) {
                        Title.sendTitle(player, 20, 20, 20, ChatColor.RED.toString() + nationTo.getName(), "is your enemy!");
                    }
                    if (rNation.hasAlly(nationTo)) {
                        Title.sendTitle(player, 20, 20, 20, ChatColor.GREEN.toString() + nationTo.getName(), "is your ally!");
                    }
                    if (!rNation.hasEnemy(nationTo) && !rNation.hasAlly(nationTo)) {
                        Title.sendTitle(player, 20, 20, 20, ChatColor.WHITE.toString() + nationTo.getName(), "is neutral with your nation.");
                    }
                    if(rNation.getName().equalsIgnoreCase(nationTo.getName()))return;
                }

            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }
    }
    private void debug(String message){
        System.out.println("[DEBUG] " + message);
    }

}
/**
 *
 * TODO:
 *
 * CHECK IF TOWN IS WILDERNESS, IF SO, STOP.
 *
 *
 **/