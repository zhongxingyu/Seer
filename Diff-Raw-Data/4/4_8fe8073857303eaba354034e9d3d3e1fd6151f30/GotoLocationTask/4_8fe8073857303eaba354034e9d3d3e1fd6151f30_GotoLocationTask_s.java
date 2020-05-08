 package com.adamki11s.quests.locations;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import com.adamki11s.exceptions.InvalidQuestException;
 import com.adamki11s.questx.QuestX;
 
 public class GotoLocationTask {
 
 	private final double x, y, z, range, rangeCheckVariation;
 	private final String world;
 
 	// marked as in chunk range
 	private boolean marked;
 
 	public GotoLocationTask(String quest, String data) throws InvalidQuestException {
 
 		String[] parts = data.split("#");
 
 		String[] locs = parts[0].split(",");
 
 		this.world = locs[0];
 
 		double lX = 0, lY = 0, lZ = 0;
 
 		try {
 			lX = Double.parseDouble(locs[1]);
 			lY = Double.parseDouble(locs[2]);
 			lZ = Double.parseDouble(locs[3]);
 			this.range = Integer.parseInt(parts[1]);
 		} catch (NumberFormatException nfe) {
 			throw new InvalidQuestException(data, "Invalid number for location or range was encountered.", quest);
 		}
 
 		World w = Bukkit.getServer().getWorld(world);
 
 		if (w == null) {
 			throw new InvalidQuestException(data, "World '" + world + "' could not be loaded. World name may be invalid or wolrd is not loaded", quest);
 		} else {
 			this.x = lX;
 			this.y = lY;
 			this.z = lZ;
 
 			this.rangeCheckVariation = (int) (Math.ceil((double) range / 16D) * 2);
 		}
 	}
 
 	public boolean isAtLocation(Location l) {
 		double tx = x + range, ty = y + range, tz = z + range, bx = x - range, by = y - range, bz = z - range;
 		double lx = l.getX(), ly = l.getY(), lz = l.getZ();
 
 		return (lx < tx && lx > bx && ly < ty && ly > by && lz < tz && lz > bz);
 	}
 
 	public boolean isInCheckRange(Location l) {
 		// distance check = range * 3, if distance between player and point is
 		// smaller check
 
 		if (!l.getWorld().getName().equalsIgnoreCase(this.world)) {
 			QuestX.logMSG("Not even in same world, do not check");
 			return false;
 		}
 
 		double simpleDist = this.getManhattanDistance(l.getBlockX(), l.getBlockY(), l.getBlockZ(), (int) x, (int) y, (int) z);
 
 		return (simpleDist < (range * 3));
 
 		/*
 		 * World w = Bukkit.getServer().getWorld(world); Chunk c =
 		 * w.getChunkAt((int)x, (int)z), target = l.getChunk(); double xDist =
 		 * this.abs(c.getX() - target.getX()) / 16D, zDist = this.abs(c.getZ() -
 		 * target.getZ()) / 16D; //start checking if within double the range
 		 * return (rangeCheckVariation > (xDist + zDist));
 		 */
 	}
 
 	private double getManhattanDistance(int sx, int sy, int sz, int ex, int ey, int ez) {
 		double dx = sx - ex, dy = sy - ey, dz = sz - ez;
 		dx = abs(dx);
 		dy = abs(dy);
 		dz = abs(dz);
 		return (dx + dy + dz);
 	}
 
 	private int markCheckCount = 0;
 
 	public boolean isMarked() {
		if (this.isMarked()) {
			markCheckCount++;
		}
 		if (markCheckCount > 10) {
 			// set chunk to be remarked in case the player has deviated from the
 			// search area
 			this.setMarked(false);
 			this.markCheckCount = 0;
 		}
 		return this.marked;
 	}
 
 	public void setMarked(boolean mark) {
 		this.marked = mark;
 	}
 
 	private double abs(double i) {
 		return (i < 0 ? -i : i);
 	}
 
 }
