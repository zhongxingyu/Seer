 package me.limebyte.battlenight.core.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.battle.TeamedBattle;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.Team;
 
 public class BattleScoreboard {
 
     private Battle battle;
     private boolean teamed = false;
 
     private Scoreboard scoreboard;
     private Objective scores;
     private Objective belowName;
 
     private static Map<String, Scoreboard> scoreboards = new HashMap<String, Scoreboard>();
 
     private static final String TITLE_PREFIX = ChatColor.BOLD.toString() + ChatColor.GRAY;
     private static final String LOBBY_TITLE = TITLE_PREFIX + "Battle Lobby";
     private static final String BATTLE_TITLE = TITLE_PREFIX + "Battle (%1$TM:%1$TS)";
 
     public BattleScoreboard(Battle battle) {
         this.battle = battle;
         teamed = battle instanceof TeamedBattle;
 
         scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
 
         scores = scoreboard.registerNewObjective("bn_scores", "dummy");
         scores.setDisplaySlot(DisplaySlot.SIDEBAR);
         scores.setDisplayName(LOBBY_TITLE);
 
         belowName = scoreboard.registerNewObjective("bn_belowname", "dummy");
         belowName.setDisplaySlot(DisplaySlot.BELOW_NAME);
         belowName.setDisplayName("Kills");
     }
 
     public void addTeam(me.limebyte.battlenight.api.battle.Team team, boolean friendlyFire) {
         if (!teamed) return;
 
         Team t = scoreboard.registerNewTeam("bn_team_" + team.getName());
         t.setDisplayName(team.getDisplayName() + " Team");
         t.setPrefix(team.getColour().toString());
 
         t.setAllowFriendlyFire(friendlyFire);
         t.setCanSeeFriendlyInvisibles(true);
     }
 
     public void addPlayer(Player player) {
         scoreboards.put(player.getName(), player.getScoreboard());
         player.setScoreboard(scoreboard);
 
         if (teamed) {
             String teamName = ((TeamedBattle) battle).getTeam(player).getName();
             for (Team team : scoreboard.getTeams()) {
                 if (team.getName().equals("bn_team_" + teamName)) {
                     team.addPlayer(player);
                     break;
                 }
 
             }
         }
 
         scores.getScore(player).setScore(0);
     }
 
     public void removePlayer(Player player) {
         if (!scoreboard.getPlayers().contains(player)) return;
 
         Team team = scoreboard.getPlayerTeam(player);
         if (team != null) team.removePlayer(player);
 
        scoreboard.resetScores(player);

         String name = player.getName();
        Scoreboard board = scoreboards.get(name);
        if (board != null) player.setScoreboard(board);
         scoreboards.remove(name);
     }
 
     public void updateScore(Player player) {
         int score = (int) Math.round(battle.getKDR(player) * 100);
         int kills = battle.getKills(player);
 
         scores.getScore(player).setScore(score);
         belowName.getScore(player).setScore(kills);
     }
 
     public void updateTime(long time) {
         scores.setDisplayName(String.format(BATTLE_TITLE, time * 1000));
     }
 
 }
