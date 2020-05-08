 package me.ivantheforth.srotater.listeners;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import me.ivantheforth.srotater.SRotater;
 import me.ivantheforth.srotater.utils.PlanetMinecraft;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class PlayerJoin implements Listener
 {
 	
 	static SRotater sRotater;
 	public PlayerJoin(SRotater sRotater)
 	{
 		
 		PlayerJoin.sRotater = sRotater;
 		
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent e)
 	{
 		
 		String ip = PlanetMinecraft.getIp();
 		
 		e.getPlayer().sendMessage(ip);
 		
	    /*ByteArrayOutputStream b = new ByteArrayOutputStream();
 	    DataOutputStream out = new DataOutputStream(b);
 	                   
 	    try {
 	    	
 	          out.writeUTF("Connect");
 	          out.writeUTF(ip);
 	          
 	    } catch (IOException ex) {
 	    	
 	          ex.printStackTrace();
 	          
 	    }
 	    
	    e.getPlayer().sendPluginMessage(sRotater, "BungeeCord", b.toByteArray());*/
 		
 	}
 	
 }
