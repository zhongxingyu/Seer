 package demonmodders.Crymod.Common.Inventory;
 
 import net.minecraft.src.Container;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.Slot;
 
 public abstract class AbstractContainer extends Container {
 
 	private final IInventory inventory;
 	
 	public AbstractContainer(IInventory inventory) {
 		this.inventory = inventory;
 	}
 	
 	@Override
 	public boolean canInteractWith(EntityPlayer player) {
 		return inventory.isUseableByPlayer(player);
 	}
 
 	@Override
 	public Slot addSlotToContainer(Slot par1Slot) {
 		// make it public, for InventoryHelper
 		return super.addSlotToContainer(par1Slot);
 	}
 
 }
