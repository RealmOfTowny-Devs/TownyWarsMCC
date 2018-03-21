package com.danielrharris.townywars.ideologies;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class IdeologiesFile {

    private File file;
    private YamlConfiguration yamlConfiguration;

    public IdeologiesFile(Plugin plugin){
        if(!plugin.getDataFolder().exists())plugin.getDataFolder().mkdir();
        this.file=  new File(plugin.getDataFolder(),"ideologies.yml");
        if(!this.file.exists()){
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.yamlConfiguration = YamlConfiguration.loadConfiguration(this.file);
    }

    public FileConfiguration getConfig(){
        return yamlConfiguration;
    }

    public void reloadFile(){
        try {
            this.yamlConfiguration.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void saveFile(){
        try {
            this.yamlConfiguration.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
