 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.Team;
 
 import com.github.ucchyocean.ct.bridge.VaultChatBridge;
 import com.github.ucchyocean.ct.config.ClassData;
 import com.github.ucchyocean.ct.config.ColorTeamingConfig;
 import com.github.ucchyocean.ct.config.RespawnConfiguration;
 import com.github.ucchyocean.ct.config.TPPointConfiguration;
 import com.github.ucchyocean.ct.config.TeamNameConfig;
 import com.github.ucchyocean.ct.config.TeamNameSetting;
 import com.github.ucchyocean.ct.event.ColorTeamingKillDeathClearedEvent;
 import com.github.ucchyocean.ct.event.ColorTeamingPlayerAddEvent;
 import com.github.ucchyocean.ct.event.ColorTeamingPlayerLeaveEvent;
 import com.github.ucchyocean.ct.event.ColorTeamingPlayerLeaveEvent.Reason;
 import com.github.ucchyocean.ct.event.ColorTeamingTeamChatEvent;
 import com.github.ucchyocean.ct.event.ColorTeamingTeamCreateEvent;
 import com.github.ucchyocean.ct.event.ColorTeamingTeamRemoveEvent;
 import com.github.ucchyocean.ct.event.ColorTeamingTeamscoreChangeEvent;
 import com.github.ucchyocean.ct.item.CustomItem;
 
 /**
  * ColorTeamingAPIの実体クラス
  * @author ucchy
  */
 public class ColorTeamingManager implements ColorTeamingAPI {
 
     private ColorTeaming plugin;
     private ColorTeamingConfig config;
     private VaultChatBridge vaultchat;
 
     private Scoreboard scoreboard;
     private ObjectiveManager objectives;
 
     private RespawnConfiguration respawnConfig;
     private TPPointConfiguration tppointConfig;
     private TeamNameConfig teamNameConfig;
 
     private HashMap<String, ArrayList<String>> leaders;
 
     private String respawnMapName;
 
     private HashMap<String, CustomItem> customItems;
 
     private HashMap<String, ClassData> classDatas;
 
     /**
      * コンストラクタ
      * @param plugin
      * @param config
      * @param vaultchat
      */
     public ColorTeamingManager(ColorTeaming plugin, 
             ColorTeamingConfig config, VaultChatBridge vaultchat) {
 
         this.plugin = plugin;
         this.config = config;
         this.vaultchat = vaultchat;
         this.scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
 
         // 変数の初期化
         leaders = new HashMap<String, ArrayList<String>>();
         respawnConfig = new RespawnConfiguration();
         tppointConfig = new TPPointConfiguration();
         teamNameConfig = new TeamNameConfig();
         customItems = new HashMap<String, CustomItem>();
         classDatas = ClassData.loadAllClasses(new File(plugin.getDataFolder(), "classes"));
         objectives = new ObjectiveManager(scoreboard, this);
     }
     
     /**
      * 指定されたチームIDが存在するかどうかを返す。
      * @param id チームID
      * @return 存在するかどうか
      */
     @Override
     public boolean isExistTeam(String id) {
         
         Set<Team> teams = scoreboard.getTeams();
         for ( Team team : teams ) {
             if ( team.getName().equals(id) ) {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * チーム名をチームIDから取得する。
      * @param id チームID
      * @return チーム名
      */
     @Override
     public TeamNameSetting getTeamNameFromID(String id) {
         
         return teamNameConfig.getTeamNameFromID(id);
     }
     
     /**
      * Player に設定されている、チームを取得する。
      * @param player プレイヤー
      * @return チーム
      */
     @Override
     public Team getPlayerTeam(Player player) {
 
         Set<Team> teams = scoreboard.getTeams();
         for ( Team team : teams ) {
             for ( OfflinePlayer p : team.getPlayers() ) {
                 if ( p.getName().equalsIgnoreCase(player.getName()) ) {
                     return team;
                 }
             }
         }
         return null;
     }
     
     /**
      * Player に設定されている、チームのチーム名を取得する。
      * @param player プレイヤー
      * @return チーム名
      */
     @Override
     public TeamNameSetting getPlayerTeamName(Player player) {
 
         Team team = getPlayerTeam(player);
         if ( team == null ) return null;
         else return teamNameConfig.getTeamNameFromID(team.getName());
     }
 
     /**
      * Player にチームを設定する。
      * @param player プレイヤー
      * @param teamName チーム名
      * @return チーム、イベントキャンセルされた場合はnullになることに注意
      */
     @Override
     public Team addPlayerTeam(Player player, TeamNameSetting teamName) {
 
         String id = teamName.getID();
         String name = teamName.getName();
         ChatColor color = teamName.getColor();
         
         Team team = scoreboard.getTeam(id);
         if ( team == null ) {
 
             // イベントコール
             ColorTeamingTeamCreateEvent event =
                     new ColorTeamingTeamCreateEvent(teamName);
             Bukkit.getServer().getPluginManager().callEvent(event);
             if ( event.isCancelled() ) {
                 return null;
             }
 
             team = scoreboard.registerNewTeam(id);
             team.setDisplayName(color + name + ChatColor.RESET);
             team.setPrefix(color.toString());
             team.setSuffix(ChatColor.RESET.toString());
             team.setCanSeeFriendlyInvisibles(config.isCanSeeFriendlyInvisibles());
             team.setAllowFriendlyFire(config.isFriendlyFire());
         }
 
         // イベントコール
         ColorTeamingPlayerAddEvent event =
                 new ColorTeamingPlayerAddEvent(player, team);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if ( event.isCancelled() ) {
             return null;
         }
 
         team.addPlayer(player);
         player.setDisplayName(color + player.getName() + ChatColor.RESET);
         
         // 該当プレイヤーに通知
         player.sendMessage( Utility.replaceColorCode(
                 String.format("&aあなたはチーム %s &aになりました。", teamName.toString() ) ) );
 
         return team;
     }
 
     /**
      * Player に設定されているチームを削除する。
      * @param player プレイヤー
      * @param reason 離脱理由
      */
     @Override
     public void leavePlayerTeam(Player player, Reason reason) {
 
         Team team = getPlayerTeam(player);
         if ( team != null ) {
 
             // イベントコール
             ColorTeamingPlayerLeaveEvent event =
                     new ColorTeamingPlayerLeaveEvent(player, team, reason);
             Bukkit.getServer().getPluginManager().callEvent(event);
             if ( event.isCancelled() ) {
                 return;
             }
 
             team.removePlayer(player);
             
             // チーム削除により呼び出されたのでなければ、メンバー0人でチーム削除する
             if ( reason != Reason.TEAM_REMOVED && team.getPlayers().size() == 0 ) {
                 removeTeam(team.getName());
             }
         }
 
         player.setDisplayName(player.getName());
     }
 
     /**
      * フレンドリーファイアの設定。
      * @param ff trueならフレンドリーファイア有効、falseなら無効
      */
     @Override
     public void setFriendlyFire(boolean ff) {
 
         Set<Team> teams = scoreboard.getTeams();
         for ( Team t : teams ) {
             t.setAllowFriendlyFire(ff);
         }
 
         config.setFriendlyFire(ff);
         config.saveConfig();
     }
 
     /**
      * 仲間の可視化の設定。
      * @param fi trueなら仲間の可視化有効、falseなら無効
      */
     @Override
     public void setSeeFriendlyInvisibles(boolean fi) {
 
         Set<Team> teams = scoreboard.getTeams();
         for ( Team t : teams ) {
             t.setCanSeeFriendlyInvisibles(fi);
         }
 
         config.setCanSeeFriendlyInvisibles(fi);
         config.saveConfig();
     }
 
     /**
      * 指定したチームIDのチームを削除する
      * @param id チームID
      * @return 削除したかどうか（イベントでキャンセルされた場合はfalseになる）
      */
     @Override
     public boolean removeTeam(String id) {
 
         TeamNameSetting teamName = teamNameConfig.getTeamNameFromID(id);
         
         // イベントコール
         ColorTeamingTeamRemoveEvent event =
                 new ColorTeamingTeamRemoveEvent(teamName);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if ( event.isCancelled() ) {
             return false;
         }
 
         Team team = scoreboard.getTeam(id);
         if ( team != null ) {
             for ( OfflinePlayer player : team.getPlayers() ) {
                 if ( player.getPlayer() != null && player.isOnline() ) {
                     leavePlayerTeam(player.getPlayer(), Reason.TEAM_REMOVED);
                 }
             }
             team.unregister();
         }
         return true;
     }
 
     /**
      * 全てのチームを削除する
      */
     @Override
     public void removeAllTeam() {
 
         Set<Team> teams = scoreboard.getTeams();
         for ( Team team : teams ) {
             removeTeam(team.getName());
         }
     }
 
     /**
      * ユーザーをチームごとのメンバーに整理して返すメソッド
      * @return チームIDをKey メンバーをValueとした HashMap
      */
     @Override
     public HashMap<String, ArrayList<Player>> getAllTeamMembers() {
 
         ArrayList<TeamNameSetting> teamNames = getAllTeamNames();
         HashMap<String, ArrayList<Player>> result = 
                 new HashMap<String, ArrayList<Player>>();
 
         for ( TeamNameSetting tns : teamNames ) {
             result.put(tns.getID(), getTeamMembers(tns.getID()));
         }
 
         return result;
     }
     
     /**
      * チームメンバーを取得する
      * @param id チームID
      * @return チームメンバー。チームが存在しない場合はnullが返されることに注意
      */
     @Override
     public ArrayList<Player> getTeamMembers(String id) {
         
         Team team = scoreboard.getTeam(id);
         
         if ( team == null ) {
             return null;
         }
         
         Set<OfflinePlayer> playersTemp = team.getPlayers();
         ArrayList<Player> players = new ArrayList<Player>();
         for ( OfflinePlayer player : playersTemp ) {
             if ( player != null && player.isOnline() ) {
                 players.add(player.getPlayer());
             }
         }
 
         return players;
     }
 
     /**
      * 全てのプレイヤーを取得する
      * @return 全てのプレイヤー
      */
     @Override
     public ArrayList<Player> getAllPlayers() {
         
         Player[] temp = plugin.getServer().getOnlinePlayers();
         ArrayList<Player> result = new ArrayList<Player>();
         for ( Player p : temp ) {
             result.add(p);
         }
         return result;
     }
 
     /**
      * 指定したワールドにいる全てのプレイヤーを取得する。
      * ただし、指定したワールドが存在しない場合は、空のリストが返される。
      * @param worldNames 対象にするワールド名
      * @return 全てのプレイヤー
      */
     @Override
     public ArrayList<Player> getAllPlayersOnWorld(List<String> worldNames) {
 
         if ( worldNames == null ) {
             return null;
         }
 
         Player[] temp = plugin.getServer().getOnlinePlayers();
         ArrayList<Player> result = new ArrayList<Player>();
         for ( Player p : temp ) {
             if ( worldNames.contains(p.getWorld().getName()) ) {
                 result.add(p);
             }
         }
         return result;
     }
 
     /**
      * 全てのチーム名を取得する
      * @return 全てのチーム名
      */
     @Override
     public ArrayList<TeamNameSetting> getAllTeamNames() {
 
         ArrayList<TeamNameSetting> result = new ArrayList<TeamNameSetting>();
         Set<Team> teams = scoreboard.getTeams();
 
         for ( Team team : teams ) {
             result.add(teamNameConfig.getTeamNameFromID(team.getName()));
         }
 
         return result;
     }
 
     /**
      * メッセージをチームチャットに送信する。
      * @param player 送信元プレイヤー
      * @param message 送信するメッセージ
      */
     @Override
     public void sendTeamChat(Player player, String message) {
 
         // チームを取得する
         Team team = getPlayerTeam(player);
         if ( team == null ) {
             return;
         }
 
         sendTeamChat(player, team.getName(), message);
     }
 
     /**
      * 情報をチームチャットに送信する。
      * @param sender 送信者
      * @param team 送信先のチームID
      * @param message 送信するメッセージ
      */
     @Override
     public void sendTeamChat(CommandSender sender, String team, String message) {
 
         // チームを取得する
         Team t = scoreboard.getTeam(team);
         if ( t == null ) {
             return;
         }
 
         // 設定に応じて、Japanize化する
         if ( sender instanceof Player && config.isShowJapanizeTeamChat() ) {
             // 2byteコードを含む場合や、半角カタカナしか含まない場合は、
             // 処理しないようにする。
             if ( message.getBytes().length == message.length() &&
                     !message.matches("[ \\uFF61-\\uFF9F]+") ) {
                 String kana = KanaConverter.conv(message);
                 message = message + " (" + kana + ")";
             }
         }
 
         // イベントコール
         ColorTeamingTeamChatEvent event =
                 new ColorTeamingTeamChatEvent(sender, message, t);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if ( event.isCancelled() ) {
             return;
         }
         message = event.getMessage();
         
         // キーワード生成
         String teamName = t.getDisplayName();
         String playerName = "";
         String prefix = "";
         String suffix = "";
         if ( sender instanceof Player ) {
             Player player = (Player)sender;
             playerName = player.getDisplayName();
             if ( vaultchat != null ) {
                 prefix = vaultchat.getPlayerPrefix(player);
                 suffix = vaultchat.getPlayerSuffix(player);
             }
         } else {
             if ( sender != null ) {
                 playerName = sender.getName();
             }
         }
 
         // メッセージを生成
         String partyMessage = config.getTeamChatFormat();
         partyMessage = partyMessage.replace("%team", teamName);
         partyMessage = partyMessage.replace("%name", playerName);
         partyMessage = partyMessage.replace("%prefix", prefix);
         partyMessage = partyMessage.replace("%suffix", suffix);
         partyMessage = partyMessage.replace("%message", message);
         partyMessage = Utility.replaceColorCode(partyMessage);
 
         // チームメンバに送信する
         ArrayList<Player> playersToSend = getTeamMembers(t.getName());
         if ( config.isOPDisplayMode() ) {
             for ( OfflinePlayer p : plugin.getServer().getOperators() ) {
                 if ( p.isOnline() && !playersToSend.contains(p.getPlayer()) ) {
                     playersToSend.add(p.getPlayer());
                 }
             }
         }
         for ( Player p : playersToSend ) {
             p.sendMessage(partyMessage);
         }
 
         // ログ記録する
         if ( config.isTeamChatLogMode() ) {
             plugin.getLogger().info(partyMessage);
         }
     }
 
     /**
      * スコアボードの表示を行う
      */
     @Override
     public void displayScoreboard() {
         
         // サイドバー
         switch (config.getSideCriteria()) {
         case POINT:
             objectives.getTeamPointObjective().setDisplaySlot(DisplaySlot.SIDEBAR);
             break;
         case KILL_COUNT:
             objectives.getTeamKillObjective().setDisplaySlot(DisplaySlot.SIDEBAR);
             break;
         case DEATH_COUNT:
             objectives.getTeamDeathObjective().setDisplaySlot(DisplaySlot.SIDEBAR);
             break;
         case REST_PLAYER:
             objectives.getTeamRestObjective().setDisplaySlot(DisplaySlot.SIDEBAR);
             break;
         case NONE:
             break;
         default:
             break;
         }
         
         // リスト
         switch (config.getListCriteria()) {
         case POINT:
             objectives.getPersonalPointObjective().setDisplaySlot(DisplaySlot.PLAYER_LIST);
             break;
         case KILL_COUNT:
             objectives.getPersonalKillObjective().setDisplaySlot(DisplaySlot.PLAYER_LIST);
             break;
         case DEATH_COUNT:
             objectives.getPersonalDeathObjective().setDisplaySlot(DisplaySlot.PLAYER_LIST);
             break;
         case HEALTH:
             objectives.getPersonalHealthObjective().setDisplaySlot(DisplaySlot.PLAYER_LIST);
             break;
         case NONE:
             break;
         default:
             break;
         }
         
         // 頭の上
         switch (config.getBelowCriteria()) {
         case POINT:
             objectives.getPersonalPointObjective().setDisplaySlot(DisplaySlot.BELOW_NAME);
             break;
         case KILL_COUNT:
             objectives.getPersonalKillObjective().setDisplaySlot(DisplaySlot.BELOW_NAME);
             break;
         case DEATH_COUNT:
             objectives.getPersonalDeathObjective().setDisplaySlot(DisplaySlot.BELOW_NAME);
             break;
         case HEALTH:
             objectives.getPersonalHealthObjective().setDisplaySlot(DisplaySlot.BELOW_NAME);
             break;
         case NONE:
             break;
         default:
             break;
         }
     }
 
     /**
      * 残りチームメンバーのスコアボードを更新する。
      */
     @Override
     public void refreshRestTeamMemberScore() {
         
         Objective obj = objectives.getTeamRestObjective();
         
         // まず、全ての項目をいったん0にする
         for ( TeamNameSetting tns : getTeamNameConfig().getTeamNames() ) {
             if ( obj.getScore(tns.getScoreItem()).getScore() > 0 ) {
                 obj.getScore(tns.getScoreItem()).setScore(0);
             }
         }
         
         // チーム人数を取得して設定する
         HashMap<String, ArrayList<Player>> members = getAllTeamMembers();
         for ( String id : members.keySet() ) {
             TeamNameSetting tns = getTeamNameFromID(id);
             obj.getScore(tns.getScoreItem()).setScore(members.get(id).size());
         }
     }
     
     /**
      * @deprecated このメソッドは、refreshRestTeamMemberScoreに変わりました。
      */
     @Override
     public void makeSidebarScore() {
         refreshRestTeamMemberScore();
     }
     
     /**
      * キルデス数やポイントを全てクリアする
      */
     @Override
     public void clearKillDeathPoints() {
 
         // イベントコール
         ColorTeamingKillDeathClearedEvent event =
                 new ColorTeamingKillDeathClearedEvent();
         Bukkit.getServer().getPluginManager().callEvent(event);
         if ( event.isCancelled() ) {
             return;
         }
 
         // 各オブジェクティブを初期化
         objectives.resetAll();
     }
 
     /**
      * チームのポイント数を全取得する
      * @return チームのポイント数
      */
     @Override
     public HashMap<String, Integer> getAllTeamPoints() {
         
         HashMap<String, Integer> points = new HashMap<String, Integer>();
         Objective obj = objectives.getTeamPointObjective();
         
         for ( TeamNameSetting tns : getAllTeamNames() ) {
             int p = obj.getScore(tns.getScoreItem()).getScore();
             points.put(tns.getID(), p);
         }
         
         return points;
     }
     
     /**
      * プレイヤーのポイント数を全取得する
      * @return プレイヤーのポイント数
      */
     @Override
     public HashMap<String, Integer> getAllPlayerPoints() {
 
         HashMap<String, Integer> points = new HashMap<String, Integer>();
         Objective obj = objectives.getPersonalPointObjective();
         HashMap<String, ArrayList<Player>> members = getAllTeamMembers();
         
         for ( String team : members.keySet() ) {
             for ( Player player : members.get(team) ) {
                 int point = obj.getScore(player).getScore();
                 points.put(player.getName(), point);
             }
         }
         
         return points;
     }
 
     /**
      * チームポイントを設定する。
      * @param team チーム名
      * @param point ポイント数
      */
     @Override
     public void setTeamPoint(String team, int point) {
         
         Objective obj = objectives.getTeamPointObjective();
         TeamNameSetting tns = teamNameConfig.getTeamNameFromID(team);
         
         int pointBefore = obj.getScore(tns.getScoreItem()).getScore();
         
         if ( point == 0 ) {
             // NOTE: ポイント0を設定する場合は、一旦1を設定して項目を表示させる。
             obj.getScore(tns.getScoreItem()).setScore(1);
         }
         obj.getScore(tns.getScoreItem()).setScore(point);
         
         // イベント呼び出し
         ColorTeamingTeamscoreChangeEvent event = 
                 new ColorTeamingTeamscoreChangeEvent(tns, pointBefore, point);
         Bukkit.getPluginManager().callEvent(event);
     }
     
     /**
      * チームポイントを増減する。
      * @param team チーム名
      * @param amount ポイント増減量（マイナスでポイント減少）
      * @return 増減後のポイント
      */
     @Override
     public int addTeamPoint(String team, int amount) {
         
         Objective obj = objectives.getTeamPointObjective();
         TeamNameSetting tns = teamNameConfig.getTeamNameFromID(team);
         
         int point = obj.getScore(tns.getScoreItem()).getScore();
         obj.getScore(tns.getScoreItem()).setScore(point + amount);
         
         // イベント呼び出し
         ColorTeamingTeamscoreChangeEvent event = 
                 new ColorTeamingTeamscoreChangeEvent(tns, point, point + amount);
         Bukkit.getPluginManager().callEvent(event);
         
         return point + amount;
     }
     
     /**
      * チーム単位のキルデス数を取得する
      * @return キルデス数
      */
     @Override
     public HashMap<String, int[]> getKillDeathCounts() {
         
         Objective kills = objectives.getPersonalKillObjective();
         Objective deaths = objectives.getPersonalDeathObjective();
         
         HashMap<String, int[]> result = new HashMap<String, int[]>();
         HashMap<String, ArrayList<Player>> members = getAllTeamMembers();
         for ( String teamID : members.keySet() ) {
             int[] data = new int[2];
             for ( Player player : members.get(teamID) ) {
                 data[0] += kills.getScore(player).getScore();
                 data[1] += deaths.getScore(player).getScore();
             }
             result.put(teamID, data);
         }
         
         return result;
     }
     
     /**
      * ユーザー単位のキルデス数を取得する
      * @return キルデス数
      */
     @Override
     public HashMap<String, int[]> getKillDeathPersonalCounts() {
         
         Objective kills = objectives.getPersonalKillObjective();
         Objective deaths = objectives.getPersonalDeathObjective();
         
         HashMap<String, int[]> result = new HashMap<String, int[]>();
         HashMap<String, ArrayList<Player>> members = getAllTeamMembers();
         for ( String teamID : members.keySet() ) {
             for ( Player player : members.get(teamID) ) {
                 int[] data = new int[2];
                 data[0] = kills.getScore(player).getScore();
                 data[1] = deaths.getScore(player).getScore();
                 result.put(player.getName(), data);
             }
         }
         
         return result;
     }
     
     /**
      * チームのキル数カウントを、+1する。
      * @param team チームID
      */
     @Override
     public void increaseTeamKillCount(String team) {
         
         TeamNameSetting tns = getTeamNameFromID(team);
         Objective obj = objectives.getTeamKillObjective();
         Score score = obj.getScore(tns.getScoreItem());
         score.setScore(score.getScore() + 1);
     }
     
     /**
      * チームのデス数カウントを、+1する。
      * @param team チームID
      */
     @Override
     public void increaseTeamDeathCount(String team) {
         
         TeamNameSetting tns = getTeamNameFromID(team);
         Objective obj = objectives.getTeamDeathObjective();
         Score score = obj.getScore(tns.getScoreItem());
         score.setScore(score.getScore() + 1);
     }
     
     /**
      * 指定したプレイヤーのポイントを追加する。
      * @param player プレイヤー
      * @param amount 追加ポイント（マイナス指定でポイントを減らす。）
      * @return 設定後のポイント
      */
     @Override
     public int addPlayerPoint(Player player, int amount) {
         
         Score score = objectives.getPersonalPointObjective().getScore(player);
         int point = score.getScore() + amount;
         score.setScore(point);
         return point;
     }
     
     /**
      * 指定したプレイヤーのポイントを設定する。
      * @param player プレイヤー
      * @param amount ポイント
      */
     @Override
     public void setPlayerPoint(Player player, int amount) {
         objectives.getPersonalPointObjective().getScore(player).setScore(amount);
     }
     
     /**
      * ユーザー単位のキルデス数を設定する
      * @param playerName プレイヤー名
      * @param kill キル数
      * @param death デス数
      */
     @Override
     public void setKillDeathUserCounts(String playerName, int kill, int death) {
         
         Player player = Bukkit.getPlayer(playerName);
         if ( player == null ) {
             return;
         }
         
         Objective kills = objectives.getPersonalKillObjective();
         Objective deaths = objectives.getPersonalDeathObjective();
         
         kills.getScore(player).setScore(kill);
         deaths.getScore(player).setScore(death);
     }
     
     /**
      * リーダー設定を全てクリアする
      */
     @Override
     public void clearLeaders() {
         leaders.clear();
     }
 
     /**
      * リーダー設定を取得する
      * @return リーダー設定
      */
     @Override
     public HashMap<String, ArrayList<String>> getLeaders() {
         return leaders;
     }
 
     /**
      * リスポーン設定を取得する
      * @return リスポーン設定
      */
     @Override
     public RespawnConfiguration getRespawnConfig() {
         return respawnConfig;
     }
 
     /**
      * TP地点設定を取得する
      * @return TP地点設定
      */
     @Override
     public TPPointConfiguration getTppointConfig() {
         return tppointConfig;
     }
     
     /**
      * チーム名設定を取得する
      * @return チーム名設定
      */
     @Override
     public TeamNameConfig getTeamNameConfig() {
         return teamNameConfig;
     }
 
     /**
      * リスポーンマップ名を取得する
      * @return リスポーンマップ名
      */
     @Override
     public String getRespawnMapName() {
         return respawnMapName;
     }
 
     /**
      * リスポーンマップ名を設定する
      * @param respawnMapName リスポーンマップ名
      */
     @Override
     public void setRespawnMapName(String respawnMapName) {
         this.respawnMapName = respawnMapName;
     }
     
     /**
      * カスタムアイテムを登録する
      * @param item カスタムアイテム
      */
     @Override
     public void registerCustomItem(CustomItem item) {
         
         String name = item.getName();
         customItems.put(name, item);
     }
     
     /**
      * カスタムアイテムを登録する
      * @param item 登録するアイテム
      * @param name アイテム名
      * @param displayName 表示アイテム名
      */
     @Override
     public void registerCustomItem(ItemStack item, String name, String displayName) {
         registerCustomItem(new CustomItem(item, name, displayName));
     }
     
     /**
      * 登録されているカスタムアイテムを取得する
      * @param name カスタムアイテム名
      * @return カスタムアイテム、登録されていないアイテム名を指定した場合はnullが返される。
      */
     @Override
     public CustomItem getCustomItem(String name) {
         return customItems.get(name);
     }
     
     /**
      * 登録されているカスタムアイテムの名前を取得する
      * @return カスタムアイテムの名前
      */
     @Override
     public Set<String> getCustomItemNames() {
         return customItems.keySet();
     }
 
     /**
      * 指定されたプレイヤーに指定されたクラスを設定する
      * @param players プレイヤー
      * @param classname クラス名
      * @return クラス設定を実行したかどうか。<br/>
      * 例えば、指定されたクラス名が存在しない場合は、falseになる。
      */
     @Override
     public boolean setClassToPlayer(ArrayList<Player> players, String classname) {
         
         // クラス設定が存在しない場合は falseを返す
         if ( !classDatas.containsKey(classname) ) {
             return false;
         }
         
         // 設定対象が居ない場合は falseを返す
         if ( players == null || players.size() <= 0 ) {
             return false;
         }
         
         // 設定の実行
         ClassData cd = classDatas.get(classname);
         for ( Player player : players ) {
             cd.setClassToPlayer(player);
         }
         
         return true;
     }
     
     /**
      * 指定されたクラス名が存在するかどうかを確認する
      * @param classname クラス名
      * @return 存在するかどうか
      */
     @Override
     public boolean isExistClass(String classname) {
         return classDatas.containsKey(classname);
     }
     
     /**
      * 登録されている全てのクラスデータをまとめて取得する。
      * @return 全てのクラスデータ
      */
     @Override
     public HashMap<String, ClassData> getClasses() {
         return classDatas;
     }
     
     /**
      * クラスデータを設定する。同名のクラスが存在する場合は上書きに、無い場合は新規追加になる。
      * @param classdata クラスデータ
      */
     @Override
     public void setClassData(ClassData classdata) {
         
         String name = classdata.getTitle();
         classDatas.put(name, classdata);
     }
     
     /**
      * ランダムな順序で、プレイヤーをチームわけします。<br/>
      * 既にチームわけが存在する場合は、全部クリアしてから分けられます。
      * @param players チームわけを行うプレイヤー
      * @param teamNum チーム数（2から9までの数を指定可能です）
      */
     @Override
     public void makeColorTeamsWithRandomSelection(
             ArrayList<Player> players, int teamNum) {
         Collections.shuffle(players);
         makeColorTeamsWithOrderSelection(players, teamNum);
     }
 
     /**
      * 指定されたプレイヤー順序で、プレイヤーをチームわけします。<br/>
      * 既にチームわけが存在する場合は、全部クリアしてから分けられます。
      * @param players チームわけを行うプレイヤー
      * @param teamNum チーム数（2から9までの数を指定可能です）
      */
     @Override
     public void makeColorTeamsWithOrderSelection(ArrayList<Player> players, int teamNum) {
         
         // 全てのチームをいったん削除する
         removeAllTeam();
         
         // チーム名設定を取得する
         ArrayList<TeamNameSetting> tns = teamNameConfig.getTeamNames();
 
         // チームを設定していく
         for ( int i=0; i<players.size(); i++ ) {
             int group = i % teamNum;
             TeamNameSetting teamName = tns.get(group);
             addPlayerTeam(players.get(i), teamName);
         }
 
         // キルデス情報のクリア
         clearKillDeathPoints();
 
         // チーム人数の更新、スコアボードの表示
         refreshRestTeamMemberScore();
         displayScoreboard();
     }
     
     /**
      * 既存のチームわけをそのままに、指定されたプレイヤーを既存のチームへ加えていきます。<br/>
      * プレイヤーはランダムな順序で追加が行われます。<br/>
      * 加えられる先のチームは、人数の少ないチームが選択されます。
      * 同数の場合はその中からランダムに選択されます。
      * @param players チームに加えるプレイヤー
      * @return 最後まで処理が行われたかどうか
      */
     @Override
     public boolean addPlayerToColorTeamsWithRandomSelection(ArrayList<Player> players) {
         Collections.shuffle(players);
         return addPlayerToColorTeamsWithOrderSelection(players);
     }
     
     /**
      * 既存のチームわけをそのままに、指定されたプレイヤーを既存のチームへ加えていきます。<br/>
      * プレイヤーは指定の順序で追加が行われます。<br/>
      * 加えられる先のチームは、人数の少ないチームが選択されます。
      * 同数の場合はその中からランダムに選択されます。
      * @param players チームに加えるプレイヤー
      * @return 最後まで処理が行われたかどうか
      */
     @Override
     public boolean addPlayerToColorTeamsWithOrderSelection(ArrayList<Player> players) {
         
         // 人数の少ないチームに設定していく
         for ( int i=0; i<players.size(); i++ ) {
 
             // 人数の少ないチームの取得
             HashMap<String, ArrayList<Player>> members = getAllTeamMembers();
             int least = 999;
             TeamNameSetting leastTeam = null;
 
             ArrayList<TeamNameSetting> teams = getAllTeamNames();
             // ランダム要素を入れるため、チーム名をシャッフルする
             Collections.shuffle(teams);
 
             for ( TeamNameSetting t : teams ) {
                 if ( least > members.get(t.getID()).size() ) {
                     least = members.get(t.getID()).size();
                     leastTeam = t;
                 }
             }
             
             // チームへプレイヤーを追加
             if ( leastTeam != null ) {
                 addPlayerTeam(players.get(i), leastTeam);
             } else {
                 // 参加できるチームが無い
                 return false;
             }
         }
 
         // チーム人数の更新、スコアボードの表示
         refreshRestTeamMemberScore();
         displayScoreboard();
         
         return true;
     }
     
     /**
      * ColorTeamingの設定ファイルを全て再読み込みする
      */
     @Override
     public void realod() {
         
         ColorTeaming.instance.config = ColorTeamingConfig.loadConfig();
         respawnConfig = new RespawnConfiguration();
         tppointConfig = new TPPointConfiguration();
         teamNameConfig = new TeamNameConfig();
         
         // クラスデータの再読み込み
         HashMap<String, ClassData> reloadClassData = 
                 ClassData.loadAllClasses(new File(plugin.getDataFolder(), "classes"));
         for ( String key : reloadClassData.keySet() ) {
             classDatas.put(key, reloadClassData.get(key));
         }
     }
 }
