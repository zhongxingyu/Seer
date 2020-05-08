package Earth.Combiner.ItemTools; //The type com.google.common.collect.Multimap cannot be resolved. It is indirectly referenced from required .class files
  
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import Earth.Combiner.CombinerCore;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemTool;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 
 public class CombTool extends ItemTool
 {
     /** an array of the blocks this axe is effective against */
     public static final Block[] blocksEffectiveAgainst = new Block[] {
     	Block.planks, Block.bookShelf, Block.wood, Block.chest, Block.stoneDoubleSlab, 
     	Block.stoneSingleSlab, Block.pumpkin, Block.pumpkinLantern, Block.cobblestone, 
     	Block.stone, Block.sandStone, Block.cobblestoneMossy, Block.oreIron, Block.blockIron, 
     	Block.oreCoal, Block.blockGold, Block.oreGold, Block.oreDiamond, Block.blockDiamond, 
     	Block.ice, Block.netherrack, Block.oreLapis, Block.blockLapis, Block.oreRedstone, 
     	Block.oreRedstoneGlowing, Block.rail, Block.railDetector, Block.railPowered, Block.grass, 
     	Block.dirt, Block.sand, Block.gravel, Block.snow, Block.blockSnow, Block.blockClay, 
     	Block.tilledField, Block.slowSand, Block.mycelium};
     	
 
     public CombTool(int par1, EnumToolMaterial par2EnumToolMaterial)
     {
         super(par1, 3, par2EnumToolMaterial, blocksEffectiveAgainst);
         maxStackSize=1;
         this.setCreativeTab(CombinerCore.CombinerTab);
     }
 
     /**
      * Returns if the item (tool) can harvest results from the block type.
      */
     public boolean canHarvestBlock(Block par1Block)
     {
         return par1Block == Block.obsidian 
         ? this.toolMaterial.getHarvestLevel() == 3 
         : (par1Block != Block.blockDiamond && par1Block 
         != Block.oreDiamond ? (par1Block != Block.oreEmerald 
         && par1Block != Block.blockEmerald 
         ? (par1Block != Block.blockGold && par1Block 
         != Block.oreGold ? (par1Block != Block.blockIron 
         && par1Block != Block.oreIron ? (par1Block != Block.blockLapis 
         && par1Block != Block.oreLapis ? (par1Block != Block.oreRedstone 
         && par1Block != Block.oreRedstoneGlowing ? (par1Block.blockMaterial
         == Material.rock ? true : (par1Block.blockMaterial == Material.iron 
         ? true : par1Block.blockMaterial == Material.anvil)) 
         : this.toolMaterial.getHarvestLevel() >= 2) 
         : this.toolMaterial.getHarvestLevel() >= 1) 
         : this.toolMaterial.getHarvestLevel() >= 1) 
         : this.toolMaterial.getHarvestLevel() >= 2) 
         : this.toolMaterial.getHarvestLevel() >= 2) 
         : this.toolMaterial.getHarvestLevel() >= 2);
     }
     
     /**
      * Returns the strength of the stack against a given block. 1.0F base, (Quality+1)*2 if correct blocktype, 1.5F if
      * sword
      */
     public float getStrVsBlock(ItemStack par1ItemStack, Block par2Block)
     {
         return par2Block != null && (par2Block.blockMaterial == Material.wood 
         		|| par2Block.blockMaterial == Material.plants 
         		|| par2Block.blockMaterial == Material.vine
         		|| par2Block.blockMaterial == Material.iron
         		|| par2Block.blockMaterial == Material.anvil
         		|| par2Block.blockMaterial == Material.rock
         		|| par2Block.blockMaterial == Material.snow)
         		? this.efficiencyOnProperMaterial 
         		: super.getStrVsBlock(par1ItemStack, par2Block);
     }
 
     @Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
 		this.itemIcon = iconRegister.registerIcon(CombinerCore.modID + ":" + (this.getUnlocalizedName().substring(5)));
 	}
 }
 
 
