 package me.criztovyl.blockreichtools;
 
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Logger;
 
 import me.criztovyl.blockreichtools.config.Config;
 import me.criztovyl.blockreichtools.tools.MySQL;
 import me.criztovyl.clickless.ClicklessPlugin;
 import me.criztovyl.timeshift.MicroShift;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * This is BlockReichTools. Tools for your Minecraft Bukkit Server!
  * @author criztovyl
  *
  */
 public class BlockReichTools extends JavaPlugin{
 	private static Logger log;
 	private static Connection con;
 	/**
 	 * Bukkit Stuff	 */
 	public void onEnable(){
 		log = this.getLogger();
 		log.info("BlockReichTools enabeling");
 		this.saveDefaultConfig();
 		Bukkit.getPluginManager().registerEvents(new Listeners(), this);
 		log.info("Registered Listeners");
 		String query = "";
 		try{
 			con = DriverManager.getConnection("jdbc:mysql://" + Config.HostAddress(), Config.HostUser(), Config.HostPassword());
 		} catch (SQLException e) {
 			severe("Could not get Database Connection! Error:\n" + e.toString());
 			disable();
 		}
 		try{
 			con = getConnection();
 			Statement stmt = con.createStatement();
 			if(Config.isSet("MySQL.Host.Table_Users")){
 				query = String.format("Create table if not exists %s (id int auto_increment not null primary key, user varchar(50), lastlog date, password varchar(40), userhash varchar(40))", 
 						Config.UsersTable());
 				stmt.executeUpdate(query);
 			}
 			else{
 				log.warning("The Users Table is not set in the Config!");
 				disable();
 			}
 			if(Config.isSet("MySQL.Host.Table_Signs")){
 				query = String.format("Create table if not exists %s (id int auto_increment not null primary key, LocX int, LocY int, LocZ int, LocWorldUUID varchar(128), Pos varchar(10), Multi boolean default 1, Type varchar(20))",
 						Config.SignsTable());
 				stmt.executeUpdate(query);
 			}
 			else{
 				log.warning("The Signs Table is not set in the Config!");
 				disable();
 			}
 			con.close();
 		} catch (SQLException e){
 			severe("Failed to Check Tables! Error:\n" + e.toString() + "\n@Query: " + query);
 		}
 		log.info("Checked Tables");
 		MySQL.loadSigns();
 		log.info("Loaded Signs");
 		log.info("BlockReichTools enabled");
 		if(!Config.isSet("MySQL.Global_Salt")){
 			log.warning("You must set a Global Salt in the Config!(MySQL.Global_Salt)");
 			disable();
 		}
 	}
 	private void disable(){
 		this.getPluginLoader().disablePlugin((Plugin) this);
 	}
 	/**
 	 * Bukkit Stuff
 	 */
 	public void onDisable(){
 		log.info("BlockReichTools disabled");
 	}
 	/**
 	 * Bukkit Stuff
 	 */
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		if(sender instanceof Player){
 			Player player = (Player) sender;
 			if(cmd.getName().equalsIgnoreCase("BlockReichTools")){
 				if(args.length > 0){
 					if(args[0].toLowerCase().equals("day")){
 						player.getWorld().setTime(800);
 						return true;
 					}
 					if(args[0].toLowerCase().equals("rain")){
 						player.getWorld().setStorm(!player.getWorld().hasStorm());
 						return true;
 					}
 					if(args[0].toLowerCase().equals("inv")){
 						if(args[1].toLowerCase().equals("nodirt")){
 							if(args.length == 3){
 								try{
 									ItemStack items;
 									items = new ItemStack(Material.DIRT, Integer.parseInt(args[2]));
 									player.getInventory().removeItem(items);
 								} catch(NumberFormatException e){
 									player.sendMessage(ChatColor.RED + "'amount' muss eine Zahl sein!");
 									player.sendMessage(ChatColor.YELLOW + "/brt inv nodirt amount");
 									return true;
 								}				
 							}
 							else{
 								player.getInventory().remove(Material.DIRT);
 							}
 							
 							player.sendMessage(ChatColor.GREEN + "Dirt entfernt.");
 							return true;
 						}
 						if(args[1].toLowerCase().equals("clear")){
 							player.getInventory().clear();
 							player.sendMessage(ChatColor.GREEN + "Inventar geleert.");
 							return true;
 						}
 					}
 					if(args[0].toLowerCase().equals("help")){
						player.sendMessage(ChatColor.GREEN + "/brt day: Zeit auf Tag setzten (08:00h)");
						player.sendMessage(ChatColor.GREEN + "/brt inv nodirt [X]: entfernt allen oder X Dirtblöcke aus deinem Inventar.");
 						player.sendMessage(ChatColor.GREEN + "/brt inv clear: leert deinen Inventar.");
 						player.sendMessage(ChatColor.GREEN + "/brt rain: Ändert das Wetter");
 						player.sendMessage(ChatColor.GREEN + "/brt passwort: Setzt das Passwort für das WebInterface.");
 						player.sendMessage(ChatColor.GREEN + "/brt help: Diese Hilfe.");
 						return true;
 					}
 					if(args[0].toLowerCase().equals("password") || equals("passwd") || equals("passwort") || equals("pw")){
 						ClicklessPlugin.getShiftHelper().addShifted(new MicroShift() {
							
 							@Override
 							public boolean getSuccess() {
								// TODO Auto-generated method stub
								return false;
 							}
 							
 							@Override
 							public String getQuestion() {
								// TODO Auto-generated method stub
								return null;
 							}
 							
 							@Override
 							public String getPlayer() {
								// TODO Auto-generated method stub
								return null;
 							}
 							
 							@Override
 							public void executeAction(AsyncPlayerChatEvent arg0) {
								// TODO Auto-generated method stub
								
 							}
 						});
 						return true;
 					}
 					if(args[0].toLowerCase().equals("mysql")){
 						return MySQL.MySQLCommand(args, sender);
 					}
 				}
 
 			}//BRT END
 			if(cmd.getName().equalsIgnoreCase("mysql")){
 				return MySQL.MySQLCommand(args, sender);
 			}
 		}
 		else{
 			if(cmd.getName().equalsIgnoreCase("blockreichtools")){
 				info("BlockReichTools working ;)");
 				return true;
 			}
 			if(cmd.getName().equalsIgnoreCase("mysql")){
 				return MySQL.MySQLCommand(args, sender);
 			}
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * The BlockReichTools Logger.
 	 * @return the Logger
 	 */
 	public static Logger getLog(){
 		return log;
 	}
 	/**
 	 * Logs a "[SEVERE]" Message.
 	 * @param msg - Severe Message to log.
 	 */
 	public static void severe(String msg){
 		log.severe(msg);
 	}
 	/**
 	 * Logs a "[INFO]" Message.
 	 * @param msg - Info Message to log.
 	 */
 	public static void info(String msg){
 		log.info(msg);
 	}
 	public static Connection getConnection() {
 		try {
 			if(con.isClosed()){
 				con = DriverManager.getConnection("jdbc:mysql://" + Config.HostAddress(), Config.HostUser(), Config.HostPassword());
 			}
 			return con;
 		} catch (SQLException e) {
 			severe(e.toString() + "\n @ BRT.getCon");
 			return null;
 		}
 	}
 }
