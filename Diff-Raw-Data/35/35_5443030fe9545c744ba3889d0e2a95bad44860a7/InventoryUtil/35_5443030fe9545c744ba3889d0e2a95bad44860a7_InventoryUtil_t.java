 package syam.Honeychest.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
import syam.Honeychest.Actions;

 public class InventoryUtil {
 
 	/**
 	 * ItemStackの配列 を ハッシュマップ<アイテムID文字列(String), 個数(Integer)> に変換します
 	 * @param inventory 変換するItemStack配列
 	 * @return 変換後の HashMap<String, Integer>
 	 */
 	public static HashMap<String, Integer> compressInventory(ItemStack[] inventory) {
 		HashMap<String, Integer> items = new HashMap<String, Integer>();
 		for (ItemStack item : inventory){
 			if (item == null) continue;
 			// アイテムID文字列取得
 			String itemString = ItemUtil.getItemString(item);
 			// ハッシュマップに追加 既に存在すればアイテム個数を加えて上書き
 			if (items.containsKey(itemString)) {
 				items.put(itemString, items.get(itemString) + item.getAmount());
 			}else{
 				items.put(itemString, item.getAmount());
 			}
 		}
 		// アイテムのハッシュマップを返す
 		return items;
 	}
 
 	/**
 	 * ハッシュマップ<String, Integer> を元のItemStack配列に戻す
 	 * @param comp 変換された HashMap<String, Integer>
 	 * @return ItemStack配列
 	 */
 	public static ItemStack[] uncompressInventory(HashMap<String, Integer> comp) {
 		List<ItemStack> inv = new ArrayList<ItemStack>();
 		// ハッシュマップの終わりまでループ
 		for (Entry<String, Integer> item : comp.entrySet()) {
 			int i = item.getValue(); // アイテム個数
 			while (i > 0){
 				if (i < 64){
 					// 64個より少なければそのままリストに追加
 					inv.add(ItemUtil.itemStringToStack(item.getKey(), i));
 				}else{
 					// 64個以上なら64個 = 1スタック分リストに追加
 					inv.add(ItemUtil.itemStringToStack(item.getKey(), 64));
 				}
 				// 1スタック分(64個) を個数から引いて0以下になるまで繰り返す
 				i = i - 64;
 			}
 		}
 		// リストを新しいItmStack配列に変換して返す
 		return inv.toArray(new ItemStack[0]);
 	}
 
 	/**
 	 * 2つのハッシュマップ変換済みインベントリデータを比較して、違いのあるアイテムを整形して返す
 	 * @param before インベントリ変更前のハッシュマップ HashMap<String, Integer>
 	 * @param after インベントリ変更後のハッシュマップ HashMap<String, Integer>
 	 * @return 違いのあるアイテム文字列 ＠以前は加わったもの 以降は減ったもの 例: item:data,amount&item:data,amount@item:data,amount&item:data
 	 */
 	public static String createDifferenceString(HashMap<String, Integer> before, HashMap<String, Integer> after) {
 		List<String> add = new ArrayList<String>();
 		List<String> sub = new ArrayList<String>();
 
 		for (Entry<String, Integer> item : before.entrySet()){
 			if (!after.containsKey(item.getKey())){
 				// 変更後にアイテムが無かった場合
 				sub.add(item.getKey() + "," + item.getValue());
 			}else if(item.getValue() > after.get(item.getKey())){
 				// 変更後にアイテムが減っていた場合
 				sub.add(item.getKey() + "," + (item.getValue() - after.get(item.getKey())));
 			}else if(item.getValue() < after.get(item.getKey())){
 				// 変更後にアイテムが増えていた場合
 				add.add(item.getKey() + "," + (after.get(item.getKey()) - item.getValue()));
 			}
 		}
 		for (Entry<String, Integer> item : after.entrySet()) {
 			if (!before.containsKey(item.getKey())){
 				// 変更前に無いアイテムが変更後にあった場合
 				add.add(item.getKey() + "," + item.getValue());
 			}
 		}

 		// 文字列を結合して返す
 		return Util.join(add, "&") + "@" + Util.join(sub, "&");
 	}
 
 	/**
 	 * 違いのあるアイテムの文字列を受け取り、増加と減少データの2つのハッシュマップをリストに入れて返す
 	 * @param diff 処理済みの整形済みアイテム比較文字列
 	 * @return 増加分、減少分、2つのハッシュマップが入ったリストを返す 1つめの要素が増減分、2つめが減少分
 	 */
 	public static List<HashMap<String, Integer>> interpretDifferenceString(String diff) {
 		List<HashMap<String, Integer>> ops = new ArrayList<HashMap<String, Integer>>();
 
 		// ＠で区切られた増えた部分と減った部分を分けて処理する
 		for (String changes : diff.split("@")){
 			HashMap<String, Integer> op = new HashMap<String, Integer>();
 			// 一つの アイテム:個数 データごとに処理する
 			for (String change : changes.split("&")){
 				if (change.length() == 0) continue;
 				// アイテム部分と個数部分を分ける
 				String[] item = change.split(",");
 				op.put(item[0], Integer.parseInt(item[1]));
 			}
 			ops.add(op);
 		}
 		// 減少分がなく、opsリストサイズが1なら空のハッシュマップを追加してサイズを2に揃える
 		if (ops.size() == 1) {
 			ops.add(new HashMap<String, Integer>());
 		}
 		return ops;
 	}
 
 	/**
 	 * 人間にとって読みやすく整形した、チェスト増減データを作る
 	 * @param ops interpretDifferenceString関数で作った、増加分と減少分のハッシュマップが入ったリスト
 	 * @return 整形済み文字列
 	 */
 	public static String createChangeString(List<HashMap<String, Integer>> ops) {
 		if (ops.size() == 0) return "";
 		String changeString = "";
 
 		// 増加分をリストに追加
 		List<String> add = new ArrayList<String>();
 		for (Entry<String, Integer> item : ops.get(0).entrySet()){
 			add.add(item.getValue() + "x " + ItemUtil.getItemStringName(item.getKey()));
 		}
 		// 減少分をリストに追加
 		List<String> sub = new ArrayList<String>();
 		for (Entry<String, Integer> item : ops.get(1).entrySet()){
 			sub.add(item.getValue() + "x " + ItemUtil.getItemStringName(item.getKey()));
 		}
 
 		// それぞれのリストを組み立て
 		if (add.size() > 0) changeString += "&a+(" + Util.join(add, ", ") + ")";
 		if (sub.size() > 0) changeString += "&4-(" + Util.join(sub, ", ") + ")";
 
 		// 整形済み文字列を返す
 		return changeString;
 	}
 
 	/**
 	 * インベントリインターフェースを持つブロックから完全なインベントリ情報を得るための関数
 	 * @param container チェックするブロック
 	 * @return 2つのインベントリの内容を合わせた ItemStack[]
 	 * @author N3X15
 	 */
 	public static ItemStack[] getContainerContents(InventoryHolder container) {
 		// チェスト以外は問題ないのでそのまま返す
 		if (!(container instanceof Chest)) return container.getInventory().getContents();
 
 		Chest chest = (Chest) container;
 		Chest second = null;
 
 		// 隣にチェストがないか4面を探す
 		if (chest.getBlock().getRelative(BlockFace.NORTH).getType() == Material.CHEST)
 			second = (Chest) chest.getBlock().getRelative(BlockFace.NORTH).getState();
 		else if (chest.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.CHEST)
             second = (Chest) chest.getBlock().getRelative(BlockFace.SOUTH).getState();
         else if (chest.getBlock().getRelative(BlockFace.EAST).getType() == Material.CHEST)
             second = (Chest) chest.getBlock().getRelative(BlockFace.EAST).getState();
         else if (chest.getBlock().getRelative(BlockFace.WEST).getType() == Material.CHEST)
             second = (Chest) chest.getBlock().getRelative(BlockFace.WEST).getState();
 
 		// 周辺になければ、この一つだけを返す
 		if (second == null){
 			return chest.getInventory().getContents();
 		}else{
 			// I think it would be good, to consistently return same chest
             // contents, regardless of what
             // block was clicked on. That means, we must determine, which part
             // of chest comes first, and which second.
             // I choose the one, which has lower X coordinate. If they are same,
             // than it's the one with lower Z coordinate.
             // I believe it can be easily checked with this trick:
             ItemStack[] result = new ItemStack[54];
             ItemStack[] firstHalf;
             ItemStack[] secondHalf;
 
             if ((chest.getX() + chest.getZ()) < (second.getX() + second.getZ())) {
                 firstHalf = chest.getInventory().getContents();
                 secondHalf = second.getInventory().getContents();
             } else {
                 firstHalf = second.getInventory().getContents();
                 secondHalf = chest.getInventory().getContents();
             }
 
             // 合わせる
             for (int i = 0; i < 27; i++) {
                 result[i] = firstHalf[i];
                 result[i + 27] = secondHalf[i];
             }
 
             return result;
 		}
 	}
 }
