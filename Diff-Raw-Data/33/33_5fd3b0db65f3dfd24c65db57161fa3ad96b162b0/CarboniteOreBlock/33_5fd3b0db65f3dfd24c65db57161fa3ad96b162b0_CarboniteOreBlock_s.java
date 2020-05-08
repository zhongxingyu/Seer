 package com.moosetra.craftra.block;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 
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
 	
 		
 }
