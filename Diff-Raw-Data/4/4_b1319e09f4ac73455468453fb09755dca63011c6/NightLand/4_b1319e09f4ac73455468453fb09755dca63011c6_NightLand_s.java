 package com.chebab.nightland;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.EventPriority;
 import org.bukkit.plugin.PluginManager;
 
 public class NightLand extends JavaPlugin
 {
     private int moonwatcher_id = -1;
     private int stormwatcher_id = -1;
     private int weather_time = -1;
     private Random rnd;
     private List<String> worlds;
 
     private NightLandBlockListener blocklistener;
 
     public void onDisable() {
         if( moonwatcher_id != -1 )
             getServer().getScheduler().cancelTask( moonwatcher_id );
 
         if( stormwatcher_id != -1 )
             getServer().getScheduler().cancelTask( stormwatcher_id );
 
         System.out.println( "[NightLand] unloaded" );
     }
 
 
     public void onLoad() {
        this.getConfig();
        this.getConfig().options().copyDefaults(true);
     }
 
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
         rnd = new Random();
         worlds = this.getConfig().getStringList( "worlds" );
 
         // Set up the blockplacing event
         if( this.getConfig().getBoolean( "prohibitBedPlacing", false ) )
         {
             blocklistener = new NightLandBlockListener( this );
 
             pm.registerEvents( blocklistener, this );
         }
 
         // Watching the moon.. or well the time but yea..
         moonwatcher_id = getServer().getScheduler().scheduleSyncRepeatingTask(
             this, new Runnable() {
                     public void run() {
                         for( String world_name: worlds )
                         {
                             World w = getServer().getWorld( world_name );
 
                             // Check time reset if needed
                             if( w.getTime() < (long)13672 ||
                                 w.getTime() > (long)21000 )
                             {
                                 w.setTime( (long)13672 );
                             }
                         }
                     }
                 },
             (long)0, (long)1000 );
 
         // Weather gunk
         if( this.getConfig().getBoolean( "extraStorms", false ) )
         {
             weather_time = Math.min(
                 this.getConfig().getInt( "stormDurationMin", 2500 ),
                 this.getConfig().getInt( "niceWeatherMin", 500 ) );
 
             stormwatcher_id = getServer().getScheduler().scheduleSyncRepeatingTask( this, new Runnable() {
                     public void run() {
                         for( String world_name: worlds )
                         {
                             World w = getServer().getWorld( world_name );
                             int storm_min = getConfig().getInt( "stormDurationMin",
                                                                 2500 );
                             int storm_max = getConfig().getInt( "stormDurationMax",
                                                                 10000 ) - storm_min;
                             int nice_min = getConfig().getInt( "niceWeatherMin",
                                                                500 );
                             int nice_max = getConfig().getInt( "niceWeatherMax",
                                                                5000 ) - nice_min;
                             int duration = w.getWeatherDuration();
 
                             if( duration > Math.max( storm_max, nice_max ) )
                                 duration = 0; // Outside our bounds.
                             else if( duration > weather_time )
                                 continue; // Nothing to do for this one yet.
 
                             if( w.isThundering() )
                             {
                                 // Yay sunshine.. wait... no okey no rain then.
                                 w.setThundering( false );
                                 w.setStorm( false );
                                 w.setThunderDuration( 0 );
                                 duration = storm_min + rnd.nextInt( storm_max );
                             }
                             else
                             {
                                 // Okey lets make some baaad weather then
                                 w.setThundering( true );
                                 w.setStorm( true );
                                 w.setThunderDuration(
                                     storm_min + rnd.nextInt( storm_max ) );
 
                                 duration = w.getThunderDuration();
 
                             }
 
                             w.setWeatherDuration( duration );
                         }
                     }
                 }, (long)0, (long)weather_time );
         }
 
 
         System.out.print( "[NightLand] loaded, will check:" );
 
         for( String world_name: worlds )
             System.out.print( " " + world_name );
         System.out.print( "\n" );
     }
 
     public Boolean isWorldNightLand( String world_name ) {
         return worlds.contains( world_name );
     }
 
     public String getBedPlacingMessage() {
         return this.getConfig().getString( "bedPlacingMessage", "You can not do that!" );
     }
 }
