 package sammko.quantumCraft.machine; //Comments are awsome ^^
 
 import ic2.api.Direction;
 import ic2.api.energy.event.EnergyTileLoadEvent;
 import ic2.api.energy.tile.IEnergySink;
 import sammko.quantumCraft.core.Initializator;
 import sammko.quantumCraft.resources.NBTTags;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemHoe;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemSword;
 import net.minecraft.item.ItemTool;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.ISidedInventory;
 import net.minecraftforge.common.MinecraftForge;
 
 public class TileEntityExtractor extends TileEntityMachine implements
 		IInventory, ISidedInventory, IEnergySink {
 
 	// TODO: finish this
 
 	public int itemFuel;
 	public int progress;
 
 	public int getChargeState() {
 		return internalStorage * 100 / MAX_STORAGE;
 	}
 
 	public TileEntityExtractor(ForgeDirection rot) {
 		super(rot, 5, "Extractor");
 	}
 
 	public TileEntityExtractor() {
 		super(ForgeDirection.NORTH, 5, "Extractor");
 	}
 
 	private void init() {
 		internalStorage = 0;
 		progress = 0;
 	}
 
 	public int gaugeProgressScaled (int scale)
 	{
		return (progress * scale) / 1000;
 	}
 	
 	public int gaugeFuelScaled (int scale)
 	{
 		if (itemFuel == 0)
 		{
 			itemFuel = internalStorage;
 			if (itemFuel == 0)
 			{
 				itemFuel = 1000;
 			}
 		}
 		return (internalStorage * scale) / itemFuel;
 	}
 
 	
 	public void updateEntity() {
 		super.updateEntity();
 		if (!this.addedToEnergyNet) {
 			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
 			this.addedToEnergyNet = true;
 		}
 
 		boolean gf = this.internalStorage > 0;
 		boolean nu = false;
 		if (internalStorage > 0) {
 
 		}
 		if (!this.worldObj.isRemote) {
 			if (inventory[2] != null
 					&& internalStorage <= 16000 - getItemBurnTime(this.inventory[2]) && getItemBurnTime(this.inventory[2]) != 0)
 			{
 				this.itemFuel = getItemBurnTime(this.inventory[2]);
 				this.internalStorage += itemFuel;
 
 				if (this.internalStorage > 0) // If we got fuel get rid of the
 												// item
 				{
 					nu = true;
 
 					if (this.inventory[2] != null) {
 						--this.inventory[2].stackSize;
 
 						if (this.inventory[2].stackSize == 0) {
 							this.inventory[2] = this.inventory[2].getItem()
 									.getContainerItemStack(inventory[2]);
 						}
 					}
 				}
 			}
 
 			if (internalStorage >= 100 && this.canExtract()) // Smelt stuff
 			{
 				++this.progress;
 
 				if (this.progress == 20) {
 					this.progress = 0;
 					this.extractItem();
 					nu = true;
 				}
 			} else {
 				this.progress = 0;
 			}
 
 			if (gf != this.internalStorage > 0) {
 				nu = true;
 			}
 		}
 
 		if (nu) {
 			this.onInventoryChanged();
 		}
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
 		super.readFromNBT(par1nbtTagCompound);
 		internalStorage = par1nbtTagCompound.getInteger("powerLevel");
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
 		super.writeToNBT(par1nbtTagCompound);
 		par1nbtTagCompound.setInteger("powerLevel", internalStorage);
 	}
 
 	private boolean canExtract() {
 		if (this.inventory[0] == null) {
 			return false;
 		}
 		if (this.inventory[1] == null) {
 			return false;
 		} else {
 			if (inventory[1].itemID != Initializator.ItemEmptyEnergyPacket.itemID)
 				return false;
 			if (inventory[0].itemID == Initializator.ItemPositroniumCrystal.itemID
 					|| inventory[0].itemID == Initializator.ItemRadiumCrystal.itemID
 					|| inventory[0].itemID == Initializator.ItemGammatroniumCrystal.itemID
 					|| inventory[0].itemID == Initializator.ItemNeutriniumCrystal.itemID) {
 				return true;
 			}
 			return false;
 		}
 	}
 
 	public ItemStack getResult(ItemStack inp) {
 		if (inp.itemID == Initializator.ItemPositroniumCrystal.itemID) {
 			return new ItemStack(Initializator.ItemPositroniumEnergyPacket,
 					1);
 		}
 		if (inp.itemID == Initializator.ItemRadiumCrystal.itemID) {
 			return new ItemStack(Initializator.ItemRadiumEnergyPacket, 1);
 		}
 		if (inp.itemID == Initializator.ItemGammatroniumCrystal.itemID) {
 			return new ItemStack(
 					Initializator.ItemGammatroniumEnergyPacket, 1);
 		}
 		if (inp.itemID == Initializator.ItemNeutriniumCrystal.itemID) {
 			return new ItemStack(Initializator.ItemNeutriniumEnergyPacket, 1);
 		}
 		return null;
 
 	}
 
 	public void extractItem() {
 		if (this.canExtract()) {
 
 			internalStorage -= 100;
 
 			ItemStack var1 = this.getResult(this.inventory[0]);
 
 			if (this.inventory[4] == null) {
 				this.inventory[4] = var1.copy();
 			} else if (this.inventory[4].isItemEqual(var1)) {
 				inventory[4].stackSize += var1.stackSize;
 			}
 			if (this.inventory[3] == null) {
 				this.inventory[3] = new ItemStack(
 						Initializator.ItemDepletedCrystal, 1);
 			} else if (this.inventory[3].isItemEqual(new ItemStack(
 					Initializator.ItemDepletedCrystal, 1))) {
 				inventory[3].stackSize += var1.stackSize;
 			}
 
 			--this.inventory[0].stackSize;
 
 			if (this.inventory[0].stackSize <= 0) {
 				this.inventory[0] = null;
 			}
 
 			--this.inventory[1].stackSize;
 
 			if (this.inventory[1].stackSize <= 0) {
 				this.inventory[1] = null;
 			}
 		}
 	}
 
 
 }
