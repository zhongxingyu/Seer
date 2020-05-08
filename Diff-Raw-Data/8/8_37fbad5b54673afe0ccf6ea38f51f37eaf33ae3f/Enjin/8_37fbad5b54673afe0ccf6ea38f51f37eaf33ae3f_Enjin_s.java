 package com.md_5.enjin.donate;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import lombok.Data;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Enjin extends JavaPlugin {
 
     private int interval;
     private String url;
     private String command;
     private String message;
 
     @Override
     public void onEnable() {
         FileConfiguration conf = getConfig();
         conf.options().copyDefaults(true);
         saveConfig();
         interval = conf.getInt("interval") * 1200;
         url = conf.getString("url");
         command = conf.getString("command");
         message = conf.getString("message");
         if (url != null) {
             getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Checker(), 0, interval);
         } else {
             getLogger().severe("Please set your shop url in config.yml");
         }
     }
 
     @Override
     public void onDisable() {
         getServer().getScheduler().cancelTasks(this);
     }
 
     private List<DonationData> getJSON(String url) {
         try {
            InputStreamReader reader = new InputStreamReader(new URL(url).openStream());
             JsonArray array = new JsonParser().parse(reader).getAsJsonArray();
             Gson gson = new Gson();
             List<DonationData> donations = new ArrayList<DonationData>();
             for (JsonElement el : array) {
                 donations.add(gson.fromJson(el, DonationData.class));
             }
             reader.close();
             return donations;
         } catch (Exception ex) {
             ex.printStackTrace();
             return null;
         }
     }
 
     public boolean isClaimed(String date) {
         return getConfig().getStringList("claims").contains(date);
     }
 
     public void claim(DonationData donation) {
         String user = donation.getCustom_field();
         String rank = donation.getItem_name();
         if (user != null) {
             getServer().dispatchCommand(getServer().getConsoleSender(), String.format(command, user, rank));
             List<String> claims = getConfig().getStringList("claims");
             claims.add(donation.getPurchase_date());
             if (claims.size() > 100) {
                 claims.remove(0);
             }
             getConfig().set("claims", claims);
             saveConfig();
             if (message != null) {
                 getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + String.format(message, user, rank));
             }
         }
     }
 
     private class Checker implements Runnable {
 
         public void run() {
             getLogger().info("Checking for new donations");
             for (DonationData donation : getJSON(url)) {
                 if (!isClaimed(donation.getPurchase_date())) {
                     claim(donation);
                 }
             }
         }
     }
 
     @Data
     public class DonationData {
 
         private UserDetails user;
         private String item_name;
         private String item_price;
         private String purchase_date;
         private String currency;
         private String item_id;
         private String custom_field;
 
         @Data
         public class UserDetails {
 
             private String user_id;
             private String user_name;
         }
     }
 }
