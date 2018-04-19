package com.danielrharris.townywars.trades;

import com.sun.org.apache.bcel.internal.generic.GETFIELD;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class TradeFile {

    private File file;
    private YamlConfiguration yamlConfiguration;

    public TradeFile(Plugin plugin){
        if(!plugin.getDataFolder().exists()){
            plugin.getDataFolder().mkdir();
        }
        this.file = new File(plugin.getDataFolder(),"trades.yml");
        if(!this.file.exists()){
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.yamlConfiguration = YamlConfiguration.loadConfiguration(this.file);
    }

    public void reload(){
        try {
            this.yamlConfiguration.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    public void save(){
        try {
            this.yamlConfiguration.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public FileConfiguration getYamlConfiguration() {
        return yamlConfiguration;
    }
}
