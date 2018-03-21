package com.danielrharris.townywars.ideologies;

import com.danielrharris.townywars.Rebellion;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.War;
import com.danielrharris.townywars.WarManager;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.scheduler.BukkitRunnable;

public class Economic extends BukkitRunnable{


    private Town town;
    private War wWar;

    public Economic(Town town){
        this.town = town;
        try {
            this.wWar = WarManager.getWarForNation(town.getNation());
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        double pointPerMoney = TownyWars.getInstance().getConfig().getDouble("moneyPerPoint");
        double balance = 0;
        try {
            balance = town.getHoldingBalance();
        } catch (EconomyException e) {
            e.printStackTrace();
        }

        double pointsToGet =  balance / pointPerMoney;
        double pointsAdded = 0;
        try {
            pointsAdded = wWar.getTownPoints(town) + pointsToGet;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(pointsAdded >= War.getTownMaxPoints(town)){
            wWar.addNationPoint();
        }
    }
}
