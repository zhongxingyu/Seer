 package net.underrealm.packets;
 
 public class PacketHandler {


 	@SuppressWarnings("rawtypes")
 	public PacketHandler(Class packetClass, int i) {
		this.handlePacket(packetClass);
 	}

	public PacketHandler handlePacket(Class pclass) {
		if(pclass.equals(LoginPacket0.class)) {
			return LoginPacket0.handler();
		} return null;
 	}
 	
 	static {
 		PacketHandler LoginPacket0 = new PacketHandler(LoginPacket0.class, 0);
 	}


 }
