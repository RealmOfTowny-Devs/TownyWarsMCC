package com.danielrharris.townywars.tasks;

import com.danielrharris.townywars.GriefManager;
import com.danielrharris.townywars.listeners.GriefListener;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class SaveTask extends BukkitRunnable {

    private GriefManager manager;

    public SaveTask(GriefManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        if (GriefManager.getGriefedBlocks() != null) {
            if (!GriefManager.getGriefedBlocks().isEmpty()) {
                this.manager.saveData(GriefManager.getGriefedBlocks());
                Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7[&2TownyWars&7] &eSaving Data..."));
            }
        }
    }
}