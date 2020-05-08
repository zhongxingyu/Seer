 package entanglecraft.blocks;
 
 import java.util.ArrayList;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.BlockContainer;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.EntityItem;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Material;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 import entanglecraft.EntangleCraft;
 
 public class BlockLambdaMiner extends BlockContainer{
 	public int channel;
 	
 	public BlockLambdaMiner(int par1, int channel) {
 		super(par1, Material.rock);
 		this.channel = channel;
 		
 		setCreativeTab(CreativeTabs.tabMisc);
 	}
 	
 	@Override
 	public boolean isOpaqueCube(){
 		return true;
 	}
 	
 	public void onBlockAdded(World theWorld, int x, int y, int z){
 		super.onBlockAdded(theWorld, x, y, z);
 		TileEntityLambdaMiner te = (TileEntityLambdaMiner)theWorld.getBlockTileEntity(x, y, z);
 		System.out.println("adding a miner at y = " + y);
 		int[] blockCoords = new int[] {x,y,z};
 		te.setBlockCoords(blockCoords);
 		te.layerToMine = y-1;
 		te.generateLayerStructure(1,new int[] {blockCoords[0]+1,blockCoords[1],blockCoords[2]});
 	}
 	
 	@Override
 	public void breakBlock(World theWorld, int x,int y, int z, int par5, int par6){
         TileEntityLambdaMiner te = (TileEntityLambdaMiner)theWorld.getBlockTileEntity(x, y, z);
         if (te != null)
         {
             for (int i = 0; i < te.getSizeInventory(); ++i)
             {
                 ItemStack itemStack = te.getStackInSlot(i);
 
                 if (itemStack != null)
                 {
                     float randX = theWorld.rand.nextFloat() * 0.8F + 0.1F;
                     float randY = theWorld.rand.nextFloat() * 0.8F + 0.1F;
                     float randZ = theWorld.rand.nextFloat() * 0.8F + 0.1F;
 
                     while (itemStack.stackSize > 0)
                     {
                         int anInt = theWorld.rand.nextInt(21) + 10;
 
                         if (anInt > itemStack.stackSize)
                         {
                             anInt = itemStack.stackSize;
                         }
 
                         itemStack.stackSize -= anInt;
                         EntityItem itemToDrop = new EntityItem(theWorld, (double)((float)x + randX), (double)((float)y + randY), (double)((float)z + randZ), new ItemStack(itemStack.itemID, anInt, itemStack.getItemDamage()));
 
                         if (itemStack.hasTagCompound())
                         {
                             itemToDrop.item.setTagCompound((NBTTagCompound)itemStack.getTagCompound().copy());
                         }
 
                         float var13 = 0.05F;
                         itemToDrop.motionX = (double)((float)theWorld.rand.nextGaussian() * var13);
                         itemToDrop.motionY = (double)((float)theWorld.rand.nextGaussian() * var13 + 0.2F);
                         itemToDrop.motionZ = (double)((float)theWorld.rand.nextGaussian() * var13);
                         theWorld.spawnEntityInWorld(itemToDrop);
                     }
                 }
             }
 
         }
         super.breakBlock(theWorld, x, y, z,par5,par6);
     }
 	
     public boolean onBlockActivated(World par1World, int i, int j, int k, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
     {
     	TileEntityLambdaMiner tileEntitylM = (TileEntityLambdaMiner) par1World
 				.getBlockTileEntity(i, j, k);
 
     	par5EntityPlayer.openGui(EntangleCraft.instance, 1, par1World, i, j, k);
     	return true;
     }
 	
 	public int getBlockTextureFromSideAndMetadata(int i, int metaData){
 		if (i == 1){
 			return 17+(6*channel)+(3*metaData);
 		}
 		else if(i == 0){
 			return 16;
 		}
 		else if(i == 5){
 			return 18+(6*channel)+(3*metaData);
 		}
 		else return 19+(6*channel)+(3*metaData);
 		
 	}
 	
 	@Override
 	public void onNeighborBlockChange(World world, int x, int y, int z, int id){
 		super.onNeighborBlockChange(world,x,y,z,id);
 		System.out.println("Lambda Miner: neighbour block changed");
 		TileEntity te = world.getBlockTileEntity(x, y, z);
 		TileEntityLambdaMiner teLM = (TileEntityLambdaMiner)te;
 		if(teLM.getChest() == null){
 		if(id == Block.chest.blockID){
 			
 			if (world.getBlockId(x+1, y, z) == id){
 				teLM.setChest(new int[] {x+1,y,z});
 			}
 			else if (world.getBlockId(x-1, y, z) == id){
 				teLM.setChest(new int[] {x-1,y,z});
 			}
 			else if (world.getBlockId(x, y, z+1) == id){
 				teLM.setChest(new int[] {x,y,z+1});
 			}
 			else if (world.getBlockId(x, y, z-1) == id){
 				teLM.setChest(new int[] {x,y,z-1});
 			}
 			else teLM.setChest(null);
 		}
 		}
 
 	}
 	
 	public String getTextureFile(){
 		return "/lambdaTextures.png";
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
		return new TileEntityLambdaMiner();
 	}
 	
 }
