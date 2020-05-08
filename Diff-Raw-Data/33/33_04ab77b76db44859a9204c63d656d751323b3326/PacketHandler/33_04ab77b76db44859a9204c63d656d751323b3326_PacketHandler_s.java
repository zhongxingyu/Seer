 package mods.CompactStuff;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 
 import mods.CompactStuff.compactor.CompactorRecipes;
 import mods.CompactStuff.compactor.TileEntityCompactor;
 import net.minecraft.block.BlockSign;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.Player;
 
 public class PacketHandler implements IPacketHandler {
 	@Override public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
 		DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.data));
 		if(packet.channel.equals(Metas.CH_COMPCRAFT)) {
 			int[] is = readNIntsFrom(5,in); //xCoord, yCoord, zCoord, slot, button
 			TileEntityCompactor tec = getTileEntity(is, player);
 			if(tec==null) return;
 			if(is[4]==0) {
 				ItemStack slotStack = null;
 				ItemStack held = ((EntityPlayer)player).inventory.getItemStack();
 				if(held!=null) slotStack = held.copy();
 				if(slotStack!=null) slotStack.stackSize = 1;
 				tec.setInventorySlotContents(is[3], slotStack);
 				if(tec.container!=null) tec.container.onCraftMatrixChanged();
 			} else if(is[4]==1) {
 				tec.setInventorySlotContents(is[3], null);
 				if(tec.container!=null) tec.container.onCraftMatrixChanged();
 			}
 		} else if(packet.channel.equals(Metas.CH_COMPOUT)) {
 			int[] is = readNIntsFrom(5,in); //xCoord, yCoord, zCoord, slot, button
 			TileEntityCompactor tec = getTileEntity(is, player);
 			if(tec==null || tec.getStackInSlot(tec.OUTPUT)==null) return;
 			ItemStack out = tec.getStackInSlot(tec.OUTPUT).copy();
 			out.stackSize=1;
 			for(int i=tec.COMFIRST; i<=tec.COMLAST; i++) {
 				ItemStack cur = tec.getStackInSlot(i);
 				if(cur==null) { 
 					tec.setInventorySlotContents(i, out); 
 					CompactorRecipes.disableRecipe(tec.enabled(), out);
 					break; 
 				} else if(cur.itemID==out.itemID && cur.getItemDamage()==out.getItemDamage()) break;
 			}
 		} else if(packet.channel.equals(Metas.CH_COMPMAKE)) {
 			int[] is = readNIntsFrom(5, in); //xCoord, yCoord, zCoord, slot, button
 			TileEntityCompactor tec = getTileEntity(is, player);
 			if(tec==null || tec.getStackInSlot(is[3])==null) return;
 			ItemStack s = tec.getStackInSlot(is[3]);
 			if(GuiScreen.isShiftKeyDown()) {
 				if(CompactorRecipes.defaultEnabled.contains(s)) {
 					CompactorRecipes.enableRecipe(tec.enabled(), s);
 				} else CompactorRecipes.disableRecipe(tec.enabled(), s);
 				tec.setInventorySlotContents(is[3], null);
 				return;
 			} else if(is[4]==0) { //left click
 				if(CompactorRecipes.isEnabled(tec.enabled(), s))
 					synchronized (tec.enabled) {
 						CompactorRecipes.disableRecipe(tec.enabled(), s);	
 					}
 				else {
 					tec.tryToMake(CompactorRecipes.getRecipeWithOutput(s));
 				}
 			} else if(is[4]==1) { //right click
				synchronized(tec.enabled) {
					if(CompactorRecipes.isEnabled(tec.enabled(), s))
						CompactorRecipes.disableRecipe(tec.enabled(), s);
					else CompactorRecipes.enableRecipe(tec.enabled(), s);
				}
 			}
 			((EntityPlayer)player).worldObj.markBlockForUpdate(is[0], is[1], is[2]);
 		}
 	}
 	public TileEntityCompactor getTileEntity(int[] is, Player player) {
 		TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(is[0], is[1], is[2]);
 		if(te==null || !(te instanceof TileEntityCompactor)) {
 			System.out.printf("No Compactor at %d, %d, %d where there should have been%n",is[0],is[1],is[2]);
 			return null;
 		}
 		return (TileEntityCompactor)te;
 	}
 	public int[] readNIntsFrom(int n, DataInputStream in) {
 		int[] is = new int[n];
 		try {
 			for(int i=0; i<is.length; i++) {
 				is[i] = in.readInt();
 			}
 		} catch(Exception e) { e.printStackTrace(); }
 		return is;
 	}
 
 }
