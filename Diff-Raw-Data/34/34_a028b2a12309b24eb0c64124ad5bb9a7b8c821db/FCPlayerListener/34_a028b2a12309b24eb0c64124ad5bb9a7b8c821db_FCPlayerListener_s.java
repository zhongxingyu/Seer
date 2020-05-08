 /************************************************************************
  * This file is part of FunCommands.
  *
  * FunCommands is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ExamplePlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with FunCommands.  If not, see <http://www.gnu.org/licenses/>.
  ************************************************************************/
 
 package de.Lathanael.FC.Listeners;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import be.Balor.Player.ACPlayer;
 import be.Balor.Tools.Type;
 import be.Balor.Tools.Files.ObjectContainer;
 
 import de.Lathanael.FC.FunCommands.FCConfigEnum;
 import de.Lathanael.FC.FunCommands.FunCommands;
 import de.Lathanael.FC.Tools.Utilities;
 
 /**
  * @author Lathanael (aka Philippe Leipold)
  *
  */
 public class FCPlayerListener implements Listener {
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		String displayName = null;
 		ObjectContainer o = ACPlayer.getPlayer(player).getInformation("displayName");
 		if (o != null) {
 			displayName = o.getString();
 			if (displayName != null) {
 				player.setDisplayName(displayName);
 				if (!ACPlayer.getPlayer(player).hasPower(Type.INVISIBLE) || !ACPlayer.getPlayer(player).hasPower(Type.FAKEQUIT)) {
 					player.setPlayerListName(displayName);
					//Utilities.createNewPlayerShell(player, displayName);
 				}
 			}
 			FunCommands.players.put(displayName, player);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerKick(PlayerKickEvent event) {
 		FunCommands.onFire.remove(event.getPlayer());
 		String displayName = event.getPlayer().getDisplayName();
 		if (FunCommands.players.containsKey(displayName)) {
 			if (FCConfigEnum.PERS_NAMES.getBoolean()) {
 				ACPlayer player = ACPlayer.getPlayer(event.getPlayer());
 				player.setInformation("displayName", displayName);
 			}
 			FunCommands.players.remove(displayName);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		FunCommands.onFire.remove(event.getPlayer());
 		String displayName = event.getPlayer().getDisplayName();
 		if (FunCommands.players.containsKey(displayName)) {
 			if (FCConfigEnum.PERS_NAMES.getBoolean()) {
 				ACPlayer player = ACPlayer.getPlayer(event.getPlayer());
 				player.setInformation("displayName", displayName);
 			}
 			FunCommands.players.remove(displayName);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerMove(PlayerMoveEvent event) {
 		if (FunCommands.onFire.contains(event.getPlayer()))
 			event.getPlayer().setFireTicks(100);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		if (FunCommands.onFire.contains(event.getEntity())) {
 			FunCommands.onFire.remove(event.getEntity());
 		}
 	}
 }
