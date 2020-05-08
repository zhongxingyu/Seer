 /*
  * MyResidence, Bukkit plugin for managing your towns and residences
  * Copyright (C) 2011, Michael Hohl
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package at.co.hohl.myresidence;
 
 import at.co.hohl.mcutils.collections.CachedMap;
 import at.co.hohl.myresidence.storage.Session;
 import org.bukkit.entity.Player;
 
import java.util.Map;

 /**
  * Manager for all sessions of the MyResidence plugin.
  *
  * @author Michael Hohl
  */
 public class SessionManager {
     /**
      * The time in milliseconds which should a session get stored.
      */
     public static final long SESSION_DURATION = 9000000;
 
     /**
      * Session Map used by this player.
      */
    private final Map<String, Session> sessionMap;
 
     /**
      * The MyResidence plugin which holds the SessionManager.
      */
     private final MyResidence plugin;
 
     /**
      * The Nation which is handled by the plugin.
      */
     private final Nation nation;
 
     /**
      * Creates a new Session Manager.
      */
     public SessionManager(MyResidence plugin, Nation nation) {
         sessionMap = new CachedMap<String, Session>(SESSION_DURATION);
         this.plugin = plugin;
         this.nation = nation;
     }
 
     /**
      * Returns the session for the passed player.
      *
      * @param player the player to look for the session.
      * @return the found or create session.
      */
     public Session get(Player player) {
         if (!sessionMap.containsKey(player.getName())) {
             sessionMap.put(player.getName(), new Session(plugin, nation, player));
         }
 
         return sessionMap.get(player.getName());
     }
 
     /**
      * Removes the session of the passed player.
      *
      * @param player the player who's session should be removed.
      */
     public void close(Player player) {
         sessionMap.remove(player.getName());
     }
 }
