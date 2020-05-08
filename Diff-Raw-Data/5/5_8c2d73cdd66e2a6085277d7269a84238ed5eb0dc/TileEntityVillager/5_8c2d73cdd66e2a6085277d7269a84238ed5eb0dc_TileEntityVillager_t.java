 package mods.alice.villagerblock.tileentity;
 
 import java.util.List;
 
 import buildcraft.api.inventory.ISpecialInventory;
 import cpw.mods.fml.common.registry.VillagerRegistry;
 import net.minecraft.entity.IMerchant;
 import net.minecraft.entity.passive.EntityVillager;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTBase;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.village.MerchantRecipe;
 import net.minecraft.village.MerchantRecipeList;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 
 public class TileEntityVillager extends TileEntity implements ISpecialInventory, ISidedInventory, IMerchant
 {
 	static final int sideIndexes[] = new int[] {0, 1};
 	static final int topIndexes[] = new int[] {0, 1};
 	static final int bottomIndexes[] = new int[] {2};
 	static List<Item> itemList;
 	MerchantRecipeList tradeList;
 	ItemStack tradeInventory[];
 	int profession;
 
 	public TileEntityVillager()
 	{
 		this((World)null);
 	}
 
 	public TileEntityVillager(World world)
 	{
 		EntityVillager dummyVillager;
 		IMerchant merchant;
 
 		worldObj = world;
 		dummyVillager = new EntityVillager(world);
 		VillagerRegistry.applyRandomTrade(dummyVillager, dummyVillager.getRNG());
 
 		profession = dummyVillager.getProfession();
 
 		merchant = dummyVillager;
 		tradeList = merchant.getRecipes(null);
 
 		tradeInventory = new ItemStack[3];
 	}
 
 	@Override
 	public Packet getDescriptionPacket()
 	{
 		NBTTagCompound tag;
 
 		tag = new NBTTagCompound();
 		writeToNBT(tag);
 
 		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 1, tag);
 	}
 
 	@Override
 	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
 	{
 		if((pkt.xPosition == xCoord) && (pkt.yPosition == yCoord) && (pkt.zPosition == zCoord))
 		{
 			if(pkt.customParam1 != null)
 			{
 				readFromNBT(pkt.customParam1);
 			}
 		}
 	}
 
 	public void setProfession(int _profession)
 	{
 		profession = _profession;
 	}
 
 	public int getProfession()
 	{
 		return profession;
 	}
 
 	public MerchantRecipeList getTrades()
 	{
 		return tradeList;
 	}
 
 	public void setTrades(MerchantRecipeList _tradeList)
 	{
 		tradeList = _tradeList;
 	}
 
 	public ItemStack[] getItemStacks()
 	{
 		return tradeInventory;
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound tagCompound)
 	{
 		MerchantRecipeList savedTrade;
 		NBTBase base;
 		NBTTagCompound savedTradeRecipes;
 		NBTTagList items;
 		int i, slot, tags;
 
 		super.readFromNBT(tagCompound);
 
 		if(tagCompound.hasKey("Items"))
 		{
 			items = tagCompound.getTagList("Items");
 			if(items != null)
 			{
 				tags = items.tagCount();
 				if(tags > 0)
 				{
 					for(i = 0; i < tags; i++)
 					{
 						if(i >= tradeInventory.length)
 						{
 							break;
 						}
 						base = items.tagAt(i);
 						if(base instanceof NBTTagCompound)
 						{
 							try
 							{
 								slot = ((NBTTagCompound)base).getByte("Slot") & 255;
 								tradeInventory[slot] = ItemStack.loadItemStackFromNBT((NBTTagCompound)base);
 							}
 							catch(Exception e)
 							{
 							}
 						}
 					}
 				}
 			}
 		}
 
 		savedTrade = null;
 
 		profession = tagCompound.getInteger("Profession");
 
 		savedTradeRecipes = tagCompound.getCompoundTag("Trade");
 		if(savedTradeRecipes != null)
 		{
 			savedTrade = new MerchantRecipeList(savedTradeRecipes);
 		}
 
 		if((savedTrade != null) && (savedTrade.size() > 0))
 		{
 			tradeList = savedTrade;
 		}
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound tagCompound)
 	{
 		NBTTagCompound item, saveTradeRecipes;
 		NBTTagList items;
 		byte i;
 
 		super.writeToNBT(tagCompound);
 
 		items = new NBTTagList();
 		for(i = 0; i < 3; i++)
 		{
 			item = new NBTTagCompound();
 			if(tradeInventory[i] != null)
 			{
 				tradeInventory[i].writeToNBT(item);
 				item.setByte("Slot", i);
 				items.appendTag(item);
 			}
 		}
 		tagCompound.setTag("Items", items);
 
 		tagCompound.setInteger("Profession", profession);
 
 		saveTradeRecipes = tradeList.getRecipiesAsTags();
 		tagCompound.setCompoundTag("Trade", saveTradeRecipes);
 	}
 
 	@Override
 	public void updateEntity()
 	{
 		ItemStack stack;
 		ItemStack buyStack[];
 		ItemStack tradeInventorySwap[];
 		MerchantRecipe recipe;
 		int stackCount;
 		byte i;
 
 		if(worldObj.isRemote)
 		{
 			return;
 		}
 
 		if(worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
 		{
 			return;
 		}
 
 		if((tradeInventory[0] == null) && (tradeInventory[1] == null))
 		{
 			// 交換対象のアイテムが入っていない場合は何もしない。
 			return;
 		}
 
 		if(tradeInventory[2] != null)
 		{
 			stackCount = tradeInventory[2].stackSize;
 			if(stackCount >= tradeInventory[2].getMaxStackSize())
 			{
 				// これ以上スタックできない場合は何もしない。
 				return;
 			}
 		}
 
 		// 入れたアイテムが交換対象かどうかを確認する。
 		try
 		{
 			recipe = tradeList.canRecipeBeUsed(tradeInventory[0], tradeInventory[1], 0);
 		}
 		catch(NullPointerException e)
 		{
 			recipe = null;
 		}
 		if(recipe == null)
 		{
 			try
 			{
 				recipe = tradeList.canRecipeBeUsed(tradeInventory[1], tradeInventory[0], 0);
 			}
 			catch(NullPointerException e)
 			{
 				recipe = null;
 			}
 			if(recipe == null)
 			{
 				return;
 			}
 		}
 
 		buyStack = new ItemStack[2];
 		stack = recipe.getItemToSell();
 		if(stack == null)
 		{
 			return;
 		}
 
 		if(tradeInventory[2] != null)
 		{
 			stackCount = tradeInventory[2].stackSize;
 			if((stackCount + stack.stackSize) > tradeInventory[2].getMaxStackSize())
 			{
 				return;
 			}
 		}
 
 		buyStack[0] = recipe.getItemToBuy();
 		buyStack[1] = recipe.getSecondItemToBuy();
 
 		if((buyStack[0] != null) && (buyStack[1] == null))
 		{
 			if(tradeInventory[0] != null)
 			{
 				i = 0;
 			}
 			else
 			{
 				i = 1;
 			}
 
			if(tradeInventory[i].stackSize < buyStack[0].stackSize)
 			{
 				return;
 			}
			tradeInventory[i].stackSize -= buyStack[0].stackSize;
 			if(tradeInventory[i].stackSize <= 0)
 			{
 				tradeInventory[i] = null;
 			}
 		}
 		else
 		{
 			tradeInventorySwap = new ItemStack[2];
 
 			if((tradeInventory[0] != null) && (tradeInventory[1] != null))
 			{
 				if(buyStack[0].itemID == tradeInventory[0].itemID)
 				{
 					tradeInventorySwap[0] = tradeInventory[0];
 					tradeInventorySwap[1] = tradeInventory[1];
 				}
 				else
 				{
 					tradeInventorySwap[0] = tradeInventory[1];
 					tradeInventorySwap[1] = tradeInventory[0];
 				}
 
 				if((tradeInventorySwap[0].stackSize < buyStack[0].stackSize) || (tradeInventorySwap[1].stackSize < buyStack[1].stackSize))
 				{
 					return;
 				}
 				tradeInventorySwap[0].stackSize -= buyStack[0].stackSize;
 				if(tradeInventorySwap[0].stackSize <= 0)
 				{
 					tradeInventorySwap[0] = null;
 				}
 				tradeInventorySwap[1].stackSize -= buyStack[1].stackSize;
 				if(tradeInventorySwap[1].stackSize <= 0)
 				{
 					tradeInventorySwap[1] = null;
 				}
 			}
 		}
 
 		if(tradeInventory[2] == null)
 		{
 			tradeInventory[2] = stack.copy();
 		}
 		else
 		{
 			tradeInventory[2].stackSize += stack.stackSize;
 		}
 	}
 
 	@Override
 	public int getSizeInventory()
 	{
 		return tradeInventory.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int index)
 	{
 		if((index < 0) || (index >= tradeInventory.length))
 		{
 			return null;
 		}
 
 		return tradeInventory[index];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int index, int amount)
 	{
 		ItemStack itemstack;
 
 		if((index < 0) || (index >= tradeInventory.length))
 		{
 			return null;
 		}
 
 		if(tradeInventory[index] != null)
 		{
 			if(tradeInventory[index].stackSize <= amount)
 			{
 				itemstack = tradeInventory[index];
 				tradeInventory[index] = null;
 				return itemstack;
 			}
 			else
 			{
 				itemstack = tradeInventory[index].splitStack(amount);
 
 				if(tradeInventory[index].stackSize == 0)
 				{
 					tradeInventory[index] = null;
 				}
 
 				return itemstack;
 			}
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int index)
 	{
 		return null;
 	}
 
 	@Override
 	public void setInventorySlotContents(int index, ItemStack itemStack)
 	{
 		if((index < 0) || (index >= tradeInventory.length))
 		{
 			return;
 		}
 
 		tradeInventory[index] = itemStack;
 
 		if((itemStack != null) && (itemStack.stackSize > getInventoryStackLimit()))
 		{
 			itemStack.stackSize = getInventoryStackLimit();
 		}
 	}
 
 	@Override
 	public String getInvName()
 	{
 		return "container.villagerblock";
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
 	public boolean isUseableByPlayer(EntityPlayer player)
 	{
 		if(worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
 		{
 			return false;
 		}
 		if(player.getDistanceSq((double)xCoord + 0.5, (double)yCoord + 0.5, (double)zCoord + 0.5) > 64)
 		{
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void openChest()
 	{
 	}
 
 	@Override
 	public void closeChest()
 	{
 	}
 
 	@Override
 	public boolean isStackValidForSlot(int index, ItemStack itemstack)
 	{
 		return true;
 	}
 
 	@Override
 	public int[] getSizeInventorySide(int side)
 	{
 		int indexes[];
 
 		switch(side)
 		{
 		case 0: // Bottom
 			indexes = bottomIndexes;
 			break;
 		case 1: // Top
 			indexes = topIndexes;
 			break;
 		default:
 			indexes = sideIndexes; // DO NOT RETURN NULL!!!
 			break;
 		}
 		return indexes;
 	}
 
 	@Override
 	public boolean func_102007_a(int index, ItemStack itemStack, int j)
 	{
 		return isStackValidForSlot(index, itemStack);
 	}
 
 	@Override
 	public boolean func_102008_b(int i, ItemStack itemstack, int j)
 	{
 		return true;
 	}
 
 	// !!! BUILDCRAFT SPECIFIC !!!
 
 	@Override
 	public int addItem(ItemStack stack, boolean doAdd, ForgeDirection from)
 	{
 		// BuildCraftのパイプを使う場合は、全方向対応とする。
 
 		int stackSize;
 		byte i;
 
 		for(i = 0; i < 2; i++)
 		{
 			if(tradeInventory[i] != null)
 			{
 				if(tradeInventory[i].itemID == stack.itemID)
 				{
 					if(tradeInventory[i].isStackable())
 					{
 						if(tradeInventory[i].stackSize < tradeInventory[i].getMaxStackSize())
 						{
 							break;
 						}
 					}
 				}
 			}
 			else
 			{
 				break;
 			}
 		}
 
 		if(i == 2)
 		{
 			return 0;
 		}
 
 		if(tradeInventory[i] == null)
 		{
 			stackSize = stack.stackSize;
 			if(doAdd)
 			{
 				tradeInventory[i] = stack.copy();
 			}
 		}
 		else
 		{
 			stackSize = tradeInventory[i].getMaxStackSize() - tradeInventory[i].stackSize;
 			if(stackSize > stack.stackSize)
 			{
 				stackSize = stack.stackSize;
 			}
 			else
 			{
 				stackSize = stackSize - stack.stackSize;
 			}
 			if(doAdd)
 			{
 				tradeInventory[i].stackSize += stackSize;
 			}
 		}
 
 		return stackSize;
 	}
 
 	@Override
 	public ItemStack[] extractItem(boolean doRemove, ForgeDirection from, int maxItemCount)
 	{
 		// BuildCraftのパイプを使う場合は、全方向対応とする。
 		ItemStack stack[];
 
 		if(tradeInventory[2] == null)
 		{
 			return null;
 		}
 
 		stack = new ItemStack[1];
 		stack[0] = tradeInventory[2].copy();
 		if(tradeInventory[2].stackSize > maxItemCount)
 		{
 			stack[0].stackSize = maxItemCount;
 			if(doRemove)
 			{
 				tradeInventory[2].stackSize -= maxItemCount;
 			}
 		}
 		else
 		{
 			if(doRemove)
 			{
 				tradeInventory[2] = null;
 			}
 		}
 
 		return stack;
 	}
 
 	@Override
 	public void setCustomer(EntityPlayer entityplayer)
 	{
 	}
 
 	@Override
 	public EntityPlayer getCustomer()
 	{
 		return null;
 	}
 
 	@Override
 	public MerchantRecipeList getRecipes(EntityPlayer entityplayer)
 	{
 		return tradeList;
 	}
 
 	@Override
 	public void setRecipes(MerchantRecipeList merchantrecipelist)
 	{
 		tradeList = merchantrecipelist;
 	}
 
 	@Override
 	public void useRecipe(MerchantRecipe merchantrecipe)
 	{
 	}
 }
