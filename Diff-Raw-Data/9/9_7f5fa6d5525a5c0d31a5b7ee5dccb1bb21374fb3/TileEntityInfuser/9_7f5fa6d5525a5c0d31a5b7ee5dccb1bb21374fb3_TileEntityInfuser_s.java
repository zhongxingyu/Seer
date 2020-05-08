 package com.cane.tileentity;
 
 import com.cane.CaneCraft;
 import com.cane.PowerProviderCC;
 import com.cane.CaneCraft.Items;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.ISidedInventory;
 
 
 public class TileEntityInfuser extends TileEntityMachineProcess implements ISidedInventory
 {
 	public TileEntityInfuser()
 	{
 		items = new ItemStack[2];
 		provider = new PowerProviderCC(20, 4000);
 	}
 	
 	@Override
 	protected boolean needNewCurrent()
 	{
 		if(itemExist(current)) return false;
 		if(!itemExist(items[0])) return false;
 		if(overSpace(items[1], 1) > 0) return false;
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
 	}
 	
 	@Override
 	protected void eject()
 	{
 		if(items[1] != null)
 		{
 			ejectItem(1, items[1].stackSize, orange);
 		}
 	}
 
 	@Override
 	protected int getFaceTexture()
 	{
 		return 2;
 	}
 	
 	@Override
 	public ItemStack getOutput(ItemStack in)
 	{
 		if(in == null) return null;
 		
 		if(in.getItem() == CaneCraft.Items.caneUtil &&
 				in.getItemDamage() > 0 & in.getItemDamage() < 16)
 		{
 			return new ItemStack(Items.cane, 1, in.getItemDamage() - 1);
 		}
 		
 		return null;
 	}
 
 	@Override
 	public int getTime(ItemStack is)
 	{
 		if(is == null) return 0;
 		
 		if(is.getItemDamage() < 8)
 		{
 			return 60;
 		}
 		if(is.getItemDamage() < 15)
 		{
 			return 100;
 		}
 		if(is.getItemDamage() == 15)
 		{
 			return 160;
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
			items[1].stackSize = Math.min(items[1].stackSize + is.stackSize, items[0].getMaxStackSize());
 		}
 	}
 
 	@Override
 	public int getSizeInventorySide(ForgeDirection side)
 	{
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
 		return "INFUSER";
 	}
 	
 	public String getDisplayName()
 	{
 		return "Cane Infuser";
 	}
 	
 	@Override
 	protected int getPowerUsage()
 	{
 		return 4;
 	}
 }
