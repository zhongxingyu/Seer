 package dk.gabriel333.BITBackpack;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.gui.GenericLabel;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import dk.gabriel333.BITBackpack.BITBackpackLanguageInterface.Language;
 import dk.gabriel333.BukkitInventoryTools.BIT;
 import dk.gabriel333.Library.BITConfig;
 import dk.gabriel333.Library.BITMessages;
 import dk.gabriel333.Library.BITPermissions;
 import dk.gabriel333.register.payment.Method.MethodAccount;
 import dk.gabriel333.BITBackpack.BITBackpackInventorySaveTask;
 
 @SuppressWarnings({})
 public class BITBackpack implements CommandExecutor {
 
 	public BITBackpack(BIT instance) {
 		plugin = instance;
 	}
 
 	public static Logger logger = Logger.getLogger("minecraft");
 
 	public BIT plugin;
 
 	public static String inventoryName = "Backpack";  // Added Hash to Map
 	public static HashMap<String, ItemStack[]> inventories = new HashMap<String, ItemStack[]>();
 	public static HashMap<String, Inventory> openedInventories = new HashMap<String, Inventory>();
 	public static HashMap<String, String> openedInventoriesOthers = new HashMap<String, String>();
 	public static HashMap<String, GenericLabel> widgets = new HashMap<String, GenericLabel>();
 
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 		if (sender instanceof Player) {
 			Player player = (Player) sender;
 			if (BITPermissions.hasPerm(sender, "backpack.use",
 					BITPermissions.NOT_QUIET)) {
 				if (canOpenBackpack(player.getWorld(), player)) {
 					if (args.length == 0) {
 						if (allowedSize(player.getWorld(), player, true) > 0) {
 							String playerName = sender.getName();
 							if (inventories.containsKey(playerName)) {
 								if (!openedInventories.containsKey(playerName)) {
 									Inventory inv = SpoutManager
 											.getInventoryBuilder().construct(
 													BITBackpack.allowedSize(
 															player.getWorld(),
 															player, true),
 													inventoryName);
 									openedInventories.put(playerName, inv);
 									// TODO: I dont know if these two lines
 									// should be here.
 									// openedInventoriesOthers.put(
 									// player.getName(), playerName);
 									inv.setContents(inventories.get(playerName));
 									((org.getspout.spoutapi.player.SpoutPlayer) player)
 											.openInventoryWindow(inv);
 								} else {
 									player.sendMessage(BIT.li
 											.getMessage("playerhasalreadyhis")
 											+ ChatColor.RED
 											+ inventoryName
 											+ ChatColor.WHITE
 											+ BIT.li.getMessage("opened"));
 									}
 							} else {
 								player.sendMessage(ChatColor.RED
 										+ BIT.li.getMessage("playernotfound"));
 							}
 						} else {
 							player.sendMessage("You need to buy a backpack. Use /backpack buy");
 							return true;
 						}
 					} else if (args.length == 1) {
 						String argument = args[0];
 						if (argument.equalsIgnoreCase("reload")) {
 							if (BITPermissions
 									.hasPerm(player, "backpack.reload",
 											BITPermissions.NOT_QUIET)) {
 								player.sendMessage(BIT.li
 										.getMessage("configreloaded1"));
 								player.sendMessage(BIT.li
 										.getMessage("configreloaded2"));
 								player.sendMessage(BIT.li
 										.getMessage("configreloaded3"));
 								return true;
 							}
 						} else if (argument.equalsIgnoreCase("help")
 								|| argument.equalsIgnoreCase("?")) {
 							showHelp(player);
 							return true;
 						} else if (argument.equalsIgnoreCase("info")) {
 							int size = allowedSize(player.getWorld(), player,
 									true);
 							if (size == 54) {
 								player.sendMessage(BIT.li
 										.getMessage("youvegotthebiggest")
 										+ ChatColor.RED
 										+ inventoryName
 										+ ChatColor.WHITE
 										+ BIT.li.getMessage("!"));
 							} else if (size == sizeInConfig(player.getWorld(),
 									player)) {
 								player.sendMessage(BIT.li
 										.getMessage("youvegotthebiggest")
 										+ ChatColor.RED
 										+ inventoryName
 										+ ChatColor.WHITE
 										+ BIT.li.getMessage("foryourpermissions"));
 							} else {
 								player.sendMessage(BIT.li.getMessage("your")
 										+ ChatColor.RED + inventoryName
 										+ ChatColor.WHITE
 										+ BIT.li.getMessage("has")
 										+ ChatColor.RED + size
 										+ ChatColor.WHITE
 										+ BIT.li.getMessage("slots"));
 								if (size < upgradeAllowedSize(
 										player.getWorld(), player)
 										&& BIT.useEconomy) {
 									double cost = calculateCostToUpgrade(size);
 									player.sendMessage(BIT.li
 											.getMessage("nextupgradecost")
 											+ ChatColor.RED
 											+ plugin.Method.format(cost)
 											+ ChatColor.WHITE + ".");
 								}
 							}
 							return true;
 						} else if (argument.equalsIgnoreCase("upgrade")
 								|| argument.equalsIgnoreCase("buy")) {
 							if (allowedSize(player.getWorld(), player, false) > sizeInConfig(
 									player.getWorld(), player)
 									&& upgradeAllowedSize(player.getWorld(),
 											player) > sizeInConfig(
 											player.getWorld(), player)) {
 								if (BIT.useEconomy) {
 									startUpgradeProcedure(
 											allowedSize(player.getWorld(),
 													player, true), player,
 											player);
 								}
 							} else if (allowedSize(player.getWorld(), player,
 									true) == 54) {
 								player.sendMessage(BIT.li
 										.getMessage("youvegotthebiggest")
 										+ ChatColor.RED
 										+ inventoryName
 										+ ChatColor.WHITE
 										+ BIT.li.getMessage("!"));
 							} else {
 								player.sendMessage(BIT.li
 										.getMessage("youvegotthebiggest")
 										+ ChatColor.RED
 										+ inventoryName
 										+ ChatColor.WHITE
 										+ BIT.li.getMessage("foryourpermissions"));
 							}
 							return true;
 
 						} else if (argument.equalsIgnoreCase("clear")) {  // Updated 1/26/12 to fix Clear Inventory Dupe Bug.
 							if (BITPermissions.hasPerm(player,
 									"backpack.clear", BITPermissions.NOT_QUIET)) {
 									if (BITBackpack.canOpenBackpack(player.getWorld(), player)) {
 										BITBackpack.loadInventory(player, player.getWorld());
 										if (BITBackpack.inventories.containsKey(player.getName())) {
 											// inventories.remove(player.getName());  // Removing the Key does no good since the loadCall regens it.
 											ItemStack[] items = BITBackpack.inventories.get(player
 													.getName());
 											         Inventory inventory = SpoutManager
 													.getInventoryBuilder()
 													.construct(BITBackpack.allowedSize(player.getWorld(),player, true),	BITBackpack.inventoryName);
 											for (Integer i = 0; i < BITBackpack.allowedSize(
 													player.getWorld(), player, true); i++) {
 												ItemStack item = new ItemStack(0, 0);
 												inventory.setItem(i, item);
 											}
 											BITBackpack.inventories.put(player.getName(),
 													inventory.getContents());
 											BITBackpackInventorySaveTask.saveInventory(player,
 													player.getWorld());
 											player.sendMessage(BIT.li
 													.getMessage("your")
 													+ ChatColor.RED
 													+ inventoryName
 													+ ChatColor.WHITE
 													+ BIT.li.getMessage("hasbeencleared"));
 											
 										}
 									
 								} else {
 									player.sendMessage(BIT.li
 											.getMessage("youdonthavearegistred")
 											+ ChatColor.RED
 											+ inventoryName
 											+ ChatColor.WHITE
 											+ BIT.li.getMessage("!"));
 								}
 							}
 							return true;
 
 						} else {
 							showHelp(player);
 							return true;
 
 						}
 					} else if (args.length == 2) {
 						String firstArgument = args[0];
 						String playerName = args[1];
 						if (firstArgument.equalsIgnoreCase("info")) {
 							if (BITPermissions.hasPerm(player,
 									"backpack.info.other",
 									BITPermissions.NOT_QUIET)) {
 								if (inventories.containsKey(playerName)) {
 									Player playerCmd = Bukkit.getServer()
 											.getPlayer(playerName);
 									int size = allowedSize(
 											playerCmd.getWorld(), playerCmd,
 											true);
 									if (size == 54) {
 										player.sendMessage(BIT.li
 												.getMessage("playerhasgotthebiggest")
 												+ ChatColor.RED
 												+ inventoryName
 												+ ChatColor.WHITE
 												+ BIT.li.getMessage("!"));
 									} else {
 										player.sendMessage(BIT.li
 												.getMessage("players")
 												+ ChatColor.RED
 												+ inventoryName
 												+ ChatColor.WHITE
 												+ BIT.li.getMessage("hasbis")
 												+ size
 												+ BIT.li.getMessage("slots"));
 										if (size < upgradeAllowedSize(
 												player.getWorld(), player)
 												&& BIT.useEconomy) {
 											double cost = calculateCostToUpgrade(size);
 											player.sendMessage(BIT.li
 													.getMessage("nextupgradecost")
 													+ ChatColor.RED
 													+ plugin.Method
 															.format(cost)
 													+ ChatColor.WHITE + ".");
 										}
 									}
 								} else {
 									player.sendMessage(ChatColor.RED
 											+ BIT.li.getMessage("playernotfound"));
 								}
 							}
 							return true;
 
 						} else if (firstArgument.equalsIgnoreCase("upgrade")) {
 							if (playerName.equalsIgnoreCase("workbench")) {
 								if (!hasWorkbench(player)
 										&& BITConfig.SBP_workbenchBuyable) {
 									setWorkbench(player, true);
 								} else
 									player.sendMessage(ChatColor.RED
 											+ BIT.li.getMessage("youalreadyhaveaccesstotheworkbench"));
 							} else {
 								if (BITPermissions.hasPerm(player,
 										"backpack.upgrade.other",
 										BITPermissions.NOT_QUIET)) {
 									if (inventories.containsKey(playerName)) {
 										Player playerCmd = Bukkit.getServer()
 												.getPlayer(playerName);
 										if (allowedSize(playerCmd.getWorld(),
 												playerCmd, false) < upgradeAllowedSize(
 												playerCmd.getWorld(), playerCmd)) {
 											if (BIT.useEconomy) {
 												startUpgradeProcedure(
 														allowedSize(playerCmd
 																.getWorld(),
 																playerCmd, true),
 														playerCmd, player);
 											}
 										} else if (allowedSize(
 												playerCmd.getWorld(),
 												playerCmd, true) == 54) {
 											player.sendMessage(BIT.li
 													.getMessage("playerhasgotthebiggest")
 													+ ChatColor.RED
 													+ inventoryName
 													+ ChatColor.WHITE
 													+ BIT.li.getMessage("!"));
 										} else {
 											player.sendMessage(BIT.li
 													.getMessage("playerhasgotthebiggest")
 													+ ChatColor.RED
 													+ inventoryName
 													+ ChatColor.WHITE
 													+ BIT.li.getMessage("forhispermissions"));
 										}
 									} else {
 										player.sendMessage(ChatColor.RED
 												+ BIT.li.getMessage("playernotfound"));
 									}
 								}
 							}
 							return true;
 
 						} else if (firstArgument.equalsIgnoreCase("clear")) {
 							if (BITPermissions.hasPerm(player,
 									"backpack.clear.other",
 									BITPermissions.NOT_QUIET)) {
 								if (inventories.containsKey(playerName)) {
 									// inventories.remove(playerName); Inventory Dupe Bug Fix. 1-26-12
 									ItemStack[] items = BITBackpack.inventories.get(player
 											.getName());
 									         Inventory inventory = SpoutManager
 											.getInventoryBuilder()
 											.construct(BITBackpack.allowedSize(player.getWorld(),player, true),	BITBackpack.inventoryName);
 									for (Integer i = 0; i < BITBackpack.allowedSize(
 											player.getWorld(), player, true); i++) {
 										ItemStack item = new ItemStack(0, 0);
 										inventory.setItem(i, item);
 									}
 									BITBackpack.inventories.put(player.getName(),
 											inventory.getContents());
 									BITBackpackInventorySaveTask.saveInventory(player,
 											player.getWorld());
 									player.sendMessage(BIT.li
 											.getMessage("frenchonly")
 											+ playerName
 											+ BIT.li.getMessage("'s")
 											+ ChatColor.RED
 											+ inventoryName
 											+ ChatColor.WHITE
 											+ BIT.li.getMessage("hasbeencleared"));
 								} else {
 									player.sendMessage(ChatColor.RED
 											+ BIT.li.getMessage("playernotfound"));
 								}
 							}
 							return true;
 
 						} else if (firstArgument.equalsIgnoreCase("open")) {
 							if (BITPermissions.hasPerm(player,
 									"backpack.open.other",
 									BITPermissions.NOT_QUIET)) {
 								if (inventories.containsKey(playerName)) {
 									if (!openedInventories
 											.containsKey(playerName)) {
 										Inventory inv = SpoutManager
 												.getInventoryBuilder()
 												.construct(
 														BITBackpack
 																.allowedSize(
 																		player.getWorld(),
 																		player,
 																		true),
 														inventoryName);
 										openedInventories.put(playerName, inv);
 										openedInventoriesOthers.put(
 												player.getName(), playerName);
 										inv.setContents(inventories
 												.get(playerName));
 										((org.getspout.spoutapi.player.SpoutPlayer) player)
 												.openInventoryWindow(inv);
 									} else {
 										player.sendMessage(BIT.li
 												.getMessage("playerhasalreadyhis")
 												+ ChatColor.RED
 												+ inventoryName
 												+ ChatColor.WHITE
 												+ BIT.li.getMessage("opened"));
 									}
 								} else {
 									player.sendMessage(ChatColor.RED
 											+ BIT.li.getMessage("playernotfound"));
 								}
 							}
 						}
 						return true;
 					}
 				} else {
 					showHelp(player);
 				}
 			}
 		} else {
 			// the user has not permission to use the BITBackpack.
 			return true;
 		}
 		return false;
 	}
 
 	public void startUpgradeProcedure(int sizeBefore, Player player,
 			Player notificationsAndMoneyPlayer) {
 		int sizeAfter = sizeBefore + 9;
 		double cost = calculateCostToUpgrade(sizeBefore);
 		if (plugin.Method.hasAccount(notificationsAndMoneyPlayer.getName())) {
 			MethodAccount account = plugin.Method
 					.getAccount(notificationsAndMoneyPlayer.getName());
 			if (plugin.Method.hasAccount(notificationsAndMoneyPlayer.getName())) {
 				if (account.hasEnough(cost)) {
 					account.subtract(cost);
 					notificationsAndMoneyPlayer.sendMessage("Your account ("
 							+ plugin.Method.getAccount(
 									notificationsAndMoneyPlayer.getName())
 									.balance() + ") has been deducted "
 							+ plugin.Method.format(cost) + ".");
 					if (!player.getName().equals(
 							notificationsAndMoneyPlayer.getName())) {
 						player.sendMessage(player.getName()
 								+ "'s account has been deducted "
 								+ plugin.Method.format(cost) + ".");
 					}
 				} else {
 					if (player.equals(notificationsAndMoneyPlayer)) {
 						notificationsAndMoneyPlayer.sendMessage(BIT.li
 								.getMessage("notenoughmoneyyour")
 								+ ChatColor.RED
 								+ inventoryName
 								+ ChatColor.WHITE + ".");
 					} else {
 						notificationsAndMoneyPlayer.sendMessage(BIT.li
 								.getMessage("notenoughmoneyplayer")
 								+ ChatColor.RED
 								+ inventoryName
 								+ ChatColor.WHITE + ".");
 					}
 					return;
 				}
 			} else {
 				notificationsAndMoneyPlayer.sendMessage(ChatColor.RED
 						+ BIT.li.getMessage("noaccount"));
 				return;
 			}
 		}
 		
 		// The next line should not run if user has no backpack.
 		BITBackpackInventorySaveTask.saveInventory(player, player.getWorld());
 		inventories.remove(player.getName());  //DOCKTER
 		
 		File saveFile;
 		if (BITConfig.getBooleanParm("SBP.InventoriesShare."
 				+ player.getWorld().getName(), true)) {
 			saveFile = new File(plugin.getDataFolder() + File.separator
 					+ "inventories", player.getName() + ".yml");
 		} else {
 			saveFile = new File(plugin.getDataFolder() + File.separator
 					+ "inventories", player.getName() + "_"
 					+ player.getWorld().getName() + ".yml");
 		}
 		YamlConfiguration config = new YamlConfiguration();
 		try {
 			config.load(saveFile);
 		} catch (FileNotFoundException e) {
 			// e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		config.set("Size", sizeAfter);
 		try {
 			config.save(saveFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		loadInventory(player, player.getWorld());
 		notificationsAndMoneyPlayer.sendMessage(BIT.li.getMessage("your")
 				+ ChatColor.RED + inventoryName + ChatColor.WHITE
 				+ BIT.li.getMessage("hasbeenupgraded"));
 		notificationsAndMoneyPlayer.sendMessage(BIT.li.getMessage("ithasnow")
 				+ ChatColor.RED + sizeAfter + ChatColor.WHITE
 				+ BIT.li.getMessage("slots"));
 	}
 
 	public void showHelp(Player player) {
 		if (BITPermissions.hasPerm(player, "backpack.reload",
 				BITPermissions.QUIET)) {
 			player.sendMessage(BIT.li.getMessage("reloadcommand"));
 		}
 		player.sendMessage(BIT.li.getMessage("infocommand") + ChatColor.RED
 				+ inventoryName + ChatColor.WHITE + ".");
 		if (allowedSize(player.getWorld(), player, true) < upgradeAllowedSize(
 				player.getWorld(), player) && BIT.useEconomy) {
 			player.sendMessage(BIT.li.getMessage("upgradecommand")
 					+ ChatColor.RED + inventoryName + ChatColor.WHITE + ".");
 		}
 	}
 
 	public static double calculateCostToUpgrade(int size) {
 		double cost = BITConfig.SBP_price9;
 		if (size == 9) {
 			cost = BITConfig.SBP_price18;
 		} else if (size == 18) {
 			cost = BITConfig.SBP_price27;
 		} else if (size == 27) {
 			cost = BITConfig.SBP_price36;
 		} else if (size == 36) {
 			cost = BITConfig.SBP_price45;
 		} else if (size == 45) {
 			cost = BITConfig.SBP_price54;
 		}
 		return cost;
 	}
 
 	private int upgradeAllowedSize(World world, Player player) {
 		int size = 9;
 		if (BITPermissions.hasPerm(player, "backpack.upgrade54",
 				BITPermissions.QUIET)) {
 			size = 54;
 		} else if (BITPermissions.hasPerm(player, "backpack.upgrade45",
 				BITPermissions.QUIET)) {
 			size = 45;
 		} else if (BITPermissions.hasPerm(player, "backpack.upgrade36",
 				BITPermissions.QUIET)) {
 			size = 36;
 		} else if (BITPermissions.hasPerm(player, "backpack.upgrade27",
 				BITPermissions.QUIET)) {
 			size = 27;
 		} else if (BITPermissions.hasPerm(player, "backpack.upgrade18",
 				BITPermissions.QUIET)) {
 			size = 18;
 		}
 		return size;
 	}
 
 	public static int sizeInConfig(World world, Player player) {
 		File saveFile;
 		if (BITConfig.getBooleanParm("SBP.InventoriesShare."
 				+ player.getWorld().getName(), true)) {
 			saveFile = new File(BIT.plugin.getDataFolder() + File.separator
 					+ "inventories", player.getName() + ".yml");
 		} else {
 			saveFile = new File(BIT.plugin.getDataFolder() + File.separator
 					+ "inventories", player.getName() + "_" + world.getName()
 					+ ".yml");
 		}
 		YamlConfiguration config = new YamlConfiguration();
 		try {
 			config.load(saveFile);
 		} catch (FileNotFoundException e) {
 			// e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		return config.getInt("Size", 0);
 	}
 
 	public static int allowedSize(World world, Player player,
 			boolean configurationCheck) {
 		// Finding permitted size
 		int size = 0;
 		if (BITPermissions.hasPerm(player, "backpack.size54",
 				BITPermissions.QUIET)) {
 			size = 54;
 		} else if (BITPermissions.hasPerm(player, "backpack.size45",
 				BITPermissions.QUIET)) {
 			size = 45;
 		} else if (BITPermissions.hasPerm(player, "backpack.size36",
 				BITPermissions.QUIET)) {
 			size = 36;
 		} else if (BITPermissions.hasPerm(player, "backpack.size27",
 				BITPermissions.QUIET)) {
 			size = 27;
 		} else if (BITPermissions.hasPerm(player, "backpack.size18",
 				BITPermissions.QUIET)) {
 			size = 18;
 		} else if (BITPermissions.hasPerm(player, "backpack.size9",
 				BITPermissions.QUIET)) {
 			size = 9;
 		}
 		// Finding actual size
 		if (configurationCheck) {
 			if (sizeInConfig(world, player) < size) {
 				size = sizeInConfig(world, player);
 			}
 		}
 
 		// Automatic upgrading BP if its free
 		if (BIT.useEconomy) {
 			double upgradePrice = 0;
			while (upgradePrice == 0) {
 				upgradePrice = calculateCostToUpgrade(size);
 				if (upgradePrice == 0)
 					size = size + 9;
 			}
 		}
 		return size;
 	}
 
 	public static Inventory getClosedBackpack(Player player) {
 		Inventory inventory = SpoutManager.getInventoryBuilder().construct(
 				BITBackpack.allowedSize(player.getWorld(), player, true),
 				inventoryName);
 		if (inventories.containsKey(player.getName())) {
 			inventory.setContents(inventories.get(player.getName()));
 		}
 		return inventory;
 	}
 
 	public static void setClosedBackpack(Player player, Inventory inventory) {
 		inventories.put(player.getName(), inventory.getContents());
 		return;
 	}
 
 	public static boolean isOpenBackpack(Player player) {
 		return openedInventories.containsKey(player.getName());
 	}
 
 	public static Inventory getOpenedBackpack(Player player) {
 		return openedInventories.get(player.getName());
 	}
 
 	public static void updateInventory(Player player, ItemStack[] is) {
 		inventories.put(player.getName(), is);
 	}
 
 	public static boolean canOpenBackpack(World world, Player player) {
 		boolean canOpenBackpack = false;
 		if (BITPermissions.hasPerm(player, "backpack.size54",
 				BITPermissions.QUIET)
 				|| BITPermissions.hasPerm(player, "backpack.size45",
 						BITPermissions.QUIET)
 				|| BITPermissions.hasPerm(player, "backpack.size36",
 						BITPermissions.QUIET)
 				|| BITPermissions.hasPerm(player, "backpack.size27",
 						BITPermissions.QUIET)
 				|| BITPermissions.hasPerm(player, "backpack.size18",
 						BITPermissions.QUIET)
 				|| BITPermissions.hasPerm(player, "backpack.size9",
 						BITPermissions.QUIET)) {
 			canOpenBackpack = true;
 		} else {
 			canOpenBackpack = false;
 		}
 
 		if (BIT.getWorldGuard() != null) {
 			Location location = player.getLocation();
 			com.sk89q.worldedit.Vector vector = new com.sk89q.worldedit.Vector(
 					location.getX(), location.getY(), location.getZ());
 			Map<String, ProtectedRegion> regions = BIT.getWorldGuard()
 					.getGlobalRegionManager().get(location.getWorld())
 					.getRegions();
 			List<String> inRegions = new ArrayList<String>();
 			for (String key_ : regions.keySet()) {
 				ProtectedRegion region = regions.get(key_);
 				if (region.contains(vector)) {
 					inRegions.add(key_);
 				}
 			}
 			for (String region : BITConfig.SBP_noBackpackRegions) {
 				if (inRegions.contains(region)) {
 					canOpenBackpack = false;
 				}
 			}
 		}
 		if (BIT.mobArenaHandler != null) {
 			if (BIT.mobArenaHandler.inRegion(player.getLocation())) {
 				canOpenBackpack = false;
 			}
 		}
 		if (BIT.jail != null) {
 			if (BIT.jail.isPlayerJailed(player.getName()) == true) {
 				canOpenBackpack = false;
 			}
 		}
 		return canOpenBackpack;
 	}
 
 	public static boolean hasWorkbench(Player player) {
 		if (BITPermissions.hasPerm(player, "backpack.workbench",
 				BITPermissions.NOT_QUIET))
 			return true;
 		File saveFile;
 		if (BITConfig.getBooleanParm("SBP.InventoriesShare."
 				+ player.getWorld().getName(), true)) {
 			saveFile = new File(BIT.plugin.getDataFolder() + File.separator
 					+ "inventories", player.getName() + ".yml");
 		} else {
 			saveFile = new File(BIT.plugin.getDataFolder() + File.separator
 					+ "inventories", player.getName() + "_"
 					+ player.getWorld().getName() + ".yml");
 		}
 		YamlConfiguration config = new YamlConfiguration();
 		try {
 			config.load(saveFile);
 		} catch (FileNotFoundException e) {
 			// BITMessages
 			// .showInfo("The workbench file did not exist for player:"
 			// + player.getName());
 			// e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		boolean enabled = config.getBoolean("Workbench", false);
 		try {
 			config.save(saveFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if (enabled)
 			return true;
 		return false;
 	}
 
 	public static void setWorkbench(Player player, boolean enabled) {
 		File saveFile;
 		if (BITConfig.getBooleanParm("SBP.InventoriesShare."
 				+ player.getWorld().getName(), true)) {
 			saveFile = new File(BIT.plugin.getDataFolder() + File.separator
 					+ "inventories", player.getName() + ".yml");
 		} else {
 			saveFile = new File(BIT.plugin.getDataFolder() + File.separator
 					+ "inventories", player.getName() + "_"
 					+ player.getWorld().getName() + ".yml");
 		}
 		YamlConfiguration config = new YamlConfiguration();
 		try {
 			config.load(saveFile);
 		} catch (FileNotFoundException e) {
 			// e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		config.set("Workbench", enabled);
 		try {
 			config.save(saveFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void loadInventory(Player player, World world) {
 		if (inventories.containsKey(player.getName())) {
 			if (inventories.get(player.getName()).length > 0) {
 				//BITMessages.showInfo("Player already has a Key assigned.");
 				 return;
 			}
 		}
 		File saveFile;
 		
 		//if (BITConfig.getBooleanParm("SBP.InventoriesShare."+ player.getWorld().getName(), true)) { //Version 3.2.5
 		//	saveFile = new File(BIT.plugin.getDataFolder() + File.separator	+ "inventories", player.getName() + ".yml");
 		
 		//} else {
 		
 		//	saveFile = new File(BIT.plugin.getDataFolder() + File.separator	+ "inventories", player.getName() + "_" + world.getName()+ ".yml");
 		
 		//}
 		
 		if (BITConfig.getBooleanParm("SBP.InventoriesShare."+ world.getName(), true)) {  // Fix for Transport Error / Shared Inventories
 			saveFile = new File(BIT.plugin.getDataFolder() + File.separator	+ "inventories", player.getName() + ".yml");
 		
 		} else {
 		
 			saveFile = new File(BIT.plugin.getDataFolder() + File.separator	+ "inventories", player.getName() + "_" + world.getName()+ ".yml");
 		
 		}
 		
 			// BITMessages.showInfo("Player Config:" +" " + saveFile +" " + world + " " +player);
 		
 			@SuppressWarnings({})
 		YamlConfiguration config = new YamlConfiguration();
 		try {
 			config.load(saveFile);
 		} catch (FileNotFoundException e) {
 			// BITMessages
 			// .showWarning("The Inventoryfile was not found for user:"
 			// + player.getName());
 			// e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		int size = BITBackpack.sizeInConfig(world, player);
 		int allowedSize = BITBackpack.allowedSize(world, player, false);
 		if (BIT.useEconomy) {
 			if (allowedSize < size) {
 				player.sendMessage("Your backpack is too big, downgrading to your permissions size.");
 				size = allowedSize;
 			}
 		} else {
 			size = allowedSize;
 		}
 		Inventory inv = SpoutManager.getInventoryBuilder().construct(size,
 				inventoryName);
 		if (saveFile.exists()) {
 			Integer i = 0;
 			for (i = 0; i < size; i++) {
 				ItemStack item = new ItemStack(0, 0);
 				item.setAmount(config.getInt(i.toString() + ".amount", 0));
 				item.setTypeId(config.getInt(i.toString() + ".type", 0));
 				Integer durability = config.getInt(
 						i.toString() + ".durability", 0);
 				item.setDurability(Short.parseShort(durability.toString()));
                 String enchant = null;
                 if((enchant = config.getString(i.toString() + ".enchant0")) != null){
                     // item.addEnchantment(Enchantment.getByName(enchant), config.getInt(i.toString() + ".level0", 0));
                 	item.addUnsafeEnchantment(Enchantment.getByName(enchant), config.getInt(i.toString() + ".level0", 0));
                 }
                 enchant = null;
                 if((enchant = config.getString(i.toString() + ".enchant1")) != null){
                     //item.addEnchantment(Enchantment.getByName(enchant), config.getInt(i.toString() + ".level1", 0));
                 	item.addUnsafeEnchantment(Enchantment.getByName(enchant), config.getInt(i.toString() + ".level1", 0));
                 }
 				inv.setItem(i, item);
 			}
 		}
 		inventories.put(player.getName(), inv.getContents());
 	}
 
 	public static Language loadLanguage() {
 		if (BITConfig.SBP_language.equalsIgnoreCase("EN")) {
 			return Language.ENGLISH;
 		} else if (BITConfig.SBP_language.equalsIgnoreCase("FR")) {
 			return Language.FRENCH;
 		} else {
 			BITMessages
 					.showInfo("SpoutBackpack: language set to ENGLISH by default.");
 			return Language.ENGLISH;
 		}
 	}
 
 }
