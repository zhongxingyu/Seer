 package net.slipcor.pvparena.modules.colorteams;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.SpoutManager;
 
 import net.minecraft.server.EntityPlayer;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.managers.Arenas;
 import net.slipcor.pvparena.managers.Teams;
 import net.slipcor.pvparena.neworder.ArenaModule;
 
 public class CTManager extends ArenaModule {
 	protected static String spoutHandler = null;
 	private Field name = null;
 
 	public CTManager() {
 		super("ColorTeams");
 	}
 	
 	@Override
 	public String version() {
		return "v0.8.10.10";
 	}
 
 	@Override
 	public void addSettings(HashMap<String, String> types) {
 		types.put("game.hideName", "boolean");
		types.put("game.mustbesafe", "boolean");
		types.put("game.woolFlagHead", "boolean");
 		types.put("messages.colorNick", "boolean");
 		types.put("colors.requireVault", "boolean");
 	}
 
 	private void colorizePlayer(Arena a, Player player) {
 		db.i("colorizing player " + player.getName() + ";");
 
 		Arena arena = Arenas.getArenaByPlayer(player);
 		if (arena == null) {
 			db.i("> arena is null");
 			if (spoutHandler != null) {
 				SpoutManager.getPlayer(player).setTitle(player.getName());
 			} else if (!a.cfg.getBoolean("colors.requireVault")) {
 				disguise(player, player.getName());
 			}
 			return;
 		}
 
 		ArenaTeam team = Teams.getTeam(arena, ArenaPlayer.parsePlayer(player));
 		String n;
 		if (team == null) {
 			db.i("> team is null");
 			if (spoutHandler != null) {
 				SpoutManager.getPlayer(player).setTitle(player.getName());
 			} else if (!a.cfg.getBoolean("colors.requireVault")) {
 				disguise(player, player.getName());
 			}
 			return;
 		} else {
 			n = team.getColorString() + player.getName();
 		}
 		n = n.replaceAll("(&([a-f0-9]))", "$2");
 		
 		player.setDisplayName(n);
 
 		if (arena.cfg.getBoolean("game.hideName")) {
 			n = " ";
 		}
 		if (spoutHandler != null) {
 			SpoutManager.getPlayer(player).setTitle(n);
 		} else if (!a.cfg.getBoolean("colors.requireVault")) {
 			disguise(player, n);
 		}
 	}
 	
 	private void disguise(Player player, String name) {
 		String listName = name;
 		if(listName.length() >= 16) {
 			listName = listName.substring(0, 16);
 		}
 		// does this actually help?
 		player.setDisplayName(name);
 		try {
 			player.setPlayerListName(listName);
 		} catch (Exception e) {
 			// don't print the error
 		}
 		// then the rest
 		CraftPlayer cp = (CraftPlayer) player;
 		EntityPlayer ep = cp.getHandle();
 		ep.name = name;
 	}
 
 	@Override
 	public void configParse(Arena arena, YamlConfiguration config, String type) {
 		config.addDefault("game.hideName", Boolean.valueOf(false));
 		config.addDefault("messages.colorNick", Boolean.valueOf(true));
 		config.addDefault("colors.requireVault", Boolean.valueOf(false));
 		config.options().copyDefaults(true);
 	}
 	
 	@Override
 	public void initLanguage(YamlConfiguration config) {
 		config.addDefault("log.nospout",
 				"Spout not found, you are missing some features ;)");
 		config.addDefault("log.spout", "Hooking into Spout!");
 	}
 	
 	@Override
 	public void onEnable() {
 		if (Bukkit.getServer().getPluginManager().getPlugin("Spout") != null) {
 			spoutHandler = SpoutManager.getInstance().toString();
 		} else {
 			try {
 				// Grab the field
 				for(Field field : EntityPlayer.class.getFields()) {
 					if(field.getName().equalsIgnoreCase("name")) {
 						name = field;
 					}
 				}
 				name.setAccessible(true);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		Language.log_info((spoutHandler == null) ? "nospout" : "spout");
 	}
 
 	@Override
 	public void parseInfo(Arena arena, CommandSender player) {
 		player.sendMessage("");
 		player.sendMessage("6ColoredTeams:f "
 				+ StringParser.colorVar("hideName", arena.cfg.getBoolean("game.hideName"))
 				+ " || "
 				+ StringParser.colorVar("colorNick", arena.cfg.getBoolean("messages.colorNick"))
 				+ " || "
 				+ StringParser.colorVar("requireVaualt", arena.cfg.getBoolean("colors.requireVault")));
 	}
 
 	@Override
 	public void tpPlayerToCoordName(Arena arena, Player player, String place) {
 		if (arena.cfg.getBoolean("messages.colorNick", true)) {
 			if (spoutHandler != null) {
 				colorizePlayer(arena, player);	
 			} else {
 				ArenaTeam team = Teams.getTeam(arena, ArenaPlayer.parsePlayer(player));
 				String n;
 				if (team == null) {
 					db.i("> team is null");
 					n = player.getName();
 				} else {
 					n = team.getColorString() + player.getName();
 				}
 				n = n.replaceAll("(&([a-f0-9]))", "$2");
 				
 				player.setDisplayName(n);
 
 				if (team != null && arena.cfg.getBoolean("game.hideName")) {
 					n = " ";
 				}
 				
 				updateName(player, n);
 			}
 		}
 	}
 	
 	@Override
 	public void unload(Player player) {
 		if (spoutHandler != null) {
 			SpoutManager.getPlayer(player).setTitle(player.getName());
 		}
 	}
 	
 	public void updateName(Player player, String team) {
 		
 		// Update the name
 		disguise(player, team);
 		Player[] players = Bukkit.getOnlinePlayers();
 		for(Player p : players) {
 			if(p != player) {
 				// Refresh the packet!
 				p.hidePlayer(player);
 				p.showPlayer(player);
 			}
 		}
 		//setName(player, ChatColor.stripColor(n));
 	}
 }
