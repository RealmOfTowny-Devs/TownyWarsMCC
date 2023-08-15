package com.danielrharris.townywars.cmds;

import com.danielrharris.townywars.SLocation;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.trades.TradeFile;
import com.palmergames.adventure.text.Component;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.Arrays;

public class TradeCMD {

    private static TradeFile tf = TownyWars.getInstance().getTradeFile();
    private static FileConfiguration tc = tf.getYamlConfiguration();

    public static void handleArgs(String[] args, Player player) {
        int offset = 0;

        // Check if args length is at least offset+1
        if (args.length <= offset) {
            sendHelpMenu(player);
            return;
        }

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

                            final ItemStack stack = player.getInventory().getItemInMainHand();
                            if(stack.getType().equals(Material.AIR)) {
                                player.sendMessage("§cCan't trade air!");
                                return;
                            }
                            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                            World world = pickoff.getWorld();
                            Horse entity = (Horse) world.spawnEntity(pickoff, EntityType.HORSE);
                            entity.setAI(false);


                            double totalDistance = pickoff.distance(dropoff);
                            double speed = TownyWars.getInstance().getConfig().getDouble("trade.speed");
                            double distancePerTick = speed / 20.0 * totalDistance; // assuming speed is in blocks per second and server runs at 20 TPS

                            // 2. Calculate the Path and Move the Entity
                            new BukkitRunnable() {
                                double traveledDistance = 0.0;

                                @Override
                                public void run() {
                                    double fractionTraveled = traveledDistance / totalDistance;

                                    if (fractionTraveled > 1) {
                                        double reward = TownyWars.getInstance().getConfig().getDouble("trade.reward");
                                        town.getAccount().deposit(reward,
                                                "Trade Complete with Town \"" + rTown.getName() +"\"");
                                        town.sendMessage(Component.text(ChatColor.translateAlternateColorCodes('&',
                                                "&aTrade Complete with Town \"" + rTown.getName() +"\"" +
                                                        " &b+$"+reward)));
                                        rTown.getAccount().deposit(reward,
                                                "Trade Complete with Town \"" + town.getName() +"\"");
                                        rTown.sendMessage(Component.text(ChatColor.translateAlternateColorCodes('&',
                                                "&aTrade Complete with Town \"" + rTown.getName() +"\""+
                                                        " &b+$"+reward)));
                                        entity.getLocation().getWorld().dropItem(entity.getLocation(), stack);
                                        Bukkit.getScheduler().runTaskLater(TownyWars.getInstance(), entity::remove, 30*20);
                                        this.cancel();
                                        return;
                                    }

                                    double x = pickoff.getX() + fractionTraveled * (dropoff.getX() - pickoff.getX());
                                    double y = pickoff.getY() + fractionTraveled * (dropoff.getY() - pickoff.getY());
                                    double z = pickoff.getZ() + fractionTraveled * (dropoff.getZ() - pickoff.getZ());

                                    Location nextLocation = new Location(world, x, y, z);
                                    Vector direction = dropoff.clone().subtract(nextLocation).toVector();
                                    if (!isPathClear(nextLocation, direction)) {
                                        // Set the location to the top of the obstacle
                                        nextLocation.setY(getTopYLevel(nextLocation, direction));
                                    } else {
                                        // Descend to the ground level if there's nothing beneath or if path below isn't clear
                                        double groundY = getGroundYLevel(nextLocation, direction);
                                        nextLocation.setY(groundY);
                                    }

                                    // 3. Load Chunks
                                    Chunk chunk = nextLocation.getChunk();
                                    if (!chunk.isLoaded()) {
                                        chunk.load();
                                    }

                                    if(entity.isDead()) {
                                        entity.getLocation().getWorld().dropItem(entity.getLocation(), stack);
                                        cancel();
                                    }
                                    nextLocation.setDirection(direction);
                                    entity.teleport(nextLocation);

                                    // 4. Move the Entity

                                    traveledDistance += distancePerTick;
                                }
                            }.runTaskTimer(TownyWars.getInstance(), 0L, 1L);
                        } else {
                            player.sendMessage("§cYou are neither the §nMayor§c of this town or an §nAssistant§c!");
                        }
                    } else {
                        player.sendMessage("§cYou haven't set up your §npick-off§c and §ndrop-off§c locations!");
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

                } else player.sendMessage("§cUnknown trade Command!");

                tf.save();
                break;
        }
    }

    private static boolean isPathClear(Location loc, Vector direction) {
        World world = loc.getWorld();
        int baseX = loc.getBlockX();
        int baseY = loc.getBlockY();
        int baseZ = loc.getBlockZ();
        boolean moveAlongX = Math.abs(direction.getX()) > Math.abs(direction.getZ());

        for (int y = baseY; y < baseY + 3; y++) {
            for (int offset = -1; offset <= 1; offset++) {
                int checkX = moveAlongX ? baseX + offset : baseX;
                int checkZ = moveAlongX ? baseZ : baseZ + offset;

                if (world.getBlockAt(checkX, y, checkZ).getType().isSolid()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double getGroundYLevel(Location location, Vector direction) {
        double originalY = location.getY();
        while (location.getY() > 0 && (isBelowClear(location, direction) || location.getBlock().getType() == Material.AIR)) {
            location.add(0, -1, 0); // Go down
        }

        // If we reached the bedrock and it's still not clear, place the horse at the original Y-level
        if (location.getY() <= 0 && (isBelowClear(location, direction) || location.getBlock().getType() == Material.AIR)) {
            return originalY;
        }

        return location.getY() + 1;  // +1 to make sure the horse stands on top of the block
    }

    private static boolean isBelowClear(Location location, Vector direction) {
        Location checkLocation = location.clone();
        for (int i = 1; i <= 3; i++) {
            checkLocation.add(0, -1, 0); // Go down
            if (!isPathClear(checkLocation, direction)) {
                return false;
            }
        }
        return true;
    }

    private static double getTopYLevel(Location location, Vector direction) {
        double originalY = location.getY();
        while (!isPathClear(location, direction) && location.getY() < location.getWorld().getMaxHeight()) {
            location.add(0, 1, 0);
        }

        // If we reached the max height and it's still not clear, keep the horse at the same Y-level
        if (location.getY() >= location.getWorld().getMaxHeight() && !isPathClear(location, direction)) {
            return originalY;
        }

        return location.getY();
    }


    public static void sendHelpMenu(Player player) {
        player.sendMessage(new String[]{"§a -- Trade help menu --",
                "§a/twar trade request <townName> - Request a trade with a town. [ASSISTANT,MAYOR]",
                "§a/twar trade accept <townName> - Accept a trade request.[ASSISTANT,MAYOR]",
                "§a/twar trade deny <townName> - Deny a trade request.[ASSISTANT,MAYOR]",
                "§a/twar trade status <townName> - View the status of a trade.",
                "",
                "§aTRADE SET COMMANDS: ",
                "§a/twar trade set pickoff - Set the pick-off location of your horse.[ASSISTANT,MAYOR]",
                "§a/twar trade set dropoff - Set the drop-off location of your horse.[ASSISTANT,MAYOR]",
        });
    }
}
