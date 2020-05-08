 package slimevoid.projectbench.container;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.InventoryCraftResult;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import slimevoid.projectbench.core.PBCore;
 import slimevoid.projectbench.core.lib.CommandLib;
 import slimevoid.projectbench.core.lib.InventoryMatch;
 import slimevoid.projectbench.core.lib.ItemLib;
 import slimevoid.projectbench.network.packet.PacketProjectGui;
 import slimevoid.projectbench.tileentity.TileEntityProjectBench;
 
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
 
 		public InventorySubUpdate(IInventory parentInventory, int startSlot,
 				int inventorySize) {
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
 
 		@Override
 		// isItemValid
 		public boolean isItemValid(ItemStack itemstack) {
 			return itemstack.itemID == PBCore.itemPlanBlank.itemID
 			|| itemstack.itemID == PBCore.itemPlanFull.itemID;
 		}
 
 		public int getSlotStackLimit() {
 			return 1;
 		}
 	}
 
 	TileEntityProjectBench projectbench;
 	public IInventory playerInventory;
 	public IInventory externalInventories[];
 	public IInventory externalSlotInventories[];
 	SlotCraftRefill slotCraft;
 	public IInventory craftResult;
     public InventoryCrafting fakeInv;
 	public InventoryCrafting craftMatrix;
 	
 	public int satisfyMask;
 	public boolean playerInventoryUsed;
 	public boolean playerInventoryLocked = true;
 
 	public ContainerProjectBench(InventoryPlayer playerInventory, TileEntityProjectBench tileentity) {
 		super();
 		this.projectbench = tileentity;
 		this.craftMatrix = new InventorySubCraft(this, tileentity);
 		this.craftResult = new InventoryCraftResult();
 		this.playerInventory = playerInventory;
 		int b0 = 140;
 		int l;
 		int i1;
 
 		// crafting matrix will have ghost inventory "slots" behind
 		// This area should drop items stored here out into world or attempt to
 		// store items into internal
 		// inventory and then drop remaining items from the player
 		for (l = 0; l < 3; ++l) {
 			for (i1 = 0; i1 < 3; ++i1) {
 				this.addSlotToContainer(new Slot(this.craftMatrix, i1 + l * 3, 48 + i1 * 18, 18 + l * 18));
 			}
 		}
 		// plan slot
 		this.addSlotToContainer(new SlotPlan(new InventorySubUpdate(tileentity, 9, 1), 0, 17, 36));
 		
 		IInventory[] sourceInventories = new IInventory[2];
 		sourceInventories[0] = this.projectbench;
 		sourceInventories[1] = playerInventory;
 		
 		// crafting result
 		slotCraft = new SlotCraftRefill(playerInventory.player,
 				this.craftMatrix, this.craftResult, sourceInventories, this, 0, 143, 35);
 		this.addSlotToContainer(slotCraft);
 
 		
 		// bench inventory
 		for (l = 0; l < 2; ++l) {
 			for (i1 = 0; i1 < 9; ++i1) {
 				int slotIndex = i1 + (l * 9);
 				this.addSlotToContainer(new Slot(new InventorySubUpdate(tileentity, 10, 18), slotIndex, 8 + i1 * 18, l * 18 + 90));
 			}
 		}
 		
 		// Player inventory
 		for (l = 0; l < 3; ++l) {
 			for (i1 = 0; i1 < 9; ++i1) {
 				int slotIndex = i1 + (l * 9);
 				this.addSlotToContainer(new Slot(new InventorySubUpdate(playerInventory, 9, 27), slotIndex, 8 + i1 * 18, l * 18 + b0));
 			}
 		}
 		// hotbar inventory
 		for (l = 0; l < 9; ++l) {
 			this.addSlotToContainer(new Slot(new InventorySubUpdate(playerInventory,0,9), l, 8 + l * 18, 58 + b0));
 		}
 		
 		//add n,e,w,s inventories
 		fakeInv = new InventoryCrafting(new ContainerNull(), 3, 3);
 		this.onCraftMatrixChanged(craftMatrix);
 	}
 
 	public int getSatisfyMask() {
 		ItemStack plan = this.projectbench.getStackInSlot(9);
 		ItemStack items[] = null;
 		if (plan != null) {
 			items = getShadowItems(plan);
 		}
 		int bits = 0;
 		for (int i = 0; i < 9; i++) {
 			ItemStack st = this.projectbench.getStackInSlot(i);
 			if (st != null) {
 				bits |= 1 << i;
 				continue;
 			}
 			if (items == null || items[i] == null)
 				bits |= 1 << i;
 		}
 
 		if (bits == 511) {
 			return 511;
 		}
 		for (int i = 0; i < 18; i++) {
 			ItemStack test = this.projectbench.getStackInSlot(10 + i);
 			if (test == null || test.stackSize == 0) {
 				continue;
 			}
 			int sc = test.stackSize;
 			for (int j = 0; j < 9; j++) {
 				if ((bits & 1 << j) > 0) {
 					continue;
 				}
 				ItemStack st = this.projectbench.getStackInSlot(j);
 				if (st != null) {
 					continue;
 				}
 				st = items[j];
 				if (st == null || !ItemLib.matchOre(st, test)) {
 					continue;
 				}
 				bits |= 1 << j;
 				if (--sc == 0) {
 					break;
 				}
 			}
 
 		}
 		if (bits == 511) {
 			return 511;
 		}
 		for (int i = 0; i < this.playerInventory.getSizeInventory(); i++) {
 			ItemStack test = this.playerInventory.getStackInSlot(i);
 			if (test == null || test.stackSize == 0) {
 				continue;
 			}
 			int sc = test.stackSize;
 			for (int j = 0; j < 9; j++) {
 				if ((bits & 1 << j) > 0) {
 					continue;
 				}
 				ItemStack st = this.projectbench.getStackInSlot(j);
 				if (st != null) {
 					continue;
 				}
 				st = items[j];
 				if (st == null || !ItemLib.matchOre(st, test)) {
 					continue;
 				}
 				bits |= 1 << j;
 				
 				if (--sc == 0) {
 					break;
 				}
 			}
 
 		}		
 		return bits;
 	}
 
 	private InventoryMatch findMatch(ItemStack a) {
 		for (int i = 0; i < 18; i++) {
 			ItemStack test = this.projectbench.getStackInSlot(10 + i);
 			if (test != null && test.stackSize != 0
 					&& ItemLib.matchOre(a, test))
 				return new InventoryMatch(this.projectbench,10 + i);
 		}
 		for (int i = 0; i < this.playerInventory.getSizeInventory(); i++) {
 			ItemStack test = this.playerInventory.getStackInSlot(i);
 			if (test != null && test.stackSize != 0
 					&& ItemLib.matchOre(a, test))
 				return new InventoryMatch(this.playerInventory,i);
 		}
 		//TODO: add External Inventories
 		return null;
 	}
     
 	@Override
 	public void onCraftMatrixChanged(IInventory inventory) {
 		ItemStack plan = this.projectbench.getStackInSlot(9);
 		ItemStack items[] = null;
 		if (plan != null) {
 			items = getShadowItems(plan);
 		}
 		for (int i = 0; i < 9; i++) {
 			ItemStack tos = this.projectbench.getStackInSlot(i);
 			if (tos == null && items != null && items[i] != null) {
 				InventoryMatch match = findMatch(items[i]);
 				if (match != null) {
 					tos = match.InventoryMatch.getStackInSlot(match.slotIndex);
 				}
 			}
 			this.fakeInv.setInventorySlotContents(i, tos);
 		}
 		satisfyMask = getSatisfyMask();
 		if (satisfyMask == 511) {
 			this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(fakeInv, projectbench.worldObj));
 		} else {
 			this.craftResult.setInventorySlotContents(0, null);
 		}
 	}
 
 	/**
 	 * Called when the container is closed.
 	 */
 	public void onContainerClosed(EntityPlayer entityplayer) {
 		// TODO :: try to shove as many items found in crafting matrix into
 		// internal storage before dumping to world
 		super.onContainerClosed(entityplayer);
 
 		if (!this.projectbench.worldObj.isRemote) {
 			for (int i = 0; i < 9; ++i) {
 				ItemStack itemstack = this.craftMatrix
 						.getStackInSlotOnClosing(i);
 
 				if (itemstack != null) {
 					entityplayer.dropPlayerItem(itemstack);
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer) {
 		return this.projectbench.isUseableByPlayer(entityplayer);
 	}
 
 	public ItemStack[] getPlanItems() {
 		ItemStack plan = this.projectbench.getStackInSlot(9);
 		if (plan == null)
 			return null;
 		else
 			return getShadowItems(plan);
 	}
 
 	public static ItemStack[] getShadowItems(ItemStack ist) {
 		if (ist.stackTagCompound == null)
 			return null;
 		NBTTagList require = ist.stackTagCompound.getTagList("requires");
 		if (require == null)
 			return null;
 		ItemStack tr[] = new ItemStack[9];
 		for (int i = 0; i < require.tagCount(); i++) {
 			NBTTagCompound item = (NBTTagCompound) require.tagAt(i);
 			ItemStack is2 = ItemStack.loadItemStackFromNBT(item);
 			int sl = item.getByte("Slot");
 			if (sl >= 0 && sl < 9)
 				tr[sl] = is2;
 		}
 
 		return tr;
 	}
 
 	/**
 	 * Called when a player shift-clicks on a slot. You must override this or
 	 * you will crash when someone does that.
 	 */
 	@Override
 	public ItemStack transferStackInSlot(EntityPlayer entityplayer,
 			int slotShiftClicked) {
 		ItemStack itemstackCopy = null;
 		Slot slot = (Slot) this.inventorySlots.get(slotShiftClicked);
 		
 		if (slot != null && slot.getHasStack()) {
 			ItemStack stackInSlot = slot.getStack();
 			itemstackCopy = stackInSlot.copy();
 			if (slotShiftClicked != 9 && (stackInSlot.itemID == PBCore.itemPlanBlank.itemID
 					|| stackInSlot.itemID == PBCore.itemPlanFull.itemID)){
				if (!this.mergeItemStack(stackInSlot, 9, 10, true)) {//try to place into plan slot					
					if ((slotShiftClicked >= 11 && slotShiftClicked < 29)||!this.mergeItemStack(stackInSlot, 11, 29, false)) {//else place in internal inventory
 						return null;
 					}
 				}				
 			}else if (slotShiftClicked < 9 || slotShiftClicked==10) {
 				if (!this.mergeItemStack(stackInSlot, 11, 65, false)) {
 					return null;
 				}
 			} else if (slotShiftClicked < 29) { //if internal inventory shift click into player inventory
				if (!this.mergeItemStack(stackInSlot, 29, 65, true)) {					
 						return null;					
 				}
 			} else if (!this.mergeItemStack(stackInSlot, 11, 29, false)) { //if player then go into internal inventory first
 				if (!this.mergeItemStack(stackInSlot, 0, 9, false)){//then crafting grid					
 					return null;					
 				}
 			}
 			if (stackInSlot.stackSize == 0) {
 				slot.putStack(null);
 			} else {
 				slot.onSlotChanged();
 			}
 			if (stackInSlot.stackSize != itemstackCopy.stackSize) {
 				slot.onPickupFromSlot(entityplayer, stackInSlot);
 			} else {
 				return null;
 			}
 		}
 		return itemstackCopy;
 	}
 
 	protected void retrySlotClick(int par1, int par2, boolean par3, EntityPlayer par4EntityPlayer)
     {
 		if(this.playerInventoryUsed){
 			this.playerInventoryUsed=false;
 		}else{
         super.retrySlotClick(par1, par2, par3, par4EntityPlayer);
 		}
     }
 
 	public class InventorySubCraft extends InventoryCrafting {
 		private Container eventHandler;
 		private TileEntityProjectBench parent;
 
 		public InventorySubCraft(Container container, TileEntityProjectBench par) {
 			super(container, 3, 3);
 			parent = par;
 			eventHandler = container;
 		}
 
 		@Override
 		public int getSizeInventory() {
 			return 9;
 		}
 
 		@Override
 		public ItemStack getStackInSlot(int i) {
 			if (i >= 9) {
 				return null;
 			} else {
 				return parent.getStackInSlot(i);
 			}
 		}
 
 		@Override
 		public ItemStack getStackInRowAndColumn(int i, int j) {
 			if (i < 0 || i >= 3) {
 				return null;
 			} else {
 				int k = i + j * 3;
 				return getStackInSlot(k);
 			}
 		}
 
 		@Override
 		public ItemStack decrStackSize(int i, int j) {
 			ItemStack tr = parent.decrStackSize(i, j);
 			if (tr != null) {
 				eventHandler.onCraftMatrixChanged(this);
 			}
 			return tr;
 		}
 
 		@Override
 		public void setInventorySlotContents(int i, ItemStack ist) {
 			parent.setInventorySlotContents(i, ist);
 			eventHandler.onCraftMatrixChanged(this);
 		}
 	}
 
 	public void handleGuiEvent(PacketProjectGui packet) {
 		if (this.projectbench.worldObj == null
 				|| this.projectbench.worldObj.isRemote)
 			return;
 		if (!packet.getCommand().equals(CommandLib.CREATE_PROJECT_PLAN))
 			return;
 		ItemStack blank = this.projectbench.getStackInSlot(9);
 		if (blank == null || blank.itemID != PBCore.itemPlanBlank.itemID)
 			return;
 		ItemStack plan = new ItemStack(PBCore.itemPlanFull);
 		plan.stackTagCompound = new NBTTagCompound();
 		NBTTagCompound result = new NBTTagCompound();
 		System.out.println(craftResult.getStackInSlot(0));
 		craftResult.getStackInSlot(0).writeToNBT(result);
 		plan.stackTagCompound.setCompoundTag("result", result);
 		NBTTagList requires = new NBTTagList();
 		for (int i = 0; i < 9; i++) {
 			ItemStack is1 = craftMatrix.getStackInSlot(i);
 			if (is1 != null) {
 				ItemStack ist = new ItemStack(is1.itemID, 1,
 						is1.getItemDamage());
 				NBTTagCompound item = new NBTTagCompound();
 				ist.writeToNBT(item);
 				item.setByte("Slot", (byte) i);
 				requires.appendTag(item);
 			}
 		}
 
 		plan.stackTagCompound.setTag("requires", requires);
 		this.projectbench.setInventorySlotContents(9, plan);
 	}
 
 }
