 
 package hawksmachinery.items;
 
 import hawksmachinery.HawksMachinery;
 import hawksmachinery.interfaces.HMRepairInterfaces.IHMRepairable;
 import hawksmachinery.interfaces.HMRepairInterfaces.IHMRivet;
 import universalelectricity.prefab.ItemElectric;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EnumAction;
 import net.minecraft.src.EnumMovingObjectType;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.MovingObjectPosition;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 
 /**
  * 
  * 
  * 
  * @author Elusivehawk
  */
 public class HMItemRivetGun extends ItemElectric
 {
 	public static HawksMachinery BASEMOD;
 	
 	public HMItemRivetGun(int id)
 	{
 		super(id, CreativeTabs.tabTools);
 		setTextureFile(BASEMOD.ITEM_TEXTURE_FILE);
 		setItemName("rivetGun");
 		setIconIndex(17);
 		
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player)
 	{
 		if (player.isSneaking() && item.getItemDamage() <= this.getTransferRate())
 		{
 			player.setItemInUse(item, getMaxItemUseDuration(item));
 		}
 		
 		return item;
 	}
 	
 	@Override
 	public ItemStack onFoodEaten(ItemStack item, World world, EntityPlayer player)
 	{
 		MovingObjectPosition locatedBlock = getMovingObjectPositionFromPlayer(world, player, true);
 		
 		if (locatedBlock != null)
 		{
 			if (locatedBlock.typeOfHit == EnumMovingObjectType.TILE);
 			{
 				TileEntity foundBlock = world.getBlockTileEntity(locatedBlock.blockX, locatedBlock.blockY, locatedBlock.blockZ);
 				
 				if (foundBlock instanceof IHMRepairable)
 				{
 					for (int counter = 0; counter <= 8; ++counter)
 					{
 						ItemStack testedItem = player.inventory.mainInventory[counter];
 						
 						if (testedItem != null)
 						{
 							if (testedItem.getItem() instanceof IHMRivet)
 							{
 								int potentialRepairAmount = ((IHMRivet)testedItem.getItem()).getRepairAmount(testedItem);
 								
 								if (potentialRepairAmount > 0)
 								{
 									if (((IHMRepairable)foundBlock).attemptToRepair(potentialRepairAmount))
 									{
 										--testedItem.stackSize;
 										if (testedItem.stackSize == 0)
 										{
 											player.inventory.mainInventory[counter] = null;
 										}
 										
 										player.swingItem();
										item.damageItem(100, null);
 										return item;
 									}
 									
 								}
 								
 							}
 							
 						}
 						
 					}
 					
 				}
 				
 			}
 			
 		}
 		
 		return item;
 	}
 	
 	@Override
 	public EnumAction getItemUseAction(ItemStack item)
 	{
 		return EnumAction.bow;
 	}
 	
 	@Override
 	public int getMaxItemUseDuration(ItemStack item)
 	{
 		return 25;
 	}
 	
 	@Override
 	public double getMaxWattHours()
 	{
 		return 30000;
 	}
 	
 	@Override
 	public double getTransferRate()
 	{
 		return 100;
 	}
 	
 	@Override
 	public double getVoltage()
 	{
 		return 120;
 	}
 	
 	@Override
 	public boolean isFull3D()
 	{
 		return true;
 	}
 	
 }
