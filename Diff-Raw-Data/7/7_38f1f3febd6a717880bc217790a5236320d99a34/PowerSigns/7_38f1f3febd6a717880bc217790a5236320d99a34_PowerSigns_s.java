 package com.ricochet1k.bukkit.powersigns;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.minecraft.server.Entity;
 import net.minecraft.server.EntityFallingSand;
 import net.minecraft.server.EntitySnowball;
 import net.minecraft.server.EntityTNTPrimed;
 import net.minecraft.server.TileEntity;
 import net.minecraft.server.World;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.ContainerBlock;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.CraftChunk;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.craftbukkit.block.CraftDispenser;
 import org.bukkit.craftbukkit.block.CraftSign;
 import org.bukkit.craftbukkit.entity.CraftEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class PowerSigns extends JavaPlugin
 {
 	////////////// Settings /////////////////
 	
 	// General settings
 	public static int maxDistance = 50;
 	
 	// Cannon settings
 	public static int powerPerTNT = 50; // use up multiple TNT's for more power
 	public static int maxCannonPower = 200;
 	
 	// Fling settings
 	public static int maxFlingPower = 900;
 	
 //ADD STUFF TO THIS
 	final String[][] syntaxMessages = {
 		{
 		ChatColor.YELLOW + "Push", 
 		ChatColor.YELLOW + "Pull",  
 		ChatColor.YELLOW + "InvPush",  
 		ChatColor.YELLOW + "InvPull",  
 		ChatColor.YELLOW + "Fling",  
 		ChatColor.YELLOW + "Cannon",  
 		ChatColor.YELLOW + "Linecopy",  
 		ChatColor.YELLOW + "Lineswap"
 		}, 
 		
 		{
 			"powersigns.create.push",
 			"powersigns.create.pull",
 			"powersigns.create.invpush",
 			"powersigns.create.invpull",
 			"powersigns.create.fling",
 			"powersigns.create.cannon",
 			"powersigns.create.line",
 			"powersigns.create.line"
 		}
 		};
 	
 	//////////// End Settings ////////////
 	
 	
 	// junk
 	public static final Logger log = Logger.getLogger("Minecraft");
 	public final PowerSignsBlockListener blockListener = new PowerSignsBlockListener(this);
 	public final PowerSignsPlayerListener playerListener = new PowerSignsPlayerListener(this);
 	public static final Random random = new Random();
 	
 	public static PermissionHandler Permissions = null;
 
 	@Override
 	public void onEnable()
 	{
 		Plugin test = getServer().getPluginManager().getPlugin("Permissions");
 		if (test != null)
 		{
 			PowerSigns.Permissions = ((Permissions)test).getHandler();
 			log.info("["+getDescription().getName()+"] Enabling " + getDescription().getFullName() + " [Permissions active]");
 		}
 		else
 		{
 			log.info("Permissions not found for PowerSigns...");
 			log.info("["+getDescription().getName()+"] Enabling " + getDescription().getFullName() + "[Permissions not found]");
 		}
 		
 		getServer().getPluginManager().registerEvent(Type.REDSTONE_CHANGE, blockListener, Priority.Highest, this);
 		
 		// Once Bukkit has support for unregistering events this should be handled in setDebugRightClick
 		getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
 	
 	}
 
 	@Override
 	public void onDisable()
 	{
 		log.info("["+getDescription().getName()+"] Disabling PowerSigns");
 	}
 	
 	public static boolean hasPermission(Player player, String permission)
 	{
 		if (PowerSigns.Permissions != null)
 		{
 			if (PowerSigns.Permissions.has(player, permission)) 
 				return true;
 			else return false;
 		}
 		return player.isOp();
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		Player player = null;
 		
 		log.info("["+getDescription().getName()+"]" + "got command: "+ sender.toString());
 		
 		if (label.equalsIgnoreCase("PowerSigns") || label.equalsIgnoreCase("ps"))
 		{
 			// §
 			if (sender instanceof Player)
 				player = (Player)sender;
 			else
 			{
 				log.info("Error: That command must be used by a player.");
 				return false;
 			}
 				
 			
 			if (args.length == 0)
 			{
 				//sendUsage(player);
 				player.sendMessage(ChatColor.RED + "PowerSigns commands: (alias /ps)");
 				if(PowerSigns.hasPermission(player, "powersigns.debug"))
 					player.sendMessage(ChatColor.RED + "/powersigns debug (snow[balls] | rightclick | rc)");
 				if(PowerSigns.hasPermission(player, "powersigns.syntax"))
 					player.sendMessage(ChatColor.RED + "/powersigns syntax [signtype]");
 				return true;
 			}
 			
 			
 			if (args.length > 0)
 			{
 				if (args[0].equalsIgnoreCase("debug"))
 				{
 					//improper arguments, display syntax
 					if (args.length == 1)
 					{
 						player.sendMessage(ChatColor.RED + "PowerSigns usage: /powersigns debug (snow[balls] | rightclick | rc ...");
 						return true;
 					}
 					
 					//snowball debugging
 					if (args[1].equalsIgnoreCase("snowballs") || args[1].equalsIgnoreCase("snow"))
 					{
 						if (!hasPermission(player, "powersigns.debug.snowballs"))
 						{
 							player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
 							return true;
 						}
 						
 						if (args.length > 2)
 						{
 							if (args[2].equalsIgnoreCase("on"))
 								setDebugSnowballs(true);
 							else if (args[2].equalsIgnoreCase("off"))
 								setDebugSnowballs(false);
 							else
 							{
 								player.sendMessage(ChatColor.RED + "PowerSigns usage: /powersigns debug snow[balls] [on|off]");
 								return true;
 							}
 						}
 						else
 							setDebugSnowballs(!debugSnowballs);
 						
 						player.sendMessage(ChatColor.GREEN + "Debugging snowballs " + (debugSnowballs? "enabled." : "disabled."));
 						return true;
 					}
 					//right-click debugging
 					else if (args[1].equalsIgnoreCase("rightclick") || args[1].equalsIgnoreCase("rc"))
 					{
 						if (!hasPermission(player, "powersigns.debug.rightclick"))
 						{
 							player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
 							return true;
 						}
 						
 						if (args.length > 2)
 						{
 							if (args[2].equalsIgnoreCase("on")) setDebugRightClick(true);
 							else if (args[2].equalsIgnoreCase("off")) setDebugRightClick(false);
 							else
 							{
 								player.sendMessage(ChatColor.RED + "PowerSigns usage: /powersigns debug rightclick [on|off]");
 								return true;
 							}
 						}
 						else
 							setDebugRightClick(!debugRightClick);
 						
 						player.sendMessage(ChatColor.GREEN + "Debugging right click " + (debugRightClick? "enabled." : "disabled."));
 						return true;
 					}
 				}
 				else if(args[0].equalsIgnoreCase("syntax"))
 				{
 					if(!PowerSigns.Permissions.has(player, "powersigns.syntax"))
 					{
 						player.sendMessage(ChatColor.RED + "You do not have access to this command.");
 						return true;
 					}
 					//open-ended
 					if(args.length == 1)
 					{
 						boolean hasAnything = false;
 						//if !permissions player.sendMessage(ChatColor.RED + "You do not have access to this command.");
 						player.sendMessage(ChatColor.GREEN + "Syntax browsing: /powersigns syntax [sign]");
 						player.sendMessage(ChatColor.GOLD + "Signs available to you:");
 						//show a list of the signs they have access to
 						for(int i = 0; i < syntaxMessages[0].length; i++)
 							//permissions check against corresponding string
 							if(PowerSigns.hasPermission(player, syntaxMessages[1][i]))
 							{
 								player.sendMessage(syntaxMessages[0][i]);
 								hasAnything = true;
 							}
 						player.sendMessage(hasAnything? 
 								(ChatColor.GOLD + "End list.")
 								:(ChatColor.RED + "You don't have access to any PowerSigns!"));
 						return true;				
 					}
 				}
 					//sign-specific syntax
 				else if(args[1].equalsIgnoreCase("reload"))
 				{
 					if(!PowerSigns.hasPermission(player, "powersigns.reload"))
 					{
 						player.sendMessage(ChatColor.RED + "You do not have access to this command.");
 						return true;
 					}
 					reloadPowerSigns();
 				}
 				else if(args[1].equalsIgnoreCase("asdf"))
 				{
 					// TODO find something to do for this. :P
 					return true;
 				}
 				else if((args[0].equalsIgnoreCase("wires"))) //display "wiring status"
 				{
 					if(!PowerSigns.hasPermission(player, "powersigns.wires"))
 					{
 						player.sendMessage(ChatColor.RED + "You do not have access to this command.");
 						return true;
 					}
 //DISPLAY STATUSES FOR GLOBAL WIRE-ON/OFF SETTINGS
 				}
 				//handle incorrect commands
 				else
 					player.sendMessage(ChatColor.RED + "[PowerSigns]Command not found");
 			}
 		}
 		
 		return false;
 	}
 	
 	private void reloadPowerSigns() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	static final Material[] signMaterials = new Material[] { Material.SIGN_POST, Material.WALL_SIGN };
 	
 	
 	/////////////// Patterns //////////////////
 	
 	//static final Pattern actionPattern = Pattern.compile("!([a-z_]+)", Pattern.CASE_INSENSITIVE);
 	static final String repeatPart = "(?:\\*(\\d{1,2}))?";
 	static final String verticalPart = "([ud])?";
 	static final String moveDirPart = "([^v<>])?";
 	static final String cannonTypePart = "(tnt|sand|gravel)?";
 	static final String directionPart = "([fblruds])";
 	static final String allPart = "(all)?";
 	
 	static final Pattern pushPattern = Pattern.compile(joinBy("\\s*","!push",repeatPart,verticalPart,moveDirPart), Pattern.CASE_INSENSITIVE);
 	static final Pattern pullPattern = Pattern.compile(joinBy("\\s*","!pull",repeatPart,verticalPart,moveDirPart), Pattern.CASE_INSENSITIVE);
 	static final Pattern detectPattern = Pattern.compile(joinBy("\\s*","!detect",verticalPart), Pattern.CASE_INSENSITIVE);
 	static final Pattern linemovePattern = Pattern.compile(joinBy("\\s*","!line(copy|swap)"), Pattern.CASE_INSENSITIVE);
 	static final Pattern activatePattern = Pattern.compile(joinBy("\\s*","!activate", allPart, verticalPart), Pattern.CASE_INSENSITIVE);
 	static final Pattern activateLongPattern = Pattern.compile(joinBy("\\s*","!activate_long",verticalPart), Pattern.CASE_INSENSITIVE);
 	static final Pattern cannonPattern = Pattern.compile(joinBy("\\s*","!cannon",cannonTypePart,verticalPart), Pattern.CASE_INSENSITIVE);
 	static final Pattern invpushPattern = Pattern.compile(joinBy("\\s*","!invpush",repeatPart,verticalPart,directionPart), Pattern.CASE_INSENSITIVE);
 	static final Pattern invpullPattern = Pattern.compile(joinBy("\\s*","!invpull",repeatPart,verticalPart,directionPart), Pattern.CASE_INSENSITIVE);
 	static final Pattern flingPattern = Pattern.compile(joinBy("\\s*","!fling",allPart, directionPart), Pattern.CASE_INSENSITIVE);
 	static final Pattern lineSpecPattern = Pattern.compile("([fblruds])([1234]) ([fblruds])([1234])", Pattern.CASE_INSENSITIVE);
 	
 	public static String[] matchPatterns(String input, Pattern... patterns)
 	{
 		String[] matches = new String[patterns.length+1];
 		int i = 0;
 		
 		for (Pattern pattern : patterns)
 		{
 			Matcher m = pattern.matcher(input);
 			
 			if (!m.lookingAt())
 				return null;
 			
 			matches[i++] = m.group(1);
 			input = input.substring(m.end());
 		}
 		
 		matches[i] = input;
 		
 		return matches;
 	}
 	public static String[] matchPatternsEnd(String input, Pattern... patterns)
 	{
 		String[] result = matchPatterns(input, patterns);
 		if (result == null || (result[result.length-1] != null && result[result.length-1].length() != 0)) return null;
 		return result;
 	}
 	
 	public boolean tryPowerSign(Block signBlock)
 	{
 		if(!matchesMaterials(signBlock, signMaterials)) return false; // not a sign
 		
 		Sign signState = (Sign) signBlock.getState();
 
 		String command = signState.getLine(0);
 		if (command.length() == 0 || command.charAt(0) != '!') return false;
 		
 		failMsg = "";
 		
 		//final String action;
 		
 		//String[] result = matchPatterns(command, actionPattern);
 		//if (result == null) { debugFail("syntax"); doDebugging(signBlock, 2); return false; }
 		
 		//action = m.group(0);
 
 		
 
 		try
 		{
 //NEED TO ADD SETTINGS(wire/rc) AND PERMISSIONS(rc-perm checking) NODES FOR THESE
 			boolean ret = false;
 			Matcher m = pushPattern.matcher(command);
 			// Execute the action
 			if (m.usePattern(pushPattern).matches())
 			{
 				final int repeat = (m.group(1) != null) ? Integer.parseInt(m.group(1)) : 1;
 				if (repeat <= 0) return debugFail("bad repeat");
 
 				BlockFace signDir = getSignDirection(signBlock);
 				BlockFace forward = getForward(signDir, m.group(2));
 				Block startBlock = getStartBlock(signBlock, signDir, forward).getFace(forward, 1);
 				BlockFace direction = getDirection(m.group(3), signDir, m.group(2));
 
 				Material[] moveTypes = getMaterials(signState.getLine(1));
 				
 				ret = tryPushBlocks(startBlock, forward, moveTypes, repeat);
 			}
 			//match to !pull
 			else if (m.usePattern(pullPattern).matches())
 			{
 				final int repeat = (m.group(1) != null) ? Integer.parseInt(m.group(1)) : 1;
 				if (repeat <= 0) return debugFail("bad repeat");
 
 				BlockFace signDir = getSignDirection(signBlock);
 				BlockFace forward = getForward(signDir, m.group(2));
 				Block startBlock = getStartBlock(signBlock, signDir, forward).getFace(forward, 1);
 				BlockFace direction = getDirection(m.group(3), signDir, m.group(2));
 
 				Material[] moveTypes = getMaterials(signState.getLine(1));
 				
 				ret = tryPullBlocks(startBlock, forward, moveTypes, repeat);
 			}
 			//match to !detect
 			else if (m.usePattern(detectPattern).matches())
 			{
 				BlockFace signDir = getSignDirection(signBlock);
 				BlockFace forward = getForward(signDir, m.group(1));
 				Block startBlock = getStartBlock(signBlock, signDir, forward).getFace(forward, 1);
 				
 				signState.setLine(1, startBlock.getType().toString().toLowerCase());
 				return true;
 			}
 			
 			else if (m.usePattern(linemovePattern).matches())
 			{
 				BlockFace signDir = getSignDirection(signBlock);
 				
 				ret = tryLineAction(m.group(1), signState.getLine(1), signBlock, signDir);
 			}
 			//match to !activate
 			else if (m.usePattern(activatePattern).matches())
 			{
 				BlockFace signDir = getSignDirection(signBlock);
 				BlockFace forward = getForward(signDir, m.group(2));
 				Block startBlock;
 				if (forward.equals(signDir) && !signBlock.getType().equals(Material.WALL_SIGN))
 					startBlock = getStartBlock(signBlock, signDir, forward).getFace(forward, 1);
 				else
 					startBlock = signBlock.getFace(forward, 1);
 				
 				//log.info("["+getDescription().getName()+"]" + "activate: "+m + "  " + forward);
 				ret = tryActivate(startBlock, forward, m.group(1) != null);
 			}
 			else if (m.usePattern(activateLongPattern).matches())
 			{
 				BlockFace signDir = getSignDirection(signBlock);
 				BlockFace forward = getForward(signDir, m.group(1));
 				Block startBlock = getStartBlock(signBlock, signDir, forward).getFace(forward, 1);
 
 				ret = tryActivateLong(startBlock, forward);
 			}
 			else if (m.usePattern(cannonPattern).matches())
 			{
 				BlockFace signDir = getSignDirection(signBlock);
 				BlockFace forward = getForward(signDir, m.group(2));
 				Block startBlock = getStartBlock(signBlock, signDir, forward).getFace(forward, 1);
 
 				ret = tryCannonSign(startBlock, forward, signState.getLine(1), m.group(1));
 			}
 			else if (m.usePattern(invpushPattern).matches())
 			{
 				final int repeat = (m.group(1) != null) ? Integer.parseInt(m.group(1)) : 1;
 				if (repeat <= 0) return debugFail("bad repeat");
 				
 				BlockFace signDir = getSignDirection(signBlock);
 				BlockFace forward = getForward(signDir, m.group(2));
 				Block startBlock = getStartBlock(signBlock, signDir, forward).getFace(forward, 1);
 				
 				BlockFace dir = strToDirection(m.group(3), signDir);
 				if (dir == null) { debugFail("bad dir: "+m.group(3)); doDebugging(signBlock, 2); return false; }// shouldn't happen
 				
 				
 				Material[] moveTypes = getMaterials(signState.getLine(1));
 				
 				ret = tryInvPush(signBlock.getFace(dir), startBlock, forward, moveTypes, repeat);
 			}
 			else if (m.usePattern(invpullPattern).matches())
 			{
 				final int repeat = (m.group(1) != null) ? Integer.parseInt(m.group(1)) : 1;
 				if (repeat <= 0) return debugFail("bad repeat");
 				
 				BlockFace signDir = getSignDirection(signBlock);
 				BlockFace forward = getForward(signDir, m.group(2));
 				Block startBlock = getStartBlock(signBlock, signDir, forward).getFace(forward, 1);
 				
 				BlockFace dir = strToDirection(m.group(3), signDir);
 				if (dir == null) { debugFail("bad dir: "+m.group(3)); doDebugging(signBlock, 2); return false; }// shouldn't happen
 				
 
 				Material[] moveTypes = getMaterials(signState.getLine(1));
 				
 				ret = tryInvPull(signBlock.getFace(dir), startBlock, forward, moveTypes, repeat);
 			}
 			else if (m.usePattern(flingPattern).matches())
 			{
 				BlockFace signDir = getSignDirection(signBlock);
 				boolean applyAll;
 				try
 				{
 					applyAll = (m.group(1).equals("all"));
 				}
 				catch(Exception e)
 				{
 					applyAll = false;
 				}
 				BlockFace flingDir = strToDirection(m.group(2), signDir);
 				if(flingDir.equals(BlockFace.UP) 
 						|| flingDir.equals(BlockFace.DOWN) 
 						|| flingDir.equals(BlockFace.SELF)
 						|| flingDir == null)
 					flingDir = signDir;
 			    
 				ret = tryFlingSign(signBlock, signDir, flingDir, applyAll);
 			}
 			else
 			{ debugFail("syntax"); doDebugging(signBlock, 2); return false; }
 			
 			debugFail("unknown");
 			doDebugging(signBlock, ret? 0 : 1);
 			return ret;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			doDebugging(signBlock, 10);
 		}
 
 		return false;
 	}
 
 
 	// ////////////////////////////////// Helper Methods ///////////////////////////////////
 
 	public static BlockFace getForward(BlockFace signDir, String verticalStr)
 	{
 		if (verticalStr != null)
 		{
 			if (verticalStr.equals("u"))
 				return BlockFace.UP;
 			return BlockFace.DOWN;
 		}
 
 		return signDir;
 	}
 
 	public static Block getStartBlock(Block signBlock, BlockFace signDir, BlockFace forward)
 	{
 		if (signBlock.getType().equals(Material.WALL_SIGN))
 			return signBlock.getFace(signDir, 1); // skip the block it's attached to
 		else if (signBlock.getType().equals(Material.SIGN_POST) && forward == BlockFace.DOWN)
 			return signBlock.getFace(forward, 1); // let the sign post stand on something
 		return signBlock;
 	}
 
 	public static BlockFace getDirection(String directionStr, BlockFace signDir, String verticalStr)
 	{
 		if (directionStr == null) return null;
 
 		//parse perpendicular move-type sign modifiers
 		if (directionStr.equals("^"))
 		{
 			if (verticalStr == null) return BlockFace.UP;
 			else return signDir;
 		}
 		if (directionStr.equals("v"))
 		{
 			if (verticalStr == null) return BlockFace.DOWN;
 			else return getOppositeFace(signDir);
 		}
 		if (directionStr.equals("<")) return rotateFaceLeft(signDir);
 		if (directionStr.equals(">")) return rotateFaceRight(signDir);
 
 		return null;//shouldn't get to this point
 	}
 
 	public static Material[] getMaterials(String materialsStr)
 	{
 		String[] materialStrs = materialsStr.split("/");
 		Material[] materials = new Material[materialStrs.length];
 
 		for (int i = 0; i < materialStrs.length; i++)
 		{
 			materials[i] = Material.matchMaterial(materialStrs[i]);
 
 			if (materials[i] == null)
 				return null; // failure to match one of the materials
 		}
 
 		return materials;
 	}
 
 	public static boolean matchesMaterials(Block block, Material[] materials)
 	{
 		if (block == null) return false;
 		for (Material material : materials)
 		{
 			if (material != null && material.getId() == block.getTypeId())
 				return true;
 		}
 		return false;
 	}
 	
 	public void moveBlockLine(BlockLineIterator fromLine, BlockLineIterator toLine, int howMany)
 	{
 		if (fromLine == toLine || fromLine.nextBlock == toLine.nextBlock)
 		{
 			doDebugging(fromLine.nextBlock, 4);
 			
 			debugFail("fromLine == toLine");
 			
     		return;
 		}
 		if (howMany <= 0)
 			return;
 
 		int i = 0;
 
 		//moveBlock(fromLine.currentBlock, toLine.currentBlock);
 
 		for (; i < howMany; i++)
 		{
 			moveBlock(fromLine.next(), toLine.next());
 		}
 	}
 
 	public void moveBlock(Block from, Block to)
 	{
 		if (from == to)
 		{
 			debugFail("from == to");
 			doDebugging(from, 3);
 			return;
 		}
 		World world = ((CraftChunk) from.getChunk()).getHandle().d;
 
 		to.setTypeId(from.getTypeId());
 		to.setData(from.getData());
 
 		TileEntity tileEntity = world.getTileEntity(from.getX(), from.getY(), from.getZ());
 
 		if (tileEntity != null)
 		{
 			world.n(from.getX(), from.getY(), from.getZ()); // delete old tile entity
 			world.setTileEntity(to.getX(), to.getY(), to.getZ(), tileEntity);
 		}
 
 		from.setTypeId(0); // clear from
 	}
 
 	public BlockFace strToDirection(String s, BlockFace forward)
 	{
 		if (s.equals("f")) 		return forward;
 		else if (s.equals("b")) return getOppositeFace(forward);
 		else if (s.equals("l")) return rotateFaceLeft(forward);
 		else if (s.equals("r")) return rotateFaceRight(forward);
 		else if (s.equals("u")) return BlockFace.UP;
 		else if (s.equals("d")) return BlockFace.DOWN;
 		else if (s.equals("s")) return BlockFace.SELF;
 		else
 			return null;
 	}
 	
 
 	// //////////////////////// Debugging Stuff ///////////////////////////
 	
 	// Debug settings
 	public static boolean debugSnowballs = false;
 	public static boolean debugRightClick = false;
 	
 	
 	
 	String failMsg = "";
 	
 	protected boolean debugFail(String msg)
 	{
 		if (failMsg == null || failMsg.length() == 0)
 			failMsg = msg;
 		return false; // so you can return debugFail("...")
 	}
 	
 	public void doDebugging(Block block, int num)
 	{
 		if (block == null)
 		{
 			log.warning("["+getDescription().getName()+"]" + "null debug block");
 			return;
 		}
 		if (debugSnowballs && num > 0)
 		{
     		Location loc = block.getLocation();
     		CraftWorld cworld = ((CraftWorld) block.getWorld());
     		for (int i = 0; i < num; i++)
     		{
         		EntitySnowball snowball = new EntitySnowball(cworld.getHandle(), loc.getX()+0.5d, loc.getY()+1.0d, loc.getZ()+0.5d);
         		snowball.a(0.0d, 1.0d, 0.0d, 0.2f + 0.03f*(float)i, 4.0f);
         		cworld.getHandle().a(snowball);
     		}
 		}
 		//else
 		//	failMsg = "";
 		
 		BlockState state = block.getState();
 		if (state instanceof Sign)
 		{
 			Sign sign = ((Sign) state);
 			if (sign.getLine(2).equals("stacktrace"))
 			{
 				sign.setLine(2, "");
 				(new Throwable("Stacktrace")).fillInStackTrace().printStackTrace();
 			}
 			else if (sign.getLine(2).equals("debug"))
 			{
 				sign.setLine(3, failMsg);
 			}
 			sign.update(true);
 			//failMsg = "";
 		}
 	}
 	
 	public void setDebugSnowballs(boolean debug)
 	{
 		debugSnowballs = debug;
 	}
 	
 	public void setDebugRightClick(boolean debug)
 	{
 		/// Once Bukkit has support for unregistering events this will work.
 		
 		//if (debugRightClick && !debug)
 		//	getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
 		//else
 		//	getServer().getPluginManager().unregisterEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
 		
 		debugRightClick = debug;
 	}
 	
 	// //////////////////////// Action Methods ///////////////////////////
 
 	public boolean tryPushBlocks(Block startBlock, BlockFace forward, Material[] materials, int amount)
 	{
 		assert amount > 0;
 
 		int numToPush = 0;
 		int numEmpty = 0;
 
 		BlockLineIterator line = blocksInLine(startBlock, forward);
 
 		numToPush = skipMatching(line, materials);
 		if (numToPush < 1)
 		{ return debugFail("nothing to push"); }
 
 		Block pushEnd = line.currentBlock;
 
 		
 		numEmpty = skipEmpty(line, amount);
 
 		if (numEmpty != -1) // -1 means it hit the max, anything else means it didn't find enough
 			return debugFail("not enough space");
 
 
 		Block emptyEnd = line.currentBlock;
 
 		// have to move them backwards so that the blocks aren't overwritten during the copy
 		BlockFace backward = getOppositeFace(forward);
 		if (backward == null)
 		{
 			log.severe("["+getDescription().getName()+"]" + "backward == null!! forward: "+forward.toString());
 			return debugFail("backward == null");
 		}
 
 		moveBlockLine(blocksInLine(pushEnd, backward), blocksInLine(emptyEnd, backward), numToPush);
 
 		return true;
 	}
 
 	public boolean tryPullBlocks(Block startBlock, BlockFace forward, Material[] materials, int amount)
 	{
 		assert amount > 0;
 
 		int numEmpty = 0;
 		int numToPull = 0;
 		
 		
 		BlockLineIterator line = blocksInLine(startBlock, forward);
 
 		numEmpty = skipEmpty(line);
 
 		if (numEmpty < amount)
 		{
 			return debugFail("not enough space");
 		}
 
 		Block blockToPull = line.nextBlock;
 
 		numToPull = skipMatching(line, materials);
 		if (numToPull == 0)
 		{return debugFail("nothing to pull"); }
 
 
 		moveBlockLine(blocksInLine(blockToPull, forward),
 		              blocksInLine(blockToPull.getFace(forward, -1 * amount), forward), numToPull);
 
 		return true;
 	}
 
 	public boolean tryLineAction(final String action, String lineSpec, Block signBlock, BlockFace forward)
 	{
 		Matcher m = lineSpecPattern.matcher(lineSpec);
 		if (!m.matches())
 		{return debugFail("parse linespec"); }
 
 		// Sign[] signStates = new Sign[] { null, null };
 		Location[] signLocs = new Location[] { null, null };
 		int[] signLines = new int[] { -1, -1 };
 
 		for (int i = 0; i < 2; i++)
 		{
 			int skip = 1;
 
 			BlockFace dir = strToDirection(m.group(1 + 2 * i), forward);
 			if (dir == null)
 			{
 				log.warning("["+getDescription().getName()+"]" + "Bad direction: " + m.group(1 + 2 * i));
 				return false; // Shouldn't happen
 			}
 			
 			if (dir.equals(BlockFace.DOWN))
 				skip = 2;
 			
 
 			Block found;
 
 			if (dir != BlockFace.SELF)
 			{
 				Block start = signBlock.getFace(dir, skip);
 				BlockLineIterator line = blocksInLine(start, dir);
 				skipEmpty(line);
 				found = line.nextBlock; //start.getFace(dir, countEmpty(start, dir));
 			}
 			else
 			{
 				found = signBlock;
 			}
 			
 			if (found.getType().equals(Material.SIGN_POST) || found.getType().equals(Material.WALL_SIGN))
 			{
 				signLocs[i] = found.getLocation();
 			}
 			else
 			{
 				log.info("["+getDescription().getName()+"]" + "Bad block: " + found.getType().toString());
 				return false;
 			}
 
 			signLines[i] = Integer.parseInt(m.group(2 + 2 * i)) - 1;
 		}
 
 
 		// final String lineOp = parts[1];
 		final Location[] rsignLocs = signLocs;
 		final int[] rsignLines = signLines;
 
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
 		{
 
 			@Override
 			public void run()
 			{
 				CraftSign sign0 = ((CraftSign) rsignLocs[1].getBlock().getState());
 				CraftSign sign1 = ((CraftSign) rsignLocs[1].getBlock().getState());
 				if (action.equals("copy"))
 				{
 					sign1.setLine(rsignLines[1], sign0.getLine(rsignLines[0]));
 
 					sign1.update();
 				}
 				else if (action.equals("swap"))
 				{
 					String line1 = sign1.getLine(rsignLines[1]);
 					sign1.setLine(rsignLines[1], sign0.getLine(rsignLines[0]));
 					sign0.setLine(rsignLines[0], line1);
 
 					sign0.update();
 					sign1.update();
 				}
 			}
 		});
 
 		return true;
 	}
 	
 	public boolean tryActivate(Block startBlock, BlockFace forward, boolean all)
 	{
 		boolean didSomething = false;
 		for (Block block : matchingBlocksInLine(startBlock, forward, signMaterials))
 		{
 			if (!tryPowerSign(block) && !all)
 			{
 				debugFail("sign fail");
 				break;
 			}
 				
 			didSomething = true;
 		}
 		debugFail("no match");
 		return didSomething;
 	}
 
 	public boolean tryActivateLong(Block startBlock, BlockFace forward)
 	{
 		BlockLineIterator line = blocksInLine(startBlock, forward);
 		skipEmpty(line);
 		if (matchesMaterials(line.currentBlock, signMaterials))
 		{
 			if (tryPowerSign(line.currentBlock))
 				return true;
 			else
 				return debugFail("sign fail");
 		}
 		return debugFail("no match");
 	}
 	
 	static final Pattern cannonBallisticsPattern = Pattern.compile("(\\d{1,3})\\s+(\\d{1,2})(?:\\s+(ns))?");
 	public boolean tryCannonSign(Block dispenserBlock, BlockFace forward, String ballisticsLine, String type)
 	{
 		int minCost = 1;
 		//Sign signState = (Sign) signBlock.getState();
 
 		// get the information about the ballistics for the TNT projectile on the second line
 		Matcher m = cannonBallisticsPattern.matcher(ballisticsLine);
 		if (!m.matches())
 			return debugFail("parse ballistics");
 		int power = Integer.parseInt(m.group(1));
 		
 		if (power > maxCannonPower)
 		{
 			//log.info("["+getDescription().getName()+"]" + "Power input exceeded - limit is " + maxCannonPower);
 			power = maxCannonPower;
 		}
 		double powerd = power / 100.0;
 		
 		double angle = Integer.parseInt(m.group(2));
 		
 		if (Math.abs(angle) > 90)
 			return debugFail("bad angle: " + Double.toString(angle));
 		
 		
 		// make sure it's a dispenser
 		if (!dispenserBlock.getType().equals(Material.DISPENSER))
 			return debugFail("not dispenser");
 		// check that the dispenser's facing isn't obstructed
 		final Block placeHere = dispenserBlock.getFace(notchToFacing(dispenserBlock.getData()));
 		if (!isEmpty(placeHere))
 			return debugFail("dispenser blocked");
 		
 		int tntCost = power / powerPerTNT;
 		
 		Material entityMaterial = null;
 		ItemStack[] neededItems;
 		if (type == null || type.equals("tnt"))
 		{
 			neededItems = new ItemStack[] {new ItemStack(Material.TNT, minCost + tntCost)};
 		}
 		else
 		{
 			entityMaterial = Material.matchMaterial(type);
 			neededItems = new ItemStack[] {new ItemStack(Material.TNT, tntCost), new ItemStack(entityMaterial, minCost)};
 		}
 		if (!tryRemoveItems(neededItems, ((CraftDispenser)dispenserBlock.getState()).getInventory()))
 			return debugFail("not enough items");
 
 		// spawn the entity
 		final CraftWorld cWorld = (CraftWorld) dispenserBlock.getWorld();
 		final Entity projectile;
 		if (type == null || type.equals("tnt"))
 		{
     		projectile = new EntityTNTPrimed(cWorld.getHandle(), placeHere.getX() + 0.5f, placeHere.getY() + 0.5f,
     		                placeHere.getZ() + 0.5f);
 		}
 		else if (type.equals("sand") || type.equals("gravel"))
 		{
 			projectile = new EntityFallingSand(cWorld.getHandle(), placeHere.getX() + 0.5f, placeHere.getY() + 0.5f,
     		                placeHere.getZ() + 0.5f, entityMaterial.getId());
 		}
 		else
 			return false; //shouldn't happen unless they try an invalid type
 		// some messy vector math for the ballistics input
 		double vector_x = powerd * Math.cos(angle * Math.PI / 180) * (placeHere.getX() - dispenserBlock.getX());
 		double vector_y = powerd * Math.sin(angle * Math.PI / 180);
 		double vector_z = powerd * Math.cos(angle * Math.PI / 180) * (placeHere.getZ() - dispenserBlock.getZ());
 		
 		if (m.group(3) == null)
 		{
     		// randomize the velocity a bit
     		vector_x += random.nextGaussian() * 0.045d * powerd;
     		vector_y += random.nextGaussian() * 0.045d * powerd;
     		vector_z += random.nextGaussian() * 0.045d * powerd;
 		}
 		
 		// adjust velocity
 		projectile.f(vector_x, vector_y, vector_z);
 		
 		// Fire!
 		cWorld.getHandle().a(projectile);
 		
 		return true;
 	}
 	
 	public boolean tryInvPush(Block invBlock, Block startBlock, BlockFace forward, Material[] materials, int amount)
 	{
 		Inventory inventory;
 		BlockState state = invBlock.getState();
 		if (state instanceof ContainerBlock)
 			inventory = ((ContainerBlock)state).getInventory();
 		else
 			return debugFail("bad inv:"+invBlock.getType().toString());
 		
 		Material putMaterial = null;
 		ItemStack[] items = inventory.getContents();
 		
 		for (Material material : materials)
 		{
 			int count = 0;
 			for (ItemStack itemStack : items)
 			{
				if (!itemStack.getType().equals(material))
 					continue;
 				count += itemStack.getAmount();
 			}
 			if (count >= amount)
 			{
 				putMaterial = material;
 				break;
 			}
 		}
 		
 		if (putMaterial == null)
 			return debugFail("not enough items");
 		
 		BlockLineIterator line = blocksInLine(startBlock, forward);
 		
 		for (int x = amount; x > 0; x--)
 		{
 			if (isEmpty(line.next()))
 				continue;
 			if (!tryPushBlocks(line.currentBlock, forward, materials, x))
 				return debugFail("couldn't push");
 			break;
 		}
 		
 		
 		line = blocksInLine(startBlock, forward);
 		
 		
 		for (int i = 0; i < items.length; i++)
 		{
 			ItemStack itemStack = items[i];
			if (!itemStack.getType().equals(putMaterial))
 				continue;
 			
 			int iAmount = itemStack.getAmount();
 			int howMany;
 			if (amount <= iAmount)
 				howMany = amount;
 			else
 				howMany = iAmount;
 			
 			amount -= howMany;
 			if (iAmount == howMany)
 			{
 				inventory.setItem(i, null);
 			}
 			else
 			{
 				itemStack.setAmount(iAmount - howMany);
 				inventory.setItem(i, itemStack);
 			}
 			
 			for (int j = 0; j < howMany; j++)
 				line.next().setTypeIdAndData(itemStack.getTypeId(), (byte)itemStack.getDurability(), true);
 		}
 		
 		
 		return true;
 	}
 	
 	public boolean tryInvPull(Block invBlock, Block startBlock, BlockFace forward, Material[] materials, int amount)
 	{
 		Inventory inventory;
 		BlockState state = invBlock.getState();
 		if (state instanceof ContainerBlock)
 			inventory = ((ContainerBlock)state).getInventory();
 		else
 			return debugFail("bad inv:"+invBlock.getType().toString());
 		
 		
 		BlockLineIterator line = blocksInLine(startBlock, forward);
 		
 		for (int i = 0; i < amount; i++)
 		{
 			if (!matchesMaterials(line.next(), materials))
 				return debugFail("not matching");
 		}
 		
 		
 		line = blocksInLine(startBlock, forward);
 		
 		
 		for (int i = 0; i < amount; i++)
 		{
 			Block block = line.next();
 			ItemStack itemStack = new ItemStack(block.getType(), 1, (short)0, (Byte)block.getData());
 			
 			if (!inventory.addItem(itemStack).isEmpty())
 				return debugFail("inv full");
 			
 			block.setTypeIdAndData(0, (byte)0, true);
 		}
 		
 		tryPullBlocks(startBlock, forward, materials, amount);
 		return true;
 	}
 	
 	public boolean tryFlingSign(Block signBlock, BlockFace signDirection, BlockFace flingDirection, boolean applyAll)
 	{
 		//get the target area
 		Block targArea = signBlock.getFace(signDirection, 1);
 		if(signBlock.getType().equals(Material.WALL_SIGN))
 			targArea = targArea.getFace(signDirection, 1);
 		
 		//check the target area is empty
 		Sign signState = (Sign)signBlock.getState();
 		String ballisticsLine = signState.getLine(1);
 		if (!isFlingable(targArea))
 			return debugFail("target blocked");
 		
 		//get the ballistics
 		final Pattern flingBallisticsPattern = Pattern.compile("(\\d{1,3})\\s+(\\d{1,2})(?:\\s+(ns))?");
 		Matcher m = flingBallisticsPattern.matcher(ballisticsLine);
 		if (!m.matches())
 			return debugFail("parse ballistics");
 		int power = Integer.parseInt(m.group(1));
 		if (power > maxFlingPower)
 			power = maxFlingPower;
 		double powerd = power / 100.0;
 		double angle = Integer.parseInt(m.group(2));
 		if (Math.abs(angle) > 90)
 			return debugFail("bad angle: " + Double.toString(angle));
 		boolean launched = false;
 		
 		//use int modifiers to fling in the right direction later
 		int x = 0, z = 0;
 		if(flingDirection == BlockFace.NORTH) x = -1;
 		else if(flingDirection == BlockFace.WEST) z = 1;
 		else if(flingDirection == BlockFace.SOUTH) x = 1;
 		else if(flingDirection == BlockFace.EAST) z = -1;
 		
 		for(org.bukkit.entity.Entity ent : ((CraftWorld)targArea.getWorld()).getEntities())
 		{
 			
 			if(((int)ent.getLocation().getX() == (int)targArea.getLocation().getX()+1
 					&& (int)ent.getLocation().getY() == (int)targArea.getLocation().getY()
 					&& (int)ent.getLocation().getZ() == (int)targArea.getLocation().getZ())
 					|| applyAll)	
 			{
 				
 				try
 				{
 					ent.setVelocity(new Vector(0, 0, 0).toBlockVector());
 					ent.setVelocity(new Vector(
 							(powerd * Math.cos(angle * Math.PI / 180) * x),
 							powerd * Math.sin(angle * Math.PI / 180),
 							(powerd * Math.cos(angle * Math.PI / 180) * z)).toBlockVector());
 					
 					launched = true;
 					((Player)ent).sendMessage("You got launched with vel: ");
 					((Player)ent).sendMessage("x="	+ (powerd * Math.cos(angle * Math.PI / 180) * x));
 					((Player)ent).sendMessage(" y=" + powerd * Math.sin(angle * Math.PI / 180));
 					((Player)ent).sendMessage(" z=" + (powerd * Math.cos(angle * Math.PI / 180) * z));
 				}
 				catch(Exception e){}
 			}
 		}
 		if(!launched)
 			return debugFail("no target");
 		return true;
 	}
 	
 	//////////////////////// Block Iterator Methods ///////////////////////////////
 
 	public static BlockLineIterator blocksInLine(Block start, BlockFace dir)
 	{
 		return new BlockLineIterator(start, dir);
 	}
 
 	public static BlockLineIterator matchingBlocksInLine(Block start, BlockFace dir, Material[] mats)
 	{
 		final Material[] materials = mats;
 		return new BlockLineIterator(start, dir)
 		{
 			@Override
 			public boolean blockMatches(Block block)
 			{
 				return matchesMaterials(block, materials);
 			}
 		};
 	}
 
 	public static BlockLineIterator emptyBlocksInLine(Block start, BlockFace dir)
 	{
 		return new BlockLineIterator(start, dir)
 		{
 			@Override
 			public boolean blockMatches(Block block)
 			{
 				return isEmpty(block);
 			}
 		};
 	}
 
 	
 	public static int countEmpty(Block startBlock, BlockFace direction)
 	{
 		return countEmpty(startBlock, direction, maxDistance);
 	}
 	public static int countEmpty(Block startBlock, BlockFace direction, int max)
 	{
 		int found = 0;
 		BlockLineIterator line = emptyBlocksInLine(startBlock, direction);
 		while(line.hasNext())
 		{
 			line.next();
 			
 			found += 1;
 			if (found >= max)
 				return -1; // Prevent searching off into oblivion
 		}
 
 		return found;
 	}
 	
 	public static int skipEmpty(BlockLineIterator line)
 	{
 		return skipEmpty(line, maxDistance);
 	}
 	public static int skipEmpty(BlockLineIterator line, int max)
 	{
 		int found = 0;
 		while(line.hasNext() && isEmpty(line.nextBlock))
 		{
 			line.next();
 			found += 1;
 			if (found >= max)
 				return -1; // Prevent searching off into oblivion
 		}
 
 		return found;
 	}
 
 	public static int skipMatching(BlockLineIterator line, Material[] materials)
 	{
 		return skipMatching(line, materials, maxDistance);
 	}
 	
 	public static int skipMatching(BlockLineIterator line, Material[] materials, int max)
 	{
 		int found = 0;
 		while(line.hasNext() && matchesMaterials(line.nextBlock, materials))
 		{
 			line.next();
 			found += 1;
 			if (found >= max)
 				return -1; // Prevent searching off into oblivion
 		}
 
 		return found;
 	}
 
 	
 
     //////////////////////// Simple Helper Methods //////////////////////// 
 	// / These are helper methods I wish were added to their respective classes
 	
 	public static String join(String... strings)
 	{
 		StringBuilder b = new StringBuilder();
 		for(String string : strings)
 			b.append(string);
 		return b.toString();
 	}
 	
 	public static String joinBy(String sep, String... strings)
 	{
 		StringBuilder b = new StringBuilder();
 		boolean first = true;
 		for(String string : strings)
 		{
 			if (first)
 			{
 				first = false;
 				b.append(string);
 				continue;
 			}
 			b.append(sep);
 			b.append(string);
 		}
 		return b.toString();
 	}
 	
 	public static boolean hasItems(ItemStack[] reqItems, Inventory inventory)
 	{
 		int[] itemCounts = new int[reqItems.length];
 		
 		for (ItemStack item : inventory.getContents())
 		{
 			for (int i = 0; i < reqItems.length; i++)
 				if (reqItems[i].getType().equals(item.getType()))
 					itemCounts[i] += item.getAmount();
 		}
 		
 		for (int i = 0; i < reqItems.length; i++)
 			if (reqItems[i].getAmount() > itemCounts[i])
     			return false;
 		return true;
 	}
 	
 	public static boolean tryRemoveItems(ItemStack[] reqItems, Inventory inventory)
 	{
 		if (!hasItems(reqItems, inventory))
     			return false;
 		inventory.removeItem(reqItems);
 		return true;
 	}
 
 	public static boolean isEmpty(Block block)
 	{
 		return (block.getType().equals(Material.AIR) || ((block.getType().equals(Material.STATIONARY_WATER) 
 				|| block.getType().equals(Material.STATIONARY_LAVA)) && block.getData() != 0));
 	}
 	
 	public static boolean isFlingable(Block block)
 	{
 		return (block.getType().equals(Material.AIR)
 				|| block.getType().equals(Material.STONE_BUTTON)
 				|| block.getType().equals(Material.RED_ROSE)
 				|| block.getType().equals(Material.YELLOW_FLOWER)
 				|| block.getType().equals(Material.LEVER)
 				|| block.getType().equals(Material.STONE_PLATE)
 				|| block.getType().equals(Material.WOOD_PLATE)
 				|| block.getType().equals(Material.STONE_BUTTON)
 				|| block.getType().equals(Material.WALL_SIGN)
 				|| block.getType().equals(Material.SIGN_POST)
 				|| block.getType().equals(Material.REDSTONE_WIRE)
 				|| block.getType().equals(Material.REDSTONE_TORCH_OFF)
 				|| block.getType().equals(Material.REDSTONE_TORCH_ON)
 				|| block.getType().equals(Material.DIODE_BLOCK_OFF)
 				|| block.getType().equals(Material.DIODE_BLOCK_ON));
 	}
 
 	public static BlockFace getSignDirection(Block signBlock)
 	{
 		if (signBlock.getType().equals(Material.WALL_SIGN))
 			return getOppositeFace(getWallSignFacing((Sign) signBlock.getState()));
 		else if (signBlock.getType().equals(Material.SIGN_POST))
 			return getOppositeFace(getSignPostFacing((Sign) signBlock.getState()));
 
 		return null; // not a sign, shouldn't be necessary
 	}
 
 	public static BlockFace getWallSignFacing(Sign signState)
 	{
 		int direction = signState.getData().getData();
 		return notchToFacing(direction);
 	}
 	
 	public static BlockFace notchToFacing(int notch)
 	{
 		if (notch == 2) return BlockFace.EAST;
 		if (notch == 3) return BlockFace.WEST;
 		if (notch == 4) return BlockFace.NORTH;
 		if (notch == 5) return BlockFace.SOUTH;
 		return null;
 	}
 
 	public static BlockFace getSignPostFacing(Sign signState)
 	{
 		int direction = signState.getData().getData();
 		if (direction == 0) return BlockFace.WEST;
 		if (direction == 4) return BlockFace.NORTH;
 		if (direction == 8) return BlockFace.EAST;
 		if (direction == 12)return BlockFace.SOUTH;
 		return null;
 	}
 
 	public static BlockFace getOppositeFace(BlockFace face)
 	{
 		switch (face)
 		{
 		case NORTH: return BlockFace.SOUTH;
 		case SOUTH: return BlockFace.NORTH;
 		case EAST: 	return BlockFace.WEST;
 		case WEST: 	return BlockFace.EAST;
 		case UP: 	return BlockFace.DOWN;
 		case DOWN: 	return BlockFace.UP;
 		default: 	return null;
 		}
 	}
 
 	public static BlockFace rotateFaceLeft(BlockFace face)
 	{
 		switch (face)
 		{
 		case NORTH: return BlockFace.WEST;
 		case SOUTH: return BlockFace.EAST;
 		case EAST: 	return BlockFace.NORTH;
 		case WEST: 	return BlockFace.SOUTH;
 		default: 	return null;
 		}
 	}
 
 	public static BlockFace rotateFaceRight(BlockFace face)
 	{
 		switch (face)
 		{
 		case NORTH:	return BlockFace.EAST;
 		case SOUTH:	return BlockFace.WEST;
 		case EAST:	return BlockFace.SOUTH;
 		case WEST:	return BlockFace.NORTH;
 		default: 	return null;
 		}
 	}
 	public static List<Player> getPlayersAtBlock(Block lookHere)
 	{
 		//get a list of players at this block
 		log.info("Searching at " + lookHere.getLocation().toString());
 		CraftWorld cWorld = (CraftWorld)lookHere.getWorld();
 		List<Player> playerList = cWorld.getPlayers();
 		for(Player player : playerList)
 		{
 			player.sendMessage("You are at location " + player.getLocation().toString());
 			if((int)player.getLocation().getX() == (int)lookHere.getLocation().getX())
 			{
 				//playerList.add(player);
 				log.info("Found target player " + player.getName()
 						+ " at " + player.getLocation());
 			}
 			else
 				playerList.remove(player);
 		}
 		return playerList;
 	}
 	
 	public static List<CraftEntity> getEntitiesAtBlock(Block lookHere)
 	{
 		//get a list of players at this block
 		CraftWorld cWorld = (CraftWorld)lookHere.getWorld();
 		List<CraftEntity> entList = Collections.emptyList();
 		for(org.bukkit.entity.Entity ent : cWorld.getEntities())	
 			if(((CraftEntity)ent).getLocation().equals(lookHere.getLocation()))
 				entList.add((CraftEntity)ent);
 
 		return entList;
 	}
 }
