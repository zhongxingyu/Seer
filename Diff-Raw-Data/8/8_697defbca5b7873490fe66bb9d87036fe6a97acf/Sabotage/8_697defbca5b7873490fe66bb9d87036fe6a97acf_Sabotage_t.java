 package net.slipcor.pvparena.arenas.sabotage;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.arena.ArenaPlayer.Status;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.listeners.PlayerListener;
 import net.slipcor.pvparena.managers.Arenas;
 import net.slipcor.pvparena.managers.Spawns;
 import net.slipcor.pvparena.managers.Teams;
 import net.slipcor.pvparena.neworder.ArenaType;
 import net.slipcor.pvparena.runnables.EndRunnable;
 
 public class Sabotage extends ArenaType {
 	/**
 	 * TeamName => PlayerName
 	 */
 	public HashMap<String, String> paTeamFlags = null;
 
 	public Sabotage() {
 		super("sabotage");
 	}
 	
 	@Override
 	public String version() {
		return "v0.8.10.0";
 	}
 	
 	@Override
 	public void addDefaultTeams(YamlConfiguration config) {
 		config.addDefault("game.woolHead", Boolean.valueOf(false));
 		if (arena.cfg.get("teams") == null) {
 			db.i("no teams defined, adding custom red and blue!");
 			arena.cfg.getYamlConfiguration().addDefault("teams.red",
 					ChatColor.RED.name());
 			arena.cfg.getYamlConfiguration().addDefault("teams.blue",
 					ChatColor.BLUE.name());
 		}
 	}
 
 	@Override
 	public boolean allowsJoinInBattle() {
 		return arena.cfg.getBoolean("join.inbattle");
 	}
 
 	@Override
 	public boolean checkAndCommit() {
 		db.i("[FLAG]");
 		
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
 		
 		if (activePlayer == null) {
 			commit("$%&/", true);
 			return false;
 		}
 		
 		ArenaTeam team = Teams.getTeam(arena, activePlayer);
 		
 		if (team == null) {
 			commit("$%&/", true);
 			return false;
 		}
 		
 		commit(team.getName(), true);
 		return true;
 	}
 
 	@Override
 	public void checkEntityDeath(Player player) {
 		db.i("checking death in a ctf arena");
 
 		ArenaTeam flagTeam = Teams.getTeam(arena,
 				getHeldFlagTeam(player.getName()));
 		if (flagTeam != null) {
 			ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
 			paTeamFlags.remove(flagTeam.getName());
 			distributeFlag(ap, flagTeam);
 		}
 	}
 
 	@Override
 	public void checkInteract(Player player, Block block) {
 		if (block == null) {
 			return;
 		}
 		db.i("checking interact");
 
 		if (!block.getType().equals(Material.TNT)) {
 			db.i("flag & not flag");
 			return;
 		}
 		db.i("tnt click!");
 
 		Vector vLoc;
 		Vector vFlag = null;
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
 
 		if (paTeamFlags.containsValue(player.getName())) {
 			ArenaTeam pTeam = Teams.getTeam(arena, ap);
 			if (pTeam == null) {
 				return;
 			}
 			for (ArenaTeam team : arena.getTeams()) {
 				String aTeam = team.getName();
 
 				if (aTeam.equals(pTeam.getName()))
 					continue;
 				if (team.getTeamMembers().size() < 1)
 					continue; // dont check for inactive teams
 				
 				db.i("checking for tnt of team " + aTeam);
 				vLoc = block.getLocation().toVector();
 				db.i("block: " + vLoc.toString());
 				if (Spawns.getSpawns(arena, aTeam + "flag").size() > 0) {
 					vFlag = Spawns.getNearest(
 							Spawns.getSpawns(arena, aTeam + "flag"),
 							player.getLocation()).toVector();
 				}
 				if ((vFlag != null) && (vLoc.distance(vFlag) < 2)) {
 					db.i("tnt found!");
 					db.i("vFlag: " + vFlag.toString());
 					
 					HashSet<ArenaPlayer> plrs = team.getTeamMembers();
 					
 					for (ArenaPlayer loser : plrs) {
 						EntityDamageEvent e = new EntityDamageEvent(loser.get(), DamageCause.VOID, 10);
 						PlayerListener.commitPlayerDeath(arena, loser.get(), e);
 					}
 					
 					return;
 				}
 			}
 		}
 	}
 
 	@Override
 	protected boolean checkSetFlag(Player player, Block block) {
 		if (Arena.regionmodify.equals("")) {
 			return false;
 		}
 		setFlag(player, block);
 		if (Arena.regionmodify.equals("")) {
 			return true; // success :)
 		}
 		return false;
 	}
 
 	@Override
 	public String checkSpawns(Set<String> list) {
 		for (ArenaTeam team : arena.getTeams()) {
 			String sTeam = team.getName();
 			if (!list.contains(team + "flag")) {
 				boolean found = false;
 				for (String s : list) {
 					if (s.startsWith(sTeam) && s.endsWith("flag")) {
 						found = true;
 						break;
 					}
 				}
 				if (!found)
 					return team.getName() + "tnt not set";
 			}
 		}
 		return super.checkSpawns(list);
 	}
 
 	private void commit(String sTeam, boolean win) {
 		db.i("[SABOTAGE] committing end: " + sTeam);
 		db.i("win: " + String.valueOf(win));
 
 		String winteam = sTeam;
 
 		for (ArenaTeam team : arena.getTeams()) {
 			if (team.getName().equals(sTeam) == win) {
 				continue;
 			}
 			for (ArenaPlayer ap : team.getTeamMembers()) {
 
 				ap.losses++;
 				arena.tpPlayerToCoordName(ap.get(), "spectator");
 				ap.setTelePass(false);
 			}
 		}
 		for (ArenaTeam team : arena.getTeams()) {
 			for (ArenaPlayer ap : team.getTeamMembers()) {
 				if (!ap.getStatus().equals(Status.FIGHT)) {
 					continue;
 				}
 				winteam = team.getName();
 				break;
 			}
 		}
 
 		if (Teams.getTeam(arena, winteam) != null) {
 			PVPArena.instance.getAmm().announceWinner(arena,
 					Language.parse("teamhaswon", "Team " + winteam));
 			arena.tellEveryone(Language.parse("teamhaswon",
 					Teams.getTeam(arena, winteam).getColor() + "Team "
 							+ winteam));
 		}
 
 		arena.lives.clear();
 		EndRunnable er = new EndRunnable(arena, arena.cfg.getInt("goal.endtimer"),0);
 		arena.REALEND_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance,
 				er, 20L, 20L);
 		er.setId(arena.REALEND_ID);
 	}
 	
 	@Override
 	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
 		if (!(sender instanceof Player)) {
 			Language.parse("onlyplayers");
 			return;
 		}
 		
 		System.out.print(StringParser.parseArray(args));
 
 		Player player = (Player) sender;
 		
 		if (!PVPArena.hasAdminPerms(player)
 				&& !(PVPArena.hasCreatePerms(player, arena))) {
 			Arenas.tellPlayer(player,
 					Language.parse("nopermto", Language.parse("admin")), arena);
 			return;
 		}
 		
 		if (args[0].startsWith("spawn") || args[0].equals("spawn")) {
 			Arenas.tellPlayer(sender, Language.parse("errorspawnfree", args[0]),
 					arena);
 			return;
 		}
 
 		if (args[0].contains("spawn")) {
 			String[] split = args[0].split("spawn");
 			String sName = split[0];
 			if (Teams.getTeam(arena, sName) == null) {
 				Arenas.tellPlayer(sender, Language.parse("arenateamunknown", sName), arena);
 				return;
 			}
 
 			Spawns.setCoords(arena, player, args[0]);
 			Arenas.tellPlayer(player, Language.parse("setspawn", sName), arena);
 			return;
 		}
 		
 		if (args[0].equals("lounge")) {
 			Arenas.tellPlayer(sender, Language.parse("errorloungefree", args[0]),
 					arena);
 			return;
 		}
 
 		if (args[0].contains("lounge")) {
 			String[] split = args[0].split("lounge");
 			String sName = split[0];
 			if (Teams.getTeam(arena, sName) == null) {
 				Arenas.tellPlayer(sender, Language.parse("arenateamunknown", sName), arena);
 				return;
 			}
 
 			Spawns.setCoords(arena, player, args[0]);
 			Arenas.tellPlayer(player, Language.parse("loungeset", sName), arena);
 			return;
 		}
 		Arena.regionmodify = arena.name + ":" + args[0];
 		Arenas.tellPlayer(sender, Language.parse("tosettnt", args[0]));
 	}
 
 	@Override
 	public void configParse() {
 		paTeamFlags = new HashMap<String, String>();
 	}
 
 	private void distributeFlag(ArenaPlayer player, ArenaTeam team) {
 		HashSet<ArenaPlayer> players = team.getTeamMembers();
 		
 		int i = (new Random()).nextInt(players.size());
 		
 		for (ArenaPlayer ap : players) {
 			db.i("distributing " + ap.getName());
 			if (ap.equals(player)) {
 				continue;
 			}
 			if (--i <= 1) {
 				paTeamFlags.put(team.getName(), ap.getName());
 				ap.get().getInventory().addItem(new ItemStack(Material.FLINT_AND_STEEL, 1));
 				ap.get().getInventory().addItem(new ItemStack(Material.TNT, 1));
 				Arenas.tellPlayer(ap.get(), Language.parse("youtnt"));
 				return;
 			}
 		}
 	}
 	
 	@Override
 	public int getLives(Player defender) {
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(defender);
 		ArenaTeam team = Teams.getTeam(arena,ap);
 		if (team == null) {
 			System.out.print("[WARNING] player in null team: " + ap.getName());
 			return 1;
 		}
 		if (arena.lives.get(team.getName()) == null) {
 			return 1;
 		}
 		return 1;
 	}
 
 	/**
 	 * get the team name of the flag a player holds
 	 * 
 	 * @param player
 	 *            the player to check
 	 * @return a team name
 	 */
 	protected String getHeldFlagTeam(String player) {
 		db.i("getting held FLAG of player " + player);
 		for (String sTeam : paTeamFlags.keySet()) {
 			db.i("team " + sTeam + " is in " + paTeamFlags.get(sTeam)
 					+ "s hands");
 			if (player.equals(paTeamFlags.get(sTeam))) {
 				return sTeam;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String guessSpawn(String place) {
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
 			if (name.endsWith("flag")) {
 				for (ArenaTeam team : arena.getTeams()) {
 					String sTeam = team.getName();
 					if (name.startsWith(sTeam)) {
 						locs.put(i++, name);
 						db.i("found match: " + name);
 					}
 				}
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
 		db.i("initiating arena");
 		arena.lives.clear();
 		for (ArenaTeam team : arena.getTeams()) {
 			if (team.getTeamMembers().size() > 0) {
 				db.i("adding team " + team.getName());
 				// team is active
 				arena.lives.put(team.getName(), 0);
 				distributeFlag(null, team);
 			}
 		}
 	}
 
 	@Override
 	public void initLanguage(YamlConfiguration config) {
 		config.addDefault("lang.killedby", "%1% has been killed by %2%!");
 		config.addDefault("lang.tosettnt", "TNT to set: %1%");
 		config.addDefault("lang.settnt", "TNT set: %1%");
 		config.addDefault("lang.youtnt",
 				"You now carry the sabotage materials!'");
 	}
 
 	@Override
 	public boolean parseCommand(String s) {
 		if (s.contains("spawn")) {
 			String[] split = s.split("spawn");
 			String sName = split[0];
 			if (Teams.getTeam(arena, sName) == null) {
 				return false;
 			}
 			return true;
 		}
 		if (s.contains("lounge")) {
 			String[] split = s.split("lounge");
 			String sName = split[0];
 			if (Teams.getTeam(arena, sName) == null) {
 				return false;
 			}
 			return true;
 		}
		if (!s.contains("tnt")) {
 			return false;
 		}
		String sName = s.replace("tnt", "");
 
 
 		for (ArenaTeam team : arena.getTeams()) {
 			String sTeam = team.getName();
 			if (sName.startsWith(sTeam)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	@Override
 	public void parseRespawn(Player respawnPlayer, ArenaTeam respawnTeam,
 			int lives, DamageCause cause, Entity damager) {
 		arena.tellEveryone(Language.parse("killedby",
 				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
 				arena.parseDeathCause(respawnPlayer, cause, damager)));
 		arena.tpPlayerToCoordName(respawnPlayer, respawnTeam.getName()
 				+ "spawn");
 
 		checkEntityDeath(respawnPlayer);
 	}
 
 	@Override
 	public int reduceLives(Player player, int lives) {
 		return lives;
 	}
 
 	@Override
 	public boolean reduceLivesCheckEndAndCommit(String team) {
 		return false;
 	}
 
 	@Override
 	public void reset(boolean force) {
 		if (paTeamFlags != null) {
 			paTeamFlags.clear();
 		}
 	}
 
 	private void setFlag(Player player, Block block) {
 		if (block == null) {
 			return;
 		}
 		
 		if (!block.getType().equals(Material.TNT)) {
 			return;
 		}
 		
 		if (!PVPArena.hasAdminPerms(player)
 				&& !(PVPArena.hasCreatePerms(player, arena))) {
 			return;
 		}
 
 		db.i("trying to set a flag");
 
 		String sName = Arena.regionmodify.replace(arena.name + ":", "");
 
 		// command : /pa redflag1
 		// location: red1flag:
 
 		Spawns.setCoords(arena, block.getLocation(), sName + "flag");
 
 		Arenas.tellPlayer(player, Language.parse("settnt", sName));
 
 		Arena.regionmodify = "";
 	}
 
 	/**
 	 * hook into the timed end
 	 */
 	@Override
 	public void timed() {
 		int i;
 
 		int max = -1;
 		HashSet<String> result = new HashSet<String>();
 		db.i("timed end!");
 
 		for (String sTeam : arena.lives.keySet()) {
 			i = arena.lives.get(sTeam);
 
 			if (i > max) {
 				result = new HashSet<String>();
 				result.add(sTeam);
 				max = i;
 			} else if (i == max) {
 				result.add(sTeam);
 			}
 		}
 
 		// result hat die Teams mit dem hchsten lebenswert
 		
 		for (ArenaTeam team : arena.getTeams()) {
 			if (result.contains(team.getName())) {
 				PVPArena.instance.getAmm().announceWinner(arena,
 						Language.parse("teamhaswon", "Team " + team.getName()));
 				arena.tellEveryone(Language.parse("teamhaswon", team.getColor()
 						+ "Team " + team.getName()));
 			}
 			for (ArenaPlayer p : arena.getPlayers()) {
 				if (!p.getStatus().equals(Status.FIGHT)) {
 					continue;
 				}
 				if (!result.contains(team.getName())) {
 					p.losses++;
 					arena.tpPlayerToCoordName(p.get(), "spectator");
 				}
 			}
 		}
 
 		PVPArena.instance.getAmm().timedEnd(arena, result);
 		EndRunnable er = new EndRunnable(arena, arena.cfg.getInt("goal.endtimer"),0);
 		arena.REALEND_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance,
 				er, 20L, 20L);
 		er.setId(arena.REALEND_ID);
 	}
 	
 	@Override
 	public void unload(Player player) {
 		String flag = this.getHeldFlagTeam(player.getName());
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
 		if (flag != null) {
 			ArenaTeam flagTeam = Teams.getTeam(arena, flag);
 			paTeamFlags.remove(flag);
 			distributeFlag(ap, flagTeam);
 		}
 	}
 
 	@Override
 	public boolean usesFlags() {
 		return true;
 	}
 }
