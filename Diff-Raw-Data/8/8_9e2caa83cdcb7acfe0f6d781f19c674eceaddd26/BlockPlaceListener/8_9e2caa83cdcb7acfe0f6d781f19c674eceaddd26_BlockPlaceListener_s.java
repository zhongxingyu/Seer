 package com.github.owatakun.mahime;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 

 public class BlockPlaceListener implements Listener {
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		// EditMode以外なら抜ける
 		if (!RandomChest.instance.isRcEditMode()) {
 			return;
 		}
 		// 置かれたブロックと置いたプレイヤーを取得
 		Block block = event.getBlockPlaced();
 		Player player = event.getPlayer();
 		// Edit指示ブロックでなければ抜ける
 		if (block.getTypeId() != RandomChest.instance.getRcEditBlock()) {
 			return;
 		}
 		if (RandomChest.instance.addRCPoint(player, block.getLocation())) {
 			return;
 		}
 	}
 }
