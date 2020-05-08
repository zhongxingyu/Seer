 package assets.tacotek.blocks;
 
 import net.minecraft.block.BlockCrops;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import assets.tacotek.Items.ItemsHelper;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class RedOnionCrop extends BlockCrops {
 	
 	@SideOnly(Side.CLIENT)
 	private Icon[] iconArray;
 
 	public RedOnionCrop (int id) {
 		super(id);
 		//setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.5F, 1.0F);
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int par1, int par2) {
 		if (par2 < 7) {
 			if (par2 == 6) {
 				par2 = 5;
 			}
 			return this.iconArray[par2 >> 1];
 		} else {
 			return this.iconArray[3];
 		}
 	}
 
 	//Seed that is dropped by harvesting the crop
 	protected int getSeedItem() {
 		return ItemsHelper.redOnion.itemID;
 	}
 	
 	//Food that is dropped by the harvesting the crop
 	protected int getCropItem() {
 		return ItemsHelper.redOnion.itemID;
 	}
 
 	public int idPicked (World world, int x, int y, int z) {
 		//seeds
 		return ItemsHelper.redOnion.itemID;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister) {
 		this.iconArray = new Icon[4];
 		
 		for (int i = 0; i < this.iconArray.length; ++i) {
 			this.iconArray[i] = par1IconRegister.registerIcon("RedOnionCrop_stage_" + (i + i));
 		}
 	}
 }
