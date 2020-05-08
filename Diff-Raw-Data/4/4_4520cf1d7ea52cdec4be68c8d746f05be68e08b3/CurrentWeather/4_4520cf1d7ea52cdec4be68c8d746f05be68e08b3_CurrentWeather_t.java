 /*
  * @author     kebin, ucchy
  * @license    LGPLv3
  * @copyright  Copyright kebin, ucchy 2013
  */
 package com.github.ucchyocean.cw;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Random;
 import java.util.TimeZone;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scheduler.BukkitScheduler;
 import org.bukkit.scheduler.BukkitTask;
 
 import com.github.ucchyocean.cw.japan.JapanSelectorFrame;
 import com.github.ucchyocean.cw.owm.OpenWeatherMapAccessException;
 import com.github.ucchyocean.cw.owm.OpenWeatherMapAccesser;
 import com.github.ucchyocean.cw.owm.OpenWeatherMapCity;
 import com.github.ucchyocean.cw.owm.OpenWeatherMapWeather;
 
 /**
  * Current weather plugin.
  * @author ucchy
  */
 public class CurrentWeather extends JavaPlugin implements Listener {
     
     private static final String PREFIX = ChatColor.GOLD + "[CW]" + ChatColor.WHITE;
     
     // インスタンス
     protected static CurrentWeather instance;
     
     // 前回取得した天気情報
     protected static OpenWeatherMapWeather lastWeather;
     
     // コンフィグ
     protected static CurrentWeatherConfig config;
     
     // メッセージ
     private String msg_notify;
     private String msg_notify_url;
     
     // 非同期タスクのID
     private int syncTimeTaskID = -1;
     private int syncWeatherTaskID = -1;
 
     /**
      * プラグインが有効化されたときのイベント
      * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
      */
     @Override
     public void onEnable() {
         
         instance = this;
         
         // イベントリスナーとして登録する
         getServer().getPluginManager().registerEvents(this, this);
         
         // コンフィグの作成
         config = CurrentWeatherConfig.loadConfig();
         
         // メッセージリソースのロード
         Messages.initialize();
         msg_notify = PREFIX + " " + Messages.get("notify_message");
         msg_notify_url = PREFIX + " " + Messages.get("notify_url");
         
         // 非同期タスクの開始
         startAsyncTask();
     }
     
     /**
      * プレイヤーがサーバーに参加したときのイベント
      * @param event 
      */
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         
         // 現在の天気が取得済みのとき、ログインしてきた人に情報を流す。
         if ( lastWeather != null ) {
             Player player = event.getPlayer();
             
             int weatherCode = lastWeather.getWeatherNumber();
             String weather_localized = Messages.get(
                     String.valueOf(weatherCode), lastWeather.getWeather());
             String cityname_localized = Messages.get(
                     lastWeather.getCity().getName(), lastWeather.getCity().getName());
 
             String message = String.format(msg_notify,
                     cityname_localized,
                     weather_localized,
                     lastWeather.getTemparature() );
             player.sendMessage(message); 
             player.sendMessage(String.format(msg_notify_url, 
                     OpenWeatherMapAccesser.getDetailURL(lastWeather.getCity().getId()))); 
         }
     }
 
     /**
      * プラグインのコマンドが実行されたときのイベント
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     @Override
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length == 0 ) {
             return false;
         }
 
         final CommandSender s = sender;
 
         if ( args.length >= 2 && args[0].equalsIgnoreCase("find") ) {
             
             final String name;
             final double lat;
             final double lon;
             
             if ( args.length >= 3 && 
                     Utility.tryFloatParse(args[1]) && 
                     Utility.tryFloatParse(args[2]) ) {
                 // 緯度経度指定検索の場合
                 
                 name = null;
                 lat = Double.parseDouble(args[1]);
                 lon = Double.parseDouble(args[2]);
 
             } else {
                 // 都市名検索の場合
                 
                 name = args[1];
                 lat = -1;
                 lon = -1;
             }
             
             // 都市情報取得
             Bukkit.getScheduler().runTaskAsynchronously(this, new BukkitRunnable() {
                 @Override
                 public void run() {
                     
                     try {
                         ArrayList<OpenWeatherMapWeather> results;
                         
                         if ( name != null ) 
                             results = OpenWeatherMapAccesser.findCity(name);
                         else
                             results = OpenWeatherMapAccesser.findCity(lat, lon);
                         
                         for ( OpenWeatherMapWeather r : results ) {
                             OpenWeatherMapCity city = r.getCity();
                             String desc = String.format(
                                     ChatColor.GOLD + "%d" + ChatColor.WHITE + " %s, %s (%.2f, %.2f)", 
                                     city.getId(),
                                     city.getName(), city.getCountry(), city.getLat(), city.getLon() );
                             s.sendMessage(desc);
                         }
                         
                     } catch (OpenWeatherMapAccessException e) {
                         e.printStackTrace();
                         s.sendMessage(ChatColor.RED + e.getLocalizedMessage());
                     }
                 }
             });
             
             return true;
 
             
         } else if ( args[0].equalsIgnoreCase("japan") ) {
             
             // コンソールからの実行でない場合はエラー
             if ( !(sender instanceof ConsoleCommandSender) ) {
                 sender.sendMessage(ChatColor.RED + "This command can be used only by console.");
                 return true;
             }
 
             Bukkit.getScheduler().runTaskAsynchronously(this, new BukkitRunnable() {
                 @Override
                 public void run() {
 
                     // 地図を開く
                     JapanSelectorFrame frame = new JapanSelectorFrame();
                     OpenWeatherMapWeather weather = frame.showWindow();
                     
                     if ( weather != null ) {
                         CurrentWeather.lastWeather = weather;
                         
                         // 設定ファイルにIDを保存
                         CurrentWeather.config.setCity(weather.getCity().getId());
                         CurrentWeather.config.saveConfig();
                         
                         s.sendMessage(PREFIX + " City was set " + weather.getCity().getId() + ".");
 
                         // 天気情報を取得・同期
                         CurrentWeather.instance.getWeatherInformation();
                     }
                 }
             });
 
             return true;
             
             
         } else if ( args.length >= 2 && args[0].equalsIgnoreCase("set") ) {
             
             // IDとして正しくない文字列が入っていたらエラー
             if ( !args[1].matches("[0-9]{1,10}") ) {
                 sender.sendMessage(ChatColor.RED + "Command error.");
                 sender.sendMessage(ChatColor.YELLOW + "usage: /" + label + " set (id)");
                 return true;
             }
             final int id = Integer.parseInt(args[1]);
             
             Bukkit.getScheduler().runTaskAsynchronously(this, new BukkitRunnable() {
                 @Override
                 public void run() {
                     
                     try {
                         // 天気情報取得、IDが見つからないならエラー
                         lastWeather = OpenWeatherMapAccesser.getCurrentWeather(id);
                         
                         // 設定ファイルにIDを保存
                         CurrentWeather.config.setCity(id);
                         CurrentWeather.config.saveConfig();
                         
                         s.sendMessage(PREFIX + " City was set " + id + ".");
 
                         // 天気情報を取得・同期
                         getWeatherInformation();
                         
                     } catch (OpenWeatherMapAccessException e) {
                         e.printStackTrace();
                         s.sendMessage(PREFIX + ChatColor.RED + " Not found city id " + id);
                     }
                 }
             });
 
             return true;
             
             
         } else if ( args[0].equalsIgnoreCase("clear") ) {
             
             // 設定の消去
             CurrentWeather.config.setCity(-1);
             CurrentWeather.config.saveConfig();
             lastWeather = null;
             
             // 天気の再設定
             int duration = (300 + new Random().nextInt(600)) * 20;
             for ( World world : getServer().getWorlds() ) {
                 world.setWeatherDuration(duration);
                 world.setThunderDuration(duration);
                 world.setStorm(false);
                 world.setThundering(false);
             }
             
             // 時刻のクリア
             for ( World world : Bukkit.getWorlds() ) {
                 world.setGameRuleValue("doDaylightCycle", String.valueOf(true));
             }
             
             sender.sendMessage(PREFIX + " Weather was cleared.");
 
             return true;
             
             
         } else if ( args[0].equalsIgnoreCase("sync") ) {
             
             // 天気情報を取得・同期
             Bukkit.getScheduler().runTaskAsynchronously(this, new BukkitRunnable() {
                 @Override
                 public void run() {
                     getWeatherInformation();
                     s.sendMessage(PREFIX + " Weather was synchronized.");
                 }
             });
 
             return true;
             
             
         } else if ( args[0].equalsIgnoreCase("reload") ) {
             
             // コンフィグのリロード
             config = CurrentWeatherConfig.loadConfig();
             Messages.initialize();
             msg_notify = PREFIX + " " + Messages.get("notify_message");
             msg_notify_url = PREFIX + " " + Messages.get("notify_url");
             
             // 非同期タスクを再起動して、インターバル設定を適用する
             startAsyncTask();
             
             sender.sendMessage(PREFIX + " Reload completed.");
 
             return true;
             
             
         }
         
         return false;
     }
 
     /**
      * 設定された地域の天気を取得する。
      * このメソッドは処理が重いため、必ず非同期タスクで実行すること。
      */
     public void getWeatherInformation() {
 
         // 前回の天気コードを取得して、天気情報をいったん削除する
         int lastCode;
         int lastCity;
         if ( CurrentWeather.lastWeather == null ) {
             lastCode = -1;
             lastCity = -1;
         } else {
             lastCode = CurrentWeather.lastWeather.getWeatherNumber();
             lastCity = CurrentWeather.lastWeather.getCity().getId();
         }
         CurrentWeather.lastWeather = null;
         
         // コンフィグから、現在設定されている都市IDを取得
         // 取得できなければ何もしない
         int cityid = CurrentWeather.config.getCity();
         if ( cityid == -1 ) {
             return;
         }
         
         // 天気情報取得
         try {
             CurrentWeather.lastWeather = OpenWeatherMapAccesser.getCurrentWeather(cityid);
         } catch (OpenWeatherMapAccessException e) {
             e.printStackTrace();
             return;
         }
 
         // ワールドに天気を反映
         Bukkit.getScheduler().runTask(instance, new BukkitRunnable() {
             @Override
             public void run() {
                 syncWeather();
             }
         });
         
         // 今回の天気コードを取得、前回から変わっているなら、メッセージを流す
         int code = CurrentWeather.lastWeather.getWeatherNumber();
         int city = CurrentWeather.lastWeather.getCity().getId();
         if ( lastCode != code || lastCity != city ) {
             broadcastWeatherInformation();
         }
     }
     
     /**
      * lastWeather の内容を、ワールドへ同期させる。
      * このメソッドは、同期タスク内で実行すること。
      */
     protected void syncWeather() {
         
         // Precipitation（降水量）が0以上なら、ワールドを雨にする
         boolean isRain = CurrentWeather.lastWeather.getPrecipitation() > 0;
         
         // 天気コードが200番台なら、ワールドに雷を発生する
         boolean isThundering = 
                 (200 <= CurrentWeather.lastWeather.getWeatherNumber() && 
                         CurrentWeather.lastWeather.getWeatherNumber() <= 299);
         List<String> ignoreWorlds = CurrentWeather.config.getIgnoreWorlds();
         
         // ワールドに設定
         for ( World world : CurrentWeather.instance.getServer().getWorlds() ) {
             if ( !ignoreWorlds.contains(world.getName()) ){
                world.setWeatherDuration(20000000);
                world.setThunderDuration(20000000);
                 world.setStorm(isRain);
                 world.setThundering(isThundering);
             }
         }
     }
     
     /**
      * 設定された地域の経度から概算時差を求め、ワールドに反映する。
      * このメソッドは、同期タスク内で実行すること。
      */
     protected void syncTime() {
         
         boolean isEnabled = CurrentWeather.config.isSyncTime();
         
         // 設定が無効、または、天気未取得なら動作しない
         if ( !isEnabled || CurrentWeather.lastWeather == null ) {
             return;
         }
 
         // Minecraft時間を算出
         double lon = CurrentWeather.lastWeather.getCity().getLon();
         int mctime = getMinecraftTimeFromLon(lon);
         
         // ワールドに設定
         List<String> ignoreWorld = CurrentWeather.config.getIgnoreWorlds();
         for ( World world : Bukkit.getWorlds() ) {
             if ( !ignoreWorld.contains(world.getName()) ) {
                 long days = world.getFullTime() / 24000;
                 long time = days * 24000 + mctime;
                 world.setFullTime(time);
                 world.setGameRuleValue("doDaylightCycle", String.valueOf(false));
             }
         }
     }
     
     /**
      * サーバー全体メッセージで、天気情報を流す
      */
     public void broadcastWeatherInformation() {
         
         if ( lastWeather == null ) {
             return;
         }
         
         int weatherCode = lastWeather.getWeatherNumber();
         String weather_localized = Messages.get(
                 String.valueOf(weatherCode), lastWeather.getWeather());
         String cityname_localized = Messages.get(
                 lastWeather.getCity().getName(), lastWeather.getCity().getName());
 
         String message = String.format(msg_notify,
                 cityname_localized,
                 weather_localized,
                 lastWeather.getTemparature() );
         Bukkit.broadcastMessage(message);
         Bukkit.broadcastMessage(String.format(msg_notify_url, 
                 OpenWeatherMapAccesser.getDetailURL(lastWeather.getCity().getId()))); 
     }
     
     /**
      * 非同期タスクを開始する。
      * 既に開始中の場合は、既にあるタスクを終了して、新しいタスクを開始する。
      */
     private void startAsyncTask() {
         
         CurrentWeather.instance.getLogger().finest("- entered startAsyncTask timetask=" + 
                 syncTimeTaskID + ", weathertask=" + syncWeatherTaskID);
         
         BukkitScheduler scheduler = Bukkit.getScheduler();
         
         // 既に開始中のタスクがある場合は、停止を行う
         if ( scheduler.isCurrentlyRunning(syncTimeTaskID) ) {
             scheduler.cancelTask(syncTimeTaskID);
         }
         if ( scheduler.isCurrentlyRunning(syncWeatherTaskID) ) {
             scheduler.cancelTask(syncWeatherTaskID);
         }
 
         BukkitTask task = null;
 
         // 時刻更新タスクの起動
         int it = config.getIntervalTicksSyncTime();
         task = scheduler.runTaskTimerAsynchronously(this, new SyncTimeTask(), it, it);
         syncTimeTaskID = task.getTaskId();
         
         // 天気更新タスクの起動
         int iw = config.getIntervalTicksSyncWeater();
         task = scheduler.runTaskTimerAsynchronously(this, new SyncWeatherTask(), 1, iw);
         syncWeatherTaskID = task.getTaskId();
         
         CurrentWeather.instance.getLogger().finest("- exit startAsyncTask timetask=" + 
                 syncTimeTaskID + ", weathertask=" + syncWeatherTaskID);
     }
 
     /**
      * 経度から、概算の時差を求め、それをMinecraft時間に変換して返す
      * @param lon 経度
      * @return Minecraft時間
      */
     private static int getMinecraftTimeFromLon(double lon) {
         
         // 緯度から概算のGMT時差を算出する
         int timezoneHour = (int)Math.round( (double)((int)(lon + 360) % 360) / 15.0 );
         if ( timezoneHour > 12 ) {
             timezoneHour -= 24;
         }
         
         // 現地時刻のカレンダーを作成する
         String timezoneID = String.format("GMT%+2d:00", timezoneHour);
         Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezoneID));
 
         // 現在時刻（0時からのミリ秒）を算出する
         int hour = cal.get(Calendar.HOUR_OF_DAY);
         int minute = cal.get(Calendar.MINUTE);
         int second = cal.get(Calendar.SECOND);
         int milli = cal.get(Calendar.MILLISECOND);
         int current = ((hour * 60 + minute) * 60 + second) * 1000 + milli;
         
         // Minecraft時間に変換して返す
         // 参考：http://minecraft-ja.gamepedia.com/%E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB:Day_Night_Clock_24h.png
         return ( (int)(current / 3600) + 18000 ) % 24000;
     }
 
     /**
      * このプラグインのJarファイル自身を示すFileクラスを返す。
      * @return
      */
     public File getPluginJarFile() {
         return this.getFile();
     }
 }
