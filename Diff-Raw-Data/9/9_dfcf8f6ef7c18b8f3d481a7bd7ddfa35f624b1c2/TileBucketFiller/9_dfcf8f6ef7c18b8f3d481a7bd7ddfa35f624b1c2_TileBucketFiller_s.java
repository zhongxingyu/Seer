 package net.minecraft.src.buildcraft.pigalot;
 
 import java.util.Arrays;
 import net.minecraft.src.Block;
 import net.minecraft.src.BuildCraftEnergy;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.ICrafting;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.NBTTagList;
 import net.minecraft.src.buildcraft.api.API;
 import net.minecraft.src.buildcraft.api.APIProxy;
 import net.minecraft.src.buildcraft.api.ILiquidContainer;
 import net.minecraft.src.buildcraft.api.IPowerReceptor;
 import net.minecraft.src.buildcraft.api.Orientations;
 import net.minecraft.src.buildcraft.api.PowerFramework;
 import net.minecraft.src.buildcraft.api.PowerProvider;
 import net.minecraft.src.buildcraft.api.SafeTimeTracker;
 import net.minecraft.src.buildcraft.api.TileNetworkData;
 import net.minecraft.src.buildcraft.core.IMachine;
 import net.minecraft.src.buildcraft.factory.TileMachine;
 import net.minecraft.src.forge.ISidedInventory;
 import net.minecraft.src.mod_jBuildCraft_BucketFiller;
 
 public class TileBucketFiller extends TileMachine
     implements IMachine, IPowerReceptor, ILiquidContainer, IInventory, ISidedInventory {
     
     protected PowerProvider powerProvider;
     SafeTimeTracker updateNetworkTime = new SafeTimeTracker();
     private ItemStack[] BucketFillerStacks;
     private boolean _isActive;
     public static int LIQUID_PER_SLOT = API.BUCKET_VOLUME * 10;
     @TileNetworkData public int CookTime;
     public int RequiredCookTime;
     boolean hasPower;
     int NewBucket = 0;
     int BucketQuantity = 0;
     int retflag;
     boolean debug = false;
     
     public static int MAX_ENERGY = mod_jBuildCraft_BucketFiller.fillBucketEnergy * 4;
 
     public static class Slot {
         @TileNetworkData public int liquidId = 0;
         @TileNetworkData public int quantity = 0;
 
         public int fill(Orientations from, int amount, int id, boolean doFill) {
             if (quantity != 0 && liquidId != id) {
                 return 0;
             } else if (quantity + amount <= LIQUID_PER_SLOT) {
                 if (doFill) {
                     quantity = quantity + amount;
                 }
 
                 liquidId = id;
                 return amount;
             } else {
                 int used = LIQUID_PER_SLOT - quantity;
 
                 if (doFill) {
                     quantity = LIQUID_PER_SLOT;
                 }
 
                 liquidId = id;
                 return used;				
             }			
         }				
 
         public void writeFromNBT(NBTTagCompound nbttagcompound) {
             nbttagcompound.setInteger("liquidId", liquidId);
             nbttagcompound.setInteger("quantity", quantity);
         }
 
         public void readFromNBT(NBTTagCompound nbttagcompound) {
             liquidId = nbttagcompound.getInteger("liquidId");
             quantity = nbttagcompound.getInteger("quantity");			
         }
     }
 
     @TileNetworkData public TileBucketFiller.Slot slot = new TileBucketFiller.Slot();
 
     @Override
     public int getStartInventorySide(int side) {
         if (side == 0 || side == 1) {
             return 0;
         }
         return 1;
     }
 
     @Override
     public int getSizeInventorySide(int side) {
         return 1;
     }
 
     public TileBucketFiller() {
         powerProvider = PowerFramework.currentFramework.createPowerProvider();
         powerProvider.configure(20, 1, MAX_ENERGY, 10, MAX_ENERGY);
         BucketFillerStacks = new ItemStack[2];
         CookTime = 0;
         RequiredCookTime = 200;
         hasPower = false;
     }
     
     public int getCookProgressScaled(int i) {
         if(RequiredCookTime == 0 || i == 0 || CookTime == 0) {
             return 0;
         }
         return (CookTime * i) / RequiredCookTime;
     }
 
     @Override
     public boolean isActive() {
         return _isActive;
     }
 
     @Override
     public boolean manageLiquids() {
         return true;
     }
 
     @Override
     public boolean manageSolids() {
         return true;
     }
 
     @Override
     public void setPowerProvider(PowerProvider provider) {
         powerProvider = provider;
     }
 
     @Override
     public PowerProvider getPowerProvider() {
         return powerProvider;
     }
 
     @Override
     public void doWork() {
     }
 
     @Override
     public void updateEntity() {
         if (!APIProxy.isClient(worldObj)) {
             if ((APIProxy.isServerSide()) && (updateNetworkTime.markTimeIfDelay(worldObj, 20L))) {
                 sendNetworkUpdate();
             }
         }
         _isActive = false;
         Boolean flag = false;
         if(mod_jBuildCraft_BucketFiller.ic2IsInstalled && BucketFillerStacks[1] != null){
             Integer[] testcase = {mod_jBuildCraft_BucketFiller.ic2CellLava.shiftedIndex,mod_jBuildCraft_BucketFiller.ic2CellWater.shiftedIndex,mod_jBuildCraft_BucketFiller.ic2CellCoal.shiftedIndex,mod_jBuildCraft_BucketFiller.ic2CellCoalRef.shiftedIndex};
             if(BucketFillerStacks[1] != null){
                 flag = Arrays.asList(testcase).contains(BucketFillerStacks[1].itemID);
             } else {
                 flag = false;
             }
         }else {
             flag = false;
         }
         
         if ((BucketFillerStacks[1] != null && !flag)) {
             if(retflag != 1 && debug == true) {
                 retflag = 1;
                 System.out.println("return 1");
                 System.out.println("flag = ");
                 System.out.print(flag);
                 System.out.println("");
                 System.out.println(" Stacks[1] = ");
                 System.out.print(BucketFillerStacks[1] != null);
                 System.out.println("");
             }
             CookTime = 0;
             return;
         }
         if(BucketFillerStacks[0] == null){
             if(retflag != 2 && debug == true) {
                 retflag = 2;
                 System.out.println("return 2");
             }
             CookTime = 0;
             return;
         }
         
         if(flag && !(BucketFillerStacks[1].stackSize < BucketFillerStacks[1].getMaxStackSize())) {
            if(retflag != 3 && debug == true) {
                 retflag = 3;
                 System.out.println("return 3");
             }
             CookTime = 0;
             return;
         }
         
         int liquidId = API.getLiquidForBucket(BucketFillerStacks[0].itemID);
         
         if (liquidId == 0 && BucketFillerStacks[0].itemID != Item.bucketEmpty.shiftedIndex && 
                 (mod_jBuildCraft_BucketFiller.ic2IsInstalled == true && (BucketFillerStacks[0].itemID != mod_jBuildCraft_BucketFiller.ic2CellEmpty.shiftedIndex)) ) {
             if(retflag != 4 && debug == true) {
                 retflag = 4;
                 System.out.println("return 4");
             }
             CookTime = 0;
             return;
         }
         
         if (liquidId != 0 && (getFreeCapacity() == 0 || (slot.liquidId != liquidId && (slot.liquidId != 0 && slot.quantity !=0)))) {
             if(retflag != 5 && debug == true) {
                 retflag = 5;
                 System.out.println("return 5");
             }
             CookTime = 0;
             return;
         }
         
         if (liquidId == 0 && slot.quantity < API.BUCKET_VOLUME) {
             if(retflag != 6 && debug == true) {
                 retflag = 6;
                 System.out.println("return 6");
             }
             CookTime = 0;
             return;
         }
         
         RequiredCookTime = 100;
         
         if(liquidId == 0){
             if(BucketFillerStacks[0].itemID == Item.bucketEmpty.shiftedIndex) {
                 RequiredCookTime = 200;
             } else if(mod_jBuildCraft_BucketFiller.ic2IsInstalled == true){
                 RequiredCookTime = 300;
             }
         }
         
         if(CookTime > RequiredCookTime)
             CookTime = 0;
         
         _isActive = true;
         
         //int energyToUse = 2+ powerProvider.energyStored/1000;
         //int CookInc = energyToUse;
         int energyToUse = mod_jBuildCraft_BucketFiller.fillBucketEnergy;
 
         int energyUsed = 0;
         if(liquidId == 0){
             if(BucketFillerStacks[0].itemID == Item.bucketEmpty.shiftedIndex) {
                 //System.out.println("Bucket");
                 if (!hasPower) {
                     energyUsed = powerProvider.useEnergy(energyToUse, energyToUse, true);
                     NewBucket = API.getBucketForLiquid(slot.liquidId);
                     BucketQuantity = 1;
                 }
             } else if(mod_jBuildCraft_BucketFiller.ic2IsInstalled == true){
                 //System.out.println("IC2");
                 energyToUse = mod_jBuildCraft_BucketFiller.fillCellEnergy;
                 if (slot.liquidId == Block.lavaStill.blockID && !hasPower) {
                     energyUsed = powerProvider.useEnergy(energyToUse, energyToUse, true);
                     NewBucket = mod_jBuildCraft_BucketFiller.ic2CellLava.shiftedIndex;
                     BucketQuantity = 1;
                 } else if (slot.liquidId == Block.waterStill.blockID && !hasPower) {
                     energyUsed = powerProvider.useEnergy(energyToUse, energyToUse, true);
                     NewBucket = mod_jBuildCraft_BucketFiller.ic2CellWater.shiftedIndex;
                     BucketQuantity = 1;
                 } else if (slot.liquidId == BuildCraftEnergy.oilStill.blockID && !hasPower) {
                     energyUsed = powerProvider.useEnergy(energyToUse, energyToUse, true);
                     NewBucket = mod_jBuildCraft_BucketFiller.ic2CellCoalRef.shiftedIndex;
                     if(BucketQuantity <1) {
                         BucketQuantity = 30;
                     }
                 } else if (slot.liquidId == BuildCraftEnergy.fuel.shiftedIndex && !hasPower) {
                     energyUsed = powerProvider.useEnergy(energyToUse, energyToUse, true);
                     NewBucket = mod_jBuildCraft_BucketFiller.ic2CellCoal.shiftedIndex;
                     if(BucketQuantity <1) {
                         BucketQuantity = 36;
                     }
                 }
             }
         } else {
             energyToUse = mod_jBuildCraft_BucketFiller.emptyBucketEnergy;
             //System.out.println("reverse");
             energyUsed = powerProvider.useEnergy(energyToUse, energyToUse, true);
             NewBucket = Item.bucketEmpty.shiftedIndex;
             BucketQuantity = 1;
         }
         
         //CookTime += CookInc;
         if (energyUsed >= energyToUse  && !hasPower)
             hasPower = true;
 
         if (hasPower && NewBucket != 0 && BucketQuantity >= 1) {
             CookTime += 10;
         
             if(CookTime != RequiredCookTime) {
                 return;
             }
             
             decrStackSize(0, 1);
             createBucket(new ItemStack(NewBucket,1,0));
             
             onInventoryChanged();
             
             CookTime = 0;
             hasPower = false;
             
             BucketQuantity--;
             
             if(BucketQuantity > 0){
                 return;
             }
             
             if(liquidId == 0) {
                 slot.quantity -= API.BUCKET_VOLUME;
             } else {
                 fill(Orientations.Unknown, API.BUCKET_VOLUME, liquidId, true);
             }
             
             NewBucket = 0;
         }
     }
     
     public void createBucket(ItemStack toAdd) {
         if(BucketFillerStacks[1] == null) {
             setInventorySlotContents(1, toAdd.copy());
         } else if (BucketFillerStacks[1].isItemEqual(toAdd)) {
             BucketFillerStacks[1].stackSize += toAdd.stackSize;
         }
     }
 
     @Override
     public int fill(Orientations from, int quantity, int id, boolean doFill) {
         int used = slot.fill(from, quantity, id, doFill);
         if ((doFill) && (used > 0)) {
             updateNetworkTime.markTime(worldObj);
             sendNetworkUpdate();
         }
         return used;
     }
 
     @Override
     public int empty(int quantityMax, boolean doEmpty) {
         int res = 0;
         if (slot.quantity >= quantityMax) {
             res = quantityMax;
 
         if (doEmpty)
             slot.quantity -= quantityMax;
         }
         else {
             res = slot.quantity;
 
             if (doEmpty) {
                 slot.quantity = 0;
             }
         }
         if ((doEmpty) && (res > 0)) {
             updateNetworkTime.markTime(worldObj);
             sendNetworkUpdate();
         }
 
         return res;
     }
 
     public int getScaledQuantity(int i) {
         if(slot.quantity == 0 || i == 0 || LIQUID_PER_SLOT == 0){
             return 0;
         }
         return (int) (((float) slot.quantity / (float) (LIQUID_PER_SLOT)) * (float) i);
     }
 
     @Override
     public int getLiquidQuantity() {
         return slot.quantity;
     }
 
     @Override
     public int getCapacity() {
         return LIQUID_PER_SLOT - 1;
     }
     
     public int getFreeCapacity() {
         int ret = LIQUID_PER_SLOT - slot.quantity;
         if(ret < 0){
             return 0;
         }
         return ret;
     }
 
     @Override
     public int getLiquidId() {
         return slot.liquidId;
     }
 
     @Override
     public int getSizeInventory() {
         return BucketFillerStacks.length;
     }
 
     @Override
     public ItemStack getStackInSlot(int i) {
         return BucketFillerStacks[i];
     }
 
     @Override
     public ItemStack decrStackSize(int i, int j) {
         if (BucketFillerStacks[i] != null) {
             if (BucketFillerStacks[i].stackSize <= j) {
                 ItemStack itemstack = BucketFillerStacks[i];
                 BucketFillerStacks[i] = null;
                 return itemstack;
             }
             ItemStack itemstack1 = BucketFillerStacks[i].splitStack(j);
             if (BucketFillerStacks[i].stackSize == 0) {
                 BucketFillerStacks[i] = null;
             }
             return itemstack1;
         }
         return null;
     }
 
     @Override
     public void setInventorySlotContents(int i, ItemStack itemstack) {
         BucketFillerStacks[i] = itemstack;
         if ((itemstack != null) && (itemstack.stackSize > getInventoryStackLimit())) {
             itemstack.stackSize = getInventoryStackLimit();
         }
         onInventoryChanged();
     }
 
     @Override
     public String getInvName() {
         return "Bucket Filler";
     }
 
     @Override
     public int getInventoryStackLimit() {
         return 64;
     }
 
     @Override
     public boolean isUseableByPlayer(EntityPlayer entityplayer) {
         return true;
     }
 
     @Override
     public void openChest() {}
 
     @Override
     public void closeChest() {}
 
     @Override
     public void readFromNBT(NBTTagCompound nbttagcompound) {
         super.readFromNBT(nbttagcompound);
         NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
         BucketFillerStacks = new ItemStack[getSizeInventory()];
         for(int i = 0; i < nbttaglist.tagCount(); i++) {
             NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
             byte byte0 = nbttagcompound1.getByte("Slot");
             if(byte0 >= 0 && byte0 < BucketFillerStacks.length) {
                 BucketFillerStacks[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
             }
         }
         if (nbttagcompound.hasKey("lSlot")) {
             slot.readFromNBT(nbttagcompound.getCompoundTag("lSlot"));
         }
         CookTime = nbttagcompound.getShort("CookTime");
         RequiredCookTime = nbttagcompound.getShort("RequiredCookTime");
         BucketQuantity = nbttagcompound.getShort("BucketQuantity");
         NewBucket = nbttagcompound.getInteger("NewBucket");
         PowerFramework.currentFramework.loadPowerProvider(this, nbttagcompound);
         powerProvider.configure(20, 10, 10, 10, 100);
     }
 
     @Override
     public void writeToNBT(NBTTagCompound nbttagcompound) {
         super.writeToNBT(nbttagcompound);
         NBTTagCompound NBTslot = new NBTTagCompound();
         slot.writeFromNBT(NBTslot);
         nbttagcompound.setTag("lSlot", NBTslot);
         nbttagcompound.setShort("CookTime", (short)CookTime);
         nbttagcompound.setShort("RequiredCookTime", (short)RequiredCookTime);
         nbttagcompound.setInteger("NewBucket", NewBucket);
         nbttagcompound.setShort("BucketQuantity", (short)BucketQuantity);
         PowerFramework.currentFramework.savePowerProvider(this, nbttagcompound);
 
         NBTTagList nbttaglist = new NBTTagList();
         for(int i = 0; i < BucketFillerStacks.length; i++) {
             if(BucketFillerStacks[i] != null) {
                 NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                 nbttagcompound1.setByte("Slot", (byte)i);
                 BucketFillerStacks[i].writeToNBT(nbttagcompound1);
                 nbttaglist.appendTag(nbttagcompound1);
             }
         }
 
         nbttagcompound.setTag("Items", nbttaglist);
     }
 
     public void getGUINetworkData(int i, int j) {
         switch (i) {
             case 0:
                 slot.quantity = j;
             break;
             case 1:
                 slot.liquidId = j;
             break;
             case 2:
                 CookTime = j;
             break;  
             case 3:
                 RequiredCookTime = j;
             break;  
             case 4:
                 NewBucket = j;
             break;  
             case 5:
                 BucketQuantity = j;
             break;  
         }
     }
 
     public void sendGUINetworkData(ContainerBucketFiller containerBucketFiller, ICrafting iCrafting)
     {
         iCrafting.updateCraftingInventoryInfo(containerBucketFiller, 0, slot.quantity);
         iCrafting.updateCraftingInventoryInfo(containerBucketFiller, 1, slot.liquidId);
         iCrafting.updateCraftingInventoryInfo(containerBucketFiller, 2, CookTime);
         iCrafting.updateCraftingInventoryInfo(containerBucketFiller, 3, RequiredCookTime);
         iCrafting.updateCraftingInventoryInfo(containerBucketFiller, 4, NewBucket);
         iCrafting.updateCraftingInventoryInfo(containerBucketFiller, 5, BucketQuantity);
     }
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slotId) {
 		return null;
 	}
 }
