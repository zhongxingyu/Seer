 package net.craftrepo.Gimme;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandException;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.*;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
  * CraftRepo gimme for Bukkit
  * @author AllGamer
  * 
  * Copyright 2011 AllGamer, LLC.
  * See LICENSE for licensing information.
  */
 
 public class gimme extends JavaPlugin
 {
 	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 	public HashMap<String, Integer> items = new HashMap<String, Integer>();
 	private final Logger log = Logger.getLogger("Minecraft");
 	public static PermissionHandler Permissions = null;
 	public static String logPrefix = "[Gimme]";
 	private gimmeConfiguration confSetup;
 	public gimme plugin;
 	public static Configuration config;
 	public static String id = null;
 	public int amount = 64;
 
 	public void populateItemMap()
 	{
 		items.put("stone", 1);
 		items.put("smoothstone", 1);
 		items.put("grass", 2);
 		items.put("dirt", 3);
 		items.put("cobble", 4);
 		items.put("cobblestone", 4);
 		items.put("wood", 5);
 		items.put("plank", 5);
 		items.put("planks", 5);
 		items.put("sapling", 6);
 		items.put("bedrock", 7);
 		items.put("adminium", 7);
 		items.put("water", 8);
 		items.put("movingwater", 8);
 		items.put("stationarywater", 9);
 		items.put("stillwater", 9);
 		items.put("swater", 9);
 		items.put("lava", 10);
 		items.put("movinglava", 10);
 		items.put("stationarylava", 11);
 		items.put("slava", 11);
 		items.put("movinglava", 11);
 		items.put("sand", 12);
 		items.put("gravel", 13);
 		items.put("goldore", 14);
 		items.put("ironore", 15);
 		items.put("coalore", 16);
 		items.put("log", 17);
 		items.put("logs", 17);
 		items.put("leaves", 18);
 		items.put("leaf", 18);
 		items.put("sponge", 19);
 		items.put("yellowsponge", 19);
 		items.put("glass", 20);
 		items.put("lapislazuliore", 21);
 		items.put("lapislazuliblock", 22);
 		items.put("lapislazuliblocks", 22);
 		items.put("dispenser", 23);
 		items.put("dispensers", 23);
 		items.put("sandstone", 24);
 		items.put("noteblock", 25);
 		items.put("musicblock", 25);
 		items.put("bed", 26);
 		items.put("beds", 26);
 		items.put("poweredrail", 27);
 		items.put("powerrail", 27);
 		items.put("booster", 27);
 		items.put("detectorrail", 28);
 		items.put("web", 30);
 		items.put("spiderweb", 30);
 		items.put("wool", 35);
 		items.put("cloth", 35);
 		items.put("whitewool", 35);
 		items.put("yellowflower", 37);
 		items.put("yellowflowers", 37);
 		items.put("redrose", 38);
 		items.put("redroses", 38);
 		items.put("redflower", 38);
 		items.put("redflowers", 38);
 		items.put("brownmushroom", 39);
 		items.put("brownmushrooms", 39);
 		items.put("redmushroom", 40);
 		items.put("redmushrooms", 40);
 		items.put("goldblock", 41);
 		items.put("goldblocks", 41);
 		items.put("gold", 41);
 		items.put("iron", 42);
 		items.put("ironblock", 42);
 		items.put("ironblocks", 42);
 		items.put("doublestep", 43);
 		items.put("doubleslab", 43);
 		items.put("slab", 44);
 		items.put("singleslab", 44);
 		items.put("step", 44);
 		items.put("singlestep", 44);
 		items.put("brickblock", 45);
 		items.put("brick", 45);
 		items.put("bricks", 45);
 		items.put("tnt", 46);
 		items.put("dynamite", 46);
 		items.put("tntblock", 46);
 		items.put("bookshelf", 47);
 		items.put("shelf", 47);
 		items.put("bookshelfs", 47);
 		items.put("shelfs", 47);
 		items.put("mossstone", 48);
 		items.put("mossy", 48);
 		items.put("mossycobble", 48);
 		items.put("mossycobblestone", 48);
 		items.put("obsidian", 49);
 		items.put("torch", 50);
 		items.put("torches", 50);
 		items.put("coaltorch", 50);
 		items.put("fire", 51);
 		items.put("flames", 51);
 		items.put("monsterspawner", 52);
 		items.put("mobspawner", 52);
 		items.put("monsterspawners", 52);
 		items.put("mobspawners", 52);
 		items.put("woodenstairs", 53);
 		items.put("woodstairs", 53);
 		items.put("woodenstair", 53);
 		items.put("woodstair", 53);
 		items.put("chest", 54);
 		items.put("chests", 54);
 		items.put("redstonewire", 55);
 		items.put("diamondore", 56);
 		items.put("diamondblock", 56);
 		items.put("diamondblocks", 56);
 		items.put("diamond", 56);
 		items.put("craftingtable", 58);
 		items.put("crafttable", 58);
 		items.put("workbench", 58);
 		items.put("workbenches", 58);
 		items.put("crops", 59);
 		items.put("crop", 59);
 		items.put("farmland", 60);
 		items.put("furnace", 61);
 		items.put("furnaces", 61);
 		items.put("burningfurnace", 62);
 		items.put("burningfurnaces", 62);
 		items.put("signpost", 63);
 		items.put("signposts", 63);
 		items.put("signblock", 63);
 		items.put("signblocks", 63);
 		items.put("woodenhalfdoor", 64);
 		items.put("woodhalfdoor", 64);
 		items.put("ladder", 65);
 		items.put("ladders", 65);
 		items.put("rails", 66);
 		items.put("tracks", 66);
 		items.put("minecarttracks", 66);
 		items.put("carttracks", 66);
 		items.put("cobblestonestairs", 67);
 		items.put("cobblestonestair", 67);
 		items.put("cobblestair", 67);
 		items.put("cobblestairs", 67);
 		items.put("wallsign", 68);
 		items.put("wallsigns", 68);
 		items.put("lever", 69);
 		items.put("stonelever", 69);
 		items.put("stonepressureplate", 70);
 		items.put("stoneplate", 70);
 		items.put("stoneplates", 70);
 		items.put("stonepressureplates", 70);
 		items.put("ironhalfdoor", 71);
 		items.put("ironhalfdoors", 71);
 		items.put("woodenpressureplate", 72);
 		items.put("woodenpressureplates", 72);
 		items.put("woodpressureplate", 72);
 		items.put("woodpressureplates", 72);
 		items.put("woodplate", 72);
 		items.put("woodplates", 72);
 		items.put("woodenplate", 72);
 		items.put("woodenplates", 72);
 		items.put("redstoneore", 73);
 		items.put("glowingredstoneore", 73);
 		items.put("redstonetorchon", 76);
 		items.put("redstonetorchoff", 75);
 		items.put("redstonetorch", 76);
 		items.put("stonebutton", 77);
 		items.put("button", 77);
 		items.put("snow", 78);
 		items.put("ice", 79);
 		items.put("iceblock", 79);
 		items.put("iceblocks", 79);
 		items.put("snowblock", 80);
 		items.put("snowblocks", 80);
 		items.put("cactus", 81);
 		items.put("cactusblock", 81);
 		items.put("cactusblocks", 81);
 		items.put("clayblock", 82);
 		items.put("clayblocks", 82);
 		items.put("sugarcaneblock", 83);
 		items.put("sugarcaneblocks", 83);
 		items.put("jukebox", 84);
 		items.put("jukeboxblock", 84);
 		items.put("fence", 85);
 		items.put("pumpkin", 86);
 		items.put("pumpkins", 86);
 		items.put("netherrack", 87);
 		items.put("netherstone", 87);
 		items.put("netherrock", 87);
 		items.put("soulsand", 88);
 		items.put("slowsand", 88);
 		items.put("glowstoneblock", 89);
 		items.put("glowstoneblocks", 89);
 		items.put("portal", 90);
 		items.put("portals", 90);
 		items.put("portalblock", 90);
 		items.put("portalblocks", 90);
 		items.put("jackolantern", 91);
 		items.put("jackolanterns", 91);
 		items.put("cakeblock", 92);
 		items.put("cakeblocks", 92);
 		items.put("redstonerepeateroff", 93);
 		items.put("redstonerepeateron", 94);
 		items.put("ironspade", 256); 
 		items.put("ironpickaxe", 257);
 		items.put("ironpick", 257); 
 		items.put("ironaxe", 258);
 		items.put("ironhatchet", 258);
 		items.put("flintandsteel", 259);
 		items.put("lighter", 259);
 		items.put("apple", 260);
 		items.put("apples", 260);
 		items.put("bow", 261);
 		items.put("arrow", 262);
 		items.put("arrows", 262);
 		items.put("coal", 263);
 		items.put("diamonds", 264);
 		items.put("ironingot", 265);
 		items.put("ironbar", 265);
 		items.put("goldingot", 266);
 		items.put("goldbar", 266);
 		items.put("ironsword", 267);
 		items.put("woodsword", 268);
 		items.put("woodensword", 268);
 		items.put("woodshovel", 269);
 		items.put("woodspade", 269);
 		items.put("woodpickaxe", 270);
 		items.put("woodpick", 270);
 		items.put("woodaxe", 271);
 		items.put("woodhatchet", 271);
 		items.put("stonesword", 272);
 		items.put("stoneshovel", 273);
 		items.put("stonespade", 273);
 		items.put("stonepickaxe", 274);
 		items.put("stonepick", 274);
 		items.put("stoneaxe", 275);
 		items.put("stonehatchet", 275);
 		items.put("diamondsword", 276);
 		items.put("diamondshovel", 277);
 		items.put("diamondspade", 277);
 		items.put("diamondpickaxe", 278);
 		items.put("diamondpick", 278);
 		items.put("diamondaxe", 279);
 		items.put("diamondhatchet", 279);
 		items.put("stick", 280);
 		items.put("sticks", 280);
 		items.put("bowl", 281);
 		items.put("woodbowl", 281);
 		items.put("mushroomsoup", 282);
 		items.put("goldsword", 283);
 		items.put("goldshovel", 284);
 		items.put("goldspade", 284);
 		items.put("goldpickaxe", 285);
 		items.put("goldpick", 285);
 		items.put("goldaxe", 286);
 		items.put("goldhatchet", 286);
 		items.put("string", 287);
 		items.put("thread", 287);
 		items.put("feather", 288);
 		items.put("feathers", 288);
 		items.put("gunpowder", 289);
 		items.put("woodhoe", 290);
 		items.put("stonehoe", 291);
 		items.put("ironhoe", 292);
 		items.put("diamondhoe", 293);
 		items.put("goldhoe", 294);
 		items.put("seed", 295);
 		items.put("seeds", 295);
 		items.put("wheat", 296);
 		items.put("bread", 297);
 		items.put("leatherhelmet", 298);
 		items.put("leatherchest", 299);
 		items.put("leatherchestplate", 299);
 		items.put("leathertop", 299);
 		items.put("leatherleggings", 300);
 		items.put("leatherlegs", 300);
 		items.put("leatherpants", 300);
 		items.put("leatherboots", 301);
 		items.put("leatherboot", 301);
 		items.put("leathershoe", 301);
 		items.put("leathershoes", 301);
 		items.put("leatherfeet", 301);
 		items.put("chainmailhelmet", 302);
 		items.put("chainhelmet",302);
 		items.put("chainmailchestplate", 303);
 		items.put("chainmailchest", 303);
 		items.put("chainchest", 303);
 		items.put("chaintop", 303);
 		items.put("chainmailleggings", 304);
 		items.put("chainmaillegs", 304);
 		items.put("chainmailpants", 304);
 		items.put("chainmailboots", 305);
 		items.put("chainboots", 305);
 		items.put("chainmailfeet", 305);
 		items.put("chainfeet", 305);
 		items.put("chainshoe",305);
 		items.put("chainshoes",305);
 		items.put("chainmailshoes",305);
 		items.put("chainmailshoe",305);
 		items.put("ironhelmet", 306);
 		items.put("ironchestplate", 307);
 		items.put("ironchest", 307);
 		items.put("irontop", 307);
 		items.put("ironleggings", 308);
 		items.put("ironpants", 308);
 		items.put("ironlegs", 308);
 		items.put("ironboots", 309);
 		items.put("ironboot", 309);
 		items.put("ironfeet", 309);
 		items.put("ironshoe", 309);
 		items.put("ironshoes", 309);
 		items.put("diamondhelmet", 310);
 		items.put("diamondchestplate", 311);
 		items.put("diamondchest", 311);
 		items.put("diamondtop", 311);
 		items.put("diamondleggings", 312);
 		items.put("diamondlegs", 312);
 		items.put("diamondleg", 312);
 		items.put("diamondpants", 312);
 		items.put("diamondboots", 313);
 		items.put("diamondboot", 313);
 		items.put("diamondfeet", 313);
 		items.put("diamondshoe", 313);
 		items.put("diamondshoes", 313);
 		items.put("goldhelmet", 314);
 		items.put("goldchestplate", 315);
 		items.put("goldchest", 315);
 		items.put("goldtop", 315);
 		items.put("goldleggings", 316);
 		items.put("goldlegs", 316);
 		items.put("goldleg", 316);
 		items.put("goldpants", 316);
 		items.put("goldboots", 317);
 		items.put("goldboot", 317);
 		items.put("goldfeet", 317);
 		items.put("goldshoe", 317);
 		items.put("goldshoes", 317);
 		items.put("flint", 318);
 		items.put("rawporkchop", 319);
 		items.put("rawporkchops", 319);
 		items.put("rawpork", 319);
 		items.put("rawporks", 319);
 		items.put("cookedporkchop", 320);
 		items.put("cookedporkchops", 320);
 		items.put("cookedpork", 320);
 		items.put("cookedporks", 320);
 		items.put("paintings", 321);
 		items.put("painting", 321);
 		items.put("goldenapple", 322);
 		items.put("goldapple", 322);
 		items.put("sign", 323);
 		items.put("signs", 323);
 		items.put("woodendoor", 324);
 		items.put("wooddoor", 324);
 		items.put("bucket", 325);
 		items.put("ironbucket", 325);
 		items.put("waterbucket", 326);
 		items.put("waterbuckets", 326);
 		items.put("bucketofwater", 326);
 		items.put("lavabucket", 327);
 		items.put("lavabuckets", 327);
 		items.put("bucketoflava", 327);
 		items.put("minecart", 328);
 		items.put("minecarts", 328);
 		items.put("cart", 328);
 		items.put("carts", 328);
 		items.put("saddle", 329);
 		items.put("saddles", 329);
 		items.put("leathersaddle", 329);
 		items.put("leathersaddles", 329);
 		items.put("irondoor", 330);
 		items.put("irondoors", 330);
 		items.put("redstone", 331);
 		items.put("redstonedust", 331);
 		items.put("snowball", 332);
 		items.put("snowballs", 332);
 		items.put("boat", 333);
 		items.put("woodboat", 333);
 		items.put("woodenboat", 333);
 		items.put("leather", 334);
 		items.put("leatherhide", 334);
 		items.put("leatherhides", 334);
 		items.put("milk", 335);
 		items.put("milkbucket", 335);
 		items.put("bucketofmilk", 335);
 		items.put("claybrick", 336);
 		items.put("brickbar", 336);
 		items.put("brickbars", 336);
 		items.put("clayballs", 337);
 		items.put("clayball", 337);
 		items.put("clay", 337);
 		items.put("sugarcane", 338);
 		items.put("paper", 339);
 		items.put("book", 340);
 		items.put("books", 340);
 		items.put("slimeball", 341);
 		items.put("slimeballs", 341);
 		items.put("storageminecart", 342);
 		items.put("storagecart", 342);
 		items.put("poweredminecart", 343);
 		items.put("poweredcart", 343);
 		items.put("egg", 344);
 		items.put("eggs", 344);
 		items.put("rawegg", 344);
 		items.put("raweggs", 334);
 		items.put("compass", 345);
 		items.put("fishingrod", 346);
 		items.put("fishingpole", 346);
 		items.put("fishingstick", 346);
 		items.put("clock", 347);
 		items.put("glowstonedust", 348);
 		items.put("yellowdust", 348);
 		items.put("lightstonedust", 348);
 		items.put("rawfish", 349);
 		items.put("cookedfish", 356);
 		items.put("blackdye", 351);
 		items.put("squiddye", 351);
 		items.put("inksac", 351);
 		items.put("bones", 352);
 		items.put("sugar", 353);
 		items.put("cake", 354);
 		items.put("cakes", 354);
 		items.put("bedblock", 355);
 		items.put("redstonerepeater", 356);
 		items.put("repeater", 356);
 		items.put("repeaters", 356);
 		items.put("diode", 356);
 		items.put("cookie", 357);
 		items.put("cookies", 357);
 		items.put("cookie", 357);
 		items.put("cooky", 357);
 		items.put("cookies", 357);
 		items.put("goldmusicdisc", 2256);
 		items.put("golddisc", 2256);
 		items.put("greenmusicdisc", 2257);
 		items.put("greendisc", 2257);
 	}
 
 	public void configInit()
 	{
 		getDataFolder().mkdirs();
 		config = new Configuration(new File(this.getDataFolder(), "config.yml"));
 		confSetup = new gimmeConfiguration(this.getDataFolder(), this);
 	}
 
 	public void setupPermissions() 
 	{
 		Plugin perms = this.getServer().getPluginManager().getPlugin("Permissions");
 		PluginDescriptionFile pdfFile = this.getDescription();
 
 		if (gimme.Permissions == null) 
 		{
 			if (perms != null) 
 			{
 				this.getServer().getPluginManager().enablePlugin(perms);
 				gimme.Permissions = ((Permissions) perms).getHandler();
 				log.info(logPrefix + " version " + pdfFile.getVersion() + " Permissions detected...");
 			}
 			else 
 			{
 				log.severe(logPrefix + " version " + pdfFile.getVersion() + " not enabled. Permissions not detected.");
 				this.getServer().getPluginManager().disablePlugin(this);
 			}
 		}
 	}
 
 	public static String strip(String s) 
 	{
 		String good = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
 		String result = "";
 		for ( int i = 0; i < s.length(); i++ ) 
 		{
 			if ( good.indexOf(s.charAt(i)) >= 0 )
 				result += s.charAt(i);
 		}
 		return result;
 	}
 
 	public boolean itemDeny(int args)
 	{
 		gimme.config.load();
 		String x = gimme.config.getProperty("denied").toString();
 		String[] blacklist = x.split(" ");
 		String arg = Integer.toString(args);
 		if (arg.contains(":"))
 		{
 			String clone = arg;
 			String[] split = clone.split(":");
 			int item = Integer.parseInt(split[0]);
 			for (String s : blacklist)
 			{
 				String black = strip(s);
 				if (Integer.parseInt(black) == item)
 				{
 					return true;
 				}
 			}
 			return false;
 		}
 		else
 		{
 			for (String s : blacklist)
 			{
 				String black = strip(s);
 				if (Integer.parseInt(black) == args)
 				{
 					return true;
 				}
 			}
 			return false;
 		}
 	}
 
 	public boolean itemAllow(int args)
 	{
 		gimme.config.load();
 		String x = gimme.config.getProperty("allowed").toString();
 		String[] whitelist = x.split(" ");
 		String arg = Integer.toString(args);
 
 		if (arg.contains(":"))
 		{
 			String clone = arg;
 			String[] split = clone.split(":");
 			int item = Integer.parseInt(split[0]);
 			for (String s : whitelist)
 			{
 				String white = strip(s);
 				if (!(Integer.parseInt(white) == item))
 				{
 					return true;
 				}
 			}
 			return false;
 		}
 		else
 		{
 			for (String s : whitelist)
 			{
 				String white = strip(s);
 				if (!(Integer.parseInt(white) == args))
 				{
 					return true;
 				}
 			}
 			return false;
 		}
 	}
 
 	public void giveItemId(String item, String amount, Player player)
 	{
 		ItemStack itemstack;
 		String clone = item;
 		PlayerInventory inventory = player.getInventory();
 		if (item.contains(":"))
 		{
 			String[] split = clone.split(":");
 			itemstack = new ItemStack(Integer.parseInt(strip(split[0])));
 			itemstack.setDurability(Short.parseShort(split[1]));
 			itemstack.setAmount(Integer.valueOf(amount));
 			player.sendMessage("Here you go!");
 			inventory.addItem(itemstack);
 		}
 		else
 		{
 			itemstack = new ItemStack(Integer.parseInt(strip(item)));
 			itemstack.setAmount(Integer.parseInt(amount));
 			player.sendMessage("Here you go!");
 			inventory.addItem(itemstack);
 		}
 	}
 
 	public void giveItemName(String item, String amount, Player player)
 	{
 		ItemStack itemstack;
 		String clone = item;
 		PlayerInventory inventory = player.getInventory();
 		if (item.contains(":"))
 		{
 			String[] split = clone.split(":");
			if(!items.containsKey(split[0].toLowerCase()))
 			{
 				itemstack = new ItemStack(items.get(split[0].toLowerCase()));
 				itemstack.setDurability(Short.parseShort(split[1]));
 				player.sendMessage("Here you go!");
 				itemstack.setAmount(Integer.parseInt(amount));
 				inventory.addItem(itemstack);
 			}
 			else
 			{
 				player.sendMessage("No such item " + split[0]);
 			}
 		}
 		else
 		{
			if(!items.containsKey(item.toLowerCase()))
 			{
 				itemstack = new ItemStack(items.get(item.toLowerCase()));
 				itemstack.setAmount(Integer.parseInt(amount));
 				player.sendMessage("Here you go!");
 				inventory.addItem(itemstack);
 			}
 			else
 			{
 				player.sendMessage("No such item " + item);
 			}
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command commandArg, String commandLabel, String[] arg) 
 	{
 		try
 		{
 			Player player = (Player) sender;
 			String command = commandArg.getName().toLowerCase();
 			try
 			{
 				if (command.equalsIgnoreCase("gimme")) 
 				{
 					if (player.isOp() || gimme.Permissions.has(player, "gimme.gimme"))
 					{
 						if (arg.length >= 1 && arg.length <= 2)
 						{
 							Pattern p = Pattern.compile("[-]?[0-9]+");
 							Matcher m = p.matcher(strip(arg[0]));		
 							if (m.matches())
 							{
 								if (arg.length == 2)
 								{
 									giveItemId(arg[0], arg[1], player);
 								}
 								else
 								{
 									giveItemId(arg[0], "64", player);
 								}
 							}
 							else
 							{
 								if (arg.length == 2)
 								{
 									giveItemName(arg[0], arg[1], player);
 								}
 								else
 								{
 									giveItemName(arg[0], "64", player);
 								}
 							}
 						}
 						else
 						{
 							player.sendMessage("Correct usage is /gimme [item] {amount}");
 						}
 					}
 					if (gimme.Permissions.has(player, "gimme.blacklist")) 
 					{
 						if (!gimme.Permissions.has(player, "gimme.gimme"))
 						{
 							if (arg.length >= 1 && arg.length <= 2)
 							{
 								Pattern p = Pattern.compile("[-]?[0-9]+");
 								Matcher m = p.matcher(arg[0]);
 								if (m.matches())
 								{
 									if (!(itemDeny(Integer.valueOf(arg[0]))))
 									{
 										if (arg[1] != null)
 										{
 											giveItemId(arg[0], arg[1], player);
 										}
 										else
 										{
 											giveItemId(arg[0], "64", player);
 										}
 									}
 									else
 									{
 										player.sendMessage(logPrefix + " You aren't allowed to get that item!");
 										log.info(logPrefix + player.getDisplayName() + " tried to get " + arg[0].toString());
 									}
 								}
 								else
 								{
 									if (!(itemDeny(items.get(arg[0]))))
 									{
 										if (arg[1] != null)
 										{
 											giveItemName(arg[0], arg[1], player);
 										}
 										else
 										{
 											giveItemName(arg[0], "64", player);
 										}
 									}
 									else
 									{
 										player.sendMessage(logPrefix + " You aren't allowed to get that item!");
 										log.info(logPrefix + player.getDisplayName() + " tried to get " + arg[0].toString());
 									}
 								}
 							}
 							else
 							{
 								player.sendMessage("Correct usage is /gimme [item] {amount}");
 								return false;
 							}
 						}
 					}
 					if (gimme.Permissions.has(player, "gimme.whitelist")) 
 					{
 						if (!gimme.Permissions.has(player, "gimme.gimme"))
 						{
 							if (arg.length >= 1 && arg.length <= 2)
 							{
 								Pattern p = Pattern.compile("[-]?[0-9]+");
 								Matcher m = p.matcher(arg[0]);
 								if (m.matches())
 								{
 									if (!(itemAllow(Integer.valueOf(arg[0]))))
 									{
 										if (arg[1] != null)
 										{
 											giveItemId(arg[0], arg[1], player);
 										}
 										else
 										{
 											giveItemId(arg[0], "64", player);
 										}
 									}
 									else
 									{
 										player.sendMessage(logPrefix + " You aren't allowed to get that item!");
 										log.info(logPrefix + player.getDisplayName() + " tried to get " + arg[0].toString());
 									}
 								}
 								else
 								{
 									if (!(itemAllow(items.get(arg[0]))))
 									{
 										if (arg[1] != null)
 										{
 											giveItemName(arg[0], arg[1], player);
 										}
 										else
 										{
 											giveItemName(arg[0], "64", player);
 										}
 									}
 									else
 									{
 										player.sendMessage(logPrefix + " You aren't allowed to get that item!");
 										log.info(logPrefix + player.getDisplayName() + " tried to get " + arg[0].toString());
 									}
 								}
 							}
 							else
 							{
 								player.sendMessage("Correct usage is /gimme [item] {amount}");
 								return false;
 							}
 						}
 					}
 					return true;
 				}
 			}
 			catch(NumberFormatException e)
 			{
 				e.printStackTrace();
 			}
 			return true;
 
 		}
 		catch(CommandException e)
 		{
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	public void onEnable() 
 	{
 		setupPermissions();
 		configInit();
 		confSetup.setupConfigs();
 		populateItemMap();
 		log.info(logPrefix + " version " + this.getDescription().getVersion() + " enabled!");
 	}
 
 	public void onDisable() 
 	{
 		log.info(logPrefix + " version " + this.getDescription().getVersion() + " disabled!");
 	}
 
 	public boolean isDebugging(final Player player) 
 	{
 		if (debugees.containsKey(player)) 
 			return debugees.get(player);
 		return false;
 	}
 
 	public void setDebugging(final Player player, final boolean value) 
 	{
 		debugees.put(player, value);
 	}
 }
 
