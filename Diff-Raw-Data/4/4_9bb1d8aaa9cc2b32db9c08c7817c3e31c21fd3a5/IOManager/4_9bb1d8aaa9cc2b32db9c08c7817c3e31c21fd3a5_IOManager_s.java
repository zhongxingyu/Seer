 package org.efreak.warps;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.conversations.Conversable;
 import org.bukkit.plugin.Plugin;
 import org.efreak.warps.util.Translator;
 
 
 /**
  * 
  * The IOManager Handles all Input and Output
  * 
  * @author efreak
  * 
  */
 
 public class IOManager {
 	
 	private static final Configuration config;
 	private static final Plugin plugin;
 	public static String prefix = ChatColor.DARK_RED + "[Bukkitmanager] " + ChatColor.WHITE;
 	public static String error = ChatColor.RED + "[Error] ";
 	public static String warning = ChatColor.YELLOW + "[Warning] ";
 	public static String debug = ChatColor.AQUA + "[Debug] ";
 	private static Translator translator;
 	private static boolean color = true;
 	
 	static {
 		plugin = WarpsReloaded.getInstance();
 		config = WarpsReloaded.getConfiguration();
 	}
 	
 	public void init() {
 		color = config.getBoolean("IO.ColoredLogs");
 		prefix = color(config.getString("IO.Prefix")) + " " + ChatColor.WHITE;
		error = color(config.getString("IO.Error")) + " " + ChatColor.WHITE;
		warning = color(config.getString("IO.Warning")) + " " + ChatColor.WHITE;
 		translator = new Translator();
 		translator.initialize();
 	}
 
 	public void broadcast(String msg) {
 		if (config.getBoolean("IO.Show-Prefix")) plugin.getServer().broadcastMessage(parseColor(prefix + msg));
 		else plugin.getServer().broadcastMessage(parseColor(msg));
 	}
 	public void broadcast(String msg, boolean showPrefix) {
 		if (showPrefix) plugin.getServer().broadcastMessage(parseColor(prefix + msg));
 		else plugin.getServer().broadcastMessage(parseColor(msg));
 	}
 	public void broadcast(String msg, String perm) {
 		if (config.getBoolean("IO.Show-Prefix")) plugin.getServer().broadcast(parseColor(prefix + msg), perm);
 		else plugin.getServer().broadcast(parseColor(msg), perm);
 	}
 	public void broadcast(String msg, String perm, boolean showPrefix) {
 		if (showPrefix) plugin.getServer().broadcast(parseColor(prefix + msg), perm);
 		else plugin.getServer().broadcast(parseColor(msg), perm);
 	}	
 	
 	public void sendConsole(String msg) {
 		plugin.getServer().getConsoleSender().sendMessage(color(prefix + msg));
 	}
 	public void sendConsoleWarning(String msg) {
 		plugin.getServer().getConsoleSender().sendMessage(color(prefix + warning + msg));
 	}
 	public void sendConsoleError(String msg) {
 		plugin.getServer().getConsoleSender().sendMessage(color(prefix + error + msg));
 	}
 	public void debug(String msg) {
 		plugin.getServer().getConsoleSender().sendMessage(color(prefix + debug + msg));
 	}
 	
 	public void send(CommandSender sender, String msg) {
 		if (config.getBoolean("IO.Show-Prefix")) sender.sendMessage(parseColor(prefix + msg));
 		else sender.sendMessage(parseColor(msg));
 	}
 	public void send(CommandSender sender, String msg, boolean showPrefix) {
 		if (showPrefix) sender.sendMessage(parseColor(prefix + msg));
 		else sender.sendMessage(parseColor(msg));
 	}
 
 	public void sendHeader(CommandSender sender, String title) {
 		sender.sendMessage(parseColor(formatHeader(title)));
 	}
 	
 	public void sendWarning(CommandSender sender, String msg) {
 		if (config.getBoolean("IO.Show-Prefix")) sender.sendMessage(parseColor(prefix + warning + msg));
 		else sender.sendMessage(parseColor(warning + msg));
 	}
 	public void sendWarning(CommandSender sender, String msg, boolean showPrefix) {
 		if (showPrefix) sender.sendMessage(parseColor(prefix + warning + ChatColor.YELLOW + msg));
 		else sender.sendMessage(parseColor(warning + msg));
 	}
 
 	public void sendError(CommandSender sender, String msg) {
 		if (config.getBoolean("IO.Show-Prefix")) sender.sendMessage(parseColor(prefix + error + msg));
 		else sender.sendMessage(parseColor(error + msg));
 	}
 	public void sendError(CommandSender sender, String msg, boolean showPrefix) {
 		if (showPrefix) sender.sendMessage(parseColor(prefix + error + msg));
 		else sender.sendMessage(parseColor(error + msg));
 	}
 	
 	public void sendFewArgs(CommandSender sender, String usage) {
 		if (config.getBoolean("IO.Show-Prefix")) {
 			sender.sendMessage(parseColor(prefix + translate("Command.FewArgs")));
 			sender.sendMessage(parseColor(prefix + translate("Command.Usage").replaceAll("%usage%", usage)));
 		}else {
 			sender.sendMessage(parseColor(translate("Command.FewArgs")));
 			sender.sendMessage(parseColor(translate("Command.Usage").replaceAll("%usage%", usage)));
 		}
 	}
 	public void sendFewArgs(CommandSender sender, String usage, boolean showPrefix) {
 		if (showPrefix) {
 			sender.sendMessage(parseColor(prefix + translate("Command.FewArgs")));
 			sender.sendMessage(parseColor(prefix + translate("Command.Usage").replaceAll("%usage%", usage)));
 		}else {
 			sender.sendMessage(parseColor(translate("Command.FewArgs")));
 			sender.sendMessage(parseColor(translate("Command.Usage").replaceAll("%usage%", usage)));
 		}
 	}
 
 	public void sendManyArgs(CommandSender sender, String usage) {
 		if (config.getBoolean("IO.Show-Prefix")) {
 			sender.sendMessage(parseColor(prefix + translate("Command.ManyArgs")));
 			sender.sendMessage(parseColor(prefix + translate("Command.Usage").replaceAll("%usage%", usage)));
 		}else {
 			sender.sendMessage(parseColor(translate("Command.ManyArgs")));
 			sender.sendMessage(parseColor(translate("Command.Usage").replaceAll("%usage%", usage)));
 		}
 	}	
 	public void sendManyArgs(CommandSender sender, String usage, boolean showPrefix) {
 		if (showPrefix) {
 			sender.sendMessage(parseColor(prefix + translate("Command.ManyArgs")));
 			sender.sendMessage(parseColor(prefix + translate("Command.Usage").replaceAll("%usage%", usage)));
 		}else {
 			sender.sendMessage(parseColor(translate("Command.ManyArgs")));
 			sender.sendMessage(parseColor(translate("Command.Usage").replaceAll("%usage%", usage)));
 		}
 	}
 	
 	public void sendConversable(Conversable conversable, String msg) {
 		if (config.getBoolean("IO.Show-Prefix")) conversable.sendRawMessage(parseColor(prefix + msg));
 		else conversable.sendRawMessage(parseColor(msg));
 	}
 	public void sendConversable(Conversable conversable, String msg, boolean showPrefix) {
 		if (showPrefix) conversable.sendRawMessage(parseColor(prefix + msg));
 		else conversable.sendRawMessage(parseColor(msg));
 	}
 
 	public String translate(String key) {
 		return translator.getKey(key);
 	}
 	
 	public Translator getTranslator() {
 		return translator;
 	}
 	
 	public String color(String msg) {
 		if (color) return parseColor(msg);
 		else return remColor(msg);
 	}
 	
 	public static String parseColor(String message) {
 		return ChatColor.translateAlternateColorCodes('&', message);
 	}
 	
 	public static String remColor(String message) {
 		return ChatColor.stripColor(message);
 	}	
 	
 	public static String formatHeader(String title) {
 		int length = (55 - title.length())/2-1;
 		String lines = "";
 		for (int i = 0; i < length; i++) lines += "-";
 		if (title.length() + 2*lines.length() != 53) title = "&e-" + lines + " &f" + title + " &e" + lines;
 		else title = "&e" + lines + " &f" + title + " &e" + lines;
 		return title;
 	}
 }
