 /**
  * This file is part of Orion source code
  * 
  * Copyright (C) 2012 [Gore]Clan - http://www.goreclan.net
  *
  * Orion is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Lesser Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Orion. If not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
  * 
  * @author      Daniele Pantaleone
  * @version     1.0
  * @copyright   Daniele Pantaleone, 10 February, 2013
  * @package     com.orion.console
  **/
 
 package com.orion.console;
 
 import java.util.List;
 import java.util.Map;
 
 import com.orion.command.Command;
 import com.orion.domain.Client;
 import com.orion.urt.Team;
 
 
 public interface Console {
     
     
     /**
      * Ban a client from the server using the FS Auth System
      * 
      * @author Daniele Pantaleone
      * @param  client The client to be banned
      * @param  days The number of days of the ban
      * @param  hours The number of hours of the ban
      * @param  mins The number of minutes of the ban
      * @throws UnsupportedOperationException If the RCON command is not supported by the console implementation
      **/
     public abstract void authban(Client client, int days, int hours, int mins) throws UnsupportedOperationException;
     
     
     /**
      * Ban a client from the server using the FS Auth System
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the client to be banned
      * @param  days The number of days of the ban
      * @param  hours The number of hours of the ban
      * @param  mins The number of minutes of the ban
      * @throws UnsupportedOperationException If the RCON command is not supported by the console implementation
      **/
     public abstract void authban(int slot, int days, int hours, int mins) throws UnsupportedOperationException;
     
     
     /**
      * Fetch FS Auth System informations for the specified client
      * 
      * @author Daniele Pantaleone
      * @param  client The client whose informations needs to be retrieved
      * @throws UnsupportedOperationException If the RCON command is not supported by the console implementation
      * @return Map<String, String>
      **/
     public abstract Map<String, String> authwhois(Client client) throws UnsupportedOperationException;
     
     
     /**
      * Fetch FS Auth System informations for the specified client
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the client whose informations needs to be retrieved
      * @throws UnsupportedOperationException If the RCON command is not supported by the console implementation
      * @return Map<String, String>
      **/
     public abstract Map<String, String> authwhois(int slot) throws UnsupportedOperationException;
     
     
     /**
      * Ban a player from the server permanently.
      * 
      * @author Daniele Pantaleone
      * @param  client The client to ban from the server
      **/
     public abstract void ban(Client client);
     
     
     /**
      * Ban an ip address from the server permanently.
      * 
      * @author Daniele Pantaleone
      * @param  ip The IP address to ban from the server
      **/
     public abstract void ban(String ip);
     
     
     /**
      * Write a bold message in the middle of the screen of all players.
      * The message is going to disappear in few seconds (almost 3).
      * 
      * @author Daniele Pantaleone
      * @param  message The message to be printed
      **/
     public abstract void bigtext(String message);
     
     
     /**
      * Broadcast a message in the top-left screen.
      * 
      * @author Daniele Pantaleone
      * @param  message The message to be sent
      **/
     public abstract void broadcast(String message);
     
     
     /**
      * Cycle the current map on the server
      * 
      * @author Daniele Pantaleone
      **/
     public abstract void cyclemap();
     
     /**
      * Dump user information for the specified client.
      * 
      * @author Daniele Pantaleone
      * @param  client The client you want to retrieve informations
      * @return A Map<String,String> with the dumped result or return null if the player is not connected anymore
      **/
     public abstract Map<String, String> dumpuser(Client client);
     
     
     /**
      * Dump user information for the specified player slot.
      * 
      * @author Daniele Pantaleone
      * @param  slot The player slot on which perform the dumpuser command
      * @return A Map<String,String> with the dumped result or return null if the player is not connected anymore
      **/
     public abstract Map<String, String> dumpuser(int slot);
         
     
     /**
      * Force a player in the blue team.
      * 
      * @author Daniele Pantaleone
      * @param  client The client who is going to be forced in the blue team
      **/
     public abstract void forceblue(Client client);
     
     
     /**
      * Force a player in the blue team.
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the player who is going to be forced in the blue team
      **/
     public abstract void forceblue(int slot);
     
     
     /**
      * Force a player in the free team (autojoin).
      *
      * @author Daniele Pantaleone
      * @param  client The client who is going to be forced
      **/
     public abstract void forcefree(Client client);
     
     
     /**
      * Force a player in the free team (autojoin).
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the player who is going to be forced
      **/
     public abstract void forcefree(int slot);
     
     
     /**
      * Force a player in the red team.
      * 
      * @author Daniele Pantaleone
      * @param  client The client who is going to be forced in the red team
      **/
     public abstract void forcered(Client client);
     
     
     /**
      * Force a player in the red team.
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the player who is going to be forced in the red team
      **/
     public abstract void forcered(int slot);
     
     
     /**
      * Force a player in the spectator team.
      * 
      * @author Daniele Pantaleone
      * @param  client The client who is going to be forced in the spectators team
      **/
     public abstract void forcespec(Client client);
     
     
     /**
      * Force a player in the spectator team.
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the player who is going to be forced in the spectators team
      **/
     public abstract void forcespec(int slot);
     
     
     /**
      * Force a player in the specified team.
      *
      * @author Daniele Pantaleone
      * @param  client The client who is going to be forced
      * @param  team The team where to force the player in
      **/
     public abstract void forceteam(Client client, Team team);
     
     
     /**
      * Force a player in the specified team.
      *
      * @author Daniele Pantaleone
      * @param  slot The slot of the player who is going to be forced
      * @param  team The team where to force the player in
      **/
     public abstract void forceteam(int slot, Team team);
      
     
     /**
      * Return a cvar value.
      * 
      * @author Daniele Pantaleone
      * @param  name The cvar name
      * @return The cvar value as a String
      **/
     public abstract String getCvar(String name);
     
     
     /**
      * Return a cvar value converted into the specified object class
      * 
      * @author Daniele Pantaleone
      * @param  name The cvar name
      * @param  c The class into which convert the cvar value
      * @return The cvar value parsed according to the input class given
      **/
     public abstract <E> E getCvar(String name, Class<E> c);
     
     
     /**
      * Return the current map name.
      * 
      * @author Daniele Pantaleone
      * @return The current map name 
      **/
     public abstract String getMap();
     
     
     /**
      * Return a list of available maps.
      * 
      * @author Daniele Pantaleone
      * @return A list of all the maps available on the server
      **/
     public abstract List<String> getMapList();
     
     
     /**
      * Return a <tt>List</tt> of maps matching the given search key
      * 
      * @author Daniele Pantaleone
      * @param  search The map name search <tt>String</tt>
      * @return A <tt>List</tt> of maps matching the given search key
      **/
     public abstract List<String> getMapSoundingLike(String search);
     
     
     /**
      * Return the name of the nextmap set on the server<br>
      * Will return <tt>null</tt> if the operation doesn't succeed
      * 
      * @author Daniele Pantaleone
      * @return The name of the nextmap set on the server or <tt>null</tt> if the operation doesn't succeed
      **/
     public abstract String getNextMap();
         
     
     /**
      * Return an List containing the result of the "/rcon players" command.
      * 
      * @author Daniele Pantaleone
      * @return A list containing players informations
      **/
     public abstract List<List<String>> getPlayers();
     
     
     /**
      * Return an List containing the result of the "/rcon status" command.
      * 
      * @author Daniele Pantaleone
      * @return A list containing status informations
      **/
     public abstract List<List<String>> getStatus();
     
     
     /**
      * Kick the specified client from the server.
      * 
      * @author Daniele Pantaleone
      * @param  client The client who is going to be kicked from the server
      **/
     public abstract void kick(Client client);
     
     
     /**
      * Kick the specified client from the server.
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the player who is going to be kicked from the server
      **/
     public abstract void kick(int slot);
     
     
     /**
      * Kick the specified client from the server by specifying a reason.
      * 
      * @author Daniele Pantaleone
      * @param  client The client who is going to be kicked from the server
      * @param  reason The reason why the client is going to be kicked
      **/
     public abstract void kick(Client client, String reason);
     
     
     /**
      * Kick the specified client from the server by specifying a reason.
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the player who is going to be kicked from the server
      * @param  reason The reason why the player with the specified slot is going to be kicked
      **/
     public abstract void kick(int slot, String reason);
     
     
     /**
      * Instantly kill a player.
      * 
      * @author Daniele Pantaleone
      * @param  client The client who is going to be killed
      * @throws UnsupportedOperationException If the RCON command is not supported by the console implementation
      **/
     public abstract void kill(Client client) throws UnsupportedOperationException;
     
     
     /**
      * Instantly kill a player.
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the player who is going to be killed
      * @throws UnsupportedOperationException If the RCON command is not supported by the console implementation
      **/
     public abstract void kill(int slot) throws UnsupportedOperationException;
     
     
     /**
      * Change server current map.
      * 
      * @author Daniele Pantaleone
      * @param  mapname The name of the map to load
      **/
     public abstract void map(String mapname);
     
     
     /**
      * Mute a player.
      * 
      * @author Daniele Pantaleone 
      * @param  client The client who is going to be muted
      **/
     public abstract void mute(Client client);
     
     
     /**
      * Mute a player.
      * 
      * @author Daniele Pantaleone 
      * @param  slot The slot of the player who is going to be muted
      **/
     public abstract void mute(int slot);
     
     
     /**
      * Mute a player.
      * 
      * @author Daniele Pantaleone 
      * @param  client The client who is going to be muted
      * @param  seconds The amount of seconds after which the mute will expire
      **/
     public abstract void mute(Client client, int seconds);
     
     
     /**
      * Mute a player.
      * 
      * @author Daniele Pantaleone 
      * @param  slot The slot of the player who is going to be muted
      * @param  seconds The amount of seconds after which the mute will expire
      **/
     public abstract void mute(int slot, int seconds);
     
      
     /**
      * Nuke a player.
      * 
      * @author Daniele Pantaleone
      * @param  client The client who is going to be nuked
      **/
     public abstract void nuke(Client client);
     
     
     /**
      * Nuke a player.
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the player who is going to be nuked
      **/
     public abstract void nuke(int slot);
     
     
     /**
      * Print a message to the in-game chat.
      * 
      * @author Daniele Pantaleone
      * @param  message The message to print
      **/
     public abstract void say(String message);
     
     
     /**
      * Print an in-game message with visibility regulated by the command object
      * 
      * @author Daniele Pantaleone
      * @param  command The command issued
      * @param  message The message to be printed
      **/
     public abstract void sayLoudOrPm(Command command, String message);
     
     
     /**
      * Set a cvar value.
      * 
      * @author Daniele Pantaleone
      * @param  name The name of the cvar
      * @param  value The value to assign to the cvar
      **/
     public abstract void setCvar(String name, Object value);
     
     
     /**
      * Slap a player.
      * 
      * @author Daniele Pantaleone
      * @param  client The client who is going to be slapped
      **/
     public void slap(Client client);
     
     
     /**
      * Slap a player.
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the player who is going to be slapped
      **/
     public abstract void slap(int slot);
     
     
     /**
      * Start recording a server side demo of a player.
      * 
      * @author Daniele Pantaleone
      * @param  client The client whose we want to record a demo
      **/
     public abstract void startserverdemo(Client client);
 
     
     /**
      * Start recording a server side demo of a player.
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the player whose we want to record a demo
      **/
     public abstract void startserverdemo(int slot);
     
     
     /**
      * Start recording a server side demo of all the online players.
      * 
      * @author Daniele Pantaleone
      **/
     public abstract void startserverdemo();
     
     
     /**
      * Stop recording a server side demo of a player.
      * 
      * @author Daniele Pantaleone 
      * @param  client The client whose we want to stop a demo recording
      **/
     public abstract void stopserverdemo(Client client);
     
     
     /**
      * Stop recording a server side demo of a player.
      * 
      * @author Daniele Pantaleone 
      * @param  slot The slot of the player whose we want to stop a demo recording
      **/
     public abstract void stopserverdemo(int slot);
     
     
     /**
      * Stop recording a server side demo of all the online players.
      * 
      * @author Daniele Pantaleone
      **/
     public abstract void stopserverdemo();
     
     
     /**
     * Send a provate message to a player.
      * 
      * @author Daniele Pantaleone
      * @param  client The client you want to send the message
      * @param  message The message to be sent
      **/
     public abstract void tell(Client client, String message);
     
     
     /**
     * Send a provate message to a player.
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the player you want to send the message
      * @param  message The message to be sent
      **/
     public abstract void tell(int slot, String message);
     
     
     /**
      * Unban a player from the server.
      * 
      * @author Daniele Pantaleone
      * @param  client The client we want to unban
      **/
     public abstract void unban(Client client);
     
 
     /**
      * Unban a player from the server.
      * 
      * @author Daniele Pantaleone
      * @param  ip The IP address of the player we want to unban
      **/
     public abstract void unban(String ip);
     
     
     /**
      * Write a message directly in the Urban Terror console.
      * Try to avoid the use of this command. Use instead the other optimized methods available in this class.
      * 
      * @author Daniele Pantaleone
      * @param  command The command to execute
      * @return The server response to the RCON command
      **/
     public abstract String write(String command);
      
 }
