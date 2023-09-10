package com.danielrharris.townywars;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.danielrharris.townywars.exceptions.Exceptions.NotInWarException;
import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;
import com.palmergames.bukkit.towny.object.Resident;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

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
					{//This will probably need fixed
						try {
							points = ChatColor.RED + "" + ChatColor.BOLD + participant.getName() + ChatColor.DARK_RED + " is Under Attack!";
							BaseComponent comp = new TextComponent("");
							comp.addExtra(d.format((wwar.getParticipantPoints(participant))));
							comp.setColor(ChatColor.YELLOW);
							HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(points).create());
							comp.setHoverEvent(event);
							ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twar showtowndp");
							comp.setClickEvent(clickEvent);
							player.spigot().sendMessage(comp);
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
						///////////////////////////         Recode to use bungee api instead
						/*new FancyMessage("                 ")
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
						.send(player);*/
						player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 10);
					}
				}
			}
		}			
	}
	
}

	