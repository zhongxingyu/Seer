 package me.kantenkugel.serveress.whitelisted;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Whitelisted extends JavaPlugin {
 	//public boolean SqlOn, SqlEn;												//SqlOn -> No errors; SqlEn -> Config
 	//public boolean Customtable, SqlMode;										//SqlMode: 0->Own, 1->External
 	//public String Sqlname;
 	public String chatprefix;
 	public String whitelistmsg;
 	public boolean showconsolelog;
 	public PluginDescriptionFile pdf;
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public List<String> whitelisted;
 	
 	public void onEnable() {
 		pdf = this.getDescription();
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new WlListener(this), this);
 		chatprefix = "["+pdf.getName()+"] ";
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 		logger.info(chatprefix + "v" + pdf.getVersion() + " is now enabled!");
 	}
 	
 	public void onDisable() {
 		logger.info(chatprefix + "is now disabled!");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player && !(sender.hasPermission("whitelisted.admin"))) {
 			sender.sendMessage(ChatColor.RED + chatprefix + "You dont have the Permission to do that!");
 			return true;
 		} else if(args.length > 0) {
 			switch(args[0].toLowerCase()) {
 			case "list":
 				refreshlist();
 				if(whitelisted.isEmpty()) {
 					report(sender, "No Players are whitelisted!", ChatColor.YELLOW);
 					break;
 				}
 				String seplist = "";
 				for(String pl: whitelisted) {
 					seplist = seplist + pl + ", ";
 				}
 				report(sender, "Players in list: ", ChatColor.YELLOW);
 				report(sender, seplist, ChatColor.GREEN);
 				break;
 			case "add":
 				if(args.length != 2) {
 					report(sender, "You have to specify a player.", ChatColor.RED);
 					report(sender, "e.g. /whitelist add Steve", ChatColor.RED);
 				} else {
 					refreshlist();
 					if(whitelisted.contains(args[1].toLowerCase())) report(sender, "That Player is already whitelisted", ChatColor.GOLD);
 					else {
 						whitelisted.add(args[1].toLowerCase());
 						this.getConfig().set("Whitelist", whitelisted);
 						this.saveConfig();
 						report(sender, "Player added to whitelist", ChatColor.GREEN);
 					}
 				}
 				break;
 			case "rem":
 			case "remove":
 			case "rm":
 				if(args.length != 2) {
 					report(sender, "You have to specify a player.", ChatColor.RED);
 					report(sender, "e.g. /whitelist rem Steve", ChatColor.RED);
 				} else {
 					refreshlist();
 					if(whitelisted.contains(args[1].toLowerCase()) == false) report(sender, "That Player is not whitelisted", ChatColor.GOLD);
 					else {
 						whitelisted.remove(args[1].toLowerCase());
 						this.getConfig().set("Whitelist", whitelisted);
 						this.saveConfig();
 						report(sender, "Player removed from whitelist", ChatColor.GREEN);
 					}
 					
 				}
 				break;
 			default:
 				return false;
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	private void report(CommandSender sender, String msg, ChatColor color) {
 		if(sender == getServer().getConsoleSender()) {
 			logger.info(chatprefix + msg);
 		} else if(sender instanceof Player) {
 			sender.sendMessage(color + chatprefix + msg);
 		}	
 	}
 	
 	public void refreshlist() {
 		this.reloadConfig();
 		whitelisted = this.getConfig().getStringList("Whitelist");
 		showconsolelog = this.getConfig().getBoolean("Config.ShowLog", false);
 		whitelistmsg = this.getConfig().getString("Config.WhitelistMsg", "You are not whitelisted!");
 		
 	}
 	
 	
 
 }
