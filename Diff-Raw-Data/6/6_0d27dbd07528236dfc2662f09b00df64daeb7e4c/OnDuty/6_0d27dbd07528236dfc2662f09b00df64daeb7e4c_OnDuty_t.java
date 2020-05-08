 /**
  * 
  */
 package org.morganm.loginlimiter;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /** Class for keeping track of "on duty" status for moderators.
  * 
  * @author morganm
  *
  */
 public class OnDuty {
 	private LoginLimiter plugin;
 	private HashSet<String> offDutyList = new HashSet<String>(3);
 	
 	public OnDuty(final LoginLimiter plugin) {
 		this.plugin = plugin;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if( "onduty".equals(command.getName()) ) {
 			offDutyList.remove(sender.getName());
 			sender.sendMessage(ChatColor.YELLOW+"You are now ON duty.");
 			
 		}
 		else if( "offduty".equals(command.getName()) ) {
 			offDutyList.add(sender.getName());
 			sender.sendMessage(ChatColor.YELLOW+"You are now OFF duty.");
 		}
 		else if( "dutylist".equals(command.getName()) ) {
 			List<Player> dutyEligibleOnline = plugin.getDutyEligiblePlayers();
 			StringBuffer sbOffDuty = new StringBuffer();
 			StringBuffer sbOnDuty = new StringBuffer();
 			
 			for(String s :offDutyList) {
 				if( sbOffDuty.length() > 0 )
 					sbOffDuty.append(", ");
 				sbOffDuty.append(s);
 				
 				// since this player is off duty, remove them from the on duty list
				for(Iterator<Player> i = dutyEligibleOnline.iterator(); i.hasNext();) {
					Player p = i.next();
 					if( offDutyList.contains(p.getName()) ) {
						i.remove();
 						break;
 					}
 				}
 			}
 			
 			// now put together the on duty list from whoever is left
 			for(Player p : dutyEligibleOnline) {
 				if( sbOnDuty.length() > 0 )
 					sbOnDuty.append(", ");
 				sbOnDuty.append(p.getName());
 			}
 			
 			if( sbOffDuty.length() == 0 )
 				sbOffDuty.append("(none)");
 			if( sbOnDuty.length() == 0 )
 				sbOnDuty.append("(none)");
 			
 			sender.sendMessage(ChatColor.YELLOW+"People currently OFF duty: "+sbOffDuty.toString());
 			sender.sendMessage(ChatColor.YELLOW+"People currently ON duty: "+sbOnDuty.toString());
 		}
 		
 		return true;
 	}
 	
 	public boolean isOffDuty(String playerName) {
 		return offDutyList.contains(playerName);
 	}
 	
 	public List<Player> getOnDutyPlayers() {
 		// first get all the online duty-eligible players
 		List<Player> onDuty = plugin.getDutyEligiblePlayers();
 		
 		// now subtract from that list anyone who is off duty
 		for(Iterator<Player> i = onDuty.iterator(); i.hasNext();) {
 			if( offDutyList.contains(i.next().getName()) )
 				i.remove();
 		}
 		
 		return onDuty;
 	}
 }
