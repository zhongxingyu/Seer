 package com.isocraft.item;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import net.minecraftforge.common.util.ForgeDirection;
 
 import com.isocraft.entity.EntityEridiumFluxShell;
 import com.isocraft.lib.ItemInfo;
 
 /**
  * ISOCraft
  * 
  * Item class for the tab icon item
  * 
  * @author Turnermator13
  */
 
 public class ItemTabIcon extends ISOCraftItem {
 
 	public ItemTabIcon() {
 		super(true);
 		this.setUnlocalizedName(ItemInfo.TabIcon_name);
 		this.setHasSubtypes(false);
 		this.setMaxDamage(0);
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer e) {
		world.spawnEntityInWorld(new EntityEridiumFluxShell(world, e.posX, e.posY, e.posZ, ForgeDirection.NORTH, 6));
 		return itemStack;
 	}
 }
