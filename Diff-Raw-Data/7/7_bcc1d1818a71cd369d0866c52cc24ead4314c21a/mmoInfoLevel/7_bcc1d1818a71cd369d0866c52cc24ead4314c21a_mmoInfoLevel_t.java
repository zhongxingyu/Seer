 /*
  * This file is part of mmoInfoLevel <http://github.com/mmoMinecraftDev/mmoInfoLevel>.
  *
  * mmoInfoLevel is free software: you can redistribute it and/or modify
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
 import java.util.Map;
 import java.util.UUID;
 
 import mmo.Core.InfoAPI.MMOInfoEvent;
 import mmo.Core.MMOPlugin;
 import mmo.Core.MMOPlugin.Support;
 import mmo.Core.util.EnumBitSet;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerExpChangeEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.plugin.PluginManager;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.ContainerType;
 import org.getspout.spoutapi.gui.GenericContainer;
 import org.getspout.spoutapi.gui.GenericGradient;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.Gradient;
 import org.getspout.spoutapi.gui.InGameHUD;
 import org.getspout.spoutapi.gui.Label;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.gui.Screen;
 import org.getspout.spoutapi.gui.Texture;
 import org.getspout.spoutapi.gui.Widget;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public final class mmoInfoLevel extends MMOPlugin implements Listener {
 
 	private static final Map<Player, Widget> xplevelbar = new HashMap<Player, Widget>();
 	private static String config_displayas = "bar";	
 	private static final Color greenBar = new Color(0.0980f,0.4823f,0.1882f,1f);
 	private static final Map<UUID, Boolean> updateStatuses = new HashMap<UUID, Boolean>();
 
 	@Override
 	public EnumBitSet mmoSupport(final EnumBitSet support) {		
 		support.set(Support.MMO_AUTO_EXTRACT);
 		return support;
 	}
 
 	@Override
 	public void loadConfiguration(final FileConfiguration cfg) {
 		config_displayas = cfg.getString("displayas", config_displayas);		
 	}
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		this.pm.registerEvents(this, this);
 	}
 
 	@EventHandler
 	public void onMMOInfo(MMOInfoEvent event)
 	{
 		if (event.isToken("level")) {
 			SpoutPlayer player = event.getPlayer();
 			if (player.hasPermission("mmo.info.level")) {				
 				if (config_displayas.equalsIgnoreCase("bar")) {				
 					final CustomWidget widget = new CustomWidget();
 					xplevelbar.put(player, widget);
 					event.setWidget(plugin, widget);
 					event.setIcon("xp.png");
 					updateStatuses.put(player.getUniqueId(), true);	
 				} else { 
 					CustomLabel label = (CustomLabel)new CustomLabel().setResize(true).setFixed(true);				
 					xplevelbar.put(player, label);
 					event.setWidget(this.plugin, label);
 					event.setIcon("level.png");
 					updateStatuses.put(player.getUniqueId(), true);	
 				}
 
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onExpChange(PlayerExpChangeEvent event) {			
 		updateStatuses.put(event.getPlayer().getUniqueId(), true);							
 	}
 
 	public class CustomLabel extends GenericLabel {		
 		public void onTick() {
 			final boolean update = updateStatuses.containsKey(getScreen().getPlayer().getUniqueId()) ? updateStatuses.get(getScreen().getPlayer().getUniqueId()) : false;
 			if (update) {				
 				setText(String.format("Lvl. " + getScreen().getPlayer().getLevel()));
 				updateStatuses.put(getScreen().getPlayer().getUniqueId(), false);
 			}
 		}
 	}
 	public class CustomWidget extends GenericContainer {
 
 		private final Gradient slider = new GenericGradient();
 		private final Texture bar = new GenericTexture();
 		private final Label level = new GenericLabel();
 		private transient int tick = 0;
 
 		public CustomWidget() {
 			super();
 			slider.setMargin(1).setPriority(RenderPriority.Normal).setHeight(5).shiftXPos(1).shiftYPos(2);
 			bar.setUrl("bar10.png").setPriority(RenderPriority.Lowest).setHeight(7).setWidth(103).shiftYPos(1);
			level.setScale(0.8F).setHeight(5).shiftXPos(105).shiftYPos(1);			
			this.setLayout(ContainerType.OVERLAY).setMinWidth(120).setMaxWidth(120).setWidth(120);
 			this.addChildren(slider, level, bar);
 		}
 
 		public void onTick() {			
 			final boolean update = updateStatuses.containsKey(getScreen().getPlayer().getUniqueId()) ? updateStatuses.get(getScreen().getPlayer().getUniqueId()) : false;
 			if (update) {	
 				final int currentExp = Math.max(0, Math.min( 100, (int) (getScreen().getPlayer().getExp()*100)));			
 				slider.setColor(greenBar).setWidth(currentExp); 	
				level.setText("" + getScreen().getPlayer().getLevel());
 				updateStatuses.put(getScreen().getPlayer().getUniqueId(), false);
 			}			
 		}
 	}
 }
