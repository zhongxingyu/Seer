 package com.moosetra.craftra.block;
 
import java.util.Random;

 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
 
import com.moosetra.craftra.Craftra;
 import com.moosetra.craftra.lib.Reference;
 
 public class CarboniteOreBlock extends Block {
 
 	public CarboniteOreBlock(int ID, Material material) {
 		super(ID, material);
 		
 		setHardness(3.0f);
 		setStepSound(Block.soundStoneFootstep);
 		setUnlocalizedName("CarboniteOre");
 		setCreativeTab(CreativeTabs.tabBlock);
 		setTextureName(Reference.MOD_NAME.toLowerCase() + ":CarboniteOre");
 		setLightValue(0.5f);
	
		
	}	
	
	public int idDropped(int metadata, Random random, int fortune) {
		return Craftra.CarboniteOreBlock.blockID;
		
	}
	
	public void dropBlockAsItemWithChance(World world, int par2, int par3, int par4, int par5, float par6, int par7) {
		
		super.dropBlockAsItemWithChance(world, par2, par3, par4, par5, par6, par7);
		
		if (this.idDropped(par5, world.rand, par7) != this.blockID){
			
			int xp = 0;
			xp = MathHelper.getRandomIntegerInRange(world.rand, 2, 10);
			
			this.dropXpOnBlockBreak(world, par2, par3, par4, xp);
			
		}
		
	} 
 		
 	
 		
 }
