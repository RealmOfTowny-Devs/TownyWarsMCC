package com.danielrharris.townywars;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.util.FileMgmt;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarManager
{

  private static String fileSeparator = System.getProperty("file.separator");
  private static Set<War> activeWars = new HashSet<War>();
  private static Set<String> requestedPeace = new HashSet<String>();
  public static Map<String, Double> neutral = new HashMap<String, Double>();
  public static Town townremove;
  //private static final int SAVING_VERSION = 1;


    // utility methods
    /**
     * Pass a file and it will return it's contents as a string.
     *
     * @param file File to read.
     * @return Contents of file. String will be empty in case of any errors.
     */
    public static String convertFileToString(File file) {

        if (file != null && file.exists() && file.canRead() && !file.isDirectory()) {
            Writer writer = new StringWriter();
            InputStream is = null;

            char[] buffer = new char[1024];
            try {
                is = new FileInputStream(file);
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Exception ");
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ignore) {
                    }
                }
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    //writes a string to a file making all newline codes platform specific
    public static boolean stringToFile(String source, String FileName) {

        if (source != null) {
            // Save the string to file (*.yml)
            try {
                return stringToFile(source, new File(FileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;

    }

    /**
     * Writes the contents of a string to a file.
     *
     * @param source String to write.
     * @param file File to write to.
     * @return True on success.
     * @throws IOException
     */
    public static boolean stringToFile(String source, File file) throws IOException {

        try {

            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

            //BufferedWriter out = new BufferedWriter(new FileWriter(FileName));

            source = source.replaceAll("\n", System.getProperty("line.separator"));

            out.write(source);
            out.close();
            return true;

        } catch (IOException e) {
            System.out.println("Exception ");
            return false;
        }
    }

    // War manager

  public static void save()
    throws Exception
  {
	  FileMgmt.checkOrCreateFile("plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml");
	    if(!WarManager.getWars().isEmpty()){
		    String s = new String("");
		    
		    for(War w : WarManager.getWars())
		    	s += w.objectToString() + "\n";
		    
		    s = s.substring(0, s.length()-1);
		    
		    stringToFile(s, "plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml");
		 } else
	    	stringToFile("", "plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml");
    
    //save Rebellions
    //tripple space to separate rebellion objects
    FileMgmt.checkOrCreateFile("plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml");
    if(!Rebellion.getAllRebellions().isEmpty()){
	    String s = new String("");
	    
	    for(Rebellion r : Rebellion.getAllRebellions())
	    	s += r.objectToString() + "\n";
	    
	    s = s.substring(0, s.length()-1);
	    
	    stringToFile(s, "plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml");
	 } else
    	stringToFile("", "plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml");
  }
  
  public static void load(File dataFolder)
    throws Exception
  {
	  	String folders[] = {"plugins" + fileSeparator + "TownyWars"};
	  	FileMgmt.checkOrCreateFolders(folders);
	  	
	  	 //load rebellions
	    FileMgmt.checkOrCreateFile("plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml");
	    String s = convertFileToString(new File("plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml"));
	    
	    if(!s.isEmpty()){
		    ArrayList<String> slist = new ArrayList<String>();
		    
		    for(String temp : s.split("\n"))
		    	slist.add(temp);
		    
		    for(String s2 : slist)
		    	Rebellion.getAllRebellions().add(new Rebellion(s2));
	    }
	    
	    //load wars
	  	FileMgmt.checkOrCreateFile("plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml");
	    String sw = convertFileToString(new File("plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml"));
	    
	    if(!sw.isEmpty()){
		    ArrayList<String> slist = new ArrayList<String>();

		    for(String temp : sw.split("\n"))
		    	slist.add(temp);
		    
		    for(String s2 : slist)
		    	WarManager.getWars().add(new War(s2));
	    }
  }
  
  public static Set<War> getWars()
  {
    return activeWars;
  }
  
  public static War getWarForNation(Nation onation)
  {
    for (War w : activeWars) {
      if (w.hasNation(onation)) {
        return w;
      }
    }
    return null;
  }
  
  public static void createWar(Nation nat, Nation onat, CommandSender cs){
	  createWar(nat, onat, cs, null);
  }
  
  @SuppressWarnings("deprecation")
public static void createWar(Nation nat, Nation onat, CommandSender cs, Rebellion r)
  { 
    if ((getWarForNation(nat) != null) || (getWarForNation(onat) != null))
    {
      cs.sendMessage(ChatColor.RED + "Your nation is already at war with another nation!");
    }
    else
    {
      try
      {
        try
        {
          TownyUniverse.getInstance().getDataSource().getNation(nat.getName()).addEnemy(onat);
          TownyUniverse.getInstance().getDataSource().getNation(onat.getName()).addEnemy(nat);
        }
        catch (AlreadyRegisteredException ex)
        {
          Logger.getLogger(WarManager.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      catch (NotRegisteredException ex)
      {
        Logger.getLogger(WarManager.class.getName()).log(Level.SEVERE, null, ex);
      }
      War war = new War(nat, onat, r);
      activeWars.add(war);
      for (Resident re : nat.getResidents())
      {
        Player plr = Bukkit.getPlayer(re.getName());
        if (plr != null) {
          plr.sendMessage(ChatColor.RED + "Your nation is now at war with " + onat.getName() + "!");
        }
      }
      for (Resident re : onat.getResidents())
      {
        Player plr = Bukkit.getPlayer(re.getName());
        if (plr != null) {
          plr.sendMessage(ChatColor.RED + "Your nation is now at war with " + nat.getName() + "!");
        }
      }
      for (Town t : nat.getTowns()) {
        t.setPVP(true);
      }
      for (Town t : onat.getTowns()) {
        t.setPVP(true);
      }
    }
    
    TownyUniverse.getInstance().getDataSource().saveTowns();
    TownyUniverse.getInstance().getDataSource().saveNations();
    try {
		WarManager.save();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  @SuppressWarnings("deprecation")
public static boolean requestPeace(Nation nat, Nation onat, boolean admin)
  {
	  
    if ((admin) || (requestedPeace.contains(onat.getName())))
    {
      if(getWarForNation(nat).getRebellion() != null)
    	  getWarForNation(nat).getRebellion().peace();
      endWar(nat, onat, true);
      
      try
      {
        nat.collect(TownyWars.endCost);
        onat.collect(TownyWars.endCost);
      }
      catch (Exception ex)
      {
        Logger.getLogger(WarManager.class.getName()).log(Level.SEVERE, null, ex);
      }
      return true;
    }
    if (admin)
    {
      endWar(nat, onat, true);
      return true;
    }
    requestedPeace.add(nat.getName());
    for (Resident re : onat.getResidents()) {
      if ((re.isKing()) || (onat.hasAssistant(re)))
      {
        Player plr = Bukkit.getPlayer(re.getName());
        if (plr != null) {
          plr.sendMessage(ChatColor.GREEN + nat.getName() + " has requested peace!");
        }
      }
    }
    return false;
  }
  
  public static void endWar(Nation winner, Nation looser, boolean peace)
  {
	boolean isRebelWar = WarManager.getWarForNation(winner).getRebellion() != null;
	Rebellion rebellion = WarManager.getWarForNation(winner).getRebellion();
	
	try
	{
	   TownyUniverse.getInstance().getDataSource().getNation(winner.getName()).removeEnemy(looser);
	   TownyUniverse.getInstance().getDataSource().getNation(looser.getName()).removeEnemy(winner);
	    }
	    catch (NotRegisteredException ex)
	    {
	      Logger.getLogger(WarManager.class.getName()).log(Level.SEVERE, null, ex);
	    }
    
    activeWars.remove(getWarForNation(winner));
    requestedPeace.remove(looser.getName());
    War.broadcast(winner, ChatColor.GREEN + "You are now at peace!");
    War.broadcast(looser, ChatColor.GREEN + "You are now at peace!");
    for (Town t : winner.getTowns()) {
      t.setPVP(false);
    }
    
    //rebels win
    if(!peace && isRebelWar && winner == rebellion.getRebelnation()){
		War.broadcast(looser, ChatColor.RED + winner.getName() + " won the rebellion and are now free!");
		War.broadcast(winner, ChatColor.GREEN + winner.getName() + " won the rebellion and are now free!");
    	rebellion.success();
    	Rebellion.getAllRebellions().remove(rebellion);
    	TownyUniverse.getInstance().getDataSource().removeNation(winner);
        winner.clear();
        TownyWars.tUniverse.getNations().remove(winner.getName());
    }
    
    //rebelwar white peace
    if(isRebelWar && peace){
    	if(winner != rebellion.getMotherNation()){
	    	TownyUniverse.getInstance().getDataSource().removeNation(winner);
		    TownyWars.tUniverse.getNations().remove(winner.getName());
    	} else{
    		TownyUniverse.getInstance().getDataSource().removeNation(looser);
		    TownyWars.tUniverse.getNations().remove(winner.getName());
    	}
    }
    
    //TODO risk of concurrentmodificationexception please fix or something
    for (Town t : looser.getTowns())
    {
      if (!peace && !isRebelWar) {
        try
        {
          WarManager.townremove = t;
          looser.getTowns().remove(t);
          winner.addTown(t);
        }
        catch (Exception ex)
        {
          Logger.getLogger(WarManager.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      t.setPVP(false);
    }
    if (!peace && isRebelWar && winner != rebellion.getRebelnation())
    {
      TownyUniverse.getInstance().getDataSource().removeNation(looser);
      looser.clear();
      TownyWars.tUniverse.getNations().remove(winner.getName());
    }
    Rebellion.getAllRebellions().remove(rebellion);
    
    if(looser.getTowns().size() == 0)
    	TownyUniverse.getInstance().getDataSource().removeNation(looser);
    if(winner.getTowns().size() == 0)
    	TownyUniverse.getInstance().getDataSource().removeNation(winner);
    
    TownyUniverse.getInstance().getDataSource().saveTowns();
    TownyUniverse.getInstance().getDataSource().saveNations();
    try {
		WarManager.save();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  public static boolean hasBeenOffered(War ww, Nation nation)
  {
    try {
		return requestedPeace.contains(ww.getEnemy(nation));
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    return false;
  }
}