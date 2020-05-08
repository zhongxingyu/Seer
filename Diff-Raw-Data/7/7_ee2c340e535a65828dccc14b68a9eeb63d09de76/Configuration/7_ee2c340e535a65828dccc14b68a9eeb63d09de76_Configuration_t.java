 package at.junction.transmission;
 
 import org.bukkit.ChatColor;
 
 import java.util.ArrayList;
 import java.util.List;
 class CircularQueue<T> {
     private ArrayList<T> objects = new ArrayList<>();
     private int pointer;
 
     public T get(){
         if (objects.size() == 0){
             throw new ArrayIndexOutOfBoundsException();
         }
         T returnObject = objects.get(pointer);
         pointer++;
        if (pointer >= objects.size()-1)
             pointer=0;

         return returnObject;
     }
 
     public void add(T insert){
         if (objects.size() == 0){
             pointer = 0;
         }
         objects.add(insert);
     }
 
     public int size(){
         return objects.size();
     }
 }
 
 public class Configuration {
     private Transmission plugin;
     boolean RATE_LIMIT;
     int MESSAGES;
     int TIME;
     List<String> MUTED_PLAYERS;
     String MUTE_MESSAGE;
     String SPAM_MESSAGE;
     boolean MOTD;
     ChatColor MOTD_COLOR;
     String MOTD_MESSAGE;
     CircularQueue<String> ALERTS;
     long ALERT_PERIOD;
     long ALERT_DELAY;
     ChatColor ALERT_COLOR;
     String ALERT_PREFIX;
 
     public Configuration(Transmission instance) {
         plugin = instance;
     }
 
     public void load() {
         plugin.reloadConfig();
         RATE_LIMIT = plugin.getConfig().getBoolean("rate-limit", false);
         MESSAGES = plugin.getConfig().getInt("rate-limit-messages", 100);
         TIME = plugin.getConfig().getInt("rate-limit-time", 1);
         MUTED_PLAYERS = plugin.getConfig().getStringList("muted-players");
         MUTE_MESSAGE = plugin.getConfig().getString("mute-message");
         SPAM_MESSAGE = plugin.getConfig().getString("spam-message");
         MOTD = plugin.getConfig().getBoolean("motd-enabled", false);
         MOTD_COLOR = ChatColor.valueOf(plugin.getConfig().getString("motd-color"));
         MOTD_MESSAGE = plugin.getConfig().getString("motd-message");
 
         ALERT_PERIOD = plugin.getConfig().getLong("alert-period", 300);
         ALERT_DELAY = plugin.getConfig().getLong("alert-delay", 60);
         ALERT_COLOR = ChatColor.valueOf(plugin.getConfig().getString("alert-color", "LIGHT_PURPLE"));
         ALERT_PREFIX = plugin.getConfig().getString("alert-prefix", "SERVER");
         ALERTS = new CircularQueue<>();
         for (String t : plugin.getConfig().getStringList("alerts")){
             ALERTS.add(t);
         }
 
     }
     public void save(){
         plugin.getConfig().set("muted-players", MUTED_PLAYERS);
     }
 }
