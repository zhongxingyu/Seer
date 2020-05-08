 package me.greatman.Craftconomy.listeners;
 
import java.util.ArrayList;
import java.util.List;
 import java.util.Timer;
 
 import me.greatman.Craftconomy.SpoutHandler;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 
 public class SpoutListener implements Listener
 {
	List<Timer> timerList = new ArrayList<Timer>();
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onSpoutCraftEnable(SpoutCraftEnableEvent event)
 	{
 		long time = (1 * 60) * 1000L;
 		Timer timer = new Timer();
 		timer.schedule(new SpoutHandler(event.getPlayer()), 0, time);
		timerList.add(timer);
 	}
 	
 }
