 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.logging.Logger;
 import java.util.Map;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.io.IOException;
 
 public class HeyRunesTeleport extends Plugin {
 	private static final String waypointsFile = "heyrunesteleport_waypoints.txt";
 	private static final String teleportersFile = "heyrunesteleport_teleporters.txt";
 	protected static final Logger log = Logger.getLogger("Minecraft");
 
 	private static final int waypointBlockId = 41;
 	private static final int teleporterBlockId = 42;
 	private static final int poolBlockId = 9;
 
 	NamedPointMap waypoints = new NamedPointMap();
 	PointMap teleporters = new PointMap();
 	public HeyRune teleporterRune;
 	public HeyRune waypointRune;
 
 	public void enable() { }
 	public void disable() { }
 
 	public void initialize() {
 		{	HModPluginListener l = new HModPluginListener(this);
 			etc.getLoader().addListener(PluginLoader.Hook.COMMAND, l, this, PluginListener.Priority.MEDIUM);
 			etc.getLoader().addListener(PluginLoader.Hook.BLOCK_PLACE, l, this, PluginListener.Priority.MEDIUM);
 			etc.getLoader().addListener(PluginLoader.Hook.BLOCK_BROKEN, l, this, PluginListener.Priority.MEDIUM);
 			etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, l, this, PluginListener.Priority.MEDIUM);
 		}
 
 		int OO;
 
 		OO = teleporterBlockId;
 		HeyRunes.addListener(teleporterRune = new HeyRune("Teleporter", new int[][] {
 			{ -1,-1,-1,-1,-1 },
 			{ -1,-1,OO,-1,-1 },
 			{ -1,OO,63,OO,-1 },
 			{ -1,-1,OO,-1,-1 },
 			{ -1,-1,-1,-1,-1 }
 		}), new TeleporterListener(this), this);
 
 		OO = waypointBlockId;
 		HeyRunes.addListener(waypointRune = new HeyRune("Waypoint", new int[][] {
 			{ -1,-1,-1,-1,-1 },
 			{ -1,-1,OO,-1,-1 },
 			{ -1,OO,63,OO,-1 },
 			{ -1,-1,OO,-1,-1 },
 			{ -1,-1,-1,-1,-1 }
 		}), new WaypointListener(this), this);
 
 		loadPoints(waypointsFile, waypoints);
 		loadPoints(teleportersFile, teleporters);
 		log.info("HeyRunesTeleport: HeyRunesTeleport: Loaded " + waypoints.size() + " waypoints...");
 		log.info("HeyRunesTeleport: HeyRunesTeleport: Loaded " + teleporters.size() + " teleporters...");
 	}
 
 	public void loadPoints(String filename, PointMap points) {
 		if (!new File(filename).exists()) return;
 		points.clear();
 		try {
 		BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
 		String line;
 		while ((line = reader.readLine()) != null) {
 			String[] l = line.split(":");
 			String name = l[0];
 			int bx,by,bz;
 			bx = Integer.parseInt(l[1]);
 			by = Integer.parseInt(l[2]);
 			bz = Integer.parseInt(l[3]);
 
 			points.add(new NamedPoint(name, new Coords(bx, by, bz)));
 		}
 		reader.close();
 		} catch (IOException e) { log.info("HeyRunesTeleport: Exception loading points: " + e); }
 	}
 
 	public void savePoints(String filename, PointMap points) {
 		try {
 		FileWriter writer = new FileWriter(filename);
 		NamedPoint[] nps = points.getAll();
 		for(int i=0;i<nps.length;i++) {
 			NamedPoint np = nps[i];
 			writer.write(np.name + ":" + np.location.X + ":" + np.location.Y + ":" + np.location.Z + "\r\n");
 		}
 		writer.close();
 		} catch (IOException e) { log.info("HeyRunesTeleport: Exception saving points: " + e); }
 	}
 
 	public void onTeleporterCreated(Player player, Block block) {
 		ComplexBlock b = etc.getServer().getComplexBlock(block.getX(),block.getY(),block.getZ());
 		if (!(b instanceof Sign)) return;
 		Sign s = (Sign)b;
 		String name = s.getText(0);
 
		if (!isValidName(name)) {
 			player.sendMessage("Invalid name!");
 			return;
 		}
 
 		Coords c = new Coords(block);
 		teleporters.add(new NamedPoint(name, c));
 		savePoints(teleportersFile, teleporters);
 
 		etc.getServer().setBlockAt(9,c.X,c.Y,c.Z);
 		player.sendMessage("Teleporter '" + name + "' created");
 	}
 
 	public void onWaypointCreated(Player player, Block block) {
 		ComplexBlock b = etc.getServer().getComplexBlock(block.getX(),block.getY(),block.getZ());
 		if (!(b instanceof Sign)) return;
 		Sign s = (Sign)b;
 		String name = s.getText(0);
 
		if (!isValidName(name)) {
 			player.sendMessage("Invalid name!");
 			return;
 		}
 
 		Coords c = new Coords(block);
 		waypoints.add(new NamedPoint(name, c));
 		savePoints(waypointsFile, waypoints);
 
 		etc.getServer().setBlockAt(9,c.X,c.Y,c.Z);
 
 		player.sendMessage("Waypoint '" + name + "' created");
 	}
 
 	public Boolean isValidName(String name) {
 		if (name.length() == 0) return false;
 		for(int i=0;i<name.length();i++) {
 			char c = name.charAt(i);
 			if (!(	(c >= 'a' && c <= 'z') ||
 				(c >= 'A' && c <= 'Z') ||
 				(c >= '0' && c <= '9')
 				))
 				return false;
 		}
 		return true;
 	}
 
 	public boolean onCommand(Player player, String[] split) {
 		if ("/waypoints".equals(split[0])) {
 			NamedPoint[] nps = waypoints.getAll();
 			player.sendMessage("Waypoints: " + nps.length);
 			for (int i=0;i<nps.length;i++) {
 				player.sendMessage(" " + nps[i].name);
 			}
 			return true;
 		} else if ("/teleporters".equals(split[0])) {
 			NamedPoint[] nps = teleporters.getAll();
 			player.sendMessage("Teleporters: " + nps.length);
 			for (int i=0;i<nps.length;i++) {
 				player.sendMessage(" " + nps[i].name + " " + nps[i].location.X + "," + nps[i].location.Y + "," + nps[i].location.Z);
 			}
 			return true;
 		}/* else if ("/position".equals(split[0])) {
 			player.sendMessage(player.getX() + "," + player.getY() + "," + player.getZ());
 		}*/
 		return false;
 	}
 
 	Coords tmpcoords = new Coords(0,0,0);
 	public void onPlayerMove(Player player, Location from, Location to) {
 		if (player == null) return;
 		tmpcoords.X = (int)to.x; tmpcoords.Y = (int)to.y; tmpcoords.Z = (int)to.z;
 		NamedPoint np;
 		if ((np = teleporters.get(tmpcoords)) != null) {
 			NamedPoint wp = waypoints.get(np.name);
 			if (wp == null) return;
 			player.teleportTo(wp.location.X+0.5f,wp.location.Y,wp.location.Z+0.5f,0,0);
 		}
 	}
 
 	public boolean onBlockPlace(Player player, Block block, Block blockClicked, Item itemInHand) {
 		tmpcoords.X = block.getX(); tmpcoords.Y = block.getY(); tmpcoords.Z = block.getZ();
 		NamedPoint np;
 		if ((np = waypoints.get(tmpcoords)) != null) {
 			waypoints.remove(np);
 			savePoints(waypointsFile, waypoints);
 			log.info("HeyRunesTeleport: Waypoint '" + np.name + "' destroyed.");
 		} else if ((np = teleporters.get(tmpcoords)) != null) {
 			teleporters.remove(np);
 			savePoints(teleportersFile, teleporters);
 			log.info("HeyRunesTeleport: Teleporter '" + np.name + "' destroyed.");
 		}
 		return false;
 	}
 
 	public NamedPoint findAround(PointMap ps, Coords c) {
 		NamedPoint r = null;
 		c.X--;
 		if ((r = ps.get(c)) != null) return r;
 		c.X++;c.X++;
 		if ((r = ps.get(c)) != null) return r;
 		c.X--;c.Z--;
 		if ((r = ps.get(c)) != null) return r;
 		c.Z++;c.Z++;
 		if ((r = ps.get(c)) != null) return r;
 		c.Z--;
 		return null;
 	}
 
 	public boolean onBlockBreak(Player player, Block block) {
 		int type = block.getType();
 		if (type == waypointBlockId) {
 			NamedPoint wp = findAround(waypoints,new Coords(block));
 			if (wp != null) {
 				waypoints.remove(wp);
 				etc.getServer().setBlockAt(0,wp.location.X,wp.location.Y,wp.location.Z);
 				etc.getServer().dropItem(wp.location.X+0.5,wp.location.Y+0.5,wp.location.Z+0.5,323);
 				log.info("HeyRunesTeleport: Waypoint '" + wp.name + "' destroyed.");
 			}
 		} else if (type == teleporterBlockId) {
 			NamedPoint tp = findAround(teleporters,new Coords(block));
 			if (tp != null) {
 				teleporters.remove(tp);
 				etc.getServer().setBlockAt(0,tp.location.X,tp.location.Y,tp.location.Z);
 				etc.getServer().dropItem(tp.location.X+0.5,tp.location.Y+0.5,tp.location.Z+0.5,323);
 				log.info("HeyRunesTeleport: Teleporter '" + tp.name + "' destroyed.");
 			}
 		} else if (type == poolBlockId) {
 			tmpcoords.X = block.getX(); tmpcoords.Y = block.getY(); tmpcoords.Z = block.getZ();
 			NamedPoint np;
 			if ((np = waypoints.get(tmpcoords)) != null) {
 				waypoints.remove(np);
 				savePoints(waypointsFile, waypoints);
 				log.info("HeyRunesTeleport: Waypoint '" + np.name + "' destroyed.");
 			} else if ((np = teleporters.get(tmpcoords)) != null) {
 				teleporters.remove(np);
 				savePoints(teleportersFile, teleporters);
 				log.info("HeyRunesTeleport: Teleporter '" + np.name + "' destroyed.");
 			}
 		}
 		return false;
 	}
 
 	public class TeleporterListener extends HeyRunesListener
 	{
 		HeyRunesTeleport hr;
 		public TeleporterListener(HeyRunesTeleport hr) { this.hr = hr; }
 		public void runeCreated(Player player, HeyRune rune, Block block) { hr.onTeleporterCreated(player, block); }
 	}
 
 	public class WaypointListener extends HeyRunesListener
 	{
 		HeyRunesTeleport hr;
 		public WaypointListener(HeyRunesTeleport hr) { this.hr = hr; }
 		public void runeCreated(Player player, HeyRune rune, Block block) { hr.onWaypointCreated(player, block); }
 	}
 
 	public class HModPluginListener extends PluginListener
 	{
 		HeyRunesTeleport hr;
 		public HModPluginListener(HeyRunesTeleport hr) { this.hr = hr; }
 		public boolean onCommand(Player player, java.lang.String[] split) { return hr.onCommand(player, split); }
 		public boolean onBlockPlace(Player player, Block blockPlaced, Block blockClicked, Item itemInHand) { return hr.onBlockPlace(player, blockPlaced, blockClicked, itemInHand); }
 		public boolean onBlockBreak(Player player, Block block) { return hr.onBlockBreak(player, block); }
 		public void onPlayerMove(Player player, Location from, Location to) { hr.onPlayerMove(player, from, to); }
 	}
 }
