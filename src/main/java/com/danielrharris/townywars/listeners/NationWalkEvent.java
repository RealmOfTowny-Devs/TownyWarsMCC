package com.danielrharris.townywars.listeners;

import com.danielrharris.townywars.Title;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NationWalkEvent implements Listener {

    @EventHandler
    public void onEnter(PlayerChangePlotEvent event) throws NotRegisteredException {
        Player player = event.getPlayer();
        Resident resident = null;
        WorldCoord blockTo = event.getTo();
        WorldCoord blockFrom = event.getFrom();
        try {
            resident = TownyUniverse.getInstance().getResident(player.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert resident != null;
        if (!resident.hasTown()) return;
        if (resident.hasTown() && !resident.hasNation()) return;
        if (blockTo == null) return;

        try {
            if (TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(event.getMoveEvent().getTo())) != null) {
                TownBlock townBlock = TownyUniverse.getInstance().getTownBlock(WorldCoord.parseWorldCoord(event.getMoveEvent().getTo()));

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
                    if (!townTo.hasNation()) {
                        Title.sendTitle(player, 20, 20, 20, ChatColor.WHITE.toString() + townTo.getName(), "doesn't have a nation.");
                    }
                    assert blockFrom != null;
                    if (blockFrom.getTownyWorld().isClaimable()) {
                        nationTo = townTo.getNation();
                        assert rNation != null;
                        if (rNation.getName().equals(nationTo.getName())) return;

                        if (rNation.hasEnemy(nationTo) && !blockFrom.getTownyWorld().hasTowns()) {
                            Title.sendTitle(player, 20, 20, 20, ChatColor.RED.toString() + nationTo.getName(), "is your enemy!");
                        }
                        if (rNation.hasAlly(nationTo)) {
                            Title.sendTitle(player, 20, 20, 20, ChatColor.GREEN.toString() + nationTo.getName(), "is your ally!");
                        }
                        if (!rNation.hasEnemy(nationTo) && !rNation.hasAlly(nationTo)) {
                            Title.sendTitle(player, 20, 20, 20, ChatColor.WHITE.toString() + nationTo.getName(), "is neutral with your nation.");
                        }
                    }

                } catch (NotRegisteredException e) {
                    e.printStackTrace();
                }
            }
        } catch (NotRegisteredException ignored) {
        }
    }

}
/**
 * TODO:
 * <p>
 * CHECK IF TOWN IS WILDERNESS, IF SO, STOP.
 * <p>
 * <p>
 * if (!townBlock1.hasTown() || townBlock1.getType() == TownBlockType.WILDS) {
 **/