 package at.Grevinelveck.SLFunctions;
 
 import org.bukkit.command.*;
 import org.bukkit.entity.Player;
 
 import at.Grevinelveck.Skylab.SkyLab;
 
 public class SkyLabCommandExecutor implements CommandExecutor {
 	static MSleepThread mST;
 	static LSleepThread lSt;
    
 	private SkyLab plugin;
  
 	public SkyLabCommandExecutor(SkyLab plugin) {
 		this.plugin = plugin;
 	}
  
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLable, String[]args){
 		Player player = (Player) sender;
 		if (commandLable.equalsIgnoreCase("SkyLab")){
 			if(args.length==0){
 				player.sendMessage("SkyLab requires a target to fire");
 			}else if (args.length==1){
 				if (player.getServer().getPlayer(args[0]) != null){			
 	mST=new MSleepThread();
 	lSt=new LSleepThread(player);
 	//Ban or kick if needed with appropriate message
 	//Power down message
 	return true;
 				}
 				
 		
 			
 			}
 	}
 		return false;
 	}
 }
