 package com.ftwinston.Killer;
 
 import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class EventListener implements Listener
 {
     public static Killer plugin;
     
     public EventListener(Killer instance)
 	{
 		plugin = instance;
     }
     
     
     private int autoStartProcessID = -1;
     
     @EventHandler
 	public void onPlayerJoin(PlayerJoinEvent p)
     {
     	Player[] players = null;
     	
     	if ( plugin.restartDayWhenFirstPlayerJoins )
     	{
     		players = plugin.getServer().getOnlinePlayers();
     		if ( players.length == 1 )
     			plugin.getServer().getWorlds().get(0).setTime(0);
     	}
 
     	if ( plugin.autoAssignKiller )
     	{
     		if ( players == null )
     			players = plugin.getServer().getOnlinePlayers();
     		
     		if ( players.length != 1 )
     			return; // only do this when the first player joins
     		
     		plugin.autoStartProcessID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
     			long lastRun = 0;
     			public void run()
     			{
     				long time = plugin.getServer().getWorlds().get(0).getTime();    				
     				
     				if ( time < lastRun ) // time of day has gone backwards! Must be a new day!
     				{	
     					// only if we have enough players to assign a killer should we cancel this loop process. Otherwise, try again tomorrow.
     					if ( plugin.hasKillerAssigned() || plugin.assignKiller(null) )
     					{
     						plugin.getServer().getScheduler().cancelTask(autoStartProcessID);
         					plugin.autoStartProcessID = -1;
     					}
     					else
     						lastRun = time;
     				}
     				else
     					lastRun = time;
     			}
     		}, 600L, 100L); // initial wait: 30s, then check every 5s
     	}
     }
     
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent p)
     {
 		plugin.cancelAutoStart();
 		
 		// if the game is "active" then give them 30s to rejoin, otherwise consider them to be "killed" right away.
 		if ( plugin.hasKillerAssigned() )
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ForgetDisconnectedPlayer(p.getPlayer().getName()), 600);
 		else
			plugin.playerKilled(p.getPlayer());
     }
     
     class ForgetDisconnectedPlayer implements Runnable
     {
     	String name;
     	public ForgetDisconnectedPlayer(String playerName) { name = playerName; }
     	
     	public void run()
     	{
 			Player player = Bukkit.getServer().getPlayerExact(name);
 			if ( player == null || !player.isOnline() )
 			{
 				plugin.playerKilled(name);
 			}
     	}
     }
     
     
     
     @EventHandler
     public void onEntityDeath(EntityDeathEvent event)
     {
     	if (!(event instanceof PlayerDeathEvent))
     		return;
 		
 		Player player = (Player) event.getEntity();
 		if ( player == null )
 			return;
 		
 		// the only reason this is delayed is to avoid banning the player before they properly die.
 		// once observer mode is in place instead of banning, this can be a direct function call! 
 		
 		// plugin.playerKilled(player.getName());
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedDeathEffect(player.getName()), 30);		
 	}
     
     class DelayedDeathEffect implements Runnable
     {
     	String name;
     	public DelayedDeathEffect(String playerName) { name = playerName; }
     	
     	public void run()
     	{
     		plugin.playerKilled(name);
     	}
     }
 }
