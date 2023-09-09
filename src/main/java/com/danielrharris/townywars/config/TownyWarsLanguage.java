package com.danielrharris.townywars.config;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
	
	public String formatMessage(Player player, String message) {
		String string = "";
		string = TownyWars.getInstance().getPapiInstance().translatePlaceholders(player, message);
		string = ""; // <--- mdvwPlaceholderAPI translate here
		string = ChatColor.translateAlternateColorCodes('&', message);
		return string;
	}
}