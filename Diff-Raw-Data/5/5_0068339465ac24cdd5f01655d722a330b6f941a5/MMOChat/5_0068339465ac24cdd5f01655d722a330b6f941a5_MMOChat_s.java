 /*
  * This file is part of mmoChat <http://github.com/mmoMinecraftDev/mmoChat>.
  *
  * mmoChat is free software: you can redistribute it and/or modify
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
 package mmo.Chat;
 
 import java.util.*;
 import mmo.Core.MMO;
 import mmo.Core.MMOMinecraft;
 import mmo.Core.MMOPlugin;
 import mmo.Core.MMOPlugin.Support;
 import mmo.Core.util.EnumBitSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.*;
 import org.getspout.spoutapi.event.screen.ScreenCloseEvent;
 import org.getspout.spoutapi.event.screen.ScreenOpenEvent;
 import org.getspout.spoutapi.gui.*;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class MMOChat extends MMOPlugin implements Listener {
 
 	static final ChatAPI chat = ChatAPI.instance;
 	static final HashMap<Player, Widget> chatbar = new HashMap<Player, Widget>();
 	static public boolean config_default_color = false;
 	static public boolean config_replace_vanilla_chat = true;
 
 	@Override
 	public EnumBitSet mmoSupport(EnumBitSet support) {
 		support.set(Support.MMO_AUTO_EXTRACT);
 		return support;
 	}
 			
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		ChatAPI.plugin = this;
 		ChatAPI.cfg = cfg;
 		MMOMinecraft.addAPI(chat);
 		pm.registerEvents(this, this);
 		pm.registerEvents(new Channels(), this);
 	}
 
 	@Override
 	public void loadConfiguration(final FileConfiguration cfg) {
 		if (cfg.contains("default_channel")) {
 			cfg.getString("default.channel", cfg.getString("default_channel", "Chat"));
 		} else {
 			cfg.getString("default.channel", "Chat");
 		}
 		config_default_color = cfg.getBoolean("default.colour", config_default_color);
 		config_replace_vanilla_chat = cfg.getBoolean("replace_vanilla_chat", config_replace_vanilla_chat);
 		final Set<String> keys = cfg.getConfigurationSection("channel").getKeys(false);
 		if (keys == null || keys.isEmpty()) {
 			final List<String> list = new ArrayList();
 
 			list.add("Server");
 			cfg.addDefault("channel.Chat.enabled", true);
 			cfg.addDefault("channel.Chat.command", true);
 			cfg.addDefault("channel.Chat.log", true);
 			cfg.addDefault("channel.Chat.filters", list);
 
 			list.clear();
 			list.add("World");
 			cfg.addDefault("channel.Shout.enabled", true);
 			cfg.addDefault("channel.Shout.command", true);
 			cfg.addDefault("channel.Shout.log", true);
 			cfg.addDefault("channel.Shout.filters", list);
 
 			list.clear();
 			list.add("Yell");
 			cfg.addDefault("channel.Yell.enabled", true);
 			cfg.addDefault("channel.Yell.command", true);
 			cfg.addDefault("channel.Yell.log", true);
 			cfg.addDefault("channel.Yell.filters", list);
 
 			list.clear();
 			list.add("Say");
 			cfg.addDefault("channel.Say.enabled", true);
 			cfg.addDefault("channel.Say.command", true);
 			cfg.addDefault("channel.Say.log", true);
 			cfg.addDefault("channel.Say.filters", list);
 
 			list.clear();
 			list.add("Tell");
 			cfg.addDefault("channel.Tell.enabled", true);
 			cfg.addDefault("channel.Tell.command", true);
 			cfg.addDefault("channel.Tell.log", false);
 			cfg.addDefault("channel.Tell.filters", list);
 
 			list.clear();
 			list.add("Reply");
 			cfg.addDefault("channel.Reply.enabled", true);
 			cfg.addDefault("channel.Reply.command", true);
 			cfg.addDefault("channel.Reply.log", false);
 			cfg.addDefault("channel.Reply.filters", list);
 
 			list.clear();
 			list.add("Party");
 			cfg.addDefault("channel.Party.enabled", false);
 			cfg.addDefault("channel.Party.command", false);
 			cfg.addDefault("channel.Party.log", false);
 			cfg.addDefault("channel.Party.filters", list);
 		} else {
 			for (final Iterator<String> it = keys.iterator(); it.hasNext();) {
 				final String channel = it.next();
 				chat.addChannel(channel);
 				if (cfg.isList("channel." + channel + ".alias")) {
 					for (String alias : cfg.getStringList("channel." + channel + ".alias")) {
 						chat.addAlias(channel, alias);
 					}
 				}
 			}
 		}
 	}	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			return false;
 		}
 		Player player = (Player) sender;
 		if (command.getName().equalsIgnoreCase("channel")) {
 			String channel;
 			if (args.length > 0 && (channel = chat.findChannel(args[0])) != null) {
 				if (args.length > 1) {
 					if (args[1].equalsIgnoreCase("hide")) {
 						chat.hideChannel(player, channel);
 						return true;
 					} else if (args[1].equalsIgnoreCase("show")) {
						chat.hideChannel(player, channel);
 						return true;
 					}
 				} else {
 					if (chat.setChannel(player, channel)) {
 						sendMessage(player, "Channel changed to %s", chat.getChannel(player));
 					} else {
 						sendMessage(player, "Unknown channel");
 					}
 					return true;
 				}
 			} else {
 				sendMessage(player, "Currently speaking on %s", chat.getChannel(player));
 				return true;
 			}
 		}
 		return false;
 	}
 	@Override
 	public List<Class<?>> getDatabaseClasses() {
 		List<Class<?>> list = new ArrayList<Class<?>>();
 		list.add(ChatDB.class);
 		return list;
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerJoin(final PlayerJoinEvent event) {
 		chat.load(event.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerQuit(final PlayerQuitEvent event) {
 		chat.unload(event.getPlayer().getName());
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerKick(final PlayerKickEvent event) {
 		chat.unload(event.getPlayer().getName());
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerChat(final AsyncPlayerChatEvent event) {
 		if(event.isCancelled()) 			
 			return;
 		
 		if (chat.doChat(null, event.getPlayer(), event.getMessage()) || config_replace_vanilla_chat) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
 		String message = event.getMessage();
 		String channel = MMO.firstWord(message);
 		if (channel != null && !channel.isEmpty()) {
 			channel = channel.substring(1);
 			if (("me".equalsIgnoreCase(channel)
 					&& chat.doChat(null, event.getPlayer(), message))
 					|| ((channel = chat.findChannel(channel)) != null
 					&& cfg.getBoolean("channel." + channel + ".command", true)
 					&& (chat.doChat(channel, event.getPlayer(), MMO.removeFirstWord(message))
 					|| config_replace_vanilla_chat))) {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onScreenOpen(final ScreenOpenEvent event) {
 		if (!event.isCancelled() && event.getScreenType() == ScreenType.CHAT_SCREEN) {
 			Color black = new Color(0f, 0f, 0f, 0.5f), white = new Color(1f, 1f, 1f, 0.5f);
 			SpoutPlayer player = event.getPlayer();
 			Widget label, bar = chatbar.get(player);
 			if (bar == null) {
 				bar = new GenericContainer(
 						label = new GenericLabel(ChatColor.GRAY + chat.getChannel(player)).setResize(true).setFixed(true).setMargin(3, 3, 0, 3),
 						new GenericGradient(black).setPriority(RenderPriority.Highest),
 						new GenericGradient(white).setMaxWidth(1).setPriority(RenderPriority.High),
 						new GenericGradient(white).setMaxWidth(1).setMarginLeft(label.getWidth() + 5).setPriority(RenderPriority.High),
 						new GenericGradient(white).setMaxHeight(1).setPriority(RenderPriority.High)).setLayout(ContainerType.OVERLAY).setAnchor(WidgetAnchor.BOTTOM_LEFT).setY(-27).setX(4).setHeight(13).setWidth(label.getWidth() + 6).setVisible(false);
 				chatbar.put(player, bar);
 				player.getMainScreen().attachWidget(plugin, bar);
 			}
 			bar.setVisible(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onScreenClose(final ScreenCloseEvent event) {
 		if (!event.isCancelled() && event.getScreenType() == ScreenType.CHAT_SCREEN) {
 			Widget bar = chatbar.remove(event.getPlayer());
 			if (bar != null) {
 				bar.setVisible(false);
 			}
 		}
 	}
 }
