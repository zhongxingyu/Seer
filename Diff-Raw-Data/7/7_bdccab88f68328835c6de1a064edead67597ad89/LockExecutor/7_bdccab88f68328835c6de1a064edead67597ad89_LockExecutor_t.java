 package uk.co.jacekk.bukkit.grouplock.commands;
 
 import net.minecraft.server.TileEntity;
 import net.minecraft.server.World;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Player;
 
 import uk.co.jacekk.bukkit.baseplugin.v5.command.BaseCommandExecutor;
 import uk.co.jacekk.bukkit.baseplugin.v5.command.CommandHandler;
 import uk.co.jacekk.bukkit.grouplock.Config;
 import uk.co.jacekk.bukkit.grouplock.GroupLock;
 import uk.co.jacekk.bukkit.grouplock.Permission;
 import uk.co.jacekk.bukkit.grouplock.nms.TileEntityLockable;
 
 public class LockExecutor extends BaseCommandExecutor<GroupLock> {
 	
 	public LockExecutor(GroupLock plugin){
 		super(plugin);
 	}
 	
 	@CommandHandler(names = {"lock", "l"}, description = "Lock or unlock a block", usage = "[<add/remove> <player_name>]")
 	public void lock(CommandSender sender, String label, String[] args){
 		if (!(sender instanceof Player)){
 			sender.sendMessage(plugin.formatMessage(ChatColor.RED + "The /lock command can only be used in game"));
 			return;
 		}
 		
 		if (!Permission.LOCK.has(sender)){
 			sender.sendMessage(plugin.formatMessage(ChatColor.RED + "You do not have permission to use this command"));
 			return;
 		}
 		
 		Player player = (Player) sender;
 		String playerName = player.getName();
 		
 		if (plugin.config.getStringList(Config.IGNORE_WORLDS).contains(player.getWorld().getName())){
 			player.sendMessage(plugin.formatMessage(ChatColor.RED + "You cannot lock blocks in this world"));
 			return;
 		}
 		
 		Block block = player.getTargetBlock(null, 20);
 		Material type = block.getType();
 		
 		World world = ((CraftWorld) player.getWorld()).getHandle();
 		TileEntity tileEntity = world.getTileEntity(block.getX(), block.getY(), block.getZ());
 		
 		String blockName = type.name().toLowerCase().replace('_', ' ');
 		String ucfBlockName = Character.toUpperCase(blockName.charAt(0)) + blockName.substring(1);
 		
		if (tileEntity == null || !(tileEntity instanceof TileEntityLockable)){
			player.sendMessage(plugin.formatMessage(ChatColor.RED + "A" + blockName + " is not a lockable block"));
 			return;
 		}
 		
		// TODO: get adjacent blocks
		
 		TileEntityLockable lockable = (TileEntityLockable) tileEntity;
 		
 		if (!lockable.hasOwnerName()){
 			lockable.setOwnerName(playerName);
 			player.sendMessage(plugin.formatMessage(ChatColor.GREEN + ucfBlockName + " locked"));
 		}else{
 			if (!Permission.UNLOCK_LOCKED.has(player) && !lockable.canModify(playerName)){
 				player.sendMessage(plugin.formatMessage(ChatColor.RED + "That " + blockName + " is locked by " + lockable.getOwnerName()));
 				return;
 			}
 			
 			if (args.length == 2){
 				if (args[0].equalsIgnoreCase("add")){
 					lockable.addAllowedPlayer(args[1]);
 					player.sendMessage(plugin.formatMessage(ChatColor.GREEN + args[1] + " has been added to the access list"));
 				}else{
 					lockable.removeAllowedPlayer(args[1]);
 					player.sendMessage(plugin.formatMessage(ChatColor.GREEN + args[1] + " has been removed from the access list"));
 				}
 			}else{
 				lockable.reset();
 				player.sendMessage(plugin.formatMessage(ChatColor.GREEN + ucfBlockName + " unlocked"));
 			}
 		}
 	}
 	
 }
