 package loecraftpack.ponies.abilities;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import loecraftpack.LoECraftPack;
 import loecraftpack.enums.Race;
 import loecraftpack.packet.PacketHelper;
 import loecraftpack.packet.PacketIds;
 import loecraftpack.ponies.abilities.mechanics.MechanicTreeBucking;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 
 public class AbilityBuckTree extends AbilityBase {
 
	public AbilityBuckTree(int par1)
 	{
		super("Buck Tree", Race.EARTH, 3, 1);
 	}
 	
 	@Override
 	protected boolean CastSpellClient(EntityPlayer player, World world)
 	{
 		System.out.println("TreeBuck Client");
 		return true;
 	}
 	
 	@Override
 	protected boolean CastSpellServer(EntityPlayer player, World world)
 	{
 		System.out.println("TreeBuck Server");
 		MovingObjectPosition target = player.rayTrace(100, 1);
 		if (target == null)
 			return false;
 		else
 		{
 			int x = (int)target.hitVec.xCoord;
 			int y = (int)target.hitVec.yCoord;
 			int z = (int)target.hitVec.zCoord;
 			
 			if (player.worldObj.getBlockId(x, y, z) == LoECraftPack.blockZapAppleLog.blockID ||
 				player.worldObj.getBlockId(x, y, z) == LoECraftPack.blockAppleBloomLog.blockID)
 			{
 				System.out.println("BUCK"+world.isRemote);
 				MechanicTreeBucking.buckTree(player.worldObj, x, y, z, 0/*Do: BuckTree - fortune*/);
 				return true;
 			}
 			return false;
 		}
 	}
 	
 }
