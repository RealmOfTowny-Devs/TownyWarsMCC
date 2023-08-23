package com.danielrharris.townywars.tasks;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.WarManager;
import com.danielrharris.townywars.warObjects.War;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import mkremins.fanciful.FancyMessage;
import net.md_5.bungee.api.chat.TextComponent;

public class AttackWarnBarTask extends BukkitRunnable{
	
	private float percent;
	private Town town = null;
	private Nation nation = null;
	private TownyWars plugin;
	private DecimalFormat d = new DecimalFormat("#.00");
	private BossBar bossBar;
	
	public AttackWarnBarTask(Town town, TownyWars plugin){
		this.town = town;
		try {
			if(town.hasNation()){
				this.nation = town.getNation();
			}			
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.plugin = plugin;
		
	}
	
	@Override
	public void run() {
		War wwar = null;
		if(nation!=null){
			for(Resident r : nation.getResidents()){
				if(r.getName()!=null){
					final Player player = Bukkit.getServer().getPlayer(r.getName());
					if(player!=null){								
						percent = 1.0F;
						wwar = WarManager.getWarForNation(nation);
						if(wwar != null && !((Integer)(wwar.getParticipantMaxPoints(nation.getName()))).equals(null)){
							try {
								percent = (float)(wwar.getParticipantPoints(nation.getName())/wwar.getParticipantMaxPoints(nation.getName()));
								if(TownyWars.isBossBar){
									if(percent!=0f){
										String barMessage = "&c&l" + nation.getName() + " &r&4&ois Under Attack! &r&4(&fBar is Actual NPs&4)";
										bossBar = Bukkit.createBossBar(
												net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', barMessage),
									            BarColor.RED,
									            BarStyle.SEGMENTED_20);
										bossBar.setProgress(percent);		
										bossBar.addPlayer(player);
										bossBar.setVisible(true);
										new BukkitRunnable(){
											@Override
											public void run() {
												if(bossBar!=null)
													if(bossBar.getPlayers().contains(player)){
														bossBar.removePlayer(player);
													}											
											}				
										}.runTaskLater(plugin, 140L);
									}
								}else{
									sendAttackMessage(player, wwar, nation.getName());
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}				
					}
				}
			}
		}else if(town!=null) {
			for(Resident r : town.getResidents()){
				if(r.getName()!=null){
					final Player player = Bukkit.getServer().getPlayer(r.getName());
					if(player!=null){								
						percent = 1.0F;
						wwar = WarManager.getWarForTown(town);
						if(wwar != null && !((Integer)(wwar.getParticipantMaxPoints(town.getName()))).equals(null)){
							try {
								percent = (float)(wwar.getParticipantPoints(town.getName())/wwar.getParticipantMaxPoints(town.getName()));
								if(TownyWars.isBossBar){
									if(percent!=0f){
										String barMessage = "&c&l" + town.getName() + " &r&4&ois Under Attack! &r&4(&fBar is Actual TPs&4)";
										bossBar = Bukkit.createBossBar(
												net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', barMessage),
									            BarColor.RED,
									            BarStyle.SEGMENTED_20);
										bossBar.setProgress(percent);		
										bossBar.addPlayer(player);
										bossBar.setVisible(true);
										new BukkitRunnable(){
											@Override
											public void run() {
												if(bossBar!=null)
													if(bossBar.getPlayers().contains(player)){
														bossBar.removePlayer(player);
													}											
											}				
										}.runTaskLater(plugin, 140L);
									}
								}else{
									sendAttackMessage(player, wwar, town.getName());
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}				
					}
				}
			}
		}	
	} 
	
	public void sendAttackMessage(Player player, War wwar, String participant){
		String points = "";
		final String name = player.getName();
		
		if (TownyWars.messagedPlayers.contains(name))
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
		
		if (!TownyWars.messagedPlayers.contains(name))
		{
			TownyWars.messagedPlayers.add(name);
			
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				  public void run() {
				      TownyWars.messagedPlayers.remove(name);
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
			.then(town.getName())
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