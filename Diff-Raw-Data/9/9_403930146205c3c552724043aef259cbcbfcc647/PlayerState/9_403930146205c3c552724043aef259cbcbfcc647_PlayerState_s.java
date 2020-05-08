 package edu.vub.at.nfcpoker;
 
 import com.esotericsoftware.kryonet.Connection;
 
 import edu.vub.at.nfcpoker.comm.Message.ClientActionType;
 
public class PlayerState {
 	// Connection
 	public volatile Connection connection;
 	public volatile int clientId;
 	
 	// Player statistics
 	public volatile int money;
 	public volatile String name;
 	public volatile int avatar;
 	
 	// Game-specific
 	public volatile int gameMoney;
 	public volatile Card[] gameHoleCards;
 	
 	// Round-specific (roundTable)
 	public volatile ClientActionType roundActionType;
 	public volatile int roundMoney;
 	
 	public PlayerState(Connection connection, int clientId, int money, String name, int avatar) {
 		this.connection = connection;
 		this.clientId = clientId;
 		
 		this.money = money;
 		this.name = name;
 		this.avatar = avatar;
 		
 		this.gameMoney = 0;
 		this.gameHoleCards = null;
 		
 		this.roundActionType = ClientActionType.Unknown;
 		this.roundMoney = 0;
 	}
 	
 	public String toString() {
 		return "Player ("+clientId+"): "+name+" - "+money+" - "+avatar+" - "+
 				gameMoney+" - "+gameHoleCards+" - "+
 				roundActionType+" - "+roundMoney;
 	}
 }
