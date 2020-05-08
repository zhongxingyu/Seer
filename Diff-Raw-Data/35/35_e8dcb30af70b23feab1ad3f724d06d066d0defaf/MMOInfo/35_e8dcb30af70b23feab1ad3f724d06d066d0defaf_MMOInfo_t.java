 /*
  * This file is part of mmoInfo <http://github.com/mmoMinecraftDev/mmoInfo>,
  * which is part of mmoMinecraft <http://github.com/mmoMinecraftDev>.
  *
  * mmoInfo is free software: you can redistribute it and/or modify
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
 
 import java.util.Arrays;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import mmo.Core.CoreAPI.MMOHUDEvent;
 import mmo.Core.InfoAPI.MMOInfoEvent;
 import mmo.Core.MMOPlugin.Support;
 import mmo.Core.util.EnumBitSet;
 import mmo.Core.MMO;
 import mmo.Core.MMOPlugin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.block.Furnace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.Container;
 import org.getspout.spoutapi.gui.ContainerType;
 import org.getspout.spoutapi.gui.GenericContainer;
 import org.getspout.spoutapi.gui.GenericGradient;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.Gradient;
 import org.getspout.spoutapi.gui.Label;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.gui.Screen;
 import org.getspout.spoutapi.gui.Widget;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class MMOInfo extends MMOPlugin implements Listener {
 	static String config_info1 = "{name}~mmoMinecraft~{compass}{coords}";
 	static String config_info2 = config_info1;
 	static String config_info3 = config_info1;
 	static String config_info4 = config_info1;
 	static boolean config_hidehungergui = false;
 	static boolean config_hidehealthgui = false;
 	static boolean config_hidearmorgui = false;
 	static boolean config_hideexpgui = false;
 	static boolean config_hideoxygengui = false;
 	static int token_lines = 1;
 	int height = 10;
 	int offset = 1;	
 
 	@Override
 	public EnumBitSet mmoSupport(EnumBitSet support) {
 		support.set(Support.MMO_AUTO_EXTRACT);
 		return support;
 	}
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 
 	@Override
 	public void loadConfiguration(FileConfiguration cfg) {
 		token_lines = cfg.getInt("tokenlines", token_lines);	
 		config_hidehungergui = cfg.getBoolean("HideDefaultHungerBar", config_hidehungergui);
 		config_hidehealthgui = cfg.getBoolean("HideDefaultHealthBar", config_hidehealthgui);
 		config_hidearmorgui = cfg.getBoolean("HideDefaultArmorBar", config_hidearmorgui);
 		config_hideexpgui = cfg.getBoolean("HideDefaultexpBar", config_hideexpgui);
 		config_hideoxygengui = cfg.getBoolean("HideDefaultOxygenBar", config_hideoxygengui);
 		if (token_lines >= 1) {
 			config_info1 = cfg.getString("info", config_info1);
 		}
 		if (token_lines >= 2) {
 			config_info2 = cfg.getString("info2", config_info2);
 			height = height + 10;
 		}
 		if (token_lines >= 3) {
 			config_info3 = cfg.getString("info3", config_info3);
 			height = height + 10;
 		}
 		if (token_lines >= 4) {
 			config_info4 = cfg.getString("info4", config_info4);
 			height = height + 10;
 		}
 	}
 
 	@EventHandler
 	public void onMMOHUD(MMOHUDEvent event) {
 		Player player = event.getPlayer();
 		if ((player.hasPermission("mmo.info.display"))
 				&& ((event.getAnchor() == WidgetAnchor.TOP_LEFT)
 						|| (event.getAnchor() == WidgetAnchor.TOP_CENTER) || (event
 								.getAnchor() == WidgetAnchor.TOP_RIGHT)))
 
 			event.setOffsetY(event.getOffsetY() + this.height + this.offset + 1);
 	}	
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
 		if (event.isCancelled())
 			return;	
 		final SpoutPlayer sPlayer = SpoutManager.getPlayer(event.getPlayer());	
 		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				hideDefaultGui(sPlayer);
 			}
 		}, 60L); 
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {	
 		final SpoutPlayer sPlayer = SpoutManager.getPlayer(event.getPlayer());
 		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				hideDefaultGui(sPlayer);
 			}
 		}, 60L); 
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {	
 		final SpoutPlayer sPlayer = SpoutManager.getPlayer(event.getPlayer());
 		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				hideDefaultGui(sPlayer);
 			}
 		}, 60L); 
 	}
 
 	public void hideDefaultGui(SpoutPlayer sPlayer) {
 		if (config_hidehungergui = true) {
 			sPlayer.getMainScreen().getHungerBar().setVisible(false);
 		}
 		if (config_hidehealthgui = true) {
 			sPlayer.getMainScreen().getHealthBar().setVisible(false);	
 		}
 		if (config_hidearmorgui = true) {
 			sPlayer.getMainScreen().getArmorBar().setVisible(false);
 		}
 		if (config_hideexpgui = true) {
 			sPlayer.getMainScreen().getExpBar().setVisible(false);
 		}
 		if (config_hideoxygengui = true) {
 			sPlayer.getMainScreen().getBubbleBar().setVisible(false);
 		}	
 		sPlayer.getMainScreen().setDirty(true);	
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onMMOInfo(MMOInfoEvent event) {
 		if (event.isToken("name")) {
 			SpoutPlayer player = event.getPlayer();
 			event.setWidget(this.plugin, new GenericLabel(MMO.getName(player))
 			.setResize(true).setFixed(true));
 		}	
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("info")) {
 			onSpoutCraftPlayer(SpoutManager.getPlayer((Player) sender));
 			return true;
 		}
 		return false;
 	}
 
 
 	@EventHandler
 	public void onSpoutcraftEnable(SpoutCraftEnableEvent e) {
 		onSpoutCraftPlayer(e.getPlayer());
 		hideDefaultGui(e.getPlayer());
 	}
 
 	public void onSpoutCraftPlayer(SpoutPlayer player) {
 		if (!player.hasPermission("mmo.info.display")) {
 			return;
 		}
 
 		// Global Settings
 		Color back = new Color(0.0F, 0.0F, 0.0F, 0.75F);
 		Color bottom = new Color(1.0F, 1.0F, 1.0F, 0.75F);
 		Screen screen = player.getMainScreen();
 		screen.removeWidgets(this);
 
 		// Global Gradient Image for BlackBox
 		screen.attachWidget(
 				this.plugin,
 				new GenericGradient().setBottomColor(back).setTopColor(back)
 				.setMaxWidth(2048).setX(0).setY(0).setWidth(2048)
 				.setHeight(this.height + this.offset)
 				.setAnchor(WidgetAnchor.TOP_LEFT)
 				.setPriority(RenderPriority.Highest));
 
 		// Global Gradient Image for White Line at bottom of Box
 		screen.attachWidget(
 				this.plugin,
 				new GenericGradient().setBottomColor(bottom)
 				.setTopColor(bottom).setX(0)
 				.setY(this.height + this.offset).setMaxWidth(2048)
 				.setWidth(2048).setHeight(1)
 				.setAnchor(WidgetAnchor.TOP_LEFT)
 				.setPriority(RenderPriority.Highest));
 
 		// Process Token Line 1
 		if (token_lines >= 1) {
 			Container left;
 			screen.attachWidget(
 					this.plugin,
 					left = (Container) new GenericContainer()
 					.setLayout(ContainerType.HORIZONTAL)
 					.setAlign(WidgetAnchor.TOP_LEFT)
 					.setAnchor(WidgetAnchor.TOP_LEFT).setWidth(427)
					.setHeight(10).setX(0).setY(this.offset));
 
 			Container center;
 			screen.attachWidget(
 					this.plugin,
 					center = (Container) new GenericContainer()
 					.setLayout(ContainerType.HORIZONTAL)
 					.setAlign(WidgetAnchor.TOP_CENTER)
 					.setAnchor(WidgetAnchor.TOP_CENTER).setWidth(427)
					.setHeight(10).setX(-213).setY(this.offset));
 
 			Container right;
 			screen.attachWidget(
 					this.plugin,
 					right = (Container) new GenericContainer()
 					.setLayout(ContainerType.HORIZONTAL)
 					.setAlign(WidgetAnchor.TOP_RIGHT)
 					.setAnchor(WidgetAnchor.TOP_RIGHT).setWidth(427)
					.setHeight(10).setX(-427).setY(this.offset));
 
 			Container current = left;
 			Matcher match = Pattern.compile("[^{~]+|(~)|\\{([^}]*)\\}")
 					.matcher(config_info1);
 			while (match.find())
 				if (match.group(1) != null) {
 					if (current == left) {
 						current = center;
 					} else {
 						if (current != center)
 							break;
 						current = right;
 					}
 				} else if (match.group(2) != null) {
 					String[] args = MMO.smartSplit(match.group(2));
 					MMOInfoEventAPI event = new MMOInfoEventAPI(player,
 							args[0], (String[]) Arrays.copyOfRange(args, 1,
 									args.length));
 					this.pm.callEvent(event);
 					if ((!event.isCancelled()) && (event.getWidget() != null)) {
 						Widget widget = event.getWidget().setMargin(0, 3);
 						Plugin widgetPlugin = event.getPlugin();
 						if (event.getIcon() != null) {
 							Widget icon = new GenericTexture(event.getIcon())
 							.setMargin(0, 0, 0, 3).setHeight(8)
 							.setWidth(8).setFixed(true);
 							screen.attachWidget(widgetPlugin, icon);
 							current.addChild(icon);
 						}
 						screen.attachWidget(widgetPlugin, widget);
 						current.addChild(widget);
 					}
 				} else {
 					String str = match.group().trim();
 					if (!str.isEmpty())
 						current.addChild(new GenericLabel(str).setResize(true)
 								.setMargin(0, 3).setFixed(true));
 				}
 		}
 
 		// Process Token Line 2
 		if (token_lines >= 2) {
 			Container left2;
 			screen.attachWidget(
 					this.plugin,
 					left2 = (Container) new GenericContainer()
 					.setLayout(ContainerType.HORIZONTAL)
 					.setAlign(WidgetAnchor.TOP_LEFT)
 					.setAnchor(WidgetAnchor.TOP_LEFT).setWidth(427)
					.setHeight(10).setX(0).setY(this.offset+this.offset+10));
 
 			Container center2;
 			screen.attachWidget(
 					this.plugin,
 					center2 = (Container) new GenericContainer()
 					.setLayout(ContainerType.HORIZONTAL)
 					.setAlign(WidgetAnchor.TOP_CENTER)
 					.setAnchor(WidgetAnchor.TOP_CENTER).setWidth(427)
					.setHeight(10).setX(-213).setY(this.offset+this.offset+10));
 
 			Container right2;
 			screen.attachWidget(
 					this.plugin,
 					right2 = (Container) new GenericContainer()
 					.setLayout(ContainerType.HORIZONTAL)
 					.setAlign(WidgetAnchor.TOP_RIGHT)
 					.setAnchor(WidgetAnchor.TOP_RIGHT).setWidth(427)	
					.setHeight(10).setX(-427).setY(this.offset+this.offset+10));
 
 			Container current = left2;
 			Matcher match2 = Pattern.compile("[^{~]+|(~)|\\{([^}]*)\\}").matcher(
 					config_info2);
 			while (match2.find())
 				if (match2.group(1) != null) {
 					if (current == left2) {
 						current = center2;
 					} else {
 						if (current != center2)
 							break;
 						current = right2;
 					}
 				} else if (match2.group(2) != null) {
 					String[] args = MMO.smartSplit(match2.group(2));
 					MMOInfoEventAPI event = new MMOInfoEventAPI(player, args[0],
 							(String[]) Arrays.copyOfRange(args, 1, args.length));
 					this.pm.callEvent(event);
 					if ((!event.isCancelled()) && (event.getWidget() != null)) {
 						Widget widget = event.getWidget().setMargin(0, 3);
 						Plugin widgetPlugin = event.getPlugin();
 						if (event.getIcon() != null) {
 							Widget icon = new GenericTexture(event.getIcon())
 							.setMargin(0, 0, 0, 3).setHeight(8).setWidth(8)
 							.setFixed(true);
 							screen.attachWidget(widgetPlugin, icon);
 							current.addChild(icon);
 						}
 						screen.attachWidget(widgetPlugin, widget);
 						current.addChild(widget);
 					}
 				} else {
 					String str = match2.group().trim();
 					if (!str.isEmpty())
 						current.addChild(new GenericLabel(str).setResize(true)
 								.setMargin(0, 3).setFixed(true));
 				}
 		}
 		// Process Token Line 3
 		if (token_lines >= 3) {
 			Container left3;
 			screen.attachWidget(
 					this.plugin,
 					left3 = (Container) new GenericContainer()
 					.setLayout(ContainerType.HORIZONTAL)
 					.setAlign(WidgetAnchor.TOP_LEFT)
 					.setAnchor(WidgetAnchor.TOP_LEFT).setWidth(427)
					.setHeight(10).setX(0).setY(this.offset+this.offset+20));
 
 			Container center3;
 			screen.attachWidget(
 					this.plugin,
 					center3 = (Container) new GenericContainer()
 					.setLayout(ContainerType.HORIZONTAL)
 					.setAlign(WidgetAnchor.TOP_CENTER)
 					.setAnchor(WidgetAnchor.TOP_CENTER).setWidth(427)
					.setHeight(10).setX(-213).setY(this.offset+this.offset+20));
 
 			Container right3;
 			screen.attachWidget(
 					this.plugin,
 					right3 = (Container) new GenericContainer()
 					.setLayout(ContainerType.HORIZONTAL)
 					.setAlign(WidgetAnchor.TOP_RIGHT)
 					.setAnchor(WidgetAnchor.TOP_RIGHT).setWidth(427)	
					.setHeight(10).setX(-427).setY(this.offset+this.offset+20));
 
 			Container current = left3;
 			Matcher match2 = Pattern.compile("[^{~]+|(~)|\\{([^}]*)\\}").matcher(
 					config_info3);
 			while (match2.find())
 				if (match2.group(1) != null) {
 					if (current == left3) {
 						current = center3;
 					} else {
 						if (current != center3)
 							break;
 						current = right3;
 					}
 				} else if (match2.group(2) != null) {
 					String[] args = MMO.smartSplit(match2.group(2));
 					MMOInfoEventAPI event = new MMOInfoEventAPI(player, args[0],
 							(String[]) Arrays.copyOfRange(args, 1, args.length));
 					this.pm.callEvent(event);
 					if ((!event.isCancelled()) && (event.getWidget() != null)) {
 						Widget widget = event.getWidget().setMargin(0, 3);
 						Plugin widgetPlugin = event.getPlugin();
 						if (event.getIcon() != null) {
 							Widget icon = new GenericTexture(event.getIcon())
 							.setMargin(0, 0, 0, 3).setHeight(8).setWidth(8)
 							.setFixed(true);
 							screen.attachWidget(widgetPlugin, icon);
 							current.addChild(icon);
 						}
 						screen.attachWidget(widgetPlugin, widget);
 						current.addChild(widget);
 					}
 				} else {
 					String str = match2.group().trim();
 					if (!str.isEmpty())
 						current.addChild(new GenericLabel(str).setResize(true)
 								.setMargin(0, 3).setFixed(true));
 				}
 		}
 		//Process Token Line 4
 		if (token_lines >= 4) {
 			Container left4;
 			screen.attachWidget(
 					this.plugin,
 					left4 = (Container) new GenericContainer()
 					.setLayout(ContainerType.HORIZONTAL)
 					.setAlign(WidgetAnchor.TOP_LEFT)
 					.setAnchor(WidgetAnchor.TOP_LEFT).setWidth(427)
					.setHeight(10).setX(0).setY(this.offset+this.offset+30));
 
 			Container center4;
 			screen.attachWidget(
 					this.plugin,
 					center4 = (Container) new GenericContainer()
 					.setLayout(ContainerType.HORIZONTAL)
 					.setAlign(WidgetAnchor.TOP_CENTER)
 					.setAnchor(WidgetAnchor.TOP_CENTER).setWidth(427)
					.setHeight(10).setX(-213).setY(this.offset+this.offset+30));
 
 			Container right4;
 			screen.attachWidget(
 					this.plugin,
 					right4 = (Container) new GenericContainer()
 					.setLayout(ContainerType.HORIZONTAL)
 					.setAlign(WidgetAnchor.TOP_RIGHT)
 					.setAnchor(WidgetAnchor.TOP_RIGHT).setWidth(427)	
					.setHeight(10).setX(-427).setY(this.offset+this.offset+30));
 
 			Container current = left4;
 			Matcher match2 = Pattern.compile("[^{~]+|(~)|\\{([^}]*)\\}").matcher(
 					config_info4);
 			while (match2.find())
 				if (match2.group(1) != null) {
 					if (current == left4) {
 						current = center4;
 					} else {
 						if (current != center4)
 							break;
 						current = right4;
 					}
 				} else if (match2.group(2) != null) {
 					String[] args = MMO.smartSplit(match2.group(2));
 					MMOInfoEventAPI event = new MMOInfoEventAPI(player, args[0],
 							(String[]) Arrays.copyOfRange(args, 1, args.length));
 					this.pm.callEvent(event);
 					if ((!event.isCancelled()) && (event.getWidget() != null)) {
 						Widget widget = event.getWidget().setMargin(0, 3);
 						Plugin widgetPlugin = event.getPlugin();
 						if (event.getIcon() != null) {
 							Widget icon = new GenericTexture(event.getIcon())
 							.setMargin(0, 0, 0, 3).setHeight(8).setWidth(8)
 							.setFixed(true);
 							screen.attachWidget(widgetPlugin, icon);
 							current.addChild(icon);
 						}
 						screen.attachWidget(widgetPlugin, widget);
 						current.addChild(widget);
 					}
 				} else {
 					String str = match2.group().trim();
 					if (!str.isEmpty())
 						current.addChild(new GenericLabel(str).setResize(true)
 								.setMargin(0, 3).setFixed(true));
 				}
 		}
 
 	}
 }
