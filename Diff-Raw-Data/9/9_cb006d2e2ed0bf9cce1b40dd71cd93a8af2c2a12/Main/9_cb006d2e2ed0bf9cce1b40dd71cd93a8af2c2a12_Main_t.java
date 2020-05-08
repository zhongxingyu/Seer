 package com.cole2sworld.dragonlist;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Locale;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 /**
  * Main class for DragonList
  *
  */
 public final class Main extends JavaPlugin {
 	static Main instance;
 	public CommandHandler handler = new CommandHandler();
 	public Logger logger = Logger.getLogger("Minecraft");
 	public Main() {
 		super();
 		instance = this;
 	}
 	@Override
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(new DragonListener(), this);
 		WhitelistManager.initalize();
 		GlobalConf.loadConfig();
 		WhitelistManager.importVanillaWhitelist();
 	}
 	@Override
 	public void onDisable() {
 		for (Player player : RestrictionManager.frozen) {
 			RestrictionManager.thaw(player);
 		}
 		saveConfig();
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (args.length < 1) {
 			sender.sendMessage(ChatColor.RED+"Not enough arguments! Usage:");
 			sender.sendMessage(ChatColor.RED+"/"+label+" <subcommand> [arguments] [...]");
 			sender.sendMessage("For more help, go to "+ChatColor.AQUA+ChatColor.UNDERLINE+"http://c2wr.com/dlwk");
 		}
 		String sublabel = args[0];
		if (sublabel.equalsIgnoreCase("import")) sublabel = "importWhitelist"; //Java keywords are evil sometimes
 		String[] subargs = new String[args.length-1];
 		System.arraycopy(args, 1, subargs, 0, args.length-1);
 		try {
 			handler.getClass().getMethod(sublabel.toLowerCase(Locale.ENGLISH), CommandSender.class, String[].class, String.class).invoke(handler, sender, subargs, label);
 		} catch (IllegalArgumentException e) {
 			sender.sendMessage(ChatColor.RED+"Sub-command implemented wrong! Usage:");
 			sender.sendMessage(ChatColor.RED+"/"+label+" <subcommand> [arguments] [...]");
 			sender.sendMessage("For more help, go to "+ChatColor.AQUA+ChatColor.UNDERLINE+"http://c2wr.com/dlwk");
 		} catch (SecurityException e) {
 			sender.sendMessage(ChatColor.RED+"Plugin conflict! Usage:");
 			sender.sendMessage(ChatColor.RED+"/"+label+" <subcommand> [arguments] [...]");
 			sender.sendMessage("For more help, go to "+ChatColor.AQUA+ChatColor.UNDERLINE+"http://c2wr.com/dlwk");
 		} catch (IllegalAccessException e) {
 			sender.sendMessage(ChatColor.RED+"Poorly made sub-command! Usage:");
 			sender.sendMessage(ChatColor.RED+"/"+label+" <subcommand> [arguments] [...]");
 			sender.sendMessage("For more help, go to "+ChatColor.AQUA+ChatColor.UNDERLINE+"http://c2wr.com/dlwk");
 		} catch (InvocationTargetException e) {
 			sender.sendMessage(ChatColor.RED+"An error occurred while executing the command. See console.");
 			e.getCause().printStackTrace();
 		} catch (NoSuchMethodException e) {
 			sender.sendMessage(ChatColor.RED+"Invalid sub command! Usage:");
 			sender.sendMessage(ChatColor.RED+"/"+label+" <subcommand> [arguments] [...]");
 			sender.sendMessage("For more help, go to "+ChatColor.AQUA+ChatColor.UNDERLINE+"http://c2wr.com/dlwk");
 		}
 		return true;
 	}
 
 }
