 package net.kiwz.ThePlugin.utils;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldType;
 
 public class MyWorld {
 	private static HashMap<String, MyWorld> worlds = new HashMap<String, MyWorld>();
 	
 	private String name;
 	private Environment env;
 	private WorldType type;
 	private long seed;
 	private Location spawn;
 	private boolean keepSpawn;
 	private boolean pvp;
 	private boolean monsters;
 	private boolean animals;
 	private boolean monsterGrief;
 	private boolean fireSpread;
 	private boolean claimable;
 	private boolean explosions;
 	private boolean trample;
 	private int border;
 	private int fill;
 	private String spawnCoords;
 	private String spawnDirection;
 	private boolean changed;
 	private boolean loaded;
 	private boolean removed;
 	
 	public MyWorld(String name) {
 		this(name, "");
 	}
 	
 	public MyWorld(String name, String env) {
		this(name, env, "");
 	}
 	
 	public MyWorld(String name, String env, String type) {
		this(name, env, type, "");
 	}
 	
 	public MyWorld(String name, String env, String type, String seed) {
 		this.name = name.toLowerCase();
 		this.env = getEnvironment(env);
 		this.type = getWorldType(type);
 		this.seed = getSeed(seed);
 		this.spawn = null;
 		this.keepSpawn = true;
 		this.pvp = false;
 		this.monsters = true;
 		this.animals = true;
 		this.monsterGrief = false;
 		this.fireSpread = false;
 		this.claimable = false;
 		this.explosions = false;
 		this.trample = false;
 		this.border = 1000;
 		this.fill = 0;
 		this.changed = true;
 		this.loaded = true;
 		this.removed = false;
 	}
 	
 	public MyWorld(World world) {
 		this.name = world.getName();
 		this.env = world.getEnvironment();
 		this.type = world.getWorldType();
 		this.seed = world.getSeed();
 		this.spawn = world.getSpawnLocation();
 		this.keepSpawn = world.getKeepSpawnInMemory();
 		this.pvp = world.getPVP();
 		this.monsters = world.getAllowMonsters();
 		this.animals = world.getAllowAnimals();
 		this.monsterGrief = false;
 		this.fireSpread = false;
 		this.claimable = false;
 		this.explosions = false;
 		this.trample = false;
 		this.border = 1000;
 		this.fill = 0;
 		this.changed = true;
 		this.loaded = true;
 		this.removed = false;
 	}
 	
 	public MyWorld(String name, String env, String type, long seed, String coords, String direction, boolean keepSpawn, boolean pvp,
 			boolean monsters, boolean animals, boolean monsterGrief, boolean fireSpread, boolean claimable, boolean explosions, boolean trample, int border, int fill) {
 		this.name = name;
 		this.env = getEnvironment(env);
 		this.type = getWorldType(type);
 		this.seed = seed;
 		this.spawn = null;
 		this.keepSpawn = keepSpawn;
 		this.pvp = pvp;
 		this.monsters = monsters;
 		this.animals = animals;
 		this.monsterGrief = monsterGrief;
 		this.fireSpread = fireSpread;
 		this.claimable = claimable;
 		this.explosions = explosions;
 		this.trample = trample;
 		this.border = border;
 		this.fill = fill;
 		this.spawnCoords = coords;
 		this.spawnDirection = direction;
 		this.changed = false;
 		this.loaded = true;
 		this.removed = false;
 	}
 	
 	public static MyWorld getWorld(String name) {
 		if (worlds.get(name.toLowerCase()) != null) return worlds.get(name.toLowerCase());
 		for (String key : worlds.keySet()) if (key.startsWith(name.toLowerCase())) return worlds.get(key);
 		return null;
 	}
 	
 	public static MyWorld getWorld(World world) {
 		return worlds.get(world.getName());
 	}
 	
 	public static List<MyWorld> getWorlds() {
 		List<MyWorld> list = new ArrayList<MyWorld>();
 		for (String key : worlds.keySet()) {
 			list.add(worlds.get(key));
 		}
 		return list;
 	}
 	
 	public World getWorld() {
 		return Bukkit.getServer().getWorld(this.name);
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public void setEnv(Environment env) {
 		this.env = env;
 	}
 	
 	public Environment getEnv() {
 		return this.env;
 	}
 	
 	public void setType(WorldType type) {
 		this.type = type;
 	}
 	
 	public WorldType getType() {
 		return this.type;
 	}
 	
 	public void setSeed(long seed) {
 		this.seed = seed;
 	}
 	
 	public long getSeed() {
 		return this.seed;
 	}
 	
 	public void setSpawn(Location loc) {
 		this.spawn = loc;
 		setChanged(true);
 	}
 	
 	public Location getSpawn() {
 		return this.spawn;
 	}
 	
 	public void setKeepSpawn(boolean keepSpawn) {
 		this.keepSpawn = keepSpawn;
 		setChanged(true);
 	}
 	
 	public boolean getKeepSpawn() {
 		return this.keepSpawn;
 	}
 	
 	public void setPvp(boolean pvp) {
 		this.pvp = pvp;
 		setChanged(true);
 	}
 	
 	public boolean getPvp() {
 		return this.pvp;
 	}
 	
 	public void setMonsters(boolean monsters) {
 		this.monsters = monsters;
 		setChanged(true);
 	}
 	
 	public boolean getMonsters() {
 		return this.monsters;
 	}
 	
 	public void setAnimals(boolean animals) {
 		this.animals = animals;
 		setChanged(true);
 	}
 	
 	public boolean getAnimals() {
 		return this.animals;
 	}
 	
 	public void setMonsterGrief(boolean monsterGrief) {
 		this.monsterGrief = monsterGrief;
 		setChanged(true);
 	}
 	
 	public boolean getMonsterGrief() {
 		return this.monsterGrief;
 	}
 	
 	public void setFireSpread(boolean fireSpread) {
 		this.fireSpread = fireSpread;
 		setChanged(true);
 	}
 	
 	public boolean getFireSpread() {
 		return this.fireSpread;
 	}
 	
 	public void setClaimable(boolean claimable) {
 		this.claimable = claimable;
 		setChanged(true);
 	}
 	
 	public boolean getClaimable() {
 		return this.claimable;
 	}
 	
 	public void setExplosions(boolean explosions) {
 		this.explosions = explosions;
 		setChanged(true);
 	}
 	
 	public boolean getExplosions() {
 		return this.explosions;
 	}
 	
 	public void setTrample(boolean trample) {
 		this.trample = trample;
 		setChanged(true);
 	}
 	
 	public boolean getTrample() {
 		return this.trample;
 	}
 	
 	public void setBorder(int border) {
 		this.border = border;
 		setChanged(true);
 	}
 	
 	public int getBorder() {
 		return this.border;
 	}
 	
 	public boolean reachedBorder(Location loc) {
 		if (this.border < loc.getBlockX() || -this.border + 1 > loc.getBlockX()
 				|| this.border < loc.getBlockZ() || -this.border + 1 > loc.getBlockZ()) {
 			return true;
 		}
 		return false;
 	}
 	
 	public void setFill(int fill) {
 		this.fill = fill;
 		setChanged(true);
 	}
 	
 	public int getFill() {
 		return this.fill;
 	}
 	
 	public String getSpawnCoords() {
 		return this.spawnCoords;
 	}
 	
 	public String getSpawnDirection() {
 		return this.spawnDirection;
 	}
 	
 	public void setChanged(boolean changed) {
 		this.changed = changed;
 	}
 	
 	public boolean isChanged() {
 		return this.changed;
 	}
 	
 	public void setLoaded(boolean loaded) {
 		this.loaded = loaded;
 	}
 	
 	public boolean isLoaded() {
 		return this.loaded;
 	}
 	
 	public void setRemoved(boolean removed) {
 		this.removed = removed;
 	}
 	
 	public boolean isRemoved() {
 		return this.removed;
 	}
 	
 	public void remove() {
 		worlds.remove(this.name);
 	}
 	
 	public void save() {
 		worlds.put(name, this);
 	}
 	
 	private Environment getEnvironment(String envString) {
 		Environment env = Environment.NORMAL;
 		if (envString.equalsIgnoreCase("NETHER")) env = Environment.NETHER;
 		if (envString.equalsIgnoreCase("THE_END")) env = Environment.THE_END;
 		return env;
 	}
 	
 	private WorldType getWorldType(String typeString) {
 		WorldType type = WorldType.NORMAL;
 		if (typeString.equalsIgnoreCase("AMPLIFIED")) type = WorldType.AMPLIFIED;
 		if (typeString.equalsIgnoreCase("FLAT")) type = WorldType.FLAT;
 		if (typeString.equalsIgnoreCase("LARGE_BIOMES")) type = WorldType.LARGE_BIOMES;
 		if (typeString.equalsIgnoreCase("VERSION_1_1")) type = WorldType.VERSION_1_1;
 		return type;
 	}
 	
 	private long getSeed(String seedString) {
 		long seed;
 		if (seedString != "") {
 			try {
 				seed = Long.parseLong(seedString);
 			} catch (NumberFormatException e) {
 				seed = (long) seedString.hashCode();
 			}
 		} else {
 			seed = new Random().nextLong();
 		}
 		return seed;
 	}
 	
 	
 }
