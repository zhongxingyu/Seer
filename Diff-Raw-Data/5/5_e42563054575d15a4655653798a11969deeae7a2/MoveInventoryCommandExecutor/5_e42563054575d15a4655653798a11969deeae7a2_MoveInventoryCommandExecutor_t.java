 package me.babarix.MoveInventory;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.griefcraft.model.Protection;
 
 public class MoveInventoryCommandExecutor implements CommandExecutor {
 
 	private final MoveInventory plugin;
 
 	public MoveInventoryCommandExecutor(MoveInventory plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		Player player;
 		Block tblock;
 		Chest chest1, chest2;
 		Protection protection;
 		byte[] ini = { 0, 6, 27, 28, 37, 38, 39, 40, 50, 53, 55, 59, 63, 65,
 				66, 68, 69, 70, 72, 75, 76, 77, 78, 90, 92, 93, 94, 96 };
 
 		HashSet<Byte> trans = new HashSet<Byte>();
 		for (byte b : ini) {
 			trans.add(b);
 		}
 
 		if (label.equalsIgnoreCase("mi")) {
 
 			if (sender instanceof Player) {
 				player = (Player) sender;
 			} else {
 				return true;
 			}
 			if (args.length < 1) {
 				help(player);
 			}
 			tblock = player.getTargetBlock(trans, 119);
 			if (tblock.getTypeId() != 54 || tblock == null) {
 				player.sendMessage("Your Target is no chest.");
 				return true;
 			}
 
 			if (plugin.lwc != null) {
 				protection = plugin.lwc.findProtection(tblock);
 				if (plugin.lwc.canAccessProtection(player, protection) == false) {
 					player.sendMessage("This chest is locked by sombody else.");
 					return true;
 				}
 			}
 
 			chest1 = (Chest) tblock.getState();
 			chest2 = GetDoubleChest(tblock);
 
 			if (args[0].equalsIgnoreCase("tc")) {
 				if (IsEmpty(player.getInventory())) {
 					player.sendMessage("Your inventory is empty.");
					return true; 
 				}
 				doTc(player, chest1, false);
 				if (chest2 != null) {
 					doTc(player, chest2, true);
 				}
 				return true;
 			} else if (args[0].equalsIgnoreCase("tp")) {
 				if (IsEmpty(chest1.getInventory())) {
 					if (chest2 == null) {
 						player.sendMessage("The chest is empty.");
						return true;
 					} else if (IsEmpty(chest2.getInventory())) {
 						player.sendMessage("The chest is empty.");
						return true;
 					}
 				}
 				doTp(player, chest1, false);
 				if (chest2 != null) {
 					doTp(player, chest2, true);
 				}
 				return true;
 			} else if (args[0].equalsIgnoreCase("v")) {
 				toggleVerbose(player);
 			} else {
 				help(player);
 				return true;
 			}
 
 		}
 		return true;
 	}
 
 	public void toggleVerbose(Player player) {
 
 		if (plugin.verbose.containsKey(player.getName())) {
 			if (plugin.verbose.get(player.getName())) {
 				plugin.verbose.put(player.getName(), false);
 				player.sendMessage("Reporting disabled");
 			} else {
 				plugin.verbose.put(player.getName(), true);
 				player.sendMessage("Reporting enabled");
 			}
 		} else {
 			plugin.verbose.put(player.getName(), true);
 			player.sendMessage("Reporting enabled");
 		}
 	}
 
 	public void report(int amout, String name, Player player) {
 		if (plugin.verbose.containsKey(player.getName())) {
 			if (plugin.verbose.get(player.getName())) {
 				player.sendMessage("Moved " + amout + " of " + name);
 			}
 		} else {
 			plugin.verbose.put(player.getName(), false);
 		}
 	}
 
 	public void help(Player player) {
 		player.sendMessage("usage: /mi [tc|tp|v]");
 		player.sendMessage("tc: Moves your inventory into target chest.");
 		player.sendMessage("tp: Moves target chests inventory into your inventory.");
 		player.sendMessage("v:  Turns move message on/off");
 	}
 
 	public Chest GetDoubleChest(Block block) {
 		Chest chest = null;
 		if (block.getRelative(BlockFace.NORTH).getTypeId() == 54) {
 			chest = (Chest) block.getRelative(BlockFace.NORTH).getState();
 			return chest;
 		} else if (block.getRelative(BlockFace.EAST).getTypeId() == 54) {
 			chest = (Chest) block.getRelative(BlockFace.EAST).getState();
 			return chest;
 		} else if (block.getRelative(BlockFace.SOUTH).getTypeId() == 54) {
 			chest = (Chest) block.getRelative(BlockFace.SOUTH).getState();
 			return chest;
 		} else if (block.getRelative(BlockFace.WEST).getTypeId() == 54) {
 			chest = (Chest) block.getRelative(BlockFace.WEST).getState();
 			return chest;
 		}
 		return chest;
 	}
 
 	public boolean doTc(Player player, Chest chest, boolean fullFlag) {
 		Inventory ichest, iplayer;
 		HashMap<Integer, ItemStack> leftovers;
 
 		ichest = chest.getInventory();
 		iplayer = player.getInventory();
 		if (ichest.firstEmpty() == -1 && fullFlag) {
 			player.sendMessage("The Chest is alredy full.");
 			return true;
 		}
 		for (ItemStack item : iplayer.getContents()) {
 			if (item != null && ichest.firstEmpty() != -1) {
 				leftovers = ichest.addItem(item);
 				if (leftovers.size() == 0) {
 					report(item.getAmount(), item.getType().name(), player);
 					iplayer.removeItem(item);
 				} else {
 					iplayer.removeItem(item);
 					iplayer.addItem(leftovers.get(0));
 					report(item.getAmount() - leftovers.get(0).getAmount(),
 							item.getType().name(), player);
 				}
 			}
 
 		}
 		return true;
 	}
 
 	public boolean doTp(Player player, Chest chest, boolean fullFlag) {
 		Inventory ichest, iplayer;
 		HashMap<Integer, ItemStack> leftovers;
 
 		ichest = chest.getInventory();
 		iplayer = player.getInventory();
 		if (iplayer.firstEmpty() == -1 && fullFlag) {
 			player.sendMessage("Your Inventory is full.");
 			return true;
 		}
 
 		for (ItemStack item : ichest.getContents()) {
 			if (item != null && iplayer.firstEmpty() != -1) {
 
 				leftovers = iplayer.addItem(item);
 				if (leftovers.size() == 0) {
 					ichest.removeItem(item);
 					report(item.getAmount(), item.getType().name(), player);
 				} else {
 					ichest.removeItem(item);
 					ichest.addItem(leftovers.get(0));
 					report(item.getAmount() - leftovers.get(0).getAmount(),
 							item.getType().name(), player);
 				}
 			}
 
 		}
 		return true;
 	}
 
 	public boolean IsEmpty(Inventory in) {
 		boolean ret = false;
 
 		if (in == null) {
 			return true;
 		}
 		for (ItemStack item : in.getContents()) {
 			ret |= (item != null);
 		}
 
 		return !ret;
 
 	}
 
 }
