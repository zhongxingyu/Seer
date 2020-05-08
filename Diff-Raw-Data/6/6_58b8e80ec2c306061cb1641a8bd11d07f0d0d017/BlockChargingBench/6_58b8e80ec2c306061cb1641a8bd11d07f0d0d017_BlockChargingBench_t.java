 import ic2.common.BlockMultiID;
 import ic2.common.TileEntityBlock;
 import ic2.platform.Platform;
 import java.util.Random;
 
import forge.*;

public class BlockChargingBench extends BlockMultiID implements ITextureProvider {
 
    public BlockChargingBench(int id) {
       super(id, acn.d);
       this.c(1.0F);
    }
 
    public boolean b(xd world, int i, int j, int k, yw entityplayer) {
       return Platform.isSimulating()?ChargingBenchMod.launchGUI(entityplayer, world.b(i, j, k)):true;
    }
 
    public kw u_() {
     return getBlockEntity();
    }
 
    public TileEntityBlock getBlockEntity(int meta) {
       switch(meta) {
       case 0:
          return new TileEntityChargingBench1();
       case 1:
          return new TileEntityChargingBench2();
       case 2:
          return new TileEntityChargingBench3();
       default:
          return null;
       }
    }
 
    public String getTextureFile() {
       return "/ic2/sprites/ChargingBench.png";
    }
 
    public int a(int i, Random random, int j) {
       return this.bO;
    }
 
    protected int c(int i) {
       return i;
    }
 }
