 package com.isocraft.block.eridiumnet;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IIconRegister;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.IIcon;
 import net.minecraft.world.World;
 import net.minecraftforge.common.util.ForgeDirection;
 
 import com.isocraft.ISOCraft;
 import com.isocraft.block.ISOCraftBlockMachine;
 import com.isocraft.lib.Reference;
 import com.isocraft.tileentity.eridiumnet.TileEridiumEnricher;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockEridiumEnricher extends ISOCraftBlockMachine {
 
 	public ForgeDirection dir;
 
 	public BlockEridiumEnricher() {
 		super(Material.iron, false);
 		this.setCreativeTab(ISOCraft.tabsISO);
 		this.setHardness(3.0F);
 		this.setResistance(5.0F);
 		this.setStepSound(Block.soundTypeMetal);
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World var1, int meta) {
 		TileEridiumEnricher tile = new TileEridiumEnricher();
 		return tile;
 	}
 
 	@Override
 	public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
 		int meta = par1World.getBlockMetadata(x, y, z);
 		++meta;
 
 		if (meta > 5) {
 			meta = 0;
 		}
 
 		par1World.setBlockMetadataWithNotify(x, y, z, meta, 3);
 		TileEridiumEnricher tile = (TileEridiumEnricher) par1World.getTileEntity(x, y, z);
 		tile.updateMeta();
 		tile.checkConnections();
 		return true;
 
 	}
 
 	@Override
 	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
 		int l = ISOCraftBlockMachine.determineOrientation(world, x, y, z, player);
 		world.setBlockMetadataWithNotify(x, y, z, l, 2);
 	}

	@Override
	public void breakBlock(World par1World, int x, int y, int z, Block par5, int par6) {
		TileEridiumEnricher tile = (TileEridiumEnricher) par1World.getTileEntity(x, y, z);	
		if (tile.entity != null){
			par1World.removeEntity(tile.entity);
		}
	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public IIcon getIcon(int i, int j) {
 		if (i == j) {
 			return this.Textures[1];
 		}
 		else {
 			return this.Textures[0];
 		}
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerBlockIcons(IIconRegister par1IconRegister) {
 		this.Textures = new IIcon[2];
 		this.Textures[0] = par1IconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":eridiumNet/" + this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1) + "_0");
 		this.Textures[1] = par1IconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":eridiumNet/" + this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1) + "_1");
 	}
 
 }
