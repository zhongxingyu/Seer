 package btwmod.spawncustomizer;
 
 import java.io.IOException;
 import java.util.List;
 
 import btwmods.ModLoader;
 import btwmods.Util;
 import btwmods.player.SpawnPosition;
 import net.minecraft.src.CommandBase;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.ICommandSender;
 import net.minecraft.src.MathHelper;
 import net.minecraft.src.NumberInvalidException;
 import net.minecraft.src.WrongUsageException;
 
 public class CommandSpawn extends CommandBase {
 	
 	private final mod_SpawnCustomizer mod;
 
 	public CommandSpawn(mod_SpawnCustomizer mod) {
 		this.mod = mod;
 	}
 
 	@Override
 	public String getCommandName() {
 		return "spawn";
 	}
 
 	@Override
 	public String getCommandUsage(ICommandSender sender) {
		return "/" + getCommandName() + " <loc|global> ...";
 	}
 
 	@Override
 	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
 		if (args.length == 1)
 			return getListOfStringsMatchingLastWord(args, new String[] { "loc", "global" });
 		
 		return super.addTabCompletionOptions(sender, args);
 	}
 
 	@Override
 	public void processCommand(ICommandSender sender, String[] args) {
 		if (args.length == 1 && args[0].equalsIgnoreCase("loc") && sender instanceof EntityPlayer) {
 			EntityPlayer player = (EntityPlayer)sender;
 			sender.sendChatToPlayer("You are at " + MathHelper.floor_double(player.posX) + "," + MathHelper.floor_double(player.posY) + "," + MathHelper.floor_double(player.posZ)
					+ " with pitch " + Util.DECIMAL_FORMAT_2.format(player.rotationPitch) + " and yaw " + Util.DECIMAL_FORMAT_2.format(player.rotationYaw));
 		}
 		else if (args.length == 1 && args[0].equalsIgnoreCase("global")) {
 			if (mod.getGlobalSpawn() == null) {
 				sender.sendChatToPlayer("A global spawn is not set.");
 			}
 			else {
 				sender.sendChatToPlayer("The global spawn is set to " + mod.getGlobalSpawn().x + "," + mod.getGlobalSpawn().y + "," + mod.getGlobalSpawn().z
 						+ (mod.getGlobalSpawn().yaw == 0.0F ? "" : " yaw " + Util.DECIMAL_FORMAT_2.format(mod.getGlobalSpawn().yaw))
 						+ (mod.getGlobalSpawn().pitch == 0.0F ? "" : " pitch " + Util.DECIMAL_FORMAT_2.format(mod.getGlobalSpawn().pitch)));
 			}
 		}
 		else if ((args.length == 4 || args.length == 6) && args[0].equalsIgnoreCase("global")) {
 			int x = parseInt(sender, args[1]);
 			int y = parseInt(sender, args[2]);
 			int z = parseInt(sender, args[3]);
 			
 			float yaw = 0.0F;
 			float pitch = 0.0F;
 			
 			if (args.length == 6) {
 				try {
 					yaw = Float.parseFloat(args[4]);
 				}
 				catch (NumberFormatException e) {
 					throw new NumberInvalidException("commands.generic.num.invalid", new Object[] { args[4] });
 				}
 				try {
 					pitch = Float.parseFloat(args[5]);
 				}
 				catch (NumberFormatException e) {
 					throw new NumberInvalidException("commands.generic.num.invalid", new Object[] { args[5] });
 				}
 			}
 			
 			try {
 				mod.setGlobalSpawn(new SpawnPosition(x, y, z, yaw, pitch));
 				
 			} catch (IOException e) {
 				sender.sendChatToPlayer("Warning: Failed to save global spawn setting to disk.");
 				ModLoader.outputError(e, mod.getName() + "'s " + getCommandName() + " command failed to save a global spawn position: " + e.getMessage());
 			}
 			
 			sender.sendChatToPlayer("The global spawn is set to " + mod.getGlobalSpawn().x + "," + mod.getGlobalSpawn().y + "," + mod.getGlobalSpawn().z
 					+ (mod.getGlobalSpawn().yaw == 0.0F ? "" : " yaw " + Util.DECIMAL_FORMAT_2.format(mod.getGlobalSpawn().yaw))
 					+ (mod.getGlobalSpawn().pitch == 0.0F ? "" : " pitch " + Util.DECIMAL_FORMAT_2.format(mod.getGlobalSpawn().pitch)));
 		}
 		else {
 			throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
 		}
 	}
 
 }
