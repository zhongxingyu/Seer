 package syam.Honeychest;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 import syam.Honeychest.config.MessageManager;
 
 public class HoneychestPlayerListener implements Listener {
 	public final static Logger log = Honeychest.log;
 	private static final String logPrefix = Honeychest.logPrefix;
 	private static final String msgPrefix = Honeychest.msgPrefix;
 
 	private final Honeychest plugin;
 
 	public HoneychestPlayerListener(Honeychest plugin){
 		this.plugin = plugin;
 	}
 
 	/* 登録するイベントはここから下に */
 
 	// プレイヤーがブロックをクリックした
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event){
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 
 		// インベントリが閉じられたかチェック
 		Honeychest.containerManager.checkInventoryClose(player);
 
 		if (block != null) {
 			Location loc = block.getLocation();
 
 			switch (block.getType()){
 				case FURNACE:
 				case DISPENSER:
 				case CHEST:
 					// ハニーチェストのインベントリを開いた
 					if (event.getAction() == Action.RIGHT_CLICK_BLOCK && HoneyData.getHc(block.getLocation()) != null) {
 						if (Honeychest.containerManager.isAccessing(block.getLocation())){
 							// このハニーチェストは他人に開かれている
 
 							event.setCancelled(true);
 							event.setUseInteractedBlock(Result.DENY);
 							event.setUseItemInHand(Result.DENY);
 							Actions.message(null, player, MessageManager.getString("PlayerListener.openedTrap"));
 							return;
 						}else{
 							// 開くことが出来る
 							Honeychest.containerManager.checkInventoryOpen(player, block);
 						}
 					}
 					break;
 			}
 		}
 	}
 
 	/* ハニーチェスト管理モードの処理 */
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void createHoneyChest(PlayerInteractEvent event){
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		if (block != null) {
 			Location loc = block.getLocation();
 
 			switch (block.getType()){
 				case FURNACE:
 				case DISPENSER:
 				case CHEST:
 					if (event.getAction() == Action.RIGHT_CLICK_BLOCK && HoneyData.isCreator(player)) {
 						if (player.getItemInHand().getTypeId() == plugin.getHCConfig().getToolId() && player.hasPermission("honeychest.manage")){
 							/* 管理モードで特定のアイテムを持ったままコンテナブロックを右クリックした */
 
 							// チェストならラージチェストか判定
 							Block second = null;
 							if (block.getType() == Material.CHEST){
 								// 走査開始
 								if (block.getRelative(BlockFace.NORTH).getType() == Material.CHEST)
 						            second = block.getRelative(BlockFace.NORTH);
 						        else if (block.getRelative(BlockFace.SOUTH).getType() == Material.CHEST)
 						            second = block.getRelative(BlockFace.SOUTH);
 						        else if (block.getRelative(BlockFace.EAST).getType() == Material.CHEST)
 						            second = block.getRelative(BlockFace.EAST);
 						        else if (block.getRelative(BlockFace.WEST).getType() == Material.CHEST)
 						            second = block.getRelative(BlockFace.WEST);
 							}
 
 							// 既にハニーチェストになっているか判定
 							if (HoneyData.getHc(loc) == null) {
 								HoneyData.setHc(loc, "*"); // 登録
 
 								if (second != null)
 									HoneyData.setHc(second.getLocation(), "*"); // ラージチェスト登録
 
 								Actions.message(null, player, MessageManager.getString("PlayerListener.createTrap"));
 							}else{
 								HoneyData.removeHc(loc); // 削除
 								if (second != null)
									HoneyData.setHc(second.getLocation(), "*"); // ラージチェスト削除
 
 								Actions.message(null, player, MessageManager.getString("PlayerListener.removeTrap"));
 							}
 							event.setCancelled(true);
 						}
 					}
 					break;
 			}
 		}
 	}
 
 	/* 他のイベントでコンテナインベントリが閉じられていないかチェックする */
 
 	// チャット
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerChat(PlayerChatEvent event){
 		// インベントリが閉じられたかチェック
 		Honeychest.containerManager.checkInventoryClose(event.getPlayer());
 	}
 
 	// コマンド
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
 		// インベントリが閉じられたかチェック
 		Honeychest.containerManager.checkInventoryClose(event.getPlayer());
 	}
 
 	// テレポート
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
 		// インベントリが閉じられたかチェック
 		Honeychest.containerManager.checkInventoryClose(event.getPlayer());
 	}
 
 	// 切断
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		// アクセスリストから削除
 		Honeychest.containerManager.checkInventoryClose(event.getPlayer());
 		//Honeychest.containerManager.removeAccessList(event.getPlayer());
 	}
 }
