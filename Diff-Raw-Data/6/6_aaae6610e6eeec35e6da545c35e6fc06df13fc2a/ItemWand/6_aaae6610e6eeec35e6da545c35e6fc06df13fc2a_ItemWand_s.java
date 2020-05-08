 package cc.cu.maximka.maxcraft.item;
 
import net.minecraft.block.Block;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import cc.cu.maximka.maxcraft.MaxCraft;
 import cc.cu.maximka.maxcraft.block.ModBlocks;
 import cc.cu.maximka.maxcraft.lib.Strings;
 import cc.cu.maximka.maxcraft.lib.Textures;
 
 public class ItemWand extends Item {
 
 	public ItemWand(int id) {
 		super(id);
 		this.setUnlocalizedName(Strings.Wand_Name);
 		this.setTextureName(Textures.Wand);
 		this.setMaxStackSize(1);
 		this.setCreativeTab(MaxCraft.tabMC);
 	}
 	
 	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
 		int id = par3World.getBlockId(par4, par5, par6);
 		if(id == ModBlocks.mectoriditeBlock.blockID && par3World.getBlockId(par4, par5 + 1, par6) == ModBlocks.mectoriditeBlock.blockID) {
			par3World.destroyBlock(par4, par5 + 1, par6, false);
			par3World.setBlock(par4, par5, par6, Block.lavaStill.blockID);
 			par3World.destroyBlock(par4, par5, par6, false);
             EntityItem entityitem = new EntityItem(par3World, (double)par4, (double)par5, (double)par6, new ItemStack(ModBlocks.mectoriditePillar, 10));
             par3World.spawnEntityInWorld(entityitem);
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isFull3D() {
 		return true;
 	}
 
 }
