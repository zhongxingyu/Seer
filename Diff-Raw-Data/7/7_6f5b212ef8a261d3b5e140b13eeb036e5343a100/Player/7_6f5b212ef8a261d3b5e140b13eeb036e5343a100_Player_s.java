 package cs309.a1.shared;
 
 import static cs309.a1.crazyeights.Constants.ID;
 import static cs309.a1.crazyeights.Constants.RESOURCE_ID;
 import static cs309.a1.crazyeights.Constants.SUIT;
 import static cs309.a1.crazyeights.Constants.VALUE;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 public class Player {
 
 	private List<Card> cards;
 	private String name;
 	private String id;
 
 	public Player() {
 		this.cards = new ArrayList<Card>();
 		this.name = null;
 		this.id = null;
 	}
 
 	public List<Card> getCards() {
 		return cards;
 	}
 
 	public int getNumCards() {
 		return cards.size();
 	}
 
 	public void addCard(Card card){
 		cards.add(card);
 	}
 
 	public void removeCard(Card card){
		cards.remove(card);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public String toString() {
 		// Encode the cards into a JSONArray
 		try {
 			JSONArray arr = new JSONArray();
 			for (Card c : cards) {
 				JSONObject obj = new JSONObject();
 				obj.put(SUIT, c.getSuit());
 				obj.put(VALUE, c.getValue());
 				obj.put(RESOURCE_ID, c.getResourceId());
 				obj.put(ID, c.getIdNum());
 
 				arr.put(obj);
 			}
 
 			return arr.toString();
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return "";
 		}
 	}
 }
