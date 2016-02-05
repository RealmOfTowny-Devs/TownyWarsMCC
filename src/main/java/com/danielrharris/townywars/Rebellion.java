package com.danielrharris.townywars;

import java.util.ArrayList;
import java.util.List;

//import main.java.com.danielrharris.townywars.War.MutableInteger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.EmptyNationException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

//Author: Noxer
public class Rebellion {

	private static ArrayList<Rebellion> allRebellions = new ArrayList<Rebellion>();
	private Nation motherNation;
	private Nation rebelnation;
	private String name;
	private Town leader;
	private List<Town> originalMotherTowns = new ArrayList<Town>();
	private List<Town> rebels = new ArrayList<Town>();
	
	public Rebellion(Nation mn, String n, Town l){
		this.motherNation = mn;
		this.name = n;
		this.leader = l;
		allRebellions.add(this);
	}
	
	//create new rebellion from savefile string
	public Rebellion(String s){
		ArrayList<String> slist = new ArrayList<String>();
		
		for(String temp : s.split("  "))
			slist.add(temp);
		
		try {
			motherNation = TownyUniverse.getDataSource().getNation(slist.get(0));
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			if(!slist.get(1).equals("n u l l"))
				rebelnation = TownyUniverse.getDataSource().getNation(slist.get(1));
			else
				rebelnation = null;
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ahhhhhh");
		}
			
		name = slist.get(2);
			
			try {
				leader = TownyUniverse.getDataSource().getTown(slist.get(3));
			} catch (NotRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if(!slist.get(4).equals("e m p t y")){
			for(String temp : slist.get(4).split(" "))
				try {
					originalMotherTowns.add(TownyUniverse.getDataSource().getTown(temp));
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		if(!slist.get(5).equals("e m p t y")){
			for(String temp : slist.get(5).split(" "))
				try {
					rebels.add(TownyUniverse.getDataSource().getTown(temp));
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static Rebellion getRebellionFromName(String s) throws Exception{
		for(Rebellion r : allRebellions)
			if(r.getName().equals(s))
				return r;
		throw(new Exception("Rebellion not found!"));
	}
	
	public Town getLeader() {
		return leader;
	}
	
	public List<Town> getRebels() {
		return rebels;
	}
	
	public void Execute(CommandSender cs){
		try {
			TownyUniverse.getDataSource().newNation(name + "-rebels");
		} catch (AlreadyRegisteredException e2) {
			cs.sendMessage(ChatColor.RED + "Error: A nation with the name of your rebellion already exists.");
			return;
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			rebelnation = TownyUniverse.getDataSource().getNation(name + "-rebels");
		} catch (NotRegisteredException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {
			motherNation.removeTown(leader);
		} catch (NotRegisteredException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (EmptyNationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			rebelnation.addTown(leader);
			TownyUniverse.getDataSource().saveTown(leader);
		} catch (AlreadyRegisteredException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for(Town town : rebels){
			try {
				try {
					motherNation.removeTown(town);
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (EmptyNationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				rebelnation.addTown(town);
				TownyUniverse.getDataSource().saveTown(town);
			} catch (AlreadyRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(Town town : motherNation.getTowns()){
			originalMotherTowns.add(town);
		}
		
		rebelnation.setCapital(leader);
		try {
			rebelnation.setKing(leader.getMayor());
		} catch (TownyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		WarManager.createWar(rebelnation, motherNation, cs, this);
		TownyUniverse.getDataSource().saveTown(leader);
		TownyUniverse.getDataSource().saveNation(rebelnation);
		TownyUniverse.getDataSource().saveNationList();
		try {
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cs.sendMessage(ChatColor.RED + "You executed your rebellion and are now at war with your nation!");
	}
	
	public void success(){
		ArrayList<Town> townsToBeMoved = new ArrayList<Town>();
		ArrayList<Town> townsToBeRemoved = new ArrayList<Town>();
		for(Town town : rebelnation.getTowns()){
			if(originalMotherTowns.contains(town))
				townsToBeMoved.add(town);
			else
				townsToBeRemoved.add(town);
		}
		
		for(Town town : townsToBeMoved){
			try {
				rebelnation.removeTown(town);
				town.setNation(null);
				motherNation.addTown(town);
			} catch (AlreadyRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EmptyNationException e) {
				;
			}
		}
		
		for(Town town : townsToBeRemoved){
			try {
				rebelnation.removeTown(town);
			} catch (NotRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EmptyNationException e) {
				//exception WILL be created. Ignore.
				;
			}
		}
		
		TownyUniverse.getDataSource().saveNation(motherNation);
		TownyUniverse.getDataSource().saveNation(rebelnation);
		try {
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void peace(){
		try {
			motherNation.collect(rebelnation.getHoldingBalance());
			rebelnation.pay(rebelnation.getHoldingBalance(), "Lost rebellion. Tough luck!");
		} catch (EconomyException e1) {
			e1.printStackTrace();
		}
		
		ArrayList<Town> l = new ArrayList<Town>(rebelnation.getTowns());
		for(Town town : l)
			try {
				WarManager.townremove = town;
				try {
					rebelnation.removeTown(town);
				} catch (EmptyNationException e) {
					; 
				}
				motherNation.addTown(town);
			} catch (AlreadyRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotRegisteredException e) {
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
	
	public Nation getRebelnation() {
		return rebelnation;
	}

	public boolean isRebelTown(Town town){
		for(Town rebel : rebels)
			if(town == rebel)
				return true;
		return false;
	}
	
	public boolean isRebelLeader(Town town){
		return town == leader;
	}
	
	public String getName(){
		return name;
	}
	
	public static ArrayList<Rebellion> getAllRebellions(){
		return allRebellions;
	}
	
	public Nation getMotherNation(){
		return motherNation;
	}
	
	public void addRebell(Town town){
		rebels.add(town);
	}
	
	public void removeRebell(Town town){
		rebels.remove(town);
	}
	
	//double space separates objects, single space separates list elements
	public String objectToString(){
		String s = new String("");
		s += motherNation.getName() + "  ";
		
		if(rebelnation != null)
			s += rebelnation.getName() + "  ";
		else
			s += "n u l l" + "  ";
		
		s += name + "  ";
		
		s += leader.getName() + "  ";
		
		if(!originalMotherTowns.isEmpty()){
			for(Town town : originalMotherTowns)
				s += town.getName() + " ";
			
			s += " ";
		}
		else
			s += "e m p t y" + "  ";
		
		if(!rebels.isEmpty()){
			for(Town town : rebels)
				s += town.getName() + " ";
			
			s = s.substring(0, s.length()-1);
		}
		else
			s += "e m p t y";
		
		return s;
	}
}