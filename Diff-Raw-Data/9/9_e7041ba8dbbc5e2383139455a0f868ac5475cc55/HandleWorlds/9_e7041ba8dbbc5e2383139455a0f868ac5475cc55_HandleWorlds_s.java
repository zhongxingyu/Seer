 package net.kiwz.ThePlugin.utils;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import net.kiwz.ThePlugin.ThePlugin;
 import net.kiwz.ThePlugin.mysql.Worlds;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldCreator;
 import org.bukkit.WorldType;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.util.ChatPaginator;
 
 public class HandleWorlds {
 	Worlds world = new Worlds();
 	HandleHomes hHomes = new HandleHomes();
 	RemoveEntities e = new RemoveEntities();
 	private HashMap<String, Worlds> worlds = ThePlugin.getWorlds;
 
 	public void loadWorlds() {
 		for (String worldName : getWorldNames()) {
 			
 			String env = worlds.get(worldName).environment;
 			String type = worlds.get(worldName).type;
 			String seed = worlds.get(worldName).seed;
 			String[] stringCoords = worlds.get(worldName).coords.split(" ");
 			int x = (int) Double.parseDouble(stringCoords[0]);
 			int y = (int) Double.parseDouble(stringCoords[1]);
 			int z = (int) Double.parseDouble(stringCoords[2]);
 			WorldCreator wc = new WorldCreator(worldName.toLowerCase());
 			
 			if (env.equalsIgnoreCase("nether")) wc.environment(Environment.NETHER);
 			else if (env.equalsIgnoreCase("the_end")) wc.environment(Environment.THE_END);
 			else wc.environment(Environment.NORMAL);
 			
 			if (type.equalsIgnoreCase("flat")) wc.type(WorldType.FLAT);
 			else if (type.equalsIgnoreCase("largebiomes")) wc.type(WorldType.LARGE_BIOMES);
 			else if (type.equalsIgnoreCase("version1")) wc.type(WorldType.VERSION_1_1);
 			else wc.type(WorldType.NORMAL);
 			
 			if (seed.matches("[0-9-]+")) wc.seed(Long.parseLong(seed));
 			
 			wc.createWorld();
 			
 			World world = Bukkit.getServer().getWorld(worldName);
 			
 			boolean pvp = false;
 			if (worlds.get(worldName).pvp == 1) pvp = true;
 			boolean monsters = false;
 			if (worlds.get(worldName).monsters == 1) monsters = true;
 			boolean animals = false;
 			if (worlds.get(worldName).animals == 1) animals = true;
 			boolean keepSpawn = false;
 			if (worlds.get(worldName).keepSpawn == 1) keepSpawn = true;
 			
 			world.setPVP(pvp);
 			world.setSpawnFlags(monsters, animals);
 			world.setKeepSpawnInMemory(keepSpawn);
 			world.setSpawnLocation(x, y, z);
 		}
 	}
 	
 	public String remWorld(String worldName) {
 		World world = getWorld(worldName);
 		if (world == null) {
 			return ThePlugin.c2 + worldName + " finnes ikke";
 		}
 		for (Player player : world.getPlayers()) {
 			player.teleport(getSpawn(player, "world"));
 			player.sendMessage(ThePlugin.c2 + "Verdenen du var i ble slettet, du er n i hoved-spawnen");
 		}
 		if (!Bukkit.getServer().unloadWorld(world, true)) {
 			return ThePlugin.c2 + worldName + " kunne ikke bli slettet";
 		}
 		hHomes.removeHomes(world);
 		worldName = world.getName();
 		worlds.remove(worldName);
 		ThePlugin.remWorlds.add(worldName);
 		try {
 			new File("plugins\\ThePlugin\\Old worlds\\").mkdirs();
 			File folder = new File(worldName);
 			folder.renameTo(new File("plugins\\ThePlugin\\Old Worlds\\" + folder.getName() + " (" + System.currentTimeMillis() / 1000 + ")"));
 		}
 		catch (Exception e) {
 			return ThePlugin.c1 + worldName + " er slettet (filene m slettes/flyttes manuelt)";
 		}
 		return ThePlugin.c1 + worldName + " er slettet (filene ligger i egen mappe)";
 	}
 	
 	public String createWorld(String worldName, String env, String type, String seed) {
 		if (!isWorldNameAvailable(worldName.toLowerCase())) return ThePlugin.c2 + worldName + " Er opptatt";
 		
 		WorldCreator wc = new WorldCreator(worldName.toLowerCase());
 		
 		if (env.equalsIgnoreCase("nether")) wc.environment(Environment.NETHER);
 		else if (env.equalsIgnoreCase("the_end")) wc.environment(Environment.THE_END);
 		else wc.environment(Environment.NORMAL);
 
 		if (type.equalsIgnoreCase("flat")) wc.type(WorldType.FLAT);
 		else if (type.equalsIgnoreCase("large")) wc.type(WorldType.LARGE_BIOMES);
 		else if (type.equalsIgnoreCase("version1")) wc.type(WorldType.VERSION_1_1);
 		else wc.type(WorldType.NORMAL);
 		
 		if (seed.matches("[0-9-]+")) wc.seed(Long.parseLong(seed));
 		
 		wc.createWorld();
 		World world = Bukkit.getServer().getWorld(worldName);
 		addWorld(world);
 		return ChatColor.GOLD + "World: " + ChatColor.WHITE + world.getName()
 				+ ChatColor.GOLD + " Environment: " + ChatColor.WHITE + world.getEnvironment()
 				+ ChatColor.GOLD + " Type: " + ChatColor.WHITE + world.getWorldType()
 				+ ChatColor.GOLD + " Seed: " + ChatColor.WHITE + world.getSeed();
 	}
 	
 	/**
 	 * 
 	 * @param newWorld
 	 * 
 	 * <p>Adds a new world with given newWorld</p>
 	 */
 	public void addWorld(World newWorld) {
 		String coords = newWorld.getSpawnLocation().getX() + " " + newWorld.getSpawnLocation().getY() + " " + newWorld.getSpawnLocation().getZ();
 		String pitch = newWorld.getSpawnLocation().getPitch() + " " + newWorld.getSpawnLocation().getYaw();
 		int border = 1000;
 		int pvp = 0;
 		if (newWorld.getPVP()) pvp = 1;
 		int monsters = 0;
 		if (newWorld.getAllowMonsters()) monsters = 1;
 		int animals = 0;
 		if (newWorld.getAllowAnimals()) animals = 1;
 		int keepSpawn = 0;
 		if (newWorld.getKeepSpawnInMemory()) keepSpawn = 1;
 		String environment = newWorld.getEnvironment().toString();
 		String type = newWorld.getWorldType().getName();
 		String seed = "" + newWorld.getSeed();
 		
 		world.world = newWorld.getName();
 		world.coords = coords;
 		world.pitch = pitch;
 		world.border = border;
 		world.claimable = 0;
 		world.fireSpread = 0;
 		world.explosions = 0;
 		world.monsterGrief = 0;
 		world.trample = 0;
 		world.pvp = pvp;
 		world.monsters = monsters;
 		world.animals = animals;
 		world.keepSpawn = keepSpawn;
 		world.environment = environment; // Kan ikke endres senere
 		world.type = type; // Kan ikke endres senere
 		world.seed = seed; // Kan ikke endres senere
 		worlds.put(world.world, world);
 		ThePlugin.remWorlds.remove(newWorld.getName());
 	}
 	
 	public ArrayList<String> getWorldNames() {
 		ArrayList<String> worldNames = new ArrayList<String>();
 		for (String worldName : worlds.keySet()) {
 			worldNames.add(worldName);
 		}
 		return worldNames;
 	}
 	
 	/**
 	 * 
 	 * @param worldName to search for
 	 * @return World with the given name, or null if none exists
 	 */
 	public World getWorld(String worldName) {
 		World world = null;
 		
 		for (World worlds : Bukkit.getServer().getWorlds()) {
 			if (worlds.getName().toUpperCase().contains(worldName.toUpperCase())) {
 				world = Bukkit.getServer().getWorld(worlds.getName());
 				break;
 			}
 		}
 		
 		for (World worlds : Bukkit.getServer().getWorlds()) {
 			if (worlds.getName().equalsIgnoreCase(worldName)) {
 				world = Bukkit.getServer().getWorld(worlds.getName());
 				break;
 			}
 		}
 		return world;
 	}
 	
 	/**
 	 * 
 	 * @param worldName as String
 	 * @return spawn location for given worldName as Location
 	 */
 	public Location getSpawn(String worldName) {
 		Location loc = null;
 		World world = getWorld(worldName);
 		
 		if (world == null) {
 			return loc;
 		}
 		
 		if (worlds.get(world.getName()) == null) {
 			return world.getSpawnLocation();
 		}
 		
 		else {
 			String[] stringCoords = worlds.get(world.getName()).coords.split(" ");
 			String[] stringPitch = worlds.get(world.getName()).pitch.split(" ");
 			double x = Double.parseDouble(stringCoords[0]);
 			double y = Double.parseDouble(stringCoords[1]);
 			double z = Double.parseDouble(stringCoords[2]);
 			float pitch = Float.parseFloat(stringPitch[0]);
 			float yaw = Float.parseFloat(stringPitch[1]);
 			loc = new Location(world, x, y, z);
 			loc.setPitch(pitch);
 			loc.setYaw(yaw);
 			return loc;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param player as Object
 	 * @param worldName as String
 	 * @return spawn location for given worldName as Location
 	 */
 	public Location getSpawn(Player player, String worldName) {
 		Location loc = null;
 		World world = getWorld(worldName);
 		
 		if (world == null) {
 			player.sendMessage(ThePlugin.c2 + worldName + " finnes ikke");
 			return loc;
 		}
 		
 		if (worlds.get(world.getName()) == null) {
 			return world.getSpawnLocation();
 		}
 		
 		else {
 			String[] stringCoords = worlds.get(world.getName()).coords.split(" ");
 			String[] stringPitch = worlds.get(world.getName()).pitch.split(" ");
 			double x = Double.parseDouble(stringCoords[0]);
 			double y = Double.parseDouble(stringCoords[1]);
 			double z = Double.parseDouble(stringCoords[2]);
 			float pitch = Float.parseFloat(stringPitch[0]);
 			float yaw = Float.parseFloat(stringPitch[1]);
 			loc = new Location(world, x, y, z);
 			loc.setPitch(pitch);
 			loc.setYaw(yaw);
 			return loc;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param world
 	 */
 	
 	public int getBorder(World world) {
 		return worlds.get(world.getName()).border;
 	}
 	
 	public boolean isWorldNameAvailable(String worldName) {
 		if (worlds.containsKey(worldName.toLowerCase())) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param world
 	 * @return true if world is claimable
 	 */
 	public boolean isClaimable(World world) {
 		if (worlds.get(world.getName()).claimable == 0) return false;
 		return true;
 	}
 	
 	public boolean isFireSpread(World world) {
 		if (worlds.get(world.getName()).fireSpread == 0) return false;
 		return true;
 	}
 	
 	public boolean isExplosions(World world) {
 		if (worlds.get(world.getName()).explosions == 0) return false;
 		return true;
 	}
 	
 	public boolean isMonsterGrief(World world) {
 		if (worlds.get(world.getName()).monsterGrief == 0) return false;
 		return true;
 	}
 	
 	public boolean isTrample(World world) {
 		if (worlds.get(world.getName()).trample == 0) return false;
 		return true;
 	}
 	
 	public boolean isPvP(World world) {
 		if (worlds.get(world.getName()).pvp == 0) return false;
 		return true;
 	}
 	
 	public boolean isMonsters(World world) {
 		if (worlds.get(world.getName()).monsters == 0) return false;
 		return true;
 	}
 	
 	public boolean isAnimals(World world) {
 		if (worlds.get(world.getName()).animals == 0) return false;
 		return true;
 	}
 	
 	public boolean isKeepSpawnInMemory(World world) {
 		if (worlds.get(world.getName()).keepSpawn == 0) return false;
 		return true;
 	}
 	
 	public String getEnvironment(World world) {
 		return worlds.get(world.getName()).environment;
 	}
 	
 	public String getType(World world) {
 		return worlds.get(world.getName()).type;
 	}
 	
 	public String getSeed(World world) {
 		return worlds.get(world.getName()).seed;
 	}
 	
 	/**
 	 * 
 	 * @param loc as Location where the new spawn will be
 	 * @return String describing the result
 	 */
 	public String setSpawn(Location loc) {
 		String worldName = loc.getWorld().getName();
 		Double x = loc.getX();
 		Double y = loc.getY();
 		Double z = loc.getZ();
 		Float pitch = loc.getPitch();
 		Float yaw = loc.getYaw();
 		String stringCoords = x + " " + y + " " + z;
 		String stringPitch = pitch + " " + yaw;
 		worlds.get(worldName).coords = stringCoords;
 		worlds.get(worldName).pitch = stringPitch;
 		Bukkit.getWorld(worldName).setSpawnLocation(x.intValue(), y.intValue(), z.intValue());
 		return ThePlugin.c1 + "Du har satt spawnen her";
 	}
 	
 	/**
 	 * 
 	 * @param border as int
 	 * @return String describing the result
 	 */
 	public String setBorder(String worldName, String size) {
 		int border = Integer.parseInt(size);
 		World world = getWorld(worldName);
 		if (world == null) {
 			return ThePlugin.c2 + worldName + " finnes ikke";
 		}
 		worldName = world.getName();
 		worlds.get(worldName).border = border;
 		return ThePlugin.c1 + "Ny grense for " + worldName + " er: " + border;
 	}
 	
 	public String setClaimable(String worldName) {
 		World world = getWorld(worldName);
 		if (world == null) {
 			return ThePlugin.c2 + worldName + " finnes ikke";
 		}
 		if (isClaimable(world)) {
 			worlds.get(world.getName()).claimable = 0;
 			return ThePlugin.c1 + "Plasser er deaktviert for " + world.getName();
 		}
 		worlds.get(world.getName()).claimable = 1;
 		return ThePlugin.c1 + "Plasser er aktivert for " + world.getName();
 	}
 	
 	public String setFireSpread(String worldName) {
 		World world = getWorld(worldName);
 		if (world == null) {
 			return ThePlugin.c2 + worldName + " finnes ikke";
 		}
 		if (isFireSpread(world)) {
 			worlds.get(world.getName()).fireSpread = 0;
 			return ThePlugin.c1 + "Ill spredning er deaktviert for " + world.getName();
 		}
 		worlds.get(world.getName()).fireSpread = 1;
 		return ThePlugin.c1 + "Ill spredning er aktivert for " + world.getName();
 	}
 	
 	public String setExplosions(String worldName) {
 		World world = getWorld(worldName);
 		if (world == null) {
 			return ThePlugin.c2 + worldName + " finnes ikke";
 		}
 		if (isExplosions(world)) {
 			worlds.get(world.getName()).explosions = 0;
 			return ThePlugin.c1 + "Eksplosjoner er deaktviert for " + world.getName();
 		}
 		worlds.get(world.getName()).explosions = 1;
 		return ThePlugin.c1 + "Eksplosjoner er aktivert for " + world.getName();
 	}
 	
 	public String setMonsterGrief(String worldName) {
 		World world = getWorld(worldName);
 		if (world == null) {
 			return ThePlugin.c2 + worldName + " finnes ikke";
 		}
 		if (isMonsterGrief(world)) {
 			worlds.get(world.getName()).monsterGrief = 0;
 			return ThePlugin.c1 + "Monster grifing er deaktviert for " + world.getName();
 		}
 		worlds.get(world.getName()).monsterGrief = 1;
 		return ThePlugin.c1 + "Monster grifing er aktivert for " + world.getName();
 	}
 	
 	public String setTrample(String worldName) {
 		World world = getWorld(worldName);
 		if (world == null) {
 			return ThePlugin.c2 + worldName + " finnes ikke";
 		}
 		if (isTrample(world)) {
 			worlds.get(world.getName()).trample = 0;
 			return ThePlugin.c1 + "Trkking er deaktviert for " + world.getName();
 		}
 		worlds.get(world.getName()).trample = 1;
 		return ThePlugin.c1 + "Trkking er aktivert for " + world.getName();
 	}
 	
 	public String setPvP(String worldName) {
 		World world = getWorld(worldName);
 		if (world == null) {
 			return ThePlugin.c2 + worldName + " finnes ikke";
 		}
 		if (isPvP(world)) {
 			worlds.get(world.getName()).pvp = 0;
 			world.setPVP(false);
 			return ThePlugin.c1 + "PvP er deaktviert for " + world.getName();
 		}
 		worlds.get(world.getName()).pvp = 1;
 		world.setPVP(true);
 		return ThePlugin.c1 + "PvP er aktivert for " + world.getName();
 	}
 	
 	public String setMonsters(String worldName) {
 		World world = getWorld(worldName);
 		if (world == null) {
 			return ThePlugin.c2 + worldName + " finnes ikke";
 		}
 		if (isMonsters(world)) {
 			worlds.get(world.getName()).monsters = 0;
 			world.setSpawnFlags(false, isAnimals(world));
 			e.killMonsters(world);
 			return ThePlugin.c1 + "Monstre er deaktviert for " + world.getName();
 		}
 		worlds.get(world.getName()).monsters = 1;
 		world.setSpawnFlags(true, isAnimals(world));
 		return ThePlugin.c1 + "Monstre er aktivert for " + world.getName();
 	}
 	
 	public String setAnimals(String worldName) {
 		World world = getWorld(worldName);
 		if (world == null) {
 			return ThePlugin.c2 + worldName + " finnes ikke";
 		}
 		if (isAnimals(world)) {
 			worlds.get(world.getName()).animals = 0;
 			world.setSpawnFlags(isMonsters(world), false);
 			e.killAnimals(world);
 			return ThePlugin.c1 + "Dyr er deaktviert for " + world.getName();
 		}
 		worlds.get(world.getName()).animals = 1;
 		world.setSpawnFlags(isMonsters(world), true);
 		return ThePlugin.c1 + "Dyr er aktivert for " + world.getName();
 	}
 	
 	public String setKeepSpawnInMemory(String worldName) {
 		World world = getWorld(worldName);
 		if (world == null) {
 			return ThePlugin.c2 + worldName + " finnes ikke";
 		}
 		if (isKeepSpawnInMemory(world)) {
 			worlds.get(world.getName()).keepSpawn = 0;
 			world.setKeepSpawnInMemory(false);
 			return ThePlugin.c1 + "Beholde spawn i minne er deaktivert for " + world.getName();
 		}
 		worlds.get(world.getName()).keepSpawn = 1;
 		world.setKeepSpawnInMemory(true);
 		return ThePlugin.c1 + "Beholde spawn i minne er aktivert for " + world.getName();
 	}
 	
 	public void sendWorld(CommandSender sender, String worldName) {
 		World world = getWorld(worldName);
 		if (world == null) {
 			sender.sendMessage(ThePlugin.c2 + worldName + " finnes ikke");
 			return;
 		}
 		String claimable = "Nei";
 		if (isClaimable(world)) claimable = "Ja";
 		String fireSpread = "Nei";
 		if (isFireSpread(world)) fireSpread = "Ja";
 		String explosions = "Nei";
 		if (isExplosions(world)) explosions = "Ja";
 		String monsterGrief = "Nei";
 		if (isMonsterGrief(world)) monsterGrief = "Ja";
 		String trample = "Nei";
 		if (isTrample(world)) trample = "Ja";
 		String pvp = "Nei";
 		if (isPvP(world)) pvp = "Ja";
 		String monsters = "Nei";
 		if (isMonsters(world)) monsters = "Ja";
 		String animals = "Nei";
 		if (isAnimals(world)) animals = "Ja";
 		String keepSpawn = "Nei";
 		if (isKeepSpawnInMemory(world)) keepSpawn = "Ja";
 		String environment = getEnvironment(world);
 		String type = getType(world);
 		String seed = getSeed(world);
 		
 		StringBuilder header = new StringBuilder();
 		header.append(ChatColor.YELLOW);
 		header.append("----- ");
 		header.append(ChatColor.WHITE);
 		header.append(world.getName().toUpperCase());
 		header.append(ChatColor.GRAY);
 		header.append(" (" + environment + ", " + type + ") ");
 		header.append(ChatColor.YELLOW);
 		header.append(" -----");
 		for (int i = header.length(); i < ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH; i++) {
 			header.append("-");
 		}
 		sender.sendMessage(header.toString());
 		sender.sendMessage(ThePlugin.c1 + "Strrelse: " + ThePlugin.c4 + getBorder(world) * 2);
 		sender.sendMessage(ThePlugin.c1 + "Plasser: " + ThePlugin.c4 + claimable);
 		sender.sendMessage(ThePlugin.c1 + "Ill spredning: " + ThePlugin.c4 + fireSpread);
 		sender.sendMessage(ThePlugin.c1 + "Eksplosjoner: " + ThePlugin.c4 + explosions);
 		sender.sendMessage(ThePlugin.c1 + "Monster grifing: " + ThePlugin.c4 + monsterGrief);
 		sender.sendMessage(ThePlugin.c1 + "Trkking: " + ThePlugin.c4 + trample);
 		sender.sendMessage(ThePlugin.c1 + "PvP: " + ThePlugin.c4 + pvp);
 		sender.sendMessage(ThePlugin.c1 + "Monstre: " + ThePlugin.c4 + monsters);
 		sender.sendMessage(ThePlugin.c1 + "Dyr: " + ThePlugin.c4 + animals);
 		sender.sendMessage(ThePlugin.c1 + "Behold spawn i minne: " + ThePlugin.c4 + keepSpawn);
 		sender.sendMessage(ThePlugin.c1 + "Fr: " + ThePlugin.c4 + seed);
 	}
 	
 	public void sendWorldList(CommandSender sender) {
 		ArrayList<String> worldList = new ArrayList<String>();
 		for (World world : Bukkit.getServer().getWorlds()) {
 			worldList.add(ThePlugin.c1 + world.getName());
 		}
 		
 		StringBuilder header = new StringBuilder();
 		header.append(ChatColor.YELLOW);
 		header.append("----- ");
 		header.append(ChatColor.WHITE);
 		header.append("Liste over alle verdener p denne server");
 		header.append(ChatColor.YELLOW);
 		header.append(" -----");
 		for (int i = header.length(); i < ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH; i++) {
 			header.append("-");
 		}
 		sender.sendMessage(header.toString());
 		
 		Collections.sort(worldList, String.CASE_INSENSITIVE_ORDER);
 		for (String worldName : worldList) {
 			sender.sendMessage(worldName);
 		}
 	}
 }
