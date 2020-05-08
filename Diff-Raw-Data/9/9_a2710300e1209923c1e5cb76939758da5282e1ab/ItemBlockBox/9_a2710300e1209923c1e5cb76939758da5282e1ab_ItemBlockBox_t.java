 /*******************************************************************************
  * Copyright (c) 2012 Mrbrutal. All rights reserved.
  * @name Logisticraft
  * @author Mrbrutal
  * @licence Lesser GNU Public License v3 http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 
 package si.meansoft.logisticraft.common.items;
 
 import net.minecraft.src.*;
 
 public class ItemBlockBox extends ItemBlock {
 	
 	public ItemBlockBox(int i) {
 		super(i);
 		setMaxDamage(0);
 		setHasSubtypes(true);
 		setMaxStackSize(64);
 	}
	public String[] blockNames = { "Block of wheat", "Block of sugarcane", "Apple basket", "Egg basket", "Cake basket", "Bread basket", "Zombie basket", "Cookie basket", "Watermelon basket", "Box of pork", "Box of fish", "Box of beef", "Box of chicken", "SlimeBall basket", "Box of watermelon", "Compost basket" };
 
 	public String getItemNameIS(ItemStack itemstack) {
 		return(new StringBuilder()).append(super.getItemName()).append(".").append(blockNames[itemstack.getItemDamage()]).toString();
 	}
 	
 	public int getMetadata(int i) {
 		return i;
 	}
 	
 }
