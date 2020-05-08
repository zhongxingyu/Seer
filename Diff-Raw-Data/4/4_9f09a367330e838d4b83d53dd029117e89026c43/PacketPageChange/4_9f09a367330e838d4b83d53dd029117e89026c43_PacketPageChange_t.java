 package demonmodders.Crymod.Common.Network;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 import net.minecraft.src.Container;
 import net.minecraft.src.EntityPlayer;
 
 import cpw.mods.fml.common.network.Player;
 import demonmodders.Crymod.Common.Inventory.ContainerSummoner;
 
 public class PacketPageChange extends CrymodPacket {
 
 	private int page;
 	private int windowId;
 	
 	public PacketPageChange() {}
 	
 	public PacketPageChange(int page, Container container) {
 		this.page = page;
 		this.windowId = container.windowId;
 	}
 	
 	@Override
 	void writeData(DataOutput out) throws IOException {
 		out.writeByte(windowId);
 		out.writeByte(page);
 	}
 
 	@Override
 	void readData(DataInput in) throws IOException {
 		windowId = in.readByte();
 		page = in.readByte();
 	}
 
 	@Override
 	void execute(EntityPlayer player) {
		if (player.inventoryContainer.windowId == windowId && player.inventoryContainer instanceof ContainerSummoner) {
			((ContainerSummoner)player.inventoryContainer).setCurrentPage(page);
 		}
 	}
 
 }
