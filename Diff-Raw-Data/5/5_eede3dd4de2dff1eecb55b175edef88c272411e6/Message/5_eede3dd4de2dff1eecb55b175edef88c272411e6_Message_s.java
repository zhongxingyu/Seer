 package uk.codingbadgers.bFundamentals.message;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.Validate;
 import org.bukkit.Achievement;
 import org.bukkit.ChatColor;
 import org.bukkit.inventory.ItemStack;
 
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 
 public class Message {
 
 	private String text;
 	private ChatColor color;
 	private List<ChatColor> styles;
 	private List<Message> extra;
 	private ClickEvent clickevent;
 	private HoverEvent hoverevent;
 	
 	public Message() {
 		this("");
 	}
 	
 	public Message(String text) {
 		this.text = text;
 		
 		this.color = null;
 		this.styles = new ArrayList<ChatColor>();
 		this.extra = new ArrayList<Message>();
 	}
 	
 	public void setColor(ChatColor color) {
 		Validate.isTrue(color.isColor(), "Cannot set color to a non color");
 		this.color = color;
 	}
 
 	public ChatColor getColor() {
 		return this.color;
 	}
 	
 	public void addStyle(ChatColor color) {
 		Validate.isTrue(color.isFormat() || color == ChatColor.RESET, "Cannot add a color as a style");
 		
 		if (color == ChatColor.RESET) {
 			styles.clear();
 			this.color = ChatColor.WHITE;
 			return;
 		}
 		
 		styles.add(color);
 	}
 
 	public boolean hasStyle(ChatColor style) {
 		Validate.isTrue(style.isFormat(), "A format is needed not a color");
 		return styles.contains(style);
 	}
 	
 	public void addClickEvent(ClickEventType type, String value) {
 		addClickEvent(new ClickEvent(type, value));
 	}
 	
 	public void addClickEvent(ClickEvent event) {
 		this.clickevent = event;
 	}
 	
 	public void addItemTooltip(ItemStack stack) {
 		addHoverEvent(new HoverEvent(HoverEventType.SHOW_ITEM, stack));
 	}
 
 
 	public void addAchivementTooltip(Achievement achivement) {
 		addHoverEvent(new HoverEvent(HoverEventType.SHOW_ACHIEVEMENT, achivement));
 	}
 
 	public void addHoverEvent(HoverEventType type, String value) {
 		addHoverEvent(new HoverEvent(type, value));
 	}
 	
 	public void addHoverEvent(HoverEvent event) {
 		this.hoverevent = event;
 	}
 
 	public void addExtra(Message message) {
 		if (message.getColor() == null) {
 			message.setColor(this.getColor());
 		}
 		
 		this.extra.add(message);
 	}
 
 	@Override
 	public String toString() {
 		return "Message [text=" + text + ", color=" + color + ", styles=" + styles + ", extra=" + extra + ", clickevent=" + clickevent + ", hoverevent=" + hoverevent + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((clickevent == null) ? 0 : clickevent.hashCode());
 		result = prime * result + ((color == null) ? 0 : color.hashCode());
 		result = prime * result + ((extra == null) ? 0 : extra.hashCode());
 		result = prime * result + ((hoverevent == null) ? 0 : hoverevent.hashCode());
 		result = prime * result + ((styles == null) ? 0 : styles.hashCode());
 		result = prime * result + ((text == null) ? 0 : text.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return obj instanceof Message && obj.hashCode() == hashCode();
 	}
 	
 	public static class MessageSerializer implements JsonSerializer<Message>, JsonDeserializer<Message> {
 
 		@Override
 		public Message deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
 			if (json == null || !json.isJsonObject()) {
 				return new Message();
 			}
 			
 			JsonObject object = (JsonObject) json;
 			
 			Message message = new Message();
 			
 			if (object.has("text") && object.get("text").isJsonPrimitive()) {
 				message.text = object.get("text").getAsString();
 			}
 			
 			if (object.has("color") && object.get("color").isJsonPrimitive()) {
 				message.color = ChatColor.valueOf(object.get("color").getAsString().toUpperCase());
 			}
 			
 			if (object.has("clickEvent") && object.get("clickEvent").isJsonObject()) {
 				message.clickevent = (ClickEvent) context.deserialize(object.get("clickEvent"), ClickEvent.class);
 			}
 
 			if (object.has("hoverEvent") && object.get("hoverEvent").isJsonObject()) {
 				message.hoverevent = (HoverEvent) context.deserialize(object.get("hoverEvent"), HoverEvent.class);
 			}
 			
 			if (object.has("bold") && object.get("bold").getAsBoolean()) { message.styles.add(ChatColor.BOLD); }
 			if (object.has("italic") && object.get("italic").getAsBoolean()) { message.styles.add(ChatColor.ITALIC); }
			if (object.has("underline") && object.get("underline").getAsBoolean()) { message.styles.add(ChatColor.UNDERLINE); }
 			if (object.has("strikethrough") && object.get("strikethrough").getAsBoolean()) { message.styles.add(ChatColor.STRIKETHROUGH); }
 			if (object.has("obfuscated") && object.get("obfuscated").getAsBoolean()) { message.styles.add(ChatColor.MAGIC); }
 			
 			if (object.has("extra") && object.get("extra").isJsonArray()) {
 				for (JsonElement item : object.get("extra").getAsJsonArray()) {
 					message.extra.add((Message) context.deserialize(item, Message.class));
 				}
 			}
 			
 			return message;
 		}
 
 		@Override
 		public JsonElement serialize(Message msg, Type type, JsonSerializationContext context) {
 			JsonObject json = new JsonObject();
 
 			if (msg == null) {
 				return json;
 			}
 			
 			json.addProperty("text", msg.text);
 			
 			if (msg.color != null) { json.addProperty("color", msg.color.name().toLowerCase()); }
 			
 			if (msg.styles.contains(ChatColor.BOLD)) { json.addProperty("bold", true); }
 			if (msg.styles.contains(ChatColor.ITALIC)) { json.addProperty("italic", true); }
			if (msg.styles.contains(ChatColor.UNDERLINE)) { json.addProperty("underline", true); }
 			if (msg.styles.contains(ChatColor.STRIKETHROUGH)) { json.addProperty("strikethrough", true); }
 			if (msg.styles.contains(ChatColor.MAGIC)) { json.addProperty("obfuscated", true); }
 			
 			if (!msg.extra.isEmpty()) { json.add("extra", context.serialize(msg.extra)); }
 			
 			if (msg.clickevent != null) { json.add("clickEvent", context.serialize(msg.clickevent)); }
 			if (msg.hoverevent != null) { json.add("hoverEvent", context.serialize(msg.hoverevent)); }
 			
 			return json;
 		}
 		
 	}
 }
