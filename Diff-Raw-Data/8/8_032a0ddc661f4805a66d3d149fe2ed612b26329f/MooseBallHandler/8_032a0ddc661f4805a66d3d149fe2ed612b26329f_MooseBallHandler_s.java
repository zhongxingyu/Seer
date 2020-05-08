 package net.charter.jpatterson72.MooseBall;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.Packet20NamedEntitySpawn;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 
 public class MooseBallHandler {
 	public static ArrayList<String> redTeamcue = new ArrayList<String>();
 	public static ArrayList<String> blueTeamcue = new ArrayList<String>();
 	public static ArrayList<String> redTeamMatch = new ArrayList<String>();
 	public static ArrayList<String> blueTeamMatch = new ArrayList<String>();
 	public static ArrayList<String> redTeamEleminated = new ArrayList<String>();
 	public static ArrayList<String> blueTeamEleminated = new ArrayList<String>();
 	public static ArrayList<String> blueCueClear = new ArrayList<String>();
 	public static ArrayList<String> redCueClear = new ArrayList<String>();
 	public static int matchStartTimerID;
 	public static int MooseBall15SecondTimerID;
 	public static int MooseBall5SecondTimerID;
 	public static int MooseBall1SecondTimerID;
 
 	public static boolean matchInit = false;
 	public static MooseBall plugin;
 
 	public MooseBallHandler(MooseBall instance) {
 
 		plugin = instance;
 	}
 
 	Logger log;
 
 	public static boolean applyTeamRed(Player p) {
 
 		clearMooseBall(p);
 
 		p.setMetadata("MooseBall", new FixedMetadataValue(plugin, "red"));
 
 		if (p.hasMetadata("MooseBallArmor")) {
 			String armor = p.getMetadata("MooseBallArmor").get(0).asString();
 
 			if (armor.equalsIgnoreCase("leather")) {
 
 				p.getInventory().setHelmet(
 						new ItemStack(Material.WOOL, 1, (short) 14));
 				p.getInventory().setChestplate(
 						new ItemStack(Material.LEATHER_CHESTPLATE, 1));
 				p.getInventory().setLeggings(
 						new ItemStack(Material.LEATHER_LEGGINGS, 1));
 				p.getInventory().setBoots(
 						new ItemStack(Material.LEATHER_BOOTS, 1));
 			}
 
 			else if (armor.equalsIgnoreCase("gold")) {
 				p.getInventory().setHelmet(
 						new ItemStack(Material.WOOL, 1, (short) 14));
 				p.getInventory().setChestplate(
 						new ItemStack(Material.GOLD_CHESTPLATE, 1));
 				p.getInventory().setLeggings(
 						new ItemStack(Material.GOLD_LEGGINGS, 1));
 				p.getInventory()
 						.setBoots(new ItemStack(Material.GOLD_BOOTS, 1));
 			}
 
 			else if (armor.equalsIgnoreCase("iron")) {
 				p.getInventory().setHelmet(
 						new ItemStack(Material.WOOL, 1, (short) 14));
 				p.getInventory().setChestplate(
 						new ItemStack(Material.IRON_CHESTPLATE, 1));
 				p.getInventory().setLeggings(
 						new ItemStack(Material.IRON_LEGGINGS, 1));
 				p.getInventory()
 						.setBoots(new ItemStack(Material.IRON_BOOTS, 1));
 			} else if (armor.equalsIgnoreCase("chain")) {
 				p.getInventory().setHelmet(
 						new ItemStack(Material.WOOL, 1, (short) 14));
 				p.getInventory().setChestplate(
 						new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));
 				p.getInventory().setLeggings(
 						new ItemStack(Material.CHAINMAIL_LEGGINGS, 1));
 				p.getInventory().setBoots(
 						new ItemStack(Material.CHAINMAIL_BOOTS, 1));
 			}
 
 			else if (armor.equalsIgnoreCase("diamond")) {
 				p.getInventory().setHelmet(
 						new ItemStack(Material.WOOL, 1, (short) 14));
 				p.getInventory().setChestplate(
 						new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
 				p.getInventory().setLeggings(
 						new ItemStack(Material.DIAMOND_LEGGINGS, 1));
 				p.getInventory().setBoots(
 						new ItemStack(Material.DIAMOND_BOOTS, 1));
 			}
 		}
 
 		p.getPlayer().getInventory()
 				.addItem(new ItemStack(Material.SNOW_BALL, 64));
 		p.getPlayer().getInventory()
 				.addItem(new ItemStack(Material.SNOW_BALL, 64));
 		p.getPlayer().getInventory()
 				.addItem(new ItemStack(Material.SNOW_BALL, 64));
 		String oldName = p.getName();
 		EntityPlayer changingName = ((CraftPlayer) p).getHandle();
 
 		changingName.name = "§c" + p.getName();
 
 		for (Player pInWorld : Bukkit.getServer().getOnlinePlayers()) {
 			if (pInWorld != p) {
 				((CraftPlayer) pInWorld).getHandle().netServerHandler
 						.sendPacket(new Packet20NamedEntitySpawn(changingName));
 			}
 		}
 
 		String colorName = changingName.name;
 		if (colorName.length() > 16) {
 			colorName = colorName.substring(0, 16);
 
 		}
 
 		p.setPlayerListName(colorName);
 		changingName.name = oldName;
 		if (p.hasMetadata("Eleminated")) {
 			p.removeMetadata("Eleminated", plugin);
 		}
 
 		return true;
 	}
 
 	public static boolean applyTeamBlue(Player p) {
 		clearMooseBall(p);
 		p.setMetadata("MooseBall", new FixedMetadataValue(plugin, "blue"));
 		if (p.hasMetadata("MooseBallArmor")) {
 			String armor = p.getMetadata("MooseBallArmor").get(0).asString();
 
 			if (armor.equalsIgnoreCase("leather")) {
 				p.getInventory().setHelmet(
 						new ItemStack(Material.WOOL, 1, (short) 11));
 				p.getInventory().setChestplate(
 						new ItemStack(Material.LEATHER_CHESTPLATE, 1));
 				p.getInventory().setLeggings(
 						new ItemStack(Material.LEATHER_LEGGINGS, 1));
 				p.getInventory().setBoots(
 						new ItemStack(Material.LEATHER_BOOTS, 1));
 			}
 			if (armor.equalsIgnoreCase("gold")) {
 				p.getInventory().setHelmet(
 						new ItemStack(Material.WOOL, 1, (short) 11));
 				p.getInventory().setChestplate(
 						new ItemStack(Material.GOLD_CHESTPLATE, 1));
 				p.getInventory().setLeggings(
 						new ItemStack(Material.GOLD_LEGGINGS, 1));
 				p.getInventory()
 						.setBoots(new ItemStack(Material.GOLD_BOOTS, 1));
 			}
 			if (armor.equalsIgnoreCase("iron")) {
 				p.getInventory().setHelmet(
 						new ItemStack(Material.WOOL, 1, (short) 11));
 				p.getInventory().setChestplate(
 						new ItemStack(Material.IRON_CHESTPLATE, 1));
 				p.getInventory().setLeggings(
 						new ItemStack(Material.IRON_LEGGINGS, 1));
 				p.getInventory()
 						.setBoots(new ItemStack(Material.IRON_BOOTS, 1));
 			} else if (armor.equalsIgnoreCase("chain")) {
 				p.getInventory().setHelmet(
 						new ItemStack(Material.WOOL, 1, (short) 11));
 				p.getInventory().setChestplate(
 						new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));
 				p.getInventory().setLeggings(
 						new ItemStack(Material.CHAINMAIL_LEGGINGS, 1));
 				p.getInventory().setBoots(
 						new ItemStack(Material.CHAINMAIL_BOOTS, 1));
 			}
 			if (armor.equalsIgnoreCase("diamond")) {
 				p.getInventory().setHelmet(
 						new ItemStack(Material.WOOL, 1, (short) 11));
 				p.getInventory().setChestplate(
 						new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
 				p.getInventory().setLeggings(
 						new ItemStack(Material.DIAMOND_LEGGINGS, 1));
 				p.getInventory().setBoots(
 						new ItemStack(Material.DIAMOND_BOOTS, 1));
 			}
 		}
 		p.getPlayer().getInventory()
 				.addItem(new ItemStack(Material.SNOW_BALL, 64));
 		p.getPlayer().getInventory()
 				.addItem(new ItemStack(Material.SNOW_BALL, 64));
 		p.getPlayer().getInventory()
 				.addItem(new ItemStack(Material.SNOW_BALL, 64));
 
 		String oldName = p.getName();
 		EntityPlayer changingName = ((CraftPlayer) p).getHandle();
 
 		changingName.name = "§1" + p.getName();
 
 		for (Player pInWorld : Bukkit.getServer().getOnlinePlayers()) {
 			if (pInWorld != p) {
 				((CraftPlayer) pInWorld).getHandle().netServerHandler
 						.sendPacket(new Packet20NamedEntitySpawn(changingName));
 			}
 		}
 		String colorName = changingName.name;
 		if (colorName.length() > 16) {
 			colorName = colorName.substring(0, 16);
 
 		}
 
 		p.setPlayerListName(colorName);
 		changingName.name = oldName;
 		if (p.hasMetadata("Eleminated")) {
 			p.removeMetadata("Eleminated", plugin);
 		}
 		return true;
 	}
 
 	public static void clearMooseBall(Player victim) {
 		victim.setHealth(20);
 
 		victim.getPlayer().getInventory().clear();
 		victim.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
 		victim.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 		victim.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
 		victim.getInventory().setBoots(new ItemStack(Material.AIR, 1));
 	}
 
 	public static void lobbySpawn(Player player) {
 		org.bukkit.World myworld = player.getWorld();
 		Location yourlocation = new Location(myworld, MooseBallConfig.spawnX,
 				MooseBallConfig.spawnY, MooseBallConfig.spawnZ);
 		yourlocation.setYaw(MooseBallConfig.spawnYaw);
 		player.teleport(yourlocation);
 		player.setFoodLevel(20);
 
 	}
 
 	public static void redSpawn(Player player) {
 		Random randx = new Random();
 		Random randz = new Random();
 		org.bukkit.World myworld = player.getWorld();
 		double randX = (MooseBallConfig.redX - 2) + (randx.nextInt(4));
 		double randZ = (MooseBallConfig.redZ - 2) + (randz.nextInt(4));
 		Location yourlocation = new Location(myworld, randX,
 				MooseBallConfig.redY + 1, randZ);
 		yourlocation.setYaw(MooseBallConfig.redYaw);
 		player.teleport(yourlocation);
 
 		String oldName = player.getName();
 		EntityPlayer changingName = ((CraftPlayer) player).getHandle();
 
 		changingName.name = "§c" + player.getName();
 
 		for (Player pInWorld : Bukkit.getServer().getOnlinePlayers()) {
 			if (pInWorld != player) {
 				((CraftPlayer) pInWorld).getHandle().netServerHandler
 						.sendPacket(new Packet20NamedEntitySpawn(changingName));
 			}
 		}
 
 		String colorName = changingName.name;
 		if (colorName.length() > 16) {
 			colorName = colorName.substring(0, 16);
 
 		}
 
 		player.setPlayerListName(colorName);
 		changingName.name = oldName;
 
 		player.setMetadata("MooseBallAFK", new FixedMetadataValue(plugin,
 				"true"));
 	}
 
 	public static void blueSpawn(Player player) {
 		Random randx = new Random();
 		Random randz = new Random();
 		double randX = (MooseBallConfig.blueX - 2) + (randx.nextInt(4));
 		double randZ = (MooseBallConfig.blueZ - 2) + (randz.nextInt(4));
 
 		org.bukkit.World myworld = player.getWorld();
 		Location yourlocation = new Location(myworld, randX,
 				MooseBallConfig.blueY + 1, randZ);
 		yourlocation.setYaw(MooseBallConfig.blueYaw);
 		player.teleport(yourlocation);
 
 		String oldName = player.getName();
 		EntityPlayer changingName = ((CraftPlayer) player).getHandle();
 
 		changingName.name = "§1" + player.getName();
 
 		for (Player pInWorld : Bukkit.getServer().getOnlinePlayers()) {
 			if (pInWorld != player) {
 				((CraftPlayer) pInWorld).getHandle().netServerHandler
 						.sendPacket(new Packet20NamedEntitySpawn(changingName));
 			}
 		}
 		String colorName = changingName.name;
 		if (colorName.length() > 16) {
 			colorName = colorName.substring(0, 16);
 
 		}
 
 		player.setPlayerListName(colorName);
 		changingName.name = oldName;
 
 		player.setMetadata("MooseBallAFK", new FixedMetadataValue(plugin,
 				"true"));
 	}
 
 	public static void checkTeams() {
 		int redTeam = redTeamcue.size();
 		int blueTeam = blueTeamcue.size();
 		int plyrSize = redTeam + blueTeam;
 		if (plyrSize >= (MooseBallConfig.minimumPlayers))
 
 		{
 
 			if (matchInit == false) {
 				plugin.getServer().broadcastMessage(
 						ChatColor.RED + "MooseBall: " + ChatColor.AQUA
 								+ "Next Match Begins In 30 Seconds!");
 				matchInit = true;
 
 				MooseBallStartTimer.matchStartTimer(plugin);
 				MooseBall15SecondTimer.MooseBall15SecondTimer(plugin);
 				MooseBall1SecondTimer.MooseBall1SecondTimer(plugin);
 				MooseBall5SecondTimer.MooseBall5SecondTimer(plugin);
 
 			}
 
 		}
 	}
 
 	public static void addRedCue(Player player) {
 		String playerName = player.getName();
 		if (redTeamcue.contains(playerName)) {
 			player.sendMessage(ChatColor.RED + "Already Joined Red Team");
 			return;
 		} else if (blueTeamcue.contains(playerName)) {
 			player.sendMessage(ChatColor.RED + "Already Joined Blue Team");
 			return;
 		} else if (redTeamcue.size() >= MooseBallConfig.maxPlayers) {
 			player.sendMessage(ChatColor.RED + "Red Team Full!");
 			return;
 		} else if (blueTeamMatch.contains(playerName)) {
 			player.sendMessage(ChatColor.RED + "Already Joined Blue Team");
 			return;
 		} else if (redTeamMatch.contains(playerName)) {
 			player.sendMessage(ChatColor.RED + "Already Joined Red Team");
 			return;
 		} else {
 			redTeamcue.add(playerName);
 			player.sendMessage(ChatColor.RED + "You Have Joined The Red Team");
 			checkTeams();
 			plugin.getServer().broadcastMessage(
 					ChatColor.AQUA + "[MooseBall] " + ChatColor.RED
 							+ "Red Team " + redTeamcue.size() + " Players  "
 							+ ChatColor.BLUE + "Blue Team "
 							+ blueTeamcue.size() + " Players " + ChatColor.AQUA
 							+ "Have Joined!");
 
 		}
 	}
 
 	public static void addBlueCue(Player player) {
 		String playerName = player.getName();
 		if (redTeamcue.contains(playerName)) {
 			player.sendMessage(ChatColor.RED + "Already Joined Red Team");
 			return;
 		} else if (blueTeamcue.contains(playerName)) {
 			player.sendMessage(ChatColor.RED + "Already Joined Blue Team");
 			return;
 		} else if (blueTeamcue.size() >= MooseBallConfig.maxPlayers) {
 			player.sendMessage(ChatColor.RED + "Blue Team Full!");
 			return;
 		} else if (blueTeamMatch.contains(playerName)) {
 			player.sendMessage(ChatColor.RED + "Already Joined Blue Team");
 			return;
 		} else if (redTeamMatch.contains(playerName)) {
 			player.sendMessage(ChatColor.RED + "Already Joined Red Team");
 			return;
 		} else {
 			blueTeamcue.add(playerName);
 			player.sendMessage(ChatColor.RED + "You have Joined The Blue Team");
 			checkTeams();
 			plugin.getServer().broadcastMessage(
 					ChatColor.AQUA + "[MooseBall] " + ChatColor.RED
 							+ "Red Team " + redTeamcue.size() + " Players  "
 							+ ChatColor.BLUE + "Blue Team "
 							+ blueTeamcue.size() + " Players " + ChatColor.AQUA
 							+ "Have Joined!");
 		}
 	}
 
 	public static void loadRedTeam() {
 
 		int teamCue = 0;
 		if (redTeamcue.size() > MooseBallConfig.maxPlayers) {
 			teamCue = MooseBallConfig.maxPlayers;
 		} else {
 			teamCue = redTeamcue.size();
 		}
 
 		for (int i = 0; i < teamCue; i++) {
 			String playerName = (redTeamcue.get(i));
 			if ((plugin.getServer().getPlayer(playerName)) != null)
 
 			{
 
 				Player player = plugin.getServer().getPlayer(playerName);
 
 				if (!(redTeamMatch.contains(playerName))
 						&& (!(blueTeamMatch.contains(playerName)))) {
 					player.setMetadata("MooseBallAFK", new FixedMetadataValue(
 							plugin, "true"));
 					redTeamMatch.add(playerName);
 					redSpawn(player);
 					applyTeamRed(player);
 					MooseBallAFKInitTimer.MooseBallAFKInitTimer(plugin, player);
 				}
 				MooseBallRemoveTimerRed.MooseBallRemoveTimerRed(player, plugin);
 
 			}
 
 		}
 		redTeamcue.clear();
 	}
 
 	public static void loadBlueTeam() {
 
 		int teamCue = 0;
 		if (blueTeamcue.size() > MooseBallConfig.maxPlayers) {
 			teamCue = MooseBallConfig.maxPlayers;
 		} else {
 			teamCue = blueTeamcue.size();
 		}
 
 		for (int i = 0; i < teamCue; i++) {
 			String playerName = (blueTeamcue.get(i));
 			if ((plugin.getServer().getPlayer(playerName)) != null)
 
 			{
 				Player player = plugin.getServer().getPlayer(playerName);
 				if (!(redTeamMatch.contains(playerName))
 						&& (!(blueTeamMatch.contains(playerName)))) {
 					player.setMetadata("MooseBallAFK", new FixedMetadataValue(
 							plugin, "true"));
 					blueTeamMatch.add(playerName);
 					blueSpawn(player);
 					applyTeamBlue(player);
 					MooseBallAFKInitTimer.MooseBallAFKInitTimer(plugin, player);
 				}
 
 			}
 
 		}
 		blueTeamcue.clear();
 	}
 
 	public static void normalizeTeams() {
 
 		int redTeam = redTeamcue.size();
 		int blueTeam = blueTeamcue.size();
 
 		if (redTeam > blueTeam) {
 			int teamOffset = (redTeam - blueTeam);
 			if (teamOffset > 1) {
 				teamOffset = teamOffset / 2;
 
 				for (int i = 0; i < teamOffset; i++) {
 
 					String playerName = (redTeamcue
 							.get((redTeamcue.size() - i - 1)));
 
 					if ((plugin.getServer().getPlayer(playerName)) != null)
 
 					{
 						if (!(blueTeamcue.contains(playerName))) {
 							blueTeamcue.add(playerName);
 
 							MooseBallRemoveTimerRedCue
 									.MooseBallRemoveTimerRedCue(playerName,
 											plugin);
 						}
 					}
 				}
 				return;
 			}
 
 		}
 		if (blueTeam > redTeam) {
 			int teamOffset = (blueTeam - redTeam);
 			if (teamOffset > 1) {
 				teamOffset = teamOffset / 2;
 
 				for (int i = 0; i < teamOffset; i++)
 
 				{
 
 					String playerName = (blueTeamcue.get((blueTeamcue.size()
 							- i - 1)));
 					if ((plugin.getServer().getPlayer(playerName)) != null) {
 						if (!(redTeamcue.contains(playerName))) {
 							redTeamcue.add(playerName);
 
 							MooseBallRemoveTimerBlueCue
 									.MooseBallRemoveTimerBlueCue(playerName,
 											plugin);
 						}
 					}
 				}
 				return;
 			}
 
 		}
 		return;
 	}
 
 	public static void checkWin() {
 		if (matchInit == true) {
 
 			if ((blueTeamMatch.size() == 0) || (redTeamMatch.size() == 0)) {
 				if (blueTeamMatch.size() == 0) {
 
 					plugin.getServer()
 							.broadcastMessage(
 									ChatColor.AQUA + "The " + ChatColor.RED
 											+ "RED" + ChatColor.AQUA
 											+ " Team is Victorious!!!");
 
 					for (int i = 0; i < redTeamMatch.size(); i++) {
 						String playerName = (redTeamMatch.get(i));
 
 						if ((plugin.getServer().getPlayer(playerName)) != null)
 
 						{
 							Player player = plugin.getServer().getPlayer(
 									playerName);
 							MooseBallHandler.clearMooseBall(player);
 							MooseBallHandler.lobbySpawn(player);
 							MooseBallHandler.deColorize(player);
 							player.sendMessage(ChatColor.AQUA
 									+ "Winning Team: 2 Points Awarded!!");
 							int MooseBalls = player.getMetadata("MooseBalls")
 									.get(0).asInt();
 							MooseBalls = MooseBalls + 2;
 							player.setMetadata("MooseBalls",
 									new FixedMetadataValue(plugin, MooseBalls));
 							player.removeMetadata("MooseBall", plugin);
 							if (!(redTeamcue.contains(playerName))) {
 								redTeamcue.add(playerName);
 
 								player.sendMessage(ChatColor.RED
 										+ "Rejoined Red Team");
 							}
 
 						}
 
 					}
 
 					for (int i = 0; i < redTeamEleminated.size(); i++) {
 						String playerName = (redTeamEleminated.get(i));
 
 						if ((plugin.getServer().getPlayer(playerName)) != null)
 
 						{
 							Player player = plugin.getServer().getPlayer(
 									playerName);
 
 							player.sendMessage(ChatColor.AQUA
 									+ "Winning Team: 2 Points Awarded!!");
 							int MooseBalls = player.getMetadata("MooseBalls")
 									.get(0).asInt();
 							MooseBalls = MooseBalls + 2;
 							player.setMetadata("MooseBalls",
 									new FixedMetadataValue(plugin, MooseBalls));
 							player.removeMetadata("MooseBall", plugin);
 							if (!(redTeamcue.contains(playerName))
 									&& (!(blueTeamcue.contains(playerName)))) {
 								redTeamcue.add(playerName);
 
 								player.sendMessage(ChatColor.RED
 										+ "Rejoined Red Team");
 							}
 						}
 
 					}
 					for (int i = 0; i < blueTeamEleminated.size(); i++) {
 						String playerName = (blueTeamEleminated.get(i));
 
 						if ((plugin.getServer().getPlayer(playerName)) != null)
 
 						{
 							Player player = plugin.getServer().getPlayer(
 									playerName);
 
 							player.removeMetadata("MooseBall", plugin);
 							if (!(redTeamcue.contains(playerName))
 									&& (!(blueTeamcue.contains(playerName)))) {
 								blueTeamcue.add(playerName);
 
 								player.sendMessage(ChatColor.BLUE
 										+ "Rejoined Blue Team");
 							}
 						}
 
 					}
 
 				} else if (redTeamMatch.size() == 0)
 
 				{
 					matchInit = false;
 					plugin.getServer()
 							.broadcastMessage(
 									ChatColor.AQUA + "The " + ChatColor.BLUE
 											+ "BlUE" + ChatColor.AQUA
 											+ " Team is Victorious!!!");
 					for (int i = 0; i < blueTeamMatch.size(); i++) {
 						String playerName = (blueTeamMatch.get(i));
 
 						if ((plugin.getServer().getPlayer(playerName)) != null)
 
 						{
 							Player player = plugin.getServer().getPlayer(
 									playerName);
 							MooseBallHandler.clearMooseBall(player);
 							MooseBallHandler.lobbySpawn(player);
 							MooseBallHandler.deColorize(player);
 							player.sendMessage(ChatColor.AQUA
 									+ "Winning Team: 2 Points Awarded!!");
 							int MooseBalls = player.getMetadata("MooseBalls")
 									.get(0).asInt();
 							MooseBalls = MooseBalls + 2;
 							player.setMetadata("MooseBalls",
 									new FixedMetadataValue(plugin, MooseBalls));
 							player.removeMetadata("MooseBall", plugin);
 							if (!(redTeamcue.contains(playerName))
 									&& (!(blueTeamcue.contains(playerName)))) {
 								blueTeamcue.add(playerName);
 
 								player.sendMessage(ChatColor.BLUE
 										+ "Rejoined Blue Team");
 							}
 						}
 
 					}
 					for (int i = 0; i < blueTeamEleminated.size(); i++) {
 						String playerName = (blueTeamEleminated.get(i));
 
 						if ((plugin.getServer().getPlayer(playerName)) != null)
 
 						{
 							Player player = plugin.getServer().getPlayer(
 									playerName);
 
 							player.sendMessage(ChatColor.AQUA
 									+ "Winning Team: 2 Points Awarded!!");
 							int MooseBalls = player.getMetadata("MooseBalls")
 									.get(0).asInt();
 							MooseBalls = MooseBalls + 2;
 							player.setMetadata("MooseBalls",
 									new FixedMetadataValue(plugin, MooseBalls));
 							player.removeMetadata("MooseBall", plugin);
 							if (!(redTeamcue.contains(playerName))
 									&& (!(blueTeamcue.contains(playerName)))) {
 								blueTeamcue.add(playerName);
 
 								player.sendMessage(ChatColor.BLUE
 										+ "Rejoined Blue Team");
 							}
 						}
 
 					}
 
 					for (int i = 0; i < redTeamEleminated.size(); i++) {
 						String playerName = (redTeamEleminated.get(i));
 
 						if ((plugin.getServer().getPlayer(playerName)) != null)
 
 						{
 							Player player = plugin.getServer().getPlayer(
 									playerName);
 
 							player.removeMetadata("MooseBall", plugin);
 							if (!(redTeamcue.contains(playerName))
 									&& (!(blueTeamcue.contains(playerName)))) {
 								redTeamcue.add(playerName);
 
 								player.sendMessage(ChatColor.RED
 										+ "Rejoined Red Team");
 							}
 
 						}
 					}
 
 				}
 				matchInit = false;
 
 				MooseBallResetTimer.MooseBallResetTimer(plugin);
 			}
 		}
 	}
 
 	public static void deColorize(Player p) {
 
 		EntityPlayer changingName = ((CraftPlayer) p).getHandle();
 
 		changingName.name = p.getName();
 		p.setPlayerListName(p.getName());
 		for (Player pInWorld : Bukkit.getServer().getOnlinePlayers()) {
 			if (pInWorld != p) {
 				((CraftPlayer) pInWorld).getHandle().netServerHandler
 						.sendPacket(new Packet20NamedEntitySpawn(changingName));
 			}
 
 		}
 
 	}
 
 	public static void teamLeave(Player player) {
 
 		String playerName = player.getName();
 
 		if (redTeamcue.contains(playerName)) {
 			redTeamcue.remove(playerName);
 			player.sendMessage(ChatColor.RED
 					+ "Removed From Red Team Waiting List");
 		}
 
 		if (blueTeamcue.contains(playerName)) {
 			blueTeamcue.remove(playerName);
 			player.sendMessage(ChatColor.RED
 					+ "Removed From Blue Team Waiting List");
 		}
 
 		if (blueTeamMatch.contains(playerName)) {
 			if (player.hasMetadata("Eleminated")) {
 				blueTeamMatch.remove(playerName);
 			} else {
 				blueTeamMatch.remove(playerName);
 				MooseBallHandler.clearMooseBall(player);
 				MooseBallHandler.lobbySpawn(player);
 				MooseBallHandler.deColorize(player);
 				MooseBallHandler.checkWin();
 			}
 		}
 		if (redTeamMatch.contains(playerName)) {
 			if (player.hasMetadata("Eleminated")) {
 				redTeamMatch.remove(playerName);
 			} else {
 				redTeamMatch.remove(playerName);
 				MooseBallHandler.clearMooseBall(player);
 				MooseBallHandler.lobbySpawn(player);
 				MooseBallHandler.deColorize(player);
 				MooseBallHandler.checkWin();
 			}
 		}
 		player.removeMetadata("MooseBall", plugin);
 	}
 
 	public static void eleminatePlayer(Player player) {
 		String playerName = player.getName();
 
 		if (player.hasMetadata("MooseBall")) {
 
 			String teamColor = player.getMetadata("MooseBall").get(0)
 					.asString();
 			if (teamColor.equalsIgnoreCase("red")) {
 
 				if (redTeamMatch.contains(playerName)) {
 
 					redTeamEleminated.add(playerName);
 					redTeamMatch.remove(playerName);
 				}
 			}
 
 			if (teamColor.equalsIgnoreCase("blue")) {
 				if (blueTeamMatch.contains(playerName)) {
 					blueTeamEleminated.add(playerName);
 					blueTeamMatch.remove(playerName);
 				}
 			}
 		}
 	}
 
 	public static void MooseBallReset() {
 		Player[] playerlist = plugin.getServer().getOnlinePlayers();
 		int listsize = plugin.getServer().getOnlinePlayers().length;
 		for (int i = 0; i < listsize; i++) {
 			Player replayer = playerlist[i];
 			if (replayer.hasMetadata("MooseBall")) {
 
 				MooseBallHandler.clearMooseBall(replayer);
 				MooseBallHandler.lobbySpawn(replayer);
 				MooseBallHandler.deColorize(replayer);
 				MooseBallHandler.eleminatePlayer(replayer);
 				MooseBallHandler.checkWin();
 				redTeamMatch.clear();
 				blueTeamMatch.clear();
 				redTeamcue.clear();
 				blueTeamcue.clear();
 				redTeamEleminated.clear();
 				blueTeamEleminated.clear();
 				
 			}
 		}
 	}
 
 }
