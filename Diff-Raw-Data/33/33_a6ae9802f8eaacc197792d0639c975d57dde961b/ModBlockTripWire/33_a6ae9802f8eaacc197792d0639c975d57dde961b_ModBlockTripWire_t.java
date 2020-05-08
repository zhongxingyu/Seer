 package newdungeons;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockTripWire;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ModBlockTripWire extends BlockTripWire
 {
     protected ModBlockTripWire(int var1)
     {
         super(var1);
     }
 
     @Override
     public int idDropped(int var1, Random var2, int var3)
     {
         return Block.tripWire.idDropped(0, (Random)null, 0);
     }
 
     @Override
     public void onEntityCollidedWithBlock(World var1, int var2, int var3, int var4, Entity var5)
     {
         if ( var5 instanceof EntityPlayer)
         {
             super.onEntityCollidedWithBlock(var1, var2, var3, var4, var5);
         }
     }
 }
