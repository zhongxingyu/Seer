 /*******************************************************************************
  * Copyright (c) 2013 James Richardson
  * 
  * OfflinePlayerMatcher.java is part of BukkitUtilities.
  * 
  * BukkitUtilities is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * BukkitUtilities is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * BukkitUtilities. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.bukkit.utilities.matchers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 
 public class OfflinePlayerMatcher implements Matcher {
 
 	private final Server server;
 
 	public OfflinePlayerMatcher() {
 		this.server = Bukkit.getServer();
 	}
 
 	public List<String> getMatches(String argument) {
 		argument = argument.toLowerCase();
 		final Set<String> set = new TreeSet<String>();
 		final List<String> list = new ArrayList<String>();
		// this is here to prevent large sets disconnecting clients
		// up to around 1000 names appears to be ok at once.
		if (argument.length() != 0) {
			for (final OfflinePlayer player : this.server.getOfflinePlayers()) {
				if (player.getName().toLowerCase().startsWith(argument)) {
					set.add(player.getName());
				}
 			}
 		}
 		list.addAll(set);
 		return list;
 	}
 
 }
