 package ml.boxes.network.packets;
 
 import java.io.IOException;
 
 import ml.boxes.tile.TileEntityCrate;
 import ml.core.network.MLPacket;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import cpw.mods.fml.common.network.Player;
 
 public class PacketDescribeCrate extends MLPacket {
 
 	public Integer x;
 	public Integer y;
 	public Integer z;
 	public boolean hasStack;
 	public ItemStack is;
 	public ForgeDirection facing;
 	public int itemCnt;
 	
 	public PacketDescribeCrate(TileEntityCrate tec) {
 		super(null);
 
 		x = tec.xCoord;
 		y = tec.yCoord;
 		z = tec.zCoord;
 		hasStack = tec.getStackInSlot(0) != null;
 		is = hasStack ? tec.getStackInSlot(0).copy() : new ItemStack(0, 0, 0);
 		is.stackSize = 1;
 		facing = tec.facing;
 		itemCnt = tec.getTotalItems();
 		
 		writeInt(x);
 		writeInt(y);
 		writeInt(z);
 		writeBoolean(hasStack);
 		writeNBTTagCompound(is.writeToNBT(new NBTTagCompound()));
 		writeInt(facing.ordinal());
 		writeInt(itemCnt);
 	}
 	
 	public PacketDescribeCrate(Player pl, ByteArrayDataInput data) {
 		super(pl, data);
 
 		try {
 			x = dataIn.readInt();
 			y = dataIn.readInt();
 			z = dataIn.readInt();
 			hasStack = dataIn.readBoolean();
 			is = ItemStack.loadItemStackFromNBT(readNBTTagCompound());
 			facing = ForgeDirection.getOrientation(dataIn.readInt());
 			itemCnt = dataIn.readInt();
 		} catch (IOException e){
 			
 		}
 	}
 
 	@Override
 	public void handleClientSide() throws IOException {
 		EntityPlayer asEntPl = (EntityPlayer)player;
 		TileEntity te = asEntPl.worldObj.getBlockTileEntity(x, y, z);
 		
 		if (te instanceof TileEntityCrate){
 			TileEntityCrate tec = (TileEntityCrate)te;
 			tec.cItem = hasStack ? is : null;
 			tec.facing = facing;
 			tec.itemCount = itemCnt;
 			
 			tec.updateClientDetails();
 		}
 	}
 
 	@Override
 	public void handleServerSide() throws IOException {}
 
 }
