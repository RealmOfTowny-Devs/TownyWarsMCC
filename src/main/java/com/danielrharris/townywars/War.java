package main.java.com.danielrharris.townywars;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.java.com.danielrharris.townywars.War.MutableInteger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class War {
	private Nation nation1, nation2;
	private int nation1points, nation2points;
	private Map<Town, MutableInteger> towns = new HashMap<Town, MutableInteger>();

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
				towns.put(TownyUniverse.getDataSource().getTown(temp2[0]), new MutableInteger(Integer.parseInt(temp2[1])));
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
			s += towns.get(town).value + "  ";
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
	
	public Integer getNationPoints(Nation nation) throws Exception {
		if(nation == nation1)
			return nation1points;
		if(nation == nation2)
			return nation2points;
		throw(new Exception("Not registred"));
	}

	public Integer getTownPoints(Town town) throws Exception {
		return towns.get(town).value;
	}

	//rewrite
	public final void recalculatePoints(Nation nat) {
		if(nat.equals(nation1))
			nation1points = nat.getNumTowns();
		else if(nat.equals(nation2))
			nation2points = nat.getNumTowns();
		for (Town town : nat.getTowns()) {
			towns.put(town,
					new MutableInteger((int) (town.getNumResidents()
							* TownyWars.pPlayer + (60-60*Math.pow(Math.E, (-0.00203*town.getTownBlocks().size()))))));
		}
	}

	boolean hasNation(Nation onation) {
		return onation == nation1 || onation == nation2;
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
		towns.get(town).value -= i;
		if (towns.get(town).value <= 0) {
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
				try {	
						WarManager.townremove = town;
						nnation.removeTown(town);
				} catch (Exception ex) {
				}
				nation.addTown(town);
				town.setNation(nation);
				int mr = nnation.getNumTowns() + 1;
				if (mr != 0) {
					mr = (int) (nnation.getHoldingBalance() / mr);
					nnation.pay(mr, "War issues");
					nation.collect(mr);
				}
				TownyUniverse.getDataSource().saveNation(nation);
				TownyUniverse.getDataSource().saveNation(nnation);
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
						winner.collect(looser.getHoldingBalance());
						looser.pay(looser.getHoldingBalance(),
								"Lost the freakin war");
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
				new MutableInteger((int) (town.getNumResidents()
						* TownyWars.pPlayer + TownyWars.pPlot
						* town.getTownBlocks().size())));
	}

	public static void broadcast(Nation n, String message) {
		for (Resident re : n.getResidents()) {
			Player plr = Bukkit.getPlayer(re.getName());
			if (plr != null) {
				plr.sendMessage(message);
			}
		}
	}

	public static class MutableInteger {
		public int value;

		public MutableInteger(int v) {
			this.value = v;
		}
	}
}
