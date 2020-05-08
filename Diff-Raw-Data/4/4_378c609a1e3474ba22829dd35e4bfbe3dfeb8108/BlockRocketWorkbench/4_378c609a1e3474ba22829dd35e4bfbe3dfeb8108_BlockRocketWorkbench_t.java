 package spazzysmod.blocks;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import spazzysmod.SpazzysmodBase;
 import spazzysmod.client.gui.inventory.GuiRocketCrafting;
 import spazzysmod.creativetab.SpazzysTabs;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockRocketWorkbench extends Block
 {
 	@SideOnly ( Side.CLIENT )
 	private Icon workbenchIconTop;
 	@SideOnly ( Side.CLIENT )
 	private Icon workbenchIconFront;
 
 	protected BlockRocketWorkbench ( int par1 )
 	{
 		super ( par1, Material.iron );
 		this.setCreativeTab ( SpazzysTabs.tabSolarSystem );
 		this.setHardness ( 1F );
 		this.setResistance ( 1F );
 		this.func_111022_d ( SpazzysmodBase.MODID + ":" + this.getUnlocalizedName ().substring ( 5 ) );
 	}
 
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon ( int par1, int par2 ) {
 		return par1 == 1 ? this.workbenchIconTop : ( par1 == 0 ? Block.planks.getBlockTextureFromSide ( par1 ) : ( par1 != 2 && par1 != 4 ? this.blockIcon : this.workbenchIconFront ) );
 
 	}
 
 	public boolean onBlockActivated ( World var1, int var2, int var3, int var4, EntityPlayer player, int var6, float var7, float var8, float var9 ) {
 		if ( !player.isSneaking () ) {
 			player.openGui(SpazzysmodBase.instance, GuiRocketCrafting.GUI_ID, var1, var2, var3, var4);
 
 			return true;
 		}
 		else
 			return false;
 	}
 }
