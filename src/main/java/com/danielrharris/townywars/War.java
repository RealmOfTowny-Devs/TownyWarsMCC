package com.danielrharris.townywars;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

//import java.io.DataInputStream;
//import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//import main.java.com.danielrharris.townywars.War.MutableInteger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class War {
	private Nation nation1, nation2;
	private int nation1points, nation2points;
	private Map<Town, Double> towns = new HashMap<Town, Double>();

	private Rebellion rebelwar;

	public War(Nation nat, Nation onat, Rebellion rebellion) {
		nation1 = nat;
		nation2 = onat;
		recalculatePoints(nat);
		recalculatePoints(onat);
		this.rebelwar = rebellion;
	}

	public War(Nation nat, Nation onat) {
		this(nat, onat, null);
	}
	
	public War(String s){
		ArrayList<String> slist = new ArrayList<String>();
		
		for(String temp : s.split("   "))
			slist.add(temp);
		
		try {
			nation1 = TownyUniverse.getDataSource().getNation(slist.get(0));
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			nation2 = TownyUniverse.getDataSource().getNation(slist.get(1));
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		nation1points = Integer.parseInt(slist.get(2));
		
		nation2points = Integer.parseInt(slist.get(3));
		
		String temp2[] = {"",""};
		
		for(String temp : slist.get(4).split("  ")){
			temp2 = temp.split(" ");
			try {
				towns.put(TownyUniverse.getDataSource().getTown(temp2[0]), Double.parseDouble(temp2[1]));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(slist.get(5).equals("n u l l"))
			rebelwar = null;
		else
			try {
				rebelwar = Rebellion.getRebellionFromName(slist.get(5));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public Rebellion getRebellion() {
		return this.rebelwar;
	}

	//tripple space separates objects, double space separates list elements, single space separates map pairs
	public String objectToString(){
		String s = new String("");
		
		s += nation1.getName() + "   ";
		s += nation2.getName() + "   ";
		s += nation1points + "   ";
		s += nation2points + "   ";
		
		for(Town town : towns.keySet()){
			s += town.getName() + " ";
			s += towns.get(town) + "  ";
		}
		
		if(rebelwar != null)
			s += " " + rebelwar.getName();
		else
			s += " " + "n u l l";
		
		return s;
	}

	public void setNation1(Nation nation1) {
		this.nation1 = nation1;
	}

	public void setNation2(Nation nation2) {
		this.nation2 = nation2;
	}
	
	public Set<Nation> getNationsInWar() {
		HashSet<Nation> s = new HashSet<Nation>();
		s.add(nation1);
		s.add(nation2);
		return s;
	}
	
	public void removeTown(Town town, Nation nation){
		towns.remove(town);
		if(nation == nation1)
			nation1points--;
		else if(nation == nation2)
			nation2points--;
	}
	
	public Integer getNationPoints(Nation nation) throws Exception {
		if(nation == nation1)
			return nation1points;
		if(nation == nation2)
			return nation2points;
		throw(new Exception("Not registred"));
	}

	public double getTownPoints(Town town) throws Exception {
		return (double)towns.get(town);
	}

	//rewrite
	public final void recalculatePoints(Nation nat) {
		if(nat.equals(nation1))
			nation1points = nat.getNumTowns();
		else if(nat.equals(nation2))
			nation2points = nat.getNumTowns();
		for (Town town : nat.getTowns()) {
			towns.put(town, getTownMaxPoints(town));
		}
	}
	
	public void addNewTown(Town town){
		towns.put(town, getTownMaxPoints(town));
	}
	
	public static double getTownMaxPoints(Town town){
		return (town.getNumResidents()
				* TownyWars.pPlayer) + (TownyWars.pPlot
				* town.getTownBlocks().size());
	}

	public boolean hasNation(Nation onation) {
		if(onation != nation1 && onation != nation2 && (onation.getName().equals(nation1.getName()) || onation.getName().equals(nation2.getName())))
				System.out.println("hasNation() error. Please report to Noxer");
		return (onation.getName().equals(nation1.getName()) || onation.getName().equals(nation2.getName()));
	}

	public Nation getEnemy(Nation onation) throws Exception {
			if (nation1 == onation) {
				return nation2;
			}
			if (nation2 == onation) {
				return nation1;
			}
		throw new Exception("War.getEnemy: Specified nation is not in war.");
	}

	public void chargeTownPoints(Nation nnation, Town town, double i) {
		double value = towns.get(town) - i;
		if(value > 0){
			towns.replace(town, value);
		}
		if (value <= 0) {
			try {
				if(nnation.getTowns().size() > 1 && nnation.getCapital() == town){
					if(nnation.getTowns().get(0) != town){
						nnation.setCapital(nnation.getTowns().get(0));
					}else{
						nnation.setCapital(nnation.getTowns().get(1));
					}
				}
					
					
				towns.remove(town);
				Nation nation = WarManager.getWarForNation(nnation).getEnemy(nnation);
				removeNationPoint(nnation);
				addNationPoint(nation, town);
				try {	
						WarManager.townremove = town;
						nnation.removeTown(town);
				} catch (Exception ex) {
				}
				nation.addTown(town);
				town.setNation(nation);
				TownyUniverse.getDataSource().saveNation(nation);
				TownyUniverse.getDataSource().saveNation(nnation);
				try {
					WarManager.save();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				broadcast(
						nation,
						ChatColor.GREEN
								+ town.getName()
								+ " has been conquered and joined your nation in the war!");
			} catch (Exception ex) {
				Logger.getLogger(War.class.getName()).log(Level.SEVERE, null,
						ex);
			}
		}
		try {
			if (this.getNationPoints(nnation) <= 0) {
				try {
						Nation winner = getEnemy(nnation);
						Nation looser = nnation;
						boolean endWarTransfersDone = false;
						for(Rebellion r : Rebellion.getAllRebellions()){
							if(r.getRebelnation() == winner){
								winner.getCapital().collect(winner.getHoldingBalance());
								winner.pay(winner.getHoldingBalance(), "You are disbanded. You don't need money.");
								endWarTransfersDone = true;
								break;
							}
						}
						
						if(!endWarTransfersDone){
							winner.collect(looser.getHoldingBalance());
							looser.pay(looser.getHoldingBalance(), "Conquered. Tough luck!");
						}
						WarManager.endWar(winner, looser, false);

				} catch (Exception ex) {
					Logger.getLogger(War.class.getName()).log(Level.SEVERE, null,
							ex);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeNationPoint(Nation nation) {
		if(nation1 == nation)
			nation1points--;
		if(nation2 == nation)
			nation2points--;
	}

	public void addNationPoint(Nation nation, Town town) {
		if(nation1 == nation)
			nation1points++;
		if(nation2 == nation)
			nation2points++;
		towns.put(town,
				(town.getNumResidents()
						* TownyWars.pPlayer + TownyWars.pPlot
						* town.getTownBlocks().size()));
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
}