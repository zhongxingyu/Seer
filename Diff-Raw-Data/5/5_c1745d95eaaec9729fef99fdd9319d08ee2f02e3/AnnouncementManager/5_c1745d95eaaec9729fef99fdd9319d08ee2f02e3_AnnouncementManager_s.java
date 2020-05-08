 package net.slipcor.pvparena.modules.announcements;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.commands.AbstractArenaCommand;
 import net.slipcor.pvparena.core.Debug;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.managers.TeamManager;
 import net.slipcor.pvparena.loadables.ArenaModule;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 public class AnnouncementManager extends ArenaModule {
 
 	public AnnouncementManager() {
 		super("Announcements");
 		debug = new Debug(400);
 	}
 
 	@Override
 	public String version() {
		return "v1.0.2.146";
 	}
 
 	@Override
 	public void announce(String message, String type) {
 		Announcement.announce(arena, Announcement.type.valueOf(type), message);
 	}
 
 	@Override
 	public boolean checkCommand(String s) {
 		return s.equals("!aa") || s.startsWith("announce");
 	}
 
 	@Override
 	public void commitCommand(CommandSender sender, String[] args) {
 		// !aa [type]
 
 		if (!PVPArena.hasAdminPerms(sender)
 				&& !(PVPArena.hasCreatePerms(sender, arena))) {
 			arena.msg(
 					sender,
 					Language.parse(MSG.ERROR_NOPERM,
 							Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
 			return;
 		}
 
 		if (!AbstractArenaCommand.argCountValid(sender, arena, args,
 				new Integer[] { 2 })) {
 			return;
 		}
 
 		if (args[0].equals("!aa") || args[0].startsWith("announce")) {
 
 			for (Announcement.type t : Announcement.type.values()) {
 				if (t.name().equalsIgnoreCase(args[1])) {
 					boolean b = arena.getArenaConfig().getBoolean(
 							CFG.valueOf("MODULES_ANNOUNCEMENTS_" + t.name()));
 					arena.getArenaConfig().set(
 							CFG.valueOf("MODULES_ANNOUNCEMENTS_" + t.name()),
 							!b);
 					arena.getArenaConfig().save();
 
 					arena.msg(
 							sender,
 							Language.parse(MSG.SET_DONE, t.name(),
 									String.valueOf(!b)));
 					return;
 				}
 			}
 
 			String list = StringParser.joinArray(Announcement.type.values(),
 					", ");
 			arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], list));
 			return;
 		}
 	}
 
 	@Override
 	public void parsePlayerDeath(Player player, EntityDamageEvent cause) {
 		Announcement.announce(arena, Announcement.type.LOSER, Language.parse(
 				MSG.FIGHT_KILLED_BY,
 				player.getName(),
 				arena.parseDeathCause(player, cause.getCause(),
						ArenaPlayer.getLastDamagingPlayer(cause))));
 	}
 
 	@Override
 	public void displayInfo(CommandSender player) {
 		player.sendMessage("");
 		player.sendMessage("radius: "
 				+ StringParser.colorVar(arena.getArenaConfig().getInt(
 						CFG.MODULES_ANNOUNCEMENTS_RADIUS, 0))
 				+ " || color: "
 				+ StringParser.colorVar(arena.getArenaConfig().getString(
 						CFG.MODULES_ANNOUNCEMENTS_COLOR)));
 		player.sendMessage(StringParser.colorVar("advert", arena.getArenaConfig()
 						.getBoolean(CFG.MODULES_ANNOUNCEMENTS_ADVERT))
 				+ " || "
 				+ StringParser.colorVar("custom", arena.getArenaConfig()
 						.getBoolean(CFG.MODULES_ANNOUNCEMENTS_CUSTOM))
 				+ " || "
 				+ StringParser.colorVar("end", arena.getArenaConfig()
 						.getBoolean(CFG.MODULES_ANNOUNCEMENTS_END))
 				+ " || "
 				+ StringParser.colorVar("join", arena.getArenaConfig()
 						.getBoolean(CFG.MODULES_ANNOUNCEMENTS_JOIN))
 				+ " || "
 				+ StringParser.colorVar("loser", arena.getArenaConfig()
 						.getBoolean(CFG.MODULES_ANNOUNCEMENTS_LOSER))
 				+ " || "
 				+ StringParser.colorVar("prize", arena.getArenaConfig()
 						.getBoolean(CFG.MODULES_ANNOUNCEMENTS_PRIZE))
 				+ " || "
 				+ StringParser.colorVar("start", arena.getArenaConfig()
 						.getBoolean(CFG.MODULES_ANNOUNCEMENTS_START))
 				+ " || "
 				+ StringParser.colorVar("winner", arena.getArenaConfig()
 						.getBoolean(CFG.MODULES_ANNOUNCEMENTS_WINNER)));
 	}
 
 	@Override
 	public void parseJoin(CommandSender sender, ArenaTeam team) {
 
 		debug.i("parseJoin ... ", sender);
 
 		if (TeamManager.countPlayersInTeams(arena) < 2) {
 			Announcement.announce(arena, Announcement.type.ADVERT, Language
 					.parse(MSG.ANNOUNCE_ARENA_STARTING, arena.getName()));
 		}
 
 		if (arena.isFreeForAll()) {
 			Announcement.announce(arena, Announcement.type.JOIN,
 					arena.getArenaConfig().getString(CFG.MSG_PLAYERJOINED)
 							.replace("%1%", sender.getName()));
 		} else {
 			Announcement.announce(
 					arena,
 					Announcement.type.JOIN,
 					arena.getArenaConfig().getString(CFG.MSG_PLAYERJOINEDTEAM)
 							.replace("%1%", sender.getName())
 							.replace("%2%", team.getColoredName()));
 		}
 	}
 
 	@Override
 	public void parsePlayerLeave(Player player, ArenaTeam team) {
 		if (team == null) {
 			Announcement.announce(arena, Announcement.type.LOSER,
 					Language.parse(MSG.FIGHT_PLAYER_LEFT, player.getName()));
 		} else {
 			Announcement.announce(
 					arena,
 					Announcement.type.LOSER,
 					Language.parse(MSG.FIGHT_PLAYER_LEFT,
 							team.colorizePlayer(player)));
 		}
 	}
 
 	@Override
 	public void parseStart() {
 		Announcement.announce(arena, Announcement.type.START,
 				Language.parse(MSG.FIGHT_BEGINS));
 	}
 }
