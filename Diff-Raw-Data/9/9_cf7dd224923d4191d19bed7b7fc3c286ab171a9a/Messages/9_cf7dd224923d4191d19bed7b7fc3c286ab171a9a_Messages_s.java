 package com.minecraftdimensions.bungeesuite.objects;
 
 import com.minecraftdimensions.bungeesuite.configlibrary.Config;
 import net.md_5.bungee.api.ChatColor;
 
 public class Messages {
 
     static String configpath = "/plugins/BungeeSuite/Messages.yml";
     static Config m = new Config( configpath );
    public static String MOTD = colorize( ( m.getString( "MOTD", "&dWelcome to the server!" ) ) );
     public static String PLAYER_CONNECT_PROXY = colorize( m.getString( "PLAYER_CONNECT_PROXY", "{player}&e has joined the server!" ) );
     public static String PLAYER_DISCONNECT_PROXY = colorize( m.getString( "PLAYER_DISCONNECT_PROXY", "{player}&e has left the server!" ) );
     public static String PLAYER_DOES_NOT_EXIST = colorize( m.getString( "PLAYER_DOES_NOT_EXIST", "&c" + "That player does not exist" ) );
     public static String PLAYER_LOAD = colorize( m.getString( "PLAYER_LOAD", "Loaded player &9{player}" ) );
     public static String PLAYER_UNLOAD = colorize( m.getString( "PLAYER_UNLOAD", "Unloaded player &c{player}" ) );
     public static String PLAYER_NOT_ONLINE = colorize( m.getString( "PLAYER_NOT_ONLINE", "&c" + "That player is not online" ) );
     public static String NO_PERMISSION = colorize( m.getString( "NO_PERMISSION", "&c" + "You do not have permission to use that command" ) );
     // teleport specific messages
     public static String ALL_PLAYERS_TELEPORTED = colorize( m.getString( "ALL_PLAYERS_TELEPORTED", "&2" + "All players have been teleported to {player}" ) );
     public static String TELEPORTED_TO_PLAYER = colorize( m.getString( "TELEPORTED_TO_PLAYER", "&2" + "You have been teleported to {player}" ) );
     public static String PLAYER_TELEPORT_PENDING = colorize( m.getString( "PLAYER_TELEPORT_PENDING", "&c" + "You already have a teleport pending" ) );
     public static String PLAYER_TELEPORT_PENDING_OTHER = colorize( m.getString( "PLAYER_TELEPORT_PENDING_OTHER", "&c" + "That player already has a teleport pending" ) );
     public static String PLAYER_TELEPORTED_TO_YOU = colorize( m.getString( "PLAYER_TELEPORTED_TO_YOU", "&2" + "{player} has teleported to you" ) );
     public static String PLAYER_TELEPORTED = colorize( m.getString( "PLAYER_TELEPORTED", "&2" + "{player} has teleported to {target}" ) );
     public static String PLAYER_REQUESTS_TO_TELEPORT_TO_YOU = colorize( m.getString( "PLAYER_REQUESTS_TO_TELEPORT_TO_YOU", "&2" + "{player} has requested to teleport to you. Type /tpaccept to allow" ) );
     public static String PLAYER_REQUESTS_YOU_TELEPORT_TO_THEM = colorize( m.getString( "PLAYER_REQUESTS_YOU_TELEPORT_TO_THEM", "&2" + "{player} has requested you teleport to them. Type /tpaccept to allow" ) );
     public static String TELEPORT_DENIED = colorize( m.getString( "TELEPORT_DENIED", "&c" + "You denied {player}'s teleport request" ) );
     public static String TELEPORT_REQUEST_DENIED = colorize( m.getString( "TELEPORT_REQUEST_DENIED", "&c" + "{player} denied your teleport request" ) );
     public static String NO_TELEPORTS = colorize( m.getString( "NO_TELEPORTS", "&c" + "You do not have any pending teleports" ) );
     public static String TELEPORT_REQUEST_SENT = colorize( m.getString( "TELEPORT_REQUEST_SENT", "&2" + "Your request has been sent" ) );
     public static String TPA_REQUEST_TIMED_OUT = colorize( m.getString( "TPA_REQUEST_TIMED_OUT", "&c" + "Your request to teleport to {player} has timed out" ) );
     public static String TP_REQUEST_OTHER_TIMED_OUT = colorize( m.getString( "TPA_REQUEST_OTHER_TIMED_OUT", "&c" + "{player}'s teleport request has timed out" ) );
     public static String TPAHERE_REQUEST_TIMED_OUT = colorize( m.getString( "TPAHERE_REQUEST_TIMED_OUT", "&c" + "Your request to have {player} teleport to you has timed out" ) );
     public static String NO_BACK_TP = colorize( m.getString( "NO_BACK_TP", "&c" + "You do not have anywhere to go back to" ) );
     public static String SENT_BACK = colorize( m.getString( "SENT_BACK", "&2" + "You have been sent back" ) );
     public static String TELEPORT_TOGGLE_ON = colorize( m.getString( "TELEPORT_TOGGLE_ON", "&2" + "Telports have been toggled on" ) );
     public static String TELEPORT_TOGGLE_OFF = colorize( m.getString( "TELEPORT_TOGGLE_OFF", "&c" + "Telports have been toggled off" ) );
     public static String TELEPORT_UNABLE = colorize( m.getString( "TELEPORT_UNABLE", "&c" + "You are unable to teleport to this player" ) );
     // warp specific messages
     public static String WARP_CREATED = colorize( m.getString( "WARP_CREATED", "&2" + "Successfully created a warp" ) );
     public static String WARP_UPDATED = colorize( m.getString( "WARP_UPDATED", "&2" + "Successfully updated the warp" ) );
     public static String WARP_DELETED = colorize( m.getString( "WARP_DELETED", "&2" + "Successfully deleted the warp" ) );
     public static String PLAYER_WARPED = colorize( m.getString( "PLAYER_WARPED", "&7" + "You have been warped to {warp}" ) );
     public static String PLAYER_WARPED_OTHER = colorize( m.getString( "PLAYER_WARPED_OTHER", "&7" + "{player} has been warped to {warp}" ) );
     public static String WARP_DOES_NOT_EXIST = colorize( m.getString( "WARP_DOES_NOT_EXIST", "&c" + "That warp does not exist" ) );
     public static String WARP_NO_PERMISSION = colorize( m.getString( "WARP_NO_PERMISSION", "&c" + "You do not have permission to use that warp" ) );
     public static String WARP_SERVER = colorize( m.getString( "WARP_SERVER", "&c" + "Warp not on the same server" ) );
 
     // portal specific messages
     public static String PORTAL_NO_PERMISSION = colorize( m.getString( "PORTAL_NO_PERMISSION", "&c" + "You do not have permission to enter this portal" ) );
     public static String PORTAL_CREATED = colorize( m.getString( "PORTAL_CREATED", "&2" + "You have successfully created a portal" ) );
     public static String PORTAL_UPDATED = colorize( m.getString( "PORTAL_UPDATED", "&2" + "You have successfully updated the portal" ) );
     public static String PORTAL_DELETED = colorize( m.getString( "PORTAL_DELETED", "&c" + "Portal deleted" ) );
     public static String PORTAL_FILLTYPE = colorize( m.getString( "PORTAL_FILLTYPE", "&c" + "That filltype does not exist" ) );
     public static String PORTAL_DESTINATION_NOT_EXIST = colorize( m.getString( "PORTAL_DESTINATION_NOT_EXIST", "&c" + "That portal destination does not exist" ) );
     public static String PORTAL_DOES_NOT_EXIST = colorize( m.getString( "PORTAL_DOES_NOT_EXIST", "&c" + "That portal does not exist" ) );
     public static String INVALID_PORTAL_TYPE = colorize( m.getString( "INVALID_PORTAL_TYPE", "&c" + "That is an invalid portal type. Use warp or server" ) );
     public static String NO_SELECTION_MADE = colorize( m.getString( "NO_SELECTION_MADE", "&c" + "No world edit selection has been made" ) );
     // Spawn messages
     public static String SPAWN_DOES_NOT_EXIST = colorize( m.getString( "SPAWN_DOES_NOT_EXIST", "&c" + "The spawn point has not been set yet" ) );
     public static String SPAWN_UPDATED = colorize( m.getString( "SPAWN_UPDATED", "&2" + "Spawn point updated" ) );
     public static String SPAWN_SET = colorize( m.getString( "SPAWN_SET", "&2" + "Spawn point set" ) );
     // ban messages
     public static String KICK_PLAYER_MESSAGE = colorize( m.getString( "KICK_PLAYER_MESSAGE", "&c" + "You have been kicked for: {message}, by {sender}" ) );
     public static String KICK_PLAYER_BROADCAST = colorize( m.getString( "KICK_PLAYER_BROADCAST", "&c" + "{player} has been kicked for {message}, by {sender}!" ) );
     public static String PLAYER_ALREADY_BANNED = colorize( m.getString( "PLAYER_ALREADY_BANNED", "&c" + "That player is already banned" ) );
     public static String PLAYER_NOT_BANNED = colorize( m.getString( "PLAYER_NOT_BANNED", "&c" + "That player is not banned" ) );
     public static String IPBAN_PLAYER = colorize( m.getString( "IPBAN_PLAYER", "&c" + "You have been IP banned for: {message}, by {sender}" ) );
     public static String IPBAN_PLAYER_BROADCAST = colorize( m.getString( "IPBAN_PLAYER_BROADCAST", "&c" + "{player} has been ip banned for: {message}, by {sender}" ) );
     public static String DEFAULT_BAN_REASON = colorize( m.getString( "DEFAULT_BAN_REASON", "Breaking server rules" ) );
     public static String DEFAULT_KICK_MESSAGE = colorize( m.getString( "DEFAULT_KICK_MESSAGE", "&cBreaking server rules" ) );
     public static String BAN_PLAYER_MESSAGE = colorize( m.getString( "BAN_PLAYER_MESSAGE", "&c" + "You have been banned for: {message}, by {sender}" ) );
     public static String BAN_PLAYER_BROADCAST = colorize( m.getString( "BAN_PLAYER_BROADCAST", "&c" + "{player} has been banned for: {message}, by {sender}" ) );
     public static String TEMP_BAN_BROADCAST = colorize( m.getString( "TEMP_BAN_BROADCAST", "&c" + "{player} has been temporarily banned for {message} until {time}, by {sender}!" ) );
     public static String TEMP_BAN_MESSAGE = colorize( m.getString( "TEMP_BAN_MESSAGE", "&c" + "You have been temporarily until {time} for {message}, by {sender}!" ) );
     public static String PLAYER_UNBANNED = colorize( m.getString( "PLAYER_UNBANNED", "&2" + "{player} has been unbanned by {sender}!" ) );
     // chat
     public static String NEW_PLAYER_BROADCAST = colorize( m.getString( "NEW_PLAYER_BROADCAST", "&d Welcome {player} the newest member of the server!" ) );
     public static String CHANNEL_DEFAULT_GLOBAL = m.getString( "CHANNEL_DEFAULT_GLOBAL", "&c[{channel}]&e[{server}]{prefix}&f{player}&f{suffix}&f: &f{message}" );
     public static String CHANNEL_DEFAULT_SERVER = m.getString( "CHANNEL_DEFAULT_SERVER", "&e[{server}]{prefix}&f{player}&f{suffix}&f: &7{message}" );
     public static String CHANNEL_DEFAULT_LOCAL = m.getString( "CHANNEL_DEFAULT_LOCAL", "&9[Local]{prefix}&f{player}&f{suffix}&f: &7{message}" );
     public static String CHANNEL_DEFAULT_ADMIN = m.getString( "CHANNEL_DEFAULT_ADMIN", "&9[Admin]{player}:{message}" );
     public static String CHANNEL_DEFAULT_FACTION = m.getString( "CHANNEL_DEFAULT_FACTION", "&a{factions_roleprefix}{factions_title}{player}:&r {message}" );
     public static String CHANNEL_DEFAULT_FACTION_ALLY = m.getString( "CHANNEL_DEFAULT_FACTION_ALLY", "&d{factions_roleprefix}{factions_name}{player}:&r {message}" );
     public static String PRIVATE_MESSAGE_OTHER_PLAYER = colorize( m.getString( "PRIVATE_MESSAGE_OTHER_PLAYER", "&7" + "[" + "&3" + "me" + "&7" + "->" + "&6" + "{player}" + "&7" + "] {message}" ) );
     public static String PRIVATE_MESSAGE_RECEIVE = colorize( m.getString( "PRIVATE_MESSAGE_RECEIVE", "&7" + "[" + "&b" + "{player}" + "&7" + "->" + "&6" + "me" + "&7" + "] {message}" ) );
     public static String PRIVATE_MESSAGE_SPY = colorize( m.getString( "PRIVATE_MESSAGE_SPY", "&7" + "[" + "&b" + "{sender}" + "&7" + "->" + "&6" + "{player}" + "&7" + "] {message}" ) );
     public static String MUTE_ALL_ENABLED = colorize( m.getString( "MUTE_ALL_ENABLED", "&c" + "All players have been muted by {sender}" ) );
     public static String MUTE_ALL_DISABLED = colorize( m.getString( "MUTE_ALL_DISABLED", "&2" + "All players have been unmuted by {sender}" ) );
     public static String PLAYER_MUTED = colorize( m.getString( "PLAYER_MUTED", "&2" + "{player} has been muted" ) );
     public static String PLAYER_UNMUTED = colorize( m.getString( "PLAYER_UNMUTED", "&c" + "{player} has been unmuted" ) );
     public static String MUTED = colorize( m.getString( "MUTED", "&c" + "You have been muted" ) );
     public static String UNMUTED = colorize( m.getString( "UNMUTED", "&2" + "You have been unmuted" ) );
     public static String PLAYER_NOT_MUTE = colorize( m.getString( "PLAYER_NOT_MUTE", "&c" + "That player is not muted" ) );
     public static String NO_ONE_TO_REPLY = colorize( m.getString( "NO_ONE_TO_REPLY", "&c" + "You have no one to reply to" ) );
     public static String CHANNEL_DOES_NOT_EXIST = colorize( m.getString( "CHANNEL_DOES_NOT_EXIST", "&c" + "That channel does not exist" ) );
     public static String CHANNEL_NOT_A_MEMBER = colorize( m.getString( "CHANNEL_NOT_A_MEMBER", "&c" + "You are not allowed to join this channel" ) );
     public static String CHANNEL_TOGGLE = colorize( m.getString( "CHANNEL_TOGGLE", "&2" + "You are now talking in the channel {channel}" ) );
     public static String CHANNEL_UNTOGGLABLE = colorize( m.getString( "CHANNEL_UNTOGGLABLE", "&c" + "You are unable to toggle to the channel {channel}" ) );
     public static String FACTION_TOGGLE = colorize( m.getString( "FACTION_TOGGLE", "&e" + "Faction only chat mode" ) );
     public static String FACTION_ALLY_TOGGLE = colorize( m.getString( "FACTION_ALLY_TOGGLE", "&e" + "Ally only chat mode" ) );
     public static String FACTION_OFF_TOGGLE = colorize( m.getString( "FACTION_OFF_TOGGLE", "&e" + "Public chat mode" ) );
     public static String FACTION_NONE = colorize( m.getString( "FACTION_NONE", "&c" + "You do not have a faction" ) );
     public static String NICKNAMED_PLAYER = colorize( m.getString( "NICKNAMED_PLAYER", "&2" + "You have set {player}'s name to {name}" ) );
     public static String NICKNAME_CHANGED = colorize( m.getString( "NICKNAME_CHANGED", "&2" + "Your nickname has been change to {name}" ) );
     public static String NICKNAME_TOO_LONG = colorize( m.getString( "NICKNAME_TOO_LONG", "&c" + "That nickname is too long!" ) );
     public static String NICKNAME_TAKEN = colorize( m.getString( "NICKNAME_TAKEN", "&c" + "That nickname is already taken by a player!" ) );
     public static String NICKNAME_REMOVED_PLAYER = colorize( m.getString( "NICKNAME_REMOVED_PLAYER", "&c" + "You have removed {player}'s nickname!" ) );
     public static String NICKNAME_REMOVED = colorize( m.getString( "NICKNAME_REMOVED", "&c" + "Your nickname has been removed!" ) );
     public static String PLAYER_IGNORED = colorize( m.getString( "PLAYER_IGNORED", "&2" + "{player} has been ignored" ) );
     public static String PLAYER_UNIGNORED = colorize( m.getString( "PLAYER_UNIGNORED", "&2" + "{player} has been unignored" ) );
     public static String PLAYER_IGNORING = colorize( m.getString( "PLAYER_IGNORING", "&c" + "{player} is ignoring you" ) );
     public static String PLAYER_NOT_IGNORED = colorize( m.getString( "PLAYER_NOT_IGNORED", "&c" + "{player} is not ignored" ) );
     public static String CHATSPY_ENABLED = colorize( m.getString( "CHATSPY_ENABLED", "&2" + "ChatSpy enabled" ) );
     public static String CHATSPY_DISABLED = colorize( m.getString( "CHATSPY_DISABLED", "&c" + "ChatSpy disabled" ) );
     public static String AFK_DISPLAY = colorize( m.getString( "AFK_DISPLAY", "&5" + "[AFK]&r" ) );
     public static String PLAYER_AFK = colorize( m.getString( "PLAYER_AFK", "&6" + "{player} &6is now afk" ) );
     public static String PLAYER_NOT_AFK = colorize( m.getString( "PLAYER_NOT_AFK", "&7" + "{player} &7is no longer afk" ) );
     public static String SENT_HOME = colorize( m.getString( "SENT_HOME", "&2" + "You have been sent home" ) );
     public static String NO_HOMES_ALLOWED_SERVER = colorize( m.getString( "NO_HOMES_ALLOWED_SERVER", "&c" + "Your are not able to set anymore homes on this server" ) );
     public static String NO_HOMES_ALLOWED_GLOBAL = colorize( m.getString( "NO_HOMES_ALLOWED_GLOBAL", "&c" + "Your are not able to set anymore homes globally" ) );
     public static String NO_HOMES = colorize( m.getString( "NO_HOMES", "&c" + "You do not have any set homes" ) );
 
     public static String HOME_UPDATED = colorize( m.getString( "HOME_UPDATED", "&2" + "Your home has been updated" ) );
     public static String HOME_SET = colorize( m.getString( "HOME_SET", "&2" + "Your home has been set" ) );
     public static String HOME_DOES_NOT_EXIST = colorize( m.getString( "HOME_DOES_NOT_EXIST", "&c" + "That home does not exist" ) );
     public static String HOME_DELETED = colorize( m.getString( "HOME_DELETED", "&c" + "Your home has been deleted" ) );
 
     public static String colorize( String input ) {
         return ChatColor.translateAlternateColorCodes( '&', input );
     }
 
     public static void reloadMessages() {
         m = null;
         m = new Config( configpath );
         MOTD = colorize( ( m.getString( "MOTD", "&dWelcome to the server!" ) ) );
         PLAYER_CONNECT_PROXY = colorize( m.getString( "PLAYER_CONNECT_PROXY", "{player}&e has joined the server!" ) );
         PLAYER_DISCONNECT_PROXY = colorize( m.getString( "PLAYER_DISCONNECT_PROXY", "{player}&e has left the server!" ) );
         PLAYER_DOES_NOT_EXIST = colorize( m.getString( "PLAYER_DOES_NOT_EXIST", "&c" + "That player does not exist" ) );
         PLAYER_LOAD = colorize( m.getString( "PLAYER_LOAD", "Loaded player &9{player}" ) );
         PLAYER_UNLOAD = colorize( m.getString( "PLAYER_UNLOAD", "Unloaded player &c{player}" ) );
         PLAYER_NOT_ONLINE = colorize( m.getString( "PLAYER_NOT_ONLINE", "&c" + "That player is not online" ) );
         NO_PERMISSION = colorize( m.getString( "NO_PERMISSION", "&c" + "You do not have permission to use that command" ) );
         // teleport specific messages
         ALL_PLAYERS_TELEPORTED = colorize( m.getString( "ALL_PLAYERS_TELEPORTED", "&2" + "All players have been teleported to {player}" ) );
         TELEPORTED_TO_PLAYER = colorize( m.getString( "TELEPORTED_TO_PLAYER", "&2" + "You have been teleported to {player}" ) );
         PLAYER_TELEPORT_PENDING = colorize( m.getString( "PLAYER_TELEPORT_PENDING", "&c" + "You already have a teleport pending" ) );
         PLAYER_TELEPORT_PENDING_OTHER = colorize( m.getString( "PLAYER_TELEPORT_PENDING_OTHER", "&c" + "That player already has a teleport pending" ) );
         PLAYER_TELEPORTED_TO_YOU = colorize( m.getString( "PLAYER_TELEPORTED_TO_YOU", "&2" + "{player} has teleported to you" ) );
         PLAYER_TELEPORTED = colorize( m.getString( "PLAYER_TELEPORTED", "&2" + "{player} has teleported to {target}" ) );
         PLAYER_REQUESTS_TO_TELEPORT_TO_YOU = colorize( m.getString( "PLAYER_REQUESTS_TO_TELEPORT_TO_YOU", "&2" + "{player} has requested to teleport to you. Type /tpaccept to allow" ) );
         PLAYER_REQUESTS_YOU_TELEPORT_TO_THEM = colorize( m.getString( "PLAYER_REQUESTS_YOU_TELEPORT_TO_THEM", "&2" + "{player} has requested you teleport to them. Type /tpaccept to allow" ) );
         TELEPORT_DENIED = colorize( m.getString( "TELEPORT_DENIED", "&c" + "You denied {player}'s teleport request" ) );
         TELEPORT_REQUEST_DENIED = colorize( m.getString( "TELEPORT_REQUEST_DENIED", "&c" + "{player} denied your teleport request" ) );
         NO_TELEPORTS = colorize( m.getString( "NO_TELEPORTS", "&c" + "You do not have any pending teleports" ) );
         TELEPORT_REQUEST_SENT = colorize( m.getString( "TELEPORT_REQUEST_SENT", "&2" + "Your request has been sent" ) );
         TPA_REQUEST_TIMED_OUT = colorize( m.getString( "TPA_REQUEST_TIMED_OUT", "&c" + "Your request to teleport to {player} has timed out" ) );
         TP_REQUEST_OTHER_TIMED_OUT = colorize( m.getString( "TPA_REQUEST_OTHER_TIMED_OUT", "&c" + "{player}'s teleport request has timed out" ) );
         TPAHERE_REQUEST_TIMED_OUT = colorize( m.getString( "TPAHERE_REQUEST_TIMED_OUT", "&c" + "Your request to have {player} teleport to you has timed out" ) );
         NO_BACK_TP = colorize( m.getString( "NO_BACK_TP", "&c" + "You do not have anywhere to go back to" ) );
         SENT_BACK = colorize( m.getString( "SENT_BACK", "&2" + "You have been sent back" ) );
         TELEPORT_TOGGLE_ON = colorize( m.getString( "TELEPORT_TOGGLE_ON", "&2" + "Telports have been toggled on" ) );
         TELEPORT_TOGGLE_OFF = colorize( m.getString( "TELEPORT_TOGGLE_OFF", "&c" + "Telports have been toggled off" ) );
         TELEPORT_UNABLE = colorize( m.getString( "TELEPORT_UNABLE", "&c" + "You are unable to teleport to this player" ) );
         // warp specific messages
         WARP_CREATED = colorize( m.getString( "WARP_CREATED", "&2" + "Successfully created a warp" ) );
         WARP_UPDATED = colorize( m.getString( "WARP_UPDATED", "&2" + "Successfully updated the warp" ) );
         WARP_DELETED = colorize( m.getString( "WARP_DELETED", "&2" + "Successfully deleted the warp" ) );
         PLAYER_WARPED = colorize( m.getString( "PLAYER_WARPED", "&7" + "You have been warped to {warp}" ) );
         PLAYER_WARPED_OTHER = colorize( m.getString( "PLAYER_WARPED_OTHER", "&7" + "{player} has been warped to {warp}" ) );
         WARP_DOES_NOT_EXIST = colorize( m.getString( "WARP_DOES_NOT_EXIST", "&c" + "That warp does not exist" ) );
         WARP_NO_PERMISSION = colorize( m.getString( "WARP_NO_PERMISSION", "&c" + "You do not have permission to use that warp" ) );
         WARP_SERVER = colorize( m.getString( "WARP_SERVER", "&c" + "Warp not on the same server" ) );
 
         // portal specific messages
         PORTAL_NO_PERMISSION = colorize( m.getString( "PORTAL_NO_PERMISSION", "&c" + "You do not have permission to enter this portal" ) );
         PORTAL_CREATED = colorize( m.getString( "PORTAL_CREATED", "&2" + "You have successfully created a portal" ) );
         PORTAL_UPDATED = colorize( m.getString( "PORTAL_UPDATED", "&2" + "You have successfully updated the portal" ) );
         PORTAL_DELETED = colorize( m.getString( "PORTAL_DELETED", "&c" + "Portal deleted" ) );
         PORTAL_FILLTYPE = colorize( m.getString( "PORTAL_FILLTYPE", "&c" + "That filltype does not exist" ) );
         PORTAL_DESTINATION_NOT_EXIST = colorize( m.getString( "PORTAL_DESTINATION_NOT_EXIST", "&c" + "That portal destination does not exist" ) );
         PORTAL_DOES_NOT_EXIST = colorize( m.getString( "PORTAL_DOES_NOT_EXIST", "&c" + "That portal does not exist" ) );
         INVALID_PORTAL_TYPE = colorize( m.getString( "INVALID_PORTAL_TYPE", "&c" + "That is an invalid portal type. Use warp or server" ) );
         NO_SELECTION_MADE = colorize( m.getString( "NO_SELECTION_MADE", "&c" + "No world edit selection has been made" ) );
         // Spawn messages
         SPAWN_DOES_NOT_EXIST = colorize( m.getString( "SPAWN_DOES_NOT_EXIST", "&c" + "The spawn point has not been set yet" ) );
         SPAWN_UPDATED = colorize( m.getString( "SPAWN_UPDATED", "&2" + "Spawn point updated" ) );
         SPAWN_SET = colorize( m.getString( "SPAWN_SET", "&2" + "Spawn point set" ) );
         // ban messages
         KICK_PLAYER_MESSAGE = colorize( m.getString( "KICK_PLAYER_MESSAGE", "&c" + "You have been kicked for: {message}, by {sender}" ) );
         KICK_PLAYER_BROADCAST = colorize( m.getString( "KICK_PLAYER_BROADCAST", "&c" + "{player} has been kicked for {message}, by {sender}!" ) );
         PLAYER_ALREADY_BANNED = colorize( m.getString( "PLAYER_ALREADY_BANNED", "&c" + "That player is already banned" ) );
         PLAYER_NOT_BANNED = colorize( m.getString( "PLAYER_NOT_BANNED", "&c" + "That player is not banned" ) );
         IPBAN_PLAYER = colorize( m.getString( "IPBAN_PLAYER", "&c" + "You have been IP banned for: {message}, by {sender}" ) );
         IPBAN_PLAYER_BROADCAST = colorize( m.getString( "IPBAN_PLAYER_BROADCAST", "&c" + "{player} has been ip banned for: {message}, by {sender}" ) );
         DEFAULT_BAN_REASON = colorize( m.getString( "DEFAULT_BAN_REASON", "Breaking server rules" ) );
         DEFAULT_KICK_MESSAGE = colorize( m.getString( "DEFAULT_KICK_MESSAGE", "&cBreaking server rules" ) );
         BAN_PLAYER_MESSAGE = colorize( m.getString( "BAN_PLAYER_MESSAGE", "&c" + "You have been banned for: {message}, by {sender}" ) );
         BAN_PLAYER_BROADCAST = colorize( m.getString( "BAN_PLAYER_BROADCAST", "&c" + "{player} has been banned for: {message}, by {sender}" ) );
         TEMP_BAN_BROADCAST = colorize( m.getString( "TEMP_BAN_BROADCAST", "&c" + "{player} has been temporarily banned for {message} until {time}, by {sender}!" ) );
         TEMP_BAN_MESSAGE = colorize( m.getString( "TEMP_BAN_MESSAGE", "&c" + "You have been temporarily until {time} for {message}, by {sender}!" ) );
         PLAYER_UNBANNED = colorize( m.getString( "PLAYER_UNBANNED", "&2" + "{player} has been unbanned by {sender}!" ) );
         // chat
         NEW_PLAYER_BROADCAST = colorize( m.getString( "NEW_PLAYER_BROADCAST", "&d Welcome {player} the newest member of the server!" ) );
         CHANNEL_DEFAULT_GLOBAL = m.getString( "CHANNEL_DEFAULT_GLOBAL", "&c[{channel}]&e[{server}]{prefix}&f{player}&f{suffix}&f: &f{message}" );
         CHANNEL_DEFAULT_SERVER = m.getString( "CHANNEL_DEFAULT_SERVER", "&e[{server}]{prefix}&f{player}&f{suffix}&f: &7{message}" );
         CHANNEL_DEFAULT_LOCAL = m.getString( "CHANNEL_DEFAULT_LOCAL", "&9[Local]{prefix}&f{player}&f{suffix}&f: &7{message}" );
         CHANNEL_DEFAULT_ADMIN = m.getString( "CHANNEL_DEFAULT_ADMIN", "&9[Admin]{player}:{message}" );
         CHANNEL_DEFAULT_FACTION = m.getString( "CHANNEL_DEFAULT_FACTION", "&a{factions_roleprefix}{factions_title}{player}:&r {message}" );
         CHANNEL_DEFAULT_FACTION_ALLY = m.getString( "CHANNEL_DEFAULT_FACTION_ALLY", "&d{factions_roleprefix}{factions_name}{player}:&r {message}" );
         PRIVATE_MESSAGE_OTHER_PLAYER = colorize( m.getString( "PRIVATE_MESSAGE_OTHER_PLAYER", "&7" + "[" + "&3" + "me" + "&7" + "->" + "&6" + "{player}" + "&7" + "] {message}" ) );
         PRIVATE_MESSAGE_RECEIVE = colorize( m.getString( "PRIVATE_MESSAGE_RECEIVE", "&7" + "[" + "&b" + "{player}" + "&7" + "->" + "&6" + "me" + "&7" + "] {message}" ) );
         PRIVATE_MESSAGE_SPY = colorize( m.getString( "PRIVATE_MESSAGE_SPY", "&7" + "[" + "&b" + "{sender}" + "&7" + "->" + "&6" + "{player}" + "&7" + "] {message}" ) );
         MUTE_ALL_ENABLED = colorize( m.getString( "MUTE_ALL_ENABLED", "&c" + "All players have been muted by {sender}" ) );
         MUTE_ALL_DISABLED = colorize( m.getString( "MUTE_ALL_DISABLED", "&2" + "All players have been unmuted by {sender}" ) );
         PLAYER_MUTED = colorize( m.getString( "PLAYER_MUTED", "&2" + "{player} has been muted" ) );
         PLAYER_UNMUTED = colorize( m.getString( "PLAYER_UNMUTED", "&c" + "{player} has been unmuted" ) );
         MUTED = colorize( m.getString( "MUTED", "&c" + "You have been muted" ) );
         UNMUTED = colorize( m.getString( "UNMUTED", "&2" + "You have been unmuted" ) );
         PLAYER_NOT_MUTE = colorize( m.getString( "PLAYER_NOT_MUTE", "&c" + "That player is not muted" ) );
         NO_ONE_TO_REPLY = colorize( m.getString( "NO_ONE_TO_REPLY", "&c" + "You have no one to reply to" ) );
         CHANNEL_DOES_NOT_EXIST = colorize( m.getString( "CHANNEL_DOES_NOT_EXIST", "&c" + "That channel does not exist" ) );
         CHANNEL_NOT_A_MEMBER = colorize( m.getString( "CHANNEL_NOT_A_MEMBER", "&c" + "You are not allowed to join this channel" ) );
         CHANNEL_TOGGLE = colorize( m.getString( "CHANNEL_TOGGLE", "&2" + "You are now talking in the channel {channel}" ) );
         CHANNEL_UNTOGGLABLE = colorize( m.getString( "CHANNEL_UNTOGGLABLE", "&c" + "You are unable to toggle to the channel {channel}" ) );
         FACTION_TOGGLE = colorize( m.getString( "FACTION_TOGGLE", "&e" + "Faction only chat mode" ) );
         FACTION_ALLY_TOGGLE = colorize( m.getString( "FACTION_ALLY_TOGGLE", "&e" + "Ally only chat mode" ) );
         FACTION_OFF_TOGGLE = colorize( m.getString( "FACTION_OFF_TOGGLE", "&e" + "Public chat mode" ) );
         FACTION_NONE = colorize( m.getString( "FACTION_NONE", "&c" + "You do not have a faction" ) );
         NICKNAMED_PLAYER = colorize( m.getString( "NICKNAMED_PLAYER", "&2" + "You have set {player}'s name to {name}" ) );
         NICKNAME_CHANGED = colorize( m.getString( "NICKNAME_CHANGED", "&2" + "Your nickname has been change to {name}" ) );
         NICKNAME_TOO_LONG = colorize( m.getString( "NICKNAME_TOO_LONG", "&c" + "That nickname is too long!" ) );
         NICKNAME_TAKEN = colorize( m.getString( "NICKNAME_TAKEN", "&c" + "That nickname is already taken by a player!" ) );
         NICKNAME_REMOVED_PLAYER = colorize( m.getString( "NICKNAME_REMOVED_PLAYER", "&c" + "You have removed {player}'s nickname!" ) );
         NICKNAME_REMOVED = colorize( m.getString( "NICKNAME_REMOVED", "&c" + "Your nickname has been removed!" ) );
         PLAYER_IGNORED = colorize( m.getString( "PLAYER_IGNORED", "&2" + "{player} has been ignored" ) );
         PLAYER_UNIGNORED = colorize( m.getString( "PLAYER_UNIGNORED", "&2" + "{player} has been unignored" ) );
         PLAYER_IGNORING = colorize( m.getString( "PLAYER_IGNORING", "&c" + "{player} is ignoring you" ) );
         PLAYER_NOT_IGNORED = colorize( m.getString( "PLAYER_NOT_IGNORED", "&c" + "{player} is not ignored" ) );
         CHATSPY_ENABLED = colorize( m.getString( "CHATSPY_ENABLED", "&2" + "ChatSpy enabled" ) );
         CHATSPY_DISABLED = colorize( m.getString( "CHATSPY_DISABLED", "&c" + "ChatSpy disabled" ) );
         AFK_DISPLAY = colorize( m.getString( "AFK_DISPLAY", "&5" + "[AFK]&r" ) );
         PLAYER_AFK = colorize( m.getString( "PLAYER_AFK", "&6" + "{player} &6is now afk" ) );
         PLAYER_NOT_AFK = colorize( m.getString( "PLAYER_NOT_AFK", "&7" + "{player} &7is no longer afk" ) );
         SENT_HOME = colorize( m.getString( "SENT_HOME", "&2" + "You have been sent home" ) );
         NO_HOMES_ALLOWED_SERVER = colorize( m.getString( "NO_HOMES_ALLOWED_SERVER", "&c" + "Your are not able to set anymore homes on this server" ) );
         NO_HOMES_ALLOWED_GLOBAL = colorize( m.getString( "NO_HOMES_ALLOWED_GLOBAL", "&c" + "Your are not able to set anymore homes globally" ) );
         NO_HOMES = colorize( m.getString( "NO_HOMES", "&c" + "You do not have any set homes" ) );
 
         HOME_UPDATED = colorize( m.getString( "HOME_UPDATED", "&2" + "Your home has been updated" ) );
         HOME_SET = colorize( m.getString( "HOME_SET", "&2" + "Your home has been set" ) );
         HOME_DOES_NOT_EXIST = colorize( m.getString( "HOME_DOES_NOT_EXIST", "&c" + "That home does not exist" ) );
         HOME_DELETED = colorize( m.getString( "HOME_DELETED", "&c" + "Your home has been deleted" ) );
     }
 }
