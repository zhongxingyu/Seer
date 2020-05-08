 package org.rsbot.script.wrappers;
 
 /**
  * A class that handles flags of tiles.
  *
  * @author Timer
  */
 public class RSGameTile extends RSTile {
 	private int key;
 
 	public RSGameTile(final RSTile tile, final int key) {
 		super(tile.getX(), tile.getY(), tile.getZ());
 		this.key = key;
 	}
 
 	public static interface Flags {
 		public static final int WALL_NORTH_WEST = 0x1;
 		public static final int WALL_NORTH = 0x2;
 		public static final int WALL_NORTH_EAST = 0x4;
 		public static final int WALL_EAST = 0x8;
 		public static final int WALL_SOUTH_EAST = 0x10;
 		public static final int WALL_SOUTH = 0x20;
 		public static final int WALL_SOUTH_WEST = 0x40;
 		public static final int WALL_WEST = 0x80;
 		public static final int BLOCKED = 0x100;
 		public static final int WATER = 0x1280100;
 	}
 
 	public boolean questionable() {
 		return (key & Flags.WATER) == 0 && (key & Flags.BLOCKED) == 0;
 	}
 
 	public boolean walkable() {
		return (key & Flags.WALL_NORTH_WEST | Flags.WALL_NORTH | Flags.WALL_NORTH_EAST | Flags.WALL_EAST |
 				Flags.WALL_SOUTH_EAST | Flags.WALL_SOUTH | Flags.WALL_SOUTH_WEST | Flags.WALL_WEST | Flags.BLOCKED |
				Flags.WATER) == 0;
 	}
 
 	public boolean special() {
 		return (key & Flags.BLOCKED) == 0 && (key & Flags.WATER) != 0;
 	}
 
 	@Override
 	public String toString() {
 		return getX() + "," + getY() + "," + getZ() + "k" + key;
 	}
 
 	public int key() {
 		return key;
 	}
 
 	@Override
 	public boolean equals(final Object o) {
 		if (o instanceof RSGameTile) {
 			RSGameTile gameTile = (RSGameTile) o;
 			return gameTile.getX() == getX() && gameTile.getY() == getY() && gameTile.getZ() == getZ() && gameTile.key() == key;
 		} else if (o instanceof RSTile) {
 			RSTile rsTile = (RSTile) o;
 			return rsTile.getX() == getX() && rsTile.getY() == getY() && rsTile.getZ() == getZ();
 		}
 		return false;
 	}
 }
