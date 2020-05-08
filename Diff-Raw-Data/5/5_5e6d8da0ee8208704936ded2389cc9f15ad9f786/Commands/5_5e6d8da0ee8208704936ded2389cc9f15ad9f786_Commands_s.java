 package dsPlugin;
 
 import dsPlugin.Library.Direction;
 import dsPlugin.Library.Heading;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Commands
 {
 	public static boolean encase(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		Player culprit = null;
 		if ((sender instanceof Player))
 		{
 			culprit = (Player) sender;
 		}
 
 		if (args.length == 2 || args.length == 3)
 		{
 			Player target = (Bukkit.getServer().getPlayer(args[0]));
 			Material outerMaterial = Library.getMaterial(args[1]);
 
 			if (outerMaterial == null)
 			{
 				sender.sendMessage(args[1].toUpperCase() + " is not a valid material or material ID!");
 				return true;
 			}
 
 			// Check to make sure block is a placeable block
 			if (!outerMaterial.isBlock())
 			{
 				sender.sendMessage(args[1].toUpperCase() + " is not a placeable block!");
 				return true;
 			}
 
 			// Get Inner Block Type
 			Material innerMaterial = null;
 			if (args.length == 3)
 			{
 				innerMaterial = Library.getMaterial(args[2]);
 
 				if (innerMaterial == null)
 				{
 					sender.sendMessage(args[2].toUpperCase() + " is not a valid material or material ID!");
 					return true;
 				}
 
 				// Check to make sure block is a placeable block
 				if (!innerMaterial.isBlock())
 				{
 					sender.sendMessage(args[2].toUpperCase() + " is not a placeable block!");
 					return true;
 				}
 			}
 			else
 			{
 				innerMaterial = Material.AIR;
 			}
 
 			// Make sure user is online
 			if (target == null)
 			{
 				sender.sendMessage(args[0] + " is not online!");
 				return true;
 			}
 			else
 			{
 				try
 				{
 					// encase in block type
 					Location loc = target.getLocation();
 
 					// Adjust Starting Block
 					loc.setX(loc.getX() - 2);
 					loc.setY(loc.getY() - 2);
 					loc.setZ(loc.getZ() - 2);
 
 					String[] args2 = new String[8];
 					args2[0] = Integer.toString(loc.getBlockX());
 					args2[1] = Integer.toString(loc.getBlockY());
 					args2[2] = Integer.toString(loc.getBlockZ());
 					args2[3] = "5";
 					args2[4] = "5";
 					args2[5] = "6";
 					args2[6] = outerMaterial.name();
 					args2[7] = innerMaterial.name();
 
 					cuboid(sender, cmd, label, args2);
 
 					target.sendMessage(ChatColor.BLUE + (culprit == null ? "Server" : culprit.getDisplayName())
 							+ ChatColor.WHITE + " has encased you in " + outerMaterial.name() + " and "
 							+ innerMaterial.name());
 				}
 				catch (Exception ex)
 				{
 					sender.sendMessage("Unable to set block type as " + outerMaterial.name() + "!");
 				}
 
 				return true;
 			}
 		}
 
 		// If this hasn't happened the a value of false will be returned.
 		return false;
 	}
 
 	public static boolean square(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length == 5)
 		{
 			String[] args2 = new String[6];
 			args2[0] = args[0];
 			args2[1] = args[1];
 			args2[2] = args[2];
 			args2[3] = args[3];
 			args2[4] = args[3]; // Width same as Length
 			args2[5] = args[4];
 
 			return rectangle(sender, cmd, label, args2);
 		}
 
 		// If this hasn't happened the a value of false will be returned.
 		return false;
 	}
 
 	public static boolean cube(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length == 5 || args.length == 6)
 		{
 			String[] args2 = (args.length == 5 ? new String[7] : new String[8]);
 			args2[0] = args[0];
 			args2[1] = args[1];
 			args2[2] = args[2];
 			args2[3] = args[3];
 			args2[4] = args[3]; // Width same as Length
 			args2[5] = args[3]; // Height same as Length
 			args2[6] = args[4];
 			if (args.length == 6)
 			{
 				args2[7] = args[5];
 			}
 
 			return cuboid(sender, cmd, label, args2);
 		}
 
 		// If this hasn't happened the a value of false will be returned.
 		return false;
 	}
 
 	public static boolean rectangle(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length == 6)
 		{
 			String[] args2 = new String[7];
 			args2[0] = args[0];
 			args2[1] = args[1];
 			args2[2] = args[2];
 			args2[3] = args[3];
 			args2[4] = args[4];
 			args2[5] = "1"; // Height of 1
 			args2[6] = args[5];
 
 			return cuboid(sender, cmd, label, args2);
 		}
 
 		// If this hasn't happened the a value of false will be returned.
 		return false;
 	}
 
 	public static boolean cuboid(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length == 8 || args.length == 7)
 		{
 			// Make sure first 6 parameters can be parsed as a integer
 			int x;
 			int y;
 			int z;
 			int xLength;
 			int zLength;
 			int yLength;
 			try
 			{
 				// Try block type as integer
 				x = Integer.parseInt(args[0]);
 				y = Integer.parseInt(args[1]);
 				z = Integer.parseInt(args[2]);
 				xLength = Math.abs(Integer.parseInt(args[3]));
 				zLength = Math.abs(Integer.parseInt(args[4]));
 				yLength = Math.abs(Integer.parseInt(args[5]));
 			}
 			catch (NumberFormatException ex)
 			{
 				sender.sendMessage("Unable to parse one of the first 6 parameters as integer!");
 				return true;
 			}
 
 			// Get Outer Block Type
 			Material outerMaterial = Library.getMaterial(args[6]);
 
 			if (outerMaterial == null)
 			{
 				sender.sendMessage(args[6].toUpperCase() + " is not a valid material or material ID!");
 				return true;
 			}
 
 			// Check to make sure block is a placeable block
 			if (!outerMaterial.isBlock())
 			{
 				sender.sendMessage(args[6].toUpperCase() + " is not a placeable block!");
 				return true;
 			}
 
 			// Get Inner Block Type
 			Material innerMaterial = null;
 			if (args.length == 8)
 			{
 				innerMaterial = Library.getMaterial(args[7]);
 
 				if (innerMaterial == null)
 				{
 					sender.sendMessage(args[7].toUpperCase() + " is not a valid material or material ID!");
 					return true;
 				}
 
 				// Check to make sure block is a placeable block
 				if (!innerMaterial.isBlock())
 				{
 					sender.sendMessage(args[7].toUpperCase() + " is not a placeable block!");
 					return true;
 				}
 			}
 
 			// All parameters are good Proceed building box
 			World world = sender.getServer().getWorld(sender.getServer().getWorlds().get(0).getName());
 			if (sender instanceof Player)
 			{
 				// Get current world of player
 				Player player = (Player) sender;
 				world = player.getWorld();
 			}
 
 			Block block = world.getBlockAt(x, y, z);
 
 			for (int l = x; l < (x + xLength); l++)
 			{
 				for (int w = z; w < (z + zLength); w++)
 				{
 					for (int h = y; h < (y + yLength); h++)
 					{
 						block = world.getBlockAt(l, h, w);
 
 						// Determine if it is an inner block our outer block
 						if (l == x || l == (x + xLength - 1) || w == z || w == (z + zLength - 1) || h == y
 								|| h == (y + yLength - 1))
 						{
 							block.setType(outerMaterial);
 						}
 						else
 						{
 							if (args.length == 8)
 							{
 								block.setType(innerMaterial);
 							}
 						}
 					}
 				}
 			}
 			sender.sendMessage("Box successfully created!");
 			return true;
 		}
 
 		// If this hasn't happened the a value of false will be returned.
 		return false;
 	}
 
 	@SuppressWarnings("deprecation")
 	public static boolean pyramid(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length == 6 || args.length == 5)
 		{
 			// Make sure first 4 parameters can be parsed as a integer
 			int x;
 			int y;
 			int z;
 			int baseLength;
 			try
 			{
 				// Try block type as integer
 				x = Integer.parseInt(args[0]);
 				y = Integer.parseInt(args[1]);
 				z = Integer.parseInt(args[2]);
 				baseLength = Math.abs(Integer.parseInt(args[3]));
 			}
 			catch (NumberFormatException ex)
 			{
 				sender.sendMessage("Unable to parse one of the first 4 parameters as integer!");
 				return true;
 			}
 
 			// Get Outer Block Type
 			Material outerMaterial = Library.getMaterial(args[4]);
 
 			if (outerMaterial == null)
 			{
 				sender.sendMessage(args[4].toUpperCase() + " is not a valid material or material ID!");
 				return true;
 			}
 
 			// Check to make sure block is a placeable block
 			if (!outerMaterial.isBlock())
 			{
 				sender.sendMessage(args[4].toUpperCase() + " is not a placeable block!");
 				return true;
 			}
 
 			// Get Inner Block Type
 			Material innerMaterial = null;
 			if (args.length == 6)
 			{
 				innerMaterial = Library.getMaterial(args[5]);
 
 				if (innerMaterial == null)
 				{
 					sender.sendMessage(args[5].toUpperCase() + " is not a valid material or material ID!");
 					return true;
 				}
 
 				// Check to make sure block is a placeable block
 				if (!innerMaterial.isBlock())
 				{
 					sender.sendMessage(args[5].toUpperCase() + " is not a placeable block!");
 					return true;
 				}
 			}
 
 			// All parameters are good Proceed building box
 			World world = sender.getServer().getWorld(sender.getServer().getWorlds().get(0).getName());
 			if (sender instanceof Player)
 			{
 				// Get current world of player
 				Player player = (Player) sender;
 				world = player.getWorld();
 			}
 
 			Block block = world.getBlockAt(x, y, z);
 
 			// First iterate over height
 			for (int h = 1; h <= (int) Math.ceil((baseLength / 2.0d)); h++)
 			{
 				// iterate over length
 				for (int l = (x + h - 1); l < (x + (baseLength - (h - 1))); l++)
 				{
 					// iterate over width
 					for (int w = (z + h - 1); w < (z + (baseLength - (h - 1))); w++)
 					{
 						block = world.getBlockAt(l, h + y - 1, w);
 
 						// Determine if it is an inner block our outer block
 						if (l == (x + h - 1) || l == (x + (baseLength - (h - 1)) - 1) || w == (z + h - 1)
 								|| w == (z + (baseLength - (h - 1)) - 1) || h == 1
 								|| h == (int) Math.ceil(baseLength / 2.0d))
 						{
 							if (outerMaterial.name().contains("STAIRS"))
 							{
 								if (w == (z + h - 1)) // Orientation 1
 								{
 									block.setType(outerMaterial);
 									block.setData((byte) 2, true);
 								}
 								else if (l == (x + h - 1)) // Orientation 2
 								{
 									block.setType(outerMaterial);
 									block.setData((byte) 0, true);
 								}
 								else if (w == (z + (baseLength - (h - 1)) - 1)) // Orientation
 																				// 3
 								{
 									block.setType(outerMaterial);
 									block.setData((byte) 3, true);
 								}
 								else if (l == (x + (baseLength - (h - 1)) - 1)) // Orientation
 																				// 4
 								{
 									block.setType(outerMaterial);
 									block.setData((byte) 1, true);
 								}
 							}
 							else
 							{
 								block.setType(outerMaterial);
 							}
 						}
 						else
 						{
 							if (args.length == 6)
 							{
 								block.setType(innerMaterial);
 							}
 						}
 					}
 				}
 			}
 
 			sender.sendMessage("Pyramid successfully created!");
 			return true;
 		}
 
 		// If this hasn't happened the a value of false will be returned.
 		return false;
 	}
 
 	public static boolean circle(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length == 5)
 		{
 			String[] args2 = new String[8];
 			args2[0] = args[0]; // x
 			args2[1] = args[1]; // y
 			args2[2] = args[2]; // z
 			args2[3] = args[3]; // radius
 			args2[4] = args[4]; // outer material
 			args2[5] = args[4]; // Use same material for inside of circle
 			args2[6] = "360"; // Have maxTh be 360
 			args2[7] = "0"; // Have maxPhi be 0
 
 			return sphere(sender, cmd, label, args2);
 		}
 
 		// If this hasn't happened the a value of false will be returned.
 		return false;
 	}
 
 	public static boolean sphere(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length > 4 && args.length < 9)
 		{
 			String[] args2 = new String[args.length + 2];
 			args2[0] = args[0]; // x
 			args2[1] = args[1]; // y
 			args2[2] = args[2]; // z
 			args2[3] = args[3]; // a
 			args2[4] = args[3]; // b same as a
 			args2[5] = args[3]; // c same as a
 			args2[6] = args[4]; // Outer material
 			if (args.length == 6)
 			{
 				args2[7] = args[5];
 			}
 			if (args.length == 7)
 			{
 				args2[7] = args[5];
 				args2[8] = args[6];
 			}
 			if (args.length == 8)
 			{
 				args2[7] = args[5];
 				args2[8] = args[6];
 				args2[9] = args[7];
 			}
 
 			return ellipsoid(sender, cmd, label, args2);
 		}
 
 		// If this hasn't happened a value of false will be returned.
 		return false;
 	}
 
 	public static boolean tunnel(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length > 3)
 		{
 			// Make sure first 3 parameters can be parsed as a integer
 			int x;
 			int y;
 			int z;
 			try
 			{
 				x = Integer.parseInt(args[0]);
 				y = Integer.parseInt(args[1]);
 				z = Integer.parseInt(args[2]);
 			}
 			catch (NumberFormatException ex)
 			{
 				sender.sendMessage("Unable to parse one of the first 3 parameters as integer!");
 				return true;
 			}
 
 			// Make sure remaining parameters are in correct format
 			List<Library.Heading> headings = new ArrayList<Library.Heading>();
 			for (int i = 3; i < args.length; i++)
 			{
 				String[] vals = args[i].split("-");
 				if (vals.length != 3)
 					return false;
 
 				try
 				{
 					Heading heading = new Heading();
 					heading.set_y(Integer.parseInt(vals[0]));
 					heading.set_length(Math.abs(Integer.parseInt(vals[1])));
 					heading.set_direction(Direction.valueOf(vals[2]));
 
 					headings.add(heading);
 				}
 				catch (Exception ex)
 				{
 					return false;
 				}
 			}
 
 			// +X ==>EAST
 			// -X ==>WEST
 			// +Z ==>SOUTH
 			// -Z ==>NORTH
 
 			// All parameters are good Proceed building box
 			World world = sender.getServer().getWorld(sender.getServer().getWorlds().get(0).getName());
 			if (sender instanceof Player)
 			{
 				// Get current world of player
 				Player player = (Player) sender;
 				world = player.getWorld();
 			}
 
 			Block block = world.getBlockAt(x, y, z);
 
 			// Loop through headings
 			for (int heading = 0; heading < headings.size(); heading++)
 			{
 				// Make sure length is greater or equal to difference in height
 				if (headings.get(heading).get_length() - 1 < Math.abs(headings.get(heading).get_y() - y))
 				{
 					sender.sendMessage("Height diffential is greater than length!");
 					return true;
 				}
 
 				// Loop through length
 				for (int l = 0; l < headings.get(heading).get_length(); l++)
 				{
 					// Determine y
 					if (l > 1 && y != headings.get(heading).get_y())
 					{
 						y += (int) Math.signum(headings.get(heading).get_y() - y);
 					}
 
 					// Determine direction
 					if (headings.get(heading).get_direction() == Direction.N)
 					{
 						if (l != 0)
 							z--;
 
 						// surround location
 						for (int h = y; h <= y + 5; h++)
 						{
 							for (int w = x - 2; w <= x + 2; w++)
 							{
 								block = world.getBlockAt(w, h, z);
 
 								if (heading != 0 && (l == 0 && (w < x) || l == 1 && (w == x - 2))
 										&& headings.get(heading - 1).get_direction() == Direction.E)
 								{
 									// Do nothing
 								}
 								else if (heading != 0 && (l == 0 && (w > x) || l == 1 && (w == x + 2))
 										&& headings.get(heading - 1).get_direction() == Direction.W)
 								{
 									// Do nothing
 								}
 								else if (w == x - 2 || w == x + 2 || h == y + 5)
 								{
 									block.setType(Material.GLASS);
 								}
 								else if (h == y && (w == x - 1 || w == x + 1))
 								{
 									block.setType(Material.GLOWSTONE);
 								}
 								else if (h == y
 										&& w == x
 										&& ((heading == 0 && l == 0) || (heading == headings.size() - 1 && l == headings
 												.get(heading).get_length() - 1)))
 								{
 									block.setType(Material.DIRT);
 								}
 								else if (h == y && w == x && (heading != 0 || l != 0))
 								{
 									block.setType(Material.REDSTONE_BLOCK);
 								}
 								else
 								{
 									block.setType(Material.AIR);
 								}
 							}
 						}
 
 						// Place endcap
 						if (l == headings.get(heading).get_length() - 1)
 						{
 							for (int h = y; h <= y + 5; h++)
 							{
 								for (int w = x - 2; w <= x + 2; w++)
 								{
 									block = world.getBlockAt(w, h, z - 1);
 
 									if (w == x - 2 || w == x + 2 || h == y + 5)
 									{
 										block.setType(Material.GLASS);
 									}
 									else if (h == y && (w == x - 1 || w == x + 1 || w == x))
 									{
 										block.setType(Material.GLOWSTONE);
 									}
 									else
 									{
 										block.setType(Material.AIR);
 									}
 								}
 							}
 
 							for (int h = y; h <= y + 5; h++)
 							{
 								for (int w = x - 2; w <= x + 2; w++)
 								{
 									block = world.getBlockAt(w, h, z - 2);
 									block.setType(Material.GLASS);
 								}
 							}
 						}
 					}
 					else if (headings.get(heading).get_direction() == Direction.S)
 					{
 						if (l != 0)
 							z++;
 
 						// surround location
 						for (int h = y; h <= y + 5; h++)
 						{
 							for (int w = x - 2; w <= x + 2; w++)
 							{
 								block = world.getBlockAt(w, h, z);
 
 								if (heading != 0 && (l == 0 && (w < x) || l == 1 && (w == x - 2))
 										&& headings.get(heading - 1).get_direction() == Direction.E)
 								{
 									// Do nothing
 								}
 								else if (heading != 0 && (l == 0 && (w > x) || l == 1 && (w == x + 2))
 										&& headings.get(heading - 1).get_direction() == Direction.W)
 								{
 									// Do nothing
 								}
 								else if (w == x - 2 || w == x + 2 || h == y + 5)
 								{
 									block.setType(Material.GLASS);
 								}
 								else if (h == y && (w == x - 1 || w == x + 1))
 								{
 									block.setType(Material.GLOWSTONE);
 								}
 								else if (h == y
 										&& w == x
 										&& ((heading == 0 && l == 0) || (heading == headings.size() - 1 && l == headings
 												.get(heading).get_length() - 1)))
 								{
 									block.setType(Material.DIRT);
 								}
 								else if (h == y && w == x && (heading != 0 || l != 0))
 								{
 									block.setType(Material.REDSTONE_BLOCK);
 								}
 								else
 								{
 									block.setType(Material.AIR);
 								}
 							}
 						}
 
 						// Place endcap
 						if (l == headings.get(heading).get_length() - 1)
 						{
 							for (int h = y; h <= y + 5; h++)
 							{
 								for (int w = x - 2; w <= x + 2; w++)
 								{
 									block = world.getBlockAt(w, h, z + 1);
 
 									if (w == x - 2 || w == x + 2 || h == y + 5)
 									{
 										block.setType(Material.GLASS);
 									}
 									else if (h == y && (w == x - 1 || w == x + 1 || w == x))
 									{
 										block.setType(Material.GLOWSTONE);
 									}
 									else
 									{
 										block.setType(Material.AIR);
 									}
 								}
 							}
 
 							for (int h = y; h <= y + 5; h++)
 							{
 								for (int w = x - 2; w <= x + 2; w++)
 								{
 									block = world.getBlockAt(w, h, z + 2);
 									block.setType(Material.GLASS);
 								}
 							}
 						}
 					}
 					else if (headings.get(heading).get_direction() == Direction.E)
 					{
 						if (l != 0)
 							x++;
 
 						// surround location
 						for (int h = y; h <= y + 5; h++)
 						{
 							for (int w = z - 2; w <= z + 2; w++)
 							{
 								block = world.getBlockAt(x, h, w);
 
 								if (heading != 0 && (l == 0 && (w > z) || l == 1 && (w == z + 2))
 										&& headings.get(heading - 1).get_direction() == Direction.N)
 								{
 									// Do nothing
 								}
 								else if (heading != 0 && (l == 0 && (w < z) || l == 1 && (w == z - 2))
 										&& headings.get(heading - 1).get_direction() == Direction.S)
 								{
 									// Do nothing
 								}
 								else if (w == z - 2 || w == z + 2 || h == y + 5)
 								{
 									block.setType(Material.GLASS);
 								}
 								else if (h == y && (w == z - 1 || w == z + 1))
 								{
 									block.setType(Material.GLOWSTONE);
 								}
 								else if (h == y
 										&& w == z
 										&& ((heading == 0 && l == 0) || (heading == headings.size() - 1 && l == headings
 												.get(heading).get_length() - 1)))
 								{
 									block.setType(Material.DIRT);
 								}
 								else if (h == y && w == z && (heading != 0 || l != 0))
 								{
 									block.setType(Material.REDSTONE_BLOCK);
 								}
 								else
 								{
 									block.setType(Material.AIR);
 								}
 							}
 						}
 
 						// Place endcap
 						if (l == headings.get(heading).get_length() - 1)
 						{
 							for (int h = y; h <= y + 5; h++)
 							{
 								for (int w = z - 2; w <= z + 2; w++)
 								{
 									block = world.getBlockAt(x + 1, h, w);
 
 									if (w == z - 2 || w == z + 2 || h == y + 5)
 									{
 										block.setType(Material.GLASS);
 									}
 									else if (h == y && (w == z - 1 || w == z + 1 || w == z))
 									{
 										block.setType(Material.GLOWSTONE);
 									}
 									else
 									{
 										block.setType(Material.AIR);
 									}
 								}
 							}
 
 							for (int h = y; h <= y + 5; h++)
 							{
 								for (int w = z - 2; w <= z + 2; w++)
 								{
 									block = world.getBlockAt(x + 2, h, w);
 									block.setType(Material.GLASS);
 								}
 							}
 						}
 					}
 					else if (headings.get(heading).get_direction() == Direction.W)
 					{
 						if (l != 0)
 							x--;
 
 						// surround location
 						for (int h = y; h <= y + 5; h++)
 						{
 							for (int w = z - 2; w <= z + 2; w++)
 							{
 								block = world.getBlockAt(x, h, w);
 
 								if (heading != 0 && (l == 0 && (w > z) || l == 1 && (w == z + 2))
 										&& headings.get(heading - 1).get_direction() == Direction.N)
 								{
 									// Do nothing
 								}
 								else if (heading != 0 && (l == 0 && (w < z) || l == 1 && (w == z - 2))
 										&& headings.get(heading - 1).get_direction() == Direction.S)
 								{
 									// Do nothing
 								}
 								else if (w == z - 2 || w == z + 2 || h == y + 5)
 								{
 									block.setType(Material.GLASS);
 								}
 								else if (h == y && (w == z - 1 || w == z + 1))
 								{
 									block.setType(Material.GLOWSTONE);
 								}
 								else if (h == y
 										&& w == z
 										&& ((heading == 0 && l == 0) || (heading == headings.size() - 1 && l == headings
 												.get(heading).get_length() - 1)))
 								{
 									block.setType(Material.DIRT);
 								}
 								else if (h == y && w == z && (heading != 0 || l != 0))
 								{
 									block.setType(Material.REDSTONE_BLOCK);
 								}
 								else
 								{
 									block.setType(Material.AIR);
 								}
 							}
 						}
 
 						// Place endcap
 						if (l == headings.get(heading).get_length() - 1)
 						{
 							for (int h = y; h <= y + 5; h++)
 							{
 								for (int w = z - 2; w <= z + 2; w++)
 								{
 									block = world.getBlockAt(x - 1, h, w);
 
 									if (w == z - 2 || w == z + 2 || h == y + 5)
 									{
 										block.setType(Material.GLASS);
 									}
 									else if (h == y && (w == z - 1 || w == z + 1 || w == z))
 									{
 										block.setType(Material.GLOWSTONE);
 									}
 									else
 									{
 										block.setType(Material.AIR);
 									}
 								}
 							}
 
 							for (int h = y; h <= y + 5; h++)
 							{
 								for (int w = z - 2; w <= z + 2; w++)
 								{
 									block = world.getBlockAt(x - 2, h, w);
 									block.setType(Material.GLASS);
 								}
 							}
 						}
 					}
 
 					// Set powered Rail above
 					Block block2 = world.getBlockAt(x, y + 1, z);
 					if ((heading == 0 && l == 1)
 							|| (heading == headings.size() - 1 && l == headings.get(heading).get_length() - 2)
 							|| (heading != 0 && l == 0))
 					{
 						block2.setType(Material.RAILS);
 					}
 					else if (heading == 0 && l == 0)
 					{
 						// Drop minecart here
 						block2.setType(Material.POWERED_RAIL);
 						world.spawnEntity(block2.getLocation(), EntityType.MINECART);
 					}
 					else
 					{
 						block2.setType(Material.POWERED_RAIL);
 					}
 				}
 			}
 
 			sender.sendMessage("Tunnel successfully created!");
 			return true;
 		}
 
 		// If this hasn't happened the a value of false will be returned.
 		return false;
 	}
 
 	public static boolean cone(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length == 7 || args.length == 8)
 		{
 			// Make sure first 4 parameters can be parsed as int
 			int x;
 			int y;
 			int z;
 			int height;
 			int radius1;
 			int radius2;
 			try
 			{
 				// Try block type as integer
 				x = Integer.parseInt(args[0]);
 				y = Integer.parseInt(args[1]);
 				z = Integer.parseInt(args[2]);
				height = Math.abs(Integer.parseInt(args[3]));
 				radius1 = Math.abs(Integer.parseInt(args[4]));
 				radius2 = Math.abs(Integer.parseInt(args[5]));
 			}
 			catch (NumberFormatException ex)
 			{
 				sender.sendMessage("Unable to parse one of the parameters as integer!");
 				return true;
 			}
 
 			// Get Outer Block Material
 			Material outerMaterial = Library.getMaterial(args[6]);
 
 			if (outerMaterial == null)
 			{
 				sender.sendMessage(args[6].toUpperCase() + " is not a valid material or material ID!");
 				return true;
 			}
 
 			// Check to make sure block is a placeable block
 			if (!outerMaterial.isBlock())
 			{
 				sender.sendMessage(args[6].toUpperCase() + " is not a placeable block!");
 				return true;
 			}
 
 			// Get Inner Block Material
 			Material innerMaterial = null;
 			if (args.length > 7)
 			{
 				innerMaterial = Library.getMaterial(args[7]);
 
 				if (innerMaterial == null)
 				{
 					sender.sendMessage(args[7].toUpperCase() + " is not a valid material or material ID!");
 					return true;
 				}
 
 				// Check to make sure block is a placeable block
 				if (!innerMaterial.isBlock())
 				{
 					sender.sendMessage(args[7].toUpperCase() + " is not a placeable block!");
 					return true;
 				}
 			}
 
 			// All parameters are good Proceed building box
 			World world = sender.getServer().getWorld(sender.getServer().getWorlds().get(0).getName());
 			if (sender instanceof Player)
 			{
 				// Get current world of player
 				Player player = (Player) sender;
 				world = player.getWorld();
 			}
 
 			Block block = world.getBlockAt(x, y, z);
 
 			Location location = block.getLocation();
 
 			// First iterate height
			for (int h = y; h <= y + height; h++)
 			{
 				double maxRadius = ((double) (radius2 - radius1) / height) * (h - y) + radius1;
 				double minAngle = Library.minRequiredAngle(maxRadius);
 
 				// Next iterate th circle
 				for (double th = 0; th <= 360.0; th += minAngle)
 				{
 					// Next iterate over radius
 					for (int r = 0; r <= Math.rint(maxRadius); r++)
 					{
 						location.setX(Math.rint(x + r * Math.cos(th * Math.PI / 180)));
 						location.setY(h);
 						location.setZ(Math.rint(z + r * Math.sin(th * Math.PI / 180)));
 
 						block = world.getBlockAt(location);
 
 						if (r == Math.rint(maxRadius) || h == y || h == y + height)
 						{
 							block.setType(outerMaterial);
 						}
 						else
 						{
 							if (innerMaterial != null)
 								block.setType(innerMaterial);
 						}
 					}
 				}
 			}
 
 			sender.sendMessage("Cone successfully created!");
 			return true;
 		}
 
 		// If this hasn't happened the a value of false will be returned.
 		return false;
 	}
 
 	public static boolean cylinder(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length == 6 || args.length == 7)
 		{
 			String[] args2 = new String[args.length + 1];
 			args2[0] = args[0]; // x
 			args2[1] = args[1]; // y
 			args2[2] = args[2]; // z
 			args2[3] = args[3]; // height
 			args2[4] = args[4]; // radius1
 			args2[5] = args[4]; // radius2 = radius1
 			args2[6] = args[5]; // Outer Material
 			if (args.length == 7)
 			{
 				args2[7] = args[6]; // Inner Material
 			}
 
 			return cone(sender, cmd, label, args2);
 		}
 
 		// If this hasn't happened the a value of false will be returned.
 		return false;
 	}
 
 	public static boolean pyramid2(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		return Library.createPyramid(sender, cmd, label, args, true);
 	}
 
 	public static boolean octahedron(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length < 5)
 		{
 			return false;
 		}
 
 		boolean firstHalf = Library.createPyramid(sender, cmd, label, args, false);
 
 		// Flip the sign on the radius to draw the other half of the pyramid.
 		args[3] = Integer.toString(Integer.parseInt(args[3]) * -1);
 		boolean secondHalf = Library.createPyramid(sender, cmd, label, args, false);
 
 		return firstHalf && secondHalf;
 	}
 
 	public static boolean ellipsoid(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length > 6 && args.length < 11)
 		{
 			// Make sure first 6 parameters can be parsed as a string
 			int x;
 			int y;
 			int z;
 			int a;
 			int b;
 			int c;
 			int maxTh = 360;
 			int maxPhi = 360;
 			try
 			{
 				// Try block type as integer
 				x = Integer.parseInt(args[0]);
 				y = Integer.parseInt(args[1]);
 				z = Integer.parseInt(args[2]);
 				a = Integer.parseInt(args[3]);
 				b = Integer.parseInt(args[4]);
 				c = Integer.parseInt(args[5]);
 				if (args.length > 8)
 					maxTh = Math.abs(Integer.parseInt(args[8]));
 				if (args.length > 9)
 					maxPhi = Math.abs(Integer.parseInt(args[9]));
 			}
 			catch (NumberFormatException ex)
 			{
 				sender.sendMessage("Unable to parse one of the parameters as integer!");
 				return true;
 			}
 
 			// Get Outer Block Material
 			Material outerMaterial = Library.getMaterial(args[6]);
 
 			if (outerMaterial == null)
 			{
 				sender.sendMessage(args[6].toUpperCase() + " is not a valid material or material ID!");
 				return true;
 			}
 
 			// Check to make sure block is a placeable block
 			if (!outerMaterial.isBlock())
 			{
 				sender.sendMessage(args[6].toUpperCase() + " is not a placeable block!");
 				return true;
 			}
 
 			// Get Inner Block Material
 			Material innerMaterial = null;
 			if (args.length > 7)
 			{
 				innerMaterial = Library.getMaterial(args[7]);
 
 				if (innerMaterial == null)
 				{
 					sender.sendMessage(args[7].toUpperCase() + " is not a valid material or material ID!");
 					return true;
 				}
 
 				// Check to make sure block is a placeable block
 				if (!innerMaterial.isBlock())
 				{
 					sender.sendMessage(args[7].toUpperCase() + " is not a placeable block!");
 					return true;
 				}
 			}
 
 			// All parameters are good Proceed building box
 			World world = sender.getServer().getWorld(sender.getServer().getWorlds().get(0).getName());
 			if (sender instanceof Player)
 			{
 				// Get current world of player
 				Player player = (Player) sender;
 				world = player.getWorld();
 			}
 
 			Block block = world.getBlockAt(x, y, z);
 
 			Location location = block.getLocation();
 
 			// Figure minAngle for worst case scenario (take greatest chord length)
 			double minAngle = Library.minRequiredAngle(Math.max(Math.max(a, b), c));
 
 			// First iterate th circle
 			for (double th = 0; th <= maxTh; th += minAngle)
 			{
 				// Next iterate phi circle
 				for (double phi = 0; phi <= maxPhi; phi += minAngle)
 				{
 					// Next iterate over a chord
 					for (int aChord = 0; aChord <= a; aChord++)
 					{
 						location.setX(Math.rint(x + aChord * Math.cos(th * Math.PI / 180)
 								* Math.cos(phi * Math.PI / 180)));
 
 						// if b equals a use same loop
 						if (b == a)
 						{
 							location.setY(Math.rint(y + aChord * Math.sin(phi * Math.PI / 180)));
 
 							// Check if c equals a to use the same loop
 							if (c == a)
 							{
 								location.setZ(Math.rint(z + aChord * Math.sin(th * Math.PI / 180)
 										* Math.cos(phi * Math.PI / 180)));
 
 								block = world.getBlockAt(location);
 
 								if (aChord == a)
 								{
 									block.setType(outerMaterial);
 								}
 								else
 								{
 									block.setType(innerMaterial);
 								}
 							}
 							else
 							{
 								// Need to iterate c chord since it is not equal
 								// to a
 								for (int cChord = 0; cChord <= c; cChord++)
 								{
 									location.setZ(Math.rint(z + cChord * Math.sin(th * Math.PI / 180)
 											* Math.cos(phi * Math.PI / 180)));
 
 									block = world.getBlockAt(location);
 
 									if (aChord == a || cChord == c)
 									{
 										block.setType(outerMaterial);
 									}
 									else
 									{
 										block.setType(innerMaterial);
 									}
 								}
 							}
 						}
 						else
 						{
 							// Check if c equals a to use the same loop
 							if (c == a)
 							{
 								location.setZ(Math.rint(z + aChord * Math.sin(th * Math.PI / 180)
 										* Math.cos(phi * Math.PI / 180)));
 
 								// Next iterate over b chord
 								for (int bChord = 0; bChord <= b; bChord++)
 								{
 									location.setY(Math.rint(y + bChord * Math.sin(phi * Math.PI / 180)));
 
 									block = world.getBlockAt(location);
 
 									if (aChord == a || bChord == b)
 									{
 										block.setType(outerMaterial);
 									}
 									else
 									{
 										block.setType(innerMaterial);
 									}
 								}
 							}
 							else
 							{
 								// Next iterate over b chord
 								for (int bChord = 0; bChord <= b; bChord++)
 								{
 									location.setY(Math.rint(y + bChord * Math.sin(phi * Math.PI / 180)));
 
 									// Check if c equals b to use the same loop
 									if (c == b)
 									{
 										location.setZ(Math.rint(z + bChord * Math.sin(th * Math.PI / 180)
 												* Math.cos(phi * Math.PI / 180)));
 
 										block = world.getBlockAt(location);
 
 										if (aChord == a || bChord == c)
 										{
 											block.setType(outerMaterial);
 										}
 										else
 										{
 											block.setType(innerMaterial);
 										}
 									}
 									else
 									{
 										// Need to iterate c chord since it is
 										// not equal to b
 										for (int cChord = 0; cChord <= c; cChord++)
 										{
 											location.setZ(Math.rint(z + cChord * Math.sin(th * Math.PI / 180)
 													* Math.cos(phi * Math.PI / 180)));
 
 											block = world.getBlockAt(location);
 
 											if (aChord == a || bChord == b || cChord == c)
 											{
 												block.setType(outerMaterial);
 											}
 											else
 											{
 												block.setType(innerMaterial);
 											}
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 
 			sender.sendMessage("Ellipsoid successfully created!");
 			return true;
 		}
 
 		// If this hasn't happened the a value of false will be returned.
 		return false;
 	}
 }
