 package org.monstercraft.support.plugin.command.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.monstercraft.support.MonsterTickets;
 import org.monstercraft.support.plugin.command.GameCommand;
 
 public class Adminchat extends GameCommand {
 
 	private static MonsterTickets instance;
 
 	public Adminchat(MonsterTickets instance) {
 		Adminchat.instance = instance;
 	}
 
 	@Override
 	public boolean canExecute(CommandSender sender, String[] split) {
 		return split.length > 1
				&& (split[0].equalsIgnoreCase("adminchat") || split[0]
 						.equalsIgnoreCase("ac"));
 	}
 
 	@Override
 	public boolean execute(CommandSender sender, String[] split) {
		if (split.length == 1) {
 			sender.sendMessage(ChatColor.GREEN
 					+ "You must write a message to send!.");
 			return true;
 		}
		String[] temp = new String[split.length - 1];
		int i = 0;
		for (String s : split) {
			if (i == 0) {
				continue;
			}
			temp[i] = s;
			i++;
		}
		split = temp;
 		StringBuilder sb = new StringBuilder();
 		for (String s : split) {
 			sb.append(s);
 			sb.append(" ");
 		}
 		for (Player p : Bukkit.getOnlinePlayers()) {
 			if (instance.getPermissionsHandler().hasNode(p,
 					"monstertickets.adminchat")) {
 				p.sendMessage(ChatColor.RED + "[Admin Chat]" + sender.getName()
 						+ ": " + ChatColor.RESET + sb.toString().trim());
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public String getPermission() {
 		return "monstertickets.adminchat";
 	}
 }
