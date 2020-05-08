 package newdungeons;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockTorch;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ModBlockTorch extends BlockTorch
 {
     protected ModBlockTorch(int var1)
     {
         super(var1);
        func_111022_d("torch_on");
     }
 
     @Override
     public void onBlockClicked(World var1, int var2, int var3, int var4, EntityPlayer var5)
     {
         //System.out.println("clicky click");
 
         if (var5.getItemInUse() == new ItemStack(Item.flintAndSteel))
         {
             this.burn(var1, var2, var3, var4);
         }
     }
 
     private void burn(World var1, int var2, int var3, int var4)
     {
         var1.setBlock(var2, var3, var4, Block.torchWood.blockID, var1.getBlockMetadata(var2, var3, var4), 3);
         var1.playSoundEffect((double)((float)var2 + 0.5F), (double)((float)var3 + 0.5F), (double)((float)var4 + 0.5F), "random.fizz", 0.5F, 2.6F + (var1.rand.nextFloat() - var1.rand.nextFloat()) * 0.8F);
         int var5 = var1.getBlockMetadata(var2, var3, var4);
         double var6 = (double)((float)var2 + 0.5F);
         double var8 = (double)((float)var3 + 0.7F);
         double var10 = (double)((float)var4 + 0.5F);
         double var12 = 0.2199999988079071D;
         double var14 = 0.27000001072883606D;
         int var16;
 
         if (var5 == 1)
         {
             for (var16 = 0; var16 < 8; ++var16)
             {
                 var1.spawnParticle("flame", var6 - var14, var8 + var12 + (double)var16 * 0.01D, var10, 0.0D, 0.03D, 0.0D);
             }
         }
         else if (var5 == 2)
         {
             for (var16 = 0; var16 < 8; ++var16)
             {
                 var1.spawnParticle("flame", var6 + var14, var8 + var12 + (double)var16 * 0.01D, var10, 0.0D, 0.03D, 0.0D);
             }
         }
         else if (var5 == 3)
         {
             for (var16 = 0; var16 < 8; ++var16)
             {
                 var1.spawnParticle("flame", var6, var8 + var12 + (double)var16 * 0.01D, var10 - var14, 0.0D, 0.03D, 0.0D);
             }
         }
         else if (var5 == 4)
         {
             for (var16 = 0; var16 < 8; ++var16)
             {
                 var1.spawnParticle("flame", var6, var8 + var12 + (double)var16 * 0.01D, var10 + var14, 0.0D, 0.03D, 0.0D);
             }
         }
         else
         {
             for (var16 = 0; var16 < 8; ++var16)
             {
                 var1.spawnParticle("flame", var6, var8 + (double)var16 * 0.01D, var10, 0.0D, 0.03D, 0.0D);
             }
         }
     }
 
     @Override
     public void onNeighborBlockChange(World var1, int var2, int var3, int var4, int var5)
     {
         if (var5 == Block.fire.blockID)
         {
             this.burn(var1, var2, var3, var4);
         }
 
         super.onNeighborBlockChange(var1, var2, var3, var4, var5);
     }
 }
