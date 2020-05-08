 package forgetools.common;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.CommandBase;
 import net.minecraft.src.EntityChicken;
 import net.minecraft.src.EntityLightningBolt;
 import net.minecraft.src.EntityPlayerMP;
 import net.minecraft.src.ICommandSender;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.ServerConfigurationManager;
 import net.minecraft.src.WorldServer;
 import net.minecraft.src.WrongUsageException;
 
 public class HealCommand extends CommandBase{
 
 	@Override
 	public String getCommandName() {
 		return "heal";
 	}
 	
 	@Override
 	public String getCommandUsage(ICommandSender par1ICommandSender)
     {
    	return "/heal [hp | hunger | username] [amount] [username]";
     }
 
 	@Override
 	public void processCommand(ICommandSender sender, String[] args) {
 		if(!FMLCommonHandler.instance().getEffectiveSide().isServer()) return;
 		
 		ServerConfigurationManager serverConfig = ModLoader.getMinecraftServerInstance().getConfigurationManager();
 		MinecraftServer server = ForgeTools.server;
 		
 		EntityPlayerMP player = getCommandSenderAsPlayer(sender);
 		if (!player.username.equalsIgnoreCase("Server") && !serverConfig.getOps().contains(player.username.trim().toLowerCase()))
 		{
 			sender.sendChatToPlayer("\u00a74You do not have permission to use the /heal command.");
 			return;
 		}
 		
 		boolean full = false, hp = false, food = false;
 		
 		// Only accept the command with one or three arguments
 		if (args.length == 1)	full = true;
 		else if (args.length == 3) { 
 			if (args[0].equals("hp"))
 				hp = true;
 			else if (args[0].equals("hunger"))
 				food = true;
 			else
 				throw new WrongUsageException(getCommandUsage(sender));
 		}
 		else	throw new WrongUsageException(getCommandUsage(sender));
 		
 		String user = "";
 		int amt = 20;
 		if (full)
 			user = args[0];
 		else {
 			user = args[2];
 			double temp = Double.parseDouble(args[1]);
 			amt = (int) (temp * 2);
 		}
 		
 		if (amt < 0) {
 			sender.sendChatToPlayer("\u00a7cYou cannot heal for a negative amount");
 			return;
 		}
 		
 		
 		String players[] = serverConfig.getAllUsernames();	// Get an array of all usernames
 		boolean found = false;
 		for (String s: players) {							// Search for the targeted username
 			if (s.equals(user))
 				 found = true;
 		}
 		
 		if (found) {
 			EntityPlayerMP target = serverConfig.getPlayerForUsername(user);
 			
 			if (full) {
 				sender.sendChatToPlayer("\u00a7aHealing " + user + "'s HP and hunger to full");
 				target.heal(20);
				target.getFoodStats().addStats(20, 20);
 				target.sendChatToPlayer("\u00a7a" + player.username + " has healed your HP and hunger to full");
 			} else {
 				sender.sendChatToPlayer("\u00a7aHealing " + user + "'s " + ((hp == true) ? "hp" : "hunger") + " by " + amt / 2);
 				if (hp) {
 					target.heal(amt);
 					
 					target.sendChatToPlayer("\u00a7a" + player.username + " has healed your HP by " + amt / 2 + " hearts");
 				} else {
					target.getFoodStats().addStats(amt, 20);
 					
 					target.sendChatToPlayer("\u00a7a" + player.username + " has healed your hunger by " + amt / 2 + " drumsticks");
 				}
 			}
 		} else
 			sender.sendChatToPlayer("\u00a7c" + user + " cannot be found");
 	}
 
 }
