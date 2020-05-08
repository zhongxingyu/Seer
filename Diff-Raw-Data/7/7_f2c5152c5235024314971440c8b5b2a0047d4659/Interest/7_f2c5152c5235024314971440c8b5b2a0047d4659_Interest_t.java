 package com.seiken.interest;
 
 import java.io.File;
 import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Interest extends JavaPlugin {
 	
 	public static final String VERSION_1_1 = "version1.1";
 	public static final String DATA_FILE = "places.txt";
 	public static final String CONFIG_FILE = "config.txt";
 
     private final InterestPlayer player = new InterestPlayer( this );
     private final InterestVehicle vehicle = new InterestVehicle( this );
     private final Places places = new Places( this );
     private final Config config = new Config( this );
     
    private static final Logger log = Logger.getLogger("Minecraft");
    
     private PlaceTree placeTree = null;
     private HashMap< Player, Place > current = new HashMap< Player, Place >();
     private HashMap< Player, Long > times = new HashMap< Player, Long >();
 
     public void onEnable()
     {
    	log.log(Level.INFO, this.getDescription().getFullName() + " is enabled!");
     	updatePlaces();
         getServer().getPluginManager().registerEvent( Event.Type.PLAYER_JOIN,    player,  Priority.Normal, this );
         getServer().getPluginManager().registerEvent( Event.Type.PLAYER_QUIT,    player,  Priority.Normal, this );
         getServer().getPluginManager().registerEvent( Event.Type.PLAYER_MOVE,    player,  Priority.Normal, this );
         getServer().getPluginManager().registerEvent( Event.Type.VEHICLE_MOVE,   vehicle, Priority.Normal, this );
     }
 
 
     public void onDisable()
     {
     }
     
     @Override
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
     	
     	boolean b = false;
     	
     	String commandName = cmd.getName().toLowerCase();
     	Player p = null;
     	if(sender instanceof Player)
     		p = (Player) sender;
     	
     	if ( ( commandName.equals( "who" ) && !this.getConfig().disableWho() ) || commandName.equals( "where" ) ) {
     		if ( args.length <= 0 )
     			this.sendWho( p );
     		else
     			this.sendWho( p, args[ 0 ] );
     		b = true;
     	}
     	
     	if ( commandName.equals( "nearest" ) ) {
     		this.sendNearest( p );
     		b = true;
     	}
     	
     	if ( commandName.equals( "mark" ) ) {
     		if ( args.length <= 0 )
     			this.markPlace( p, null, -1, -1, -1, -1 );
     		else {
     			String[] split = args[ 0 ].split( " " );
     			int r = -1;
     			int x = -1;
     			int y = -1;
     			int z = -1;
     			boolean a = true;
     			int n = 0;
     			while ( a ) {
     				a = false;
     				if ( split[ n ].length() >= 3 && split[ n ].substring( 0, 2 ).equals( "r:" ) ) {
         				try {
         					r = Integer.parseInt( split[ n ].substring( 2 ) );
         					a = true;
         					n++;
         				}
         				catch ( NumberFormatException e ) {
         				}
     				}
     				if ( split[ n ].length() >= 3 && split[ n ].substring( 0, 2 ).equals( "x:" ) ) {
         				try {
         					x = Integer.parseInt( split[ n ].substring( 2 ) );
         					a = true;
         					n++;
         				}
         				catch ( NumberFormatException e ) {
         				}
     				}
     				if ( split[ n ].length() >= 3 && split[ n ].substring( 0, 2 ).equals( "y:" ) ) {
         				try {
         					y = Integer.parseInt( split[ n ].substring( 2 ) );
         					a = true;
         					n++;
         				}
         				catch ( NumberFormatException e ) {
         				}
     				}
     				if ( split[ n ].length() >= 3 && split[ n ].substring( 0, 2 ).equals( "z:" ) ) {
         				try {
         					z = Integer.parseInt( split[ n ].substring( 2 ) );
         					a = true;
         					n++;
         				}
         				catch ( NumberFormatException e ) {
         				}
     				}
     			}
     			
     			this.markPlace( p, Interest.safeMessage( args[ 0 ].split( " ", n + 1 )[ n ].replaceAll( "##", "§" ) ), r, x, y, z );
     		}
     		b = true;
     	}
     	
     	if ( commandName.equals( "unmark" ) ) {
     		this.unmarkPlace( p );
     		b = true;
     	}
     	
     	return b;
 	}
 
 	public Config getConfig()
     {
     	return config;
     }
     
     public static String safeMessage( String message )
     {
     	String r = message;
     	boolean b = true;
     	while ( b ) {
     		b = false;
 	    	while ( r.length() >= 2 && r.charAt( r.length() - 2 ) == '§' ) {
 	    		r = r.substring( 0, r.length() - 2 );
 	    		b = true;
 	    	}
 	    	while ( r.length() >= 1 && r.charAt( r.length() - 1 ) == '§' ) {
 	    		r = r.substring( 0, r.length() - 1 );
 	    		b = true;
 	    	}
     	}
     	return r;
     }
 
     private Place nearestPlace( Player player )
     {
     	if ( placeTree == null )
     		return null;
     	return placeTree.nearest( player.getLocation() );
     }
     
     public void updateCurrent( Player player )
     {
     	if ( times.containsKey( player ) && System.currentTimeMillis() - times.get( player ) < 8000 )
     		return;
     	
     	Place place = nearestPlace( player );
     	
 		Place old = null;
 		if ( current.containsKey( player ) )
 			old = current.get( player );
 		
 		if ( ( place != old || !current.containsKey( player ) ) && !( old != null && place != null && old.getName().equals( place.getName() ) ) ) {
 			if ( place == null ) {
 				if ( old != null || !config.leavingUsesArg() )
 					player.sendMessage( config.leaving( old != null ? old.getName() : "" ) );
 			}
 			else
 				player.sendMessage( config.entering( place.getName() ) );
 			times.put( player, System.currentTimeMillis() );
 		}
 		
 		current.put( player, place );
     }
     
     public void removeCurrent( Player player )
     {
     	current.remove( player );
     	times.remove( player );
     }
     
     public void sendWho( Player player )
     {
     	Player[] players = getServer().getOnlinePlayers();
     	player.sendMessage( config.whoHead( Integer.toString( players.length ) ) );
     	for ( Player p : players ) {
 			Place place = nearestPlace( p );
 		
 			if ( place == null )
 				player.sendMessage( config.whoLineNoPlace( p.getName() ) );
 			else
 				player.sendMessage( config.whoLinePlace( p.getName(), place.getName() ) );
     	}
     }
     
     public void sendWho( Player player, String who )
     {
     	boolean b = false;
     	Player[] players = getServer().getOnlinePlayers();
     	for ( Player p : players ) {
     		if ( p.getName().equalsIgnoreCase( who ) ) {
     			Location loc = p.getLocation();
     			Place place = nearestPlace( p );
     			
     			if ( place == null )
     				player.sendMessage( config.whoLineNoPlace( p.getName() ) );
     			else
     				player.sendMessage( config.whoLinePlace( p.getName(), place.getName() ) );
     			player.sendMessage( "§f[" + Integer.toString( loc.getBlockX() ) + ", " + Integer.toString( loc.getBlockY() ) + ", " + Integer.toString( loc.getBlockZ() ) + "]" );
     			b = true;
     		}
     	}
     	if ( !b ) {
     		player.sendMessage( "§fplayer not found!" );
     	}
     }
     
     public void sendNearest( Player player )
     {
     	Place nearest = nearestPlace( player );
     	if ( nearest != null )
     		player.sendMessage( nearest.getDesc() );
     }
     
     public void markPlace( Player player, String name, int radius, int xDist, int yDist, int zDist )
     {
     	if ( config.opsOnly() && !player.isOp() ) {
     		player.sendMessage( "§fops only!" );
     		return;
     	}
     	
     	Place nearest = nearestPlace( player );
     	
     	if ( nearest != null && nearest.distance( player.getLocation() ) < 100 ) {
     		player.sendMessage( "§ftoo close to " + nearest.getDesc() + "!" );
     		return;
     	}
     	
     	if ( name == null || name.trim().equals( "" ) ) {
     		player.sendMessage( "§fsupply a name!" );
     		return;
     	}
     	
     	Place mark = null;
     	if ( radius > 0 )
     		mark = new Place( player.getLocation(), radius, name );
     	else
     		mark = new Place( player.getLocation(), xDist, yDist, zDist, name );
     	
     	places.getPlaces().add( mark );
     	updatePlaces();
     	player.sendMessage( "§fmarked " + mark.getDesc() );
     	
     	for ( Player p : getServer().getOnlinePlayers() )
     		updateCurrent( p );
     }
     
     public void unmarkPlace( Player player )
     {
     	if ( config.opsOnly() && !player.isOp() ) {
     		player.sendMessage( "§fops only!" );
     		return;
     	}
     	
     	Place nearest = nearestPlace( player );
     	
     	if ( nearest == null ) {
     		player.sendMessage( "§fnothing to unmark!" );
     		return;
     	}
     	
     	places.getPlaces().remove( nearest );
     	updatePlaces();
     	player.sendMessage( "§funmarked " + nearest.getDesc() );
     	
     	for ( Player p : getServer().getOnlinePlayers() )
     		updateCurrent( p );
     }
     
     
     private void updatePlaces()
     {
     	placeTree = new PlaceTree( places.getPlaces() );
     	places.updateData();
     }
     
 }
