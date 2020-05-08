 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands.player
  * Created: 2013/01/12 17:49:00
  */
 package net.orange_server.orangeserver.commands.player;
 
 import java.util.Map;
 
 import net.orange_server.orangeserver.OSHelper;
 import net.orange_server.orangeserver.commands.BaseCommand;
 import net.orange_server.orangeserver.permission.Perms;
 import net.orange_server.orangeserver.player.OrangePlayer;
 import net.orange_server.orangeserver.player.PlayerManager;
 import net.orange_server.orangeserver.player.Power;
 import net.orange_server.orangeserver.utils.plugin.OrangeServerUtil;
 import net.orange_server.orangeserver.worker.FlymodeWorker;
 import net.syamn.utils.LogUtil;
 import net.syamn.utils.TimeUtil;
 import net.syamn.utils.Util;
 import net.syamn.utils.economy.EconomyUtil;
 import net.syamn.utils.exception.CommandException;
 import net.syamn.utils.queue.ConfirmQueue;
 import net.syamn.utils.queue.Queueable;
 import net.syamn.utils.queue.QueuedCommand;
 
 /**
  * FlymodeCommand (FlymodeCommand.java)
  * @author syam(syamn)
  */
 public class FlymodeCommand extends BaseCommand implements Queueable{
     public FlymodeCommand(){
         bePlayer = true;
         name = "flymode";
         perm = Perms.FLYMODE;
         argLength = 0;
         usage = "<- buy flymode";
     }
     
     private double getCost(){
         return OSHelper.getInstance().getConfig().getFlymodeCost();
     }
     private int getDuration(){
         return OSHelper.getInstance().getConfig().getFlymodeTimeInMinutes();
     }
     private int getMaxPlayers(){
         return OSHelper.getInstance().getConfig().getFlymodePlayersLimit();
     }
 
     public void execute() throws CommandException{
         final OrangePlayer sp = PlayerManager.getPlayer(player);
         
         if (args.size() > 0 && args.get(0).equalsIgnoreCase("list")){
             // Show flymode players list
             Map<String, Integer> players = FlymodeWorker.getInstance().getFlymodePlayers();
             
             if (players.size() == 0){
                 Util.message(sender, "&a現在飛行権が有効なプレイヤーはいません");
             }else{
                 Util.message(sender, "&6飛行権が有効なプレイヤーリスト:");
                 int now = TimeUtil.getCurrentUnixSec().intValue();
                 for (final Map.Entry<String, Integer> entry : players.entrySet()){
                     Util.message(sender, "&7 - &a" + entry.getKey() + "&7 (あと " + TimeUtil.getReadableTimeBySecond(entry.getValue() - now) + ")");
                 }
             }
         }
         else{
             if (sp.hasPower(Power.FLYMODE)){
                 Util.message(player, "&bあなたはあと &a" + FlymodeWorker.getInstance().getRemainTime(player) + "間 &b飛行可能です");
                 return;
             }
             
             ConfirmQueue.getInstance().addQueue(sender, this, null, 15);
             
            Util.message(sender, "&6現在の飛行権価格は &a" + getDuration() + "分間 " + getCost() + " Gold &6です");
             Util.message(sender, "&6一部ワールドでのみ飛行可能になります");
             Util.message(sender, "&6本当に購入しますか？ &a/confirm&6 コマンドで続行します。");
         }
     }
     
     @Override
     public void executeQueue(QueuedCommand queued){
         if (player.getLocation().getY() > 257 || player.getLocation().getY() < 0){
             Util.message(sender, "&cあなたの座標からこのコマンドは使えません！");
             return;
         }
         if (FlymodeWorker.getInstance().getPlayersCount() >= getMaxPlayers()){
             Util.message(sender, "&c同時に飛行可能な最大人数に達しています！ (" + FlymodeWorker.getInstance().getPlayersCount() + "/" + getMaxPlayers() + ")");
             return;
         }
         
         final OrangePlayer sp = PlayerManager.getPlayer(player);
         
         if (sp.hasPower(Power.FLYMODE)){
             Util.message(sender, "&cあなたは飛行モード、または飛行権限が既に有効になっています！");
             return;
         }
         
         // pay cost
         if (!OSHelper.getInstance().isEnableEcon()){
             Util.message(sender, "&c経済システムが動作していないため使えません！");
             return;
         }
         
         double cost = getCost();
         boolean paid = EconomyUtil.takeMoney(player, cost);
         if (!paid){
            Util.message(sender, "&cお金が足りません！ " + cost + "Gold必要です！");
             return;
         }
         
         int minute = getDuration();
         
         final FlymodeWorker worker = FlymodeWorker.getInstance();
         worker.enableFlymode(sp, minute);
         
         Util.message(player, "&a飛行モードが " + minute + "分間 有効になりました！");
        LogUtil.info(player.getName() + " is bought flying mode: " + minute + " minutes for " + cost + " gold");
         OrangeServerUtil.sendlog(player, sp.getName() + " &aが飛行権限を購入しました");
     }
 }
