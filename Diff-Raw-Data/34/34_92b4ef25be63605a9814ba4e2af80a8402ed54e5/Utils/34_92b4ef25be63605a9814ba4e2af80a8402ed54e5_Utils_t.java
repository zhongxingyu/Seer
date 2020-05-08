package ardenexal.nethercraft.utils;

import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;

public class Utils {
	public static void preDestroyBlock(World world, int i, int j, int k) {
TileEntity tile = world.getBlockTileEntity(i, j, k);

if (tile instanceof IInventory && !CoreProxy.proxy.isRenderWorld(world))
if (!(tile instanceof IDropControlInventory) || ((IDropControlInventory) tile).doDrop()) {
dropItems(world, (IInventory) tile, i, j, k);
}


}
}
