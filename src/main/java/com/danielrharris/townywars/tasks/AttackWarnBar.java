package com.danielrharris.townywars.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.WarManager;
import com.danielrharris.townywars.exceptions.Exceptions.NotInWarException;
import com.danielrharris.townywars.exceptions.Exceptions.ParticipantNotFoundException;
import com.danielrharris.townywars.warObjects.War;
import com.danielrharris.townywars.warObjects.WarParticipant;
import com.palmergames.bukkit.towny.object.Resident;

public class AttackWarnBar extends BukkitRunnable{
	
	private float percent;
	private WarParticipant participant;
	private TownyWars plugin;
	private BossBar bossBar;
	
	public AttackWarnBar(WarParticipant participant, TownyWars plugin){
		this.participant = participant;
		this.plugin = plugin;	
	}
	
	@Override
	public void run() {
		War wwar;
		try {
			wwar = WarManager.getWar(participant);
			if(wwar!=null && participant!=null){
				for(Resident r : participant.getResidents()){
					if(r.getPlayer()!=null){
						final Player player = r.getPlayer();
						if(player!=null){								
							percent = 1.0F;
							if(wwar != null && !((Integer)(wwar.getParticipantMaxPoints(participant))).equals(null)){
								try {
									percent = (float)(wwar.getParticipantPoints(participant)/wwar.getParticipantMaxPoints(participant));
									if(TownyWars.getInstance().getConfigInstance().isBossBar){
										if(percent!=0f){
											String barMessage = "&c&l" + participant.getName() + " &r&4&ois Under Attack! &r&4(&fBar is Actual NPs&4)";
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
		} catch (NotInWarException | ParticipantNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
	}
	
}