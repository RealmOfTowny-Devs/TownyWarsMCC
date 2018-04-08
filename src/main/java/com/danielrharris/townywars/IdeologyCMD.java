package com.danielrharris.townywars;

import com.danielrharris.townywars.ideologies.IdeologiesManager;
import com.danielrharris.townywars.ideologies.Ideology;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IdeologyCMD implements CommandExecutor{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players can use this command, sorry!");
            return true;
        }
        Player player = (Player) sender;

        Resident resident = null;
        try {
            resident = TownyUniverse.getDataSource().getResident(player.getName());
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }
        assert resident != null;
        assert resident.hasTown();
        Town town = null;
        try {
            town = resident.getTown();
        } catch (NotRegisteredException e) {
        	player.sendMessage(ChatColor.RED + "You must be in a town to use this command!");
        	return true;
            //e.printStackTrace();
        }
        if(IdeologiesManager.getIdeologiesManager().hasIdeology(town)){
        	Ideology id = IdeologiesManager.getIdeologiesManager().getIdeology(town);
        	String idName = id.getName();
            player.sendMessage(ChatColor.RED + "Your town already has " + ChatColor.GREEN + idName + ChatColor.RED + " as your ideology! You may not change your ideology, it has already been selected and is permanent.");
            return true;
        }else {
            if(args.length == 0){
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD .toString() + "!!WARNING!! THIS IS A PERMANENT DECISION. CHOOSE CAREFULLY!\n" +ChatColor.GREEN.toString() + "Choose between the following 3 ideologies:\n§a - Economic - Gains additional defense points for having money in your town bank (per thousand dollars)" +
                        "\n§a - Religious - Get additional defense points for having more users in your town in addition to that." +
                        "\n§a - Militaristic - Get additional defense points for having more walls and more land.");
                return true;
            }
            if (args[0].equalsIgnoreCase("economic")) {
                if(resident.isMayor()){
                    IdeologiesManager.getIdeologiesManager().addIdeologyToTown(Ideology.ECONOMIC,town);
                    player.sendMessage(ChatColor.GREEN.toString() + "Successfully chose \"Economic\" as your ideology.");
                    //todo EXECUTE THE RUNNABLE
                    return true;
                }else {
                    player.sendMessage(ChatColor.RED.toString() + "You are not the mayor!");
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("religious")) {
                if(resident.isMayor()){
                    IdeologiesManager.getIdeologiesManager().addIdeologyToTown(Ideology.RELIGIOUS,town);
                    player.sendMessage(ChatColor.GREEN.toString() + "Successfully chose \"Religious\" as your ideology.");
                    //todo EXECUTE THE RUNNABLE
                    return true;
                }else {
                    player.sendMessage(ChatColor.RED.toString() + "You are not the mayor!");
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("militaristic")) {
                if(resident.isMayor()){
                    IdeologiesManager.getIdeologiesManager().addIdeologyToTown(Ideology.MILITARISTIC,town);
                    player.sendMessage(ChatColor.GREEN.toString() + "Successfully chose \"Militaristic\" as your ideology.");
                    //todo EXECUTE THE RUNNABLE
                    return true;
                }else {
                    player.sendMessage(ChatColor.RED.toString() + "You are not the mayor!");
                    return true;
                }
            }
            player.sendMessage(ChatColor.RED + "Invalid ideology! Your choices are:");
            player.sendMessage(ChatColor.RED + " - Religious");
            player.sendMessage(ChatColor.RED + " - Militaristic");
            player.sendMessage(ChatColor.RED + " - Economic");
        }
        return true;
    }
}
