 /*
  *  Copyright:
  *  2013 Darius Mewes
  */
 
 package de.dariusmewes.TimoliaCore.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import de.dariusmewes.TimoliaCore.Message;
 
 public class exe extends TCommand {
 
 	public exe(String name) {
 		super(name);
 		setMinArgs(2);
 		setUsage("/exe <Spieler> <Befehl>");
 		setDesc("Execute a command with all permissions for someone else");
 	}
 
 	public void perform(CommandSender sender, String[] args) {
 		Player target = Bukkit.getPlayer(args[0]);
 		if (target == null) {
 			sender.sendMessage(_("notonline"));
 			return;
 		}
 
 		if (target.hasPermission("tcore.admin") || target.isOp()) {
 			sender.sendMessage(_("exeOP"));
 			return;
 		}
 
 		String targetcmd = "";
 		for (int i = 1; i < args.length; i++)
 			targetcmd += args[i] + " ";
 
 		if (targetcmd.charAt(0) != '/')
 			targetcmd = "/" + targetcmd;
 
 		boolean wasOP = target.isOp();
 
 		if (!wasOP)
 			target.setOp(true);
 
 		target.chat(targetcmd);
 
 		if (!wasOP)
 			target.setOp(false);
 
		Message.console(sender.getName() + " made " + target.getName() + " execute\"" + targetcmd + "\"");
 	}
 
 }
