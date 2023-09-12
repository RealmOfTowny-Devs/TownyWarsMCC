package com.danielrharris.townywars.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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
	public String noPermissionMessage;
	public String notInWarErrorMessage;
	
	//commands
	public Command mainCommand;
	public Command reload;
	public String successfulReloadMessage;
	public Command help;
	public List<String> rebellionMessages;
	public List<String> adminMessages;
	public String adminHelpPerm;
	public List<String> leaderMessages;
	public Command status;
	
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
		this.noPermissionMessage = language.getString("noPermissionMessage");
		this.notInWarErrorMessage = language.getString("notInWarErrorMessage");
		loadCommands();		
	}
	
	private void loadCommands() {
		this.mainCommand = new Command(language.getString("commands.main.name"), language.getStringList("commands.main.message"), language.getString("commands.main.permission"));
		this.reload = new Command(language.getString("commands.reload.name"), language.getStringList("commands.reload.message"), language.getString("commands.reload.permission"));
		this.successfulReloadMessage = language.getString("commands.reload.successfulReloadMessage");
		this.help = new Command(language.getString("commands.help.name"), language.getStringList("commands.help.message"), language.getString("commands.help.permission"));
		this.rebellionMessages = language.getStringList("commands.help.rebellionMessages");
		this.adminMessages = language.getStringList("commands.help.adminMessages.message");
		this.adminHelpPerm = language.getString("commands.help.adminMessages.permission");
		this.leaderMessages = language.getStringList("commands.help.leaderMessages");
		this.status = new Command(language.getString("commands.status.name"), language.getStringList("commands.status.message"), language.getString("commands.status.permission"));
	}
	
	public static void sendFormattedMessage(Player player, List<String> message) {
		TownyWarsLanguage.sendFormattedMessage(player, message);
	}
	
	public static void sendFormattedMessage(Player player, String message) {
		List<String> messages = new ArrayList<String>();
		messages.add(message);
		TownyWarsLanguage.sendFormattedMessage(player, messages);
	}
	
	public static void sendFormattedMessage(CommandSender sender, String message) {
		List<String> messages = new ArrayList<String>();
		messages.add(message);
		TownyWarsLanguage.sendFormattedMessage(sender, messages);
	}
	
	public static void sendFormattedMessage(CommandSender sender, List<String> message) {
		if(message==null)
			return;
		if(message.isEmpty())
			return;
		Player player = null;
		OfflinePlayer offlinePlayer = null;
		ConsoleCommandSender consoleSender = null;
		if(sender instanceof Player) {
			player = (Player) sender;
		}else if(sender instanceof OfflinePlayer) {
			offlinePlayer = (OfflinePlayer) sender;
		}else {
			consoleSender = Bukkit.getServer().getConsoleSender();
		}
		for(String string : message) {
			if(string == message.get(0)) {
	            if(string.charAt(0) != '!')
	            	string = TownyWars.getInstance().getLanguage().messagePrefix + " " + message.get(0);	
				else
					string = string.substring(1); //Trim off the !
			}				
			if(TownyWars.getInstance().getPapiInstance()!=null)
				if(player!=null)
					string = TownyWars.getInstance().getPapiInstance().translatePlaceholders(player, string);
			if(TownyWars.getInstance().getMpapiInstance()!=null) {
				if(player!=null)
					string = TownyWars.getInstance().getMpapiInstance().translatePlaceholders(player, string);
				if(offlinePlayer!=null) {
					string = TownyWars.getInstance().getMpapiInstance().translatePlaceholders(offlinePlayer, string);
				}
			}
				
			if(isJson(string)) { //try these different methods out
				//return Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),"tellraw " + player.getName() + " " + string);
				//player.spigot().sendMessage(TextComponent.fromLegacyText(string));
				if(player!=null) {
					player.sendRawMessage(string);
				} else if(consoleSender!=null) {
					consoleSender.sendRawMessage(string);
				}
			}else {
				if(player!=null) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', string));
				} else if(consoleSender!=null) {
					consoleSender.sendMessage(ChatColor.translateAlternateColorCodes('&', string));
				}
			}
		}		
	}
	
	private static boolean isJson(String message) {
		try {
			new JSONObject(message);
			return true;
		}catch (JSONException e) {
			return false;
		}
	}
	
	public class Command {
		
		private List<String> names;
		private List<String> message;
		private String permission;
		
		public Command(String names, List<String> message, String permission) {
			this.names = splitNames(names);
			this.message = message;
			this.permission = permission;
		}
		
		private List<String> splitNames(String string) {
			List<String> split = new ArrayList<String>();
			if(string.contains(",")) {
				for(String s : string.split(",")) {
					split.add(s.replaceAll("\\s", ""));
				}
			}else {
				split.add(string);
			}
			return split;	
		}
		
		public List<String> getNames(){
			return this.names;
		}
		
		public List<String> getMessage(){
			return this.message;
		}
		
		public String getPermission(){
			return this.permission;
		}
		
	}

}