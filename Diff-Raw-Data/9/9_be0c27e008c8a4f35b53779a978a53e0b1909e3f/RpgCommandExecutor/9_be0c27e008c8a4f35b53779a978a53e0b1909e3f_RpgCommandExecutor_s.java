 package me.Guga.Guga_SERVER_MOD.RPG;
 
 import me.Guga.Guga_SERVER_MOD.Guga_SERVER_MOD;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class RpgCommandExecutor implements CommandExecutor
 {
	public RpgCommandExecutor()
	{}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) 
 	{
 		if(sender instanceof Player)
 		{
 			if(command.getName().equalsIgnoreCase("rpg"))
 			{
				Guga_SERVER_MOD plugin = Guga_SERVER_MOD.getInstance();
 				GugaProfession2 prof;
 				if ((prof = plugin.userManager.getUser(sender.getName()).getProfession()) != null)
 				{
 					int lvl = prof.GetLevel();
 					int xp = prof.GetXp();
 					int xpNeeded = prof.GetXpNeeded();
 					sender.sendMessage(ChatColor.YELLOW + "**************************");
 					sender.sendMessage("*Guga RPG version 1 stats*");
 					sender.sendMessage("**Level:" + lvl);
 					sender.sendMessage("**XP:" + xp + "/" + xpNeeded);
 					sender.sendMessage(ChatColor.YELLOW + "**************************");
 					int chance[] = prof.GetChances();
 					int iron = chance[0];
 					int gold = chance[1];
 					int diamond = chance[2];
 					int emerald = chance[3];
 					double chanceIron = ( (double)iron / (double)1000 )  * (double)100;
 					double chanceGold = ( (double)gold / (double)1000 )  * (double)100;
 					double chanceDiamond = ( (double)diamond/ (double)1000 )  * (double)100;
 					double chanceEmerald = ( (double)emerald/ (double)1000 )  * (double)100;
 					sender.sendMessage(ChatColor.GOLD + "***Sance na nalezeni ve stonu***");
 					sender.sendMessage(ChatColor.GRAY + "ZELEZO: " + ChatColor.WHITE + chanceIron + "% (+0.1% kazdych 10 levelu)");
 					sender.sendMessage(ChatColor.GOLD + "ZLATO: " + ChatColor.WHITE  + chanceGold + "% (+0.1% kazdych 20 levelu)");
 					sender.sendMessage(ChatColor.AQUA + "DIAMANT: " + ChatColor.WHITE + chanceDiamond + "% (+0.1% kazdych 50 levelu)");
 					sender.sendMessage(ChatColor.GREEN + "EMERALD: " + ChatColor.WHITE + chanceEmerald + "% (+0.1% kazdych 100 levelu)");
 					sender.sendMessage(ChatColor.YELLOW + "**************************");
 				}
 				else 
 				{
 					plugin.log.warning("Cannot get profession for player "+sender.getName()+".");
 				}
 			}
 		}
 		return false;
 	}
 }
