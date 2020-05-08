 package main.java.me.cain.mobspawn;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class Util
 {
	public static void console(String message)
 	{
 		System.out.println(message);
 	}
 	
	public static void success(Player p, String message)
 	{
 		p.sendMessage(ChatColor.GREEN + message);
 	}
 	
	public static void deny(Player p, String message)
 	{
 		p.sendMessage(ChatColor.RED + message);
 	}
 	
 }
