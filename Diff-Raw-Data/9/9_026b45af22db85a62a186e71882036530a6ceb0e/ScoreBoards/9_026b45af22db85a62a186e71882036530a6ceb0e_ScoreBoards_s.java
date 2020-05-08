 package net.slipcor.pvparena.modules.scoreboards;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitTask;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 import org.bukkit.scoreboard.Team;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.classes.PACheck;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.loadables.ArenaModule;
 
 public class ScoreBoards extends ArenaModule {
 	private boolean setup = false;
 	private static ScoreboardManager sbm = null;
 	
 	private Scoreboard board = null;
 	Objective obj = null;
 	Objective oBM = null;
 	Objective oTB = null;
 	private BukkitTask updateTask = null;
 	
 	Map<String, Scoreboard> playerBoards = new HashMap<String, Scoreboard>();
 	
 	public ScoreBoards() {
 		super("ScoreBoards");
 	}
 	
 	@Override
 	public String version() {
		return "v1.1.0.329";
 	}
 
 	
 	@Override
 	public void configParse(YamlConfiguration config) {
 		if (sbm == null) {
 			sbm = Bukkit.getScoreboardManager();
 		}
 		if (setup) {
 			return;
 		}
 		Bukkit.getPluginManager().registerEvents(new PAListener(this), PVPArena.instance);
 		setup = true;
 	}
 
 	public void update(Player player) {
 		if (board == null || obj == null) {
 			return;
 		}
 		if (arena.isFreeForAll()) {
 			Score score = obj.getScore(player);
 			score.setScore(PACheck.handleGetLives(arena, ArenaPlayer.parsePlayer(player.getName())));
 		} else {
 			ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
 			if (ap.getArenaTeam() == null) {
 				return;
 			}
 			OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(ap.getArenaTeam().getName());
 			obj.getScore(op).setScore(PACheck.handleGetLives(arena, ap));
 		}
 		player.setScoreboard(board);
 		
 		if (!block) {
 			class RunLater implements Runnable {
 
 				@Override
 				public void run() {
 					update();
 				}
 				
 			}
 			Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
 		}
 	}
 
 	private boolean block = false;
 	
 	private void update() {
 		block = true;
 		for (ArenaPlayer player : arena.getEveryone()) {
 			update(player.get());
 		}
 		block = false;
 		
 	}
 	
 	@Override
 	public void resetPlayer(Player player, boolean force) {
 		remove(player);
 	}
 
 	public void stop(Arena arena) {
 		if (board == null) {
 			return;
 		}
 		for (OfflinePlayer player : board.getPlayers()) {
 			if (player != null) {
 				board.resetScores(player);
 			}
 		}
 		obj.unregister();
 		obj = null;
 		board.clearSlot(DisplaySlot.SIDEBAR);
 		board = null;
 		
 		if (updateTask != null) {
 			updateTask.cancel();
 			updateTask = null;
 		}
 	}
 
 	public void remove(Player player) {
 		// after runnable: remove player's scoreboard, remove player from scoreboard
 		// and update all players' scoreboards
 		arena.getDebugger().i("ScoreBoards: remove: " + player.getName(), player);
 		
 		try {
 			boolean found = false;
 			for (Team team : board.getTeams()) {
 				if (team.hasPlayer(player)) {
 					team.removePlayer(player);
 					board.resetScores(player);
 					found = true;
 				}
 			}
 			if (!found) {
 				board.resetScores(player);
 			}
 		} catch (Exception e) {
 			
 		}
 		if (playerBoards.containsKey(player.getName())) {
 			player.setScoreboard(playerBoards.get(player.getName()));
 		} else {
 			player.setScoreboard(sbm.getMainScoreboard());
 		}
 	}
 
 	public void add(final Player player) {
 		// after runnable: add player to scoreboard, resend all scoreboards
 		
 		playerBoards.put(player.getName(), player.getScoreboard());
 		
 		// first, check if the scoreboard exists
 		class RunLater implements Runnable {
 
 			@Override
 			public void run() {
 				
 				if (board == null) {
 
 					board = sbm.getNewScoreboard();
 					
 					oBM = sbm.getMainScoreboard().getObjective(DisplaySlot.BELOW_NAME);
 					if (oBM != null) {
 						oBM = board.registerNewObjective(oBM.getCriteria(), oBM.getDisplayName());
 						oBM.setDisplaySlot(DisplaySlot.BELOW_NAME);
 						
 					}
 					
 					oTB = sbm.getMainScoreboard().getObjective(DisplaySlot.PLAYER_LIST);
 					if (oTB != null) {
 						oTB = board.registerNewObjective(oTB.getCriteria(), oTB.getDisplayName());
 						oTB.setDisplaySlot(DisplaySlot.PLAYER_LIST);
 					}
 					
 					for (ArenaTeam team : arena.getTeams()) {
 						
 						try {
 							board.registerNewTeam(team.getName());
 							Team bukkitTeam = board.getTeam(team.getName());
 							bukkitTeam.setPrefix(team.getColor().toString());
 							OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(team.getName());
 							bukkitTeam.addPlayer(op);
 							bukkitTeam.setAllowFriendlyFire(arena.getArenaConfig().getBoolean(CFG.PERMS_TEAMKILL));
 							
 							bukkitTeam.setCanSeeFriendlyInvisibles(
 									!arena.isFreeForAll()
 									);
 						} catch (Exception e) {
 							
 						}
 						
 						if (team == ArenaPlayer.parsePlayer(player.getName()).getArenaTeam()) {
 							board.getTeam(team.getName()).addPlayer(player);
 						}
 					}
 					
 					if (board.getObjective("lives") != null) {
 						board.getObjective("lives").unregister();
 						if (board.getObjective(DisplaySlot.SIDEBAR) != null) {
 							board.getObjective(DisplaySlot.SIDEBAR).unregister();
 						}
 					}
 					
 					obj = board.registerNewObjective("lives", "dummy"); //deathCount
 					
 					obj.setDisplayName(ChatColor.GREEN+"PVP Arena"+ChatColor.RESET+" - " + ChatColor.YELLOW + arena.getName());
 					obj.setDisplaySlot(DisplaySlot.SIDEBAR);
 					
 					update(player);
 				} else {
 					ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
 					if (aPlayer.getArenaTeam() != null) {
 						board.getTeam(aPlayer.getArenaTeam().getName()).addPlayer(player);
 					}
 					update(player);
 				}
 			}
 			
 		}
 		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
 	}
 
 	public void start() {
 		
 		for (ArenaPlayer player : arena.getEveryone()) {
 			add(player.get());
 		}
 		
 		class RunLater implements Runnable {
 
 			@Override
 			public void run() {
 				update();
 			}
 			
 		}
 		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
 		
 		class UpdateTask implements Runnable {
 			private final ScoreBoards mod;
 			UpdateTask(ScoreBoards mod) {
 				this.mod = mod;
 			}
 			
 			@Override
 			public void run() {
 				mod.update();
 			}
 			
 		}
 		
 		updateTask = Bukkit.getScheduler().runTaskTimer(PVPArena.instance, new UpdateTask(this), 50, 50);
 	}
 	
 	
 }
