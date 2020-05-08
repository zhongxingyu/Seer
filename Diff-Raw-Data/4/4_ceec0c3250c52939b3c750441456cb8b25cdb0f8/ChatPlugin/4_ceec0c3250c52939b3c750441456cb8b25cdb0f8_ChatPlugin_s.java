 package de.johannes13.minecraft.bukkit.chat.plugin;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.ConfigurationNode;
 
 import de.johannes13.minecraft.bukkit.chat.inspircd.IrcUser;
 import de.johannes13.minecraft.bukkit.chat.inspircd.Ircd;
 
 public class ChatPlugin extends JavaPlugin implements Listener {
 
 	private Hashtable<Player, PlayerMetadata> umeta;
 	/* package private */Ircd ircd;
 	private Method gmGetPermission;
 	private Method gmHas;
 	private Plugin gm;
 	Hashtable<String, ChannelMetadata> cmeta;
 	public ChatPlugin() {
 	}
 
 	public ChatPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader loader) {
 		super(pluginLoader, instance, desc, folder, plugin, loader);
 	}
 
 	@Override
 	public void onDisable() {
 		getServer().broadcastMessage("Chat Channels and IRC relay have been disabled");
 		ircd.exit();
 		ircd = null;
 		umeta = null;
 		cmeta = null;
 
 	}
 
 	@Override
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		ChatListener cl = new ChatListener(this);
 		pm.registerEvent(Type.PLAYER_CHAT, cl, Priority.High, this);
 		pm.registerEvent(Type.PLAYER_JOIN, cl, Priority.Monitor, this);
 		pm.registerEvent(Type.PLAYER_QUIT, cl, Priority.Monitor, this);
 		pm.registerEvent(Type.PLAYER_KICK, cl, Priority.Monitor, this);
 		List<ConfigurationNode> empty = Collections.emptyList();
 
 		umeta = new Hashtable<Player, PlayerMetadata>();
 		cmeta = new Hashtable<String, ChannelMetadata>();
 
 		// parse config
 		List<ConfigurationNode> chs = getConfiguration().getNodeList("channels", empty);
 		for (ConfigurationNode conf : chs) {
 			cmeta.put(conf.getString("name"), new ChannelMetadata(this, conf.getString("name"), conf.getString("irc"), conf.getString("public-name"), conf.getBoolean("hidden", false), conf.getInt("priority", 0)));
 		}
 		ircd = new Ircd(getConfiguration().getNode("ircd"), this);
 		for (Player p : getServer().getOnlinePlayers()) {
 			pluginAddPlayer(p);
 		}
 		ircd.start();
 	}
 
 	void pluginAddPlayer(Player p) {
 		PlayerMetadata meta = new PlayerMetadata(p);
 		int priority = -1;
 		for (ChannelMetadata ch : cmeta.values()) {
 			if (check(p, "chatplugin.autojoin." + ch.name) && check(p, "chatplugin.join." + ch.name)) {
 				ch.players.add(meta);
 				meta.getChannels().add(ch.name);
 				if (ch.priority > priority) {
 					priority = ch.priority;
 					meta.setCurrentChannel(ch);
 				}
 			}
 		}
 		umeta.put(p, meta);
 	}
 
 	public boolean check(Player p, String perm) {
 		if (gm == null)
 			gm = getServer().getPluginManager().getPlugin("GroupManager");
 		if (gm == null)
 			// gm is still not loaded, assume it works
 			return true;
 		if (gmGetPermission == null) {
 			try {
 				Class<?> clazz = gm.getClass();
 				gmGetPermission = clazz.getMethod("getPermissionHandler");
 			} catch (Exception e) {
 				getServer().getLogger().log(Level.WARNING, "Tried to access GroupManager, got exception", e);
 				// allow it - not a big security risk imho
 				return true;
 			}
 		}
 		if (gmHas == null) {
 			try {
 				Class<?> cl2 = gm.getClass().getClassLoader().loadClass("com.nijiko.permissions.PermissionHandler");
 				gmHas = cl2.getMethod("has", Player.class, String.class);
 			} catch (Exception e) {
 				getServer().getLogger().log(Level.WARNING, "Tried to access GroupManager, got exception", e);
 				return true;
 			}
 
 		}
 		try {
 			return (Boolean) gmHas.invoke(gmGetPermission.invoke(gm), p, perm);
 		} catch (Exception e) {
 			getServer().getLogger().log(Level.WARNING, "Tried to invoke GroupManager, got exception", e);
 			return true;
 		}
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (sender instanceof Player) {
 			Player p = (Player) sender;
 			PlayerMetadata pm = umeta.get(p);
 			ChannelMetadata cm = pm.getCurrentChannel();
 			try {
 				ChatCommands c = ChatCommands.valueOf(commandLabel.toUpperCase());
 				switch (c) {
 				case CH:
 					if (args.length < 1)
 						return false;
 					if (args[0].equals("help")) {
						// TODO: help message
 						return true;
 					}
 					if (args[0].equals("pm")) {
 						if (args.length != 2)
 							return false; // TODO: maybe we should return a the help ourself
 						List<? extends CommandSender> res = findPlayer(args[1]);
 						if (res.size() > 1) {
 							p.sendMessage(ChatColor.RED + "User matched against:" + res);
 							return true;
 						} else if (res.size() < 1) {
 							p.sendMessage(ChatColor.RED + "User not found");
 							return true;
 						}
 						pm.setCurrentChannel(new PmChannel(this, res.get(0)));
 						String nick;
 						if (res.get(0) instanceof IrcUser) {
 							nick = ((IrcUser) res.get(0)).getNick();
 						} else {
 							nick = ((Player) res.get(0)).getName();
 						}
 						p.sendMessage(ChatColor.YELLOW + "You are now in private chat with " + nick);
 						return true;
 					}
 					if (cmeta.containsKey(args[0])) {
 						cm = cmeta.get(args[0]);
 						if (check(p, "chatplugin.join." + cm.name)) {
 							pm.setCurrentChannel(cm);
 							if (!pm.getChannels().contains(cm.name)) {
 								pm.getChannels().add(cm.name);
 								for(PlayerMetadata pmm : cm.players) {
 									pmm.getPlayer().sendMessage(ChatColor.YELLOW + "[" + p.getDisplayName() + ChatColor.YELLOW + " has joined " + cm.getPublicName() + "]");
 								}
 								p.sendMessage(ChatColor.YELLOW + "[You have joined " + p.getDisplayName() + "]");
 								cm.players.add(pm);
 							}
 							return true;
 						} else {
 							p.sendMessage("You don't have the permission to join " + cm.name);
 							return true;
 						}
 					}
 					break;
 				case ME:
 					if (args.length < 1)
 						return false;
 					cm.sendAction(pm, joinArgs(args));
 					return true;
 				case MME:
 					if (args.length < 2)
 						return false;
 					List<? extends CommandSender> res = findPlayer(args[0]);
 					if (res.size() > 1) {
 						p.sendMessage(ChatColor.RED + "User matched against:" + res);
 						return true;
 					} else {
 						CommandSender t = res.get(0);
 						String[] args0 = new String[args.length-1];
 						System.arraycopy(args, 1, args0, 0, args0.length);
 						String msg = joinArgs(args0);
 						p.sendMessage("(MSG) * " + p.getDisplayName() + " " + msg);
 						if (t instanceof Player) {
 							t.sendMessage("(MSG) * " + p.getDisplayName() + " " + msg);
 						} else if (t instanceof IrcUser) {
 							ircd.sendAction(pm, ((IrcUser) t).getUid(), msg);
 						}
 						return true;
 					}
 				case MSG:
 					if (args.length < 2)
 						return false;
 					res = findPlayer(args[0]);
 					if (res.size() > 1) {
 						p.sendMessage(ChatColor.RED + "User matched against:" + res);
 						return true;
 					} else {
 						CommandSender t = res.get(0);
 						String[] args0 = new String[args.length-1];
 						System.arraycopy(args, 1, args0, 0, args0.length);
 						String msg = joinArgs(args0);
 						p.sendMessage("(MSG) " + p.getDisplayName() + ": " + msg);
 						if (t instanceof Player) {
 							t.sendMessage("(MSG) " + p.getDisplayName() + ": " + msg);
 						} else if (t instanceof IrcUser) {
 							ircd.sendMessage(pm, ((IrcUser) t).getUid(), msg);
 						}
 						return true;
 					}
 				case NAMES:
 					if (args.length == 1) {
 						cm = cmeta.get(args[0]);
 					}
 					if (cm == null) {
 						p.sendMessage(ChatColor.RED + "You didn't specify a valid channel");
 					}
 					p.sendMessage(ChatColor.YELLOW + "Current Members in " + cm.getPublicName() + ":");
 					if (cm.players.size() > 0) p.sendMessage(ChatColor.YELLOW + cm.getPublicName() + ChatColor.WHITE + " " + joinPlayerMetadata(cm.players));
 					if (cm.ircuser.size() > 0) p.sendMessage(ChatColor.YELLOW + cm.ircRelay + ChatColor.WHITE + " " + joinIrcUser(cm.ircuser));
 					break;
 
 				}
 			} catch (IllegalArgumentException e) {
 
 			}
 		}
 		return false;
 	}
 	
 	public String joinPlayerMetadata(List<PlayerMetadata> pms) {
 		StringBuilder res = new StringBuilder(80);
 		String delim = "";
 		for(PlayerMetadata pm : pms) {
 			res.append(delim);
 			res.append(pm.getPlayer().getDisplayName());
 			delim = " ";
 		}
 		return res.toString();
 	}
 	
 	public String joinIrcUser(List<IrcUser> ircu) {
 		StringBuilder res = new StringBuilder(80);
 		String delim = "";
 		for(IrcUser pm : ircu) {
 			res.append(delim);
 			res.append(pm.getNick());
 			delim = " ";
 		}
 		return res.toString();
 	}
 
 	public static String joinArgs(String... args) {
 		String lmt = "";
 		StringBuilder msg = new StringBuilder(80);
 		for (String part : args) {
 			msg.append(lmt);
 			msg.append(part);
 			lmt = " ";
 		}
 		return msg.toString();
 	}
 
 	public List<? extends CommandSender> findPlayer(String nick) {
 		List<Player> plMatch = getServer().matchPlayer(nick);
 		ArrayList<IrcUser> plMatch2 = new ArrayList<IrcUser>();
 		for (IrcUser u : ircd.getUser()) {
 			if (u.getNick().startsWith(nick))
 				plMatch2.add(u);
 		}
 		if ((plMatch.size() + plMatch2.size()) > 1) {
 			// Check if args[0] is the full player name..
 			for (Player plm : plMatch) {
 				if (plm.getName().equalsIgnoreCase(nick))
 					return Arrays.asList(plm);
 			}
 			for (IrcUser iu : plMatch2) {
 				if (iu.getNick().equalsIgnoreCase(nick))
 					return Arrays.asList(iu);
 			}
 			ArrayList<CommandSender> res = new ArrayList<CommandSender>();
 			res.addAll(plMatch);
 			res.addAll(plMatch2);
 		}
 		if (plMatch.size() > 0) {
 			return plMatch;
 		}
 		if (plMatch2.size() > 0) {
 			return plMatch2;
 		}
 		return new ArrayList<CommandSender>();
 	}
 
 	public PlayerMetadata getMetadata(Player p) {
 		PlayerMetadata res = umeta.get(p);
 		if (res == null) {
 			res = new PlayerMetadata(p);
 			umeta.put(p, res);
 		}
 		return res;
 	}
 
 	public Collection<ChannelMetadata> getChannels() {
 		return cmeta.values();
 	}
 
 	public void ircJoin(IrcUser u, ChannelMetadata cm, boolean notify) {
 		cm.ircuser.add(u);
 		if (notify) {
 			for (PlayerMetadata pm : cm.players) {
 				pm.getPlayer().sendMessage(ChatColor.YELLOW + "[" + u.getNick() + " joined " + cm.ircRelay + "]");
 			}
 		}
 	}
 
 	public void playerQuit(IrcUser ircUser) {
 		ArrayList<PlayerMetadata> sendQuit = new ArrayList<PlayerMetadata>();
 		for (ChannelMetadata cm : getChannels()) {
 			if (!cm.ircuser.contains(ircUser))
 				continue;
 			for (PlayerMetadata pm : cm.players) {
 				if (sendQuit.contains(pm))
 					continue;
 				sendQuit.add(pm);
 				pm.getPlayer().sendMessage(ChatColor.YELLOW + "[" + ircUser.getNick() + " has quit IRC]");
 			}
 		}
 	}
 
 	public void forceJoin(PlayerMetadata playerMetadata, ChannelMetadata channelMetadata, boolean force) {
 		playerMetadata.getChannels().add(channelMetadata.name);
 		channelMetadata.players.add(playerMetadata);
 		playerMetadata.getPlayer().sendMessage(ChatColor.YELLOW + "[You were forced to join " + channelMetadata.getName() + "]");
 	}
 
 	public void forcePart(PlayerMetadata playerMetadata, ChannelMetadata channelMetadata) {
 		playerMetadata.getChannels().remove(channelMetadata.name);
 		channelMetadata.players.remove(playerMetadata);
 		if (playerMetadata.getCurrentChannel().equals(channelMetadata)) {
 			int max = -1;
 			String nch = "";
 			for (String ch : playerMetadata.getChannels()) {
 				if (cmeta.get(ch).priority > max) {
 					max = cmeta.get(ch).priority;
 					nch = ch;
 				}
 			}
 			playerMetadata.setCurrentChannel(cmeta.get(nch));
 			if (nch == null) {
 				playerMetadata.getPlayer().sendMessage(ChatColor.RED + "[You were forced to leave " + channelMetadata.name + " you are currently in no channel]");
 			} else {
 				playerMetadata.getPlayer().sendMessage(ChatColor.YELLOW + "[You were forced to leave " + channelMetadata.name + ", your current channel is now " + nch + "]");
 			}
 		} else {
 			playerMetadata.getPlayer().sendMessage(ChatColor.YELLOW + "[You were forced to leave " + channelMetadata.name);
 		}
 		for (PlayerMetadata pm : channelMetadata.players) {
 			pm.getPlayer().sendMessage(ChatColor.YELLOW + "[" + playerMetadata.getPlayer().getDisplayName() + ChatColor.YELLOW + " was forced to leave " + channelMetadata.name + "]");
 		}
 	}
 
 	public void sendIrcMessage(String srcNick, ChannelMetadata channel, String message) {
 		for (PlayerMetadata pm : channel.players) {
 			pm.getPlayer().sendMessage("[" + channel.getPublicName() + "] " + srcNick + ": " + message);
 		}
 
 	}
 
 	public void sendIrcMessage(String srcNick, PlayerMetadata playerMetadata, String message) {
 		playerMetadata.getPlayer().sendMessage("(MSG) " + srcNick + ": " + message);
 	}
 
 	public void sendIrcPart(IrcUser ircUser, ChannelMetadata channelMetadata) {
 		channelMetadata.ircuser.remove(ircUser);
 		for (PlayerMetadata pm : channelMetadata.players) {
 			pm.getPlayer().sendMessage(ChatColor.YELLOW + "[" + ircUser.getNick() + " has left " + channelMetadata.ircRelay + "]");
 		}
 	}
 
 	public ChannelMetadata getChannel(String s) {
 		return cmeta.get(s);
 	}
 
 	public void restartIrcd() {
 		ircd.exit();
 		ircd = new Ircd(getConfiguration().getNode("ircd"), this);
 		ircd.start();
 	}
 
 	public void removePlayer(Player player) {
 		PlayerMetadata pm = umeta.remove(player);
 		for (String s : pm.getChannels()) {
 			try {
 				cmeta.get(s).players.remove(pm);
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	public void sendIrcNickchange(IrcUser u, String oldnick) {
 		ArrayList<PlayerMetadata> nickSend = new ArrayList<PlayerMetadata>();
 		for (ChannelMetadata cm : cmeta.values()) {
 			if (!cm.ircuser.contains(u)) continue;
 			for (PlayerMetadata pm : cm.players) {
 				if (nickSend.contains(pm)) continue;
 				pm.getPlayer().sendMessage(ChatColor.YELLOW + "[" + oldnick + " is now known as " + u.getNick() + "]");
 			}
 		}
 	}
 
 	public void sendIrcAction(String nick, PlayerMetadata pm, String message) {
 		pm.getPlayer().sendMessage("(MSG) * " + nick + " " + message);
 	}
 
 }
