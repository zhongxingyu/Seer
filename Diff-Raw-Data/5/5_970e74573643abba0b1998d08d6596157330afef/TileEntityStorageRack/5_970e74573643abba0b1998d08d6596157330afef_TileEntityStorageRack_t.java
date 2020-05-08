 package ip.industrialProcessing.machines.plants.storage.storageRack;
 
 import java.io.ObjectOutputStream.PutField;
 
 import ip.industrialProcessing.IndustrialProcessing;
 import ip.industrialProcessing.LocalDirection;
 import ip.industrialProcessing.config.ConfigMachineBlocks;
 import ip.industrialProcessing.config.ConfigRenderers;
 import ip.industrialProcessing.config.INamepace;
 import ip.industrialProcessing.config.ISetupCreativeTabs;
 import ip.industrialProcessing.gui.GuiLayout;
 import ip.industrialProcessing.gui.IGuiLayout;
 import ip.industrialProcessing.gui.components.GuiLayoutPanelType;
 import ip.industrialProcessing.machines.BlockMachineRendered;
 import ip.industrialProcessing.machines.RecipesMachine;
 import ip.industrialProcessing.machines.TileEntityMachine;
 import ip.industrialProcessing.machines.hydroCyclone.TileEntityHydroCyclone;
 import ip.industrialProcessing.machines.plants.storage.storageBox.BlockStorageBox;
 import ip.industrialProcessing.recipes.IRecipeBlock;
 import ip.industrialProcessing.utils.IDescriptionBlock;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class TileEntityStorageRack extends TileEntityMachine {
 
 	public TileEntityStorageRack() {
 		// conveyors/pipes can't pick up boxes from here!
 		LocalDirection[] noDirection = new LocalDirection[0];
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 
 		// box1
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 
 		// box2
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 
 		// box3
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 
 		// box4
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 
 		// box5
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 
 		// box6
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 		addStack(null, noDirection, true, false);
 	}
 
 	@Override
 	protected boolean isValidInput(int slot, int itemID) {
 		if (slot < 6)
 			return itemID == IndustrialProcessing.blockStorageBox.blockID;
 		return ((itemID != IndustrialProcessing.blockStorageBox.blockID)&&(getStackInSlot((slot - 6)/9) != null));
 	}
 
 	public ItemStack popBox() {
 		for (int i = 0; i < 6; i++) {
 			ItemStack stack = decrStackSize(i, 1);
 			if(stack != null){
 				return stack;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public ItemStack decrStackSize(int i, int j) {
 		if (i > 6) {
 			return super.decrStackSize(i, j);
 		}
		
		if (getStackInSlot(i) != null) {
			ItemStack stack = getStackInSlot(i).copy();
 			for (int k = 0; k < 9; k++) {
 				ItemStack stackInSlot = getStackInSlot(6 + i * 9 + k);
 				if (stackInSlot != null) {
 					BlockStorageBox.putStackInBox(stackInSlot.copy(), stack, k);
 					setInventorySlotContents(6 + i * 9 + k, null);
 				}
 			}
 			setInventorySlotContents(i, null);
 			onInventoryChanged();
 			return stack;
 		}
 		return null;
 	}
 
 	public boolean pushBox(ItemStack itemStack) {
 
 		if (itemStack == null)
 			return false;
 		if (itemStack.stackSize != 1)
 			return false;
 		for (int i = 0; i < 6; i++) {
 			if (isValidInput(i, itemStack.itemID)) {
 				ItemStack slot = getStackInSlot(i);
 				if (slot == null) {
 					this.setInventorySlotContents(i, itemStack);
 					onInventoryChanged();
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void setInventorySlotContents(int slotIndex, ItemStack stack) {
 		if(slotIndex < 6 || stack != null || (slotIndex >= 6 && getStackInSlot((slotIndex - 6)/9) != null))
 			super.setInventorySlotContents(slotIndex, stack);
 		if (slotIndex < 6 && stack != null) {
 			for (int i = 0; i < 9; i++) {
 				ItemStack stackFromBox = BlockStorageBox.getStackFromBox(stack, i, 64);
 				if (stackFromBox != null) {
 					this.setInventorySlotContents(6 + 9 * slotIndex + i, stackFromBox);
 				}
 			}
 		}
 	}
 
 }
