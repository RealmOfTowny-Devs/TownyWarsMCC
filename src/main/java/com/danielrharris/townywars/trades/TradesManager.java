package com.danielrharris.townywars.trades;

import com.palmergames.bukkit.towny.object.Town;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradesManager {

    private static TradesManager instance = new TradesManager();

    /**
     *
     *
     * @param String is the town that is getting requested to trade
     * @param Town is the town that is requesting the trade.
     *
     */

    private HashMap<String,Town> tradesHashMap = new HashMap<>();


    public HashMap<String, Town> getTradesHashMap() {
        return tradesHashMap;
    }
    public static TradesManager getInstance() {
        return instance;
    }
}
