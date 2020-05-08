 package btwmod.centralchat.struct;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 public class User extends Username {
 	public final String gateway;
 	
 	public User(String username) {
		this(username, null);
 	}
 	
	public User(String username, String gateway) {
 		super(username);
 		this.gateway = gateway;
 	}
 	
 	public User(JsonObject json) {
 		super(json);
 		JsonElement gateway = json.get("gateway");
 		this.gateway = gateway != null && gateway.isJsonPrimitive() ? gateway.getAsString() : null;
 	}
 	
 	public JsonObject toJson() {
 		JsonObject json = super.toJson();
 		
 		if (gateway != null)
 			json.addProperty("gateway", gateway);
 		
 		return json;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((gateway == null) ? 0 : gateway.toLowerCase().hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		
 		if (getClass() != obj.getClass())
 			return false;
 		
 		if (!super.equals(obj))
 			return false;
 		
 		User other = (User)obj;
 		if (gateway == null) {
 			if (other.gateway != null) {
 				return false;
 			}
 		}
 		else if (!gateway.equalsIgnoreCase(other.gateway)) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	
 }
