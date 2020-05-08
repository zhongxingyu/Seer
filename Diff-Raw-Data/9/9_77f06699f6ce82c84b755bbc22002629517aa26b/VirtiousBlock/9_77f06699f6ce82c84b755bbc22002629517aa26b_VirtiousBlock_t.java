 package teamm.mods.virtious.block;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 
 public class VirtiousBlock extends Block
 {
 	public VirtiousBlock(int id, Material mat) 
 	{
 		super(id, mat);
 	}
 	
 	public void registerIcons(IconRegister r)
 	{
		r.registerIcon("virtious:" + this.getUnlocalizedName().substring(5));
 	}
 }
