 package net.mcft.copy.vanilladj.recipe;
 
import net.mcft.copy.vanilladj.misc.Constants;
 import net.mcft.copy.vanilladj.misc.Utils;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockWood;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.IRecipe;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class RecipeItemReplacerWood implements IRecipeListener {
 	
 	private final Entry[] entries;
 	private ItemStack[] matchOnly = null;
 	
 	public RecipeItemReplacerWood(Object... replace) {
 		
 		if ((replace.length % 3) > 0)
 			throw new IllegalArgumentException("Number of arguments need to be divisible by 3.");
 		
 		int count = replace.length / 3;
 		entries = new Entry[count];
 		for (int i = 0; i < count; i++)
 			entries[i] = new Entry(Utils.makeStack(replace[i * 3]),
 			                       (Class)replace[i * 3 + 1],
 			                       (Boolean)replace[i * 3 + 2]);
 		
 		matchOnly(Block.wood, Block.planks, Item.stick);
 		
 	}
 	
 	public void matchOnly(Object... items) {
 		matchOnly = new ItemStack[items.length];
 		for (int i = 0; i < items.length; i++)
 			matchOnly[i] = Utils.makeStack(items[i]);
 	}
 	
 	@Override
 	public void doSomethingWith(RecipeIterator iterator, IRecipe recipe) {
 		
 		ItemStack output = recipe.getRecipeOutput();
 		if (output == null) return;
 		
 		Entry entry = null;
 		for (Entry e : entries)
 			if (Utils.matches(output, e.replace)) {
 				entry = e;
 				break;
 			}
 		if (entry == null) return;
 		
 		RecipeContainer container = new RecipeContainer(recipe);
 		
 		if (matchOnly != null)
 			for (Object item : container.items) {
 				boolean isMatch = false;
 				for (ItemStack match : matchOnly)
 					if (Utils.matchesOreDict(match, item)) {
 						isMatch = true;
 						break;
 					}
 				if (!isMatch) return;
 			}
 		
 		for (int i = 0; i < BlockWood.woodType.length; i++) {
 			IRecipe clone = container.cloneRecipe();
 			RecipeContainer cloneContainer = new RecipeContainer(clone);
 			replaceWood(cloneContainer, Block.wood.blockID, i);
 			replaceWood(cloneContainer, Block.planks.blockID, i);
 			replaceWood(cloneContainer, Item.stick.itemID, i);
 			ItemStack cloneOutput = clone.getRecipeOutput();
 			cloneOutput.setItemDamage(i);
 			GameRegistry.addRecipe(clone);
 			if (entry.reverse) RecipeReverser.reverse(clone);
 		}
 		
 		if (Item.class.isAssignableFrom(entry.with))
 			Utils.replace(entry.replace.getItem(), entry.with);
 		else Utils.replaceWithMetadata(Block.blocksList[entry.replace.itemID], entry.with);
 		
 		iterator.remove();
 		
 	}
 	
 	private void replaceWood(RecipeContainer container, int id, int meta) {
 		container.replaceOreDict(new ItemStack(id, 1, Constants.anyDamage), new ItemStack(id, 1, meta));
 	}
 	
 	private static class Entry {
 		public final ItemStack replace;
 		public final Class with;
 		public final boolean reverse;
 		public Entry(ItemStack replace, Class with, boolean reverse) {
 			this.replace = replace;
 			this.with = with;
 			this.reverse = reverse;
 		}
 	}
 	
 }
