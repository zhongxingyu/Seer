 package loecraftpack.common.blocks;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import loecraftpack.LoECraftPack;
 import loecraftpack.common.logic.HandlerColoredBed;
 import loecraftpack.enums.Dye;
 import net.minecraft.block.Block;
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
 
 public class BlockColoredBed extends BlockBed implements ITileEntityProvider {
 
 	@SideOnly(Side.CLIENT)
     public List<Icon[]> bedend;
     @SideOnly(Side.CLIENT)
     public List<Icon[]> bedside;
     @SideOnly(Side.CLIENT)
     public List<Icon[]> bedtop;
     
     @SideOnly(Side.CLIENT)
     public List<Icon[]> bedPairTopLeft;
     @SideOnly(Side.CLIENT)
     public List<Icon[]> bedPairEndLeft;
     @SideOnly(Side.CLIENT)
     public List<Icon[]> bedPairSideLeft;
     @SideOnly(Side.CLIENT)
     public List<Icon[]> bedPairTopRight;
     @SideOnly(Side.CLIENT)
     public List<Icon[]> bedPairEndRight;
     @SideOnly(Side.CLIENT)
     public List<Icon[]> bedPairSideRight;
     
     public int renderID = 14;
     private int bedDropID = -1;//default is the null instance
     
 	public BlockColoredBed(int par1) {
 		super(par1);
 	}
 	
 	
 	@Override
     public int getRenderType()
     {
         return renderID;
     }
 	
 	
     /**
      * When this method is called, your block should register all the icons it needs with the given IconRegister. This
      * is the only chance you get to register icons.
      */
 	@SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister)
     {
 		this.bedtop = new ArrayList<Icon[]>();
 		this.bedend = new ArrayList<Icon[]>();
 		this.bedside = new ArrayList<Icon[]>();
 		
 		this.bedPairTopLeft = new ArrayList<Icon[]>();
 		this.bedPairEndLeft = new ArrayList<Icon[]>();
 		this.bedPairSideLeft = new ArrayList<Icon[]>();
 		this.bedPairTopRight = new ArrayList<Icon[]>();
 		this.bedPairEndRight = new ArrayList<Icon[]>();
 		this.bedPairSideRight = new ArrayList<Icon[]>();
 		
 		for (String name : HandlerColoredBed.iconNames)
 		{
			this.bedtop.add( new Icon[] {par1IconRegister.registerIcon("loecraftpack:beds/bed_"+name+"_feet_top"), par1IconRegister.registerIcon("loecraftpack:beds/bed_"+name+"_head_top")} );
			this.bedend.add( new Icon[] {par1IconRegister.registerIcon("loecraftpack:beds/bed_"+name+"_feet_end"), par1IconRegister.registerIcon("loecraftpack:beds/bed_"+name+"_head_end")} );
			this.bedside.add( new Icon[] {par1IconRegister.registerIcon("loecraftpack:beds/bed_"+name+"_feet_side"), par1IconRegister.registerIcon("loecraftpack:beds/bed_"+name+"_head_side")} );
 		}
 		
 		for (String name : HandlerColoredBed.bedPairs.keySet())
 		{
 			this.bedPairTopLeft.add( new Icon[] {par1IconRegister.registerIcon("loecraftpack:beds/bed_pair_"+name.replace(" ", "").toLowerCase()+"_feet_top_left"), par1IconRegister.registerIcon("loecraftpack:beds/bed_pair_"+name.replace(" ", "").toLowerCase()+"_head_top_left")} );
 			this.bedPairEndLeft.add( new Icon[] {par1IconRegister.registerIcon("loecraftpack:beds/bed_pair_"+name.replace(" ", "").toLowerCase()+"_feet_end_left"), par1IconRegister.registerIcon("loecraftpack:beds/bed_pair_"+name.replace(" ", "").toLowerCase()+"_head_end_left")} );
 			this.bedPairSideLeft.add( new Icon[] {par1IconRegister.registerIcon("loecraftpack:beds/bed_pair_"+name.replace(" ", "").toLowerCase()+"_feet_side_left"), par1IconRegister.registerIcon("loecraftpack:beds/bed_pair_"+name.replace(" ", "").toLowerCase()+"_head_side_left")} );
 			
 			this.bedPairTopRight.add( new Icon[] {par1IconRegister.registerIcon("loecraftpack:beds/bed_pair_"+name.replace(" ", "").toLowerCase()+"_feet_top_right"), par1IconRegister.registerIcon("loecraftpack:beds/bed_pair_"+name.replace(" ", "").toLowerCase()+"_head_top_right")} );
 			this.bedPairEndRight.add( new Icon[] {par1IconRegister.registerIcon("loecraftpack:beds/bed_pair_"+name.replace(" ", "").toLowerCase()+"_feet_end_right"), par1IconRegister.registerIcon("loecraftpack:beds/bed_pair_"+name.replace(" ", "").toLowerCase()+"_head_end_right")} );
 			this.bedPairSideRight.add( new Icon[] {par1IconRegister.registerIcon("loecraftpack:beds/bed_pair_"+name.replace(" ", "").toLowerCase()+"_feet_side_right"), par1IconRegister.registerIcon("loecraftpack:beds/bed_pair_"+name.replace(" ", "").toLowerCase()+"_head_side_right")} );
 		}
     }
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getBlockTextureFromSideAndMetadata(int par1, int par2)
     {
 		return Block.planks.getBlockTextureFromSide(par1);
     }
 	
 	/* bug fix : dropping only white beds */
 	@Override
 	public void breakBlock(World world, int x, int y, int z, int blockID, int meta)
     {
 		TileEntity tile = world.getBlockTileEntity(x, y, z);
 		if (tile != null && tile instanceof TileColoredBed)
 		{
 			bedDropID = ((TileColoredBed)tile).id;
 		}
 		else
 			bedDropID = -1;
 		world.removeBlockTileEntity(x, y, z);
 		TileColoredBed.finishTileRemoval(world, x, y, z, meta);
 		
     }
 	
 	@Override
 	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune)
     {
         ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
         
         int damageValue=0;
 
         TileEntity tile = world.getBlockTileEntity(x, y, z);
         
         if (tile != null && tile instanceof TileColoredBed)
         {
         	damageValue = ((TileColoredBed)tile).id;
         }
         else
         	damageValue = bedDropID;
         
         if (damageValue == -1)//null instance
         	ret.add(new ItemStack(Block.bed.blockID, 1, 0));
         else
         	ret.add(new ItemStack(idDropped(metadata, world.rand, fortune), 1, damageValue));
         return ret;
     }
 	
 	@Override
 	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int scourceID)
     {
         int i1 = par1World.getBlockMetadata(par2, par3, par4);
         int j1 = getDirection(i1);
         boolean flag = false;
         
         if (isBlockHeadOfBed(i1))
         {
             if (par1World.getBlockId(par2 - footBlockToHeadBlockMap[j1][0], par3, par4 - footBlockToHeadBlockMap[j1][1]) != this.blockID)
             {
                 par1World.setBlockToAir(par2, par3, par4);
                 flag=true;
             }
         }
         else if (par1World.getBlockId(par2 + footBlockToHeadBlockMap[j1][0], par3, par4 + footBlockToHeadBlockMap[j1][1]) != this.blockID)
         {
             par1World.setBlockToAir(par2, par3, par4);
             flag=true;
             
             if (!par1World.isRemote)
             {
                 this.dropBlockAsItem(par1World, par2, par3, par4, i1, 0);
             }
         }
     }
 	
 	@Override
 	public int idDropped(int meta, Random par2Random, int par3)
     {
         return isBlockHeadOfBed(meta) ? 0 : LoECraftPack.bedItems.itemID;
     }
 	
 	@Override
 	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
     {
 		TileEntity tile = world.getBlockTileEntity(x, y, z);
 		if (tile instanceof TileColoredBed)
 			return new ItemStack(LoECraftPack.bedItems, 1, ((TileColoredBed)tile).id);
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
 		return new TileColoredBed(0);
 	}
 }
