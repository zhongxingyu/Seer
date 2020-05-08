 package net.lotrcraft.minepermit;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 
 public class CoreCommandInterpreter implements CommandExecutor {
 
 	private MinePermit mp;
 
 	public CoreCommandInterpreter(MinePermit minePermit) {
 		this.mp = minePermit;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		
 		if(args.length < 1)
 			return false;
 		
 		if(args[0].equalsIgnoreCase("init")){
 			
 			if(args.length < 2)
 				return false;
 			
 			World w = mp.getServer().getWorld(args[1]);
 			
 			if(w == null){
 				
 				if(sender instanceof ConsoleCommandSender)
 					mp.log.info("Invalid world!");
 				else 
 					sender.sendMessage(ChatColor.RED + "Invalid World!");
 			}
 			
 			if(mp.getPWM().getPermitWorld(w) != null){
 				
 				if(sender instanceof ConsoleCommandSender)
 					mp.log.info("World already initialized!");
 				else 
 					sender.sendMessage(ChatColor.RED + "World already initialized!");
 			}
 			
 			if(mp.initWorld(w)){
 				
 				if(sender instanceof ConsoleCommandSender)
 					mp.log.info("World initialized!");
 				else 
 					sender.sendMessage(ChatColor.GREEN + "World initialized!");
 			} else {
 				
 				if(sender instanceof ConsoleCommandSender)
 					mp.log.info("Couldn't initialize world!");
 				else 
 					sender.sendMessage(ChatColor.RED + "Couldn't initialize world!");
 			}
 			
 			
 		}
 		
 		return false;
 	}
 
 }
