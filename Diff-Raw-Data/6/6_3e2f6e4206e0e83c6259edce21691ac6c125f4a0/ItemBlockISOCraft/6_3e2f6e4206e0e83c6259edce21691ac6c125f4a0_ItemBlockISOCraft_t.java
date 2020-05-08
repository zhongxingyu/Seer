 package com.isocraft.item;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 
 import com.isocraft.block.ISOCraftBlock;
 import com.isocraft.block.ISOCraftBlockMachine;
import com.isocraft.block.eridiumnet.BlockEridiumNetMajorNode;
 
 /**
  * ISOCraft
  * 
  * This class manages the ore block when it is an inventory
  * 
  * @author Turnermator13
  */
 
 public class ItemBlockISOCraft extends ItemBlock {
 
 	public ItemBlockISOCraft(Block b) {
 		super(b);
 		this.setHasSubtypes(true);
 	}
 
 	@Override
 	public String getUnlocalizedName(ItemStack itemstack) {
 		String name = "";
 
 		if (this.field_150939_a.getClass().getSuperclass().equals(ISOCraftBlock.class)) {
 			name = ((ISOCraftBlock) this.field_150939_a).subNames[itemstack.getItemDamage()];
 			return this.getUnlocalizedName() + "." + name;
 		}
 		else if (this.field_150939_a.getClass().getSuperclass().equals(ISOCraftBlockMachine.class)) {
 			name = ((ISOCraftBlockMachine) this.field_150939_a).subNames[itemstack.getItemDamage()];
 			return this.getUnlocalizedName() + "." + name;
 		}
		else if (this.field_150939_a.getClass().getSuperclass().equals(BlockEridiumNetMajorNode.class)) {
			name = ((ISOCraftBlockMachine) this.field_150939_a).subNames[itemstack.getItemDamage()];
			return this.getUnlocalizedName() + "." + name;
		}
 		else {
 			return this.getUnlocalizedName();
 		}
 	}
 
 	@Override
 	public int getMetadata(int damage) {
 		return damage;
 	}
 }
