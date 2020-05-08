 package vazkii.craftingcreation.gui;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import vazkii.craftingcreation.helper.GameHelper;
 import vazkii.craftingcreation.item.ILevelable;
 import vazkii.craftingcreation.item.ModItems;
 
 public class SlotKiln extends Slot {
 	
 	ContainerKiln container;
 	
 	public SlotKiln(InventoryKiln par1iInventory, ContainerKiln container, int par2, int par3, int par4) {
 		super(par1iInventory, par2, par3, par4);
 		this.container = container;
 	}
 	
 	@Override
 	public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
 		if(!getHasStack())
 			return false;
 		
 		ItemStack stack = getStack();
 		int cost = ContainerKiln.getItemCost(stack);
		int held = GameHelper.getClay(par1EntityPlayer, ((ILevelable) stack.getItem()).getLevel(stack));
 		
 		return held >= cost;
 	}
 	
 	@Override
 	public boolean isItemValid(ItemStack par1ItemStack) {
 		return false;
 	}
 	
 	@Override
 	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack) {
 		int cost = ContainerKiln.getItemCost(par2ItemStack);
		int level = ((ILevelable) par2ItemStack.getItem()).getLevel(par2ItemStack);
 		InventoryPlayer inv = par1EntityPlayer.inventory;
 		
 		int found = 0;
 		
 		for(int i = 0; i < inv.getSizeInventory(); i++) {
 			ItemStack stack = inv.getStackInSlot(i);
 			if(stack != null && stack.itemID == ModItems.creationClay.itemID && stack.getItemDamage() == level) {
 				inv.setInventorySlotContents(i, null);
 				found++;
 			}
 			
 			if(found >= cost)
 				break;
 		}
 		
 		inventory.setInventorySlotContents(0, par2ItemStack.copy());
 		
 		super.onPickupFromSlot(par1EntityPlayer, par2ItemStack);
 	}
 
 }
