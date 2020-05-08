 package com.mccraftaholics.warpportals.bukkit;
 
 import java.lang.reflect.Method;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.mccraftaholics.warpportals.commands.CmdBackup;
 import com.mccraftaholics.warpportals.commands.CmdDestCreate;
 import com.mccraftaholics.warpportals.commands.CmdDestDelete;
 import com.mccraftaholics.warpportals.commands.CmdDestList;
 import com.mccraftaholics.warpportals.commands.CmdDestTeleport;
 import com.mccraftaholics.warpportals.commands.CmdHelp;
 import com.mccraftaholics.warpportals.commands.CmdLoad;
 import com.mccraftaholics.warpportals.commands.CmdPortalCreate;
 import com.mccraftaholics.warpportals.commands.CmdPortalDelTool;
 import com.mccraftaholics.warpportals.commands.CmdPortalDelete;
 import com.mccraftaholics.warpportals.commands.CmdPortalList;
 import com.mccraftaholics.warpportals.commands.CmdPortalTeleport;
 import com.mccraftaholics.warpportals.commands.CmdSave;
 import com.mccraftaholics.warpportals.helpers.Defaults;
 import com.mccraftaholics.warpportals.manager.PortalManager;
 
 public class CommandHandler {
 	public PortalPlugin mPortalPlugin;
 	public PortalManager mPortalManager;
 	public YamlConfiguration mPortalConfig;
 	public ChatColor mCC;
 
 	public CommandHandler(PortalPlugin mainPlugin, PortalManager portalManager, YamlConfiguration portalConfig) {
 		mPortalPlugin = mainPlugin;
 		mPortalManager = portalManager;
 		mPortalConfig = portalConfig;
 
 		mCC = ChatColor.getByChar(mPortalConfig.getString("portals.general.textColor", Defaults.CHAT_COLOR));
 	}
 
 	public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
 		// Wrap the command argument for easy filtering
 		WPCommand cmd = new WPCommand(command);
 
 		// For commands that require the sender to be a player
 		if (cmd.test("wp-portal-create", "wppc", "pcreate")) {
 			return requirePlayer(sender, args, this, CmdPortalCreate.class);
 		} else if (cmd.test("wp-portal-deltool", "wppdt", "pdeltool")) {
 			return requirePlayer(sender, args, this, CmdPortalDelTool.class);
 		} else if (cmd.test("wp-portal-teleport", "wp-portal-tp", "wpptp")) {
 			return requirePlayer(sender, args, this, CmdPortalTeleport.class);
 		} else if (cmd.test("wp-destination-create", "wpdc")) {
 			return requirePlayer(sender, args, this, CmdDestCreate.class);
 		} else if (cmd.test("wp-destination-teleport", "wp-destination-tp", "wp-dest-teleport", "wp-dest-tp", "wpdtp")) {
 			return requirePlayer(sender, args, this, CmdDestTeleport.class);
 		}
 		// For commands that a server or player can run
 		else if (cmd.test("wp", "wp-help", "phelp"))
 			return CmdHelp.handle(sender, args, this);
 		else if (cmd.test("wp-portal-delete", "wppd", "pdelete"))
 			return CmdPortalDelete.handle(sender, args, this);
 		else if (cmd.test("wp-portal-list", "wppl", "plist"))
 			return CmdPortalList.handle(sender, args, this);
 		else if (cmd.test("wp-destination-delete", "wp-dest-delete", "wpdd", "pdestdel"))
 			return CmdDestDelete.handle(sender, args, this);
 		else if (cmd.test("wp-destination-list", "wp-dest-list", "wp-dests", "wpdl", "pdestlist"))
 			return CmdDestList.handle(sender, args, this);
 		else if (cmd.test("wp-save", "wps", "psave"))
 			return CmdSave.handle(sender, args, this);
 		else if (cmd.test("wp-load", "wpl", "pload"))
 			return CmdLoad.handle(sender, args, this);
 		else if (cmd.test("wp-backup", "wpb", "pbackup"))
 			return CmdBackup.handle(sender, args, this);
 
 		return false;
 	}
 
 	public static class CommandHandlerObject {
 		public static boolean handle(CommandSender sender, String[] args, CommandHandler main) {
 			return false;
 		}

		public static boolean handle(Player sender, String[] args, CommandHandler main) {
			return false;
		}
 	}
 
 	boolean requirePlayer(CommandSender sender, String[] args, CommandHandler main, Class<? extends CommandHandlerObject> cho) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("This command must be run from an active player");
 			return true;
 		} else {
 			try {
				Method m = cho.getMethod("handle", Player.class, String[].class, CommandHandler.class);
 				return (Boolean) m.invoke(null, sender, args, main);
 			} catch (Exception e) {
 				sender.sendMessage("THIS ERROR SHOULD NEVER OCCUR");
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	static class WPCommand {
 		String command;
 
 		WPCommand(Command bukkitCommand) {
 			command = bukkitCommand.getName().toLowerCase();
 		}
 
 		boolean test(String... strings) {
 			for (String s : strings) {
 				if (command.equals(s))
 					return true;
 			}
 			return false;
 		}
 	}
 }
