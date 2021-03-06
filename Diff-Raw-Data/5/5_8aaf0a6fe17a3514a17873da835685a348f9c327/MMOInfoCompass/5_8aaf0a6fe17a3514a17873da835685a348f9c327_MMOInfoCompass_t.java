 /*
  * This file is part of mmoInfoCompass <http://github.com/mmoMinecraftDev/mmoInfoCompass>.
  *
  * mmoInfoCompass is free software: you can redistribute it and/or modify
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
 package mmo.Info;
 
 import java.util.HashMap;
 
 import mmo.Core.InfoAPI.MMOInfoEvent;
 import mmo.Core.MMOPlugin;
 import mmo.Core.util.EnumBitSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.Label;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 
 public class MMOInfoCompass extends MMOPlugin implements Listener {
 	private HashMap<Player, Label> widgets = new HashMap<Player, Label>();
 
 	@Override
 	public EnumBitSet mmoSupport(EnumBitSet support) {
 		support.set(Support.MMO_NO_CONFIG);
 		support.set(Support.MMO_AUTO_EXTRACT);
 		return support;
 	}
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		pm.registerEvents(this, this); } 
 
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		Player player = event.getPlayer();
 		Label label = widgets.get(player);
 		if (label != null) {
 			String compass = getCompass(player);
 			if (!compass.equals(label.getText())) {
 				label.setText(compass).setDirty(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onMMOInfo(MMOInfoEvent event) {
 		if (event.isToken("compass")) {
 			Player player = event.getPlayer();
 			if (player.hasPermission("mmo.info.compass")) {
 				Label label = (Label) new GenericLabel(getCompass(player)).setFixed(true).setWidth(GenericLabel.getStringWidth("|W|.|N|")).setHeight(GenericLabel.getStringHeight("|"));
 				widgets.put(player, label);
 				event.setWidget(plugin, label);
 				event.setIcon("compass.png");			
 			}
 		}
 	}
 
 	public String getCompass(Player player) {
 		int angle = (int) (((player.getLocation().getYaw() + 360 + 11.25) / 22.5) % 16) + 3;
 		// String dirs = "|.|N|.|E|.|S|.|W|.|N|.";
 		String dirs = "|.|S|.|W|.|N|.|E|.|S|."; //Match the compass to the in-game compass.
 		return "" + ChatColor.DARK_GRAY + dirs.charAt(angle - 3)
 				+ ChatColor.DARK_GRAY + dirs.charAt(angle - 2)
 				+ ChatColor.GRAY + dirs.charAt(angle - 1)
 				+ ChatColor.WHITE + dirs.charAt(angle)
 				+ ChatColor.GRAY + dirs.charAt(angle + 1)
 				+ ChatColor.DARK_GRAY + dirs.charAt(angle + 2)
 				+ ChatColor.DARK_GRAY + dirs.charAt(angle + 3);
 	}
 }
