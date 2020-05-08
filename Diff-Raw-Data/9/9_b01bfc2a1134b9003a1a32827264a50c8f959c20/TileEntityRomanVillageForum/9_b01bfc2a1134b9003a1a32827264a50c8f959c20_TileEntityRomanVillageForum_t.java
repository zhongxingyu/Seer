 package toldea.romecraft.tileentity;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.ChatMessageComponent;
 import net.minecraft.util.MathHelper;
 import toldea.romecraft.managers.BlockManager;
 import toldea.romecraft.managers.TickManager;
 import toldea.romecraft.romanvillage.RomanVillage;
 
 public class TileEntityRomanVillageForum extends TileEntity {
 	private boolean isValidMultiblock = false;
 
 	public boolean getIsValid() {
 		return isValidMultiblock;
 	}
 
 	public void invalidateMultiblock() {
 		// Check if we were a valid multiblock. If so, try to get the accompanying village and flag it for annihilation by the RomanVillageCollection.
 		if (isValidMultiblock) {
 			RomanVillage romanVillage = TickManager.romanVillageCollection.getVillageAt(MathHelper.floor_double(xCoord), MathHelper.floor_double(yCoord),
 					MathHelper.floor_double(zCoord));
 			if (romanVillage != null) {
 				romanVillage.flagForAnnihilation();
 			}
 		}
 
 		isValidMultiblock = false;
 
 		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2);
 	}
 
 	public boolean checkIfProperlyFormed() {
 		for (int horiz = -1; horiz <= 1; horiz++) {
 			for (int vert = -1; vert <= 0; vert++) {
 				for (int depth = -1; depth <= 1; depth++) {
 					int x = xCoord + horiz;
 					int y = yCoord + vert;
 					int z = zCoord + depth;
 
 					int blockId = worldObj.getBlockId(x, y, z);
 
 					// Check if we are level with the RomanVillageForum block.
 					if (vert == 0) {
 						if (horiz == 0 && depth == 0) {
 							// We are looking at ourself, so skip.
 							continue;
 						} else {
 							// Check if the rest of the blocks around us are air, return false if not.
 							if (blockId != 0) {
 								return false;
 							} else {
 								continue;
 							}
 						}
 					}
 					// On the row below us make sure everything is made out of some sort of marble block.
 					else if (!isBlockMarble(x, y, z)) {
 						return false;
 					}
 				}
 			}
 		}
 		if (!worldObj.isRemote) {
 			TickManager.romanVillageCollection.createNewVillage(MathHelper.floor_double(xCoord), MathHelper.floor_double(yCoord),
 					MathHelper.floor_double(zCoord));
 		}
 
 		isValidMultiblock = true;
 
 		return true;
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound par1) {
 		super.writeToNBT(par1);
 		par1.setBoolean("isValidMultiblock", isValidMultiblock);
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound par1) {
 		super.readFromNBT(par1);
 		isValidMultiblock = par1.getBoolean("isValidMultiblock");
 	}
	
	public Packet getDescriptionPacket() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
	}
 
 	private boolean isBlockMarble(int x, int y, int z) {
 		int blockId = worldObj.getBlockId(x, y, z);
 		if (blockId == BlockManager.blockMarble.blockID || blockId == BlockManager.blockMarbleMosaic.blockID
 				|| blockId == BlockManager.blockMarbleMosaicHalfSlab.blockID || blockId == BlockManager.blockMarbleMosaicStairs.blockID) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public void printVillageData(EntityPlayer player) {
 		if (!isValidMultiblock) {
 			player.sendChatToPlayer(ChatMessageComponent.createFromText("Roman Village forum is not formed properly!"));
 		}
 
 		RomanVillage romanVillage = getRomanVillage();
 
 		if (romanVillage == null) {
 			player.sendChatToPlayer(ChatMessageComponent.createFromText("Roman Village forum is null!"));
 		} else {
 			player.sendChatToPlayer(ChatMessageComponent.createFromText("-- Roman Village Data --"));
 			player.sendChatToPlayer(ChatMessageComponent.createFromText(new String("Village Forum: (" + romanVillage.getVillageForumLocation().posX + ", "
 					+ romanVillage.getVillageForumLocation().posY + ", " + romanVillage.getVillageForumLocation().posZ + ")")));
 			player.sendChatToPlayer(ChatMessageComponent.createFromText(new String("Village Center: (" + romanVillage.getCenter().posX + ", "
 					+ romanVillage.getCenter().posY + ", " + romanVillage.getCenter().posZ + ")")));
 			player.sendChatToPlayer(ChatMessageComponent.createFromText(new String("Village Radius: " + romanVillage.getVillageRadius())));
 			player.sendChatToPlayer(ChatMessageComponent.createFromText(new String("Number of Doors: " + romanVillage.getNumVillageDoors())));
 			player.sendChatToPlayer(ChatMessageComponent.createFromText(new String("Number of Bloomeries: " + romanVillage.getNumBloomeries())));
 			player.sendChatToPlayer(ChatMessageComponent.createFromText(new String("Number of Plebs: " + romanVillage.getNumPlebs() + " / "
 					+ romanVillage.getMaxNumberOfPlebs())));
 
 		}
 	}
 
 	public RomanVillage getRomanVillage() {
 		return TickManager.romanVillageCollection.getVillageAt(MathHelper.floor_double(xCoord), MathHelper.floor_double(yCoord),
 				MathHelper.floor_double(zCoord));
 	}
 }
