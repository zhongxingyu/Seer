 package anotherDEVer.Noisemaker;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.*;
 import org.bukkit.entity.Player;
 import org.bukkit.Sound;
 
 public final class Noisemaker extends JavaPlugin
 {
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[])
 	{
 		if (cmd.getName().equalsIgnoreCase("moo"))
 		{
 			if (sender instanceof Player)
 			{
				if (args[0] == null)
 				{
 					Player player = (Player) sender;
 					player.playSound(player.getLocation(), Sound.COW_IDLE, 1f, 1f);
 				}
 				
 				else
 				{
 					try
 					{
 						Player mooer = (Player) sender;
 						Player mooee = mooer.getServer().getPlayer(args[0]);
 						
 						mooee.playSound(mooee.getLocation(), Sound.COW_IDLE, 1f, 1f);
 					}
 					catch (Exception e)
 					{
 						sender.sendMessage("Player is not on this server.");
 					}
 				}
 			}
 			
 			else
 			{
 				if (args[0] == null)
 				{
 					sender.sendMessage("You must specify a player when using this command from the server.");
 				}
 				
 				else
 				{
 					try
 					{
 						Player target = sender.getServer().getPlayer(args[0]);
 					
 						target.playSound(target.getLocation(), Sound.COW_IDLE, 1f, 1f);
 					}
 					catch (Exception e)
 					{
 						sender.sendMessage("You must select an online player.");
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
