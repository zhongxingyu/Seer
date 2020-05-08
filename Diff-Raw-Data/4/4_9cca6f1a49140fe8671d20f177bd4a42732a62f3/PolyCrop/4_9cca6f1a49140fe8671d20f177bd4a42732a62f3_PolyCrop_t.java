 /**
  * 
  */
 package dokutoku.lead.zotonic.crop;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import powercrystals.minefactoryreloaded.api.FertilizerType;
 import powercrystals.minefactoryreloaded.api.HarvestType;
 import powercrystals.minefactoryreloaded.api.IFactoryFertilizable;
 import powercrystals.minefactoryreloaded.api.IFactoryHarvestable;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import dokutoku.lead.zotonic.client.CropParticleFX;
 import dokutoku.lead.zotonic.lib.Configs;
 import dokutoku.lead.zotonic.lib.FXType;
 import dokutoku.lead.zotonic.lib.Reference;
 import dokutoku.lead.zotonic.crop.seed.PolySeeds;
 import forestry.api.farming.ICrop;
 import forestry.api.farming.IFarmable;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockCrops;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemSeeds;
 import net.minecraft.item.ItemStack;
 import net.minecraft.src.ModLoader;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraft.client.particle.EntityPortalFX;
 import net.minecraft.client.particle.EntityRainFX;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraftforge.common.EnumPlantType;
 
 /**
  * Codename: Lead Zotonic
  *
  * PolyCrop
  *
  * @author Atomfusion/DokuToku
  * @license MIT License (http://opensource.org/licenses/MIT)
  */
 public class PolyCrop extends BlockCrops implements IFactoryFertilizable, IFactoryHarvestable {
 	
 	protected ItemSeeds seed;
 	protected int       rarity = 3;
 	protected FXType    fxtype = FXType.IRON;
 	private   Icon      iconArray[];
 	private   boolean   fullyGrown = false;
 	private	  int		_id;
 	
 	
 	/**
 	 * @param par1
 	 */
 	public PolyCrop(int par1, ItemSeeds seed, int rarity) {
 		
 		super(par1);
 		
 		this._id = par1;
 		this.seed = seed;
 		this.rarity = rarity;
 	
 	}
 	
 	public FXType getFXType()
 	{
 		return fxtype;
 	}
 	
 	@Override
 	public boolean canThisPlantGrowOnThisBlockID(int par1)
     {
 		if(((PolySeeds)seed).getPolyType() == EnumCropType.NETHER)
 			return par1 == Block.slowSand.blockID;
 		else if(((PolySeeds)seed).getPolyType() == EnumCropType.OVERWORLD)
 			return par1 == Block.tilledField.blockID;
 		else if(((PolySeeds)seed).getPolyType() == EnumCropType.LAVA)
 			return par1 == Block.obsidian.blockID;
 		else if(((PolySeeds)seed).getPolyType() == EnumCropType.END)
 			return par1 == Block.whiteStone.blockID;
 		return false;
     }
 	
 	/**
      * Generate a seed ItemStack for this crop.
      */
 	@Override
     public int getSeedItem()
     {
         return seed.itemID;
     }
 
     /**
      * Generate a crop produce ItemStack for this crop.
      */
 	@Override
     public int getCropItem()
     {
         return this._id;
     }
 	
 	@Override
 	public void fertilize(World par1World, int par2, int par3, int par4)
     {
         super.fertilize(par1World, par2, par3, par4);
         
         tellTheNeighbors(par1World, par2, par3, par4);
         
         //par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate(par1World));
     }
 
 	
 	@Override 
     public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune)
     {
 		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
     	ItemStack[] items = new ItemStack[]{ new ItemStack(seed, 1)};
     	
     	// Are we mature enough?
     	if(metadata >= 7) {
     		
         	for(ItemStack item : items)
         	{
 	            if (world.rand.nextInt(15 + (rarity - 3)) <= 7)
 	            {
 	                ret.add(item);
 	                
 	                // Playing with triples
 	                if(world.rand.nextInt(30 + (rarity)) <= 10)
 	                {
 	                	ret.add(item);
 	                }
 	            }
         	}
         	
     	}
 		
 		ret.add(new ItemStack(seed, 1));
 		return ret;
     }
 	
 	public boolean canProvidePower()
 	{
 		if(fxtype == FXType.REDSTONE)
 		{
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public int isProvidingWeakPower(IBlockAccess metadata, int par2, int par3, int par4, int par5)
     {
 		if(fxtype == FXType.REDSTONE)
 		{
 			int i = 0;
 			switch(metadata.getBlockMetadata(par2, par3, par4))
 			{
 			case 15: 
 			case 14: 
 			case 13: 
 			case 12: 
 			case 11: 
 			case 10: 
 			case 9:  
 			case 8:  
 			case 7:  i++; i++;
 			case 6:  i++; i++;
 			case 5:  i++; i++;
 			case 4:  i++; i++;
 			case 3:  i++; i++;
 			case 2:  i++; i++;
 			case 1:  i++; i++;
 			case 0:  i++; i++;
 			}
 			return i;
 		}
         return 0;
     }
 	
 	@Override
 	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
 	{
 		
 		float growthRate = 14.875f * ((float)invertRarity() / 10.0f);
 		
 		if (par1World.getBlockLightValue(par2, par3 + 1, par4) >= 9)
         {
             int l = par1World.getBlockMetadata(par2, par3, par4);
 
             if (l < 7)
             {
             	
             	
                 if (par5Random.nextInt((int)(25.0F / growthRate) + 1) == 0)
                 {
                     ++l;
                     par1World.setBlockMetadataWithNotify(par2, par3, par4, l, 2);
                 }
             }
         }
 		
 		tellTheNeighbors(par1World, par2, par3, par4);
 		
 	}
 	
 	private int invertRarity() {
 		switch(rarity) {
 		case 10: return 1;
 		case 9: return 2;
 		case 8: return 3;
 		case 7: return 4;
 		case 6: return 5;
 		case 5: return 6;
 		case 4: return 7;
 		case 3: return 8;
 		case 2: return 9;
 		case 1: return 10;
 		default: return 1;
 		}
 	}
 	
 	public Block setFXType(FXType fxt) {
 		this.fxtype = fxt;
 		return this;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void randomDisplayTick(World par1World, int par2, int par3,
 			int par4, Random par5Random) {
 		super.randomDisplayTick(par1World, par2, par3, par4, par5Random);
 				
 		float rand    = par5Random.nextFloat();
 		float randu   = par5Random.nextInt(2); // 0 or 1
 		
 		switch(fxtype)
 		{
 		case IRON:   if(randu == 1) metalFXEffect(par1World, par2, par3, par4, par5Random, 2.55F, 2.55F, 2.55F);
 		 		     else           metalFXEffect(par1World, par2, par3, par4, par5Random, 0.68F, 0.68F, 0.68F); break;
 		 		   
 		case GOLD:   if(randu == 1) metalFXEffect(par1World, par2, par3, par4, par5Random, 2.24F, 2.24F, 0.02F);
 		   		     else           metalFXEffect(par1World, par2, par3, par4, par5Random, 2.25F, 0.71F, 0.27F); break;
 		   		   
 		case TIN:    if(randu == 1) metalFXEffect(par1World, par2, par3, par4, par5Random, 0.36F, 0.54F, 0.68F);
 		 		     else           metalFXEffect(par1World, par2, par3, par4, par5Random, 0.82F, 1.14F, 1.42F); break;
 		
 		case COPPER: if(randu == 1) metalFXEffect(par1World, par2, par3, par4, par5Random, 1.72F, 0.83F, 0.01F);
 					 else           metalFXEffect(par1World, par2, par3, par4, par5Random, 1.56F, 0.63F, 0.01F); break;
 					 
 		case SILVER: if(randu == 1) metalFXEffect(par1World, par2, par3, par4, par5Random, 2.00F, 2.20F, 2.27F);
 		 			 else           metalFXEffect(par1World, par2, par3, par4, par5Random, 0.72F, 1.40F, 1.46F); break;
 		 			 
 		case LEAD:   if(randu == 1) metalFXEffect(par1World, par2, par3, par4, par5Random, 0.73F, 0.84F, 1.16F);
 		 		     else           metalFXEffect(par1World, par2, par3, par4, par5Random, 0.45F, 0.52F, 0.73F); break;
 		 		     
 		case NICKEL: if(randu == 1) metalFXEffect(par1World, par2, par3, par4, par5Random, 2.55F, 2.55F, 2.22F);
 	     			 else           metalFXEffect(par1World, par2, par3, par4, par5Random, 1.28F, 1.24F, 0.98F); break;
 		 		     
 		case CLAY:   rainFXEffect(par1World, par2 + par5Random.nextFloat(), par3 + 0.9f, par4 + par5Random.nextFloat(), par5Random); break;
 		
 		case COAL:	 if(rand <= 0.3f) par1World.spawnParticle("smoke", par2 + par5Random.nextFloat(), par3 + 0.1f, par4 + par5Random.nextFloat(),
 											                0.0f, 0.03f, 0.0f); break;
 		
 		case HELL:	 if(rand <= 0.3f) par1World.spawnParticle("flame", par2 + par5Random.nextFloat(), par3 + 0.1f, par4 + par5Random.nextFloat(),
                 	 0.0f, 0.03f, 0.0f);
 					 par1World.spawnParticle("portal", par2 + par5Random.nextFloat(), par3 + 0.1f, par4 + par5Random.nextFloat(),
 					 0.0f, 0.00f, 0.0f);
 					 break;
 					 
 		case REDSTONE: if(rand <= 0.3f) par1World.spawnParticle("reddust", par2 + par5Random.nextFloat(), par3 + 0.1f, par4 + par5Random.nextFloat(),
            	 		   0.0f, 0.00f, 0.0f); break;
 		
 		case SOUL:
 		case QUARTZ: 
 		case GLOW: par1World.spawnParticle("portal", par2 + par5Random.nextFloat(), par3 + 0.1f, par4 + par5Random.nextFloat(),
     	 		   0.0f, 0.00f, 0.0f); break;
     	 		   
 		case LAVA: if(rand <= 0.3f) par1World.spawnParticle("flame", par2 + par5Random.nextFloat(), par3 + 0.1f, par4 + par5Random.nextFloat(),
            	 	   0.0f, 0.03f, 0.0f);
 				   if(rand <= 0.05f) par1World.spawnParticle("lava", par2, par3, par4, 0.0f, 0.0f, 0.0f);
 				   break;
 				   
 		case END:
 		case PEARL: par1World.spawnParticle("townaura", par2 + par5Random.nextFloat(), par3 + 0.5f, par4 + par5Random.nextFloat(),
  	 		   		0.0f, 0.00f, 0.0f); break;
  	 		   		
 		case LAPIS: if(randu == 1) metalFXEffect(par1World, par2, par3, par4, par5Random, 0.10F, 0.43F, 1.22F);
 	     		    else           metalFXEffect(par1World, par2, par3, par4, par5Random, 0.90F, 1.30F, 2.26F); break;
 		
 		default:   break;
 		}
 		
 	}
 	
 	@SideOnly(Side.CLIENT)
 
     /**
      * When this method is called, your block should register all the icons it needs with the given IconRegister. This
      * is the only chance you get to register icons.
      */
     public void registerIcons(IconRegister par1IconRegister)
     {
         this.iconArray = new Icon[4];
         
         for (int i = 0; i < this.iconArray.length - 1; ++i)
         {
 	        if(((PolySeeds)seed).getPolyType() == EnumCropType.NETHER)
 	        	this.iconArray[i] = par1IconRegister.registerIcon(Reference.MOD_ID+":" +"nether_" + i);
 			else if(((PolySeeds)seed).getPolyType() == EnumCropType.OVERWORLD)
 				this.iconArray[i] = par1IconRegister.registerIcon("carrots_" + i);
 			else if(((PolySeeds)seed).getPolyType() == EnumCropType.LAVA)
 				this.iconArray[i] = par1IconRegister.registerIcon(Reference.MOD_ID+":" +"lava_" + i);
 			else if(((PolySeeds)seed).getPolyType() == EnumCropType.END)
 				this.iconArray[i] = par1IconRegister.registerIcon(Reference.MOD_ID+":" +"end_" + i);
         }
         
         this.iconArray[this.iconArray.length - 1] = par1IconRegister.registerIcon(Reference.MOD_ID+":" + fxtype.toString().toLowerCase() + "_crop");
     }
 	
 	@SideOnly(Side.CLIENT)
 
     /**
      * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
      */
     public Icon getIcon(int par1, int par2)
     {
         if (par2 < 7)
         {
             if (par2 == 6)
             {
                 par2 = 5;
             }
 
             return this.iconArray[par2 >> 1];
         }
         else
         {
             return this.iconArray[3];
         }
     }
 	
 	/** FX Effect Handlers **/
	@SideOnly(Side.CLIENT)
 	private void metalFXEffect(World par1World, float par2, float par3,
 			float par4, Random par5Random, float r, float g, float b) {
 		
 		if(par5Random.nextInt() <= 5) {
 			CropParticleFX fx = new CropParticleFX(par1World,
 			/* Motion to */ par2 + .5f, par3, par4 + .5f,
 			/* Spawn at  */ par5Random.nextDouble() - .5f, .9D, par5Random.nextDouble() - .5f,
 			/* Color     */ r, g, b);
 			
 			ModLoader.getMinecraftInstance().effectRenderer.addEffect(fx);
 		}
 	}
 	
	@SideOnly(Side.CLIENT)
 	private void rainFXEffect(World par1World, float f, float g, float h, Random par5Random)
 	{
 		
 		if(par5Random.nextInt() <= 5) {
 			
 			EntityRainFX fx = new EntityRainFX(par1World, f, g, h);
 			
 			ModLoader.getMinecraftInstance().effectRenderer.addEffect(fx);
 			
 		}
 		
 	}
 	
 	public void tellTheNeighbors(World world, int x, int y, int z)
 	{
 		if(fxtype == FXType.REDSTONE)
 		{
 			world.notifyBlocksOfNeighborChange(x, y - 1, z, this.blockID);
             world.notifyBlocksOfNeighborChange(x, y + 1, z, this.blockID);
             world.notifyBlocksOfNeighborChange(x - 1, y, z, this.blockID);
             world.notifyBlocksOfNeighborChange(x + 1, y, z, this.blockID);
             world.notifyBlocksOfNeighborChange(x, y, z - 1, this.blockID);
             world.notifyBlocksOfNeighborChange(x, y, z + 1, this.blockID);
 		}
 	}
 
 
 	@Override
 	public HarvestType getHarvestType() {
 		return HarvestType.Normal;
 	}
 
 
 	@Override
 	public boolean breakBlock() {
 		return true;
 	}
 
 
 	@Override
 	public boolean canBeHarvested(World world,
 			Map<String, Boolean> harvesterSettings, int x, int y, int z) {
 		return world.getBlockMetadata(x, y, z) >= 7;
 	}
 
 
 	@Override
 	public List<ItemStack> getDrops(World world, Random rand,
 			Map<String, Boolean> harvesterSettings, int x, int y, int z) {
 		return Block.blocksList[_id].getBlockDropped(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
 	}
 
 
 	@Override
 	public void preHarvest(World world, int x, int y, int z) {
 
 		
 		
 	}
 
 
 	@Override
 	public void postHarvest(World world, int x, int y, int z) {
 
 		
 		
 	}
 
 
 	@Override
 	public int getFertilizableBlockId() {
 
 		return _id;
 		
 	}
 
 
 	@Override
 	public boolean canFertilizeBlock(World world, int x, int y, int z,
 			FertilizerType fertilizerType) {
 
 		return fertilizerType == FertilizerType.GrowPlant && world.getBlockMetadata(x, y, z) < 7;
 		
 	}
 
 
 	@Override
 	public boolean fertilize(World world, Random rand, int x, int y, int z,
 			FertilizerType fertilizerType) {
 		
 		int m = world.getBlockMetadata(x, y, z);
 		if(m < 7)
 		{
 			// 3 is 2, send change to client, plus 1, cause block update
 			world.setBlockMetadataWithNotify(x, y, z, m+1, 3);
 			return true;
 		}
 		return false;
 		
 	}
 
 	@Override
 	public int getPlantId() {
 		return _id;
 	}
 
 }
