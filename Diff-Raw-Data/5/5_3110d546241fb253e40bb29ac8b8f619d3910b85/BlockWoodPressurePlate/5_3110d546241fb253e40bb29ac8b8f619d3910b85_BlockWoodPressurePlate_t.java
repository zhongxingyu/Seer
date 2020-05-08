 package net.mcft.copy.vanilladj.block;
 
 import java.util.List;
 
 import net.mcft.copy.vanilladj.misc.WoodUtils;
 import net.minecraft.block.BlockPressurePlate;
 import net.minecraft.block.BlockWood;
 import net.minecraft.block.EnumMobType;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockWoodPressurePlate extends BlockPressurePlate {
 	
 	private Icon[] icons;
 	
 	public BlockWoodPressurePlate(int id) {
 		super(id, null, Material.wood, EnumMobType.everything);
 		setHardness(0.5F);
 		setStepSound(soundWoodFootstep);
 		setUnlocalizedName("pressurePlate");
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
 		icons = WoodUtils.registerIcons(iconRegister);
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int meta) {
 		return icons[Math.min(meta % 8, icons.length - 1)];
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void getSubBlocks(int id, CreativeTabs tabs, List list) {
 		for (int i = 0; i < BlockWood.woodType.length; i++)
 			list.add(new ItemStack(id, 1, i));
 	}
 	
 	@Override
 	protected void setStateIfMobInteractsWithPlate(World world, int x, int y, int z, int oldPower) {
 		
 		int newPower = getPlateState(world, x, y, z);
 		boolean oldPressed = (oldPower > 0);
 		boolean newPressed = (newPower > 0);
 		
 		if (oldPower != newPower) {
 			int meta = world.getBlockMetadata(x, y, z);
 			world.setBlockMetadataWithNotify(x, y, z, getMetaFromWeight(newPower, meta), 2);
 			world.markBlockForRenderUpdate(x, y, z);
 			func_94354_b_(world, x, y, z);
 		}
 		
 		if (!newPressed && oldPressed)
 			world.playSoundEffect(x + 0.5D, y + 0.1D, z + 0.5D, "random.click", 0.3F, 0.5F);
 		else if (newPressed && !oldPressed)
 			world.playSoundEffect(x + 0.5D, y + 0.1D, z + 0.5D, "random.click", 0.3F, 0.6F);
 		
 		if (newPressed)
 			world.scheduleBlockUpdate(x, y, z, blockID, tickRate(world));
 		
 	}
 	
 	@Override
 	protected int getPowerSupply(int meta) {
 		return super.getPowerSupply(meta / 8);
 	}
 	
	@Override
	public int damageDropped(int j) {
		return j;
	}
	
 	protected int getMetaFromWeight(int power, int meta) {
 		return (meta % 8 | super.getMetaFromWeight(power) * 8);
 	}
 	
 }
