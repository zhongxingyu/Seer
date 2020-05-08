 package net.t7seven7t.swornguard.listeners;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 import com.massivecraft.factions.event.FPlayerJoinEvent;
 import com.massivecraft.factions.event.FPlayerLeaveEvent;
 
 import net.t7seven7t.swornguard.SwornGuard;
 import net.t7seven7t.swornguard.types.FactionKick;
 import net.t7seven7t.swornguard.types.PlayerData;
 import net.t7seven7t.util.FormatUtil;
 import net.t7seven7t.util.TimeUtil;
 
 /**
  * @author dmulloy2
  */
 
 public class FactionsListener implements Listener
 {
 	public SwornGuard plugin;
 	private final boolean factionBetrayalDetectorEnabled;
 	
 	public FactionsListener(SwornGuard plugin)
 	{
 		this.plugin = plugin;
 		this.factionBetrayalDetectorEnabled = plugin.getConfig().getBoolean("factionBetrayalDetectorEnabled");
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerJoinFaction(FPlayerJoinEvent event)
 	{
 		if (event.isCancelled())
 			return;
 		
 		Player player = event.getFPlayer().getPlayer();
 		PlayerData data = plugin.getPlayerDataCache().getData(player);
 		data.setFactions(data.getFactions() + 1);
 		data.setLastFaction(event.getFaction().getTag());
 		
 		List<String> lines = new ArrayList<String>();
 		
 		String action = "joined";
 		if (event.getReason() == FPlayerJoinEvent.PlayerJoinReason.CREATE)
 		{
 			action = "created";
 		}
 		
 		StringBuilder line = new StringBuilder();
 		line.append(FormatUtil.format("&e[{0}] &3{1} {2} &3{3}",
 				TimeUtil.getLongDateCurr(), 
 				player.getName(), 
 				action,
 				event.getFaction().getTag()));
 		lines.add(line.toString());
 		
 		lines.addAll(data.getFactionLog());
 		data.setFactionLog(lines);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerLeaveFaction(FPlayerLeaveEvent event) 
 	{
 		if (event.isCancelled())
 			return;
 		
 		if (factionBetrayalDetectorEnabled)
 		{
 			if (event.getReason() == FPlayerLeaveEvent.PlayerLeaveReason.KICKED) 
 			{
 				plugin.getFactionBetrayaldetector().addPossibleBetrayedPlayer(event.getFPlayer().getName(), 
 						new FactionKick(event.getFaction().getTag(), System.currentTimeMillis()));
 			}
 		}
 		
 		String action = "left";
 		if (event.getReason() == FPlayerLeaveEvent.PlayerLeaveReason.KICKED)
 		{
 			action = "was kicked from";
 		}
 		
 		if (event.getReason() == FPlayerLeaveEvent.PlayerLeaveReason.DISBAND)
 		{
 			action += " (disbanded)";
 		}
 		
 		if (event.getReason() == FPlayerLeaveEvent.PlayerLeaveReason.RESET)
 		{
 			action += " (inactivity)";
 		}
 		
		OfflinePlayer player = event.getFPlayer().getPlayer();
 		PlayerData data = plugin.getPlayerDataCache().getData(player);
 		
 		List<String> lines = new ArrayList<String>();
 		
 		StringBuilder line = new StringBuilder();
 		line.append(FormatUtil.format("&e[{0}] &3{1} {2} &e{3}",
 				TimeUtil.getLongDateCurr(),
 				player.getName(),
 				action,
 				event.getFaction().getTag()));
 		lines.add(line.toString());
 		
 		lines.addAll(data.getFactionLog());
 		data.setFactionLog(lines);
 	}
 }
