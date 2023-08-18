package com.danielrharris.townywars;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TownyUtils {

    /**
     * Checks whether the given location is in a town and if the player is a resident of that town.
     *
     * @param player The player to check against the town.
     * @param location The block location to check.
     * @return A tuple where the first value indicates if the block is in a town,
     *         and the second indicates if the player is a resident of that town.
     */
    public static Tuple<Boolean, Boolean> townyVerification(Player player, Location location) {
        TownBlock townBlock = TownyAPI.getInstance().getTownBlock(location);
        if (townBlock == null) {
            return new Tuple<>(false, false);
        }
        try {
            Town targetTown = townBlock.getTown();
            if (!targetTown.hasResident(player)) {
                return new Tuple<>(true, false);
            } else {
                return new Tuple<>(true, true);
            }
        } catch (NotRegisteredException ex) {
            return new Tuple<>(false, false);
        }
    }
}
