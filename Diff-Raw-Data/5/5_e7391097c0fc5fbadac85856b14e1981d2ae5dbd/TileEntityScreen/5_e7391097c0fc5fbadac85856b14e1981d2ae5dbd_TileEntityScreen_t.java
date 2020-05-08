 package spacecraft.core.block.tile;
 
 import net.minecraft.tileentity.TileEntity;
 
 public class TileEntityScreen extends TileEntity {
 
	public void updateEntity() {
		if (worldObj.isRemote) {
			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		}
	}
 }
