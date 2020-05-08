 package net.t7seven7t.swornguard.detectors;
 
 import java.util.logging.Filter;
 import java.util.logging.LogRecord;
 
import org.bukkit.entity.Player;

 import net.t7seven7t.swornguard.SwornGuard;
 import net.t7seven7t.swornguard.events.CheatEvent;
 import net.t7seven7t.swornguard.types.CheatType;
 import net.t7seven7t.util.FormatUtil;
 import net.t7seven7t.util.Util;
 
 public class WrongMovementDetector implements Filter {
 	private final SwornGuard plugin;
 	
 	public WrongMovementDetector(SwornGuard plugin) {
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public boolean isLoggable(LogRecord record) {
 		String message = record.getMessage();
 		if (message.contains("moved wrongly!")) {
 			String playerName = message.split(" ")[0];
 			
 			Player player = Util.matchPlayer(playerName);
 			if (player != null) {
 				CheatEvent event = new CheatEvent(player.getName(), CheatType.MOVED_WRONGLY, 
						FormatUtil.format(plugin.getMessage("cheat_wrong_movement"), player.getName()));
 				plugin.getCheatHandler().announceCheat(event);
 				return false;
 			}
 		} else if (message.contains("moved too quickly!")) {
 			String playerName = message.split(" ")[0];
 			
 			Player player = Util.matchPlayer(playerName);
 			if (player != null) {
 				CheatEvent event = new CheatEvent(player.getName(), CheatType.MOVED_WRONGLY, 
						FormatUtil.format(plugin.getMessage("cheat_wrong_movement"), player.getName()));
 				plugin.getCheatHandler().announceCheat(event);
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 }
