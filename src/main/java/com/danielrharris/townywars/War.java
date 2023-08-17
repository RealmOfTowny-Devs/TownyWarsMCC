package com.danielrharris.townywars;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class War {
    private Object aggressor, defender;  // Changed 'target' to 'defender' for clarity
    private int aggressorPoints, defenderPoints;  // Updated for consistency
    private Map<Town, Double> towns = new HashMap<>();

    private Rebellion rebelwar;

    public War(Object aggressor, Object defender, Rebellion rebellion) {
        this.aggressor = aggressor;
        this.defender = defender;
        recalculatePoints(aggressor);
        recalculatePoints(defender);
        this.rebelwar = rebellion;
    }

    private void validateParameters(Object aggressor, Object defender) {
        if (!(aggressor instanceof Nation || aggressor instanceof Town) ||
                !(defender instanceof Nation || defender instanceof Town)) {
            throw new IllegalArgumentException("Invalid types for War participants");
        }
    }

    public War(String s) {

        ArrayList<String> slist = new ArrayList<String>(Arrays.asList(s.split("   ")));

        aggressor = TownyUniverse.getInstance().getNation(slist.get(0));

        defender = TownyUniverse.getInstance().getNation(slist.get(1));

        aggressorPoints = Integer.parseInt(slist.get(2));

        defenderPoints = Integer.parseInt(slist.get(3));

        String[] temp2 = {"", ""};

        for (String temp : slist.get(4).split("  ")) {
            temp2 = temp.split(" ");
            try {
                towns.put(TownyUniverse.getInstance().getTown(temp2[0]), Double.parseDouble(temp2[1]));
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (slist.get(5).equals("n u l l"))
            rebelwar = null;
        else
            try {
                rebelwar = Rebellion.getRebellionFromName(slist.get(5));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    public War(Nation aggressorNation, Nation defenderNation) {
        this(aggressorNation, defenderNation, null);
    }

    public War(Town aggressorTown, Town defenderTown) {
        this(aggressorTown, defenderTown, null);
    }

    public War(Town aggressorTown, Nation defenderNation) {
        this(aggressorTown, defenderNation, null);
    }

    public War(Nation aggressorNation, Town defenderTown) {
        this(aggressorNation, defenderTown, null);
    }

    public static double getTownMaxPoints(Town town) {
        return (town.getNumResidents() * TownyWars.pPlayer) + (TownyWars.pPlot * town.getTownBlocks().size());
    }

    @SuppressWarnings("deprecation")
    public static void broadcast(Nation n, String message) {
        for (Resident re : n.getResidents()) {
            Player plr = Bukkit.getPlayer(re.getName());
            if (plr != null) {
                plr.sendMessage(message);
            }
        }
    }

    public Rebellion getRebellion() {
        return this.rebelwar;
    }

    // Updated method name for clarity
    public String warStateToString() {
        StringBuilder s = new StringBuilder();

        if (aggressor instanceof Nation) {
            s.append(((Nation) aggressor).getName());
        } else if (aggressor instanceof Town) {
            s.append(((Town) aggressor).getName());
        }

        s.append("   ");

        if (defender instanceof Nation) {
            s.append(((Nation) defender).getName());
        } else if (defender instanceof Town) {
            s.append(((Town) defender).getName());
        }

        s.append("   ");

        s.append(aggressorPoints).append("   ");
        s.append(defenderPoints).append("   ");

        for (Town town : towns.keySet()) {
            s.append(town.getName()).append(" ");
            s.append(towns.get(town)).append("  ");
        }

        if (rebelwar != null)
            s.append(" ").append(rebelwar.getName());
        else
            s.append(" " + "n u l l");

        return s.toString();
    }

    public Set<Object> getEntitiesInWar() {
        HashSet<Object> s = new HashSet<>();
        if (aggressor instanceof Nation) {
            s.add((Nation) aggressor);
        }else{
            s.add((Town) aggressor);
        }
        if (defender instanceof Nation) {
            s.add((Nation) defender);
        }else{
            s.add((Town) defender);
        }
        return s;
    }

    public boolean hasTown(Town town){
        if(getEntitiesInWar().contains(town))
            return true;
        return false;
    }

    public boolean hasNation(Nation nation){
        if(getEntitiesInWar().contains(nation))
            return true;
        return false;
    }

    public void removeTown(Town town, Nation nation) {
        towns.remove(town);
        if (nation.equals(aggressor))
            aggressorPoints--;
        else if (nation.equals(defender))
            defenderPoints--;
    }

    public int getPoints(Object participant) {
        if (participant.equals(aggressor))
            return aggressorPoints;
        else if (participant.equals(defender))
            return defenderPoints;
        else
            throw new IllegalArgumentException("Participant not part of the war");
    }

    public final void recalculatePoints(Object obj) {
        if (obj instanceof Nation) {
            Nation nat = (Nation) obj;
            int points = nat.getNumTowns();
            for (Town town : nat.getTowns()) {
                towns.put(town, getTownMaxPoints(town));
            }
            if (obj.equals(aggressor))
                aggressorPoints = points;
            else if (obj.equals(defender))
                defenderPoints = points;
        } else if (obj instanceof Town) {
            Town town = (Town) obj;
            double points = getTownMaxPoints(town);
            towns.put(town, points);
            if (obj.equals(aggressor))
                aggressorPoints = (int) points;
            else if (obj.equals(defender))
                defenderPoints = (int) points;
        } else {
            throw new IllegalArgumentException("Invalid type for recalculation");
        }
    }

    public void chargeTownPoints(Object obj, Town town, double i) {
        double value = towns.getOrDefault(town, 0.0) - i;
        if (value > 0) {
            towns.replace(town, value);
        } else {
            if (obj instanceof Nation) {
                Nation nnation = (Nation) obj;
                // existing logic for nation...
            } else if (obj instanceof Town) {
                // logic for towns if needed...
            }
        }
    }

    public void removeNationPoint(Object participant) {
        if (participant.equals(aggressor))
            aggressorPoints--;
        else if (participant.equals(defender))
            defenderPoints--;
        else
            throw new IllegalArgumentException("Participant not part of the war");
    }

    public void addNationPoint(Object participant, Town town) {
        if (participant.equals(aggressor))
            aggressorPoints++;
        else if (participant.equals(defender))
            defenderPoints++;
        else
            throw new IllegalArgumentException("Participant not part of the war");

        towns.put(town, getTownMaxPoints(town));
    }

    public void addNewTown(Town newTown, Nation nation) {
        if (!nation.equals(aggressor) && !nation.equals(defender)) {
            throw new IllegalArgumentException("Given nation is not a participant in this war.");
        }

        // Check if the town already exists in the war's towns map
        if (towns.containsKey(newTown)) {
            throw new IllegalArgumentException("This town is already a part of the war.");
        }

        // Add town to the nation
        try {
            nation.addTown(newTown);
            TownyUniverse.getInstance().getDataSource().saveNation(nation);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Add town to the war's towns map with its max points
        towns.put(newTown, getTownMaxPoints(newTown));

        // Update the nation's points in the war
        if (nation.equals(aggressor)) {
            aggressorPoints++;
        } else {
            defenderPoints++;
        }
    }
    public Object getEnemy(Object entity) throws Exception {
        if (entity.equals(aggressor)) {
            return defender;
        } else if (entity.equals(defender)) {
            return aggressor;
        } else {
            throw new Exception("Provided entity is not involved in this war.");
        }
    }

    //tripple space separates objects, double space separates list elements, single space separates map pairs
    public String objectToString() {
        
        StringBuilder s = new StringBuilder(new String(""));
        if(aggressor instanceof Town) {
            s.append(((Town) aggressor).getName()).append("   ");
        } else {
            s.append(((Nation) aggressor).getName()).append("   ");
        }
        if(defender instanceof Town) {
            s.append(((Town) defender).getName()).append("   ");
        } else {
            s.append(((Nation) defender).getName()).append("   ");
        }
        s.append(aggressorPoints).append("   ");
        s.append(defenderPoints).append("   ");
        for (Town town : towns.keySet()) {
            s.append(town.getName()).append(" ");
            s.append(towns.get(town)).append("  ");
        }

        if (rebelwar != null)
            s.append(" ").append(rebelwar.getName());
        else
            s.append(" " + "n u l l");

        return s.toString();
    }
    public Double getTownPoints(Town town) {
        return towns.getOrDefault(town, 0.0);  // return the points or 0.0 if the town is not in the map
    }

    public void setTownPoints(Town town, Double points) {
        this.towns.put(town, points);
    }
}

