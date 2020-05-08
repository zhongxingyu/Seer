 package server.game.players.packets;
 
 
 import server.game.players.Client;
 import server.game.players.PacketType;
 
 
 public class IdleLogout implements PacketType {
 
 	@Override
 	public void processPacket(Client c, int packetType, int packetSize) {
		if (c.playerRights >= 2) {
		    //do nothing hahahah
		} else {
 		c.logout();
 	    //TODO make a idle timer
		}
 	}
 }
