 package syam.Honeychest;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import syam.Honeychest.config.MessageManager;
 
 public class HoneychestBlockListener implements Listener {
 	public final static Logger log = Honeychest.log;
 	private static final String logPrefix = Honeychest.logPrefix;
 	private static final String msgPrefix = Honeychest.msgPrefix;
 
 	private final Honeychest plugin;
 
 	public HoneychestBlockListener(Honeychest plugin){
 		this.plugin = plugin;
 	}
 
 	/* 登録するイベントはここから下に */
 
 	// プレイヤーがブロックをクリックした
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event){
 		Block block = event.getBlock();
 		Player player = event.getPlayer();
 
 		if (block != null) {
 			Location loc = block.getLocation();
 
 			switch (block.getType()){
 				case FURNACE:
 				case DISPENSER:
 				case CHEST:
 					// ハニーチェストか判定 ハニーチェストならイベントをキャンセルする
 					String str = HoneyData.getHc(loc);
 					if (str != null){
 						// ハニーチェスト イベントキャンセル
 						event.setCancelled(true);
 						Actions.message(null, player, MessageManager.getString("BlockListener.notBreakTrap"));
 					}
 					break;
 			}
 		}
 	}
 
 	// プレイヤーがブロックを設置した
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onBlockPlace(BlockPlaceEvent event){
 		Block block = event.getBlock();
 		Player player = event.getPlayer();
 
 		// チェスト設置時は横にハニーチェストが無いかチェック
 		if (block.getType() == Material.CHEST){
 			boolean flag = false;
 			if ((block.getRelative(BlockFace.NORTH).getType() == Material.CHEST&& HoneyData.getHc(block.getRelative(BlockFace.NORTH).getLocation()) != null) ||
 				(block.getRelative(BlockFace.SOUTH).getType() == Material.CHEST && HoneyData.getHc(block.getRelative(BlockFace.SOUTH).getLocation()) != null) ||
 				(block.getRelative(BlockFace.EAST).getType() == Material.CHEST && HoneyData.getHc(block.getRelative(BlockFace.EAST).getLocation()) != null) ||
 				(block.getRelative(BlockFace.WEST).getType() == Material.CHEST && HoneyData.getHc(block.getRelative(BlockFace.WEST).getLocation()) != null)){
 				// イベントキャンセル
 				event.setCancelled(true);
 			}
 		}
 	}
 }
