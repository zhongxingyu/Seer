 package de.philworld.bukkit.magicsigns.config;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
 import de.philworld.bukkit.magicsigns.locks.Lock;
 import de.philworld.bukkit.magicsigns.locks.PlayerLock;
 import de.philworld.bukkit.magicsigns.signs.MagicSign;
 
 /**
  * Proxy for serializing a MagicSign.
  */
 public class MagicSignSerializationProxy implements ConfigurationSerializable {
 
 	private final World world;
 	private final int x;
 	private final int y;
 	private final int z;
 	private final String type;
 	private final String[] lines;
 	private final Lock lock;
 	private final Map<String, PlayerLock> playerLocks;
 
 	public MagicSignSerializationProxy(MagicSign magicSign) {
 		world = magicSign.getLocation().getWorld();
 		x = magicSign.getLocation().getBlockX();
 		y = magicSign.getLocation().getBlockY();
 		z = magicSign.getLocation().getBlockZ();
 		type = magicSign.getClass().getName();
 		lines = magicSign.isMasked() ? magicSign.getLines() : null;
 
 		lock = magicSign.getLock();
 
 		playerLocks = magicSign.getPlayerLocks();
 		// remove obsolete playerLocks
 		long time = System.currentTimeMillis() / 1000;
 		if (playerLocks != null) {
 			for (Entry<String, PlayerLock> entry : playerLocks.entrySet()) {
 				if (entry.getValue().isObsolete(time))
 					playerLocks.remove(entry.getKey());
 			}
 		}
 	}
 
 	public MagicSignSerializationProxy(Map<String, Object> map) {
 		world = Bukkit.getServer().getWorld((String) map.get("world"));
 		x = (Integer) map.get("x");
 		y = (Integer) map.get("y");
 		z = (Integer) map.get("z");
 		type = (String) map.get("type");
 
 		if (map.get("lines") != null) {
 			@SuppressWarnings("unchecked")
 			List<String> linesList = (List<String>) map.get("lines");
 			Object[] linesAsObj = linesList.toArray();
 			lines = Arrays
 					.copyOf(linesAsObj, linesAsObj.length, String[].class);
 		} else {
 			lines = null;
 		}
 
 		lock = map.containsKey("lock") ? (Lock) map.get("lock") : null;
 
 		if (map.get("playerLocks") != null) {
 			@SuppressWarnings("unchecked")
 			Map<String, Map<String, Object>> playerLocksObj = (Map<String, Map<String, Object>>) map
 					.get("playerLocks");
 
 			playerLocks = new HashMap<String, PlayerLock>();
 			for (Entry<String, Map<String, Object>> entry : playerLocksObj
 					.entrySet()) {
 				String playername = entry.getKey();
 				Map<String, Object> playerLockData = entry.getValue();
 				playerLocks.put(playername,
 						PlayerLock.valueOf(playerLockData, lock));
 			}
 		} else {
 			playerLocks = null;
 		}
 	}
 
 	@Override
 	public Map<String, Object> serialize() {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("world", world.getName());
 		map.put("x", x);
 		map.put("y", y);
 		map.put("z", z);
 		map.put("type", type);
 
 		if (lines != null)
 			map.put("lines", Arrays.asList(lines));
 
 		if (lock != null)
 			map.put("lock", lock);
 
 		if (playerLocks != null) {
 			Map<String, Object> serialized = new HashMap<String, Object>();
 			for (Entry<String, PlayerLock> entry : playerLocks.entrySet()) {
 				serialized.put(entry.getKey(), entry.getValue().serialize());
 			}
 			map.put("playerLocks", serialized);
 		}
 
 		return map;
 	}
 
 	/**
 	 * Get the Magic Sign behind this proxy.
 	 */
 	public MagicSign getMagicSign() throws InvalidConfigException,
 			InstantiationException, IllegalAccessException,
 			IllegalArgumentException, InvocationTargetException,
 			NoSuchMethodException, SecurityException, ClassNotFoundException {
 		Location loc = new Location(world, x, y, z);
 		Block block = world.getBlockAt(loc);
 
 		if (block.getState() instanceof Sign) {
 
 			String[] lines;
 			if (this.lines == null) {
 				Sign sign = (Sign) block.getState();
 				lines = sign.getLines();
 			} else {
 				lines = this.lines;
 			}
 
 			MagicSign magicSign = (MagicSign) Class.forName(type)
					.getConstructor(Block.class, String[].class)
					.newInstance(block, lines);
 
 			if (lock != null)
 				magicSign.setLock(lock, false);
 
 			if (playerLocks != null)
 				magicSign.setPlayerLocks(playerLocks);
 
 			return magicSign;
 		}
 		throw new InvalidConfigException("No sign found at coordinates: X("
 				+ block.getLocation().getBlockX() + ") Y("
 				+ block.getLocation().getBlockY() + ") Z("
 				+ block.getLocation().getBlockZ() + ") in world '"
 				+ block.getLocation().getWorld().getName()
 				+ "' ! Ignoring the sign!");
 	}
 
 }
