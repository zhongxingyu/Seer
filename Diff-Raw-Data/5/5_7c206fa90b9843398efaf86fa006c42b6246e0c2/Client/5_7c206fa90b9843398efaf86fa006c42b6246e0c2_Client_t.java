 package fr.eurecom.messaging;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.net.wifi.p2p.WifiP2pInfo;
 import android.util.Log;
 import fr.eurecom.cardify.Game;
 import fr.eurecom.util.Card;
 
 public class Client {
 
 	private String id;
 	private WifiP2pInfo info;
	private Game game;
 	
 	public Client(WifiP2pInfo info) {
 		this.info = info;
		this.game = null;
 	}
 	
 	public void publishTakeCardFromPublicZone(Card card){
 		sendMessage(Action.REMOVED_CARD_FROM_PUBLIC_ZONE, card.toString());
 	}
 	
 	public void publishPutCardInPublicZone(Card card){
 		sendMessage(Action.ADDED_CARD_TO_PUBLIC_ZONE, card.toString());
 	}
 	
 	
 	private void sendMessage(Action action, String subject) {
 		ActionMessage message = new ActionMessage(this.id, action, subject);
 		Sender.send(message, info.groupOwnerAddress);
 	}
 	
 	
 	
 	
 	public void receiveMessage(JSONObject json){
 		try {
 			String sender = json.getString("sender");
 			Action action = Action.values()[json.getInt("action")];
 			String subject = json.getString("subject");
 			parseMessage(new ActionMessage(sender, action, subject));
 		} catch (JSONException e){
 			Log.e("ClientInterpreter:receiveMessage", e.getMessage());
 		}
 	}
 	
 	private void parseMessage(ActionMessage message){
 		
 		if (this.game == null) {
 			if (message.getAction().equals(Action.GAME_STARTED)){
 				handleGameStarted(message);
 			} else {
 				return;
 			}
 		}
 		
 		switch (message.getAction()){
 		case ADDED_CARD_TO_PUBLIC_ZONE:
 			handleNewCardInPublicZone(message);
 			return;
 		case REMOVED_CARD_FROM_PUBLIC_ZONE:
 			return;
 		case ILLEGAL_ACTION:
 			return;
 		default:
 			return;
 		}
 	}
 	
 	private void handleNewCardInPublicZone(ActionMessage message) {
 		char suit = message.getSubject().charAt(0);
 		int face = Integer.parseInt(message.getSubject().substring(1));
 		//TODO:
 		/*
 		 * Make receiver method in game instance to handle such events
 		 */
 		return;
 	}
 	
 	private void handleGameStarted(ActionMessage message){
 		//TODO:
 		/*
 		 * Start new game and set local game variable in this interpreter to game instance
 		 */
 	}
 	
 }
