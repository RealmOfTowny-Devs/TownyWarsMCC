package com.danielrharris.townywars.tasks;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.bossbar.BossBar;
import org.inventivetalent.bossbar.BossBarAPI;

import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.War;
import com.danielrharris.townywars.WarManager;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import net.md_5.bungee.api.chat.TextComponent;

public class ShowDPTask extends BukkitRunnable{
	
	private Town town;
	private TownyWars plugin;
	private Nation nation;
	private float percent;
	private DecimalFormat d = new DecimalFormat("#.00");
	
	public ShowDPTask(Town town, TownyWars plugin){
		this.town = town;
		this.plugin = plugin;
		try {
			if(town.hasNation()){
				this.nation = town.getNation();
			}			
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "deprecation", "unused" })
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
						if(wwar != null && !((Double)(War.getTownMaxPoints(town))).equals(null)){
							try {
								percent = (float)((wwar.getTownPoints(town)/((Double)War.getTownMaxPoints(town)).intValue()));
								if(percent!=0f){
									if(BossBarAPI.hasBar(player)){
										BossBarAPI.removeAllBars(player);
									}
									String barMessage = "&a&l" + town.getName() + " &r&eDefense points: &b" + d.format(wwar.getTownPoints(town)) + "&f/&3" + ((Double)War.getTownMaxPoints(town)).intValue();
									final BossBar bossBar = BossBarAPI.addBar(player,
											new TextComponent(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', barMessage)), // Displayed message
									   BossBarAPI.Color.RED,
									   BossBarAPI.Style.PROGRESS, 
									   percent,
									   2000,
									   5000);
									new BukkitRunnable(){
										@Override
										public void run() {
											if(BossBarAPI.hasBar(player)){
												BossBarAPI.removeAllBars(player);
											}											
										}				
									}.runTaskLater(plugin, 200L);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}else if(!((Double)War.getTownMaxPoints(town)).equals(null)){
							if(BossBarAPI.hasBar(player)){
								BossBarAPI.removeAllBars(player);
							}
							String barMessage = "&a&l" + town.getName() + " &r&eDefense points: &b" + ((Double)War.getTownMaxPoints(town)).intValue() + "&f/&3" + ((Double)War.getTownMaxPoints(town)).intValue();
							final BossBar bossBar = BossBarAPI.addBar(player,
									new TextComponent(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', barMessage)), // Displayed message
							   BossBarAPI.Color.RED,
							   BossBarAPI.Style.PROGRESS, 
							   percent,
							   2000,
							   5000);
							new BukkitRunnable(){
								@Override
								public void run() {
									if(BossBarAPI.hasBar(player)){
										BossBarAPI.removeAllBars(player);
									}											
								}				
							}.runTaskLater(plugin, 200L);
						}				
					}
				}
			}
		}
	}
}