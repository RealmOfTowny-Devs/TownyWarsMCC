package com.danielrharris.townywars.trades;

import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.inventory.ItemStack;

public class Trade {

    private Town requesting,requested;

    public Trade(Town requesting, Town requested) {
        this.requesting = requesting;
        this.requested = requested;
    }

    public void setItemsTrading(ItemStack[] itemStacks){

    }

    public Town getRequesting() {
        return requesting;
    }

    public Town getRequested() {
        return requested;
    }
}
