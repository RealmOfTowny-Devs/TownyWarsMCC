package com.danielrharris.townywars.ideologies;

import com.danielrharris.townywars.TownyWars;
import com.palmergames.bukkit.towny.object.Town;

import org.bukkit.configuration.file.FileConfiguration;

public class IdeologiesManager {

    private static IdeologiesManager ideologiesManager = new IdeologiesManager();
    private IdeologiesFile ideologiesFile = TownyWars.getInstance().getIdeologiesFile();
    private FileConfiguration config = ideologiesFile.getConfig();

    public boolean hasIdeology(Town town){
        return config.contains(town.getName());
    }

    public void addIdeologyToTown(Ideology ideology, Town toTown){
        if(hasIdeology(toTown))return;
        config.set(toTown.getName(), ideology.getName());
        ideologiesFile.saveFile();
        ideologiesFile.reloadFile();
    }

    public Ideology getIdeology(Town town){
        if(hasIdeology(town)){
            String idName = (String) config.get(town.getName());
            return getIdeologyFromName (idName);
        }
		return null;
    }

    public static IdeologiesManager getIdeologiesManager() {
        return ideologiesManager;
    }
    
    public Ideology getIdeologyFromName (String string)
    {
    	String name = string.toLowerCase();
    	if (name.contains("eco"))
    	{
    		return Ideology.ECONOMIC;
    	}
    	if (name.contains("mili"))
    	{
    		return Ideology.MILITARISTIC;
    	}
    	if (name.contains("reli"))
    	{
    		return Ideology.RELIGIOUS;
    	}
    	return null;
    }
}