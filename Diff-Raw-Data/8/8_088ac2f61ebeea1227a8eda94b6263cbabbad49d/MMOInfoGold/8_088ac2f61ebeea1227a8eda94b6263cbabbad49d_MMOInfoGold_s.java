 /*
  * This file is part of mmoInfoGold <http://github.com/mmoMinecraftDev/mmoInfoCoords>.
  *
  * mmoInfoGold is free software: you can redistribute it and/or modify
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
 
 import java.text.NumberFormat;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import mmo.Core.InfoAPI.MMOInfoEvent;
 import mmo.Core.MMOPlugin;
 import mmo.Core.util.EnumBitSet;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 
public final class MMOInfoGold extends MMOPlugin {
 	private final transient Map<Player, CustomLabel> widgets = new HashMap<Player, CustomLabel>();
 	private static Economy economy;
 	private static String config_curtype = "US";
 	private static String config_displayas = "currency";
 	private static NumberFormat numForm;
 	private static Locale caLoc = new Locale("en", "US");
 
 	@Override
 	public EnumBitSet mmoSupport(final EnumBitSet support) {
 		support.set(Support.MMO_AUTO_EXTRACT);
 		return support;
 	}
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
		}
		// TODO: is this register needed?
		//registerEvents(new MMOListener(this));
 	}
 
 	public void updateCur() {
 		
 	}
 	
 	@Override
 	public void loadConfiguration(final FileConfiguration cfg) {
 		config_curtype = cfg.getString("curtype", config_curtype);
 		config_displayas = cfg.getString("displayas", config_displayas);		
 	}
 	
 	public static final class MMOListener implements Listener {
 		private MMOInfoGold plugin;
 
 		public MMOListener(MMOInfoGold plugin) {
 			this.plugin = plugin;
 		}
 
 		@EventHandler
 		public void onMMOInfo(final MMOInfoEvent event) {
 			if (event.isToken("gold")) {
 				final SpoutPlayer player = event.getPlayer();
 				if (config_displayas.equalsIgnoreCase("currency")) {
 					if (config_curtype.equalsIgnoreCase("US")) {
 						numForm = NumberFormat.getCurrencyInstance(caLoc);
 					} else if (config_curtype.equalsIgnoreCase("DE")) {
 						final Locale caLoc = new Locale("de", "DE");							
 					}
 				}
 
 				if (player.hasPermission("mmo.info.gold")) {
 					final CustomLabel label = (CustomLabel) new CustomLabel().setResize(true).setFixed(true);
 					plugin.getWidgets().put(player, label);
 					event.setWidget(plugin, label);
 					event.setIcon("coin.png");				
 				}
 			}
 		}
 	}
 
 	public Map getWidgets() {
 		return widgets;
 	}
 
 	public static final class CustomLabel extends GenericLabel {		
 		private transient int tick = 0;
 
 		@Override
 		public void onTick() {
 			if (tick++ % 100 == 0) {
 				if (config_displayas.equalsIgnoreCase("currency")) {				
 					final String plat = numForm.format(economy.getBalance(this.getScreen().getPlayer().getName()));
 					setText(String.format(ChatColor.WHITE + plat));						
 				} else {
 					final String[] money = Double.toString((double) economy.getBalance(this.getScreen().getPlayer().getName())).split("\\.");				
 					setText(String.format(ChatColor.WHITE + "%s" + ChatColor.YELLOW + "g " + ChatColor.WHITE + "%s" + ChatColor.GRAY + "s", money.length > 0 ? money[0] : "0", money.length > 1 ? money[1] : "0"));
 				}
 			}
 		}
 	}
 }
