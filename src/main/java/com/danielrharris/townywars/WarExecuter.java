package com.danielrharris.townywars;

import com.danielrharris.townywars.listeners.GriefListener;
import com.danielrharris.townywars.tasks.ShowDPTask;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import me.drkmatr1984.BlocksAPI.utils.SBlock;
import me.drkmatr1984.BlocksAPI.utils.Utils;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;


class WarExecutor implements CommandExecutor
{
  private TownyWars plugin;
  private GriefManager gm;
 
   public WarExecutor(TownyWars aThis)
  {
    this.plugin = aThis;
    this.gm = new GriefManager(plugin);
  }
  
  public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings)
  {
	boolean unknownCommand=true;
	DecimalFormat d = new DecimalFormat("#.00");
    if (strings.length == 0)
    {
    	unknownCommand=false;
      cs.sendMessage(ChatColor.GREEN + "For help with TownyWars, type /twar help");
      return true;
    }
    String farg = strings[0];
    if (farg.equals("reload"))
    {
    	unknownCommand=false;
      if (!cs.hasPermission("townywars.admin")) {
        return false;
      }
      cs.sendMessage(ChatColor.GREEN + "Reloading plugin...");
      PluginManager pm = Bukkit.getServer().getPluginManager();
      pm.disablePlugin(this.plugin);
      pm.enablePlugin(this.plugin);
      cs.sendMessage(ChatColor.GREEN + "Plugin reloaded!");
    }
    if (farg.equals("help"))
    {
      Player p;
      Resident res;  
      cs.sendMessage(ChatColor.GREEN + "Towny Wars Help:");
      cs.sendMessage(ChatColor.AQUA + "/twar - " + ChatColor.YELLOW + "Displays the TownyWars configuration information");
      cs.sendMessage(ChatColor.AQUA + "/twar help - " + ChatColor.YELLOW + "Displays the TownyWars help page");
      cs.sendMessage(ChatColor.AQUA + "/twar status - " + ChatColor.YELLOW + "Displays a list of on-going wars");
      cs.sendMessage(ChatColor.AQUA + "/twar status [nation] - " + ChatColor.YELLOW + "Displays a list of the nation's towns and their defense points");
      if(cs.hasPermission("townywars.leader")){
    	  cs.sendMessage(ChatColor.AQUA + "/twar repair - " + ChatColor.YELLOW + "Allows you to repair war grief by paying money from town bank");
      }  
      cs.sendMessage(ChatColor.AQUA + "/twar showtowndp - " + ChatColor.YELLOW + "Shows your towns current defense points.");
      cs.sendMessage(ChatColor.AQUA + "/twar showtownmaxdp - " + ChatColor.YELLOW + "Shows your towns max defense points.");
      if(cs.hasPermission("townywars.leader")){
    	  cs.sendMessage(ChatColor.AQUA + "/twar declare [nation] - " + ChatColor.YELLOW + "Starts a war with another nation (REQUIRES YOU TO BE A KING/ASSISTANT)");
          cs.sendMessage(ChatColor.AQUA + "/twar end - " + ChatColor.YELLOW + "Request from enemy nations king to end the ongoing war. (REQUIRES YOU TO BE A KING/ASSISTANT)");
      }
      if(cs instanceof Player){
    	  p = (Player) cs;
    	  try {
			res = TownyUniverse.getDataSource().getResident(p.getName());
			if(res.getTown().getNation().getCapital() != res.getTown()){
				cs.sendMessage(ChatColor.AQUA + "/twar createrebellion [name] - " + ChatColor.YELLOW + "Creates a (secret) rebellion within your nation.");
			    cs.sendMessage(ChatColor.AQUA + "/twar joinrebellion [name] - " + ChatColor.YELLOW + "Joins a rebellion within your nation using the name.");
			    cs.sendMessage(ChatColor.AQUA + "/twar leaverebellion - " + ChatColor.YELLOW + "Leaves your current rebellion.");
			    cs.sendMessage(ChatColor.AQUA + "/twar showrebellion - " + ChatColor.YELLOW + "Shows your current rebellion and its members.");
			    cs.sendMessage(ChatColor.AQUA + "/twar executerebellion - " + ChatColor.YELLOW + "Executes your rebellion and you go to war with your nation (requires to be leader of rebellion).");
	    	}
		} catch (NotRegisteredException e) {
			return true;
		}   	  
      }
      if (cs.hasPermission("townywars.admin"))
      {
        cs.sendMessage(ChatColor.AQUA + "/twar reload - " + ChatColor.YELLOW + "Reload the plugin");
        cs.sendMessage(ChatColor.AQUA + "/twar astart [nation] [nation] - " + ChatColor.YELLOW + "Forces two nations to go to war");
        cs.sendMessage(ChatColor.AQUA + "/twar aend [nation] [nation] - " + ChatColor.YELLOW + "Forces two nations to stop a war");
        cs.sendMessage(ChatColor.AQUA + "/twar aaddtowndp [town] - " + ChatColor.YELLOW + "Adds a DP to the town");
        cs.sendMessage(ChatColor.AQUA + "/twar aremovetowndp [town] - " + ChatColor.YELLOW + "Removes a DP from the town");
      }
      return true;
    }
    War w;
    if (farg.equals("status"))
    {
    	unknownCommand=false;
      if (strings.length == 1)
      {
        cs.sendMessage(ChatColor.GREEN + "List of on-going wars:");
        for (War war : WarManager.getWars())
        {
          Nation first = null;
          Nation second = null;
          for (Nation st : war.getNationsInWar()) {
            if (first == null) {
              first = st;
            } else {
              second = st;
            }
          }
          try {
			cs.sendMessage(ChatColor.GREEN + first.getName() + " " + war.getNationPoints(first) + " vs. " + second.getName() + " " + war.getNationPoints(second));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
        return true;
      }
      String onation = strings[1];
      Nation t;
      try
      {
        t = TownyUniverse.getDataSource().getNation(onation);
      }
      catch (NotRegisteredException ex)
      {
        cs.sendMessage(ChatColor.GOLD + "No nation called " + onation + " could be found!");
        return true;
      }
      w = WarManager.getWarForNation(t);
      if (w == null)
      {
        cs.sendMessage(ChatColor.RED + "That nation isn't in a war!");
        return true;
      }
      cs.sendMessage(t.getName() + " war info:");
      for (Town tt : t.getTowns()) {
        try {
			cs.sendMessage(ChatColor.GREEN + tt.getName() + ": " + w.getTownPoints(tt) + " points");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      return true;
    }
    if (farg.equals("repair"))
    {	
    	unknownCommand=false;
    	if(cs.hasPermission("townywars.leader")){
	    	if(cs instanceof Player){
	    		Player p = (Player) cs;	        	
	        	int numBlocks = 0;
	        	Town town = null;
	        	double price = 0;
	        	try {
					town = TownyUniverse.getDataSource().getResident(p.getName()).getTown();
					if(GriefListener.getGriefedBlocks()!=null){
						if(!GriefListener.getGriefedBlocks().isEmpty()){
							for(SBlock b : GriefListener.getGriefedBlocks().get(town)){
								if(b.getType()!=Material.AIR && !Utils.isOtherAttachable(b.getType())){
									numBlocks++;
								}
							}
						}
					}
					price = Math.round((numBlocks * TownyWars.pBlock)*1e2)/1e2;
	    		} catch (NotRegisteredException e) {
					p.sendMessage(ChatColor.RED + "You are not in a Town!");
					return true;
				}		
	    		if (strings.length == 1){
	    			if(town!=null){
		    			if(numBlocks > 0){
		    				//Rollback everything, charge for block destructions
		    				p.sendMessage(ChatColor.GREEN + "Price to Repair " + town.getName() + ChatColor.WHITE + ": $" + ChatColor.YELLOW + d.format(price));
		    				p.sendMessage("   " + ChatColor.MAGIC + "l" + ChatColor.RESET + "  " + ChatColor.BOLD + ChatColor.GOLD + "Repair?" + ChatColor.RESET + "  " + ChatColor.MAGIC + "l");
		    				sendYesNoMessage(p);
		    			}else if(numBlocks == 0 && GriefListener.getGriefedBlocks().get(town)!=null && GriefListener.getGriefedBlocks().get(town).size() > 0){
		    				p.sendMessage(ChatColor.GREEN + "Price to Repair " + town.getName() + ChatColor.WHITE + ": " + ChatColor.YELLOW + "FREE!");
		    				p.sendMessage("   " + ChatColor.MAGIC + "l" + ChatColor.RESET + "  " + ChatColor.BOLD + ChatColor.GOLD + "Repair?" + ChatColor.RESET + "  " + ChatColor.MAGIC + "l");
		    				sendYesNoMessage(p);
		    				//rollback block places only (free)
		    			}else if((numBlocks == 0 && GriefListener.getGriefedBlocks().get(town)==null) || (numBlocks == 0 && GriefListener.getGriefedBlocks().get(town)!=null && GriefListener.getGriefedBlocks().get(town).isEmpty())){
		    				p.sendMessage(ChatColor.GREEN + "Nothing to Repair");
		    			}
	    			}
	        	}
	    		if (strings.length == 2){
	    			if(town!=null){
	        			if(GriefListener.getGriefedBlocks().get(town)!=null){
	            			if(!GriefListener.getGriefedBlocks().get(town).isEmpty()){
	            				String response = ChatColor.stripColor(strings[1]).toLowerCase();
	                			if(response.equals("yes")){
	                				try {
	            						if(town.canPayFromHoldings(price)){
	            							town.pay(price, "repairs");
	            							p.sendMessage("");
	            							p.sendMessage(ChatColor.GREEN+ "Repairs are underway!");
	            							p.sendMessage(ChatColor.AQUA + "New Town Balance: " + ChatColor.YELLOW + town.getHoldingFormattedBalance());  				
	            		    				gm.rollbackBlocks(town);
	            						}else{
	            							p.sendMessage("");
	            		    				p.sendMessage(ChatColor.DARK_RED + town.getName() + " does not have enough money to pay for repairs.");
	            						}
	            					} catch (EconomyException e) {
	            						// TODO Auto-generated catch block
	            						e.printStackTrace();
	            					}
	                			}
	                			if(response.equals("no")){
	                				p.sendMessage("");
	                				p.sendMessage(ChatColor.DARK_RED + "Canceling Repair...");
	                				return true;
	                			}
	            			}
	            		}
	        		}
	    		}
	        }
    	}else{
    		cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
    	}
    }
    
    if (farg.equals("showtownmaxdp")){
    	unknownCommand=false;
    	Town town = null;
    	try {
    		Resident re = TownyUniverse.getDataSource().getResident(cs.getName());
    		if(re.hasTown()){
    			town = re.getTown();
    			Double points = War.getTownMaxPoints(town);
    	    	String proper = d.format(points);
    	    	if(TownyWars.isBossBar){
    	    		new ShowDPTask(town, plugin).runTask(plugin);
    	    	}
    	    	cs.sendMessage(ChatColor.YELLOW + "Your town's max defense value is currently " +  proper + " defense points!");
    	    	cs.sendMessage(ChatColor.YELLOW + "Claim more land or get more residents to increase it!");
    	    	return true;
    		}else{
    			cs.sendMessage(ChatColor.RED + "You are not in a Town!");
    		}		
		} catch (NotRegisteredException e) {
			cs.sendMessage(ChatColor.RED + "You are not in a Town!");
			return true;
		}
    	
    }
    
    if (farg.equals("showtowndp")){
    	unknownCommand=false;
    	Town town = null;
    	Double points = null;
    	War wwar = null;
    	try {
    		Resident re = TownyUniverse.getDataSource().getResident(cs.getName());
    		if(re.hasTown()){
    			town = TownyUniverse.getDataSource().getResident(cs.getName()).getTown();
    			if(town.hasNation()){
    				try {
    					wwar = WarManager.getWarForNation(town.getNation());
    					if(wwar!=null){			
    						try {
    							points = wwar.getTownPoints(town);
    							if(points!=null){
    					    		String proper = d.format(points);
    					    		if(TownyWars.isBossBar){
    				    	    		new ShowDPTask(town, plugin).runTask(plugin);
    				    	    	}
    					        	cs.sendMessage(ChatColor.YELLOW + "Your town's defense value is currently " +  proper + " defense points!");
    					        	cs.sendMessage(ChatColor.YELLOW + "Your points will return to max value when the war ends!");
    					        	return true;
    					    	}
    						} catch (Exception e) {
    							e.printStackTrace();
    							return true;
    						}
    					}else{
    						if(TownyWars.isBossBar){
    							points = War.getTownMaxPoints(town);
    			    	    	String proper = d.format(points);
    		    	    		new ShowDPTask(town, plugin).runTask(plugin);
    		    	    		cs.sendMessage(ChatColor.YELLOW + "Your town's max defense value is currently " +  proper + " defense points!");
    		        	    	cs.sendMessage(ChatColor.YELLOW + "Claim more land or get more residents to increase it!");
    		    	    	}
    					}
    				} catch (Exception e1) {
    					cs.sendMessage(ChatColor.RED + "You are not in a Nation!");
    					return true;
    				}
    			}
    		}else{
    			cs.sendMessage(ChatColor.RED + "You are not in a Town!");
    		}
		} catch (NotRegisteredException e) {
			cs.sendMessage(ChatColor.RED + "You are not in a Town!");
			return true;
		}
    }
    
    if (farg.equals("neutral"))
    {
    	unknownCommand=false;
      if (!cs.hasPermission("townywars.neutral"))
      {
        cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
        return true;
      }
      Nation csNation;
      try
      {
        Town csTown = TownyUniverse.getDataSource().getResident(cs.getName()).getTown();
        csNation = TownyUniverse.getDataSource().getTown(csTown.toString()).getNation();
      }
      catch (NotRegisteredException ex)
      {
        cs.sendMessage(ChatColor.RED + "You are not not part of a town, or your town is not part of a nation!");
        Logger.getLogger(WarExecutor.class.getName()).log(Level.SEVERE, null, ex);
        return true;
      }
      if ((!cs.isOp()) && (!csNation.toString().equals(strings[1])))
      {
        cs.sendMessage(ChatColor.RED + "You may only set your own nation to neutral, not others.");
        return true;
      }
      if (strings.length == 0) {
        cs.sendMessage(ChatColor.RED + "You must specify a nation to toggle neutrality for (eg. /twar neutral [nation]");
      }
      if (strings.length == 1)
      {
        String onation = strings[1];
        Nation t;
        try
        {
          t = TownyUniverse.getDataSource().getNation(onation);
        }
        catch (NotRegisteredException ex)
        {
          cs.sendMessage(ChatColor.GOLD + "The nation called " + onation + " could not be found!");
          return true;
        }
        WarManager.neutral.put(t.toString(), 0D);
      }
    }
    if (farg.equals("astart"))
    {
    	unknownCommand=false;
      if (!cs.hasPermission("townywars.admin"))
      {
        cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
        return true;
      }
      return declareWar(cs, strings, true);
    }
    if (farg.equals("declare")) {
    	unknownCommand=false;
      return declareWar(cs, strings, false);
    }
    if (farg.equals("end")) {
    	unknownCommand=false;
      return declareEnd(cs, strings, false);
    }
    if (farg.equals("createrebellion")) {
    	unknownCommand=false;
        return createRebellion(cs,strings, false);
      }
    if (farg.equals("joinrebellion")) {
    	unknownCommand=false;
    	return joinRebellion(cs,strings, false);
    }
    if (farg.equals("leaverebellion")) {
    	unknownCommand=false;
    	return leaveRebellion(cs, strings, false);
    }
    if(farg.equals("executerebellion")) {
    	unknownCommand=false;
      return executeRebellion(cs, strings, false);
    }
    if(farg.equals("showrebellion")) {
    	unknownCommand=false;
    	return showRebellion(cs, strings, false);
    }
    if (farg.equals("aend"))
    {
    	unknownCommand=false;
      if (!cs.hasPermission("townywars.admin"))
      {
        cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
        return true;
      }
      return declareEnd(cs, strings, true);
    }
    if(farg.equals("aaddtowndp")){
    	unknownCommand=false;
    	if (!cs.hasPermission("townywars.admin"))
        {
          cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
          return true;
        }
        return addTownDp(cs, strings);
    }
    if(farg.equals("aremovetowndp")){
    	unknownCommand=false;
    	if (!cs.hasPermission("townywars.admin"))
        {
          cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
          return true;
        }
        return removeTownDp(cs, strings);
    }
    /*try {
    	Resident targetResident = TownyUniverse.getDataSource().getResident(farg);
    	Town targetTown = null;
    	Nation targetNation = null;
    	try {
    		targetTown = targetResident.getTown();
    		try {
        		targetNation = targetResident.getTown().getNation();
        	}
        	catch (NotRegisteredException ex) { }
    	}
    	catch (NotRegisteredException ex) { }
    	String townName = "none";
    	String nationName = "none";
    	if (targetTown!=null) {
    		townName=targetTown.getName();
    	}
    	if (targetNation!=null) {
    		nationName=targetNation.getName();
    	}
    	long lastOnline = targetResident.getLastOnline();
    	long currentTime = System.currentTimeMillis();
    	String onlineState = "offline";
    	if (currentTime-lastOnline<1000) { onlineState = "online"; }
    	cs.sendMessage(ChatColor.GREEN + targetResident.getName()+" ("+onlineState+")");
    	cs.sendMessage(ChatColor.GREEN + "--------------------");
    	if (townName.compareTo("none")==0) {
    		cs.sendMessage("Town: "+ChatColor.GRAY+townName);
    	}
    	else {
    		cs.sendMessage("Town: "+ChatColor.GREEN+townName);
    	}
    	if (nationName.compareTo("none")==0) {
    		cs.sendMessage("Nation: "+ChatColor.GRAY+nationName);
    	}
    	else {
    		cs.sendMessage("Nation: "+ChatColor.GREEN+nationName);
    	}
    	cs.sendMessage(ChatColor.GREEN + "--------------------");
    	unknownCommand=false;
    }
	catch (NotRegisteredException ex)
    {
		cs.sendMessage(ChatColor.RED + farg + " does not exist!");
    }*/
    if (unknownCommand) {
    	cs.sendMessage(ChatColor.RED + "Unknown twar command.");
    }
    return true;
  }
  
  private boolean addTownDp(CommandSender cs, String[] strings) {
	Town town = null;
	if(strings.length != 2){
		cs.sendMessage(ChatColor.RED + "You need to specify a town!");
		return false;
	}
	
	try {
		town = TownyUniverse.getDataSource().getTown(strings[1]);
	} catch (NotRegisteredException e) {
		cs.sendMessage(ChatColor.RED + "Town doesn't exist!");
		return false;
	}
	
	for(War war : WarManager.getWars())
		for(Nation nation : war.getNationsInWar())
			if(nation.hasTown(strings[1])){
				try {
					war.chargeTownPoints(town.getNation(), town, -1);
					cs.sendMessage(ChatColor.YELLOW + "Added a DP to " + town.getName());
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
	return false;
  }
  
  private boolean removeTownDp(CommandSender cs, String[] strings) {
		Town town = null;
		if(strings.length != 2){
			cs.sendMessage(ChatColor.RED + "You need to specify a town!");
			return false;
		}
		
		try {
			town = TownyUniverse.getDataSource().getTown(strings[1]);
		} catch (NotRegisteredException e) {
			cs.sendMessage(ChatColor.RED + "Town doesn't exist!");
			return false;
		}
			
		for(War war : WarManager.getWars())
			for(Nation nation : war.getNationsInWar())
				if(nation.hasTown(strings[1])){
					try {
						war.chargeTownPoints(town.getNation(), town, 1);
						cs.sendMessage(ChatColor.YELLOW + "Removed a DP from " + town.getName());
					} catch (NotRegisteredException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return true;
				}
		return false;
	}

private boolean showRebellion(CommandSender cs, String[] strings, boolean admin) {
	  
	  Resident res = null;
	  try {
		res = TownyUniverse.getDataSource().getResident(cs.getName());
	  } catch (NotRegisteredException e3) {
		// TODO Auto-generated catch block
		e3.printStackTrace();
		}
	  
	  try {
			if ((!admin) && (!res.getTown().isMayor(res)))
			  {
			      cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
			      return true;
			  }
		} catch (NotRegisteredException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	  
	  for(Rebellion r : Rebellion.getAllRebellions()){
			try {
				if(r.isRebelTown(res.getTown()) || r.isRebelLeader(res.getTown())){
						cs.sendMessage(ChatColor.YELLOW + ".oOo.___________.[ " + r.getName() + " (Rebellion) ].___________.oOo.");
						cs.sendMessage(ChatColor.GREEN + "Nation: " + r.getMotherNation().getName());
						cs.sendMessage(ChatColor.GREEN + "Leader: " + r.getLeader().getName());
						String members = new String("");
						for(Town town : r.getRebels())
							members = members + ", " + town.getName();
						if(!members.isEmpty())
							members = members.substring(1);
						cs.sendMessage(ChatColor.GREEN + "Members: " + members);
						return true;
				}
			} catch (NotRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  }
	  
	  cs.sendMessage(ChatColor.RED + "You are not in a rebellion!");
	  return true;
  }

//Author: Noxer
  private boolean createRebellion(CommandSender cs, String[] strings, boolean admin){
	  
	  Resident res = null;
	try {
		res = TownyUniverse.getDataSource().getResident(cs.getName());
	} catch (NotRegisteredException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	  
	  if(strings.length != 2){
	  	cs.sendMessage(ChatColor.RED + "You need to give your rebellion a name!");
	  	return true;
	  }
	  
	  try {
		if((!admin) && (!res.getTown().hasNation())){
			cs.sendMessage(ChatColor.RED + "You are not in a nation!");
			return true;
		  }
	} catch (NotRegisteredException e3) {
		// TODO Auto-generated catch block
		e3.printStackTrace();
	}
	  
	  try {
		if ((!admin) && (!res.getTown().isMayor(res)))
		  {
			  cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
			  return true;
		  }
	} catch (NotRegisteredException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	  
	  try {
		if (res.getTown().getNation().getCapital() == res.getTown())
		  {
			  cs.sendMessage(ChatColor.RED + "You cannot create a rebellion (towards yourself) when you are the capital!");
			  return true;
		  }
	} catch (NotRegisteredException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	  
	  for(Rebellion r : Rebellion.getAllRebellions()){
		  try {
			if(r.isRebelTown(res.getTown()) || r.isRebelLeader(res.getTown())){
			  		cs.sendMessage(ChatColor.RED + "You are already in a rebellion!");
			      	return true;
			  }
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  for(Rebellion r : Rebellion.getAllRebellions())
			if(r.getName() == strings[1]){
				  cs.sendMessage(ChatColor.RED + "Rebellion with that name already exists!");
				  return true;
			  }
	  if(strings[1].length() > 13){
		  cs.sendMessage(ChatColor.RED + "Rebellion name too long (max 13)!");
		  return true;
	  }
	  try {
		new Rebellion(res.getTown().getNation(), strings[1], res.getTown());
		cs.sendMessage(ChatColor.YELLOW + "You created the rebellion " + strings[1] + " in your nation!");
	} catch (NotRegisteredException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return true;
  }
  
  //Author: Noxer
  @SuppressWarnings("deprecation")
  private boolean joinRebellion(CommandSender cs, String[] strings, boolean admin)
  {
	  Resident res = null;
	  try {
		res = TownyUniverse.getDataSource().getResident(cs.getName());
	} catch (NotRegisteredException e3) {
		// TODO Auto-generated catch block
		e3.printStackTrace();
	}
	  
	  if(strings.length != 2){
		  	cs.sendMessage(ChatColor.RED + "You need to specify which rebellion to join!");
		  	return true;
	 }
	  
	  try {
		if ((!admin) && (!res.getTown().isMayor(res)))
		  {
		      cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
		      return true;
		  }
	} catch (NotRegisteredException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	  
	  try {
		if (res.getTown().getNation().getCapital() == res.getTown())
		  {
			  cs.sendMessage(ChatColor.RED + "You cannot join a rebellion (towards yourself) when you are the capital!");
			  return true;
		  }
	} catch (NotRegisteredException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	  
	  for(Rebellion r : Rebellion.getAllRebellions()){
		  try {
			if(r.isRebelTown(res.getTown()) || r.isRebelLeader(res.getTown())){
			  		cs.sendMessage(ChatColor.RED + "You are already in a rebellion!");
			      	return true;
			  }
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  for(Rebellion r : Rebellion.getAllRebellions()){
	  	try {
			if(r.getName().equals(strings[1]	) && res.getTown().getNation() == r.getMotherNation()){
				try {
					r.addRebell(res.getTown());
					cs.sendMessage(ChatColor.YELLOW + "You join the rebellion " + r.getName() + "!");
					Bukkit.getPlayer(r.getLeader().getMayor().getName()).sendMessage(ChatColor.YELLOW + res.getTown().getName() + " joined your rebellion!");
					return true;
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	cs.sendMessage(ChatColor.YELLOW + "No rebellion with that name!");
	return true;
  }
  
  //Author: Noxer
  private boolean leaveRebellion(CommandSender cs, String[] strings, boolean admin){
	  
	Resident res = null;
	
	try {
		res = TownyUniverse.getDataSource().getResident(cs.getName());
	} catch (NotRegisteredException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	
	try {
		if ((!admin) && (!res.getTown().isMayor(res)))
		  {
		      cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
		      return true;
		  }
	} catch (NotRegisteredException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	  
	  for(Rebellion r : Rebellion.getAllRebellions())
		try {
			if(r.isRebelLeader(res.getTown())){
				Rebellion.getAllRebellions().remove(r);
				cs.sendMessage(ChatColor.RED + "You disbanded your rebellion in your nation!");
				return true;
			}
			else if(r.isRebelTown(res.getTown())){
				r.removeRebell(res.getTown());
				cs.sendMessage(ChatColor.RED + "You left the rebellion in your nation!");
				return true;
			}
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
	  cs.sendMessage(ChatColor.RED + "You are not in a rebellion!");
	  return true;
  }
  
  //Author: Noxer
  private boolean executeRebellion(CommandSender cs, String[] strings, boolean admin){

	  Resident res = null;
	  
	  try {
			res = TownyUniverse.getDataSource().getResident(cs.getName());
		} catch (NotRegisteredException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	  
	  try {
		if ((!admin) && (!res.getTown().isMayor(res)))
		  {
		      cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
		      return true;
		  }
	} catch (NotRegisteredException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
	  try {
		if (WarManager.getWarForNation(res.getTown().getNation()) != null)
		  {
		      cs.sendMessage(ChatColor.RED + "You can't rebel while your nation is at war!");
		      return true;
		  }
	} catch (NotRegisteredException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
	  for(Rebellion r : Rebellion.getAllRebellions())
		try {
			if(res.getTown().getNation() == r.getMotherNation() && r.isRebelLeader(res.getTown())){
				  r.Execute(cs);
				  return true;
			}
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
	  cs.sendMessage(ChatColor.RED + "You are not in a rebellion!");
      return true;
  }
  
  private boolean declareEnd(CommandSender cs, String[] strings, boolean admin)
  {
    if ((admin) && (strings.length <= 2))
    {
      cs.sendMessage(ChatColor.RED + "You need to specify two nations!");
      return true;
    }
    String sonat = "";
    if (admin) {
      sonat = strings[1];
    }
    Resident res = null;
    Nation nat;
    try
    {
      if (admin)
      {
        nat = TownyUniverse.getDataSource().getNation(strings[2]);
      }
      else
      {
        res = TownyUniverse.getDataSource().getResident(cs.getName());
        nat = res.getTown().getNation();
      }
    }
    catch (Exception ex)
    {
      cs.sendMessage(ChatColor.RED + "You are not in a town, or your town isn't part of a nation!");
      return true;
    }
    if (!admin && !res.isKing() && !nat.hasAssistant(res))
    {
      cs.sendMessage(ChatColor.RED + "You are not powerful enough in your nation to do that!");
      return true;
    }
    if (!admin)
    {
      War w = WarManager.getWarForNation(nat);
      if (w == null)
      {
        cs.sendMessage(ChatColor.RED + nat.getName() + " is not at war!");
        return true;
      }
      try {
		sonat = w.getEnemy(nat).getName();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
    Nation onat;
    try
    {
      onat = TownyUniverse.getDataSource().getNation(sonat);
    }
    catch (NotRegisteredException ex)
    {
      cs.sendMessage(ChatColor.RED + "That nation doesn't exist!");
      return true;
    }
    if (WarManager.requestPeace(nat, onat, admin)) {
      return true;
    }
    if (admin) {
      cs.sendMessage(ChatColor.GREEN + "Forced peace!");
    } else {
      cs.sendMessage(ChatColor.GREEN + "Requested peace!");
    }
    return true;
  }
  
  private boolean declareWar(CommandSender cs, String[] strings, boolean admin)
  {
    if ((strings.length == 2) && (admin))
    {
      cs.sendMessage(ChatColor.RED + "You need to specify two nations!");
      return true;
    }
    if (strings.length == 1)
    {
      cs.sendMessage(ChatColor.RED + "You need to specify a nation!");
      return true;
    }
    String sonat = strings[1];
    Resident res;
    Nation nat;
    try
    {
      if (admin)
      {
        res = null;
        nat = TownyUniverse.getDataSource().getNation(strings[2]);
      }
      else
      {
        res = TownyUniverse.getDataSource().getResident(cs.getName());
        nat = res.getTown().getNation();
      }
    }
    catch (Exception ex)
    {
      cs.sendMessage(ChatColor.RED + "You are not in a town, or your town isn't part of a nation!");
      return true;
    }
    if (WarManager.getWarForNation(nat) != null)
    {
      cs.sendMessage(ChatColor.RED + "Your nation is already at war!");
      return true;
    }
    if ((!admin) && (!nat.isKing(res)) && (!nat.hasAssistant(res)))
    {
      cs.sendMessage(ChatColor.RED + "You are not powerful enough in your nation to do that!");
      return true;
    }
    Nation onat;
    try
    {
      onat = TownyUniverse.getDataSource().getNation(sonat);
    }
    catch (NotRegisteredException ex)
    {
      cs.sendMessage(ChatColor.RED + "That nation doesn't exist!");
      return true;
    }
    if (WarManager.neutral.containsKey(onat))
    {
      cs.sendMessage(ChatColor.RED + "That nation is neutral and cannot enter in a war!");
      return true;
    }
    if (WarManager.neutral.containsKey(nat))
    {
      cs.sendMessage(ChatColor.RED + "You are in a neutral nation and cannot declare war on others!");
      return true;
    }
    if (WarManager.getWarForNation(onat) != null)
    {
      cs.sendMessage(ChatColor.RED + "That nation is already at war!");
      return true;
    }
    if (nat.getName().equals(onat.getName()))
    {
      cs.sendMessage(ChatColor.RED + "A nation can't be at war with itself!");
      return true;
    }
    WarManager.createWar(nat, onat, cs);
    try
    {
      nat.collect(TownyWars.declareCost);
    }
    catch (EconomyException ex)
    {
      Logger.getLogger(WarExecutor.class.getName()).log(Level.SEVERE, null, ex);
    }
    cs.sendMessage(ChatColor.GREEN + "Declared war on " + onat.getName() + "!");
    return true;
  }
  
  public void sendYesNoMessage(Player player){
	  new FancyMessage("    Yes")
	  		.color(ChatColor.GREEN)
	  		.style(ChatColor.BOLD)
	  		.tooltip("Click Yes to Repair")
	  		.command("/twar repair yes")
	    .then(" / ")
	        .color(ChatColor.WHITE)
	        .tooltip("Click Yes or No")
	    .then("No")
	        .color(ChatColor.DARK_RED)
	        .style(ChatColor.BOLD)
	        .tooltip("Click No to Cancel")
	        .command("/twar repair no")
	    .send(player);;
  }
}