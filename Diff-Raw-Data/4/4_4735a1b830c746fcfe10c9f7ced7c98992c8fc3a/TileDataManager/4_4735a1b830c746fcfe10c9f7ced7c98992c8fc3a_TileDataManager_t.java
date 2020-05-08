 package com.isocraft.tileentity;
 
 import java.util.Map;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 
 import com.google.common.collect.ImmutableMap;
 import com.isocraft.block.BlockDataManager;
 import com.isocraft.item.ISOCraftItem;
 import com.isocraft.lib.BlockInfo;
 import com.isocraft.lib.Strings;
 import com.isocraft.network.PacketTypeHandler;
 import com.isocraft.network.packets.PacketClientDisplay;
 import com.isocraft.thesis.ThesisSystem;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 /**
  * ISOCraft
  * 
  * Tile Entity class for the Data Manager
  * 
  * @author Turnermator13
  */
 
 public class TileDataManager extends TileEntityISOCraftMachine {
 
 	public static int slots = 3;
 	public static final Map<Integer, AdvancedDataSlotInfo> advancedSlots = ImmutableMap.of(0, new AdvancedDataSlotInfo(0, "Slot for PDA or Disk"), 1, new AdvancedDataSlotInfo(1, "Slot for Item to be Copied or Digitized"));
 
 	public int state = 0;
 	private boolean change = false;
 	private int ticker = 0;
 	private boolean guiOpen = false;
 
 	@SideOnly(Side.CLIENT)
 	public boolean guiChange;
 
 	private int modeNo = 4;
 	private boolean[] modeState = new boolean[] { true, false, false, false };
 	public int modeSelected = 0;
 	public int modesAvalible = 1;
 
 	public int overridePing = 0;
 	public int overridePingTicker = 0;
 
 	public int CpyType;
 	public int CpyProgress = 0;
 	private ItemStack prevCpyStack = null;
 	private ItemStack CpyStack = null;
 	private Player CpyPlayer = null;
 
 	private int idleEnergy = 1;
 	private int CpyEnergy = 1200;
 	private int CpyTicks = 100;
 
 	public TileDataManager() {
 		super(slots, BlockInfo.DataManager_tileentity, advancedSlots, 12000);
 	}
 
 	@Override
 	public void updateEntity() {
 		super.updateEntity();
 		++this.ticker;
 
 		if (!this.worldObj.isRemote) {
 			if (this.ticker == 40) {
 				this.ticker = 0;
 				if (this.getEnergyStored(null) > 0 && this.CpyProgress > 0) {
 					this.state = 2;
 					this.change = true;
 				}
 				else if (this.getEnergyStored(null) > 0) {
 					this.state = 1;
 					this.change = true;
 				}
 				else {
 					this.state = 0;
 					this.change = true;
 				}
 			}
 
 			if (this.guiOpen) {
 				this.extractEnergy(null, this.idleEnergy, false);
 			}
 
 			if (this.change) {
 				BlockDataManager.updateState(this.state, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
 				this.change = false;
 			}
 
 			if (this.overridePing == 1) {
 				if (this.overridePingTicker == 2) {
 					this.overridePing = 0;
 					this.overridePingTicker = 0;
 				}
 				else {
 					++this.overridePingTicker;
 				}
 			}
 
 			if (this.CpyProgress == (1000 + this.CpyTicks)) {
 				this.setInventorySlotContents(0, this.CpyStack);
 				this.sendReleventData(this.CpyPlayer);
 				this.CpyPlayer = null;
 				this.overridePing = 1;
 				this.CpyProgress = 0;
				this.modeSelected = 0;
 				
				if (this.getStackInSlot(1) != null && ((ISOCraftItem) this.getStackInSlot(1).getItem()).getDestroyOnRead() && this.CpyType == 0){
 					this.setInventorySlotContents(1, null);
 				}
 			}
 			else if (this.CpyProgress == 2050) {
 				this.CpyPlayer = null;
 				this.overridePing = 1;
 				this.CpyProgress = 0;
 			}
 			else if (this.CpyProgress == (1000 + this.CpyTicks / 2)) {
 				if (this.CpyStack.equals(this.prevCpyStack)) {
 					this.CpyProgress = 2001;
 				}
 			}
 
 			if (this.CpyProgress > 0) {
 				++this.CpyProgress;
 				this.extractEnergy(null, (this.CpyEnergy / this.CpyTicks), false);
 			}
 		}
 	}
 
 	public void cpy(String peram, Player player, String args) {
 		if (this.CpyProgress == 0) {
 			if (peram.contains("-")) {
 				String[] parts = peram.split("-");
 				String thesisRef = parts[0];
 				String theoremRef = parts[1];
 
 				ItemStack iStack = null;
 				ItemStack stackSelected = null;
 				boolean reWrite = false;
 				boolean pass = true;
 
 				if (this.getEnergyStored(null) < this.CpyEnergy) {
 					pass = false;
 				}
 				else {
 					if (this.modeSelected == 1 || this.modeSelected == 2) {
 						if (this.modeSelected == 1) {
 							stackSelected = this.getStackInSlot(0);
 						}
 						else if (this.modeSelected == 2) {
 							stackSelected = this.getStackInSlot(1);
 						}
 						reWrite = ISOCraftItem.ISODataList.get(stackSelected.getItem().itemID).getReWritable();
 					}
 
 					if (args.equals("-c")) {
 						iStack = ThesisSystem.addTheoremToItem(this.getStackInSlot(0), ThesisSystem.getThesisFromReference(thesisRef), ThesisSystem.getThesisFromReference(thesisRef).getTheoremFromReference(theoremRef));
 						this.CpyType = 0;
 					}
 					else if (args.equals("-d")) {
 						if (reWrite) {
 							iStack = ThesisSystem.deleteTheoremFromItem(this.getStackInSlot(0), ThesisSystem.getThesisFromReference(thesisRef), ThesisSystem.getThesisFromReference(thesisRef).getTheoremFromReference(theoremRef));
 							this.CpyType = 1;
 						}
 						else {
 							pass = false;
 						}
 					}
 				}
 
 				if (pass) {
 					this.prevCpyStack = this.getStackInSlot(0);
 					this.CpyStack = iStack;
 					this.CpyPlayer = player;
 
 					this.CpyProgress = 1000;
 				}
 
 			}
 			else {
 				throw new IllegalArgumentException("String " + peram + " does not contain -");
 			}
 		}
 	}
 
 	public void cycleMode(Player player) {
 		int nextMode = this.modeSelected + 1;
 		boolean flag = false;
 
 		while (!flag) {
 			if (nextMode > (this.modeNo - 1)) {
 				nextMode = 0;
 			}
 			else if (!this.modeState[nextMode]) {
 				++nextMode;
 			}
 			else {
 				this.modeSelected = nextMode;
 				flag = true;
 			}
 		}
 		this.sendReleventData(player);
 	}
 
 	private boolean modeCheck() {
 		boolean ret = false;
 
 		if (!this.modeState[this.modeSelected]) {
 			ret = false;
 		}
 		else {
 			ret = true;
 		}
 		return ret;
 	}
 
 	private int modesAvalible() {
 		int ret = 0;
 		for (int i = 0; i < this.modeNo; ++i) {
 			if (this.modeState[i]) {
 				++ret;
 			}
 		}
 		return ret;
 	}
 
 	private void modeManager() {
 		if (this.getStackInSlot(0) != null && this.getStackInSlot(0).stackTagCompound != null && !this.getStackInSlot(0).stackTagCompound.getCompoundTag(Strings.Thesis_name).hasNoTags()) {
 			this.modeState[1] = true;
 		}
 		else {
 			this.modeState[1] = false;
 		}
 
 		if (this.getStackInSlot(1) != null && this.getStackInSlot(1).stackTagCompound != null && !this.getStackInSlot(1).stackTagCompound.getCompoundTag(Strings.Thesis_name).hasNoTags()) {
 			this.modeState[2] = true;
 		}
 		else {
 			this.modeState[2] = false;
 		}
 
 		if (this.getStackInSlot(0) != null && this.getStackInSlot(1) != null && this.getStackInSlot(1).stackTagCompound != null && !this.getStackInSlot(1).stackTagCompound.getCompoundTag(Strings.Thesis_name).hasNoTags()) {
 			this.modeState[3] = true;
 		}
 		else {
 			this.modeState[3] = false;
 		}
 	}
 
 	private void sendReleventData(Player player) {
 		if (this.modeSelected != 0) {
 			if (!this.worldObj.isRemote) {
 				if (this.modeSelected == 1 && this.getStackInSlot(0).stackTagCompound != null && !this.getStackInSlot(0).stackTagCompound.getCompoundTag(Strings.Thesis_name).hasNoTags()) {
 					PacketDispatcher.sendPacketToPlayer(PacketTypeHandler.writePacket(new PacketClientDisplay(this.getStackInSlot(0).stackTagCompound.getCompoundTag(Strings.Thesis_name))), player);
 				}
 				else if (this.modeSelected == 2 && this.getStackInSlot(1).stackTagCompound != null && !this.getStackInSlot(1).stackTagCompound.getCompoundTag(Strings.Thesis_name).hasNoTags()) {
 					PacketDispatcher.sendPacketToPlayer(PacketTypeHandler.writePacket(new PacketClientDisplay(this.getStackInSlot(1).stackTagCompound.getCompoundTag(Strings.Thesis_name))), player);
 				}
 				else if (this.modeSelected == 3 && this.getStackInSlot(1).stackTagCompound != null && !this.getStackInSlot(1).stackTagCompound.getCompoundTag(Strings.Thesis_name).hasNoTags()) {
 					PacketDispatcher.sendPacketToPlayer(PacketTypeHandler.writePacket(new PacketClientDisplay(this.getStackInSlot(1).stackTagCompound.getCompoundTag(Strings.Thesis_name))), player);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onInventoryChanged() {
 		if (this.guiOpen) {
 			this.modeManager();
 			this.modesAvalible = this.modesAvalible();
 			if (!this.modeCheck()) {
 				this.overridePing = 1;
 				this.modeSelected = 0;
 			}
 		}
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound nbtTagCompound) {
 		super.readFromNBT(nbtTagCompound);
 		this.state = nbtTagCompound.getInteger("state");
 
 		this.modeSelected = 0;
 		this.modeManager();
 		this.change = true;
 
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbtTagCompound) {
 		super.writeToNBT(nbtTagCompound);
 		nbtTagCompound.setInteger("state", this.state);
 	}
 
 	public void openChest(Player player) {
 		this.openChest();
 		this.overridePing = 1;
 		this.overridePingTicker = 0;
 		this.modesAvalible = this.modesAvalible();
 		this.modeManager();
 		this.sendReleventData(player);
 	}
 
 	@Override
 	public void openChest() {
 		this.guiOpen = true;
 
 		this.modeSelected = 0;
 	}
 
 	@Override
 	public void closeChest() {
 		this.guiOpen = false;
 	}
 }
