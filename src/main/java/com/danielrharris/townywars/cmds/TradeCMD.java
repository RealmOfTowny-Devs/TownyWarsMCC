package com.danielrharris.townywars.cmds;

import com.danielrharris.townywars.SLocation;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.trades.TradeFile;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.io.IOException;

public class TradeCMD implements CommandExecutor {

    private TradeFile tf = TownyWars.getInstance().getTradeFile();
    private FileConfiguration tc = tf.getYamlConfiguration();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED.toString() + "You cannot use this via the console.");
            return true;
        }
        Player player = (Player) sender;
        if(args.length == 0){
            sendHelpMenu(player);
        }else {
            handleArgs(args,player);
        }
        return true;
    }

    private void handleArgs(String[] args, Player player) {
        switch (args[0]){
            case "request":
                if(args.length < 2){
                    player.sendMessage("§cSpecify a town you want to trade with!");
                    return;
                }
                String townName = args[0];
                try {
                    Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
                    Town town = TownyUniverse.getDataSource().getTown(townName);
                    Town rTown = resident.getTown();

                    if(tc.getConfigurationSection(rTown.getName()) != null) {
                        if(rTown.hasAssistant(resident) || rTown.getMayor() == resident){
                            SLocation sPickOff = SLocation.deSerialize(tc.getString(rTown.getName() + ".pickoff"));
                            Location pickoff = sPickOff.toLocation();
                            SLocation sDropOff = SLocation.deSerialize(tc.getString(rTown.getName() + ".dropoff"));
                            Location dropoff = sDropOff.toLocation();



                        }else {
                            player.sendMessage("§cYou are neither the §nMayor§c of this town or an §nAssistant§c!");
                        }
                    }else {
                        player.sendMessage("§cYou haven't set up your §nhorse§c, §npick-off§c and §ndrop-off§c locations!");
                    }
                } catch (NotRegisteredException | IOException e) {
                    e.printStackTrace();
                }
                break;
            case "set":
                if(args.length < 2){
                    player.sendMessage("§cYou need to specify an argument!\n§a/trade set <pickoff|dropoff|horse>.");
                    return;
                }
                if (args[1].equalsIgnoreCase("pickoff")) {
                    try {
                        Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
                        SLocation sPickOff = new SLocation(player.getLocation());
                        String sPickoffLoc = sPickOff.serialize();
                        if(resident.hasTown() && resident.getTown().getMayor() == resident && resident.getTown().hasAssistant(resident)) {
                            tc.set(resident.getTown().getName() + ".pickoff",sPickoffLoc);
                            player.sendMessage("§aSuccessfully set the pick-off location to: §e" + Math.round(sPickOff.getX()) + ":" + Math.round(sPickOff.getY()) + ":" + Math.round(sPickOff.getZ()) + ".");
                        }else {
                            player.sendMessage("§cYou either do not have a town or you are not in a high enough role to use this command!");
                        }
                    } catch (NotRegisteredException e) {
                        e.printStackTrace();
                    }

                }else if (args[1].equalsIgnoreCase("dropoff")){
                    try {
                        Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
                        SLocation sDropOff = new SLocation(player.getLocation());
                        String sDropOffLoc = sDropOff.serialize();
                        if(resident.hasTown() && resident.getTown().getMayor() == resident && resident.getTown().hasAssistant(resident)) {
                            tc.set(resident.getTown().getName() + ".dropoff",sDropOffLoc);
                            player.sendMessage("§aSuccessfully set the drop-off location to: §e" + Math.round(sDropOff.getX()) + ":" + Math.round(sDropOff.getY()) + ":" + Math.round(sDropOff.getZ()) + ".");
                        }else {
                            player.sendMessage("§cYou either do not have a town or you are not in a high enough role to use this command!");
                        }
                    } catch (NotRegisteredException e) {
                        e.printStackTrace();
                    }

                }else if (args[1].equalsIgnoreCase("horse")){
                    if(player.isInsideVehicle() && player.getVehicle().getType() == EntityType.HORSE){
                        Horse horse = (Horse) player.getVehicle();
                        if(horse.isTamed() && horse.isCarryingChest()){
                            player.sendMessage("§aThat horse is valid to be set as your trading horse! Setting everything up now...");
                            //todo save the horse to the flat file. @Myekaan.
                            player.sendMessage("§aThe horse was set!");
                        }else {
                            player.sendMessage("§cThe horse isn't tamed or is not carrying a chest!");
                        }
                    }else {
                        player.sendMessage("§cThat's not a horse!");
                    }
                }
                break;
        }
    }

    private void sendHelpMenu(Player player) {
        player.sendMessage(new String[]{"§a -- Trade help menu --",
        "§a/trade request <townName> - Request a trade with a town. [ASSISTANT,MAYOR]",
        "§a/trade accept <townName> - Accept a trade request.[ASSISTANT,MAYOR]",
        "§a/trade deny <townName> - Deny a trade request.[ASSISTANT,MAYOR]",
        "§a/trade status <townName> - View the status of a trade.",
        "",
        "§aTRADE SET COMMANDS: ",
        "§a/trade set pickoff - Set the pick-off location of your horse.[ASSISTANT,MAYOR]",
        "§a/trade set dropoff - Set the drop-off location of your horse.[ASSISTANT,MAYOR]",
        "§a/trade set horse - Set the horse you are going to use for your trades.[ASSISTANT,MAYOR]"});
    }
}
