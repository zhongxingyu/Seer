 package net.slipcor.pvparena.arenas.spleef;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaPlayer.Status;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.managers.Arenas;
 import net.slipcor.pvparena.managers.Spawns;
 import net.slipcor.pvparena.neworder.ArenaType;
 import net.slipcor.pvparena.runnables.EndRunnable;
 
 public class Spleef extends ArenaType {
 
 	public Spleef() {
 		super("spleef");
 	}
 
 	@Override
 	public String version() {
		return "v0.8.8.0";
 	}
 
 	@Override
 	public void addDefaultTeams(YamlConfiguration config) {
 		if (arena.cfg.get("teams") == null) {
 			arena.cfg.getYamlConfiguration().addDefault("teams.free",
 					ChatColor.WHITE.name());
 		}
 	}
 
 	@Override
 	public boolean checkAndCommit() {
 		db.i("[FREE]");
 
 		ArenaPlayer activePlayer = null;
 		for (ArenaPlayer p : arena.getPlayers()) {
 			if (p.getStatus().equals(Status.FIGHT)) {
 				if (activePlayer != null) {
 					db.i("more than one player active => no end :p");
 					return false;
 				}
 				activePlayer = p;
 			}
 		}
 
 		if (activePlayer != null) {
 
 			PVPArena.instance.getAmm().announceWinner(
 					arena,
 					Language.parse("playerhaswon", ChatColor.WHITE
 							+ activePlayer.getName()));
 			arena.tellEveryone(Language.parse("playerhaswon", ChatColor.WHITE
 					+ activePlayer.getName()));
 		}
 		EndRunnable er = new EndRunnable(arena, arena.cfg.getInt("goal.endtimer"),0);
 		arena.REALEND_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance,
 				er, 20L, 20L);
 		er.setId(arena.REALEND_ID);
 		return true;
 	}
 
 	@Override
 	public String checkSpawns(Set<String> list) {
 		if (!list.contains("lounge"))
 			return "lounge not set";
 		Iterator<String> iter = list.iterator();
 		int spawns = 0;
 		while (iter.hasNext()) {
 			String s = iter.next();
 			if (s.startsWith("spawn"))
 				spawns++;
 		}
 		if (spawns > 3) {
 			return null;
 		}
 
 		return "not enough spawns (" + spawns + ")";
 	}
 
 	@Override
 	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
 		db.i("committing command in FFA");
 		if (!(sender instanceof Player)) {
 			Language.parse("onlyplayers");
 			return;
 		}
 
 		Player player = (Player) sender;
 		
 		if (!PVPArena.hasAdminPerms(player)
 				&& !(PVPArena.hasCreatePerms(player, arena))) {
 			Arenas.tellPlayer(player,
 					Language.parse("nopermto", Language.parse("admin")), arena);
 			return;
 		}
 
 		String cmd = args[0];
 		if (cmd.equalsIgnoreCase("lounge")) {
 			Spawns.setCoords(arena, player, "lounge");
 			Arenas.tellPlayer(player, Language.parse("setlounge"));
 			return;
 		}
 
 		if (cmd.startsWith("spawn")) {
 			Spawns.setCoords(arena, player, cmd);
 			Arenas.tellPlayer(player, Language.parse("setspawn", cmd));
 			return;
 		}
 	}
 
 	@Override
 	public void configParse() {
 		db.i("FreeFight Arena default overrides");
 
 		arena.cfg.set("game.teamKill", false);
 		arena.cfg.set("join.manual", false);
 		arena.cfg.set("join.random", true);
 		arena.cfg.set("game.woolHead", false);
 		arena.cfg.set("join.forceeven", false);
 		arena.cfg.set("arenatype.randomSpawn", true);
 		arena.cfg.set("teams", null);
 		arena.cfg.set("teams.free", "WHITE");
 		arena.cfg.save();
 	}
 
 	public HashSet<String> getAddedSpawns() {
 		HashSet<String> result = new HashSet<String>();
 
 		result.add("spawn");
 		result.add("lounge");
 
 		return result;
 	}
 
 	private void getFreeSpawn(Player player, String string) {
 		// calculate a free spawn, if applicable
 		if (arena.playerCount < 1) {
 			// arena empty, randomly put player
 			arena.tpPlayerToCoordName(player, string);
 			return;
 		}
 
 		HashSet<Location> spawns = Spawns.getSpawns(arena, "free");
 		if (arena.playerCount >= spawns.size()) {
 			// full anyways, randomly put player
 
 			arena.tpPlayerToCoordName(player, string);
 			return;
 		}
 
 		// calculate "most free"
 		int i = 0;
 		for (Location loc : spawns) {
 			i++;
 			if (i % spawns.size() != arena.playerCount % spawns.size()) {
 				continue;
 			}
 			ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
 			
 			ap.setTelePass(true);
 			player.teleport(loc);
 			ap.setTelePass(false);
 			
 			return;
 		}
 	}
 
 	@Override
 	public String guessSpawn(String place) {
 		if (!place.contains("spawn")) {
 			db.i("place not found!");
 			return null;
 		}
 		// no exact match: assume we have multiple spawnpoints
 		HashMap<Integer, String> locs = new HashMap<Integer, String>();
 		int i = 0;
 
 		db.i("searching for team spawns");
 
 		HashMap<String, Object> coords = (HashMap<String, Object>) arena.cfg
 				.getYamlConfiguration().getConfigurationSection("spawns")
 				.getValues(false);
 		for (String name : coords.keySet()) {
 			if (name.startsWith(place)) {
 				locs.put(i++, name);
 				db.i("found match: " + name);
 			}
 		}
 
 		if (locs.size() < 1) {
 			return null;
 		}
 		Random r = new Random();
 
 		place = locs.get(r.nextInt(locs.size()));
 
 		return place;
 	}
 
 	@Override
 	public void initiate() {
 		arena.playerCount = 0;
 		for (ArenaTeam team : arena.getTeams()) {
 			for (ArenaPlayer ap : team.getTeamMembers()) {
 				getFreeSpawn(ap.get(), "spawn");
 				ap.setStatus(Status.FIGHT);
 				arena.lives.put(ap.getName(),
 						arena.cfg.getInt("game.lives", 3));
 				arena.playerCount++;
 			}
 		}
 	}
 
 	@Override
 	public void initLanguage(YamlConfiguration config) {
 		config.addDefault("lang.youjoinedfree",
 				"Welcome to the FreeFight Arena");
 		config.addDefault("lang.playerjoinedfree",
 				"%1% has joined the FreeFight Arena");
 		config.addDefault("lang.playerhaswon", "%1% is the Champion!");
 	}
 	
 	@Override
 	public boolean isFreeForAll() {
 		return true;
 	}
 
 	@Override
 	public boolean parseCommand(String cmd) {
 		return cmd.equalsIgnoreCase("lounge") || cmd.startsWith("spawn");
 	}
 
 	public void parseRespawn(Player respawnPlayer, ArenaTeam respawnTeam,
 			int lives, DamageCause cause, Entity damager) {
 
 		arena.tellEveryone(Language.parse("killedbylives",
 				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
 				arena.parseDeathCause(respawnPlayer, cause, damager),
 				String.valueOf(lives)));
 		arena.lives.put(respawnPlayer.getName(), lives);
 		arena.tpPlayerToCoordName(respawnPlayer, "spawn");
 	}
 
 	@Override
 	public int ready(Arena arena) {
 		db.i("ready(): reading playerteammap");
 		for (ArenaTeam team : arena.getTeams()) {
 			if (team.getTeamMembers().size() < 1) {
 				db.i("skipping TEAM " + team.getName());
 				continue;
 			}
 			db.i("TEAM " + team.getName());
 			if (arena.cfg.getInt("ready.minTeam") > 0
 					&& team.getTeamMembers().size() < arena.cfg
 							.getInt("ready.minTeam")) {
 				return -3;
 			}
 		}
 		return 1;
 	}
 
 	@Override
 	public void timed() {
 		int i;
 		int max = -1;
 
 		HashSet<String> result = new HashSet<String>();
 		db.i("timed end!");
 
 		for (String sPlayer : arena.lives.keySet()) {
 			i = arena.lives.get(sPlayer);
 
 			if (i > max) {
 				result = new HashSet<String>();
 				result.add(sPlayer);
 				max = i;
 			} else if (i == max) {
 				result.add(sPlayer);
 			}
 
 		}
 
 		for (ArenaPlayer p : arena.getPlayers()) {
 			if (!p.getStatus().equals(Status.FIGHT)) {
 				continue;
 			}
 			if (!result.contains(p.getName())) {
 				p.losses++;
 				arena.tpPlayerToCoordName(p.get(), "spectator");
 			} else {
 				PVPArena.instance.getAmm().announceWinner(arena, p.getName());
 				arena.tellEveryone(Language.parse("playerhaswon",
 						"f" + p.getName() + "e"));
 			}
 		}
 
 		PVPArena.instance.getAmm().timedEnd(arena, result);
 		EndRunnable er = new EndRunnable(arena, arena.cfg.getInt("goal.endtimer"),0);
 		arena.REALEND_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance,
 				er, 20L, 20L);
 		er.setId(arena.REALEND_ID);
 	}
 }
