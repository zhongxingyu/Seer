 package newdungeons;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockTripWireSource;
 import net.minecraft.client.renderer.texture.IconRegister;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ModBlockTripWireSource extends BlockTripWireSource
 {
     protected ModBlockTripWireSource(int var1)
     {
         super(var1);
        func_111022_d("trip_wire_source");
     }
 
     @Override
     public int idDropped(int var1, Random var2, int var3)
     {
         return Block.tripWireSource.idDropped(0, (Random)null, 0);
     }
 }
