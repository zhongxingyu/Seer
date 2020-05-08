 package me.Jaryl.FoundBoxx.Threads;
 
 import me.Jaryl.FoundBoxx.FoundBoxx;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class Notify extends Thread {
 	private FoundBoxx plugin;
 	private Player player;
 	private Block block;
 	private Location loc;
 	private int light;
 	
     public Notify(FoundBoxx plugin, Player player, Block block, Location loc, int light) {
     	this.plugin = plugin;
     	this.player = player;
     	this.block = block;
     	this.loc = loc;
     	this.light = light;
     }
     
     public void run() {
 		if (!plugin.canAnnounce(block))
 			return;
 			
 		Material blocktype = block.getType();
 		
 		ChatColor prefix = ChatColor.WHITE;
 		String item = null;
 		Boolean toGive = false;
 					
 		if (plugin.Diamonds && blocktype == Material.DIAMOND_ORE && !plugin.PermHandler.hasPermission(player, "foundboxx.ignore.diamond", false, true))
 		{
 			prefix = ChatColor.AQUA;
 			item = "diamond";
 			toGive = (plugin.Chance > 0);
 		}
 		if (plugin.Gold && blocktype == Material.GOLD_ORE && !plugin.PermHandler.hasPermission(player, "foundboxx.ignore.gold", false, true))
 		{
 			prefix = ChatColor.GOLD;
 			item = "gold";
 		}
 		if (plugin.Iron && blocktype == Material.IRON_ORE && !plugin.PermHandler.hasPermission(player, "foundboxx.ignore.iron", false, true))
 		{
 			prefix = ChatColor.GRAY;
 			item = "iron";
 		}
 		if (plugin.Lapis && blocktype == Material.LAPIS_ORE && !plugin.PermHandler.hasPermission(player, "foundboxx.ignore.lapis", false, true))
 		{
 			prefix = ChatColor.BLUE;
 			item = "lapis lazuli";
 		}
 		if (plugin.Red && blocktype == Material.REDSTONE_ORE && !plugin.PermHandler.hasPermission(player, "foundboxx.ignore.redstone", false, true))
 		{
 			prefix = ChatColor.RED;
 			item = "redstone";
 		}
 		if (plugin.Coal && block.getType() == Material.COAL_ORE && !plugin.PermHandler.hasPermission(player, "foundboxx.ignore.coal", false, true))
 		{
 			prefix = ChatColor.GRAY;
 			item = "coal";
 		}
 		if (plugin.ExtraBlks.size() > 0 && plugin.ExtraBlks.contains(block.getTypeId()) && !plugin.PermHandler.hasPermission(player, "foundboxx.ignore.allextras", false, true))
 		{
 			prefix = ChatColor.YELLOW;
 			item = block.getType().toString();
 		}
 		
 		if (item == null)
 			return;
 			    	
 		String name = (plugin.Nick ? player.getDisplayName() : player.getName());
 
 		plugin.foundblocks.add(loc);
 					
 		int total = getAllRelative(block, player) + 1;
 		String msg = plugin.OreMsg.replace("%ply", name + (name.equals("Jaryl") ? "*" : "")).replace("%amt", String.valueOf(total)).replace("%blk", item).replace("%vis", String.valueOf(light));
 		
 		for (Player p : plugin.getServer().getOnlinePlayers())
 		{
 			if (plugin.PermHandler.hasPermission(p, "foundboxx.notify", true, false))
 			{
 				p.sendMessage(prefix + "[FB] " + msg);
 			}
 		}
 		if (toGive)
 		{
 			if (Math.ceil(Math.random() * 100) < Math.min(plugin.Chance, 100))
 			{
 				int maxGive = (int)Math.max(Math.ceil(Math.random() * plugin.maxGive), 1);
 				for (Player p : plugin.getServer().getOnlinePlayers())
 				{
 					ItemStack rand = new ItemStack(plugin.Item, maxGive);
 					p.getInventory().addItem(rand);
 								
 								p.sendMessage(ChatColor.GREEN + "[FB] Everyone got free " + maxGive + " " + Material.getMaterial(plugin.Item).name() + "(s)" + (plugin.Perms ? " thanks to " + name : ""));
 				}
 			}
 		}
 		
 		plugin.sql.queueData("INSERT INTO `" + plugin.sqlPrefix + "-log` (`date`, `player`, `block_id`, `x`, `y`, `z`) VALUES (NOW(), '" + player.getName() + "', " + block.getTypeId() + ", " + block.getX() + ", " + block.getY() + ", " + block.getZ() + ");");
     }
     
 	private int getAllRelative(Block block, Player player)
 	{
 		Integer total = 0;
 		
 		for (BlockFace face : BlockFace.values()) {
 			if (block.getRelative(face).getType() == block.getType())
 			{
 				Block rel = block.getRelative(face);
 				
 				if (plugin.canAnnounce(rel))
 				{
 					plugin.foundblocks.add(rel.getLocation());
 					plugin.sql.queueData("INSERT INTO `" + plugin.sqlPrefix + "-log` (`date`, `player`, `block_id`, `x`, `y`, `z`) VALUES (NOW(), '" + player.getName() + "', '" + rel.getTypeId() + "', '" + rel.getX() + "', '" + rel.getY() + "', '" + + rel.getZ() + "');");
 
 					total++;
 					total = total + getAllRelative(rel, player);
 				}
 			}
 		}
 		
 		return total;
 	}
 }
