package com.danielrharris.townywars.config;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

import com.danielrharris.townywars.TownyWars;

import net.md_5.bungee.api.ChatColor;

public class TownyWarsLanguage 
{
	private TownyWars plugin;
	private File languageFile;
	private FileConfiguration language;
	private File languageFolder;
	
	public String pluginName;
	public String messagePrefix;
	public String mainCommandString;
	public String[] commandNameMain;
	
	public TownyWarsLanguage(TownyWars plugin) {
		this.plugin = plugin;
		saveDefaultLanguageFile(TownyWars.getInstance().getConfigInstance().language);
		loadLanguage();
	}
	
	public void saveDefaultLanguageFile(String language) {
		this.languageFolder = new File(this.plugin.getDataFolder().toString() + "/languages");
		if (!this.languageFolder.exists())
		      this.languageFolder.mkdir();
		if (languageFile == null) {
			languageFile = new File(this.languageFolder, language + ".yml");
	    }
	    if (!languageFile.exists()) {           
	    	plugin.saveResource("languages/" + language + ".yml", false);
	    }   
	}
	
	public void loadLanguage() {		
		this.language = YamlConfiguration.loadConfiguration(languageFile);
		this.pluginName = language.getString("pluginName");
		this.messagePrefix = language.getString("messagePrefix");
		this.mainCommandString = language.getString("commands.names.main");
		if(this.mainCommandString.contains(", "))
			this.commandNameMain = this.mainCommandString.split(", ");
		else
			this.commandNameMain[0] = this.mainCommandString;
		
		
	}
	
	public static boolean sendFormattedMessage(Player player, String message) {
		String string = message;
		if(TownyWars.getInstance().getPapiInstance()!=null)
			if(player!=null)
				string = TownyWars.getInstance().getPapiInstance().translatePlaceholders(player, string);
		if(TownyWars.getInstance().getMpapiInstance()!=null)
			if(player!=null)
					string = TownyWars.getInstance().getMpapiInstance().translatePlaceholders(player, string);
		if(isJson(string)) { //try these different methods out
			//return Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),"tellraw " + player.getName() + " " + string);
			//player.spigot().sendMessage(TextComponent.fromLegacyText(string));
			player.sendRawMessage(string);
			return true;
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', string));
		return true;
	}
	
	private static boolean isJson(String message) {
		try {
			new JSONObject(message);
			return true;
		}catch (JSONException e) {
			return false;
		}
	}

}