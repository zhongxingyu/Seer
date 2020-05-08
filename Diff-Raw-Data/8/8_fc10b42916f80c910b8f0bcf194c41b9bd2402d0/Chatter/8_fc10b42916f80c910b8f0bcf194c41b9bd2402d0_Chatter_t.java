 package me.botsko.darmok.chatter;
 
 import java.util.HashMap;
 import java.util.List;
 
 import me.botsko.darmok.Darmok;
 import me.botsko.darmok.channels.Channel;
 import me.botsko.darmok.channels.ChannelPermissions;
 import me.botsko.darmok.exceptions.ChannelPermissionException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import com.earth2me.essentials.User;
 
 public class Chatter {
 	
 	/**
 	 * 
 	 */
 	protected Darmok plugin;
 	
 	/**
 	 * 
 	 */
 	protected HashMap<Player,Long> messageTimestamps = new HashMap<Player,Long>();
 	
 	
 	/**
 	 * 
 	 * @param plugin
 	 */
 	public Chatter( Darmok plugin ){
 		this.plugin = plugin;
 	}
 	
 	
 	/**
 	 * 
 	 * @param player
 	 * @param channel
 	 * @param msg
 	 */
 	public void send( Player player, Channel channel, String msg ){
 		
 		// Not spamming are you?
 		if( isPlayerSpamming( player ) ){
 			player.sendMessage( Darmok.messenger.playerError("Can the spam man!") );
 			return;
 		}
 		
 		try {
 			ChannelPermissions.playerCanSpeak( player, channel );
 		} catch (ChannelPermissionException e1) {
 			player.sendMessage( Darmok.messenger.playerError( e1.getMessage() ) );
 			return;
 		}
 		
 		// Muted?
 		if( isPlayerMuted( player ) ){
 			player.sendMessage( Darmok.messenger.playerError("You've been muted in this channel, sorry.") );
 			return;
 		}
 		
 		/**
 		 * Apply censors
 		 */
 		// Caps limits
 		if( plugin.getConfig().getBoolean("darmok.censors.caps.enabled") ){
 			msg = Darmok.getCensor().filterCaps( msg, plugin.getConfig().getInt("darmok.censors.caps.min-length"), plugin.getConfig().getInt("darmok.censors.caps.min-percentage") );
 		}
 //		// Fake censor
 //		if( plugin.getConfig().getBoolean("darmok.censors.fakecensor.enabled") && Darmok.getCensor().isFakeCensor( msg, plugin.getConfig().getString("darmok.censors.fakecensor.string") ) ){
 //			player.sendMessage( Darmok.messenger.playerError("Sorry but we do not allow stars instead of curse words.") );
 //			return;
 //		}
 		// Profanity
 		if( plugin.getConfig().getBoolean("darmok.censors.profanity.enabled") ){
 			if( Darmok.getCensor().containsSuspectedProfanity( msg ) ){
 				
 				player.sendMessage( Darmok.messenger.playerError("Profanity or trying to bypass the censor is not allowed. Sorry if this is a false catch.") );
 				
 //				String alert_msg = player.getName() + "'s message was blocked for profanity.";
 //				plugin.alertPlayers(alert_msg);
 //				plugin.log( player.getName()+"'s message was blocked for profanity. Original was: " + event.getMessage() );
 				
 				return;
 			} else {
 				// scan for words we censor
 				msg = Darmok.getCensor().replaceCensoredWords( msg );
 			}
 		}
 		
 		// Do they have permission to use colors?
 		if( !player.hasPermission("darmok.chatcolor") ){
 			msg = channel.stripColor( msg );
 		}
 		
 		// Format the final message
 		msg = channel.formatMessage( player, msg );
 		
 		/**
 		 * Build a list of all players we think we should be
 		 * messaging.
 		 */
 		List<Player> playersToMessage = null;
 		// If towny town context, get online residents of town
 		if( Darmok.getTowny() != null && channel.getContext() != null && channel.getContext().equals("towny-town") ){
			playersToMessage = Darmok.getTownyBridge().getPlayersInPlayerTown(player);
			System.out.println("SETTING TOWN CONTEXT" + playersToMessage.size());
 		}
 		// If towny nation context, get online residents of town
 		if( Darmok.getTowny() != null && channel.getContext() != null && channel.getContext().equals("towny-nation") ){
			playersToMessage = Darmok.getTownyBridge().getPlayersInPlayerNation(player);
 		}
 			
 		// Instead, just get all players in channel
 		if( playersToMessage == null ){
 			playersToMessage = Darmok.getPlayerRegistry().getPlayersInChannel(channel);
 		}
 		
 		
 		// Message players if in range
 		for( Player pl : playersToMessage ){
 			
 			int range = channel.getRange();
 
 			// Does range matter?
 			if( range > -1 ){
 				// if 0, check worlds match
 				if( range == 0 && !player.getWorld().equals( pl.getWorld() ) ){
 					continue;
 				}
 				// otherwise, it's a distance
 				else if( !player.getWorld().equals( pl.getWorld() ) || player.getLocation().distance( pl.getLocation() ) > range ){
 					continue;
 				}
 			}
 			
 			// Player is in range.
 			
 			// Ensure they have permission to READ
 			try {
 				ChannelPermissions.playerCanRead( player, channel );
 			} catch (ChannelPermissionException e) {
 				return;
 			}
 
 			// All checks are GO for launch
 			pl.sendMessage( msg );
 			
 		}
 		
 		// log to console
 		Bukkit.getServer().getConsoleSender().sendMessage(msg);
 		
 	}
 	
 	
 	/**
 	 * 
 	 * @param player
 	 * @return
 	 */
 	private boolean isPlayerSpamming( Player player ){
 		
 		if( !plugin.getConfig().getBoolean("darmok.spam-prevention.enabled") ){
 			return false;
 		}
 
 		int secondBetween = plugin.getConfig().getInt("darmok.spam-prevention.min-seconds-between-msg");
 		long currentTime = System.currentTimeMillis();
 		long spam = currentTime;
 
 		if ( messageTimestamps.containsKey(player) ){
 			spam = messageTimestamps.get(player);
 			messageTimestamps.remove(player);
 		} else {
 			spam -= ((secondBetween + 1)*1000);
 		}
 
 		messageTimestamps.put( player, currentTime );
 
 		if (currentTime - spam < (secondBetween*1000)){
 			return true;
 		}
 		
 		return false;
 	
 	}
 	
 
 	/**
 	 * 
 	 * @param player
 	 * @return
 	 */
 	private boolean isPlayerMuted( Player player ){
 		if( Darmok.getEssentials() != null ){
 			User user = Darmok.getEssentials().getUser(player);
 			if( user != null && user.isMuted() ){
 				return true;
 			}
 		}
 		// @todo add-per channel muting
 		return false;
 	}
 }
