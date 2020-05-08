 package ml.boxes.inventory;
 
 import java.util.List;
 
 import ml.boxes.Boxes;
 import ml.boxes.IBox;
 import ml.boxes.api.ContentBlacklist;
 import ml.boxes.data.BoxData;
 import ml.boxes.data.ItemIBox;
 import ml.boxes.item.ItemBox;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 
 public class ContainerBox extends Container {
 
 	public final IBox box;
 	public final EntityPlayer player;
 	
 	public ContainerBox(IBox box, EntityPlayer pl) {
 		this.box = box;
 		this.player = pl;
 		box.boxOpen();
 		
 		int leftCol = 9;
 		int ySize = 152;
 		for (int slt=9; slt < pl.inventory.mainInventory.length; slt++){
 			int row = (int)Math.floor(slt/9) -1;
 			addSlotToContainer(new Slot(pl.inventory, slt, 9 + (slt%9)*18, ySize - 83 + row*18));
 		}
 
         for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++)
         {
             addSlotToContainer(new Slot(pl.inventory, hotbarSlot, leftCol + hotbarSlot * 18, ySize - 25));
         }
         
         List<Slot> boxSlots = box.getBoxData().getSlots();
         for (Slot slt : boxSlots){
         	addSlotToContainer(slt);
         }
 	}
 	
 	@Override
 	public boolean canInteractWith(EntityPlayer var1) {
 		return box.getBoxData().isUseableByPlayer(var1) && (!(box instanceof ItemIBox) || ((ItemIBox)box).stack == var1.getCurrentEquippedItem());
 	}
 	
 	@Override
 	public ItemStack slotClick(int slotNum, int mouseBtn, int action,
 			EntityPlayer par4EntityPlayer) {
		if (box instanceof ItemIBox && slotNum>=0 && getSlot(slotNum).getSlotIndex() == par4EntityPlayer.inventory.currentItem) { //((ItemIBox)box).stack == getSlot(slotNum).getStack()){
 			//par4EntityPlayer.closeScreen();
 			return null;
 		}
 		ItemStack ret = super.slotClick(slotNum, mouseBtn, action, par4EntityPlayer);
 		save(par4EntityPlayer);
 		return ret;
 	}
 
 	@Override
     public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
     {
         ItemStack var3 = null;
         Slot var4 = (Slot)this.inventorySlots.get(par2);
 
         if (var4 != null && var4.getHasStack())
         {
             ItemStack var5 = var4.getStack();
             var3 = var5.copy();
             if (par2 < 36)
             {
                 if (!this.mergeItemStack(var5, 36, this.inventorySlots.size(), false))
                 {
                     return null;
                 }
             }
             else if (!this.mergeItemStack(var5, 0, 36, true))
             {
                 return null;
             }
 
             if (var5.stackSize == 0)
             {
                 var4.putStack((ItemStack)null);
             }
             else
             {
                 var4.onSlotChanged();
             }
         }
 
         return var3;
     }
 	
 	@Override
 	public void onCraftGuiClosed(EntityPlayer par1EntityPlayer) {
 		super.onCraftGuiClosed(par1EntityPlayer);
 		save(par1EntityPlayer);
 		box.boxClose();
 	}
 	
 	private void save(EntityPlayer pl){
 		if (!pl.worldObj.isRemote){
 			box.saveData();
 		}
 	}
 }
