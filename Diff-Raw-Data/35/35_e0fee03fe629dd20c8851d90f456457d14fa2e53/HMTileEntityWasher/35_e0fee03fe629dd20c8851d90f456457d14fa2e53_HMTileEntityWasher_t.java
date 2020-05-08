 
 package hawksmachinery.tileentity;
 
 import hawksmachinery.HawksMachinery;
 import hawksmachinery.api.HMProcessingRecipes;
 import hawksmachinery.api.HMProcessingRecipes.HMEnumProcessing;
 import net.minecraft.src.Block;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.INetworkManager;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.Packet;
 import net.minecraft.src.Packet250CustomPayload;
 import net.minecraftforge.common.ForgeDirection;
 import universalelectricity.core.electricity.ElectricInfo;
 import universalelectricity.core.implement.IItemElectric;
 import universalelectricity.prefab.network.PacketManager;
 import com.google.common.io.ByteArrayDataInput;
 import cpw.mods.fml.common.FMLCommonHandler;
 
 /**
  * 
  * 
  * 
  * @author Elusivehawk
  */
 public class HMTileEntityWasher extends HMTileEntityMachine
 {
 	public float waterUnits = 0;
 	
 	public float WATER_LIMIT = 25.0F;
 	
 	public HMTileEntityWasher()
 	{
 		super();
 		ELECTRICITY_REQUIRED = 10;
 		TICKS_REQUIRED = FMLCommonHandler.instance().getSide().isServer() ? HawksMachinery.MANAGER.crusherTicks : 100;
 		ELECTRICITY_LIMIT = 1200;
 		containingItems = new ItemStack[6];
 		machineEnum = HMEnumProcessing.WASHING;
 		voltage = 120;
 		isProcessor = true;
 		
 	}
 	
 	@Override
 	public void updateEntity()
 	{
 		super.updateEntity();
 		
 		if (!this.isDisabled())
 		{
 			if (this.containingItems[0] != null)
 			{
 				if (this.containingItems[0].getItem() instanceof IItemElectric)
 				{
 					IItemElectric electricItem = (IItemElectric)this.containingItems[0].getItem();
 					
 					if (electricItem.canProduceElectricity() && this.electricityStored > this.ELECTRICITY_LIMIT)
 					{
 						double receivedElectricity = electricItem.onUse(Math.min(electricItem.getMaxJoules()*0.01, ElectricInfo.getWattHours(this.ELECTRICITY_REQUIRED)), this.containingItems[0]);
 						this.electricityStored += ElectricInfo.getWatts(receivedElectricity);
 					}
 					
 				}
 				
 			}
 			
 			if (this.containingItems[1] != null)
 			{
 				if (this.containingItems[1].getItem() == Item.bucketWater && this.waterUnits + 1.0F <= this.WATER_LIMIT)
 				{
 					this.waterUnits += 1.0;
 					this.containingItems[1] = new ItemStack(Item.bucketEmpty, 1);
 				}
 				
 			}
 			
 			if (this.canWash())
 			{
 				if (this.containingItems[2] != null && this.workTicks == 0)
 				{
 					this.workTicks = this.TICKS_REQUIRED;
 				}
 				
 				if (this.canWash() && this.workTicks > 0)
 				{
 					--this.workTicks;
 					this.waterUnits -= 0.01F;
 					
 					if (this.workTicks == 1)
 					{
 						this.washItem();
 						this.workTicks = 0;
 					}
 					
 					this.electricityStored = this.electricityStored - this.ELECTRICITY_REQUIRED;
 				}
 				else
 				{
 					this.workTicks = 0;
 				}
 			}
 			
 			if (this.waterUnits > this.WATER_LIMIT)
 			{
 				this.waterUnits = this.WATER_LIMIT;
 			}
 			
 			if (this.worldObj.getBlockId(this.xCoord, this.yCoord + 1, this.zCoord) == Block.waterStill.blockID && this.waterUnits + 1.0F <= this.WATER_LIMIT)
 			{
 				this.waterUnits += 1.0F;
 				this.worldObj.setBlockWithNotify(this.xCoord, this.yCoord + 1, this.zCoord, 0);
 			}
 			
 			if (!this.canWash() && this.workTicks != 0)
 			{
 				this.workTicks = 0;
 			}
 			
 		}
 		
 	}
 	
 	public boolean canWash()
 	{
 		if (this.containingItems[2] == null)
 		{
 			return false;
 		}
 		else
 		{
 			if (this.electricityStored >= this.ELECTRICITY_REQUIRED * 2 && this.waterUnits >= 1.0F && !this.isDisabled())
 			{
 				ItemStack var1 = HMProcessingRecipes.getResult(this.containingItems[2], this.machineEnum);
 				if (var1 == null) return false;
 				if (this.containingItems[3] == null) return true;
 				if (!this.containingItems[3].isItemEqual(var1)) return false;
 				int result = containingItems[3].stackSize + var1.stackSize;
 				return (result <= getInventoryStackLimit() && result <= var1.getMaxStackSize());
 			}
 			else
 			{
 				return false;
 			}
 		}
 	}
 	
 	private void washItem()
 	{
 		if (this.canWash())
 		{
 			ItemStack newItem = HMProcessingRecipes.getResult(this.containingItems[2], this.machineEnum);
 			
 			if (this.containingItems[3] == null)
 			{
 				this.containingItems[3] = newItem.copy();
 			}
 			else if (this.containingItems[3].isItemEqual(newItem))
 			{
 				this.containingItems[3].stackSize += newItem.stackSize;
 			}
 			
 			this.containingItems[2].stackSize -= HMProcessingRecipes.getQuantity(this.containingItems[2], this.machineEnum);
 			
 			if (this.containingItems[2].stackSize <= 0)
 			{
 				this.containingItems[2] = null;
 			}
 			
 			this.randomlyDamageSelf();
 			
 		}
 		
 	}
 
 	@Override
 	public Packet getDescriptionPacket()
 	{
 		if (this.isOpen)
 		{
			return PacketManager.getPacket("HawksMachinery", this, this.workTicks, this.electricityStored, this.machineHP, this.waterUnits);
 		}
 		
		return PacketManager.getPacket("HawksMachinery", this, this.electricityStored, this.machineHP, this.waterUnits);
 	}
 	
 	@Override
 	public void handlePacketData(INetworkManager network, int type, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
 	{
 		try
 		{
 			if (this.isOpen)
 			{
 				if (this.isProcessor)
 				{
 					this.workTicks = dataStream.readInt();
 					
 				}
 				
 				this.electricityStored = dataStream.readDouble();
 				
 			}
 			
 			this.machineHP = dataStream.readInt();
 			this.waterUnits = dataStream.readFloat();
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}	
 	}
 	
 	public int getWashingStatus(int par1)
 	{
 		return this.workTicks * par1 / 200;
 	}
 	
 	@Override
 	public int getStartInventorySide(ForgeDirection side)
 	{
 		if (side == ForgeDirection.UP)
 		{
 			return 1;
 		}
 		
 		if (side == ForgeDirection.DOWN)
 		{
 			return 0;
 		}
 		
 		return 2;
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound NBTTag)
 	{
 		super.readFromNBT(NBTTag);
 		this.waterUnits = NBTTag.getFloat("waterUnits");
 		
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound NBTTag)
 	{
 		super.writeToNBT(NBTTag);
 		NBTTag.setFloat("waterUnits", this.waterUnits);
 		
 	}
 	
 	@Override
 	public int getSizeInventorySide(ForgeDirection side)
 	{
 		return 1;
 	}
 	
 	@Override
 	public String getInvName()
 	{
 		return "HMWasher";
 	}
 
 	@Override
 	public double wattRequest()
 	{
 		if (this.isDisabled())
 		{
 			return 0;
 		}
 		else
 		{
 			if (this.canWash() && this.electricityStored + this.ELECTRICITY_REQUIRED <= this.ELECTRICITY_LIMIT)
 			{
 				return this.ELECTRICITY_REQUIRED;
 			}
 			else
 			{
 				if (this.ELECTRICITY_LIMIT != this.electricityStored)
 				{
 					if (this.electricityStored + this.ELECTRICITY_REQUIRED >= this.ELECTRICITY_LIMIT)
 					{
 						return this.ELECTRICITY_LIMIT - this.electricityStored;
 					}
 					else
 					{
 						return this.ELECTRICITY_REQUIRED;
 					}
 					
 				}
 				else
 				{
 					return 0;
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 	/*
 	@Override
 	public ItemStack offerItem(Object source, ItemStack offer)
 	{
 		if (HMProcessingRecipes.getResult(offer, this.machineEnum) != null)
 		{
 			if (this.containingItems[2] == null)
 			{
 				this.containingItems[2] = offer;
 				return null;
 			}
 			else
 			{
 				if (this.containingItems[2].isItemEqual(offer))
 				{
 					if (this.containingItems[2].stackSize + offer.stackSize <= 64)
 					{
 						this.containingItems[2].stackSize += offer.stackSize;
 						return null;
 					}
 					else
 					{
 						int extraAmount = (this.containingItems[1].stackSize + offer.stackSize) - 64;
 						
 						this.containingItems[2].stackSize += offer.stackSize;
 						return new ItemStack(offer.getItem(), extraAmount, offer.getItemDamage());
 					}
 				}
 			}
 		}
 		else
 		{
 			if (offer.getItem() instanceof IItemElectric)
 			{
 				if (((IItemElectric)offer.getItem()).canProduceElectricity())
 				{
 					this.containingItems[0] = offer;
 					return null;
 				}
 			}
 			
 			if (offer.getItem() == Item.bucketWater)
 			{
 				if (this.containingItems[1] == null)
 				{
 					this.containingItems[1] = offer;
 					return null;
 				}
 				else
 				{
 					if (this.containingItems[1].getItem() == Item.bucketEmpty)
 					{
 						this.containingItems[1] = offer;
 						return new ItemStack(Item.bucketEmpty, 1);
 					}
 				}
 			}
 		}
 		
 		return offer;
 	}
 	
 	@Override
 	public ItemStack requestItem(Object source)
 	{
 		return null;
 	}
 	
 	@Override
 	public ItemStack requestItem(Object source, ItemStack request)
 	{
 		return null;
 	}
 	
 	@Override
 	public ItemStack requestItem(Object source, EnumItemType request)
 	{
 		return null;
 	}
 	*/
 	
 }
