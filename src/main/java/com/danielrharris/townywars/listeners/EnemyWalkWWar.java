package com.danielrharris.townywars.listeners;

import com.danielrharris.townywars.Title;
import com.danielrharris.townywars.WarManager;
import com.palmergames.bukkit.towny.TownyUniverse;
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
    public void onEnemyWalk(PlayerChangePlotEvent event) throws NotRegisteredException {
        Player player = event.getPlayer();
        Resident resident = null;
        WorldCoord to = event.getTo();
        try {
            resident = TownyUniverse.getInstance().getResident(player.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert resident !=null;
        if (!resident.hasTown()) return;
        if (resident.hasTown() && !resident.hasNation()) return;
        if (to == null) return;
        if (TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(event.getMoveEvent().getTo())) != null) {
            TownBlock townBlock = TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(event.getMoveEvent().getTo()));

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
                                    if(player1 == null)return;
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