 package bau5.mods.craftingsuite.common.recipe;
 
 import java.util.ArrayList;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.oredict.OreDictionary;
 import bau5.mods.craftingsuite.common.CraftingSuite;
 import bau5.mods.craftingsuite.common.ModificationNBTHelper;
 import bau5.mods.craftingsuite.common.tileentity.TileEntityModificationTable;
 
 public class ModificationRecipeUpgrade implements IModRecipe{
 	private ItemStack baseResult;
 	private ItemStack[] possibleInputs;
 	
 	public ModificationRecipeUpgrade(ItemStack basicResult, ItemStack[] inputs){
 		baseResult = basicResult;
 		possibleInputs = inputs;
 	}
 	
 	@Override
 	public ItemStack getOutput() {
 		return baseResult;
 	}
 
 	@Override
 	public ItemStack getExactOutput(ItemStack[] input) {
 		ItemStack[] used = doesRecipeMatch(input);
 		byte[] bytes = ModificationNBTHelper.getUpgradeByteArray(input[0].stackTagCompound).clone();
 		if(bytes == null || used == null)
 			return input[0];
 		for(ItemStack stack : used){
 			if(stack.itemID == Block.carpet.blockID){
 				bytes[3] = (byte) stack.getItemDamage();
 				continue;
 			}
 			if(stack.itemID == Block.blockClay.blockID)
 				bytes[4] = 1;
 			if(stack.itemID == CraftingSuite.modItems.itemID){
 				switch(stack.getItemDamage()){
 				case 3: bytes[1] = (byte) stack.getItemDamage();
 				}
 			}
 		}
 		ItemStack newStack = input[0].copy();
 		ModificationNBTHelper.setTagUpgradeBytes(newStack.stackTagCompound, bytes);
 		return newStack;
 	}
 
 	@Override
 	public ItemStack[] doesRecipeMatch(ItemStack[] provided) {
 		if(provided == null)
 			return null;
 		ItemStack base = null;
 		for(ItemStack stack : provided){
 			if(stack.itemID == baseResult.itemID &&
 					stack.getItemDamage() == baseResult.getItemDamage()){
 				base = stack.copy();
 				break;
 			}
 		}
 		if(base == null)
 			return null;
 		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
 		list.add(base);
 		if(baseResult.itemID == provided[0].itemID && baseResult.getItemDamage() == provided[0].getItemDamage()){
 			byte[] fromStack = ModificationNBTHelper.getUpgradeByteArray(provided[0].stackTagCompound);
 			byte[] bytes = new byte[fromStack.length];
  			System.arraycopy(fromStack, 0, bytes, 0, bytes.length);
 			for(ItemStack possible : possibleInputs){
 			outer : for(int i = 0; i < provided.length; i++){
 					if(OreDictionary.itemMatches(possible, provided[i], false)){
 						ItemStack newPiece = null;
 						if(provided[i].itemID == Block.carpet.blockID){
 							if(bytes[3] != -1)
 								continue outer;
 							newPiece = provided[i].copy();
 							newPiece.stackSize = 1;
 							list.add(newPiece);
 						}else if(provided[i].itemID == Block.blockClay.blockID){
 							if(bytes[4] != -1)
 								continue outer;
 							newPiece = provided[i].copy();
 							newPiece.stackSize = 1;
 							list.add(newPiece);
 						}else if(provided[i].itemID == CraftingSuite.modItems.itemID){
 							switch(provided[i].getItemDamage()){
 							case 3: if(bytes[1] == -1){
 										bytes[1] = (byte)provided[i].getItemDamage();
 										newPiece = provided[i].copy();
 										newPiece.stackSize = 1;
 										list.add(newPiece);
 									}
 									break;
 							default: continue outer;
 							}
 						}
 					}
 				}
 			}
 		}
 		if(list.size() == 1)
 			return null;
 		ItemStack[] stacks = new ItemStack[list.size()];
 		for(int i = 0; i < list.size(); i++) stacks[i] = list.get(i);
 		return stacks;
 	}
 
 	@Override
 	public boolean consumeItems(TileEntityModificationTable tile) {
 		boolean flag = true;
 		tile.decrStackSize(0, 1);
 		for(ItemStack component : possibleInputs){
 			for(int i = 0; i < tile.getSizeInventory(); i++){
 				int idi = OreDictionary.getOreID(tile.getStackInSlot(i));
 				int idc = OreDictionary.getOreID(component);
 				if(OreDictionary.itemMatches(component, tile.getStackInSlot(i), false)
 						|| (idi != -1 && idi == idc)){
 					tile.decrStackSize(i, component.stackSize);
 					break;
 				}else
 					flag = false;
 			}
 		}
 		return flag;
 	}
 
 }
