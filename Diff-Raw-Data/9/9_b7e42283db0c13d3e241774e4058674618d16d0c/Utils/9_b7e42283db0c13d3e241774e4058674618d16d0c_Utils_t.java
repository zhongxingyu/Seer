 package co.networkery.uvbeenzaned;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Timer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 
 public class Utils {
 
     private static ActionListener taskPerformer = new ActionListener()
     {
         public void actionPerformed(ActionEvent evt)
         {
         	for(String p : SnowballerListener.teamcyaninarena)
         	{
         		if(Bukkit.getPlayer(p).getLocation().getBlock().getType() != Material.AIR)
         		{
         			World w = Bukkit.getPlayer(p).getWorld();
         			int x = Bukkit.getPlayer(p).getLocation().getBlockX();
         			int y = Bukkit.getPlayer(p).getLocation().getBlockY();
         			int z = Bukkit.getPlayer(p).getLocation().getBlockZ();
         			while(Bukkit.getPlayer(p).getWorld().getBlockAt(x, y, z).getType() != Material.AIR)
         			{
         				y++;
         			}
         			Bukkit.getPlayer(p).teleport(new Location(w, x, y, z));
         		}
         	}
         	for(String p : SnowballerListener.teamlimeinarena)
         	{
         		if(Bukkit.getPlayer(p).getLocation().getBlock().getType() != Material.AIR)
         		{
         			World w = Bukkit.getPlayer(p).getWorld();
         			int x = Bukkit.getPlayer(p).getLocation().getBlockX();
         			int y = Bukkit.getPlayer(p).getLocation().getBlockY();
         			int z = Bukkit.getPlayer(p).getLocation().getBlockZ();
         			while(Bukkit.getPlayer(p).getWorld().getBlockAt(x, y, z).getType() != Material.AIR)
         			{
         				y++;
         			}
         			Bukkit.getPlayer(p).teleport(new Location(w, x, y, z));
         		}
         	}
         }
     };
     
     private static Timer unstucktimer;
 	
 	public static void checkPlayerStuck(int delay)
 	{
 		unstucktimer = new Timer(delay, taskPerformer);
		unstucktimer.setRepeats(false);
 		unstucktimer.start();
 	}
 	
 }
