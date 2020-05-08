 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands.db
  * Created: 2013/01/13 4:05:43
  */
 package net.syamn.sakuracmd.commands.db;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.apache.commons.lang.RandomStringUtils;
 
import net.syamn.sakuracmd.SCHelper;
 import net.syamn.sakuracmd.commands.BaseCommand;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.sakuracmd.storage.Database;
 import net.syamn.utils.TimeUtil;
 import net.syamn.utils.Util;
 import net.syamn.utils.exception.CommandException;
 
 /**
  * RegisterCommand (RegisterCommand.java)
  * @author syam(syamn)
  */
 public class RegisterCommand extends BaseCommand{
     public RegisterCommand(){
         bePlayer = true;
         name = "register";
         perm = Perms.REGISTER;
         argLength = 0;
         usage = "<- regist to website";
     }
     
     public void execute() throws CommandException{
         Util.message(sender, "&6登録処理を行っています...");
         
         final Database db = Database.getInstance();
         if (db == null || !db.isConnected()){
             throw new CommandException("&c現在データベースと接続されていません！");
         }
         
         final String pname = player.getName();
         
         // check if already registered
         HashMap<Integer, ArrayList<String>> records = db.read("SELECT * FROM `user_data` WHERE `player_name` = ?", pname);
         if (records != null && records.size() > 0){
             throw new CommandException("&c既に登録されています！");
         }
         
         // generate random 4-chars regist key
        // not use following letters: IL1 il O0
        final String registKey = RandomStringUtils.random(4, "abcdefghjkmnpqrstuvwxABCDEFGHJKMNOPQRSTUVWXYZ23456789");
         
         // expired time
         final int expired = TimeUtil.getCurrentUnixSec().intValue() + (60 * 30); // available for 30 minutes
         
         final boolean success = db.write("REPLACE INTO `regist_key` (`player_name`, `key`, `expired`) VALUES (?, ?, ?)", pname, registKey, expired);
         if (!success){
             throw new CommandException("&c登録に失敗しました！時間を置いてやり直してください！");
         }
         
         Util.message(sender, "&aあなたの登録キー:&2 " + registKey);
         Util.message(sender, "&a登録用のウェブページから、上記の登録キーを入力して続行してください");
         Util.message(sender, "&aこの登録キーは発行後30分間のみ有効です");
     }
 }
