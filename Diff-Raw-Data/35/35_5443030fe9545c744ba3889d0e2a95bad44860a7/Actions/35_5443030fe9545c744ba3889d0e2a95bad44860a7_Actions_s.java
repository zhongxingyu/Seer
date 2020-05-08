 package syam.Honeychest;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * プラグインに必要なユーティリティが含まれています
  * @author syam
  */
 public class Actions {
 	public final static Logger log = Honeychest.log;
 	private static final String logPrefix = Honeychest.logPrefix;
 	private static final String msgPrefix = Honeychest.msgPerfix;
 
 	public static Honeychest plugin;
 	public Actions(Honeychest instance){
 		plugin = instance;
 	}
 
 	/*
 	 *  TODO: Todo here
 	 *
 	 */
 
 	/****************************************/
 	// メッセージ送信系関数
 	/****************************************/
 	/**
 	 * メッセージをユニキャスト
 	 * @param sender Sender (null可)
 	 * @param player Player (null可)l
 	 * @param message メッセージ
 	 */
 	public static void message(CommandSender sender, Player player, String message){
 		if (message != null){
 			message = message
 					.replaceAll("&([0-9a-fk-or])", "\u00A7$1")
 					.replaceAll("%version", Honeychest.getInstance().getDescription().getVersion());
 			if (player != null){
 				player.sendMessage(message);
 			}
 			else if (sender != null){
 				sender.sendMessage(message);
 			}
 		}
 	}
 	/**
 	 * メッセージをブロードキャスト
 	 * @param message メッセージ
 	 */
 	public static void broadcastMessage(String message){
 		if (message != null){
 			message = message
 					.replaceAll("&([0-9a-fk-or])", "\u00A7$1")
 					.replaceAll("%version", Honeychest.getInstance().getDescription().getVersion());
 			Bukkit.broadcastMessage(message);
 		}
 	}
 	/**
 	 * メッセージをワールドキャスト
 	 * @param world
 	 * @param message
 	 */
 	public static void worldcastMessage(World world, String message){
 		if (world != null && message != null){
 			message = message
 					.replaceAll("&([0-9a-fk-or])", "\u00A7$1")
 					.replaceAll("%version", Honeychest.getInstance().getDescription().getVersion());
 			for(Player player: world.getPlayers()){
 				log.info("[Worldcast]["+world.getName()+"]: " + message);
 				player.sendMessage(message);
 			}
 		}
 	}
 	/**
 	 * メッセージをパーミッションキャスト(指定した権限ユーザにのみ送信)
 	 * @param permission 受信するための権限ノード
 	 * @param message メッセージ
 	 */
 	public static void permcastMessage(String permission, String message){
 		// 動かなかった どうして？
 		//int i = Bukkit.getServer().broadcast(message, permission);
 
 		// OK
 		int i = 0;
 		for (Player player : Bukkit.getServer().getOnlinePlayers()){
 			if (player.hasPermission(permission)){
 				Actions.message(null, player, message);
 				i++;
 			}
 		}
 
 		log.info("Received "+i+"players: "+message);
 	}
 
 	/****************************************/
 	// ヘルプメッセージ
 	/****************************************/
 	public static void sendHelp(CommandSender sender){
 		message(sender, null, "&c===================================");
 		message(sender, null, "&bHoneychest version &3%version &bby syamn");
 		message(sender, null, " &b<>&f = 必要, &b[]&f = オプション");
 		message(sender, null, " /honeychest(/hc)&7 - このヘルプを表示");
 		message(sender, null, " /honeychest manage(/hc m)&7 - Honeychest設置/破壊");
 		message(sender, null, "&c===================================");
 	}
 
 	/****************************************/
 	// ユーティリティ
 	/****************************************/
 	/**
 	 * 文字配列をまとめる
 	 * @param s つなげるString配列
 	 * @param glue 区切り文字 通常は半角スペース
 	 * @return
 	 */
 	public static String combine(String[] s, String glue)
     {
       int k = s.length;
       if (k == 0){ return null; }
       StringBuilder out = new StringBuilder();
       out.append(s[0]);
       for (int x = 1; x < k; x++){
         out.append(glue).append(s[x]);
       }
       return out.toString();
     }
 	/**
 	 * コマンドをコンソールから実行する
 	 * @param command
 	 */
 	public static void executeCommandOnConsole(String command){
 		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
 	}
 	/**
 	 * 文字列の中に全角文字が含まれているか判定
 	 * @param s 判定する文字列
 	 * @return 1文字でも全角文字が含まれていればtrue 含まれていなければfalse
 	 * @throws UnsupportedEncodingException
 	 */
 	public static boolean containsZen(String s)
 			throws UnsupportedEncodingException {
 		for (int i = 0; i < s.length(); i++) {
 			String s1 = s.substring(i, i + 1);
 			if (URLEncoder.encode(s1,"MS932").length() >= 4) {
 				return true;
 			}
 		}
 		return false;
 	}
 	/**
 	 * 現在の日時を yyyy-MM-dd HH:mm:ss 形式の文字列で返す
 	 * @return
 	 */
 	public static String getDatetime(){
 
 		Date date = new Date();
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		return df.format(date);
 	}
 	/**
 	 * 座標データを ワールド名:x, y, z の形式の文字列にして返す
 	 * @param loc
 	 * @return
 	 */
 	public static String returnLocationString(Location loc){
 		return loc.getWorld().getName()+":"+loc.getX()+","+loc.getY()+","+loc.getZ();
 	}
 	public static String returnBlockLocationString(Location loc){
 		return loc.getWorld().getName()+":"+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();
 	}
 
 	/****************************************/
 	// Honeychest
 	/****************************************/
 
 }
