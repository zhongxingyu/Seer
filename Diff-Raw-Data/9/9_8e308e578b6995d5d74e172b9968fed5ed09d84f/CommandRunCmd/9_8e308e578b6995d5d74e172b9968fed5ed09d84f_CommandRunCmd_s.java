 package com.oneofthesevenbillion.ziah.ZCord.command;
 
 import java.util.List;
 
 import com.oneofthesevenbillion.ziah.ZCord.utils.ArrayUtils;
 import com.oneofthesevenbillion.ziah.ZCord.utils.Utils;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.protocol.packet.Packet3Chat;
 
 public class CommandRunCmd extends Command {
 	public CommandRunCmd() {
 		super("runcmd", "Forces another player to run the specifed command.", "zcord.command.runcmd");
 	}
 
 	@Override
 	public void execute(CommandSender sender, String[] args) {
 		if (args.length >= 2) {
 			List<ProxiedPlayer> candidates = Utils.getPlayersFromInputName(args[0]);
			if (candidates.size() > 0) {
 				sender.sendMessage(ChatColor.RED + "Too many players" + ChatColor.GRAY + ":");
 				for (ProxiedPlayer player : candidates) {
 					sender.sendMessage(player.getName());
 				}
 			}else{
 				ProxiedPlayer player = candidates.get(0);
 				if (player != null) {
 					String command = ArrayUtils.join(args, " ");
 					command = command.substring(command.indexOf(" ") + 1, command.length());
 					sender.sendMessage(ChatColor.GOLD + "Forcing " + ChatColor.RED + player.getDisplayName() + ChatColor.GOLD + " to run the command " + ChatColor.RED + command + ChatColor.GOLD + "...");
 					player.getServer().unsafe().sendPacket(new Packet3Chat(command));
 				}else{
 					sender.sendMessage(ChatColor.RED + "Player not found.");
 				}
 			}
 		}else{
 			sender.sendMessage(ChatColor.RED + "Invalid arguments.");
 		}
 	}
 }
