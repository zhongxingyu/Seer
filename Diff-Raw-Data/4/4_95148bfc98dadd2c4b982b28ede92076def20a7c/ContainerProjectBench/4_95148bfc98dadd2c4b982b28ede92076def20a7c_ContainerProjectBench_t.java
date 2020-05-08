 package slimevoid.projectbench.container;
 
 import slimevoid.projectbench.core.PBCore;
 import slimevoid.projectbench.tileentity.TileEntityProjectBench;
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.InventoryCraftResult;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.inventory.Slot;
 import net.minecraft.inventory.SlotCrafting;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.world.World;
 
 public class ContainerProjectBench extends Container {
 	
     
 	public static class ContainerNull extends Container {
 
 
 		@Override
 		public boolean canInteractWith(EntityPlayer entityplayer) {
 			return false;
 		}
 
 		@Override
 		public void onCraftMatrixChanged(IInventory inventory) {
 		}
 
 		public ContainerNull() {
 		}
 	}
 
 	public class InventorySubUpdate implements IInventory {
 		
 		int size;
 		int start;
 		IInventory parent;
 		final ContainerProjectBench containerProjectBench;
 
 		public InventorySubUpdate(IInventory parentInventory, int startSlot, int inventorySize) {
 			super();
 			containerProjectBench = ContainerProjectBench.this;
 			parent = parentInventory;
 			start = startSlot;
 			size = inventorySize;
 		}
 
 		@Override
 		public int getSizeInventory() {
 			return size;
 		}
 
 		@Override
 		public ItemStack getStackInSlot(int slot) {
 			return parent.getStackInSlot(slot + start);
 		}
 
 		@Override
 		public ItemStack decrStackSize(int slot, int amount) {
 			ItemStack itemstack = parent.decrStackSize(slot + start, amount);
 			if (itemstack != null) {
 				ContainerProjectBench.this.onCraftMatrixChanged(this);
 			}
 			return itemstack;
 		}
 
 		@Override
 		public ItemStack getStackInSlotOnClosing(int slot) {
 			return parent.getStackInSlotOnClosing(slot + start);
 		}
 
 		@Override
 		public void setInventorySlotContents(int slot, ItemStack ist) {
 			parent.setInventorySlotContents(slot + start, ist);
 			ContainerProjectBench.this.onCraftMatrixChanged(this);
 		}
 
 		@Override
 		public String getInvName() {
 			return parent.getInvName();
 		}
 
 		@Override
 		public int getInventoryStackLimit() {
 			return parent.getInventoryStackLimit();
 		}
 
 		@Override
 		public void onInventoryChanged() {
 			ContainerProjectBench.this.onCraftMatrixChanged(this);
 			parent.onInventoryChanged();
 		}
 
 		@Override
 		public boolean isUseableByPlayer(EntityPlayer entityplayer) {
 			return false;
 		}
 
 		@Override
 		public void openChest() {
 		}
 
 		@Override
 		public void closeChest() {
 		}
 
 		@Override
 		public boolean isInvNameLocalized() {
 			return false;
 		}
 
 		@Override
 		public boolean isItemValidForSlot(int i, ItemStack itemstack) {
 			return true;
 		}
 	}
 
 	public static class SlotPlan extends Slot {
 
 		public SlotPlan(IInventory inventory, int i, int j, int k) {
 			super(inventory, i, j, k);
 		}
 		
 		
 		//add when done with plans
 		//@Override
 		//isItemValid
 		public boolean isItemValid(ItemStack itemstack) {
 			return itemstack.itemID == new ItemStack(Block.workbench).itemID;
 			//return itemstack.itemID == PBCore.itemPlanBlank.itemID
 					//|| itemstack.itemID == PBCore.itemPlanFull.itemID;
 		}
 
 		public int getSlotStackLimit() {
 			return 1;
 		}
 	}
 
 	TileEntityProjectBench projectbench;
 	// SlotCraftRefill slotCraft;
 	//public InventorySubCraft craftMatrix;
 	/** The crafting matrix inventory (3x3). */
     public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
     public IInventory craftResult = new InventoryCraftResult();
     private World worldObj;
 	public int satisfyMask;
 
 	public ContainerProjectBench(InventoryPlayer player,World world, TileEntityProjectBench tileentity) {
 		super();
 		this.projectbench = tileentity;		
 		this.worldObj = world;
 		int b0 = 140;
 		int l;
         int i1;
         
         
         
         
         //crafting matrix will have ghost inventory "slots" behind
         //This area should drop items stored here out into world or attempt to store items into internal
         //inventory and then drop remaining items from the player
 		for (l = 0; l < 3; ++l)
         {
             for (i1 = 0; i1 < 3; ++i1)
             {
                 this.addSlotToContainer(new Slot(this.craftMatrix, i1 + l * 3 , 48 + i1 * 18, 18 + l * 18));
             }
         }
 		
 		//crafting result
         this.addSlotToContainer(new SlotCrafting(player.player, this.craftMatrix, this.craftResult, 9, 143, 35));
 		
 		//plan slot
         this.addSlotToContainer(new SlotPlan(tileentity,0,17,36 ));
 		//bench inventory
 		for(l=0; l<2; ++l){
 			for (i1 = 0; i1 < tileentity.getSizeInventory()/2; ++i1)
 			{
 				this.addSlotToContainer(new Slot(tileentity, i1 + l *9 +1, 8 + i1 * 18, l *18 + 90));
 			}
 		}
 		//Player inventory
 		 for (l = 0; l < 3; ++l)
 	        {
 	            for (i1 = 0; i1 < 9; ++i1)
 	            {
 	                this.addSlotToContainer(new Slot(player, i1 + l * 9 + 9, 8 + i1 * 18, l * 18 + b0));
 	            }
 	        }
 		 //hotbar inventory
 	        for (l = 0; l < 9; ++l)
 	        {
 	            this.addSlotToContainer(new Slot(player, l, 8 + l * 18, 58 + b0));
 	        }
 	}
 	/**
      * Callback for when the crafting matrix is changed.
      */
     public void onCraftMatrixChanged(IInventory par1IInventory)
     {
         this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj));
     }
 
     /**
      * Called when the container is closed.
      */
     public void onContainerClosed(EntityPlayer par1EntityPlayer)
     {
     	//TODO:try to shove as many items found in crafting matrix into internal storage before dumping to world
         super.onContainerClosed(par1EntityPlayer);
 
         if (!this.worldObj.isRemote)
         {
             for (int i = 0; i < 9; ++i)
             {
                 ItemStack itemstack = this.craftMatrix.getStackInSlotOnClosing(i);
 
                 if (itemstack != null)
                 {
                     par1EntityPlayer.dropPlayerItem(itemstack);
                 }
             }
         }
     }
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer) {
 		return true;
 	}
 	
 	/**
      * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
      */
     public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
     {
         ItemStack itemstack = null;
         Slot slot = (Slot)this.inventorySlots.get(par2);
 
         if (slot != null && slot.getHasStack())
         {
             ItemStack itemstack1 = slot.getStack();
             itemstack = itemstack1.copy();
 
             if (par2 < 2 * 9 + 11)
             {
                 if (!this.mergeItemStack(itemstack1, 2 * 9 + 11, this.inventorySlots.size(), true))
                 {
                     return null;
                 }
                slot.onSlotChange(itemstack1, itemstack);
             }
             else if (!this.mergeItemStack(itemstack1, 10, 2 * 9 + 11, false))
             {
                 return null;
             }
 
             if (itemstack1.stackSize == 0)
             {
                 slot.putStack((ItemStack)null);
             }
             else
             {
                 slot.onSlotChanged();
             }
            
            slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
         }
 
         return itemstack;
     }
 
 }
