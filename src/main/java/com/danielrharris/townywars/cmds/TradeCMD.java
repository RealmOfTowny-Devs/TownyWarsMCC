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
                            SLocation sPickOff = SLocation.deSerialize(tc.getString(rTown.getName() + ".pickoff"));
                            Location pickoff = sPickOff.toLocation();
                            SLocation sDropOff = SLocation.deSerialize(tc.getString(townName + ".dropoff"));
                            Location dropoff = sDropOff.toLocation();

                            ItemStack stack = null;
                            SLocation sDropOffChestLoc = SLocation.deSerialize(tc.getString(rTown.getName() + ".dropoffchest"));
                            if (sDropOffChestLoc != null) {
                                Location dropOffChestLoc = sDropOffChestLoc.toLocation();
                                Block chestBlock = dropOffChestLoc.getBlock();
                                if (chestBlock.getType() == Material.CHEST || chestBlock.getType() == Material.TRAPPED_CHEST) {
                                    Chest chest = (Chest) chestBlock.getState();
                                    Inventory inv = chest.getInventory();
                                    for (ItemStack item : inv.getContents()) {
                                        if (item != null && item.getType() != Material.AIR) {
                                            stack = item.clone();
                                            inv.removeItem(item);
                                            break;
                                        }
                                    }
                                }
                            }

                            if (stack == null || stack.getType().equals(Material.AIR)) {
                                player.sendMessage("§cNo items available in the drop-off chest for trading!");
                                return;
                            }

                            World world = pickoff.getWorld();
                            Horse entity = (Horse) world.spawnEntity(pickoff, EntityType.HORSE);
                            entity.setAI(false);

                            double totalDistance = pickoff.distance(dropoff);
                            double speed = TownyWars.getInstance().getConfig().getDouble("trade.speed");
                            double distancePerTick = speed / 20.0 * totalDistance;

                            ItemStack finalStack = stack;
                            new BukkitRunnable() {
                                double traveledDistance = 0.0;

                                @Override
                                public void run() {
                                    // ... [The code inside this method remains unchanged.]
                                }
                            }.runTaskTimer(TownyWars.getInstance(), 0L, 1L);
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
