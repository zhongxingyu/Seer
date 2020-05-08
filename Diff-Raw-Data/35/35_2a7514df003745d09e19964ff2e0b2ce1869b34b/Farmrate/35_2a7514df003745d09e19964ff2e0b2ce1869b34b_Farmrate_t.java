 package me.Jaryl.FoundBoxx.Threads;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import me.Jaryl.FoundBoxx.FoundBoxx;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 
 public class Farmrate extends Thread {
 	private FoundBoxx plugin;
 	private ResultSet rs;
 	private String name;
 	private String days;
 	private CommandSender asker;
 	
     public Farmrate(FoundBoxx plugin, ResultSet rs, String name, String days, CommandSender asker) {
     	this.plugin = plugin;
     	this.rs = rs;
     	this.name = name;
     	this.days = days;
     	this.asker = asker;
     }
     
     public void run() {
 		int coal = 0;
 		int iron = 0;
 		int red = 0;
 		int lapis = 0;
 		int gold = 0;
 		int dias = 0;
 		HashMap<Integer, Integer> extra = new HashMap<Integer, Integer>();
 		
 		try {
 			while (rs.next())
 			{
 				int id = rs.getInt("block_id");
 				if (plugin.Diamonds && id == 56)
 				{
 					dias++;
 				}
 				if (plugin.Gold && id == 14)
 				{
 					gold++;
 				}
 				if (plugin.Iron && id == 15)
 				{
 					iron++;
 				}
 				if (plugin.Lapis && id == 21)
 				{
 					lapis++;
 				}
 				if (plugin.Red && (id == 73 || id == 74))
 				{
 					red++;
 				}
 				if (plugin.Coal && id == 16)
 				{
 					coal++;
 				}
 				
 				if (plugin.ExtraBlks.size() > 0)
 				{
 					if (plugin.ExtraBlks.contains(id))
 					{
 						extra.put(id, (extra.containsKey(id) ? extra.get(id) + 1 : 0));
 					}
 				}
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		asker.sendMessage(ChatColor.AQUA + "[FoundBoxx] Farming rates for " + name + " for the past " + days + " day(s):");
 		if (dias > 0)
 			asker.sendMessage("    Diamonds: " + (dias > (70 * Integer.parseInt(days)) ? ChatColor.RED : (dias > (50 * Integer.parseInt(days)) ? ChatColor.YELLOW : "")) + dias);
 		if (gold > 0)
 			asker.sendMessage("    Gold: " + (gold > (170 * Integer.parseInt(days)) ? ChatColor.RED : (gold > (100 * Integer.parseInt(days)) ? ChatColor.YELLOW : "")) + gold);
 		if (iron > 0)
 			asker.sendMessage("    Iron: " + (iron > (500 * Integer.parseInt(days)) ? ChatColor.RED : (iron > (350 * Integer.parseInt(days)) ? ChatColor.YELLOW : "")) + iron);
 		if (lapis > 0)
 			asker.sendMessage("    Lapis Lazuli: " + (lapis > (90 * Integer.parseInt(days)) ? ChatColor.RED : (lapis > (60 * Integer.parseInt(days)) ? ChatColor.YELLOW : "")) + lapis);
 		if (red > 0)
 			asker.sendMessage("    Red Stone: " + red);
 		if (coal > 0)
 			asker.sendMessage("    Coal: " + coal);
 		
 		if (extra.size() > 0)
 		{
 			for (int b : extra.keySet())
 			{
 				asker.sendMessage("    '" + Material.getMaterial(b).name() + "': " + extra.get(b));
 			}
 		}
		
		this.stop();
     }
 }
