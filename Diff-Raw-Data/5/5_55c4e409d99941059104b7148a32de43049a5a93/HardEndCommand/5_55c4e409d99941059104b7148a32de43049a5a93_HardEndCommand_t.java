 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands.player
  * Created: 2013/02/12 2:51:27
  */
 package net.syamn.sakuracmd.commands.player;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import net.syamn.sakuracmd.commands.BaseCommand;
 import net.syamn.sakuracmd.enums.PartyStatus;
 import net.syamn.sakuracmd.manager.HardEndManager;
 import net.syamn.sakuracmd.manager.Worlds;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.worker.EndResetWorker;
 import net.syamn.utils.StrUtil;
 import net.syamn.utils.TimeUtil;
 import net.syamn.utils.Util;
 import net.syamn.utils.exception.CommandException;
 import net.syamn.utils.queue.ConfirmQueue;
 import net.syamn.utils.queue.Queueable;
 import net.syamn.utils.queue.QueuedCommand;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 /**
  * HardEndCommand (HardEndCommand.java)
  * @author syam(syamn)
  */
 public class HardEndCommand extends BaseCommand implements Queueable{
     public HardEndCommand(){
         bePlayer = true;
         name = "hardend";
         perm = Perms.HARD_END;
         argLength = 0;
         usage = "[action] <- hard end commands";
     }
 
     private HardEndManager mgr = null;
     
     
     @Override
     public void execute() throws CommandException{
         mgr = HardEndManager.getInstance();
         if (mgr == null){
             throw new CommandException("&c現在ハードエンドは利用できません");
         }
         
         if (args.size() < 1){
             info();
             return;
         }
         
         final String action = args.remove(0).trim().toLowerCase(Locale.ENGLISH);
         
         if (action.equals("ready")){
             ready();
         }
         else if (action.equals("start")){
             start();
         }
         else if (action.equals("join")){
             join();
         }
         else if (action.equals("invite")){
             invite();
         }
         else if (action.equals("kick")){
             kick();
         }
         else if (action.equals("info")){
             info();
         }
         else if (action.equals("promote")){
             promote();
         }
         else if (action.equals("demote")){
             demote();
         }
         else if (action.equals("leave")){
             leave();
         }
         else if (action.equals("tp")){
             tp();
         }
         else{
             if (Perms.HARD_END_NOSIGN.has(sender)){
                 throw new CommandException("&c不正なサブコマンドです: ready / start / join / leave / invite / kick / promote / demote / tp / info");
             }else{
                 throw new CommandException("&c不正なサブコマンドです: leave / invite / kick / promote / demote / info");
             }
         }
     }
     
     @SuppressWarnings("incomplete-switch")
     private void info() throws CommandException{
         if (mgr.getStatus() == PartyStatus.WAITING){
             int cooldown = mgr.getRemainCooldownSeconds();
             if (cooldown > 0){
                 Util.message(sender, "&b ステータス: &cクールダウン中 (あと" + TimeUtil.getReadableTimeBySecond(cooldown) + ")");
             }else{
                 Util.message(sender, "&b ステータス: &7パーティ登録受付中");
                 Util.message(sender, "&6 パーティ受付所から新たなパーティを登録できます！");
             }
             return;
         }
         
         String status = null;
         int remain = -1;
         switch (mgr.getStatus()){
             case OPENING:
                 status = "&b参加受付中";
                 remain = mgr.getRemainOpenedSeconds();
                 break;
             case STARTING:
                 status = "&6開始中";
                 remain = mgr.getRemainSeconds();
                 break;
         }
         
         if (mgr.isOpenParty()) status += " &b[OPEN Party]";
         else status += " &c[CLOSE Party]";
         
         List<String> names = new ArrayList<String>(mgr.getMembersMap().size());
         for (final Map.Entry<String, Boolean> entry : mgr.getMembersMap().entrySet()){
             if (entry.getValue()){
                 names.add("&6" + entry.getKey());
             }else{
                 names.add("&3" + entry.getKey());
             }
         }
         
         Util.message(sender, "&b ステータス: " + status);
         Util.message(sender, " &bパーティメンバー (" + names.size() + "): " + StrUtil.join(names, "&7, "));
         Util.message(sender, " &b残り時間: " + TimeUtil.getReadableTimeBySecond(remain));
     }
     
     private void ready() throws CommandException{
         checkSign();
         
         if (mgr.getStatus() != PartyStatus.WAITING){
             throw new CommandException("&c現在既にパーティが作成、または開始されています");
         }
         
         final int cooldown = mgr.getRemainCooldownSeconds();
         if (cooldown > 0){
             throw new CommandException("&c次のパーティ結成まで あと" + TimeUtil.getReadableTimeBySecond(cooldown) + " 必要です！");
         }
         
         Boolean open = null;
         if (args.size() > 0){
             if (args.get(0).equalsIgnoreCase("open")){
                 open = true;
             }else if(args.get(0).equalsIgnoreCase("close")){
                 open = false;
             }
         }
         if (open == null){
             throw new CommandException("&cパーティの種類を open または close で指定してください！");
         }
         
         final EndResetWorker worker = EndResetWorker.getInstance();
         if (worker == null){
             throw new CommandException("&cエンドワールドの初期化に失敗しました！");
         }
         
         mgr.openParty(open, player);
         
         Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
             @Override public void run(){
                 worker.regen(Bukkit.getWorld(Worlds.hard_end), true);
             }
         }, 1L);
     }
     
     private void start() throws CommandException{
         checkSign();
         
         if (mgr.getStatus() != PartyStatus.OPENING){
             throw new CommandException("&c現在パーティが開始待機中ではありません！");
         }
         if (!mgr.isLeader(player)){
             throw new CommandException("&cあなたはパーティリーダーではありません！リーダーのみが実行できます！");
         }
         
         Player p;
         for (final String name : mgr.getMembersMap().keySet()){
             p = Bukkit.getPlayerExact(name);
             if (p == null || !p.isOnline()){
                 mgr.removeMember(name);
                 mgr.message("&cプレイヤー &6" + name + " &cはオフラインのため、パーティから自動削除されました");
             }
         }
         
         if (mgr.getMembersMap().size() < mgr.getMinPlayers()){
             throw new CommandException("&c開始可能なパーティメンバー数に達していません！ " + mgr.getMinPlayers() + "人必要です！");
         }
         if (mgr.getMembersMap().size() > mgr.getMaxPlayers()){
             throw new CommandException("&c開始可能なパーティメンバー数を超えています！ " + mgr.getMaxPlayers() + "人以下にしてください！");
         }
         
         ArrayList<Object> queueArgs = new ArrayList<Object>(1);
         queueArgs.add("start");
         ConfirmQueue.getInstance().addQueue(sender, this, queueArgs, 10);
         Util.message(sender, "&6ハードエンド討伐を開始しようとしています！");
         Util.message(sender, "&6討伐中は途中からパーティにメンバーを追加できません。");
         Util.message(sender, "&6本当に開始しますか？ &a/confirm&6 コマンドで続行します。");
     }
     
     private void join() throws CommandException{
         checkSign();
         
         if (mgr.getStatus() != PartyStatus.OPENING){
             throw new CommandException("&c現在パーティが開始待機中ではありません！");
         }
         if (mgr.isMember(player)){
             throw new CommandException("&cあなたは既にこのパーティに参加しています！");
         }
         if (mgr.getMembersMap().size() >= mgr.getMaxPlayers()){
             throw new CommandException("&c参加可能なパーティ人数の上限に達しています！");
         }
         if (!mgr.isOpenParty() && !mgr.invited.contains(player.getName().toLowerCase(Locale.ENGLISH))){
             throw new CommandException("&cクローズパーティのため、パーティリーダーの招待が必要です！");
         }
         
         if (!mgr.isOpenParty()){
             mgr.invited.remove(player.getName().toLowerCase(Locale.ENGLISH));
         }
         
         mgr.addMember(player.getName(), false);
         Util.broadcastMessage("&aハードエンド討伐パーティに" + PlayerManager.getPlayer(player).getName() + "が参加しました");
         mgr.message(" " + PlayerManager.getPlayer(player).getName() + " &dがこのパーティに参加しました！");
     }
     
     private void invite() throws CommandException{
         if (mgr.getStatus() != PartyStatus.OPENING){
             throw new CommandException("&c現在パーティが開始待機中ではありません！");
         }
         if (!mgr.isLeader(player)){
             throw new CommandException("&cあなたはパーティリーダーではありません！リーダーのみが実行できます！");
         }
         
         if (args.size() < 1){
             throw new CommandException("&cパーティに招待するプレイヤーを入力してください！");
         }
         
         final Player p = Bukkit.getPlayer(args.get(0).trim());
         if (p == null || !p.isOnline()){
             throw new CommandException("&cプレイヤー " + args.get(0).trim() + " が見つかりません！");
         }
         
         if (sender.equals(p)){
             throw new CommandException("&c自分に招待を送信できません！");
         }
         
         if (!mgr.isOpenParty()){
             mgr.invited.add(p.getName().toLowerCase(Locale.ENGLISH));
         }
         Util.message(p, " " + PlayerManager.getPlayer(player).getName() + " &dがあなたにパーティ招待を送信しました！");
         Util.message(p, " &dパーティ受付所からこのパーティに参加することができます！");
         
         Util.message(sender, " " + PlayerManager.getPlayer(p).getName() + " &dにパーティ招待を送信しました！");
     }
     
     private void kick() throws CommandException{
         if (mgr.getStatus() == PartyStatus.WAITING){
             throw new CommandException("&c現在パーティは作成されていません");
         }
         if (!mgr.isLeader(player)){
             throw new CommandException("&cあなたはパーティリーダーではありません！リーダーのみが実行できます！");
         }
         
         if (args.size() < 1){
             throw new CommandException("&cパーティから追放するプレイヤーを入力してください！");
         }
         
         final String name = args.get(0).trim().toLowerCase(Locale.ENGLISH);
         if (!mgr.isMember(name)){
             throw new CommandException("&6" + name + " &cはパーティメンバーではありません！");
         }
         
         if (name.equalsIgnoreCase(sender.getName())){
             throw new CommandException("&c自分を追放することはできません！");
         }
         
         mgr.removeMember(name);
         Player p = Bukkit.getPlayerExact(name);
         if (p != null && p.isOnline()){
             Util.message(p, "&cあなたはパーティから追放されました！");
             if (p.getWorld().getName().equals(Worlds.hard_end)){
                 p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation(), TeleportCause.PLUGIN);
             }
         }
         
         mgr.message(" &6" + sender.getName() + "&c はこのパーティから  &6" + name + "&c を追放しました！");
     }
     
     private void leave() throws CommandException{
         if (mgr.getStatus() == PartyStatus.WAITING){
             throw new CommandException("&c現在パーティは作成されていません");
         }
         if (!mgr.isMember(player)){
             throw new CommandException("&cあなたはパーティメンバーではありません！");
         }
         
         ArrayList<Object> queueArgs = new ArrayList<Object>(1);
         queueArgs.add("leave");
         ConfirmQueue.getInstance().addQueue(sender, this, queueArgs, 15);
         Util.message(sender, "&6このハードエンド討伐パーティから離脱しようとしています！");
         Util.message(sender, "&6開始中のパーティには途中から再参加できません。");
         Util.message(sender, "&6本当に実行しますか？ &a/confirm&6 コマンドで続行します。");
     }
     
     private void promote() throws CommandException{
         if (mgr.getStatus() == PartyStatus.WAITING){
             throw new CommandException("&c現在パーティは作成されていません");
         }
         if (!mgr.isLeader(player)){
             throw new CommandException("&cあなたはパーティリーダーではありません！リーダーのみが実行できます！");
         }
         if (args.size() < 1){
             throw new CommandException("&cパーティリーダーに任命するプレイヤーを入力してください！");
         }
         
         final String name = args.get(0).trim().toLowerCase(Locale.ENGLISH);
         if (!mgr.isMember(name)){
             throw new CommandException("&6" + name + " &cはパーティメンバーではありません！");
         }
         if (name.equalsIgnoreCase(sender.getName())){
             throw new CommandException("&c自分を変更することはできません！");
         }
         if (mgr.isLeader(name)){
             throw new CommandException("&6" + name + " &cは既にパーティリーダーです！");
         }
         
         mgr.setLeader(name, true);
         Player p = Bukkit.getPlayerExact(name);
         if (p != null && p.isOnline()){
             Util.message(p, "&aあなたはパーティリーダーに任命されました！");
         }
         
         mgr.message(" &6" + PlayerManager.getPlayer(player).getName() + "&a が  &6" + name + "&a をパーティリーダーに任命しました");
     }
     
     private void demote() throws CommandException{
         if (mgr.getStatus() == PartyStatus.WAITING){
             throw new CommandException("&c現在パーティは作成されていません");
         }
         if (!mgr.isLeader(player)){
             throw new CommandException("&cあなたはパーティリーダーではありません！リーダーのみが実行できます！");
         }
         if (args.size() < 1){
             throw new CommandException("&cパーティリーダーを解任するプレイヤーを入力してください！");
         }
         
         final String name = args.get(0).trim().toLowerCase(Locale.ENGLISH);
         if (!mgr.isMember(name)){
             throw new CommandException("&6" + name + " &cはパーティメンバーではありません！");
         }
         if (name.equalsIgnoreCase(sender.getName())){
             throw new CommandException("&c自分を変更することはできません！");
         }
         if (!mgr.isLeader(name)){
             throw new CommandException("&6" + name + " &cはパーティリーダーではありません！");
         }
         
         mgr.setLeader(name, false);
         Player p = Bukkit.getPlayerExact(name);
         if (p != null && p.isOnline()){
             Util.message(p, "&6あなたはパーティリーダーから解任されました！");
         }
         
         mgr.message(" &6" + PlayerManager.getPlayer(player).getName() + "&c が  &6" + name + "&c をパーティリーダーから解任しました");
     }
     
     private void tp() throws CommandException{
         checkSign();
         
         final Location to = Bukkit.getWorld(Worlds.hard_end).getSpawnLocation().clone();
         
         // check ground
         final Block baseBlock = to.getBlock().getRelative(BlockFace.DOWN, 1);
         Block block;
         for (int x = baseBlock.getX() - 1; x <= baseBlock.getX() + 1; x++) {
             for (int z = baseBlock.getZ() - 1; z <= baseBlock.getZ() + 1; z++) {
                 block = baseBlock.getWorld().getBlockAt(x, baseBlock.getY(), z);
                 if (block.getType() != Material.OBSIDIAN) {
                     block.setType(Material.OBSIDIAN);
                 }
             }
         }
         
         player.teleport(to.add(0.5D, 0.5D, 0.5D), TeleportCause.PLUGIN);
     }
     
     @Override
     public void executeQueue(QueuedCommand queued) {
         List<Object> queueArgs = queued.getArgs();
         if (queueArgs.size() == 1){
             if (queueArgs.get(0).equals("start")){
                 queuedStart();
                 return;
             }
             else if (queueArgs.get(0).equals("leave")){
                 queuedLeave();
                 return;
             }
         }
         throw new IllegalStateException("not handled queued command by " + queued.getSender().getName());
     }
     
     private void queuedStart(){
         if (mgr.getStatus() != PartyStatus.OPENING){
             Util.message(player, "&c現在パーティが開始待機中ではありません！");
         }
         if (!mgr.isLeader(player)){
             Util.message(player, "&cあなたはパーティリーダーではありません！リーダーのみが実行できます！");
         }
         
         Player p;
         boolean changed = false;
         for (final String name : mgr.getMembersMap().keySet()){
             p = Bukkit.getPlayerExact(name);
             if (p == null || !p.isOnline()){
                 mgr.removeMember(name);
                 mgr.message("&cプレイヤー &6" + name + " &cはオフラインのため、パーティから自動削除されました");
                 changed = true;
             }
         }
         if (changed){
             ArrayList<Object> queueArgs = new ArrayList<Object>(1);
             queueArgs.add("start");
             ConfirmQueue.getInstance().addQueue(sender, this, queueArgs, 10);
             Util.message(sender, " &6パーティメンバーに変動がありました。このまま続行してよろしいですか？");
             Util.message(sender, " &6&a/confirm&6 コマンドで続行します。");
             return;
         }
         
         if (mgr.getMembersMap().size() < mgr.getMinPlayers()){
             Util.message(player, "&c開始可能なパーティメンバー数に達していません！" + mgr.getMinPlayers() + "人必要です！");
             return;
         }
         if (mgr.getMembersMap().size() > mgr.getMaxPlayers()){
             Util.message(player, "&c開始可能なパーティメンバー数を超えています！ " + mgr.getMaxPlayers() + "人以下にしてください！");
             return;
         }
         
         mgr.startParty();
     }
     
     private void queuedLeave(){
         if (mgr.getStatus() == PartyStatus.WAITING){
             Util.message(player, "&c現在パーティは作成されていません");
             return;
         }
         if (!mgr.isMember(player)){
             Util.message(player, "&cあなたはパーティメンバーではありません！");
             return;
         }
         if (mgr.isLeader(player) && mgr.getMembersMap().size() >= 2){
             int count = 0;
             for (final Boolean leader : mgr.getMembersMap().values()){
                 if (leader.booleanValue()) count++;
             }
             if (count == 1){
                 Util.message(player, "&c脱退する前に、最低一人以上のメンバーをリーダーにする必要があります");
                 return;
             }
         }
         
         mgr.removeMember(player.getName());
         if (player.getName().equals(Worlds.hard_end)){
             player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation(), TeleportCause.PLUGIN);
         }
         
         mgr.message("&aプレイヤー " + PlayerManager.getPlayer(player).getName() + " &aがこのパーティから抜けました！");
         Util.broadcastMessage("&aハードエンド討伐パーティから " + PlayerManager.getPlayer(player).getName() + " &aが脱退しました");
         
         if (mgr.getMembersMap().size() < 1){
             mgr.cleanup();
             Util.broadcastMessage("&a今回の討伐パーティはメンバーが居なくなったため、自動で削除されました");
         }
     }
     
     private void checkSign() throws CommandException{
         if (isPlayer && !bySign && !Perms.HARD_END_NOSIGN.has(sender)){
             throw new CommandException("&cこのコマンドは直接実行できません");
         }
     }
     
     /*
     private void permCheck() throws CommandException{
         if (!Perms.HARD_END.has(sender)){
             throw new CommandException("&c権限がありません！");
         }
     }
     */
 }
