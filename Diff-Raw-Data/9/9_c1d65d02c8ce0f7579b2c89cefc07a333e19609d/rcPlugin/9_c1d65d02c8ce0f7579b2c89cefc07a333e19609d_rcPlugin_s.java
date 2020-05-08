 package rc.rc.ThaPear.RemoteChests;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.InventoryLargeChest;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import rc.rc.ThaPear.RemoteChests.rcBlockListener;
 import rc.rc.ThaPear.RemoteChests.rcPlayerListener;
 import rc.rc.ThaPear.RemoteChests.rcChest;
 import rc.rc.ThaPear.RemoteChests.rcFile;
 // Permissions
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.plugin.Plugin;
 
 
 public class rcPlugin extends JavaPlugin
 {
	static String rclinkopenPerm = "rc.linkedopen";
 	private static String rcopenPerm = "rc.open";
 	private static String rccreatePerm = "rc.create";
 	private static String rcremovePerm = "rc.remove";
 	private static String rcrenamePerm = "rc.rename";
 	private static String rcsortPerm = "rc.sort";
 	private static String rcstackPerm = "rc.stack";
 	private static String rcmergePerm = "rc.merge";
 	private static String rclistPerm = "rc.list";
 	static boolean permissionsOn = false;
 	public static final Logger log = Logger.getLogger("Minecraft");
 	public static PermissionHandler Permissions;
 	
 	public static String messagePrefix = "[RemoteChests] ";
 	@SuppressWarnings("unused") // Ignore the warning about this variable not being used, as it is used when debugging is uncommented.
 	private static String debugPrefix = "[RemoteChests][Debug] ";
 	private static HashMap<String, InventoryLargeChest> chests = new HashMap<String, InventoryLargeChest>();
 	public static ArrayList<Sign> signs = new ArrayList<Sign>();
 
 	/**
 	 * Handles all player related events.
 	 */
 	private final rcPlayerListener playerListener = new rcPlayerListener(this);
 	/**
 	 * Handles all block related events.
 	 */
 	private final rcBlockListener blockListener = new rcBlockListener(this);
 	/**
 	 * Handles all world related events.
 	 */
 	private final rcWorldListener worldListener = new rcWorldListener(this);
 	/**
 	 * Handles all file interaction.
 	 */
 	private final rcFile fileIO = new rcFile(this);
 
 	public rcPlugin()
 	{
 		super();
 	}
 
 	/**
 	 * Gets called when the plugin is enabled
 	 * (so server start/reload)
 	 */
 	@Override
 	public void onEnable()
 	{
 		// Register our events
 		PluginManager pm = getServer().getPluginManager();
 		//pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.WORLD_SAVE, worldListener, Event.Priority.Normal, this);
 	
 		chests = fileIO.loadChests();
 		
 		// Permissions support
 		setupPermissions();
 		
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println( messagePrefix + pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
 	}
 	/**
 	 * Gets called when the plugin is disabled
 	 * (so server stop/reload)
 	 */
 	@Override
 	public void onDisable()
 	{
 		// Save the chests
 		fileIO.saveChests(chests);
 
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println( messagePrefix + pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!" );
 	}
 	/**
 	 * Save the chests without outputting anything to the console.
 	 */
 	public void saveChests()
 	{
 		fileIO.saveChests(chests, true);
 	}
 	/**
 	 * Processes all commands sent from in-game
 	 * New commands need to be added to plugin.yml as well
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		if(!(sender instanceof Player))
 		{
 			sender.sendMessage( messagePrefix + ChatColor.RED + "Only players can use this command!");
 			return true;
 		}
 		Player player = (Player)sender;
 
 		if ( cmd.getName().equalsIgnoreCase("rcopen") || cmd.getName().equalsIgnoreCase("rcopenchest") )
 		{
 			if(permissionsOn && !rcPlugin.Permissions.has(player, rcopenPerm))
 				player.sendMessage(messagePrefix + ChatColor.RED + "You do not have permission to use " + cmd.getName() + ".");
 			else
 				openChest(player, args[0]);
 			return true;
 		}
 		else if ( cmd.getName().equalsIgnoreCase("rccreate") || cmd.getName().equalsIgnoreCase("rccreatechest") )
 		{
 			if(permissionsOn && !rcPlugin.Permissions.has(player, rccreatePerm))
 				player.sendMessage(messagePrefix + ChatColor.RED + "You do not have permission to use " + cmd.getName() + ".");
 			else
 				createChest(player, args[0]);
 			return true;
 		}
 		else if ( cmd.getName().equalsIgnoreCase("rcremove") )
 		{
 			if(permissionsOn && !rcPlugin.Permissions.has(player, rcremovePerm))
 				player.sendMessage(messagePrefix + ChatColor.RED + "You do not have permission to use " + cmd.getName() + ".");
 			else
 				removeChest(player, args[0]);
 			return true;
 		}
 		else if ( cmd.getName().equalsIgnoreCase("rcmerge") )
 		{
 			if(args[0].contains("?"))
 				dispMergeHelp(player);
 			else
 				if(permissionsOn && !rcPlugin.Permissions.has(player, rcmergePerm))
 					player.sendMessage(messagePrefix + ChatColor.RED + "You do not have permission to use " + cmd.getName() + ".");
 				else
 					mergeChests(player, args);
 			return true;
 		}
 		else if ( cmd.getName().equalsIgnoreCase("rcrename") )
 		{
 			if(permissionsOn && !rcPlugin.Permissions.has(player, rcrenamePerm))
 				player.sendMessage(messagePrefix + ChatColor.RED + "You do not have permission to use " + cmd.getName() + ".");
 			else 
 				renameChest(player, args);
 			return true;
 		}
 		else if ( cmd.getName().equalsIgnoreCase("rcstack") )
 		{
 			if(permissionsOn && !rcPlugin.Permissions.has(player, rcstackPerm))
 				player.sendMessage(messagePrefix + ChatColor.RED + "You do not have permission to use " + cmd.getName() + ".");
 			else if(stackChest(player, args[0]))
 				player.sendMessage(messagePrefix + ChatColor.GREEN + "Chest \"" + args[0] + "\" stacked succesfully.");
 			return true;
 		}
 		else if ( cmd.getName().equalsIgnoreCase("rcsort") )
 		{
 			if(permissionsOn && !rcPlugin.Permissions.has(player, rcsortPerm))
 				player.sendMessage(messagePrefix + ChatColor.RED + "You do not have permission to use " + cmd.getName() + ".");
 			else if(sortChest(player, args[0]))
 				player.sendMessage(messagePrefix + ChatColor.GREEN + "Chest \"" + args[0] + "\" sorted succesfully.");
 			return true;
 		}
 		else if ( cmd.getName().equalsIgnoreCase("rclist") )
 		{
 			if(permissionsOn && !rcPlugin.Permissions.has(player, rclistPerm))
 				player.sendMessage(messagePrefix + ChatColor.RED + "You do not have permission to use " + cmd.getName() + ".");
 			else
 				listChests(player, args);
 			return true;
 		}
 		else if ( cmd.getName().equalsIgnoreCase("rchelp"))
 		{
 			player.sendMessage(ChatColor.YELLOW + messagePrefix + "---------------------------------------");
 			
 			player.sendMessage(ChatColor.GREEN + "Possible commands:"); // the - all end up at the same location
 			player.sendMessage("/rcopen <chestname> - Open a chest remotely");
 			player.sendMessage("/rccreate <chestname> - Create a virtual chest manually");
 			player.sendMessage("/rcremove <chest1name> - Remove specified chest");
 			player.sendMessage("/rcrename <oldname> <newname> - Rename specified chest");
 			player.sendMessage("/rcstack <chest1name> - Stack the contents of a chest");
 			player.sendMessage("/rcsort <chest1name> - Sort the contents of a chest");
 			player.sendMessage("/rclist - List all existing chests");
 			player.sendMessage("/rcmerge <chest1name> <chest2name> [<newname>] [<flags>]");
 			player.sendMessage("Merge 2 chests, Type /rcmerge ? for more info");
 			
 			player.sendMessage(ChatColor.GREEN + "Chest linking:");
 			player.sendMessage("Place a sign above a chest to link it to a virtual chest");
 			player.sendMessage("Specify its target with [rc] <chestname> on any of the lines");
 			player.sendMessage("Or with [rc] on any of the lines, and <chestname> on the next");
 			player.sendMessage("If the chest doesn't exist, it gets auto-created");
 			
 			player.sendMessage(ChatColor.YELLOW + messagePrefix + "---------------------------------------");
 			return true;
 		}
 		return false;
 	}
 	private void listChests(Player player, String[] args)
 	{
 		int page = 0;
 		if(args.length > 0)
 			page = Integer.parseInt(args[0])-1; // 0 based in code, 1 based for users
 		int numChests = chests.keySet().size();
 		int startPos = 0;
 		if(numChests > 18)
 			startPos = Math.min(numChests - 18, page*18);
 		int curPos = 0;
 		
 		player.sendMessage(messagePrefix + " Listing " + Math.min(18, numChests) + " out of " + numChests + " chests");
 		
 		for(String name : chests.keySet())
 		{
 			curPos++;
 			if(curPos <= startPos)
 			{
 				continue;
 			}
 			player.sendMessage(name);
 			if(curPos >= startPos + 18)
 			{
 				if(numChests > 18)
 					player.sendMessage(messagePrefix + " And " + (numChests-18) + " more, specify page nr to see more");
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Sorts the contents of a chest based on ID code
 	 */
 	private boolean sortChest(Player player, String chestName)
 	{
 		if(chestName.equals(""))			{		player.sendMessage(messagePrefix + ChatColor.RED + "Please specify the name of the chest.");		return false;	}
 		if(!chests.containsKey(chestName))	{		player.sendMessage(messagePrefix + ChatColor.RED + "Chest \"" + chestName + "\" doesn't exist.");	return false; }
 
 //		System.out.println( messagePrefix + "Sorting chest \"" + chestName + "\"." );
 		InventoryLargeChest chest = chests.get(chestName);
 
 		int swapStackI = 0;
 		net.minecraft.server.ItemStack swapStack;
 		
 		for(int index = 0; index < 54; index++)
 		{
 			net.minecraft.server.ItemStack stack = chest.c_(index);
 			swapStack = stack;
 			swapStackI = index;
 			for(int i = index+1; i < 54; i++)
 			{
 				net.minecraft.server.ItemStack stack2 = chest.c_(i);
 				if((stack2 != null && stack2.count != 0 && stack2.id != 0) && 
 						(swapStack == null || stack2.id < swapStack.id))
 				{
 					swapStackI = i;
 					swapStack = stack2;
 				}
 			}
 			
 			if(swapStack != null)
 			{
 				if(swapStack != stack)
 				{
 //					if(stack != null)
 //						System.out.println( "Swapping, ids: " + stack.id + "/" + swapStack.id + " at indexes " + index + "/" + swapStackI + "." );
 //					else
 //						System.out.println( "Swapping, ids: null/" + swapStack.id + " at indexes " + index + "/" + swapStackI + "." );
 					chest.a(index, swapStack);
 					chest.a(swapStackI, stack);
 				}
 //				else
 //				{
 //					System.out.println( "Not swapping, ids: " + stack.id + "/" + swapStack.id + " at indexes " + index + "/" + swapStackI + "." );
 //				}
 			}
 			else
 			{
 //				System.out.println( messagePrefix + "Sorting done after " + index + " elements." );
 				break;
 			}
 		}
 		chest.h();
 		return true;
 	}
 	/**
 	 * Stacks the contents of a chest.
 	 */
 	private boolean stackChest(Player player, String chestName)
 	{
 		if(chestName.equals(""))			{		player.sendMessage(messagePrefix + ChatColor.RED + "Please specify the name of the chest.");		return false;	}
 		if(!chests.containsKey(chestName))	{		player.sendMessage(messagePrefix + ChatColor.RED + "Chest \"" + chestName + "\" doesn't exist.");	return false; }
 		
 		InventoryLargeChest chest = chests.get(chestName);
 		for(int index = 0; index < 54; index++)
 		{
 			net.minecraft.server.ItemStack stack = chest.c_(index);
 			if(stack != null && stack.count != 0 && stack.id != 0)
 			{
 				//player.sendMessage("Trying to merge stack: " + index);
 				int i = 0;
 				for(net.minecraft.server.ItemStack stack2 : chest.getContents())
 				{
 					// stack2.b() gets max stack size
 					if(stack2 != null && i != index && stack2.count != 0 && stack2.count < stack2.b() && stack2.id == stack.id)
 					{
 						//player.sendMessage("Found viable target stack: " + i + ", sizes: " + stack.count + ", " + stack2.count);
 						stack.count = Math.min(stack2.b(), stack.count + stack2.count );
 						chest.a(index, stack);
 						stack2.count = Math.max(0,	stack.count + stack2.count - stack2.b());
 						//player.sendMessage("New Sizes: " + stack.count + ", " + stack2.count);
 						if(stack2.count > 0)
 						{
 							chest.a(i, stack2);
 							break;
 						}
 						else
 							chest.a(i, null);
 					}
 					i++;
 				}
 			}
 			index++;
 		}
 		chest.h();
 		return true;
 	}
 	/**
 	 * Displays help with chest merging.
 	 */
 	private void dispMergeHelp(Player player)
 	{
 		player.sendMessage("/rcmerge <chest1name> <chest2name> [<newname>] [<flags>]");
 		player.sendMessage("Merge 2 chests, [arg] = Optional argument");
 		player.sendMessage("When no new name is specified, <chest1name> is used");
 		player.sendMessage("Flags:");
 		player.sendMessage("I	 - Ignore overfilled chests");
 		player.sendMessage("SWF - Auto-stack chests if overfilled");
 		player.sendMessage("O	 - Auto-overwrite target chest");
 		player.sendMessage("AM	- Auto-merge with target chest");
 		player.sendMessage("S	 - Auto-stack chests before merging");
 		player.sendMessage("SS	- Auto-stack chests before & after merging");
 	}
 	/**
 	 * Merges 2 chests, not ignoring help messages.
 	 */
 	private boolean mergeChests(Player player, String[] args)
 	{
 		return mergeChests(player, args, false);
 	}
 	/**
 	 * Merges 2 chests.
 	 * Flag I causes it to ignore if the total amount of items in the 2 chests exceeds 54
 	 * Flag SWF causes it to attempt to fix the over filled issue by pre-stacking all items in the 2 chests.
 	 * Flag O makes it remove the target chest first
 	 * Flag AM makes it merge 1 of the chests with the target chest first
 	 * Flag S stacks the chests before merging
 	 * Flag SS stacks the chests after merging as well.
 	 * 
 	 * ignoreHelp makes it not print any help when errors occur.
 	 */
 	private boolean mergeChests(Player player, String[] args, boolean ignoreHelp)
 	{
 		String newName;
 		String name1;
 		String name2;
 		boolean ignoreOverfilled = false;
 		boolean autoMergeTarget = false;
 		boolean autoOverwriteTarget = false;
 		boolean autoStackBefore = false;
 		boolean autoStackAfter = false;
 		boolean stackWhenOverfilled = false;
 		
 		if(args.length < 2 || args[0].equals("") || args[1].equals(""))
 		{
 			player.sendMessage(ChatColor.RED + "Incorrect usage of /rcmerge, it is used like this:");
 			dispMergeHelp(player);
 				return false;
 		}
 		name1 = args[0];
 		name2 = args[1];
 		if(!chests.containsKey(name1))	{	player.sendMessage(messagePrefix + ChatColor.RED + "Chest \"" + name1 + "\" doesn't exist.");	return false;	}
 		if(!chests.containsKey(name2))	{	player.sendMessage(messagePrefix + ChatColor.RED + "Chest \"" + name2 + "\" doesn't exist.");	return false;	}
 		
 		if(args.length > 3)
 		{
 			stackWhenOverfilled = args[3].toLowerCase().contains("swf" );
 			args[3].replaceFirst("swf", "	 ");
 			ignoreOverfilled 	= args[3].toLowerCase().contains("i" );
 			autoMergeTarget 	= args[3].toLowerCase().contains("am");
 			autoOverwriteTarget = args[3].toLowerCase().contains("o" );
 			autoStackAfter 		= args[3].toLowerCase().contains("ss");
 			autoStackBefore 	= args[3].toLowerCase().contains("s" );
 			
 		}
 		
 		if(args.length > 2 && !args[2].equals(""))
 		{
 			newName = args[2];
 			if(newName != name1 && newName != name2 && chests.containsKey(newName))
 			{
 				if(!(autoMergeTarget || autoOverwriteTarget))
 				{
 					player.sendMessage(messagePrefix + ChatColor.RED + "Error: Chest \"" + newName + "\" already exists.");
 					player.sendMessage(messagePrefix + "Premerge it with " + name1 + "/" + name2 + ".");
 					player.sendMessage(messagePrefix + "Remove it with the /rcremove <name> command.");
 					player.sendMessage(messagePrefix + "Or specify flag O to overwrite.");
 
 					return false;
 				}
 				else if(autoMergeTarget)
 				{
 					player.sendMessage(messagePrefix + "Premerging chests \"" + name1 + "\" and \"" + name2 + "\".");
 					String[] fakeArgs = {name1, newName, "", args[3]};
 					if(!mergeChests(player, fakeArgs))
 					{
 						player.sendMessage(messagePrefix + "Retrying premerge with pre-stack.");
 						fakeArgs[2] = "";
 						fakeArgs[3] = "swf" + args[3];
 						if(!mergeChests(player, fakeArgs))
 						{
 							player.sendMessage(messagePrefix + ChatColor.RED + "Error: \"" + name1 + "\" and \"" + name2 + "\" could not be merged.");
 							return false;
 						}
 					}
 				}
 				else if(autoOverwriteTarget)
 				{
 					chests.remove(newName);
 				}
 			}
 		}
 		else
 		{	newName = name1;	}
 		
 		InventoryLargeChest newChest = new InventoryLargeChest(newName, new rcChest(), new rcChest());
 		InventoryLargeChest chest1 = chests.get(name1);
 		InventoryLargeChest chest2 = chests.get(name2);
 		
 		if(autoStackBefore)
 		{
 			stackChest(player, name1);
 			stackChest(player, name2);
 		}
 		
 		if(!ignoreOverfilled)
 		{
 			int numItems = 0;
 			// count the amount of items
 			for(net.minecraft.server.ItemStack stack : chest1.getContents())
 				if(stack != null && stack.count != 0 && stack.id != 0)
 					numItems++;
 
 			for(net.minecraft.server.ItemStack stack : chest2.getContents())
 				if(stack != null && stack.count != 0 && stack.id != 0)
 					numItems++;
 			
 			if(numItems > 54)
 			{
 				if(stackWhenOverfilled && !autoStackBefore)
 				{
 					player.sendMessage(messagePrefix + "Total amount of items exceeds 54, prestacking chests");
 					numItems = 0;
 					stackChest(player, name1);
 					stackChest(player, name2);
 					for(net.minecraft.server.ItemStack stack : chest1.getContents())
 						if(stack != null && stack.count != 0 && stack.id != 0)
 							numItems++;
 
 					for(net.minecraft.server.ItemStack stack : chest2.getContents())
 						if(stack != null && stack.count != 0 && stack.id != 0)
 							numItems++;
 				}
 				if(numItems > 54)
 				{
 					player.sendMessage(messagePrefix + ChatColor.RED + "Error: Total amount of items exceeds 54");
 					if(!ignoreHelp)
 					{
 						if(!stackWhenOverfilled)
 							player.sendMessage(messagePrefix + "Specify flag SWF to stack contents");
 						player.sendMessage(messagePrefix + "Specify flag I to ignore this error");
 					}
 					// no dropping (yet)
 					//player.sendMessage(messagePrefix + "Excess items will be dropped");
 					return false;
 				}
 			}
 		}
 
 		int index = 0;
 		// Copy the contents of the 1st chest to the new chest, preserving locations
 		for(net.minecraft.server.ItemStack stack : chest1.getContents())
 		{
 			if(stack != null && stack.count != 0 && stack.id != 0)
 				newChest.a(index, new net.minecraft.server.ItemStack(stack.id, stack.count, 0));
 			index++;
 		}
 
 		index = 0;
 		// Merge the contents of the newChest and the 2nd chest, try to preserve locations
 		for(net.minecraft.server.ItemStack stack : chest2.getContents())
 		{
 			if(stack != null && stack.count != 0 && stack.id != 0)
 			{	
 				net.minecraft.server.ItemStack trgStack = newChest.c_(index);
 				if(trgStack == null || trgStack.count == 0 || trgStack.id == 0)
 					newChest.a(index, new net.minecraft.server.ItemStack(stack.id, stack.count, 0));
 				else
 				{
 					int i = 0;
 					for(net.minecraft.server.ItemStack targetStack : newChest.getContents())
 					{
 						if(targetStack == null || targetStack.count == 0 || targetStack.id == 0)
 						{
 							newChest.a(i, new net.minecraft.server.ItemStack(stack.id, stack.count, 0));
 							break;
 						}
 						i++;
 					}
 				}
 			}
 			index++;
 		}
 
 
 		chests.remove(name1);
 		chests.remove(name2);
 		chests.put(newName, newChest);
 		
 		if(autoStackAfter)	stackChest(player, newName);
 		
 		player.sendMessage(messagePrefix + ChatColor.GREEN + "Merged chests \"" + name1 + "\" and \"" + name2 + "\" into \"" + newName + "\".");
 		return true;
 	}
 	/**
 	 * Checks if a sign contains a chestname tag, and returns the name if it does.
 	 */
 	public String getSignName(String[] lines)
 	{
 		String name = "";
 		for(int i = 0; i < 3 && name.equals(""); i++)
 		{
 			if (lines[i].contains(" "))
 			{
 				String[] split = lines[i].split(" ");
 				if(split[0].equalsIgnoreCase("[rc]"))
 					name = split[1];
 			}
 			else if(lines[i].equalsIgnoreCase("[rc]"))
 			{
 				if(lines[i+1].length() > 1)
 					// ignore everything after spaces
 					name = lines[i+1].split(" ")[0];
 			}
 		}
 		if(name.equals(""))
 			debugPrint("No sign name specified");
 		return name;
 	}
 	/**
 	 * Removes a chest.
 	 */
 	public void removeChest(Player player, String name)
 	{
 		if(name.equals(""))
 			{
 				player.sendMessage(messagePrefix + ChatColor.RED + "Please specify a chest name");
 				return;
 			}
 			if(!chests.containsKey(name))
 			{
 				player.sendMessage(messagePrefix + ChatColor.RED + "Chest \"" + name + "\" doesn't exist.");
 				return;
 			}
 			chests.remove(name);
 		player.sendMessage(messagePrefix + ChatColor.GREEN + "Chest \"" + name + "\" succesfully removed.");
 	}
 	/**
 	 * Renames a chest
 	 */
 	public void renameChest(Player player, String[] args)
 	{
 		if(args.length < 1 || args[0].equals(""))
 		{
 			player.sendMessage(messagePrefix + ChatColor.RED + "Please specify a source chest name");
 			if(args.length < 2 || args[1].equals(""))
 				player.sendMessage(messagePrefix + ChatColor.RED + "Please specify a target chest name");
 			return;
 		}
 		if(args.length < 2 || args[1].equals(""))
 		{	player.sendMessage(messagePrefix + ChatColor.RED + "Please specify a target chest name");				return;	}
 		String oldName = args[0];
 		String newName = args[1];
 		if(!chests.containsKey(oldName))
 		{	player.sendMessage(messagePrefix + ChatColor.RED + "Chest \"" + oldName + "\" doesn't exist");		 		return;	}
 		if(chests.containsKey(newName))
 		{	player.sendMessage(messagePrefix + ChatColor.RED + "Target name: \"" + newName + "\" already exist");	return;	}
 		
 		InventoryLargeChest chest = chests.get(oldName);
 		chests.remove(oldName);
 		chests.put(newName, chest);
 		
 		player.sendMessage(messagePrefix + ChatColor.GREEN + "Renamed " + renameSigns(oldName, newName) + " signs.");
 		player.sendMessage(messagePrefix + ChatColor.GREEN + "Renamed chest \"" + oldName + "\" to \"" + newName + ".");
 	}
 	/**
 	 * Creates a chest with the specified name, used for the rcCreate command.
 	 */
 	public void createChest(Player player, String name)
 	{
 		debugPrint("Creating Chest");
 		if(name.equals(""))
 			{
 				player.sendMessage(messagePrefix + ChatColor.RED + "Please specify a chest name");
 				return;
 			}
 			if(chests.containsKey(name))
 			{
 				player.sendMessage(messagePrefix + ChatColor.RED + "Chest \"" + name + "\" already exists.");
 				return;
 			}
 			else
 				chests.put(name, new InventoryLargeChest(name, new rcChest(), new rcChest()));
 		player.sendMessage(messagePrefix + ChatColor.GREEN + "Chest \"" + name + "\" succesfully created.");
 			openChest(player, name);
 	}
 	/**
 	 * Adds a chest at the specified location, used when placing signs.
 	 * Transfers all items from the existing chest to the new chest.
 	 */
 	public void addChest(String name, Player player, Location location)
 	{
 		debugPrint("Adding Chest");
 		Block block = location.getBlock().getRelative(BlockFace.DOWN);
 		if(!(block.getState() instanceof Chest))
 		{
 			player.sendMessage(messagePrefix + ChatColor.RED + "Sign not placed above chest, try again.");
 			if(location.getBlock() instanceof Sign)
 				for(int i = 0; i < 4; i++)
 					((Sign)location.getBlock()).setLine(i, "ERROR");
 			return;
 		}
 		if(chests.containsKey(name))
 		{
 			transferItems((Chest)block.getState(), chests.get(name));
 			player.sendMessage(messagePrefix + ChatColor.GREEN + "Linked to chest \"" + name + "\".");
 			return;
 		}
 		InventoryLargeChest lrgchest = new InventoryLargeChest(name, new rcChest(), new rcChest());
 		player.sendMessage(messagePrefix + ChatColor.GREEN + "Chest \"" + name + "\" succesfully added.");
 		
 		chests.put(name, lrgchest);
 		
 		transferItems((Chest)block.getState(), lrgchest);
 	}
 	/**
 	 * Transfers the items from a chest to a virtual chest.
 	 */
 	public void transferItems(Chest chest, InventoryLargeChest lrgchest)
 	{
 		boolean full = false;
 		for (int index = 0; index < 27; index++)
 		{
 			org.bukkit.inventory.ItemStack stack = chest.getInventory().getItem(index);
 
 			if ((!full) && (stack.getTypeId() > 0) && (stack.getAmount() > 0))
 			{
 				boolean found = false;
 				for (int i = 0; i < 54; i++)
 				{
 					net.minecraft.server.ItemStack stack2 = lrgchest.c_(i);
 
 					if ((stack2 != null) && (stack2.id == stack.getTypeId()) && (stack.getMaxStackSize() > 1))
 					{
 						debugPrint("Merging itemstacks");
 						stack2.count = Math.min(stack2.b(), stack.getAmount() + stack2.count);
 						lrgchest.a(i, stack2);
 						int newcount = Math.max(0, stack.getAmount() + stack2.count - stack2.b());
 						if (newcount > 0)
 						{
 							stack = new org.bukkit.inventory.ItemStack(stack.getTypeId(), newcount);
 						}
 						else
 						{
 							found = true;
 							break;
 						}
 					}
 					else
 					{
 						if ((stack2 != null) && (stack2.id != 0) && (stack2.count != 0))
 							continue;
 						debugPrint("Creating new itemstack for transfer");
 						lrgchest.a(i, new net.minecraft.server.ItemStack(stack.getTypeId(), stack.getAmount(), 0));
 						found = true;
 						break;
 					}
 				}
 				if (!found)
 				{
 					debugPrint("Did not find clear spot, dropping item");
 					chest.getBlock().getWorld().dropItem(chest.getBlock().getLocation(), stack);
 					full = true;
 				}
 			}
 
 			if ((!full) || (stack.getTypeId() == 0) || (stack.getAmount() == 0))
 				continue;
 			debugPrint("Dropping item");
 			chest.getBlock().getWorld().dropItem(chest.getBlock().getLocation(), stack);
 		}
 
 		chest.getInventory().clear();
 		debugPrint("Done");
 	}
 	/**
 	 * Opens a chest without supressing warning when new chest is created.
 	 */
 	public boolean openChest(Player player, String chestName)
 	{	return openChest(player, chestName, false);	}
 	/**
 	 * Opens a chest.
 	 */
 	public boolean openChest(Player player, String chestName, boolean supressWarning)
 	{
 		debugPrint(player.getName() + " is trying to open chest \"" + chestName + "\".");
 		if(!(chests.containsKey(chestName)))
 		{
 			if(!supressWarning)
 			{
 				debugPrint("Chest \"" + chestName + "\" doesn't exist");
 				player.sendMessage(messagePrefix + ChatColor.RED + "Chest \"" + chestName + "\" does not exist.");
 				player.sendMessage(messagePrefix + "Type /rccreate " + chestName + " to create it");
 			}
 			return false;
 		}
 		//player.sendMessage(messagePrefix + "Opening chest \"" + chestName + "\".");
 		debugPrint("The chest \"" + chestName + "\" exists");
 		InventoryLargeChest chest = chests.get(chestName);
 		EntityPlayer cPlayer = ((CraftPlayer)player).getHandle();
 
 		debugPrint("Presenting chest \"" + chestName + "\".");
 		cPlayer.a(chest);
 		return true;
 	}
 
 	public void debugPrint(String msg)
 	{
 //		getServer().broadcastMessage(msg);
 //		log.info(debugPrefix + msg);
 	}
 	
 	/**
 	 * Add a sign to the list.
 	 */
 	public void addSign(Sign sign)
 	{	signs.add(sign);	System.out.println("Added sign " + sign.toString());	}
 	/**
 	 * Remove a sign from the list.
 	 */
 	public void removeSign(Sign a_sign)
 	{
 		for(Object obj : signs.toArray())
 		{
 			if(!(obj instanceof Sign))
 			{
 				System.out.println("AAAAAAAAAAAAAA, invalid sign in signs");
 				continue;
 			}
 			Sign sign = (Sign) obj;
 			if(		sign.getBlock().getLocation().getBlockX() == a_sign.getBlock().getLocation().getBlockX() &&
 					sign.getBlock().getLocation().getBlockY() == a_sign.getBlock().getLocation().getBlockY() &&
 					sign.getBlock().getLocation().getBlockZ() == a_sign.getBlock().getLocation().getBlockZ())
 			{
 				signs.remove(sign);
 				System.out.println("Removed sign " + sign.toString());
 			}
 		}
 	}
 	/**
 	 * Rename all signs containing specified name
 	 */
 	public int renameSigns(String oldName, String newName)
 	{
 		int amount = 0;
 		for(Object obj : signs.toArray())
 		{
 			if(!(obj instanceof Sign))
 			{
 				System.out.println("AAAAAAAAAAAAAA, invalid sign in signs");
 				continue;
 			}
 			Sign sign = (Sign)obj;
 			String[] lines = sign.getLines();
 			for(int i = 0; i < 4; i++)
 			{
 				if(lines[i].contains("[rc]"))
 				{
 					if(lines[i].contains(" "))
 					{
 						if(lines[i].contains(oldName))
 						{
 							lines[i] = lines[i].replace(oldName, newName);
 							amount++;
 						}
 					}
 					else if(i < 3)
 						if(lines[i+1].contains(oldName))
 						{
 							lines[i+1] = lines[i+1].replace(oldName, newName);
 							amount++;
 						}
 					break;
 				}
 			}
 			for(int i = 0; i < 4; i++)
 			{
 				sign.setLine(i, lines[i]);
 			}
 			sign.update();
 		}
 		return amount;
 	}
 	/**
 	 * Set up the permissions system
 	 */
 	private void setupPermissions()
 	{
 		Plugin perm = this.getServer().getPluginManager().getPlugin("Permissions");
 		if (rcPlugin.Permissions == null)
 			if (perm != null)
 			{
 				rcPlugin.Permissions = ((Permissions)perm).getHandler();
 				permissionsOn = true;
 			}
 			else
 				log.info(messagePrefix + "Permissions not detected, everyone can use chests.");
 	}
 }
