 package com.zand.areaguard.area;
 
 public abstract class Cuboid extends IdData {
 	
 	public Cuboid(int id) {
 		super(id);
 	}
 	
 	/**
 	 * Gets the {@link Area} that this cubiod is a part of.
 	 * @return The Area that this cubiod belongs to. 
 	 */
 	public abstract Area getArea();
 	
 	/**
 	 * Gets who created the cuboid.
 	 * @return The name of the person who created the cuboid.
 	 */
 	public abstract String getCreator();
 	
 	/**
 	 * Gets the coords of the cubiod.
 	 * @return An Array of the cubiod's coords.
 	 */
 	public abstract int[] getCoords();
 	
 	/**
 	 * Gets the number of blocks in a cuboid.
 	 * @return The number of blocks.
 	 */
 	public long getBlockCount() {
 		int coords[] = getCoords();
 		return 
 		(coords[3]-coords[0]+1)*
 		(coords[4]-coords[1]+1)*
 		(coords[5]-coords[2]+1);
 	}
 	
 	/**
 	 * Sets weather the cuboid is active
 	 * @param active cuboid's new active status
 	 * @return True if success.
 	 */
 	public abstract boolean setActive(boolean active);
 	
 	/**
 	 * Gets the cuboids active status.
 	 * @return
 	 */
 	public abstract boolean isActive();
 	
 	/**
 	 * Gets the Priority for the cubiod.
 	 * @return The Priority
 	 */
 	public abstract int getPriority();
 	
 	/**
 	 * Gets the {@link World} that this cubiod is a part of.
 	 * @return The World that this cubiod belongs to. 
 	 */
 	public abstract World getWorld();
 
 	/**
 	 * Tests if a point is in the cubiod.
 	 * @param world The world that the point is in.
 	 * @param x The X vector.
 	 * @param y The Y vector.
 	 * @param z The Z vector.
 	 * @return If the point is in the cubiod
 	 */
 	public boolean pointInside(World world, int x, int y, int z) {
 		int coords[] = getCoords();
 		return ((coords[0] <= x && coords[3] >= x) &&
				(coords[1] <= y && coords[4] >= y) &&
				(coords[2] <= z && coords[5] >= z) &&
 				getWorld().getName().equals(world.getName()));
 	}
 	
 	/**
 	 * Sets the Priority for the cubiod.
 	 * @param priority The Priority to set it to.
 	 * @return If success.
 	 */
 	public abstract boolean setPriority(int priority);
 	
 	/**
 	 * Sets the {@link Area} that the cubiod is a part of.
 	 * @param priority The Priority to set it to.
 	 * @return If success.
 	 */
 	public abstract boolean setArea(Area area);
 	
 	/**
 	 * Sets the location for the cubiod.
 	 * @param world The world to set it to.
 	 * @param coords The coords to set it to.
 	 * @return If success.
 	 */
 	public abstract boolean setLocation(World world, int coords[]);
 	
 	/**
 	 * Deletes this cuboid.
 	 * @return True if success.
 	 */
 	public abstract boolean delete();
 }
