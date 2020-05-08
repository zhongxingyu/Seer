 package denoflionsx.DenPipes.AddOns.Forestry.gui;
 
 import cpw.mods.fml.common.network.IGuiHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import denoflionsx.DenPipes.AddOns.Forestry.net.Packets;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 public class GuiHandler implements IGuiHandler {
 
     @Override
     public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
         switch (ID) {
             case 0:
                 TileEntity t = world.getBlockTileEntity(x, y, z);
                 return new ContainerForestryPipe(t, player);
         }
         return null;
     }
 
     @Override
     public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
         switch (ID) {
             case 0:
                 TileEntity t = world.getBlockTileEntity(x, y, z);
                Packet250CustomPayload packet = Packets.Wrapper.createPacket(Packets.packet_sync, new Object[]{x, y, z, true});
                PacketDispatcher.sendPacketToServer(packet);
                 return new GuiForestryPipe(new ContainerForestryPipe(t, player));
         }
         return null;
     }
 }
