 package us.bliven.bukkit.earthcraft;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.media.jai.JAI;
 import javax.media.jai.OperationRegistry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import us.bliven.bukkit.earthcraft.gis.DataUnavailableException;
 import us.bliven.bukkit.earthcraft.gis.ElevationProvider;
 import us.bliven.bukkit.earthcraft.gis.FlatElevationProvider;
 import us.bliven.bukkit.earthcraft.gis.MapProjection;
 import us.bliven.bukkit.earthcraft.gis.ProjectionTools;
 
 import com.sun.media.jai.imageioimpl.ImageReadWriteSpi;
 import com.vividsolutions.jts.geom.Coordinate;
 
 public class EarthcraftPlugin extends JavaPlugin {
 	private Logger log = null;
 	private ConfigManager config = null;
 
 	// Create a new EarthGen for each world to allow configurability
 	private final Map<String,EarthGen> generators = new HashMap<String, EarthGen>();
 
 	// Permissions
 	static final String PERM_TP_OTHERS = "earthcraft.tp.others";
 
     @Override
 	public void onEnable(){
 
         //log = this.getLogger();
         log = this.getLogger();//Logger.getLogger(EarthcraftPlugin.class.getName());
         log.setLevel(Level.ALL);
 
         log.info("Earthcraft enabled.");
         log.info("CLASSPATH="+System.getProperty("java.class.path"));
 
         initJAI();
 
         // Create default config file if none exists
         saveDefaultConfig();
 
         config = new ConfigManager(this);
 
     }
 
 
     @Override
 	public void onDisable(){
     	log.info("Earthcraft disabled.");
     }
 
     @Override
 	public synchronized ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
     	if(generators.containsKey(worldName)) {
     		return generators.get(worldName);
     	}
 
     	// Set up elevation provider
 
     	// Load info from config file
     	MapProjection projection = config.getProjection(worldName);
     	ElevationProvider provider = config.getProvider(worldName);
     	Coordinate spawn = config.getSpawn(worldName);
 
     	log.info("Setting spawn to "+spawn.x+","+spawn.y);
 
     	// Check that the ElevationProvider is working
     	try {
 			provider.fetchElevation(spawn);
 		} catch (DataUnavailableException e) {
 			log.log(Level.SEVERE, "Unable to load elevation provider!",e);
 			provider = new FlatElevationProvider();
 		}
 
     	EarthGen gen = new EarthGen(projection,provider,spawn);
 
     	Location spawnLoc = gen.getFixedSpawnLocation(null, null);
     	Coordinate spawn2 = projection.locationToCoordinate(spawnLoc);
     	log.info("Spawn is at block "+spawnLoc+" which would be "+spawn2);
 
     	generators.put(worldName,gen);
     	log.info("Creating new Earthcraft Generator for "+worldName);
 
     	return gen;
     }
 
     /**
      * Since plugins get loaded late, the GeoTools JAI operators need to be
      * manually initialized.
      */
     protected void initJAI() {
         // http://docs.oracle.com/cd/E17802_01/products/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/OperationRegistry.html
     	OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
     	if( registry == null) {
     		log.warning("Error with JAI initialization (needed for GeoTools).");
     	} else {
     		// Load the two registry files we need
 
     		// Deserialization throws errors, so (for now), just add the specific registries manually
     		//initJAIFromFile("/META-INF/services/javax.media.jai.OperationRegistrySpi", registry);
     		//initJAIFromFile("/META-INF/registryFile.jai", registry);
 
     		new ImageReadWriteSpi().updateRegistry(registry);
     	}
     }
 
     /**
      * Initialize JAI from a registry file
      * @param resource Local path to the registry file relative to the jar
      * @param registry The OperationRegistry to update
      */
     @SuppressWarnings("unused")
 	private void initJAIFromFile(String resource, OperationRegistry registry) {
     	InputStream in = EarthcraftPlugin.class.getResourceAsStream(resource);
     	if( in == null) {
     		log.warning("Error with JAI initialization. Unable to find "+resource);
     	} else {
     		try {
     			registry.updateFromStream(in);
     		} catch(IOException e) {
         		log.log(Level.WARNING,"Error with JAI initialization while reading "+resource, e);
     		}
     		try {
 				in.close();
 			} catch (IOException e) {
         		log.log(Level.WARNING,"Error with JAI initialization while closing "+resource, e);
 			}
     	}
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
     	String name = cmd.getName();
     	if(name.equalsIgnoreCase("earth")) {
     		if( args.length<1 )
     			//no subcommand specified
     			return false;
     		String subcmd = args[0];
     		String[] subargs = Arrays.copyOfRange(args, 1, args.length);
     		if( subcmd.equalsIgnoreCase("pos")) {
     			return onPosCommand(sender,subargs);
     		} else if(subcmd.equalsIgnoreCase("tp")) {
     			return onTPCommand(sender,subargs);
     		}
     	} else if(name.equalsIgnoreCase("earthpos")){
 			return onPosCommand(sender,args);
     	} else if(name.equalsIgnoreCase("earthtp")){
 			return onTPCommand(sender,args);
     	}
 
     	return false;
     }
 
 
     /**
      * Handle tp command
      *
      * usage: /earthtp [player] lat lon
      * @param sender
      * @param args
      * @return
      */
 	private boolean onTPCommand(CommandSender sender, String[] args) {
 		if(args.length < 2 || 3 < args.length) {
 			return false;
 		}
 
 		// Determine the teleported player
 		int argi = 0;
 		Player player;
 		World world;
 
 		if(args.length == 2) {
 			// Player is itself
 			if( ! (sender instanceof Player) ) {
 				sender.sendMessage("Error: Player required from console");
 				return false;
 			}
 			player = (Player)sender;
 			world = player.getWorld();
 		} else {
 			// Use specified player
 			String playername = args[argi++];
 			player = Bukkit.getPlayer(playername);
 			if( !player.isOnline() ) {
 				sender.sendMessage("Error: "+playername+" is offline");
 				return true;
 			}
 			// If sender is in a Earthcraft world, teleport use that world
 			if( sender instanceof Player) {
 				world = ((Player)sender).getWorld();
 				if( ! generators.containsKey(world.getName()) ) {
 					world = player.getWorld();
 				}
 			} else {
 				world = player.getWorld();
 			}
 		}
 
 		// check for permission to teleport others
 		if(!sender.equals(player)) {
 			if( ! sender.hasPermission(PERM_TP_OTHERS) ) {
 				sender.sendMessage("You don't have permission to teleport others. Need "+PERM_TP_OTHERS);
 			}
 		}
 
 
 		double lat,lon;
 		try {
 			lat = Double.parseDouble(args[argi++]);
 			lon = Double.parseDouble(args[argi++]);
 		} catch( NumberFormatException e) {
 			sender.sendMessage("Error: unable to parse coordinate");
 			return false;
 		}
 
 		Coordinate coord = new Coordinate(lat,lon);
 
 
 		EarthGen gen = generators.get(world.getName());
 		if( gen == null) {
 			sender.sendMessage(world.getName()+" is not an Earthcraft world.");
 			return false;
 		}
 
 		//coord.z = gen.getElevationProvider().fetchElevation(coord);
 
 		MapProjection proj = gen.getMapProjection();
 
 		Location loc = proj.coordinateToLocation(player.getWorld(), coord);
 
		if(Double.isNaN(loc.getZ()) ){
			loc.setZ(200);
 		}
 		log.info("Teleporting "+player.getName()+" to "+loc);
 		player.teleport(loc);
 
 		return true;
 	}
 
 
 	/**
 	 * Handle pos command
 	 *
 	 * usage: /earthpos [player]
 	 * @param sender
 	 * @param args
 	 * @return
 	 */
 	private boolean onPosCommand(CommandSender sender, String[] args) {
 		if(args.length > 1) {
 			return false;
 		}
 		Player player;
 		if( args.length == 1) {
 			// Send position of specified player
 			String playername = args[0];
 			player = Bukkit.getPlayer(playername);
 			if( !player.isOnline() ) {
 				sender.sendMessage("Error: "+playername+" is offline");
 				return true;
 			}
 
 		} else {
 			// Send position of current player
 			if( sender instanceof Player ) {
 				player = (Player) sender;
 			} else {
 				sender.sendMessage("Error: Player required from console");
 				return false;
 			}
 		}
 		String world = player.getWorld().getName();
 		EarthGen gen = generators.get(world);
 		if( gen == null) {
 			sender.sendMessage("Player "+player.getName()+" not in an Earthcraft world.");
 			return false;
 		}
 
 		Location loc = player.getLocation();
 		MapProjection proj = gen.getMapProjection();
 
 		Coordinate coord = proj.locationToCoordinate(loc);
 
 		Coordinate localScale = proj.getLocalScale(coord);
 		String message = String.format("%s located at %s", player.getName(),
 				ProjectionTools.latlonelevString(coord,localScale));
 		sender.sendMessage(message);
 		return true;
 	}
 
 
 	public static void main(String[] a) {
     	System.out.println("CLASSPATH="+System.getProperty("java.class.path"));
     	//Coordinate x = new Coordinate(1.,2.);
 
     	EarthcraftPlugin plugin = new EarthcraftPlugin();
     	plugin.getDefaultWorldGenerator("foo", "id");
     }
 }
