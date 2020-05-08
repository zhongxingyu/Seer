 package messages;
 
 import java.util.HashMap;
 
 public class GameListMessage extends Message {
 
 	private static final long serialVersionUID = 3145114793474299877L;
 
 	GameListMessage(HashMap<Integer,String> list){
 		this.list = list;
 	}
 	
 	HashMap<Integer,String> list;
 }
