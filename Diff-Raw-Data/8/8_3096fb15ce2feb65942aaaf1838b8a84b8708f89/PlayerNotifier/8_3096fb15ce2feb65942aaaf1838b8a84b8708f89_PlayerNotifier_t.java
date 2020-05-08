 /*******************************************************************************
  Copyright (c) 2013 James Richardson.
 
  PlayerNotifier.java is part of BukkitUtilities.
 
  BukkitUtilities is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 3 of the License, or (at your option) any
  later version.
 
  BukkitUtilities is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License along with
  BukkitUtilities. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.bukkit.utilities.updater;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import name.richardson.james.bukkit.utilities.listener.AbstractListener;
 import name.richardson.james.bukkit.utilities.localisation.LocalisedCommandSender;
 import name.richardson.james.bukkit.utilities.localisation.ResourceBundles;
 
 public class PlayerNotifier extends AbstractListener {
 
 	private final String permission;
 	private final String pluginName;
 	private final String version;
 
	public PlayerNotifier(String pluginName, String version) {
		super(pluginName);
		this.pluginName = pluginName;
 		this.permission = pluginName.toLowerCase();
 		this.version = version;
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		final boolean notify = event.getPlayer().hasPermission(this.permission);
 		if (notify) {
 			LocalisedCommandSender localisedPlayer = new LocalisedCommandSender(event.getPlayer(), ResourceBundles.UTILITIES);
 			localisedPlayer.send("notice.new-version-available", this.pluginName, this.version);
 		}
 	}
 
 }
