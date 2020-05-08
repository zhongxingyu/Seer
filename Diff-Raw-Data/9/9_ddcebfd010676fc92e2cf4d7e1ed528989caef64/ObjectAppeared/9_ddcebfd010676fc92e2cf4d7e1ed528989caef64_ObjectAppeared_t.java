 package com.friendlyblob.mayhemandhell.server.network.packets.server;
 
 import com.friendlyblob.mayhemandhell.server.model.GameObject;
 import com.friendlyblob.mayhemandhell.server.model.actors.GameCharacter;
import com.friendlyblob.mayhemandhell.server.model.actors.Player;
 import com.friendlyblob.mayhemandhell.server.model.instances.ItemInstance;
 import com.friendlyblob.mayhemandhell.server.network.packets.ServerPacket;
 
 public class ObjectAppeared extends ServerPacket {
 	private GameObject object;
 	
 	public ObjectAppeared(GameObject object) {
 		this.object = object;
 	}
 	
 	@Override
 	protected void write() {
 		writeC(0x03);
 		writeD(object.getObjectId());
 		writeD((int) object.getPosition().getX());
 		writeD((int) object.getPosition().getY());
 		writeD(object.getType().value);
 		
 		// ObjectGameType specific
 		switch (object.getType()) {
 			case PLAYER:
				writeD(((Player) object).getCharId());
				writeC(((Player) object).getHint(getClient().getPlayer()).value);
				break;
 			case FRIENDLY_NPC:
				writeD(((GameCharacter) object).getSprite());
				writeC(((GameCharacter) object).getHint(getClient().getPlayer()).value);
				break;
 			case HOSTILE_NPC:
 				writeD(((GameCharacter) object).getSprite());
 				writeC(((GameCharacter) object).getHint(getClient().getPlayer()).value);
 				break;
 			case ITEM:
 				writeD(((ItemInstance) object).getItemId());
 				break;
 		}
 	}
 }
