 package me.asofold.bukkit.pic.core;
 
 /**
  * Extends the cube related coordinates by convenience measures to check faster if players are in range.
  * @author mc_dev
  *
  */
 public final class Cube extends CubePos{
 	
 	public final int size;
 	
 	// Middle in real coordinates.
 	private final int mX;
 	private final int mY;
 	private final int mZ;
 	
 	
 	/**
 	 * Real coordinates !
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param size
 	 */
 	public Cube(final int x, final int y, final int z, final int size){
 		super(x / size, y / size, z / size);
 		
 		this.size = size;
 		
 		// Real coordinates for the middle of the cube:
 		final int sz2 = size / 2;
		this.mX = this.x * size + sz2;
		this.mY = this.y * size + sz2;
		this.mZ = this.z * size + sz2;
 	}
 	
 	/**
 	 * Check if the given real position is within the given distance of the cube center.
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param dist
 	 * @return
 	 */
 	public final boolean inRange(final int x, final int y, final int z, final int dist){
 		return Math.abs(mX - x) < dist && Math.abs(mY - y) < dist && Math.abs(mZ - z) < dist;
 	}
 	
 }
