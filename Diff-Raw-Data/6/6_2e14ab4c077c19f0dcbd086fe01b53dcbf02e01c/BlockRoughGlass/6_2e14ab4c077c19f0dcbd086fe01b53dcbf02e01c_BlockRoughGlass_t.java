 package teamm.mods.virtious.block;
 
 import java.util.Random;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import teamm.mods.virtious.Virtious;
 import net.minecraft.block.BlockGlass;
 import net.minecraft.block.material.Material;
 
public class BlockRoughGlass extends VirtiousBlock
{
 
 	public BlockRoughGlass(int par1, Material mat) {
 		super(par1, mat);
 	}
 	/**
      * Returns the quantity of items to drop on block destruction.
      */
     public int quantityDropped(Random par1Random)
     {
         return 0;
     }
 
 	/**
      * Returns which pass should this block be rendered on. 0 for solids and 1 for alpha
      */
     public int getRenderBlockPass()
     {
        return 1;
     }
 
     /**
      * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
      * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
      */
     public boolean isOpaqueCube()
     {
         return false;
     }
 
     /**
      * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
      */
     public boolean renderAsNormalBlock()
     {
         return false;
     }
 
 }
