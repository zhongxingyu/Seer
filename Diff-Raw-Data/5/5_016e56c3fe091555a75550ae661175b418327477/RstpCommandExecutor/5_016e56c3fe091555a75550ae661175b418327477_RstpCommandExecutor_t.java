 package com.github.owatakun.rstp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.BlockCommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 public class RstpCommandExecutor implements CommandExecutor{
 
 	private RstpConfig config;
 	private Plugin plugin;
 
 	public RstpCommandExecutor(RstpConfig config, Plugin plugin){
 		this.config = config;
 		this.plugin = plugin;
 	}
 
 	/**
 	 * コマンド実行
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		// reload
 		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
 			config.reload();
 			// エラーチェック
 			if (config.isSuccesfullyLoaded()) {
 				sender.sendMessage(Utility.msg("header") + Utility.replaceSection("&2") + "設定を再読み込みしました");
 				return true;
 			} else {
 				sender.sendMessage(Utility.msg("error") + Utility.replaceSection("&c") + "Configのフォーマットが不適切なため、リストの取得に失敗しました\nConfigを修正後、/rstp reloadで設定を再読み込みしてください");
 				return true;
 			}
 		}
 		// エラーチェック
 		if (!config.isSuccesfullyLoaded()) {
 			sender.sendMessage(Utility.msg("error") + Utility.replaceSection("&c") + "Configのフォーマットが不適切なため、リストの取得に失敗しています\nConfigを修正後、/rstp reloadで設定を再読み込みしてください");
 			return true;
 		}
 		// rstpコマンド
 		if (args.length == 0) {
 			return execRstp(sender, cmd, commandLabel, args);
 		}
 		// listコマンド
 		if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
 			return execList(sender, cmd, commandLabel, args);
 		}
 		// addコマンド
 		if (args.length >= 2 && args[0].equalsIgnoreCase("add")) {
 			return execAdd(sender, cmd, commandLabel, args);
 		}
 		// removeコマンド
 		if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
 			return execRemove(sender, cmd, commandLabel, args);
 		}
 		// tpコマンド
 		if (args.length >= 2 && args[0].equalsIgnoreCase("tp")) {
 			return execTp(sender, cmd, commandLabel, args);
 		}
 		// saveコマンド
 		if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
 			config.save();
 			sender.sendMessage(Utility.msg("header") + Utility.replaceSection("&2") + "設定を保存しました");
 			return true;
 		}
 		return false; //コマンド形式が変だったらfalseを返す
 	}
 
 	/**
 	 * Rstpコマンド実行
 	 */
 	private boolean execRstp(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		// ワールド取得、コンソールからの実行は弾く
 		World world;
 		if (sender instanceof Player) {
 			world = ((Player) sender).getWorld();
 		} else if (sender instanceof BlockCommandSender){
 			world = ((BlockCommandSender) sender).getBlock().getWorld();
 		} else {
 			sender.sendMessage(Utility.msg("senderErr"));
 			return true;
 		}
 		// リスト・オンラインプレイヤー取得
 		List<Point> list = config.getPoints();
 		Player[] tempPlayers = plugin.getServer().getOnlinePlayers();
		// gm1以外を除外したリストを作成
 		List<Player> players = new ArrayList<Player>();
 		for (Player tempPlayer: tempPlayers) {
			if (tempPlayer.getGameMode().getValue() != 1) {
 				players.add(tempPlayer);
 			}
 		}
 		// テレポート対象が0人なら中止する
 		if (players.size() == 0) {
 			sender.sendMessage(Utility.msg("error") + Utility.replaceSection("&c") + "テレポート実行対象が存在しないため、実行を中止します");
 			return true;
 		}
 		// プレイヤーの方が多い場合は中止する
 		if (list.size() < players.size()) {
 			sender.sendMessage(Utility.msg("error") + Utility.replaceSection("&c") + "テレポート箇所よりオンライン人数が多いため、実行を中止します");
 			return true;
 		}
 		// インデックスアクセス用シャッフルリストを作成
 		List<Integer> index = new ArrayList<Integer>(list.size());
 		for (int i = 0; i < list.size(); i++) {
 			index.add(i);
 		}
 		// リストをシャッフル
 		Collections.shuffle(index);
 		// テレポート処理
 		int i = 0;
 		for (Player player: players) {
 			if (player != null) {
 				// Location組立
 				Point tempLoc = list.get(index.get(i));
 				// ブロックの中央にTPするために座標に+0.5する
 				Location loc = new Location(world, tempLoc.getX() + 0.5, tempLoc.getY() + 0.5, tempLoc.getZ() + 0.5);
 				player.sendMessage(Utility.replaceSection("&2") + "Teleporting...");
 				player.teleport(loc);
 			}
 			i++;
 		}
 		sender.sendMessage(Utility.msg("header") + "テレポートを完了しました!");
 		return true;
 	}
 
 	/**
 	 * Listコマンド実行
 	 */
 	private boolean execList(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		List<Point> list = config.getPoints();
 		// 引数から表示するページ数決定
 		int page;
 		if (args.length == 2 && args[1].matches("^[0-9]*")){
 			// 引数が2つで2つめが数字ならそのまま代入
 			page = Integer.parseInt(args[1]);
 		} else if (args.length == 1){
 			// 2つめが省略されていたら1ページ
 			page = 1;
 		} else {
 			// 数字じゃない、引数が多いなどはすべて処理を抜ける
 			sender.sendMessage(Utility.msg("error") + Utility.msg("cmdErr"));
 			sender.sendMessage("/rstp list [page] - 設定されたポイントを表示する");
 			return true;
 		}
 		// 表示するアイテムの最大数
 		int max = page * 10;
 		// Listよりも最大数が多くなったらListのsizeでやめる
 		if (list.size() < max) {
 			max = list.size();
 		}
 		sender.sendMessage(Utility.replaceSection("&3") + "設定ポイントリスト " + (page * 10 -9) + "～" + max + "件目 / " + list.size() + "件中");
 		for (int i = page * 10 - 10; i < max; i++) {
 			String name = list.get(i).getName();
 			int x = list.get(i).getX();
 			int y = list.get(i).getY();
 			int z = list.get(i).getZ();
 			sender.sendMessage(name + " - " + x + "," + y + "," + z);
 		}
 		return true;
 	}
 
 	/**
 	 * Addコマンド実行
 	 */
 	private boolean execAdd(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		// hereの場合
 		if (args.length == 3 && args[2].equalsIgnoreCase("here")) {
 			// senderがプレイヤーでなければ処理を抜ける
 			if (!(sender instanceof Player)) {
 				sender.sendMessage(Utility.msg("error") + Utility.msg("senderErr"));
 				return true;
 			}
 			// 変数の準備
 			Player player = (Player) sender;
 			String name = args[1];
 			Location location = player.getLocation();
 			int x, y, z;
 			x = location.getBlockX();
 			y = location.getBlockY();
 			z = location.getBlockZ();
 			Point point = new Point(name, x, y, z);
 			config.addPoint(point);
 			sender.sendMessage(Utility.replaceSection("&2") + "次のポイントを追加しました: " + Utility.replaceSection("&r") + name + " - " + x + "," + y + "," + z);
 			return true;
 		}
 		// 座標直接指定の場合
 		if (args.length == 5) {
 			// String型のPoint形式に整形
 			String tempPoint = args[1] + "," + args[2] + "," + args[3] + "," + args[4];
 			Point point = Point.deserialize(tempPoint);
 			if(point == null) {
 				sender.sendMessage(Utility.msg("error") + Utility.replaceSection("&c") + "ポイント追加に失敗しました。\n/rstp add <Name> <x> <y> <z> で再度追加してください");
 				return true;
 			}
 			config.addPoint(point);
 			sender.sendMessage(Utility.replaceSection("&2") + "次のポイントを追加しました: " + Utility.replaceSection("&r") + point.getName() + " - " + point.getX() + "," + point.getY() + "," + point.getZ());
 		} else {
 			// コマンド書式がおかしい場合処理終了
 			sender.sendMessage(Utility.msg("cmdErr") + "\n/rstp add <pointName> here - 現在位置をリストに登録\n/rstp add <pointName> <x> <y> <z> - 指定座標をリストに登録");
 			return true;
 		}
 		return true;
 	}
 
 	/**
 	 * Removeコマンド実行
 	 */
 	private boolean execRemove(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		// 削除と、削除された場合そのポイントを表示
 		Point removedPoint = config.removePoint(args[1]);
 		if (removedPoint != null) {
 			sender.sendMessage(Utility.replaceSection("&2") + "次のポイントを削除しました: " + Utility.replaceSection("&r") + removedPoint.getName() + " - " + removedPoint.getX() + "," + removedPoint.getY() + "," + removedPoint.getZ());
 			return true;
 		} else {
 			sender.sendMessage(Utility.msg("error") + Utility.replaceSection("&c") + "ポイント名 " + args[1] + " は存在しません");
 			return true;
 		}
 	}
 
 	/**
 	 * TPコマンド実行
 	 */
 	private boolean execTp(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		Player tpPlayer = null;
 		// TP対象者が省略されている場合、senderがPlayerであれば、プレイヤー対象にする
 		if (args.length == 2) {
 			if (sender instanceof Player) {
 				tpPlayer = plugin.getServer().getPlayer(sender.getName());
 			} else {
 				sender.sendMessage(Utility.msg("error") + Utility.msg("senderErr"));
 			}
 		} else if (args.length == 3) {
 			// TP対象者が指定されている場合は、そのプレイヤーを対象にする
 			tpPlayer = plugin.getServer().getPlayer(args[2]);
 		} else {
 			// コマンド書式がおかしい場合処理終了
 			sender.sendMessage(Utility.msg("cmdErr") + "\n/rstp tp <PointName> [Player] - 指定ポイントへ(指定者を)テレポート");
 			return true;
 		}
 		// TP対象者がいなかった場合処理終了
 		if (tpPlayer == null) {
 			sender.sendMessage(Utility.msg("error") + Utility.replaceSection("&c") + "テレポート対象者が見つかりませんでした");
 			return true;
 		}
 		// TP先のポイントを検索
 		List<Point> list = config.getPoints();
 		Point tpPoint = null;
 		for (Point point: list) {
 			if (point.getName().equalsIgnoreCase(args[1])) {
 				tpPoint = point;
 			}
 		}
 		// TP先が見つからなかった場合処理終了
 		if (tpPoint == null) {
 			sender.sendMessage(Utility.msg("error") + Utility.replaceSection("&c") + "テレポート先が見つかりませんでした");
 			return true;
 		}
 		// Location組立
 		Location loc = new Location(tpPlayer.getWorld(), tpPoint.getX() + 0.5, tpPoint.getY() + 0.5, tpPoint.getZ() + 0.5);
 		// テレポート実行
 		tpPlayer.sendMessage(Utility.replaceSection("&2") + "Teleporting...");
 		tpPlayer.teleport(loc);
 		// senderが他人をTPした場合、完了を通知する
 		if (!sender.getName().equalsIgnoreCase(tpPlayer.getName())) {
 			sender.sendMessage(tpPlayer.getName() + " を " + tpPoint.getName() + " へテレポートしました");
 		}
 		return true;
 	}
 }
