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
 
 					box(sender, cmd, label, args2);
 
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
 
 	public static boolean box(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length == 8 || args.length == 7)
 		{
 			// Make sure first 6 parameters can be parsed as a integer
 			int x;
 			int y;
 			int z;
 			int length;
 			int width;
 			int height;
 			try
 			{
 				// Try block type as integer
 				x = Integer.parseInt(args[0]);
 				y = Integer.parseInt(args[1]);
 				z = Integer.parseInt(args[2]);
 				length = Math.abs(Integer.parseInt(args[3]));
 				width = Math.abs(Integer.parseInt(args[4]));
 				height = Math.abs(Integer.parseInt(args[5]));
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
 
 			for (int l = x; l < (x + length); l++)
 			{
 				for (int w = z; w < (z + width); w++)
 				{
 					for (int h = y; h < (y + height); h++)
 					{
 						block = world.getBlockAt(l, h, w);
 
 						// Determine if it is an inner block our outer block
 						if (l == x || l == (x + length - 1) || w == z || w == (z + width - 1) || h == y
 								|| h == (y + height - 1))
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
 								else if (w == (z + (baseLength - (h - 1)) - 1)) // Orientation 3
 								{
 									block.setType(outerMaterial);
 									block.setData((byte) 3, true);
 								}
 								else if (l == (x + (baseLength - (h - 1)) - 1)) // Orientation 4
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
 
 	public static boolean sphere(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length > 4 && args.length < 9)
 		{
 			// Make sure first 4 parameters can be parsed as a string
 			int x;
 			int y;
 			int z;
 			int radius;
 			int maxTh = 360;
 			int maxPhi = 360;
 			try
 			{
 				// Try block type as integer
 				x = Integer.parseInt(args[0]);
 				y = Integer.parseInt(args[1]);
 				z = Integer.parseInt(args[2]);
 				radius = Math.abs(Integer.parseInt(args[3]));
 				if (args.length > 6)
 					maxTh = Math.abs(Integer.parseInt(args[6]));
 				if (args.length > 7)
 					maxPhi = Math.abs(Integer.parseInt(args[7]));
 			}
 			catch (NumberFormatException ex)
 			{
 				sender.sendMessage("Unable to parse one of the parameters as integer!");
 				return true;
 			}
 
 			// Get Outer Block Material
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
 
 			// Get Inner Block Material
 			Material innerMaterial = null;
 			if (args.length > 5)
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
 
 			Location location = block.getLocation();
 
 			// First iterate th circle
 			for (int th = 0; th <= maxTh; th++)
 			{
 				// Next iterate phi circle
 				for (int phi = 0; phi <= maxPhi; phi++)
 				{
 					for (int r = ((args.length > 5) ? 0 : radius); r <= radius; r++)
 					{
 						location.setX(x + r * Math.cos(phi * Math.PI / 180) * Math.sin(th * Math.PI / 180));
 						location.setY(y + r * Math.cos(th * Math.PI / 180));
 						location.setZ(z + r * Math.sin(phi * Math.PI / 180) * Math.sin(th * Math.PI / 180));
 
 						block = world.getBlockAt(location);
 
 						if (r == radius)
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
 
 			sender.sendMessage("Sphere successfully created!");
 			return true;
 		}
 
 		// If this hasn't happened the a value of false will be returned.
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
 
 	public static boolean cylinder(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (args.length == 6 || args.length == 7)
 		{
 			// Make sure first 4 parameters can be parsed as a string
 			int x;
 			int y;
 			int z;
 			int height;
 			int radius;
 			try
 			{
 				// Try block type as integer
 				x = Integer.parseInt(args[0]);
 				y = Integer.parseInt(args[1]);
 				z = Integer.parseInt(args[2]);
 				height = Math.abs(Integer.parseInt(args[3]));
 				radius = Math.abs(Integer.parseInt(args[4]));
 			}
 			catch (NumberFormatException ex)
 			{
 				sender.sendMessage("Unable to parse one of the parameters as integer!");
 				return true;
 			}
 
 			// Get Outer Block Material
 			Material outerMaterial = Library.getMaterial(args[5]);
 
 			if (outerMaterial == null)
 			{
 				sender.sendMessage(args[5].toUpperCase() + " is not a valid material or material ID!");
 				return true;
 			}
 
 			// Check to make sure block is a placeable block
 			if (!outerMaterial.isBlock())
 			{
 				sender.sendMessage(args[5].toUpperCase() + " is not a placeable block!");
 				return true;
 			}
 
 			// Get Inner Block Material
 			Material innerMaterial = null;
 			if (args.length > 6)
 			{
 				innerMaterial = Library.getMaterial(args[6]);
 
 				if (innerMaterial == null)
 				{
 					sender.sendMessage(args[6].toUpperCase() + " is not a valid material or material ID!");
 					return true;
 				}
 
 				// Check to make sure block is a placeable block
 				if (!innerMaterial.isBlock())
 				{
 					sender.sendMessage(args[6].toUpperCase() + " is not a placeable block!");
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
 				// Next iterate th circle
 				for (int th = 0; th <= 360; th++)
 				{
 					// Next iterate over radius
 					for (int r = ((args.length > 6) ? 0 : radius); r <= radius; r++)
 					{
						location.setX(x + r * Math.cos(th * Math.PI / 180));
 						location.setY(h);
 						location.setZ(z + r * Math.sin(th * Math.PI / 180));
 
 						block = world.getBlockAt(location);
 
						if (r == radius || h == y || h == y + height)
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
 
 			sender.sendMessage("Cylinder successfully created!");
 			return true;
 		}
 
 		// If this hasn't happened the a value of false will be returned.
 		return false;
 	}
 }
