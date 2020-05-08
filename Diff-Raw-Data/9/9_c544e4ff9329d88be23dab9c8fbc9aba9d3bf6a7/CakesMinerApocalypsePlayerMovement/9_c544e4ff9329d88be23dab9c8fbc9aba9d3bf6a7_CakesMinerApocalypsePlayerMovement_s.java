 package me.cakenggt.CakesMinerApocalypse;
 
 import java.util.Date;
 import java.util.List;
 
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World.Environment;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 
 public class CakesMinerApocalypsePlayerMovement implements Listener {
 	CakesMinerApocalypse p;
 	public CakesMinerApocalypsePlayerMovement(CakesMinerApocalypse plugin) {
 		p = plugin;
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerMove(PlayerMoveEvent event) {
 		if (!this.p.getOn().get(event.getPlayer().getWorld())){
 			return;
 		}
 		Location to = event.getTo();
 		//try {
 		//	size = mapSize();
 		//} catch (IOException e) {
 		//	e.printStackTrace();
 		//}
 		int size = this.p.getSize();
 		double halfSize = size /2;
 		double eigthSize = halfSize/8;
 		if (to.getWorld().getEnvironment() == Environment.NETHER){
 			if (to.getX() < -1 * eigthSize) {
 				to = to.add(size, 0, 0);
 				to.getWorld().loadChunk((int) to.getX(), (int) to.getZ(), true);
 				to.setY(to.getWorld().getHighestBlockAt(to).getY());
 				event.getPlayer().teleport(to);
 			}
 			if (to.getX() > eigthSize) {
 				to = to.add(-1 * size, 0, 0);
 				to.getWorld().loadChunk((int) to.getX(), (int) to.getZ(), true);
 				to.setY(to.getWorld().getHighestBlockAt(to).getY());
 				event.getPlayer().teleport(to);
 			}
 			if (to.getZ() < -1 * eigthSize) {
 				to = to.add(0, 0, size);
 				to.getWorld().loadChunk((int) to.getX(), (int) to.getZ(), true);
 				to.setY(to.getWorld().getHighestBlockAt(to).getY());
 				event.getPlayer().teleport(to);
 			}
 			if (to.getZ() > eigthSize) {
 				to = to.add(0, 0, -1 * size);
 				to.getWorld().loadChunk((int) to.getX(), (int) to.getZ(), true);
 				to.setY(to.getWorld().getHighestBlockAt(to).getY());
 				event.getPlayer().teleport(to);
 			}
 		}
 		else {
 			if (to.getX() < -1 * halfSize) {
 				to = to.add(size, 0, 0);
 				to.getWorld().loadChunk((int) to.getX(), (int) to.getZ(), true);
 				to.setY(to.getWorld().getHighestBlockAt(to).getY());
 				event.getPlayer().teleport(to);
 			}
 			if (to.getX() > halfSize) {
 				to = to.add(-1 * size, 0, 0);
 				to.getWorld().loadChunk((int) to.getX(), (int) to.getZ(), true);
 				to.setY(to.getWorld().getHighestBlockAt(to).getY());
 				event.getPlayer().teleport(to);
 			}
 			if (to.getZ() < -1 * halfSize) {
 				to = to.add(0, 0, size);
 				to.getWorld().loadChunk((int) to.getX(), (int) to.getZ(), true);
 				to.setY(to.getWorld().getHighestBlockAt(to).getY());
 				event.getPlayer().teleport(to);
 			}
 			if (to.getZ() > halfSize) {
 				to = to.add(0, 0, -1 * size);
 				to.getWorld().loadChunk((int) to.getX(), (int) to.getZ(), true);
 				to.setY(to.getWorld().getHighestBlockAt(to).getY());
 				event.getPlayer().teleport(to);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void apocalypseDamage(PlayerMoveEvent event) {
 		if (!this.p.getApocalypseDamage())
 			return;
 		Player player = event.getPlayer();
 		ItemStack[] inventoryArray = player.getInventory().getContents();
 		Location loc = player.getLocation();
 		List<Location> craters = this.p.getCraters();
 		List<Location> GECKs = this.p.getGECKs();
 		List<Long> craterTimes = this.p.getCraterTimes();
 		double damageChance = 0;
 		double geck = 0;
 		if ((loc.getBlock().getType() == Material.WATER || loc.getBlock().getType() == Material.STATIONARY_WATER) && !player.isInsideVehicle()){
 		    //System.out.println("is in water");
             if (this.p.getConfig().getBoolean("apocalypseDamageWater", true)) {
                 damageChance += .05;
             }
 		}
 		if (loc.getWorld().getHighestBlockYAt(loc) <= loc.getY() && (loc.getWorld().hasStorm() || loc.getWorld().isThundering())){
             if (this.p.getConfig().getBoolean("apocalypseDamageAcidRain", true)) {
                 damageChance += .05;
             }
 		}
 		java.util.Date now = new Date();
 		if (craters != null) {
 			for (Location crater : craters) {
 				if (crater.getWorld() == loc.getWorld()) {
 					//damageChance += 1/(crater.distanceSquared(loc)/50);
 					//double dam = 1/(crater.distanceSquared(loc)/50);
 					//System.out.println("distance " + crater.distance(loc));
 					//System.out.println("normal damage " + dam);
 					int index = craters.lastIndexOf(crater);
 					Long history = now.getTime()- craterTimes.get(index);
 					//System.out.println("age " + history.doubleValue()/(1000*60*60));
 					damageChance += (1000/ (crater.distanceSquared(loc) / 50))* (1/ (Math.pow(10, (Math.log10(history.doubleValue()/(1000*60*60)) / Math.log10(7)))));
 					//System.out.println("damageChance " + damageChance);
 				}
 			}
 		}
 		if (GECKs != null){
 			for (Location GECK : GECKs){
 				Block block = GECK.getBlock();
 				if (block.isBlockIndirectlyPowered() && block.getRelative(BlockFace.NORTH).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.SOUTH).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.EAST).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.WEST).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.NORTH).isBlockIndirectlyPowered() && block.getRelative(BlockFace.SOUTH).isBlockIndirectlyPowered() && block.getRelative(BlockFace.EAST).isBlockIndirectlyPowered() && block.getRelative(BlockFace.WEST).isBlockIndirectlyPowered()){
 					if (GECK.getWorld() == loc.getWorld())
 						geck += 1/(GECK.distanceSquared(loc)/50);
 				}
 			}
 		}
 		if (!(loc.getWorld().getHighestBlockYAt(loc) <= loc.getY()) && (loc.getBlock().getType() != Material.WATER && loc.getBlock().getType() != Material.STATIONARY_WATER)){
 			//System.out.println("under a block and not in water");
 			damageChance = 0;
 		}
 		for (ItemStack a : inventoryArray){
 			if (a != null){
 				if (a.getType() == Material.SNOW){
 					damageChance += .05 * a.getAmount();
 				}
 			}
 		}
 		//player.sendMessage("damage Chance = " + (damageChance - geck));
 		if (Math.random() <= damageChance - geck){
 			PlayerInventory inventory = player.getInventory();
 			ItemStack[] armor = inventory.getArmorContents();
 			if (player.getItemInHand().getType() == Material.COMPASS){
 				player.playEffect(loc, Effect.CLICK1, 0);
 				//System.out.println("tick");
 			}
 			//player.sendMessage("armor length = " + armor.length);
 			int whichArmor = (int)(Math.random() * armor.length);
 			if (armor[whichArmor].getAmount() == 0){
 				player.damage(1);
 			}
 			else
 				armor[whichArmor].setDurability((short) ((short) armor[whichArmor].getDurability() + 2));
 			if (armor[whichArmor].getType() == Material.LEATHER_HELMET && armor[whichArmor].getDurability() > 56)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.LEATHER_CHESTPLATE && armor[whichArmor].getDurability() > 81)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.LEATHER_LEGGINGS && armor[whichArmor].getDurability() > 76)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.LEATHER_BOOTS && armor[whichArmor].getDurability() > 66)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.GOLD_HELMET && armor[whichArmor].getDurability() > 78)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.GOLD_CHESTPLATE && armor[whichArmor].getDurability() > 113)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.GOLD_LEGGINGS && armor[whichArmor].getDurability() > 106)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.GOLD_BOOTS && armor[whichArmor].getDurability() > 92)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.IRON_HELMET && armor[whichArmor].getDurability() > 166)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.IRON_CHESTPLATE && armor[whichArmor].getDurability() > 241)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.IRON_LEGGINGS && armor[whichArmor].getDurability() > 226)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.IRON_BOOTS && armor[whichArmor].getDurability() > 196)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.CHAINMAIL_HELMET && armor[whichArmor].getDurability() > 166)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.CHAINMAIL_CHESTPLATE && armor[whichArmor].getDurability() > 241)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.CHAINMAIL_LEGGINGS && armor[whichArmor].getDurability() > 226)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.CHAINMAIL_BOOTS && armor[whichArmor].getDurability() > 196)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.DIAMOND_HELMET && armor[whichArmor].getDurability() > 364)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.DIAMOND_CHESTPLATE && armor[whichArmor].getDurability() > 529)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.DIAMOND_LEGGINGS && armor[whichArmor].getDurability() > 496)
 				armor[whichArmor].setAmount(0);
 			else if (armor[whichArmor].getType() == Material.DIAMOND_BOOTS && armor[whichArmor].getDurability() > 430)
 				armor[whichArmor].setAmount(0);
 			inventory.setArmorContents(armor);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void geigerCounter(PlayerInteractEvent event){
 		if (!this.p.getApocalypseDamage())
 			return;
 		if (!((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getPlayer().getItemInHand().getTypeId() == this.p.getPipboyID()))
 			return;
 		Player player = event.getPlayer();
 		Location loc = player.getLocation();
 		ItemStack[] inventoryArray = player.getInventory().getContents();
 		List<Location> craters = this.p.getCraters();
 		List<Location> GECKs = this.p.getGECKs();
 		List<Long> craterTimes = this.p.getCraterTimes();
 		double damageChance = 0;
 		double geck = 0;
 		if ((loc.getBlock().getType() == Material.WATER || loc.getBlock().getType() == Material.STATIONARY_WATER) && !player.isInsideVehicle()){
 		    //System.out.println("is in water");
			damageChance += .05;
 		}
 		if (loc.getWorld().getHighestBlockYAt(loc) <= loc.getY() && (loc.getWorld().hasStorm() || loc.getWorld().isThundering())){
			damageChance += .05;
 		}
 		//System.out.println(damageChance);
 		java.util.Date now = new Date();
 		if (craters != null) {
 			for (Location crater : craters) {
 				if (crater.getWorld() == loc.getWorld()) {
 					//damageChance += 1/(crater.distanceSquared(loc)/50);
 					//double dam = 1/(crater.distanceSquared(loc)/50);
 					//System.out.println("distance " + crater.distance(loc));
 					//System.out.println("normal damage " + dam);
 					int index = craters.lastIndexOf(crater);
 					Long history = now.getTime()- craterTimes.get(index);
 					//System.out.println("age " + history.doubleValue()/(1000*60*60));
 					damageChance += (1000/ (crater.distanceSquared(loc) / 50))* (1/ (Math.pow(10, (Math.log10(history.doubleValue()/(1000*60*60)) / Math.log10(7)))));
 					//System.out.println("damageChance " + damageChance);
 				}
 			}
 		}
 		//System.out.println(damageChance);
 		if (GECKs != null){
 			for (Location GECK : GECKs){
 				Block block = GECK.getBlock();
 				if (block.isBlockIndirectlyPowered() && block.getRelative(BlockFace.NORTH).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.SOUTH).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.EAST).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.WEST).getType() == Material.PISTON_BASE && block.getRelative(BlockFace.NORTH).isBlockIndirectlyPowered() && block.getRelative(BlockFace.SOUTH).isBlockIndirectlyPowered() && block.getRelative(BlockFace.EAST).isBlockIndirectlyPowered() && block.getRelative(BlockFace.WEST).isBlockIndirectlyPowered()){
 					if (GECK.getWorld() == loc.getWorld())
 						geck += 1/(GECK.distanceSquared(loc)/50);
 				}
 			}
 		}
 		//System.out.println(damageChance);
 		if (!(loc.getWorld().getHighestBlockYAt(loc) <= loc.getY()) && (loc.getBlock().getType() != Material.WATER && loc.getBlock().getType() != Material.STATIONARY_WATER)){
 			//System.out.println("under a block and not in water");
 			damageChance = 0;
 		}
 		//System.out.println(damageChance);
 		for (ItemStack a : inventoryArray){
 			if (a != null){
 				if (a.getType() == Material.SNOW){
 					damageChance += .05 * a.getAmount();
 				}
 			}
 		}
 		//System.out.println(damageChance);
 		int rad = (int)((damageChance - geck)*1000);
 		if (rad < 0)
 			rad = 0;
 		player.sendMessage(rad + " rads/sec");
 	}
 }
