 package makmods.levelstorage.tileentity;
 
 import makmods.levelstorage.LSBlockItemList;
 import makmods.levelstorage.LevelStorage;
 import makmods.levelstorage.gui.client.GUIParticleAccelerator;
 import makmods.levelstorage.gui.container.ContainerParticleAccelerator;
 import makmods.levelstorage.gui.container.phantom.PhantomInventory;
 import makmods.levelstorage.gui.logicslot.LogicSlot;
 import makmods.levelstorage.init.LSFluids;
 import makmods.levelstorage.item.SimpleItems;
 import makmods.levelstorage.item.SimpleItems.SimpleItemShortcut;
 import makmods.levelstorage.iv.IVRegistry;
 import makmods.levelstorage.tileentity.template.IHasButtons;
 import makmods.levelstorage.tileentity.template.ITEHasGUI;
 import makmods.levelstorage.tileentity.template.TileEntityInventorySinkWithFluid;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidStack;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class TileEntityParticleAccelerator extends
 		TileEntityInventorySinkWithFluid implements ISidedInventory, ITEHasGUI,
 		IHasButtons {
 
 	public PhantomInventory phantomInventory;
 
 	public TileEntityParticleAccelerator() {
 		super(2, 128);
 		outputSlot = new LogicSlot(this, 1);
 		phantomInventory = new PhantomInventory(1);
 		sampleSlot = new LogicSlot(this.phantomInventory, 0);
 	}
 
 	public int mode = ANTIMATTER_PRODUCTION_MODE;
 	public static final int OPERATIONS_PER_TICK = 200 / IVRegistry.IV_TO_FLUID_CONVERSION
 			.getKey();
 	public static final int ANTIMATTER_PRODUCTION_MODE = 0;
 	public static final int MATTER_RESHAPING_MODE = 1;
 
 	public static final ItemStack ANTIMATTER_IS = SimpleItems.instance
 			.getIngredient(9);
 	public int internalIV = 0;
 	public static final int ANTIMATTER_IV = 32768;
 
 	public int progress;
 	public int maxProgress = 1;
 
 	@Override
 	public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
 		super.writeToNBT(par1NBTTagCompound);
 		phantomInventory.writeToNBT(par1NBTTagCompound);
 		par1NBTTagCompound.setInteger("modePA", mode);
 		par1NBTTagCompound.setInteger("progress", progress);
 		par1NBTTagCompound.setInteger("internalIV", internalIV);
 		par1NBTTagCompound.setInteger("maxProg", maxProgress);
 	}
 
 	@Override
 	public boolean canFill(ForgeDirection from, Fluid fluid) {
 		return fluid.getName().equals(LSFluids.instance.fluidIV.getName());
 	}
 
 	@Override
 	public boolean canDrain(ForgeDirection from, Fluid fluid) {
 		return false;
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
 		super.readFromNBT(par1NBTTagCompound);
 		phantomInventory.readFromNBT(par1NBTTagCompound);
 		mode = par1NBTTagCompound.getInteger("modePA");
 		progress = par1NBTTagCompound.getInteger("progress");
 		internalIV = par1NBTTagCompound.getInteger("internalIV");
 		maxProgress = par1NBTTagCompound.getInteger("maxProg");
 	}
 
 	@Override
 	public String getInvName() {
 		return "Particle Accelerator";
 	}
 
 	public int getProgress() {
 		return progress;
 	}
 
 	public int getMaxProgress() {
 		return maxProgress;
 	}
 
 	public int gaugeProgressScaled(int i) {
 		if (getProgress() <= 0) {
 			return 0;
 		}
 		int r = getProgress() * i / getMaxProgress();
 		if (r > i) {
 			r = i;
 		}
 		return r;
 	}
 
 	public void setProgress(int progress) {
 		this.progress = progress;
 	}
 
 	public void addProgress(int progress) {
 		this.progress += progress;
 	}
 
 	@Override
 	public ItemStack getWrenchDrop(EntityPlayer entityPlayer) {
 		return new ItemStack(LSBlockItemList.blockParticleAccelerator);
 	}
 
 	@Override
 	public int[] getAccessibleSlotsFromSide(int var1) {
 		return new int[] { 1 };
 	}
 
 	@Override
 	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
 		return false;
 	}
 
 	@Override
 	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
 		return true;
 	}
 
 	@Override
 	public void onUnloaded() {
 	}
 
 	@Override
 	public int getMaxInput() {
 		return 8192;
 	}
 
 	@Override
 	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
 		return false;
 	}
 
 	@Override
 	public void onLoaded() {
 
 	}
 
 	public static final int ENERGY_COST_PER_TICK = 1024;
 
 	public LogicSlot sampleSlot;
 	public LogicSlot outputSlot;
 
 	public ItemStack getPatternIdeal() {
 		if (sampleSlot.get() == null)
 			return null;
 		ItemStack is = sampleSlot.get().copy();
 		is.stackSize = 1;
 		return is;
 	}
 
 	public ItemStack getAntimatterIdeal() {
 		ItemStack is = SimpleItemShortcut.ANTMATTER_TINY_PILE.getItemStack()
 				.copy();
 		is.stackSize = 1;
 		return is;
 	}
 
 	public void updateEntity() {
 		super.updateEntity();
 		if (!LevelStorage.isSimulating())
 			return;
 		if (!canUse(ENERGY_COST_PER_TICK))
 			return;
 		switch (mode) {
 		case ANTIMATTER_PRODUCTION_MODE:
 			if (handleAntimatterTick())
 				use(ENERGY_COST_PER_TICK);
 			break;
 		case MATTER_RESHAPING_MODE:
 			if (handleMatterTick())
 				use(ENERGY_COST_PER_TICK);
 			break;
 		}
 	}
 
 	private boolean handleMatterTick() {
 		if (!IVRegistry.hasValue(getPatternIdeal()))
 			this.phantomInventory.setInventorySlotContents(0, null);
		int oldProgress = progress;
 		boolean shouldUseEnergy = false;
 		if (getPatternIdeal() == null)
 			return false;
 		if (!outputSlot.add(getPatternIdeal(), true))
 			return false;
 		int ivCost = IVRegistry.getValue(getPatternIdeal());
 		if (ivCost == IVRegistry.NOT_FOUND)
 			return false;
 		this.maxProgress = ivCost;
 		for (int i = 0; i < OPERATIONS_PER_TICK; i++) {
 			if (this.internalIV <= ivCost + 1) {
 				FluidStack drained = this.getFluidTank().drain(
 						IVRegistry.IV_TO_FLUID_CONVERSION.getValue(), true);
 				if (drained != null
 						&& drained.amount == IVRegistry.IV_TO_FLUID_CONVERSION
 								.getValue()) {
 					this.internalIV += IVRegistry.IV_TO_FLUID_CONVERSION
 							.getKey();
 				}
 			} else {
 				if (this.internalIV >= ivCost) {
 					this.internalIV -= ivCost;
 					outputSlot.add(getPatternIdeal(), false);
 					progress = 0;
 				}
 			}
 			shouldUseEnergy = true;
 			progress++;
 			if (progress > maxProgress)
 				progress = 0;
 		}
 		this.setProgress(internalIV);
		return oldProgress != progress;
 	}
 
 	private boolean handleAntimatterTick() {
 		boolean shouldUseEnergy = false;
		int oldProgress = progress;
 		if (!outputSlot.add(getAntimatterIdeal(), true))
 			return false;
 		int ivCost = ANTIMATTER_IV;
 		this.maxProgress = ivCost;
 		for (int i = 0; i < OPERATIONS_PER_TICK; i++) {
 			if (this.internalIV <= ivCost + 1) {
 				FluidStack drained = this.getFluidTank().drain(
 						IVRegistry.IV_TO_FLUID_CONVERSION.getValue(), true);
 				if (drained != null
 						&& drained.amount == IVRegistry.IV_TO_FLUID_CONVERSION
 								.getValue()) {
 					this.internalIV += IVRegistry.IV_TO_FLUID_CONVERSION
 							.getKey();
 				}
 			} else {
 				if (this.internalIV >= ivCost) {
 					this.internalIV -= ivCost;
 					outputSlot.add(getAntimatterIdeal(), false);
 					progress = 0;
 				}
 			}
 			shouldUseEnergy = true;
 			progress++;
 			if (progress > maxProgress)
 				progress = 0;
 		}
 		this.phantomInventory.setInventorySlotContents(0, getAntimatterIdeal());
 		this.setProgress(internalIV);
		return progress != oldProgress;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public GuiContainer getGUI(EntityPlayer player, World world, int x, int y,
 			int z) {
 		return new GUIParticleAccelerator(player.inventory, this);
 	}
 
 	@Override
 	public Container getContainer(EntityPlayer player, World world, int x,
 			int y, int z) {
 		return new ContainerParticleAccelerator(player.inventory, this);
 	}
 
 	@Override
 	public void handleButtonClick(int buttonId) {
 		this.mode = this.mode == 0 ? 1 : 0;
 		// if (this.mode == 0)
 		// this.mode = 1;
 		// else
 		// this.mode = 0;
 	}
 
 	@Override
 	public int getCapacity() {
 		return 400000;
 	}
 
 	@Override
 	public boolean explodes() {
 		return true;
 	}
 
 }
