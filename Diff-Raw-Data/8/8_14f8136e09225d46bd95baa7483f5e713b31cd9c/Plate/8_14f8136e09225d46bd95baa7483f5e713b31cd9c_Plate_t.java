 package com.yurijware.bukkit.deadlyplates;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 import com.avaje.ebean.validation.NotEmpty;
 import com.avaje.ebean.validation.NotNull;
 
 /**
  * Class to store plates
  * @author Yurij
  */
 @Entity()
 @Table(name = "DeadlyPlates")
 public class Plate{
 	
 	@Id
 	private int id; 
 	@NotNull
 	private int x;
 	@NotNull
 	private int y;
 	@NotNull
 	private int z;
 	@NotNull
 	private int damage = 0;
 	@NotEmpty
 	private String world;
 	@NotEmpty
 	private String player;
 	
 	/**
 	 * Default constructor
 	 */
 	public Plate() { }
 	
 	/**
 	 * Create a plate with default damage
 	 * @param block The block with the plate
 	 * @param player The plates owner/creator
 	 */
 	public Plate(Block block, String player){
 		this.x = block.getX();
 		this.y = block.getY();
 		this.z = block.getZ();
 		this.world = block.getWorld().getName();
 		this.player = player;
 	}
 	
 	/**
 	 * Create a plate with custom damage
 	 * @param block The block with the plate
 	 * @param player The plates owner/creator
 	 */
 	public Plate(Block block, String player, int damage){
 		this.x = block.getX();
 		this.y = block.getY();
 		this.z = block.getZ();
 		this.damage = damage;
 		this.world = block.getWorld().getName();
 		this.player = player;
 	}
 	
 	/**
 	 * Get the id of the plate
 	 * @return
 	 */
 	public int getId() {
 		return this.id;
 	}
 	
 	/**
 	 * Set the id of the plate
 	 * @param id
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	/**
 	 * Get the x coordinate
 	 * @return
 	 */
 	public int getX() {
 		return this.x;
 	}
 	
 	/**
 	 * Set the x coordinate
 	 * @param x
 	 */
 	public void setX(int x) {
 		this.x = x;
 	}
 	
 	/**
 	 * Get the y coordinate
 	 * @return
 	 */
 	public int getY() {
 		return this.y;
 	}
 	
 	/**
 	 * Set the y coordinate
 	 * @param y
 	 */
 	public void setY(int y) {
 		this.y = y;
 	}
 	
 	/**
 	 * Get the z coordinate
 	 * @return
 	 */
 	public int getZ() {
 		return this.z;
 	}
 	
 	/**
 	 * Set the z coordinate
 	 * @param z
 	 */
 	public void setZ(int z) {
 		this.z = z;
 	}
 	
 	/**
 	 * Get the damage
 	 * @return
 	 */
 	public int getDamage() {
 		return damage;
 	}
 	
 	/**
 	 * Set the damage
 	 * @param damage
 	 */
 	public void setDamage(int damage) {
 		this.damage = damage;
 	}
 	
 	/**
 	 * Get the world
 	 * @return The worlds name 
 	 */
 	public String getWorld() {
 		return this.world;
 	}
 	
 	/**
 	 * Set the world
 	 * @param world The worlds name
 	 */
 	public void setWorld(String world) {
 		this.world = world;
 	}
 	
 	/**
 	 * Get the owner
 	 * @return
 	 */
 	public String getPlayer() {
 		return this.player;
 	}
 	
 	/**
 	 * Set the owner
 	 * @param player
 	 */
 	public void setPlayer(String player) {
 		this.player = player;
 	}
 	
 	/**
 	 * Get the location
 	 * @return
 	 */
 	public Location getLocation() {
 		World w = DeadlyPlates.getInstance().getServer().getWorld(world);
         return new Location(w, x, y, z);
 	}
 	
 	/**
 	 * Set the location
 	 * @param location
 	 */
 	public void setLocation(Location location) {
         this.world = location.getWorld().getName();
         this.x = location.getBlockX();
         this.y = location.getBlockY();
         this.z = location.getBlockZ();
     }
 	
 	/**
 	 * Get the block
 	 * @return
 	 */
 	public Block getBlock() {
 		return Bukkit.getServer().getWorld(world).getBlockAt(x, y, z);
 	}
 	
 	/**
 	 * Check if the plate is deadly
 	 * @param block The block to check
 	 * @return The plate if it is deadly, otherwise null
 	 * @see Plate
 	 */
 	public static Plate getPlateIfDeadly(Block block) {
 		Material m = block.getType();
 		if (m != Material.WOOD_PLATE && m != Material.STONE_PLATE) { return null; }
 		
		String w = block.getWorld().getName();
 		String x = block.getX() + "";
 		String y = block.getY() + "";
 		String z = block.getZ() + "";
		return DeadlyPlates.getInstance().getDatabase().find(Plate.class).
				where().ieq("world", w).ieq("x", x).ieq("y", y).ieq("z", z).findUnique();
 	}
 	
 }
