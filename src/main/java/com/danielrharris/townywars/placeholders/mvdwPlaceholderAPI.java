package com.danielrharris.townywars.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.WarManager;

public class mvdwPlaceholderAPI {
   
	public mvdwPlaceholderAPI(TownyWars plugin) {
		
        PlaceholderReplacer isAtWar = event -> {
            if (event.getPlayer() == null) return "";
            return Boolean.toString(WarManager.isAtWar(event.getPlayer()));
        };
        PlaceholderAPI.registerPlaceholder(plugin, "isAtWar", (PlaceholderReplacer)isAtWar);
        
        
    
    }
    
    public String translatePlaceholders(CommandSender sender, String message) {
    	if(sender instanceof Player)
    	    return PlaceholderAPI.replacePlaceholders((Player)sender, message);
    	else if(sender instanceof OfflinePlayer)
    	    return PlaceholderAPI.replacePlaceholders((OfflinePlayer)sender, message);
    	else
    		return PlaceholderAPI.replacePlaceholders(null, message);
    }
}