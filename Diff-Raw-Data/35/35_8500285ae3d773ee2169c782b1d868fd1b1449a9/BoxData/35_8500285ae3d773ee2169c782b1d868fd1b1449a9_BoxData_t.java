 package ml.boxes.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ml.boxes.IBox;
 import ml.boxes.api.ContentBlacklist;
 import ml.boxes.inventory.ContentTip;
 import ml.boxes.inventory.GridContentTip;
 import ml.boxes.item.ItemBox;
 import ml.core.Geometry.rectangle;
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraftforge.common.ForgeDirection;
 
 public class BoxData implements IInventory {
 
 	private ItemStack[] inventory;
 	public String boxName = "";
 	public int boxColor = 5;
 	public final IBox IBoxOwner;
 	
 	public BoxData(IBox owner) {
 		inventory=new ItemStack[this.getSizeInventory()];
 		IBoxOwner = owner;
 	}
 	
 	public BoxData(NBTTagCompound data, IBox owner){
 		IBoxOwner = owner;
 		loadNBT(data);
 	}
 	
 	public void loadNBT(NBTTagCompound nbt){
 		boxName = nbt.getString("name");
 		boxColor = nbt.getInteger("color");
 		
 		NBTTagList nbttaglist = nbt.getTagList("Items");
         inventory = new ItemStack[getSizeInventory()];
         for (int i = 0; i < nbttaglist.tagCount(); i++)
         {
             NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
             int j = nbttagcompound1.getByte("Slot") & 0xff;
             if (j >= 0 && j < inventory.length)
             {
             	inventory[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
             }
         }
 	}
 	
 	public List<Slot> getSlots(){
 		List<Slot> slots = new ArrayList<Slot>();
 		for (int sln = 0; sln < getSizeInventory(); sln++){
 			slots.add(new BoxSlot(this, sln, 8 + (sln%9)*18, 10 + (int)Math.floor(sln/9)*18));
         }
 		return slots;
 	}
 	
 	public NBTTagCompound asNBTTag(){
 		NBTTagCompound tag = new NBTTagCompound();
 		tag.setString("name", boxName);
 		tag.setInteger("color", boxColor);
 		
 		NBTTagList nbttaglist = new NBTTagList();
         for (int i = 0; i < inventory.length; i++)
         {
             if (inventory[i] != null)
             {
                 NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                 nbttagcompound1.setByte("Slot", (byte) i);
                 inventory[i].writeToNBT(nbttagcompound1);
                 nbttaglist.appendTag(nbttagcompound1);
             }
         }
         
         tag.setTag("Items", nbttaglist);
 		
 		return tag;
 	}
 	
 	public List<ItemStack> getContainedItemStacks(){
 		List<ItemStack> iStacks = new ArrayList<ItemStack>();
 		for (int i=0; i<getSizeInventory(); i++){
 			if (inventory[i] != null){
 				iStacks.add(inventory[i]);
 			}
 		}
 		return iStacks;
 	}
 			
 	@Override
 	public int getSizeInventory() {
 		return 18;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int var1) {
 		return inventory[var1];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slotIndex, int var2) {
         if (this.inventory[slotIndex] != null)
         {
             ItemStack var3;
 
             if (this.inventory[slotIndex].stackSize <= var2)
             {
                 var3 = this.inventory[slotIndex];
                 this.inventory[slotIndex] = null;
                 this.onInventoryChanged();
                 return var3;
             }
             else
             {
                 var3 = this.inventory[slotIndex].splitStack(var2);
 
                 if (this.inventory[slotIndex].stackSize == 0)
                 {
                     this.inventory[slotIndex] = null;
                 }
 
                 this.onInventoryChanged();
                 return var3;
             }
         }
         else
         {
             return null;
         }
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int var1) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void setInventorySlotContents(int var1, ItemStack var2) {
 		if (isStackValidForSlot(var1, var2)){
 			inventory[var1] = var2;
 			this.onInventoryChanged();
 		}else{
 			IBoxOwner.ejectItem(var2);
 		}
 	}
 
 	@Override
 	public String getInvName() {
 		return this.boxName;
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 	
     public boolean mergeItemStack(ItemStack is, int lbound, int ubound)
     {
         boolean var5 = false;
         int itI = lbound;
 
         boolean direction = lbound > ubound;
         if (direction)
         {
             itI = ubound - 1;
         }
 
         ItemStack stackOn;
 
         if (is.isStackable())
         {
             while (is.stackSize > 0 && (!direction && itI < ubound || direction && itI >= lbound))
             {
                 stackOn = this.getStackInSlot(itI);
 
                 if (stackOn != null && stackOn.itemID == is.itemID && (!is.getHasSubtypes() || is.getItemDamage() == stackOn.getItemDamage()) && ItemStack.areItemStackTagsEqual(is, stackOn))
                 {
                 	int var9 = stackOn.stackSize + is.stackSize;
 
                     if (var9 <= is.getMaxStackSize())
                     {
                         is.stackSize = 0;
                         stackOn.stackSize = var9;
                         var5 = true;
                     }
                     else if (stackOn.stackSize < is.getMaxStackSize())
                     {
                         is.stackSize -= is.getMaxStackSize() - stackOn.stackSize;
                         stackOn.stackSize = is.getMaxStackSize();
                         var5 = true;
                     }
                 }
 
                 if (direction)
                 {
                     --itI;
                 }
                 else
                 {
                     ++itI;
                 }
             }
         }
 
         if (is.stackSize > 0)
         {
             if (direction)
             {
                 itI = ubound - 1;
             }
             else
             {
                 itI = lbound;
             }
 
             while (!direction && itI < ubound || direction && itI >= lbound)
             {
                 stackOn = this.getStackInSlot(itI);
 
                 if (stackOn == null && this.isStackValidForSlot(itI, is))
                 {
                 	this.setInventorySlotContents(itI, is.copy());
                     is.stackSize = 0;
                     var5 = true;
                     break;
                 }
 
                 if (direction)
                 {
                     --itI;
                 }
                 else
                 {
                     ++itI;
                 }
             }
         }
 
         return var5;
     }
     
 	public int pipeTransferIn(ItemStack stack, boolean doAdd, ForgeDirection from){
 		int orig = stack.stackSize;
 		doAdd = mergeItemStack(stack, 0, getSizeInventory());
 		onInventoryChanged();
 		return orig - stack.stackSize;
 	}
 	
 	public ItemStack[] pipeExtract(boolean doRemove, ForgeDirection from, int maxItemCount){
 		for (int i=0; i<this.getSizeInventory(); i++){
 			ItemStack is = getStackInSlot(i);
 			if (is != null){
 				int take = Math.min(maxItemCount, is.stackSize);
 				ItemStack ret = is.copy();
 				ret.stackSize = take;
 				decrStackSize(i, take);
 				return new ItemStack[]{ret};
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public void onInventoryChanged() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer var1) {
 		return true;
 	}
 
 	@Override
 	public void openChest() {}
 
 	@Override
 	public void closeChest() {}
 	
 	public ContentTip createContentTip(Slot bSlot, rectangle guiBounds){
 		return new GridContentTip(bSlot, guiBounds);
 	}
 	
 	public boolean canOpenContentTip(){
 		return true;
 	}
 	
 	public boolean canOpenContentPreview(){
 		return getContainedItemStacks().size() > 0;
 	}
 	
 	public static class BoxSlot extends Slot {
 		private final BoxData boxData;
 		
 		public BoxSlot(BoxData bd, int par2, int par3, int par4) {
 			super(bd, par2, par3, par4);
 			boxData = bd;
 		}
 		
 		@Override
 		public boolean isItemValid(ItemStack par1ItemStack) {
 			return boxData.isStackValidForSlot(this.getSlotIndex(), par1ItemStack);
 		}
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		return true;
 	}
 
 	@Override
 	public boolean isStackValidForSlot(int i, ItemStack is) {
 		if (is == null)
 			return true;
		if (is.getItem() instanceof ItemBox || ContentBlacklist.ItemBlacklisted(ContentBlacklist.LIST_BOX, is))
 			return false;
 		return true;
 	}
 }
