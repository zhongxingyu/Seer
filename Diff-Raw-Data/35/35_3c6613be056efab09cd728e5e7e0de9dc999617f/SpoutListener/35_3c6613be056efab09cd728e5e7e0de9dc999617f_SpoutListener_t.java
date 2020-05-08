 package me.greatman.Craftconomy.listeners;
 
import java.util.HashMap;
 import java.util.Timer;
 
 import me.greatman.Craftconomy.SpoutHandler;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 
 public class SpoutListener implements Listener
 {
	public static HashMap<String,Timer> timerList = new HashMap<String,Timer>();
	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onSpoutCraftEnable(SpoutCraftEnableEvent event)
 	{
 		long time = (1 * 60) * 1000L;
 		Timer timer = new Timer();
 		timer.schedule(new SpoutHandler(event.getPlayer()), 0, time);
		timerList.put(event.getPlayer().getName(),timer);
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (timerList.containsKey(event.getPlayer().getName()))
		{
			Timer timer = timerList.get(event.getPlayer().getName());
			timer.cancel();
		}
 	}
 	
 }
