 package fr.crafter.tickleman.realstats;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.logging.Level;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Blaze;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.CaveSpider;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.MushroomCow;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Silverfish;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Squid;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.entity.Villager;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 
 //##################################################################################### PlayerStats
 public class RealPlayerStats
 {
 
 	private String playerName;
 
 	public static final int BREAK       = 1;
 	public static final int CUT         = 2;
 	public static final int FEED        = 3;
 	public static final int HIT         = 4;
 	public static final int KILL        = 5;
 	public static final int LEFT_CLICK  = 6;
 	public static final int MOVING      = 7;
 	public static final int PLACE       = 8;
 	public static final int RIGHT_CLICK = 9;
 	public static final int TAME        = 10;
 
 	public static final int MOVING_BOAT   = 1;
 	public static final int MOVING_CART   = 2;
 	public static final int MOVING_SNEAK  = 3;
 	public static final int MOVING_SPRINT = 4;
 	public static final int MOVING_WALK   = 5;
 
 	public static final int CREATURES_COUNT = 23;
 	public static final int VEHICLES_COUNT  = 6;
 
 	private boolean mustSave = false;
 	private long[] breakBlocks;
 	private long[] cutWool;
 	private long[] feedCreatures;
 	private long[] hitCreatures;
 	private long[] killCreatures;
 	private long[] leftClickBlocks;
 	private long[] movingDistances;
 	private long[] placeBlocks;
 	private long[] rightClickBlocks;
 	private long[] tameCreatures;
 
 	//----------------------------------------------------------------------------------- PlayerStats
 	public RealPlayerStats(RealStatsPlugin plugin, String playerName)
 	{
 		this.playerName  = playerName;
 		breakBlocks      = new long[net.minecraft.server.Block.byId.length + 1];
 		cutWool          = new long[CREATURES_COUNT];
 		feedCreatures    = new long[CREATURES_COUNT];
 		hitCreatures     = new long[CREATURES_COUNT];
 		killCreatures    = new long[CREATURES_COUNT];
 		leftClickBlocks  = new long[net.minecraft.server.Block.byId.length + 1];
 		movingDistances  = new long[VEHICLES_COUNT];
 		placeBlocks      = new long[net.minecraft.server.Block.byId.length + 1];
 		rightClickBlocks = new long[net.minecraft.server.Block.byId.length + 1];
 		tameCreatures    = new long[CREATURES_COUNT];
 		load(plugin);
 	}
 
 	//------------------------------------------------------------------------------ actionFromString
 	public static int actionFromString(String string)
 	{
 		if (string.equals("break"))      return BREAK;
 		if (string.equals("cut"))        return CUT;
 		if (string.equals("feed"))       return FEED;
 		if (string.equals("hit"))        return HIT;
 		if (string.equals("kill"))       return KILL;
 		if (string.equals("leftclick"))  return LEFT_CLICK;
 		if (string.equals("moving"))     return MOVING;
 		if (string.equals("place"))      return PLACE;
 		if (string.equals("rightclick")) return RIGHT_CLICK;
 		if (string.equals("tame"))       return TAME;
 		return 0;
 	}
 
 	//-------------------------------------------------------------------------------------- autoSave
 	public void autoSave(RealStatsPlugin plugin)
 	{
 		if (mustSave) {
 			save(plugin);
 			mustSave = false;
 		}
 	}
 
 	//--------------------------------------------------------------------------------- getCreatureId
 	public int getCreatureId(Entity entity)
 	{
 		if (entity instanceof Chicken)     return 1;
 		if (entity instanceof Cow)         return 2;
 		if (entity instanceof Pig)         return 3;
 		if (entity instanceof Sheep)       return 4;
 		if (entity instanceof Wolf)        return 5;
 
 		if (entity instanceof Blaze)       return 6;
 		if (entity instanceof CaveSpider)  return 7;
 		if (entity instanceof Creeper)     return 8;
 		if (entity instanceof Enderman)    return 9;
 		if (entity instanceof Ghast)       return 10;
 		if (entity instanceof MagmaCube)   return 11;
 		if (entity instanceof MushroomCow) return 12;
 		if (entity instanceof PigZombie)   return 13;
 		if (entity instanceof Silverfish)  return 14;
 		if (entity instanceof Skeleton)    return 15;
 		if (entity instanceof Slime)       return 16;
 		if (entity instanceof Spider)      return 17;
 		if (entity instanceof Squid)       return 18;
 		if (entity instanceof Zombie)      return 19;
 
 		if (entity instanceof Villager)    return 20;
 		if (entity instanceof EnderDragon) return 21;
 
 		if (entity instanceof Player)      return 22;
 
 		System.out.println("[SEVERE] RealStats unknown creature class " + entity.getClass().getSimpleName());
 
 		return 0;
 	}
 
 	//---------------------------------------------------------------------------------- getVehicleId
 	public int getVehicleId(Entity vehicle)
 	{
 		if (vehicle instanceof Boat)     return MOVING_BOAT;
 		if (vehicle instanceof Minecart) return MOVING_CART;
 		return 0;
 	}
 
 	//----------------------------------------------------------------------------------------- getXp
 	public long getXp(int action, int typeId)
 	{
 		try {
 			switch (action) {
 				case BREAK:       return breakBlocks     [typeId];
 				case CUT:         return cutWool         [typeId];
 				case FEED:        return feedCreatures   [typeId];
 				case HIT:         return hitCreatures    [typeId];
 				case KILL:        return killCreatures   [typeId];
 				case LEFT_CLICK:  return leftClickBlocks [typeId];
 				case MOVING:      return movingDistances [typeId];
 				case PLACE:       return placeBlocks     [typeId];
 				case RIGHT_CLICK: return rightClickBlocks[typeId];
 				case TAME:        return tameCreatures   [typeId];
 			}
 		} catch (ArrayIndexOutOfBoundsException e) {
 		}
 		return 0;
 	}
 
 	//------------------------------------------------------------------------------------- increment
 	public void increment(int action, Block block)
 	{
 		switch (action) {
			case BREAK:       breakBlocks      [block.getTypeId()] ++; break;
			case LEFT_CLICK:  leftClickBlocks  [block.getTypeId()] ++; break;
			case PLACE:       placeBlocks      [block.getTypeId()] ++; break;
			case RIGHT_CLICK: rightClickBlocks [block.getTypeId()] ++; break;
 		}
 		mustSave = true;
 	}
 
 	//------------------------------------------------------------------------------------- increment
 	public void increment(int action, Entity entity)
 	{
 		switch (action) {
 			case CUT:  cutWool      [getCreatureId(entity)] ++; break;
 			case FEED: feedCreatures[getCreatureId(entity)] ++; break;
 			case HIT:  hitCreatures [getCreatureId(entity)] ++; break;
 			case KILL: killCreatures[getCreatureId(entity)] ++; break;
 			case TAME: tameCreatures[getCreatureId(entity)] ++; break;
 		}
 		mustSave = true;
 	}
 
 	//------------------------------------------------------------------------------------- increment
 	public void increment(int action, Vehicle vehicle, long duration)
 	{
 		if (action == MOVING) movingDistances[getVehicleId(vehicle)] += duration;
 		mustSave = true;
 	}
 
 	//------------------------------------------------------------------------------------- increment
 	public void increment(int action, int typeId, long duration)
 	{
 		if (action == MOVING) movingDistances[typeId] += duration;
 		mustSave = true;
 	}
 
 	//------------------------------------------------------------------------------------------ load
 	public void load(RealStatsPlugin plugin)
 	{
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(
 				plugin.getDataFolder() + "/" + playerName + ".txt")
 			);
 			try {
 				String buffer;
 				String[] list;
 				while ((buffer = reader.readLine()) != null) {
 					int equalPos = buffer.indexOf('=');
 					if (equalPos > 0) {
 						String key = buffer.substring(0, equalPos);
 						buffer = buffer.substring(key.length() + 1);
 						list = buffer.split(";");
 						loadLongList(key, list, "break",      breakBlocks);
 						loadLongList(key, list, "cut",        cutWool);
 						loadLongList(key, list, "feed",       feedCreatures);
 						loadLongList(key, list, "hit",        hitCreatures);
 						loadLongList(key, list, "kill",       killCreatures);
 						loadLongList(key, list, "leftclick",  leftClickBlocks);
 						loadLongList(key, list, "moving",     movingDistances);
 						loadLongList(key, list, "place",      placeBlocks);
 						loadLongList(key, list, "rightclick", rightClickBlocks);
 						loadLongList(key, list, "tame",       tameCreatures);
 					}
 				}
 			} catch (IOException e) {
 				plugin.log(
 					Level.SEVERE, "Could not load " + plugin.getDataFolder() + "/" + playerName + ".txt"
 				);
 				System.out.print("[SEVERE]" + e.getMessage());
 				e.printStackTrace(System.out);
 			}
 			reader.close();
 		} catch (IOException e) {
 			plugin.log(
 				Level.INFO, "Write default " + plugin.getDataFolder() + "/" + playerName + ".txt file"
 			);
 			save(plugin);
 		}
 	}
 
 	//---------------------------------------------------------------------------------- loadLongList
 	public void loadLongList(String key, String[] readList, String listName, long[] intList)
 	{
 		if (key.equals(listName)) {
 			for (int i = 0; i < Math.min(intList.length, readList.length); i++) {
 				intList[i] = Long.parseLong(readList[i]);
 			}
 		}
 	}
 
 	//------------------------------------------------------------------------------------------ save
 	public void save(RealStatsPlugin plugin)
 	{
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(
 				plugin.getDataFolder() + "/" + playerName + ".txt"
 			));
 			try {
 				saveLongList(writer, "break",      breakBlocks);
 				saveLongList(writer, "cut",        cutWool);
 				saveLongList(writer, "feed",       feedCreatures);
 				saveLongList(writer, "hit",        hitCreatures);
 				saveLongList(writer, "kill",       killCreatures);
 				saveLongList(writer, "leftclick",  leftClickBlocks);
 				saveLongList(writer, "moving",     movingDistances);
 				saveLongList(writer, "place",      placeBlocks);
 				saveLongList(writer, "rightclick", rightClickBlocks);
 				saveLongList(writer, "tame",       tameCreatures);
 			} catch (IOException e) {
 				System.out.print("[SEVERE]" + e.getMessage());
 				e.printStackTrace(System.out);
 			}
 			writer.close();
 		} catch (IOException e) {
 			plugin.log(
 				Level.SEVERE, "Could not save " + plugin.getDataFolder() + "/" + playerName + ".txt"
 			);
 			plugin.log(Level.SEVERE, e.getMessage());
 			e.printStackTrace(System.out);
 		}
 	}
 
 	//---------------------------------------------------------------------------------- saveLongList
 	private void saveLongList(BufferedWriter writer, String listName, long[] list) throws IOException
 	{
 		String buffer = "";
 		for (int i = 0; i < list.length; i++) {
 			buffer = buffer + ";" + list[i];
 		}
 		writer.write(listName + "=" + buffer.substring(1) + "\n");
 	}
 
 }
