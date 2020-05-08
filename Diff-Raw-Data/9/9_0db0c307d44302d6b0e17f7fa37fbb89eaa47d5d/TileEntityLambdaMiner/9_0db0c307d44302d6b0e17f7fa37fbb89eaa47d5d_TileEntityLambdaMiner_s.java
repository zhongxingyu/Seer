 package entanglecraft.blocks;
 
 import java.io.DataInputStream;
 import java.util.ArrayList;
 import java.lang.reflect.Field;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.EntityItem;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EnumSkyBlock;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.InventoryBasic;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.NBTTagList;
 import net.minecraft.src.INetworkManager;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.TileEntityChest;
 import net.minecraft.src.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.ISidedInventory;
 import entanglecraft.DistanceHandler;
 import entanglecraft.EntangleCraft;
 import entanglecraft.ServerPacketHandler;
 import entanglecraft.SoundHandling.LambdaSoundHandler;
 import entanglecraft.items.EntangleCraftItems;
 
 public class TileEntityLambdaMiner extends TileEntity implements IInventory, ISidedInventory {
	public int channel = 0;
 	public int processTime;
 	public int layerToMine;
 	public int[] blockCoords;
 	public int blockCost = 32;
 	private ArrayList<int[]> layerStructure;
 	private ArrayList<Integer> filteredIds;
 	private ItemStack[] lMItemStacks = new ItemStack[11];
 	private int speedMultiplier = 1;
 	private int[] chest;
 	private int chestRecursion = 0;
 	private int[] lastStructStackSizes = new int[] { 0, 0, 0 };
 	private boolean filterInclusive = true;
 	private boolean isMining = false;
 
 	public TileEntityLambdaMiner(int channel) {
 		this.channel = channel;
 	}
 
 	public TileEntityLambdaMiner() {
 	}
 
 	public void setLayerStructure(ArrayList newStructure) {
 		this.layerStructure = new ArrayList<int[]>();
 		this.layerStructure = newStructure;
 	}
 
 	public void setBlockCoords(int[] coords) {
 		this.blockCoords = new int[] { coords[0], coords[1], coords[2] };
 	}
 
 	public void setChest(int[] coords) {
 		if (coords != null) {
 			this.chest = new int[] { coords[0], coords[1], coords[2] };
 		} else
 			this.chest = null;
 	}
 
 	public void checkForChest() {
 		int id = Block.chest.blockID;
 		World world = this.worldObj;
 		int x = this.blockCoords[0];
 		int y = this.blockCoords[1];
 		int z = this.blockCoords[2];
 		{
 			if (world.getBlockId(x + 1, y, z) == id) {
 				setChest(new int[] { x + 1, y, z });
 			} else if (world.getBlockId(x - 1, y, z) == id) {
 				setChest(new int[] { x - 1, y, z });
 			} else if (world.getBlockId(x, y, z + 1) == id) {
 				setChest(new int[] { x, y, z + 1 });
 			} else if (world.getBlockId(x, y, z - 1) == id) {
 				setChest(new int[] { x, y, z - 1 });
 			} else
 				setChest(null);
 		}
 	}
 
 	public int[] getChest() {
 		if (this.chest == null) {
 			return null;
 		} else
 			return this.chest;
 	}
 
 	public ArrayList generateLine(int size, int[] start) {
 		ArrayList struct = new ArrayList();
 		for (int i = 0; i < size; i++) {
 			int x = start[0];
 			x += i;
 			struct.add(new int[] { x, start[1], start[2] });
 		}
 		return struct;
 	}
 
 	public void generateLayerStructure(int size, int[] start) {
 		if (!this.worldObj.isRemote) {
 			ArrayList struct = new ArrayList();
 			struct = generateLine(size, start);
 			setLayerStructure(struct);
 		}
 	}
 
 	public void generateLayerStructure() {
 		if (!this.worldObj.isRemote) {
 			ArrayList theStruct = new ArrayList();
 			ArrayList structLeft = new ArrayList();
 			ArrayList struct = new ArrayList();
 			ArrayList structRight = new ArrayList();
 			int scanLeft = lMItemStacks[0] != null ? (lMItemStacks[0].itemID == new ItemStack(EntangleCraftItems.ItemTransmitter, 1).itemID) ? lMItemStacks[0].stackSize
 					: 0
 					: 0;
 			int scanForward = lMItemStacks[1] != null ? (lMItemStacks[1].itemID == new ItemStack(EntangleCraftItems.ItemTransmitter, 1).itemID) ? lMItemStacks[1].stackSize + 1
 					: 1
 					: 1;
 			int scanRight = lMItemStacks[2] != null ? (lMItemStacks[2].itemID == new ItemStack(EntangleCraftItems.ItemTransmitter, 1).itemID) ? lMItemStacks[2].stackSize
 					: 0
 					: 0;
 			this.lastStructStackSizes = new int[] { scanLeft, scanForward, scanRight };
 			System.out.println("Last struct set to " + scanLeft + " to the left, " + scanForward + " forward, and " + scanRight + " to the right");
 			for (int i = 1; i <= scanLeft; i++) {
 				ArrayList temp;
 				temp = generateLine(scanForward, new int[] { blockCoords[0] + 1, blockCoords[1], blockCoords[2] - i });
 				for (Object block : temp) {
 					structLeft.add((int[]) block);
 				}
 			}
 
 			struct = generateLine(scanForward, new int[] { blockCoords[0] + 1, blockCoords[1], blockCoords[2] });
 
 			for (int i = 1; i <= scanRight; i++) {
 				ArrayList temp;
 				temp = generateLine(scanForward, new int[] { blockCoords[0] + 1, blockCoords[1], blockCoords[2] + i });
 				for (Object block : temp) {
 					structRight.add((int[]) block);
 				}
 			}
 			for (Object block : structLeft) {
 				theStruct.add((int[]) block);
 			}
 			for (Object block : struct) {
 				theStruct.add((int[]) block);
 			}
 			for (Object block : structRight) {
 				theStruct.add((int[]) block);
 			}
 			setLayerStructure(theStruct);
 		}
 	}
 
 	public void readFromNBT(NBTTagCompound nbt) {
 		super.readFromNBT(nbt);
 		int shouldLoad = (int) nbt.getShort("shouldLoad");
 
 		if (shouldLoad == -1) {
 			this.channel = (int) nbt.getShort("channel");
 			this.layerToMine = (int) nbt.getShort("layerToMine");
 			this.processTime = (int) nbt.getShort("processTime");
 			this.speedMultiplier = (int) nbt.getShort("speedMultiplier");
 			this.blockCost = (int) nbt.getShort("blockCost");
 			setBlockCoords(nbt.getIntArray("blockCoords"));
 			this.chest = nbt.getIntArray("chest");
 			this.readFiltersFromNBT(nbt);
 			int size = (int) nbt.getShort("layerStructureSize");
 			this.filterInclusive = nbt.getBoolean("filterInclusive");
 			this.isMining = nbt.getBoolean("isMining");
 			readLayerStructFromNBT(nbt, size);
 
 			NBTTagList tagList = nbt.getTagList("Items");
 			this.lMItemStacks = new ItemStack[this.getSizeInventory()];
 
 			for (int i = 0; i < tagList.tagCount(); ++i) {
 				NBTTagCompound itemStack = (NBTTagCompound) tagList.tagAt(i);
 				byte thisByte = itemStack.getByte("Slot");
 				if (thisByte >= 0 && thisByte < this.lMItemStacks.length) {
 					this.lMItemStacks[thisByte] = ItemStack.loadItemStackFromNBT(itemStack);
 				}
 			}
 		}
 	}
 
 	private void writeFieldToNBT(NBTTagCompound nbt, String field, Object o, String name) {
 		if (field == "int[]") {
 			int[] theArray = (int[]) o;
 			if (theArray != null) {
 				nbt.setIntArray(name, theArray);
 			}
 		}
 	}
 
 	public void writeToNBT(NBTTagCompound nbt) {
 		super.writeToNBT(nbt);
 		nbt.setShort("shouldLoad", (short) -1);
 		nbt.setShort("channel", (short) this.channel);
 		nbt.setShort("layerToMine", (short) this.layerToMine);
 		nbt.setShort("processTime", (short) this.processTime);
 		nbt.setShort("speedMultiplier", (short) this.speedMultiplier);
 		nbt.setShort("blockCost", (short) this.blockCost);
 		nbt.setIntArray("blockCoords", this.blockCoords);
 		this.writeFieldToNBT(nbt, "int[]", this.chest, "chest");
 		this.writeFiltersToNBT(nbt);
 		nbt.setShort("layerStructureSize", (short) (this.layerStructure.size()));
 		nbt.setBoolean("filterInclusive", this.filterInclusive);
 		nbt.setBoolean("isMining", this.isMining);
 
 		writeLayerStructToNBT(nbt);
 
 		NBTTagList tagList = new NBTTagList();
 		for (int var3 = 0; var3 < this.lMItemStacks.length; ++var3) {
 			if (this.lMItemStacks[var3] != null) {
 				NBTTagCompound var4 = new NBTTagCompound();
 				var4.setByte("Slot", (byte) var3);
 				this.lMItemStacks[var3].writeToNBT(var4);
 				tagList.appendTag(var4);
 			}
 		}
 
 		nbt.setTag("Items", tagList);
 	}
 
 	private void writeLayerStructToNBT(NBTTagCompound nbt) {
 		Integer counter = 0;
 		for (Object block : layerStructure) {
 			int[] intBlock = (int[]) block;
 			nbt.setIntArray("layerStructure" + counter, intBlock);
 			counter++;
 		}
 	}
 
 	private void writeFiltersToNBT(NBTTagCompound nbt) {
 		if (filteredIds != null) {
 			Integer counter = 0;
 			nbt.setInteger("numOfFilteredIds", this.filteredIds.size());
 			for (Object ID : filteredIds) {
 				Integer intID = (Integer) ID;
 				nbt.setInteger("filteredIds" + counter, intID);
 				counter++;
 			}
 		}
 	}
 
 	private void readFiltersFromNBT(NBTTagCompound nbt) {
 		Integer counter = 0;
 		this.filteredIds = new ArrayList<Integer>();
 		int size = nbt.getInteger("numOfFilteredIds");
 		while (counter < size) {
 			filteredIds.add(nbt.getInteger("filteredIds" + counter));
 			counter++;
 		}
 	}
 
 	private boolean isFiltering() {
 		boolean isFiltering = false;
 		if (this.lMItemStacks[10] != null) {
 			isFiltering = this.lMItemStacks[10].itemID == new ItemStack(EntangleCraftItems.ItemInclusiveFilter, 1).itemID
 					|| this.lMItemStacks[10].itemID == new ItemStack(EntangleCraftItems.ItemExclusiveFilter, 1).itemID;
 		}
 		return isFiltering;
 	}
 
 	private void readLayerStructFromNBT(NBTTagCompound nbt, int size) {
 		this.layerStructure = new ArrayList<int[]>();
 		ArrayList<int[]> temp = new ArrayList<int[]>();
 		for (int i = 0; i < size; i++) {
 			temp.add(nbt.getIntArray("layerStructure" + i));
 		}
 		this.layerStructure = temp;
 	}
 
 	public void recieveUpdateFromServer(INetworkManager network, DataInputStream dataStream, String fieldName) throws IllegalArgumentException,
 			IllegalAccessException {
 		if (worldObj.isRemote) {
 			Class<?> c = this.getClass();
 			Field theField = null;
 			boolean shouldUpdate = true;
 			try {
 				theField = c.getDeclaredField(fieldName);
 				if (theField != null)
 					theField.setAccessible(true);
 			} catch (NoSuchFieldException e1) {
 				shouldUpdate = false;
 				e1.printStackTrace();
 			} catch (SecurityException e1) {
 				shouldUpdate = false;
 				e1.printStackTrace();
 			}
 
 			if (shouldUpdate) {
 				if (theField.get(this) instanceof Integer) {
 					try {
 						int fieldValue = dataStream.readInt();
 						theField.setInt(this, fieldValue);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 
 				else if (theField.get(this) instanceof int[]) {
 					try {
 						int arrSize = dataStream.readInt();
 						int[] fieldValue = new int[arrSize];
 						for (int i = 0; i < arrSize; i++) {
 							fieldValue[i] = dataStream.readInt();
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					;
 				}
 
 				else if (fieldName == "filterInclusive") {
 					try {
 						boolean theValue = dataStream.readBoolean();
 						theField.setBoolean(this, theValue);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 
 				else if (fieldName.equals("isMining")) {
 					try {
 						boolean theValue = dataStream.readBoolean();
 						theField.setBoolean(this, theValue);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 			else {
 			}
 		}
 	}
 
 	public int getStartInventorySide(ForgeDirection side) {
 		return 0;
 	}
 
 	@Override
 	public int getSizeInventorySide(ForgeDirection side) {
 		return 1;
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return this.lMItemStacks.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int var1) {
 		return this.lMItemStacks[var1];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slot, int decreaseAmount) {
 		if (this.lMItemStacks[slot] != null) {
 			ItemStack itemStack;
 
 			if (this.lMItemStacks[slot].stackSize <= decreaseAmount) {
 				itemStack = this.lMItemStacks[slot];
 				this.lMItemStacks[slot] = null;
 				return itemStack;
 			} else {
 				itemStack = this.lMItemStacks[slot].splitStack(decreaseAmount);
 
 				if (this.lMItemStacks[slot].stackSize == 0) {
 					this.lMItemStacks[slot] = null;
 				}
 
 				return itemStack;
 			}
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot) {
 
 		if (this.lMItemStacks[slot] != null) {
 			ItemStack itemStack = this.lMItemStacks[slot];
 			this.lMItemStacks[slot] = null;
 			return itemStack;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack itemStack) {
 		this.lMItemStacks[slot] = itemStack;
 
 		if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
 			itemStack.stackSize = this.getInventoryStackLimit();
 		}
 	}
 
 	public void updateEntity() {
 
 		super.updateEntity();
 		if (hasStructureChanged())
 			generateLayerStructure();
 		boolean mined = false;
 		boolean isServer = !worldObj.isRemote;
 		{
 			if (canSmelt()) {
 				++this.processTime;
 				int goal = 200 / this.speedMultiplier;
 
 				if (this.isMining == false && isServer) { // Updating the
 															// texture, though
 															// not implemented
 															// yet
 					this.setIsMining(true);
 				}
 
 				if (this.processTime == goal || this.processTime > goal && isServer) {
 					this.processTime = 0;
 					ServerPacketHandler.sendTEFieldUpdate(this, "TileEntityLambdaMiner", "processTime");
 					this.smeltItem();
 
 					mined = true;
 				}
 			} else {
 				if (isServer) {
 					if (this.isMining)
 						this.setIsMining(false);
 				}
 			}
 		}
 
 		if (mined) {
 		}
 
 	}
 
 	private void setMetaData(int i) {
 		int x, y, z;
 		if (this.blockCoords != null) {
 			x = this.blockCoords[0];
 			y = this.blockCoords[1];
 			z = this.blockCoords[2];
 		} else {
 			x = this.xCoord;
 			y = this.yCoord;
 			z = this.zCoord;
 		}
 		int lightValue = i == 0 ? 0 : 1000;
 		this.worldObj.setLightValue(EnumSkyBlock.Block, x, y, z, lightValue);
 		this.worldObj.setBlockMetadataWithNotify(x, y, z, i);
 	}
 
 	public void setIsMining(boolean a) {
 		if (!worldObj.isRemote){
 			if (this.isMining != a) {
 				this.isMining = a;
 				int i = 0;
 				if (a)
 					i = 1;
 				this.setMetaData(i);
 			}
 			ServerPacketHandler.sendTEFieldUpdate(this, "TileEntityLambdaMiner", "isMining");
 		}
 	}
 
 	private void smeltItem() {
 		if (!worldObj.isRemote) {
 			String minerSound = "minerFail";
 			float minerSoundPitch = 1F - (((float) this.blockCoords[1] - (float) this.layerToMine) / (float) this.blockCoords[1]) / 4F;
 			if (minerSoundPitch > 1F)
 				minerSoundPitch = 1F;
 			if (minerSoundPitch < 0.05F)
 				minerSoundPitch = 0.05F;
 			int x, y, z;
 			x = this.blockCoords[0];
 			y = layerToMine;
 			z = this.blockCoords[2];
 
 			InventoryBasic reward = prepareToMine();
 			int counter = 0;
 			for (counter = 0; counter < reward.getSizeInventory(); counter++) {
 				ItemStack itemStack = reward.getStackInSlot(counter);
 
 				if (itemStack != null) {
 					minerSound = itemStack.itemID == new ItemStack(Item.diamond, 1).itemID ? "teleport" : "mineProcess";
 					minerSoundPitch = itemStack.itemID == new ItemStack(Item.diamond, 1).itemID ? 1F : minerSoundPitch;
 					checkForChest();
 					if (getChest() != null) {
 						TileEntityChest theChest = (TileEntityChest) this.worldObj.getBlockTileEntity(chest[0], chest[1], chest[2]);
 						addStackToInventory(theChest, itemStack);
 					}
 
 					else {
 						EntityItem e = new EntityItem(this.worldObj, (double) this.blockCoords[0] + 0.5, (double) this.blockCoords[1] + 1.5,
 								(double) this.blockCoords[2] + 0.5, itemStack);
 						e.dropItem(itemStack.itemID, itemStack.stackSize);
 					}
 
 				}
 			}
 			LambdaSoundHandler.playSound(this.worldObj, minerSound, new double[] { (double) xCoord, (double) yCoord, (double) zCoord },
 					this.worldObj.rand.nextFloat() * 0.05F + 0.05F, minerSoundPitch);
 			
 			LambdaSoundHandler.playSound(this.worldObj, minerSound, new double[] { (double) xCoord, (double) this.layerToMine, (double) zCoord },
 					this.worldObj.rand.nextFloat() * 0.2F + 0.7F, minerSoundPitch);
 		}
 	}
 
 	private InventoryBasic prepareToMine() {
 		InventoryBasic inv = new InventoryBasic(null, layerStructure.size());
 		for (Object block : this.layerStructure) {
 			if (DistanceHandler.getDistance(channel) >= this.blockCost) {
 				int[] blockCoords = (int[]) block;
 				int blockID = processBlock(this.worldObj, blockCoords[0], this.layerToMine, blockCoords[2]);
 				if (blockID != 0) {
 					ItemStack result = this.getItemStackFromID(blockID);
 
 					if (result != null) {
 						addStackToInventory(inv, this.getItemStackFromID(blockID));
 						DistanceHandler.subtractDistance(channel, this.blockCost);
 					}
 				}
 			} else
 				break;
 		}
 		if (DistanceHandler.getDistance(channel) >= this.blockCost) {
 			this.layerToMine -= 1;
 			ServerPacketHandler.sendTEFieldUpdate(this, "TileEntityLambdaMiner", "layerToMine");
 		}
 		return inv;
 	}
 
 	@Override
 	public void onInventoryChanged() {
 		super.onInventoryChanged();
 
 		if (!worldObj.isRemote) {
 			// Handling the 3 layer Generation slots
 			if (hasStructureChanged()) {
 			}
 
 			// Handling the SpeedMultiplier slot
 			if (this.lMItemStacks[3] != null) {
 				if (this.lMItemStacks[3].itemID == new ItemStack(EntangleCraftItems.ItemInductionCircuit, 1).itemID) {
 					setSpeedMultiplier(2);
 				} else if (this.lMItemStacks[3].itemID == new ItemStack(EntangleCraftItems.ItemSuperInductionCircuit, 1).itemID) {
 					setSpeedMultiplier(5);
 				}
 			} else
 				this.speedMultiplier = 1;
 
 			// Handling the filter slots
 			this.filteredIds = new ArrayList();
 			if (isFiltering()) {
 				this.filterInclusive = this.lMItemStacks[10].itemID == new ItemStack(EntangleCraftItems.ItemInclusiveFilter, 1).itemID;
 				for (int i = 4; i < 10; i++) {
 					if (this.lMItemStacks[i] != null) {
 						this.filteredIds.add(this.getBlockIDFromItem(lMItemStacks[i].itemID));
 					}
 				}
 			} else
 				this.filteredIds = null;
 			generateBlockCost();
 		}
 	}
 
 	private boolean hasStructureChanged() {
 		boolean hasChanged = false;
 		int scanLeft = lMItemStacks[0] != null ? (lMItemStacks[0].itemID == new ItemStack(EntangleCraftItems.ItemTransmitter, 1).itemID) ? lMItemStacks[0].stackSize
 				: 0
 				: 0;
 		int scanForward = lMItemStacks[1] != null ? (lMItemStacks[1].itemID == new ItemStack(EntangleCraftItems.ItemTransmitter, 1).itemID) ? lMItemStacks[1].stackSize + 1
 				: 1
 				: 1;
 		int scanRight = lMItemStacks[2] != null ? (lMItemStacks[2].itemID == new ItemStack(EntangleCraftItems.ItemTransmitter, 1).itemID) ? lMItemStacks[2].stackSize
 				: 0
 				: 0;
 		int[] temp = new int[] { scanLeft, scanForward, scanRight };
 		for (int i = 0; i < 3; i++) {
 			hasChanged = hasChanged || temp[i] != this.lastStructStackSizes[i];
 		}
 		return hasChanged || this.lastStructStackSizes[1] == 0;
 	}
 
 	public void setSpeedMultiplier(int i) {
 		if (!worldObj.isRemote) {
 			this.speedMultiplier = i;
 			System.out.println("speed multiplier is now " + i);
 			ServerPacketHandler.sendTEFieldUpdate(this, "TileEntityLambdaMiner", "speedMultiplier");
 		}
 	}
 
 	private void generateBlockCost() {
 		int cost = 32;
 		if (this.isFiltering()) {
 			if (this.filterInclusive) {
 				cost = 0;
 				for (Object id : filteredIds) {
 					Integer intID = (Integer) id;
 					cost += getInclusiveCost(intID);
 				}
 			} else {
 				cost = 32;
 				for (Object id : filteredIds) {
 					Integer intID = (Integer) id;
 					cost += getExclusiveCost(intID);
 				}
 			}
 		}
 
 		this.blockCost = cost;
 	}
 
 	private int getBlockIDFromItem(int id) {
 		int result = id;
 		if (id == new ItemStack(Item.diamond, 1).itemID) {
 			result = Block.oreDiamond.blockID;
 		} else if (id == new ItemStack(Item.coal).itemID) {
 			result = Block.oreCoal.blockID;
 		} else if (id == new ItemStack(Item.redstone).itemID) {
 			result = Block.oreRedstone.blockID;
 		} else if (id == new ItemStack(Item.dyePowder, 1, 4).itemID) {
 			result = Block.blockLapis.blockID;
 		} else if (id == Block.cobblestone.blockID) {
 			result = Block.stone.blockID;
 		}
 
 		return result;
 	}
 
 	private int getInclusiveCost(int itemID) {
 		int cost = 4;
 		if (itemID == new ItemStack(Item.diamond, 1).itemID || itemID == Block.oreDiamond.blockID) {
 			cost = 1024;
 		} else if (itemID == Block.oreIron.blockID || itemID == Block.oreCoal.blockID || itemID == Block.oreRedstone.blockID || itemID == Block.oreGold.blockID
 				|| itemID == Block.oreLapis.blockID) {
 			cost = 256;
 		}
 		return cost;
 	}
 
 	private int getExclusiveCost(int itemID) {
 		int cost = 64;
 		if (itemID == new ItemStack(Item.diamond, 1).itemID || itemID == Block.oreDiamond.blockID) {
 			cost = 0;
 		} else if (itemID == Block.oreIron.blockID || itemID == Block.oreCoal.blockID || itemID == Block.oreRedstone.blockID || itemID == Block.oreGold.blockID
 				|| itemID == Block.oreLapis.blockID) {
 			cost = 16;
 		} else if (itemID == Block.dirt.blockID || itemID == Block.sand.blockID) {
 			cost = 128;
 		}
 		return cost;
 	}
 
 	private ItemStack getItemStackFromID(int blockID) {
 		ItemStack result = null;
 		if (canMine(blockID)) {
 			if (blockID == Block.stone.blockID) {
 				result = new ItemStack(Block.cobblestone, 1);
 			} else if (blockID == Block.waterMoving.blockID || blockID == Block.waterStill.blockID || blockID == Block.lavaStill.blockID
 					|| blockID == Block.lavaMoving.blockID) {
 				result = null;
 			} else if (blockID == Block.oreLapis.blockID) {
 				result = new ItemStack(Item.dyePowder, this.worldObj.rand.nextInt(1) + 4, 4);
 			} else if (blockID == Block.oreRedstone.blockID) {
 				result = new ItemStack(Item.redstone, this.worldObj.rand.nextInt(1) + 4);
 			} else if (blockID == Block.oreCoal.blockID) {
 				result = new ItemStack(Item.coal, 1);
 			} else if (blockID == Block.oreDiamond.blockID) {
 				result = new ItemStack(Item.diamond, 1);
 			} else
 				result = new ItemStack(Item.itemsList[blockID], 1);
 		}
 
 		return result;
 	}
 
 	private boolean canMine(int blockID) {
 		boolean canMine = true;
 		if (this.isFiltering()) {
 			if (this.filterInclusive && this.filteredIds != null) {
 				canMine = false; // Assume you can't mine it for inclusive
 									// filtering
 				for (Object id : this.filteredIds) {
 					Integer intID = (Integer) id;
 					if (intID == blockID) {
 						canMine = true;
 						break;
 					}
 				}
 			}
 
 			// this part deals with exclusive filters
 			else {
 				ArrayList<Integer> notMineable = new ArrayList<Integer>();
 				notMineable.add(Block.bedrock.blockID);
 				if (filteredIds != null) {
 
 					for (Object id : filteredIds) {
 						Integer intID = (Integer) id;
 						notMineable.add(intID);
 					}
 					for (Object id : notMineable) {
 						Integer intID = (Integer) id;
 						if (intID == blockID) {
 							canMine = false;
 							break;
 						}
 					}
 				}
 			}
 		} else {
 			canMine = blockID != Block.bedrock.blockID;
 		}
 		return canMine;
 	}
 
 	private void addStackToInventory(InventoryBasic inv, ItemStack itemStack) {
 		int counter = 0;
 		boolean invContainsItem = false;
 		boolean slotOccupied = false;
 		boolean overFlow = false;
 		if (itemStack != null) {
 			ItemStack overFlowStack = itemStack.copy();
 			for (counter = 0; counter < inv.getSizeInventory(); counter++) {
 				slotOccupied = inv.getStackInSlot(counter) != null;
 				if (slotOccupied) {
 					invContainsItem = inv.getStackInSlot(counter).itemID == itemStack.itemID;
 					invContainsItem = invContainsItem && inv.getStackInSlot(counter).stackSize < 64;
 					if (invContainsItem)
 						break;
 				} else {
 					break;
 				}
 			}
 
 			if (invContainsItem) {
 				int sizeOf = inv.getStackInSlot(counter).stackSize;
 				itemStack.stackSize = (itemStack.stackSize + sizeOf) % 64;
 				if (itemStack.stackSize < sizeOf) {
 					overFlow = true;
 					overFlowStack.stackSize = 64;
 				}
 				if (!overFlow)
 					inv.setInventorySlotContents(counter, itemStack);
 				else {
 					inv.setInventorySlotContents(counter, overFlowStack);
 					addStackToInventory(inv, itemStack);
 				}
 			} else {
 				inv.setInventorySlotContents(counter, itemStack);
 			}
 		}
 	}
 
 	private void addStackToInventory(TileEntityChest inv, ItemStack itemStack) {
 		this.chestRecursion += 1;
 		int counter = 0;
 		boolean invContainsItem = false;
 		boolean slotOccupied = false;
 		boolean overFlow = false;
 		boolean stackAdded = false;
 
 		if (itemStack != null) {
 			ItemStack overFlowStack = itemStack.copy();
 			for (counter = 0; counter < inv.getSizeInventory(); counter++) {
 				slotOccupied = inv.getStackInSlot(counter) != null;
 				if (slotOccupied) {
 					invContainsItem = inv.getStackInSlot(counter).itemID == itemStack.itemID;
 					invContainsItem = invContainsItem && inv.getStackInSlot(counter).stackSize < 64;
 					if (invContainsItem)
 						break;
 				} else {
 					break;
 				}
 			}
 
 			if (invContainsItem) {
 				int sizeOf = inv.getStackInSlot(counter).stackSize;
 				itemStack.stackSize = (itemStack.stackSize + sizeOf) % 64;
 				if (itemStack.stackSize < sizeOf) {
 					overFlow = true;
 					overFlowStack.stackSize = 64;
 				}
 				if (!overFlow) {
 					inv.setInventorySlotContents(counter, itemStack);
 					stackAdded = true;
 				} else {
 					inv.setInventorySlotContents(counter, overFlowStack);
 					addStackToInventory(inv, itemStack);
 					stackAdded = true;
 				}
 			} else if (!slotOccupied) {
 				inv.setInventorySlotContents(counter, itemStack);
 				stackAdded = true;
 			}
 
 			if (!stackAdded) {
 				if (chestRecursion < 3) {
 
 					if (inv.adjacentChestXNeg != null)
 						addStackToInventory(inv.adjacentChestXNeg, itemStack);
 					else if (inv.adjacentChestXPos != null)
 						addStackToInventory(inv.adjacentChestXPos, itemStack);
 					else if (inv.adjacentChestZNeg != null)
 						addStackToInventory(inv.adjacentChestZNeg, itemStack);
 					else if (inv.adjacentChestZPosition != null)
 						addStackToInventory(inv.adjacentChestZPosition, itemStack);
 					else {
 						EntityItem e = new EntityItem(this.worldObj, (double) this.blockCoords[0] + 0.5, (double) this.blockCoords[1] + 1.5,
 								(double) this.blockCoords[2] + 0.5, itemStack);
 						e.dropItem(itemStack.itemID, itemStack.stackSize);
 					}
 				} else {
 					EntityItem e = new EntityItem(this.worldObj, (double) this.blockCoords[0] + 0.5, (double) this.blockCoords[1] + 1.5,
 							(double) this.blockCoords[2] + 0.5, itemStack);
 					e.dropItem(itemStack.itemID, itemStack.stackSize);
 				}
 			}
 		}
 		this.chestRecursion = 0;
 	}
 
 	private int processBlock(World world, int x, int y, int z) {
 		int blockID = 0;
 		if (world.blockExists(x, y, z)) {
 			blockID = world.getBlockId(x, y, z);
 			if (this.canMine(blockID)) {
 				world.setBlockWithNotify(x, y, z, 0);
 			}
 		}
 		return blockID;
 	}
 
 	private boolean canSmelt() {
 		boolean canSmelt = true;
 		if (!this.worldObj.isRemote) {
 			if (DistanceHandler.getDistance(channel) < this.blockCost)
 				canSmelt = false;
 			if (this.layerToMine == 0)
 				canSmelt = false;
 			canSmelt = canSmelt && this.worldObj.isBlockIndirectlyGettingPowered(this.blockCoords[0], this.blockCoords[1], this.blockCoords[2]);
 		} else {
 			canSmelt = this.isMining;
 		}
 		return canSmelt;
 	}
 
 	public int getCookProgressScaled(int par1) {
 		int denominator = 200 / this.speedMultiplier;
 		return this.processTime * par1 / denominator;
 	}
 
 	@Override
 	public String getInvName() {
 		return "lMInventory";
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer var1) {
 		return true;
 	}
 
 	@Override
 	public void openChest() {
 
 	}
 
 	@Override
 	public void closeChest() {
 
 	}
 
 }
