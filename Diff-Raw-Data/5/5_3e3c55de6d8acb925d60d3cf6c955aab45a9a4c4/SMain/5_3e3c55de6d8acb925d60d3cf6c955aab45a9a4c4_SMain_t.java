 package com.ProjectTeam.SideKick;
 //------------------------------\\
 //SideKick Created By emericask8ur
 //TODO: Enchantment
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldCreator;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import org.bukkit.util.Vector;
 
 import com.ProjectTeam.API.Back;
 import com.ProjectTeam.API.Menu;
 
 import FileWriter.WriteFile;
 
 public class SMain extends JavaPlugin{
 	public FileConfiguration config;
 	public String configString;
 	public String[] rules = new String[] {"This is created by emericask8ur!", "Report any bugs and ideas to emericask8ur @bukkit.org!", "Follow @emericask8ur on Twitter!", ""};
 	private HashMap<Material, Double> itemValues = new HashMap<Material, Double>();
 	public FileConfiguration BlackList;
 	public static String plugindir = "plugins/SideKick/";
 	public static File cfg = new File(plugindir + "BlackList.yml");
 	
 	public double getItemValue(Material type) {
 		if (itemValues.containsKey(type)) { 
 			return itemValues.get(type);
 		} else {
 			return 0;
 		}
 	}
 	
 	//TODO: onDisabled
 	@Override
 	public void onDisable() {
 		saveConfig();
 	}
 	//TODO: onEnabled
 	@Override 
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new PL(), this);
 		pm.registerEvents(new BL(), this);
 		pm.registerEvents(new EL(), this);
 		loadConfig();
 		HashMapz.setItems();
 		PotionEffects.setPotions();
 		System.out.println("[SideKick] Follow @emericask8ur on Twitter for SideKick updates");
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public void loadConfig() {
 		config = this.getConfig();
 		//load rules
 		rules[0] = config.getString("Rule1", rules[0]);
 		rules[1] = config.getString("Rule2", rules[1]);
 		rules[2] = config.getString("Rule3", rules[2]);
 		rules[3] = config.getString("Rule4", rules[3]);
 		//load player money values
 		ConfigurationSection section = config.getConfigurationSection("money");
 		if (section != null) {
 			for (String key : section.getKeys(false)) {
 				double money = section.getDouble(key, section.getInt(key, 0));
 				Economy.economy.put(key, money);
 			}
 		}
 		//load item values
 		section = config.getConfigurationSection("itemvalues");
 		if (section != null) {
 			for (String key : section.getKeys(false)) {	
 				double value = section.getDouble(key, section.getInt(key, 0));
 				if (value != 0) {
 					Material type = Material.matchMaterial(key.toUpperCase());
 					if (type != null) {
 						itemValues.put(type, value);
 					} else {
 						System.out.println("[SideKick] Unrecognized item material type for prices: " + key);
 					}
 				}
 			}
 		} else {
 			for(Material mat: Material.values()){
 				itemValues.put(mat, 0.0);
 			}
 		}
 		//load worlds
 		List l = config.getList("worlds");
 		if (l != null) {
 			for (Object o : l) {
 				if (o instanceof String) {
 					String worldname = (String) o;
 					//validate if world exists
 					File worldfolder = ((CraftServer) getServer()).getWorldContainer();
 					worldfolder = new File(worldfolder, worldname);
 					if (worldfolder.exists()) {
 						System.out.println("[SideKick] Loading world: " + worldname);
 						WorldCreator creator = new WorldCreator(worldname);
 						creator.environment(getEnv(worldname));
 						World w = creator.createWorld();
 						if (w == null) {
 							System.out.println("[SideKick] World failed to load!");
 						} else {
 							System.out.println("[SideKick] World loaded!");
 						}
 					} else {
 						System.out.println("[SideKick] World " + worldname + " no longer exists and has not been loaded!");
 					}
 				}
 			}
 		}
 	}
 	public void saveConfig() {
 		config = this.getConfig();
 		config.set("Rule1", rules[0]);
 		config.set("Rule2", rules[1]);
 		config.set("Rule3", rules[2]);
 		config.set("Rule4", rules[3]);
 		for (Map.Entry<String, Double> entry : Economy.economy.entrySet()) {
 			config.set("money." + entry.getKey(), entry.getValue());
 		}
 		ArrayList<String> worldnames = new ArrayList<String>();
 		for (World w : getServer().getWorlds()) {
 			worldnames.add(w.getName());
 		}
 		config.set("worlds", worldnames);
 		for (Map.Entry<Material, Double> itemvalue : itemValues.entrySet()) {
 			config.set("itemvalues." + itemvalue.getKey().toString().toLowerCase(), itemvalue.getValue());
 		}
 		super.saveConfig();
 	}
 	public Environment getEnv(String worldname) {
 		worldname = worldname.toUpperCase();
 		for (Environment env : Environment.values()) {
 			if (worldname.endsWith("_" + env.toString())) return env;
 		}
 		return Environment.NORMAL;
 	}
 	private boolean has(Player p, String node) {
 		if (p.hasPermission("sidekick." + node)) {
 			return true;
 		} else {
 			p.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
 			return false;
 		}
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) { 
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("Sidekick only works in-game!");
 			return true;
 		}
 		//Array's
 		int A[] = new int[64];
 		A[0]=1;
 		A[1]=64;
 		A[2]=2;
 		int max = 40;
 		//ChatColors
 		ChatColor R = ChatColor.RED;
 		ChatColor B = ChatColor.BLUE;
 		ChatColor Y = ChatColor.YELLOW;
 		ChatColor G = ChatColor.GREEN;
 		ChatColor qq = ChatColor.AQUA;
 		//Player
 		Player p = (Player) sender;
 		//World V
 		World w = p.getWorld();
 		//Materials
 		Material S = Material.DIAMOND_SWORD;
 		Material Ap = Material.APPLE;
 		Material T = Material.TNT;
 		Material Bo = Material.BOW;
 		Material Ar = Material.ARROW;
 		Material P = Material.PORK;
 		//Server
 		Server server = p.getServer();
 		//Location
 		Location loc = p.getLocation();
 		//Playername
 		String playername = p.getName();
 		//Menu1
 		if(p.hasPermission("Sidekick.use")){
 			if (cmdLabel.equalsIgnoreCase("sidekick")){
 				if(args.length==0){
 				Menu.SIDEKICK_1(sender);
 				System.out.println("[SideKick] " + playername + " used SideKick Page One");
 				return true;
 				}
 				//TODO: Marker for Num.
 				else if (args.length==1){
 					if(args[0].equalsIgnoreCase("1")){
 						Menu.SIDEKICK_1(sender);
 						System.out.println("[SideKick] " + playername + " used SideKick Page One");
 						return true;
 					}
 					else if (args[0].equalsIgnoreCase("2")){
 						Menu.SIDEKICK_2(sender);
 						System.out.println("[SideKick] " + playername + " used SideKick Page Two");
 						return true;
 					}
 					else if (args[0].equalsIgnoreCase("3")){
 						Menu.SIDEKICK_3(sender);
 						System.out.println("[SideKick] " + playername + " used SideKick Page Three");
 						return true;
 					}
 					else if (args[0].equalsIgnoreCase("4")){
 						Menu.SIDEKICK_4(sender);
 						System.out.println("[SideKick] " + playername + " used SideKick Page Four");
 						return true;
 					}
 					else if (args[0].equalsIgnoreCase("5")){
 						Menu.SIDEKICK_5(sender);
 						System.out.println("[SideKick] " + playername + " used SideKick Page Five");
 						return true;  
 					}
 					else if (args[0].equalsIgnoreCase("6")){
 						Menu.SIDEKICK_6(sender);
 						System.out.println("[SideKick] " + playername + " used SideKick Page Six");
 						return true;
 					}
 					else if (args[0].equalsIgnoreCase("7")){
 						Menu.SIDEKICK_7(sender);
 						System.out.println("[SideKick] " + playername + " used SideKick Page Seven");
 						return true;
 					}
 					else if (args[0].equalsIgnoreCase("8")){
 						Menu.SIDEKICK_8(sender);
 						System.out.println("[SideKick] " + playername + " used SideKick Page Eight");
 						return true;
 					} else {
 						sender.sendMessage(R + "Please enter a valid page number!");
 					}
 				} 
 				return true;
 			}
 			//TODO: Marker for End Num.
 			//Menu 2 TWO MORE UNTIL OFF CLEAR
 			else if (cmdLabel.equalsIgnoreCase("sidekick2")){
 				Menu.SIDEKICK_2(sender);
 				System.out.println("[SideKick] " + playername + " used SideKick Page Two");
 				return true;
 			}
 			//Menu 3
 			else if (cmdLabel.equalsIgnoreCase("sidekick3")){
 				Menu.SIDEKICK_3(sender);
 				System.out.println("[SideKick] " + playername + " used SideKick Page Three");
 				return true;
 			}//Menu 4 
 			else if (cmdLabel.equalsIgnoreCase("sidekick4")){
 				Menu.SIDEKICK_4(sender);
 				System.out.println("[SideKick] " + playername + " used SideKick Page Four");
 				return true;
 			}
 			//Menu 5
 			else if (cmdLabel.equalsIgnoreCase("sidekick5")){
 				Menu.SIDEKICK_5(sender);
 				System.out.println("[SideKick] " + playername + " used SideKick Page Five");
 				return true;  
 			}
 			//Menu 6
 			else if (cmdLabel.equalsIgnoreCase("sidekick6")){
 				Menu.SIDEKICK_6(sender);
 				System.out.println("[SideKick] " + playername + " used SideKick Page Six");
 				return true;
 			}
 			//Menu 7
 			else if (cmdLabel.equalsIgnoreCase("sidekick7")){
 				Menu.SIDEKICK_7(sender);
 				System.out.println("[SideKick] " + playername + " used SideKick Page Seven");
 				return true;
 			}
 			//Menu 8
 			else if (cmdLabel.equalsIgnoreCase("sidekick8")){
 				Menu.SIDEKICK_8(sender);
 				System.out.println("[SideKick] " + playername + " used SideKick Page Eight");
 				return true;
 				}
 		}
 		//Anti?
 		if (cmdLabel.equalsIgnoreCase("anti?") && has(p, "anti")   ){
 			sender.sendMessage("AntiBuild is a Mode that disables building for non ops! Repeat Command to turn mode off");
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("c")){
 			if(!p.hasPermission("Sidekick.calculator")){
 				sender.sendMessage(R + "You do not have permission to use this!");
 			}
 			try{
 			if(args.length==3){
 				double x = Double.parseDouble(args[0]);
 				double y = Double.parseDouble(args[2]);
 				if(args[1].equalsIgnoreCase("+")){
 					double z = x + y;
 					sender.sendMessage(args[0] + " + " + args[2] + " = "  + z);
 					return true;
 				}
 				else if (args[1].equalsIgnoreCase("-")){
 					double z = x - y;
 					sender.sendMessage(args[0] + " - " + args[2] + " = "  + z);
 					return true;
 				}
 				else if (args[1].equalsIgnoreCase("/")){
 					double z = x / y;
 					sender.sendMessage(args[0] + " / " + args[2] + " = "  + z);
 					return true;
 				}
 				else if (args[1].equalsIgnoreCase("*") || args[1].equalsIgnoreCase("x")){
 					double z = x * y;
 					sender.sendMessage(args[0] + " x " + args[2] + " = "  + z);
 					return true;
 				} else {
 					sender.sendMessage(R + "You can only use these: (x,/,+,-,*)");
 				}
 			} else {
 				sender.sendMessage(R + "Did you mean /C <Number> <+,-,/,*> <Number>");
 			}
 			}catch (Exception i){
 				sender.sendMessage(R + "Cannot Calculate these numbers!");
 			}
 			return true;
 		}
 		//Back
 		else if (cmdLabel.equalsIgnoreCase("back")){
 			if(!(p.hasPermission("Sidekick.back"))){
 				sender.sendMessage(R + "You do not have permission!");
 			}
 			if(!(args.length==0)){
 				sender.sendMessage(R + "Did you mean /Back");
 			}
 			if(Back.back.containsKey(playername)){
 				p.teleport(Back.getBackLocation(playername));
 				sender.sendMessage(G + "Back to location");
 			} else {
 				sender.sendMessage(R + "No where to go back!");
 			}
 			return true;
 		}
 		//PotionEffect
 		else if (cmdLabel.equalsIgnoreCase("potioneffect")){
 			ChatColor q = ChatColor.AQUA;
 			if(!sender.hasPermission("Sidekick.potion")){
 				sender.sendMessage(R + "You do not have permission for this command!");
 				} else {
 				if(args.length==0){
 					sender.sendMessage(R + "Did you mean " + G + "/PotionEffect ? or /PotionEffect <EffectType>");
 				}//end Args 0
 				else if (args.length==1){
 					if(args[0].equalsIgnoreCase("?")){
 						sender.sendMessage(Y + "<><>PotionEffects<><>");
 						sender.sendMessage(q + "Blind");
 						sender.sendMessage(q + "Confusion");
 						sender.sendMessage(q + "Digging");
 						sender.sendMessage(q + "Damage");
 						sender.sendMessage(q + "Fire");
 						sender.sendMessage(q + "Harm");
 						sender.sendMessage(q + "Heal");
 						sender.sendMessage(q + "Hunger");
 						sender.sendMessage(q + "Increase");
 						sender.sendMessage(q + "Jump");
 						sender.sendMessage(q + "Poison");
 						sender.sendMessage(q + "Regeneration");
 						sender.sendMessage(q + "Slow");
 						sender.sendMessage(q + "Speed");
 						sender.sendMessage(q + "Water");
 						sender.sendMessage(q + "Weakness");
 						return true;
 					}//end ?
 					PotionEffectType potion = PotionEffects.potion.get(args[0]);
 					if(potion !=null && !args[0].equalsIgnoreCase("?")){
 						p.addPotionEffect(new PotionEffect(potion,300,20));
 						sender.sendMessage(q + "You have set your potion effect to " + args[0]);
 						return true;
 					}else {
 						sender.sendMessage(R + "Invalid Potion Type"); // null check
 					}
 					return true;
 				}
 				else if (args.length==2){
 					Player player = server.getPlayer(args[0]);
 					PotionEffectType potion = PotionEffects.potion.get(args[1]);
 					if(player !=null && potion !=null){
 						player.addPotionEffect(new PotionEffect(potion, 300,20));
 						sender.sendMessage(G + "You have set " + args[0] + " PotionEffect to " + args[1]);
 					} else {
 						sender.sendMessage(R + "Player or Potion Not found!");
 					}
 					return true;
 				} else {
 					sender.sendMessage(R + "Did you mean " + G + "/PotionEffect ? or /PotionEffect <EffectType>");
 				}
 				return true;
 			}
 			return true;
 		}
 		//Players
 		else if (cmdLabel.equalsIgnoreCase("players")){
 			List<Player> pla = p.getWorld().getPlayers();
 			String aa = pla.toString();
 			String ii = aa.replace("[", "");
 			String i = ii.replace("CraftPlayer", "");
 			String iii = i.replace("=", "");
 			String iiii = iii.replace("{", "");
 			String a = iiii.replace("}", "");
 			String aaa = a.replace("]", ",");
 			String x = aaa.replace("name", "");
 			sender.sendMessage(ChatColor.GREEN + "Players online now: " + ChatColor.WHITE +  x);
 			return true;
 		}
 		//Wear
 		else if (cmdLabel.equalsIgnoreCase("wear") && has(p, "wear")){
 			if(args.length==0){
 			try{
 			p.getInventory().setHelmet(p.getItemInHand());
 			sender.sendMessage(G + "Hat Set!");
 			return true;
 			}catch (Exception i ){}
 			} 
 			else if (args.length==1){
 				if(args[0].equalsIgnoreCase("remove")){
 					p.getInventory().setHelmet(new ItemStack(Material.AIR,1));
 					sender.sendMessage(G + "You have removed your hat!");
 					return true;
 				}
 				else if (!args[0].equalsIgnoreCase("remove")){
 					Material mat = HashMapz.getMaterial(args[0]);
 					ItemStack i = new ItemStack(mat, 1);
 					if(mat !=null){
 						p.getInventory().setHelmet(i);
 						sender.sendMessage(G + "Hat set to " + args[0]);
 						return true;
 					} else {
 						sender.sendMessage(R + "Cannot find item: " + args[0]);
 					}
 					return true;
 				}
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("worlds")){
 			if(!p.hasPermission("Sidekick.listworlds")){
 				sender.sendMessage(R + " You do not have permission to use this!");
 			}
 			if(args.length==0){
 				List<World> list = server.getWorlds();
 				String i = list.toString();
 				String ii = i.replace("{", "");
 				String iii = ii.replace("CraftWorld", "");
 				String x = iii.replace("name", "");
 				String xx = x.replace("=", "");
 				String xxx = xx.replace("}", "");
 				String z = xxx.replace("[", "");
 				String zz = z.replace("]", " , ");
 				sender.sendMessage("Worlds are: " + zz);
 				return true;
 			} else {
 				sender.sendMessage(R + "Did you mean /Worlds");
 			}
 			return true;
 		}
 		//Eco
 		else if (cmdLabel.equalsIgnoreCase("sell") && has(p, "sell")   ) {
 			Material type = p.getItemInHand().getType();
 			int amount = p.getItemInHand().getAmount();
 			double value = getItemValue(type) * amount;
 			if (value <= 0) {
 				p.sendMessage(ChatColor.RED + "This item is worth nothing!");
 				return true;
 			}
 			sender.sendMessage("You have sold " + amount + " " + type.toString().toLowerCase().replace('_', ' ') + " for " + value + " $");
 			p.setItemInHand(null);
 			if (Economy.economy.containsKey(p.getName())) {
 				value += Economy.economy.get(p.getName());
 			}
 			Economy.economy.put(p.getName(), value);
 			return true;
 		}
 		//Setspawn
 		//// Add command
 		else if (cmdLabel.equalsIgnoreCase("add") && args.length > 2 && has(p, "blockadd")  ) {
 			String edittype = args[0];
 			int dataindex = args[1].lastIndexOf(':');
 			int data = -1;
 			if (dataindex != -1) {
 			   try {
 				   data = Byte.parseByte(args[1].substring(dataindex + 1));
 				   args[1] = args[1].substring(0, dataindex);
 			   } catch (Exception ex) {}
 			}
 			Material repl = HashMapz.getMaterial(args[1]); 
 			if (repl == null || !repl.isBlock()) {
 				sender.sendMessage("Unknown block type: " + args[1]);
 				return true;
 			}
 
 			int startx = p.getLocation().getBlockX();
 			int starty = p.getLocation().getBlockY() - 1;
 			int startz = p.getLocation().getBlockZ();
 			if (edittype.equalsIgnoreCase("under")) {
 				if (starty < 0) {
 					p.sendMessage("You are too low to change blocks underneath you!");
 					return true;
 				}
 				int x = 1;
 				int y = 1;
 				int z = 1;
 				try {
 					if (args.length == 3) {
 						y = Integer.parseInt(args[2]);
 					} else if (args.length == 4) {
 						x = Integer.parseInt(args[2]);
 						z = Integer.parseInt(args[3]);
 					} else if (args.length == 5) {
 						x = Integer.parseInt(args[2]);
 						y = Integer.parseInt(args[3]);
 						z = Integer.parseInt(args[4]);
 					}
 				} catch (Exception ex) {}
 				//actual block changing here
 				startx -= Math.floor(x / 2);
 				starty -= Math.floor(y / 2);
 				startz -= Math.floor(z / 2);
 				for (int a = 0; a < x; a++) {
 					for (int b = 0; b < y; b++) {
 						for (int c = 0; c < z; c++) {
 							Block block = p.getWorld().getBlockAt(startx + a, starty - b, startz + c);
 							if (block != null) {
 								if (data == -1) {
 									block.setType(repl);
 								} else {
 									block.setTypeIdAndData(repl.getId(), (byte) data, true);
 								}
 								block.setType(repl);
 							}
 						}
 					}
 				}
 				p.sendMessage("You have set the blocks underneath you!");
 			}
 		}
 		//Spawn
 		else if (cmdLabel.equalsIgnoreCase("spawn") && has(p, "spawn")  ){
 			p.teleport(w.getSpawnLocation());
 			sender.sendMessage(G + "You are now at Spawn!");
 			return true;
 		}
 		//Crete Name Output
 		else if (cmdLabel.equalsIgnoreCase("create")  && args.length > 0 && has(p, "file")){
 			String out = "";
 			for (String part : args) {
 				if (out != "") out += " ";
 				out += part;
 			}
 			File file = new File(this.getDataFolder(), args[0]);
 			if (file.exists()) {
 				System.out.println("It already Exists!");
 			} else {
 				try {
 					WriteFile.Write(file, out);
 				} catch (IOException e) {
 					System.out.println("/Create <FileName> <Output> Failed to Create the File!");
 				}
 			}
 			return true;
 
 		}
 		else if (cmdLabel.equalsIgnoreCase("addunder") && has(p, "edit")  ){
 			if(!(args.length==1)){sender.sendMessage(R + "Check your arguments!");}
 			Material mat = HashMapz.getMaterial(args[0]);
 			if(mat !=null){
 				Location l = p.getLocation();
 				l.setX(l.getX());
 				l.setZ(l.getX());
 				l.setZ(l.getZ());
 				l.getBlock().getRelative(BlockFace.DOWN).setType(HashMapz.getMaterial(args[0]));
 			}else {
 				sender.sendMessage(R + "Invalid Block Type!");
 			}
 		}
 		//List Worlds
 		else if (cmdLabel.equalsIgnoreCase("list") && has(p, "listworlds")  ){
 			if(!(args.length==1)){sender.sendMessage(R + "Did you mean /List Worlds");}
 			List<World> list = server.getWorlds();
 			String i = list.toString();
 			String ii = i.replace("{", "");
 			String iii = ii.replace("CraftWorld", "");
 			String x = iii.replace("name", "");
 			String xx = x.replace("=", "");
 			String xxx = xx.replace("}", "");
 			String z = xxx.replace("[", "");
 			String zz = z.replace("]", " , ");
 			if(args[0].equalsIgnoreCase("worlds")){
 				sender.sendMessage(ChatColor.AQUA + " Worlds are: " + zz);
 				return true;
 			}
 			return true;
 		}	
 		//invClear
 		else if (cmdLabel.equalsIgnoreCase("invclear")  && (has(p, "clearinventory")  )){
 			try{
 			if(!(args.length==1)){sender.sendMessage(R + "Did you mean /InvClear <Player>");}
 			Player play = server.getPlayer(args[0]);
 			if(play !=null){
 				play.getInventory().clear();
 				sender.sendMessage(G + "Clearing of " + args[0] + " Inventory Complete!");
 				return true;
 			} else {
 				sender.sendMessage(R + "Player Not Found!");
 			}
 			return true;
 			}catch (Exception i){sender.sendMessage(R + "Did you mean /Give [Player] [Item] <Amount> or /Give [Item]");}
 		}
 		//Read 
 		else if (cmdLabel.equalsIgnoreCase("read") && args.length==3 && has(p, "read")  ){
 			Player pla = server.getPlayer(args[2]);
 			if(pla !=null){
 				pla.sendMessage(rules[0]);
 				pla.sendMessage(rules[1]);
 				pla.sendMessage(rules[2]);
 				pla.sendMessage(rules[3]);
 				return true;
 			} else {
 				sender.sendMessage(R + "Player not found!");
 			}
 			return true;
 		}
 		//Log
 		else if (cmdLabel.equalsIgnoreCase("log") && args.length==1 && has(p, "log")  ){
 			if(args[0].equalsIgnoreCase("breaks")){
 				BL.log =!BL.log;
 				if(BL.log){
 					sender.sendMessage(G + "[SideKick] Will now log each Block Broken and Record players actions in the Server.txt File!");
 				} else {
 					sender.sendMessage(R + "[SideKick] Will stop logging each break in the File!");
 				}
 			}
 			return true;
 		}
 		//Warn <Playername>
 		else if (cmdLabel.equalsIgnoreCase("warn") && has(p, "warn")  ){
 			Player found = server.getPlayer(args[0]);
 			if (found != null) {
 				String message = "";
 				for (String part : args) {
 					if (message != "") message += " ";
 					message += part;
 				}
 				try{
 				String last = message.replace(sender.getName(), "");
 				found.sendMessage(Y + "----------" + R + "You have a Warning!" + Y + "----------");
 				found.sendMessage(last);
 				found.sendMessage(Y + "---------End of Message---------");
 				sender.sendMessage("Message Complete!");
 				return true;
 				}catch (Exception i){sender.sendMessage(R + "Warning could not be sent!");}
 			} else {
 				sender.sendMessage(G + "Player Not found!");
 			}
 			return true;
 		}
 		//RealMode Help Guide
 		else if (cmdLabel.equalsIgnoreCase("realmode?") && has(p, "realmode?")  ){
 			sender.sendMessage(Y + "Real mode allows you to drop meat on a players Death, For the best Survival Like Features!");
 			return true;
 		}
 		//Prefix-------------------------------
 		else if (cmdLabel.equalsIgnoreCase("prefix") && args.length==1 && has(p, "prefix")  ){
 			p.setDisplayName("[" + args[0] + "]" + p.getDisplayName());
 			sender.sendMessage(G + "You have changed your prefix!");
 			return true;
 		}
 		//Suffix--------------------------------
 		else if (cmdLabel.equalsIgnoreCase("suffix") && args.length==1 && has(p, "suffix")  ){
 			p.setDisplayName(p.getDisplayName() + "[" + args[0] + "]");
 			sender.sendMessage(G + "You have changed your suffix!");
 			return true;
 		}
 		//Chat Clear
 		else if (cmdLabel.equalsIgnoreCase("chat") && args.length==1 && has(p, "chat")  ){
 			if(args[0].equalsIgnoreCase("clear")){
 				p.setDisplayName(playername);
 				sender.sendMessage(G + "You have cleared your Prefix/Suffix!");
 				return true;
 			}
 		}
 		//Creative------------------
 		else if (cmdLabel.equalsIgnoreCase("creative")){
 			if(args.length==0){
 				if(p.hasPermission("Sidekick.creative")){
 			p.setGameMode(GameMode.CREATIVE);
 			sender.sendMessage(G + "Your Game mode is Creative!");
 			return true;
 				} else {
 					sender.sendMessage(R + "You do not have permission!");
 				}
 			}
 			else if (args.length==1){
 				Player play = server.getPlayer(args[0]);
 				if(p.hasPermission("Sidekick.creative.other")){
 				if(play !=null){
 					play.setGameMode(GameMode.CREATIVE);
 					sender.sendMessage(G + "You have set " + args[0] + " to creative!");
 					play.sendMessage(G + "Your Game mode is survival!");
 					return true;
 				} else {
 					sender.sendMessage(R + "Player " + args[0] + " not found!");
 				}
 				} else {
 					sender.sendMessage(R + "You do not have permission!");
 				}
 			} else {
 				sender.sendMessage(R + "Did you mean /Creative or /Creative [Player]");
 			}
 			return true;
 		}
 		//Survival-------------------
 		else if (cmdLabel.equalsIgnoreCase("survival")){
 			if(args.length==0){
 				if(p.hasPermission("Sidekick.survival")){
 			p.setGameMode(GameMode.SURVIVAL);
 			sender.sendMessage(G + "Your Game mode is Survival!");
 			return true;
 				} else {
 					sender.sendMessage(R + "You do not have permission!");
 				}
 			} 
 			else if (args.length==1){
 				Player play = server.getPlayer(args[0]);
 				if(p.hasPermission("Sidekick.survival.other")){
 				if(play !=null){
 				play.setGameMode(GameMode.SURVIVAL);
 				sender.sendMessage(G + "You have set " + args[0] + " to survival!");
 				play.sendMessage(G + "Your Game mode is survival!");
 				return true;
 			} else {
 				sender.sendMessage(R + "Player " + args[0] + " not found!");
 				} 
 				} else {
 					sender.sendMessage(R + "You do not have permission!");
 				}
 			} else {
 				sender.sendMessage(R + "Did you mean /Creative or /Creative [Player]");
 			}
 			return true;
 		}
 		//TP TP--------------------------
 		else if (cmdLabel.equalsIgnoreCase("tome") && has(p, "tome")  ){
 			if(args.length==0){
 				sender.sendMessage(R+ "Did you mean /ToMe <Username> or /TP <Username> ?");
 				return true;
 			}
 			else if (args.length==1){
 			Player to = getServer().getPlayer(args[0]);
 			if(to != null){
 				to.teleport(p);
 				sender.sendMessage("You have teleported!");
 				return true;
 			} else {
 				sender.sendMessage(R + "Player not found!");
 				}
 			}
 			return true;
 		}
 		//Reload------------
 		else if (cmdLabel.equalsIgnoreCase("restart") && has(p, "restart")  ){
 			server.reload();
 			sender.sendMessage(G + "Restart Complete");
 			return true;
 		}
 		//SetSpawn------------
 		else if (cmdLabel.equalsIgnoreCase("setspawn") && has(p, "setspawn")  ){
 			int x = p.getLocation().getBlockX();
 			int y = p.getLocation().getBlockY();
 			int z = p.getLocation().getBlockZ();
 			if(args.length==0){
 				w.setSpawnLocation(x, y, z);
 				sender.sendMessage(G + "Spawn set at your location!");
 			} else {
 				w.setSpawnLocation(x, y, z);
 				sender.sendMessage(G + "Spawn set at your location!");
 			}
 			return true;
 		}
 		//Kill----------
 		else if (cmdLabel.equalsIgnoreCase("kill") && args.length==1 && has(p, "kill")  ){
 			Player play = server.getPlayer(args[0]);
 			if(play !=null){
 				play.setHealth(0);
 				sender.sendMessage(G + args[0] + " has Died! :D");
 				return true;
 			} else {
 				sender.sendMessage(R + "Player not found!");
 			}
 			return true;
 		}
 		//Eat Command
 		else if (cmdLabel.equalsIgnoreCase("eat") && has(p, "eat")  ) {	
 			p.setFoodLevel(max);
 			sender.sendMessage(G + "Food Level Set Maxed!");
 			return true;
 		}
 		//Edit
 		else if (cmdLabel.equalsIgnoreCase("edit") && args.length==1 && has(p, "edit")  ){
 			Block target = p.getTargetBlock(null, 200);
 			if (target != null) {
 				int data = -1;
 			    int index = args[0].lastIndexOf(':');
 			    if (index != -1) {
 			        try {
 			        	data = Byte.parseByte(args[0].substring(index + 1));
 			        	args[0] = args[0].substring(0, index);
 			        } catch (Exception ex) {}
 			    }
 				Material type = HashMapz.getMaterial(args[0]);
 				if (type != null) {
 					if (data == -1) {
 						target.setType(type);
 					} else {
 						target.setTypeIdAndData(type.getId(), (byte) data, true);
 					}
 				} else {
 					p.sendMessage("Invalid block type!");
 				}
 				return true;		
 			}
 			if(args[0].equalsIgnoreCase("redstone")){
 				sender.sendMessage(ChatColor.RED + "Cannot Edit Redstone!");
 				return  true;
 			}
 			else if (args[0].equalsIgnoreCase("torch")){
 				sender.sendMessage(R + "Cannot Edit Torch!");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("redstonetorch")){
 				sender.sendMessage(R + "Cannot Edit RedStone Torch!");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("lever")){
 				sender.sendMessage(R + "Cannot Edit Lever!");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("button")){
 				sender.sendMessage(R + "Cannot Edit Button!");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("buttons")){
 				sender.sendMessage(R + "Cannot Edit Buttons!");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("yellowflower")){
 				sender.sendMessage(R + "Cannot Edit ANY flowers!");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("redflower")){
 				sender.sendMessage(R + "Cannot Edit ANY flowers!");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("seeds")){
 				sender.sendMessage(R + "Cannot Edit Seeds!");
 				return true;
 			}
 		}
 		//Edit
 		else if (cmdLabel.equalsIgnoreCase("edit?") && has(p, "edit?")  ){
 			sender.sendMessage(B + "/Edit <Block> - Is a command for block changes, lets say you want to change a block , just type /edit <block> where is says Block Type any block thats Solid Type /edit <remove/none/Air> to remove the block");
 			return true;
 		}
 		//Disable
 		else if (cmdLabel.equalsIgnoreCase("disable") && args.length==1 && has(p, "world")  ){
 			if(args[0].equalsIgnoreCase("creeper")) {
 				EL.creeper = !EL.creeper;
 				if (EL.creeper) {
 					sender.sendMessage(R + "Exlosion will happen!");
 				} else {
 					sender.sendMessage(B + "Creepers will not explode!");
 				}
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("goto") && args.length==1 && has(p, "world")  ){
 			World world = server.getWorld(args[0]);
 			if (world != null) {
 				p.teleport(world.getSpawnLocation());
 			} else {
 				sender.sendMessage("Invalid World! /List Worlds - For a Full List on all the Worlds!");
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("broadcast") && has(p, "broadcast")  ){
 			String message = "";
 			for (String part : args) {
 				if (message != "") message += " ";
 				message += part;
 			}
 			Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "[Broadcast]" + ChatColor.DARK_AQUA + message);
 
 		}
 		//both
 		else if (cmdLabel.equalsIgnoreCase("both") && args.length==2 && has(p, "both")  ){
 			p.setDisplayName("[" + args[0] + "]" + p.getDisplayName() + "[" + args[1] + "]");
 			sender.sendMessage("You are now using both a Prefix and a Suffix!");
 			return true;
 		}
 		//Spawner
 		else if (cmdLabel.equalsIgnoreCase("spawner") && args.length==1 && has(p,"spawner")  ){
 			Block t = p.getTargetBlock(null, 200);
			Location tll = t.getLocation();
			Block block = tll.getBlock();
			Block x = block.getRelative(BlockFace.UP);
			Location tl = x.getLocation();
 			if (args[0].equalsIgnoreCase("chicken")){
 				t.getWorld().spawnCreature(tl, EntityType.CHICKEN);
 				sender.sendMessage(Y + "You have spawned a chicken");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("cow")){
 				t.getWorld().spawnCreature(tl, EntityType.COW);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("creeper")){
 				t.getWorld().spawnCreature(tl, EntityType.CREEPER);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("enderman")){
 				t.getWorld().spawnCreature(tl, EntityType.ENDERMAN);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("ghast")){
 				t.getWorld().spawnCreature(tl, EntityType.GHAST);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("giant")){
 				t.getWorld().spawnCreature(tl, EntityType.GIANT);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("pig")){
 				t.getWorld().spawnCreature(tl, EntityType.PIG);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("sheep")){
 				t.getWorld().spawnCreature(tl, EntityType.SHEEP);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("skeleton")){
 				t.getWorld().spawnCreature(tl, EntityType.SKELETON);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("slime")){
 				t.getWorld().spawnCreature(tl, EntityType.SLIME);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("spider")){
 				t.getWorld().spawnCreature(tl, EntityType.SPIDER);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("wolf")){
 				t.getWorld().spawnCreature(tl, EntityType.WOLF);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("zombie")){
 				t.getWorld().spawnCreature(tl, EntityType.ZOMBIE);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("cavespider")){
 				t.getWorld().spawnCreature(tl, EntityType.CAVE_SPIDER); 
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("blaze")){
 				t.getWorld().spawnCreature(tl, EntityType.BLAZE);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("dragon")){
 				t.getWorld().spawnCreature(tl, EntityType.ENDER_DRAGON);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("enderdragon")){
 				t.getWorld().spawnCreature(tl, EntityType.ENDER_DRAGON);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("mustroomcow")){
 				t.getWorld().spawnCreature(tl, EntityType.MUSHROOM_COW);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("silverfish")){
 				t.getWorld().spawnCreature(tl, EntityType.SILVERFISH);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("villager")){
 				t.getWorld().spawnCreature(tl, EntityType.VILLAGER);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("npc")){
 				t.getWorld().spawnCreature(tl, EntityType.VILLAGER);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("fireball")){
 				t.getWorld().spawnCreature(tl, EntityType.FIREBALL);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("unknown")){
 				t.getWorld().spawnCreature(tl, EntityType.UNKNOWN);
 				sender.sendMessage(Y + "You have spawned a " + args[0]);
 				return true;
 			}
 			//1.2.3 Mobs
 			else if (args[0].equalsIgnoreCase("irongolem")){
 				t.getWorld().spawnCreature(tl, EntityType.IRON_GOLEM);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("iron_golem")){
 				t.getWorld().spawnCreature(tl, EntityType.IRON_GOLEM);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("ocelot")){
 				t.getWorld().spawnCreature(tl, EntityType.OCELOT);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("cat")){
 				t.getWorld().spawnCreature(tl, EntityType.OCELOT);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("ocelot")){
 				t.getWorld().spawnCreature(tl, EntityType.OCELOT);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("player")){
 				t.getWorld().spawnCreature(tl, EntityType.PLAYER);
 				return true;
 			}
 			return true;
 		}
 		//Shutdown
 		else if (cmdLabel.equalsIgnoreCase("//") && has(p, "shutdown")  ){
 			sender.sendMessage(G + "Server Shutting Down! [SideKick]");
 			server.shutdown();
 			return true;
 		}
 		//Ip
 		else if (cmdLabel.equalsIgnoreCase("ip") && args.length==1 && has(p, "ip")  ){
 			Player found = server.getPlayer(args[0]);
 			if(found !=null){
 				sender.sendMessage(args[0] + "'s Ip is " + found.getAddress());
 			} else {
 				sender.sendMessage(G + "Player not found!");
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("show") && args.length==2 && has(p, "show")  ){
 			Player online = server.getPlayer(args[1]);
 			if(args[0].equalsIgnoreCase("xp")){
 				if(online != null){
 					sender.sendMessage(args[1] + " XP is " + online.getExp());
 				} else {
 					sender.sendMessage(G + "Player Not Found!");
 				}
 				return true;
 			}
 		}
 		//G Command
 		else if (cmdLabel.equalsIgnoreCase("g") && has(p, "g")  ){
 			if(!(args.length==2)){sender.sendMessage(R + "Did you mean /G <Player> <Item:ID>");}
 			Player rec = server.getPlayer(args[0]);
 			int data = -1;
 		    int index = args[0].lastIndexOf(':');
 		    if (index != -1) {
 		        try {
 		        	data = Byte.parseByte(args[0].substring(index + 1));
 		        	args[0] = args[0].substring(0, index);
 		        } catch (Exception ex) {}
 		    }
 			Material type = HashMapz.getMaterial(args[1]);
 			if(rec !=null && type !=null){
 				ItemStack item = new ItemStack(type, type.getMaxStackSize());
 				if (data != -1) item.setDurability((short) data);
 				rec.getInventory().addItem(item);
 				sender.sendMessage(G + "You have gave " + args[0] + " " + args[1]);
 				rec.sendMessage(G + "[SideKick] You have received " + args[1]);
 				return true;
 			} else {
 				sender.sendMessage(R + "Check your Arguments!");
 			}
 			return true;
 		}
 		//Strike
 		else if (cmdLabel.equalsIgnoreCase("strike") && args.length==1 && has(p, "strike")  ){
 			Player sti = server.getPlayer(args[0]);
 			if(sti !=null){
 				w.strikeLightning(sti.getLocation());
 				sender.sendMessage(G + "You have Struck " + args[0]);
 				return true;
 			} else {
 				sender.sendMessage(R + "Check your arguments!");
 			}
 			return true;
 		}
 		//Message
 		else if (cmdLabel.equalsIgnoreCase("message") && has(p, "message")  ){
 			Player found = server.getPlayer(args[0]);
 			String playa = p.getName();
 			if (found != null) {
 				String message = "";
 				for (String part : args) {
 					if (message != "") message += " ";
 					message += part;
 				}
 				try{
 				String result = message.replace(sender.getName(), "");
 				found.sendMessage(Y + "----Message From " + ChatColor.AQUA + playa + Y + "----");
 				found.sendMessage(result);
 				found.sendMessage(Y + "---------End of Message---------");
 				sender.sendMessage("Message Complete!");
 				return true;
 				}catch (Exception i){sender.sendMessage(R + "Message could not be sent!");}
 			} else {
 				sender.sendMessage(G + "Player Not found!");
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("set") && args.length==2 && has(p, "set")  ){
 			Player playa = server.getPlayer(args[0]);
 			if(playa !=null){
 				playa.setDisplayName(args[1]);
 				sender.sendMessage(A + args[0] + " Name was changed to " + args[1]);
 			} else {
 				sender.sendMessage(R + "Player not found!");
 			}
 			return true;
 		}
 		//AdvertiseMent
 		else if (cmdLabel.equalsIgnoreCase("advertise") && has(p, "advertise")  ){
 			String message = "";
 			for (String part : args) {
 				if (message != "") message += " ";
 				message += part;
 			}
 			Bukkit.getServer().broadcastMessage(ChatColor.RED + "[Advertisement] " + ChatColor.AQUA + message);
 		}
 		//All Command
 		else if (cmdLabel.equalsIgnoreCase("all") && args.length==4 && has(p, "all")  ){
 			Player pl = server.getPlayer(args[0]);
 			Player pla = server.getPlayer(args[1]);
 			Player play = server.getPlayer(args[2]);
 			Material mat = HashMapz.getMaterial(args[3]);
 			if(pl !=null && pla !=null && play !=null && mat !=null){
 				ItemStack item = new ItemStack(mat, mat.getMaxStackSize());
 				pl.getInventory().addItem(item);
 				pla.getInventory().addItem(item);
 				play.getInventory().addItem(item);
 				sender.sendMessage(G + args[3] + " gave to " + args[0] + " " + args[1] + " " + args[2]);
 				return true;
 			} else {
 				sender.sendMessage(R + "Player(s) Not Found!");
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("player") && args.length==2 && has(p, "player")  ){
 			Player pp = server.getPlayer(args[1]);
 			if(pp !=null){
 				if(args[0].equalsIgnoreCase("spawn")){
 					pp.teleport(w.getSpawnLocation());
 					sender.sendMessage(G + "You have put " + args[1] + " to spawn!");
 					return true;
 				}
 			} else {
 				sender.sendMessage(R + " Player not found!");
 			}
 			return true;
 		}
 		//Change Name
 		else if (cmdLabel.equalsIgnoreCase("changename")) {
 			if (args.length == 1) {
 				if (p.hasPermission("Sidekick.chagename")) {
 					p.setDisplayName(args[0]);
 					p.sendMessage(R + "You have changed your name!");
 					return true;
 				} else {
 					sender.sendMessage(R + "You do not have permission!");
 				}
 			}
 			else if (args.length == 2){
 				if(p.hasPermission("Sidekick.changename.other")){
 					Player play = server.getPlayer(args[0]);
 					if(play == null){
 						sender.sendMessage(R + "Player not found!");
 					} else {
 						play.setDisplayName(args[1]);
 						sender.sendMessage(ChatColor.YELLOW + "You changed player: " + qq + play.getName() + ChatColor.YELLOW  + " to: " + args[1]);
 						return true;
 					}
 				}else {
 					sender.sendMessage(R + "You do not have permission!");//end perm
 				}
 			} else {
 				sender.sendMessage(R + "Did you mean /Changename [Name] or /Changename [Player] [Name]");
 			}
 			return true;
 		}
 		//Send Command
 		else if (cmdLabel.equalsIgnoreCase("send") && args.length==3 && has(p, "send")  ){
 			Player pl = server.getPlayer(args[1]);
 			Material mat1 = HashMapz.getMaterial(args[2]);
 			if(pl !=null && mat1 !=null && p.getInventory().contains(mat1)){
 				if(args[0].equalsIgnoreCase("to")){
 					ItemStack item = new ItemStack(mat1.getMaxStackSize());
 					sender.sendMessage(ChatColor.WHITE + "Sending.......");
 					p.getInventory().removeItem(item);
 					pl.getInventory().addItem(item);
 					sender.sendMessage(ChatColor.GREEN + "Item Sent!");
 					pl.sendMessage(ChatColor.AQUA + p.getDisplayName() + ChatColor.YELLOW + " Has sent you " + Y +  args[2]);
 					return true;
 				}
 			} else {
 				sender.sendMessage(R + "Player or item not found!");
 			}
 			return true;
 		}
 		//Move
 		else if (cmdLabel.equalsIgnoreCase("move") && args.length==2 && has(p, "move")  ){
 			World wa = server.getWorld(args[1]);
 			if(args[0].equalsIgnoreCase("to")){
 				if(wa !=null){
 					//parse mode
 					for (Player pl : wa.getPlayers()) {
 						pl.teleport(p.getLocation());
 						sender.sendMessage(G + "Everyone Teleported to you!");
 					} 
 				} else {
 					sender.sendMessage(ChatColor.RED + "World not found!");
 				}
 				return true;
 			}
 			return true;
 		}
 		//Weapon 1
 		else if (cmdLabel.equalsIgnoreCase("weapon") && args.length==1 && has(p, "weapon")  ){
 			if(args[0].equalsIgnoreCase("1")){
 				ItemStack I = new ItemStack(S, A[0]);
 				ItemStack I2 = new ItemStack(Ap, A[2]);
 				ItemStack I3 = new ItemStack(T, A[0]);
 				p.getInventory().addItem(I);
 				p.getInventory().addItem(I2);
 				p.getInventory().addItem(I3);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("2")){
 				ItemStack I4 = new ItemStack(Bo, A[0]);
 				ItemStack I5 = new ItemStack(Ar, A[1]);
 				ItemStack I6 = new ItemStack(P, A[2]);
 				p.getInventory().addItem(I4);
 				p.getInventory().addItem(I5);
 				p.getInventory().addItem(I6);
 				return true;
 			}
 		}
 		//Storm/Sun
 		else if (cmdLabel.equalsIgnoreCase("weather") && args.length==1 &&  has(p, "weather")  ){
 			if (args[0].equalsIgnoreCase("storm")){
 				w.setStorm(true);
 				sender.sendMessage(R + "Weather set to Storm, Type /Weather Sun to make it sunny");
 				server.broadcastMessage(ChatColor.AQUA + playername + " has set Weather to Storm!");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("sun")){
 				w.setStorm(false);
 				sender.sendMessage(R + "Weather set to Sun, Type /Weather Storm to make it Storm");
 				server.broadcastMessage(ChatColor.AQUA + playername + " has set Weather to Sun!");
 				return true;
 			} else {
 				sender.sendMessage(R + "Did you mean /Weather <Sun/Storm>");
 			}
 			return true;
 		}
 		//PVP true
 		else if (cmdLabel.equalsIgnoreCase("pvp") && has(p, "pvp")  ){
 			if(args.length==1){
 				if(args[0].equalsIgnoreCase("on")){
 					p.getWorld().setPVP(true);
 					server.broadcastMessage(ChatColor.AQUA + "PvP Status: " + G + "On");
 					return true;
 				}
 				else if (args[0].equalsIgnoreCase("off")){
 					p.getWorld().setPVP(false);
 					server.broadcastMessage(ChatColor.AQUA + "PvP Status: " + R + "Off");
 					return true;
 				} else {
 					sender.sendMessage(R + "Did you mean /PvP [On/Off]");
 				}
 				return true;
 			}//end length1
 			else if (args.length==2){
 				World wo = server.getWorld(args[0]);
 				if(wo == null){
 					sender.sendMessage(R + "World not found!");
 				}
 				if(args[1].equalsIgnoreCase("on")){
 					wo.setPVP(true);
 					server.broadcastMessage(ChatColor.AQUA + args[0] + " PvP Status: " + R + "On");
 					return true;
 				}
 				else if(args[1].equalsIgnoreCase("off")){
 					wo.setPVP(true);
 					server.broadcastMessage(ChatColor.AQUA + args[0] + " PvP Status: " + R + "Off");
 					return true;
 				} else {
 					sender.sendMessage(R + "Did you mean /PvP [On/Off] or /PvP [WorldName] [On/Off]");
 				}
 			} else {
 				sender.sendMessage(R + "Did you mean /PvP [On/Off] or /PvP [WorldName] [On/Off]");
 			}
 				return true;
 		}
 		//Broadcast1
 		else if (cmdLabel.equalsIgnoreCase("broadcast1") && has(p, "broadcast1")  ){
 			server.broadcastMessage(R + "No Griefing");
 			return true;
 		}
 		//Broadcast2
 		else if (cmdLabel.equalsIgnoreCase("broadcast2") && has(p, "broadcast2")  ){
 			server.broadcastMessage(B + playername + R + "FTW!");
 			return true;
 		}
 		//ExplodeMe 
 		else if (cmdLabel.equalsIgnoreCase("ExplodeMe") && has(p, "explodeme")  ){
 			w.createExplosion(loc, 1);
 			return true;
 		}
 		//Shows version Info -------------------------------- Update
 		else if (cmdLabel.equalsIgnoreCase("info") && has(p, "info")  ){
 			sender.sendMessage(G + this.getDescription().getVersion() +  " Created by emericask8ur. Follow him on twitter: @emericask8ur");
 			return true;
 		}
 		//TimeDay
 		else if (cmdLabel.equalsIgnoreCase("time") && args.length==1 && has(p, "time")){
 			if(args[0].equalsIgnoreCase("day")){
 				sender.sendMessage("You have set the time to Day");
 				server.broadcastMessage(ChatColor.AQUA + playername + " has set time to Day!");
 				w.setTime(1);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("night")){
 				sender.sendMessage("You have set the time to Night");
 				server.broadcastMessage(ChatColor.AQUA + playername + " has set time to Night!");
 				w.setTime(18000L);
 				return true;
 			} else {
 				sender.sendMessage("Did you mean /Time <day/night> ");
 			}
 			return true;
 
 		}
 		//effect1
 		else if (cmdLabel.equalsIgnoreCase("effect") && has(p, "effect")  ){
 			if(!(args.length==1)){
 				sender.sendMessage(R + "Did you mean /Effect 1/2/3");
 			}
 			if(args[0].equalsIgnoreCase("1")){
 				p.getWorld().playEffect(p.getLocation(), Effect.EXTINGUISH, 0);
 				sender.sendMessage(B + "You have played Effect1!");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("2")){
 				p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, A[2]);
 				sender.sendMessage(B + "You have played Effect2!");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("3")){
 				p.getWorld().playEffect(p.getLocation(), Effect.BOW_FIRE, A[0]);
 				sender.sendMessage(B + "You have played Effect3!");
 				return true;
 			} else {
 				sender.sendMessage(R + "Did you mean /Effect 1/2/3");
 			}
 			return true;
 		}
 		//heal command
 		else if (cmdLabel.equalsIgnoreCase("heal") && has(p, "heal")  ){
 			if(args.length==0){
 				p.setHealth(20);
 				sender.sendMessage(G + "You have healed yourself!");
 			}
 			else if (args.length==1){
 			Player heal = server.getPlayer(args[0]);
 			if(heal !=null){
 				server.getPlayer(args[0]).setHealth(20);
 				sender.sendMessage(B + "You have Healed " + args[0] + " !");
 			} else {
 				sender.sendMessage(R + "Player not found!");
 				}
 			} else {
 				sender.sendMessage(R + "Too many arguments! /Heal [User]");
 			}
 			return true;
 		}
 		//ThunderOff
 		else if (cmdLabel.equalsIgnoreCase("thunder") && has(p, "weather")  ){
 			if(!(args.length==1)){
 				sender.sendMessage(R + "Did you mean /Thunder [On/Off]");
 			}
 			if(args[0].equalsIgnoreCase("off")){
 				w.setThundering(false);
 				sender.sendMessage(R + "Thunder is turned off, Command /Thunder On Enables it!");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("on")){
 				w.setThundering(true);
 				sender.sendMessage(R + "Thunder is turned on, Command /Thunder off Disables it!");
 				return true;
 			}
 			return true;
 		}
 		//Super
 		else if (cmdLabel.equalsIgnoreCase("super") && has(p, "superjump")  ){
 			Vector vec = new Vector(1, 2, 1);
 			p.getVelocity();
 			p.setVelocity(vec);
 			sender.sendMessage("You Just Super Jumped!");
 			return true;
 		}
 		//super off
 		else if (cmdLabel.equalsIgnoreCase("superoff") && has(p, "superjump")  ){
 			Vector slowvec = new Vector(0, 0, 0);
 			p.getVelocity();
 			p.setVelocity(slowvec);
 		}
 		//Clearing
 		else if (cmdLabel.equalsIgnoreCase("clearinv") && has(p, "clearinventory")  ){
 			p.getInventory().clear();
 			sender.sendMessage(B + "Inv Cleared");
 			return true;
 		}
 		//Location
 		else if (cmdLabel.equalsIgnoreCase("location") && has(p, "location")  ){
 			sender.sendMessage("You Cords are " +  "X- " + loc.getBlockX() +" Y- " + loc.getBlockY() + " Z- " +  loc.getBlockZ());
 			return true;
 		}
 		//Broadcast loc
 		else if (cmdLabel.equalsIgnoreCase("broadcastloc") && has(p, "broadcastlocation")  ){
 			server.broadcastMessage(Y + playername + " Is at X= " + loc.getBlockX() +" Y= " + loc.getBlockY() +" Z= " + loc.getBlockZ());
 			return true;
 		}
 		//itemspawner
 		else if (cmdLabel.equalsIgnoreCase("i") && args.length==1 && has(p, "item")  ){
 			int data = -1;
 		    int index = args[0].lastIndexOf(':');
 		    if (index != -1) {
 		        try {
 		        	data = Byte.parseByte(args[0].substring(index + 1));
 		        	args[0] = args[0].substring(0, index);
 		        } catch (Exception ex) {}
 		    }
 			Material type = HashMapz.getMaterial(args[0]);
 			if (type == null) {
 				sender.sendMessage(R + "Invalid item name");
 				return true;
 			}
 			ItemStack item = new ItemStack(type, type.getMaxStackSize());
 			if (data != -1) item.setDurability((short) data);
 			p.getInventory().addItem(item);
 			sender.sendMessage(ChatColor.AQUA + "You have gave yourself " + args[0]);
 			return true;	    					 
 		}
 		//REAL MODE 
 		else if (cmdLabel.equalsIgnoreCase("realmode") && has(p, "realmode")  ){
 			EL.usePorkDrops = !EL.usePorkDrops;
 			if (EL.usePorkDrops) {
 				sender.sendMessage("Players will now drop pork.");
 			} else {
 				sender.sendMessage("Players will no longer drop pork.");
 			}
 			return true;
 		}
 		//AFK
 		else if (cmdLabel.equalsIgnoreCase("afk") && has(p, "afkmessage")  ){
 			p.setDisplayName("[AFK] " +  playername);
 			server.broadcastMessage(playername + " is now AFK");
 			return true;
 		}
 		//AFKOFF
 		else if (cmdLabel.equalsIgnoreCase("afkoff") && has(p, "afkmessage")  ){
 			p.setDisplayName(playername);
 			server.broadcastMessage(playername + " is no longer AFK");
 			return true;
 		}
 		//MysteryBox
 		else if (cmdLabel.equalsIgnoreCase("mysterybox") && has(p, "mysterybox")  ){
 			BL.Mystery =!BL.Mystery;
 			if (BL.Mystery) {
 				sender.sendMessage("Mystery will now begin!");
 			} else {
 				sender.sendMessage("Mystery off.");
 			}
 			return true;   		
 		}
 		//Teleport
 		else if (cmdLabel.equalsIgnoreCase("teleport") && has(p, "teleport")  ){
 			if(args.length==0){
 				sender.sendMessage(R + "Did you mean /Teleport <Username> ?");
 			}
 			else if (args.length==1){
 			if(server.getPlayer(args[0]).isOnline()) {
 				p.teleport(server.getPlayer(args[0]).getLocation());
 				p.sendMessage(ChatColor.GREEN + "Teleported you to " + ChatColor.BLUE + server.getPlayer(args[0]).getName());
 				return true;
 			} else {
 				sender.sendMessage("Invalid Arugments or Name!");
 			}
 			}
 			return true;
 		}      
 		//No Mobs
 		else if (cmdLabel.equalsIgnoreCase("nomobs") && has(p, "nomobs")  ){
 			for (Entity e : w.getEntities()) {
 				if (!(e instanceof Player)) {
 					if (e instanceof LivingEntity) {        
 						e.remove();            
 					}
 				}
 			}
 			sender.sendMessage("You have cleared all Mobs!");
 			return true;
 		}
 		//ToolLast
 		else if (cmdLabel.equalsIgnoreCase("toolLast") && has(p, "toollast")  ){
 			PL.Tools =!PL.Tools;
 			if (PL.Tools) {
 				sender.sendMessage("Everlasting Tools Enabled!");
 			} else {
 				sender.sendMessage("ToolLast off.");
 			}
 			return true;   		
 		}
 		//WorldGen type name
 		else if (cmdLabel.equalsIgnoreCase("worldgen") && args.length==2 && has(p, "worldgen")  ){
 			if(!(args.length==2)){
 				sender.sendMessage(R + "Did you mean /Worldgen <Nether/Normal/End> <worldname> ");
 			}
 			if(args[0].equalsIgnoreCase("normal")){
 				WorldCreator c = new WorldCreator(args[1]);
 				c.environment(Environment.NORMAL);
 				c.createWorld();
 				sender.sendMessage(G + "You have Made a new World! " + ChatColor.AQUA + args[1]);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("nether")){
 				WorldCreator c = new WorldCreator(args[1]);
 				c.environment(Environment.NETHER);
 				c.createWorld();
 				sender.sendMessage(G + "You have Made a new World! " + ChatColor.AQUA + args[1]);
 				return true;
 			}
 			return true;
 		}
 		//Tools
 		else if (cmdLabel.equalsIgnoreCase("tools") && has(p, "tools")  ){
 			Material t = Material.DIAMOND_PICKAXE;
 			Material t1 = Material.DIAMOND_AXE;
 			Material t2 = Material.DIAMOND_SPADE;
 			ItemStack i = new ItemStack(t,A[0]);
 			ItemStack i2 = new ItemStack(t1,1);
 			ItemStack i3 = new ItemStack(t2, 1);
 			p.getInventory().addItem(i);
 			p.getInventory().addItem(i2);
 			p.getInventory().addItem(i3);
 			return true;
 		}
 		//LoginKit
 		else if (cmdLabel.equalsIgnoreCase("loginkit") && has(p, "loginkit")  ){
 			PL.TheKit = !PL.TheKit;
 			if(PL.TheKit){
 				sender.sendMessage("LoginKit Enabled! On Login Players will receive a Start Kit");	    			
 			} else {
 				sender.sendMessage("LoginKit Disabled!");
 			}
 			return true;
 		}
 		//Save
 		else if (cmdLabel.equalsIgnoreCase("save")){
 			if(args.length==0){
 				if(p.hasPermission("Sidekick.save")){
 				p.setFireTicks(0);
 				sender.sendMessage(ChatColor.YELLOW + "Fire removed!");
 				return true;
 				} else {
 					sender.sendMessage(R + "You do not have permission!");
 				}
 			} else {
 				sender.sendMessage(R + "Did you mean /Save");
 			}
 			return true;
 		}
 		//Tp
 		else if(cmdLabel.equalsIgnoreCase("tp") && has(p, "teleport")  ) {
 			if(args.length==0){
 				sender.sendMessage(R + "Did you mean /Tp <Username> or /ToMe <Username> ?");
 			}
 			else if (args.length==1){
 			Player tel = server.getPlayer(args[0]);
 			if(tel !=null){
 				try{
 				p.teleport(server.getPlayer(args[0]).getLocation());
 				p.sendMessage(ChatColor.GREEN + "Teleported you to " + ChatColor.BLUE + server.getPlayer(args[0]).getName());
 				}catch (Exception i){
 					sender.sendMessage(R + "Cannot perform teleport!");
 				}
 				return true;
 			} else {
 				sender.sendMessage("Player not found!!");
 				}
 			}
 			else if (args.length==2){
 				Player player = server.getPlayer(args[0]);
 				Player play = server.getPlayer(args[1]);
 				if(player == null){
 					sender.sendMessage(R + "Player " + args[0] + " not found!");
 				}
 				if(play == null){
 					sender.sendMessage(R + "Player " + args[1] + " not found!");
 				}
 				try{
 				player.teleport(play.getLocation());
 				sender.sendMessage(Y + "Player " + args[0] + " teleported to " + args[1]);
 				}catch (Exception i){
 					sender.sendMessage(R + "Cannot perform teleport!");
 				}
 				return true;
 			} else {
 				sender.sendMessage(R + "Did you mean /Tp <Player> | /Tp <Player> <Player>");
 			}
 			return true;
 		}
 		//God Mode
 		else if (cmdLabel.equalsIgnoreCase("god")){
 			try{
 			if(args.length==1){
 				if(!(p.hasPermission("Sidekick.god.other"))){
 					sender.sendMessage(R + "You do not have permission!");
 				}
 				Player play = server.getPlayer(args[0]);
 				PlayerInfo tri = PlayerInfo.get(play);
 				tri.godmode = !tri.godmode;
 				if(play !=null){
 					if(tri.godmode){
 						sender.sendMessage(G + "God mode " + G + "enabled" + G + " for user " + ChatColor.YELLOW + play.getName());
 					} else {
 						sender.sendMessage(G + "God mode " + R + "disabled" + G + " for user " + ChatColor.YELLOW + play.getName());
 					}
 				} else {
 					sender.sendMessage(R + "Player not found!");
 				}
 				return true;
 			}
 			else if (args.length==0){
 			if(p.hasPermission("SideKick.god")){
 				PlayerInfo pi = PlayerInfo.get(p);
 				pi.godmode = !pi.godmode;
 				if(pi.godmode){
 					sender.sendMessage("God Mode Enabled!");
 				} else {
 					sender.sendMessage("God Mode Disabled!");
 				}//args 0
 			}
 			return true;
 			}
 			}catch (Exception i){}
 		}
 		//AntiBuild 
 		else if (cmdLabel.equalsIgnoreCase("antibuild") && has(p, "antibuild")  ){
 			BL.grief =!BL.grief;
 			if(BL.grief){
 				sender.sendMessage(Y + "AntiBuild Griefing Tool Enabled, Dont Let Anyone Place Blocks!");
 				sender.sendMessage( Y + "AntiBuild Disabled!");
 			}
 			return true;
 		}
 		//SignName
 		else if (cmdLabel.equalsIgnoreCase("signname") && has(p, "signname")  ){
 			BL.SignCh =!BL.SignCh;
 			if(BL.SignCh){
 				sender.sendMessage("SignName On!");
 			} else {
 				sender.sendMessage("SignName Off");
 			}
 			return true;
 		}
 		//Portal
 		else if (cmdLabel.equalsIgnoreCase("portal") && has(p, "portal")  ){
 			BL.portal =!BL.portal;
 			if(BL.portal){
 				sender.sendMessage("Portal Activated!");
 			} else {
 				sender.sendMessage("Portal Deactivated!");
 			}
 			return true;
 		}
 		//give
 		else if (cmdLabel.equalsIgnoreCase("give") && has(p, "item")  ){
 			try{
 			if(args.length==1){
 				int data = -1;
 			    int index = args[0].lastIndexOf(':');
 			    if (index != -1) {
 			        try {
 			        	data = Byte.parseByte(args[0].substring(index + 1));
 			        	args[0] = args[0].substring(0, index);
 			        } catch (Exception ex) {}
 			    }
 				Material type = HashMapz.getMaterial(args[0]);
 				if (type == null) {
 					sender.sendMessage(R + "Invalid item name");
 					return true;
 				}
 				ItemStack item = new ItemStack(type, type.getMaxStackSize());
 				if (data != -1) item.setDurability((short) data);
 				try{
 				p.getInventory().addItem(item);
 				sender.sendMessage(G + "You gave yourself: " + args[0]);
 				return true;
 				}catch (Exception i){}
 				return true;	  
 			}
 			else if (args.length==2){
 				Player player = server.getPlayer(args[0]);
 				if(player == null){
 					sender.sendMessage(R + "Player not found!");
 				}
 				int data = -1;
 			    int index = args[1].lastIndexOf(':');
 			    if (index != -1) {
 			        try {
 			        	data = Byte.parseByte(args[1].substring(index + 1));
 			        	args[1] = args[1].substring(0, index);
 			        } catch (Exception ex) {}
 			    }
 				Material type = HashMapz.getMaterial(args[1]);
 				if (type == null) {
 					sender.sendMessage(R + "Invalid item name");
 					return true;
 				}
 				ItemStack item = new ItemStack(type, type.getMaxStackSize());
 				if (data != -1) item.setDurability((short) data);
 				try{
 				player.getInventory().addItem(item);
 				sender.sendMessage(G + "You gave yourself: " + args[1]);
 				return true;
 				}catch (Exception i){}
 				return true;	  
 			}
 			else if (args.length==3){
 				int amount = Integer.parseInt(args[2]);
 				Player player = server.getPlayer(args[0]);
 				if(player == null){
 					sender.sendMessage(R + "Player not found!");
 				}
 				int data = -1;
 			    int index = args[1].lastIndexOf(':');
 			    if (index != -1) {
 			        try {
 			        	data = Byte.parseByte(args[1].substring(index + 1));
 			        	args[1] = args[1].substring(0, index);
 			        } catch (Exception ex) {}
 			    }
 				Material type = HashMapz.getMaterial(args[1]);
 				if (type == null) {
 					sender.sendMessage(R + "Invalid item name");
 					return true;
 				}
 				ItemStack item = new ItemStack(type, amount);
 				if (data != -1) item.setDurability((short) data);
 				try{
 				player.getInventory().addItem(item);
 				sender.sendMessage(G + "You gave yourself: " + args[1]);
 				return true;
 				}catch (Exception i){sender.sendMessage(R + "Enter a valid amount");}
 				return true;	
 			} else {
 				sender.sendMessage(R + "Did you mean /Give [Player] [Item] <Amount> or /Give [Item]");
 			}
 			return true;
 			}catch (Exception i){sender.sendMessage(R + "Did you mean /Give [Player] [Item] <Amount> or /Give [Item]");}
 		}
 		//File
 		else if (cmdLabel.equalsIgnoreCase("rules")){
 			if(p.hasPermission("sidekick.rules")){
 				p.sendMessage(rules[0]);
 				p.sendMessage(rules[1]);
 				p.sendMessage(rules[2]);
 				p.sendMessage(rules[3]);
 				return true;
 			} else {
 				p.sendMessage(R + "You don't have permission to use this!");
 			}
 		}
 		//Sneaks
 		else if (cmdLabel.equalsIgnoreCase("sneaks") && has(p, "sneaks")  ){
 			PL.Sneak =!PL.Sneak;
 			if(PL.Sneak){
 				sender.sendMessage("Sneaking Is Disabled! Repeat Command to Enable!");        		 
 			} else {
 				sender.sendMessage("Sneaking Is Now Enabled");
 			}
 			return true;
 		}
 		//nofire
 		else if (cmdLabel.equalsIgnoreCase("nofire") && has(p, "nofire")  ){
 			BL.burn =!BL.burn;
 			if(BL.burn){
 				sender.sendMessage ("NoFire Enabled!");
 			} else {
 				sender.sendMessage("NoFire Disabled!");
 			}
 			return true;
 		}
 		//Fly
 		else if (cmdLabel.equalsIgnoreCase("fly") && has(p, "fly")  ){
 			PL.fly = !PL.fly;
 			if(PL.fly){
 				sender.sendMessage("Click with Sign in Hand!");
 			} else {
 				sender.sendMessage(R + "Fly Disabled");
 			}
 			return true;
 		} 
 		/* ----- Do not add Below here ----- */
 		else if (cmdLabel.equalsIgnoreCase("sk")  ) {
 			if (args.length != 0) {
 				if (args[0].equalsIgnoreCase("money") && has(p, "sk.money")  ) {
 					if(Economy.economy.containsKey(p.getName())) {
 						p.sendMessage(ChatColor.GREEN + "You have " + Economy.economy.get(p.getName()) + " money");
 					} else{
 						p.sendMessage(ChatColor.RED + "You have no money!");
 					}
 				} else if (args[0].equalsIgnoreCase("pay") && has(p, "sk.pay")  ) {
 					if (args.length == 3) {
 						String username = args[1];
 						try {
 							double amount = Double.parseDouble(args[2]);
 							if (amount > 0) {
 								double money = Economy.economy.get(p.getName());
 								money -= amount;
 								if (money > 0) {
 									Economy.economy.put(p.getName(), money);
 									double usermoney = Economy.economy.get(username);
 									usermoney += amount;
 									Economy.economy.put(username, usermoney);
 									p.sendMessage(ChatColor.GREEN + "You payed '" + username + "' " + amount + " money");
 									Player user = Bukkit.getServer().getPlayer(username);
 									if (user != null) {
 										user.sendMessage(p.getName() + " sent you " + amount + " money!");
 									}
 								} else {
 									p.sendMessage(ChatColor.RED + "You have insufficient money! :(");
 								}
 							} else {
 								p.sendMessage(ChatColor.RED + "Please enter a positive money value!");
 							}
 						} catch (Exception ex) {
 							//invalid numeric amount value
 							p.sendMessage(ChatColor.RED + "Please enter a valid number!");
 						}
 					} else {
 						sender.sendMessage("Invalid Arguments");
 					}
 				} else if (args[0].equalsIgnoreCase("give") && has(p, "sk.give")) {
 					if (args.length == 3) {
 						String username = args[1];
 						try {
 							double amount = Double.parseDouble(args[2]);
 							double money = Economy.economy.get(username);
 							money += amount;
 							Economy.economy.put(username, money);
 							p.sendMessage(ChatColor.GREEN + "You gave '" + username + "' " + amount + " money");						
 							Player user = Bukkit.getServer().getPlayer(username);
 							if (user != null) {
 								if (amount < 0) {
 									user.sendMessage(p.getName() + " took " + -amount + " money from you!");
 								} else {
 									user.sendMessage(p.getName() + " sent you " + amount + " money!");
 								}
 							}
 						} catch (Exception ex) {
 							//invalid numeric amount value
 							p.sendMessage(ChatColor.RED + "Please enter a valid number!");
 						}
 					} else {
 						sender.sendMessage("Invalid Arguments!");
 					}
 				} else {
 					sender.sendMessage("Invalid Arguments!");
 				}
 			} else {
 				sender.sendMessage("Invalid Arguments!");
 			}
 		}
 		return true;
 	}
 }
