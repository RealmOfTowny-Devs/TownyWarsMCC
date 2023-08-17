package com.danielrharris.townywars;

import com.danielrharris.townywars.cmds.TradeCMD;
import com.danielrharris.townywars.listeners.GriefListener;
import com.danielrharris.townywars.tasks.ShowDPTask;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
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

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.Iterator;

import static com.danielrharris.townywars.TownyWars.idConfig;
import static com.danielrharris.townywars.TownyWars.idConfigFile;


class WarExecutor implements CommandExecutor {
    private TownyWars plugin;
    private GriefManager gm;

    public WarExecutor(TownyWars aThis) {
        this.plugin = aThis;
        this.gm = new GriefManager(plugin);
    }

    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        boolean unknownCommand = true;
        DecimalFormat d = new DecimalFormat("#.00");
        if (strings.length == 0) {
            unknownCommand = false;
            cs.sendMessage(ChatColor.GREEN + "For help with TownyWars, type /twar help");
            return true;
        }
        String farg = strings[0];
        if (farg.equals("reload")) {
            unknownCommand = false;
            if (!cs.hasPermission("townywars.admin")) {
                return false;
            }
            cs.sendMessage(ChatColor.GREEN + "Reloading plugin...");
            PluginManager pm = Bukkit.getServer().getPluginManager();
            pm.disablePlugin(this.plugin);
            pm.enablePlugin(this.plugin);
            cs.sendMessage(ChatColor.GREEN + "Plugin reloaded!");
        }
        if (farg.equals("help")) {
            Player p;
            Resident res;
            cs.sendMessage(ChatColor.GREEN + "Towny Wars Help:");
            cs.sendMessage(ChatColor.AQUA + "/twar - " + ChatColor.YELLOW + "Displays the TownyWars configuration information");
            cs.sendMessage(ChatColor.AQUA + "/twar trade - " + ChatColor.YELLOW + "Displays Trade help menu");
            cs.sendMessage(ChatColor.AQUA + "/twar help - " + ChatColor.YELLOW + "Displays the TownyWars help page");
            cs.sendMessage(ChatColor.AQUA + "/twar status - " + ChatColor.YELLOW + "Displays a list of on-going wars");
            cs.sendMessage(ChatColor.AQUA + "/twar status [nation] - " + ChatColor.YELLOW + "Displays a list of the nation's towns and their defense points");
            if (cs.hasPermission("townywars.leader")) {
                cs.sendMessage(ChatColor.AQUA + "/twar repair - " + ChatColor.YELLOW + "Allows you to repair war grief by paying money from town bank");
            }
            cs.sendMessage(ChatColor.AQUA + "/twar showtowndp - " + ChatColor.YELLOW + "Shows your towns current defense points.");
            cs.sendMessage(ChatColor.AQUA + "/twar showtownmaxdp - " + ChatColor.YELLOW + "Shows your towns max defense points.");
            if (cs.hasPermission("townywars.leader")) {
                cs.sendMessage(ChatColor.AQUA + "/twar declare [nation] - " + ChatColor.YELLOW + "Starts a war with another nation (REQUIRES YOU TO BE A KING/ASSISTANT)");
                cs.sendMessage(ChatColor.AQUA + "/twar end - " + ChatColor.YELLOW + "Request from enemy nations king to end the ongoing war. (REQUIRES YOU TO BE A KING/ASSISTANT)");
                cs.sendMessage(ChatColor.AQUA + "/twar ideology [ideology] - " + ChatColor.YELLOW + "(select one: Economic, Religious or Militaristic) (REQUIRES YOU TO BE A KING/ASSISTANT)");
            }
            if (cs instanceof Player) {
                p = (Player) cs;
                try {
                    res = TownyUniverse.getInstance().getResident(p.getName());
                    if (res.getTown().getNation().getCapital() != res.getTown()) {
                        cs.sendMessage(ChatColor.AQUA + "/twar createrebellion [name] - " + ChatColor.YELLOW + "Creates a (secret) rebellion within your nation.");
                        cs.sendMessage(ChatColor.AQUA + "/twar joinrebellion [name] - " + ChatColor.YELLOW + "Joins a rebellion within your nation using the name.");
                        cs.sendMessage(ChatColor.AQUA + "/twar leaverebellion - " + ChatColor.YELLOW + "Leaves your current rebellion.");
                        cs.sendMessage(ChatColor.AQUA + "/twar showrebellion - " + ChatColor.YELLOW + "Shows your current rebellion and its members.");
                        cs.sendMessage(ChatColor.AQUA + "/twar executerebellion - " + ChatColor.YELLOW + "Executes your rebellion and you go to war with your nation (requires to be leader of rebellion).");
                    }
                } catch (Exception e) {
                    return true;
                }
            }
            if (cs.hasPermission("townywars.admin")) {
                cs.sendMessage(ChatColor.AQUA + "/twar reload - " + ChatColor.YELLOW + "Reload the plugin");
                cs.sendMessage(ChatColor.AQUA + "/twar astart [nation] [nation] - " + ChatColor.YELLOW + "Forces two nations to go to war");
                cs.sendMessage(ChatColor.AQUA + "/twar aend [nation] [nation] - " + ChatColor.YELLOW + "Forces two nations to stop a war");
                cs.sendMessage(ChatColor.AQUA + "/twar aaddtowndp [town] - " + ChatColor.YELLOW + "Adds a DP to the town");
                cs.sendMessage(ChatColor.AQUA + "/twar aremovetowndp [town] - " + ChatColor.YELLOW + "Removes a DP from the town");
            }
            return true;
        }
        War w;
        if (farg.equals("status")) {
            unknownCommand = false;
            if (strings.length == 1) {
                cs.sendMessage(ChatColor.GREEN + "List of on-going wars:");
                for (War war : WarManager.getWars()) {
                    Object firstEntity = null;
                    Object secondEntity = null;

                    Set<Object> entitiesInWar = war.getEntitiesInWar();
                    Iterator<Object> iterator = entitiesInWar.iterator();

                    if (iterator.hasNext()) {
                        firstEntity = iterator.next();
                    }

                    if (iterator.hasNext()) {
                        secondEntity = iterator.next();
                    }

                    try {
                        cs.sendMessage(ChatColor.GREEN + WarManager.getNameForEntity(firstEntity) + " " + war.getPoints(firstEntity) + " vs. " + WarManager.getNameForEntity(secondEntity) + " " + war.getPoints(secondEntity));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
            String onation = strings[1];
            Nation t;
            try {
                t = TownyUniverse.getInstance().getNation(onation);
            } catch (Exception ex) {
                cs.sendMessage(ChatColor.GOLD + "No nation called " + onation + " could be found!");
                return true;
            }
            w = WarManager.getWarForEntity(t);  // Assuming there's a method getWarForEntity
            if (w == null) {
                cs.sendMessage(ChatColor.RED + "That nation isn't in a war!");
                return true;
            }
            cs.sendMessage(t.getName() + " war info:");
            for (Town tt : t.getTowns()) {
                try {
                    cs.sendMessage(ChatColor.GREEN + tt.getName() + ": " + w.getPoints(tt) + " points");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return true;
        }

        if (farg.equals("repair")) {
            unknownCommand = false;
            if (cs.hasPermission("townywars.leader")) {
                if (cs instanceof Player) {
                    Player p = (Player) cs;
                    int numBlocks = 0;
                    Town town = null;
                    double price = 0;
                    try {
                        town = TownyUniverse.getInstance().getResident(p.getName()).getTown();
                        if (GriefListener.getGriefedBlocks() != null) {
                            if (!GriefListener.getGriefedBlocks().isEmpty()) {
                                for (SBlock b : GriefListener.getGriefedBlocks().get(town)) {
                                    if (b.getType() != Material.AIR && !Utils.isOtherAttachable(b.getType())) {
                                        numBlocks++;
                                    }
                                }
                            }
                        }
                        price = Math.round((numBlocks * TownyWars.pBlock) * 1e2) / 1e2;
                    } catch (NotRegisteredException e) {
                        p.sendMessage(ChatColor.RED + "You are not in a Town!");
                        return true;
                    }
                    if (strings.length == 1) {
                        if (town != null) {
                            if (numBlocks > 0) {
                                //Rollback everything, charge for block destructions
                                p.sendMessage(ChatColor.GREEN + "Price to Repair " + town.getName() + ChatColor.WHITE + ": $" + ChatColor.YELLOW + d.format(price));
                                p.sendMessage("   " + ChatColor.MAGIC + "l" + ChatColor.RESET + "  " + ChatColor.BOLD + ChatColor.GOLD + "Repair?" + ChatColor.RESET + "  " + ChatColor.MAGIC + "l");
                                sendYesNoMessage(p);
                            } else if (numBlocks == 0 && GriefListener.getGriefedBlocks().get(town) != null && GriefListener.getGriefedBlocks().get(town).size() > 0) {
                                p.sendMessage(ChatColor.GREEN + "Price to Repair " + town.getName() + ChatColor.WHITE + ": " + ChatColor.YELLOW + "FREE!");
                                p.sendMessage("   " + ChatColor.MAGIC + "l" + ChatColor.RESET + "  " + ChatColor.BOLD + ChatColor.GOLD + "Repair?" + ChatColor.RESET + "  " + ChatColor.MAGIC + "l");
                                sendYesNoMessage(p);
                                //rollback block places only (free)
                            } else if ((numBlocks == 0 && GriefListener.getGriefedBlocks().get(town) == null) || (numBlocks == 0 && GriefListener.getGriefedBlocks().get(town) != null && GriefListener.getGriefedBlocks().get(town).isEmpty())) {
                                p.sendMessage(ChatColor.GREEN + "Nothing to Repair");
                            }
                        }
                    }
                    if (strings.length == 2) {
                        if (town != null) {
                            if (GriefListener.getGriefedBlocks().get(town) != null) {
                                if (!GriefListener.getGriefedBlocks().get(town).isEmpty()) {
                                    String response = ChatColor.stripColor(strings[1]).toLowerCase();
                                    if (response.equals("yes")) {
                                        try {
                                            if (town.getAccount().canPayFromHoldings(price)) {
                                                town.getAccount().withdraw(price, "repairs");
                                                p.sendMessage("");
                                                p.sendMessage(ChatColor.GREEN + "Repairs are underway!");
                                                p.sendMessage(ChatColor.AQUA + "New Town Balance: " + ChatColor.YELLOW + town.getAccount().getHoldingFormattedBalance());
                                                gm.rollbackBlocks(town);
                                            } else {
                                                p.sendMessage("");
                                                p.sendMessage(ChatColor.DARK_RED + town.getName() + " does not have enough money to pay for repairs.");
                                            }
                                        } catch (Exception e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                    }
                                    if (response.equals("no")) {
                                        p.sendMessage("");
                                        p.sendMessage(ChatColor.DARK_RED + "Canceling Repair...");
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
            }
        }

        if (farg.equals("showtownmaxdp")) {
            unknownCommand = false;
            Town town = null;
            try {
                Resident re = TownyUniverse.getInstance().getResident(cs.getName());
                assert re != null;
                if (re.hasTown()) {
                    town = re.getTown();
                    Double points = War.getTownMaxPoints(town);
                    String proper = d.format(points);
                    if (TownyWars.isBossBar) {
                        new ShowDPTask(town, plugin).runTask(plugin);
                    }
                    cs.sendMessage(ChatColor.YELLOW + "Your town's max defense value is currently " + proper + " defense points!");
                    cs.sendMessage(ChatColor.YELLOW + "Claim more land or get more residents to increase it!");
                    return true;
                } else {
                    cs.sendMessage(ChatColor.RED + "You are not in a Town!");
                }
            } catch (NotRegisteredException e) {
                cs.sendMessage(ChatColor.RED + "You are not in a Town!");
                return true;
            }

        }

        if (farg.equals("showtowndp")) {
            cs.sendMessage("Command recognized!");
            unknownCommand = false;
            Town town = null;
            Double points = null;
            War wwar = null;
            try {
                Resident re = TownyUniverse.getInstance().getResident(cs.getName());
                assert re != null;
                if (re.hasTown()) {
                    town = Objects.requireNonNull(TownyUniverse.getInstance().getResident(cs.getName())).getTown();
                    if (town.hasNation()) {
                        try {
                            wwar = WarManager.getWarForNation(town.getNation());
                            if (wwar != null) {
                                try {
                                    points = wwar.getTownPoints(town);
                                    if (points != null) {
                                        String proper = d.format(points);
                                        if (TownyWars.isBossBar) {
                                            new ShowDPTask(town, plugin).runTask(plugin);
                                        }
                                        cs.sendMessage(ChatColor.YELLOW + "Your town's defense value is currently " + proper + " defense points!");
                                        cs.sendMessage(ChatColor.YELLOW + "Your points will return to max value when the war ends!");
                                        return true;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return true;
                                }
                            } else {
                                if (TownyWars.isBossBar) {
                                    points = War.getTownMaxPoints(town);
                                    String proper = d.format(points);
                                    new ShowDPTask(town, plugin).runTask(plugin);
                                    cs.sendMessage(ChatColor.YELLOW + "Your town's max defense value is currently " + proper + " defense points!");
                                    cs.sendMessage(ChatColor.YELLOW + "Claim more land or get more residents to increase it!");
                                }
                            }
                        } catch (Exception e1) {
                            cs.sendMessage(ChatColor.RED + "You are not in a Nation!");
                            return true;
                        }
                    }
                } else {
                    cs.sendMessage(ChatColor.RED + "You are not in a Town!");
                }
            } catch (NotRegisteredException e) {
                cs.sendMessage(ChatColor.RED + "You are not in a Town!");
                return true;
            }
        }

        if (farg.equals("neutral")) {
            unknownCommand = false;
            if (!cs.hasPermission("townywars.neutral")) {
                cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
                return true;
            }
            Nation csNation;
            try {
                Town csTown = Objects.requireNonNull(TownyUniverse.getInstance().getResident(cs.getName())).getTown();
                csNation = Objects.requireNonNull(TownyUniverse.getInstance().getTown(csTown.toString())).getNation();
            } catch (NotRegisteredException ex) {
                cs.sendMessage(ChatColor.RED + "You are not not part of a town, or your town is not part of a nation!");
                Logger.getLogger(WarExecutor.class.getName()).log(Level.SEVERE, null, ex);
                return true;
            }
            if ((!cs.isOp()) && (!csNation.toString().equals(strings[1]))) {
                cs.sendMessage(ChatColor.RED + "You may only set your own nation to neutral, not others.");
                return true;
            }
            if (strings.length == 0) {
                cs.sendMessage(ChatColor.RED + "You must specify a nation to toggle neutrality for (eg. /twar neutral [nation]");
            }
            if (strings.length == 1) {
                String onation = strings[1];
                Nation t;
                t = TownyUniverse.getInstance().getNation(onation);
                WarManager.neutral.put(t.toString(), 0D);
            }
        }

        if (farg.equals("astart")) {
            unknownCommand = false;
            if (!cs.hasPermission("townywars.admin")) {
                cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
                return true;
            }
            return declareWar(cs, strings, true);
        }
        if (farg.equals("declare")) {
            unknownCommand = false;
            return declareWar(cs, strings, false);
        }
        if (farg.equals("end")) {
            unknownCommand = false;
            return declareEnd(cs, strings, false);
        }
        if (farg.equals("createrebellion")) {
            unknownCommand = false;
            return createRebellion(cs, strings, false);
        }
        if (farg.equals("joinrebellion")) {
            unknownCommand = false;
            return joinRebellion(cs, strings, false);
        }
        if (farg.equals("leaverebellion")) {
            unknownCommand = false;
            return leaveRebellion(cs, strings, false);
        }
        if (farg.equals("executerebellion")) {
            unknownCommand = false;
            return executeRebellion(cs, strings, false);
        }
        if (farg.equals("showrebellion")) {
            unknownCommand = false;
            return showRebellion(cs, strings, false);
        }
        if (farg.equals("aend")) {
            unknownCommand = false;
            if (!cs.hasPermission("townywars.admin")) {
                cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
                return true;
            }
            return declareEnd(cs, strings, true);
        }
        if (farg.equals("aaddtowndp")) {
            unknownCommand = false;
            if (!cs.hasPermission("townywars.admin")) {
                cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
                return true;
            }
            return addTownDp(cs, strings);
        }
        if (farg.equals("aremovetowndp")) {
            unknownCommand = false;
            if (!cs.hasPermission("townywars.admin")) {
                cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
                return true;
            }
            return removeTownDp(cs, strings);
        }

        if (farg.equalsIgnoreCase("ideology")) {
            unknownCommand = false;

            if (!(cs instanceof Player)) {
                cs.sendMessage(ChatColor.RED + "This command is only usable by players, sorry!");
                return true;
            }

            Player player = (Player) cs;

            try {
                Resident resident = TownyUniverse.getInstance().getResident(player.getName());

                if (resident.hasTown()) {
                    Town town = resident.getTown();
                    String tname = town.getName();

                    // If the player has already set an ideology
                    if (idConfig.contains(tname)) {
                        player.sendMessage(ChatColor.GREEN.toString() + "Your ideology is: " + ChatColor.DARK_GREEN.toString() + idConfig.getString(tname));
                        return true;
                    } else if (strings.length == 1) {
                        // Player hasn't set an ideology and is just typing /ideology
                        player.sendMessage(ChatColor.RED + "/ideology <ideology> (select one: Economic, Religious or Militaristic)");
                        return true;
                    } else if (strings.length > 1 && (town.getMayor() == resident || resident.isKing() || resident.isMayor())) {
                        // Player is trying to set the ideology and is a mayor/king/assistant
                        String id = strings[1].toLowerCase();
                        if (id.equals("economic") || id.equals("religious") || id.equals("militaristic")) {
                            idConfig.set(town.getName(), id);
                            try {
                                idConfig.save(idConfigFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            player.sendMessage(ChatColor.GREEN + "Ideology set!");
                        } else {
                            player.sendMessage(ChatColor.RED + "Valid ideologies are: Economic, Religious, Militaristic");
                        }
                        return true;
                    } else if (strings.length > 1) {
                        // Player is trying to set ideology but doesn't have the right permissions
                        player.sendMessage(ChatColor.RED + "You cannot set your town's ideology if you are not the mayor, king, or an assistant!");
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED.toString() + "You do not have a town!");
                    }
                }
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }

        // Check if the command is /twar trade
        if (farg.equalsIgnoreCase("trade")) {
            unknownCommand = false;
            if (!(cs instanceof Player)) {
                cs.sendMessage(ChatColor.RED.toString() + "You cannot use this via the console.");
                return true;
            }
            Player player = (Player) cs;

            if (farg.length() == 1) {
                TradeCMD.sendHelpMenu(player);
            } else {
                TradeCMD.handleArgs(Arrays.copyOfRange(strings, 1, strings.length), player);
            }
        }


        if (unknownCommand) {
            cs.sendMessage(ChatColor.RED + "Unknown twar command.");
        }
        return true;
    }

    private boolean addTownDp(CommandSender cs, String[] strings) {
        Town town = null;
        if (strings.length != 2) {
            cs.sendMessage(ChatColor.RED + "You need to specify a town!");
            return false;
        }

        town = TownyUniverse.getInstance().getTown(strings[1]);
        if (town == null) {
            cs.sendMessage(ChatColor.RED + "Town doesn't exist!");
            return false;
        }

        for (War war : WarManager.getWars()) {
            Set<Object> entitiesInWar = war.getEntitiesInWar();
            for (Object entity : entitiesInWar) {
                if (entity instanceof Nation && ((Nation) entity).hasTown(strings[1])) {
                    try {
                        war.chargeTownPoints(town.getNation(), town, -1);
                        cs.sendMessage(ChatColor.YELLOW + "Added a DP to " + town.getName());
                    } catch (NotRegisteredException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean removeTownDp(CommandSender cs, String[] strings) {
        Town town = null;
        if (strings.length != 2) {
            cs.sendMessage(ChatColor.RED + "You need to specify a town!");
            return false;
        }

        town = TownyUniverse.getInstance().getTown(strings[1]);
        if (town == null) {
            cs.sendMessage(ChatColor.RED + "Town doesn't exist!");
            return false;
        }

        for (War war : WarManager.getWars()) {
            Set<Object> entitiesInWar = war.getEntitiesInWar();
            for (Object entity : entitiesInWar) {
                if (entity instanceof Nation && ((Nation) entity).hasTown(strings[1])) {
                    try {
                        war.chargeTownPoints(town.getNation(), town, 1);  // Notice the change in the third argument from -1 to 1.
                        cs.sendMessage(ChatColor.YELLOW + "Removed a DP from " + town.getName());
                    } catch (NotRegisteredException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean showRebellion(CommandSender cs, String[] strings, boolean admin) {

        Resident res = null;
        try {
            res = TownyUniverse.getInstance().getResident(cs.getName());
        } catch (Exception e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
        }

        try {
            if ((!admin) && (!res.getTown().isMayor(res))) {
                cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
                return true;
            }
        } catch (NotRegisteredException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        for (Rebellion r : Rebellion.getAllRebellions()) {
            try {
                if (r.isRebelTown(res.getTown()) || r.isRebelLeader(res.getTown())) {
                    cs.sendMessage(ChatColor.YELLOW + ".oOo.___________.[ " + r.getName() + " (Rebellion) ].___________.oOo.");
                    cs.sendMessage(ChatColor.GREEN + "Nation: " + r.getMotherNation().getName());
                    cs.sendMessage(ChatColor.GREEN + "Leader: " + r.getLeader().getName());
                    String members = new String("");
                    for (Town town : r.getRebels())
                        members = members + ", " + town.getName();
                    if (!members.isEmpty())
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
    private boolean createRebellion(CommandSender cs, String[] strings, boolean admin) {

        Resident res = null;
        res = TownyUniverse.getInstance().getResident(cs.getName());

        if (strings.length != 2) {
            cs.sendMessage(ChatColor.RED + "You need to give your rebellion a name!");
            return true;
        }

        try {
            if ((!admin) && (!res.getTown().hasNation())) {
                cs.sendMessage(ChatColor.RED + "You are not in a nation!");
                return true;
            }
        } catch (NotRegisteredException e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
        }

        try {
            if ((!admin) && (!res.getTown().isMayor(res))) {
                cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
                return true;
            }
        } catch (NotRegisteredException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        try {
            if (res.getTown().getNation().getCapital() == res.getTown()) {
                cs.sendMessage(ChatColor.RED + "You cannot create a rebellion (towards yourself) when you are the capital!");
                return true;
            }
        } catch (NotRegisteredException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        for (Rebellion r : Rebellion.getAllRebellions()) {
            try {
                if (r.isRebelTown(res.getTown()) || r.isRebelLeader(res.getTown())) {
                    cs.sendMessage(ChatColor.RED + "You are already in a rebellion!");
                    return true;
                }
            } catch (NotRegisteredException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for (Rebellion r : Rebellion.getAllRebellions())
            if (r.getName() == strings[1]) {
                cs.sendMessage(ChatColor.RED + "Rebellion with that name already exists!");
                return true;
            }
        if (strings[1].length() > 13) {
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
    private boolean joinRebellion(CommandSender cs, String[] strings, boolean admin) {
        Resident res = null;
        res = TownyUniverse.getInstance().getResident(cs.getName());

        if (strings.length != 2) {
            cs.sendMessage(ChatColor.RED + "You need to specify which rebellion to join!");
            return true;
        }

        try {
            if ((!admin) && (!res.getTown().isMayor(res))) {
                cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
                return true;
            }
        } catch (NotRegisteredException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        try {
            if (res.getTown().getNation().getCapital() == res.getTown()) {
                cs.sendMessage(ChatColor.RED + "You cannot join a rebellion (towards yourself) when you are the capital!");
                return true;
            }
        } catch (NotRegisteredException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        for (Rebellion r : Rebellion.getAllRebellions()) {
            try {
                if (r.isRebelTown(res.getTown()) || r.isRebelLeader(res.getTown())) {
                    cs.sendMessage(ChatColor.RED + "You are already in a rebellion!");
                    return true;
                }
            } catch (NotRegisteredException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for (Rebellion r : Rebellion.getAllRebellions()) {
            try {
                if (r.getName().equals(strings[1]) && res.getTown().getNation() == r.getMotherNation()) {
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
    private boolean leaveRebellion(CommandSender cs, String[] strings, boolean admin) {

        Resident res = null;

        res = TownyUniverse.getInstance().getResident(cs.getName());

        try {
            if ((!admin) && (!res.getTown().isMayor(res))) {
                cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
                return true;
            }
        } catch (NotRegisteredException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        for (Rebellion r : Rebellion.getAllRebellions())
            try {
                if (r.isRebelLeader(res.getTown())) {
                    Rebellion.getAllRebellions().remove(r);
                    cs.sendMessage(ChatColor.RED + "You disbanded your rebellion in your nation!");
                    return true;
                } else if (r.isRebelTown(res.getTown())) {
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
    private boolean executeRebellion(CommandSender cs, String[] strings, boolean admin) {

        Resident res = null;

        res = TownyUniverse.getInstance().getResident(cs.getName());

        try {
            if ((!admin) && (!res.getTown().isMayor(res))) {
                cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
                return true;
            }
        } catch (NotRegisteredException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            if (WarManager.getWarForNation(res.getTown().getNation()) != null) {
                cs.sendMessage(ChatColor.RED + "You can't rebel while your nation is at war!");
                return true;
            }
        } catch (NotRegisteredException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (Rebellion r : Rebellion.getAllRebellions())
            try {
                if (res.getTown().getNation() == r.getMotherNation() && r.isRebelLeader(res.getTown())) {
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

    private boolean declareEnd(CommandSender cs, String[] strings, boolean admin) {
        if ((admin) && (strings.length <= 2)) {
            cs.sendMessage(ChatColor.RED + "You need to specify two nations!");
            return true;
        }
        String sonat = "";
        if (admin) {
            sonat = strings[1];
        }
        Resident res = null;
        Nation nat;
        try {
            if (admin) {
                nat = TownyUniverse.getInstance().getNation(strings[2]);
            } else {
                res = TownyUniverse.getInstance().getResident(cs.getName());
                nat = res.getTown().getNation();
            }
        } catch (Exception ex) {
            cs.sendMessage(ChatColor.RED + "You are not in a town, or your town isn't part of a nation!");
            return true;
        }
        if (!admin && !res.isKing() && !nat.hasAssistant(res)) {
            cs.sendMessage(ChatColor.RED + "You are not powerful enough in your nation to do that!");
            return true;
        }
        if (!admin) {
            War w = WarManager.getWarForNation(nat);  // Assuming that this method has been updated to getWarForEntity
            if (w == null) {
                cs.sendMessage(ChatColor.RED + nat.getName() + " is not at war!");
                return true;
            }
            try {
                sonat = WarManager.getNameForEntity(w.getEnemy(nat));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Nation onat;
        onat = TownyUniverse.getInstance().getNation(sonat);
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

    private boolean declareWar(CommandSender cs, String[] strings, boolean admin) {
        if ((strings.length == 2) && (admin)) {
            cs.sendMessage(ChatColor.RED + "You need to specify two entities (either a nation or a town)!");
            return true;
        }
        if (strings.length == 1) {
            cs.sendMessage(ChatColor.RED + "You need to specify an entity (either a nation or a town)!");
            return true;
        }

        String target = strings[1];
        Object declarer = null;
        Object declaree = null;
        try {
            if (admin) {
                if (TownyUniverse.getInstance().hasTown(target))
                    declaree = TownyUniverse.getInstance().getTown(target);
                else
                    declaree = TownyUniverse.getInstance().getNation(target);
            }
            Resident res = TownyUniverse.getInstance().getResident(cs.getName());
            if (res.hasTown()) {
                if (res.getTown().hasNation()) {
                    declarer = res.getTown().getNation();
                } else {
                    declarer = res.getTown();
                }
            } else {
                cs.sendMessage(ChatColor.RED + "You are not in a town!");
                return true;
            }
            if (TownyUniverse.getInstance().hasTown(target)) {
                declaree = TownyUniverse.getInstance().getTown(target);
                } else {
                    declaree = TownyUniverse.getInstance().getNation(target);
            }
        } catch (Exception ex) {
            cs.sendMessage(ChatColor.RED + "Specified entity (either a nation or a town) does not exist!");
            return true;
        }

        // Check for invalid combinations, e.g. a town declaring war on itself
        if (declarer.equals(declaree)) {
            cs.sendMessage(ChatColor.RED + "You can't declare war on yourself!");
            return true;
        }

        // If it's Nation vs Nation, validate that neither is already in a war
        if (declarer instanceof Nation && declaree instanceof Nation) {
            Nation declaringNation = (Nation) declarer;
            Nation targetNation = (Nation) declaree;
            if (WarManager.getWarForNation(declaringNation) != null || WarManager.getWarForNation(targetNation) != null) {
                cs.sendMessage(ChatColor.RED + "One or both nations are already at war!");
                return true;
            }
        }

        // Assuming checks passed, declare the war
        WarManager.createWar(declarer, declaree, cs, null);
        return true;
    }
    private String getNameForEntity(Object entity) {
        if (entity instanceof Nation) {
            return ((Nation) entity).getName();
        } else if (entity instanceof Town) {
            return ((Town) entity).getName();
        } else {
            return "Unknown";
        }
    }

    public void sendYesNoMessage(Player player) {
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
                .send(player);
        ;
    }
}