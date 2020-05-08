 package com.webkonsept.bukkit.simplechestlock.locks;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 import com.webkonsept.bukkit.simplechestlock.SCL;
 
 public class TrustHandler {
     
     private SCL plugin;
     private HashMap<String,HashSet<String>> trust = new HashMap<String,HashSet<String>>();
     
     public TrustHandler(SCL instance){
         plugin = instance;
         loadFromConfig();
     }
     
     public void updateConfig(){
         for (String truster : trust.keySet()){
             List<String> trustees = new ArrayList<String>();
             for (String trustee : trust.get(truster)){
                 trustees.add(trustee);
             }
             plugin.getConfig().set("trust."+truster,trustees);
         }
         plugin.saveConfig();
     }
     public void loadFromConfig(){
         plugin.babble("Loading trust relations from config");
         ConfigurationSection trustSection =  plugin.getConfig().getConfigurationSection("trust");
         if (trustSection != null){
             Set<String> people = trustSection.getKeys(false);
             plugin.babble(people.size()+" people have registered trust");
             for (String truster : people){
                 plugin.babble("Parsing truster "+truster);
                 List<String> trustedDudes = plugin.getConfig().getStringList("trust."+truster);
                 HashSet<String> trustees = new HashSet<String>();
                 for (String trustee : trustedDudes){
                     plugin.babble("   -> "+trustee);
                     trustees.add(trustee);
                 }
                 trust.put(truster,trustees);
             }
         }
     }
     
     public HashSet<String> getTrusteesCopy(String playerName){
         HashSet<String> original = trust.get(playerName.toLowerCase());
         HashSet<String> safeCopy = new HashSet<String>();
        
        if (original == null){
            return safeCopy; // Still empty, player trusts noone!
        }
        
         /*
          * Why not just .clone?  Because it's an "Unsafe cast".  It really isn't but whatever.
          * Doing it this way is O(n), doing .clone is likely O(n) as well.  Haven't checked.
          * What are the odds a trust list is long enough for anyone to notice?
          * Still:  TODO check for faster way to do this, out of principle.
          */
         for (String trusted : original){
             safeCopy.add(trusted);
         }
         return safeCopy;
     }
     public void addTrust (String truster,String trustee){
         if (trust.containsKey(truster.toLowerCase())){
             trust.get(truster).add(trustee.toLowerCase());
             plugin.babble(truster+" now also trusts "+trustee);
         }
         else {
             HashSet<String> trustage = new HashSet<String>();
             trustage.add(trustee.toLowerCase());
             trust.put(truster.toLowerCase(), trustage);
             plugin.babble(truster+" trusts "+trustee);
         }
     }
     public void removeTrust(String truster,String trustee){
         if (trust.containsKey(truster.toLowerCase())){
             trust.get(truster.toLowerCase()).remove(trustee.toLowerCase());
             plugin.babble(truster+" no longer trusts "+trustee);
         }
         else {
             plugin.babble(truster+" never trusted "+trustee);
         }
     }
     
     public String trustList(String playerName){
         HashSet<String> trusts = trust.get(playerName.toLowerCase());
         if (trusts == null){
             return null;
         }
         StringBuilder sb = new StringBuilder();
         sb.append(playerName);
         sb.append(" trust:");
         int trusted = 0;
         for (String trustee : trusts){
             trusted++;
             sb.append(" ");
             sb.append(trustee);
         }
         if (trusted == 0){
             sb.append(ChatColor.RED);
             sb.append(" Nobody!");
         }
         return sb.toString();
     }
 
     public void parseCommand(Player player, String[] args) {
         if (args[0].equalsIgnoreCase("trust")){
             HashSet<String> trusts;
             if (trust.containsKey(player.getName().toLowerCase())){
                 trusts = trust.get(player.getName().toLowerCase());
             }
             else {
                 trusts = new HashSet<String>();
                 trust.put(player.getName().toLowerCase(),trusts);
             }
             if (args.length > 1){
                 String action = args[1];
                 if (action.equalsIgnoreCase("list")){
                     StringBuilder sb = new StringBuilder();
                     sb.append(ChatColor.GREEN);
                     sb.append("You trust:");
                     int trusted = 0;
                     for (String trustee : trusts){
                         trusted++;
                         sb.append(" ");
                         sb.append(trustee);
                     }
                     if (trusted == 0){
                         sb.append(ChatColor.RED);
                         sb.append(" Nobody!");
                     }
                     player.sendMessage(sb.toString());
                 }
                 else if (action.equalsIgnoreCase("add")){
                     if (args.length == 3){
                         String addWho = args[2].toLowerCase();
                         trusts.add(addWho);
                         updateConfig();
                         player.sendMessage(ChatColor.GREEN+"In the future, chests you locked will allow "+addWho+" in.");
                     }
                     else {
                         player.sendMessage(ChatColor.YELLOW+"Add who?  What?  Huh?");
                     }
                 }
                 else if (action.equalsIgnoreCase("remove")){
                     if (args.length == 3){
                         String removeWho = args[2].toLowerCase();
                         trusts.remove(removeWho);
                         updateConfig();
                         player.sendMessage(ChatColor.GREEN+"In the future, chests you locked will not allow "+removeWho+" in.");
                     }
                     else {
                         player.sendMessage(ChatColor.YELLOW+"Add who?  What?  Huh?");
                     }
                 }
                 else if (action.equalsIgnoreCase("clear")){
                     trust.get(player.getName().toLowerCase()).clear();
                     updateConfig();
                     player.sendMessage(ChatColor.YELLOW+"You no longer trust anyone!");
                 }
                 else {
                     player.sendMessage(ChatColor.YELLOW+"Hmm?  /scl trust [list|add|remove|clear] <who>");
                 }
             }
             else {
                 player.sendMessage(ChatColor.YELLOW+"You have to specify an action.  list, clear, add or remove?");
             }
         }
         else {
             plugin.crap("Trust handler asked to handle "+args[0]);
         }
     }
 }
