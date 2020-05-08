 package loecraftpack.blocks;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import loecraftpack.LoECraftPack;
 import loecraftpack.blocks.te.ColoredBedTileEntity;
 import loecraftpack.enums.Dye;
 import net.minecraft.block.BlockBed;
 import net.minecraft.block.ITileEntityProvider;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ColoredBedBlock extends BlockBed implements ITileEntityProvider {
 
 	// TODO change 16 to a value dependent on the number of types
 	public static int bedTypes = 16;
 	
     @SideOnly(Side.CLIENT)
     public Icon[][] bedend;
     @SideOnly(Side.CLIENT)
     public Icon[][] bedside;
     @SideOnly(Side.CLIENT)
     public Icon[][] bedtop;
     
     public int renderID = 14;
     
 	public ColoredBedBlock(int par1) {
 		super(par1);
 	}
 	
 	/**
      * The type of render function that is called for this block
      */
 	@Override
     public int getRenderType()
     {
         return renderID;
     }
 	
 	@SideOnly(Side.CLIENT)
 
     /**
      * When this method is called, your block should register all the icons it needs with the given IconRegister. This
      * is the only chance you get to register icons.
      */
     public void registerIcons(IconRegister par1IconRegister)
     {
 		bedend = new Icon[bedTypes][];//end
 		bedside = new Icon[bedTypes][];//side
 		bedtop = new Icon[bedTypes][];//top
 		for(int i=0; i<bedTypes; i++)
 		{
 			this.bedtop[i] = new Icon[] {par1IconRegister.registerIcon("loecraftpack:bed_"+Dye.values()[i]+"_feet_top"), par1IconRegister.registerIcon("loecraftpack:bed_"+Dye.values()[i]+"_head_top")};
 			this.bedend[i] = new Icon[] {par1IconRegister.registerIcon("loecraftpack:bed_"+Dye.values()[i]+"_feet_end"), par1IconRegister.registerIcon("loecraftpack:bed_"+Dye.values()[i]+"_head_end")};
 			this.bedside[i] = new Icon[] {par1IconRegister.registerIcon("loecraftpack:bed_"+Dye.values()[i]+"_feet_side"), par1IconRegister.registerIcon("loecraftpack:bed_"+Dye.values()[i]+"_head_side")};
 		}
     }
 		
 	@Override
 	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune)
     {
         ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
         
         int damageValue=0;
         TileEntity tile = world.getBlockTileEntity(x, y, z);
         if (tile != null && tile instanceof ColoredBedTileEntity)
         {
         	damageValue = ((ColoredBedTileEntity)tile).color.ordinal();
         }
 
         int count = quantityDropped(metadata, fortune, world.rand);
         for(int i = 0; i < count; i++)
         {
             int id = idDropped(metadata, world.rand, fortune);
             if (id > 0)
             {
                 ret.add(new ItemStack(id, 1, damageValue));
             }
         }
         return ret;
     }
 	
 	@Override
 	public int idDropped(int par1, Random par2Random, int par3)
     {
         return isBlockHeadOfBed(par1) ? 0 : LoECraftPack.bedItems.itemID;
     }
 	
 	@Override
 	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
     {
 		TileEntity tile = world.getBlockTileEntity(x, y, z);
 		if( tile instanceof ColoredBedTileEntity)
 			return new ItemStack(LoECraftPack.bedItems, 1, ((ColoredBedTileEntity)tile).color.ordinal());
 		else
 		    return new ItemStack(LoECraftPack.bedItems, 1, Dye.White.ordinal());
     }
 	
 	@Override
 	public boolean isBed(World world, int x, int y, int z, EntityLiving player)
     {
         return true;
     }
 
 	@Override
 	public TileEntity createNewTileEntity(World world)
 	{
 		System.out.println("create tile");
 		return new ColoredBedTileEntity(Dye.White);
 	}
 }
