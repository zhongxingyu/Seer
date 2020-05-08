 package me.thefiscster510.noadminkick;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import me.thefiscster510.noadminkick.Main;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin{
 	 public static Main plugin;
 	 public final Logger logger = Logger.getLogger("Minecraft");
 	 
 	 private final EnvListen pListener = new EnvListen(); 
 	 public final HashMap<Player, Player> pm = new HashMap<Player, Player>();
 	 public boolean AdminOn = false;
 	 @Override
 	 public void onEnable() {
 		 plugin = this;
 		 PluginDescriptionFile pdffile = this.getDescription();
 		 PluginManager pm = getServer().getPluginManager();
 		 pm.registerEvents(this.pListener, this);
 		 this.logger.info("["+pdffile.getName()+"] " + pdffile.getName() + " V" + pdffile.getVersion() + " has been enabled!");	
 		 getConfig().options().copyDefaults(true);
 		 saveConfig();
 		 File admins = new File(Main.plugin.getDataFolder() + File.separator + "Admins.yml");
 		 if(admins.exists()){
 			 
 		 }else{
 			try {
 				admins.createNewFile();
 				FileConfiguration admin = YamlConfiguration.loadConfiguration(admins);
 				ArrayList<String> adminsarr = new ArrayList<String>();
 				admin.set("Admins", adminsarr);
 				admin.save(admins);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		 }
 	 }
 	 @Override
 	 public void onDisable(){
 		 PluginDescriptionFile pdffile = this.getDescription();
 		 this.logger.info("["+pdffile.getName()+"] " + pdffile.getName() + " V" + pdffile.getVersion() + " has been disabled!");	
 	 }
 	 
 	 public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		 
 		 if(commandLabel.equalsIgnoreCase("adminon")){
 			 if(console(sender)){
 			 Player player = (Player)sender;
 			 if(player.hasPermission("noadminkicker.adminon")){
 				 if(AdminOn == false){
 					 AdminOn = true;
 					 pmessage(player, ChatColor.GREEN + "AdminOn set to true");
 				 }else{
 					 AdminOn = false;
 					 pmessage(player, ChatColor.RED + "AdminOn set to false");
 					 brod(Bukkit.getServer(),getConfig().getString("Strings.AllGone"));
 				 }
 			 }else{
 				 pmessage(player, getConfig().getString("Strings.NoPerm"));
 			 }
 			 }else{
 				 if(AdminOn == false){
 					 AdminOn = true;
 					 this.logger.info("AdminOn set to true");
 				 }else{
 					 AdminOn = false;
 					 this.logger.info("AdminOn set to false");
 					 brod(Bukkit.getServer(), getConfig().getString("Strings.AllGone"));
 				 }
 			 }
 		 }else if(commandLabel.equalsIgnoreCase("addadmin")){
 			 if(args.length > 0 && args.length < 2){
 				 if(console(sender)){
 					 Player player = (Player)sender;
 					 if(player.hasPermission("noadminkicker.addadmin")){
 						 File admins = new File(Main.plugin.getDataFolder() + File.separator + "Admins.yml");
 						 FileConfiguration admin = YamlConfiguration.loadConfiguration(admins);
 						 List<String> list = admin.getStringList("Admins");
 						 if(!list.contains(args[0])){
 							 list.add(args[0]);
 							 pmessage(player, ChatColor.YELLOW + args[0] + ChatColor.GREEN + " added to admin list.");
 							 admin.set("Admins", list);
 						 }else{
 							 pmessage(player, ChatColor.YELLOW + args[0] + ChatColor.RED + " is already in the list.");
 						 }
 						 try {
 							admin.save(admins);
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					 }else{
 						 pmessage(player, getConfig().getString("Strings.NoPerm"));
 					 }
 				 }else{
 					 File admins = new File(Main.plugin.getDataFolder() + File.separator + "Admins.yml");
 					 FileConfiguration admin = YamlConfiguration.loadConfiguration(admins);
 					 List<String> list = admin.getStringList("Admins");
 					 if(!list.contains(args[0])){
 						 list.add(args[0]);
 						 this.logger.info(args[0] + " added to admin list.");
 						 admin.set("Admins", list);
 					 }else{
 						 this.logger.info(args[0] + " is already in the list.");
 					 }
 					 try {
 						admin.save(admins);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					 
 				 }
 			 }else{
 				 if(console(sender)){
 					 Player player = (Player)sender;
 					 pmessage(player, getConfig().getString("Strings.InvalidParams"));
 				 }else{
 					 this.logger.info(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Strings.InvalidParams")));
 				 }
 			 }
 		 }else if(commandLabel.equalsIgnoreCase("deladmin")){
 			 if(args.length > 0 && args.length < 2){
 				 if(console(sender)){
 					 Player player = (Player)sender;
 					 if(player.hasPermission("noadminkicker.deladmin")){
 						 File admins = new File(Main.plugin.getDataFolder() + File.separator + "Admins.yml");
 						 FileConfiguration admin = YamlConfiguration.loadConfiguration(admins);
 						 List<String> list = admin.getStringList("Admins");
 						 if(!list.contains(args[0])){
 							 pmessage(player, ChatColor.YELLOW + args[0] + ChatColor.RED + " is not an admin.");
 						 }else{
 							 list.remove(args[0]);
 							 pmessage(player, ChatColor.YELLOW + args[0] + ChatColor.GREEN + " removed from the admin list.");
 							 admin.set("Admins", list);
 						 }
 						 try {
 							admin.save(admins);
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					 }else{
 						 pmessage(player, getConfig().getString("Strings.NoPerm"));
 					 }
 				 }else{
 					 File admins = new File(Main.plugin.getDataFolder() + File.separator + "Admins.yml");
 					 FileConfiguration admin = YamlConfiguration.loadConfiguration(admins);
 					 List<String> list = admin.getStringList("Admins");
 					 if(!list.contains(args[0])){
 						 this.logger.info(args[0] + " is not an admin.");
 					 }else{
 						 list.remove(args[0]);
 						 this.logger.info(args[0] + " removed from the admin list.");
 						 admin.set("Admins", list);
 					 }
 					 try {
 						admin.save(admins);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					 
 				 }
 			 }else{
 				 if(console(sender)){
 					 Player player = (Player)sender;
 					 pmessage(player, getConfig().getString("Strings.InvalidParams"));
 				 }else{
 					 this.logger.info(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Strings.InvalidParams")));
 				 }
 			 }
 		 }else if(commandLabel.equalsIgnoreCase("onlineadmins")){
 			 if(console(sender)){
 				Player player = (Player)sender;
 				if(player.hasPermission("noadminkicker.onlineadmins")){
 				String ret = "&2Online Admins: &f";
 				 for(Player p : this.getServer().getOnlinePlayers()){
 					 if(isadmin(p)){
 						 if(ret.equalsIgnoreCase("&2Online Admins: &f")){
 							 ret = "&2Online Admins: &f" + p.getDisplayName();
 						 }else{
 							 ret = ret + ", " + p.getDisplayName();
 						 }
 					 }
					 pmessage(player, ret);
 				 }
 				}else{
 					
 				}
 			 }else{
 				 String ret = "Online Admins: ";
 				 for(Player p : this.getServer().getOnlinePlayers()){
 					 if(isadmin(p)){
 						 if(ret.equalsIgnoreCase("Online Admins: ")){
 							 ret = "Online Admins: " + p.getDisplayName();
 						 }else{
 							 ret = ret + ", " + p.getDisplayName();
 						 }
 					 }
 					 this.logger.info(ret);
 				 }
 			 }
 		 }else if(commandLabel.equalsIgnoreCase("adminlist")){
 			 if(console(sender)){
 				 Player player = (Player)sender;
 				 if(player.hasPermission("noadminkicker.adminlist")){
 					 File admins = new File(Main.plugin.getDataFolder() + File.separator + "Admins.yml");
 					 FileConfiguration admin = YamlConfiguration.loadConfiguration(admins);
 					 List<String> list = admin.getStringList("Admins");
 					 String ret = "";
 					 for(String st : list){
 						 if(ret.equalsIgnoreCase("")){
 							 ret = "&2Admins: &f" + st;
 						 }else{
							 ret = ret + st;
 						 }
 					 }
 					 pmessage(player, ret);
 				 }else{
 					 pmessage(player, getConfig().getString("Strings.NoPerm"));
 				 }
 			 }else{
 				 File admins = new File(Main.plugin.getDataFolder() + File.separator + "Admins.yml");
 				 FileConfiguration admin = YamlConfiguration.loadConfiguration(admins);
 				 List<String> list = admin.getStringList("Admins");
 				 String ret = "";
 				 for(String st : list){
 					 if(ret.equalsIgnoreCase("")){
 						 ret = "Admins: " + st;
 					 }else{
 						 ret = ret + ", " + st;
 					 }
 				 }
 				 this.logger.info(ret);
 			 }
 		 }else if(commandLabel.equalsIgnoreCase("admintool")){
 			 if(console(sender)){
 				 Player player = (Player)sender;
 				 if(player.hasPermission("noadminkicker.admintool")){
 					 if(getConfig().getString("S-Area").equalsIgnoreCase("true")){
 						 ItemStack bone = new ItemStack(Material.BONE);
 						 player.getInventory().addItem(bone);
 						 pmessage(player, "&2Left click position 1, Right Click position 2.");
 					 }else{
 						 pmessage(player, "&4To use this tool, you must enable S-Area in the config.");
 					 }
 				 }else{
 					 pmessage(player, getConfig().getString("Strings.NoPerm"));
 				 }
 			 }else{
 				 this.logger.info("This command can only be used in game.");
 			 }
 		 }else if(commandLabel.equalsIgnoreCase("asz")){
 			 if(console(sender)){
 				 
 				 Player player = (Player) sender;
 				 if(player.hasPermission("noadminkicker.admintool")){
 					 if(EnvListen.first.containsKey(player)){
 						 if(EnvListen.second.containsKey(player)){
 							 getConfig().set("Area.F", EnvListen.first.get(player));
 							 getConfig().set("Area.S", EnvListen.second.get(player));
 							 saveConfig();
 							 pmessage(player, "&2S-Area Defined! Use /set-s-spawn to set a SAZ spawn");
 						 }else{
 							 pmessage(player, "&4You must select the first point.");
 						 }
 					 }else{
 						 pmessage(player, "&4You must select the first point.");
 					 }
 				 }else{
 					 pmessage(player, getConfig().getString("Strings.NoPerm"));
 				 }
 			 }else{
 				 this.logger.info("This command can only be used in game.");
 			 }
 		 }else if(commandLabel.equalsIgnoreCase("set-s-spawn")){
 			 if(console(sender)){
 				 Player player = (Player) sender;
 				 if(player.hasPermission("noadminkicker.admintool")){
 					 getConfig().set("S-Spawn.X", player.getLocation().getX());
 					 getConfig().set("S-Spawn.Y", player.getLocation().getY());
 					 getConfig().set("S-Spawn.Z", player.getLocation().getZ());
 					 getConfig().set("S-Spawn.YAW", Float.toString(player.getLocation().getYaw()));
 					 saveConfig();
 					 player.sendMessage(ChatColor.GREEN + "S-Spawn Set!");
 				 }else{
 					 pmessage(player, getConfig().getString("Strings.NoPerm"));
 				 }
 			 }else{
 				 this.logger.info("This command can only be used in game.");
 			 }
 		 }else if(commandLabel.equalsIgnoreCase("s-spawn")){
 			 if(console(sender)){
 				 Player player = (Player)sender;
 				 if(player.hasPermission("noadminkicker.spawn")){
 					 Location s = player.getLocation();
 					 if(getConfig().isSet("S-Spawn.X") && getConfig().isSet("S-Spawn.Y") && getConfig().isSet("S-Spawn.Z") && getConfig().isSet("S-Spawn.YAW")){
 						 s.setX(getConfig().getDouble("S-Spawn.X"));
 						 s.setY(getConfig().getDouble("S-Spawn.Y"));
 						 s.setZ(getConfig().getDouble("S-Spawn.Z"));
 						 s.setYaw(Float.parseFloat(getConfig().getString("S-Spawn.YAW")));
 						 player.teleport(s);
 					 }else{
 						 pmessage(player, "&4S-Spawn not set.");
 					 }
 				 }else{
 					 pmessage(player, getConfig().getString("Strings.NoPerm"));
 				 }
 				 
 			 }else{
 				 this.logger.info("This command can only be used in game.");
 			 }
 			 
 		 }
 		 
		 
 		 return true;
 	 }
 	 
 	 	public void kick(Player p, String message){
 			p.kickPlayer(ChatColor.translateAlternateColorCodes('&', message));
 		}
 		public void pmessage(Player p, String message){
 			p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
 		}
 		public void brod(Server s, String message){
 			s.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
 		}
 		public boolean console(CommandSender sender){
 			if(sender instanceof Player){
 				return true;
 			}else{
 				return false;
 			}
 		}
 		public boolean isadmin(Player p){
 			File admins = new File(Main.plugin.getDataFolder() + File.separator + "Admins.yml");
 			FileConfiguration admin = YamlConfiguration.loadConfiguration(admins);
 			List<String> list = admin.getStringList("Admins");
 			if(p.hasPermission("noadminkicker.isadmin") || list.contains(p.getName())){
 				return true;
 			}else{
 				return false;
 			}
 		}
 }
