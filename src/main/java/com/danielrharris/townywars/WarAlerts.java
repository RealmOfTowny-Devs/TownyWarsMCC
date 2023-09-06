package com.danielrharris.townywars;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.danielrharris.townywars.exceptions.Exceptions.NotInWarException;
import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;
import com.palmergames.bukkit.towny.object.Resident;

import mkremins.fanciful.FancyMessage;

public class WarAlerts {
	
	private static final DecimalFormat d = new DecimalFormat("#.00");
    public static List<UUID> messagedPlayers;
    
    public WarAlerts() {
    	messagedPlayers = new ArrayList<UUID>();
    }
	
	public static void sendFancyAttackMessage(WarParticipant participant) throws NotInWarException{
		String points = "";
		War wwar = participant.getWar();
		if(wwar!=null) {
			
		}
		for(Resident r : participant.getResidents()) {
			if(r.getPlayer()!=null) {
				if(r.getPlayer().isOnline()) {
					Player player = r.getPlayer();
					final UUID uuid = player.getUniqueId();
					if (messagedPlayers.contains(uuid))
					{
						try {
							points = ChatColor.RED + "" + ChatColor.BOLD + participant + ChatColor.DARK_RED + " is Under Attack!";
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							new FancyMessage("                     ")
							.then(d.format((wwar.getParticipantPoints(participant))))
							    .color(ChatColor.YELLOW)
							    .tooltip(points)
							    .command("/twar showtowndp")
							.then(" Defense Points Remaining")
								.color(ChatColor.WHITE)
							    .tooltip(points)
							    .command("/twar showtowndp")
							.send(player);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 10);
					}
					
					if (!messagedPlayers.contains(uuid))
					{
						messagedPlayers.add(uuid);
						
						TownyWars.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(TownyWars.getInstance(), new Runnable() {
							  public void run() {
							      messagedPlayers.remove(uuid);
							  }
							}, 3 * 60 * 20);
						try {
							points = ChatColor.YELLOW + d.format((wwar.getParticipantPoints(participant))) + ChatColor.WHITE + " Points Left";
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						new FancyMessage("                 ")
						.then("g")
							.color(ChatColor.WHITE)
							.style(ChatColor.MAGIC)
						.then("  ")
						.then(participant.getName())
							.color(ChatColor.RED)
							.style(ChatColor.BOLD)
							.tooltip("Click to Travel to " + ChatColor.GREEN + participant)
							.command("/t spawn " + participant)
						.then(" is Under Attack!")
						    .color(ChatColor.DARK_RED)
						    .style(ChatColor.ITALIC)
						    .tooltip(points)
						    .command("/twar showtowndp")
						.then("  ")
						.then("g")
							.color(ChatColor.WHITE)
							.style(ChatColor.MAGIC)
						.send(player);
						player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 10);
					}
				}
			}
		}			
	}
	
}

	