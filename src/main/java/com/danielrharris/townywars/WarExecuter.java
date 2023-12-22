package com.danielrharris.townywars;

import com.danielrharris.townywars.config.TownyWarsLanguage;
import com.danielrharris.townywars.listeners.GriefListener;
import com.danielrharris.townywars.warObjects.Rebellion;
import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
		
		for(String commandName : this.plugin.getLanguage().mainCommand.getNames()) {
			if (cmnd.getName().equalsIgnoreCase(commandName)) {					
				if(hasPermission(cs, this.plugin.getLanguage().mainCommand.getPermission())) {
                    //  ******************************       Main Command       ***********************
					
					if (strings.length == 0)
					{
						if(hasPermission(cs, this.plugin.getLanguage().mainCommand.getPermission())) {
							unknownCommand=false;
							TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().mainCommand.getMessage());
							return true;
						}else {
							TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().noPermissionMessage);
							return false;
						}
						
					}
					
					String farg = strings[0];
					
					//  *****************************    Reload Command    ******************************
					
					for(String reload : this.plugin.getLanguage().reload.getNames()) {
						if (farg.equalsIgnoreCase(reload))
						{
							unknownCommand=false;
							if (!hasPermission(cs, this.plugin.getLanguage().mainCommand.getPermission())) {
								TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().noPermissionMessage);
								return false;
							}
							TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().reload.getMessage());
		                    if(TownyWars.getInstance().reload())
		                    	TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().successfulReloadMessage);
						}
					}				
					
					/// *************************   Help Command    ******************************
					
					for(String help : this.plugin.getLanguage().help.getNames()) {
						if (farg.equalsIgnoreCase(help))
						{
							if(hasPermission(cs, this.plugin.getLanguage().help.getPermission()) || hasPermission(cs, this.plugin.getLanguage().adminHelpPerm)){
								Player p;
								Resident res; 
								String[] helpMessages = (String[]) this.plugin.getLanguage().help.getMessage().toArray();
								for(int i = 0; i > helpMessages.length; i++) {
									TownyWarsLanguage.sendFormattedMessage(cs, helpMessages[i]);
								}
															
								if(cs instanceof Player){
									p = (Player) cs;
									try {
										res = TownyUniverse.getInstance().getResident(p.getName());
										if(res.hasNation()) {
											if(!WarManager.isAtWar(res.getNation()))
												if(res.getTown().getNation().getCapital() != res.getTown() && res.isMayor()){
													String[] rebellionMessages = (String[]) this.plugin.getLanguage().rebellionMessages.toArray();
													for(int i = 0; i > rebellionMessages.length; i++) {
														TownyWarsLanguage.sendFormattedMessage(cs, rebellionMessages[i]);
													}
												}
										}
										if(res.hasNation()) {
											if(res.getNation().getAssistants().contains(res) || res.isKing()) {
												String[] leaderMessages = (String[]) this.plugin.getLanguage().leaderMessages.toArray();
												for(int i = 0; i > leaderMessages.length; i++) {
													TownyWarsLanguage.sendFormattedMessage(cs, leaderMessages[i]);
												}
											}	
										}else if(res.hasTown()) {
											Town town = res.getTown();
											//Trusted Residents may need looked at again
											if(town.getTrustedResidents().contains(res) || res.isMayor()) {
												String[] leaderMessages = (String[]) this.plugin.getLanguage().leaderMessages.toArray();
												for(int i = 0; i > leaderMessages.length; i++) {
													TownyWarsLanguage.sendFormattedMessage(cs, leaderMessages[i]);
												}
											}
										}
									} catch (NotRegisteredException e) {
										return true;
									}								
								}
								if (hasPermission(cs, this.plugin.getLanguage().adminHelpPerm))
								{
									String[] adminMessages = (String[]) this.plugin.getLanguage().adminMessages.toArray();
									for(int i = 0; i > adminMessages.length; i++) {
										TownyWarsLanguage.sendFormattedMessage(cs, adminMessages[i]);
									}
								}
								return true;
							}else {
								TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().noPermissionMessage);
								return false;
							}				
						}
					}
						
					
					/// *************************   Status Command    ******************************
					
					for(String status : this.plugin.getLanguage().status.getNames()) {
						if (farg.equalsIgnoreCase(status)) {
							if(hasPermission(cs, this.plugin.getLanguage().status.getPermission())) {
								unknownCommand=false;
								if (strings.length == 1)
								{
									TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notInWarErrorMessage);
									for (War war : WarManager.getWars())
									{
										showWarStatus(cs, war);								
									}
									return true;
								}
								
								String part = strings[1];
								if(TownyUniverse.getInstance().hasTown(part)) {
									Town town = TownyUniverse.getInstance().getTown(part);
									if(WarManager.isAtWar(town)) {
										War war = WarManager.getWar(town);
										showWarStatus(cs, war);
										return true;									
									}else {
										TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notInWarErrorMessage);
										return false;
									}
								}
								if(TownyUniverse.getInstance().hasNation(part)) {
									Nation nation = TownyUniverse.getInstance().getNation(part);
									if(WarManager.isAtWar(nation)) {
										War war = WarManager.getWar(nation);
										showWarStatus(cs, war);
										return true;									
									}else {
										TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notInWarErrorMessage);
										return false;
									}
								}							
								return true;
							}else {
								TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().noPermissionMessage);
								return false;
							}
						}							
					}
					
					// TODO

					/// *************************   Repair Command    ******************************
					/*
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
					}*/
			 	  
					/// *************************   Show town max Defense points Command    ******************************
					
					for(String showTownMaxDP : this.plugin.getLanguage().showTownMaxDP.getNames()) {
						if (farg.equalsIgnoreCase(showTownMaxDP)){
							if(hasPermission(cs, this.plugin.getLanguage().showTownMaxDP.getPermission())) {
								unknownCommand=false;
								List<Town> towns = new ArrayList<Town>();
								if (strings.length == 1)
								{
									try {
										Resident re = TownyUniverse.getInstance().getResident(cs.getName());
										if(re.hasTown()){
											towns.add(re.getTown());									
										}else{
											TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notInTownErrorMessage);
											return false;
										}		
									} catch (NotRegisteredException e) {
										TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notInTownErrorMessage);
										return false;
									}
								}
								if(strings.length > 1) {
									for (int i = 0; i < strings.length; i++) {
										if(!strings[i].equalsIgnoreCase(showTownMaxDP))
											if(TownyUniverse.getInstance().hasTown(strings[i])) {
												towns.add(TownyUniverse.getInstance().getTown(strings[i]));
											}
									}
								}
								if(!towns.isEmpty() && towns!=null) {
									for(Town town : towns) {
										int points = WarManager.getTownMaxPoints(town);
										String proper = Integer.toString(points);
										if(towns.size()==1) {
											//BossBar Stuff
											if(TownyWars.getInstance().getConfigInstance().isBossBar){
												//new ShowDPTask(town, plugin).runTask(plugin);
											}
										}																
										if(this.plugin.getLanguage().showTownMaxDP.getMessage()!=null) {
											List<String> message = new ArrayList<String>();
											for(String s : this.plugin.getLanguage().showTownMaxDP.getMessage()) {
												if(s.contains("%max_defense_points%"))
													s = s.replace("%max_defense_points%", proper);
												message.add(s);
											}
											TownyWarsLanguage.sendFormattedMessage(cs, message);										
										}
									}
									return true;
								}					
							}else {
								TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().noPermissionMessage);
								return false;
							}							  	
						}
					}
					
					/// *************************   Show town Defense points Command    ******************************
					
					for(String showTownDP : this.plugin.getLanguage().showTownDP.getNames()) {
						if (farg.equalsIgnoreCase(showTownDP)){
							if(hasPermission(cs, this.plugin.getLanguage().showTownDP.getPermission())) {
								unknownCommand=false;
								List<Town> towns = new ArrayList<Town>();
								if (strings.length == 1)
								{									
									try {
										Resident re = TownyUniverse.getInstance().getResident(cs.getName());
										if(re.hasTown()){
											towns.add(re.getTown());									
										}else{
											TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notInTownErrorMessage);
											return false;
										}		
									} catch (NotRegisteredException e) {
										TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notInTownErrorMessage);
										return false;
									}
								}
								if(strings.length > 1) {
									for (int i = 0; i < strings.length; i++) {
										if(!strings[i].equalsIgnoreCase(showTownDP))
											if(TownyUniverse.getInstance().hasTown(strings[i])) {
												towns.add(TownyUniverse.getInstance().getTown(strings[i]));
											}
									}
								}
								if(!towns.isEmpty() && towns!=null) {
									for(Town town : towns) {
										if(WarManager.isAtWar(town)) { //If at war show both current and max defense points
											String current = Integer.toString(WarManager.getTownPoints(town));
											String max = Integer.toString(WarManager.getTownMaxPoints(town));
											if(towns.size()==1) {
												//BossBar Stuff
												if(TownyWars.getInstance().getConfigInstance().isBossBar){
													//new ShowDPTask(town, plugin).runTask(plugin);
												}
											}																					
											if(this.plugin.getLanguage().showTownDP.getMessage()!=null) {
												List<String> message = new ArrayList<String>();
												for(String s : this.plugin.getLanguage().showTownDP.getMessage()) {
													if(s.contains("%max_defense_points%"))
														s = s.replace("%max_defense_points%", max);
													if(s.contains("%defense_points%"))
														s = s.replace("%defense_points%", current);
													message.add(s);
												}
												TownyWarsLanguage.sendFormattedMessage(cs, message);										
											}
										}else { //If not at war just show max defense points message
                                            String max = Integer.toString(WarManager.getTownMaxPoints(town));											
											if(towns.size()==1) {
												//BossBar Stuff
												if(TownyWars.getInstance().getConfigInstance().isBossBar){
													//new ShowDPTask(town, plugin).runTask(plugin);
												}
											}																					
											if(this.plugin.getLanguage().showTownMaxDP.getMessage()!=null) {
												List<String> message = new ArrayList<String>();
												for(String s : this.plugin.getLanguage().showTownMaxDP.getMessage()) {
													if(s.contains("%max_defense_points%"))
														s = s.replace("%max_defense_points%", max);
													message.add(s);
												}
												TownyWarsLanguage.sendFormattedMessage(cs, message);										
											}
										}
										
									}
									return true;
								}
							}else {
								TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().noPermissionMessage);
								return false;
							}
						}
					}
					
					/// *************************   Set town/nation as Neutral Command   ******************************

					for(String neutral : this.plugin.getLanguage().neutral.getNames()) {
						if (farg.equalsIgnoreCase(neutral)){
							Set<Town> towns = new HashSet<Town>();
							Set<Nation> nations = new HashSet<Nation>();
							Resident re = null;
							try {
								re = TownyUniverse.getInstance().getResident(cs.getName());		
							} catch (NotRegisteredException e) {
								TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notInTownErrorMessage);
								return false;
							}
							if(re!=null) {

								// Already in a war
								if(WarManager.isAtWar(re)) {
									TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().alreadyInWarErrorMessage);
									return false;
								}
								if (strings.length == 1)
								{
									if(hasPermission(cs, this.plugin.getLanguage().neutral.getPermission())) {
										if(re.hasNation()){
											Nation nation = re.getNation();
											if(re == nation.getKing() || WarManager.getAssistants(nation).contains(re)) {
											    nations.add(nation);
											} else {
												TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notHighEnoughRankInTownErrorMessage);
												return false;
											}
										}else if(re.hasTown()) {
											Town town = re.getTown();
											if(re == town.getMayor() || WarManager.getAssistants(town).contains(re)) {
												towns.add(town);
											} else {
												TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notHighEnoughRankInTownErrorMessage);
												return false;
											}											
										}else{
											TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notInTownErrorMessage);
											return false;
										}
									}else {
										TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().noPermissionMessage);
										return false;
									}
								}
								//This one will have to check for admins and all that :/
								if(strings.length > 1) {
									if(hasPermission(cs, this.plugin.getLanguage().neutral.getPermission()) && !hasPermission(cs, this.plugin.getLanguage().adminPermissionNeutral)) {
										for (int i = 0; i < strings.length; i++) {
											if(!strings[i].equalsIgnoreCase(neutral))
												if(TownyUniverse.getInstance().hasTown(strings[i])) {
													Town town = TownyUniverse.getInstance().getTown(strings[i]);
													if(re.hasTown()) {
														if(re.getTown()==town) {
															if(re == town.getMayor() || WarManager.getAssistants(town).contains(re)) {
																towns.add(town);
															} else {
																TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notHighEnoughRankInTownErrorMessage);
																return false;
															}
														}else {
															TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().cannotAddNotUrOwn);
															return false;
														}
													}
												}else if(TownyUniverse.getInstance().hasNation(strings[i])) {
													Nation nation = TownyUniverse.getInstance().getNation(strings[i]);
													if(re.hasNation()) {
														if(re.getNation()==nation) {
															if(re == nation.getKing() || WarManager.getAssistants(nation).contains(re)) {
															    nations.add(nation);
															} else {
																TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().notHighEnoughRankInTownErrorMessage);
																return false;
															}
															nations.add(nation);
														}else {
															TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().cannotAddNotUrOwn);
															return false;
														}
													}
												}else {
													if(this.plugin.getLanguage().noTownsNationsExist!=null) {
														List<String> message = new ArrayList<String>();
														for(String s : this.plugin.getLanguage().noTownsNationsExist) {
															if(s.contains("%townOrNation%"))
																s = s.replace("%townOrNation%", strings[i]);
															message.add(s);
														}
														TownyWarsLanguage.sendFormattedMessage(cs, message);										
													}
													return false;
												}
										}
									}else if(hasPermission(cs, this.plugin.getLanguage().adminPermissionNeutral)) { // admin
										for (int i = 0; i < strings.length; i++) {
											if(!strings[i].equalsIgnoreCase(neutral))
												if(TownyUniverse.getInstance().hasTown(strings[i])) {
													towns.add(TownyUniverse.getInstance().getTown(strings[i]));						
												}else if(TownyUniverse.getInstance().hasNation(strings[i])) {
													nations.add(TownyUniverse.getInstance().getNation(strings[i]));											
												}else {
													if(this.plugin.getLanguage().noTownsNationsExist!=null) {
														List<String> message = new ArrayList<String>();
														for(String s : this.plugin.getLanguage().noTownsNationsExist) {
															if(s.contains("%townOrNation%"))
																s = s.replace("%townOrNation%", strings[i]);
															message.add(s);
														}
														TownyWarsLanguage.sendFormattedMessage(cs, message);										
													}
													return false;
												}
										}
									}else {
										TownyWarsLanguage.sendFormattedMessage(cs, this.plugin.getLanguage().noPermissionMessage);
										return false;
									}
								}
								if(towns!=null && !towns.isEmpty()) {
									for(Town town : towns) {
										if(WarManager.isNeutral(town)) {
											WarManager.setNonNeutral(town);
											if(this.plugin.getLanguage().selfMessageNeutralOff!=null) {
												List<String> message = new ArrayList<String>();
												for(String s : this.plugin.getLanguage().selfMessageNeutralOff) {
													if(s.contains("%townOrNation%"))
														s = s.replace("%townOrNation%", town.getName());
													message.add(s);
												}
												TownyWarsLanguage.sendFormattedMessage(cs, message);										
											}
											for(Resident r : town.getResidents()) {
												Player p = WarManager.getPlayerFromResident(r);
												if(p!=null) {
													TownyWarsLanguage.sendFormattedMessage(p, this.plugin.getLanguage().neutralMessageOff);
												}
											}
										}else {
											WarManager.setNeutral(town);
											if(this.plugin.getLanguage().selfMessageNeutral!=null) {
												List<String> message = new ArrayList<String>();
												for(String s : this.plugin.getLanguage().selfMessageNeutral) {
													if(s.contains("%townOrNation%"))
														s = s.replace("%townOrNation%", town.getName());
													message.add(s);
												}
												TownyWarsLanguage.sendFormattedMessage(cs, message);										
											}
											for(Resident r : town.getResidents()) {
												Player p = WarManager.getPlayerFromResident(r);
												if(p!=null) {
													TownyWarsLanguage.sendFormattedMessage(p, this.plugin.getLanguage().neutral.getMessage());
												}
											}
										}
									}
								}
								if(nations!=null && !nations.isEmpty()) {
									for(Nation nation : nations) {
										if(WarManager.isNeutral(nation)) {
											WarManager.setNonNeutral(nation);
											if(this.plugin.getLanguage().selfMessageNeutralOff!=null) {
												List<String> message = new ArrayList<String>();
												for(String s : this.plugin.getLanguage().selfMessageNeutralOff) {
													if(s.contains("%townOrNation%"))
														s = s.replace("%townOrNation%", nation.getName());
													message.add(s);
												}
												TownyWarsLanguage.sendFormattedMessage(cs, message);										
											}
											for(Resident r : nation.getResidents()) {
												Player p = WarManager.getPlayerFromResident(r);
												if(p!=null) {
													TownyWarsLanguage.sendFormattedMessage(p, this.plugin.getLanguage().neutralMessageOff);
												}
											}
										}else {
											WarManager.setNeutral(nation);
											if(this.plugin.getLanguage().selfMessageNeutral!=null) {
												List<String> message = new ArrayList<String>();
												for(String s : this.plugin.getLanguage().selfMessageNeutral) {
													if(s.contains("%townOrNation%"))
														s = s.replace("%townOrNation%", nation.getName());
													message.add(s);
												}
												TownyWarsLanguage.sendFormattedMessage(cs, message);										
											}
											for(Resident r : nation.getResidents()) {
												Player p = WarManager.getPlayerFromResident(r);
												if(p!=null) {
													TownyWarsLanguage.sendFormattedMessage(p, this.plugin.getLanguage().neutral.getMessage());
												}
											}
										}
									}
								}
								return true;
							}						
						}
					}
					
					/// *************************   Admin Start War Command   ******************************
					
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
					if (farg.equals("peace")) {
						unknownCommand=false;
						return declarePeace(cs, strings, false);
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
				}else {
					/////    DISPLAY NO PERMISSION MESSAGE HERE
				}
			}
		}
	}
	
	private void showWarStatus(CommandSender cs, War war) {
		WarParticipant first = null;
		WarParticipant second = null;
		for (WarParticipant st : war.getWarParticipants()) {
			if (first == null) {
				first = st;
			} else {
				second = st;
			}
		}
		String message = this.plugin.getLanguage().statusListMessage;
		if(message.contains("%participant1%"))
			message = message.replace("%participant1%", first.getName());
		if(message.contains("%participant2%"))
			message = message.replace("%participant2%", second.getName());
		if(message.contains("%participant1_points%"))
			message = message.replace("%participant1_points%", Integer.toString(war.getParticipantPoints(first)));
		if(message.contains("%participant2_points%"))
			message = message.replace("%participant2_points%", Integer.toString(war.getParticipantPoints(second)));		
		TownyWarsLanguage.sendFormattedMessage(cs, message);
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
	  for(Rebellion r : WarManager.getAllRebellions())
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
			res = TownyUniverse.getInstance().getResident(cs.getName());
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
  
  private boolean declarePeace(CommandSender cs, String[] strings, boolean admin)
  {
      if ((admin) && (strings.length <= 2))
      {
    	  cs.sendMessage(ChatColor.RED + "You need to specify two towns/nations!");
    	  return true;
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
    if (WarManager.getNeutral().containsKey(onat))
    {
      cs.sendMessage(ChatColor.RED + "That nation is neutral and cannot enter in a war!");
      return true;
    }
    if (WarManager.getNeutral().containsKey(nat))
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
  
  private boolean hasPermission(CommandSender sender, String permission) {
	  if(permission=="")
		  return true;
	  if(sender instanceof Player) {
		  Player player = (Player)sender;
		  if(player.hasPermission(permission))
			  return true;
	  }else
		  return true;
	  return false;
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