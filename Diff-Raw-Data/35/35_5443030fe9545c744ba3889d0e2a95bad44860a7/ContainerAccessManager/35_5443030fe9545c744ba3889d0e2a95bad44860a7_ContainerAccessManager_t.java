 package syam.Honeychest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.InventoryHolder;
 
 import syam.Honeychest.util.InventoryUtil;
 
 /**
  * コンテナへのアクセスを管理するためのメソッドが含まれています
  */
 public class ContainerAccessManager {
 	public final static Logger log = Honeychest.log;
 	private static final String logPrefix = Honeychest.logPrefix;
 	private static final String msgPrefix = Honeychest.msgPerfix;
 
 	// コンテナへのアクセスリスト コンテナインベントリを開いているプレイヤーがここに入る
 	private final List<ContainerAccess> accessList = new ArrayList<ContainerAccess>();
 
 	/**
 	 * 開いているプレイヤーインベントリとコンテナインベントリの現在の内容を格納
 	 * @param player チェックするプレイヤー
 	 * @param block インベントリインターフェースを持つブロック
 	 */
 	public void checkInventoryOpen(Player player, Block block){
 		// インベントリインターフェースを持たないブロックは返す
 		if (!(block.getState() instanceof InventoryHolder)){
 			return;
 		}
 		InventoryHolder container = (InventoryHolder) block.getState();
		accessList.add(new ContainerAccess(container, player, InventoryUtil.compressInventory(InventoryUtil.getContainerContents(container)), block.getLocation()));
 	}
 
 	/**
 	 * プレイヤーがコンテナインベントリが開いていたかチェックして表示
 	 * @param player チェックするプレイヤー
 	 */
 	public void checkInventoryClose(Player player) {
 		// アクセスリストを取得
 		ContainerAccess access = null;
 		for (ContainerAccess acc : accessList){
 			if (acc.player == player){
 				access = acc;
 			}
 		}
 
 		// アクセスリスト(インベントリを開いた記録)がなければ返す
 		if (access == null) return;
 
 		// 今のインベントリを取得して、アイテムの増減分をプレイヤーに送信
 		HashMap<String, Integer> after = InventoryUtil.compressInventory(InventoryUtil.getContainerContents(access.container));
 		String diff = InventoryUtil.createDifferenceString(access.beforeInv, after);
 		String readble = InventoryUtil.createChangeString(InventoryUtil.interpretDifferenceString(diff));
 		if (diff.length() > 1){
 			// メッセージを送る
 			Actions.message(null, player, msgPrefix + readble);
 		}
 
 		// アクセスリストから削除
 		accessList.remove(access);
 	}
 
 
 	/**
 	 * インベントリインターフェースを持つブロックへのアクセスを表すクラス
 	 * @author syam
 	 */
 	public class ContainerAccess {
 		public InventoryHolder container;
 		public Player player;
 		public HashMap<String, Integer> beforeInv;
 		public Location loc;
 
 		public ContainerAccess(InventoryHolder container, Player player, HashMap<String, Integer> beforeInv, Location loc){
 			this.container = container;
 			this.player = player;
 			this.beforeInv = beforeInv;
 			this.loc = loc;
 		}
 	}
 }
