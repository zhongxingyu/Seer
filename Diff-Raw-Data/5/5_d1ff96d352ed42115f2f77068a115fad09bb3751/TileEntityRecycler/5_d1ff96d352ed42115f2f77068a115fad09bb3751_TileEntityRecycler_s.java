 package com.cane.tileentity;
 
 import com.cane.CaneCraft;
 import com.cane.PowerProviderCC;
 
 import net.minecraft.item.ItemStack;
 
 public class TileEntityRecycler extends TileEntityMachineProcess
 {
	public static int chanceForScrap = 15;
 	
 	public TileEntityRecycler()
 	{
 		items = new ItemStack[2];
 		provider = new PowerProviderCC(20, 4000);
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
 	protected void finish()
 	{
 		current = null;
 		
 		if((Math.random() * chanceForScrap) < 1)
 		{
 			addScrap();
 		}
 	}
 	
 	public void addScrap()
 	{
 		if(items[1] == null)
 		{
 			items[1] = new ItemStack(CaneCraft.Items.caneUtil, 1, 16);
 		}
 		else
 		{
			items[1].stackSize = Math.min(items[1].stackSize + 1, items[0].getMaxStackSize());
 		}
 	}
 	
 	@Override
 	protected int getFaceTexture()
 	{
 		return 3;
 	}
 
 	@Override
 	protected ItemStack getOutput(ItemStack in)
 	{
 		return new ItemStack(CaneCraft.Items.caneUtil, 1, 16);
 	}
 
 	@Override
 	protected int getPowerUsage()
 	{
 		return 8;
 	}
 
 	@Override
 	protected int getTime(ItemStack in)
 	{
 		return getTime();
 	}
 
 	private int getTime()
 	{
 		return 40;
 	}
 	
 	@Override
 	protected boolean needNewCurrent()
 	{
 		if(itemExist(current)) return false;
 		if(!itemExist(items[0])) return false;
 		if(overSpace(items[1], 1) > 0) return false;
 		
 		if(getTime() * 4 > provider.getEnergyStored())
 		{
 			return false;
 		}
 		
 		return true;
 	}
 
 	@Override
 	public String getDisplayName()
 	{
 		return "Recycler";
 	}
 
 	@Override
 	public String getInvName()
 	{
 		return null;
 	}
 
 }
