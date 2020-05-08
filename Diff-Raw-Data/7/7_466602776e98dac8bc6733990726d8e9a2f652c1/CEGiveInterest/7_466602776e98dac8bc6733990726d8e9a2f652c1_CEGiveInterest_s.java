 package me.ccattell.plugins.completeeconomy.utilities;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import me.ccattell.plugins.completeeconomy.CompleteEconomy;
 import me.ccattell.plugins.completeeconomy.database.CEQueryFactory;
 import org.bukkit.entity.Player;
 
 public class CEGiveInterest {
 
     boolean enabled = CompleteEconomy.plugin.configs.getBankConfig().getBoolean("Banking.Interest.Enabled");
     boolean online = CompleteEconomy.plugin.configs.getBankConfig().getBoolean("Banking.Interest.Online");
     boolean announce = CompleteEconomy.plugin.configs.getBankConfig().getBoolean("Banking.Interest.Announce");
     long interval = CompleteEconomy.plugin.configs.getBankConfig().getInt("Banking.Interest.Interval") * 20;
     // convert to float
     float cutoff = CompleteEconomy.plugin.configs.getBankConfig().getInt("Banking.Interest.Cutoff") * 1.0F;
    // convert int to float - old default was 0.1 so divide by 10
    float percent = (CompleteEconomy.plugin.configs.getBankConfig().getInt("Banking.Interest.Amount") / 10F);
     CEQueryFactory qf = new CEQueryFactory();
 
     // add class constructor
     public void CEGiveInterest() {
     }
 
     public void interest() {
 
         if (enabled) {
             CompleteEconomy.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(CompleteEconomy.plugin, new Runnable() {
                 @Override
                 public void run() {
                     CompleteEconomy.plugin.getServer().getConsoleSender().sendMessage("Give Interest");
                     // do stuff
                     doInterest();
                 }
             }, interval, interval);
         }
     }
 
     private void doInterest() {
 
         List<Player> online_players = Arrays.asList(CompleteEconomy.plugin.getServer().getOnlinePlayers());
         HashMap<Player, Float> db_players = qf.getPlayers();
         for (Map.Entry<Player, Float> entry : db_players.entrySet()) {
             if ((online && online_players.contains(entry.getKey())) || !online) {
                 // calculate interest
                 if (entry.getValue() >= cutoff) {
                    float credit = toCents((entry.getValue() / 100F) * percent);
                     if (credit > 0) { // don't give interest unless there is some
                         qf.alterBalance("bank", entry.getKey().getName(), credit);
                         if (announce) {
                             String s = new CEMajorMinor().getFormat(credit);
                             entry.getKey().sendMessage("You recieved " + s + " in interest on your savings!");
                         }
                     }
                 }
             }
         }
     }
 
     public static float toCents(float value) {
         value = value * 100;
         long tmp = Math.round(value);
         return (float) tmp / 100;
     }
 }
