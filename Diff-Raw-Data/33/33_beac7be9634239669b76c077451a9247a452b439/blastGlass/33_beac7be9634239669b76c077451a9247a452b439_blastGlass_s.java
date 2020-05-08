 package assets.electrolysm.electro.block.basic;
 
 import java.util.Random;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 import assets.electrolysm.electro.electrolysmCore;
 import assets.electrolysm.electro.block.GlassBlockConnectedMeta;
 
 
 public class blastGlass extends GlassBlockConnectedMeta {
 
 	public blastGlass(int par1, String location, boolean hasAlpha,
 			String[] textures) {
 		super(par1, location, hasAlpha, textures);
 
 		this.setUnlocalizedName("blastGlass");
 		this.setCreativeTab(electrolysmCore.TabElectrolysm);
 	}
 
 	//It's not an opaque cube, so you need this.
 	@Override
 	public boolean isOpaqueCube() {
 			return false;
 	}
 
 	//It's not a normal block, so you need this too.
 	public boolean renderAsNormalBlock() {
         return false;
 	}
 
 }
