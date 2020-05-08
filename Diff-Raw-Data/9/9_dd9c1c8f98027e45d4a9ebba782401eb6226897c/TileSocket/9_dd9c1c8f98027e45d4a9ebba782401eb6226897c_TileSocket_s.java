 package emasher.sockets;
 
 import ic2.api.Direction;
 import ic2.api.energy.event.EnergyTileLoadEvent;
 import ic2.api.energy.event.EnergyTileUnloadEvent;
 import ic2.api.energy.tile.IEnergyAcceptor;
 import ic2.api.energy.tile.IEnergySink;
 import ic2.api.energy.tile.IEnergySource;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import emasher.api.IGasReceptor;
 import emasher.api.ModuleRegistry;
 import emasher.api.SideConfig;
 import emasher.api.SocketModule;
 import emasher.api.SocketTileAccess;
 import emasher.sockets.modules.ModMachineOutput;
 import buildcraft.api.core.Position;
 import buildcraft.api.inventory.ISpecialInventory;
 import buildcraft.api.power.IPowerReceptor;
 import buildcraft.api.power.PowerHandler;
 import buildcraft.api.power.PowerHandler.PowerReceiver;
 import buildcraft.api.power.PowerHandler.Type;
 import buildcraft.api.transport.IPipeConnection;
 import buildcraft.api.transport.IPipeTile;
 import buildcraft.api.transport.IPipeTile.PipeType;
 import buildcraft.core.inventory.ITransactor;
 import buildcraft.core.inventory.Transactor;
 import net.minecraft.block.Block;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.inventory.InventoryBasic;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityHopper;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidContainerRegistry;
 import net.minecraftforge.fluids.FluidStack;
 import net.minecraftforge.fluids.FluidTank;
 import net.minecraftforge.fluids.FluidTankInfo;
 import net.minecraftforge.fluids.IFluidHandler;
 import net.minecraftforge.fluids.IFluidTank;
 
 public class TileSocket extends SocketTileAccess implements ISpecialInventory, IPowerReceptor, IGasReceptor, IEnergySink, IEnergySource, IFluidHandler
 {
 	public FluidTank[] tanks;
 	public InventoryBasic inventory;
 	
 	public boolean[] rsControl;
 	public boolean[] rsLatch;
 	public boolean addedToEnergyNet;
 	
 	public int[] sides;
 	public SideConfig[] configs;
 	public boolean[] sideRS;
 	public boolean initialized = false;
 	
 	public int[] sideID;
 	public int[] sideMeta;
 	public boolean[] sideLocked;
 	
 	public int[] facID;
 	public int[] facMeta;
 	
 	public boolean isRSShared;
 	
 	public TileSocket()
 	{
 		tanks = new FluidTank[3];
 		rsControl = new boolean[3];
 		rsLatch = new boolean[3];
 		sideLocked = new boolean[6];
 		facID = new int[6];
 		facMeta = new int[6];
 		isRSShared = false;
 		
 		for(int i = 0; i < 3; i++)
 		{
 			tanks[i] = new FluidTank(8 * FluidContainerRegistry.BUCKET_VOLUME);
 			rsControl[i] = false;
 			rsLatch[i] = false;
 		}
 		
 		inventory = new InventoryBasic("socket", true, 3);
 		sideInventory = new InventoryBasic("socketSide", true, 6);
 		
 		/*if(PowerFramework.currentFramework != null)
 		{
 			powerHandler = (PowerHandler) PowerFramework.currentFramework.createPowerHandler();
 		}*/
 		
 		/*if(powerHandler == null)
 		{
 			powerHandler = new SocketPowerHandler();
 		}*/
 		
 		powerHandler = new PowerHandler(this, PowerHandler.Type.STORAGE);
 		
 		powerHandler.configure(0.0F, 1000.0F, 0.0F, 500.0F);
 		powerHandler.configurePowerPerdition(0,0);
 		powerHandler.useEnergy(powerHandler.getEnergyStored(), powerHandler.getEnergyStored(), true);
 		
 		sides = new int[6];
 		configs = new SideConfig[6];
 		sideRS = new boolean[6];
 		for(int i = 0; i < 6; i++)
 		{
 			sides[i] = 0;
 			configs[i] = new SideConfig();
 			sideRS[i] = false;
 			sideLocked[i] = false;
 			facID[i] = 0;
 			facMeta[i] = 0;
 		}
 		
 	}
 	
 	public void updateEntity()
 	{
 		if(! worldObj.isRemote)
 		{
 			ForgeDirection d;
 			SocketModule m;
 			SideConfig c;
 			for(int i = 0; i < 6; i++)
 			{
 				d = ForgeDirection.getOrientation(i);
 				m = getSide(d);
 				c = configs[i];
 				
 				if(m.pullsFromHopper())
 				{
 					int xo = xCoord + d.offsetX;
 					int yo = yCoord + d.offsetY;
 					int zo = zCoord + d.offsetZ;
 					
 					TileEntity t = worldObj.getBlockTileEntity(xo, yo, zo);
 					
 					if(t != null && t instanceof TileEntityHopper)
 					{	
 						TileEntityHopper th = (TileEntityHopper)t;
 						
 						for (int j = 0; j < th.getSizeInventory(); ++j)
 			            {
 			                if (th.getStackInSlot(j) != null)
 			                {
 			                    ItemStack itemstack = th.getStackInSlot(j).copy();
 			                    itemstack.stackSize = 1;
 			                    int added =  addItem(itemstack, true, d);
 			                    
 			                    itemstack.stackSize = th.getStackInSlot(j).stackSize - added;
 			                    if(itemstack.stackSize <= 0) itemstack = null;
 			                    
 			                    th.setInventorySlotContents(j, itemstack);
 			                    break;
 			                }
 			            }
 			        }
 				}
 				m.updateSide(c, this, d);
 			}
 		}
 		
 		if (!initialized && worldObj != null)
 		{
 			if (! worldObj.isRemote)
 			{
 				EnergyTileLoadEvent loadEvent = new EnergyTileLoadEvent(this);
 				MinecraftForge.EVENT_BUS.post(loadEvent);
 				this.addedToEnergyNet = true;
 				
 				sideID = new int[6];
 				sideMeta = new int[6];
 				
 				for(int i = 0; i < 6; i++)
 				{
 					sideID[i] = -1;
 					sideMeta[i] = -1;
 					
 					checkSideForChange(i);
 				}
 				
 			}
 			
 			initialized = true;
 		}
 	}
 	
 	@Override
 	public void invalidate()
 	{
 		MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
 		addedToEnergyNet = false;
 	}
 	
 	@Override
 	public void onChunkUnload()
 	{
 		MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
 		addedToEnergyNet = false;
 	}
 	
 	public void updateAllAdj()
 	{
 		ForgeDirection d;
 		for(int i = 0; i < 6; i++)
 		{
 			d = ForgeDirection.getOrientation(i);
 			updateAdj(d);
 		}
 	}
 	
 	public void updateAdj(ForgeDirection d)
 	{
 		int id = worldObj.getBlockId(xCoord, yCoord, zCoord);
 		int xo = xCoord + d.offsetX;
 		int yo = yCoord + d.offsetY;
 		int zo = zCoord + d.offsetZ;
 		Block b = Block.blocksList[worldObj.getBlockId(xo, yo, zo)];
 		if(b != null)
 		{
 			b.onNeighborBlockChange(worldObj, xo, yo, zo, id);
 		}
 	}
 	
 	public void resetConfig(int side)
 	{
 		configs[side] = new SideConfig();
 		sideLocked[side] = false;
 		sideInventory.setInventorySlotContents(side, null);
 		PacketHandler.instance.SendClientSideState(this, (byte)side);
 	}
 	
 	@Override
 	public void validate()
 	{
 		super.validate();
 		if(this.worldObj.isRemote)
 		{
 			for(int i = 0; i < 6; i++)
 			emasher.sockets.client.ClientPacketHandler.instance.requestSideData(this, (byte)i);
 			for(int i = 0; i < 3; i++)
 			{
 				emasher.sockets.client.ClientPacketHandler.instance.requestInventoryData(this, (byte)i);
 				emasher.sockets.client.ClientPacketHandler.instance.requestTankData(this, (byte)i);
 			}
 		}
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound data)
 	{
 		super.readFromNBT(data);
 		
 		if(data.hasKey("shareRS"))
 		{
 			isRSShared = data.getBoolean("shareRS");
 		}
 	    
 	    for(int i = 0; i < 3; i++)
 	    {
 	    	if(data.hasKey("Fluid" + i))
 	    	{
 	    		tanks[i].setFluid(FluidStack.loadFluidStackFromNBT(data.getCompoundTag("Fluid" + i)));
 	    	}
 	    	
 	    	if(data.hasKey("rsControl"+ i)) rsControl[i] = data.getBoolean("rsControl" + i);
 	    	if(data.hasKey("rsLatch" + i)) rsLatch[i] = data.getBoolean("rsLatch" + i);
 	    }
 	    
 	    NBTTagList itemList = data.getTagList("items");
 	    
 	    for(int i = 0; i < itemList.tagCount(); i++)
 	    {
 	    	NBTTagCompound itemCompound = (NBTTagCompound) itemList.tagAt(i);
 	    	int slot = itemCompound.getInteger("slot");
 	    	inventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(itemCompound));
 	    }
 	    
 	    for(int i = 0; i < 6; i++)
 	    {
 	    	if(data.hasKey("side" + i))
 	    	{
 	    		sides[i] = data.getInteger("side" + i);
 	    	}
 	    	if(data.hasKey("config" + i))
 	    	{
 	    		configs[i] = new SideConfig();
 	    		configs[i].readFromNBT(data.getCompoundTag("config" + i));
 	    	}
 	    	if(data.hasKey("rs" + i))
 	    	{
 	    		sideRS[i] = data.getBoolean("rs" + i);
 	    	}
 	    	if(data.hasKey("lock" + i))
 	    	{
 	    		sideLocked[i] = data.getBoolean("lock" + i);
 	    	}
 	    	if(data.hasKey("facID" + i))
 	    	{
 	    		facID[i] = data.getInteger("facID" + i);
 	    	}
 	    	if(data.hasKey("facMeta" + i))
 	    	{
 	    		facMeta[i] = data.getInteger("facMeta" + i);
 	    	}
 	    }
 	    
 	    itemList = data.getTagList("sideItems");
 	    
 	    for(int i = 0; i < itemList.tagCount(); i++)
 	    {
 	    	NBTTagCompound itemCompound = (NBTTagCompound) itemList.tagAt(i);
 	    	int slot = itemCompound.getInteger("slot");
 	    	sideInventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(itemCompound));
 	    }
 	    
 	    powerHandler.readFromNBT(data);
 	    float power = powerHandler.getEnergyStored();
	    this.setMaxEnergyStored((int)data.getFloat("powerCap"));
 	    powerHandler.setEnergy(power);
 	    
 	    
 	}
 	
 
 	@Override
 	public void writeToNBT(NBTTagCompound data)
 	{
 		super.writeToNBT(data);
 		
 		NBTTagList itemList = new NBTTagList();
 		NBTTagList sideItemList = new NBTTagList();
 		NBTTagCompound configData;
 		
 		data.setBoolean("shareRS", isRSShared);
 		
 		for(int i = 0; i < 3; i++)
 		{
 			
 			if(inventory.getStackInSlot(i) != null)
 			{
 				NBTTagCompound itemCompound = new NBTTagCompound();
 				itemCompound.setInteger("slot", i);
 				inventory.getStackInSlot(i).writeToNBT(itemCompound);
 				itemList.appendTag(itemCompound);
 			}
 			
 			if(tanks[i].getFluid() != null)
 			{
 				data.setTag("Fluid" + i, tanks[i].getFluid().writeToNBT(new NBTTagCompound()));
 			}
 			
 			data.setBoolean("rsControl" + i, rsControl[i]);
 			data.setBoolean("rsLatch" + i, rsLatch[i]);
 		}
 		
 		data.setTag("items", itemList);
 		
 		
 		for(int i = 0; i < 6; i++)
 		{
 			data.setInteger("side" + i, sides[i]);
 			configData = new NBTTagCompound();
 			configs[i].writeToNBT(configData);
 			data.setCompoundTag("config" + i, configData);
 			data.setBoolean("rs" + i, sideRS[i]);
 			data.setBoolean("lock" + i, sideLocked[i]);
 			data.setInteger("facID" + i, facID[i]);
 			data.setInteger("facMeta" + i, facMeta[i]);
 			
 			if(sideInventory.getStackInSlot(i) != null)
 			{
 				NBTTagCompound itemCompound = new NBTTagCompound();
 				itemCompound.setInteger("slot", i);
 				sideInventory.getStackInSlot(i).writeToNBT(itemCompound);
 				sideItemList.appendTag(itemCompound);
 				
 			}
 			
 		}
 		
 		data.setTag("sideItems", sideItemList);
 		
 		if(powerHandler != null)
 		{
 			powerHandler.writeToNBT(data);
 			data.setFloat("powerCap", powerHandler.getMaxEnergyStored());
 		}
 		
  	}
 	
 	@Override
 	public SocketModule getSide(ForgeDirection direction)
 	{
 		SocketModule result = ModuleRegistry.getModule(0);
 		SocketModule temp = null;
 		if(direction.ordinal() < 6) temp =  ModuleRegistry.getModule(sides[direction.ordinal()]);
 		if(temp != null) result = temp;
 		return result;
 	}
 	
 	@Override
 	public SideConfig getConfigForSide(ForgeDirection direction)
 	{
 		return configs[direction.ordinal()];
 	}
 	
 	public void lockSide(int side)
 	{
 		sideLocked[side] = ! sideLocked[side];
 		PacketHandler.instance.SendClientSideState(this, (byte)side);
 	}
 	
 	public void checkSideForChange(int side)
 	{
 		ForgeDirection d = ForgeDirection.getOrientation(side);
 		SocketModule m = getSide(d);
 		int xo = xCoord + d.offsetX;
 		int yo = yCoord + d.offsetY;
 		int zo = zCoord + d.offsetZ;
 		boolean result = false;
 			
 		int id = worldObj.getBlockId(xo, yo, zo);
 		int meta = worldObj.getBlockMetadata(xo, yo, zo);
 		if((id != sideID[side] && sideID[side] != 1) || (meta != sideMeta[side] && sideMeta[side] != -1))
 		{
 			m.onAdjChangeSide(this, configs[side], d);
 		}
 		sideID[side] = id;
 		sideMeta[side] = meta;
 	}
 	
 	public int tankIndicatorIndex(int side)
 	{
 		SideConfig c = configs[side];
 		int temp;
 		if(c.tank == -1) temp = 3;
 		else temp = c.tank;
 		return temp;
 	}
 	
 	public int inventoryIndicatorIndex(int side)
 	{
 		SideConfig c = configs[side];
 		int temp;
 		if(c.inventory == -1) temp = 3;
 		else temp = c.inventory;
 		return temp;
 	}
 	
 	public int rsIndicatorIndex(int side)
 	{
 		SideConfig c = configs[side];
 		int temp = 0;
 		
 		if(c.rsControl[0]) temp |= 1;
 		if(c.rsControl[1]) temp |= 2;
 		if(c.rsControl[2]) temp |= 4;
 		
 		return temp;
 	}
 	
 	public int latchIndicatorIndex(int side)
 	{
 		SideConfig c = configs[side];
 		int temp = 0;
 		
 		if(c.rsLatch[0]) temp |= 1;
 		if(c.rsLatch[1]) temp |= 2;
 		if(c.rsLatch[2]) temp |= 4;
 		
 		return temp;
 	}
 	
 	public void nextTank(int side)
 	{
 		configs[side].tank++;
 		if(configs[side].tank == 3) configs[side].tank = -1;
 		getSide(ForgeDirection.getOrientation(side)).indicatorUpdated(this, configs[side], ForgeDirection.getOrientation(side));
 		PacketHandler.instance.SendClientSideState(this, (byte)side);
 	}
 	
 	public void nextInventory(int side)
 	{
 		configs[side].inventory++;
 		if(configs[side].inventory == 3) configs[side].inventory = -1;
 		getSide(ForgeDirection.getOrientation(side)).indicatorUpdated(this, configs[side], ForgeDirection.getOrientation(side));
 		PacketHandler.instance.SendClientSideState(this, (byte)side);
 	}
 	
 	public void nextRS(int side)
 	{
 		boolean reset = false;
 		SideConfig c = configs[side];
 		
 		if(c.rsControl[0])
 		{
 			if(c.rsControl[1])
 			{
 				c.rsControl[1] = false;
 				if(c.rsControl[2])
 				{
 					reset = true;
 					c.rsControl[0] = false;
 					c.rsControl[2] = false;
 				}
 				else c.rsControl[2] = true;
 			}
 			else
 			{
 				c.rsControl[1] = true;
 			}
 		}
 		
 		if(! reset) configs[side].rsControl[0] = ! configs[side].rsControl[0];
 		getSide(ForgeDirection.getOrientation(side)).indicatorUpdated(this, configs[side], ForgeDirection.getOrientation(side));
 		PacketHandler.instance.SendClientSideState(this, (byte)side);
 	}
 	
 	public void nextLatch(int side)
 	{
 		boolean reset = false;
 		SideConfig c = configs[side];
 		
 		if(c.rsLatch[0])
 		{
 			if(c.rsLatch[1])
 			{
 				c.rsLatch[1] = false;
 				if(c.rsLatch[2])
 				{
 					reset = true;
 					c.rsLatch[0] = false;
 					c.rsLatch[2] = false;
 				}
 				else c.rsLatch[2] = true;
 			}
 			else
 			{
 				c.rsLatch[1] = true;
 			}
 		}
 		
 		if(! reset) configs[side].rsLatch[0] = ! configs[side].rsLatch[0];
 		getSide(ForgeDirection.getOrientation(side)).indicatorUpdated(this, configs[side], ForgeDirection.getOrientation(side));
 		PacketHandler.instance.SendClientSideState(this, (byte)side);
 	}
 	
 	public void modifyRS(int cell, boolean on)
 	{
 		if(rsControl[cell] != on)
 		{
 			rsControl[cell] = on;
 		
 			for(int i = 0; i < 6; i++)
 			{
 				SocketModule m = getSide(ForgeDirection.getOrientation(i));
 				m.onRSInterfaceChange(configs[i], cell, this, ForgeDirection.getOrientation(i), on);
 			}
 		}
 	}
 	
 	@Override
 	public void modifyLatch(int cell, boolean on)
 	{
 		if(rsLatch[cell] != on)
 		{
 			rsLatch[cell] = on;
 			
 			for(int i = 0; i < 6; i++)
 			{
 				SocketModule m = getSide(ForgeDirection.getOrientation(i));
 				m.onRSLatchChange(configs[i], cell, this, ForgeDirection.getOrientation(i), on);
 			}
 		}
 	}
 	
 	@Override
 	public boolean getRSControl(int channel)
 	{
 		return rsControl[channel];
 	}
 
 	@Override
 	public boolean getRSLatch(int channel)
 	{
 		return rsLatch[channel];
 	}
 	
 	@Override
 	public boolean getSideRS(ForgeDirection side)
 	{
 		return this.sideRS[side.ordinal()];
 	}
 	
 	
 
 	@Override
 	public int getSizeInventory()
 	{
 		return 3;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot)
 	{
 		return inventory.getStackInSlot(slot);
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slot, int amnt)
 	{
 		return this.extractItemInternal(true, slot, amnt);
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot)
 	{
 		return inventory.getStackInSlot(slot);
 	}
 
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack item)
 	{
 		inventory.setInventorySlotContents(slot, item);
 	}
 
 	@Override
 	public String getInvName()
 	{
 		return "socket";
 	}
 
 	@Override
 	public boolean isInvNameLocalized()
 	{
 		return false;
 	}
 
 	@Override
 	public int getInventoryStackLimit()
 	{
 		return 64;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer entityplayer)
 	{
 		return true;
 	}
 
 	@Override
 	public void openChest() {}
 
 	@Override
 	public void closeChest() {}
 
 	@Override
 	public boolean isItemValidForSlot(int i, ItemStack itemstack) 
 	{
 		return false;
 	}
 	
 	@Override
 	public ItemStack pullItem(ForgeDirection side, boolean doPull)
 	{
 		int xo = xCoord + side.offsetX;
 		int yo = yCoord + side.offsetY;
 		int zo = zCoord + side.offsetZ;
 		
 		TileEntity t = worldObj.getBlockTileEntity(xo, yo, zo);
 		
 		if(t instanceof IInventory)
 		{		
 			if(t instanceof ISpecialInventory)
 			{
 				ISpecialInventory isi = (ISpecialInventory)t;
 				ItemStack[] items = isi.extractItem(doPull, side.getOpposite(), 1);
 				if(items != null && items.length > 0) return items[0];
 			}
 			else if(t instanceof ISidedInventory)
 			{
 				ISidedInventory isi = (ISidedInventory)t;
 				int[] slots = isi.getAccessibleSlotsFromSide(side.getOpposite().ordinal());
 				
 				for(int i = 0; i < slots.length; i++)
 				{
 					ItemStack pulled = isi.getStackInSlot(slots[i]);
 					if(pulled != null && isi.canExtractItem(slots[i], pulled, side.getOpposite().ordinal()))
 					{
 						ItemStack result = pulled.copy().splitStack(1);
 						if(doPull)
 						{
 							pulled.stackSize--;
 							//isi.setInventorySlotContents(slots[i], pulled);
 							if(pulled.stackSize <= 0) isi.setInventorySlotContents(slots[i], null);
 							isi.onInventoryChanged();
 						}
 						return result;
 					}
 				}
 			}
 			else
 			{
 				IInventory ii = (IInventory)t;
 				
 				for(int i = 0; i < ii.getSizeInventory(); i++)
 				{
 					ItemStack pulled = ii.getStackInSlot(i);
 					if(pulled != null)
 					{
 						ItemStack result = pulled.copy().splitStack(1);
 						if(doPull)
 						{
 							pulled.stackSize--;
 							//ii.setInventorySlotContents(i, pulled);
 							if(pulled.stackSize <= 0)ii.setInventorySlotContents(i, null);
 							ii.onInventoryChanged();
 						}
 						return result;
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	public int addItemInternal(ItemStack stack, boolean doAdd, int inv)
 	{
 		int amntAdded;
 		int temp;
 		
 		if(inv > -1 && inv < 3)
 		{
 			ItemStack currStack = inventory.getStackInSlot(inv);
 			
 			if(currStack == null)
 			{
 				if(doAdd)
 				{
 					inventory.setInventorySlotContents(inv, stack.copy());
 					for(int i = 0; i < 6; i++)
 					{
 						SocketModule m = getSide(ForgeDirection.getOrientation(i));
 						m.onInventoryChange(configs[i], inv, this, ForgeDirection.getOrientation(i), true);
 					}
 				}
 				
 				return stack.stackSize;
 			}
 			else if(currStack.isItemEqual(stack))
 			{
 				temp = Math.min(currStack.stackSize + stack.stackSize, currStack.getItem().getItemStackLimit());
 				if(temp == (currStack.stackSize + stack.stackSize))
 				{
 					amntAdded = stack.stackSize;
 				}
 				else
 				{
 					amntAdded = currStack.getItem().getItemStackLimit() - currStack.stackSize;
 				}
 				
 				if(doAdd && amntAdded > 0)
 				{
 					currStack.stackSize += amntAdded;
 					for(int i = 0; i < 6; i++)
 					{
 						SocketModule m = getSide(ForgeDirection.getOrientation(i));
 						m.onInventoryChange(configs[i], inv, this, ForgeDirection.getOrientation(i), true);
 					}
 				}
 				
 				return amntAdded;
 			}
 		}
 		
 		return 0;
 	}
 
 	public ItemStack extractItemInternal(boolean doRemove, int inv, int maxItemCount)
 	{
 		ItemStack newStack;
 		
 		if(inv > -1 && inv < 3)
 		{
 			ItemStack currStack = inventory.getStackInSlot(inv);
 			
 			if(currStack != null)
 			{
 				newStack = currStack.copy();
 				newStack.stackSize = Math.min(currStack.stackSize, maxItemCount);
 				if(doRemove)
 				{
 					currStack.stackSize -= newStack.stackSize;
 					if(currStack.stackSize <= 0) inventory.setInventorySlotContents(inv, null);
 					for(int i = 0; i < 6; i++)
 					{
 						SocketModule m = getSide(ForgeDirection.getOrientation(i));
 						m.onInventoryChange(configs[i], inv, this, ForgeDirection.getOrientation(i), false);
 					}
 				}
 				
 				return newStack;
 			}
 			
 		}
 		
 		return null;
 	}
 	
 	public ItemStack getStackInInventorySlot(int inv)
 	{
 		return inventory.getStackInSlot(inv);
 	}
 	
 	public void setInventoryStack(int inv, ItemStack stack)
 	{
 		if(stack == null || stack.stackSize <= 0) inventory.setInventorySlotContents(inv, null);
 		else inventory.setInventorySlotContents(inv, stack);
 		
 		for(int i = 0; i < 6; i++)
 		{
 			ForgeDirection d = ForgeDirection.getOrientation(i);
 			getSide(d).onInventoryChange(configs[i], inv, this, d, false);
 		}
 	}
 
 	
 	@Override
 	public int addItem(ItemStack stack, boolean doAdd, ForgeDirection direction)
 	{
 		if(direction.ordinal() >= 0 && direction.ordinal() < 6)
 		{
 			SocketModule m = getSide(direction);
 			SideConfig c = configs[direction.ordinal()];
 			if(m.isItemInterface() && m.canInsertItems()) return m.itemFill(stack, doAdd, c, this, direction);
 		}
 		return 0;
 	}
 
 	@Override
 	public ItemStack[] extractItem(boolean doRemove, ForgeDirection direction, int maxItemCount)
 	{
 		if(direction.ordinal() > 0 && direction.ordinal() < 6)
 		{
 			SocketModule m = getSide(direction);
 			SideConfig c = configs[direction.ordinal()];
 			
 			if(m.isItemInterface() && m.canExtractItems())
 			{
 				ItemStack temp = m.itemExtract(maxItemCount, doRemove, c, this);
 				if(temp != null) return new ItemStack[]{temp};
 			}
 		}
 		return new ItemStack[]{};
 	}
 	
 	public boolean tryInsertItem(ItemStack stack, ForgeDirection side)
 	{
 		int xo = xCoord + side.offsetX;
 		int yo = yCoord + side.offsetY;
 		int zo = zCoord + side.offsetZ;
 		
 		TileEntity t = worldObj.getBlockTileEntity(xo, yo, zo);
 		
 		if(stack == null) return false;
 		
 		if(t instanceof IInventory)
 		{
 			if(t instanceof ISpecialInventory)
 			{
 				ISpecialInventory isi = (ISpecialInventory)t;
 				ItemStack ghost = stack.copy().splitStack(1);
 				int used = isi.addItem(ghost, true, side.getOpposite());
 				if(used > 0) return true;
 				
 			}
 			else if(t instanceof ISidedInventory)
 			{
 				ISidedInventory isi = (ISidedInventory)t;
 				ItemStack ghost = stack.copy().splitStack(1);
 				int[] slots = isi.getAccessibleSlotsFromSide(side.getOpposite().ordinal());
 				
 				for(int i = 0; i < slots.length; i++)
 				{
 					if(isi.canInsertItem(slots[i], ghost, side.getOpposite().ordinal()))
 					{
 						ItemStack inSlot = isi.getStackInSlot(slots[i]);
 						if(inSlot != null && inSlot.isItemEqual(ghost) && inSlot.stackSize < inSlot.getMaxStackSize() && inSlot.stackSize < isi.getInventoryStackLimit())
 						{
 							inSlot.stackSize++;
 							isi.onInventoryChanged();
 							return true;
 						}
 					}
 				}
 				
 				for(int i = 0; i < slots.length; i++)
 				{
 					if(isi.canInsertItem(slots[i], ghost, side.getOpposite().ordinal()))
 					{
 						ItemStack inSlot = isi.getStackInSlot(slots[i]);
 						if(inSlot == null)
 						{
 							isi.setInventorySlotContents(slots[i], ghost);
 							isi.onInventoryChanged();
 							return true;
 						}
 					}
 				}
 				
 				return false;
 			}
 			else
 			{
 				IInventory ii = (IInventory)t;
 				ItemStack ghost = stack.copy().splitStack(1);
 				
 				for(int i = 0; i < ii.getSizeInventory(); i++)
 				{
 					if(ii.isItemValidForSlot(i, ghost))
 					{
 						ItemStack inSlot = ii.getStackInSlot(i);
 						if(inSlot != null && inSlot.isItemEqual(ghost) && inSlot.stackSize < inSlot.getMaxStackSize() && inSlot.stackSize < ii.getInventoryStackLimit())
 						{
 							inSlot.stackSize++;
 							ii.onInventoryChanged();
 							return true;
 						}
 					}
 				}
 				
 				for(int i = 0; i < ii.getSizeInventory(); i++)
 				{
 					if(ii.isItemValidForSlot(i, ghost))
 					{
 						ItemStack inSlot = ii.getStackInSlot(i);
 						if(inSlot == null)
 						{
 							ii.setInventorySlotContents(i, ghost);
 							ii.onInventoryChanged();
 							return true;
 						}
 					}
 				}
 				
 				return false;
 			}
 		}
 		
 		if(Loader.isModLoaded("BuildCraft|Core") && t != null && stack != null)
 		{
 			if(t instanceof IPipeTile)
 			{
 				IPipeTile p = (IPipeTile)t;
 				
 				if(p.getPipeType() == PipeType.ITEM && p.isPipeConnected(side.getOpposite()))
 				{
 					int res = p.injectItem(stack, false, side.getOpposite());
 					if(res == stack.stackSize)
 					{
 						p.injectItem(stack, true, side.getOpposite());
 						stack.stackSize = 0;
 						return true;
 					}
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	/*@Override
 	public int recieveGas(FluidStack gas, ForgeDirection direction)
 	{
 		SocketModule m = getSide(direction);
 		
 		if(m.isGasInterface())
 		{
 			return m.gasFill(gas, this);
 		}
 		
 		return 0;
 	}*/
 
 	/*@Override
 	public void setPowerHandler(IPowerHandler provider)
 	{
 		powerHandler = (PowerHandler) provider;
 	}
 
 	@Override
 	public IPowerHandler getPowerHandler()
 	{
 		return powerHandler;
 	}
 
 	@Override
 	public void doWork() {}
 
 	@Override
 	public int powerRequest(ForgeDirection direction)
 	{
 		SocketModule m = getSide(direction);
 		
 		if(m.isEnergyInterface(configs[direction.ordinal()]) && m.acceptsEnergy(configs[direction.ordinal()])) return Math.min(m.getPowerRequested(configs[direction.ordinal()], this), (int)(powerHandler.getMaxEnergyStored() - powerHandler.getEnergyStored()));
 		else return 0;
 	}*/
 
 	
 
 	@Override
 	public int fill(ForgeDirection direction, FluidStack resource, boolean doFill)
 	{
 		SocketModule m = getSide(direction);
 		SideConfig c = configs[direction.ordinal()];
 		
 		if(m.isFluidInterface() && m.canInsertFluid()) return m.fluidFill(resource, doFill, c, this, direction);
 		return 0;
 	}
 	
 	public int fillInternal(int tank, FluidStack resource, boolean doFill)
 	{
 		if(tank >= 0 && tank < 3)
 		{
 			int result = tanks[tank].fill(resource, doFill);
 			
 			if(result > 0 && doFill)
 			{
 				for(int i = 0; i < 6; i++)
 				{
 					SocketModule m = getSide(ForgeDirection.getOrientation(i));
 					m.onTankChange(configs[i], tank, this, ForgeDirection.getOrientation(i), true);
 				}
 			}
 			
 			return result;
 		}
 		return 0;
 	}
 	
 	/*@Override
 	public FluidStack getFluidInTank(int tank)
 	{
 		return tanks[tank].getFluid();
 	}
 
 	@Override
 	public int fill(int tankIndex, FluidStack resource, boolean doFill)
 	{
 		return 0;
 	}*/
 
 	@Override
 	public FluidStack drain(ForgeDirection direction, int maxDrain, boolean doDrain)
 	{
 		SocketModule m = getSide(direction);
 		SideConfig c = configs[direction.ordinal()];
 		
 		if(m.isFluidInterface() && m.canExtractFluid()) return m.fluidExtract(maxDrain, doDrain, c, this);
 		return null;
 	}
 	
 	public FluidStack drainInternal(int tank, int maxDrain, boolean doDrain)
 	{
 		if(tank >= 0 && tank < 3)
 		{
 			FluidStack result = tanks[tank].drain(maxDrain, doDrain);
 			
 			if(result != null  && doDrain)
 			{
 				for(int i = 0; i < 6; i++)
 				{
 					SocketModule m = getSide(ForgeDirection.getOrientation(i));
 					m.onTankChange(configs[i], tank, this, ForgeDirection.getOrientation(i), false);
 				}
 			}
 			
 			return result;
 		}
 		return null;
 	}
 
 	/*@Override
 	public FluidStack drain(int tankIndex, int maxDrain, boolean doDrain)
 	{
 		return null;
 	}*/
 
 	/*@Override
 	public IFluidTank[] getTanks(ForgeDirection direction)
 	{
 		/*SocketModule m = getSide(direction);
 		if(m != null)
 		{
 			SideConfig c = configs[direction.ordinal()];
 			if(m.isFluidInterface()) return new IFluidTank[] {m.getAssociatedTank(c, this)};
 		}
 		return new IFluidTank[]{};
 	}
 
 	@Override
 	public IFluidTank getTank(ForgeDirection direction, FluidStack type)
 	{
 		/*SocketModule m = getSide(direction);
 		SideConfig c = configs[direction.ordinal()];
 		
 		if(m.isFluidInterface()) return m.getAssociatedTank(c, this);
 		return null;
 	}*/
 	
 	public void tryInsertFluid(int tank, ForgeDirection side)
 	{
 		int xo = xCoord + side.offsetX;
 		int yo = yCoord + side.offsetY;
 		int zo = zCoord + side.offsetZ;
 		
 		TileEntity t = worldObj.getBlockTileEntity(xo, yo, zo);
 		
 		if(tanks[tank].getFluid() != null && t != null && t instanceof IFluidHandler)
 		{
 			IFluidHandler tn = (IFluidHandler)t;
 			
 			int amnt = tn.fill(side.getOpposite(), tanks[tank].getFluid(), true);
 			this.drainInternal(tank, amnt, true);
 		}
 	}
 	
 	public void tryExtractFluid(int tank, ForgeDirection side, int volume)
 	{
 		int xo = xCoord + side.offsetX;
 		int yo = yCoord + side.offsetY;
 		int zo = zCoord + side.offsetZ;
 		
 		TileEntity t = worldObj.getBlockTileEntity(xo, yo, zo);
 		
 		if(t != null && t instanceof IFluidHandler)
 		{
 			IFluidHandler tn = (IFluidHandler)t;
 			
 			FluidStack ghost = tn.drain(side.getOpposite(), volume, false);
 			//int amnt = tanks[tank].fill(ghost, true);
 			int amnt = this.fillInternal(tank, ghost, true);
 			tn.drain(side.getOpposite(), amnt, true);
 		}
 	}
 
 	@Override
 	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
 	{
 		SocketModule m = getSide(direction);
 		return m.isEnergyInterface(configs[direction.ordinal()]) && m.acceptsEnergy(configs[direction.ordinal()]);
 	}
 
 	/*@Override
 	public boolean isAddedToEnergyNet()
 	{
 		return addedToEnergyNet;
 	}*/
 
 	@Override
 	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
 	{
 		SocketModule m = getSide(direction);
 		return m.isEnergyInterface(configs[direction.ordinal()]) && m.outputsEnergy(configs[direction.ordinal()]);
 	}
 
 	/*@Override
 	public int getMaxEnergyOutput()
 	{
 		return 512;
 	}*/
 
 	/*@Override
 	public int demandsEnergy(ForgeDirection direction)
 	{
 		return Math.min((int)(SocketsMod.EUPerMJ * 100), (int)powerHandler.getMaxEnergyStored() - (int)powerHandler.getEnergyStored());
 	}*/
 
 	/*@Override
 	public int injectEnergy(ForgeDirection directionFrom, int amount)
 	{
 		SocketModule m = getSide(directionFrom);
 		if(m.isEnergyInterface(configs[directionFrom.ordinal()]) && m.acceptsEnergy(configs[directionFrom.ordinal()]))
 		{
 			int euAmnt = (int)(amount * SocketsMod.EUPerMJ);
 			int amnt = Math.min(amount, (int)powerHandler.getMaxEnergyStored() - (int)powerHandler.getEnergyStored());
 			powerHandler.receiveEnergy(amnt, directionFrom);
 			return amount - amnt;
 		}
 		
 		return 0;
 	}*/
 
 	/*@Override
 	public int getMaxSafeInput()
 	{
 		return 100000;
 	}*/
 
 	@Override
 	public void sendClientSideState(int side)
 	{
 		PacketHandler.instance.SendClientSideState(this, (byte)side);
 	}
 	
 	@Override
 	public void sendClientInventorySlot(int inv)
 	{
 		PacketHandler.instance.SendClientInventorySlot(this, inv);
 	}
 	
 	@Override
 	public void sendClientTankSlot(int tank)
 	{
 		PacketHandler.instance.SendClientTankSlot(this, tank);
 	}
 
 	@Override
 	public void outputEnergy(int mjMax, int ic2PacketSize, ForgeDirection side)
 	{
 		int xo = xCoord + side.offsetX;
 		int yo = yCoord + side.offsetY;
 		int zo = zCoord + side.offsetZ;
 		
 		TileEntity t = worldObj.getBlockTileEntity(xo, yo, zo);
 		
 		if(t != null && t instanceof IPowerReceptor)
 		{
 			PowerReceiver receptor = ((IPowerReceptor)t).getPowerReceiver(side.getOpposite());
 			
 			int amnt = 0;
 			
 			if(receptor != null && powerHandler.getEnergyStored() > receptor.getMinEnergyReceived())
 			{
 				amnt = (int)Math.min(receptor.getMaxEnergyReceived(), receptor.getMaxEnergyStored() - (int)receptor.getEnergyStored());
 				amnt = Math.min((int)powerHandler.getEnergyStored(), amnt);
 				amnt = Math.min(amnt, mjMax);
 				
 				powerHandler.useEnergy(amnt, amnt, true);
 				receptor.receiveEnergy(PowerHandler.Type.STORAGE, amnt, side.getOpposite());
 			}
 		}
 		/*else if(t != null && t instanceof IEnergyAcceptor)
 		{
 
 			int amnt = Math.min((int)powerHandler.getEnergyStored(), (int)(ic2PacketSize/SocketsMod.EUPerMJ));
 			EnergyTileSourceEvent evt = new EnergyTileSourceEvent(this,(int)(SocketsMod.EUPerMJ * amnt));
 			MinecraftForge.EVENT_BUS.post(evt);
 			int use = Math.max(0, amnt - evt.amount);
 			powerHandler.useEnergy(use, use, true);
 		}*/
 	}
 	
 	@Override
 	public int getMaxEnergyStored()
 	{
 		return (int)powerHandler.getMaxEnergyStored();
 	}
 	
 	@Override
 	public float getCurrentEnergyStored()
 	{
 		return powerHandler.getEnergyStored();
 	}
 
 	@Override
 	public void setMaxEnergyStored(int newMax)
 	{
 		float used = Math.max(getCurrentEnergyStored() - newMax, 0);
 		powerHandler.useEnergy(used, used, true);
 		//powerHandler.configure(0, 0, 100, 0, newMax);
 		powerHandler.configure(0.0F, 1000.0F, 0.0F, newMax);
 	}
 	
 	@Override
 	public float useEnergy(float toUse, boolean doUse)
 	{
 		return powerHandler.useEnergy(toUse, toUse, doUse);
 	}
 
 	@Override
 	public void addEnergy(float energy, ForgeDirection side)
 	{
 		//receiveEnergy(energy, side);
 		this.getPowerReceiver(ForgeDirection.UP).receiveEnergy(PowerHandler.Type.PIPE, energy, side);
 		
 	}
 
 	@Override
 	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
 	{
 		if (resource == null || !resource.isFluidEqual(drain(from, resource.amount, false)))
         {
             return null;
         }
         return drain(from, resource.amount, doDrain);
 	}
 
 	@Override
 	public boolean canFill(ForgeDirection from, Fluid fluid)
 	{
 		return this.getSide(from).canInsertFluid();
 	}
 
 	@Override
 	public boolean canDrain(ForgeDirection from, Fluid fluid)
 	{
 		return this.getSide(from).canExtractFluid();
 	}
 
 	@Override
     public FluidTankInfo[] getTankInfo(ForgeDirection from)
     {
         return new FluidTankInfo[] { tanks[0].getInfo(), tanks[1].getInfo(), tanks[2].getInfo() };
     }
 	
 	/*
 	 * IPowerReceptor
 	 * 
 	 */
 
 	@Override
 	public PowerReceiver getPowerReceiver(ForgeDirection side)
 	{
 		return powerHandler.getPowerReceiver();
 	}
 
 	@Override
 	public void doWork(PowerHandler workProvider)
 	{
 		
 	}
 
 	@Override
 	public World getWorld()
 	{
 		return worldObj;
 	}
 
 	@Override
 	public FluidStack getFluidInTank(int tank)
 	{
 		if(tank < 0 || tank >= 3) return null;
 		return tanks[tank].getFluid();
 	}
 
 	@Override
 	public double getOfferedEnergy()
 	{
 		//return Math.min(powerHandler.getEnergyStored(), arg1)
 		return Math.min((int)powerHandler.getEnergyStored() * SocketsMod.EUPerMJ, 512);
 	}
 
 	@Override
 	public void drawEnergy(double amount)
 	{
 		powerHandler.setEnergy((float)Math.max(0, powerHandler.getEnergyStored() - (amount/SocketsMod.EUPerMJ)));
 	}
 
 	@Override
 	public double demandedEnergyUnits()
 	{
 		int euAmnt = (int)(512 / SocketsMod.EUPerMJ);
 		int amnt = (int)Math.min(euAmnt, (int)powerHandler.getMaxEnergyStored() - (int)powerHandler.getEnergyStored());
 		if(amnt <= 10) amnt = 0;
 		return amnt * SocketsMod.EUPerMJ;
 	}
 
 	@Override
 	public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
 	{
 		double amntMJ = amount/SocketsMod.EUPerMJ;
 		double result = amount;
 		if(powerHandler.getEnergyStored() < powerHandler.getMaxEnergyStored() - 10)
 		{
 			this.powerHandler.getPowerReceiver().receiveEnergy(Type.PIPE, (float)amntMJ, ForgeDirection.UP);
 			result = 0.0D;
 		}
 		
 		return result;
 	}
 
 	@Override
 	public int getMaxSafeInput()
 	{
 		return 1000000;
 	}
 
 	@Override
 	public int forceOutputItem(ItemStack stack, boolean doOutput)
 	{
 		int origAmnt = stack.stackSize;
 		
 		for(int i = 0; i < 6; i++)
 		{
 			ForgeDirection d = ForgeDirection.getOrientation(i);
 			if(getSide(d) instanceof ModMachineOutput)
 			{
 				SideConfig mConfig = configs[i];
 				if(mConfig.inventory >= 0 && mConfig.inventory < 3)
 				{
 					int amnt = this.addItemInternal(stack, doOutput, mConfig.inventory);
 					if(doOutput)stack.stackSize -= amnt;
 					getSide(d).updateSide(configs[i], this, d);
 					return origAmnt - amnt;
 				}
 			}
 		}
 		
 		return 0;
 	}
 
 
 	@Override
 	public int forceOutputFluid(FluidStack stack, boolean doOutput)
 	{
 		int origAmnt = stack.amount;
 		for(int i = 0; i < 6; i++)
 		{
 			ForgeDirection d = ForgeDirection.getOrientation(i);
 			if(getSide(d) instanceof ModMachineOutput)
 			{
 				SideConfig mConfig = configs[i];
 				if(mConfig.tank >= 0 && mConfig.tank < 3)
 				{
 					int amnt = this.fillInternal(mConfig.tank, stack, doOutput);
 					if(doOutput)stack.amount -= amnt;
 					getSide(d).updateSide(configs[i], this, d);
 					return origAmnt - amnt;
 				}
 			}
 		}
 		
 		return 0;
 		
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getTexture(int texture, int moduleID)
 	{
 		return ((BlockSocket)SocketsMod.socket).textures[moduleID][texture];
 	}
 
 	@Override
 	public int recieveGas(FluidStack gas, ForgeDirection direction, boolean doFill)
 	{
 		return this.fill(direction, gas, doFill);
 	}
 }
