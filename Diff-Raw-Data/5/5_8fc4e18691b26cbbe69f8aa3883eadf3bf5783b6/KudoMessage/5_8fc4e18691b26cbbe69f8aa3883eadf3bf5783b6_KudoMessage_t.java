 package se.kudomessage.jessica;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class KudoMessage {
 	protected String id, 
 		content, 
 		origin;
 	protected ArrayList<String> receivers;
 	
 	public KudoMessage(){
 		//Empty message
		this.receivers = new ArrayList<String>();
 	}
 
 	public KudoMessage(String content, String origin, String receiver, String id){
 		this(content, origin, receiver);
 		this.id = id;
 	}
 	
 	public KudoMessage(String content, String origin, String receiver){
		this();
 		this.content = content;
 		this.origin = origin;
 		this.receivers.add(receiver);
 	}
 	
 	public KudoMessage(String id){
		this();
 		this.id = id;
 	}	
 	
 	public String getFirstReceiver(){
 		return receivers.get(0);
 	}
 	
 	public void addReceiver(String receiver){
 		this.receivers.add(receiver);
 	}
 	
 	
 	public String toString(){
 		return toJSON().toString();
 	}
 
 	public JSONObject toJSON() {
 		JSONObject json = new JSONObject();
 		
 		try {
 			json.put("protocol", "SMS");
 			json.put("id", this.id);
 			json.put("origin", this.origin);
 			json.put("content", this.content);
 			
 			JSONArray rl = new JSONArray();
 			for( String r : receivers){
 				rl.put(r);
 			}
 			
 			json.put("receivers", rl);
 			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return json;
 	}
 }
