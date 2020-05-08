 package IE.src;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.EntityFallingSand;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Material;
 import net.minecraft.src.World;
 
 public class BlockCSand extends Block
 {
 	public static final String[] dyeColorNames = new String[] {"Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan", "Silver", "Gray", "Pink", "Lime", "Yellow", "Light Blue", "Magenta", "Orange", "White"};
 	 
 	public BlockCSand(int par1) 
 	{
 		super(par1, Material.sand);
 		this.setCreativeTab(CreativeTabs.tabDecorations);
 		this.setHardness(1f);
 		this.setResistance(.5f);
 		this.setTextureFile("/Textures/StainGlass.png");
 		this.blockIndexInTexture = 16;
 	}
 	
 	 public BlockCSand(int par1, int par2, Material par3Material)
 	    {
 	        super(par1, par2, par3Material);
 	    }
 	
     public static boolean fallInstantly = true;
     
     public void onBlockAdded(World par1World, int par2, int par3, int par4)
     {
         par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate());
     }
     
     public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5)
     {
         par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate());
     }
     
     public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         if (!par1World.isRemote)
         {
             this.tryToFall(par1World, par2, par3, par4);
         }
     }
     
     private void tryToFall(World par1World, int par2, int par3, int par4)
     {
         if (canFallBelow(par1World, par2, par3 - 1, par4) && par3 >= 0)
         {
             byte var8 = 32;
 
             if (!fallInstantly && par1World.checkChunksExist(par2 - var8, par3 - var8, par4 - var8, par2 + var8, par3 + var8, par4 + var8))
             {
                 if (!par1World.isRemote)
                 {
                    return;
                 }
             }
             else
             {
                 par1World.setBlockWithNotify(par2, par3, par4, 0);
 
                 while (canFallBelow(par1World, par2, par3 - 1, par4) && par3 > 0)
                 {
                     --par3;
                 }
 
                 if (par3 > 0)
                 {
                    par1World.setBlockWithNotify(par2, par3, par4, this.blockID);
                 }
             }
         }
     }
 
     protected void func_82520_a(EntityFallingSand var9) {}
     
     public int tickRate()
     {
         return 3;
     }
    
     public static boolean canFallBelow(World par0World, int par1, int par2, int par3)
     {
         int var4 = par0World.getBlockId(par1, par2, par3);
 
         if (var4 == 0)
         {
             return true;
         }
         else if (var4 == Block.fire.blockID)
         {
             return true;
         }
         else
         {
             Material var5 = Block.blocksList[var4].blockMaterial;
             return var5 == Material.water ? true : var5 == Material.lava;
         }
     }
 
     public void func_82519_a_(World par1World, int par2, int par3, int par4, int par5) {}
     public static int getDyeFromBlock(int par0)
     {
         return ~par0 & 15;
     }
     @Override
 	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
 	{
 		for(int i = 0; i < dyeColorNames.length;i++)
 		{
 			par3List.add(new ItemStack(par1, 1, i));
 		}
 	}
     
     public static int getBlockFromDye(int par0)
     {
         return ~par0 & 15;
     }
     
     public int getBlockTextureFromSideAndMetadata(int side, int meta)
     {        
             return this.blockIndexInTexture+meta;
     }
     public int damageDropped(int par1)
     {
         return par1;
     }
     
 }
 
 
 
