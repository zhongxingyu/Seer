 package me.olloth.lordned.seasons;
 
 import java.io.File;
 import java.util.logging.Logger;
import me.olloth.lordned.seasons.util.Config;
import me.olloth.lordned.seasons.util.Enums.Day;
import me.olloth.lordned.seasons.util.Enums.Season;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Seasons extends JavaPlugin {
     //Setup Bukkit's Logger
     Logger log = Logger.getLogger("Minecraft");
     String logPrefix = "[Seasons] ";
     
     private PluginDescriptionFile info;
     private File directory, config;
     
     
     public void onDisable()
     {
         System.out.println("[" + info.getName() + "] is now disabled!");
     }
 
     public void onEnable()
     {        
         directory = getDataFolder();
         info = getDescription();
         
         //Load the Config
         Config.configSetup(directory, config);
         
         System.out.println("[" + info.getName() + "] version " + 
                 info.getVersion() + " is now enabled!");
         
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
             public void run()
             {
                 log.info(logPrefix + "Current Year: " + getYear());
                 log.info(logPrefix + "Current Season: " + getSeason().toString() );
                 log.info(logPrefix + "Current Day: " + getDay().toString() );
             }
         }, 1, 20);
 
     }
     
     //getSeason() returns 1-4 based off of the time since the map was created.
     //1 = Spring, 2 = Summer, 3 = Fall, 4 = Winter
     public Season getSeason()
     {
         long fullTime = getServer().getWorld(Config.WORLD).getFullTime();
         
         //Seasons are read from the config as minutes!
         long numberOfDays = (fullTime / 24000); //24000 Ticks to a Minecraft-Day
         int season = (int) (((numberOfDays) / Config.SEASON_LENGTH) % 4) + 1;
         switch (season)
         {
             case 1:
                 return Season.SPRING;
             case 2:
                 return Season.SUMMER;
             case 3:
                 return Season.FALL;
             case 4:
                 return Season.WINTER;
             default:
                 log.severe(logPrefix + "Calculated Invalid Season!");
                 return Season.SPRING;
         }
     }
     
     //getDay() returns 1-7 based off of the time since the map was created.
     public Day getDay()
     {
         long fullTime = getServer().getWorld(Config.WORLD).getFullTime();
         
         //Seasons are read from the config as minutes!
         long numberOfDays = (fullTime / 24000); //24000 Ticks to a Minecraft-Day
         
         int day = (int) ((numberOfDays) % 7) + 1;
         switch(day)
         {
             case 1:
                 return Day.SUNDAY;
             case 2:
                 return Day.MONDAY;
             case 3:
                 return Day.TUESDAY;
             case 4:
                 return Day.WEDNESDAY;
             case 5:
                 return Day.THURSDAY;
             case 6:
                 return Day.FRIDAY;
             case 7:
                 return Day.SATURDAY;
             default:
                 log.severe(logPrefix + "Calculated Invalid Day of the Week!");
                 return Day.SATURDAY;
         }
     }
     
     //getDayOfSeason, returns 0-???
     public int getDayOfSeason()
     {
        long fullTime = getServer().getWorld(Config.WORLD).getFullTime();
         
         //Seasons are read from the config as minutes!
         long numberOfDays = (fullTime / 24000); //24000 Ticks to a Minecraft-Day
         
         return (int) (numberOfDays % Config.SEASON_LENGTH);
     }
     
     //getYear() returns the number of years since the map was created.
     public int getYear()
     {
         long fullTime = getServer().getWorld(Config.WORLD).getFullTime();
         
         //Seasons are read from the config as minutes!
         long numberOfDays = (fullTime / 24000); //24000 Ticks to a Minecraft-Day
         
         //Return the number of years
         return (int) (numberOfDays / 360) + 1;
     }
 }
