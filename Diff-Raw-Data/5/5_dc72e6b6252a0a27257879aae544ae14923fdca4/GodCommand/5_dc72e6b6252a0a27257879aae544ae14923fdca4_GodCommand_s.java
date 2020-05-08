 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands.player
  * Created: 2013/01/06 17:44:37
  */
 package net.syamn.sakuracmd.commands.player;
 
 import net.syamn.sakuracmd.commands.BaseCommand;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.player.Power;
 import net.syamn.sakuracmd.player.SakuraPlayer;
 import net.syamn.sakuracmd.utils.plugin.SakuraCmdUtil;
 import net.syamn.utils.Util;
 import net.syamn.utils.exception.CommandException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 /**
  * GodCommand (GodCommand.java)
  * @author syam(syamn)
  */
 public class GodCommand extends BaseCommand{
     public GodCommand(){
         bePlayer = false;
         name = "god";
         perm = Perms.GOD;
         argLength = 0;
         usage = "[player] <- toggle godmode";
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
         
         if (sp.hasPower(Power.GODMODE)){
             sp.removePower(Power.GODMODE);
             if (!sender.equals(target)){
                 Util.message(sender, "&3" + sp.getName() + " &3の無敵モードを解除しました");
             }
             Util.message(target, "&3あなたの無敵モードは解除されました");
            SakuraCmdUtil.sendlog(sp.getName() + "&c が無敵モードになりました");
             SakuraCmdUtil.sendlog(sender, sp.getName() + "&3 が無敵モードを解除しました");
        }else{
             sp.addPower(Power.GODMODE);
             if (!sender.equals(target)){
                 Util.message(sender, "&3" + sp.getName() + " &3を無敵モードにしました");
             }
             Util.message(target, "&3あなたは無敵モードになりました");
             SakuraCmdUtil.sendlog(sender, sp.getName() + "&3 が無敵モードになりました");
         }
     }
 }
