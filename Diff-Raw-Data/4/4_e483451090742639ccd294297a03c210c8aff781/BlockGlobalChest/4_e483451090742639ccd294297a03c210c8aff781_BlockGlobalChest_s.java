 package fuj1n.globalLinkMod.common.blocks;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.InventoryEnderChest;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityEnderChest;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.FMLCommonHandler;
 import fuj1n.globalLinkMod.GlobalChests;
 import fuj1n.globalLinkMod.client.ClientProxyGlobalChests;
 import fuj1n.globalLinkMod.common.tileentity.TileEntityGlobalChest;
 
 public class BlockGlobalChest extends BlockContainer {
 
 	public BlockGlobalChest(int par1) {
 		super(par1, Material.iron);
 		this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
 	}
 
 	@Override
 	public boolean isOpaqueCube() {
 		return false;
 	}
 
 	@Override
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 
 	@Override
 	public Icon getIcon(int par1, int par2) {
 		return Block.blockIron.getIcon(par1, par2);
 	}
 
 	@Override
 	public int getRenderType() {
 		return ClientProxyGlobalChests.GlobalChestRenderId;
 	}
 
 	@Override
 	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
 		if (FMLCommonHandler.instance().getMinecraftServerInstance() != null && FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) {
 			InventoryEnderChest inventoryenderchest = par5EntityPlayer.getInventoryEnderChest();
 			TileEntityEnderChest tileentityenderchest = (TileEntityEnderChest) par1World.getBlockTileEntity(par2, par3, par4);
 
 			if (inventoryenderchest != null && tileentityenderchest != null) {
 				if (par1World.isBlockNormalCube(par2, par3 + 1, par4)) {
 					return true;
 				} else if (par1World.isRemote) {
 					return true;
 				} else {
 					inventoryenderchest.setAssociatedChest(tileentityenderchest);
 					par5EntityPlayer.displayGUIChest(inventoryenderchest);
 					return true;
 				}
 			} else {
 				return false;
 			}
		} else if (par1World.getBlockId(par2, par3 + 1, par3) == 0) {
 			par5EntityPlayer.openGui(GlobalChests.instance, 0, par1World, par2, par3, par4);
 			return true;
 		}
		return false;
 	}
 
 	@Override
 	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLiving par5EntityLiving, ItemStack par6ItemStack) {
 		byte b0 = 0;
 		int l = MathHelper.floor_double((par5EntityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
 
 		if (l == 0) {
 			b0 = 2;
 		}
 
 		if (l == 1) {
 			b0 = 5;
 		}
 
 		if (l == 2) {
 			b0 = 3;
 		}
 
 		if (l == 3) {
 			b0 = 4;
 		}
 
 		par1World.setBlockMetadataWithNotify(par2, par3, par4, b0, 2);
 	}
 
 	@Override
 	public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random) {
 		if (par5Random.nextInt(2) == 0) {
 			for (int l = 0; l < 3; ++l) {
 				double d0 = (par2 + par5Random.nextFloat());
 				double d1 = (par3 + par5Random.nextFloat());
 				d0 = (par4 + par5Random.nextFloat());
 				double d2 = 0.0D;
 				double d3 = 0.0D;
 				double d4 = 0.0D;
 				int i1 = par5Random.nextInt(2) * 2 - 1;
 				int j1 = par5Random.nextInt(2) * 2 - 1;
 				d2 = (par5Random.nextFloat() - 0.5D) * 0.125D;
 				d3 = (par5Random.nextFloat() - 0.5D) * 0.125D;
 				d4 = (par5Random.nextFloat() - 0.5D) * 0.125D;
 				double d5 = par4 + 0.5D + 0.25D * j1;
 				d4 = (par5Random.nextFloat() * 1.0F * j1);
 				double d6 = par2 + 0.5D + 0.25D * i1;
 				d2 = (par5Random.nextFloat() * 1.0F * i1);
 				par1World.spawnParticle("enchantmenttable", d6, d1, d5, d2, d3, d4);
 			}
 		}
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		if (FMLCommonHandler.instance().getMinecraftServerInstance() != null && FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) {
 			return Block.enderChest.createTileEntity(world, 0);
 		} else {
 			return new TileEntityGlobalChest();
 		}
 	}
 
 	@Override
 	public void registerIcons(IconRegister par1IconRegister) {
 	}
 
 }
