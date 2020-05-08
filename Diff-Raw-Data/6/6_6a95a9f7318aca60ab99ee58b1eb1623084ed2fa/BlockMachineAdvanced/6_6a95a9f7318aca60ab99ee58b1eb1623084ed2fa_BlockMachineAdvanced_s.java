 package makmods.levelstorage.block;
 
 import ic2.api.tile.IWrenchable;
 import makmods.levelstorage.network.packet.PacketReRender;
 import makmods.levelstorage.proxy.ClientProxy;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 /**
  * BlockMachineAdvanced - a standart machine with facing
  * 
  * @author mak326428
  * 
  */
 public abstract class BlockMachineAdvanced extends BlockMachineStandart {
 
 	public BlockMachineAdvanced(int par1) {
 		super(par1);
 	}
 
 	private Icon facing;
 
 	public Icon getBlockTexture(IBlockAccess par1IBlockAccess, int x, int y,
 			int z, int side) {
 		TileEntity te = par1IBlockAccess.getBlockTileEntity(x, y, z);
 		if (te == null || !(te instanceof IWrenchable))
 			return null;
 		IWrenchable asu = (IWrenchable) te;
 		return asu.getFacing() == side ? facing : icons[side];
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int par2) {
 		return side != ForgeDirection.SOUTH.ordinal() ? icons[side] : facing;
 	}
 
 	public void onBlockPlacedBy(World world, int i, int j, int k,
 			EntityLivingBase entityliving, ItemStack itemStack) {
 		if (world.isRemote)
 			return;
 
 		IWrenchable te = (IWrenchable) world.getBlockTileEntity(i, j, k);
 
 		if (entityliving == null) {
 			te.setFacing((short) 1);
 		} else {
 			int yaw = MathHelper
 					.floor_double(entityliving.rotationYaw * 4.0F / 360.0F + 0.5D) & 0x3;
 			int pitch = Math.round(entityliving.rotationPitch);
 
 			if (pitch >= 65)
 				te.setFacing((short) 1);
 			else if (pitch <= -65)
 				te.setFacing((short) 0);
 			else
 				switch (yaw) {
 				case 0:
 					te.setFacing((short) 2);
 					break;
 				case 1:
 					te.setFacing((short) 5);
 					break;
 				case 2:
 					te.setFacing((short) 3);
 					break;
 				case 3:
 					te.setFacing((short) 4);
 				}
 		}
 		TileEntity tile = (TileEntity)te;
 		PacketDispatcher.sendPacketToAllPlayers(tile.getDescriptionPacket());
 		PacketReRender.reRenderBlock(tile.xCoord, tile.yCoord, tile.zCoord);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
 		super.registerIcons(iconRegister);
 		String blockName = this.getUnlocalizedName().replace("tile.", "");
		iconRegister.registerIcon(ClientProxy.getTexturePathFor(blockName + "/"
 				+ "facing"));
 	}
 
 }
