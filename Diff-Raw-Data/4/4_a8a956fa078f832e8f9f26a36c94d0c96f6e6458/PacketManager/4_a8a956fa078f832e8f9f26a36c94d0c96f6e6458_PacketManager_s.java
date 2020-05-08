 package eu.tomylobo.ccnoise.common;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 
 public class PacketManager implements IPacketHandler {
 	public static interface PacketHandler {
 		void handlePacket(byte type, INetworkManager manager, DataInputStream dis, Player player) throws IOException, Exception;
 	}
 
 	public static final String CHANNEL_ID = "CCNoise";
 
 	public static class PacketStream extends DataOutputStream {
 		public PacketStream(byte type) throws IOException {
 			super(new ByteArrayOutputStream());
 
 			writeByte(type);
 		}
 
 
 		public void writeString(String string) throws IOException {
 			Packet.writeString(string, this);
 		}
 
 		public void writeItemStack(ItemStack stack) throws IOException {
 			Packet.writeItemStack(stack, this);
 		}
 
 
 		public byte[] toByteArray() {
 			return ((ByteArrayOutputStream)out).toByteArray();
 		}
 
 		public Packet250CustomPayload toPacket() {
 			return new Packet250CustomPayload(CHANNEL_ID, toByteArray());
 		}
 
 
 
 		public void sendToServer() {
 			PacketDispatcher.sendPacketToServer(toPacket());
 		}
 
 		public void sendToAllPlayers() {
 			PacketDispatcher.sendPacketToAllPlayers(toPacket());
 		}
 
 		public void sendTo(EntityPlayer player) {
 			PacketDispatcher.sendPacketToPlayer( toPacket(), (Player) player );
 		}
 
 		public void sendToAllInDimension(int dimensionId) {
 			PacketDispatcher.sendPacketToAllInDimension( toPacket(), dimensionId);
 		}
 
 		public void sendToAllAround(double x, double y, double z, double range, int dimensionId) {
 			PacketDispatcher.sendPacketToAllAround(x, y, z, range, dimensionId, toPacket());
 		}
 	}
 
 	public static class TileEntityPacketStream extends PacketStream {
 		public TileEntityPacketStream(byte type, TileEntity tileEntity) throws IOException {
 			super(type);
 
 			writeInt(tileEntity.worldObj.getWorldInfo().getDimension()); // Not really needed, but let's stick it here for good measure
 			writeInt(tileEntity.xCoord);
 			writeShort(tileEntity.yCoord);
 			writeInt(tileEntity.zCoord);
 		}
 	}
 
 	@Override
 	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
 		final EntityPlayer notchPlayer = (EntityPlayer) player;
 
 		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.data));
 
 		try {
 			final byte type = dis.readByte();
 			if (type < 0) {
 				final int dimension = dis.readInt(); // Not really needed, but let's stick it here for good measure
 				assert(notchPlayer.dimension == dimension);
 				final World world = notchPlayer.worldObj;
 				final int x = dis.readInt();
 				final int y = dis.readShort();
 				final int z = dis.readInt();
 
 				final TileEntity te = world.getBlockTileEntity(x, y, z);
 				if (te == null)
 					throw new RuntimeException("CCNoise TileEntity payload package sent to block without TileEntity!");
 
 				if (!(te instanceof PacketHandler))
 					throw new RuntimeException("CCNoise TileEntity payload package sent to non-PacketHandler TileEntity!");
 
 				((PacketHandler) te).handlePacket(type, manager, dis, player);
 			}
 			else {
 				final PacketHandler packetHandler = packetHandlers.get(type);
 				if (packetHandler == null)
 					throw new RuntimeException("Unhandled CCNoise plain payload package!");
 
 				packetHandler.handlePacket(type, manager, dis, player);
 			}
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	private static final Map<Byte, SoundSystemUtils> packetHandlers = new HashMap<Byte, SoundSystemUtils>();
 	public static byte registerPayloadPacket(int type, SoundSystemUtils instance) {
 		packetHandlers.put((byte) type, instance);
 
 		return (byte) type;
 	}
 }
