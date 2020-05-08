 package com.theisleoffavalon.mcmanager.commands;
 
 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.player.EntityPlayer;
 
 import com.theisleoffavalon.mcmanager.MCManager;
 
 /**
  * Heals the current player or a targeted player to full health.
  * 
  * @author Cadyyan
  *
  */
 public class CommandHeal extends Command
 {
 	/**
 	 * Creates a heal command instance.
 	 */
 	public CommandHeal()
 	{
 		super("heal");
 	}
 
 	@Override
 	public String getCommandUsage(ICommandSender sender)
 	{
 		return "/" + name + "[player]";
 	}
 
 	@Override
 	public void processCommand(ICommandSender sender, String[] args)
 	{
 		String username = args.length < 1 ? null : args[0];
 		EntityPlayer player = username == null ? getCommandSenderAsPlayer(sender) :
 												 MCManager.instance.getServer().getConfigurationManager().getPlayerForUsername(username);
 		
 		player.heal(player.getMaxHealth() * 2);
 		notifyAdmins(sender, "Healing " + player.username);
 	}
 }
