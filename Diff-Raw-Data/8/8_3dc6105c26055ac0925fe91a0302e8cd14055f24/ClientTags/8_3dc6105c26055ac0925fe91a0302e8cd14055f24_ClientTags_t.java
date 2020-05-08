 package client;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import shared.HTSMsg;
 
 public class ClientTags {
 	/**
	 * required
 	 */
 	public final static String TAGID = "tagId";
 	
 	/**
	 * required
 	 */
 	public final static String TAGNAME = "tagName";
 	
 	/**
 	 * optional
 	 */
 	public final static String TAGICON = "tagIcon";
 	
 	/**
 	 * optional
 	 */
 	public final static String TAGTITLEICON = "tagTitledIcon";
 	
 	/**
 	 * optional
 	 */
 	public final static String MEMBERS = "members";
 	
 	public static final String[] HTSMsgFields = {TAGID,TAGNAME,TAGICON,TAGTITLEICON,MEMBERS};
 	
 	private Map<Long, HTSMsg> tags;
 	
 	public ClientTags() {
 		this.tags = new HashMap<Long, HTSMsg>();
 	}
 	
 	/**
 	 * Adds a channel.
 	 * @param msg
 	 */
 	public void add(HTSMsg msg){
 		tags.put(((Number)msg.get(TAGID)).longValue(), msg);
 	}
 	
 	/**
 	 * Updates a channel.
 	 * @param reply
 	 */
 	public void update(HTSMsg msg) {
 		HTSMsg chann = tags.get(((Number)msg.get(TAGID)).longValue());			
 		
 		for (String name : msg.keySet()){
 			if (Arrays.asList(HTSMsgFields).contains(name)){
 				chann.put(name, msg.get(name));
 			}
 			else if (name.equals("method")){
 				//This is normal, do nothing...
 			}
 			else{
 				System.out.println("N.B. Unrecognized field: " + name);
 			}
 		}		
 	}
 	
 	public void remove(HTSMsg msg){
		tags.remove(((Number)msg.get(TAGID)).longValue());
 	}
 }
