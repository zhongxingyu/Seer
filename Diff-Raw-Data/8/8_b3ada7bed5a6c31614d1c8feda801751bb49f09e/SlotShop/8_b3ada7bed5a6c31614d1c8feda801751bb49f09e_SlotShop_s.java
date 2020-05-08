 package hunternif.mc.dota2items.inventory;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
 
 public class SlotShop extends Slot {
 
 	public SlotShop(IInventory inventory, int slotIndex, int x, int y) {
 		super(inventory, slotIndex, x, y);
 	}
 	
 	@Override
 	public boolean canTakeStack(EntityPlayer entityPlayer) {
 		return false;
 	}

 }
