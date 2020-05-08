 package com.cane.tileentity;
 
 import com.cane.CaneCraft;
 import com.cane.PowerProviderCC;
 import com.cane.item.ItemCane;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.ISidedInventory;
 
 
 public class TileEntityExtractor extends TileEntityMachineProcess implements ISidedInventory
 {
 	private boolean tier2;
 	
 	public TileEntityExtractor()
 	{
 		items = new ItemStack[3];
 		provider = new PowerProviderCC(20, 4000);
 	}
 	
 	public void setTier2(boolean tier2)
 	{
 		this.tier2 = tier2;
 	}
 	
 	public boolean getTier2()
 	{
 		return tier2;
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound tag)
 	{
 		super.writeToNBT(tag);
 		tag.setBoolean("tier2", this.tier2);
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound tag)
 	{
 		super.readFromNBT(tag);
 		
 		this.tier2 = tag.getBoolean("tier2");
 	}
 	
 	@Override
 	protected boolean needNewCurrent()
 	{
 		if(itemExist(current)) return false;
 		if(!itemExist(items[0])) return false;
 		if(overSpace(items[1], 1) > 0) return false;
 		if(overSpace(items[2], 1) > 0) return false;
 		ItemStack out = getOutput(items[0]);
 		
 		if(getTime(out) * 4 > provider.getEnergyStored())
 		{
 			return false;
 		}
 		
 		if(out != null && (items[1] == null || out.isItemEqual(items[1]) &&
 				overSpace(items[1], out.stackSize) <= 0))
 		{
 			return true;
 		}
 		
 		return false;
 
 	}
 	
 	@Override
 	protected void finish()
 	{
 		ItemStack out = getOutput(current);
 		current = null;
 		
 		if(out == null) return;
 		
 		addOutput(out);
 		
 		if(tier2 || (Math.random() * 10) < 1)
 		{
 			addEmptyCane();
 		}
 	}
 	
 	@Override
 	protected void eject()
 	{
 		if(items[1] != null)
 		{
 			ejectItem(1, items[1].stackSize, orange);
 		}
 		if(items[2] != null)
 		{
 			ejectItem(2, items[2].stackSize, orange);
 		}
 	}
 
 	@Override
 	protected int getFaceTexture()
 	{
 		return tier2 ? 1 : 0;
 	}
 	
 	@Override
 	public ItemStack getOutput(ItemStack in)
 	{
 		if(in == null) return null;
 		
 		if(in.getItem() == CaneCraft.Items.cane)
 		{
 			if(!tier2 & in.getItemDamage() > 7){return null;}
 			
 			return ItemCane.output[in.getItemDamage()];
 		}
 		
 		if(in.getItem() == Item.reed)
 		{
 			return new ItemStack(Item.sugar);
 		}
 		
 		return null;
 	}
 
 	@Override
 	public int getTime(ItemStack is)
 	{
 		if(is == null) return 0;
 		
 		if(is.getItemDamage() < 6)
 		{
 			return 60;
 		}
 		if(is.getItemDamage() < 15)
 		{
 			return 100;
 		}
 		if(is.getItemDamage() == 15)
 		{
 			return 250;
 		}
 		
 		return 0;
 	}
 	
 	public void addOutput(ItemStack is)
 	{
 		if(items[1] == null)
 		{
 			items[1] = is.copy();
 		}
 		else
 		{
			items[1].stackSize = Math.min(items[1].stackSize + is.stackSize, items[1].getMaxStackSize());
 		}
 	}
 	
 	public void addEmptyCane()
 	{
 		if(items[2] == null)
 		{
 			items[2] = new ItemStack(CaneCraft.Items.caneUtil, 1, 0);
 		}
 		else
 		{
 			items[2].stackSize++;
 		}
 	}
 
 	@Override
 	public int getSizeInventorySide(ForgeDirection side)
 	{
 		if(side.ordinal() == orange)
 		{
 			return 2;
 		}
 		return 1;
 	}
 
 	@Override
 	public int getStartInventorySide(ForgeDirection side)
 	{
 		if(side.ordinal() == orange)
 		{
 			return 1;
 		}
 		return 0;
 	}
 
 	@Override
 	public String getInvName()
 	{
 		return tier2 ? "EXTRACTOR2" : "EXTRACTOR1";
 	}
 	
 	public String getDisplayName()
 	{
 		return tier2 ? "Extractor mark II" : "Extractor mark I";
 	}
 	
 	@Override
 	protected int getPowerUsage()
 	{
 		return 4;
 	}
 }
