 package game.net.packets;
 
 import initial3d.engine.Vec3;
 
 import java.awt.image.BufferedImage;
 
 import common.DataPacket;
 import game.ItemType;
 import game.ItemType;
 import game.item.Item;
 import game.item.Key;
 import game.modelloader.Content;
 
 public class ItemLifePacket extends Packet {
 
 	public static final short ID = 19871;
 	
 	public ItemLifePacket() {
 		super(ID);
 	}
 	
 	public long itemID;
 	public ItemType itemType;
 	public Vec3 position;
 	
 	private int mode = 0;
 	
 
 	@Override
 	public void fromData(DataPacket packet)
 	{
 		if(packet.getShort() != ID)
 			return;
 		
 		this.itemID = packet.getLong();
 		this.itemType = ItemType.values()[packet.getShort()];
 		this.position = Vec3.create(packet.getDouble(), packet.getDouble(), packet.getDouble());
 		this.mode = (int)packet.getByte();
 	}
 
 	@Override
 	public DataPacket toData() {
 		DataPacket dp = new DataPacket();
 		dp.addShort(ID);
 		dp.addLong(itemID);
 		dp.addShort(itemType.ordinal());
 		
 		dp.addDouble(position.x);
 		dp.addDouble(position.y);
 		dp.addDouble(position.z);
 		
 		dp.addByte((byte)mode);
 		
 		return dp;
 	}
 	
 	public boolean isCreate() {
 		return this.mode == 0;
 	}
 	
 	public void setCreate() {
 		this.mode = 0;
 	}
 	
 	public boolean isDestroy() {
 		return this.mode == 1;
 	}
 	
 	public void setDestroy() {
 		this.mode = 1;
 	}
 	
 	public Item getItem()
 	{
 		switch(this.itemType)
 		{
 			case Key:
 				return new Key(this.itemID, this.position);
 			case Battery:
 				return new Battery(this.itemID, this.position);
 			default:
 				return null;
 		}
 	}
 }
