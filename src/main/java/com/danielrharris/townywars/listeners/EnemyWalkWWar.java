package com.danielrharris.townywars.listeners;

import com.danielrharris.townywars.Title;
import com.danielrharris.townywars.WarManager;
import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class EnemyWalkWWar implements Listener {

    @EventHandler
    public void onEnemyWalk(PlayerChangePlotEvent event) {
        Player player = event.getPlayer();
        Resident resident = null;
        WorldCoord to = event.getTo();
        try {
            resident = TownyUniverse.getDataSource().getResident(player.getName());
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }
        if (!Objects.requireNonNull(resident).hasTown()) System.out.println("The resident doesn't have a town.");
        if (resident.hasTown() && !resident.hasNation()) System.out.println("Resident has town but doesn't have a Nation.");
        if (to == null) return;
        if (TownyUniverse.getTownBlock(event.getMoveEvent().getTo()) != null) {
            TownBlock townBlock = TownyUniverse.getTownBlock(event.getMoveEvent().getTo());

            Town rTown;
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
                assert townTo.hasNation();
                nationTo = townTo.getNation();
                assert rNation != null;
                if(rNation.hasEnemy(nationTo) && Objects.requireNonNull(WarManager.getWarForNation(nationTo)).getEnemy(nationTo) == rNation) {
                    if (WarManager.getWarForNation(nationTo) != null) {
                        if (Objects.requireNonNull(WarManager.getWarForNation(nationTo)).getEnemy(nationTo) == rNation) {
                            if(nationTo.getAllies().isEmpty())return;
                            for (Nation nation : nationTo.getAllies()){
                                for (Resident resident1 : nation.getResidents()){
                                    Player player1 = Bukkit.getPlayer(resident1.getName());
                                    Title.sendTitle(player1,20,20,20, ChatColor.GREEN.toString() + nationTo.getName()," is under attack!");
                                }
                            }
                        }
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}