package tekner.loecraftpack;
 
 import java.util.ArrayList;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockBed;
 import net.minecraft.block.ITileEntityProvider;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Direction;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 
 public class BlockBedColor extends BlockBed {
 
 	public ItemBedColor item;
 	
 	/** Maps the foot-of-bed block to the head-of-bed block. */
     public static final int[][] footBlockToHeadBlockMap = new int[][] {{0, 1}, { -1, 0}, {0, -1}, {1, 0}};
     @SideOnly(Side.CLIENT)
     private Icon[] field_94472_b;
     @SideOnly(Side.CLIENT)
     private Icon[] field_94473_c;
     @SideOnly(Side.CLIENT)
     private Icon[] field_94471_cO;
     
     
     // TODO  update constructor to accept Icons, to simply changing render.
 	public BlockBedColor(int par1) {
 		super(par1);
 	}
 
 }
