 package zh.usefulthings.blocks;
 
 import zh.usefulthings.tileentities.ZHTradePostEntity;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
public class ZHTradePost extends BlockContainer {
 
 	private Icon[] icons;
 	private String _unlocalizedName;
 	
 	public ZHTradePost(int id) 
 	{
 		super(id, Material.wood);
 	}
 
 	@Override
 	public Block setUnlocalizedName(String name)
     {
 		this._unlocalizedName = name;
 		return super.setUnlocalizedName(name);
     }
 	
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister)
 	{
 		icons = new Icon[3];
 		
 		icons[0] = iconRegister.registerIcon("wood");
 		icons[1] = iconRegister.registerIcon("UsefulThings:" + _unlocalizedName + "_top");
 		icons[1] = iconRegister.registerIcon("UsefulThings:" + _unlocalizedName + "_side");
 		
 	    
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int meta)
 	{
 		if (side <= 1) //top/bottom
 			return icons[side];
 			
 		return icons[2];
 	}
 	
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float xOffset, float yOffset, float zOffset)
 	{
 		if(!world.isRemote)
 		{
 			TileEntity tile = world.getBlockTileEntity(x, y, z);
 			
 			if (tile instanceof ZHTradePostEntity)
 			{
 				Packet packet = ((ZHTradePostEntity)tile).getTradePacket(player);
 				
 				if (packet != null)
 					PacketDispatcher.sendPacketToPlayer(packet, (Player)player);
 				else
 					player.sendChatToPlayer("No villagers found in range of this trade post");
 				
 				return true;
 			}
 		}
		
		return false;
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) 
 	{
 		return new ZHTradePostEntity();
 	}
 
 }
