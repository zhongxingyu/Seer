 /*******************************************************************************
  * Copyright (c) 2012 Mrbrutal. All rights reserved.
  * 
  * @name Logisticraft
  * @author Mrbrutal
  * @licence Lesser GNU Public License v3 http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 
 package si.meansoft.logisticraft.common.items;
 
 import net.minecraft.src.*;
 
 public class ItemBlockCrate extends ItemBlock {
 	public ItemBlockCrate(int i) {
 		super(i);
 		setMaxDamage(0);
 		setHasSubtypes(true);
 		setMaxStackSize(1);
 	}
	public String[] blockNames = { "Crated wheat", "Crated sugarcane", "Crated apples", "Crated eggs", "Crated cake", "Crated bread", "Crated zombie flesh", "Crated cookies", "Crated melon", "Crated porkchops", "Crated fish", "Crated beef", "Crated chicken", "Crated slimeballs", "Crated watermelon", "Rotten crate"};
 
 	public String getItemNameIS(ItemStack itemstack) {
 		return(new StringBuilder()).append(super.getItemName()).append(".").append(blockNames[itemstack.getItemDamage()]).toString();
 	}
 	
 	public int getMetadata(int i) {
 		return i;
 	}
 	
 }
