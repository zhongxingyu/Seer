 package com.github.owatakun.mahime;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.command.BlockCommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 public class MahimeCommandExecutor implements CommandExecutor {
 
 	private Plugin plugin;
 	private Logger logger;
 	private FileConfiguration config;
 	private File kitsFile;
 	private YamlConfiguration kitsConf;
 	private Kits Kits;
 	private LinkedHashMap<String, String> playersJob;
 	private LinkedHashMap<String, String> jobPlayers;
 	private File rcFile;
 	private YamlConfiguration rcConf;
 	private RandomChest rc;
 
 	public MahimeCommandExecutor(FileConfiguration config, Plugin plugin){
 		this.plugin = plugin;
 		this.logger = plugin.getLogger();
 		this.config = config;
 		this.kitsFile = new File(plugin.getDataFolder(), "kits.yml");
 		this.kitsConf = YamlConfiguration.loadConfiguration(kitsFile);
 		this.Kits = new Kits(plugin, kitsConf);
 		this.playersJob = new LinkedHashMap<String, String>();
 		this.jobPlayers = new LinkedHashMap<String, String>();
 		this.rcFile = new File(plugin.getDataFolder(), "rc.yml");
 		this.rcConf = YamlConfiguration.loadConfiguration(rcFile);
 		this.rc = new RandomChest(plugin, config, rcConf);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		// reload
 		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
 			// config.yml
 			plugin.reloadConfig();
 			config = plugin.getConfig();
 			// kits.yml
 			kitsConf = YamlConfiguration.loadConfiguration(kitsFile);
 			Kits = new Kits(plugin, kitsConf);
 			// rc.yml
 			rcConf = YamlConfiguration.loadConfiguration(rcFile);
 			rc = new RandomChest(plugin, config, rcConf);
 			sender.sendMessage("設定を再読み込みしました");
 			return true;
 		}
 		// kit配布
 		if (args.length == 1 && args[0].equalsIgnoreCase("gkit")) {
 			try {
 				return execgkit (sender, cmd, commandLabel, args);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		// 指定kit配布
 		if (args.length >= 2 && args[0].equalsIgnoreCase("gskit")) {
 			return execgskit (sender, cmd, commandLabel, args);
 		}
 		// kitリスト
 		if (args.length == 1 && args[0].equalsIgnoreCase("kitlist")) {
 			return execkitlist (sender, cmd, commandLabel, args);
 		}
 		// RandomChest
 		if (args.length >= 1 && args[0].equalsIgnoreCase("rc")) {
 			return execrc (sender, cmd, commandLabel, args);
 		}
 		return false;
 	}
 
 	/**
 	 * gkit実行
 	 * @throws IOException
 	 */
 	private boolean execgkit(CommandSender sender, Command cmd,	String commandLabel, String[] args) throws IOException {
 		// kits.ymlの確認
 		if (!kitsFile.exists()) {
 			sender.sendMessage("Error: kits.ymlが存在しません");
 			logger.severe("Error: kits.ymlが存在しません");
 			return true;
 		}
 		// 職一覧を取得
 		List<String> jobList = config.getStringList("UseJobs");
 		// gm1以外のオンラインリストを生成
 		List<Player> players = new ArrayList<Player>();
 		for (Player player: plugin.getServer().getOnlinePlayers()) {
 			if (player.getGameMode().getValue() != 1) {
 				players.add(player);
 			}
 		}
 		if (players == null | players.size() == 0) {
 			sender.sendMessage("Error: 対象が1人も存在しません");
 			return true;
 		}
 		// 職の数が足りないときはキャンセル
 		if (players.size() > jobList.size()) {
 			sender.sendMessage("Error: 設定されている職数より参加人数が多いため中止します");
 			return true;
 		}
 		// 読み込まれているKitがすべて正常か確認
 		for (String kitName: jobList) {
 			if (!kitsConf.contains("Kits." + kitName)) {
 				sender.sendMessage("Error: config.ymlで指定された \"" + kitName + "\" が存在しないため中止します");
 				return true;
 			}
 		}
 		// プレイヤーシャッフルして職を割り当てMapに保存
 		Collections.shuffle(players);
 		playersJob.clear();
 		jobPlayers.clear();
 		for (int i = 0; i < players.size(); i++) {
 			playersJob.put(players.get(i).getName(), jobList.get(i));
 			jobPlayers.put(jobList.get(i), players.get(i).getName());
 		}
 		// ログファイル初期化
 		SimpleDateFormat time = new SimpleDateFormat("MMdd'_'HHmm");
 		Date date = new Date();
 		File logFile = new File(plugin.getDataFolder(), "log_" + time.format(date) + ".txt");
 		if (logFile.exists()) {
 			logFile.renameTo(new File(logFile.getName() + ".bak"));
 		}
 		logFile.createNewFile();
 		BufferedWriter log = new BufferedWriter(new FileWriter(logFile, true));
 		time.applyPattern("yyyy'/'MM'/'dd HH':'mm':'ss");
 		log.write("魔姫kitログ 実行日時:" + time.format(date));
 		log.newLine();
 		log.newLine();
 		// kit配布、メッセージ発信、ログ書き出し
 		for (Map.Entry<String, String> e: playersJob.entrySet()) {
 			Player player = plugin.getServer().getPlayer(e.getKey());
 			String job = e.getValue();
 			// 回復
 			player.setFoodLevel(20);
 			player.setSaturation(10);
 			player.setHealth(player.getMaxHealth());
 			// kit配布(returnは現在すべてtrue)
 			if (Kits.giveKit(player, job)) {
 				String kitName, message, kitNotice;
 				if (kitsConf.contains("Kits." + job + ".name")) {
 					kitName = kitsConf.getString("Kits." + job + ".name");
 				} else {
 					kitName = job;
 				}
 				// kit配布後メッセージ送付
 				if (config.contains("gkitMessage")) {
 					message = config.getString("gkitMessage");
 					message = message.replaceAll("%job%", kitName);
 					player.sendMessage(Util.repSec(message));
 				}
 				// kit個別メッセージ送付
 				if (config.contains("KitNotice." + job)) {
 					kitNotice = config.getString("KitNotice." + job);
 					kitNotice = kitNotice.replaceAll("%job%", kitName);
 					kitNotice = repVar(kitNotice);
 					player.sendMessage(Util.repSec(kitNotice));
 				}
 				// ログ出力
 				log.write(kitName + "\t" + player.getName() + "\t");
 				log.newLine();
 			}
 		}
 		log.close();
 		if (config.getBoolean("AutoLogOpen")) {
			ProcessBuilder pb = new ProcessBuilder(config.getString("notepadPath"), logFile.toString());
 			pb.start();
 		}
 		sender.sendMessage("配布が完了しました");
 		return true;
 	}
 
 	/**
 	 * 指定したkitを配布
 	 */
 	private boolean execgskit(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 		String kitName = null;
 		if (args.length == 2) {
 			if (sender instanceof Player) {
 				player = (Player) sender;
 			} else {
 				sender.sendMessage("Error: このコマンドはプレイヤーのみ実行出来ます");
 				return true;
 			}
 		} else if (args.length == 3) {
 			player = plugin.getServer().getPlayer(args[2]);
 			if (player == null) {
 				sender.sendMessage("Error: 対象が存在しません");
 				return true;
 			}
 		}
 		kitName = args[1];
 		if (!kitsConf.contains("Kits." + kitName)) {
 			sender.sendMessage("Error: 指定されたkit \"" + kitName + "\" が存在しません");
 			return true;
 		}
 		if (Kits.giveKit(player, kitName)) {
 			String name;
 			if (kitsConf.contains("Kits." + kitName + ".name")) {
 				name = kitsConf.getString("Kits." + kitName + ".name");
 				sender.sendMessage(player.getName() + " に " + name + "(" + kitName + ")" + " を配布しました");
 			} else {
 				sender.sendMessage(player.getName() + " に " + kitName + " を配布しました");
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * kitlist実行
 	 */
 	private boolean execkitlist(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		// 職一覧を取得
 		List<String> jobList = config.getStringList("UseJobs");
 		String kitName;
 		for (String job: jobList) {
 			kitName = null;
 			if (kitsConf.contains("Kits." + job)) {
 				kitName = kitsConf.getString("Kits." + job + ".name");
 			} else {
 				kitName = "Error: kits.ymlに存在しません";
 			}
 			sender.sendMessage(job + " , " + kitName);
 		}
 		return true;
 	}
 
 	/**
 	 * rcコマンド
 	 */
 	private boolean execrc(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		// ワールド取得
 		World world;
 		if (sender instanceof Player) {
 			world = ((Player) sender).getWorld();
 		} else if (sender instanceof BlockCommandSender){
 			world = ((BlockCommandSender) sender).getBlock().getWorld();
 		} else {
 			sender.sendMessage("このコマンドはゲーム内からのみ実行出来ます");
 			return true;
 		}
 		// rcコマンド
 		if (args.length == 1) {
 			ArrayList<Point> list = rc.getrcPointListCopy();
 			if (list.size() < 4) {
 				sender.sendMessage("Error: ポイント数が4未満です");
 				return true;
 			}
 			Collections.shuffle(list);
 			plugin.getLogger().info("RandomChest Result");
 			for (int i = 0; i < 4; i++) {
 				Point pt = list.get(i);
 				int x, y, z;
 				x = pt.getX();
 				y = pt.getY();
 				z = pt.getZ();
 				Block block = world.getBlockAt(x, y, z);
 				block.setTypeId(54);
 				Chest chest = (Chest) block.getState();
 				ItemStack item = new ItemStack(264, 1);
 				chest.getInventory().setItem(13, item);
 				plugin.getLogger().info(i + 1 + " : " +  pt.serialize());
 			}
 			sender.sendMessage("チェストを配置しました。配置先はコンソールを参照してください");
 			return true;
 		}
 		// list
 		if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
 			int page;
 			if (args.length == 3 && Util.tryIntParse(args[2])) {
 				page = Integer.parseInt(args[2]);
 			} else if (args.length == 2) {
 				page = 1;
 			} else {
 				return false;
 			}
 			int max = page * 10;
 			ArrayList<Point> list = rc.getrcPointListCopy();
 			if (list.size() < max) {
 				max = list.size();
 			}
 			sender.sendMessage(Util.repSec("&3") + "設定ポイントリスト " + (page * 10 -9) + "～" + max + "件目 / " + list.size() + "件中");
 			for (int i = page * 10 - 10; i < max; i++) {
 				sender.sendMessage(list.get(i).serialize());
 			}
 			return true;
 		}
 		// edit
 		if (args.length >= 2 && args[1].equalsIgnoreCase("edit")) {
 			// コマンドブロックはEditModeは使用させない
 			if (sender instanceof BlockCommandSender) {
 				sender.sendMessage("Error: EditModeはプレイヤーのみ実行出来ます");
 				return true;
 			}
 			// edit start
 			if (args.length == 3 && args[2].equalsIgnoreCase("start")) {
 				// 現在EditModeなら抜ける
 				if (RandomChest.instance.isRcEditMode()) {
 					sender.sendMessage("Error: 既にEditModeです");
 					return true;
 				}
 				// EditModeに入る
 				rc.enableEditMode(world, sender);
 				return true;
 			}
 			// edit end
 			if (args.length == 3 && args[2].equalsIgnoreCase("end")) {
 				if (RandomChest.instance.isRcEditMode()) {
 					rc.disableEditMode(sender);
 				} else {
 					sender.sendMessage("Error: EditModeではありません");
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * 変数を変換する
 	 * @param source 変換対象の文字列
 	 * @return 変換後の文字列
 	 */
 	private String repVar(String source) {
 		String message = source;
 		for (Map.Entry<String, String> e: jobPlayers.entrySet()) {
 			message = message.replaceAll("%" + e.getKey() + "%", e.getValue());
 		}
 		return message;
 	}
 }
