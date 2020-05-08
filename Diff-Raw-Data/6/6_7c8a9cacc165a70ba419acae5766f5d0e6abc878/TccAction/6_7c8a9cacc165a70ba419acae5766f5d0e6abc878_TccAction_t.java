 package me.Schm0ftie.TownyCommunityChests;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class TccAction {
 	
 	private static boolean isLargeChest(Block block){
 		Location location = block.getLocation();
 		World world = location.getWorld();
 		if (world.getBlockTypeIdAt(location.getBlockX() + 1 , location.getBlockY(), location.getBlockZ()) == TownyCommunityChests.chest_id){
 			return true;
 		}
 		if (world.getBlockTypeIdAt(location.getBlockX() - 1 , location.getBlockY(), location.getBlockZ()) == TownyCommunityChests.chest_id){
 			return true;
 		}
 		if (world.getBlockTypeIdAt(location.getBlockX(), location.getBlockY(), location.getBlockZ() + 1) == TownyCommunityChests.chest_id){
 			return true;
 		}
 		if (world.getBlockTypeIdAt(location.getBlockX(), location.getBlockY(), location.getBlockZ() - 1) == TownyCommunityChests.chest_id){
 			return true;
 		}
 		return false;
 	}
 	
 	@SuppressWarnings("unused")
 	private static Chest getDoubleChest(Block block){
 		Location location = block.getLocation();
 		World world = location.getWorld();
 		
 		Block bl1 = world.getBlockAt(location.getBlockX() + 1 , location.getBlockY(), location.getBlockZ());
 		Block bl2 = world.getBlockAt(location.getBlockX() - 1 , location.getBlockY(), location.getBlockZ());
 		Block bl3 = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ() + 1);
 		Block bl4 = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ() - 1);
 	
 		if (bl1.getTypeId() == TownyCommunityChests.chest_id){
 			return (Chest) bl1;
 		}
 		if (bl2.getTypeId() == TownyCommunityChests.chest_id){
 			return (Chest) bl2;
 		}
 		if (bl3.getTypeId() == TownyCommunityChests.chest_id){
 			return (Chest) bl3;
 		}
 		if (bl4.getTypeId() == TownyCommunityChests.chest_id){
 			return (Chest) bl4;
 		}
 		return null;
 	}
 	
 	public static Chest getChest(Block sign){
 		Block block = sign.getWorld().getBlockAt(sign.getLocation().getBlockX(), sign.getLocation().getBlockY() - 1, sign.getLocation().getBlockZ());
 		if (!(block.getState() instanceof Chest) || isLargeChest(block)){
 			return null;
 		}
 		return (Chest) block.getState();
 		
 	}
 	
 	public static void updateProgress(Sign sign, Chest chest){
 		ItemStack filter = new ItemStack(Material.valueOf(sign.getLine(1)));
 		int maxStackSize = filter.getType().getMaxStackSize();
 		int maxAmount = 0;
 		int currAmount = 0;
 		if (sign.getLine(2).equalsIgnoreCase(Text.EMPTY_STRING)){
 			maxAmount = maxStackSize * 27;
 		}
 		else{
 			maxAmount = parseMaxAmount(sign.getLine(2));
 		}
 		for (ItemStack itemStack : chest.getInventory().getContents()){
 			if (itemStack != null){
 				if (itemStack.getType() == filter.getType()){
 					currAmount = currAmount + itemStack.getAmount();
 				}
 			}
 		}
 		
 		if (currAmount >= maxAmount){
 			sign.setLine(3, Text.CHEST_FULL);
 		}
 		else{
 			sign.setLine(3, Text.EMPTY_STRING);
 		}
 		sign.setLine(2, "[" + currAmount + "/" + maxAmount + "]");
 		sign.update();
 	}
 	
 	public static boolean isFilteredItem(ItemStack holding, String line){
 		ItemStack filter = new ItemStack(Material.valueOf(line));
 		if (holding.getType() == filter.getType()){
 			return true;
 		}
 		return false;
 	}
 	
 	public static int parseMaxAmount(String line){
 		int indexStart = line.indexOf("/") + 1;
 		int indexEnd = line.indexOf("]", indexStart);
 		return Integer.parseInt(line.substring(indexStart, indexEnd));
 	}
 	
 	public static int parseCurrAmount(String line){
 		int indexStart = line.indexOf("[") + 1;
 		int indexEnd = line.indexOf("/", indexStart);
 		if (line.contains("?")){
 			return 0;
 		}
 		return Integer.parseInt(line.substring(indexStart,indexEnd));
 	}
 	
 	public static ItemStack myAddItem(ItemStack iStack, Inventory inv, int maxAmount){
 		int currentAmount = 0;
 		int amountInHand = iStack.getAmount();
 		int balance = 0;
 		for (ItemStack itemStackChest: inv.getContents()){
 			if (itemStackChest != null){
 				currentAmount = currentAmount + itemStackChest.getAmount();
 			}
 		}
 		if(currentAmount + amountInHand > maxAmount){
 			balance = currentAmount + amountInHand - maxAmount;
 			ItemStack item = new ItemStack(iStack.getType(), amountInHand - balance);
			item.setDurability(iStack.getDurability());
 			inv.addItem(item);
			ItemStack returnItems = new ItemStack (iStack.getType(), balance);
			returnItems.setDurability(iStack.getDurability());
			return returnItems;
 		}
 		inv.addItem(iStack);
 		return null;
 	}
 }
