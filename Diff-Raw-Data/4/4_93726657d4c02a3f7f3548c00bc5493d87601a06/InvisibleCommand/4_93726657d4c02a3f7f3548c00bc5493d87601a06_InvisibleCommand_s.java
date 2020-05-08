 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands.player
  * Created: 2013/01/04 13:55:35
  */
 package net.syamn.sakuracmd.commands.player;
 
 import static net.syamn.sakuracmd.storage.I18n._;
 import net.syamn.sakuracmd.SCHelper;
 import net.syamn.sakuracmd.commands.BaseCommand;
 import net.syamn.sakuracmd.feature.GeoIP;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.player.SakuraPlayer;
 import net.syamn.sakuracmd.storage.I18n;
 import net.syamn.sakuracmd.utils.plugin.SakuraCmdUtil;
 import net.syamn.sakuracmd.worker.InvisibleWorker;
 import net.syamn.utils.Util;
 import net.syamn.utils.exception.CommandException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 /**
  * InvisibleCommand (InvisibleCommand.java)
  * @author syam(syamn)
  */
 public class InvisibleCommand extends BaseCommand{
     public InvisibleCommand(){
         bePlayer = false;
         name = "invisible";
         perm = Perms.INVISIBLE;
         argLength = 0;
         usage = "<- toggle your invisible status";
     }
     
     public void execute() throws CommandException{
         if (args.size() == 0 && !isPlayer){
             throw new CommandException("&cプレイヤー名を指定してください！");
         }
         
         final Player target = (args.size() > 0) ? Bukkit.getPlayer(args.get(0)) : player;
         if (target == null || !target.isOnline()){
             throw new CommandException("&cプレイヤーが見つかりません！");
         }
         final SakuraPlayer sp = PlayerManager.getPlayer(target);
         
         InvisibleWorker worker = InvisibleWorker.getInstance();
         
         if (worker.isInvisible(target)){
             worker.reappear(target);
             
             // send fake join message
             String msg = _("joinMessage", I18n.PLAYER, sp.getName(true));
             if (msg != null && !msg.isEmpty()) {
                if (SCHelper.getInstance().getConfig().getUseGeoIP() && !Perms.HIDE_GEOIP.has(player)){
                    String geoStr = GeoIP.getInstance().getGeoIpString(player, SCHelper.getInstance().getConfig().getUseSimpleFormatOnJoin());
                     msg = msg + Util.coloring("&7") + " (" + geoStr + ")";
                 }
                 Util.broadcastMessage(msg);
             }
              
             if (!sender.equals(target)){
                 Util.message(sender, "&a" + sp.getName() + " &aの透明モードを解除しました");
             }
             Util.message(target, "&aあなたの透明モードは解除されました");
             SakuraCmdUtil.sendlog(sender, sp.getName() + "&a が透明モードを解除しました");
         }else{
             worker.vanish(target, false);
             
             // send fake quit message
             String msg = _("quitMessage", I18n.PLAYER, sp.getName(true));
             if (msg != null && !msg.isEmpty()) {
                 Util.broadcastMessage(msg);
             }
             
             if (!sender.equals(target)){
                 Util.message(sender, "&c" + sp.getName() + " &cを透明モードにしました");
             }
             Util.message(target, "&cあなたは透明モードになりました");
             SakuraCmdUtil.sendlog(sender, sp.getName() + "&c が透明モードになりました");
         }
     }
 }
