package com.danielrharris.townywars;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.util.FileMgmt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WarManager {

    public static Map<String, Double> neutral = new HashMap<String, Double>();
    public static Town townremove;
    private static String fileSeparator = System.getProperty("file.separator");
    private static Set<War> activeWars = new HashSet<War>();
    private static Set<String> requestedPeace = new HashSet<String>();
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
     * @param file   File to write to.
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
            throws Exception {
        FileMgmt.checkOrCreateFile("plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml");
        if (!WarManager.getWars().isEmpty()) {
            String s = new String("");

            for (War w : WarManager.getWars())
                s += w.objectToString() + "\n";

            s = s.substring(0, s.length() - 1);

            stringToFile(s, "plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml");
        } else
            stringToFile("", "plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml");

        //save Rebellions
        //tripple space to separate rebellion objects
        FileMgmt.checkOrCreateFile("plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml");
        if (!Rebellion.getAllRebellions().isEmpty()) {
            String s = new String("");

            for (Rebellion r : Rebellion.getAllRebellions())
                s += r.objectToString() + "\n";

            s = s.substring(0, s.length() - 1);

            stringToFile(s, "plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml");
        } else
            stringToFile("", "plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml");
    }

    public static void load(File dataFolder)
            throws Exception {
        String folders[] = {"plugins" + fileSeparator + "TownyWars"};
        FileMgmt.checkOrCreateFolders(folders);

        //load rebellions
        FileMgmt.checkOrCreateFile("plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml");
        String s = convertFileToString(new File("plugins" + fileSeparator + "TownyWars" + fileSeparator + "rebellions.yml"));

        if (!s.isEmpty()) {
            ArrayList<String> slist = new ArrayList<String>();

            for (String temp : s.split("\n"))
                slist.add(temp);

            for (String s2 : slist)
                Rebellion.getAllRebellions().add(new Rebellion(s2));
        }

        //load wars
        FileMgmt.checkOrCreateFile("plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml");
        String sw = convertFileToString(new File("plugins" + fileSeparator + "TownyWars" + fileSeparator + "activeWars.yml"));

        if (!sw.isEmpty()) {
            ArrayList<String> slist = new ArrayList<String>();

            for (String temp : sw.split("\n"))
                slist.add(temp);

            for (String s2 : slist)
                WarManager.getWars().add(new War(s2));
        }
    }

    public static Set<War> getWars() {
        return activeWars;
    }

    public static War getWarForNation(Nation onation) {
        for (War w : activeWars) {
            if (w.hasNation(onation)) {
                return w;
            }
        }
        return null;
    }

    public static void createWar(Nation nat, Nation onat, CommandSender cs) {
        createWar(nat, onat, cs, null);
    }

    public static void enableExplosions(Nation target) {
        for(Town town : target.getTowns()) {
            System.out.println("enabled explosions");
            town.setExplosion(true);
            town.setFire(true);
        }
    }

    @SuppressWarnings("deprecation")
    public static void createWar(Object declarer, Object declaree, CommandSender cs, Rebellion r) {
        Nation declaringNation = null;
        Town declaringTown = null;
        Nation targetNation = null;
        Town targetTown = null;

        // Determine type of declarer and declaree
        if (declarer instanceof Nation) {
            declaringNation = (Nation) declarer;
        } else if (declarer instanceof Town) {
            declaringTown = (Town) declarer;
        }

        if (declaree instanceof Nation) {
            targetNation = (Nation) declaree;
        } else if (declaree instanceof Town) {
            targetTown = (Town) declaree;
        }

        // Enable PvP for the involved parties
        if (declaringTown != null) {
            declaringTown.setPVP(true);
        }
        if (targetTown != null) {
            targetTown.setPVP(true);
        }
        if (declaringNation != null) {
            for (Town town : declaringNation.getTowns()) {
                town.setPVP(true);
            }
        }
        if (targetNation != null) {
            for (Town town : targetNation.getTowns()) {
                town.setPVP(true);
            }
        }

        // Logic for Nation declaring war on Town
        if (declaringNation != null && targetTown != null) {
            cs.sendMessage(ChatColor.GREEN + "Nation " + declaringNation.getName() + " has declared war on Town " + targetTown.getName() + "!");
            // Notify players in the declaring nation
            for (Resident resident : declaringNation.getResidents()) {
                Player player = Bukkit.getPlayer(resident.getName());
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "Your nation has declared war on the town " + targetTown.getName() + "!");
                }
            }
            // Notify players in the target town
            for (Resident resident : targetTown.getResidents()) {
                Player player = Bukkit.getPlayer(resident.getName());
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "The nation " + declaringNation.getName() + " has declared war on your town!");
                }
            }
            // TODO: You'd typically also do other things here, like setting up war timers, updating data structures, etc.

        }
        // Logic for Town declaring war on Nation
        else if (declaringTown != null && targetNation != null) {
            cs.sendMessage(ChatColor.GREEN + "Town " + declaringTown.getName() + " has declared war on Nation " + targetNation.getName() + "!");
            // Notify players in the declaring town
            for (Resident resident : declaringTown.getResidents()) {
                Player player = Bukkit.getPlayer(resident.getName());
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "Your town has declared war on the nation " + targetNation.getName() + "!");
                }
            }
            // Notify players in the target nation
            for (Resident resident : targetNation.getResidents()) {
                Player player = Bukkit.getPlayer(resident.getName());
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "The town " + declaringTown.getName() + " has declared war on your nation!");
                }
            }
            // TODO: Similar war setup logic as above.

        }
        // Traditional Nation vs. Nation war
        else if (declaringNation != null && targetNation != null) {
            cs.sendMessage(ChatColor.GREEN + "Nation " + declaringNation.getName() + " has declared war on Nation " + targetNation.getName() + "!");
            // Notify players in the declaring nation
            for (Resident resident : declaringNation.getResidents()) {
                Player player = Bukkit.getPlayer(resident.getName());
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "Your nation has declared war on the nation " + targetNation.getName() + "!");
                }
            }
            // Notify players in the target nation
            for (Resident resident : targetNation.getResidents()) {
                Player player = Bukkit.getPlayer(resident.getName());
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "The nation " + declaringNation.getName() + " has declared war on your nation!");
                }
            }
            // TODO: The original logic for setting up the war, timers, data structures, etc.
        }
    }


    @SuppressWarnings("deprecation")
    public static boolean requestPeace(Nation nat, Nation onat, boolean admin) {

        if ((admin) || (requestedPeace.contains(onat.getName()))) {
            if (getWarForNation(nat).getRebellion() != null)
                getWarForNation(nat).getRebellion().peace();
            endWar(nat, onat, true);

            try {
                nat.collect(TownyWars.endCost);
                onat.collect(TownyWars.endCost);
            } catch (Exception ex) {
                Logger.getLogger(WarManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }
        if (admin) {
            endWar(nat, onat, true);
            return true;
        }
        requestedPeace.add(nat.getName());
        for (Resident re : onat.getResidents()) {
            if ((re.isKing()) || (onat.hasAssistant(re))) {
                Player plr = Bukkit.getPlayer(re.getName());
                if (plr != null) {
                    plr.sendMessage(ChatColor.GREEN + nat.getName() + " has requested peace!");
                }
            }
        }
        return false;
    }

    public static void endWar(Nation winner, Nation looser, boolean peace) {
        boolean isRebelWar = WarManager.getWarForNation(winner).getRebellion() != null;
        Rebellion rebellion = WarManager.getWarForNation(winner).getRebellion();

        try {
            TownyUniverse.getInstance().getNation(winner.getName()).removeEnemy(looser);
            TownyUniverse.getInstance().getNation(looser.getName()).removeEnemy(winner);
        } catch (NullPointerException ex) {
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
        if (!peace && isRebelWar && winner == rebellion.getRebelnation()) {
            War.broadcast(looser, ChatColor.RED + winner.getName() + " won the rebellion and are now free!");
            War.broadcast(winner, ChatColor.GREEN + winner.getName() + " won the rebellion and are now free!");
            rebellion.success();
            Rebellion.getAllRebellions().remove(rebellion);
            TownyUniverse.getInstance().getDataSource().removeNation(winner);
            winner.clear();
            TownyWars.tUniverse.getNations().remove(winner.getName());
        }

        //rebelwar white peace
        if (isRebelWar && peace) {
            if (winner != rebellion.getMotherNation()) {
                TownyUniverse.getInstance().getDataSource().removeNation(winner);
                TownyWars.tUniverse.getNations().remove(winner.getName());
            } else {
                TownyUniverse.getInstance().getDataSource().removeNation(looser);
                TownyWars.tUniverse.getNations().remove(winner.getName());
            }
        }

        //TODO risk of concurrentmodificationexception please fix or something
        for (Town t : looser.getTowns()) {
            if (!peace && !isRebelWar) {
                try {
                    WarManager.townremove = t;
                    looser.getTowns().remove(t);
                    winner.addTown(t);
                } catch (Exception ex) {
                    Logger.getLogger(WarManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            t.setPVP(false);
        }
        if (!peace && isRebelWar && winner != rebellion.getRebelnation()) {
            TownyUniverse.getInstance().getDataSource().removeNation(looser);
            looser.clear();
            TownyWars.tUniverse.getNations().remove(winner.getName());
        }
        Rebellion.getAllRebellions().remove(rebellion);

        if (looser.getTowns().size() == 0)
            TownyUniverse.getInstance().getDataSource().removeNation(looser);
        if (winner.getTowns().size() == 0)
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

    public static boolean hasBeenOffered(War ww, Nation nation) {
        try {
            return requestedPeace.contains(ww.getEnemy(nation));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }
}