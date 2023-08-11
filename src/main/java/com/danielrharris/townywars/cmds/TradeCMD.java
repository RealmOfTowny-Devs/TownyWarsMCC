package com.danielrharris.townywars.cmds;

import com.danielrharris.townywars.SLocation;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.trades.TradeFile;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public class TradeCMD {

    private static TradeFile tf = TownyWars.getInstance().getTradeFile();
    private static FileConfiguration tc = tf.getYamlConfiguration();

    public static boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED.toString() + "You cannot use this via the console.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 1) {
            sendHelpMenu(player);
        } else {
            handleArgs(args, player);
        }
        return true;
    }

    private static void handleArgs(String[] args, Player player) {
        int offset = 1;
        switch (args[offset+0]) {
            case "request":
                if (args.length < 2 + offset) {
                    player.sendMessage("§cSpecify a town you want to trade with!");
                    return;
                }
                String townName = args[offset+1];
                try {
                    Resident resident = TownyUniverse.getInstance().getResident(player.getName());
                    if(!TownyUniverse.getInstance().hasTown(townName)) {
                        player.sendMessage("§cInvalid Town Name!");
                        return;
                    }
                    Town town = TownyUniverse.getInstance().getTown(townName);
                    assert resident != null;
                    Town rTown = resident.getTown();

                    if (tc.getConfigurationSection(rTown.getName()) != null) {
                        if (rTown.hasResidentWithRank(resident, "Assistant") || rTown.getMayor() == resident) {
                            SLocation sPickOff = SLocation.deSerialize(tc.getString(rTown.getName() + ".pickoff"));
                            Location pickoff = sPickOff.toLocation();
                            SLocation sDropOff = SLocation.deSerialize(tc.getString(townName + ".dropoff"));
                            Location dropoff = sDropOff.toLocation();

                            World world = pickoff.getWorld();
                            Horse entity = (Horse) world.spawnEntity(pickoff, EntityType.HORSE);
                            entity.setAI(false);


                            double totalDistance = pickoff.distance(dropoff);
                            int speed = 5;
                            double distancePerTick = speed / 20.0 * totalDistance; // assuming speed is in blocks per second and server runs at 20 TPS

                            // 2. Calculate the Path and Move the Entity
                            new BukkitRunnable() {
                                double traveledDistance = 0.0;

                                @Override
                                public void run() {
                                    double fractionTraveled = traveledDistance / totalDistance;

                                    if (fractionTraveled > 1) {
                                        this.cancel();
                                        return;
                                    }

                                    double x = pickoff.getX() + fractionTraveled * (dropoff.getX() - pickoff.getX());
                                    double y = pickoff.getY() + fractionTraveled * (dropoff.getY() - pickoff.getY());
                                    double z = pickoff.getZ() + fractionTraveled * (dropoff.getZ() - pickoff.getZ());

                                    Location nextLocation = new Location(world, x, y, z);

                                    // 3. Load Chunks
                                    Chunk chunk = nextLocation.getChunk();
                                    if (!chunk.isLoaded()) {
                                        chunk.load();
                                    }

                                    // 4. Move the Entity
                                    entity.teleport(nextLocation);

                                    traveledDistance += distancePerTick;
                                }
                            }.runTaskTimer(TownyWars.getInstance(), 0L, 1L);
                        } else {
                            player.sendMessage("§cYou are neither the §nMayor§c of this town or an §nAssistant§c!");
                        }
                    } else {
                        player.sendMessage("§cYou haven't set up your §nhorse§c, §npick-off§c and §ndrop-off§c locations!");
                    }
                } catch (NotRegisteredException | IOException e) {
                    e.printStackTrace();
                }
                break;
            case "set":
                if (args.length < 2 + offset) {
                    player.sendMessage("§cYou need to specify an argument!\n§a/twar trade set <pickoff|dropoff|horse>.");
                    return;
                }
                if (args[offset+1].equalsIgnoreCase("pickoff")) {
                    try {
                        Resident resident = TownyUniverse.getInstance().getResident(player.getName());
                        SLocation sPickOff = new SLocation(player.getLocation());
                        String sPickoffLoc = sPickOff.serialize();
                        if (resident.hasTown() && (resident.getTown().getMayor() == resident || resident.getTown().hasResidentWithRank(resident, "Assistant"))) {
                            tc.set(resident.getTown().getName() + ".pickoff", sPickoffLoc);
                            player.sendMessage("§aSuccessfully set the pick-off location to: §e" + Math.round(sPickOff.getX()) + ":" + Math.round(sPickOff.getY()) + ":" + Math.round(sPickOff.getZ()) + ".");
                        } else {
                            player.sendMessage("§cYou either do not have a town or you are not in a high enough role to use this command!");
                        }
                    } catch (NotRegisteredException e) {
                        e.printStackTrace();
                    }

                } else if (args[offset+1].equalsIgnoreCase("dropoff")) {
                    try {
                        Resident resident = TownyUniverse.getInstance().getResident(player.getName());
                        SLocation sDropOff = new SLocation(player.getLocation());
                        String sDropOffLoc = sDropOff.serialize();
                        if (resident.hasTown() && (resident.getTown().getMayor() == resident || resident.getTown().hasResidentWithRank(resident, "Assistant"))) {
                            tc.set(resident.getTown().getName() + ".dropoff", sDropOffLoc);
                            player.sendMessage("§aSuccessfully set the drop-off location to: §e" + Math.round(sDropOff.getX()) + ":" + Math.round(sDropOff.getY()) + ":" + Math.round(sDropOff.getZ()) + ".");
                        } else {
                            player.sendMessage("§cYou either do not have a town or you are not in a high enough role to use this command!");
                        }
                    } catch (NotRegisteredException e) {
                        e.printStackTrace();
                    }

                } else if (args[offset+1].equalsIgnoreCase("horse")) {
                    if (player.isInsideVehicle() && player.getVehicle().getType() == EntityType.HORSE) {
                        Horse horse = (Horse) player.getVehicle();
                        if (horse.isTamed() && horse.isCarryingChest()) {
                            player.sendMessage("§aThat horse is valid to be set as your trading horse! Setting everything up now...");
                            //todo save the horse to the flat file. @Myekaan.
                            player.sendMessage("§aThe horse was set!");
                        } else {
                            player.sendMessage("§cThe horse isn't tamed or is not carrying a chest!");
                        }
                    } else {
                        player.sendMessage("§cThat's not a horse!");
                    }
                }

                tf.save();
                break;
        }
    }

    private static void sendHelpMenu(Player player) {
        player.sendMessage(new String[]{"§a -- Trade help menu --",
                "§a/twar trade request <townName> - Request a trade with a town. [ASSISTANT,MAYOR]",
                "§a/twar trade accept <townName> - Accept a trade request.[ASSISTANT,MAYOR]",
                "§a/twar trade deny <townName> - Deny a trade request.[ASSISTANT,MAYOR]",
                "§a/twar trade status <townName> - View the status of a trade.",
                "",
                "§aTRADE SET COMMANDS: ",
                "§a/twar trade set pickoff - Set the pick-off location of your horse.[ASSISTANT,MAYOR]",
                "§a/twar trade set dropoff - Set the drop-off location of your horse.[ASSISTANT,MAYOR]",
                "§a/twar trade set horse - Set the horse you are going to use for your trades.[ASSISTANT,MAYOR]"});
    }
}
