package com.danielrharris.townywars.placeholders;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.danielrharris.townywars.TownyWars;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPI extends PlaceholderExpansion {
	
	private final TownyWars plugin;
	
	public PlaceholderAPI(TownyWars plugin) {
		this.plugin = plugin;
	}
	
    @Override
    public boolean persist(){
        return true;
    }  

   @Override
   public boolean canRegister(){
       return true;
   }

   @Override
   public String getAuthor(){
       return plugin.getDescription().getAuthors().toString();
   }

	@Override
	public String getIdentifier(){
		return this.plugin.getLanguage().pluginName;
	}

	@Override
	public String getVersion(){
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(@NotNull Player player, String identifier){
		if(identifier.equals("is_disabled")){
           // return Boolean.toString(PiggybackAPI.isDisabled(player));  /////Register placeholders here
		}
		return null;
	}
	
	public String translatePlaceholders(@NotNull Player p, String message) {
		return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, message);
	}
	
}