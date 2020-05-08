 package bau5.mods.craftingsuite.common.tileentity;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.inventory.InventoryCraftResult;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import bau5.mods.craftingsuite.common.CSLogger;
 import bau5.mods.craftingsuite.common.ModificationNBTHelper;
 import bau5.mods.craftingsuite.common.inventory.EnumInventoryModifier;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 
 public class TileEntityProjectBench extends TileEntity implements IModifiedTileEntityProvider, IInventory, ISidedInventory{
 	
 	private NBTTagCompound modifiers;
 	private byte[]		   upgrades;
 	
 	public class LocalInventoryCrafting extends InventoryCrafting{
 		private TileEntity theTile;
 		public LocalInventoryCrafting() {
 			super(new Container(){
 				@Override
 				public boolean canInteractWith(EntityPlayer var1) {
 					return false;
 				}
 			}, 3, 3);
 		}
 		public LocalInventoryCrafting(TileEntity tileEntity){
 			this();
 			theTile = tileEntity;
 		}
 	}
 	
 	public ItemStack[] inv = null;
 	public ItemStack result;
 	public ItemStack[] tools = new ItemStack[3];
 	public int selectedToolIndex  = -1;
 	public int toolIndexInCrafting= -1;
 	private ItemStack lastResult = null;
 	public IInventory craftingResult = new InventoryCraftResult();
 	public LocalInventoryCrafting craftingMatrix = new LocalInventoryCrafting(this);
 	public boolean containerInit = false;
 	public boolean containerWorking = false;
 	public boolean sendRenderPacket = false;
 	public boolean initialized = false;
 	private boolean shouldUpdateOutput;
 	private int update = 0;
 	private byte directionFacing = 0;
 	public float randomShift = 0.0F;
 	
 	private HashMap<EnumInventoryModifier, int[]> inventoryMap = new HashMap();
 	
 	/**
 	 * Unlinked crafting matrix, used to test the difference between inventory changes,
 	 * avoid spam of searching the recipe list.
 	 */
 	private LocalInventoryCrafting lastCraftMatrix = new LocalInventoryCrafting();
 
 	public TileEntityProjectBench() {
 		modifiers = ModificationNBTHelper.getModifierTag(null);
 		upgrades = ModificationNBTHelper.newBytes();
 	}
 	
 	public ItemStack findRecipe(boolean fromPacket) {
 		if(worldObj == null)
 			return null;
 		long start = System.currentTimeMillis();
 		lastResult = result;
 		boolean toolIn = false;
 		ItemStack stack = null;
 		if(getStackInSlot(4) == null && selectedToolIndex != -1 && !toolIn && stack == null){
 			craftingMatrix.setInventorySlotContents(4, getSelectedTool());
 			toolIn = true;
 			toolIndexInCrafting = 4;
 		}
 		for(int i = 0; i < craftingMatrix.getSizeInventory(); ++i) 
 		{
 			stack = getStackInSlot(i);
 			if(i == 4 && (toolIn && toolIndexInCrafting == 4))
 				continue;
 			if(!toolIn && selectedToolIndex != -1 && stack == null){
 				craftingMatrix.setInventorySlotContents(i, getSelectedTool());
 				toolIn = true;
 				toolIndexInCrafting = i;
 			}
 			else
 				craftingMatrix.setInventorySlotContents(i, stack);
 		}
 	
 		ItemStack recipe = CraftingManager.getInstance().findMatchingRecipe(craftingMatrix, worldObj);
 //		if(recipe == null && validPlanInSlot() && haveSuppliesForPlan())
 //			recipe = getPlanResult();
 		setResult(recipe);
 		
 		if(!ItemStack.areItemStacksEqual(lastResult, result) && !fromPacket && !worldObj.isRemote)
 			sendRenderPacket = true;
 		long end = System.currentTimeMillis();
 		CSLogger.log("Recipe found on " + ((worldObj.isRemote) ? "client " : "server ") +"in " +(end - start) +" milliseconds.");
 		return recipe;
 		
 	}
 
 	@Override
 	public void initializeFromNBT(NBTTagCompound nbtTagCompound) {
 		modifiers = nbtTagCompound;
 		upgrades = modifiers.getByteArray(ModificationNBTHelper.upgradeArrayName);
 		initialized = true;
 	}
 
 	private void setResult(ItemStack recipe) {
 		result = recipe;
 		craftingResult.setInventorySlotContents(0, result);
 	}
 
 	@Override
 	public void handleModifiers() {
 		inv = new ItemStack[getModifiedInventorySize()];
 		buildInventoryMap();
 	}
 	
 	@Override
 	public EnumInventoryModifier getInventoryModifier() {
 		switch(upgrades[1]){
 		case 3: return EnumInventoryModifier.TOOLS;
 		default: return EnumInventoryModifier.NONE;
 		}
 	}
 	
 	private void buildInventoryMap(){
 		inventoryMap = new HashMap<EnumInventoryModifier, int[]>();
 		int[] indicies = new int[2];
 		indicies[0] = 0; indicies[1] = 27;
 		inventoryMap.put(EnumInventoryModifier.NONE, indicies);
 		switch(getInventoryModifier()){
 		case TOOLS:
 			indicies = new int[2];
 			indicies[0] = 27; indicies[1] = 30;
 			inventoryMap.put(EnumInventoryModifier.TOOLS, indicies);
 			break;
 		default: break;
 		}
 	}
 
 	@Override
 	public int getToolModifierInvIndex() {
 		if(getInventoryModifier() == EnumInventoryModifier.TOOLS){
 			if(inventoryMap == null || !inventoryMap.containsKey(EnumInventoryModifier.TOOLS)){
 				buildInventoryMap();
 				return 27;
 			}
 			return inventoryMap.get(EnumInventoryModifier.TOOLS)[0];
 		}
 		else
 			return -1;
 	}
 
 	public void setSelectedTool(int toolIndex) {
 		if(toolIndex == selectedToolIndex)
 			selectedToolIndex = -1;
 		else
 			selectedToolIndex = toolIndex;
 		sendRenderPacket = true;
 	}
 	
 	public ItemStack getSelectedTool(){
 		if(getInventoryModifier() != EnumInventoryModifier.TOOLS)
 			return null;
 		return inv[selectedToolIndex + getToolModifierInvIndex()];
 	}
 	
 	public int getSelectedToolIndex(){
 		return selectedToolIndex;
 	}
 	@Override
 	public int getModifiedInventorySize() {
 		return getBaseInventorySize() +getInventoryModifier().getNumSlots();
 	}
 	
 	@Override
 	public int getBaseInventorySize() {
 		return 27;
 	}
 
 	public NBTTagCompound getModifiers() {
 		return modifiers;
 	}
 	
 	public byte[] getUpgrades(){
 		return upgrades;
 	}
 
 	public ItemStack getPlanksUsed() {
 		NBTTagCompound tag = ModificationNBTHelper.getPlanksUsed(modifiers);
 		ItemStack stack = ItemStack.loadItemStackFromNBT(ModificationNBTHelper.getPlanksUsed(modifiers));
 		return stack;
 	}
 	
 	public LocalInventoryCrafting getCopyOfMatrix(LocalInventoryCrafting matrix){
 		LocalInventoryCrafting temp = new LocalInventoryCrafting(this);
 		for(int i = 0; i < temp.getSizeInventory(); i++){
 			temp.setInventorySlotContents(i, matrix.getStackInSlot(i) != null ? matrix.getStackInSlot(i).copy() : null);
 		}
 		return temp;
 	}
 	
 	@Override
 	public void onInventoryChanged() {
 		if(!containerInit && !containerWorking){
 			if(checkDifferences()){
 				markForUpdate();
 				makeNewMatrix();
 			}
 		}
 		super.onInventoryChanged();
 	}
 	
 	@Override
 	public void updateEntity() {
 		if(shouldUpdateOutput && !containerInit && !containerWorking){
 			findRecipe(false);
 			shouldUpdateOutput = false;
 		}
 		if(sendRenderPacket){
 			PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 64D, worldObj.provider.dimensionId, getLiteDescription(0));
 			sendRenderPacket = false;
 		}
 		if(update <= 5 && update != -1)
 			update++;
 		if(update >= 5 && worldObj.isRemote){
 			FMLClientHandler.instance().getClient().renderGlobal.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
 			update = -1;
 		}
 		if(initialized && (getModifiers() == null || getInventoryModifier() == null)){
 			if(getModifiers() == null)
 				CSLogger.logError("TIle entity has null upgrades.");
 			if(getInventoryModifier() == null)
 				CSLogger.logError("Tile entity has null inventory modifier.");
 		}
 		super.updateEntity();
 	}
 	
 	private void makeNewMatrix() {
 		for(int i = 0; i < 9; i++){
 			lastCraftMatrix.setInventorySlotContents(i, inv[i]);
 		}
 	}
 
 	private boolean checkDifferences() {
 		for(int i = 0; i < 9; i++){
 			if(!ItemStack.areItemStacksEqual(lastCraftMatrix.getStackInSlot(i), inv[i]))
 				return true;
 		}
 		return false;
 	}
 
 	public void markForUpdate() {
 		shouldUpdateOutput = true;
 	}
 
 	@Override
 	public Packet getDescriptionPacket() {
 		NBTTagCompound tag = new NBTTagCompound();
 		writeToNBT(tag);
 		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 1, tag);
 	}
 	
 	public Packet getLiteDescription(int type) {
 		NBTTagCompound tag = new NBTTagCompound();
 		if(type == 0){
 			tag.setTag("displayedResult", result != null ? result.writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
 			if(getInventoryModifier() == EnumInventoryModifier.TOOLS){
 				NBTTagCompound tag2 = new NBTTagCompound();
 				for(int i = 0; i < 3; i++){
 					tag2 = new NBTTagCompound();
 					tag.setTag("tool" +i, inv[i +getToolModifierInvIndex()] != null ? inv[i +getToolModifierInvIndex()].writeToNBT(tag2) : tag2);
 				}
 				tag.setByte("selectedToolIndex", (byte)selectedToolIndex);
 			}
 		}
 		if(type == 1){
 			tag.setFloat("randomShift", new Random().nextFloat()/100);
 		}
 		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 1, tag);
 	}
 	
 	@Override
 	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
 		super.onDataPacket(net, pkt);
 		if(pkt.data.hasKey("displayedResult")){
 			result = ItemStack.loadItemStackFromNBT(pkt.data.getCompoundTag("displayedResult"));
 			if(getInventoryModifier() == EnumInventoryModifier.TOOLS){
 				for(int i = 0; i < 3; i++){
 					tools[i] = ItemStack.loadItemStackFromNBT(pkt.data.getCompoundTag("tool" +i));
 				}
 			}
 			selectedToolIndex = pkt.data.getByte("selectedToolIndex");
 			return;
 		}
 		if(pkt.data.hasKey("randomShift")){
 			randomShift = pkt.data.getFloat("randomShift");
 			return;
 		}
 		readFromNBT(pkt.data);
		if(this.worldObj != null && inv != null)
 			findRecipe(true);
 	}
 	
 	@Override
 	public int getSizeInventory() {
 		return inv.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		return inv[slot];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slot, int amount) {
 		ItemStack stack = getStackInSlot(slot);
 		if(stack != null)
 		{
 			if(stack.stackSize <= amount)
 			{
 				setInventorySlotContents(slot, null);
 			} else
 			{
 				stack = stack.splitStack(amount);
 				if(stack.stackSize == 0) 
 				{
 					setInventorySlotContents(slot, null);
 				}else
 					onInventoryChanged();
 			}
 		}
 		return stack;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot) {
 		ItemStack stack = getStackInSlot(slot);
 		if(stack != null)
 		{
 			setInventorySlotContents(slot, null);
 		}
 		onInventoryChanged();
 		return stack;
 	}
 
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack stack) {
 		inv[slot] = stack;
 		if(stack != null && stack.stackSize > getInventoryStackLimit())
 		{
 			stack.stackSize = getInventoryStackLimit();
 		}
 		if(slot >= 27 && !worldObj.isRemote){
 			sendRenderPacket = true;
 			if(slot == 27)
 				tools[0] = inv[slot];
 			if(slot == 28)
 				tools[1] = inv[slot];
 			if(slot == 29)
 				tools[2] = inv[slot];
 		}
 	}
 
 	public void readFromNBT(NBTTagCompound tagCompound)
 	{
 		super.readFromNBT(tagCompound);
 		
 		try{
 			initializeFromNBT(ModificationNBTHelper.getModifierTag(tagCompound));
 			handleModifiers();
 			
 			if(!modifiers.getName().equals(""))
 				modifiers.setName("");
 			
 			if(inv != null){
 				NBTTagList tagList = tagCompound.getTagList("Inventory");
 				for(int i = 0; i < tagList.tagCount(); i++)
 				{
 					NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
 					byte slot = tag.getByte("Slot");
 					if(slot >= 0 && slot < inv.length)
 					{
 						inv[slot] = ItemStack.loadItemStackFromNBT(tag);
 						if(slot >= 27){
 							if(slot == 27)
 								tools[0] = inv[slot];
 							if(slot == 28)
 								tools[1] = inv[slot];
 							if(slot == 29)
 								tools[2] = inv[slot];
 						}
 					}
 				}
 			}
 			if(getInventoryModifier() == EnumInventoryModifier.TOOLS){
 				selectedToolIndex = tagCompound.getByte("selectedToolIndex");
 			}
 			directionFacing = tagCompound.getByte("direction");
 		}catch(Exception ex){
 			CSLogger.logError("Failed loading a crafting table. Dropping inventory at " +xCoord +","+yCoord+","+zCoord, ex);
 			NBTTagList tagList = tagCompound.getTagList("Inventory");
 			if(tagList != null && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
 				for(int i = 0; i < tagList.tagCount(); i++)
 				{
 					ItemStack item = ItemStack.loadItemStackFromNBT((NBTTagCompound)tagList.tagAt(i));
 					if(item != null && item.stackSize > 0)
 					{
 						EntityItem ei = new EntityItem(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld(), xCoord, yCoord, zCoord,
 								new ItemStack(item.itemID, item.stackSize, item.getItemDamage()));
 						if(item.hasTagCompound())
 							ei.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
 						float factor = 0.05f;
 						FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().spawnEntityInWorld(ei);
 						item.stackSize = 0;
 					}
 				}
 			}
 		}
 	}
 	@Override
 	public void writeToNBT(NBTTagCompound tagCompound)
 	{
 		super.writeToNBT(tagCompound);
 		
 		NBTTagList itemList = new NBTTagList();	
 		
 		if(inv != null){
 			for(int i = 0; i < inv.length; i++)
 			{
 				ItemStack stack = inv[i];
 				if(stack != null)
 				{
 					NBTTagCompound tag = new NBTTagCompound();	
 					tag.setByte("Slot", (byte)i);
 					stack.writeToNBT(tag);
 					itemList.appendTag(tag);
 				}
 			}
 		}
 		tagCompound.setTag("Inventory", itemList);
 		if(modifiers.hasKey(ModificationNBTHelper.modifierTag))
 			modifiers = (NBTTagCompound) modifiers.getTag(ModificationNBTHelper.modifierTag);
 		if(!modifiers.getName().equals(""))
 			modifiers.setName(null);
 		tagCompound.setTag(ModificationNBTHelper.modifierTag, modifiers);
 		tagCompound.setByte("direction", directionFacing);
 		if(getInventoryModifier() == EnumInventoryModifier.TOOLS)
 			tagCompound.setByte("selectedToolIndex", (byte)selectedToolIndex);
 	}
 	
 	@Override
 	public String getInvName() {
 		return "Project Bench";
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		return false;
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer player) {
 		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this &&
 				player.getDistanceSq(xCoord +0.5, yCoord +0.5, zCoord +0.5) < 64;
 	}
 
 	@Override
 	public void openChest() {}
 
 	@Override
 	public void closeChest() {
 		if(!worldObj.isRemote)
 			PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 64D, worldObj.provider.dimensionId, getLiteDescription(1));
 	}
 
 	@Override
 	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
 		return true;
 	}
 	
 	@Override
 	public int[] getAccessibleSlotsFromSide(int side) {
 		int[] slots = null;
 		switch(side){
 		case 0: 
 			slots = new int[9];
 			for(int i = 0; i < slots.length; i++)
 				slots[i] = i;
 			return slots;
 		default:
 			slots = new int[18];
 			for(int i = 0; i < slots.length; i++)
 				slots[i] = i +9;
 			return slots;
 		}
 	}
 
 	@Override
 	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
 		return true;
 	}
 
 	@Override
 	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
 		return true;
 	}
 
 	@Override
 	public byte getDirectionFacing() {
 		return directionFacing;
 	}
 
 	@Override
 	public void setDirectionFacing(byte byt) {
 		directionFacing = byt;
 	}
 }
