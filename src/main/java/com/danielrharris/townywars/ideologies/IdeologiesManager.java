package com.danielrharris.townywars.ideologies;

import com.danielrharris.townywars.TownyWars;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.configuration.file.FileConfiguration;

public class IdeologiesManager {

    private static IdeologiesManager ideologiesManager = new IdeologiesManager();
    private IdeologiesFile ideologiesFile = TownyWars.getInstance().getIdeologiesFile();
    private FileConfiguration config = ideologiesFile.getConfig();

    public boolean hasIdeology(Town town){
        return config.contains("Ideologies." + town.getUID() + "." + town.getName() + ".ideology");
    }

    public void addIdeologyToTown(Ideology ideology, Town toTown){
        if(hasIdeology(toTown))return;
        config.set("Ideologies." + toTown.getUID() + "." + toTown.getName()+".ideology",ideology);
        ideologiesFile.saveFile();
        ideologiesFile.reloadFile();
    }

    public void getIdeology(Town town){
        if(hasIdeology(town)){
            config.get("Ideologies." + town.getUID() + "." + town.getName() + ".ideology");
        }
    }

    public static IdeologiesManager getIdeologiesManager() {
        return ideologiesManager;
    }
}
