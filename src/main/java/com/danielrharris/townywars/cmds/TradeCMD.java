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
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
                String townName = args[offset + 1];
                try {
                    Resident resident = TownyUniverse.getInstance().getResident(player.getName());
                    if (!TownyUniverse.getInstance().hasTown(townName)) {
                        player.sendMessage("§cInvalid Town Name!");
                        return;
                    }
                    Town town = TownyUniverse.getInstance().getTown(townName);
                    assert resident != null;
                    Town rTown = resident.getTown();
        
                    if (tc.getConfigurationSection(rTown.getName()) != null) {
                        if (rTown.hasResidentWithRank(resident, "Assistant") || rTown.getMayor() == resident) {
                            String sPickOffString = tc.getString(rTown.getName() + ".pickoff");
                            if (sPickOffString == null) {
                                player.sendMessage("§cPick-off location not set!");
                                return;
                            }
                            SLocation sPickOff = SLocation.deSerialize(sPickOffString);
                            Location pickoff = sPickOff.toLocation();
        
                            String sDropOffString = tc.getString(townName + ".dropoff");
                            if (sDropOffString == null) {
                                player.sendMessage("§cDrop-off location not set!");
                                return;
                            }
                            SLocation sDropOff = SLocation.deSerialize(sDropOffString);
                            Location dropoff = sDropOff.toLocation();
        
                            ItemStack stack = null;
                            String sPickUpChestString = tc.getString(rTown.getName() + ".pickup");
                            if (sPickUpChestString != null) {
                                SLocation sPickUpChestLoc = SLocation.deSerialize(sPickUpChestString);
                                Location pickUpChestLoc = sPickUpChestLoc.toLocation();
                                Block chestBlock = pickUpChestLoc.getBlock();
                                if (chestBlock.getType() == Material.CHEST) {
                                    Chest chest = (Chest) chestBlock.getState();
                                    for (ItemStack item : chest.getInventory().getContents()) {
                                        if (item != null && item.getType() != Material.AIR) {
                                            stack = item.clone();
                                            chest.getInventory().removeItem(item);
                                            break;
                                        }
                                    }
                                }
                            }
        
                            if (stack == null || stack.getType().equals(Material.AIR)) {
                                player.sendMessage("§cNo items available in the pickup chest for trading!");
                                return;
                            }
                            
                            World world = pickoff.getWorld();
                            Horse entity = (Horse) world.spawnEntity(pickoff, EntityType.HORSE);
                            entity.setTamed(true); // Ensure the horse is tamed to avoid it running off.
                            entity.setAdult(); // Ensure the horse is not a baby for obvious size reasons.
                            entity.setAI(false); // Turning off AI to manually control its path.
                            
                            double totalDistance = pickoff.distance(dropoff);
                            double speed = TownyWars.getInstance().getConfig().getDouble("trade.speed", 0.25); // Default speed if not set
                            double ticksToComplete = totalDistance / speed;
                            long tickInterval = 1; // Run every tick for smooth movement
                            
                            new BukkitRunnable() {
                                private double elapsedTicks = 0;
                            
                                @Override
                                public void run() {
                                    if (elapsedTicks >= ticksToComplete) {
                                        // Finalize the trade and reward both towns
                                        Bukkit.getScheduler().runTaskLater(TownyWars.getInstance(), entity::remove, 20L); // Remove horse after 1 second
                                        double reward = TownyWars.getInstance().getConfig().getDouble("trade.reward");
                                        town.getAccount().deposit(reward, "Trade Complete with Town \"" + rTown.getName() + "\"");
                                        town.sendMessage(Component.text(ChatColor.translateAlternateColorCodes('&',
                                                "&aTrade Complete with Town \"" + rTown.getName() + "\"" + " &b+$" + reward)));
                                        rTown.getAccount().deposit(reward, "Trade Complete with Town \"" + town.getName() + "\"");
                                        rTown.sendMessage(Component.text(ChatColor.translateAlternateColorCodes('&',
                                                "&aTrade Complete with Town \"" + rTown.getName() + "\"" + " &b+$" + reward)));
                            
                                        this.cancel();
                                        return;
                                    }
                            
                                    double fractionTraveled = elapsedTicks / ticksToComplete;
                                    double x = pickoff.getX() + fractionTraveled * (dropoff.getX() - pickoff.getX());
                                    double z = pickoff.getZ() + fractionTraveled * (dropoff.getZ() - pickoff.getZ());
                                    double y = getGroundYLevel(new Location(world, x, pickoff.getY(), z), new Vector(dropoff.getX() - pickoff.getX(), 0, dropoff.getZ() - pickoff.getZ())) + 1.0; // Adjust Y based on ground level
                            
                                    Location nextLocation = new Location(world, x, y, z);
                                    entity.teleport(nextLocation);
                            
                                    elapsedTicks += tickInterval;
                                }
                            }.runTaskTimer(TownyWars.getInstance(), 0L, tickInterval);                            
                            
                            } else {
                                player.sendMessage("§cYou need to be either the §nMayor§c or an §nAssistant§c to initiate a trade!");
                            }
                    } else {
                        player.sendMessage("§cTrade setup incomplete. Set your §npick-off§c and §ndrop-off§c locations!");
                    }
                } catch (NotRegisteredException | IOException e) {
                    player.sendMessage("§cAn error occurred. Check console for details.");
                    e.printStackTrace();
                }
                break;
            case "set":
                if (args.length < 2 + offset) {
                    player.sendMessage("§cYou need to specify an argument!\n§a/twar trade set <pickoff|dropoff|horse>.");
                    return;
                }
                if (args[offset + 1].equalsIgnoreCase("pickoff")) {
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

                } else if (args[offset + 1].equalsIgnoreCase("dropoff")) {
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

                } else if (args[offset+1].equalsIgnoreCase("dropoffchest")) {
                    Block targetBlock = player.getTargetBlock(null, 5); // get the block player is looking at within 5 block range
                    if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
                        player.sendMessage("§cYou must be looking at a chest!");
                        return;
                    }
                    Resident resident = TownyUniverse.getInstance().getResident(player.getName());
                    try {
                        if (resident.hasTown()) {
                            Town town = resident.getTown();
                            if (town.getMayor() == resident || town.hasResidentWithRank(resident, "Assistant")) {
                                SLocation sDropOffChest = new SLocation(targetBlock.getLocation());
                                tc.set(town.getName() + ".dropoffchest", sDropOffChest.serialize());
                                player.sendMessage("§aSuccessfully set the drop-off chest location.");
                            } else {
                                player.sendMessage("§cYou are not in a high enough role to use this command!");
                            }
                        } else {
                            player.sendMessage("§cYou do not have a town to set a drop-off chest for!");
                        }
                    } catch (NotRegisteredException e) {
                        player.sendMessage("§cAn error occurred while trying to find your town. Make sure you're part of a town!");
                    }

                        player.sendMessage("§cYou either do not have a town or you are not in a high enough role to use this command!");
                } else if (args[offset+1].equalsIgnoreCase("pickupchest")) {
                    try {
                        Resident resident = TownyUniverse.getInstance().getResident(player.getName());
                        Block block = player.getTargetBlock(null, 5); // get the block player is looking at within 5 block range
                        if (block == null || !(block.getState() instanceof Chest)) {
                            player.sendMessage("§cYou must be looking at a chest to set it as the pickup chest!");
                            return;
                        }
                        SLocation sPickUp = new SLocation(block.getLocation());
                        String sPickUpLoc = sPickUp.serialize();
                        if (resident.hasTown() && (resident.getTown().getMayor() == resident || resident.getTown().hasResidentWithRank(resident, "Assistant"))) {
                            tc.set(resident.getTown().getName() + ".pickup", sPickUpLoc);
                            player.sendMessage("§aSuccessfully set the pickup chest location.");
                        } else {
                            player.sendMessage("§cYou either do not have a town or you are not in a high enough role to use this command!");
                        }
                    } catch (NotRegisteredException e) {
                        e.printStackTrace();
                    }
                } else {
                    player.sendMessage("§cUnknown trade Command!");
            }
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
        player.sendMessage(new String[]{
                "§a§lTOWNY WARS TRADE HELP",
                "§m---------------------------",
                "§7Use the commands below to manage and view trades:",
                "",
                "§6TRADE COMMANDS:",
                "§a/twar trade request <townName> §f- Request a trade. §8[ASSISTANT,MAYOR]",
                "§a/twar trade accept <townName> §f- Accept a trade request. §8[ASSISTANT,MAYOR]",
                "§a/twar trade deny <townName> §f- Deny a trade request. §8[ASSISTANT,MAYOR]",
                "§a/twar trade status <townName> §f- View trade status.",
                "",
                "§6SETUP COMMANDS:",
                "§a/twar trade set pickoff §f- Set pick-off location for horse. §8[ASSISTANT,MAYOR]",
                "§a/twar trade set dropoff §f- Set drop-off location for horse. §8[ASSISTANT,MAYOR]",
                "§a/twar trade set dropoffchest §f- Set drop-off chest location. §8[ASSISTANT,MAYOR]",
                "§a/twar trade set pickupchest §f- Set pickup chest location. §8[ASSISTANT,MAYOR]",
                "",
                "§7Remember to set up both chest and horse locations before initiating a trade!"
        });
    }

}
