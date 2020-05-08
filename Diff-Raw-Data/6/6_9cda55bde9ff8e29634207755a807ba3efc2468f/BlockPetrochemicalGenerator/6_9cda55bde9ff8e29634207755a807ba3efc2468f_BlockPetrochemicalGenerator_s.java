 /** 
  * Copyright (C) 2013 Flow86
  * 
  * AdditionalIC2Objects is open-source.
  *
  * It is distributed under the terms of my Open Source License. 
  * It grants rights to read, modify, compile or run the code. 
  * It does *NOT* grant the right to redistribute this software or its 
  * modifications in any form, binary or source, except if expressively
  * granted by the copyright holder.
  */
 
 package aic2o.energy;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import aic2o.AIC2O;
 import aic2o.gui.AIC2OGuiIds;
 import buildcraft.api.tools.IToolWrench;
 import buildcraft.core.IItemPipe;
 import buildcraft.energy.BlockEngine;
 import buildcraft.energy.TileEngine;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockPetrochemicalGenerator extends BlockEngine {
 
 	private Icon petrochemicalTexture;
 
 	public BlockPetrochemicalGenerator(int i) {
 		super(i);
 		setUnlocalizedName("blockPetrochemicalGenerator");
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public void getSubBlocks(int blockid, CreativeTabs par2CreativeTabs, List itemList) {
 		// super.getSubBlocks(blockid, par2CreativeTabs, itemList);
 		itemList.add(new ItemStack(this, 1, 4));
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister) {
 		petrochemicalTexture = par1IconRegister.registerIcon("aic2o:enginePetrochemicalBottom");
 
 		super.registerIcons(par1IconRegister);
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World var1) {
 		return new TilePetrochemicalGenerator();
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int meta) {
 		switch (meta) {
 		case 4:
 			return petrochemicalTexture;
 		}
 		return super.getIcon(side, meta);
 	}
 
 	@Override
 	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int side, float par7, float par8, float par9) {
 
 		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
 
 		// Drop through if the player is sneaking
 		if (player.isSneaking())
 			return false;
 
 		// Switch orientation if whacked with a wrench.
 		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
 		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, i, j, k)) {
 
 			tile.switchOrientation();
 			((IToolWrench) equipped).wrenchUsed(player, i, j, k);
 			return true;
 
 		} else {
 
 			// Do not open guis when having a pipe in hand
 			if (player.getCurrentEquippedItem() != null) {
 				if (player.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
 					return false;
 				}
				if (tile.engine instanceof EnginePetrochemical) {
					ItemStack current = player.getCurrentEquippedItem();
					if (current != null && current.itemID != Item.bucketEmpty.itemID) {
						return true;
					}
				}
 			}
 
 			if (tile.engine instanceof EnginePetrochemical) {
 				if (!world.isRemote) {
 					player.openGui(AIC2O.instance, AIC2OGuiIds.ENGINE_PETROCHEMICAL, world, i, j, k);
 				}
 				return true;
 			}
 		}
 
 		return false;
 	}
 }
