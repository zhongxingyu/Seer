 package com.martinbrook.tesseractuhc.startpoint;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.martinbrook.tesseractuhc.UhcTeam;
 import com.martinbrook.tesseractuhc.util.MatchUtils;
 
 public abstract class UhcStartPoint {
 	protected Location location;
 	private UhcTeam team = null;
 	protected int number;
 	protected boolean hasChest = true;
 
 	public UhcStartPoint(int number, Location location, boolean hasChest) {
 		this.location=location;
 		this.number=number;
 		this.hasChest = hasChest;
 	}
 	
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public void setLocation(Location location) {
 		this.location = location;
 	}
 
 	public UhcTeam getTeam() {
 		return team;
 	}
 
 	public void setTeam(UhcTeam team) {
 		this.team = team;
 	}
 
 	public double getX() { return location.getX(); }
 	public double getY() { return location.getY(); }
 	public double getZ() { return location.getZ(); }
 
 	public int getNumber() {
 		return number;
 	}
 
 
 	
 	
 	/**
 	 * Build a starting trough at this start point, and puts a starter chest and sign there.
 	 *
 	 */
 	public abstract void buildStartingTrough();
 	public abstract Block getChestBlock();
 	public abstract Block getSignBlock();
 	
 	public boolean hasChest() {
 		return hasChest;
 	}
 	
 
 	/**
 	 * Empty the starting chest
 	 */
 	public void emptyChest() {
 		if (!hasChest) return;
 		this.fillChest(new ItemStack[27]);
 	}
 	
 	/**
 	 * Fill the starting chest with specified contents
 	 */
 	public void fillChest(ItemStack[] items) {
 		if (!hasChest) return;
 		Block b = getChestBlock();
 		if (b.getType() != Material.CHEST) b.setType(Material.CHEST);
 		
 		Inventory chest = ((Chest) b.getState()).getBlockInventory();
 		chest.setContents(items);
 	}
 	
 	protected void placeChest() {
 		if (!hasChest) return;
 		Block chestBlock = this.getChestBlock();
 		if (chestBlock.getType() != Material.CHEST) chestBlock.setType(Material.CHEST);
 	}
 	
 	/**
 	 * Make a default start sign
 	 */
 	public void makeSign() {
 		if (team == null) 
 			makeSign("Start #" + number);
 		else
 			makeSign(team.getName());
 	}
 	
 	/**
 	 * Make a start sign with specific text
 	 * 
 	 * @param text The text to write on the sign
 	 */
 	public void makeSign(String text) {
 		Block b = getSignBlock();
 		b.setType(Material.SIGN_POST);
 		
		Sign s = (Sign) b.getState();
 		String[] t = MatchUtils.signWrite(text);
 		
 		s.setLine(0, t[0]);
 		s.setLine(1, t[1]);
 		s.setLine(2, t[2]);
 		s.setLine(3, t[3]);
 
 		s.update();
 	}
 
 	
 }
