 package btwmod.centralchat.message;
 
 import btwmod.centralchat.ClientType;
 import btwmod.centralchat.IServer;
 import btwmod.centralchat.ResourceConfig;
 import btwmods.Util;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 public abstract class MessageUser extends Message {
 	
 	public final String username;
 	public final String gateway;
 	public final String color;
 	public final String alias;
 	
 	public MessageUser(String username, String gateway, String color, String alias) {
 		this.username = username;
 		this.gateway = gateway;
 		this.color = color;
 		this.alias = alias;
 	}
 	
 	public MessageUser(JsonObject json) {
 		this.username = json.get("username").getAsString();
 		
 		JsonElement gateway = json.get("gateway");
 		this.gateway = gateway != null && gateway.isJsonPrimitive() ? gateway.getAsString() : null;
 		
 		JsonElement color = json.get("color");
 		this.color = color != null && color.isJsonPrimitive() ? color.getAsString() : null;
 
 		JsonElement alias = json.get("alias");
 		this.alias = alias != null && alias.isJsonPrimitive() ? alias.getAsString() : null;
 	}
 
 	@Override
 	public JsonObject toJson() {
 		JsonObject obj = super.toJson();
 		obj.addProperty("username", this.username);
 		
 		if (this.gateway != null)
 			obj.addProperty("gateway", this.gateway);
 		
 		if (this.color != null)
 			obj.addProperty("color", this.color);
 		
 		if (this.alias != null)
 			obj.addProperty("alias", this.alias);
 		
 		return obj;
 	}
 	
 	@Override
 	public JsonObject toJsonCleaned(IServer server, ResourceConfig config) {
 		JsonObject json = super.toJsonCleaned(server, config);
		String username = server.getActualUsername(config.clientType == ClientType.USER ? config.id : this.username);
 		
 		// Force user ID for those authenticated as users.
		json.addProperty("username", username);
 		
 		// USER clients should not have a gateway.
 		if (config.clientType == ClientType.USER && json.has("gateway"))
 			json.remove("gateway");
 		
 		// Set the user's chat color, if it has one.
 		json.addProperty("color", server.getChatColor(username));
 		if (json.get("color").isJsonNull())
 			json.remove("color");
 		
 		// Set the user's alias, if it has one.
 		json.addProperty("alias", server.getChatAlias(username));
 		if (json.get("alias").isJsonNull())
 			json.remove("alias");
 		
 		return json;
 	}
 	
 	public String getDisplayUsername(boolean withColor) {
 		return getDisplayUsername(withColor, Util.COLOR_RESET);
 	}
 	
 	public String getDisplayUsername(boolean withColor, String resetColorChar) {
 		String displayName = alias == null ? username : alias;
 		if (!withColor)
 			return displayName;
 		
 		String colorChar = Message.getColorChar(color);
 		return colorChar == null ? displayName : colorChar + displayName + resetColorChar;
 	}
 }
